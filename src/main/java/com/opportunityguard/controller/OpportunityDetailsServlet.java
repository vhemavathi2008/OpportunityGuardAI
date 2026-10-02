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
import java.util.Optional;

/**
 * The user id always comes from the session. The opportunity id comes from the URL, but is only honoured when
 * that opportunity belongs to the logged-in user; otherwise the response is a plain 404.
 */
@WebServlet({"/opportunity", "/opportunity/status", "/opportunity/delete"})
public class OpportunityDetailsServlet extends HttpServlet {
    private final OpportunityService service = new OpportunityService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!"/opportunity".equals(req.getServletPath())) {
            WebUtil.redirect(req, resp, "/opportunities");
            return;
        }
        long userId = SessionUtil.requireUserId(req);
        long id = WebUtil.parseLong(req.getParameter("id"), -1);
        Optional<Opportunity> opp = service.get(id, userId);
        if (opp.isEmpty()) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        req.setAttribute("opportunity", opp.get());
        WebUtil.forward(req, resp, "opportunity-details.jsp");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        long userId = SessionUtil.requireUserId(req);
        long id = WebUtil.parseLong(req.getParameter("id"), -1);
        String path = req.getServletPath();

        if ("/opportunity/status".equals(path)) {
            boolean ok = service.updateStages(userId, id,
                    req.getParameter("registered") != null,
                    req.getParameter("ideaSubmitted") != null,
                    req.getParameter("pptSubmitted") != null,
                    req.getParameter("demoSubmitted") != null,
                    req.getParameter("finalSubmitted") != null);
            if (!ok) {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            Flash.success(req, "Participation status updated.");
            WebUtil.redirect(req, resp, "/opportunity?id=" + id);
        } else if ("/opportunity/delete".equals(path)) {
            if (!service.delete(userId, id)) {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            Flash.success(req, "Opportunity deleted.");
            WebUtil.redirect(req, resp, "/opportunities");
        } else {
            resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
        }
    }
}
