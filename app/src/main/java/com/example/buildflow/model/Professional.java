package com.example.buildflow.model;

public class Professional {
    private String userId;
    private String profession;

    private String phoneNumber;
    private String area;
    private double rating;
    private int reviewsCount;

    public Professional() {
    }

    public Professional(String userId, String profession, String phoneNumber, String area,double rating,int reviewsCount) {
        this.userId = userId;
        this.profession = profession;
        this.phoneNumber = phoneNumber;
        this.area = area;
        this.rating = rating;
        this.reviewsCount = reviewsCount;
    }

    // --- Getters and Setters ---
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getProfession() { return profession; }
    public void setProfession(String profession) { this.profession = profession; }
    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getArea() { return area; }
    public void setArea(String area) { this.area = area; }

    public double getRating() { return rating; }
    public void setRating(double rating) { this.rating = rating; }

    public int getReviewsCount() {
        return reviewsCount;
    }

    public void setReviewsCount(int reviewsCount) {
        this.reviewsCount = reviewsCount;
    }
}