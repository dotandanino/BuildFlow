package com.example.buildflow.model;

import com.google.firebase.database.Exclude;

public class User {
    private String uid;
    private String email;
    private String name;
    private String profileImageUrl; // השדה החדש לתמונת הפרופיל
    private String projectRole;


    public User() {
        // בנאי ריק חובה עבור Firestore
    }

    public User(String uid, String email, String name) {
        this.uid = uid;
        this.email = email;
        this.name = name;

        // set a default profile image URL based on the user's name
        if (name != null && !name.isEmpty()) {
            this.profileImageUrl = "https://ui-avatars.com/api/?name=" + name + "&background=random&color=fff";
        } else {
            // in case there is no name
            this.profileImageUrl = "https://cdn.pixabay.com/photo/2015/10/05/22/37/blank-profile-picture-973460_1280.png";
        }
    }

    // --- Getters & Setters ---

    public String getUid() { return uid; }
    public void setUid(String uid) { this.uid = uid; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getProfileImageUrl() { return profileImageUrl; }
    public void setProfileImageUrl(String profileImageUrl) { this.profileImageUrl = profileImageUrl; }
    @Exclude
    public String getProjectRole() { return projectRole; }

    @Exclude
    public void setProjectRole(String projectRole) { this.projectRole = projectRole; }
}