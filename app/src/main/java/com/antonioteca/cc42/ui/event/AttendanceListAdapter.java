package com.antonioteca.cc42.ui.event;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.antonioteca.cc42.R;
import com.antonioteca.cc42.databinding.ItemRecyclerviewAttendanceListBinding;
import com.antonioteca.cc42.model.User;
import com.antonioteca.cc42.utility.Util;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class AttendanceListAdapter extends RecyclerView.Adapter<AttendanceListAdapter.AttendanceListViewHolder> {

    private Context context;
    private List<User> userListFull;
    private final List<User> userList;

    public AttendanceListAdapter() {
        this.userList = new ArrayList<>();
    }

    public void updateUserList(List<User> newUserList, Context context) {
        this.context = context;
        int positionStart = getItemCount(); // Posição onde os novos itens começarão
        this.userList.addAll(newUserList);  // Adiciona novos usuários à lista existente
        this.userListFull = new ArrayList<>(this.userList);
        notifyItemRangeChanged(positionStart, newUserList.size()); // Notificar apenas a faixa adicionada
    }

    public void updateAttendanceUser(List<String> usersIdsWithMarkedPresence) {
        for (int i = 0; i < getItemCount(); i++) {
            this.userList.get(i).setPresent(usersIdsWithMarkedPresence.contains(String.valueOf(this.userList.get(i).uid)));
            notifyItemChanged(i);
        }
    }

    public void updateAttendanceUserSingle(Long uid) {
        for (int i = 0; i < getItemCount(); i++) {
            if (this.userList.get(i).uid == uid) {
                this.userList.get(i).setPresent(true);
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
    public AttendanceListAdapter.AttendanceListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemRecyclerviewAttendanceListBinding binding = ItemRecyclerviewAttendanceListBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new AttendanceListViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull AttendanceListAdapter.AttendanceListViewHolder holder, int position) {
        String imageUrl;
        int redColor = ContextCompat.getColor(context, R.color.red);
        int greenColor = ContextCompat.getColor(context, R.color.green);
        User user = userList.get(position);
        imageUrl = user.getUrlImageUser();
        holder.binding.textViewLogin.setText(user.login);
        holder.binding.textViewName.setText(user.displayName);
        if (user.isPresent() != null && user.isPresent()) {
            holder.binding.textViewPresent.setTextColor(greenColor);
            holder.binding.textViewPresent.setText(context.getString(R.string.text_present));
        } else if (user.isPresent() != null && !user.isPresent()) {
            holder.binding.textViewPresent.setTextColor(redColor);
            holder.binding.textViewPresent.setText(context.getString(R.string.text_absent));
        }
        holder.itemView.setOnClickListener(v -> {
            if (user.isPresent() != null)
                Util.showModalUserDetails(context, user.login, user.displayName, imageUrl, holder.binding.textViewPresent.getText().toString(), user.isPresent());
        });
        Util.setImageUserRegistered(context, imageUrl, holder.binding.imageViewUserRegistered);
    }

    @Override
    public int getItemCount() {
        return this.userList.size();
    }

    public List<User> getUserList() {
        return this.userList;
    }

    public int getNumberUser(boolean isPresent) {
        int size = 0;
        for (User user : getUserList()) {
            if (user.isPresent() != null && user.isPresent() && isPresent) {
                size += 1;
            } else if (user.isPresent() != null && !user.isPresent() && !isPresent) {
                size += 1;
            }
        }
        return size;
    }

    public static class AttendanceListViewHolder extends RecyclerView.ViewHolder {
        ItemRecyclerviewAttendanceListBinding binding;

        public AttendanceListViewHolder(@NonNull ItemRecyclerviewAttendanceListBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}