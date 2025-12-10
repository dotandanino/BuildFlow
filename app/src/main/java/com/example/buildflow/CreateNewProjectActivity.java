package com.example.buildflow;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

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
        findViewById(R.id.btnSelectRenovation).setOnClickListener(chooseRenovationProject());
        findViewById(R.id.btnSelectConstruction).setOnClickListener(chooseConstructionProject());
    }

    private View.OnClickListener chooseConstructionProject() {
        Intent intent = new Intent(CreateNewProjectActivity.this, addProjectActivity.class);
        intent.putExtra("TYPE","Renovation");
        startActivity(intent);
        finish();
        return null;
    }

    private View.OnClickListener chooseRenovationProject() {
        Intent intent = new Intent(CreateNewProjectActivity.this, addProjectActivity.class);
        intent.putExtra("TYPE","Construction");
        startActivity(intent);
        finish();
        return null;
    }
}