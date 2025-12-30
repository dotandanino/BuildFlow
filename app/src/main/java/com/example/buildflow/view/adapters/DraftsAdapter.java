package com.example.buildflow.view.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.buildflow.R;
import com.example.buildflow.model.ProjectRequest;
import java.util.List;

public class DraftsAdapter extends RecyclerView.Adapter<DraftsAdapter.ViewHolder> {

    private List<ProjectRequest> draftsList;
    private final OnDraftClickListener listener;

    public interface OnDraftClickListener {
        void onDraftClick(ProjectRequest draft);
        void onDeleteClick(ProjectRequest draft, int position);
    }

    public DraftsAdapter(List<ProjectRequest> draftsList, OnDraftClickListener listener) {
        this.draftsList = draftsList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_personal_request, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ProjectRequest draft = draftsList.get(position);

        if (holder.tvTitle != null) {
            String title = draft.getTitle();
            holder.tvTitle.setText((title != null && !title.isEmpty()) ? title : "Untitled Draft");
        }

        if (holder.tvStatus != null) holder.tvStatus.setText("Draft");
        if (holder.tvDate != null) holder.tvDate.setText(draft.getPreferredDate());

        // --- הסרנו את החלק של tvDescription כי ביקשת למחוק אותו ---

        // חשיפת כפתור המחיקה בטיוטות
        if (holder.btnDelete != null) {
            holder.btnDelete.setVisibility(View.VISIBLE);
            holder.btnDelete.setOnClickListener(v -> listener.onDeleteClick(draft, position));
        }

        holder.itemView.setOnClickListener(v -> listener.onDraftClick(draft));
    }

    @Override
    public int getItemCount() {
        return draftsList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvStatus, tvDate; // מחקנו את tvDescription
        ImageButton btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvDate = itemView.findViewById(R.id.tvDate);
            // מחקנו את החיפוש של tvDescription
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}