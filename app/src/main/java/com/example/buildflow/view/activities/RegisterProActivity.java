package com.example.buildflow.view.activities;

import android.app.ProgressDialog;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.buildflow.R;
import com.example.buildflow.model.Professional;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class RegisterProActivity extends AppCompatActivity {

    private EditText etName, etPhone, etCities, etRate, etDesc;
    private Spinner spinnerProfession;
    private Button btnSubmit;
    private ImageButton btnBack;
    private ImageView ivAvatarPreview; // התמונה החדשה

    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private FirebaseStorage storage; // משתנה ל-Storage

    private Uri selectedImageUri = null; // משתנה לשמירת ה-URI של התמונה שנבחרה מהגלריה
    private ProgressDialog progressDialog;

    // הדרך החדשה לפתוח את הגלריה ולקבל תוצאה
    private final ActivityResultLauncher<String> pickImageLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    selectedImageUri = uri;
                    // הצגת תצוגה מקדימה של התמונה שנבחרה
                    ivAvatarPreview.setImageURI(uri);
                    // הסרת הפדינג כדי שהתמונה תמלא את העיגול
                    ivAvatarPreview.setPadding(0,0,0,0);
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_pro);

        // אתחול פיירבייס
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance(); // אתחול Storage

        // קישור משתנים לעיצוב
        ivAvatarPreview = findViewById(R.id.ivProAvatarPreview);
        etName = findViewById(R.id.etRegName);
        etPhone = findViewById(R.id.etRegPhone);
        // etEmail הוסר
        etCities = findViewById(R.id.etRegCities);
        etRate = findViewById(R.id.etRegRate);
        etDesc = findViewById(R.id.etRegDesc);
        spinnerProfession = findViewById(R.id.spinnerRegProfession);
        btnSubmit = findViewById(R.id.btnSubmitPro);
        btnBack = findViewById(R.id.btnBack);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Registering and uploading...");
        progressDialog.setCancelable(false);


        // מילוי רשימת המקצועות בספינר
        String[] professions = {"Plumber", "Electrician", "Architect", "Contractor", "Painter", "Other"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, professions);
        spinnerProfession.setAdapter(adapter);

        // לחיצה על התמונה פותחת את הגלריה
        ivAvatarPreview.setOnClickListener(v -> pickImageLauncher.launch("image/*"));

        // חזרה אחורה
        btnBack.setOnClickListener(v -> finish());

        // שמירת הנתונים
        btnSubmit.setOnClickListener(v -> startRegistrationProcess());
    }

    private void startRegistrationProcess() {
        String name = etName.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String citiesStr = etCities.getText().toString().trim();
        String rateStr = etRate.getText().toString().trim();

        // בדיקת תקינות בסיסית
        if (name.isEmpty() || phone.isEmpty() || rateStr.isEmpty() || citiesStr.isEmpty()) {
            Toast.makeText(this, "Please fill in all mandatory fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (auth.getCurrentUser() == null) {
            Toast.makeText(this, "You must be logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        progressDialog.show(); // הצגת דיאלוג טעינה

        // אם נבחרה תמונה - מעלים אותה קודם
        if (selectedImageUri != null) {
            uploadImageToStorage();
        } else {
            // אם לא נבחרה תמונה - ממשיכים לשמירה עם URL ריק
            saveProToFirestore("");
        }
    }


    private void uploadImageToStorage() {
        String userId = auth.getCurrentUser().getUid();
        // יצירת שם ייחודי לקובץ (למשל: pro_avatars/user123_randomId.jpg)
        String fileName = "pro_avatars/" + userId + "_" + UUID.randomUUID().toString() + ".jpg";
        StorageReference fileRef = storage.getReference().child(fileName);

        fileRef.putFile(selectedImageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    // ההעלאה הצליחה, עכשיו מקבלים את הקישור להורדה
                    fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        String downloadUrl = uri.toString();
                        // ממשיכים לשמירה ב-Firestore עם הקישור לתמונה
                        saveProToFirestore(downloadUrl);
                    });
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(this, "Image upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }


    // פונקציה ששומרת את הנתונים ב-Firestore (מקבלת את ה-URL של התמונה)
    private void saveProToFirestore(String avatarUrl) {
        String name = etName.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String citiesStr = etCities.getText().toString().trim();
        String rateStr = etRate.getText().toString().trim();
        String desc = etDesc.getText().toString().trim();
        String profession = spinnerProfession.getSelectedItem().toString();

        double rate = Double.parseDouble(rateStr);
        List<String> citiesList = Arrays.asList(citiesStr.split("\\s*,\\s*"));

        String userId = auth.getCurrentUser().getUid();
        String userEmail = auth.getCurrentUser().getEmail(); // לוקחים את המייל מהאפליקציה עצמה

        // המזהה המשולב
        String documentId = userId + "_" + profession;

        // --- Geocoder ---
        double latitude = 0.0;
        double longitude = 0.0;
        String primaryCity = citiesList.get(0);
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocationName(primaryCity, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                latitude = address.getLatitude();
                longitude = address.getLongitude();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        // ----------------

        // יצירת אובייקט איש המקצוע
        Professional newPro = new Professional();
        newPro.setUserId(userId);
        newPro.setName(name);
        newPro.setProfession(profession);
        newPro.setPhoneNumber(phone);
        newPro.setEmail(userEmail); // מייל המשתמש
        newPro.setServiceCities(citiesList);
        newPro.setHourlyRate(rate);
        newPro.setDescription(desc);

        // הגדרת ה-URL של התמונה שהעלינו (או ריק אם לא נבחרה)
        newPro.setAvatarUrl(avatarUrl);

        // נתוני ברירת מחדל
        newPro.setRating(5.0);
        newPro.setReviewsCount(0);
        newPro.setTotalJobs(0);
        newPro.setVerified(false);
        newPro.setLatitude(latitude);
        newPro.setLongitude(longitude);

        // שמירה ב-Firestore
        db.collection("professionals").document(documentId)
                .set(newPro)
                .addOnSuccessListener(aVoid -> {
                    progressDialog.dismiss();
                    Toast.makeText(this, "Successfully registered as " + profession + "!", Toast.LENGTH_LONG).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(this, "Registration failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}