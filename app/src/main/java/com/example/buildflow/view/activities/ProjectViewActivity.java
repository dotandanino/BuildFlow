package com.example.buildflow.view.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.example.buildflow.view.fragments.DraftsFragment;
import com.example.buildflow.view.fragments.SettingsFragment;
import com.example.buildflow.view.fragments.HelpFragment;
import com.example.buildflow.view.fragments.NewRequestFragment;
import com.example.buildflow.view.fragments.PrivacyFragment;
import com.example.buildflow.R;
import com.example.buildflow.view.fragments.HomePageFragment;
import com.example.buildflow.view.fragments.allChatsFragment;
import com.example.buildflow.view.fragments.profileFragment;
import com.example.buildflow.view.fragments.requestManagmentFragment;
import com.example.buildflow.view.fragments.searchProFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;

public class ProjectViewActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private String currentProjectId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_project_view_page);

        // take the ID from the intent
        currentProjectId = getIntent().getStringExtra("PROJECT_ID");

        // we want to make sure we received the project id
        if (currentProjectId == null || currentProjectId.isEmpty()) {
            Toast.makeText(this, "Error: Project ID missing", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        View btnLogout = findViewById(R.id.btnLogout);
        if (btnLogout != null) {
            btnLogout.setOnClickListener(v -> logoutUser());
        }

        drawerLayout = findViewById(R.id.drawer_layout);
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        NavigationView navigationView = findViewById(R.id.nav_view);

        //the home fragment is the default one
        if (savedInstanceState == null) {
            loadFragment(new HomePageFragment());
        }

        // the bottom nenu
        bottomNav.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                selectedFragment = new HomePageFragment();
            } else if (id == R.id.nav_search) {
                selectedFragment = new searchProFragment();
            } else if (id == R.id.nav_request) {
                selectedFragment = new NewRequestFragment();
            } else if (id == R.id.nav_chats) {
                selectedFragment = new allChatsFragment();
            } else if (id == R.id.nav_profile) {
                selectedFragment = new profileFragment();
            }

            if (selectedFragment != null) {
                loadFragment(selectedFragment);
            }
            return true;
        });

        // Burger menu
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                Fragment selectedFragment = null;
                if (id == R.id.nav_settings) {
                    selectedFragment = new SettingsFragment();
                } else if (id == R.id.nav_help) {
                    selectedFragment = new HelpFragment();
                } else if (id == R.id.nav_my_requests) {
                    selectedFragment = new requestManagmentFragment();
                } else if (id == R.id.nav_privacy) {
                    selectedFragment = new PrivacyFragment();
                } else if (id == R.id.nav_profile) {
                    selectedFragment = new profileFragment();
                } else if (id == R.id.nav_my_drafts) {
                    selectedFragment = new DraftsFragment();
                }



                if (selectedFragment != null) {
                    loadFragment(selectedFragment);
                }

                drawerLayout.closeDrawer(GravityCompat.END);
                return true;
            }
        });
    }

    // function to change fragment
    private void loadFragment(Fragment fragment) {
        Bundle args = new Bundle();
        args.putString("PROJECT_ID", currentProjectId);
        fragment.setArguments(args); // We send the project ID to the fragment
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }

    public void openDrawer() {// open the "burger menu"
        if (!drawerLayout.isDrawerOpen(GravityCompat.END)) {
            drawerLayout.openDrawer(GravityCompat.END);
        }
    }

    private void logoutUser() {
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }
    }

//    public void navigateToNewRequest() {
//        NewRequestFragment fragment = new NewRequestFragment();
//
//        Bundle args = new Bundle();
//        args.putString("PROJECT_ID", currentProjectId);
//        fragment.setArguments(args);
//
//        getSupportFragmentManager().beginTransaction()
//                .replace(R.id.fragment_container, fragment)
//                .addToBackStack(null)
//                .commit();
//    }
}