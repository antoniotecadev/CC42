package com.antonioteca.cc42.viewmodel;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
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
import com.antonioteca.cc42.utility.EventObserver;
import com.antonioteca.cc42.utility.Loading;
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
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MealViewModel extends ViewModel {

    public List<Meal> mealList = new ArrayList<>();
    private DatabaseReference mealRef;
    private ValueEventListener valueEventListener;
    private MutableLiveData<Meal> createdMealMutableLiveData;
    private MutableLiveData<Meal> updatedMealMutableLiveData;
    private MutableLiveData<EventObserver<Meal>> deleteMealMutableLiveData;
    private MutableLiveData<List<Meal>> mealListMutableLiveData;
    private MutableLiveData<List<String>> pathImageMutableLiveData;
    private MutableLiveData<List<Object>> ratingValuesMutableLiveData;

    public LiveData<Meal> getCreatedMealLiveData() {
        if (createdMealMutableLiveData == null)
            createdMealMutableLiveData = new MutableLiveData<>();
        return createdMealMutableLiveData;
    }

    public LiveData<Meal> getUpdatedMealLiveData() {
        if (updatedMealMutableLiveData == null)
            updatedMealMutableLiveData = new MutableLiveData<>();
        return updatedMealMutableLiveData;
    }

    public LiveData<EventObserver<Meal>> getDeleteMealLiveData() {
        if (deleteMealMutableLiveData == null)
            deleteMealMutableLiveData = new MutableLiveData<>();
        return deleteMealMutableLiveData;
    }

    public LiveData<List<String>> getPathImageLiveData() {
        if (pathImageMutableLiveData == null)
            pathImageMutableLiveData = new MutableLiveData<>();
        return pathImageMutableLiveData;
    }

    public LiveData<List<Object>> getRatingValuesLiveData(Context context, FirebaseDatabase firebaseDatabase, String campusId, String cursusId, String mealId) {
        if (ratingValuesMutableLiveData == null) {
            getRateMeal(context, firebaseDatabase, campusId, cursusId, mealId);
            ratingValuesMutableLiveData = new MutableLiveData<>();
        }
        return ratingValuesMutableLiveData;
    }

    public LiveData<List<Meal>> getMealList(Context context, FragmentMealBinding binding, DatabaseReference mealsRef, String startAtKey) {
        if (mealListMutableLiveData == null) {
            mealListMutableLiveData = new MutableLiveData<>();
            binding.progressBarMeal.setVisibility(View.VISIBLE);
            loadMeals(context, binding, mealsRef, startAtKey);
        } else if (mealListMutableLiveData.getValue() != null && !this.mealList.isEmpty()) {
            mealListMutableLiveData.getValue().clear();
            List<Meal> mealList = new ArrayList<>(this.mealList);
            this.mealList.clear();
            mealListMutableLiveData.postValue(mealList);
        }
        return mealListMutableLiveData;
    }

    public void loadMeals(Context context, FragmentMealBinding binding, @NonNull DatabaseReference mealsRef, String startAtKey) {
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
                               Meal meal,
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
                            pathImageMutableLiveData.setValue(Arrays.asList(mealId, newImageUrl));
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
                                    meal,
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
                                         Meal meal,
                                         String updatedBy,
                                         String campusId,
                                         String cursusId,
                                         String mealId,
                                         String newImageUrl,
                                         int mealsQuantity
    ) {

        String mealName = binding.nameEditText.getText().toString();
        String mealDescription = binding.descriptionEditText.getText().toString();

        DatabaseReference mealsRef = firebaseDatabase.getReference("campus")
                .child(campusId)
                .child("cursus")
                .child(cursusId)
                .child("meals")
                .child(mealId);

        String updatedDate = DateUtils.getCurrentDate();

        // Criar um mapa com os novos valores
        Map<String, Object> updates = new HashMap<>();
        updates.put("name", mealName);
        updates.put("description", mealDescription);
        updates.put("quantity", mealsQuantity);
        updates.put("updatedBy", updatedBy);
        updates.put("updatedDate", updatedDate);
        if (newImageUrl != null) {
            meal.setPathImage(newImageUrl);
            updates.put("pathImage", newImageUrl);
        }
        meal.setName(mealName);
        meal.setDescription(mealDescription);
        meal.setQuantity(mealsQuantity);

        // Atualizar os dados
        mealsRef.updateChildren(updates)
                .addOnSuccessListener(aVoid -> {
                    updatedMealMutableLiveData.setValue(meal);
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

        String mealName = binding.nameEditText.getText().toString();
        String mealDescription = binding.descriptionEditText.getText().toString();
        String type = MealsUtils.getMealType(context);

        DatabaseReference mealsRef = firebaseDatabase.getReference("campus")
                .child(campusId)
                .child("cursus")
                .child(cursusId)
                .child("meals");
        // Gerar um ID único para a refeição
        String mealId = mealsRef.push().getKey();

        String createdDate = DateUtils.getCurrentDate();

        // Criar um objeto Meal
        Meal meal = new Meal(
                mealId,
                type,
                mealName,
                mealDescription,
                mealsQuantity,
                imageUrl,
                0,
                createdBy,
                createdDate
        );
        // Salvar os dados da refeição no Firebase Realtime Database
        mealsRef.child(Objects.requireNonNull(mealId)).setValue(meal)
                .addOnSuccessListener(aVoid -> {
                    createdMealMutableLiveData.setValue(meal);
                    binding.chipContainer.removeAllViews();
                    binding.nameEditText.setText("");
                    binding.descriptionEditText.setText("");
                    binding.quantityEditText.setText("0");
                    restaureViews(binding);
                    String message = mealName + "\n" + context.getString(R.string.save_meal);
                    Util.showAlertDialogMessage(context, layoutInflater, context.getString(R.string.sucess), message, "#4CAF50", null);
                    try {
                        Notification.sendNotificationForTopic(context, layoutInflater, meal, Integer.parseInt(campusId), Integer.parseInt(cursusId));
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
                                       Meal meal) {

        DatabaseReference mealsRef = firebaseDatabase.getReference("campus")
                .child(campusId)
                .child("cursus")
                .child(cursusId)
                .child("meals")
                .child(meal.getId());

        mealsRef.removeValue()
                .addOnSuccessListener(aVoid -> {
                    deleteMealMutableLiveData.setValue(new EventObserver<>(meal));
                    String message = context.getString(R.string.sucess_meal_delete);
                    deleteImageFromCloudinary(meal.getPathImage(), layoutInflater, context);
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

    public void rateMeal(
            Context context,
            @NonNull FirebaseDatabase firebaseDatabase,
            Loading loading,
            ProgressBar progressBarMeal,
            String campusId,
            String cursusId,
            String mealId,
            String userId,
            int rating
    ) {
        // Referência para a refeição específica
        DatabaseReference mealRef = firebaseDatabase.getReference("campus")
                .child(campusId)
                .child("cursus")
                .child(cursusId)
                .child("meals")
                .child(mealId);

        mealRef.child("ratings").child(userId).setValue(rating)
                .addOnSuccessListener(aVoid -> {
                    loading.isLoading = false;
                    progressBarMeal.setVisibility(View.INVISIBLE);
                    Util.showAlertDialogMessage(context, LayoutInflater.from(context), "" + rating, context.getString(R.string.rating_submitted_successfully), "#4CAF50", null);
                })
                .addOnFailureListener(e -> {
                    loading.isLoading = false;
                    progressBarMeal.setVisibility(View.INVISIBLE);
                    Util.showAlertDialogMessage(context, LayoutInflater.from(context), context.getString(R.string.err), e.getMessage(), "#E53935", null);
                });
    }

    public void getRateMeal(
            Context context,
            FirebaseDatabase firebaseDatabase,
            String campusId,
            String cursusId,
            String mealId
    ) {
        // Referência para a refeição específica
        mealRef = firebaseDatabase.getReference("campus")
                .child(campusId)
                .child("cursus")
                .child(cursusId)
                .child("meals")
                .child(mealId);

        valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    int totalRatings = 0;
                    int numberOfRatings = 0;
                    // HashMap to store the count of each rating (ratings 1-5)
                    HashMap<Integer, Integer> ratingCounts = new HashMap<>();
                    HashMap<String, Integer> ratingValuesUsers = new HashMap<>();
                    for (int i = 1; i <= 5; i++)
                        ratingCounts.put(i, 0);
                    // Soma todas as avaliações e conta o número de avaliações
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        Integer rating = dataSnapshot.getValue(Integer.class);
                        if (rating == null || rating < 1 || rating > 5) continue;
                        totalRatings += rating;
                        numberOfRatings++;
                        // Increment the count for the specific rating
                        Integer count = ratingCounts.get(rating);
                        if (count == null) count = 0;
                        ratingCounts.put(rating, count + 1);
                        ratingValuesUsers.put(dataSnapshot.getKey(), rating);
                    }
                    // Calcula a média das avaliações
                    double averageRating = (double) totalRatings / numberOfRatings;
                    // Arredonda a média para o número inteiro mais próximo
                    int roundedRating = (int) Math.round(averageRating);
                    // Formata a média para uma casa decimal
                    DecimalFormat decimalFormat = new DecimalFormat("#.0");
                    decimalFormat.setRoundingMode(RoundingMode.HALF_UP);
                    String formattedAverage = decimalFormat.format(averageRating);
                    ratingValuesMutableLiveData.setValue(Arrays.asList(roundedRating, formattedAverage, ratingCounts, numberOfRatings, ratingValuesUsers));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Util.showAlertDialogMessage(context, LayoutInflater.from(context), context.getString(R.string.err), error.getMessage(), "#E53935", null);
            }
        };
        mealRef.child("ratings").addValueEventListener(valueEventListener);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (mealRef != null && valueEventListener != null)
            mealRef.removeEventListener(valueEventListener);
    }
}