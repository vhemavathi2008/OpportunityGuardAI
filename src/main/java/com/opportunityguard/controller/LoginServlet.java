package com.opportunityguard.controller;

import com.opportunityguard.model.User;
import com.opportunityguard.security.SessionUtil;
import com.opportunityguard.service.AuthService;
import com.opportunityguard.util.WebUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {
    private final AuthService auth = new AuthService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (SessionUtil.isLoggedIn(req)) {
            WebUtil.redirect(req, resp, "/dashboard");
            return;
        }
        WebUtil.forward(req, resp, "login.jsp");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String email = WebUtil.param(req, "email");
        String password = req.getParameter("password");
        req.setAttribute("email", email);
        if (email.isEmpty() || password == null || password.isEmpty()) {
            req.setAttribute("error", "Enter both your email and password.");
            WebUtil.forward(req, resp, "login.jsp");
            return;
        }
        Optional<User> user = auth.authenticate(email, password);
        if (user.isEmpty()) {
            req.setAttribute("error", "Invalid email or password.");
            WebUtil.forward(req, resp, "login.jsp");
            return;
        }
        SessionUtil.login(req, user.get());
        WebUtil.redirect(req, resp, "/dashboard");
    }
}
