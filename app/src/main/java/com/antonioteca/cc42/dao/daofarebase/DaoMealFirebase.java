package com.antonioteca.cc42.dao.daofarebase;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.antonioteca.cc42.R;
import com.antonioteca.cc42.databinding.FragmentDialogCreateMealBinding;
import com.antonioteca.cc42.model.Meal;
import com.antonioteca.cc42.utility.DateUtils;
import com.antonioteca.cc42.utility.Util;
import com.antonioteca.cc42.viewmodel.MealViewModel;
import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DaoMealFirebase {

    public static void uploadImageToCloudinary(FirebaseDatabase firebaseDatabase,
                                               LayoutInflater layoutInflater,
                                               FragmentDialogCreateMealBinding binding,
                                               Context context,
                                               String campusId,
                                               Uri imageUri) {

        MediaManager.get().upload(imageUri)
                .option("asset_folder", "meals") // Pasta no Cloudinary (opcional)
                .callback(new UploadCallback() {
                    @Override
                    public void onStart(String requestId) {
                        // Upload iniciado
                        Toast.makeText(context, context.getString(R.string.start_upload_image), Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onProgress(String requestId, long bytes, long totalBytes) {
                        // Progresso do upload
                        // Double progress = (double) bytes / totalBytes;
                        // post progress to app UI (e.g. progress bar, notification)
                    }

                    @Override
                    public void onSuccess(String requestId, Map resultData) {
                        String imageUrl = (String) resultData.get("url");
                        saveMealToFirebase(firebaseDatabase,
                                layoutInflater,
                                binding,
                                context,
                                campusId,
                                imageUrl);
                    }

                    @Override
                    public void onError(String requestId, ErrorInfo error) {
                        String message = context.getString(R.string.error_save_image) + ": " + error.getDescription();
                        Util.showAlertDialogMessage(context, layoutInflater, context.getString(R.string.err), message, "#E53935", null);
                        saveMealToFirebase(firebaseDatabase,
                                layoutInflater,
                                binding,
                                context,
                                campusId,
                                "");
                    }

                    @Override
                    public void onReschedule(String requestId, ErrorInfo error) {
                        // Tentar novamente em caso de falha
                    }
                }).dispatch();
    }

    public static void saveMealToFirebase(FirebaseDatabase firebaseDatabase,
                                          LayoutInflater layoutInflater,
                                          FragmentDialogCreateMealBinding binding,
                                          Context context,
                                          String campusId,
                                          String imageUrl) {

        String mealName = binding.textInputEditTextName.getText().toString();
        String mealDescription = binding.textInputEditTextDescription.getText().toString();
        int mealsQauntity = 0;
        int selectedPosition = binding.spinnerQuantity.getSelectedItemPosition();
        if (selectedPosition != AdapterView.INVALID_POSITION) {
            mealsQauntity = (int) binding.spinnerQuantity.getItemAtPosition(selectedPosition);
        }

        // Cria ou atualiza a lista de participantes do evento
        DatabaseReference mealsRef = firebaseDatabase.getReference("campus")
                .child(campusId)
                .child("meals");
        // Gerar um ID único para a refeição
        String mealId = mealsRef.push().getKey();

        String dateCreated = DateUtils.getCurrentDate();

        // Criar um objeto Meal
        Meal meal = new Meal(
                mealId,
                mealName,
                mealDescription,
                dateCreated,
                mealsQauntity,
                imageUrl
        );

        // Salvar os dados da refeição no Firebase Realtime Database
        mealsRef.child(mealId).setValue(meal)
                .addOnSuccessListener(aVoid -> {
                    binding.buttonClose.setEnabled(true);
                    binding.buttonCreateMeal.setEnabled(true);
                    binding.spinnerQuantity.setSelection(0);
                    binding.textInputEditTextName.setText("");
                    binding.textInputEditTextDescription.setText("");
                    binding.textInputEditTextName.requestFocus();
                    binding.progressBarMeal.setVisibility(View.GONE);
                    String message = mealName + "\n" + context.getString(R.string.save_meal);
                    Util.showAlertDialogMessage(context, layoutInflater, context.getString(R.string.sucess), message, "#4CAF50", null);
                })
                .addOnFailureListener(e -> {
                    binding.buttonClose.setEnabled(true);
                    binding.buttonCreateMeal.setEnabled(true);
                    binding.progressBarMeal.setVisibility(View.GONE);
                    String message = mealName + "\n" + context.getString(R.string.error_save_meal) + ": " + e.getMessage();
                    Util.showAlertDialogMessage(context, layoutInflater, context.getString(R.string.err), message, "#E53935", null);
                });
    }

    public static void loadMeals(MealViewModel mealViewModel, FirebaseDatabase firebaseDatabase,
                                 LayoutInflater layoutInflater,
                                 ProgressBar progressBarMeal, Context context,
                                 String campusId) {
        if (mealViewModel.getMealList().getValue() == null || mealViewModel.getMealList().getValue().isEmpty()) {
            progressBarMeal.setVisibility(View.VISIBLE);
            DatabaseReference mealsRef = firebaseDatabase.getReference("campus")
                    .child(campusId)
                    .child("meals");

            mealsRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        List<Meal> mealList = new ArrayList<>();
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            Meal meal = dataSnapshot.getValue(Meal.class);
                            mealList.add(meal);
                        }
                        mealViewModel.setMealList(mealList); // Atualizar RecyclerView
                        progressBarMeal.setVisibility(View.INVISIBLE);
                    } else {
                        String message = context.getString(R.string.meals_not_found);
                        Util.showAlertDialogMessage(context, layoutInflater, context.getString(R.string.warning), message, "#FDD835", null);
                        progressBarMeal.setVisibility(View.INVISIBLE);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    String message = context.getString(R.string.error_load_data) + ": " + error.getMessage();
                    Util.showAlertDialogMessage(context, layoutInflater, context.getString(R.string.err), message, "#E53935", null);
                    progressBarMeal.setVisibility(View.INVISIBLE);
                }
            });
        }
    }
}
