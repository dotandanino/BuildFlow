package com.example.buildflow.model;

import androidx.lifecycle.MutableLiveData;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

public class AuthRepository {
    private FirebaseAuth mAuth; // handle authentication
    private FirebaseFirestore db; // Connection to Firestore database
    private MutableLiveData<FirebaseUser> userLiveData; // Observes current user state (LoggedIn / Null)

    public AuthRepository() {
        this.mAuth = FirebaseAuth.getInstance();
        this.db = FirebaseFirestore.getInstance();
        this.userLiveData = new MutableLiveData<>();
    }

    public MutableLiveData<FirebaseUser> getUserLiveData() {
        return userLiveData;
    }

    /**
     * Login with email and password
     * @param email - email
     * @param password -password
     */
    public void login(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        updateFcmToken(); // make the notification available
                        userLiveData.postValue(mAuth.getCurrentUser());
                    } else {
                        userLiveData.postValue(null);
                    }
                });
    }

    /**
     * Login with Google
     * @param idToken - the google token for login
     */
    public void loginWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        updateFcmToken(); // make the notification available
                        FirebaseUser user = mAuth.getCurrentUser();
                        userLiveData.postValue(user);
                    } else {
                        userLiveData.postValue(null);
                    }
                });
    }

    /**
     * Register with email , name and password
     * @param email - email
     * @param password - password
     * @param nameInput - nameInput
     */
    public void register(String email, String password, String nameInput) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        if (firebaseUser != null) {
                            saveUserToFirestore(firebaseUser, nameInput);
                        }
                    } else {
                        userLiveData.postValue(null);
                    }
                });
    }

    /**
     * Register with Google
     * @param idToken - the token from google
     */
    public void signUpWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            db.collection("users").document(user.getUid()).get()
                                    .addOnSuccessListener(documentSnapshot -> {
                                        if (documentSnapshot.exists()) {
                                            updateFcmToken();
                                            userLiveData.postValue(user);
                                        } else {
                                            // --- משתמש חדש: יצירת שם חכם ---
                                            String finalName = user.getDisplayName();
                                            String email = user.getEmail(); // הנה הקריאה מהמייל שביקשת

                                             if (finalName == null || finalName.isEmpty()) {
                                                if (email != null && email.contains("@")) {
                                                    finalName = email.substring(0, email.indexOf('@'));
                                                } else {
                                                    finalName = "Google User";
                                                }
                                            }

                                            saveUserToFirestore(user, finalName);
                                        }
                                    })
                                    .addOnFailureListener(e -> {
                                        userLiveData.postValue(null);
                                    });
                        }
                    } else {
                        userLiveData.postValue(null);
                    }
                });
    }

    /**
     * save the user to firestore
     * @param firebaseUser - the user that registered
     * @param name - the name to save the user
     */
    private void saveUserToFirestore(FirebaseUser firebaseUser, String name) {
        User user = new User(firebaseUser.getUid(), firebaseUser.getEmail(), name);
        db.collection("users").document(user.getUid())
                .set(user, SetOptions.merge())
                .addOnSuccessListener(aVoid -> {
                    userLiveData.postValue(firebaseUser);
                    updateFcmToken(); //about notification
                })
                .addOnFailureListener(e -> {
                    userLiveData.postValue(null);
                });
    }

    /**
     * we need the fcm token to send notifications
     */
    private void updateFcmToken() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            com.google.firebase.messaging.FirebaseMessaging.getInstance().getToken()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && task.getResult() != null) {
                            String token = task.getResult();
                            // עדכון השדה fcmToken במסמך של המשתמש
                            db.collection("users").document(currentUser.getUid())
                                    .update("fcmToken", token);
                        }
                    });
        }
    }
}