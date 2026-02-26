package com.example.buildflow.model;

/**
 * a class for the expenses in the budget fragment
 */
public class Expense {
    private String id;
    private String title;
    private double amount;
    private String category;
    private long timestamp;// date and time of the expense in milliseconds.

    public Expense() {}

    public Expense(String id, String title, double amount, String category, long timestamp) {
        this.id = id;
        this.title = title;
        this.amount = amount;
        this.category = category;
        this.timestamp = timestamp;
    }

    // --- Getters & Setters ---
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}