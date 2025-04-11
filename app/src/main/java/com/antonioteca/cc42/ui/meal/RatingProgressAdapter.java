package com.antonioteca.cc42.ui.meal;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.antonioteca.cc42.databinding.ItemRatingProgressBinding;

import java.util.List;

public class RatingProgressAdapter extends RecyclerView.Adapter<RatingProgressAdapter.ViewHolder> {

    private final List<RatingProgressItem> ratingProgressItems;

    public RatingProgressAdapter(List<RatingProgressItem> ratingProgressItems) {
        this.ratingProgressItems = ratingProgressItems;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemRatingProgressBinding binding = ItemRatingProgressBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        RatingProgressItem item = ratingProgressItems.get(position);
        int ratingCount = item.ratingCount();

        // Define a estrela (ícone)
        if (position >= 0)
            holder.binding.star1.setVisibility(View.VISIBLE);
        if (position >= 1)
            holder.binding.star2.setVisibility(View.VISIBLE);
        if (position >= 2)
            holder.binding.star3.setVisibility(View.VISIBLE);
        if (position >= 3)
            holder.binding.star4.setVisibility(View.VISIBLE);
        if (position >= 4)
            holder.binding.star5.setVisibility(View.VISIBLE);

        // Define o progresso da barra
        holder.binding.progressBar.setProgress(item.progress());
        // Define a porcentagem e numero de avaliação para cada estrela
        holder.binding.textViewRatingCountPercentage.setText(ratingCount + " - " + item.progress() + "%");
    }

    @Override
    public int getItemCount() {
        return ratingProgressItems.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemRatingProgressBinding binding;

        public ViewHolder(@NonNull ItemRatingProgressBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}