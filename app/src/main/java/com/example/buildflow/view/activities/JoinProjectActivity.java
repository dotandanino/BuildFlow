package com.example.buildflow.view.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.buildflow.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

public class JoinProjectActivity extends AppCompatActivity {
    private EditText etProjectId;
    private Spinner spinnerRole;
    private FirebaseFirestore db;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_join_project);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        db = FirebaseFirestore.getInstance();
        etProjectId = findViewById(R.id.etProjectId);
        spinnerRole = findViewById(R.id.spinnerRole);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        Button btnJoin = findViewById(R.id.btnJoin);
        btnJoin.setOnClickListener(v -> joinProject());
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
    private void joinProject() {
        String projectId = etProjectId.getText().toString().trim();
        String selectedRole = spinnerRole.getSelectedItem().toString();
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        if (projectId.isEmpty()) {
            Toast.makeText(this, "Please enter a Project ID", Toast.LENGTH_SHORT).show();
            return;
        }
        db.collection("projects").document(projectId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    //check this is a real project id
                    if (documentSnapshot.exists()) {
                        db.collection("projects").document(projectId)
                                .update(//we are updating like this and not take the project and update him in the code
                                        //because this could lead to problem if 2 people try to join at the same time.
                                        "participants", FieldValue.arrayUnion(currentUserId),//add him to the participants array
                                        "roles." + currentUserId, selectedRole// add him to the roles map
                                )
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(this, "Joined successfully!", Toast.LENGTH_LONG).show();
                                    Intent intent = new Intent(this, ChooseProjectActivity.class);
                                    startActivity(intent);
                                    finish();
                                })
                                .addOnFailureListener(e ->
                                        Toast.makeText(this, "Failed to join: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                                );

                    } else {
                        Toast.makeText(this, "Project ID not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error checking ID", Toast.LENGTH_SHORT).show()
                );
    }
}