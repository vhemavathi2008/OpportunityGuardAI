package com.opportunityguard.model;

import java.sql.Timestamp;

public class EmailRecord {
    private long id;
    private long userId;
    private String gmailMessageId;
    private String sender;
    private String subject;
    private String emailContent;
    private boolean analyzed;
    private Timestamp createdAt;

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public long getUserId() { return userId; }
    public void setUserId(long userId) { this.userId = userId; }
    public String getGmailMessageId() { return gmailMessageId; }
    public void setGmailMessageId(String gmailMessageId) { this.gmailMessageId = gmailMessageId; }
    public String getSender() { return sender; }
    public void setSender(String sender) { this.sender = sender; }
    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }
    public String getEmailContent() { return emailContent; }
    public void setEmailContent(String emailContent) { this.emailContent = emailContent; }
    public boolean isAnalyzed() { return analyzed; }
    public void setAnalyzed(boolean analyzed) { this.analyzed = analyzed; }
    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
}
