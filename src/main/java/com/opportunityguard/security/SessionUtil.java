package com.opportunityguard.security;

import com.opportunityguard.model.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

/** The only place that reads or writes the authenticated user in the HTTP session. */
public final class SessionUtil {
    public static final String USER_ID = "userId";
    public static final String USER_NAME = "userName";
    public static final String USER_EMAIL = "userEmail";

    private SessionUtil() {}

    public static void login(HttpServletRequest req, User user) {
        HttpSession session = req.getSession(true);
        req.changeSessionId(); // prevents session fixation
        session.setAttribute(USER_ID, user.getId());
        session.setAttribute(USER_NAME, user.getFullName());
        session.setAttribute(USER_EMAIL, user.getEmail());
        session.setMaxInactiveInterval(30 * 60);
    }

    public static boolean isLoggedIn(HttpServletRequest req) {
        HttpSession s = req.getSession(false);
        return s != null && s.getAttribute(USER_ID) instanceof Long;
    }

    /** The authenticated user's id, always taken from the server-side session, never from request parameters. */
    public static long requireUserId(HttpServletRequest req) {
        HttpSession s = req.getSession(false);
        if (s == null || !(s.getAttribute(USER_ID) instanceof Long id)) {
            throw new IllegalStateException("No authenticated user in session");
        }
        return id;
    }

    public static void updateName(HttpServletRequest req, String fullName) {
        req.getSession(true).setAttribute(USER_NAME, fullName);
    }

    public static void logout(HttpServletRequest req) {
        HttpSession s = req.getSession(false);
        if (s != null) s.invalidate();
    }
}
