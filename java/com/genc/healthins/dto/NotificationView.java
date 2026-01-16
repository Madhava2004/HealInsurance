package com.genc.healthins.dto;

import java.time.LocalDateTime;

public class NotificationView {

    private String title;        // "Reply on Ticket #3 (Claim Issue)"
    private String replyMessage; // Actual agent message
    private Long ticketId;
    private LocalDateTime createdAt;
    private Long id;
    // getters & setters

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getReplyMessage() { return replyMessage; }
    public void setReplyMessage(String replyMessage) { this.replyMessage = replyMessage; }

    public Long getTicketId() { return ticketId; }
    public void setTicketId(Long ticketId) { this.ticketId = ticketId; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
}