package com.example.buildflow.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Project {
    private String id;
    private String name;
    private String description;
    private String type;
    private String startDate;
    private int progress;
    private int membersCount;
    private String status;

    // רשימת ה-UID של המשתתפים
    private List<String> participants;

    // מיפוי: מפתח=UID, ערך=תפקיד (למשל: "manager", "plumber")
    private Map<String, String> roles;

    // --- הוספנו את זה! רשימת התקלות בפרויקט ---
    private List<ProjectRequest> requests;

    public Project() {
        // בנאי ריק חובה ל-Firestore
    }

    public Project(String ID, String name, String type, String startDate, int progress, int membersCount, String status, String description) {
        this.id = ID;
        this.name = name;
        this.type = type;
        this.startDate = startDate;
        this.progress = progress;
        this.membersCount = membersCount;
        this.status = status;
        this.description = description;
        this.participants = new ArrayList<>();
        this.roles = new HashMap<>();
        this.requests = new ArrayList<>(); // אתחול הרשימה
    }

    // --- תוספות לניהול בקשות ---
    public List<ProjectRequest> getRequests() {
        if (requests == null) requests = new ArrayList<>(); // הגנה מקריסה אם הרשימה ריקה בשרת
        return requests;
    }

    public void setRequests(List<ProjectRequest> requests) {
        this.requests = requests;
    }

    public void addRequest(ProjectRequest request) {
        if (this.requests == null) {
            this.requests = new ArrayList<>();
        }
        this.requests.add(request);
    }

    // --- שאר הגטרים והסטרים (ללא שינוי) ---
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getStartDate() { return startDate; }
    public void setStartDate(String startDate) { this.startDate = startDate; }
    public int getProgress() { return progress; }
    public void setProgress(int progress) { this.progress = progress; }
    public int getMembersCount() { return membersCount; }
    public void setMembersCount(int membersCount) { this.membersCount = membersCount; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public List<String> getParticipants() { return participants; }
    public void setParticipants(List<String> participants) { this.participants = participants; }
    public Map<String, String> getRoles() { return roles; }
    public void setRoles(Map<String, String> roles) { this.roles = roles; }

    public void addPartner(String userID, String role) {
        if (!this.participants.contains(userID)) {
            this.participants.add(userID);
            membersCount++;
        }
        this.roles.put(userID, role);
    }

    public String getRole(String userId){
        return roles.get(userId);
    }
}