package com.example.myapplication.presentation.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.data.model.User;

import java.util.List;

public class UserSearchAdapter extends RecyclerView.Adapter<UserSearchAdapter.UserViewHolder> {
    private List<User> users;
    private OnAddFriendListener listener;

    public interface OnAddFriendListener {
        void onAddFriend(String userId);
    }

    public UserSearchAdapter(List<User> users, OnAddFriendListener listener) {
        this.users = users;
        this.listener = listener;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_user_search, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = users.get(position);
        holder.bind(user, listener);
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    public void updateUsers(List<User> newUsers) {
        this.users = newUsers;
        notifyDataSetChanged();
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        private ImageView avatarImageView;
        private TextView usernameTextView;
        private Button addFriendBtn;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            avatarImageView = itemView.findViewById(R.id.userAvatar);
            usernameTextView = itemView.findViewById(R.id.userUsername);
            addFriendBtn = itemView.findViewById(R.id.addFriendBtn);
        }

        public void bind(User user, OnAddFriendListener listener) {
            usernameTextView.setText(user.getUsername());

            int avatarResId = getAvatarResourceId(user.getAvatar());
            if (avatarResId != 0) {
                avatarImageView.setImageResource(avatarResId);
            }

            addFriendBtn.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onAddFriend(user.getId());
                }
            });
        }

        private int getAvatarResourceId(String avatarName) {
            if (avatarName == null) return R.drawable.person1;
            switch (avatarName) {
                case "person1": return R.drawable.person1;
                case "person2": return R.drawable.person2;
                case "person3": return R.drawable.person3;
                case "person4": return R.drawable.person4;
                case "person5": return R.drawable.person5;
                default: return R.drawable.person1;
            }
        }
    }
}
