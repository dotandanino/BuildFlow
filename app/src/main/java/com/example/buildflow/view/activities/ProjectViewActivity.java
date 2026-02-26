//package com.example.buildflow.view.activities;
//
//import android.content.Intent;
//import android.os.Bundle;
//import android.view.Menu;
//import android.view.MenuItem;
//import android.view.View;
//import android.widget.Toast;
//
//import androidx.annotation.NonNull;
//import androidx.appcompat.app.AppCompatActivity;
//import androidx.core.view.GravityCompat;
//import androidx.drawerlayout.widget.DrawerLayout;
//import androidx.fragment.app.Fragment;
//
//import com.example.buildflow.R;
//import com.example.buildflow.view.fragments.AllChatsFragment;
//import com.example.buildflow.view.fragments.DraftsFragment;
//import com.example.buildflow.view.fragments.OwnerHomePageFragment;
//import com.example.buildflow.view.fragments.ProHomePageFragment;
//import com.example.buildflow.view.fragments.NewRequestFragment;
//import com.example.buildflow.view.fragments.SettingsFragment;
//import com.example.buildflow.view.fragments.profileFragment;
//import com.example.buildflow.view.fragments.requestManagmentFragment;
//import com.example.buildflow.view.fragments.searchProFragment;
//import com.google.android.material.bottomnavigation.BottomNavigationView;
//import com.google.android.material.navigation.NavigationView;
//import com.google.firebase.auth.FirebaseAuth;
//import com.google.firebase.database.DataSnapshot;
//import com.google.firebase.database.DatabaseError;
//import com.google.firebase.database.DatabaseReference;
//import com.google.firebase.database.FirebaseDatabase;
//import com.google.firebase.database.ValueEventListener;
//
//public class ProjectViewActivity extends AppCompatActivity {
//
//    private DrawerLayout drawerLayout;
//    private String currentProjectId;
//    private String currentUserRole; // שומר את התפקיד של המשתמש בפרויקט הנוכחי
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_project_view_page);
//
//        // הפעלת מערכת הנוכחות (Online/Offline)
//        setupPresenceSystem();
//
//        // 1. חילוץ הנתונים מה-Intent (שהגיעו מ-ChooseProjectActivity)
//        currentProjectId = getIntent().getStringExtra("PROJECT_ID");
//        currentUserRole = getIntent().getStringExtra("ROLE");
//
//        if (currentProjectId == null || currentProjectId.isEmpty()) {
//            Toast.makeText(this, "Error: Project ID missing", Toast.LENGTH_SHORT).show();
//            finish();
//            return;
//        }
//
//        // הגדרת כפתור התנתקות (אם קיים במסך הראשי של ה-Activity)
//        View btnLogout = findViewById(R.id.btnLogout);
//        if (btnLogout != null) {
//            btnLogout.setOnClickListener(v -> logoutUser());
//        }
//
//        drawerLayout = findViewById(R.id.drawer_layout);
//        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
//        NavigationView navigationView = findViewById(R.id.nav_view);
//
//        // 2. התאמת ממשק המשתמש לפי תפקיד (Owner לעומת איש מקצוע)
//        adjustUIByRole(bottomNav, navigationView);
//
//        // 3. טעינת מסך הבית כדיפולט בפעם הראשונה
//        if (savedInstanceState == null) {
//            if(currentUserRole == null || !currentUserRole.equals("Owner"))
//                loadFragment(new ProHomePageFragment());
//            else
//                loadFragment(new OwnerHomePageFragment());
//        }
//
//        // 4. ניהול לחיצות בתפריט התחתון (Bottom Navigation)
//        bottomNav.setOnItemSelectedListener(item -> {
//            Fragment selectedFragment = null;
//            int id = item.getItemId();
//
//            if (id == R.id.nav_home) {
//                if(currentUserRole == null || !currentUserRole.equals("Owner"))
//                    selectedFragment = new ProHomePageFragment();
//                else
//                    selectedFragment = new OwnerHomePageFragment();
//            } else if (id == R.id.nav_search) {
//                selectedFragment = new searchProFragment();
//            } else if (id == R.id.nav_request) {
//                selectedFragment = new NewRequestFragment();
//            } else if (id == R.id.nav_chats) {
//                selectedFragment = new AllChatsFragment();
//            } else if (id == R.id.nav_profile) {
//                selectedFragment = new profileFragment();
//            }
//
//            if (selectedFragment != null) {
//                loadFragment(selectedFragment);
//            }
//            return true;
//        });
//
//        // 5. ניהול לחיצות בתפריט הצדדי (Burger Menu / Drawer)
//        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
//            @Override
//            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
//                int id = item.getItemId();
//                Fragment selectedFragment = null;
//
//                if (id == R.id.nav_settings) {
//                    selectedFragment = new SettingsFragment();
//                } else if (id == R.id.nav_my_requests) {
//                    selectedFragment = new requestManagmentFragment();
//                } else if (id == R.id.nav_profile) {
//                    selectedFragment = new profileFragment();
//                } else if (id == R.id.nav_my_drafts) {
//                    selectedFragment = new DraftsFragment();
//                } else if (id == R.id.nav_my_projects) {
//                    Intent intent = new Intent(ProjectViewActivity.this, ChooseProjectActivity.class); // שנה לשם המחלקה שלך
//                    startActivity(intent);
//                }
//
//                if (selectedFragment != null) {
//                    loadFragment(selectedFragment);
//                }
//
//                // סגירת תפריט הצד לאחר הלחיצה (בהנחה שהוא נפתח מימין - END)
//                drawerLayout.closeDrawer(GravityCompat.END);
//                return true;
//            }
//        });
//    }
//
//    /**
//     * פונקציה חכמה שמעלימה כפתורים מהתפריטים אם המשתמש הוא איש מקצוע ולא בעל הבית
//     */
//    private void adjustUIByRole(BottomNavigationView bottomNav, NavigationView navView) {
//        // אם התפקיד חסר או שהוא לא "Owner", מדובר באיש מקצוע
//        if (currentUserRole == null || !currentUserRole.equals("Owner")) {
//
//            // 1. הסתרת כפתורים מהתפריט התחתון
//            Menu bottomMenu = bottomNav.getMenu();
//            MenuItem searchProItem = bottomMenu.findItem(R.id.nav_search);
//            if (searchProItem != null) {
//                searchProItem.setVisible(false); // אנשי מקצוע לא מחפשים אנשי מקצוע
//            }
//
//
////            // 2. הסתרת כפתורים מתפריט הצד
////            Menu drawerMenu = navView.getMenu();
////
////            MenuItem draftsItem = drawerMenu.findItem(R.id.nav_my_drafts);
////            if (draftsItem != null) {
////                draftsItem.setVisible(false); // לאיש מקצוע אין טיוטות לפרויקט
////            }
////
////            MenuItem settingsItem = drawerMenu.findItem(R.id.nav_settings);
////            if (settingsItem != null) {
////                settingsItem.setVisible(false); // לאיש מקצוע אין גישה להגדרות הכלליות של הפרויקט
////            }
//        }
//    }
//
//    /**
//     * טוען פרגמנט ומעביר אליו את נתוני הפרויקט (ID ותפקיד)
//     */
//    private void loadFragment(Fragment fragment) {
//        Bundle args = new Bundle();
//        args.putString("PROJECT_ID", currentProjectId);
//        args.putString("ROLE", currentUserRole); // כל פרגמנט יידע עכשיו מה התפקיד!
//        fragment.setArguments(args);
//
//        getSupportFragmentManager().beginTransaction()
//                .replace(R.id.fragment_container, fragment)
//                .commit();
//    }
//
//    /**
//     * פונקציית עזר לפרגמנטים שרוצים לפתוח את תפריט הצד (Burger Menu)
//     */
//    public void openDrawer() {
//        if (drawerLayout != null && !drawerLayout.isDrawerOpen(GravityCompat.END)) {
//            drawerLayout.openDrawer(GravityCompat.END);
//        }
//    }
//
//    /**
//     * התנתקות משתמש וחזרה למסך הלוגין
//     */
//    private void logoutUser() {
//        FirebaseAuth.getInstance().signOut();
//        Intent intent = new Intent(this, LoginActivity.class);
//        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//        startActivity(intent);
//        finish();
//    }
//
//    /**
//     * מנהל את הסטטוס מחובר/מנותק מול Firebase Realtime Database
//     */
//    private void setupPresenceSystem() {
//        String currentUserId = FirebaseAuth.getInstance().getUid();
//        if (currentUserId == null) return;
//
//        // 1. חיבור למסד הנתונים
//        FirebaseDatabase database = FirebaseDatabase.getInstance();
//        DatabaseReference myStatusRef = database.getReference("status/" + currentUserId);
//        DatabaseReference connectedRef = database.getReference(".info/connected");
//
//        // 2. האזנה לחיבור לרשת
//        connectedRef.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                boolean connected = Boolean.TRUE.equals(snapshot.getValue(Boolean.class));
//                if (connected) {
//                    // כשהמכשיר מחובר לאינטרנט, הסטטוס מוגדר ל-online
//                    myStatusRef.setValue("online");
//                    // כשפיירבייס מזהה שהחיבור נותק, הוא ישנה ל-offline אוטומטית
//                    myStatusRef.onDisconnect().setValue("offline");
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//                // שגיאה בתקשורת (אופציונלי לטפל)
//            }
//        });
//    }
//}