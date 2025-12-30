package com.example.buildflow.model;

import androidx.lifecycle.MutableLiveData;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProjectRepository {
    private FirebaseFirestore db;
    private MutableLiveData<Boolean> projectCreationLiveData;

    public ProjectRepository() {
        this.db = FirebaseFirestore.getInstance();
        this.projectCreationLiveData = new MutableLiveData<>();
    }

    public MutableLiveData<Boolean> getProjectCreationLiveData() {
        return projectCreationLiveData;
    }

    // הפונקציה ששומרת את הפרויקט ב-Firebase
    public void createNewProject(Project project) {
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // בדיקה שהמשתמש קיים (שמרתי על הלוגיקה שלך)
        db.collection("users").document(currentUserId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // שמירת הפרויקט
                        db.collection("projects").document(project.getId()) // שימי לב: Project צריך להחזיק את ה-ID שלו
                                .set(project)
                                .addOnSuccessListener(aVoid -> {
                                    // הצלחה!
                                    projectCreationLiveData.postValue(true);
                                })
                                .addOnFailureListener(e -> {
                                    // כישלון בשמירה
                                    projectCreationLiveData.postValue(false);
                                });
                    } else {
                        // המשתמש לא נמצא
                        projectCreationLiveData.postValue(false);
                    }
                })
                .addOnFailureListener(e -> {
                    projectCreationLiveData.postValue(false);
                });
    }
}