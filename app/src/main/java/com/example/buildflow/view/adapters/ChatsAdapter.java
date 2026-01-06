package com.example.buildflow.view.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.buildflow.R;
import com.example.buildflow.model.ChatConversation;
import java.util.List;

public class ChatsAdapter extends RecyclerView.Adapter<ChatsAdapter.ChatViewHolder> {

    private List<ChatConversation> chatList;
    private OnChatClickListener listener;

    public interface OnChatClickListener {
        void onChatClick(ChatConversation chat);
    }

    public ChatsAdapter(List<ChatConversation> chatList, OnChatClickListener listener) {
        this.chatList = chatList;
        this.listener = listener;
    }

    // --- פונקציה לסינון הרשימה (עבור מנוע החיפוש) ---
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

        // חיבור לנתונים לפי השמות ב-XML
        holder.tvName.setText(chat.getName());
        holder.tvRole.setText(chat.getRole());
        holder.tvLastMessage.setText(chat.getLastMessage());
        holder.tvTime.setText(chat.getTime());

        // ניהול תגית הודעות שלא נקראו
        if (chat.getUnreadCount() > 0) {
            holder.tvUnreadBadge.setVisibility(View.VISIBLE);
            holder.tvUnreadBadge.setText(String.valueOf(chat.getUnreadCount()));
        } else {
            holder.tvUnreadBadge.setVisibility(View.GONE);
        }

        // ניהול מחובר/לא מחובר
        if (chat.isOnline()) {
            holder.viewOnline.setVisibility(View.VISIBLE);
        } else {
            holder.viewOnline.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> listener.onChatClick(chat));
    }

    @Override
    public int getItemCount() {
        return chatList.size();
    }

    public static class ChatViewHolder extends RecyclerView.ViewHolder {
        // המשתנים בדיוק כמו ב-item_chat_row.xml
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