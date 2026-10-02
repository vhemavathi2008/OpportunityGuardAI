package com.opportunityguard.security;

import com.opportunityguard.service.NotificationService;
import com.opportunityguard.util.Flash;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.HexFormat;
import java.util.Set;

/**
 * Front gate for every request:
 * 1) redirects anonymous visitors to /login for all pages except the public ones,
 * 2) enforces a per-session CSRF token on every POST,
 * 3) sets no-cache and basic security headers so logged-out users cannot go "Back" into private pages.
 */
@WebFilter(urlPatterns = "/*")
public class AuthFilter implements Filter {
    private static final String CSRF = "csrfToken";
    private static final Set<String> PUBLIC_PATHS = Set.of("/", "/index.jsp", "/login", "/register");
    private static final SecureRandom RNG = new SecureRandom();
    private final NotificationService notifications = new NotificationService();

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;
        req.setCharacterEncoding("UTF-8");

        String path = req.getRequestURI().substring(req.getContextPath().length());
        if (path.startsWith("/css/") || path.startsWith("/js/")) {
            chain.doFilter(request, response);
            return;
        }

        resp.setHeader("Cache-Control", "no-store, no-cache, must-revalidate");
        resp.setHeader("Pragma", "no-cache");
        resp.setHeader("X-Content-Type-Options", "nosniff");
        resp.setHeader("X-Frame-Options", "DENY");

        HttpSession session = req.getSession(true);
        String token = (String) session.getAttribute(CSRF);
        if (token == null) {
            byte[] bytes = new byte[32];
            RNG.nextBytes(bytes);
            token = HexFormat.of().formatHex(bytes);
            session.setAttribute(CSRF, token);
        }
        req.setAttribute("csrfToken", token);
        Flash.expose(req);

        if ("POST".equalsIgnoreCase(req.getMethod())) {
            String sent = req.getParameter("_csrf");
            if (sent == null || !MessageDigest.isEqual(
                    sent.getBytes(StandardCharsets.UTF_8), token.getBytes(StandardCharsets.UTF_8))) {
                resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Invalid or missing security token. Reload the page and try again.");
                return;
            }
        }

        boolean loggedIn = SessionUtil.isLoggedIn(req);
        boolean isPublic = PUBLIC_PATHS.contains(path);
        if (!loggedIn && !isPublic) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }
        if (loggedIn && !isPublic && "GET".equalsIgnoreCase(req.getMethod())) {
            req.setAttribute("unreadCount", notifications.unreadCount(SessionUtil.requireUserId(req)));
        }
        chain.doFilter(request, response);
    }
}
