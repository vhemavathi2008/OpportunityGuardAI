package com.opportunityguard.controller;

import com.opportunityguard.security.SessionUtil;
import com.opportunityguard.service.AuthService;
import com.opportunityguard.service.GmailService;
import com.opportunityguard.util.Flash;
import com.opportunityguard.util.WebUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@WebServlet("/settings")
public class SettingsServlet extends HttpServlet {
    private final AuthService auth = new AuthService();
    private final GmailService gmail = new GmailService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        long userId = SessionUtil.requireUserId(req);
        req.setAttribute("gmailConnected", gmail.isConnected(userId));
        req.setAttribute("oauthConfigured", gmail.isOAuthConfigured());
        req.setAttribute("geminiConfigured", gmail.isGeminiConfigured());
        WebUtil.forward(req, resp, "settings.jsp");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        long userId = SessionUtil.requireUserId(req);
        List<String> errors = auth.changePassword(userId, req.getParameter("currentPassword"),
                req.getParameter("newPassword"), req.getParameter("confirmPassword"));
        if (errors.isEmpty()) Flash.success(req, "Password changed.");
        else Flash.error(req, String.join(" ", errors));
        WebUtil.redirect(req, resp, "/settings");
    }
}
