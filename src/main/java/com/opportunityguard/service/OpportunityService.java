package com.opportunityguard.service;

import com.opportunityguard.model.DashboardStats;
import com.opportunityguard.model.Opportunity;
import com.opportunityguard.repository.NotificationRepository;
import com.opportunityguard.repository.OpportunityRepository;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class OpportunityService {
    private final OpportunityRepository repo = new OpportunityRepository();
    private final NotificationRepository notifications = new NotificationRepository();

    public List<Opportunity> list(long userId) {
        return repo.getOpportunitiesByUserId(userId);
    }

    public List<Opportunity> recent(long userId, int limit) {
        return repo.getRecentByUserId(userId, limit);
    }

    /**
     * Returns empty if the opportunity does not exist OR belongs to another user.
     */
    public Optional<Opportunity> get(long opportunityId, long userId) {
        return repo.getOpportunityByIdAndUserId(opportunityId, userId);
    }

    public List<Opportunity> upcoming(long userId, int withinDays, int limit) {
        LocalDate today = LocalDate.now();
        LocalDate end = today.plusDays(withinDays);
        return repo.getOpportunitiesByUserId(userId).stream()
                .filter(o -> o.getDeadline() != null && !o.getDeadline().isBefore(today)
                        && !o.getDeadline().isAfter(end) && !o.isFinalSubmitted())
                .limit(limit)
                .toList();
    }

    public DashboardStats stats(long userId) {
        List<Opportunity> all = repo.getOpportunitiesByUserId(userId);
        LocalDate today = LocalDate.now();
        int upcoming = 0, reg = 0, idea = 0, ppt = 0, demo = 0, fin = 0;
        for (Opportunity o : all) {
            if (o.getDeadline() != null && !o.getDeadline().isBefore(today) && !o.isFinalSubmitted()) upcoming++;
            if (o.isRegistered()) reg++;
            if (o.isIdeaSubmitted()) idea++;
            if (o.isPptSubmitted()) ppt++;
            if (o.isDemoSubmitted()) demo++;
            if (o.isFinalSubmitted()) fin++;
        }
        return new DashboardStats(all.size(), upcoming, reg, idea, ppt, demo, fin, notifications.countUnread(userId));
    }

    /**
     * Manual entry by the logged-in user. Returns validation errors; empty list means saved.
     */
    public List<String> create(long userId, String title, String organization, String type, String description,
                               String deadline, String link, String eligibility) {
        List<String> errors = new ArrayList<>();
        if (title.isEmpty()) errors.add("Opportunity name is required.");
        else if (title.length() > 255) errors.add("Opportunity name must be 255 characters or fewer.");
        if (organization.length() > 255) errors.add("Organization must be 255 characters or fewer.");
        if (!Opportunity.TYPES.contains(type)) errors.add("Choose a valid type.");
        if (description.length() > 2000) errors.add("Description must be 2000 characters or fewer.");
        if (eligibility.length() > 2000) errors.add("Eligibility must be 2000 characters or fewer.");
        LocalDate date = null;
        if (!deadline.isEmpty()) {
            try {
                date = LocalDate.parse(deadline);
            } catch (DateTimeParseException e) {
                errors.add("Deadline must be a valid date.");
            }
        }
        if (!link.isEmpty() && !(link.startsWith("http://") || link.startsWith("https://"))) {
            errors.add("Registration link must start with http:// or https://");
        } else if (link.length() > 1000) {
            errors.add("Registration link must be 1000 characters or fewer.");
        }
        if (!errors.isEmpty()) return errors;

        Opportunity o = new Opportunity();
        o.setUserId(userId);
        o.setTitle(title);
        o.setOrganization(organization.isEmpty() ? null : organization);
        o.setType(type);
        o.setDescription(description.isEmpty() ? null : description);
        o.setDeadline(date);
        o.setRegistrationLink(link.isEmpty() ? null : link);
        o.setEligibility(eligibility.isEmpty() ? null : eligibility);
        o.setTrustVerdict("Unverified"); // manual entries have not been checked
        o.setTrustConfidence(null);
        repo.insert(o);
        return errors;
    }

    /**
     * Stores an opportunity extracted from the user's own email. Returns false if a duplicate already exists.
     */
    public boolean addExtracted(Opportunity o) {
        if (repo.existsForUser(o.getUserId(), o.getTitle(), o.getOrganization())) return false;
        repo.insert(o);
        return true;
    }

    public boolean updateStages(long userId, long opportunityId, boolean registered, boolean idea, boolean ppt,
                                boolean demo, boolean fin) {
        String status = Opportunity.computeStatus(registered, idea, ppt, demo, fin);
        return repo.updateStages(opportunityId, userId, registered, idea, ppt, demo, fin, status);
    }

    public boolean updateAutomaticStage(long userId, long opportunityId, String stage) {
        return repo.updateAutomaticStage(opportunityId, userId, stage);
    }

    public boolean delete(long userId, long opportunityId) {
        return repo.deleteByIdAndUserId(opportunityId, userId);
    }
}