package com.example.buildflow.view.fragments;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider; // חשוב!
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.buildflow.R;
import com.example.buildflow.model.ChatConversation;
import com.example.buildflow.view.activities.ProjectViewActivity;
import com.example.buildflow.view.adapters.ChatsAdapter;
import com.example.buildflow.viewmodel.AllChatsViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

public class AllChatsFragment extends Fragment {

    private RecyclerView rvChats;
    private ChatsAdapter adapter;
    private ImageButton btnMenu;
    private EditText etSearch;

    private AllChatsViewModel viewModel; // ה-ViewModel שלנו
    private List<ChatConversation> fullChatList = new ArrayList<>(); // רשימה לשמירה מקומית לצורך חיפוש

    public AllChatsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_chats_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1. אתחול ה-ViewModel
        viewModel = new ViewModelProvider(this).get(AllChatsViewModel.class);

        // 2. חיבור לרכיבים
        rvChats = view.findViewById(R.id.rvChats);
        btnMenu = view.findViewById(R.id.btnMenu);
        etSearch = view.findViewById(R.id.etSearch);

        // 3. הגדרת ה-RecyclerView (בהתחלה ריק)
        rvChats.setLayoutManager(new LinearLayoutManager(getContext()));
        String myId = FirebaseAuth.getInstance().getUid(); // שליפת ה-ID שלי
        adapter = new ChatsAdapter(new ArrayList<>(), myId, this::openChatDetail);
        rvChats.setAdapter(adapter);

        // 4. כפתור תפריט
        btnMenu.setOnClickListener(v -> {
            if (getActivity() instanceof ProjectViewActivity) {
                ((ProjectViewActivity) getActivity()).openDrawer();
            }
        });

        // 5. --- הקסם של MVVM: האזנה לנתונים ---
        // אנחנו מבקשים מה-Activity את ה-ID של הפרויקט הנוכחי
        String projectId = null;
        if (getActivity() != null && getActivity().getIntent() != null) {
            projectId = getActivity().getIntent().getStringExtra("PROJECT_ID");
        }
        String currentUserId = FirebaseAuth.getInstance().getUid();

        if (projectId != null && currentUserId != null) {
            // מתחילים להאזין
            viewModel.loadChats(projectId, currentUserId);

            // ברגע שיהיה עדכון ב-ViewModel, הקוד הזה ירוץ:
            viewModel.getChats().observe(getViewLifecycleOwner(), chats -> {
                fullChatList = chats; // שומרים בצד לחיפוש
                adapter.updateList(chats); // נצטרך להוסיף פונקציה קטנה באדפטר
            });
        }

        // 6. חיפוש (נשאר אותו דבר, רק עובד על הרשימה האמיתית)
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filter(s.toString());
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        // ... בתוך onViewCreated ...

        FloatingActionButton btnNewChat = view.findViewById(R.id.btnNewChat);
        btnNewChat.setOnClickListener(v -> {
            // מעבר למסך בחירת משתמש
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new com.example.buildflow.view.fragments.PickUserFragment())
                    .addToBackStack(null)
                    .commit();
        });

    }

    private void filter(String text) {
        List<ChatConversation> filteredList = new ArrayList<>();
        for (ChatConversation item : fullChatList) {
            if (item.getName().toLowerCase().contains(text.toLowerCase())) {
                filteredList.add(item);
            }
        }
        // עדכון האדפטר (נוודא שיש לך פונקציה כזו באדפטר, אם לא - תגידי לי)
        // אפשר גם ליצור אדפטר חדש כל פעם אם אין פונקציית עדכון:
        String myId = FirebaseAuth.getInstance().getUid(); // שליפת ה-ID שלי
        adapter = new ChatsAdapter(new ArrayList<>(), myId, this::openChatDetail);
        rvChats.setAdapter(adapter);
    }

    private void openChatDetail(ChatConversation chat) {
        ChatDetailFragment chatFragment = new ChatDetailFragment();
        Bundle args = new Bundle();

        // 1. העברת השם והתפקיד (זה כבר היה לך)
        args.putString("CHAT_NAME", chat.getName());
        args.putString("CHAT_ROLE", chat.getRole());

        // 2. העברת מזהה הפרויקט (חשוב!)
        String projectId = getActivity().getIntent().getStringExtra("PROJECT_ID");
        args.putString("PROJECT_ID", projectId);

        // --- התיקון הגדול: חילוץ ה-ID של הצד השני ---
        String currentUserId = FirebaseAuth.getInstance().getUid();
        String receiverId = "";

        // ה-ID של הצ'אט הוא תמיד: uid1_uid2
        if (chat.getId() != null && chat.getId().contains("_")) {
            String[] parts = chat.getId().split("_");
            if (parts.length == 2) {
                if (parts[0].equals(currentUserId)) {
                    receiverId = parts[1]; // החלק השני הוא החבר
                } else {
                    receiverId = parts[0]; // החלק הראשון הוא החבר
                }
            }
        }

        // 3. העברת ה-ID הקריטי!
        args.putString("RECEIVER_ID", receiverId);

        chatFragment.setArguments(args);

        getParentFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, chatFragment)
                .addToBackStack(null)
                .commit();
    }
}