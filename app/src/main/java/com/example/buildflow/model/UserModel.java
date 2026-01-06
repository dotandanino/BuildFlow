package com.example.buildflow.model;

public class UserModel {
    private String uid;
    private String name;
    private String role;
    private String email;

    public UserModel() {} // חובה לפיירבייס

    public UserModel(String uid, String name, String role, String email) {
        this.uid = uid;
        this.name = name;
        this.role = role;
        this.email = email;
    }

    public String getUid() { return uid; }
    public String getName() { return name; }
    public String getRole() { return role; }
    public String getEmail() { return email; }
}