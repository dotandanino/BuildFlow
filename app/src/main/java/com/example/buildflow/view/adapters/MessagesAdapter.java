package com.example.buildflow.view.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.buildflow.R;
import com.example.buildflow.model.ChatMessage;

import java.util.List;

public class MessagesAdapter extends RecyclerView.Adapter<MessagesAdapter.MessageViewHolder> {

    private final List<ChatMessage> messages;
    private final String currentUserId; // ה-ID שלי, כדי לדעת איזה צד לבחור

    public MessagesAdapter(List<ChatMessage> messages, String currentUserId) {
        this.messages = messages;
        this.currentUserId = currentUserId;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // משתמשים ב-XML החדש שיצרנו למעלה
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message, parent, false);
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        ChatMessage message = messages.get(position);

        // בדיקה: האם אני שלחתי את ההודעה?
        if (message.getSenderId().equals(currentUserId)) {
            // אם כן -> תציג את העיצוב הכחול (Sent), תסתיר את האפור (Received)
            holder.layoutSent.setVisibility(View.VISIBLE);
            holder.layoutReceived.setVisibility(View.GONE);

            holder.tvMessageSent.setText(message.getContent());
            holder.tvTimeSent.setText(message.getFormattedTime());
        } else {
            // אחרת -> תציג את האפור, תסתיר את הכחול
            holder.layoutSent.setVisibility(View.GONE);
            holder.layoutReceived.setVisibility(View.VISIBLE);

            holder.tvMessageReceived.setText(message.getContent());
            holder.tvTimeReceived.setText(message.getFormattedTime());
        }
    }

    @Override
    public int getItemCount() {
        return messages != null ? messages.size() : 0;
    }

    static class MessageViewHolder extends RecyclerView.ViewHolder {
        // רכיבים להודעה יוצאת
        LinearLayout layoutSent;
        TextView tvMessageSent, tvTimeSent;

        // רכיבים להודעה נכנסת
        LinearLayout layoutReceived;
        TextView tvMessageReceived, tvTimeReceived;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);

            // קישור ה-ID מהקובץ item_message.xml
            layoutSent = itemView.findViewById(R.id.layoutSent);
            tvMessageSent = itemView.findViewById(R.id.tvMessageSent);
            tvTimeSent = itemView.findViewById(R.id.tvTimeSent);

            layoutReceived = itemView.findViewById(R.id.layoutReceived);
            tvMessageReceived = itemView.findViewById(R.id.tvMessageReceived);
            tvTimeReceived = itemView.findViewById(R.id.tvTimeReceived);
        }
    }
}