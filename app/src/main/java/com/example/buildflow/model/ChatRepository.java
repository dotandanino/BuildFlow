package com.example.buildflow.model; // <--- שימי לב: זה עכשיו ב-model

import androidx.lifecycle.MutableLiveData;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatRepository {

    private final FirebaseFirestore db;
    private static ChatRepository instance;

    // Singleton - the only show of the class.
    public static ChatRepository getInstance() {
        if (instance == null) {
            instance = new ChatRepository();
        }
        return instance;
    }

    private ChatRepository() {
        db = FirebaseFirestore.getInstance();
    }

    // --- create a special ID (user1_user2) ---
    public String generateChatId(String userId1, String userId2) {
        List<String> ids = Arrays.asList(userId1, userId2);
        Collections.sort(ids);
        return ids.get(0) + "_" + ids.get(1);
    }

    // --- send a massage ---
    public void sendMessage(String projectId, String chatId, ChatMessage message, String chatName, String chatRole) {
        WriteBatch batch = db.batch();

        // 1. save the massege in the collection of messages
        CollectionReference messagesRef = db.collection("projects").document(projectId)
                .collection("chats").document(chatId).collection("messages");

        batch.set(messagesRef.document(message.getMessageId()), message);

        // 2. עדכון השיחה הכללית (Last Message)
        CollectionReference chatRef = db.collection("projects").document(projectId)
                .collection("chats");

        Map<String, Object> chatUpdates = new HashMap<>();
        chatUpdates.put("lastMessage", message.getContent());
        chatUpdates.put("time", message.getFormattedTime());
        chatUpdates.put("timestamp", message.getTimestamp());
        chatUpdates.put("participants", Arrays.asList(message.getSenderId(), message.getReceiverId()));

        // כאן אנחנו שומרים פרטים בסיסיים כדי להציג ברשימה
        // (הערה: זה ידרוס את השם בכל פעם, באפליקציה גדולה מנהלים את זה בנפרד, אבל לתרגיל זה מעולה)
        chatUpdates.put("name", chatName);
        chatUpdates.put("role", chatRole);
        chatUpdates.put("id", chatId); // שיהיה לנו את ה-ID גם בתוך המסמך

        batch.set(chatRef.document(chatId), chatUpdates, com.google.firebase.firestore.SetOptions.merge());

        batch.commit();
    }

    // --- האזנה להודעות בתוך צ'אט ---
    public void listenToMessages(String projectId, String chatId, MutableLiveData<List<ChatMessage>> messagesLiveData) {
        db.collection("projects").document(projectId)
                .collection("chats").document(chatId).collection("messages")
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) return;
                    if (value != null) {
                        List<ChatMessage> list = value.toObjects(ChatMessage.class);
                        messagesLiveData.postValue(list);
                    }
                });
    }

    // --- האזנה לרשימת הצ'אטים ---
    public void listenToAllChats(String projectId, String currentUserId, MutableLiveData<List<ChatConversation>> chatsLiveData) {
        db.collection("projects").document(projectId).collection("chats")
                .whereArrayContains("participants", currentUserId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) return;
                    if (value != null) {
                        List<ChatConversation> list = value.toObjects(ChatConversation.class);
                        chatsLiveData.postValue(list);
                    }
                });
    }


    public void getProjectUsers(String projectId, MutableLiveData<List<UserModel>> usersLiveData) {
        // הנחה: המשתמשים שמורים באוסף "users" ויש להם שדה "projectId" שמקשר אותם
        // או שיש תת-אוסף "team" בתוך הפרויקט. נלך על האופציה הנפוצה של אוסף users ראשי:

        db.collection("users")
                //.whereEqualTo("projectId", projectId) // תורידי את ההערה אם יש לך שדה כזה
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots != null) {
                        List<UserModel> users = queryDocumentSnapshots.toObjects(UserModel.class);
                        usersLiveData.postValue(users);
                    }
                })
                .addOnFailureListener(e -> {
                    // טיפול בשגיאה (אפשר להחזיר רשימה ריקה או לוג)
                });
    }

}