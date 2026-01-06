package com.example.buildflow.view.fragments;

import android.graphics.Color; // הוספנו לצבעים
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.buildflow.R;
import com.example.buildflow.model.ChatMessage;
import com.example.buildflow.view.adapters.MessagesAdapter;
import com.example.buildflow.viewmodel.ChatDetailViewModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot; // הוספנו ל-Realtime DB
import com.google.firebase.database.DatabaseError; // הוספנו
import com.google.firebase.database.DatabaseReference; // הוספנו
import com.google.firebase.database.FirebaseDatabase; // הוספנו
import com.google.firebase.database.ValueEventListener; // הוספנו

import java.util.ArrayList;
import java.util.UUID;

public class ChatDetailFragment extends Fragment {

    private EditText etMessage;
    private ImageButton btnSend, btnBack;
    private TextView tvHeaderName, tvHeaderRole;
    private RecyclerView rvMessages;

    private MessagesAdapter adapter;
    private ChatDetailViewModel viewModel;

    private String currentUserId;
    private String currentUserName = "Me";
    private String receiverId;
    private String receiverName;
    private String projectId;
    private String chatId;

    // --- התיקון: הוספת המשתנה החסר ---
    private ValueEventListener presenceListener;

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

        // 1. אתחול ViewModel
        viewModel = new ViewModelProvider(this).get(ChatDetailViewModel.class);

        // 2. זיהוי משתמש
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            getParentFragmentManager().popBackStack();
            return;
        }
        currentUserId = currentUser.getUid();
        if (currentUser.getDisplayName() != null && !currentUser.getDisplayName().isEmpty()) {
            currentUserName = currentUser.getDisplayName();
        }

        // 3. אתחול רכיבי UI (העברתי את זה למעלה כדי שנוכל להשתמש ב-tvHeaderRole מיד)
        tvHeaderName = view.findViewById(R.id.tvHeaderName);
        tvHeaderRole = view.findViewById(R.id.tvHeaderRole);
        btnBack = view.findViewById(R.id.btnBack);
        rvMessages = view.findViewById(R.id.rvMessages);
        etMessage = view.findViewById(R.id.etMessage);
        btnSend = view.findViewById(R.id.btnSend);

        // 4. קבלת פרטים מהארגומנטים
        String originalRole = "Professional"; // ברירת מחדל
        if (getArguments() != null) {
            receiverName = getArguments().getString("CHAT_NAME");
            originalRole = getArguments().getString("CHAT_ROLE");
            receiverId = getArguments().getString("RECEIVER_ID");
            projectId = getArguments().getString("PROJECT_ID");

            if (projectId == null) projectId = "default_project";
            if (receiverId == null) receiverId = "unknown";

            tvHeaderName.setText(receiverName);
            tvHeaderRole.setText(originalRole);

            // יצירת מזהה השיחה
            chatId = viewModel.getChatId(currentUserId, receiverId);
        }

        // 5. אדפטר
        adapter = new MessagesAdapter(new ArrayList<>(), currentUserId);
        rvMessages.setLayoutManager(new LinearLayoutManager(getContext()));
        rvMessages.setAdapter(adapter);

        // 6. --- התיקון: האזנה לסטטוס (Realtime Database) ---
        if (receiverId != null && !receiverId.equals("unknown")) {
            DatabaseReference userStatusRef = FirebaseDatabase.getInstance().getReference("status/" + receiverId);

            String finalOriginalRole = originalRole; // לשמור את התפקיד המקורי למקרה שהוא לא מחובר
            presenceListener = userStatusRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    // כאן אנחנו מקבלים את הסטטוס
                    String status = snapshot.getValue(String.class);

                    if ("online".equals(status)) {
                        tvHeaderRole.setText("Online");
                        tvHeaderRole.setTextColor(Color.parseColor("#4CAF50")); // ירוק
                    } else {
                        // אם הוא לא מחובר - נחזיר את התפקיד המקורי (למשל "Plumber")
                        tvHeaderRole.setText(finalOriginalRole);
                        tvHeaderRole.setTextColor(Color.parseColor("#6B7280")); // אפור
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                }
            });
        }

        // 7. MVVM: האזנה להודעות
        if (chatId != null) {
            viewModel.startListening(projectId, chatId);

            viewModel.getMessages().observe(getViewLifecycleOwner(), messages -> {
                adapter = new MessagesAdapter(messages, currentUserId);
                rvMessages.setAdapter(adapter);
                if (!messages.isEmpty()) {
                    rvMessages.smoothScrollToPosition(messages.size() - 1);
                }
            });
        }

        // 8. שליחה
        String finalOriginalRole1 = originalRole;
        btnSend.setOnClickListener(v -> {
            String text = etMessage.getText().toString().trim();
            if (!TextUtils.isEmpty(text)) {
                sendMessage(text, finalOriginalRole1);
            }
        });

        btnBack.setOnClickListener(v -> getParentFragmentManager().popBackStack());
    }

    private void sendMessage(String text, String receiverRole) {
        long timestamp = System.currentTimeMillis();
        String messageId = UUID.randomUUID().toString();

        ChatMessage newMessage = new ChatMessage(
                messageId,
                projectId,
                currentUserId,
                receiverId,
                currentUserName,
                text,
                timestamp
        );

        // שימוש בתפקיד האמיתי שהועבר
        viewModel.sendMessage(projectId, chatId, newMessage, receiverName, receiverRole);

        etMessage.setText("");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // ניקוי ההאזנה לסטטוס כדי לא לבזבז משאבים
        if (presenceListener != null && receiverId != null) {
            FirebaseDatabase.getInstance().getReference("status/" + receiverId)
                    .removeEventListener(presenceListener);
        }
    }
}