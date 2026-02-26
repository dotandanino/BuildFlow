package com.example.buildflow.model;

import android.util.Log;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.Arrays;
import java.util.UUID;

public class DatabaseSeeder {

    public static void seedProfessionals() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // הקטגוריות שלפיהן ניצור אנשי מקצוע (תואם ל-searchProFragment)
        String[] professions = {"Contractor", "Supervisor", "Electrician", "Architect", "Plumber"};

        // נתונים רנדומליים ליצירת פרופילים אמינים
        String[] firstNames = {"Yossi", "Moshe", "David", "Eran", "Omer", "Ron", "Avi", "Eli", "Daniel", "Idan"};
        String[] lastNames = {"Cohen", "Levi", "Mizrachi", "Peretz", "Biton", "Dahan", "Agam", "Katz"};

        // ערים מרכזיות בישראל והקואורדינטות שלהן (כדי שחישוב המרחק יעבוד!)
        String[] cities = {"Tel Aviv", "Haifa", "Jerusalem", "Netanya", "Rishon LeZion"};
        double[] lats = {32.0853, 32.7940, 31.7683, 32.3215, 31.9730};
        double[] lons = {34.7818, 34.9896, 35.2137, 34.8532, 34.7925};

        for (String profession : professions) {
            for (int i = 0; i < 5; i++) {
                // יצירת ID ייחודי קצר
                String userId = UUID.randomUUID().toString().substring(0, 10);
                String firstName = firstNames[(int) (Math.random() * firstNames.length)];
                String lastName = lastNames[(int) (Math.random() * lastNames.length)];
                String fullName = firstName + " " + lastName;

                // בחירת עיר רנדומלית מתוך ה-5
                int cityIndex = (int) (Math.random() * cities.length);

                Professional pro = new Professional();
                pro.setUserId(userId);
                pro.setName(fullName);
                pro.setProfession(profession);
                pro.setAvatarUrl(""); // אפשר להשאיר ריק, האדפטר שלך שם תמונת ברירת מחדל

                // יצירת דירוג אקראי בין 4.0 ל-5.0
                double randomRating = 4.0 + (Math.random());
                pro.setRating(Math.round(randomRating * 10.0) / 10.0);

                pro.setReviewsCount((int) (Math.random() * 100) + 1);
                pro.setHourlyRate(100 + (Math.random() * 200)); // תעריף בין 100 ל-300 דולר/שקל
                pro.setTotalJobs((int) (Math.random() * 50) + 5);
                pro.setDescription("Expert " + profession + " with over 10 years of experience. Providing top quality services in " + cities[cityIndex] + " and surrounding areas.");
                pro.setVerified(Math.random() > 0.5); // 50% סיכוי להיות Verified
                pro.setPhoneNumber("050-" + (1000000 + (int) (Math.random() * 8999999)));
                pro.setEmail(firstName.toLowerCase() + "." + lastName.toLowerCase() + "@example.com");
                pro.setServiceCities(Arrays.asList(cities[cityIndex]));

                // קואורדינטות (עם סטייה קטנה מאוד כדי שלא כולם יישבו על אותו פיקסל במפה)
                pro.setLatitude(lats[cityIndex] + (Math.random() * 0.02 - 0.01));
                pro.setLongitude(lons[cityIndex] + (Math.random() * 0.02 - 0.01));

                // יצירת מפתח המסמך כמו שעשינו בהרשמה: userId_profession
                String docId = userId + "_" + profession;

                db.collection("professionals").document(docId).set(pro)
                        .addOnSuccessListener(aVoid -> Log.d("DatabaseSeeder", "Created " + profession + ": " + fullName))
                        .addOnFailureListener(e -> Log.e("DatabaseSeeder", "Error creating pro", e));
            }
        }
    }
}