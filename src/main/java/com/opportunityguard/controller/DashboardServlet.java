package com.opportunityguard.controller;

import com.opportunityguard.security.SessionUtil;
import com.opportunityguard.service.GmailService;
import com.opportunityguard.service.NotificationService;
import com.opportunityguard.service.OpportunityService;
import com.opportunityguard.util.WebUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/dashboard")
public class DashboardServlet extends HttpServlet {
    private final OpportunityService opportunities = new OpportunityService();
    private final NotificationService notifications = new NotificationService();
    private final GmailService gmail = new GmailService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        long userId = SessionUtil.requireUserId(req);
        notifications.generateDeadlineNotifications(userId);
        req.setAttribute("stats", opportunities.stats(userId));
        req.setAttribute("recent", opportunities.recent(userId, 5));
        req.setAttribute("upcoming", opportunities.upcoming(userId, 14, 5));
        req.setAttribute("recentNotifications", notifications.list(userId, 5));
        req.setAttribute("gmailConnected", gmail.isConnected(userId));
        WebUtil.forward(req, resp, "dashboard.jsp");
    }
}
