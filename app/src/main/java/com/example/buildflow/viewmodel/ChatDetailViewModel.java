package com.example.buildflow.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.buildflow.model.ChatMessage;
import com.example.buildflow.model.ChatRepository; // <--- הייבוא המעודכן

import java.util.List;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
public class ChatDetailViewModel extends ViewModel {

    private final ChatRepository repository;
    private final MutableLiveData<List<ChatMessage>> messages = new MutableLiveData<>();

    public ChatDetailViewModel() {
        // מקבלים את המופע היחיד של ה-Repository
        repository = ChatRepository.getInstance();
    }

    public LiveData<List<ChatMessage>> getMessages() {
        return messages;
    }

    public void startListening(String projectId, String chatId) {
        repository.listenToMessages(projectId, chatId, messages);
    }

    public void sendMessage(String projectId, String chatId, ChatMessage message, String receiverName, String role) {
        repository.sendMessage(projectId, chatId, message, receiverName, role);
    }

    // פונקציית עזר לייצור ID לצ'אט
    public String getChatId(String userId1, String userId2) {
        return repository.generateChatId(userId1, userId2);
    }

    public void uploadMediaAndSendMessage(String projectId, String chatId, String currentUserId, String receiverId, String currentUserName, String receiverName, String role, android.net.Uri mediaUri, String mediaType, String fileName) {
        // יצירת שם קובץ ייחודי
        String uniqueFileName = java.util.UUID.randomUUID().toString();
        StorageReference storageRef = FirebaseStorage.getInstance().getReference()
                .child("chat_media/" + chatId + "/" + uniqueFileName);

        storageRef.putFile(mediaUri).addOnSuccessListener(taskSnapshot -> {
            storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                long timestamp = System.currentTimeMillis();
                String messageId = java.util.UUID.randomUUID().toString();

                // כאן אנחנו משתמשים בשם הקובץ האמיתי!
                String fallbackText = mediaType.equals("image") ? "📷 Image" : "📎 " + fileName;

                ChatMessage newMessage = new ChatMessage(
                        messageId, projectId, currentUserId, receiverId, currentUserName, fallbackText, timestamp
                );

                // שומרים את הנתונים המיוחדים
                newMessage.setMessageType(mediaType);
                newMessage.setMediaUrl(uri.toString());
                newMessage.setFileName(fileName);

                // שליחת ההודעה דרך ה-Repository
                sendMessage(projectId, chatId, newMessage, receiverName, role);
            });
        }).addOnFailureListener(e -> {
            // כאן אפשר להוסיף טיפול בשגיאות העלאה
        });
    }
}