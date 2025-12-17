package com.example.buildflow.view.activities;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.buildflow.R;
import com.example.buildflow.model.Project;
import com.example.buildflow.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;

public class AddProjectActivity extends AppCompatActivity {
    private int currentProgress;
    private Spinner spinnerStatus;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_project);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        Intent intent = getIntent();
        String type = intent.getStringExtra("TYPE");
        if (type != null && type.equals("Construction")) {
            updateUiForConstruction();
        }
        findViewById(R.id.btnBack).setOnClickListener(V->{
            Intent backIntent = new Intent(this, CreateNewProjectActivity.class);
            startActivity(backIntent);
            finish();
        });
        Button btnContinue = findViewById(R.id.btnContinue);
        btnContinue.setOnClickListener(V->addProjectToFirebase());


        SeekBar sbProgress = findViewById(R.id.sbProgress);
        TextView tvProgressValue = findViewById(R.id.tvProgressValue);

        sbProgress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // *** זה הקסם! ***
                // בכל פעם שהערך משתנה, אנחנו מעדכנים את הטקסט
                tvProgressValue.setText(progress + "%");
                currentProgress = progress;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                //nothing to do here now
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                //nothing to do here now
            }
        });

        EditText etStartDate = findViewById(R.id.etStartDate);
        etStartDate.setOnClickListener(v -> {
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    this,
                    (view, selectedYear, selectedMonth, selectedDay) -> {
                        //we have +1 because the month starts from 0.
                        String date = selectedDay + "/" + (selectedMonth + 1) + "/" + selectedYear;
                        etStartDate.setText(date);
                    },
                    year, month, day);

            datePickerDialog.show();
        });

        spinnerStatus = findViewById(R.id.spinnerStatus);

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
    private void addProjectToFirebase() {

        String projectName=((android.widget.EditText) findViewById(R.id.etProjectName)).getText().toString();
        String startDate=((android.widget.EditText) findViewById(R.id.etStartDate)).getText().toString();
        String selectedStatus = spinnerStatus.getSelectedItem().toString();
        if(projectName.isEmpty() || startDate.isEmpty()){
            android.widget.Toast.makeText(this, "you must enter project name and start date", android.widget.Toast.LENGTH_SHORT).show();
            return;
        }
        String description = ((android.widget.EditText) findViewById(R.id.etDescription)).getText().toString();
        Intent intent=getIntent();
        String projectType=intent.getStringExtra("TYPE");
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String projectId = db.collection("projects").document().getId();
        db.collection("users").document(currentUserId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        User user = documentSnapshot.toObject(User.class);
                        Project newProject = new Project(projectId,projectName, projectType,startDate,currentProgress,0,selectedStatus,description);
                        assert user != null;
                        newProject.addPartner(user,"Owner");
                        db.collection("projects").document(projectId).set(newProject);
                        Intent backIntent = new Intent(this, CreateNewProjectActivity.class);
                        startActivity(backIntent);
                        finish();
                    } else {
                        //to complete
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to fetch user data", Toast.LENGTH_SHORT).show();
                });

    }

    private void updateUiForConstruction() {
        TextView title = findViewById(R.id.tvPageTitle);
        title.setText("New Construction Project");
        View header = findViewById(R.id.headerLayout);
        header.setBackgroundResource(R.drawable.bg_construction_gradient);

        TextView projectTypeTitle = findViewById(R.id.tvProjectTypeTitle);
        projectTypeTitle.setText("Construction Project");

        View badgeLayout = findViewById(R.id.badgeLayout);
        badgeLayout.setBackgroundResource(R.drawable.bg_construction_gradient);

        ImageView imgProjectTypeIcon = findViewById(R.id.imgProjectTypeIcon);
        imgProjectTypeIcon.setImageResource(R.drawable.ic_building);

        Button btnContinue = findViewById(R.id.btnContinue);
        btnContinue.setBackgroundResource(R.drawable.bg_construction_gradient);
    }
}