package com.opportunityguard.util;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

/** One-time messages that survive a redirect (stored in the session, moved to request scope once). */
public final class Flash {
    private Flash() {}

    public static void success(HttpServletRequest req, String msg) {
        req.getSession(true).setAttribute("flashSuccess", msg);
    }

    public static void error(HttpServletRequest req, String msg) {
        req.getSession(true).setAttribute("flashError", msg);
    }

    public static void expose(HttpServletRequest req) {
        HttpSession s = req.getSession(false);
        if (s == null) return;
        for (String key : new String[] {"flashSuccess", "flashError"}) {
            Object v = s.getAttribute(key);
            if (v != null) {
                req.setAttribute(key, v);
                s.removeAttribute(key);
            }
        }
    }
}
