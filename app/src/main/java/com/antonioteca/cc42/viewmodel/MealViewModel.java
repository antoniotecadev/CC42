package com.antonioteca.cc42.viewmodel;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.antonioteca.cc42.R;
import com.antonioteca.cc42.databinding.FragmentDialogCreateMealBinding;
import com.antonioteca.cc42.databinding.FragmentMealBinding;
import com.antonioteca.cc42.model.Meal;
import com.antonioteca.cc42.network.NotificationFirebase.Notification;
import com.antonioteca.cc42.utility.DateUtils;
import com.antonioteca.cc42.utility.MealsUtils;
import com.antonioteca.cc42.utility.Util;
import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.cloudinary.utils.ObjectUtils;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MealViewModel extends ViewModel {

    private DatabaseReference mealsRef;
    private ValueEventListener valueEventListener;
    private MutableLiveData<Meal> createdMealMutableLiveData;
    private MutableLiveData<List<Meal>> mealListMutableLiveData;

    public LiveData<Meal> getCreatedMealLiveData() {
        if (createdMealMutableLiveData == null)
            createdMealMutableLiveData = new MutableLiveData<>();
        return createdMealMutableLiveData;
    }

    public LiveData<List<Meal>> getMealList(Context context, FragmentMealBinding binding, DatabaseReference mealsRef, String startAtKey) {
        if (mealListMutableLiveData == null) {
            mealListMutableLiveData = new MutableLiveData<>();
            binding.progressBarMeal.setVisibility(View.VISIBLE);
            loadMeals(context, binding, mealsRef, startAtKey);
        }
        return mealListMutableLiveData;
    }

    public void loadMeals(Context context, FragmentMealBinding binding, @NonNull DatabaseReference mealsRef, String startAtKey) {
        this.mealsRef = mealsRef;
        Query query = mealsRef.orderByKey();
        if (startAtKey != null) {
            query = query.endBefore(startAtKey).limitToLast(15);
        } else {
            query = query.limitToLast(15);
        }
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Meal> mealList = new ArrayList<>();
                if (snapshot.exists()) {
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        Meal meal = dataSnapshot.getValue(Meal.class);
                        mealList.add(meal);
                    }
                    Collections.reverse(mealList);
                    MealsUtils.setupVisibility(binding, View.INVISIBLE, false, View.INVISIBLE, View.VISIBLE);
                } else if (mealListMutableLiveData.getValue() == null) {
                    MealsUtils.setupVisibility(binding, View.INVISIBLE, false, View.VISIBLE, View.INVISIBLE);
                    String message = context.getString(R.string.meals_not_found);
                    Util.showAlertDialogBuild(context.getString(R.string.warning), message, context, () -> {
                        MealsUtils.setupVisibility(binding, View.INVISIBLE, true, View.INVISIBLE, View.INVISIBLE);
                        loadMeals(context, binding, mealsRef, startAtKey);
                    });
                }
                mealListMutableLiveData.setValue(mealList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (mealListMutableLiveData.getValue() == null) {
                    String message = context.getString(R.string.error_load_data) + ": " + error.getMessage();
                    Util.showAlertDialogBuild(context.getString(R.string.err), message, context, () -> {
                        MealsUtils.setupVisibility(binding, View.INVISIBLE, true, View.INVISIBLE, View.INVISIBLE);
                        loadMeals(context, binding, mealsRef, startAtKey);
                    });
                }
                mealListMutableLiveData.setValue(new ArrayList<>());
            }
        });
    }

    public void uploadImageToCloudinary(FirebaseDatabase firebaseDatabase,
                                        LayoutInflater layoutInflater,
                                        FragmentDialogCreateMealBinding binding,
                                        Context context,
                                        String createdBy,
                                        String campusId,
                                        String cursusId,
                                        Uri imageUri,
                                        int mealsQuantity
    ) {

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
                                createdBy,
                                campusId,
                                cursusId,
                                imageUrl,
                                mealsQuantity
                        );
                    }

                    @Override
                    public void onError(String requestId, ErrorInfo error) {
                        String message = context.getString(R.string.error_save_image) + ": " + error.getDescription();
                        Util.showAlertDialogMessage(context, layoutInflater, context.getString(R.string.err), message, "#E53935", null);
                        saveMealToFirebase(firebaseDatabase,
                                layoutInflater,
                                binding,
                                context,
                                createdBy,
                                campusId,
                                cursusId,
                                "",
                                mealsQuantity);
                    }

                    @Override
                    public void onReschedule(String requestId, ErrorInfo error) {
                        // Tentar novamente em caso de falha
                    }
                }).dispatch();
    }

    public void uploadNewImage(FirebaseDatabase firebaseDatabase,
                               LayoutInflater layoutInflater,
                               FragmentDialogCreateMealBinding binding,
                               Context context,
                               String updatedBy,
                               String campusId,
                               String cursusId,
                               String mealId,
                               Uri newImageUri,
                               String publicId,
                               boolean onlyImage,
                               int mealsQuantity
    ) {

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
                                    updatedBy,
                                    campusId,
                                    cursusId,
                                    mealId,
                                    newImageUrl,
                                    mealsQuantity); // Actualizar todos os dados no Firebase
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

    private void updateImageUrlInFirebase(@NonNull FirebaseDatabase firebaseDatabase,
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

    public void updateMealDataInFirebase(@NonNull FirebaseDatabase firebaseDatabase,
                                         LayoutInflater layoutInflater,
                                         @NonNull FragmentDialogCreateMealBinding binding,
                                         Context context,
                                         String updatedBy,
                                         String campusId,
                                         String cursusId,
                                         String mealId,
                                         String newImageUrl,
                                         int mealsQuantity
    ) {

        String mealName = binding.mealsEditText.getText().toString();

        DatabaseReference mealsRef = firebaseDatabase.getReference("campus")
                .child(campusId)
                .child("cursus")
                .child(cursusId)
                .child("meals")
                .child(mealId);

        String updatedData = DateUtils.getCurrentDate();

        // Criar um mapa com os novos valores
        Map<String, Object> updates = new HashMap<>();
        updates.put("name", mealName);
        updates.put("quantity", mealsQuantity);
        if (newImageUrl != null)
            updates.put("pathImage", newImageUrl);
        updates.put("updatedBy", updatedBy);
        updates.put("updatedData", updatedData);

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

    public void saveMealToFirebase(@NonNull FirebaseDatabase firebaseDatabase,
                                   LayoutInflater layoutInflater,
                                   @NonNull FragmentDialogCreateMealBinding binding,
                                   Context context,
                                   String createdBy,
                                   String campusId,
                                   String cursusId,
                                   String imageUrl,
                                   int mealsQuantity
    ) {

        String mealName = binding.mealsEditText.getText().toString();
        String type = MealsUtils.getMealType(context);

        DatabaseReference mealsRef = firebaseDatabase.getReference("campus")
                .child(campusId)
                .child("cursus")
                .child(cursusId)
                .child("meals");
        // Gerar um ID único para a refeição
        String mealId = mealsRef.push().getKey();

        String createdData = DateUtils.getCurrentDate();

        // Criar um objeto Meal
        Meal meal = new Meal(
                mealId,
                type,
                mealName,
                mealsQuantity,
                imageUrl,
                createdBy,
                createdData
        );

        // Salvar os dados da refeição no Firebase Realtime Database
        mealsRef.child(Objects.requireNonNull(mealId)).setValue(meal)
                .addOnSuccessListener(aVoid -> {
                    createdMealMutableLiveData.setValue(meal);
                    binding.chipContainer.removeAllViews();
                    binding.mealsEditText.setText("");
                    binding.quantityEditText.setText("0");
                    restaureViews(binding);
                    String message = mealName + "\n" + context.getString(R.string.save_meal);
                    Util.showAlertDialogMessage(context, layoutInflater, context.getString(R.string.sucess), message, "#4CAF50", null);
                    try {
                        Notification.sendNotificationForTopic(context, layoutInflater, meal, Integer.parseInt(cursusId));
                    } catch (IOException e) {
                        Toast.makeText(context, R.string.error_send_notification, Toast.LENGTH_LONG).show();
                    }
                })
                .addOnFailureListener(e -> {
                    restaureViews(binding);
                    String message = mealName + "\n" + context.getString(R.string.error_save_meal) + ": " + e.getMessage();
                    Util.showAlertDialogMessage(context, layoutInflater, context.getString(R.string.err), message, "#E53935", null);
                    deleteImageFromCloudinary(imageUrl, layoutInflater, context);
                });
    }

    private void restaureViews(@NonNull FragmentDialogCreateMealBinding binding) {
        binding.buttonClose.setEnabled(true);
        binding.buttonCreateMeal.setEnabled(true);
        binding.progressBarMeal.setVisibility(View.GONE);
    }

    public void deleteMealFromFirebase(@NonNull FirebaseDatabase firebaseDatabase,
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

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final Handler hendler = new Handler(Looper.getMainLooper());

    private void deleteImageFromCloudinary(String imageUrl, LayoutInflater layoutInflater, Context context) {
        String publicId = extractPublicIdFromUrl(imageUrl);
        if (publicId == null) return;
        executorService.execute(() -> {
            try {
                Map<?, ?> result = MediaManager.get().getCloudinary()
                        .uploader()
                        .destroy(publicId, ObjectUtils.asMap("invalidate", true));
                hendler.post(() -> {
                    if (!"ok".equals(result.get("result"))) {
                        String message = context.getString(R.string.error_delete_image);
                        Util.showAlertDialogMessage(context, layoutInflater, context.getString(R.string.err), message, "#E53935", null);
                    }
                });
            } catch (IOException e) {
                Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
            } finally {
                executorService.shutdown();
            }
        });
    }

    public String extractPublicIdFromUrl(String imageUrl) {
        if (imageUrl == null || imageUrl.isEmpty())
            return null;

        String[] parts = imageUrl.split("/");
        String lastPart = parts[parts.length - 1];
        return lastPart.replaceAll("\\.[a-zA-Z0-9]+$", "");
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (mealsRef != null && valueEventListener != null)
            mealsRef.removeEventListener(valueEventListener);
    }
}