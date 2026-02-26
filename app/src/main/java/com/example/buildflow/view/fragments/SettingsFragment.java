package com.example.buildflow.view.fragments;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.buildflow.R;
import com.example.buildflow.view.activities.LoginActivity;
import com.example.buildflow.view.activities.ProjectViewActivity;
import com.example.buildflow.viewmodel.SettingsViewModel;

public class SettingsFragment extends Fragment {

    private SettingsViewModel viewModel;
    private TextView btnChangePassword;
    private TextView btnDeleteAccount;
    private ImageButton btnMenuSettings;

    public SettingsFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(SettingsViewModel.class);

        initViews(view);
        setupListeners();
        observeViewModel();
    }

    private void initViews(View view) {
        btnMenuSettings = view.findViewById(R.id.btnMenuSettings);
        btnChangePassword = view.findViewById(R.id.btnChangePassword);
        btnDeleteAccount = view.findViewById(R.id.btnDeleteAccount);
    }

    private void setupListeners() {
        btnMenuSettings.setOnClickListener(v -> {
            if (getActivity() instanceof ProjectViewActivity) {
                ((ProjectViewActivity) getActivity()).openDrawer();
            }
        });

        btnChangePassword.setOnClickListener(v -> {
            new AlertDialog.Builder(getContext())
                    .setTitle("Change Password")
                    .setMessage("We will send a password reset link to your email address. Continue?")
                    .setPositiveButton("Yes", (dialog, which) -> viewModel.sendPasswordResetEmail())
                    .setNegativeButton("Cancel", null)
                    .show();
        });

        btnDeleteAccount.setOnClickListener(v -> {
            new AlertDialog.Builder(getContext())
                    .setTitle("Delete Account")
                    .setMessage("Are you sure you want to permanently delete your account? All your data will be removed from projects. This action cannot be undone.")
                    .setPositiveButton("Delete", (dialog, which) -> {
                        Toast.makeText(getContext(), "Deleting account and data...", Toast.LENGTH_LONG).show();
                        viewModel.deleteAccount();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });
    }

    private void observeViewModel() {
        viewModel.getToastMessage().observe(getViewLifecycleOwner(), message -> {
            if (message != null) {
                Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
            }
        });

        viewModel.getAccountDeleted().observe(getViewLifecycleOwner(), isDeleted -> {
            if (isDeleted) {
                Intent intent = new Intent(getActivity(), LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        });
    }
}