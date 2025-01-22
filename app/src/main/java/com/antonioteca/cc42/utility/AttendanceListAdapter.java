package com.antonioteca.cc42.utility;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.antonioteca.cc42.R;
import com.antonioteca.cc42.databinding.ItemRecyclerviewAttendanceListBinding;
import com.antonioteca.cc42.model.User;

import java.util.ArrayList;
import java.util.List;

public class AttendanceListAdapter extends RecyclerView.Adapter<AttendanceListAdapter.AttendanceListViewHolder> {

    private Context context;
    private final String colorCoalition;
    private final List<User> userList;

    public AttendanceListAdapter(String colorCoalition) {
        this.userList = new ArrayList<>();
        this.colorCoalition = colorCoalition;
    }

    public void updateUserList(List<User> newUserList, Context context) {
        this.context = context;
        int positionStart = userList.size(); // Posição onde os novos itens começarão
        this.userList.addAll(newUserList);  // Adiciona novos usuários à lista existente
        notifyItemRangeChanged(positionStart, newUserList.size()); // Notificar apenas a faixa adicionada
    }

    public void updateUserList(List<String> usersWhoMarkedPresence) {
        for (int i = 0; i < getItemCount(); i++) {
            if (usersWhoMarkedPresence.contains(String.valueOf(this.userList.get(i).uid)))
                this.userList.get(i).setPresent(true);
            else
                this.userList.get(i).setPresent(false);
            notifyItemChanged(i);
        }
    }

    @NonNull
    @Override
    public AttendanceListAdapter.AttendanceListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemRecyclerviewAttendanceListBinding binding = ItemRecyclerviewAttendanceListBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new AttendanceListViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull AttendanceListAdapter.AttendanceListViewHolder holder, int position) {
        User user = userList.get(position);
        if (colorCoalition != null) {
            int color = Color.parseColor(colorCoalition);
            holder.binding.dividerBottom.setBackgroundColor(color);
        }
        Util.setImageUserRegistered(context, user.getUrlImageUserRegisteredEvent(), holder.binding.imageViewUserRegistered);
        holder.binding.textViewLogin.setText(user.login);
        holder.binding.textViewName.setText(user.displayName);
        if (user.isPresent() != null && user.isPresent()) {
            holder.binding.textViewLogin.setTextColor(Color.GREEN);
            holder.binding.textViewPresent.setTextColor(Color.GREEN);
            holder.binding.textViewPresent.setText(context.getString(R.string.text_present));
        } else if (user.isPresent() != null && !user.isPresent()) {
            holder.binding.textViewPresent.setTextColor(Color.RED);
            holder.binding.textViewPresent.setText(context.getString(R.string.text_absent));
        }
        holder.itemView.setOnClickListener(v -> {
            if (user.isPresent() != null)
                Util.showModalUserDetails(context, user.login, user.displayName, user.getUrlImageUserRegisteredEvent(), user.isPresent());
        });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public static class AttendanceListViewHolder extends RecyclerView.ViewHolder {
        ItemRecyclerviewAttendanceListBinding binding;

        public AttendanceListViewHolder(@NonNull ItemRecyclerviewAttendanceListBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
