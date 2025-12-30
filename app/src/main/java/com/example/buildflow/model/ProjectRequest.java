package com.example.buildflow.model;

import java.util.UUID;

public class ProjectRequest {
    private String requestId;
    private String title;
    private String category;
    private String description;
    private String urgency;
    private String location;
    private String preferredDate;
    private String preferredTime;
    private String receiver;
    private String status;
    private long createdAt;
    private String imageUrl;        // תמונת התקלה (בהתחלה)
    private String closingImageUrl; // תמונת התיקון (בסוף)
    private String resolutionDescription; // --- חדש: תיאור הטיפול ---

    public ProjectRequest() { }

    public ProjectRequest(String title, String category, String description, String urgency,
                          String location, String preferredDate, String preferredTime,
                          String receiver, String imageUrl) {
        this.requestId = UUID.randomUUID().toString();
        this.title = title;
        this.category = category;
        this.description = description;
        this.urgency = urgency;
        this.location = location;
        this.preferredDate = preferredDate;
        this.preferredTime = preferredTime;
        this.receiver = receiver;
        this.imageUrl = imageUrl;
        this.status = "Open";
        this.createdAt = System.currentTimeMillis();
    }

    // Getters & Setters
    public String getRequestId() { return requestId; }
    public void setRequestId(String requestId) { this.requestId = requestId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getUrgency() { return urgency; }
    public void setUrgency(String urgency) { this.urgency = urgency; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getPreferredDate() { return preferredDate; }
    public void setPreferredDate(String preferredDate) { this.preferredDate = preferredDate; }

    public String getPreferredTime() { return preferredTime; }
    public void setPreferredTime(String preferredTime) { this.preferredTime = preferredTime; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public String getReceiver() { return receiver; }
    public void setReceiver(String receiver) { this.receiver = receiver; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getClosingImageUrl() { return closingImageUrl; }
    public void setClosingImageUrl(String closingImageUrl) { this.closingImageUrl = closingImageUrl; }

    // --- הגטר והסטר החדשים ---
    public String getResolutionDescription() { return resolutionDescription; }
    public void setResolutionDescription(String resolutionDescription) { this.resolutionDescription = resolutionDescription; }
}