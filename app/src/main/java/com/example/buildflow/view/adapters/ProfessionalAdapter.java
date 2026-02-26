package com.example.buildflow.view.adapters;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.buildflow.R;
import com.example.buildflow.model.Professional;

import java.util.ArrayList;
import java.util.List;

public class ProfessionalAdapter extends RecyclerView.Adapter<ProfessionalAdapter.ProViewHolder> {

    private List<Professional> proList = new ArrayList<>();

    public void setProfessionals(List<Professional> newList) {
        this.proList = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ProViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_professional, parent, false);
        return new ProViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProViewHolder holder, int position) {
        Professional pro = proList.get(position);

        holder.tvName.setText(pro.getName() != null ? pro.getName() : "Unknown");
        holder.tvRole.setText(pro.getProfession());
        holder.tvRating.setText("⭐ " + pro.getRating() + " (" + pro.getReviewsCount() + ")");
        holder.tvJobs.setText(pro.getTotalJobs() + " jobs");
        holder.tvDesc.setText(pro.getDescription());
        holder.tvRate.setText("$" + pro.getHourlyRate() + "/hr");

        // --- טיפול בערים ומרחק ---
        String cities = "Anywhere";
        if (pro.getServiceCities() != null && !pro.getServiceCities().isEmpty()) {
            cities = String.join(", ", pro.getServiceCities());
        }

        // אם חישבנו מרחק גדול מ-0, נציג גם אותו
        if (pro.getDistanceFromUser() > 0) {
            String distanceStr = String.format("%.1f km", pro.getDistanceFromUser());
            holder.tvLocation.setText("📍 " + cities + " (" + distanceStr + ")");
        } else {
            holder.tvLocation.setText("📍 " + cities);
        }

        // טעינת תמונת פרופיל
        if (pro.getAvatarUrl() != null && !pro.getAvatarUrl().isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(pro.getAvatarUrl())
                    .circleCrop()
                    .into(holder.ivAvatar);
        } else {
            holder.ivAvatar.setImageResource(R.drawable.bg_icon_circle); // תמונת ברירת מחדל
        }

        // --- טיפול בלחיצה על כפתור Contact (הקפצת פופ-אפ) ---
        holder.btnContact.setOnClickListener(v -> {
            View dialogView = LayoutInflater.from(v.getContext()).inflate(R.layout.dialog_contact_pro, null);
            AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
            builder.setView(dialogView);
            AlertDialog dialog = builder.create();

            TextView tvDialogName = dialogView.findViewById(R.id.tvDialogProName);
            TextView tvDialogRole = dialogView.findViewById(R.id.tvDialogProRole);
            Button btnDialogCall = dialogView.findViewById(R.id.btnDialogCall);
            Button btnDialogEmail = dialogView.findViewById(R.id.btnDialogEmail);
            TextView tvDialogClose = dialogView.findViewById(R.id.tvDialogClose);

            tvDialogName.setText(pro.getName() != null ? pro.getName() : "Unknown Pro");
            tvDialogRole.setText(pro.getProfession());

            // חיוג
            String phone = pro.getPhoneNumber();
            if (phone != null && !phone.isEmpty()) {
                btnDialogCall.setText("Call " + phone);
                btnDialogCall.setOnClickListener(callView -> {
                    Intent intent = new Intent(Intent.ACTION_DIAL);
                    intent.setData(Uri.parse("tel:" + phone));
                    v.getContext().startActivity(intent);
                });
            } else {
                btnDialogCall.setVisibility(View.GONE);
            }

            // מייל
            String email = pro.getEmail();
            if (email != null && !email.isEmpty()) {
                btnDialogEmail.setText("Email: " + email);
                btnDialogEmail.setOnClickListener(emailView -> {
                    Intent intent = new Intent(Intent.ACTION_SENDTO);
                    intent.setData(Uri.parse("mailto:" + email));
                    v.getContext().startActivity(intent);
                });
            } else {
                btnDialogEmail.setVisibility(View.GONE);
            }

            tvDialogClose.setOnClickListener(closeView -> dialog.dismiss());

            if (dialog.getWindow() != null) {
                dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            }

            dialog.show();
        });
    }

    @Override
    public int getItemCount() { return proList.size(); }

    static class ProViewHolder extends RecyclerView.ViewHolder {
        ImageView ivAvatar;
        TextView tvName, tvRole, tvRating, tvJobs, tvDesc, tvLocation, tvRate;
        Button btnContact;

        public ProViewHolder(@NonNull View itemView) {
            super(itemView);
            ivAvatar = itemView.findViewById(R.id.ivProAvatar);
            tvName = itemView.findViewById(R.id.tvProName);
            tvRole = itemView.findViewById(R.id.tvProRole);
            tvRating = itemView.findViewById(R.id.tvProRating);
            tvJobs = itemView.findViewById(R.id.tvProJobs);
            tvDesc = itemView.findViewById(R.id.tvProDesc);
            tvLocation = itemView.findViewById(R.id.tvProLocation);
            tvRate = itemView.findViewById(R.id.tvProRate);

            // חיבור כפתור ה-Contact (וודא שהוספת את ה-ID ל-XML!)
            btnContact = itemView.findViewById(R.id.btnContact);
        }
    }
}