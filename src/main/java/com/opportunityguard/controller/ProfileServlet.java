package com.opportunityguard.controller;

import com.opportunityguard.model.User;
import com.opportunityguard.security.SessionUtil;
import com.opportunityguard.service.AuthService;
import com.opportunityguard.service.GmailService;
import com.opportunityguard.service.OpportunityService;
import com.opportunityguard.util.Flash;
import com.opportunityguard.util.WebUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

@WebServlet("/profile")
public class ProfileServlet extends HttpServlet {
    private final AuthService auth = new AuthService();
    private final OpportunityService opportunities = new OpportunityService();
    private final GmailService gmail = new GmailService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        long userId = SessionUtil.requireUserId(req);
        Optional<User> user = auth.findById(userId);
        if (user.isEmpty()) {
            SessionUtil.logout(req);
            WebUtil.redirect(req, resp, "/login");
            return;
        }
        req.setAttribute("profileUser", user.get());
        req.setAttribute("stats", opportunities.stats(userId));
        req.setAttribute("emailCount", gmail.countEmails(userId));
        WebUtil.forward(req, resp, "profile.jsp");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        long userId = SessionUtil.requireUserId(req);
        String fullName = WebUtil.param(req, "fullName");
        List<String> errors = auth.updateName(userId, fullName);
        if (errors.isEmpty()) {
            SessionUtil.updateName(req, fullName);
            Flash.success(req, "Profile updated.");
        } else {
            Flash.error(req, String.join(" ", errors));
        }
        WebUtil.redirect(req, resp, "/profile");
    }
}
