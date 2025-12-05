package com.example.buildflow;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

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

import com.google.android.libraries.identity.googleid.GetGoogleIdOption;
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {
    @Override
    public void onStart() {
        super.onStart();
        // check if a user is already connected
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if(currentUser != null){
            // if the user is already connected we want to go to the next activity
            Intent intent = new Intent(LoginActivity.this, ChooseProjectActivity.class);
            startActivity(intent);
            finish();
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        findViewById(R.id.signUpWithGoogleButton).setOnClickListener(v -> googleSignIn());
        findViewById(R.id.signUpButton).setOnClickListener(v -> registerUser(
                ((android.widget.EditText) findViewById(R.id.nameText)).getText().toString(), ((android.widget.EditText) findViewById(R.id.pass)).getText().toString()));
        findViewById(R.id.loginButton).setOnClickListener(v -> loginUser(((android.widget.EditText) findViewById(R.id.nameText)).getText().toString(), ((android.widget.EditText) findViewById(R.id.pass)).getText().toString()));
    }
    private void registerUser(String email, String password) {
        // בדיקה שהשדות לא ריקים
        if (email.isEmpty() || password.isEmpty()) {
            android.widget.Toast.makeText(this, "נא למלא מייל וסיסמה", android.widget.Toast.LENGTH_SHORT).show();
            return;
        }

        // הפקודה שיוצרת משתמש חדש
        com.google.firebase.auth.FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // הצלחה: המשתמש נוצר
                        android.widget.Toast.makeText(LoginActivity.this, "נרשמת בהצלחה!", android.widget.Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(LoginActivity.this, ChooseProjectActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        // כישלון: למשל המייל כבר קיים או הסיסמה חלשה
                        android.widget.Toast.makeText(LoginActivity.this, "הרשמה נכשלה: " + task.getException().getMessage(), android.widget.Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void loginUser(String email, String password) {
        // בדיקה שהשדות לא ריקים
        if (email.isEmpty() || password.isEmpty()) {
            android.widget.Toast.makeText(this, "נא למלא מייל וסיסמה", android.widget.Toast.LENGTH_SHORT).show();
            return;
        }

        // הפקודה שבודקת פרטים ומחברת את המשתמש
        com.google.firebase.auth.FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // הצלחה: הפרטים נכונים
                        android.widget.Toast.makeText(LoginActivity.this, "התחברת בהצלחה!", android.widget.Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(LoginActivity.this, ChooseProjectActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        // כישלון: סיסמה שגויה או משתמש לא קיים
                        android.widget.Toast.makeText(LoginActivity.this, "שגיאה בכניסה: " + task.getException().getMessage(), android.widget.Toast.LENGTH_LONG).show();
                    }
                });
    }


    // --- זו הפונקציה שחסרה לך ---
    private void firebaseAuthWithGoogle(String idToken) {
        com.google.firebase.auth.AuthCredential credential = com.google.firebase.auth.GoogleAuthProvider.getCredential(idToken, null);
        com.google.firebase.auth.FirebaseAuth.getInstance().signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // התחברות הצליחה
                        android.widget.Toast.makeText(LoginActivity.this, "התחברת בהצלחה!", android.widget.Toast.LENGTH_SHORT).show();
                        Log.d("Auth", "User signed in: " + com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser().getEmail());
                        Intent intent = new Intent(LoginActivity.this, ChooseProjectActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        // התחברות נכשלה
                        android.widget.Toast.makeText(LoginActivity.this, "החיבור נכשל.", android.widget.Toast.LENGTH_SHORT).show();
                        Log.e("Auth", "Firebase sign in failed", task.getException());
                    }
                });
    }
    protected void googleSignIn(){
        GetGoogleIdOption googleIdOption = new GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(getString(R.string.default_web_client_id))
                .build();
        GetCredentialRequest request = new GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build();
        // אנחנו יוצרים את המנהל ושולחים את הבקשה
        CredentialManager credentialManager = CredentialManager.create(this);

        credentialManager.getCredentialAsync(this, request, new android.os.CancellationSignal(), getMainExecutor(),
                new androidx.credentials.CredentialManagerCallback<GetCredentialResponse, GetCredentialException>() {
                    @Override
                    public void onResult(GetCredentialResponse result) {
                        // --- כאן, ורק כאן, המשתנה credential נוצר! ---
                        androidx.credentials.Credential credential = result.getCredential();

                        // 3. בדיקת התוצאה (הקוד שהיה לך למטה נכנס עכשיו לכאן)
                        if (credential instanceof CustomCredential &&
                                credential.getType().equals(GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL)) {

                            try {
                                GoogleIdTokenCredential googleIdTokenCredential = GoogleIdTokenCredential.createFrom(((CustomCredential) credential).getData());

                                // יש הצלחה! שולחים לפיירבייס
                                firebaseAuthWithGoogle(googleIdTokenCredential.getIdToken());

                            } catch (Exception e) {
                                android.util.Log.e("Auth", "Error parsing token", e);
                            }
                        } else {
                            android.util.Log.w("Auth", "Credential is not of type Google ID!");
                        }
                    }

                    @Override
                    public void onError(GetCredentialException e) {
                        // מה קורה אם המשתמש סגר את החלונית או שיש שגיאה
                        android.util.Log.e("Auth", "SignIn failed", e);
                        android.widget.Toast.makeText(LoginActivity.this, "שגיאה: " + e.getMessage(), android.widget.Toast.LENGTH_LONG).show();
                    }
                }
        );
    }

}