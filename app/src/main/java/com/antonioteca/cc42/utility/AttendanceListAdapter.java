package com.antonioteca.cc42.utility;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.antonioteca.cc42.R;
import com.antonioteca.cc42.model.User;

import java.util.ArrayList;
import java.util.List;

public class AttendanceListAdapter extends RecyclerView.Adapter<AttendanceListAdapter.AttendanceListViewHolder> {

    private final List<User> userList;

    public AttendanceListAdapter() {
        userList = new ArrayList<>();
    }

    public void updateUserList(List<User> newUserList) {
        int positionStart = userList.size(); // Posição onde os novos itens começarão
        this.userList.addAll(newUserList);  // Adiciona novos usuários à lista existente
        notifyItemRangeChanged(positionStart, newUserList.size()); // Notificar apenas a faixa adicionada
    }

    @NonNull
    @Override
    public AttendanceListAdapter.AttendanceListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recyclerview_event, parent, false);
        return new AttendanceListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AttendanceListAdapter.AttendanceListViewHolder holder, int position) {
        User user = userList.get(position);
        holder.textView.setText(user.displayName);
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public static class AttendanceListViewHolder extends RecyclerView.ViewHolder {
        TextView textView;

        public AttendanceListViewHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.textViewName);
        }
    }
}
