package com.example.buildflow.view.fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

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
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class
requestManagmentFragment extends Fragment {


    private TextView tabNew, tabOpen, tabClosed;
    private LinearLayout viewNew, viewOpen, viewClosed;
    private ImageView btnCreateRequest;

    // --- משתנים חדשים לטיפול בנתונים ---
    private RecyclerView rvOpenRequests, rvClosedRequests;
    private MyRequestsAdapter openAdapter, closedAdapter;
    private FirebaseFirestore db;
    private String currentProjectId;

    public requestManagmentFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_request_managment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1. אתחול Firestore וקבלת מזהה הפרויקט
        db = FirebaseFirestore.getInstance();
        if (getActivity() != null && getActivity().getIntent() != null) {
            currentProjectId = getActivity().getIntent().getStringExtra("PROJECT_ID");
        }

        // 2. כפתור התפריט
        ImageButton btnOpenMenu = view.findViewById(R.id.btnMenu);
        btnOpenMenu.setOnClickListener(v -> {
            if (getActivity() instanceof ProjectViewActivity) {
                ((ProjectViewActivity) getActivity()).openDrawer();
            }
        });

        // 3. אתחול טאבים ותצוגות
        tabNew = view.findViewById(R.id.tabNew);
        tabOpen = view.findViewById(R.id.tabOpen);
        tabClosed = view.findViewById(R.id.tabClosed);

        viewNew = view.findViewById(R.id.viewNewRequest);
        viewOpen = view.findViewById(R.id.viewOpenRequests);
        viewClosed = view.findViewById(R.id.viewClosedRequests);

        // 4. אתחול ה-RecyclerViews וה-Adapters
        rvOpenRequests = view.findViewById(R.id.rvOpenRequests);
        rvClosedRequests = view.findViewById(R.id.rvClosedRequests);

        rvOpenRequests.setLayoutManager(new LinearLayoutManager(getContext()));
        rvClosedRequests.setLayoutManager(new LinearLayoutManager(getContext()));

        // שימוש ב-Adapter הקיים שלך (כולל לחיצה למעבר לפרטים)
        openAdapter = new MyRequestsAdapter(new ArrayList<>(), this::openRequestDetails);
        closedAdapter = new MyRequestsAdapter(new ArrayList<>(), this::openRequestDetails);

        rvOpenRequests.setAdapter(openAdapter);
        rvClosedRequests.setAdapter(closedAdapter);

        btnCreateRequest = view.findViewById(R.id.btnCreateRequest);
        btnCreateRequest.setOnClickListener(v -> {
            NewRequestFragment newRequestFragment = new NewRequestFragment();

            Bundle args = new Bundle();
            args.putString("PROJECT_ID", currentProjectId);
            newRequestFragment.setArguments(args);
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, newRequestFragment)
                    .addToBackStack(null)
                    .commit();
        });

        tabNew.setOnClickListener(v -> updateTabs("new"));
        tabOpen.setOnClickListener(v -> updateTabs("open"));
        tabClosed.setOnClickListener(v -> updateTabs("closed"));

        updateTabs("new");

        if (currentProjectId != null) {
            loadProjectRequests();
        }
    }
    // load the requests from the database and send them to the list they belong to
    private void loadProjectRequests() {
        db.collection("projects").document(currentProjectId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    Project project = documentSnapshot.toObject(Project.class);
                    if (project != null && project.getRequests() != null) {

                        List<ProjectRequest> openList = new ArrayList<>();
                        List<ProjectRequest> closedList = new ArrayList<>();

                        for (ProjectRequest req : project.getRequests()) {
                            String status = req.getStatus();
                            if ("Closed".equals(status)) {
                                closedList.add(req);
                            } else {
                                openList.add(req);
                            }
                        }
                        openAdapter.updateList(openList);
                        closedAdapter.updateList(closedList);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("RequestManagement", "Error loading requests", e);
                    Toast.makeText(getContext(), "Failed to load requests", Toast.LENGTH_SHORT).show();
                });
    }

    // same like in home fragment
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

    private void updateTabs(String selectedTab) {
        resetTabs();
        switch (selectedTab) {
            case "new":
                highlightTab(tabNew);
                viewNew.setVisibility(View.VISIBLE);
                break;
            case "open":
                highlightTab(tabOpen);
                viewOpen.setVisibility(View.VISIBLE);
                // update the data when we change tab
                if(currentProjectId != null) loadProjectRequests();
                break;
            case "closed":
                highlightTab(tabClosed);
                viewClosed.setVisibility(View.VISIBLE);
                // update the data when we change tab
                if(currentProjectId != null) loadProjectRequests();
                break;
        }
    }

    private void resetTabs() {
        int inactiveColor = Color.parseColor("#E6FFFFFF");
        tabNew.setTextColor(inactiveColor);
        tabOpen.setTextColor(inactiveColor);
        tabClosed.setTextColor(inactiveColor);

        tabNew.setBackground(null);
        tabOpen.setBackground(null);
        tabClosed.setBackground(null);

        tabNew.setElevation(0);
        tabOpen.setElevation(0);
        tabClosed.setElevation(0);

        viewNew.setVisibility(View.GONE);
        viewOpen.setVisibility(View.GONE);
        viewClosed.setVisibility(View.GONE);
    }

    private void highlightTab(TextView tab) {
        tab.setTextColor(Color.parseColor("#F4511E"));
        tab.setBackgroundResource(R.drawable.bg_tab_selected);
        tab.setElevation(4f);
    }
}