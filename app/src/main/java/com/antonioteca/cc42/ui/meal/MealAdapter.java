package com.antonioteca.cc42.ui.meal;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.antonioteca.cc42.databinding.ItemRecyclerviewMealListBinding;
import com.antonioteca.cc42.model.Meal;
import com.antonioteca.cc42.utility.Util;

import java.util.List;

public class MealAdapter extends RecyclerView.Adapter<MealAdapter.MealAdapterViewHolder> {

    private final Context context;
    private List<Meal> mealList;

    public MealAdapter(Context context, List<Meal> mealList) {
        this.context = context;
        this.mealList = mealList;
    }

    @NonNull
    @Override
    public MealAdapterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemRecyclerviewMealListBinding binding = ItemRecyclerviewMealListBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new MealAdapterViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull MealAdapterViewHolder holder, int position) {
        Meal meal = mealList.get(position);
        holder.binding.textViewNameMeal.setText(meal.getName());
        holder.binding.textViewDescription.setText(meal.getDescription());
        holder.binding.txtQuantidadeProduto.setText(String.valueOf(meal.getQuantity()));
        holder.binding.textViewDateCreated.setText(meal.getDate());
        Util.loadingImageMeal(context, meal.getPathImage(), holder.binding.imageViewMeal);
        holder.itemView.setOnClickListener(v -> {
            MealFragmentDirections.ActionNavMealToDetailsMealFragment actionNavMealToDetailsMealFragment
                    = MealFragmentDirections.actionNavMealToDetailsMealFragment(meal);
            Navigation.findNavController(v).navigate(actionNavMealToDetailsMealFragment);
        });
    }

    @Override
    public int getItemCount() {
        return mealList.size();
    }

    public void updateMealList(List<Meal> newMealList) {
        mealList = newMealList;
        notifyDataSetChanged();
    }

    public static class MealAdapterViewHolder extends RecyclerView.ViewHolder {
        ItemRecyclerviewMealListBinding binding;

        public MealAdapterViewHolder(@NonNull ItemRecyclerviewMealListBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
