package com.antonioteca.cc42.ui.meal;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.antonioteca.cc42.R;
import com.antonioteca.cc42.databinding.ItemRecyclerviewSubscriptionListBinding;
import com.antonioteca.cc42.model.User;
import com.antonioteca.cc42.model.UserDiffCallback;
import com.antonioteca.cc42.utility.StarUtils;
import com.antonioteca.cc42.utility.Util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class SubscriptionListAdapter extends RecyclerView.Adapter<SubscriptionListAdapter.SubscriptionListViewHolder> {

    private Context context;
    private List<User> userListFull;
    private final List<User> userList;

    public SubscriptionListAdapter() {
        this.userList = new ArrayList<>();
    }

    public void updateUserList(List<User> newUserList, Context context) {
        this.context = context;
        this.userListFull = new ArrayList<>(this.userList);
        // Calcule a diferença
        UserDiffCallback diffCallback = new UserDiffCallback(new ArrayList<>(this.userList), newUserList); // Passe cópias
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(diffCallback);
        this.userList.addAll(newUserList);
        // Despache as atualizações para o RecyclerView.
        // Isso irá chamar os métodos notifyItemInserted, notifyItemRemoved,
        // notifyItemMoved, ou notifyItemChanged (com ou sem payload)
        // apenas para os itens que realmente mudaram.
        diffResult.dispatchUpdatesTo(this);
    }

    public void updateSubscriptionUser(List<String> usersIdsSubscription) {
        for (int i = 0; i < getItemCount(); i++) {
            this.userList.get(i).setSubscription(usersIdsSubscription.contains(String.valueOf(this.userList.get(i).uid)));
            notifyItemChanged(i);
        }
    }

    public void updateSubscriptionUserSingle(Long uid) {
        for (int i = 0; i < getItemCount(); i++) {
            if (Objects.equals(this.userList.get(i).uid, uid)) {
                this.userList.get(i).setSubscription(true);
                notifyItemChanged(i);
                break;
            }
        }
    }

    public void updateRatingValueUser(HashMap<?, ?> ratingValuesUsers) {
        for (int i = 0; i < getItemCount(); i++) {
            String uid = String.valueOf(this.userList.get(i).uid);
            if (ratingValuesUsers.containsKey(uid)) {
                this.userList.get(i).ratingValue = (int) ratingValuesUsers.get(uid);
                notifyItemChanged(i);
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

    public String containsUser(long userId) {
        for (User user : getUserList()) {
            if (user.uid == userId) {
                return Objects.requireNonNullElse(user.getUrlImageUser(), "");
            }
        }
        return null;
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
        int redColor = ContextCompat.getColor(context, R.color.red);
        int greenColor = ContextCompat.getColor(context, R.color.green);
        User user = userList.get(position);
        imageUrl = user.getUrlImageUser();
        holder.binding.textViewLogin.setText(user.login);
        holder.binding.textViewName.setText(user.displayName);
        if (user.ratingValue > 0)
            StarUtils.selectedRating(holder.binding.starRatingDone, user.ratingValue);
        StarUtils.reduceStarSize(context, holder.binding.starRatingDone, 20, 20);
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