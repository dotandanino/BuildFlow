package com.example.buildflow.model;

import com.google.firebase.firestore.Exclude;
import java.util.List;

public class Professional {
    private String userId;
    private String name;
    private String profession;
    private String avatarUrl;
    private double rating;
    private int reviewsCount;
    private double hourlyRate;
    private int totalJobs;
    private String description;
    private boolean isVerified;
    private String phoneNumber;
    private String email;

    // to calculate the distance
    private List<String> serviceCities; // locations
    private double latitude;
    private double longitude;

    // temporary and will not be saved in firestore
    private double distanceFromUser = 0.0;

    // בנאי ריק חובה עבור Firestore
    public Professional() {}

    // בנאי מלא
    public Professional(String userId, String name, String profession, String avatarUrl,
                        double rating, int reviewsCount, double hourlyRate, int totalJobs,
                        String description, boolean isVerified, String phoneNumber,
                        List<String> serviceCities, double latitude, double longitude) {
        this.userId = userId;
        this.name = name;
        this.profession = profession;
        this.avatarUrl = avatarUrl;
        this.rating = rating;
        this.reviewsCount = reviewsCount;
        this.hourlyRate = hourlyRate;
        this.totalJobs = totalJobs;
        this.description = description;
        this.isVerified = isVerified;
        this.phoneNumber = phoneNumber;
        this.serviceCities = serviceCities;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    // --- Getters & Setters ---

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getProfession() { return profession; }
    public void setProfession(String profession) { this.profession = profession; }

    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }

    public double getRating() { return rating; }
    public void setRating(double rating) { this.rating = rating; }

    public int getReviewsCount() { return reviewsCount; }
    public void setReviewsCount(int reviewsCount) { this.reviewsCount = reviewsCount; }

    public double getHourlyRate() { return hourlyRate; }
    public void setHourlyRate(double hourlyRate) { this.hourlyRate = hourlyRate; }

    public int getTotalJobs() { return totalJobs; }
    public void setTotalJobs(int totalJobs) { this.totalJobs = totalJobs; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public boolean isVerified() { return isVerified; }
    public void setVerified(boolean verified) { isVerified = verified; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public List<String> getServiceCities() { return serviceCities; }
    public void setServiceCities(List<String> serviceCities) { this.serviceCities = serviceCities; }

    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }

    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }

    // @exclude because we dont want to save this in firestore
    @Exclude
    public double getDistanceFromUser() { return distanceFromUser; }
    @Exclude
    public void setDistanceFromUser(double distanceFromUser) { this.distanceFromUser = distanceFromUser; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}