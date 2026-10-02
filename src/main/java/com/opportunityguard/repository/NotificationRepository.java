package com.opportunityguard.repository;

import com.opportunityguard.config.Db;
import com.opportunityguard.model.Notification;
import com.opportunityguard.util.DataAccessException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class NotificationRepository {
    private static Notification map(ResultSet rs) throws SQLException {
        Notification n = new Notification();
        n.setId(rs.getLong("id"));
        n.setUserId(rs.getLong("user_id"));
        long oid = rs.getLong("opportunity_id");
        n.setOpportunityId(rs.wasNull() ? null : oid);
        n.setTitle(rs.getString("title"));
        n.setMessage(rs.getString("message"));
        n.setRead(rs.getBoolean("is_read"));
        n.setCreatedAt(rs.getTimestamp("created_at"));
        return n;
    }

    public List<Notification> getNotificationsByUserId(long userId, int limit) {
        try (Connection c = Db.get();
             PreparedStatement ps = c.prepareStatement(
                     "SELECT id, user_id, opportunity_id, title, message, is_read, created_at FROM notifications "
                             + "WHERE user_id = ? ORDER BY created_at DESC, id DESC LIMIT ?")) {
            ps.setLong(1, userId);
            ps.setInt(2, limit);
            try (ResultSet rs = ps.executeQuery()) {
                List<Notification> list = new ArrayList<>();
                while (rs.next()) list.add(map(rs));
                return list;
            }
        } catch (SQLException e) {
            throw new DataAccessException("getNotificationsByUserId failed", e);
        }
    }

    public int countUnread(long userId) {
        try (Connection c = Db.get();
             PreparedStatement ps = c.prepareStatement("SELECT COUNT(*) FROM notifications WHERE user_id = ? AND is_read = FALSE")) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new DataAccessException("countUnread failed", e);
        }
    }

    public boolean exists(long userId, long opportunityId, String title) {
        try (Connection c = Db.get();
             PreparedStatement ps = c.prepareStatement(
                     "SELECT 1 FROM notifications WHERE user_id = ? AND opportunity_id = ? AND title = ? LIMIT 1")) {
            ps.setLong(1, userId);
            ps.setLong(2, opportunityId);
            ps.setString(3, title);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new DataAccessException("notification exists failed", e);
        }
    }

    public void insert(long userId, Long opportunityId, String title, String message) {
        try (Connection c = Db.get();
             PreparedStatement ps = c.prepareStatement(
                     "INSERT INTO notifications (user_id, opportunity_id, title, message) VALUES (?,?,?,?)")) {
            ps.setLong(1, userId);
            if (opportunityId == null) ps.setNull(2, Types.BIGINT); else ps.setLong(2, opportunityId);
            ps.setString(3, title);
            ps.setString(4, message);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("insert notification failed", e);
        }
    }

    public boolean markRead(long notificationId, long userId) {
        try (Connection c = Db.get();
             PreparedStatement ps = c.prepareStatement("UPDATE notifications SET is_read = TRUE WHERE id = ? AND user_id = ?")) {
            ps.setLong(1, notificationId);
            ps.setLong(2, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DataAccessException("markRead failed", e);
        }
    }

    public void markAllRead(long userId) {
        try (Connection c = Db.get();
             PreparedStatement ps = c.prepareStatement("UPDATE notifications SET is_read = TRUE WHERE user_id = ?")) {
            ps.setLong(1, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("markAllRead failed", e);
        }
    }
}
