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

/** GET /gmail shows connection + email-analysis status; POST /gmail/sync runs a sync for the logged-in user. */
@WebServlet({"/gmail", "/gmail/sync"})
public class GmailSyncServlet extends HttpServlet {
    private final GmailService gmail = new GmailService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!"/gmail".equals(req.getServletPath())) {
            WebUtil.redirect(req, resp, "/gmail");
            return;
        }
        long userId = SessionUtil.requireUserId(req);
        req.setAttribute("connected", gmail.isConnected(userId));
        req.setAttribute("oauthConfigured", gmail.isOAuthConfigured());
        req.setAttribute("geminiConfigured", gmail.isGeminiConfigured());
        req.setAttribute("lastSync", gmail.lastSync(userId));
        req.setAttribute("records", gmail.records(userId, 50));
        req.setAttribute("totalEmails", gmail.countEmails(userId));
        req.setAttribute("analyzedEmails", gmail.countAnalyzed(userId));
        WebUtil.forward(req, resp, "gmail.jsp");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        long userId = SessionUtil.requireUserId(req);
        GmailService.SyncResult r = gmail.sync(userId);
        if (r.error != null && r.listed == 0 && r.stored == 0 && r.analyzed == 0) {
            Flash.error(req, "Sync failed: " + r.error);
        } else {
            String msg = "Sync finished: " + r.stored + " new email(s) stored, " + r.analyzed + " analysed, "
                    + r.created + " opportunity(ies) added, " + r.notOpportunity + " not an opportunity.";
            if (r.geminiMissing) msg += " Emails were stored but not analysed because GEMINI_API_KEY is not configured.";
            if (r.error != null) msg += " Stopped early: " + r.error;
            if (r.error != null) Flash.error(req, msg); else Flash.success(req, msg);
        }
        WebUtil.redirect(req, resp, "/gmail");
    }
}
