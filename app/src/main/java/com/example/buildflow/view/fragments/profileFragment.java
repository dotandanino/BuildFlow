package com.example.buildflow.view.fragments;

import android.app.AlertDialog;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.buildflow.R;
import com.example.buildflow.model.User;
import com.example.buildflow.viewmodel.ProfileViewModel;

import java.util.List;

public class profileFragment extends Fragment {

    private ProfileViewModel viewModel;

    // רכיבי תצוגה עליונים
    private ImageView ivProfileImage;
    private TextView tvProfileName;
    private TextView tvProfileRole;
    private TextView tvProfileUid;
    private TextView tvProfileEmail;

    // רכיבי תצוגה לסטטיסטיקות
    private TextView tvTotalRequests;
    private TextView tvActiveRequests;
    private TextView tvTasksCompleted;
    private ImageView btnEditProfileImage;

    // כפתורים
    private Button btnLogout;
    private TextView btnSettings;
    private TextView btnProjectMembers; // הכפתור החדש במקום Notifications!

    private String currentProjectId;

    public profileFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        viewModel = new ViewModelProvider(this).get(ProfileViewModel.class);

        if (getActivity() != null && getActivity().getIntent() != null) {
            currentProjectId = getActivity().getIntent().getStringExtra("PROJECT_ID");
            if (getActivity().getIntent().hasExtra("ROLE")) {
                String role = getActivity().getIntent().getStringExtra("ROLE");
                tvProfileRole.setText(role != null && !role.isEmpty() ? role : "Team Member");
            } else {
                tvProfileRole.setText("Team Member");
            }
        }

        android.widget.ImageButton btnOpenMenu = view.findViewById(R.id.btnOpenMenu);
        btnOpenMenu.setOnClickListener(v -> {
            if (getActivity() instanceof com.example.buildflow.view.activities.ProjectViewActivity) {
                ((com.example.buildflow.view.activities.ProjectViewActivity) getActivity()).openDrawer();
            }
        });

        observeViewModel();
        setupClickListeners();
        viewModel.loadProfileData(currentProjectId);
    }

    private final ActivityResultLauncher<String> selectImageLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    Glide.with(this).load(uri).circleCrop().into(ivProfileImage);
                    viewModel.uploadProfileImage(uri);
                    Toast.makeText(getContext(), "Uploading image...", Toast.LENGTH_SHORT).show();
                }
            }
    );

    private void initViews(View view) {
        ivProfileImage = view.findViewById(R.id.ivProfileImage);
        tvProfileName = view.findViewById(R.id.tvProfileName);
        tvProfileRole = view.findViewById(R.id.tvProfileRole);
        tvProfileUid = view.findViewById(R.id.tvProfileUid);
        tvProfileEmail = view.findViewById(R.id.tvProfileEmail);
        tvTotalRequests = view.findViewById(R.id.tvTotalRequests);
        tvActiveRequests = view.findViewById(R.id.tvActiveRequests);
        tvTasksCompleted = view.findViewById(R.id.tvTasksCompleted);
        btnLogout = view.findViewById(R.id.btnLogout);
        btnSettings = view.findViewById(R.id.btnSettings);
        btnProjectMembers = view.findViewById(R.id.btnProjectMembers); // הוחלף מ-Notifications
        btnEditProfileImage = view.findViewById(R.id.btnEditProfileImage);
    }

    private void observeViewModel() {
        viewModel.getUserLiveData().observe(getViewLifecycleOwner(), user -> {
            if (user != null) {
                tvProfileName.setText(user.getName() != null && !user.getName().isEmpty() ? user.getName() : "Unknown User");
                tvProfileEmail.setText(user.getEmail() != null ? user.getEmail() : "No email");
                tvProfileUid.setText("UID: " + user.getUid());
                if (user.getProfileImageUrl() != null && !user.getProfileImageUrl().isEmpty() && isAdded() && getActivity() != null) {
                    Glide.with(this).load(user.getProfileImageUrl()).circleCrop().into(ivProfileImage);
                }
            }
        });

        viewModel.getTotalRequestsLiveData().observe(getViewLifecycleOwner(), total -> tvTotalRequests.setText(String.valueOf(total)));
        viewModel.getActiveRequestsLiveData().observe(getViewLifecycleOwner(), active -> tvActiveRequests.setText(String.valueOf(active)));
        viewModel.getTasksCompletedLiveData().observe(getViewLifecycleOwner(), tasks -> tvTasksCompleted.setText(String.valueOf(tasks)));

        // האזנה לרשימת חברי הפרויקט מ-Firebase
        viewModel.getProjectMembersLiveData().observe(getViewLifecycleOwner(), members -> {
            if (members != null) {
                showProjectMembersDialog(members);
                // מאפסים את ה-LiveData כדי שהפופ-אפ לא יקפוץ שוב כשמסובבים את המסך
                viewModel.clearProjectMembersEvent();
            }
        });
    }

    private void setupClickListeners() {
        btnLogout.setOnClickListener(v -> {
            viewModel.logout();
            Toast.makeText(getContext(), "Logged out successfully", Toast.LENGTH_SHORT).show();
            if (getActivity() != null) {
                getActivity().finish();
            }
        });

        btnSettings.setOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new SettingsFragment())
                        .addToBackStack(null) // מאפשר חזרה אחורה עם כפתור החזור של הטלפון
                        .commit();
            }
        });

        // לחיצה על "חברי פרויקט" - מבקשים מה-ViewModel להביא נתונים!
        btnProjectMembers.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Loading members...", Toast.LENGTH_SHORT).show();
            viewModel.fetchProjectMembers(currentProjectId);
        });

        btnEditProfileImage.setOnClickListener(v -> selectImageLauncher.launch("image/*"));
    }

    // --- הפונקציה שפותחת את הפופ-אפ ---
    private void showProjectMembersDialog(List<User> members) {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_project_members, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();

        RecyclerView rvMembers = dialogView.findViewById(R.id.rvMembers);
        rvMembers.setLayoutManager(new LinearLayoutManager(getContext()));
        rvMembers.setAdapter(new MembersAdapter(members));

        dialogView.findViewById(R.id.btnCloseDialog).setOnClickListener(v -> dialog.dismiss());

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        dialog.show();
    }

    // --- אדפטר פנימי פשוט לציור הרשימה בתוך הפופ-אפ ---

    // --- אדפטר פנימי לציור הרשימה בתוך הפופ-אפ ---
    private class MembersAdapter extends RecyclerView.Adapter<MembersAdapter.MemberViewHolder> {
        private final List<User> membersList;

        public MembersAdapter(List<User> membersList) {
            this.membersList = membersList;
        }

        @NonNull
        @Override
        public MemberViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_project_member, parent, false);
            return new MemberViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull MemberViewHolder holder, int position) {
            User user = membersList.get(position);
            holder.tvName.setText(user.getName() != null ? user.getName() : "Unknown");
            holder.tvEmail.setText(user.getEmail() != null ? user.getEmail() : "No email provided");

            // הצגת התפקיד שהבאנו מה-ViewModel!
            holder.tvRole.setText(user.getProjectRole() != null ? user.getProjectRole() : "Member");

            if (user.getProfileImageUrl() != null && !user.getProfileImageUrl().isEmpty()) {
                Glide.with(holder.itemView.getContext())
                        .load(user.getProfileImageUrl())
                        .circleCrop()
                        .into(holder.ivAvatar);
            } else {
                holder.ivAvatar.setImageResource(R.drawable.bg_icon_circle);
            }
        }

        @Override
        public int getItemCount() {
            return membersList.size();
        }

        class MemberViewHolder extends RecyclerView.ViewHolder {
            ImageView ivAvatar;
            TextView tvName, tvEmail, tvRole; // הוספנו את tvRole

            public MemberViewHolder(@NonNull View itemView) {
                super(itemView);
                ivAvatar = itemView.findViewById(R.id.ivMemberAvatar);
                tvName = itemView.findViewById(R.id.tvMemberName);
                tvEmail = itemView.findViewById(R.id.tvMemberEmail);
                tvRole = itemView.findViewById(R.id.tvMemberRole); // קישור ל-XML
            }
        }
    }
}