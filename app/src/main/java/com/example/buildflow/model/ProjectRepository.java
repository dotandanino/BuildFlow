package com.example.buildflow.model;

import androidx.lifecycle.MutableLiveData;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProjectRepository {
    private FirebaseFirestore db; // Connection to Firestore database
    private MutableLiveData<Boolean> projectCreationLiveData;

    public ProjectRepository() {
        this.db = FirebaseFirestore.getInstance();
        this.projectCreationLiveData = new MutableLiveData<>();
    }

    public MutableLiveData<Boolean> getProjectCreationLiveData() {
        return projectCreationLiveData;
    }

    /**
     * create new project and save him to the database
     * @param project - the project we want to create
     */
    public void createNewProject(Project project) {
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        // try to get the current user
        db.collection("users").document(currentUserId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // save the project with his ID as name
                        db.collection("projects").document(project.getId())
                                .set(project)
                                .addOnSuccessListener(aVoid -> {
                                    projectCreationLiveData.postValue(true);
                                })
                                .addOnFailureListener(e -> {
                                    projectCreationLiveData.postValue(false);
                                });
                    } else {
                        projectCreationLiveData.postValue(false);
                    }
                })
                .addOnFailureListener(e -> {
                    projectCreationLiveData.postValue(false);
                });
    }
}