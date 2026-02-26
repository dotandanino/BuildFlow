package com.example.buildflow.viewmodel;

import android.net.Uri;
import android.util.Log;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.buildflow.model.Project;
import com.example.buildflow.model.ProjectRequest;
import com.example.buildflow.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class ProfileViewModel extends ViewModel {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseAuth mAuth = FirebaseAuth.getInstance();

    private final MutableLiveData<User> userLiveData = new MutableLiveData<>();
    private final MutableLiveData<Integer> totalRequestsLiveData = new MutableLiveData<>(0);
    private final MutableLiveData<Integer> activeRequestsLiveData = new MutableLiveData<>(0);
    private final MutableLiveData<Integer> tasksCompletedLiveData = new MutableLiveData<>(0);

    // LiveData חדש לרשימת המשתתפים!
    private final MutableLiveData<List<User>> projectMembersLiveData = new MutableLiveData<>();

    public LiveData<User> getUserLiveData() { return userLiveData; }
    public LiveData<Integer> getTotalRequestsLiveData() { return totalRequestsLiveData; }
    public LiveData<Integer> getActiveRequestsLiveData() { return activeRequestsLiveData; }
    public LiveData<Integer> getTasksCompletedLiveData() { return tasksCompletedLiveData; }
    public LiveData<List<User>> getProjectMembersLiveData() { return projectMembersLiveData; }

    public void loadProfileData(String currentProjectId) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) return;

        String uid = currentUser.getUid();

        db.collection("users").document(uid).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        User user = documentSnapshot.toObject(User.class);
                        userLiveData.postValue(user);
                    }
                });

        if (currentProjectId != null && !currentProjectId.isEmpty()) {
            db.collection("projects").document(currentProjectId).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        Project project = documentSnapshot.toObject(Project.class);

                        if (project != null && project.getRequests() != null) {
                            int total = 0, active = 0, done = 0;
                            for (ProjectRequest req : project.getRequests()) {
                                if (req.getReceiver() != null && req.getReceiver().equals(uid)) {
                                    total++;
                                    if ("Closed".equalsIgnoreCase(req.getStatus()) || "Done".equalsIgnoreCase(req.getStatus())) {
                                        done++;
                                    } else {
                                        active++;
                                    }
                                }
                            }
                            totalRequestsLiveData.postValue(total);
                            activeRequestsLiveData.postValue(active);
                            tasksCompletedLiveData.postValue(done);
                        }
                    });
        }
    }

    /**
     * הפונקציה החדשה: מביאה את כל המשתתפים בפרויקט ואת התפקידים שלהם
     */
    public void fetchProjectMembers(String projectId) {
        if (projectId == null || projectId.isEmpty()) return;

        db.collection("projects").document(projectId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    // מושכים את מערך ה-participants (רשימה של UIDs)
                    List<String> participantIds = (List<String>) documentSnapshot.get("participants");

                    // מושכים את מפת התפקידים (roles) מהפרויקט
                    java.util.Map<String, String> rolesMap = (java.util.Map<String, String>) documentSnapshot.get("roles");

                    if (participantIds != null && !participantIds.isEmpty()) {
                        // מעבירים גם את ה-UIDs וגם את מפת התפקידים
                        loadUsersData(participantIds, rolesMap);
                    } else {
                        projectMembersLiveData.postValue(new java.util.ArrayList<>()); // רשימה ריקה
                    }
                })
                .addOnFailureListener(e -> Log.e("ProfileViewModel", "Error loading project members", e));
    }

    // פונקציית עזר שהופכת את ה-UIDs למודלים של User ומשדכת להם את התפקיד
    private void loadUsersData(List<String> uids, java.util.Map<String, String> rolesMap) {
        List<User> members = new java.util.ArrayList<>();
        AtomicInteger tasksCompleted = new AtomicInteger(0);

        for (String uid : uids) {
            db.collection("users").document(uid).get()
                    .addOnSuccessListener(doc -> {
                        User user = doc.toObject(User.class);
                        if (user != null) {
                            // שידוך התפקיד! אם יש לו תפקיד במפה, נציב אותו, אחרת נרשום "Member"
                            if (rolesMap != null && rolesMap.containsKey(uid)) {
                                user.setProjectRole(rolesMap.get(uid));
                            } else {
                                user.setProjectRole("Member");
                            }
                            members.add(user);
                        }

                        // רק כשכל הקריאות מסתיימות, אנחנו מעדכנים את ה-LiveData
                        if (tasksCompleted.incrementAndGet() == uids.size()) {
                            projectMembersLiveData.postValue(members);
                        }
                    })
                    .addOnFailureListener(e -> {
                        if (tasksCompleted.incrementAndGet() == uids.size()) {
                            projectMembersLiveData.postValue(members);
                        }
                    });
        }
    }


    // מנקה את ה-LiveData אחרי שהפופ-אפ נפתח, כדי שלא יקפוץ שוב בעת סיבוב מסך
    public void clearProjectMembersEvent() {
        projectMembersLiveData.setValue(null);
    }

    public void uploadProfileImage(Uri imageUri) {
        // (הקוד הקודם שלך להעלאת תמונה נשאר ללא שינוי...)
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) return;
        String uid = currentUser.getUid();
        StorageReference storageRef = FirebaseStorage.getInstance().getReference().child("profile_images/" + uid + ".jpg");
        storageRef.putFile(imageUri).addOnSuccessListener(taskSnapshot -> {
            storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                String downloadUrl = uri.toString();
                db.collection("users").document(uid).update("profileImageUrl", downloadUrl).addOnSuccessListener(aVoid -> {
                    User currentUserObj = userLiveData.getValue();
                    if (currentUserObj != null) {
                        currentUserObj.setProfileImageUrl(downloadUrl);
                        userLiveData.postValue(currentUserObj);
                    }
                });
            });
        });
    }

    public void logout() {
        mAuth.signOut();
    }
}