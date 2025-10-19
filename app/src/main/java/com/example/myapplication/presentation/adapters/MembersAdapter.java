package com.example.myapplication.presentation.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;

import java.util.List;

public class MembersAdapter extends RecyclerView.Adapter<MembersAdapter.MemberViewHolder> {
    private List<String> members;

    public MembersAdapter(List<String> members) {
        this.members = members;
    }

    @NonNull
    @Override
    public MemberViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_member, parent, false);
        return new MemberViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MemberViewHolder holder, int position) {
        String memberName = members.get(position);
        holder.bind(memberName);
    }

    @Override
    public int getItemCount() {
        return members.size();
    }

    public void updateMembers(List<String> newMembers) {
        this.members = newMembers;
        notifyDataSetChanged();
    }

    static class MemberViewHolder extends RecyclerView.ViewHolder {
        private TextView memberNameText;

        public MemberViewHolder(@NonNull View itemView) {
            super(itemView);
            memberNameText = itemView.findViewById(R.id.memberNameText);
        }

        public void bind(String memberName) {
            memberNameText.setText(memberName);
        }
    }
}
