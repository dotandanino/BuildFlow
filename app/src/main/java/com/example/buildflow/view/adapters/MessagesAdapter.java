package com.example.buildflow.view.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.buildflow.R;
import com.example.buildflow.model.ChatMessage;
import java.util.List;

public class MessagesAdapter extends RecyclerView.Adapter<MessagesAdapter.MessageViewHolder> {

    private List<ChatMessage> messagesList;
    private String currentUserId; // המזהה שלי - כדי לדעת אילו הודעות הן שלי

    // בנאי שמקבל גם את המזהה שלי
    public MessagesAdapter(List<ChatMessage> messagesList, String currentUserId) {
        this.messagesList = messagesList;
        this.currentUserId = currentUserId;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message, parent, false);
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        ChatMessage message = messagesList.get(position);

        // הבדיקה: האם המזהה של שולח ההודעה שווה למזהה שלי?
        // שימי לב: אני משתמש ב-getSenderId() מהמודל שלך
        boolean isSentByMe = message.getSenderId() != null && message.getSenderId().equals(currentUserId);

        if (isSentByMe) {
            // הודעה שלי -> כתום
            holder.layoutSent.setVisibility(View.VISIBLE);
            holder.layoutReceived.setVisibility(View.GONE);

            holder.tvSentMessage.setText(message.getContent()); // שימוש ב-getContent
            holder.tvSentTime.setText(message.getFormattedTime()); // שימוש בפונקציה שלך מהמודל
        } else {
            // הודעה של מישהו אחר -> לבן
            holder.layoutSent.setVisibility(View.GONE);
            holder.layoutReceived.setVisibility(View.VISIBLE);

            holder.tvReceivedMessage.setText(message.getContent());
            holder.tvReceivedTime.setText(message.getFormattedTime());
        }
    }

    @Override
    public int getItemCount() {
        return messagesList.size();
    }

    static class MessageViewHolder extends RecyclerView.ViewHolder {
        View layoutSent, layoutReceived;
        TextView tvSentMessage, tvSentTime, tvReceivedMessage, tvReceivedTime;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            layoutSent = itemView.findViewById(R.id.layoutSent);
            tvSentMessage = itemView.findViewById(R.id.tvSentMessage);
            tvSentTime = itemView.findViewById(R.id.tvSentTime);

            layoutReceived = itemView.findViewById(R.id.layoutReceived);
            tvReceivedMessage = itemView.findViewById(R.id.tvReceivedMessage);
            tvReceivedTime = itemView.findViewById(R.id.tvReceivedTime);
        }
    }
}