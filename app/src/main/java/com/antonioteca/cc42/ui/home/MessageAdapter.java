package com.antonioteca.cc42.ui.home;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.antonioteca.cc42.R;
import com.antonioteca.cc42.model.Message;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    private List<Message> messageList = new ArrayList<>();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

    public void setMessages(List<Message> messages) {
        this.messageList = messages;
        notifyDataSetChanged(); // Ou use DiffUtil para melhor performance
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_message, parent, false);
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        Message message = messageList.get(position);
        holder.bind(message);
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder {
        TextView textViewTitle;
        TextView textViewTimestamp;
        TextView textViewMessageText;

        MessageViewHolder(View itemView) {
            super(itemView);
            textViewTitle = itemView.findViewById(R.id.textViewMessageTitle);
            textViewTimestamp = itemView.findViewById(R.id.textViewMessageTimestamp);
            textViewMessageText = itemView.findViewById(R.id.textViewMessageText);
        }

        void bind(@NonNull Message message) {
            textViewTitle.setText(message.getTitle());
            textViewMessageText.setText(message.getMessage());

            if (message.getTimestamp() != null) {
                textViewTimestamp.setText(dateFormat.format(new Date(message.getTimestamp())));
            } else {
                textViewTimestamp.setText(R.string.date_univalible);
            }
        }
    }
}