package com.opportunityguard.service;

import com.opportunityguard.model.User;
import com.opportunityguard.repository.UserRepository;
import com.opportunityguard.security.PasswordHasher;
import com.opportunityguard.util.DataAccessException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Pattern;

public class AuthService {
    private static final Pattern EMAIL = Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private final UserRepository users = new UserRepository();

    /** Returns validation errors; an empty list means the account was created. */
    public List<String> register(String fullName, String email, String password, String confirm) {
        List<String> errors = new ArrayList<>();
        fullName = fullName == null ? "" : fullName.trim();
        email = email == null ? "" : email.trim().toLowerCase(Locale.ROOT);

        if (fullName.isEmpty()) errors.add("Full name is required.");
        else if (fullName.length() > 100) errors.add("Full name must be 100 characters or fewer.");

        if (email.isEmpty()) errors.add("Email is required.");
        else if (email.length() > 190 || !EMAIL.matcher(email).matches()) errors.add("Enter a valid email address.");

        if (password == null || password.isEmpty()) errors.add("Password is required.");
        else if (password.length() < 8) errors.add("Password must be at least 8 characters.");
        else if (password.length() > 128) errors.add("Password must be 128 characters or fewer.");
        else if (confirm == null || confirm.isEmpty()) errors.add("Please confirm your password.");
        else if (!password.equals(confirm)) errors.add("Passwords do not match.");

        if (errors.isEmpty() && users.findByEmail(email).isPresent()) {
            errors.add("An account with this email already exists.");
        }
        if (errors.isEmpty()) {
            try {
                users.create(fullName, email, PasswordHasher.hash(password));
            } catch (DataAccessException e) {
                if (users.findByEmail(email).isPresent()) errors.add("An account with this email already exists.");
                else throw e;
            }
        }
        return errors;
    }

    public Optional<User> authenticate(String email, String password) {
        Optional<User> user = users.findByEmail(email.trim().toLowerCase(Locale.ROOT));
        if (user.isPresent() && PasswordHasher.verify(password, user.get().getPasswordHash())) return user;
        return Optional.empty();
    }

    public Optional<User> findById(long userId) {
        return users.findById(userId);
    }

    public List<String> updateName(long userId, String fullName) {
        List<String> errors = new ArrayList<>();
        fullName = fullName == null ? "" : fullName.trim();
        if (fullName.isEmpty()) errors.add("Full name is required.");
        else if (fullName.length() > 100) errors.add("Full name must be 100 characters or fewer.");
        if (errors.isEmpty()) users.updateName(userId, fullName);
        return errors;
    }

    public List<String> changePassword(long userId, String current, String next, String confirm) {
        List<String> errors = new ArrayList<>();
        Optional<User> user = users.findById(userId);
        if (user.isEmpty() || current == null || !PasswordHasher.verify(current, user.get().getPasswordHash())) {
            errors.add("Your current password is incorrect.");
        }
        if (next == null || next.length() < 8) errors.add("New password must be at least 8 characters.");
        else if (next.length() > 128) errors.add("New password must be 128 characters or fewer.");
        else if (!next.equals(confirm)) errors.add("New passwords do not match.");
        if (errors.isEmpty()) users.updatePassword(userId, PasswordHasher.hash(next));
        return errors;
    }
}
