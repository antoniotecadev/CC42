package com.antonioteca.cc42.ui.meal;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.antonioteca.cc42.R;
import com.antonioteca.cc42.databinding.FragmentMealBinding;
import com.antonioteca.cc42.databinding.ItemRecyclerviewMealListBinding;
import com.antonioteca.cc42.model.Meal;
import com.antonioteca.cc42.model.MealQrCode;
import com.antonioteca.cc42.network.NotificationFirebase.Notification;
import com.antonioteca.cc42.utility.Loading;
import com.antonioteca.cc42.utility.MealsUtils;
import com.antonioteca.cc42.utility.Util;
import com.antonioteca.cc42.viewmodel.MealViewModel;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class MealAdapter extends RecyclerView.Adapter<MealAdapter.MealAdapterViewHolder> {
    public final List<MealQrCode> listMealQrCode = new ArrayList<>();
    public final List<String> idMealQrCode = new ArrayList<>();
    private final Set<Integer> selectedPositions = new HashSet<>();
    public final List<Meal> mealList = new ArrayList<>();
    private final FirebaseDatabase firebaseDatabase;
    private final LayoutInflater layoutInflater;
    private final FragmentMealBinding binding;
    private final MealViewModel mealViewModel;
    private final DatabaseReference mealsRef;
    private final Context context;
    private String lastKey = null;
    private final Loading loading;
    private final int campusId;
    private final int cursusId;
    private final long userId;

    public MealAdapter(Context context, Loading loading, FragmentMealBinding binding, DatabaseReference mealsRef, MealViewModel mealViewModel, FirebaseDatabase firebaseDatabase, LayoutInflater layoutInflater, long uid, int campusId, int cursusId) {
        this.loading = loading;
        this.binding = binding;
        this.mealsRef = mealsRef;
        this.mealViewModel = mealViewModel;
        this.context = context;
        this.userId = uid;
        this.campusId = campusId;
        this.cursusId = cursusId;
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
        if (!loading.isLoading && (position == getItemCount() - 1)) {
            Toast.makeText(context, R.string.loading_more_meals, Toast.LENGTH_SHORT).show();
            loading.isLoading = true;
            loadMoreMeals();
        }

        if (selectedPositions.contains(position))
            holder.itemView.setBackgroundColor(Color.LTGRAY);
        else
            holder.itemView.setBackgroundColor(Color.TRANSPARENT);

        Meal meal = mealList.get(position);
        holder.binding.textViewType.setText(meal.getType());
        holder.binding.textViewName.setText(meal.getName());
        holder.binding.textViewDescription.setText(meal.getDescription());
        holder.binding.txtViewQuantity.setText(String.valueOf(meal.getQuantity()));
        holder.binding.textViewDateCreated.setText(meal.getCreatedDate());
        MealsUtils.loadingImageMeal(context, meal.getPathImage(), holder.binding.imageViewMeal, false);
        holder.itemView.setOnClickListener(v -> {
            MealListFragmentDirections.ActionNavMealToDetailsMealFragment actionNavMealToDetailsMealFragment = MealListFragmentDirections.actionNavMealToDetailsMealFragment(meal, cursusId);
            Navigation.findNavController(v).navigate(actionNavMealToDetailsMealFragment);
        });
        holder.itemView.setOnCreateContextMenuListener((contextMenu, view, contextMenuInfo) -> {
            contextMenu.setHeaderTitle(meal.getName());
            MenuItem menuItemEdit = contextMenu.add(view.getContext().getString(R.string.edit_meal));
            MenuItem menuItemDelete = contextMenu.add(view.getContext().getString(R.string.delete_meal));
            MenuItem menuItemNotify = contextMenu.add(view.getContext().getString(R.string.notify_meal));
            MenuItem menuItemAddQrCode = contextMenu.add("Add Qr Code");
            MenuItem menuItemDelQrCode = contextMenu.add("Del Qr Code");
            if (idMealQrCode.contains(meal.getId())) {
                menuItemAddQrCode.setVisible(false);
                menuItemDelQrCode.setVisible(true);
            } else {
                menuItemAddQrCode.setVisible(true);
                menuItemDelQrCode.setVisible(false);
            }
            menuItemEdit.setOnMenuItemClickListener(item -> {
                MealListFragmentDirections.ActionNavMealToDialogFragmentCreateMeal actionNavMealToDialogFragmentCreateMeal = MealListFragmentDirections.actionNavMealToDialogFragmentCreateMeal(false, cursusId).setMeal(meal);
                Navigation.findNavController(view).navigate(actionNavMealToDialogFragmentCreateMeal);
                return true;
            });
            menuItemDelete.setOnMenuItemClickListener(item -> {
                deleteMeal(firebaseDatabase, context, meal, layoutInflater, campusId, cursusId);
                return true;
            });
            menuItemNotify.setOnMenuItemClickListener(item -> {
                ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(context, R.array.meals_way, android.R.layout.simple_spinner_item);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

                LinearLayout linearLayout = new LinearLayout(context);
                linearLayout.setOrientation(LinearLayout.VERTICAL);
                linearLayout.setPadding(32, 16, 32, 0);
                AppCompatSpinner spinner = new AppCompatSpinner(context);
                spinner.setPadding(0, 0, 0, 32);
                spinner.setAdapter(adapter);
                linearLayout.addView(spinner);

                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setView(linearLayout);
                builder.setTitle(R.string.notify_meal);
                builder.setMessage(meal.getName());
                builder.setIcon(R.drawable.logo_42);
                builder.setCancelable(false);
                builder.setNeutralButton(R.string.no, (dialog, which) -> dialog.dismiss());
                builder.setPositiveButton(R.string.yes, null);

                AlertDialog dialog = builder.create();
                dialog.show();

                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                    meal.setType(meal.getType() + ": " + spinner.getSelectedItem().toString());
                    try {
                        String topicStudent = "meals_" + campusId + "_" + cursusId;
                        Notification.sendNotificationForTopic(context, layoutInflater, meal, cursusId, topicStudent, null);
                        dialog.dismiss();
                    } catch (IOException e) {
                        Toast.makeText(context, R.string.error_send_notification, Toast.LENGTH_LONG).show();
                    }
                });
                return true;
            });
            menuItemAddQrCode.setOnMenuItemClickListener(item -> {
                Bitmap bitmapQrCode = Util.generateQrCodeWithLogo(context, "meal" + meal.getId() + "#" + userId);
                if (bitmapQrCode != null) {
                    if (!selectedPositions.contains(position)) {
                        selectedPositions.add(position);
                        notifyItemChanged(position);
                    }
                    idMealQrCode.add(meal.getId());
                    listMealQrCode.add(new MealQrCode(meal.getId(), meal.getName(), meal.getDescription(), campusId, cursusId, bitmapQrCode));
                    Snackbar.make(view, meal.getName(), Snackbar.LENGTH_LONG).show();
                } else
                    Snackbar.make(view, R.string.msg_qr_code_invalid, Snackbar.LENGTH_LONG).show();
                return true;
            });
            menuItemDelQrCode.setOnMenuItemClickListener(item -> {
                if (selectedPositions.contains(position)) {
                    selectedPositions.remove(position);
                    notifyItemChanged(position);
                }
                idMealQrCode.remove(meal.getId());
                for (MealQrCode mealQrCodo : listMealQrCode) {
                    if (meal.getId().equals(mealQrCodo.id())) {
                        listMealQrCode.remove(mealQrCodo);
                        break;
                    }
                }
                Snackbar.make(view, meal.getName(), Snackbar.LENGTH_LONG).show();
                return true;
            });
        });
    }

    @Override
    public int getItemCount() {
        return mealList.size();
    }

    public void clean() {
        this.mealList.clear();
        notifyItemRangeRemoved(0, getItemCount());
    }

    public void loadMoreMeals() {
        if (lastKey != null) {
            mealViewModel.loadMeals(context, binding, mealsRef, lastKey);
        }
    }

    public void addMeal(Meal meal) {
        mealList.add(0, meal);
        notifyItemInserted(0);
        mealViewModel.mealList.add(0, meal);
    }

    public void updateMeal(Meal meal) {
        int index = mealList.indexOf(meal);
        if (index != -1) {
            mealList.set(index, meal);
            notifyItemChanged(index);
            mealViewModel.mealList.set(index, meal);
        }
    }

    public void deleteMeal(Meal meal) {
        int index = mealList.indexOf(meal);
        if (index != -1) {
            mealList.remove(index);
            notifyItemRemoved(index);
            mealViewModel.mealList.remove(index);
        }
    }

    public void updatePathImage(String idMeal, String pathImage) {
        for (int i = 0; i < mealList.size(); i++) {
            if (Objects.equals(mealList.get(i).getId(), idMeal)) {
                mealList.get(i).setPathImage(pathImage);
                notifyItemChanged(i);
                break;
            }
        }
    }

    public void updateMealList(List<Meal> newMealList, String lastKey) {
        int previousSize = getItemCount();
        this.mealList.addAll(newMealList);
        this.lastKey = lastKey;
        notifyItemRangeInserted(previousSize, newMealList.size());
        loading.isLoading = false;
    }

    private void deleteMeal(FirebaseDatabase firebaseDatabase, Context context, Meal meal, LayoutInflater layoutInflater, int campusId, int cursusId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.delete_meal);
        builder.setMessage(meal.getName());
        builder.setIcon(R.drawable.logo_42);
        builder.setNeutralButton(R.string.no, (dialog, which) -> dialog.dismiss());
        builder.setPositiveButton(R.string.yes, (dialog, which) -> mealViewModel.deleteMealFromFirebase(firebaseDatabase, layoutInflater, context, String.valueOf(campusId), String.valueOf(cursusId), meal));
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
