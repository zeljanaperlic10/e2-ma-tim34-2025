package com.example.myapplication.presentation.adapters;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.data.model.ChatMessage;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.MessageViewHolder> {
    private List<ChatMessage> messages;
    private String currentUserId;
    private SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

    public ChatAdapter(List<ChatMessage> messages, String currentUserId) {
        this.messages = messages;
        this.currentUserId = currentUserId;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_chat_message, parent, false);
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        ChatMessage message = messages.get(position);
        holder.bind(message, currentUserId, timeFormat);
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    public void updateMessages(List<ChatMessage> newMessages) {
        this.messages = newMessages;
        notifyDataSetChanged();
    }

    static class MessageViewHolder extends RecyclerView.ViewHolder {
        private LinearLayout messageContainer;
        private TextView senderTextView;
        private TextView messageTextView;
        private TextView timeTextView;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            messageContainer = itemView.findViewById(R.id.messageContainer);
            senderTextView = itemView.findViewById(R.id.senderTextView);
            messageTextView = itemView.findViewById(R.id.messageTextView);
            timeTextView = itemView.findViewById(R.id.timeTextView);
        }

        public void bind(ChatMessage message, String currentUserId, SimpleDateFormat timeFormat) {
            senderTextView.setText(message.getSenderUsername());
            messageTextView.setText(message.getMessage());
            timeTextView.setText(timeFormat.format(new Date(message.getTimestamp())));

            // Postavi alignment - svoje poruke desno, tuđe levo
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) messageContainer.getLayoutParams();
            if (message.getSenderId().equals(currentUserId)) {
                params.gravity = Gravity.END;
                messageContainer.setBackgroundResource(R.drawable.bg_message_sent);
            } else {
                params.gravity = Gravity.START;
                messageContainer.setBackgroundResource(R.drawable.bg_message_received);
            }
            messageContainer.setLayoutParams(params);
        }
    }
}
