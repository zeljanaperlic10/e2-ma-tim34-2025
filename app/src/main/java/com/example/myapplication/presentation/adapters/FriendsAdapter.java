package com.example.myapplication.presentation.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.data.model.Friend;

import java.util.List;

public class FriendsAdapter extends RecyclerView.Adapter<FriendsAdapter.FriendViewHolder> {
    private List<Friend> friends;
    private OnFriendClickListener listener;

    public interface OnFriendClickListener {
        void onFriendClick(Friend friend);
    }

    public FriendsAdapter(List<Friend> friends, OnFriendClickListener listener) {
        this.friends = friends;
        this.listener = listener;
    }

    @NonNull
    @Override
    public FriendViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_friend, parent, false);
        return new FriendViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FriendViewHolder holder, int position) {
        Friend friend = friends.get(position);
        holder.bind(friend, listener);
    }

    @Override
    public int getItemCount() {
        return friends.size();
    }

    public void updateFriends(List<Friend> newFriends) {
        this.friends = newFriends;
        notifyDataSetChanged();
    }

    static class FriendViewHolder extends RecyclerView.ViewHolder {
        private ImageView avatarImageView;
        private TextView usernameTextView;

        public FriendViewHolder(@NonNull View itemView) {
            super(itemView);
            avatarImageView = itemView.findViewById(R.id.friendAvatar);
            usernameTextView = itemView.findViewById(R.id.friendUsername);
        }

        public void bind(Friend friend, OnFriendClickListener listener) {
            usernameTextView.setText(friend.getUsername());

            // Postavi avatar
            int avatarResId = getAvatarResourceId(friend.getAvatar());
            if (avatarResId != 0) {
                avatarImageView.setImageResource(avatarResId);
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onFriendClick(friend);
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
