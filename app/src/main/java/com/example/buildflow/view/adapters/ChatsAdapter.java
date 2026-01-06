package com.example.buildflow.view.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.buildflow.R;
import com.example.buildflow.model.ChatConversation;
// --- ייבואים חדשים שחייבים להוסיף בשביל הסטטוס ---
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class ChatsAdapter extends RecyclerView.Adapter<ChatsAdapter.ChatViewHolder> {

    private List<ChatConversation> chatList;
    private final OnChatClickListener listener;

    // 1. שינוי ראשון: משתנה לשמירת ה-ID שלי
    private final String currentUserId;

    public interface OnChatClickListener {
        void onChatClick(ChatConversation chat);
    }

    // 2. שינוי שני: הוספנו את currentUserId לבנאי
    public ChatsAdapter(List<ChatConversation> chatList, String currentUserId, OnChatClickListener listener) {
        this.chatList = chatList;
        this.currentUserId = currentUserId; // שומרים אותו
        this.listener = listener;
    }

    public void updateList(List<ChatConversation> newList) {
        this.chatList = newList;
        notifyDataSetChanged();
    }

    public void filterList(List<ChatConversation> filteredList) {
        this.chatList = filteredList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_row, parent, false);
        return new ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        ChatConversation chat = chatList.get(position);

        holder.tvName.setText(chat.getName());
        holder.tvRole.setText(chat.getRole());
        holder.tvLastMessage.setText(chat.getLastMessage());
        holder.tvTime.setText(chat.getTime());

        if (chat.getUnreadCount() > 0) {
            holder.tvUnreadBadge.setVisibility(View.VISIBLE);
            holder.tvUnreadBadge.setText(String.valueOf(chat.getUnreadCount()));
        } else {
            holder.tvUnreadBadge.setVisibility(View.GONE);
        }

        // 3. שינוי שלישי: בדיקת סטטוס חכמה בזמן אמת

        // קודם כל מאפסים (כי ב-RecyclerView שורות ממוחזרות)
        holder.viewOnline.setVisibility(View.GONE);

        // מנסים להבין מי הצד השני בשיחה
        String otherUserId = null;

        // ה-ID של הצ'אט בנוי בצורה: user1_user2
        if (chat.getId() != null && chat.getId().contains("_")) {
            String[] parts = chat.getId().split("_");
            if (parts.length == 2) {
                // אם החלק הראשון הוא אני -> השני הוא החבר
                if (parts[0].equals(currentUserId)) {
                    otherUserId = parts[1];
                } else {
                    // אחרת -> הראשון הוא החבר
                    otherUserId = parts[0];
                }
            }
        }

        // אם מצאנו את ה-ID של החבר, בודקים אם הוא מחובר
        if (otherUserId != null) {
            DatabaseReference statusRef = FirebaseDatabase.getInstance().getReference("status/" + otherUserId);
            statusRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    String status = snapshot.getValue(String.class);
                    if ("online".equals(status)) {
                        holder.viewOnline.setVisibility(View.VISIBLE); // מציגים ירוק
                    } else {
                        holder.viewOnline.setVisibility(View.GONE); // מסתירים
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                }
            });
        }

        holder.itemView.setOnClickListener(v -> listener.onChatClick(chat));
    }

    @Override
    public int getItemCount() {
        return chatList != null ? chatList.size() : 0;
    }

    public static class ChatViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvRole, tvLastMessage, tvTime, tvUnreadBadge;
        View viewOnline;

        public ChatViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvRole = itemView.findViewById(R.id.tvRole);
            tvLastMessage = itemView.findViewById(R.id.tvLastMessage);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvUnreadBadge = itemView.findViewById(R.id.tvUnreadBadge);
            viewOnline = itemView.findViewById(R.id.viewOnline);
        }
    }
}