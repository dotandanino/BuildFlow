package com.example.buildflow.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.buildflow.model.Expense;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

public class ExpensesViewModel extends ViewModel {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private List<Expense> allExpenses = new ArrayList<>();

    private final MutableLiveData<List<Expense>> filteredExpensesLiveData = new MutableLiveData<>();

    // שני סכומים נפרדים! אחד לכלל הפרויקט, ואחד לסינון הנוכחי
    private final MutableLiveData<Double> totalAmountLiveData = new MutableLiveData<>(0.0);
    private final MutableLiveData<Double> filteredAmountLiveData = new MutableLiveData<>(0.0);

    private String currentDateFilter = "All";
    private String currentCategoryFilter = "All";

    public LiveData<List<Expense>> getFilteredExpenses() { return filteredExpensesLiveData; }
    public LiveData<Double> getTotalAmount() { return totalAmountLiveData; } // הסכום הכללי
    public LiveData<Double> getFilteredAmount() { return filteredAmountLiveData; } // הסכום המסונן

    public void loadExpenses(String projectId) {
        db.collection("projects").document(projectId).collection("expenses")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    allExpenses.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Expense exp = doc.toObject(Expense.class);
                        allExpenses.add(exp);
                    }
                    Collections.sort(allExpenses, (e1, e2) -> Long.compare(e2.getTimestamp(), e1.getTimestamp()));
                    applyFilters();
                })
                .addOnFailureListener(e -> {
                    System.out.println("Error loading expenses: " + e.getMessage());
                });
    }

    public void setDateFilter(String dateFilter) {
        this.currentDateFilter = dateFilter;
        applyFilters();
    }

    public void setCategoryFilter(String categoryFilter) {
        this.currentCategoryFilter = categoryFilter;
        applyFilters();
    }

    private void applyFilters() {
        List<Expense> filteredList = new ArrayList<>();
        double absoluteTotal = 0.0; // סכום של כל ההוצאות
        double filteredTotal = 0.0; // סכום של ההוצאות שעברו את הסינון

        long targetTime = 0;
        Calendar calendar = Calendar.getInstance();

        if (currentDateFilter.equals("Week")) {
            calendar.add(Calendar.DAY_OF_YEAR, -7);
            targetTime = calendar.getTimeInMillis();
        } else if (currentDateFilter.equals("Month")) {
            calendar.add(Calendar.MONTH, -1);
            targetTime = calendar.getTimeInMillis();
        } else if (currentDateFilter.equals("6Months")) {
            calendar.add(Calendar.MONTH, -6);
            targetTime = calendar.getTimeInMillis();
        }

        for (Expense exp : allExpenses) {
            // קודם כל מוסיפים לסכום הכללי (ללא קשר לסינון)
            absoluteTotal += exp.getAmount();

            boolean matchesDate = (currentDateFilter.equals("All") || exp.getTimestamp() >= targetTime);
            boolean matchesCategory = (currentCategoryFilter.equals("All") || exp.getCategory().equals(currentCategoryFilter));

            // אם זה תואם לסינון, נוסיף לרשימה ולסכום המסונן
            if (matchesDate && matchesCategory) {
                filteredList.add(exp);
                filteredTotal += exp.getAmount();
            }
        }

        // מעדכנים את כל הנתונים החוצה
        filteredExpensesLiveData.setValue(filteredList);
        totalAmountLiveData.setValue(absoluteTotal); // מעדכן את המספר הגדול למעלה
        filteredAmountLiveData.setValue(filteredTotal); // מעדכן את המספר הקטן מעל הרשימה
    }

    public void addExpense(String projectId, Expense newExpense) {
        db.collection("projects").document(projectId).collection("expenses")
                .document(newExpense.getId())
                .set(newExpense)
                .addOnSuccessListener(aVoid -> {
                    allExpenses.add(newExpense);
                    Collections.sort(allExpenses, (e1, e2) -> Long.compare(e2.getTimestamp(), e1.getTimestamp()));
                    applyFilters();
                })
                .addOnFailureListener(e -> {
                    System.out.println("Error adding expense: " + e.getMessage());
                });
    }
}