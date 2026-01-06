package com.example.buildflow.view.fragments;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.buildflow.R;
import com.example.buildflow.model.ChatConversation;
import com.example.buildflow.view.activities.ProjectViewActivity;
import com.example.buildflow.view.adapters.ChatsAdapter;

import java.util.ArrayList;
import java.util.List;

public class AllChatsFragment extends Fragment {

    private RecyclerView rvChats;
    private ChatsAdapter adapter;
    private ImageButton btnMenu;

    // --- הוספתי את המשתנים האלו בשביל החיפוש ---
    private EditText etSearch;
    private List<ChatConversation> chatList; // הפכתי את הרשימה למשתנה של המחלקה כדי שנוכל לסנן אותה
    // -------------------------------------------

    public AllChatsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // מוודא שאנחנו משתמשים בשם ה-XML הנכון שהופיע בקוד שלך
        return inflater.inflate(R.layout.fragment_chats_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1. חיבור לרכיבים (כולל שורת החיפוש שהוספתי)
        rvChats = view.findViewById(R.id.rvChats);
        btnMenu = view.findViewById(R.id.btnMenu);
        etSearch = view.findViewById(R.id.etSearch); // <--- הוספתי קישור לחיפוש

        // 2. הגדרת כפתור התפריט (הקוד המקורי שלך)
        btnMenu.setOnClickListener(v -> {
            if (getActivity() instanceof ProjectViewActivity) {
                ((ProjectViewActivity) getActivity()).openDrawer();
            }
        });

        // 3. יצירת הנתונים
        chatList = new ArrayList<>();
        // הנתונים שהכנסת (השארתי כמו שהם)
        chatList.add(new ChatConversation("1", "Mike Johnson", "Plumber", "I'll arrive around 2 PM tomorrow", "10:30 AM", 2, true));
        chatList.add(new ChatConversation("2", "Sarah Lee", "Painter", "Color samples are ready", "Yesterday", 0, false));
        chatList.add(new ChatConversation("3", "David Chen", "Electrician", "Wiring is done", "Dec 1", 0, true));
        chatList.add(new ChatConversation("4", "Emma Wilson", "Interior Designer", "Design looks great!", "Nov 30", 1, false));

        // 4. חיבור ה-Adapter
        rvChats.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new ChatsAdapter(chatList, this::openChatDetail); // קיצור דרך לקריאה לפונקציה
        rvChats.setAdapter(adapter);

        // 5. --- כאן הוספתי את הלוגיקה של החיפוש ---
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // בכל פעם שמקלידים אות, אנחנו קוראים לפונקציית הסינון
                filter(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    // --- פונקציית הסינון שהוספתי ---
    private void filter(String text) {
        // אם מחקו את הטקסט - מציגים את הכל
        if (text.isEmpty()) {
            adapter.filterList(chatList);
            return;
        }

        // יצירת רשימה חדשה רק עם התוצאות המתאימות
        List<ChatConversation> filteredList = new ArrayList<>();
        for (ChatConversation item : chatList) {
            if (item.getName().toLowerCase().contains(text.toLowerCase())) {
                filteredList.add(item);
            }
        }
        // עדכון ה-Adapter
        adapter.filterList(filteredList);
    }

    private void openChatDetail(ChatConversation chat) {
        ChatDetailFragment detailFragment = new ChatDetailFragment();

        Bundle args = new Bundle();
        args.putString("CHAT_NAME", chat.getName());
        args.putString("CHAT_ROLE", chat.getRole());
        detailFragment.setArguments(args);

        getParentFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, detailFragment)
                .addToBackStack(null)
                .commit();
    }
}