package com.example.buildflow.view.fragments;

import android.Manifest;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.buildflow.R;
import com.example.buildflow.model.Project;
import com.example.buildflow.model.ProjectRequest;
import com.example.buildflow.model.User;
import com.example.buildflow.view.activities.ProjectViewActivity;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class NewRequestFragment extends Fragment {

    private String currentProjectId;
    private String selectedCategory = "";
    private String selectedUrgency = "";
    private String selectedAssigneeUid = "";

    // משתנה שמחזיק את ה-ID של הטיוטה (אם אנחנו עורכים טיוטה קיימת)
    private String loadedDraftId = null;

    // משתנים לתמונה
    private Uri imageUri = null;
    private Bitmap imageBitmap = null;
    private ImageView ivSelectedImage;
    private TextView tvUploadHint;

    private List<Button> categoryButtons = new ArrayList<>();
    private List<Button> urgencyButtons = new ArrayList<>();
    private AutoCompleteTextView autoCompleteAssignee;

    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private Map<String, String> assigneeNameMap = new HashMap<>();

    private ActivityResultLauncher<Intent> cameraLauncher;
    private ActivityResultLauncher<String> galleryLauncher;
    private ActivityResultLauncher<String> requestPermissionLauncher;

    public NewRequestFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            currentProjectId = getArguments().getString("PROJECT_ID");
        }
        initImageLaunchers();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_new_request, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (currentProjectId == null || currentProjectId.isEmpty()) {
            Toast.makeText(getContext(), "Error: Project ID not found!", Toast.LENGTH_SHORT).show();
            return;
        }

        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();

        initCategoryButtons(view);
        initUrgencyButtons(view);
        setupPickers(view);

        ImageButton btnMenu = view.findViewById(R.id.btnMenu);
        btnMenu.setOnClickListener(v -> {
            if (getActivity() instanceof ProjectViewActivity) {
                ((ProjectViewActivity) getActivity()).openDrawer();
            }
        });

        setupImageUpload(view);

        autoCompleteAssignee = view.findViewById(R.id.autoCompleteAssignee);
        loadProjectTeamMembers();

        Button btnSubmit = view.findViewById(R.id.btnSubmit);
        btnSubmit.setOnClickListener(v -> submitRequest(view));

        Button btnSaveDraft = view.findViewById(R.id.btnSaveDraft);
        if (btnSaveDraft != null) {
            btnSaveDraft.setOnClickListener(v -> saveDraft(view));
        }

        if (getArguments() != null && getArguments().containsKey("DRAFT_DATA")) {
            String draftJson = getArguments().getString("DRAFT_DATA");
            restoreDraftFromData(view, draftJson);
        }
    }

    private void openDraftsFragment() {
        DraftsFragment draftsFragment = new DraftsFragment();
        Bundle args = new Bundle();
        args.putString("PROJECT_ID", currentProjectId);
        draftsFragment.setArguments(args);

        getParentFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, draftsFragment)
                .addToBackStack(null)
                .commit();
    }


    private void submitRequest(View view) {
        // check the user enter all the field he must
        if (selectedCategory.isEmpty()) {
            Toast.makeText(getContext(), "Please select a category", Toast.LENGTH_SHORT).show();
            return;
        }
        if (selectedUrgency.isEmpty()) {
            Toast.makeText(getContext(), "Please select urgency level", Toast.LENGTH_SHORT).show();
            return;
        }
        //check he have internet connection
        if (!isNetworkAvailable()) {
            new AlertDialog.Builder(getContext())
                    .setTitle("No Internet Connection")
                    .setMessage("You are offline. Save as draft?")
                    .setPositiveButton("Save Draft", (dialog, which) -> saveDraft(view))
                    .setNegativeButton("Cancel", null)
                    .show();
            return;
        }

        //show loading
        ProgressDialog progressDialog = new ProgressDialog(getContext());
        progressDialog.setMessage("Submitting Request...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        //save and upload
        if (imageUri != null || imageBitmap != null) {
            uploadImageToStorage(url -> {
                // picture upload successfully
                saveToFirestore(view, url, progressDialog);
            }, e -> {
                // picture upload failed
                progressDialog.dismiss();
                Toast.makeText(getContext(), "Failed to upload image", Toast.LENGTH_SHORT).show();
            });
        } else {
            // if there is no picture
            saveToFirestore(view, null, progressDialog);
        }
    }

    private void saveToFirestore(View view, String imageUrl, ProgressDialog progressDialog) {
        EditText etTitle = view.findViewById(R.id.etTitle);
        EditText etDesc = view.findViewById(R.id.etDescription);
        EditText etDate = view.findViewById(R.id.etDate);
        EditText etTime = view.findViewById(R.id.etTime);
        EditText etLocation = view.findViewById(R.id.etLocation);

        ProjectRequest newRequest = new ProjectRequest(
                etTitle.getText().toString(),
                selectedCategory,
                etDesc.getText().toString(),
                selectedUrgency,
                etLocation.getText().toString(),
                etDate.getText().toString(),
                etTime.getText().toString(),
                selectedAssigneeUid,
                imageUrl
        );

        db.collection("projects").document(currentProjectId)
                .update("requests", FieldValue.arrayUnion(newRequest))
                .addOnSuccessListener(aVoid -> {
                    progressDialog.dismiss();

                    // if its draft we delete him
                    removeDraftFromList();

                    Toast.makeText(getContext(), "Request Created Successfully!", Toast.LENGTH_LONG).show();
                    getParentFragmentManager().popBackStack();
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private boolean isNetworkAvailable() {
        android.net.ConnectivityManager cm = (android.net.ConnectivityManager) requireContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        android.net.NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnected();
    }


    private void saveDraft(View view) {
        EditText etTitle = view.findViewById(R.id.etTitle);
        EditText etDesc = view.findViewById(R.id.etDescription);
        EditText etDate = view.findViewById(R.id.etDate);
        EditText etTime = view.findViewById(R.id.etTime);
        EditText etLocation = view.findViewById(R.id.etLocation);

        // save the image locally
        String localImageUriString = null;
        if (imageUri != null) {
            localImageUriString = imageUri.toString();
        } else if (imageBitmap != null) {
            localImageUriString = saveBitmapToLocalFile(imageBitmap);
        }

        ProjectRequest draftRequest = new ProjectRequest(
                etTitle.getText().toString(),
                selectedCategory,
                etDesc.getText().toString(),
                selectedUrgency,
                etLocation.getText().toString(),
                etDate.getText().toString(),
                etTime.getText().toString(),
                selectedAssigneeUid,
                localImageUriString
        );

        // save the draft id
        if (loadedDraftId != null) {
            draftRequest.setRequestId(loadedDraftId);
        }

        // update the list of drafts
        SharedPreferences prefs = requireContext().getSharedPreferences("ProjectDrafts", Context.MODE_PRIVATE);
        String jsonList = prefs.getString("all_drafts_" + currentProjectId, null);
        Gson gson = new Gson();
        List<ProjectRequest> drafts;

        if (jsonList != null) {
            Type type = new TypeToken<ArrayList<ProjectRequest>>() {}.getType();
            drafts = gson.fromJson(jsonList, type);
        } else {
            drafts = new ArrayList<>();
        }

        boolean updated = false;
        if (loadedDraftId != null) {
            for (int i = 0; i < drafts.size(); i++) {
                if (drafts.get(i).getRequestId().equals(loadedDraftId)) {
                    drafts.set(i, draftRequest);
                    updated = true;
                    break;
                }
            }
        }

        if (!updated) {
            drafts.add(draftRequest);
            loadedDraftId = draftRequest.getRequestId();
        }

        prefs.edit().putString("all_drafts_" + currentProjectId, gson.toJson(drafts)).apply();
        Toast.makeText(getContext(), "Draft Saved!", Toast.LENGTH_SHORT).show();
    }

    private void restoreDraftFromData(View view, String json) { // restore the draft from the data
        try {
            ProjectRequest draft = new Gson().fromJson(json, ProjectRequest.class);
            loadedDraftId = draft.getRequestId();

            EditText etTitle = view.findViewById(R.id.etTitle);
            EditText etDesc = view.findViewById(R.id.etDescription);
            EditText etDate = view.findViewById(R.id.etDate);
            EditText etTime = view.findViewById(R.id.etTime);
            EditText etLocation = view.findViewById(R.id.etLocation);

            etTitle.setText(draft.getTitle());
            etDesc.setText(draft.getDescription());
            etDate.setText(draft.getPreferredDate());
            etTime.setText(draft.getPreferredTime());
            etLocation.setText(draft.getLocation());

            if (draft.getCategory() != null) {
                selectedCategory = draft.getCategory();
                for (Button btn : categoryButtons) {
                    if (btn.getText().toString().equals(selectedCategory)) updateButtonSelection(categoryButtons, btn);
                }
            }
            if (draft.getUrgency() != null) {
                selectedUrgency = draft.getUrgency();
                for (Button btn : urgencyButtons) {
                    if (btn.getText().toString().equals(selectedUrgency)) updateButtonSelection(urgencyButtons, btn);
                }
            }

            if (draft.getImageUrl() != null) {
                imageUri = Uri.parse(draft.getImageUrl());
                ivSelectedImage.setImageURI(imageUri);
                ivSelectedImage.setColorFilter(null);
                tvUploadHint.setVisibility(View.GONE);
            }

        } catch (Exception e) {
            Toast.makeText(getContext(), "Error loading draft", Toast.LENGTH_SHORT).show();
        }
    }

    private void removeDraftFromList() {
        if (loadedDraftId == null) return;

        SharedPreferences prefs = requireContext().getSharedPreferences("ProjectDrafts", Context.MODE_PRIVATE);
        String jsonList = prefs.getString("all_drafts_" + currentProjectId, null);

        if (jsonList != null) {
            Gson gson = new Gson();
            Type type = new TypeToken<ArrayList<ProjectRequest>>() {}.getType();
            List<ProjectRequest> drafts = gson.fromJson(jsonList, type);

            drafts.removeIf(d -> d.getRequestId().equals(loadedDraftId));
            prefs.edit().putString("all_drafts_" + currentProjectId, gson.toJson(drafts)).apply();
        }
    }

    private String saveBitmapToLocalFile(Bitmap bitmap) {
        try {
            File file = new File(requireContext().getCacheDir(), "draft_img_" + System.currentTimeMillis() + ".jpg");
            FileOutputStream out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush();
            out.close();
            return Uri.fromFile(file).toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // --- תמונות ---

    private void initImageLaunchers() {
        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == -1 && result.getData() != null) {
                        Bundle extras = result.getData().getExtras();
                        imageBitmap = (Bitmap) extras.get("data");
                        imageUri = null;

                        ivSelectedImage.setImageBitmap(imageBitmap);
                        ivSelectedImage.setColorFilter(null);
                        tvUploadHint.setVisibility(View.GONE);
                    }
                }
        );

        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        imageUri = uri;
                        imageBitmap = null;

                        ivSelectedImage.setImageURI(imageUri);
                        ivSelectedImage.setColorFilter(null);
                        tvUploadHint.setVisibility(View.GONE);
                    }
                }
        );

        requestPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) openCamera();
                    else Toast.makeText(getContext(), "Camera permission needed", Toast.LENGTH_SHORT).show();
                }
        );
    }

    private void setupImageUpload(View view) {
        LinearLayout layoutUploadPhoto = view.findViewById(R.id.layoutUploadPhoto);
        ivSelectedImage = view.findViewById(R.id.ivSelectedImage);
        tvUploadHint = view.findViewById(R.id.tvUploadHint);

        layoutUploadPhoto.setOnClickListener(v -> showImageSourceDialog());
    }

    private void showImageSourceDialog() {
        String[] options = {"Take Photo", "Choose from Gallery"};
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Select Image");
        builder.setItems(options, (dialog, which) -> {
            if (which == 0) {
                if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
                        != PackageManager.PERMISSION_GRANTED) {
                    requestPermissionLauncher.launch(Manifest.permission.CAMERA);
                } else {
                    openCamera();
                }
            } else {
                openGallery();
            }
        });
        builder.show();
    }

    private void openCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraLauncher.launch(intent);
    }

    private void openGallery() {
        galleryLauncher.launch("image/*");
    }

    private void uploadImageToStorage(OnUploadSuccess onSuccess, OnUploadFailure onFailure) {
        String filename = UUID.randomUUID().toString();
        StorageReference ref = storage.getReference().child("request_images/" + filename);

        if (imageUri != null) {
            ref.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> ref.getDownloadUrl().addOnSuccessListener(uri -> onSuccess.onSuccess(uri.toString())))
                    .addOnFailureListener(onFailure::onFailure);
        } else if (imageBitmap != null) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] data = baos.toByteArray();

            ref.putBytes(data)
                    .addOnSuccessListener(taskSnapshot -> ref.getDownloadUrl().addOnSuccessListener(uri -> onSuccess.onSuccess(uri.toString())))
                    .addOnFailureListener(onFailure::onFailure);
        }
    }

    interface OnUploadSuccess { void onSuccess(String url); }
    interface OnUploadFailure { void onFailure(Exception e); }

    // --- שאר הפונקציות (חברי צוות, כפתורים, תאריך) ---

    private void loadProjectTeamMembers() {
        if (currentProjectId == null) return;
        List<String> displayList = new ArrayList<>();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),R.layout.item_dropdown, displayList);
        autoCompleteAssignee.setAdapter(adapter);
        autoCompleteAssignee.setOnClickListener(v -> autoCompleteAssignee.showDropDown());
        autoCompleteAssignee.setDropDownBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.WHITE));
        db.collection("projects").document(currentProjectId).get().addOnSuccessListener(documentSnapshot -> {
            Project project = documentSnapshot.toObject(Project.class);
            if (project != null && project.getParticipants() != null) {
                for (String uid : project.getParticipants()) {
                    String role = (project.getRoles() != null) ? project.getRoles().get(uid) : "";
                    final String finalRole = role;
                    db.collection("users").document(uid).get().addOnSuccessListener(userDoc -> {
                        User user = userDoc.toObject(User.class);
                        if (user != null) {
                            String label = (user.getName() != null && !user.getName().isEmpty()) ? user.getName() : user.getEmail();
                            if (finalRole != null && !finalRole.isEmpty()) label += " - " + finalRole;
                            displayList.add(label);
                            assigneeNameMap.put(label, uid);
                            adapter.notifyDataSetChanged();
                        }
                    });
                }
            }
        });
        autoCompleteAssignee.setOnItemClickListener((parent, view, position, id) -> {
            String selection = (String) parent.getItemAtPosition(position);
            selectedAssigneeUid = assigneeNameMap.get(selection);
        });
    }

    private void initCategoryButtons(View view) {
        categoryButtons.add(view.findViewById(R.id.btnCatPlumbing));
        categoryButtons.add(view.findViewById(R.id.btnCatElectrical));
        categoryButtons.add(view.findViewById(R.id.btnCatPainting));
        categoryButtons.add(view.findViewById(R.id.btnCatOther));
        for (Button btn : categoryButtons) {
            if (btn != null) {
                btn.setOnClickListener(v -> {
                    selectedCategory = btn.getText().toString();
                    updateButtonSelection(categoryButtons, btn);
                });
            }
        }
    }

    private void initUrgencyButtons(View view) {
        urgencyButtons.add(view.findViewById(R.id.btnUrgLow));
        urgencyButtons.add(view.findViewById(R.id.btnUrgNormal));
        urgencyButtons.add(view.findViewById(R.id.btnUrgHigh));
        urgencyButtons.add(view.findViewById(R.id.btnUrgEmergency));
        for (Button btn : urgencyButtons) {
            if (btn != null) {
                btn.setOnClickListener(v -> {
                    selectedUrgency = btn.getText().toString();
                    updateButtonSelection(urgencyButtons, btn);
                });
            }
        }
    }

    private void updateButtonSelection(List<Button> buttons, Button selectedBtn) {
        for (Button btn : buttons) {
            if (btn == selectedBtn) {
                btn.setBackgroundResource(R.drawable.bg_category_selected);
                btn.setTextColor(Color.parseColor("#F4511E"));
            } else {
                btn.setBackgroundResource(R.drawable.bg_category_btn);
                btn.setTextColor(Color.BLACK);
            }
        }
    }

    private void setupPickers(View view) {
        EditText etDate = view.findViewById(R.id.etDate);
        EditText etTime = view.findViewById(R.id.etTime);
        if (etDate != null) {
            etDate.setOnClickListener(v -> {
                final Calendar c = Calendar.getInstance();
                new DatePickerDialog(getContext(), (view1, year, month, dayOfMonth) ->
                        etDate.setText(dayOfMonth + "/" + (month + 1) + "/" + year),
                        c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
            });
        }
        if (etTime != null) {
            etTime.setOnClickListener(v -> {
                final Calendar c = Calendar.getInstance();
                new TimePickerDialog(getContext(), (view1, hourOfDay, minute) ->
                        etTime.setText(String.format("%02d:%02d", hourOfDay, minute)),
                        c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), true).show();
            });
        }
    }
}