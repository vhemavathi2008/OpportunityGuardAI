package com.opportunityguard.model;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class Opportunity {
    public static final List<String> TYPES =
            List.of("Hackathon", "Internship", "Job", "Scholarship", "Competition", "Workshop", "Other");
    public static final List<String> VERDICTS = List.of("Likely Legitimate", "Suspicious", "Unverified");

    private long id;
    private long userId;
    private String title;
    private String organization;
    private String type = "Other";
    private String description;
    private LocalDate deadline;
    private String registrationLink;
    private String eligibility;
    private String trustVerdict = "Unverified";
    private Integer trustConfidence;
    private boolean registered;
    private boolean ideaSubmitted;
    private boolean pptSubmitted;
    private boolean demoSubmitted;
    private boolean finalSubmitted;
    private String status = "Not Registered";
    private Timestamp createdAt;

    /** The status is the highest stage the user has ticked; nothing is inferred automatically. */
    public static String computeStatus(boolean registered, boolean idea, boolean ppt, boolean demo, boolean fin) {
        if (fin) return "Final Submission";
        if (demo) return "Demo Submitted";
        if (ppt) return "PPT Submitted";
        if (idea) return "Idea Submitted";
        if (registered) return "Registered";
        return "Not Registered";
    }

    public Long getDaysLeft() {
        return deadline == null ? null : ChronoUnit.DAYS.between(LocalDate.now(), deadline);
    }

    public String getDeadlineClass() {
        Long d = getDaysLeft();
        if (d == null) return "muted";
        if (d < 0) return "overdue";
        if (d <= 3) return "soon";
        return "ok";
    }

    public String getStatusClass() {
        return switch (status == null ? "" : status) {
            case "Final Submission" -> "b-final";
            case "Demo Submitted" -> "b-demo";
            case "PPT Submitted" -> "b-ppt";
            case "Idea Submitted" -> "b-idea";
            case "Registered" -> "b-reg";
            default -> "b-none";
        };
    }

    public String getTrustClass() {
        return switch (trustVerdict == null ? "" : trustVerdict) {
            case "Likely Legitimate" -> "t-good";
            case "Suspicious" -> "t-bad";
            default -> "t-unknown";
        };
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public long getUserId() { return userId; }
    public void setUserId(long userId) { this.userId = userId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getOrganization() { return organization; }
    public void setOrganization(String organization) { this.organization = organization; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public LocalDate getDeadline() { return deadline; }
    public void setDeadline(LocalDate deadline) { this.deadline = deadline; }
    public String getRegistrationLink() { return registrationLink; }
    public void setRegistrationLink(String registrationLink) { this.registrationLink = registrationLink; }
    public String getEligibility() { return eligibility; }
    public void setEligibility(String eligibility) { this.eligibility = eligibility; }
    public String getTrustVerdict() { return trustVerdict; }
    public void setTrustVerdict(String trustVerdict) { this.trustVerdict = trustVerdict; }
    public Integer getTrustConfidence() { return trustConfidence; }
    public void setTrustConfidence(Integer trustConfidence) { this.trustConfidence = trustConfidence; }
    public boolean isRegistered() { return registered; }
    public void setRegistered(boolean registered) { this.registered = registered; }
    public boolean isIdeaSubmitted() { return ideaSubmitted; }
    public void setIdeaSubmitted(boolean ideaSubmitted) { this.ideaSubmitted = ideaSubmitted; }
    public boolean isPptSubmitted() { return pptSubmitted; }
    public void setPptSubmitted(boolean pptSubmitted) { this.pptSubmitted = pptSubmitted; }
    public boolean isDemoSubmitted() { return demoSubmitted; }
    public void setDemoSubmitted(boolean demoSubmitted) { this.demoSubmitted = demoSubmitted; }
    public boolean isFinalSubmitted() { return finalSubmitted; }
    public void setFinalSubmitted(boolean finalSubmitted) { this.finalSubmitted = finalSubmitted; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
}
