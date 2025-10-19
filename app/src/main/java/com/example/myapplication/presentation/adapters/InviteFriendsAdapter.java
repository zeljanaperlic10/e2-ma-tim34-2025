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
import com.example.myapplication.data.model.Friend;

import java.util.List;

public class InviteFriendsAdapter extends RecyclerView.Adapter<InviteFriendsAdapter.InviteViewHolder> {
    private List<Friend> friends;
    private OnInviteClickListener listener;

    public interface OnInviteClickListener {
        void onInvite(Friend friend);
    }

    public InviteFriendsAdapter(List<Friend> friends, OnInviteClickListener listener) {
        this.friends = friends;
        this.listener = listener;
    }

    @NonNull
    @Override
    public InviteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_invite_friend, parent, false);
        return new InviteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull InviteViewHolder holder, int position) {
        Friend friend = friends.get(position);
        holder.bind(friend, listener);
    }

    @Override
    public int getItemCount() {
        return friends.size();
    }

    static class InviteViewHolder extends RecyclerView.ViewHolder {
        private ImageView avatarImageView;
        private TextView usernameTextView;
        private Button inviteBtn;

        public InviteViewHolder(@NonNull View itemView) {
            super(itemView);
            avatarImageView = itemView.findViewById(R.id.inviteFriendAvatar);
            usernameTextView = itemView.findViewById(R.id.inviteFriendUsername);
            inviteBtn = itemView.findViewById(R.id.inviteBtn);
        }

        public void bind(Friend friend, OnInviteClickListener listener) {
            usernameTextView.setText(friend.getUsername());

            int avatarResId = getAvatarResourceId(friend.getAvatar());
            if (avatarResId != 0) {
                avatarImageView.setImageResource(avatarResId);
            }

            inviteBtn.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onInvite(friend);
                    inviteBtn.setEnabled(false);
                    inviteBtn.setText("Poslato");
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
