package com.example.buildflow.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import com.example.buildflow.model.AuthRepository;
import com.google.firebase.auth.FirebaseUser;

public class AuthViewModel extends AndroidViewModel {

    private AuthRepository repository;
    private LiveData<FirebaseUser> userLiveData;

    public AuthViewModel(@NonNull Application application) {
        super(application);
        repository = new AuthRepository();
        userLiveData = repository.getUserLiveData();
    }

    public LiveData<FirebaseUser> getUserLiveData() {
        return userLiveData;
    }

    public void login(String email, String password) {
        repository.login(email, password);
    }

    public void loginWithGoogle(String idToken) {
        repository.loginWithGoogle(idToken);
    }

    public void register(String email, String password, String name) {
        repository.register(email, password, name);
    }

    public void signUpWithGoogle(String idToken) {
        repository.signUpWithGoogle(idToken);
    }
}