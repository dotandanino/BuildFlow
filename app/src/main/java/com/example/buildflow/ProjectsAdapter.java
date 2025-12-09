package com.example.buildflow;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ProjectsAdapter extends RecyclerView.Adapter<ProjectsAdapter.ProjectViewHolder>{

    private List<Project> projects;

    public ProjectsAdapter(List<Project> projects) {
        this.projects = projects;
    }

    // Function to update the list when data changes in Firebase
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

        holder.tvProgressPercent.setText(project.getProgress() + "%");
        holder.progressBar.setProgress(project.getProgress());

        holder.tvStatus.setText(project.getStatus());
    }

    @Override
    public int getItemCount() {
        return projects != null ? projects.size() : 0;
    }

    // ViewHolder Class
    public static class ProjectViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvType, tvDate, tvMembers, tvProgressPercent, tvStatus;
        ProgressBar progressBar;

        public ProjectViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvProjectName);
            tvType = itemView.findViewById(R.id.tvProjectType);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvMembers = itemView.findViewById(R.id.tvMembers);
            tvProgressPercent = itemView.findViewById(R.id.tvProgressPercent);
            progressBar = itemView.findViewById(R.id.progressBar);
            tvStatus = itemView.findViewById(R.id.tvStatus);
        }
    }
}
