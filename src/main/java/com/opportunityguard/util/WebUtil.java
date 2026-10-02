package com.opportunityguard.util;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

public final class WebUtil {
    private WebUtil() {}

    public static void forward(HttpServletRequest req, HttpServletResponse resp, String view)
            throws ServletException, IOException {
        req.getRequestDispatcher("/WEB-INF/views/" + view).forward(req, resp);
    }

    public static void redirect(HttpServletRequest req, HttpServletResponse resp, String path) throws IOException {
        resp.sendRedirect(req.getContextPath() + path);
    }

    /** Returns the trimmed parameter value, or "" when missing. */
    public static String param(HttpServletRequest req, String name) {
        String v = req.getParameter(name);
        return v == null ? "" : v.trim();
    }

    public static long parseLong(String s, long fallback) {
        try {
            return Long.parseLong(s == null ? "" : s.trim());
        } catch (NumberFormatException e) {
            return fallback;
        }
    }
}
