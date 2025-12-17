package com.example.buildflow.view.activities;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.buildflow.R;

public class CreateNewProjectActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_create_new_project);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        findViewById(R.id.btnSelectRenovation).setOnClickListener(v -> chooseRenovationProject());
        findViewById(R.id.btnSelectConstruction).setOnClickListener(v ->chooseConstructionProject());
        findViewById(R.id.btnBack).setOnClickListener(v-> startActivity(new Intent(this, ChooseProjectActivity.class)));
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
    private void chooseConstructionProject() {
        Intent intent = new Intent(CreateNewProjectActivity.this, AddProjectActivity.class);
        intent.putExtra("TYPE","Construction");
        startActivity(intent);
        finish();
    }

    private void chooseRenovationProject() {
        Intent intent = new Intent(CreateNewProjectActivity.this, AddProjectActivity.class);
        intent.putExtra("TYPE","Renovation");
        startActivity(intent);
        finish();
    }
}