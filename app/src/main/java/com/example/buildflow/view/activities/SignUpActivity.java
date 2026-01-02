package com.example.buildflow.view.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.credentials.CredentialManager;
import androidx.credentials.CustomCredential;
import androidx.credentials.GetCredentialRequest;
import androidx.credentials.GetCredentialResponse;
import androidx.credentials.exceptions.GetCredentialException;
import androidx.lifecycle.ViewModelProvider;

import com.example.buildflow.R;
import com.example.buildflow.viewmodel.AuthViewModel;
import com.google.android.libraries.identity.googleid.GetGoogleIdOption;
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential;

public class SignUpActivity extends AppCompatActivity {

    private AuthViewModel viewModel; // מחזיקים את ה-ViewModel

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

        // init the view model
        viewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        // observer to check if the user is registered
        viewModel.getUserLiveData().observe(this, firebaseUser -> {
            if (firebaseUser != null) {
                // successful registration
                Toast.makeText(SignUpActivity.this, "Registered successfully!", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(SignUpActivity.this, ChooseProjectActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            } else {
                //the user failed to register
                Toast.makeText(SignUpActivity.this, "Registered Failed!", Toast.LENGTH_SHORT).show();
            }
        });

        findViewById(R.id.registerButton).setOnClickListener(v -> registerUser());
        findViewById(R.id.loginLinkButton).setOnClickListener(v -> {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
        findViewById(R.id.signInWithGoogleButton).setOnClickListener(v -> googleSignIn());
    }

    private void registerUser() {
        String email = ((EditText) findViewById(R.id.emailTextView)).getText().toString();
        String password = ((EditText) findViewById(R.id.passwordTextView)).getText().toString();
        String name = ((EditText) findViewById(R.id.nameTextView)).getText().toString();

        if (email.isEmpty() || password.isEmpty() || name.isEmpty()) {
            Toast.makeText(this, "You must enter email, password and name", Toast.LENGTH_SHORT).show();
            return;
        }

        viewModel.register(email, password, name);
    }

    private void googleSignIn() {
        GetGoogleIdOption googleIdOption = new GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(getString(R.string.default_web_client_id))
                .build();
        GetCredentialRequest request = new GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build();
        CredentialManager credentialManager = CredentialManager.create(this);

        credentialManager.getCredentialAsync(this, request, new android.os.CancellationSignal(), getMainExecutor(),
                new androidx.credentials.CredentialManagerCallback<GetCredentialResponse, GetCredentialException>() {
                    @Override
                    public void onResult(GetCredentialResponse result) {
                        androidx.credentials.Credential credential = result.getCredential();
                        if (credential instanceof CustomCredential &&
                                credential.getType().equals(GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL)) {
                            try {
                                GoogleIdTokenCredential googleIdTokenCredential = GoogleIdTokenCredential.createFrom(((CustomCredential) credential).getData());

                               //send to view model
                                viewModel.signUpWithGoogle(googleIdTokenCredential.getIdToken());

                            } catch (Exception e) {
                                Log.e("Auth", "Error parsing token", e);
                            }
                        }
                    }

                    @Override
                    public void onError(GetCredentialException e) {
                        Toast.makeText(SignUpActivity.this, "error  " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }
        );
    }
}