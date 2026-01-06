package com.example.buildflow.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.buildflow.model.ChatConversation;
import com.example.buildflow.model.ChatRepository; // <--- הייבוא המעודכן

import java.util.List;

public class AllChatsViewModel extends ViewModel {

    private final ChatRepository repository;
    private final MutableLiveData<List<ChatConversation>> chats = new MutableLiveData<>();

    public AllChatsViewModel() {
        repository = ChatRepository.getInstance();
    }

    public LiveData<List<ChatConversation>> getChats() {
        return chats;
    }

    public void loadChats(String projectId, String currentUserId) {
        repository.listenToAllChats(projectId, currentUserId, chats);
    }
}