package com.example.buildflow.viewmodel;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.buildflow.model.ChatRepository;
import com.example.buildflow.model.UserModel;
import com.google.firebase.auth.FirebaseAuth; // הוספנו

import java.util.ArrayList;
import java.util.List;

public class PickUserViewModel extends ViewModel {

    private final ChatRepository repository;
    public final MutableLiveData<List<UserModel>> users = new MutableLiveData<>();

    public PickUserViewModel() {
        repository = ChatRepository.getInstance();
    }

    public void loadUsers(String projectId) {
        repository.getProjectUsers(projectId, new MutableLiveData<List<UserModel>>() {
            @Override
            public void postValue(List<UserModel> allUsers) {
                String myId = FirebaseAuth.getInstance().getUid();
                List<UserModel> filteredList = new ArrayList<>();

                if (allUsers != null) {
                    for (UserModel user : allUsers) {
                        if (myId != null && !user.getUid().equals(myId)) {
                            filteredList.add(user);
                        }
                    }
                }
                users.setValue(filteredList);
            }
        });
    }
}