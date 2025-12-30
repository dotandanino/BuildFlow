package com.example.buildflow.view.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.buildflow.R;
import com.example.buildflow.model.Project;
import com.example.buildflow.model.ProjectRequest;
import com.example.buildflow.view.activities.ProjectViewActivity;
import com.example.buildflow.view.adapters.MyRequestsAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class HomePageFragment extends Fragment {

    private RecyclerView recyclerView;
    private TextView tvEmptyState;
    private MyRequestsAdapter adapter;
    private FirebaseFirestore db;
    private String currentUserId;
    private String currentProjectId;

    public HomePageFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home_page, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = FirebaseFirestore.getInstance();
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }

        if (getActivity() != null && getActivity().getIntent() != null) {
            currentProjectId = getActivity().getIntent().getStringExtra("PROJECT_ID");
        }

        ImageButton btnOpenMenu = view.findViewById(R.id.btnOpenMenu);
        btnOpenMenu.setOnClickListener(v -> {
            if (getActivity() instanceof ProjectViewActivity) {
                ((ProjectViewActivity) getActivity()).openDrawer();
            }
        });

        Button newRequestBtn = view.findViewById(R.id.newRequestBtn);
        newRequestBtn.setOnClickListener(v -> {
            NewRequestFragment newRequestFragment = new NewRequestFragment();
            Bundle args = new Bundle();
            args.putString("PROJECT_ID", currentProjectId);
            newRequestFragment.setArguments(args);
            // -------------------------------------
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, newRequestFragment)
                    .addToBackStack(null)
                    .commit();
        });

        recyclerView = view.findViewById(R.id.rvActiveRequests);
        tvEmptyState = view.findViewById(R.id.tvEmptyState);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // שימי לב: גם פה הוספנו את this::openRequestDetails כדי שהלחיצה תעבוד גם מהבית
        adapter = new MyRequestsAdapter(new ArrayList<>(), this::openRequestDetails);
        recyclerView.setAdapter(adapter);

        if (currentProjectId != null) {
            loadActiveRequests();
        } else {
            tvEmptyState.setVisibility(View.VISIBLE);
            tvEmptyState.setText("No project selected");
        }
    }

    // הוספנו את הפונקציה הזו גם כאן כדי שאפשר יהיה לפתוח פרטים מדף הבית
    private void openRequestDetails(ProjectRequest request) {
        RequestDetailsFragment detailsFragment = new RequestDetailsFragment();

        Bundle args = new Bundle();
        args.putString("PROJECT_ID", currentProjectId);
        args.putString("REQUEST_ID", request.getRequestId());

        args.putString("TITLE", request.getTitle());
        args.putString("DESC", request.getDescription());
        args.putString("CATEGORY", request.getCategory());
        args.putString("STATUS", request.getStatus());
        args.putString("URGENCY", request.getUrgency());
        args.putString("DATE", request.getPreferredDate());
        args.putString("TIME", request.getPreferredTime());
        args.putString("LOCATION", request.getLocation());
        args.putString("IMAGE_URL", request.getImageUrl());

        detailsFragment.setArguments(args);

        getParentFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, detailsFragment)
                .addToBackStack(null)
                .commit();
    }

    private void loadActiveRequests() {
        db.collection("projects").document(currentProjectId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    Project project = documentSnapshot.toObject(Project.class);
                    if (project != null && project.getRequests() != null) {
                        List<ProjectRequest> myTasks = new ArrayList<>();

                        for (ProjectRequest req : project.getRequests()) {
                            // סינון כפול: גם שזה שייך לי וגם שזה לא סגור
                            if (req.getReceiver() != null && req.getReceiver().equals(currentUserId)) {
                                if (!"Closed".equals(req.getStatus())) { // הנה התיקון!
                                    myTasks.add(req);
                                }
                            }
                        }

                        if (myTasks.isEmpty()) {
                            tvEmptyState.setVisibility(View.VISIBLE);
                            recyclerView.setVisibility(View.GONE);
                        } else {
                            tvEmptyState.setVisibility(View.GONE);
                            recyclerView.setVisibility(View.VISIBLE);
                            adapter.updateList(myTasks);
                        }
                    } else {
                        tvEmptyState.setVisibility(View.VISIBLE);
                        recyclerView.setVisibility(View.GONE);
                    }
                })
                .addOnFailureListener(e -> Log.e("HomePage", "Error loading tasks", e));
    }
}