package com.opportunityguard.repository;

import com.opportunityguard.config.Db;
import com.opportunityguard.model.User;
import com.opportunityguard.util.DataAccessException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Optional;

public class UserRepository {
    private static final String COLS = "id, full_name, email, password, created_at";

    private static User map(ResultSet rs) throws SQLException {
        User u = new User();
        u.setId(rs.getLong("id"));
        u.setFullName(rs.getString("full_name"));
        u.setEmail(rs.getString("email"));
        u.setPasswordHash(rs.getString("password"));
        u.setCreatedAt(rs.getTimestamp("created_at"));
        return u;
    }

    public Optional<User> findByEmail(String email) {
        try (Connection c = Db.get();
             PreparedStatement ps = c.prepareStatement("SELECT " + COLS + " FROM users WHERE email = ?")) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new DataAccessException("findByEmail failed", e);
        }
    }

    public Optional<User> findById(long id) {
        try (Connection c = Db.get();
             PreparedStatement ps = c.prepareStatement("SELECT " + COLS + " FROM users WHERE id = ?")) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new DataAccessException("findById failed", e);
        }
    }

    public long create(String fullName, String email, String passwordHash) {
        try (Connection c = Db.get();
             PreparedStatement ps = c.prepareStatement(
                     "INSERT INTO users (full_name, email, password) VALUES (?, ?, ?)", Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, fullName);
            ps.setString(2, email);
            ps.setString(3, passwordHash);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next();
                return keys.getLong(1);
            }
        } catch (SQLException e) {
            throw new DataAccessException("create user failed", e);
        }
    }

    public void updateName(long userId, String fullName) {
        try (Connection c = Db.get();
             PreparedStatement ps = c.prepareStatement("UPDATE users SET full_name = ? WHERE id = ?")) {
            ps.setString(1, fullName);
            ps.setLong(2, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("updateName failed", e);
        }
    }

    public void updatePassword(long userId, String passwordHash) {
        try (Connection c = Db.get();
             PreparedStatement ps = c.prepareStatement("UPDATE users SET password = ? WHERE id = ?")) {
            ps.setString(1, passwordHash);
            ps.setLong(2, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("updatePassword failed", e);
        }
    }
}
