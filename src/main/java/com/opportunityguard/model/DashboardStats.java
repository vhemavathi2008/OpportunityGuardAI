package com.opportunityguard.model;

/** Per-user statistics. Always computed from the logged-in user's own opportunities. */
public class DashboardStats {
    private final int total;
    private final int upcoming;
    private final int registered;
    private final int ideaSubmitted;
    private final int pptSubmitted;
    private final int demoSubmitted;
    private final int finalSubmitted;
    private final int unreadNotifications;

    public DashboardStats(int total, int upcoming, int registered, int ideaSubmitted, int pptSubmitted,
                          int demoSubmitted, int finalSubmitted, int unreadNotifications) {
        this.total = total;
        this.upcoming = upcoming;
        this.registered = registered;
        this.ideaSubmitted = ideaSubmitted;
        this.pptSubmitted = pptSubmitted;
        this.demoSubmitted = demoSubmitted;
        this.finalSubmitted = finalSubmitted;
        this.unreadNotifications = unreadNotifications;
    }

    public int getTotal() { return total; }
    public int getUpcoming() { return upcoming; }
    public int getRegistered() { return registered; }
    public int getIdeaSubmitted() { return ideaSubmitted; }
    public int getPptSubmitted() { return pptSubmitted; }
    public int getDemoSubmitted() { return demoSubmitted; }
    public int getFinalSubmitted() { return finalSubmitted; }
    public int getUnreadNotifications() { return unreadNotifications; }
}
