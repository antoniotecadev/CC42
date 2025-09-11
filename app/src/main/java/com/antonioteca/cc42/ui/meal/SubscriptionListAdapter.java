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
import com.antonioteca.cc42.utility.Util;

import java.util.ArrayList;
import java.util.List;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class SubscriptionListAdapter extends RecyclerView.Adapter<SubscriptionListAdapter.SubscriptionListViewHolder> {

    private Context context;
    private final List<User> userList;
    public boolean isFilterSecondPortion;
    private final List<User> userListFilter;

    public SubscriptionListAdapter() {
        this.userList = new ArrayList<>();
        this.userListFilter = new ArrayList<>();
    }

    public void updateUserList(List<User> newUserList, Context context) {
        this.context = context;

        List<User> oldList = new ArrayList<>(this.userList);

        this.userList.addAll(newUserList);
        this.userListFilter.addAll(newUserList);

        // Calcule a diferença
        UserDiffCallback diffCallback = new UserDiffCallback(oldList, newUserList); // Passe cópias
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(diffCallback);
        // Despache as atualizações para o RecyclerView.
        // Isso irá chamar os métodos notifyItemInserted, notifyItemRemoved,
        // notifyItemMoved, ou notifyItemChanged (com ou sem payload)
        // apenas para os itens que realmente mudaram.
        diffResult.dispatchUpdatesTo(this);
    }

    public void updateSubscriptionUser(List<String> usersIdsSubscription) {
        Set<String> usersIdsSet = new HashSet<>(usersIdsSubscription);
        boolean asigned;
        int count = getItemCount();
        for (int i = 0; i < count; i++) {
            User currentUser = this.userList.get(i);
            asigned = usersIdsSet.contains(String.valueOf(currentUser.uid));
            if (currentUser.isSubscription() == null || currentUser.isSubscription() != asigned) {
                currentUser.setSubscription(asigned);
                this.userListFilter.get(i).setSubscription(asigned); // Assuming userListFilter has the same user objects or needs parallel update
                notifyItemChanged(i);
            }
        }
    }

    public void updateSubscriptionUserSingle(Long uid) {
        int count = getItemCount();
        for (int i = 0; i < count; i++) {
            if (Objects.equals(this.userList.get(i).uid, uid)) {
                this.userList.get(i).setSubscription(true);
                notifyItemChanged(i);
                this.userList.add(0, this.userList.get(i));
                notifyItemInserted(0);
                this.userListFilter.add(0, this.userList.get(i));
                this.userList.remove(i);
                notifyItemRemoved(i);
                this.userListFilter.remove(i);
                break;
            }
        }
    }

//    public void updateRatingValueUser(HashMap<?, ?> ratingValuesUsers) {
//        for (int i = 0; i < getItemCount(); i++) {
//            String uid = String.valueOf(this.userList.get(i).uid);
//            if (ratingValuesUsers.containsKey(uid)) {
//                this.userList.get(i).ratingValue = (int) ratingValuesUsers.get(uid);
//                notifyItemChanged(i);
//            }
//        }
//    }

    public void clean() {
        this.userList.clear();
        notifyItemRangeRemoved(0, getItemCount());
    }

    public void filterSearch(@NonNull String text) {
        this.userList.clear();
        if (text.isEmpty())
            this.userList.addAll(userListFilter);
        else if (!userListFilter.isEmpty()) {
            text = text.toLowerCase();
            for (User user : userListFilter) {
                if (user.login.toLowerCase().contains(text) || user.displayName.toLowerCase().contains(text)) {
                    userList.add(user);
                    break;
                }
            }
        }
        isFilterSecondPortion = false;
        notifyDataSetChanged();
    }

    public void filterListStatus(Boolean status) {
        this.userList.clear();
        if (status == null)
            this.userList.addAll(userListFilter);
        else if (status) {
            for (User user : userListFilter) {
                if (user.isSubscription())
                    userList.add(user);
            }
        } else {
            for (User user : userListFilter) {
                if (!user.isSubscription())
                    userList.add(user);
            }
        }
        isFilterSecondPortion = false;
        notifyDataSetChanged();
    }

    public void filterUsersSubscriptedSecondPortion(List<String> usersIdsSubscription) {
        List<User> filteredList = new ArrayList<>();
        Set<String> usersIdsSet = new HashSet<>(usersIdsSubscription); // Converta para HashSet para pesquisa O(1)
        int itemCount = getItemCount();
        for (int i = 0; i < itemCount; i++) {
            User currentUser = this.userListFilter.get(i);
            if (usersIdsSet.contains("-" + currentUser.uid)) {
                filteredList.add(this.userList.get(i)); // this.userList e this.userListFilter sincronizados
            }
        }
        this.userList.clear();
        isFilterSecondPortion = true;
        this.userList.addAll(filteredList);
        notifyDataSetChanged();
    }

//    public String containsUser(long userId) {
//        for (User user : getUserList()) {
//            if (user.uid == userId) {
//                return Objects.requireNonNullElse(user.getUrlImageUser(), "");
//            }
//        }
//        return null;
//    }

//    public String[] containsUserFaceID(long userId) {
//        for (User user : getUserList()) {
//            if (user.uid == userId) {
//                return new String[]{user.displayName, Objects.requireNonNullElse(user.getUrlImageUser(), "")};
//            }
//        }
//        return new String[]{"", null};
//    }

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
//        if (user.ratingValue > 0) Avaliação do usuario
//            StarUtils.selectedRating(holder.binding.starRatingDone, user.ratingValue);
//        StarUtils.reduceStarSize(context, holder.binding.starRatingDone, 20, 20);
        if (user.isSubscription() != null && user.isSubscription()) {
            holder.binding.textViewSubscription.setTextColor(greenColor);
            holder.binding.textViewSubscription.setText(context.getString(R.string.text_signed));
        } else if (user.isSubscription() != null && !user.isSubscription()) {
            holder.binding.textViewSubscription.setTextColor(redColor);
            holder.binding.textViewSubscription.setText(context.getString(R.string.text_unsigned));
        }
        if (isFilterSecondPortion) {
            holder.binding.textViewSubscription.setTextColor(greenColor);
            holder.binding.textViewSubscription.setText(context.getString(R.string.second_portion));
        }
        holder.binding.cardViewRegisteredUser.setOnClickListener(v -> {
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

    public int[] getNumberUser() {
        int size0 = 0;
        int size1 = 0;
        for (User user : getUserList()) {
            if (user.isSubscription() != null && user.isSubscription()) {
                size0 += 1;
            } else {
                size1 += 1;
            }
        }
        return new int[]{size0, size1};
    }

    public static class SubscriptionListViewHolder extends RecyclerView.ViewHolder {
        ItemRecyclerviewSubscriptionListBinding binding;

        public SubscriptionListViewHolder(@NonNull ItemRecyclerviewSubscriptionListBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}