package com.opportunityguard.service;

import com.opportunityguard.model.Notification;
import com.opportunityguard.model.Opportunity;
import com.opportunityguard.repository.NotificationRepository;
import com.opportunityguard.repository.OpportunityRepository;
import java.time.temporal.ChronoUnit;
import java.time.LocalDate;
import java.util.List;

/** Notifications are generated only from the user's own real deadlines; nothing is pre-seeded. */
public class NotificationService {
    private static final String T_SOON = "Registration deadline approaching";
    private static final String T_PASSED = "Submission stage needs to be updated";
    private final NotificationRepository notifications = new NotificationRepository();
    private final OpportunityRepository opportunities = new OpportunityRepository();

    public void generateDeadlineNotifications(long userId) {
        for (Opportunity o : opportunities.getOpportunitiesByUserId(userId)) {
            if (o.getDeadline() == null || o.isFinalSubmitted()) continue;
            long days = ChronoUnit.DAYS.between(LocalDate.now(), o.getDeadline());
            if (days >= 0 && days <= 3) {
                if (notifications.exists(userId, o.getId(), T_SOON)) continue;
                String when = days == 0 ? "today" : days == 1 ? "tomorrow" : "in " + days + " days";
                notifications.insert(userId, o.getId(), T_SOON,
                        "\"" + o.getTitle() + "\" closes " + when + " (" + o.getDeadline() + ").");
            } else if (days < 0 && days >= -14) {
                if (notifications.exists(userId, o.getId(), T_PASSED)) continue;
                notifications.insert(userId, o.getId(), T_PASSED,
                        "The deadline for \"" + o.getTitle() + "\" was " + o.getDeadline()
                                + ". Update its submission stage if anything has changed.");
            }
        }
    }

    public List<Notification> list(long userId, int limit) {
        return notifications.getNotificationsByUserId(userId, limit);
    }

    public int unreadCount(long userId) {
        return notifications.countUnread(userId);
    }

    public boolean markRead(long notificationId, long userId) {
        return notifications.markRead(notificationId, userId);
    }

    public void markAllRead(long userId) {
        notifications.markAllRead(userId);
    }
}
