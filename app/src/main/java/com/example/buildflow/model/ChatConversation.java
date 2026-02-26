package com.example.buildflow.model;

/**
 * a class for the chat conversation
 */
public class ChatConversation {
    private String id;
    private String name;
    private String role;
    private String lastMessage;
    private String time;
    private int unreadCount;
    private boolean isOnline;
    public ChatConversation() {
        // Default constructor required for Firebase.
    }

    public ChatConversation(String id, String name, String role, String lastMessage, String time, int unreadCount, boolean isOnline) {
        this.id = id;
        this.name = name;
        this.role = role;
        this.lastMessage = lastMessage;
        this.time = time;
        this.unreadCount = unreadCount;
        this.isOnline = isOnline;
    }

    //  Getters
    public String getName() { return name; }
    public String getRole() { return role; }
    public String getLastMessage() { return lastMessage; }
    public String getTime() { return time; }
    public int getUnreadCount() { return unreadCount; }
    public boolean isOnline() { return isOnline; }
    public String getId() { return id; }

}