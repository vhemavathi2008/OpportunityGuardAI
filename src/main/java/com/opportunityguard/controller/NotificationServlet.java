package com.opportunityguard.controller;

import com.opportunityguard.security.SessionUtil;
import com.opportunityguard.service.NotificationService;
import com.opportunityguard.util.WebUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet({"/notifications", "/notifications/read"})
public class NotificationServlet extends HttpServlet {
    private final NotificationService service = new NotificationService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!"/notifications".equals(req.getServletPath())) {
            WebUtil.redirect(req, resp, "/notifications");
            return;
        }
        long userId = SessionUtil.requireUserId(req);
        service.generateDeadlineNotifications(userId);
        req.setAttribute("notifications", service.list(userId, 100));
        WebUtil.forward(req, resp, "notifications.jsp");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        long userId = SessionUtil.requireUserId(req);
        if ("all".equals(req.getParameter("id"))) {
            service.markAllRead(userId);
        } else {
            service.markRead(WebUtil.parseLong(req.getParameter("id"), -1), userId);
        }
        WebUtil.redirect(req, resp, "/notifications");
    }
}
