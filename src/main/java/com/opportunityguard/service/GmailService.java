package com.opportunityguard.service;

import com.opportunityguard.gmail.GmailApiClient;
import com.opportunityguard.gmail.GmailMessage;
import com.opportunityguard.gmail.GoogleOAuthClient;
import com.opportunityguard.model.EmailRecord;
import com.opportunityguard.model.Opportunity;
import com.opportunityguard.repository.EmailRecordRepository;
import com.opportunityguard.repository.GmailTokenRepository;
import com.opportunityguard.repository.OpportunityRepository;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

/**
 * Gmail connection + sync pipeline:
 * OAuth token -> Gmail API -> email_records -> Gemini -> opportunities.
 *
 * Also detects automatic submission stages:
 * Idea -> PPT -> Demo -> Final.
 *
 * Manual participation status remains supported separately.
 */
public class GmailService {

    /*
     * Gmail search query.
     *
     * Includes both normal opportunity emails and
     * participation/submission confirmation emails.
     */
    private static final String QUERY =
            "newer_than:30d ("
                    + "hackathon "
                    + "OR internship "
                    + "OR hiring "
                    + "OR scholarship "
                    + "OR competition "
                    + "OR contest "
                    + "OR workshop "
                    + "OR deadline "
                    + "OR \"last date\" "
                    + "OR \"register now\" "
                    + "OR \"apply now\" "
                    + "OR registration "
                    + "OR registered "
                    + "OR submission "
                    + "OR submitted "
                    + "OR \"successfully submitted\" "
                    + "OR PPT "
                    + "OR presentation "
                    + "OR demo "
                    + "OR \"final submission\""
                    + ")";

    private static final int MAX_FETCH = 20;
    private static final int MAX_ANALYZE = 20;
    private static final int MAX_STORED_CHARS = 20000;

    public static class SyncResult {

        public int listed;
        public int stored;
        public int analyzed;
        public int created;
        public int notOpportunity;
        public boolean geminiMissing;
        public String error;
    }

    private final GoogleOAuthClient oauth =
            new GoogleOAuthClient();

    private final GmailApiClient gmail =
            new GmailApiClient();

    private final GeminiService gemini =
            new GeminiService();

    private final GmailTokenRepository tokens =
            new GmailTokenRepository();

    private final EmailRecordRepository emails =
            new EmailRecordRepository();

    private final OpportunityRepository opportunityRepository =
            new OpportunityRepository();

    private final OpportunityService opportunities =
            new OpportunityService();

    private final NotificationService notifications =
            new NotificationService();

    public boolean isOAuthConfigured() {
        return oauth.isConfigured();
    }

    public boolean isGeminiConfigured() {
        return gemini.isConfigured();
    }

    public boolean isConnected(long userId) {
        return tokens.findRefreshToken(userId).isPresent();
    }

    public Timestamp lastSync(long userId) {
        return tokens.findLastSync(userId);
    }

    public String authorizationUrl(String state) {
        return oauth.authorizationUrl(state);
    }

    public void completeAuth(
            long userId,
            String code
    ) throws IOException, InterruptedException {

        GoogleOAuthClient.Tokens t =
                oauth.exchangeCode(code);

        if (t.refreshToken() == null) {

            throw new IOException(
                    "Google did not return a refresh token. Remove this app at "
                            + "myaccount.google.com/permissions and connect again."
            );
        }

        tokens.save(
                userId,
                t.refreshToken()
        );
    }

    public void disconnect(long userId) {
        tokens.delete(userId);
    }

    public SyncResult sync(long userId) {

        SyncResult r = new SyncResult();

        /*
         * Check Gmail connection for this logged-in user.
         */
        Optional<String> refresh =
                tokens.findRefreshToken(userId);

        if (refresh.isEmpty()) {

            r.error =
                    "Gmail is not connected.";

            return r;
        }

        String access;

        /*
         * Get a fresh Gmail access token.
         */
        try {

            access =
                    oauth.refreshAccessToken(
                            refresh.get()
                    );

        } catch (IOException | InterruptedException e) {

            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }

            r.error =
                    "Could not get Gmail access ("
                            + e.getMessage()
                            + "). Disconnect and connect Gmail again.";

            return r;
        }

        /*
         * =========================================================
         * STEP 1
         * Fetch Gmail messages and store them in email_records.
         * =========================================================
         */
        try {

            List<String> ids =
                    gmail.listMessageIds(
                            access,
                            QUERY,
                            MAX_FETCH
                    );

            r.listed =
                    ids.size();

            for (String id : ids) {

                /*
                 * Do not store the same Gmail message twice
                 * for the same application user.
                 */
                if (emails.existsByUserAndMessageId(
                        userId,
                        id
                )) {
                    continue;
                }

                GmailMessage m =
                        gmail.getMessage(
                                access,
                                id
                        );

                EmailRecord er =
                        new EmailRecord();

                er.setUserId(userId);

                er.setGmailMessageId(
                        m.id()
                );

                er.setSender(
                        cut(
                                m.sender(),
                                500
                        )
                );

                er.setSubject(
                        cut(
                                m.subject(),
                                500
                        )
                );

                er.setEmailContent(
                        cut(
                                m.body(),
                                MAX_STORED_CHARS
                        )
                );

                emails.insert(er);

                r.stored++;
            }

        } catch (IOException | InterruptedException e) {

            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }

            r.error =
                    e.getMessage();
        }

        /*
         * =========================================================
         * STEP 2
         * Gemini analysis.
         * =========================================================
         */
        if (!gemini.isConfigured()) {

            r.geminiMissing = true;

        } else {

            List<EmailRecord> pending =
                    emails.findUnanalyzedByUserId(
                            userId,
                            MAX_ANALYZE
                    );

            for (EmailRecord er : pending) {

                try {

                    /*
                     * -------------------------------------------------
                     * FIRST:
                     * Check whether the email is a submission
                     * confirmation for an existing opportunity.
                     *
                     * Supported automatic stages:
                     *
                     * IDEA_SUBMITTED
                     * PPT_SUBMITTED
                     * DEMO_SUBMITTED
                     * FINAL_SUBMITTED
                     * -------------------------------------------------
                     */
                    GeminiService.SubmissionInfo submission =
                            gemini.detectSubmissionStage(
                                    er.getSender(),
                                    er.getSubject(),
                                    er.getEmailContent()
                            );

                    /*
                     * If Gemini detected a submission stage,
                     * try to find the EXISTING opportunity.
                     *
                     * IMPORTANT:
                     * A submission email must NOT create
                     * a new opportunity.
                     */
                    if (!"NOT_SUBMISSION".equals(
                            submission.getStage()
                    )
                            && submission.getOpportunityTitle() != null
                            && !submission.getOpportunityTitle().isBlank()) {

                        String title =
                                submission.getOpportunityTitle();

                        String organization =
                                submission.getOrganization();

                        Optional<Opportunity> existing =
                                opportunityRepository.findForUser(
                                        userId,
                                        title,
                                        organization
                                );

                        /*
                         * Existing opportunity found.
                         */
                        if (existing.isPresent()) {

                            Opportunity o =
                                    existing.get();

                            /*
                             * Automatically mark the detected
                             * stage as completed.
                             *
                             * Manual checkbox functionality
                             * remains unchanged.
                             */
                            boolean updated =
                                    opportunities.updateAutomaticStage(
                                            userId,
                                            o.getId(),
                                            submission.getStage()
                                    );

                            if (updated) {
                                r.analyzed++;
                            }

                        } else {

                            /*
                             * Submission email belongs to an
                             * opportunity that is not currently
                             * present in this user's database.
                             *
                             * Do NOT create a new opportunity.
                             */
                            r.notOpportunity++;
                        }

                    } else {

                        /*
                         * -------------------------------------------------
                         * NORMAL OPPORTUNITY EXTRACTION
                         * -------------------------------------------------
                         *
                         * If the email is not a submission confirmation,
                         * use the existing Gemini opportunity extraction.
                         */
                        Optional<Opportunity> found =
                                gemini.analyze(
                                        er.getSender(),
                                        er.getSubject(),
                                        er.getEmailContent()
                                );

                        if (found.isPresent()) {

                            Opportunity o =
                                    found.get();

                            /*
                             * Keep the opportunity attached to
                             * the currently logged-in application user.
                             */
                            o.setUserId(userId);

                            if (opportunities.addExtracted(o)) {
                                r.created++;
                            }

                        } else {

                            r.notOpportunity++;
                        }

                        r.analyzed++;
                    }

                    /*
                     * Mark this email as analysed only after
                     * successful processing.
                     */
                    emails.markAnalyzed(
                            er.getId(),
                            userId
                    );

                } catch (IOException | InterruptedException e) {

                    if (e instanceof InterruptedException) {
                        Thread.currentThread().interrupt();
                    }

                    r.error =
                            e.getMessage();

                    /*
                     * Stop processing when Gemini/API fails.
                     *
                     * Remaining emails stay unanalysed and can
                     * be processed during the next sync.
                     */
                    break;
                }
            }
        }

        /*
         * =========================================================
         * STEP 3
         * Update last sync time and notifications.
         * =========================================================
         */
        tokens.touchLastSync(userId);

        notifications.generateDeadlineNotifications(
                userId
        );

        return r;
    }

    public List<EmailRecord> records(
            long userId,
            int limit
    ) {

        return emails.getEmailRecordsByUserId(
                userId,
                limit
        );
    }

    public int countEmails(long userId) {

        return emails.countByUserId(
                userId,
                false
        );
    }

    public int countAnalyzed(long userId) {

        return emails.countByUserId(
                userId,
                true
        );
    }

    private static String cut(
            String s,
            int max
    ) {

        if (s == null) {
            return null;
        }

        return s.length() > max
                ? s.substring(0, max)
                : s;
    }
}