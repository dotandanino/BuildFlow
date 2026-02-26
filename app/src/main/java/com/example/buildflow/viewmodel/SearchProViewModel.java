package com.example.buildflow.viewmodel;

import android.location.Location;
import android.util.Log;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.buildflow.model.Professional;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SearchProViewModel extends ViewModel {

    // מגדיר את כל אפשרויות המיון שלנו
    public enum SortType {
        DISTANCE_ASC("Distance: Closest"),
        DISTANCE_DESC("Distance: Farthest"),
        RATING_DESC("Rating: High to Low"),
        RATING_ASC("Rating: Low to High");

        private final String label;
        SortType(String label) { this.label = label; }
        public String getLabel() { return label; }
    }

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private List<Professional> allProfessionals = new ArrayList<>();

    private final MutableLiveData<List<Professional>> filteredProfessionalsLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> selectedCategoryLiveData = new MutableLiveData<>("All");
    private final MutableLiveData<SortType> currentSortTypeLiveData = new MutableLiveData<>(SortType.DISTANCE_ASC);

    // שמירת מצב הסינונים הנוכחי
    private String currentCategory = "All";
    private String currentSearchQuery = "";
    private SortType currentSortType = SortType.DISTANCE_ASC;

    public LiveData<List<Professional>> getFilteredProfessionals() { return filteredProfessionalsLiveData; }
    public LiveData<String> getSelectedCategory() { return selectedCategoryLiveData; }
    public LiveData<SortType> getCurrentSortType() { return currentSortTypeLiveData; }

    public void loadProfessionalsFromFirestore(Location userCurrentLocation) {
        db.collection("professionals").get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                allProfessionals.clear();

                for (QueryDocumentSnapshot doc : task.getResult()) {
                    Professional pro = doc.toObject(Professional.class);

                    if (userCurrentLocation != null && pro.getLatitude() != 0 && pro.getLongitude() != 0) {
                        Location proLocation = new Location("");
                        proLocation.setLatitude(pro.getLatitude());
                        proLocation.setLongitude(pro.getLongitude());

                        float distanceInMeters = userCurrentLocation.distanceTo(proLocation);
                        pro.setDistanceFromUser(distanceInMeters / 1000.0);
                    }
                    allProfessionals.add(pro);
                }
                applyFilters();

            } else {
                Log.e("SearchProViewModel", "Error getting professionals", task.getException());
            }
        });
    }

    // --- פעולות שהמשתמש עושה במסך ---

    public void filterByCategory(String category) {
        this.currentCategory = category;
        selectedCategoryLiveData.setValue(category);
        applyFilters();
    }

    public void setSearchQuery(String query) {
        this.currentSearchQuery = query;
        applyFilters();
    }

    public void setSortType(SortType type) {
        this.currentSortType = type;
        currentSortTypeLiveData.setValue(type);
        applyFilters();
    }

    // --- ליבת הסינון והמיון ---

    private void applyFilters() {
        List<Professional> filteredList = new ArrayList<>();

        // 1. סינון לפי קטגוריה וטקסט חיפוש
        for (Professional pro : allProfessionals) {
            boolean matchesCategory = currentCategory.equals("All") || currentCategory.equalsIgnoreCase(pro.getProfession());
            boolean matchesSearch = currentSearchQuery.isEmpty() ||
                    (pro.getName() != null && pro.getName().toLowerCase().contains(currentSearchQuery.toLowerCase()));

            if (matchesCategory && matchesSearch) {
                filteredList.add(pro);
            }
        }

        // 2. מיון התוצאות לפי בחירת המשתמש (4 אפשרויות)
        switch (currentSortType) {
            case DISTANCE_ASC:
                // מרחק: מהקרוב לרחוק (קטן לגדול)
                Collections.sort(filteredList, (p1, p2) -> Double.compare(p1.getDistanceFromUser(), p2.getDistanceFromUser()));
                break;
            case DISTANCE_DESC:
                // מרחק: מהרחוק לקרוב (גדול לקטן)
                Collections.sort(filteredList, (p1, p2) -> Double.compare(p2.getDistanceFromUser(), p1.getDistanceFromUser()));
                break;
            case RATING_DESC:
                // דירוג: מהגבוה לנמוך
                Collections.sort(filteredList, (p1, p2) -> Double.compare(p2.getRating(), p1.getRating()));
                break;
            case RATING_ASC:
                // דירוג: מהנמוך לגבוה
                Collections.sort(filteredList, (p1, p2) -> Double.compare(p1.getRating(), p2.getRating()));
                break;
        }

        filteredProfessionalsLiveData.setValue(filteredList);
    }
}