package com.example.buildflow;

public class Project {
    private String id;
    private String name;
    private String type;
    private String startDate;
    private int progress;
    private int membersCount;
    private String status;

    // Empty constructor required for Firestore
    public Project() {
    }

    public Project(String name, String type, String startDate, int progress, int membersCount, String status) {
        this.name = name;
        this.type = type;
        this.startDate = startDate;
        this.progress = progress;
        this.membersCount = membersCount;
        this.status = status;
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
}
