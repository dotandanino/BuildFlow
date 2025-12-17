package com.example.buildflow.model;
public class Issue {
    private String id;
    private String projectId;
    private String reporterId;
    private String description;
    private String location;
    private String status;
    private String priority;
    private String imageUrl;
    private String dateCreated;

    //for firestore
    public Issue() {
    }
    public Issue(String id, String projectId, String reporterId, String description, String location, String status, String priority, String dateCreated,String imageUrl) {
        this.id = id;
        this.projectId = projectId;
        this.reporterId = reporterId;
        this.description = description;
        this.location = location;
        this.status = status;
        this.priority = priority;
        this.dateCreated = dateCreated;
        this.imageUrl = imageUrl;
    }

    // --- Getters and Setters ---

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public String getReporterId() {
        return reporterId;
    }

    public void setReporterId(String reporterId) {
        this.reporterId = reporterId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(String dateCreated) {
        this.dateCreated = dateCreated;
    }
}
