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
public class LoginActivity extends AppCompatActivity {
    private AuthViewModel viewModel; // hold the viewmodel
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // we want to make sure the app will take all the screen
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        // make sure the buttons will not be in the edge
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        // initialize the ViewModel
        viewModel = new ViewModelProvider(this).get(AuthViewModel.class);
        //observer
        viewModel.getUserLiveData().observe(this, firebaseUser -> {
            if (firebaseUser != null) {
                // signed in sucssefully now move to the next screen
                Toast.makeText(LoginActivity.this, "login successfully", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(LoginActivity.this, ChooseProjectActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                // Intent.FLAG_ACTIVITY_CLEAR_TASK - Clears the existing task (history)
                // Intent.FLAG_ACTIVITY_NEW_TASK - Starts the activity in a new, empty task
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(LoginActivity.this,"Login Failed", Toast.LENGTH_SHORT).show();
            }
        });
        findViewById(R.id.signInWithGoogleButton).setOnClickListener(v -> startGoogleSignInFlow());

        findViewById(R.id.loginButton).setOnClickListener(v -> {
            String email = ((EditText) findViewById(R.id.emailTextView)).getText().toString();
            String password = ((EditText) findViewById(R.id.passwordTextView)).getText().toString();
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "pls enter email and password", Toast.LENGTH_SHORT).show();
            } else {
                viewModel.login(email, password);
            }
        });
        findViewById(R.id.signUpButton).setOnClickListener(v -> {
            Intent intent = new Intent(this, SignUpActivity.class);
            startActivity(intent);
            finish();
        });
    }
    private void startGoogleSignInFlow() {
        GetGoogleIdOption googleIdOption = new GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)//show all the email in the device
                .setServerClientId(getString(R.string.default_web_client_id))// connect to the project in fire base
                .build();
        GetCredentialRequest request = new GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption) // set google sign in
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
                                //give the token to the view model
                                viewModel.loginWithGoogle(googleIdTokenCredential.getIdToken());
                            } catch (Exception e) {
                                Log.e("Auth", "Error parsing token", e);
                            }
                        }
                    }
                    @Override
                    public void onError(GetCredentialException e) {
                        Toast.makeText(LoginActivity.this, "error : " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }
        );
    }
}