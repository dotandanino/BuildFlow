package com.example.buildflow.view.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.buildflow.R;
import com.example.buildflow.model.ProjectRequest;
import java.util.List;

public class MyRequestsAdapter extends RecyclerView.Adapter<MyRequestsAdapter.ViewHolder> {

    // 1. הוספת ממשק ללחיצה
    public interface OnItemClickListener {
        void onItemClick(ProjectRequest request);
    }

    private List<ProjectRequest> requestList;
    private final OnItemClickListener listener; // המשתנה של המאזין

    // 2. עדכון הבנאי לקבלת ה-Listener
    public MyRequestsAdapter(List<ProjectRequest> requestList, OnItemClickListener listener) {
        this.requestList = requestList;
        this.listener = listener;
    }

    public void updateList(List<ProjectRequest> newList) {
        this.requestList = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_personal_request, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ProjectRequest request = requestList.get(position);
        holder.tvTitle.setText(request.getTitle());
        holder.tvStatus.setText(request.getStatus());

        if (request.getPreferredDate() != null && !request.getPreferredDate().isEmpty()) {
            holder.tvDate.setText(request.getPreferredDate());
        } else {
            holder.tvDate.setText("No Date");
        }

        holder.tvExtraInfo.setText(request.getUrgency());

        if ("Closed".equals(request.getStatus())) {
            holder.tvStatus.setTextColor(Color.parseColor("#D32F2F"));
        } else {
            holder.tvStatus.setTextColor(Color.parseColor("#1976D2"));
        }

        // 3. הוספת אירוע לחיצה על כל השורה
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(request);
            }
        });
    }

    @Override
    public int getItemCount() {
        return requestList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvDate, tvStatus, tvExtraInfo;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvExtraInfo = itemView.findViewById(R.id.tvExtraInfo);
        }
    }
}