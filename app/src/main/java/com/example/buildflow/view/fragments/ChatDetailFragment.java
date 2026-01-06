package com.example.buildflow.view.fragments;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.buildflow.R;
import com.example.buildflow.model.ChatMessage;
import com.example.buildflow.view.adapters.MessagesAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ChatDetailFragment extends Fragment {

    private EditText etMessage;
    private ImageButton btnSend, btnBack;
    private TextView tvHeaderName, tvHeaderRole;
    private RecyclerView rvMessages;

    private MessagesAdapter adapter;
    private List<ChatMessage> messageList;

    // משתנים אמיתיים לניהול השיחה
    private String currentUserId;
    private String currentUserName = "Me"; // בהמשך תוכלי לשלוף את השם האמיתי מה-User Profile
    private String receiverId;
    private String receiverName;
    private String projectId;

    public ChatDetailFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_chat_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // --- 1. זיהוי המשתמש האמיתי (FirebaseAuth) ---
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(getContext(), "Error: User not logged in", Toast.LENGTH_SHORT).show();
            getParentFragmentManager().popBackStack(); // יציאה אם אין משתמש
            return;
        }
        currentUserId = currentUser.getUid();
        //currentUserName = currentUser.getDisplayName();// אופציונלי

        // --- 2. קבלת פרטי הצ'אט מהמסך הקודם ---
        if (getArguments() != null) {
            receiverName = getArguments().getString("CHAT_NAME");
            String role = getArguments().getString("CHAT_ROLE");

            // חשוב: אנחנו חייבים לקבל את ה-ID של מי שאנחנו מדברים איתו ושל הפרויקט
            receiverId = getArguments().getString("RECEIVER_ID");
            projectId = getArguments().getString("PROJECT_ID");

            // וודאי שאנחנו לא קורסים אם חסר מידע (למשל בזמן פיתוח)
            if (projectId == null) projectId = "default_project";
            if (receiverId == null) receiverId = "unknown_receiver";

            // קישור לממשק
            tvHeaderName = view.findViewById(R.id.tvHeaderName);
            tvHeaderRole = view.findViewById(R.id.tvHeaderRole);
            tvHeaderName.setText(receiverName);
            tvHeaderRole.setText(role);
        }

        // --- 3. אתחול הרכיבים ---
        btnBack = view.findViewById(R.id.btnBack);
        rvMessages = view.findViewById(R.id.rvMessages);
        etMessage = view.findViewById(R.id.etMessage);
        btnSend = view.findViewById(R.id.btnSend);

        // --- 4. אתחול ה-Adapter עם ה-ID האמיתי ---
        messageList = new ArrayList<>();
        // כאן בעתיד תהיה שליפה מ-Firebase (db.collection...). בינתיים הרשימה ריקה.

        adapter = new MessagesAdapter(messageList, currentUserId); // מעבירים את ה-ID האמיתי שלי
        rvMessages.setLayoutManager(new LinearLayoutManager(getContext()));
        rvMessages.setAdapter(adapter);

        // --- 5. לוגיקת השליחה ---
        btnSend.setOnClickListener(v -> {
            String text = etMessage.getText().toString().trim();
            if (!TextUtils.isEmpty(text)) {
                sendMessage(text);
            }
        });

        btnBack.setOnClickListener(v -> getParentFragmentManager().popBackStack());
    }
    @Override
    public void onResume() {
        super.onResume();
        // כשהמסך עולה - נסתיר את התפריט התחתון
        if (getActivity() != null) {
            View bottomNav = getActivity().findViewById(R.id.bottom_navigation); // וודאי שזה ה-ID אצלך
            if (bottomNav != null) {
                bottomNav.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void onPause() { // אפשר גם ב-onDestroyView
        super.onPause();
        // כשהמסך נסגר - נחזיר את התפריט התחתון
        if (getActivity() != null) {
            View bottomNav = getActivity().findViewById(R.id.bottom_navigation);
            if (bottomNav != null) {
                bottomNav.setVisibility(View.VISIBLE);
            }
        }
    }

    private void sendMessage(String text) {
        long timestamp = System.currentTimeMillis();
        String messageId = UUID.randomUUID().toString(); // ID ייחודי להודעה

        // יצירת ההודעה עם הנתונים האמיתיים
        ChatMessage newMessage = new ChatMessage(
                messageId,
                projectId,       // ה-ID של הפרויקט שהועבר
                currentUserId,   // אני השולח (מה-Auth)
                receiverId,      // הוא המקבל (מה-Arguments)
                currentUserName, // השם שלי
                text,
                timestamp
        );

        // הוספה ל-UI
        messageList.add(newMessage);
        adapter.notifyItemInserted(messageList.size() - 1);
        rvMessages.smoothScrollToPosition(messageList.size() - 1);

        // כאן תוסיפי את השמירה ל-Firebase Database בהמשך:
        // DatabaseReference ref = FirebaseDatabase.getInstance().getReference()...
        // ref.child("messages").child(messageId).setValue(newMessage);

        etMessage.setText("");
    }

}