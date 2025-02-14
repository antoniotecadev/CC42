package com.antonioteca.cc42.dao.daofarebase;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.antonioteca.cc42.R;
import com.antonioteca.cc42.databinding.FragmentDialogCreateMealBinding;
import com.antonioteca.cc42.databinding.FragmentMealBinding;
import com.antonioteca.cc42.model.Meal;
import com.antonioteca.cc42.network.NotificationFirebase.Notification;
import com.antonioteca.cc42.utility.DateUtils;
import com.antonioteca.cc42.utility.Util;
import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.cloudinary.utils.ObjectUtils;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DaoMealFirebase {

    public static void uploadImageToCloudinary(FirebaseDatabase firebaseDatabase,
                                               LayoutInflater layoutInflater,
                                               FragmentDialogCreateMealBinding binding,
                                               Context context,
                                               String campusId,
                                               String cursusId,
                                               Uri imageUri) {

        MediaManager.get().upload(imageUri)
                .option("asset_folder", "campus/" + campusId + "/meals") // Pasta no Cloudinary (opcional)
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
                                cursusId,
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
                                cursusId,
                                "");
                    }

                    @Override
                    public void onReschedule(String requestId, ErrorInfo error) {
                        // Tentar novamente em caso de falha
                    }
                }).dispatch();
    }

    public static void uploadNewImage(FirebaseDatabase firebaseDatabase,
                                      LayoutInflater layoutInflater,
                                      FragmentDialogCreateMealBinding binding,
                                      Context context,
                                      String campusId,
                                      String cursusId,
                                      String mealId,
                                      Uri newImageUri,
                                      String publicId,
                                      boolean onlyImage) {

        MediaManager.get().upload(newImageUri)
                .option("public_id", publicId) // Usar o mesmo public ID para substituir a imagem
                .option("overwrite", publicId != null) // Substituir a imagem existente
                .option("asset_folder", "campus/" + campusId + "/meals") // Pasta no Cloudinary (opcional)
                .callback(new UploadCallback() {
                    @Override
                    public void onStart(String requestId) {
                        Toast.makeText(context, context.getString(R.string.start_upload_image), Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onProgress(String requestId, long bytes, long totalBytes) {

                    }

                    @Override
                    public void onSuccess(String requestId, Map resultData) {
                        String newImageUrl = (String) resultData.get("url");
                        if (onlyImage) {
                            updateImageUrlInFirebase(firebaseDatabase,
                                    layoutInflater,
                                    binding,
                                    context,
                                    campusId,
                                    cursusId,
                                    mealId,
                                    newImageUrl); // Actualizar URL no Firebase
                        } else {
                            updateMealDataInFirebase(firebaseDatabase,
                                    layoutInflater,
                                    binding,
                                    context,
                                    campusId,
                                    cursusId,
                                    mealId,
                                    newImageUrl); // Actualizar todos os dados no Firebase
                        }
                    }

                    @Override
                    public void onError(String requestId, ErrorInfo error) {
                        restaureViews(binding);
                        String message = context.getString(R.string.error_update_image) + error.getDescription();
                        Util.showAlertDialogMessage(context, layoutInflater, context.getString(R.string.err), message, "#E53935", null);
                    }

                    @Override
                    public void onReschedule(String requestId, ErrorInfo error) {
                        // Tentar novamente em caso de falha
                    }
                }).dispatch();
    }

    private static void updateImageUrlInFirebase(@NonNull FirebaseDatabase firebaseDatabase,
                                                 LayoutInflater layoutInflater,
                                                 FragmentDialogCreateMealBinding binding,
                                                 Context context,
                                                 String campusId,
                                                 String cursusId,
                                                 String mealId,
                                                 String newImageUrl) {
        DatabaseReference mealsRef = firebaseDatabase.getReference("campus")
                .child(campusId)
                .child("cursus")
                .child(cursusId)
                .child("meals")
                .child(mealId);
        // Atualizar apenas o campo pathImage
        mealsRef.child("pathImage").setValue(newImageUrl)
                .addOnSuccessListener(aVoid -> {
                    restaureViews(binding);
                    String message = context.getString(R.string.sucess_image_update);
                    Util.showAlertDialogMessage(context, layoutInflater, context.getString(R.string.sucess), message, "#4CAF50", null);
                })
                .addOnFailureListener(e -> {
                    restaureViews(binding);
                    String message = context.getString(R.string.error_update_image_url) + e.getMessage();
                    Util.showAlertDialogMessage(context, layoutInflater, context.getString(R.string.err), message, "#E53935", null);
                    deleteImageFromCloudinary(newImageUrl, layoutInflater, context);
                });
    }

    public static void updateMealDataInFirebase(FirebaseDatabase firebaseDatabase,
                                                LayoutInflater layoutInflater,
                                                @NonNull FragmentDialogCreateMealBinding binding,
                                                Context context,
                                                String campusId,
                                                String cursusId,
                                                String mealId,
                                                String newImageUrl) {

        String mealName = binding.textInputEditTextName.getText().toString();
        String mealDescription = binding.textInputEditTextDescription.getText().toString();
        int mealsQauntity = (int) binding.spinnerQuantity.getItemAtPosition(binding.spinnerQuantity.getSelectedItemPosition());
        String type = (String) binding.spinnerMeals.getItemAtPosition(binding.spinnerMeals.getSelectedItemPosition());

        DatabaseReference mealsRef = firebaseDatabase.getReference("campus")
                .child(campusId)
                .child("cursus")
                .child(cursusId)
                .child("meals")
                .child(mealId);

        // Criar um mapa com os novos valores
        Map<String, Object> updates = new HashMap<>();
        updates.put("name", mealName);
        updates.put("description", mealDescription);
        updates.put("quantity", mealsQauntity);
        updates.put("type", type);
        if (newImageUrl != null)
            updates.put("pathImage", newImageUrl);

        // Atualizar os dados
        mealsRef.updateChildren(updates)
                .addOnSuccessListener(aVoid -> {
                    restaureViews(binding);
                    String message = context.getString(R.string.sucess_meal_update);
                    Util.showAlertDialogMessage(context, layoutInflater, context.getString(R.string.sucess), message, "#4CAF50", null);
                })
                .addOnFailureListener(e -> {
                    restaureViews(binding);
                    String message = context.getString(R.string.error_meal_update) + e.getMessage();
                    Util.showAlertDialogMessage(context, layoutInflater, context.getString(R.string.err), message, "#E53935", null);
                    deleteImageFromCloudinary(newImageUrl, layoutInflater, context);
                });
    }

    public static void saveMealToFirebase(FirebaseDatabase firebaseDatabase,
                                          LayoutInflater layoutInflater,
                                          FragmentDialogCreateMealBinding binding,
                                          Context context,
                                          String campusId,
                                          String cursusId,
                                          String imageUrl) {

        String mealName = binding.textInputEditTextName.getText().toString();
        String mealDescription = binding.textInputEditTextDescription.getText().toString();
        int mealsQauntity = (int) binding.spinnerQuantity.getItemAtPosition(binding.spinnerQuantity.getSelectedItemPosition());
        String type = (String) binding.spinnerMeals.getItemAtPosition(binding.spinnerMeals.getSelectedItemPosition());

        DatabaseReference mealsRef = firebaseDatabase.getReference("campus")
                .child(campusId)
                .child("cursus")
                .child(cursusId)
                .child("meals");
        // Gerar um ID único para a refeição
        String mealId = mealsRef.push().getKey();

        String dateCreated = DateUtils.getCurrentDate();

        // Criar um objeto Meal
        Meal meal = new Meal(
                mealId,
                mealName,
                mealDescription,
                mealsQauntity,
                type,
                dateCreated,
                imageUrl
        );

        // Salvar os dados da refeição no Firebase Realtime Database
        mealsRef.child(mealId).setValue(meal)
                .addOnSuccessListener(aVoid -> {
                    binding.spinnerQuantity.setSelection(0);
                    binding.textInputEditTextName.setText("");
                    binding.textInputEditTextDescription.setText("");
                    binding.textInputEditTextName.requestFocus();
                    restaureViews(binding);
                    String message = mealName + "\n" + context.getString(R.string.save_meal);
                    Util.showAlertDialogMessage(context, layoutInflater, context.getString(R.string.sucess), message, "#4CAF50", null);
                    try {
                        Notification.sendNotificationForTopic(context, layoutInflater, meal, Integer.parseInt(cursusId));
                    } catch (IOException e) {
                        Toast.makeText(context, R.string.error_send_notification, Toast.LENGTH_LONG).show();
                        e.printStackTrace();
                    }
                })
                .addOnFailureListener(e -> {
                    restaureViews(binding);
                    String message = mealName + "\n" + context.getString(R.string.error_save_meal) + ": " + e.getMessage();
                    Util.showAlertDialogMessage(context, layoutInflater, context.getString(R.string.err), message, "#E53935", null);
                    deleteImageFromCloudinary(imageUrl, layoutInflater, context);
                });
    }

    public static void setupVisibility(FragmentMealBinding binding, int viewP, boolean refreshing, int viewT, int viewR) {
        binding.progressBarMeal.setVisibility(viewP);
        binding.swipeRefreshLayout.setRefreshing(refreshing);
        binding.textViewNotFoundMeals.setVisibility(viewT);
        binding.recyclerViewMeal.setVisibility(viewR);
    }

    private static void restaureViews(FragmentDialogCreateMealBinding binding) {
        binding.buttonClose.setEnabled(true);
        binding.buttonCreateMeal.setEnabled(true);
        binding.progressBarMeal.setVisibility(View.GONE);
    }

    public static void deleteMealFromFirebase(FirebaseDatabase firebaseDatabase,
                                              LayoutInflater layoutInflater,
                                              Context context,
                                              String campusId,
                                              String cursusId,
                                              String mealId,
                                              String imageUrl) {

        DatabaseReference mealsRef = firebaseDatabase.getReference("campus")
                .child(campusId)
                .child("cursus")
                .child(cursusId)
                .child("meals")
                .child(mealId);

        mealsRef.removeValue()
                .addOnSuccessListener(aVoid -> {
                    deleteImageFromCloudinary(imageUrl, layoutInflater, context);
                    String message = context.getString(R.string.sucess_meal_delete);
                    Util.showAlertDialogMessage(context, layoutInflater, context.getString(R.string.sucess), message, "#4CAF50", null);
                })
                .addOnFailureListener(e -> {
                    String message = context.getString(R.string.error_meal_delete) + e.getMessage();
                    Util.showAlertDialogMessage(context, layoutInflater, context.getString(R.string.err), message, "#E53935", null);
                });
    }

    private static final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private static final Handler hendler = new Handler(Looper.getMainLooper());

    private static void deleteImageFromCloudinary(String imageUrl, LayoutInflater layoutInflater, Context context) {
        String publicId = extractPublicIdFromUrl(imageUrl);
        if (publicId == null) return;
        executorService.execute(() -> {
            try {
                Map result = MediaManager.get().getCloudinary()
                        .uploader()
                        .destroy(publicId, ObjectUtils.asMap("invalidate", true));
                hendler.post(() -> {
                    if (!"ok".equals(result.get("result"))) {
                        String message = context.getString(R.string.error_delete_image);
                        Util.showAlertDialogMessage(context, layoutInflater, context.getString(R.string.err), message, "#E53935", null);
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public static String extractPublicIdFromUrl(String imageUrl) {
        if (imageUrl == null || imageUrl.isEmpty())
            return null;

        String[] parts = imageUrl.split("/");
        String lastPart = parts[parts.length - 1];
        return lastPart.replaceAll("\\.[a-zA-Z0-9]+$", "");
    }

//    public static String extractPublicIdFromUrlJPG(String imageUrl) {
//        if (imageUrl.contains(".jpg")) {
//            String[] parts = imageUrl.split("/");
//            return parts[parts.length - 1].replace(".jpg", ""); // Adiciona o prefixo da pasta (se houver)
//        }
//        return null;
//    }
}