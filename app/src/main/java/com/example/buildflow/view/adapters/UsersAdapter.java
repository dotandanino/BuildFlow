package com.example.buildflow.view.adapters;

import android.util.Log; // הוספנו לוגים
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.buildflow.R;
import com.example.buildflow.model.UserModel;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.UserViewHolder> {

    private final List<UserModel> users;
    private final OnUserClickListener listener;

    public interface OnUserClickListener {
        void onUserClick(UserModel user);
    }

    public UsersAdapter(List<UserModel> users, OnUserClickListener listener) {
        this.users = users;
        this.listener = listener;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_row, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        UserModel user = users.get(position);

        holder.tvName.setText(user.getName());
        holder.tvRole.setText(user.getRole());

        // הסתרת אלמנטים לא רלוונטיים
        holder.tvTime.setVisibility(View.GONE);
        holder.tvLastMessage.setVisibility(View.GONE);
        holder.tvUnreadBadge.setVisibility(View.GONE);
        holder.viewOnline.setVisibility(View.GONE); // איפוס התחלתי

        // בדיקת סטטוס
        String path = "status/" + user.getUid();
        DatabaseReference statusRef = FirebaseDatabase.getInstance().getReference(path);

        // --- הדפסה לבדיקה: האם אנחנו מאזינים לנתיב הנכון? ---
        Log.d("DEBUG_STATUS", "Listening to: " + path);

        statusRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String status = snapshot.getValue(String.class);

                // --- הדפסה לבדיקה: מה קיבלנו מהשרת? ---
                Log.d("DEBUG_STATUS", "User: " + user.getName() + " | Status from DB: " + status);

                if ("online".equals(status)) {
                    holder.viewOnline.setVisibility(View.VISIBLE);
                    Log.d("DEBUG_STATUS", ">>> SHOWING GREEN DOT for " + user.getName());
                } else {
                    holder.viewOnline.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("DEBUG_STATUS", "Error: " + error.getMessage());
            }
        });

        holder.itemView.setOnClickListener(v -> listener.onUserClick(user));
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvRole, tvTime, tvLastMessage, tvUnreadBadge;
        View viewOnline;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvRole = itemView.findViewById(R.id.tvRole);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvLastMessage = itemView.findViewById(R.id.tvLastMessage);
            tvUnreadBadge = itemView.findViewById(R.id.tvUnreadBadge);
            viewOnline = itemView.findViewById(R.id.viewOnline);
        }
    }
}