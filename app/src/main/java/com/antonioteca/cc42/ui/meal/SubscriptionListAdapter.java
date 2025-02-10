package com.antonioteca.cc42.ui.meal;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.antonioteca.cc42.R;
import com.antonioteca.cc42.databinding.ItemRecyclerviewSubscriptionListBinding;
import com.antonioteca.cc42.model.User;
import com.antonioteca.cc42.utility.Util;

import java.util.ArrayList;
import java.util.List;

public class SubscriptionListAdapter extends RecyclerView.Adapter<SubscriptionListAdapter.SubscriptionListViewHolder> {

    private Context context;
    private List<User> userListFull;
    private final List<User> userList;

    public SubscriptionListAdapter() {
        this.userList = new ArrayList<>();
    }

    public void updateUserList(List<User> newUserList, Context context) {
        this.context = context;
        int positionStart = getItemCount(); // Posição onde os novos itens começarão
        this.userList.addAll(newUserList);  // Adiciona novos usuários à lista existente
        this.userListFull = new ArrayList<>(this.userList);
        notifyItemRangeChanged(positionStart, newUserList.size()); // Notificar apenas a faixa adicionada
    }

    public void updateSubscriptionUser(List<String> usersIdsSubscription) {
        for (int i = 0; i < getItemCount(); i++) {
            this.userList.get(i).setSubscription(usersIdsSubscription.contains(String.valueOf(this.userList.get(i).uid)));
            notifyItemChanged(i);
        }
    }

    public void updateSubscriptionUserSingle(Long uid) {
        for (int i = 0; i < getItemCount(); i++) {
            if (this.userList.get(i).uid == uid) {
                this.userList.get(i).setSubscription(true);
                notifyItemChanged(i);
                break;
            }
        }
    }

    public void clean() {
        this.userList.clear();
        notifyItemRangeRemoved(0, getItemCount());
    }

    public void filter(String text) {
        this.userList.clear();
        if (text.isEmpty())
            this.userList.addAll(userListFull);
        else if (userListFull != null && !userListFull.isEmpty()) {
            text = text.toLowerCase();
            for (User user : userListFull) {
                if (user.login.toLowerCase().contains(text) || user.displayName.toLowerCase().contains(text)) {
                    userList.add(user);
                }
            }
        }
        notifyDataSetChanged();
    }

    public boolean containsUser(long userId) {
        for (User user : getUserList()) {
            if (user.uid == userId) {
                return true;
            }
        }
        return false;
    }

    @NonNull
    @Override
    public SubscriptionListAdapter.SubscriptionListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemRecyclerviewSubscriptionListBinding binding = ItemRecyclerviewSubscriptionListBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new SubscriptionListViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull SubscriptionListAdapter.SubscriptionListViewHolder holder, int position) {
        String imageUrl;
        int redColor = Color.rgb(200, 0, 0);
        int greenColor = Color.rgb(0, 200, 0);
        User user = userList.get(position);
        imageUrl = user.getUrlImageUser();
        holder.binding.textViewLogin.setText(user.login);
        holder.binding.textViewName.setText(user.displayName);
        if (user.isSubscription() != null && user.isSubscription()) {
            holder.binding.textViewSubscription.setTextColor(greenColor);
            holder.binding.textViewSubscription.setText(context.getString(R.string.text_signed));
        } else if (user.isSubscription() != null && !user.isSubscription()) {
            holder.binding.textViewSubscription.setTextColor(redColor);
            holder.binding.textViewSubscription.setText(context.getString(R.string.text_unsigned));
        }
        holder.itemView.setOnClickListener(v -> {
            if (user.isSubscription() != null)
                Util.showModalUserDetails(context, user.login, user.displayName, imageUrl, holder.binding.textViewSubscription.getText().toString(), user.isSubscription());
        });
        Util.setImageUserRegistered(context, imageUrl, holder.binding.imageViewUser);
    }

    @Override
    public int getItemCount() {
        return this.userList.size();
    }

    public List<User> getUserList() {
        return this.userList;
    }

    public int getNumberUser(boolean isSubscription) {
        int size = 0;
        for (User user : getUserList()) {
            if (user.isSubscription() != null && user.isSubscription() && isSubscription) {
                size += 1;
            } else if (user.isSubscription() != null && !user.isSubscription() && !isSubscription) {
                size += 1;
            }
        }
        return size;
    }

    public static class SubscriptionListViewHolder extends RecyclerView.ViewHolder {
        ItemRecyclerviewSubscriptionListBinding binding;

        public SubscriptionListViewHolder(@NonNull ItemRecyclerviewSubscriptionListBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}