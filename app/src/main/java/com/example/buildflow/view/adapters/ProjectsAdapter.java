package com.example.buildflow.view.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.buildflow.R;
import com.example.buildflow.model.Project;

import java.util.List;

public class ProjectsAdapter extends RecyclerView.Adapter<ProjectsAdapter.ProjectViewHolder>{

    private List<Project> projects;
    private OnProjectClickListener listener;
    public interface OnProjectClickListener {
        void onProjectClick(Project project);
    }
    public ProjectsAdapter(List<Project> projects, OnProjectClickListener listener) {
        this.projects = projects;
        this.listener = listener;
    }
    public void updateData(List<Project> newProjects) {
        this.projects = newProjects;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ProjectViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_project, parent, false);
        return new ProjectViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProjectViewHolder holder, int position) {
        Project project = projects.get(position);

        holder.tvName.setText(project.getName());
        holder.tvType.setText(project.getType());
        holder.tvDate.setText("Started " + project.getStartDate());
        holder.tvMembers.setText(project.getMembersCount() + " members");
        holder.tvId.setText("ID: " + project.getId());
        holder.tvProgressPercent.setText(project.getProgress() + "%");
        holder.progressBar.setProgress(project.getProgress());
        holder.tvStatus.setText(project.getStatus());
        if (project.getDescription() != null && !project.getDescription().isEmpty()) {
            holder.tvDescription.setText(project.getDescription());
            holder.tvDescription.setVisibility(View.VISIBLE);
            holder.tvLabelDescription.setVisibility(View.VISIBLE);
        } else {
            holder.tvDescription.setVisibility(View.GONE);
            holder.tvLabelDescription.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onProjectClick(project);
            }
        });
    }

    @Override
    public int getItemCount() {
        return projects != null ? projects.size() : 0;
    }

    public static class ProjectViewHolder extends RecyclerView.ViewHolder {
        TextView tvName,tvId, tvType, tvDate, tvMembers, tvProgressPercent, tvStatus, tvDescription, tvLabelDescription;
        ProgressBar progressBar;

        public ProjectViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvProjectName);
            tvId = itemView.findViewById(R.id.tvProjectId);
            tvType = itemView.findViewById(R.id.tvProjectType);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvMembers = itemView.findViewById(R.id.tvMembers);
            tvProgressPercent = itemView.findViewById(R.id.tvProgressPercent);
            progressBar = itemView.findViewById(R.id.progressBar);
            tvStatus = itemView.findViewById(R.id.tvStatus);

            tvDescription = itemView.findViewById(R.id.tvProjectDescription);
            tvLabelDescription = itemView.findViewById(R.id.tvLabelDescription);
        }
    }
}