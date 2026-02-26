//package com.example.buildflow.model;
//
//import android.util.Log;
//
//import androidx.lifecycle.MutableLiveData;
//import com.google.firebase.auth.AuthCredential;
//import com.google.firebase.auth.FirebaseAuth;
//import com.google.firebase.auth.FirebaseUser;
//import com.google.firebase.auth.GoogleAuthProvider;
//import com.google.firebase.firestore.FirebaseFirestore;
//import com.google.firebase.firestore.SetOptions;
//
//public class AuthRepository {
//    private FirebaseAuth mAuth; // handle authentication
//    private FirebaseFirestore db; // Connection to Firestore database
//    private MutableLiveData<FirebaseUser> userLiveData; // Observes current user state (LoggedIn / Null)
//
//    public AuthRepository() {
//        this.mAuth = FirebaseAuth.getInstance();
//        this.db = FirebaseFirestore.getInstance();
//        this.userLiveData = new MutableLiveData<>();
//    }
//
//    public MutableLiveData<FirebaseUser> getUserLiveData() {
//        return userLiveData;
//    }
//
//    /**
//     * Login with email and password
//     * @param email - email
//     * @param password -password
//     */
//    public void login(String email, String password) {
//        mAuth.signInWithEmailAndPassword(email, password)
//                .addOnCompleteListener(task -> {
//                    if (task.isSuccessful()) {
//                        updateFcmToken(); // make the notification available
//                        userLiveData.postValue(mAuth.getCurrentUser());
//                    } else {
//                        userLiveData.postValue(null);
//                    }
//                });
//    }
//
//    /**
//     * Login with Google
//     * @param idToken - the google token for login
//     */
//    public void loginWithGoogle(String idToken) {
//        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
//        mAuth.signInWithCredential(credential)
//                .addOnCompleteListener(task -> {
//                    if (task.isSuccessful()) {
//                        updateFcmToken(); // make the notification available
//                        FirebaseUser user = mAuth.getCurrentUser();
//                        userLiveData.postValue(user);
//                    } else {
//                        userLiveData.postValue(null);
//                    }
//                });
//    }
//
//    /**
//     * Register with email , name and password
//     * @param email - email
//     * @param password - password
//     * @param nameInput - nameInput
//     */
//    public void register(String email, String password, String nameInput) {
//        mAuth.createUserWithEmailAndPassword(email, password)
//                .addOnCompleteListener(task -> {
//                    if (task.isSuccessful()) {
//                        Log.d("AuthRepo", "Step 1: Auth successful! Moving to Firestore...");
//                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
//                        if (firebaseUser != null) {
//                            saveUserToFirestore(firebaseUser, nameInput);
//                        }
//                    } else {
//                        // in case there was a failure we can print why
//                        Log.e("AuthRepo", "CRASH in Auth: " + task.getException().getMessage());
//                        userLiveData.postValue(null);
//                    }
//                });
//    }
//
//    /**
//     * Register with Google
//     * @param idToken - the token from google
//     */
//    public void signUpWithGoogle(String idToken) {
//        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
//        mAuth.signInWithCredential(credential)
//                .addOnCompleteListener(task -> {
//                    if (task.isSuccessful()) {
//                        FirebaseUser user = mAuth.getCurrentUser();
//                        if (user != null) {
//                            db.collection("users").document(user.getUid()).get()
//                                    .addOnSuccessListener(documentSnapshot -> {
//                                        if (documentSnapshot.exists()) {
//                                            updateFcmToken();
//                                            userLiveData.postValue(user);
//                                        } else {
//                                            // if this is a new user that registered with his email we will extract the
//                                            String finalName = user.getDisplayName();
//                                            String email = user.getEmail();
//
//                                            if (finalName == null || finalName.isEmpty()) {
//                                                if (email != null && email.contains("@")) {
//                                                    finalName = email.substring(0, email.indexOf('@'));
//                                                } else {
//                                                    finalName = "Google User";
//                                                }
//                                            }
//
//                                            saveUserToFirestore(user, finalName);
//                                        }
//                                    })
//                                    .addOnFailureListener(e -> {
//                                        userLiveData.postValue(null);
//                                    });
//                        }
//                    } else {
//                        userLiveData.postValue(null);
//                    }
//                });
//    }
//
//    /**
//     * save the user to firestore
//     * @param firebaseUser - the user that registered
//     * @param name - the name to save the user
//     */
//    private void saveUserToFirestore(FirebaseUser firebaseUser, String name) {
//        User user = new User(firebaseUser.getUid(), firebaseUser.getEmail(), name);
//        if (firebaseUser.getPhotoUrl() != null) {
//            user.setProfileImageUrl(firebaseUser.getPhotoUrl().toString());
//        }
//        db.collection("users").document(user.getUid())
//                .set(user, SetOptions.merge())
//                .addOnSuccessListener(aVoid -> {
//                    Log.d("AuthRepo", "Step 2: User saved to Firestore successfully!");
//                    userLiveData.postValue(firebaseUser);
//                    updateFcmToken(); //about notification
//                })
//                .addOnFailureListener(e -> {
//                    // if the firestore blocked us we will print the error.
//                    Log.e("AuthRepo", "CRASH in Firestore: " + e.getMessage());
//                    userLiveData.postValue(null);
//                });
//    }
//
//
//    private void updateFcmToken() {
//        FirebaseUser currentUser = mAuth.getCurrentUser();
//        if (currentUser != null) {
//            com.google.firebase.messaging.FirebaseMessaging.getInstance().getToken()
//                    .addOnCompleteListener(task -> {
//                        if (task.isSuccessful() && task.getResult() != null) {
//                            String token = task.getResult();
//                            // we will update the FCM token to make sure we will notify the correct phone
//                            db.collection("users").document(currentUser.getUid())
//                                    .update("fcmToken", token);
//                        }
//                    });
//        }
//    }
//}