package com.example.buildflow.view.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton; // הוספנו

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.buildflow.R;
import com.example.buildflow.model.UserModel;
import com.example.buildflow.view.adapters.UsersAdapter;
import com.example.buildflow.viewmodel.PickUserViewModel;

public class PickUserFragment extends Fragment {

    private RecyclerView rvUsers;
    private PickUserViewModel viewModel;
    private ImageButton btnBack; // משתנה לכפתור החזור

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // שימוש בקובץ ה-XML החדש והנקי שיצרנו
        return inflater.inflate(R.layout.fragment_pick_user, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // אתחול רכיבים (שימי לב שה-ID התעדכנו לפי ה-XML החדש)
        rvUsers = view.findViewById(R.id.rvUsers);
        btnBack = view.findViewById(R.id.btnBack);

        // לוגיקה לכפתור חזור
        btnBack.setOnClickListener(v -> getParentFragmentManager().popBackStack());

        // אתחול ViewModel
        viewModel = new ViewModelProvider(this).get(PickUserViewModel.class);

        String projectId = "default_project";
        if (getActivity() != null && getActivity().getIntent() != null) {
            projectId = getActivity().getIntent().getStringExtra("PROJECT_ID");
        }

        rvUsers.setLayoutManager(new LinearLayoutManager(getContext()));

        viewModel.loadUsers(projectId);
        viewModel.users.observe(getViewLifecycleOwner(), users -> {
            Log.d("DEBUG_STATUS", "List size received: " + (users != null ? users.size() : "null"));
            UsersAdapter adapter = new UsersAdapter(users, this::openNewChat);
            rvUsers.setAdapter(adapter);
        });
    }

    private void openNewChat(UserModel user) {
        ChatDetailFragment chatFragment = new ChatDetailFragment();
        Bundle args = new Bundle();
        args.putString("CHAT_NAME", user.getName());
        args.putString("CHAT_ROLE", user.getRole());
        args.putString("RECEIVER_ID", user.getUid());

        String projectId = getActivity().getIntent().getStringExtra("PROJECT_ID");
        args.putString("PROJECT_ID", projectId);

        chatFragment.setArguments(args);

        getParentFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, chatFragment)
                .addToBackStack(null)
                .commit();
    }
}