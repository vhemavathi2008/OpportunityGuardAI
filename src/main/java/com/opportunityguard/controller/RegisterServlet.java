package com.opportunityguard.controller;

import com.opportunityguard.security.SessionUtil;
import com.opportunityguard.service.AuthService;
import com.opportunityguard.util.Flash;
import com.opportunityguard.util.WebUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@WebServlet("/register")
public class RegisterServlet extends HttpServlet {
    private final AuthService auth = new AuthService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (SessionUtil.isLoggedIn(req)) {
            WebUtil.redirect(req, resp, "/dashboard");
            return;
        }
        WebUtil.forward(req, resp, "register.jsp");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String fullName = WebUtil.param(req, "fullName");
        String email = WebUtil.param(req, "email");
        List<String> errors = auth.register(fullName, email, req.getParameter("password"), req.getParameter("confirmPassword"));
        if (errors.isEmpty()) {
            Flash.success(req, "Account created. Please log in.");
            WebUtil.redirect(req, resp, "/login");
            return;
        }
        req.setAttribute("errors", errors);
        req.setAttribute("fullName", fullName);
        req.setAttribute("email", email);
        WebUtil.forward(req, resp, "register.jsp");
    }
}
