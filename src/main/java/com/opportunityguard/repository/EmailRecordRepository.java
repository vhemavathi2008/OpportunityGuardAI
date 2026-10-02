package com.opportunityguard.repository;

import com.opportunityguard.config.Db;
import com.opportunityguard.model.EmailRecord;
import com.opportunityguard.util.DataAccessException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class EmailRecordRepository {
    private static EmailRecord map(ResultSet rs, boolean withContent) throws SQLException {
        EmailRecord e = new EmailRecord();
        e.setId(rs.getLong("id"));
        e.setUserId(rs.getLong("user_id"));
        e.setGmailMessageId(rs.getString("gmail_message_id"));
        e.setSender(rs.getString("sender"));
        e.setSubject(rs.getString("subject"));
        if (withContent) e.setEmailContent(rs.getString("email_content"));
        e.setAnalyzed(rs.getBoolean("analyzed"));
        e.setCreatedAt(rs.getTimestamp("created_at"));
        return e;
    }

    /** List view: content column is skipped because it can be large. */
    public List<EmailRecord> getEmailRecordsByUserId(long userId, int limit) {
        try (Connection c = Db.get();
             PreparedStatement ps = c.prepareStatement(
                     "SELECT id, user_id, gmail_message_id, sender, subject, analyzed, created_at FROM email_records "
                             + "WHERE user_id = ? ORDER BY created_at DESC, id DESC LIMIT ?")) {
            ps.setLong(1, userId);
            ps.setInt(2, limit);
            try (ResultSet rs = ps.executeQuery()) {
                List<EmailRecord> list = new ArrayList<>();
                while (rs.next()) list.add(map(rs, false));
                return list;
            }
        } catch (SQLException e) {
            throw new DataAccessException("getEmailRecordsByUserId failed", e);
        }
    }

    public List<EmailRecord> findUnanalyzedByUserId(long userId, int limit) {
        try (Connection c = Db.get();
             PreparedStatement ps = c.prepareStatement(
                     "SELECT id, user_id, gmail_message_id, sender, subject, email_content, analyzed, created_at "
                             + "FROM email_records WHERE user_id = ? AND analyzed = FALSE ORDER BY id ASC LIMIT ?")) {
            ps.setLong(1, userId);
            ps.setInt(2, limit);
            try (ResultSet rs = ps.executeQuery()) {
                List<EmailRecord> list = new ArrayList<>();
                while (rs.next()) list.add(map(rs, true));
                return list;
            }
        } catch (SQLException e) {
            throw new DataAccessException("findUnanalyzedByUserId failed", e);
        }
    }

    public boolean existsByUserAndMessageId(long userId, String gmailMessageId) {
        try (Connection c = Db.get();
             PreparedStatement ps = c.prepareStatement(
                     "SELECT 1 FROM email_records WHERE user_id = ? AND gmail_message_id = ? LIMIT 1")) {
            ps.setLong(1, userId);
            ps.setString(2, gmailMessageId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new DataAccessException("existsByUserAndMessageId failed", e);
        }
    }

    public void insert(EmailRecord e) {
        try (Connection c = Db.get();
             PreparedStatement ps = c.prepareStatement(
                     "INSERT INTO email_records (user_id, gmail_message_id, sender, subject, email_content) VALUES (?,?,?,?,?)")) {
            ps.setLong(1, e.getUserId());
            ps.setString(2, e.getGmailMessageId());
            ps.setString(3, e.getSender());
            ps.setString(4, e.getSubject());
            ps.setString(5, e.getEmailContent());
            ps.executeUpdate();
        } catch (SQLException ex) {
            throw new DataAccessException("insert email record failed", ex);
        }
    }

    public void markAnalyzed(long emailRecordId, long userId) {
        try (Connection c = Db.get();
             PreparedStatement ps = c.prepareStatement("UPDATE email_records SET analyzed = TRUE WHERE id = ? AND user_id = ?")) {
            ps.setLong(1, emailRecordId);
            ps.setLong(2, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("markAnalyzed failed", e);
        }
    }

    public int countByUserId(long userId, boolean analyzedOnly) {
        String sql = "SELECT COUNT(*) FROM email_records WHERE user_id = ?" + (analyzedOnly ? " AND analyzed = TRUE" : "");
        try (Connection c = Db.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new DataAccessException("countByUserId failed", e);
        }
    }
}
