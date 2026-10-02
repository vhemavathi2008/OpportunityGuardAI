package com.opportunityguard.controller;

import com.opportunityguard.security.SessionUtil;
import com.opportunityguard.service.GmailService;
import com.opportunityguard.util.Flash;
import com.opportunityguard.util.WebUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.HexFormat;

@WebServlet({"/gmail/connect", "/gmail/disconnect"})
public class GmailConnectServlet extends HttpServlet {
    private static final SecureRandom RNG = new SecureRandom();
    private final GmailService gmail = new GmailService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        WebUtil.redirect(req, resp, "/gmail");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        long userId = SessionUtil.requireUserId(req);
        if (req.getServletPath().endsWith("/disconnect")) {
            gmail.disconnect(userId);
            Flash.success(req, "Gmail disconnected. Stored emails and opportunities were kept.");
            WebUtil.redirect(req, resp, "/gmail");
            return;
        }
        if (!gmail.isOAuthConfigured()) {
            Flash.error(req, "Google OAuth is not configured on this server. Set GOOGLE_CLIENT_ID and GOOGLE_CLIENT_SECRET (see README).");
            WebUtil.redirect(req, resp, "/gmail");
            return;
        }
        byte[] bytes = new byte[24];
        RNG.nextBytes(bytes);
        String state = HexFormat.of().formatHex(bytes);
        req.getSession(true).setAttribute("oauthState", state);
        resp.sendRedirect(gmail.authorizationUrl(state));
    }
}
