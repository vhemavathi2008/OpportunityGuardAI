package com.opportunityguard.service;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.opportunityguard.gemini.GeminiClient;
import com.opportunityguard.model.Opportunity;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Optional;

/** Turns emails into structured opportunities and detects submission confirmations using Gemini. */
public class GeminiService {

    private static final int MAX_BODY_CHARS = 6000;

    private final GeminiClient client = new GeminiClient();

    public boolean isConfigured() {
        return client.isConfigured();
    }

    /**
     * Analyses an email and returns an Opportunity when the email announces
     * a student opportunity.
     */
    public Optional<Opportunity> analyze(
            String sender,
            String subject,
            String content
    ) throws IOException, InterruptedException {

        String body = content == null ? "" : content;

        if (body.length() > MAX_BODY_CHARS) {
            body = body.substring(0, MAX_BODY_CHARS);
        }

        String prompt =
                "You analyse emails for a student opportunity tracker. Today's date is "
                        + LocalDate.now() + ".\n"
                        + "Decide whether the email announces an opportunity a student could apply for: "
                        + "hackathon, internship, job, scholarship, competition or workshop. "
                        + "Newsletters, receipts, OTPs, social notifications and personal mail are NOT opportunities.\n"
                        + "Respond with ONE JSON object using exactly these keys:\n"
                        + "{\"is_opportunity\": boolean, \"title\": string, \"organization\": string, "
                        + "\"type\": one of [Hackathon, Internship, Job, Scholarship, Competition, Workshop, Other], "
                        + "\"description\": string (max 400 characters), \"deadline\": \"YYYY-MM-DD\" or null, "
                        + "\"registration_link\": URL string or null, \"eligibility\": string or null, "
                        + "\"trust_verdict\": one of [Likely Legitimate, Suspicious, Unverified], "
                        + "\"trust_confidence\": integer 0-100}\n"
                        + "Rules: use only facts present in the email and never invent values; "
                        + "use null when unknown. "
                        + "trust_verdict reflects scam signals inside the email itself "
                        + "(fees or payment requests, artificial urgency, mismatched sender and organisation, "
                        + "odd or shortened links). "
                        + "trust_confidence is your own confidence in that verdict and is not a measured accuracy.\n\n"
                        + "Sender: " + nz(sender)
                        + "\nSubject: " + nz(subject)
                        + "\nBody:\n" + body;

        String raw = client.generateJson(prompt).trim();

        raw = cleanJson(raw);

        JsonObject o;

        try {
            o = JsonParser.parseString(raw).getAsJsonObject();
        } catch (RuntimeException e) {
            throw new IOException(
                    "Gemini returned text that is not valid JSON."
            );
        }

        if (!o.has("is_opportunity")
                || !o.get("is_opportunity").isJsonPrimitive()
                || !o.get("is_opportunity").getAsBoolean()) {

            return Optional.empty();
        }

        String title = str(o, "title");

        if (title == null || title.isBlank()) {
            return Optional.empty();
        }

        Opportunity opp = new Opportunity();

        opp.setTitle(cut(title, 255));
        opp.setOrganization(cut(str(o, "organization"), 255));

        String type = str(o, "type");

        opp.setType(
                type != null && Opportunity.TYPES.contains(type)
                        ? type
                        : "Other"
        );

        opp.setDescription(
                cut(str(o, "description"), 2000)
        );

        opp.setEligibility(
                cut(str(o, "eligibility"), 2000)
        );

        String deadline = str(o, "deadline");

        if (deadline != null) {
            try {
                opp.setDeadline(
                        LocalDate.parse(deadline.trim())
                );
            } catch (DateTimeParseException ignored) {
                opp.setDeadline(null);
            }
        }

        String link = str(o, "registration_link");

        if (link != null
                && (link.startsWith("http://")
                || link.startsWith("https://"))) {

            opp.setRegistrationLink(
                    cut(link, 1000)
            );
        }

        String verdict = str(o, "trust_verdict");

        opp.setTrustVerdict(
                verdict != null
                        && Opportunity.VERDICTS.contains(verdict)
                        ? verdict
                        : "Unverified"
        );

        if (o.has("trust_confidence")
                && o.get("trust_confidence").isJsonPrimitive()) {

            try {
                opp.setTrustConfidence(
                        Math.max(
                                0,
                                Math.min(
                                        100,
                                        o.get("trust_confidence").getAsInt()
                                )
                        )
                );
            } catch (RuntimeException ignored) {
                opp.setTrustConfidence(null);
            }
        }

        return Optional.of(opp);
    }

    /**
     * Holds the result of automatic submission detection.
     */
    public static class SubmissionInfo {

        private final String stage;
        private final String opportunityTitle;
        private final String organization;

        public SubmissionInfo(
                String stage,
                String opportunityTitle,
                String organization
        ) {
            this.stage = stage;
            this.opportunityTitle = opportunityTitle;
            this.organization = organization;
        }

        public String getStage() {
            return stage;
        }

        public String getOpportunityTitle() {
            return opportunityTitle;
        }

        public String getOrganization() {
            return organization;
        }
    }

    /**
     * Detects whether an email confirms a submission to an existing opportunity.
     *
     * Possible stages:
     * IDEA_SUBMITTED
     * PPT_SUBMITTED
     * DEMO_SUBMITTED
     * FINAL_SUBMITTED
     * NOT_SUBMISSION
     */
    public SubmissionInfo detectSubmissionStage(
            String sender,
            String subject,
            String content
    ) throws IOException, InterruptedException {

        String body = content == null ? "" : content;

        if (body.length() > MAX_BODY_CHARS) {
            body = body.substring(0, MAX_BODY_CHARS);
        }

        String prompt =
                "You analyse emails for a student opportunity tracker.\n"
                        + "Determine whether this email confirms a student's submission "
                        + "to an existing opportunity.\n\n"

                        + "Possible stages are exactly:\n"
                        + "IDEA_SUBMITTED\n"
                        + "PPT_SUBMITTED\n"
                        + "DEMO_SUBMITTED\n"
                        + "FINAL_SUBMITTED\n"
                        + "NOT_SUBMISSION\n\n"

                        + "Return ONE JSON object using exactly these keys:\n"
                        + "{\"is_submission\": boolean, "
                        + "\"stage\": string, "
                        + "\"opportunity_title\": string or null, "
                        + "\"organization\": string or null}\n\n"

                        + "Rules:\n"
                        + "- Use only facts present in the email.\n"
                        + "- Do not invent the opportunity title or organization.\n"
                        + "- Registration confirmation is NOT a submission.\n"
                        + "- Application confirmation is NOT automatically a submission.\n"
                        + "- Idea submission confirmation = IDEA_SUBMITTED.\n"
                        + "- PPT or presentation submission confirmation = PPT_SUBMITTED.\n"
                        + "- Demo submission confirmation = DEMO_SUBMITTED.\n"
                        + "- Final submission confirmation = FINAL_SUBMITTED.\n"
                        + "- If this is not a submission confirmation, use NOT_SUBMISSION.\n\n"

                        + "Sender: " + nz(sender)
                        + "\nSubject: " + nz(subject)
                        + "\nBody:\n" + body;

        String raw = client.generateJson(prompt).trim();

        raw = cleanJson(raw);

        try {
            JsonObject o =
                    JsonParser.parseString(raw).getAsJsonObject();

            if (!o.has("is_submission")
                    || !o.get("is_submission").isJsonPrimitive()
                    || !o.get("is_submission").getAsBoolean()) {

                return new SubmissionInfo(
                        "NOT_SUBMISSION",
                        null,
                        null
                );
            }

            String stage = str(o, "stage");

            if (!"IDEA_SUBMITTED".equals(stage)
                    && !"PPT_SUBMITTED".equals(stage)
                    && !"DEMO_SUBMITTED".equals(stage)
                    && !"FINAL_SUBMITTED".equals(stage)) {

                return new SubmissionInfo(
                        "NOT_SUBMISSION",
                        null,
                        null
                );
            }

            String title =
                    str(o, "opportunity_title");

            String organization =
                    str(o, "organization");

            return new SubmissionInfo(
                    stage,
                    title,
                    organization
            );

        } catch (RuntimeException e) {

            throw new IOException(
                    "Gemini returned invalid submission JSON."
            );
        }
    }

    private static String cleanJson(String raw) {

        if (raw == null) {
            return "";
        }

        raw = raw.trim();

        if (raw.startsWith("```")) {
            raw = raw
                    .replaceFirst(
                            "^```[a-zA-Z]*",
                            ""
                    )
                    .replaceFirst(
                            "```\\s*$",
                            ""
                    )
                    .trim();
        }

        return raw;
    }

    private static String nz(String s) {
        return s == null ? "" : s;
    }

    private static String str(
            JsonObject o,
            String key
    ) {

        JsonElement e = o.get(key);

        return (e == null
                || e.isJsonNull()
                || !e.isJsonPrimitive())
                ? null
                : e.getAsString();
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