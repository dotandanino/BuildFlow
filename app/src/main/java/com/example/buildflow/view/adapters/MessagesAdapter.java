package com.example.buildflow.view.adapters;

import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.buildflow.R;
import com.example.buildflow.model.ChatMessage;

import java.util.List;

public class MessagesAdapter extends RecyclerView.Adapter<MessagesAdapter.MessageViewHolder> {

    private final List<ChatMessage> messages;
    private final String currentUserId;

    public MessagesAdapter(List<ChatMessage> messages, String currentUserId) {
        this.messages = messages;
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
        ChatMessage message = messages.get(position);

        // ניקוי לחיצות קודמות כדי למנוע באגים של מיחזור (Recycling)
        holder.layoutSent.setOnClickListener(null);
        holder.layoutReceived.setOnClickListener(null);

        // בדיקה: האם אני שלחתי את ההודעה?
        if (message.getSenderId().equals(currentUserId)) {
            // --- אני השולח (צד ימין - כחול) ---
            holder.layoutSent.setVisibility(View.VISIBLE);
            holder.layoutReceived.setVisibility(View.GONE);
            holder.tvTimeSent.setText(message.getFormattedTime());

            if ("image".equals(message.getMessageType()) && message.getMediaUrl() != null) {
                // --- מצב תמונה ---
                holder.tvMessageSent.setVisibility(View.GONE);
                holder.ivMessageImageSent.setVisibility(View.VISIBLE);

                Glide.with(holder.itemView.getContext())
                        .load(message.getMediaUrl())
                        .centerCrop()
                        .into(holder.ivMessageImageSent);

            } else if ("file".equals(message.getMessageType()) && message.getMediaUrl() != null) {
                // --- מצב קובץ! ---
                holder.ivMessageImageSent.setVisibility(View.GONE);
                holder.tvMessageSent.setVisibility(View.VISIBLE);
                String fName = message.getFileName() != null ? message.getFileName() : "File";
                String icon = getFileIcon(fName);
                holder.tvMessageSent.setText(icon + " " + fName + "\n(לחץ לפתיחה)");
                // הגדרת לחיצה על הבועה שתפתח את הקובץ
                holder.layoutSent.setOnClickListener(v -> {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(message.getMediaUrl()));
                    v.getContext().startActivity(intent);
                });

            } else {
                // --- מצב טקסט רגיל ---
                holder.ivMessageImageSent.setVisibility(View.GONE);
                holder.tvMessageSent.setVisibility(View.VISIBLE);
                holder.tvMessageSent.setText(message.getContent());
            }

        } else {
            // --- הצד השני שלח (צד שמאל - אפור) ---
            holder.layoutSent.setVisibility(View.GONE);
            holder.layoutReceived.setVisibility(View.VISIBLE);
            holder.tvTimeReceived.setText(message.getFormattedTime());

            if ("image".equals(message.getMessageType()) && message.getMediaUrl() != null) {
                // --- מצב תמונה ---
                holder.tvMessageReceived.setVisibility(View.GONE);
                holder.ivMessageImageReceived.setVisibility(View.VISIBLE);

                Glide.with(holder.itemView.getContext())
                        .load(message.getMediaUrl())
                        .centerCrop()
                        .into(holder.ivMessageImageReceived);

            } else if ("file".equals(message.getMessageType()) && message.getMediaUrl() != null) {
                // --- מצב קובץ! ---
                holder.ivMessageImageReceived.setVisibility(View.GONE);
                holder.tvMessageReceived.setVisibility(View.VISIBLE);
                holder.tvMessageReceived.setText("📎 לחץ כאן לפתיחת הקובץ");

                // הגדרת לחיצה על הבועה שתפתח את הקובץ
                holder.layoutReceived.setOnClickListener(v -> {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(message.getMediaUrl()));
                    v.getContext().startActivity(intent);
                });

            } else {
                // --- מצב טקסט רגיל ---
                holder.ivMessageImageReceived.setVisibility(View.GONE);
                holder.tvMessageReceived.setVisibility(View.VISIBLE);
                holder.tvMessageReceived.setText(message.getContent());
            }
        }
    }

    // פונקציה חכמה שמתאימה אייקון לפי סיומת הקובץ
    private String getFileIcon(String fileName) {
        if (fileName == null) return "📄";
        String lowerName = fileName.toLowerCase();
        if (lowerName.endsWith(".pdf")) return "📕"; // PDF
        if (lowerName.endsWith(".doc") || lowerName.endsWith(".docx")) return "📘"; // Word
        if (lowerName.endsWith(".xls") || lowerName.endsWith(".xlsx")) return "📗"; // Excel
        if (lowerName.endsWith(".ppt") || lowerName.endsWith(".pptx")) return "📙"; // PowerPoint
        if (lowerName.endsWith(".zip") || lowerName.endsWith(".rar")) return "📦"; // ZIP
        if (lowerName.endsWith(".txt")) return "📝"; // Text
        return "📄"; // ברירת מחדל
    }

    @Override
    public int getItemCount() {
        return messages != null ? messages.size() : 0;
    }

    static class MessageViewHolder extends RecyclerView.ViewHolder {
        LinearLayout layoutSent;
        TextView tvMessageSent, tvTimeSent;
        ImageView ivMessageImageSent;

        LinearLayout layoutReceived;
        TextView tvMessageReceived, tvTimeReceived;
        ImageView ivMessageImageReceived;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);

            layoutSent = itemView.findViewById(R.id.layoutSent);
            tvMessageSent = itemView.findViewById(R.id.tvMessageSent);
            tvTimeSent = itemView.findViewById(R.id.tvTimeSent);
            ivMessageImageSent = itemView.findViewById(R.id.ivMessageImageSent);

            layoutReceived = itemView.findViewById(R.id.layoutReceived);
            tvMessageReceived = itemView.findViewById(R.id.tvMessageReceived);
            tvTimeReceived = itemView.findViewById(R.id.tvTimeReceived);
            ivMessageImageReceived = itemView.findViewById(R.id.ivMessageImageReceived);
        }
    }
}