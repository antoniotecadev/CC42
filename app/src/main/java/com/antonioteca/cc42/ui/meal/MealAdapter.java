package com.antonioteca.cc42.ui.meal;

import static com.antonioteca.cc42.dao.daofarebase.DaoMealFirebase.deleteMealFromFirebase;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.antonioteca.cc42.R;
import com.antonioteca.cc42.databinding.ItemRecyclerviewMealListBinding;
import com.antonioteca.cc42.model.Meal;
import com.antonioteca.cc42.utility.Util;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class MealAdapter extends RecyclerView.Adapter<MealAdapter.MealAdapterViewHolder> {

    private final FirebaseDatabase firebaseDatabase;
    private final LayoutInflater layoutInflater;
    private final int campusId;

    private final Context context;
    private List<Meal> mealList;

    public MealAdapter(Context context, List<Meal> mealList, FirebaseDatabase firebaseDatabase, LayoutInflater layoutInflater, int campusId) {
        this.context = context;
        this.mealList = mealList;
        this.campusId = campusId;
        this.layoutInflater = layoutInflater;
        this.firebaseDatabase = firebaseDatabase;
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
            MealListFragmentDirections.ActionNavMealToDetailsMealFragment actionNavMealToDetailsMealFragment
                    = MealListFragmentDirections.actionNavMealToDetailsMealFragment(meal);
            Navigation.findNavController(v).navigate(actionNavMealToDetailsMealFragment);
        });
        holder.itemView.setOnCreateContextMenuListener((contextMenu, view, contextMenuInfo) -> {
            contextMenu.setHeaderTitle(meal.getName());
            MenuItem menuItemEdit = contextMenu.add(view.getContext().getString(R.string.edit_meal));
            MenuItem menuItemDelete = contextMenu.add(view.getContext().getString(R.string.delete_meal));
            menuItemEdit.setOnMenuItemClickListener(item -> {
                MealListFragmentDirections.ActionNavMealToDialogFragmentCreateMeal actionNavMealToDialogFragmentCreateMeal =
                        MealListFragmentDirections.actionNavMealToDialogFragmentCreateMeal(false).setMeal(meal);
                Navigation.findNavController(view).navigate(actionNavMealToDialogFragmentCreateMeal);
                return true;
            });
            menuItemDelete.setOnMenuItemClickListener(item -> {
                deleteMeal(firebaseDatabase, context, meal, layoutInflater, campusId);
                return true;
            });
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

    private void deleteMeal(FirebaseDatabase firebaseDatabase, Context context, Meal meal, LayoutInflater layoutInflater, int campusId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.delete_meal);
        builder.setMessage(meal.getName());
        builder.setIcon(R.drawable.logo_42);
        builder.setNeutralButton(R.string.no, (dialog, which) -> dialog.dismiss());
        builder.setPositiveButton(R.string.yes, (dialog, which) -> deleteMealFromFirebase(firebaseDatabase,
                layoutInflater,
                context,
                String.valueOf(campusId),
                meal.getId(),
                meal.getPathImage()));
        builder.show();
    }

    public static class MealAdapterViewHolder extends RecyclerView.ViewHolder {
        ItemRecyclerviewMealListBinding binding;

        public MealAdapterViewHolder(@NonNull ItemRecyclerviewMealListBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
