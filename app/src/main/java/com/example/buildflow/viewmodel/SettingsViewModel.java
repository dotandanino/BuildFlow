package com.example.buildflow.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

public class SettingsViewModel extends ViewModel {

    private final FirebaseAuth auth = FirebaseAuth.getInstance();
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    private final MutableLiveData<String> toastMessageLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> accountDeletedLiveData = new MutableLiveData<>(false);

    public LiveData<String> getToastMessage() { return toastMessageLiveData; }
    public LiveData<Boolean> getAccountDeleted() { return accountDeletedLiveData; }

    public void sendPasswordResetEmail() {
        FirebaseUser user = auth.getCurrentUser();
        if (user != null && user.getEmail() != null) {
            String emailAddress = user.getEmail();
            auth.sendPasswordResetEmail(emailAddress)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            toastMessageLiveData.setValue("Password reset email sent to " + emailAddress);
                        } else {
                            toastMessageLiveData.setValue("Failed: " + task.getException().getMessage());
                        }
                    });
        }
    }

    public void deleteAccount() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) return;

        String uid = user.getUid();

        // 1. מחיקת המשתמש מקולקשן 'users'
        db.collection("users").document(uid).delete().addOnCompleteListener(userDeleteTask -> {

            // 2. מציאת כל הפרויקטים שבהם המשתמש משתתף ומחיקתו מהם
            db.collection("projects").whereArrayContains("participants", uid).get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        WriteBatch batch = db.batch(); // משתמשים ב-Batch כדי לעדכן הכל במכה אחת

                        for (DocumentSnapshot doc : queryDocumentSnapshots) {
                            DocumentReference projRef = doc.getReference();

                            // מסיר את ה-UID ממערך המשתתפים (participants)
                            batch.update(projRef, "participants", FieldValue.arrayRemove(uid));

                            // מסיר את ה-UID ממפת התפקידים (roles), אם קיים
                            batch.update(projRef, "roles." + uid, FieldValue.delete());
                        }

                        // מריצים את כל העדכונים לפרויקטים
                        batch.commit().addOnCompleteListener(batchTask -> {

                            // 3. רק בסוף הניקיון ב-Firestore - מוחקים את חשבון ההתחברות (Auth)
                            user.delete().addOnCompleteListener(authTask -> {
                                if (authTask.isSuccessful()) {
                                    toastMessageLiveData.postValue("Account and data completely deleted.");
                                    accountDeletedLiveData.postValue(true);
                                } else {
                                    toastMessageLiveData.postValue("Data deleted, but failed to delete auth account. You may need to log in again to delete.");
                                }
                            });
                        });
                    })
                    .addOnFailureListener(e -> {
                        toastMessageLiveData.postValue("Failed to clean up user data: " + e.getMessage());
                    });
        });
    }
}