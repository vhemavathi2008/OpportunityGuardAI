package com.opportunityguard.controller;

import com.opportunityguard.model.Opportunity;
import com.opportunityguard.security.SessionUtil;
import com.opportunityguard.service.OpportunityService;
import com.opportunityguard.util.Flash;
import com.opportunityguard.util.WebUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@WebServlet({"/opportunities", "/opportunities/add"})
public class OpportunityServlet extends HttpServlet {
    private final OpportunityService service = new OpportunityService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (req.getServletPath().endsWith("/add")) {
            WebUtil.redirect(req, resp, "/opportunities");
            return;
        }
        render(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!req.getServletPath().endsWith("/add")) {
            resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
            return;
        }
        long userId = SessionUtil.requireUserId(req);
        String title = WebUtil.param(req, "title");
        String organization = WebUtil.param(req, "organization");
        String type = WebUtil.param(req, "type");
        String description = WebUtil.param(req, "description");
        String deadline = WebUtil.param(req, "deadline");
        String link = WebUtil.param(req, "registrationLink");
        String eligibility = WebUtil.param(req, "eligibility");

        List<String> errors = service.create(userId, title, organization, type, description, deadline, link, eligibility);
        if (errors.isEmpty()) {
            Flash.success(req, "Opportunity added.");
            WebUtil.redirect(req, resp, "/opportunities");
            return;
        }
        Map<String, String> form = new HashMap<>();
        form.put("title", title);
        form.put("organization", organization);
        form.put("type", type);
        form.put("description", description);
        form.put("deadline", deadline);
        form.put("registrationLink", link);
        form.put("eligibility", eligibility);
        req.setAttribute("form", form);
        req.setAttribute("formErrors", errors);
        req.setAttribute("showForm", true);
        render(req, resp);
    }

    private void render(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        long userId = SessionUtil.requireUserId(req);
        List<Opportunity> all = service.list(userId);
        String q = WebUtil.param(req, "q");
        if (!q.isEmpty()) {
            String needle = q.toLowerCase(Locale.ROOT);
            all = all.stream().filter(o -> has(o.getTitle(), needle) || has(o.getOrganization(), needle)
                    || has(o.getType(), needle)).toList();
        }
        req.setAttribute("opportunities", all);
        req.setAttribute("q", q);
        req.setAttribute("types", Opportunity.TYPES);
        WebUtil.forward(req, resp, "opportunities.jsp");
    }

    private static boolean has(String value, String needle) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(needle);
    }
}
