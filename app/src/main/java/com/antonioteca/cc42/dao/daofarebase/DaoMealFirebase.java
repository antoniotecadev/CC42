package com.antonioteca.cc42.dao.daofarebase;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.antonioteca.cc42.R;
import com.antonioteca.cc42.model.Meal;
import com.antonioteca.cc42.utility.DateUtils;
import com.antonioteca.cc42.utility.Util;
import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Map;

public class DaoMealFirebase {

    public static void uploadImageToCloudinary(FirebaseDatabase firebaseDatabase,
                                               LayoutInflater layoutInflater,
                                               Button buttonClose, Button buttonCreateMeal, ProgressBar progressBar,
                                               Context context,
                                               String campusId,
                                               String mealName,
                                               String mealDescription,
                                               int mealsQauntity,
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
                                buttonClose, buttonCreateMeal, progressBar,
                                context,
                                campusId,
                                mealName,
                                mealDescription,
                                mealsQauntity,
                                imageUrl);
                    }

                    @Override
                    public void onError(String requestId, ErrorInfo error) {
                        String message = context.getString(R.string.error_save_image) + ": " + error.getDescription();
                        Util.showAlertDialogMessage(context, layoutInflater, context.getString(R.string.err), message, "#E53935", null);
                        saveMealToFirebase(firebaseDatabase,
                                layoutInflater,
                                buttonClose, buttonCreateMeal, progressBar,
                                context,
                                campusId,
                                mealName,
                                mealDescription,
                                mealsQauntity,
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
                                          Button buttonClose, Button buttonCreateMeal, ProgressBar progressBar,
                                          Context context,
                                          String campusId,
                                          String mealName,
                                          String mealDescription,
                                          int mealsQauntity,
                                          String imageUrl) {

        // Cria ou atualiza a lista de participantes do evento
        DatabaseReference campusRef = firebaseDatabase.getReference("campus")
                .child(campusId)
                .child("meals");
        // Gerar um ID único para a refeição
        String mealId = campusRef.push().getKey();

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
        campusRef.child(mealId).setValue(meal)
                .addOnSuccessListener(aVoid -> {
                    buttonClose.setEnabled(true);
                    buttonCreateMeal.setEnabled(true);
                    progressBar.setVisibility(View.GONE);
                    String message = mealName + "\n" + context.getString(R.string.save_meal);
                    Util.showAlertDialogMessage(context, layoutInflater, context.getString(R.string.sucess), message, "#4CAF50", null);
                })
                .addOnFailureListener(e -> {
                    buttonClose.setEnabled(true);
                    buttonCreateMeal.setEnabled(true);
                    progressBar.setVisibility(View.GONE);
                    String message = mealName + "\n" + context.getString(R.string.error_save_meal) + ": " + e.getMessage();
                    Util.showAlertDialogMessage(context, layoutInflater, context.getString(R.string.err), message, "#E53935", null);
                });
    }
}
