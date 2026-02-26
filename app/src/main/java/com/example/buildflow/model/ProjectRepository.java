package com.example.buildflow.model;

import android.util.Log;

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
        Log.d("ProjectRepo", "Starting to create project for User UID: " + currentUserId);

        // try to get the current user
        db.collection("users").document(currentUserId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Log.d("ProjectRepo", "User document found! Saving project to Firestore...");

                        // save the project with his ID as name
                        db.collection("projects").document(project.getId())
                                .set(project)
                                .addOnSuccessListener(aVoid -> {
                                    Log.d("ProjectRepo", "Project created SUCCESSFULLY in Firestore!");
                                    projectCreationLiveData.postValue(true);
                                })
                                .addOnFailureListener(e -> {
                                    Log.e("ProjectRepo", "CRASH: Failed to save project to 'projects' collection", e);
                                    projectCreationLiveData.postValue(false);
                                });
                    } else {
                        // הבעיה כנראה כאן!
                        Log.e("ProjectRepo", "CRASH: User document DOES NOT EXIST in the 'users' collection!");
                        projectCreationLiveData.postValue(false);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("ProjectRepo", "CRASH: Failed to fetch user from 'users' collection (Check Permissions/Internet)", e);
                    projectCreationLiveData.postValue(false);
                });
    }

}