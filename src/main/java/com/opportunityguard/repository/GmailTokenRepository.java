package com.opportunityguard.repository;

import com.opportunityguard.config.Db;
import com.opportunityguard.util.DataAccessException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Optional;

/** Stores each user's own Google OAuth refresh token (one row per user). */
public class GmailTokenRepository {
    public Optional<String> findRefreshToken(long userId) {
        try (Connection c = Db.get();
             PreparedStatement ps = c.prepareStatement("SELECT refresh_token FROM gmail_tokens WHERE user_id = ?")) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(rs.getString(1)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new DataAccessException("findRefreshToken failed", e);
        }
    }

    public void save(long userId, String refreshToken) {
        try (Connection c = Db.get();
             PreparedStatement ps = c.prepareStatement(
                     "INSERT INTO gmail_tokens (user_id, refresh_token) VALUES (?, ?) "
                             + "ON DUPLICATE KEY UPDATE refresh_token = VALUES(refresh_token), connected_at = CURRENT_TIMESTAMP")) {
            ps.setLong(1, userId);
            ps.setString(2, refreshToken);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("save token failed", e);
        }
    }

    public void delete(long userId) {
        try (Connection c = Db.get();
             PreparedStatement ps = c.prepareStatement("DELETE FROM gmail_tokens WHERE user_id = ?")) {
            ps.setLong(1, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("delete token failed", e);
        }
    }

    public void touchLastSync(long userId) {
        try (Connection c = Db.get();
             PreparedStatement ps = c.prepareStatement("UPDATE gmail_tokens SET last_sync_at = CURRENT_TIMESTAMP WHERE user_id = ?")) {
            ps.setLong(1, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("touchLastSync failed", e);
        }
    }

    /** Null when Gmail is not connected or has never been synced. */
    public Timestamp findLastSync(long userId) {
        try (Connection c = Db.get();
             PreparedStatement ps = c.prepareStatement("SELECT last_sync_at FROM gmail_tokens WHERE user_id = ?")) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getTimestamp(1) : null;
            }
        } catch (SQLException e) {
            throw new DataAccessException("findLastSync failed", e);
        }
    }
}
