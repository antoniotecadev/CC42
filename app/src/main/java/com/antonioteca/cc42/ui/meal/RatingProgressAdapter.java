package com.antonioteca.cc42.ui.meal;

import android.view.LayoutInflater;
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

        // Define a estrela (ícone)
        holder.binding.star1.setImageResource(item.getStarIcon());
        holder.binding.star2.setImageResource(item.getStarIcon());
        holder.binding.star3.setImageResource(item.getStarIcon());
        holder.binding.star4.setImageResource(item.getStarIcon());
        holder.binding.star5.setImageResource(item.getStarIcon());

        // Define o progresso da barra
        holder.binding.progressBar.setProgress(item.getProgress());

        // Define a porcentagem
        holder.binding.tvPercentage.setText(item.getProgress() + "%");
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