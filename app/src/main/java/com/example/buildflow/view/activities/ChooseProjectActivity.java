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

import com.example.buildflow.model.DatabaseSeeder;
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
        // TODO: למחוק אחרי הרצה אחת!
        findViewById(R.id.imgLogo).setOnClickListener(v -> {
            DatabaseSeeder.seedProfessionals();
            Toast.makeText(this, "Seeding professionals... Check Firebase!", Toast.LENGTH_SHORT).show();
        });
        findViewById(R.id.logOutButton).setOnClickListener(v -> signOutUser());
        findViewById(R.id.btnAddProject).setOnClickListener(v -> startActivity(new Intent(this, CreateNewProjectActivity.class)));

        db = FirebaseFirestore.getInstance();
        // to hold the project list in the view
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
                // info we need for the next screen
                startActivity(intent);
            }
        });
        projectsRecyclerView.setAdapter(adapter);
        loadUserProjects();

        findViewById(R.id.joinProject).setOnClickListener(v -> startActivity(new Intent(this, JoinProjectActivity.class)));

        findViewById(R.id.cardRegisterPro).setOnClickListener(v -> {
            startActivity(new Intent(this, RegisterProActivity.class));
        });

        // we ask for notification permission
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (androidx.core.content.ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                androidx.core.app.ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        // if the user is not connected we want to send him for the login screen
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            Intent intent = new Intent(this, LoginActivity.class);
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
                .whereArrayContains("participants", userId)// take all the project that the user is in
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

                        // updating the adapter with the new data
                        adapter.updateData(loadedProjects);

                    } else {
                        // no projects found
                        // right now he just don't see any projects
                    }
                })
                .addOnFailureListener(e -> {
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