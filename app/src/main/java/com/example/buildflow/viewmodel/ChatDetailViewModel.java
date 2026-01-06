package com.example.buildflow.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.buildflow.model.ChatMessage;
import com.example.buildflow.model.ChatRepository; // <--- הייבוא המעודכן

import java.util.List;

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
}