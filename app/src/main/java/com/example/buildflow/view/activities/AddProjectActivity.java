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
import androidx.lifecycle.ViewModelProvider; // חשוב!

import com.example.buildflow.R;
import com.example.buildflow.model.Project;
import com.example.buildflow.viewmodel.ProjectViewModel; // הייבוא החדש
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore; // אפשר למחוק את זה, כבר לא בשימוש פה!

import java.util.Calendar;
import java.util.UUID; // בשביל לייצר ID

public class AddProjectActivity extends AppCompatActivity {

    private int currentProgress;
    private Spinner spinnerStatus;
    private ProjectViewModel projectViewModel; // מחזיקים את ה-ViewModel החדש

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

        // init the view model
        projectViewModel = new ViewModelProvider(this).get(ProjectViewModel.class);

        // observer to listen when the project is created
        projectViewModel.getProjectCreationLiveData().observe(this, isSuccess -> {
            if (isSuccess) {
                Toast.makeText(AddProjectActivity.this, "Project Created Successfully!", Toast.LENGTH_SHORT).show();
                Intent backIntent = new Intent(AddProjectActivity.this, ChooseProjectActivity.class);
                backIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(backIntent);
                finish();
            } else {
                Toast.makeText(AddProjectActivity.this, "Failed to create project", Toast.LENGTH_SHORT).show();
            }
        });

        // Check the type of project and update the UI accordingly
        Intent intent = getIntent();
        String type = intent.getStringExtra("TYPE");
        if (type != null && type.equals("Construction")) {
            updateUiForConstruction();
        }
        /*else  is the defualt*/

        findViewById(R.id.btnBack).setOnClickListener(V -> {
            Intent backIntent = new Intent(this, CreateNewProjectActivity.class);
            startActivity(backIntent);
            finish();
        });

        Button btnContinue = findViewById(R.id.btnContinue);
        btnContinue.setOnClickListener(V -> prepareAndSaveProject()); // שיניתי את שם הפונקציה למשהו הגיוני יותר

        SeekBar sbProgress = findViewById(R.id.sbProgress);
        TextView tvProgressValue = findViewById(R.id.tvProgressValue);

        sbProgress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            /**
             * we want to make sure the details updates when the user scroll the bar
             */
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                tvProgressValue.setText(progress + "%");
                currentProgress = progress;
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        EditText etStartDate = findViewById(R.id.etStartDate);
        etStartDate.setOnClickListener(v -> {
            final Calendar c = Calendar.getInstance();//get the date the user selected
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    this,
                    (view, selectedYear, selectedMonth, selectedDay) -> {
                        String date = selectedDay + "/" + (selectedMonth + 1) + "/" + selectedYear;
                        etStartDate.setText(date);          // +1 because the months start from 0
                    },
                    year, month, day);
            datePickerDialog.show();
        });

        spinnerStatus = findViewById(R.id.spinnerStatus);
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

    // if the user clicked the continue button we want to save the project to the database
    private void prepareAndSaveProject() {
        String projectName = ((EditText) findViewById(R.id.etProjectName)).getText().toString();
        String startDate = ((EditText) findViewById(R.id.etStartDate)).getText().toString();
        String selectedStatus = spinnerStatus.getSelectedItem().toString();
        //make sure The user enter name, Date all the other items either have default values or not required
        if (projectName.isEmpty() || startDate.isEmpty()) {
            Toast.makeText(this, "You must enter project name and start date", Toast.LENGTH_SHORT).show();
            return;
        }

        String description = ((EditText) findViewById(R.id.etDescription)).getText().toString();
        String projectType = getIntent().getStringExtra("TYPE");

        // Creating unique ID for the project
        String projectId = java.util.UUID.randomUUID().toString();

        // Creating a project
        Project newProject = new Project(projectId, projectName, projectType, startDate, currentProgress, 0, selectedStatus, description);
        String currentUserId = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser().getUid();
        newProject.addPartner(currentUserId,"Owner");
        // send to the manager
        projectViewModel.addProject(newProject);
    }

    private void updateUiForConstruction() {
        // Change the color and titles of the UI for construction projects
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