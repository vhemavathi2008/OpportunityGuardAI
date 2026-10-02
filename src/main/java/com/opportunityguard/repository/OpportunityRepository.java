package com.opportunityguard.repository;

import com.opportunityguard.config.Db;
import com.opportunityguard.model.Opportunity;
import com.opportunityguard.util.DataAccessException;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Every method that touches user data requires the user id and filters on it. */
public class OpportunityRepository {

    private static final String COLS =
            "id, user_id, title, organization, type, description, deadline, registration_link, "
                    + "eligibility, trust_verdict, trust_confidence, registered, idea_submitted, ppt_submitted, "
                    + "demo_submitted, final_submitted, status, created_at";

    private static final String ORDER =
            " ORDER BY deadline IS NULL, deadline ASC, created_at DESC, id DESC";

    private static Opportunity map(ResultSet rs) throws SQLException {
        Opportunity o = new Opportunity();

        o.setId(rs.getLong("id"));
        o.setUserId(rs.getLong("user_id"));
        o.setTitle(rs.getString("title"));
        o.setOrganization(rs.getString("organization"));
        o.setType(rs.getString("type"));
        o.setDescription(rs.getString("description"));

        Date d = rs.getDate("deadline");
        o.setDeadline(d == null ? null : d.toLocalDate());

        o.setRegistrationLink(rs.getString("registration_link"));
        o.setEligibility(rs.getString("eligibility"));
        o.setTrustVerdict(rs.getString("trust_verdict"));

        int conf = rs.getInt("trust_confidence");
        o.setTrustConfidence(rs.wasNull() ? null : conf);

        o.setRegistered(rs.getBoolean("registered"));
        o.setIdeaSubmitted(rs.getBoolean("idea_submitted"));
        o.setPptSubmitted(rs.getBoolean("ppt_submitted"));
        o.setDemoSubmitted(rs.getBoolean("demo_submitted"));
        o.setFinalSubmitted(rs.getBoolean("final_submitted"));

        o.setStatus(rs.getString("status"));
        o.setCreatedAt(rs.getTimestamp("created_at"));

        return o;
    }

    public List<Opportunity> getOpportunitiesByUserId(long userId) {
        try (Connection c = Db.get();
             PreparedStatement ps = c.prepareStatement(
                     "SELECT " + COLS
                             + " FROM opportunities WHERE user_id = ?"
                             + ORDER)) {

            ps.setLong(1, userId);

            try (ResultSet rs = ps.executeQuery()) {
                List<Opportunity> list = new ArrayList<>();

                while (rs.next()) {
                    list.add(map(rs));
                }

                return list;
            }

        } catch (SQLException e) {
            throw new DataAccessException(
                    "getOpportunitiesByUserId failed", e);
        }
    }

    public List<Opportunity> getRecentByUserId(long userId, int limit) {
        try (Connection c = Db.get();
             PreparedStatement ps = c.prepareStatement(
                     "SELECT " + COLS
                             + " FROM opportunities "
                             + "WHERE user_id = ? "
                             + "ORDER BY created_at DESC, id DESC LIMIT ?")) {

            ps.setLong(1, userId);
            ps.setInt(2, limit);

            try (ResultSet rs = ps.executeQuery()) {
                List<Opportunity> list = new ArrayList<>();

                while (rs.next()) {
                    list.add(map(rs));
                }

                return list;
            }

        } catch (SQLException e) {
            throw new DataAccessException(
                    "getRecentByUserId failed", e);
        }
    }

    public Optional<Opportunity> getOpportunityByIdAndUserId(
            long opportunityId,
            long userId) {

        try (Connection c = Db.get();
             PreparedStatement ps = c.prepareStatement(
                     "SELECT " + COLS
                             + " FROM opportunities "
                             + "WHERE id = ? AND user_id = ?")) {

            ps.setLong(1, opportunityId);
            ps.setLong(2, userId);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next()
                        ? Optional.of(map(rs))
                        : Optional.empty();
            }

        } catch (SQLException e) {
            throw new DataAccessException(
                    "getOpportunityByIdAndUserId failed", e);
        }
    }

    public long insert(Opportunity o) {

        String sql =
                "INSERT INTO opportunities "
                        + "(user_id, title, organization, type, description, deadline, "
                        + "registration_link, eligibility, trust_verdict, trust_confidence, status) "
                        + "VALUES (?,?,?,?,?,?,?,?,?,?,?)";

        try (Connection c = Db.get();
             PreparedStatement ps = c.prepareStatement(
                     sql,
                     Statement.RETURN_GENERATED_KEYS)) {

            ps.setLong(1, o.getUserId());
            ps.setString(2, o.getTitle());
            ps.setString(3, o.getOrganization());
            ps.setString(4, o.getType());
            ps.setString(5, o.getDescription());

            if (o.getDeadline() == null) {
                ps.setNull(6, Types.DATE);
            } else {
                ps.setDate(6, Date.valueOf(o.getDeadline()));
            }

            ps.setString(7, o.getRegistrationLink());
            ps.setString(8, o.getEligibility());
            ps.setString(9, o.getTrustVerdict());

            if (o.getTrustConfidence() == null) {
                ps.setNull(10, Types.INTEGER);
            } else {
                ps.setInt(10, o.getTrustConfidence());
            }

            ps.setString(11, "Not Registered");

            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next();
                return keys.getLong(1);
            }

        } catch (SQLException e) {
            throw new DataAccessException(
                    "insert opportunity failed", e);
        }
    }

    /** Updates the five stage flags manually for ONE opportunity owned by ONE user. */
    public boolean updateStages(
            long opportunityId,
            long userId,
            boolean registered,
            boolean idea,
            boolean ppt,
            boolean demo,
            boolean fin,
            String status) {

        String sql =
                "UPDATE opportunities SET "
                        + "registered=?, "
                        + "idea_submitted=?, "
                        + "ppt_submitted=?, "
                        + "demo_submitted=?, "
                        + "final_submitted=?, "
                        + "status=? "
                        + "WHERE id=? AND user_id=?";

        try (Connection c = Db.get();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setBoolean(1, registered);
            ps.setBoolean(2, idea);
            ps.setBoolean(3, ppt);
            ps.setBoolean(4, demo);
            ps.setBoolean(5, fin);
            ps.setString(6, status);
            ps.setLong(7, opportunityId);
            ps.setLong(8, userId);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new DataAccessException(
                    "updateStages failed", e);
        }
    }

    /**
     * Automatically updates ONE submission stage of ONE opportunity
     * owned by ONE user.
     *
     * This does NOT remove or overwrite the other stages.
     */
    public boolean updateAutomaticStage(
            long opportunityId,
            long userId,
            String stage) {

        String column;

        switch (stage) {
            case "IDEA_SUBMITTED" ->
                    column = "idea_submitted";

            case "PPT_SUBMITTED" ->
                    column = "ppt_submitted";

            case "DEMO_SUBMITTED" ->
                    column = "demo_submitted";

            case "FINAL_SUBMITTED" ->
                    column = "final_submitted";

            default ->
            {
                return false;
            }
        }

        String sql =
                "UPDATE opportunities SET "
                        + column + " = TRUE, "
                        + "status = CASE "
                        + "WHEN final_submitted = TRUE "
                        + "OR ? = 'FINAL_SUBMITTED' "
                        + "THEN 'Final Submission' "
                        + "WHEN demo_submitted = TRUE "
                        + "OR ? = 'DEMO_SUBMITTED' "
                        + "THEN 'Demo Submitted' "
                        + "WHEN ppt_submitted = TRUE "
                        + "OR ? = 'PPT_SUBMITTED' "
                        + "THEN 'PPT Submitted' "
                        + "WHEN idea_submitted = TRUE "
                        + "OR ? = 'IDEA_SUBMITTED' "
                        + "THEN 'Idea Submitted' "
                        + "WHEN registered = TRUE "
                        + "THEN 'Registered' "
                        + "ELSE 'Not Registered' "
                        + "END "
                        + "WHERE id = ? AND user_id = ?";

        try (Connection c = Db.get();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, stage);
            ps.setString(2, stage);
            ps.setString(3, stage);
            ps.setString(4, stage);
            ps.setLong(5, opportunityId);
            ps.setLong(6, userId);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new DataAccessException(
                    "updateAutomaticStage failed", e);
        }
    }

    public boolean deleteByIdAndUserId(
            long opportunityId,
            long userId) {

        try (Connection c = Db.get();
             PreparedStatement ps = c.prepareStatement(
                     "DELETE FROM opportunities "
                             + "WHERE id = ? AND user_id = ?")) {

            ps.setLong(1, opportunityId);
            ps.setLong(2, userId);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new DataAccessException(
                    "delete opportunity failed", e);
        }
    }

    public boolean existsForUser(
            long userId,
            String title,
            String organization) {

        try (Connection c = Db.get();
             PreparedStatement ps = c.prepareStatement(
                     "SELECT 1 FROM opportunities "
                             + "WHERE user_id = ? "
                             + "AND title = ? "
                             + "AND COALESCE(organization,'') = ? "
                             + "LIMIT 1")) {

            ps.setLong(1, userId);
            ps.setString(2, title);
            ps.setString(
                    3,
                    organization == null ? "" : organization);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }

        } catch (SQLException e) {
            throw new DataAccessException(
                    "existsForUser failed", e);
        }
    }

    /**
     * Finds an existing opportunity belonging to the logged-in user.
     */
    public Optional<Opportunity> findForUser(
            long userId,
            String title,
            String organization) {

        try (Connection c = Db.get();
             PreparedStatement ps = c.prepareStatement(
                     "SELECT " + COLS
                             + " FROM opportunities "
                             + "WHERE user_id = ? "
                             + "AND title = ? "
                             + "AND COALESCE(organization,'') = ? "
                             + "LIMIT 1")) {

            ps.setLong(1, userId);
            ps.setString(2, title);
            ps.setString(
                    3,
                    organization == null ? "" : organization);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next()
                        ? Optional.of(map(rs))
                        : Optional.empty();
            }

        } catch (SQLException e) {
            throw new DataAccessException(
                    "findForUser failed", e);
        }
    }
}

