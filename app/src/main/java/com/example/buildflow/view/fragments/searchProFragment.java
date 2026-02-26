package com.example.buildflow.view.fragments;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.buildflow.R;
import com.example.buildflow.view.activities.ProjectViewActivity;
import com.example.buildflow.view.adapters.ProfessionalAdapter;
import com.example.buildflow.viewmodel.SearchProViewModel;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

public class searchProFragment extends Fragment {

    private SearchProViewModel viewModel;
    private ProfessionalAdapter adapter;
    private TextView tvResultsCount;
    private TextView tvSort;
    private EditText etSearch;
    private LinearLayout layoutCategories;
    private ImageButton btnMenu;

    private FusedLocationProviderClient fusedLocationClient;

    private final String[] CATEGORIES = {"All", "Contractor", "Supervisor", "Electrician", "Architect", "Other"};

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    fetchLocationAndLoadData();
                } else {
                    Toast.makeText(getContext(), "Location permission denied. Showing all areas.", Toast.LENGTH_SHORT).show();
                    viewModel.loadProfessionalsFromFirestore(null);
                }
            });

    public searchProFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_search_pro, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(SearchProViewModel.class);

        // אתחול רכיבי UI
        tvResultsCount = view.findViewById(R.id.tvResultsCount);
        tvSort = view.findViewById(R.id.tvSort);
        etSearch = view.findViewById(R.id.etSearch);
        layoutCategories = view.findViewById(R.id.layoutCategories);
        btnMenu = view.findViewById(R.id.btnMenu);
        RecyclerView rvProfessionals = view.findViewById(R.id.rvProfessionals);

        rvProfessionals.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new ProfessionalAdapter();
        rvProfessionals.setAdapter(adapter);

        buildCategoryChips();
        setupListeners();
        observeViewModel();

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());
        checkPermissionsAndLoadData();
    }

    private void setupListeners() {
        // פתיחת תפריט המבורגר
        btnMenu.setOnClickListener(v -> {
            if (getActivity() instanceof ProjectViewActivity) {
                ((ProjectViewActivity) getActivity()).openDrawer();
            }
        });

        // פתיחת דיאלוג המיון
        tvSort.setOnClickListener(v -> showSortDialog());

        // חיפוש חופשי בזמן אמת
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                viewModel.setSearchQuery(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    // יצירת חלון בחירת סוג המיון
    private void showSortDialog() {
        String[] options = {
                "Distance: Closest first",
                "Distance: Farthest first",
                "Rating: High to Low",
                "Rating: Low to High"
        };

        // מציאת המיקום של הסימון הנוכחי (כדי שיסומן בדיאלוג)
        int checkedItem = 0;
        SearchProViewModel.SortType currentSort = viewModel.getCurrentSortType().getValue();
        if (currentSort != null) {
            checkedItem = currentSort.ordinal(); // זה יעבוד מצוין כי הסדר במערך תואם ל-Enum
        }

        new AlertDialog.Builder(requireContext())
                .setTitle("Sort by")
                .setSingleChoiceItems(options, checkedItem, (dialog, which) -> {
                    switch (which) {
                        case 0: viewModel.setSortType(SearchProViewModel.SortType.DISTANCE_ASC); break;
                        case 1: viewModel.setSortType(SearchProViewModel.SortType.DISTANCE_DESC); break;
                        case 2: viewModel.setSortType(SearchProViewModel.SortType.RATING_DESC); break;
                        case 3: viewModel.setSortType(SearchProViewModel.SortType.RATING_ASC); break;
                    }
                    dialog.dismiss();
                })
                .show();
    }

    private void checkPermissionsAndLoadData() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fetchLocationAndLoadData();
        } else {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
        }
    }

    private void fetchLocationAndLoadData() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            viewModel.loadProfessionalsFromFirestore(null);
            return;
        }

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        viewModel.loadProfessionalsFromFirestore(location);
                    } else {
                        Toast.makeText(getContext(), "Could not get current location", Toast.LENGTH_SHORT).show();
                        viewModel.loadProfessionalsFromFirestore(null);
                    }
                })
                .addOnFailureListener(e -> viewModel.loadProfessionalsFromFirestore(null));
    }

    private void observeViewModel() {
        viewModel.getSelectedCategory().observe(getViewLifecycleOwner(), this::updateCategoryUI);

        viewModel.getFilteredProfessionals().observe(getViewLifecycleOwner(), pros -> {
            adapter.setProfessionals(pros);
            tvResultsCount.setText(pros.size() + " professionals found");
        });

        // עדכון טקסט כפתור המיון למה שהמשתמש בחר הרגע
        viewModel.getCurrentSortType().observe(getViewLifecycleOwner(), sortType -> {
            if (sortType != null) {
                tvSort.setText(sortType.getLabel()); // מביא את הטקסט מה-Enum שהגדרנו
            }
        });
    }

    private void buildCategoryChips() {
        layoutCategories.removeAllViews();
        for (String category : CATEGORIES) {
            TextView chip = new TextView(getContext());
            chip.setText(category);
            chip.setPadding(40, 16, 40, 16);
            chip.setTextSize(14f);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            params.setMargins(0, 0, 16, 0);
            chip.setLayoutParams(params);

            chip.setOnClickListener(v -> viewModel.filterByCategory(category));
            layoutCategories.addView(chip);
        }
    }

    private void updateCategoryUI(String selectedCategory) {
        for (int i = 0; i < layoutCategories.getChildCount(); i++) {
            TextView chip = (TextView) layoutCategories.getChildAt(i);
            if (chip.getText().toString().equals(selectedCategory)) {
                chip.setBackgroundResource(R.drawable.bg_category_selected);
                chip.setTextColor(Color.WHITE);
            } else {
                chip.setBackgroundResource(R.drawable.bg_category_btn);
                chip.setTextColor(Color.parseColor("#424242"));
            }
        }
    }
}