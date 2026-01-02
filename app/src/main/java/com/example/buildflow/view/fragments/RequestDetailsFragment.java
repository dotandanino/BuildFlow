package com.example.buildflow.view.fragments;

import android.app.AlertDialog;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.buildflow.R;
import com.example.buildflow.model.Project;
import com.example.buildflow.model.ProjectRequest;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;
import java.util.UUID;

public class RequestDetailsFragment extends Fragment {

    private String projectId;
    private String requestId;
    private FirebaseFirestore db;

    private Uri closingImageUri = null;
    private ImageView ivDialogPreview;

    private final ActivityResultLauncher<String> selectImageLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    closingImageUri = uri;
                    if (ivDialogPreview != null) {
                        ivDialogPreview.setVisibility(View.VISIBLE);
                        ivDialogPreview.setImageURI(uri);
                    }
                }
            }
    );

    public RequestDetailsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_request_details, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = FirebaseFirestore.getInstance();

        ImageButton btnBack = view.findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> getParentFragmentManager().popBackStack());

        Button btnStartTask = view.findViewById(R.id.btnStartTask);
        Button btnCloseRequest = view.findViewById(R.id.btnCloseRequest);

        TextView tvTitle = view.findViewById(R.id.tvTitle);
        TextView tvCategory = view.findViewById(R.id.tvCategory);
        TextView tvStatus = view.findViewById(R.id.tvStatus);
        TextView tvDescription = view.findViewById(R.id.tvDescription);
        TextView tvDate = view.findViewById(R.id.tvDate);
        TextView tvTime = view.findViewById(R.id.tvTime);
        TextView tvUrgency = view.findViewById(R.id.tvUrgency);
        TextView tvLocation = view.findViewById(R.id.tvLocation);
        ImageView ivImage = view.findViewById(R.id.ivRequestImage);
        View cardImage = view.findViewById(R.id.cardImage);

        if (getArguments() != null) {
            projectId = getArguments().getString("PROJECT_ID");
            requestId = getArguments().getString("REQUEST_ID");

            tvTitle.setText(getArguments().getString("TITLE"));
            tvCategory.setText(getArguments().getString("CATEGORY"));
            tvStatus.setText(getArguments().getString("STATUS"));
            tvDescription.setText(getArguments().getString("DESC"));
            tvDate.setText(getArguments().getString("DATE"));
            tvTime.setText(getArguments().getString("TIME"));
            tvUrgency.setText(getArguments().getString("URGENCY"));
            tvLocation.setText(getArguments().getString("LOCATION"));

            String imageUrl = getArguments().getString("IMAGE_URL");
            String status = getArguments().getString("STATUS");

            if ("Closed".equals(status)) {
                btnStartTask.setVisibility(View.GONE);
                btnCloseRequest.setVisibility(View.GONE);
                tvStatus.setText("Closed");
            } else if ("In Progress".equals(status)) {
                btnStartTask.setVisibility(View.GONE);
                btnCloseRequest.setVisibility(View.VISIBLE);
            } else {
                btnStartTask.setVisibility(View.VISIBLE);
                btnCloseRequest.setVisibility(View.VISIBLE);
            }

            if (imageUrl != null && !imageUrl.isEmpty()) {
                cardImage.setVisibility(View.VISIBLE);
                Glide.with(this).load(imageUrl).centerCrop().into(ivImage);
            } else {
                cardImage.setVisibility(View.GONE);
            }
        }

        btnStartTask.setOnClickListener(v -> updateRequestStatus("In Progress", null, null));

        // open the dialog to close the request
        btnCloseRequest.setOnClickListener(v -> showCustomCloseDialog());
    }

    private void showCustomCloseDialog() {
        // a dialog to close project
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_close_request, null);

        AlertDialog dialog = new AlertDialog.Builder(getContext())
                .setView(dialogView)
                .setCancelable(true)
                .create();

        EditText etResolution = dialogView.findViewById(R.id.etResolution);
        Button btnAddPhoto = dialogView.findViewById(R.id.btnAddClosingPhoto);
        Button btnConfirm = dialogView.findViewById(R.id.btnConfirmClose);
        ivDialogPreview = dialogView.findViewById(R.id.ivClosingPreview);
        closingImageUri = null;

        btnAddPhoto.setOnClickListener(v -> selectImageLauncher.launch("image/*"));

        btnConfirm.setOnClickListener(v -> {
            String resolution = etResolution.getText().toString().trim();
            if (resolution.isEmpty()) {
                etResolution.setError("Description required");
                return;
            }

            dialog.dismiss();
            finalizeRequestClosure(resolution);
        });

        dialog.show();
    }

    private void finalizeRequestClosure(String resolutionDesc) {
        if (closingImageUri != null) {
            // if there is picture we upload her
            Toast.makeText(getContext(), "Uploading image...", Toast.LENGTH_SHORT).show();
            StorageReference storageRef = FirebaseStorage.getInstance().getReference()
                    .child("closing_images/" + UUID.randomUUID().toString() + ".jpg");

            storageRef.putFile(closingImageUri)
                    .addOnSuccessListener(taskSnapshot -> storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        updateRequestStatus("Closed", uri.toString(), resolutionDesc);
                    }))
                    .addOnFailureListener(e -> Toast.makeText(getContext(), "Image upload failed", Toast.LENGTH_SHORT).show());
        } else {
            // if there is no image we can close
            updateRequestStatus("Closed", null, resolutionDesc);
        }
    }

    /**
     * Updates the request status in the Firestore database.
     * @param newStatus - the new status of the request
     * @param closingImgUrl - the url of the closing image (if there is one)
     * @param resolutionDesc - the description of the resolution (if there is one)
     */
    private void updateRequestStatus(String newStatus, String closingImgUrl, String resolutionDesc) {
        if (projectId == null || requestId == null) return;

        db.collection("projects").document(projectId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    Project project = documentSnapshot.toObject(Project.class);
                    if (project != null && project.getRequests() != null) {
                        List<ProjectRequest> requests = project.getRequests();
                        boolean found = false;

                        for (ProjectRequest req : requests) {
                            if (req.getRequestId().equals(requestId)) {
                                req.setStatus(newStatus);

                                if (closingImgUrl != null) req.setClosingImageUrl(closingImgUrl);
                                if (resolutionDesc != null) req.setResolutionDescription(resolutionDesc);

                                found = true;
                                break;
                            }
                        }

                        if (found) {
                            db.collection("projects").document(projectId).set(project)
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(getContext(), "Task Completed!", Toast.LENGTH_SHORT).show();
                                        getParentFragmentManager().popBackStack();
                                    })
                                    .addOnFailureListener(e -> Toast.makeText(getContext(), "Update failed", Toast.LENGTH_SHORT).show());
                        }
                    }
                });
    }
}