package com.example.buildflow.model;

import androidx.lifecycle.MutableLiveData;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

public class AuthRepository {
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private MutableLiveData<FirebaseUser> userLiveData;

    public AuthRepository() {
        this.mAuth = FirebaseAuth.getInstance();
        this.db = FirebaseFirestore.getInstance();
        this.userLiveData = new MutableLiveData<>();
    }

    public MutableLiveData<FirebaseUser> getUserLiveData() {
        return userLiveData;
    }

    // --- התחברות רגילה ---
    public void login(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        updateFcmToken(); // <--- הוספנו את זה!
                        userLiveData.postValue(mAuth.getCurrentUser());
                    } else {
                        userLiveData.postValue(null);
                    }
                });
    }

    // --- התחברות גוגל (Login) ---
    public void loginWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        updateFcmToken(); // <--- הוספנו את זה!
                        FirebaseUser user = mAuth.getCurrentUser();
                        // כאן אפשר להוסיף בדיקה אם המשתמש קיים ב-DB
                        userLiveData.postValue(user);
                    } else {
                        userLiveData.postValue(null);
                    }
                });
    }

    // --- הרשמה במייל (Register) ---
    public void register(String email, String password, String nameInput) {
        // 1. קביעת השם (אם ריק - לוקחים מהמייל)
        String finalName;
        if (nameInput == null || nameInput.isEmpty()) {
            int index = email.indexOf('@');
            if (index != -1) finalName = email.substring(0, index);
            else finalName = email;
        } else {
            finalName = nameInput;
        }

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        if (firebaseUser != null) {
                            saveUserToFirestore(firebaseUser, finalName);
                        }
                    } else {
                        userLiveData.postValue(null);
                    }
                });
    }

    // --- הרשמה בגוגל (SignUp with Google) ---
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
    // --- שמירה ב-Firestore (פרטית) ---
    private void saveUserToFirestore(FirebaseUser firebaseUser, String name) {
        User user = new User(firebaseUser.getUid(), firebaseUser.getEmail(), name);
        db.collection("users").document(user.getUid())
                .set(user, SetOptions.merge())
                .addOnSuccessListener(aVoid -> {
                    // רק אחרי השמירה המוצלחת אנחנו מעדכנים שהכל תקין
                    userLiveData.postValue(firebaseUser);
                    updateFcmToken(); //about notification
                })
                .addOnFailureListener(e -> {
                    userLiveData.postValue(null);
                });
    }
    // פונקציה ששומרת את הטוקן של המכשיר הנוכחי ב-Firestore
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