package com.example.buildflow.view.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.buildflow.view.adapters.ProjectsAdapter;
import com.example.buildflow.R;
import com.example.buildflow.model.Project;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class ChooseProjectActivity extends AppCompatActivity {
    private RecyclerView projectsRecyclerView;
    private ProjectsAdapter adapter;
    private List<Project> projectList;
    private FirebaseFirestore db;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_choose_project);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        findViewById(R.id.logOutButton).setOnClickListener(v -> signOutUser());
        findViewById(R.id.btnAddProject).setOnClickListener(v -> startActivity(new Intent(this, CreateNewProjectActivity.class)));

        db = FirebaseFirestore.getInstance();
        projectsRecyclerView = findViewById(R.id.projectsRecyclerView);
        projectsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        projectList = new ArrayList<>();
        adapter = new ProjectsAdapter(projectList, new ProjectsAdapter.OnProjectClickListener() {
            @Override
            public void onProjectClick(Project project) {
                Intent intent = new Intent(ChooseProjectActivity.this, ProjectViewActivity.class);
                intent.putExtra("PROJECT_ID", project.getId());
                intent.putExtra("PROJECT_NAME", project.getName());
                intent.putExtra("PROJECT_TYPE", project.getType());
                intent.putExtra("ROLE", project.getRole(FirebaseAuth.getInstance().getCurrentUser().getUid()));

                startActivity(intent);
            }
        });
        projectsRecyclerView.setAdapter(adapter);
        loadUserProjects();

        findViewById(R.id.joinProject).setOnClickListener(v -> startActivity(new Intent(this, JoinProjectActivity.class)));
    }

    @Override
    protected void onStart() {
        super.onStart();
        com.google.firebase.auth.FirebaseUser currentUser = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Intent intent = new Intent(this, LoginActivity.class);

            //the flags are to make sure he will have no option to come back to this activity with the back button of the phone
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

            startActivity(intent);
            finish();
        }
    }
    private void loadUserProjects() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser == null) {
            signOutUser();
            return;
        }

        String userId = currentUser.getUid();
        // searching for projects that the user is a participant
        db.collection("projects")
                .whereArrayContains("participants", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        List<Project> loadedProjects = new ArrayList<>();

                        for (DocumentSnapshot document : queryDocumentSnapshots) {
                            //automatic - casting to project
                            Project project = document.toObject(Project.class);

                            if (project != null) {
                                project.setId(document.getId());
                                if (project.getMembersCount() == 0 && document.get("participants") != null) {
                                    List<String> parts = (List<String>) document.get("participants");
                                    project.setMembersCount(parts.size());
                                }
                                loadedProjects.add(project);
                            }
                        }

                        // עדכון ה-Adapter עם הרשימה החדשה
                        adapter.updateData(loadedProjects);

                    } else {
                        // המשתמש לא נמצא באף פרויקט כרגע
                        // אפשר להציג הודעה או להשאיר ריק
//                         Toast.makeText(this, "No projects found.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
//                    Log.e("ChooseProject", "Error loading projects", e);
                    Toast.makeText(this, "Failed to load projects.", Toast.LENGTH_SHORT).show();
                });
    }

    public void signOutUser() {
        FirebaseAuth.getInstance().signOut();

        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
}