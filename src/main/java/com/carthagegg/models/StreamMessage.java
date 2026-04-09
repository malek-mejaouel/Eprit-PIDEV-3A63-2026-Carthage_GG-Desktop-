package com.carthagegg.models;

import java.time.LocalDateTime;

public class StreamMessage {
    private int messageId;
    private int streamId;
    private int userId;
    private String message;
    private boolean deleted;
    private LocalDateTime createdAt;

    public StreamMessage() {}

    public int getMessageId() { return messageId; }
    public void setMessageId(int messageId) { this.messageId = messageId; }
    public int getStreamId() { return streamId; }
    public void setStreamId(int streamId) { this.streamId = streamId; }
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public boolean isDeleted() { return deleted; }
    public void setDeleted(boolean deleted) { this.deleted = deleted; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
