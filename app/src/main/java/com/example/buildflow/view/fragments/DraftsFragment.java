package com.example.buildflow.view.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.buildflow.R;
import com.example.buildflow.model.ProjectRequest;
import com.example.buildflow.view.adapters.DraftsAdapter;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class DraftsFragment extends Fragment {

    private String currentProjectId;
    private RecyclerView rvDrafts;
    private DraftsAdapter adapter;
    private List<ProjectRequest> draftsList = new ArrayList<>();

    public DraftsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_drafts, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getArguments() != null) {
            currentProjectId = getArguments().getString("PROJECT_ID");
        }

        rvDrafts = view.findViewById(R.id.rvDrafts);
        rvDrafts.setLayoutManager(new LinearLayoutManager(getContext()));

        loadDrafts();

        adapter = new DraftsAdapter(draftsList, new DraftsAdapter.OnDraftClickListener() {
            @Override
            public void onDraftClick(ProjectRequest draft) {
                openDraftInNewRequest(draft);
            }

            @Override
            public void onDeleteClick(ProjectRequest draft, int position) {
                // show delete dialog
                new AlertDialog.Builder(getContext())
                        .setTitle("Delete Draft")
                        .setMessage("Are you sure you want to delete this draft?")
                        .setPositiveButton("Delete", (dialog, which) -> deleteDraft(position))
                        .setNegativeButton("Cancel", null)
                        .show();
            }
        });
        rvDrafts.setAdapter(adapter);

        ImageButton btnBack = view.findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> getParentFragmentManager().popBackStack());
        }
    }

    private void loadDrafts() {
        SharedPreferences prefs = requireContext().getSharedPreferences("ProjectDrafts", Context.MODE_PRIVATE);
        String json = prefs.getString("all_drafts_" + currentProjectId, null);

        if (json != null) {
            Gson gson = new Gson();
            Type type = new TypeToken<ArrayList<ProjectRequest>>() {}.getType();
            draftsList = gson.fromJson(json, type);
        }
    }

    private void deleteDraft(int position) {
        // remove from the list
        draftsList.remove(position);

        // update UI
        adapter.notifyItemRemoved(position);
        adapter.notifyItemRangeChanged(position, draftsList.size());

        // save to shared preferences
        SharedPreferences prefs = requireContext().getSharedPreferences("ProjectDrafts", Context.MODE_PRIVATE);
        String json = new Gson().toJson(draftsList);
        prefs.edit().putString("all_drafts_" + currentProjectId, json).apply();

        Toast.makeText(getContext(), "Draft deleted", Toast.LENGTH_SHORT).show();
    }

    private void openDraftInNewRequest(ProjectRequest draft) {
        NewRequestFragment fragment = new NewRequestFragment();
        Bundle args = new Bundle();
        args.putString("PROJECT_ID", currentProjectId);
        args.putString("DRAFT_DATA", new Gson().toJson(draft));
        fragment.setArguments(args);

        getParentFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit();
    }
}