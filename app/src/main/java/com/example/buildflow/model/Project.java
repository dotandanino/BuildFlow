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
    private List<String> participants;
    private Map<User, String> roles;


    // Empty constructor required for Firestore
    public Project() {
    }

    public Project(String ID,String name, String type, String startDate, int progress, int membersCount, String status,String description) {
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
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<String> getParticipants() {
        return participants;
    }

    public void setParticipants(List<String> participants) {
        this.participants = participants;
    }

    public Map<User, String> getRoles() {
        return roles;
    }

    public void setRoles(Map<User, String> roles) {
        this.roles = roles;
    }

    public void addPartner(User user, String role) {
        if (!this.participants.contains(user.getUid())) {
            this.participants.add(user.getUid());
            membersCount++;
        }
        this.roles.put(user, role);
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

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
    public String getRole(String userId){
        return roles.get(userId);
    }
}
