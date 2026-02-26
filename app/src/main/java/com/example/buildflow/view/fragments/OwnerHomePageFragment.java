package com.example.buildflow.view.fragments;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.buildflow.R;
import com.example.buildflow.model.Expense;
import com.example.buildflow.view.adapters.ExpenseAdapter;
import com.example.buildflow.viewmodel.ExpensesViewModel;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.UUID;

public class OwnerHomePageFragment extends Fragment {

    private ExpensesViewModel viewModel;
    private ExpenseAdapter adapter;
    private String currentProjectId;
    private String currentUserRole;

    private TextView tvTotalAmount;
    private LinearLayout layoutDateFilters;
    private LinearLayout layoutCategoryFilters;

    private TextView tvFilteredTotal;

    // הגדרת הסינונים
    private final String[] DATE_FILTERS = {"All", "Week", "Month", "6Months"};
    private final String[] CATEGORY_FILTERS = {"All", "Materials", "Labor", "Permits", "Contractors", "Other"};

    public OwnerHomePageFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_owner_home_page, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // שליפת הנתונים מה-Arguments
        if (getArguments() != null) {
            currentProjectId = getArguments().getString("PROJECT_ID");
            currentUserRole = getArguments().getString("ROLE");
        }

        viewModel = new ViewModelProvider(this).get(ExpensesViewModel.class);

        // אתחול UI
        tvTotalAmount = view.findViewById(R.id.tvTotalAmount);
        layoutDateFilters = view.findViewById(R.id.layoutDateFilters);
        layoutCategoryFilters = view.findViewById(R.id.layoutCategoryFilters);

        tvFilteredTotal = view.findViewById(R.id.tvFilteredTotal);

        RecyclerView rvExpenses = view.findViewById(R.id.rvExpenses);
        rvExpenses.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new ExpenseAdapter();
        rvExpenses.setAdapter(adapter);

        // בניית כפתורי הסינון
        buildFilterChips(layoutDateFilters, DATE_FILTERS, true);
        buildFilterChips(layoutCategoryFilters, CATEGORY_FILTERS, false);

        // האזנה ל-ViewModel
        observeViewModel();

        // קריאה ראשונית למסד הנתונים
        if (currentProjectId != null) {
            viewModel.loadExpenses(currentProjectId);
        }

        // לחיצה על כפתור ההוספה (FAB) פותחת עכשיו את הפופ-אפ האמיתי!
        view.findViewById(R.id.fabAddExpense).setOnClickListener(v -> showAddExpenseDialog());

        android.widget.ImageButton btnOpenMenu = view.findViewById(R.id.btnOpenMenu);
        btnOpenMenu.setOnClickListener(v -> {
            if (getActivity() instanceof com.example.buildflow.view.activities.ProjectViewActivity) {
                ((com.example.buildflow.view.activities.ProjectViewActivity) getActivity()).openDrawer();
            }
        });
    }

    private void observeViewModel() {
        // 1. האזנה לרשימת ההוצאות
        viewModel.getFilteredExpenses().observe(getViewLifecycleOwner(), expenses -> {
            adapter.setExpenses(expenses);
        });

        // 2. האזנה לסכום הכללי (מעדכן את הכותרת הגדולה למעלה)
        viewModel.getTotalAmount().observe(getViewLifecycleOwner(), total -> {
            String formattedTotal = "₪" + String.format(Locale.getDefault(), "%,.2f", total);
            tvTotalAmount.setText(formattedTotal);
        });

        // 3. האזנה לסכום המסונן (מעדכן את הכותרת הקטנה מעל הרשימה)
        viewModel.getFilteredAmount().observe(getViewLifecycleOwner(), filteredTotal -> {
            String formattedFilteredTotal = "₪" + String.format(Locale.getDefault(), "%,.2f", filteredTotal);
            tvFilteredTotal.setText(formattedFilteredTotal);
        });
    }

    // --- הוספת הפונקציה של הדיאלוג ---

    private void showAddExpenseDialog() {
        // 1. ניפוח התצוגה של הפופ-אפ מתוך קובץ ה-XML
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_expense, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();

        // 2. חיבור הרכיבים מתוך העיצוב
        EditText etTitle = dialogView.findViewById(R.id.etExpenseTitle);
        EditText etAmount = dialogView.findViewById(R.id.etExpenseAmount);
        TextView tvDateSelect = dialogView.findViewById(R.id.tvExpenseDateSelect); // השדה של התאריך
        Spinner spinnerCategory = dialogView.findViewById(R.id.spinnerExpenseCategory);
        Button btnCancel = dialogView.findViewById(R.id.btnCancelExpense);
        Button btnSave = dialogView.findViewById(R.id.btnSaveExpense);

        // 3. הגדרת רשימת הקטגוריות ל-Spinner
        String[] spinnerCategories = {"Materials", "Labor", "Permits", "Contractors", "Other"};
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, spinnerCategories);
        spinnerCategory.setAdapter(spinnerAdapter);

        // --- 4. לוגיקת בחירת התאריך (DatePicker) ---
        final Calendar calendar = Calendar.getInstance();
        // שימוש במערך של תא אחד כדי שנוכל לעדכן אותו מתוך פונקציית הלחיצה
        final long[] selectedTimestamp = {calendar.getTimeInMillis()};
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

        // הגדרת תאריך ברירת המחדל (היום) בתוך השדה
        tvDateSelect.setText(sdf.format(calendar.getTime()));

        // פתיחת יומן בעת לחיצה על שדה התאריך
        tvDateSelect.setOnClickListener(v -> {
            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    getContext(),
                    (view, year, month, dayOfMonth) -> {
                        // עדכון הקלנדר לתאריך שהמשתמש בחר
                        calendar.set(Calendar.YEAR, year);
                        calendar.set(Calendar.MONTH, month);
                        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                        // שמירת הזמן המדויק במילישניות ועדכון הטקסט במסך
                        selectedTimestamp[0] = calendar.getTimeInMillis();
                        tvDateSelect.setText(sdf.format(calendar.getTime()));
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
            );
            datePickerDialog.show();
        });

        // 5. לחיצה על כפתור ביטול
        btnCancel.setOnClickListener(v -> dialog.dismiss());

        // 6. לחיצה על כפתור שמירה
        btnSave.setOnClickListener(v -> {
            String title = etTitle.getText().toString().trim();
            String amountStr = etAmount.getText().toString().trim();
            String category = spinnerCategory.getSelectedItem().toString();

            // בדיקת תקינות קלט: חובה להזין שם וסכום
            if (title.isEmpty() || amountStr.isEmpty()) {
                Toast.makeText(getContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            double amount = Double.parseDouble(amountStr);
            String id = UUID.randomUUID().toString(); // מזהה ייחודי
                long timestamp = selectedTimestamp[0]; // התאריך שנבחר (או היום)

            // יצירת אובייקט ההוצאה
            Expense newExpense = new Expense(id, title, amount, category, timestamp);

            // שמירה ב-Firestore דרך ה-ViewModel
            if (currentProjectId != null) {
                viewModel.addExpense(currentProjectId, newExpense);
                Toast.makeText(getContext(), "Expense added successfully!", Toast.LENGTH_SHORT).show();
                dialog.dismiss(); // סגירת הפופ-אפ
            } else {
                Toast.makeText(getContext(), "Error: No project selected", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }

    // --- פונקציות העזר לבניית הכפתורים הנגללים ---
    private void buildFilterChips(LinearLayout container, String[] filters, boolean isDateFilter) {
        container.removeAllViews();
        for (String filter : filters) {
            TextView chip = new TextView(getContext());
            chip.setText(filter);
            chip.setPadding(40, 16, 40, 16);
            chip.setTextSize(14f);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            params.setMargins(0, 0, 16, 0);
            chip.setLayoutParams(params);

            // צבע התחלתי (All תמיד צבוע בהתחלה)
            if (filter.equals("All")) {
                chip.setBackgroundResource(R.drawable.bg_category_selected);
                chip.setTextColor(Color.WHITE);
            } else {
                chip.setBackgroundResource(R.drawable.bg_category_btn);
                chip.setTextColor(Color.parseColor("#424242"));
            }

            chip.setOnClickListener(v -> {
                // עדכון ה-ViewModel
                if (isDateFilter) {
                    viewModel.setDateFilter(filter);
                } else {
                    viewModel.setCategoryFilter(filter);
                }
                // שינוי הצבעים בשורה הספציפית
                updateChipColors(container, filter);
            });

            container.addView(chip);
        }
    }

    private void updateChipColors(LinearLayout container, String selectedFilter) {
        for (int i = 0; i < container.getChildCount(); i++) {
            TextView chip = (TextView) container.getChildAt(i);
            if (chip.getText().toString().equals(selectedFilter)) {
                chip.setBackgroundResource(R.drawable.bg_category_selected);
                chip.setTextColor(Color.WHITE);
            } else {
                chip.setBackgroundResource(R.drawable.bg_category_btn);
                chip.setTextColor(Color.parseColor("#424242"));
            }
        }
    }
}