package com.example.buildflow.view.activities;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.buildflow.R;
import com.example.buildflow.model.User;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

public class SignUpActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sign_up);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        findViewById(R.id.registerButton).setOnClickListener(v -> registerUser());
        findViewById(R.id.loginLinkButton).setOnClickListener(v -> login());
    }

    private void login() {
        Intent intent= new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    private void registerUser() {
        String email=((android.widget.EditText) findViewById(R.id.emailTextView)).getText().toString();
        String password=((android.widget.EditText) findViewById(R.id.passwordTextView)).getText().toString();

        if (email.isEmpty() || password.isEmpty() || ((android.widget.EditText) findViewById(R.id.nameTextView)).getText().toString().isEmpty()) {
            android.widget.Toast.makeText(this, "נא למלא מייל שם וסיסמה", android.widget.Toast.LENGTH_SHORT).show();
            return;
        }

        com.google.firebase.auth.FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = task.getResult().getUser();
                        assert user != null;
                        saveUserToFirestore(user);
                    } else {
                        android.widget.Toast.makeText(SignUpActivity.this, "הרשמה נכשלה: " + task.getException().getMessage(), android.widget.Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void saveUserToFirestore(FirebaseUser firebaseUser) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String name = ((android.widget.EditText) findViewById(R.id.nameTextView)).getText().toString();
        User user = new User(firebaseUser.getUid(), firebaseUser.getEmail(), name);

        // save in the collection user with uid as key
        //using merge to make sure we will not overwrite the user if he already exists
        db.collection("users").document(user.getUid())
                .set(user, SetOptions.merge())
                .addOnSuccessListener(aVoid -> {
                    // move to the next activity
                    android.widget.Toast.makeText(SignUpActivity.this, "Registered successfully!", android.widget.Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(SignUpActivity.this, ChooseProjectActivity.class);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    android.widget.Toast.makeText(SignUpActivity.this, "Failed to save data: " + e.getMessage(), android.widget.Toast.LENGTH_LONG).show();
                });
    }


}