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
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/** Google redirects here after the user approves access. The tokens are stored for the logged-in user only. */
@WebServlet("/gmail/callback")
public class GmailCallbackServlet extends HttpServlet {
    private final GmailService gmail = new GmailService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        long userId = SessionUtil.requireUserId(req);
        HttpSession session = req.getSession(false);
        String expected = session == null ? null : (String) session.getAttribute("oauthState");
        if (session != null) session.removeAttribute("oauthState");
        String state = req.getParameter("state");
        String code = req.getParameter("code");

        if (req.getParameter("error") != null) {
            Flash.error(req, "Gmail access was not granted (" + req.getParameter("error") + ").");
        } else if (expected == null || state == null || !MessageDigest.isEqual(
                expected.getBytes(StandardCharsets.UTF_8), state.getBytes(StandardCharsets.UTF_8))) {
            Flash.error(req, "The Google sign-in response could not be verified. Please try connecting again.");
        } else if (code == null || code.isEmpty()) {
            Flash.error(req, "Google did not return an authorization code.");
        } else {
            try {
                gmail.completeAuth(userId, code);
                Flash.success(req, "Gmail connected. You can now sync your emails.");
            } catch (IOException e) {
                Flash.error(req, "Could not connect Gmail: " + e.getMessage());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                Flash.error(req, "Gmail connection was interrupted. Please try again.");
            }
        }
        WebUtil.redirect(req, resp, "/gmail");
    }
}
