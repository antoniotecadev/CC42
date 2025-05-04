package com.antonioteca.cc42.ui.meal;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.antonioteca.cc42.R;
import com.antonioteca.cc42.databinding.ItemRecyclerviewCursuListBinding;
import com.antonioteca.cc42.model.Cursu;

import java.util.ArrayList;
import java.util.List;

public class CursuAdapter extends RecyclerView.Adapter<CursuAdapter.CursuViewHolder> {

    private final int colorIcon;
    private final Context context;
    private final int cursusIdUser;
    private final List<Cursu> cursuList;
    private final List<Cursu> cursuListFull;


    public CursuAdapter(Context context, List<Cursu> cursuList, int colorIcon, int cursusIdUser) {
        this.context = context;
        this.cursuList = cursuList;
        this.colorIcon = colorIcon;
        this.cursusIdUser = cursusIdUser;
        this.cursuListFull = new ArrayList<>(this.cursuList);
    }

    @NonNull
    @Override
    public CursuViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemRecyclerviewCursuListBinding binding = ItemRecyclerviewCursuListBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new CursuViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull CursuViewHolder holder, int position) {
        Cursu cursu = cursuList.get(position);
        holder.binding.textViewName.setTextColor(ContextCompat.getColor(context, cursusIdUser == cursu.getId() ? R.color.green : R.color.textColorPrimary));
        if (colorIcon != 0)
            holder.binding.imageViewCursu.setColorFilter(colorIcon);
        holder.binding.textViewName.setText(cursu.getName());
        holder.binding.textViewId.setText(String.valueOf(cursu.getId()));
        holder.itemView.setOnClickListener(v -> {
            CursuListMealFragmentDirections.ActionNavCursuListMealToNavMeal actionNavCursuListMealToNavMeal =
                    CursuListMealFragmentDirections.actionNavCursuListMealToNavMeal(cursu);
            Navigation.findNavController(v).navigate(actionNavCursuListMealToNavMeal);
        });
    }

    @Override
    public int getItemCount() {
        return cursuList.size();
    }

    public void moveTopCursuUser() {
        for (Cursu cursu : cursuList) {
            if (cursu.getId() == cursusIdUser && cursuList.indexOf(cursu) != 0) {
                cursuList.add(0, cursu);
                notifyItemMoved(cursuList.indexOf(cursu), 0);
                break;
            }
        }
    }

    public void filter(String text, boolean isStaff) {
        this.cursuList.clear();
        if (text.isEmpty())
            this.cursuList.addAll(cursuListFull);
        else if (!cursuListFull.isEmpty()) {
            text = text.toLowerCase();
            for (Cursu cursu : cursuListFull) {
                if (isStaff) {
                    int cursusId = cursu.getId();
                    if (cursusId == 21 || cursusId == 66 || cursusId == 9) {
                        cursuList.add(cursu);
                    }
                } else if (cursu.getName().toLowerCase().contains(text) || String.valueOf(cursu.getId()).toLowerCase().contains(text)) {
                    cursuList.add(cursu);
                }
            }
        }
        notifyDataSetChanged();
    }

    public static class CursuViewHolder extends RecyclerView.ViewHolder {

        ItemRecyclerviewCursuListBinding binding;

        public CursuViewHolder(@NonNull ItemRecyclerviewCursuListBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}