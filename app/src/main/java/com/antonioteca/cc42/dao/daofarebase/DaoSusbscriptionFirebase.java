package com.antonioteca.cc42.dao.daofarebase;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;

import com.antonioteca.cc42.R;
import com.antonioteca.cc42.utility.CustomToastManager;
import com.antonioteca.cc42.utility.Util;
import com.antonioteca.cc42.viewmodel.SharedViewModel;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DaoSusbscriptionFirebase {

    // Assinar
    public static void subscription(
            @NonNull FirebaseDatabase firebaseDatabase,
            Integer portionQuantity,
            boolean checkSubscription,
            String portionSelected,
            String mealId,
            String userStaffId,
            String userId,
            String userLogin,
            String displayName,
            String cursusId,
            String campusId,
            String urlImageUser,
            Context context,
            LayoutInflater layoutInflater,
            ProgressBar progressBarSubscription,
            SharedViewModel sharedViewModel,
            Runnable runnableResumeCamera
    ) {
        DatabaseReference subscriptionsRef = firebaseDatabase.getReference("campus")
                .child(campusId)
                .child("cursus")
                .child(cursusId)
                .child("meals")
                .child(mealId)
                .child("subscriptions");

        if ("both".equals(portionSelected)) {
            Task<DataSnapshot> taskFirstPortion = subscriptionsRef.child(userId).get();
            Task<DataSnapshot> taskSecondPortion = subscriptionsRef.child("-" + userId).get();
            Log.i("TAG_SUBSCRIPTION", "taskFirstPortion: " + taskFirstPortion);
            Log.i("TAG_SUBSCRIPTION", "taskSecondPortion: " + taskSecondPortion);
            Tasks.whenAllSuccess(taskFirstPortion, taskSecondPortion).addOnSuccessListener(new OnSuccessListener<>() {
                @Override
                public void onSuccess(List<Object> dataSnapshots) {
                    DataSnapshot snapshotFirstPortion = (DataSnapshot) dataSnapshots.get(0);
                    DataSnapshot snapshotSecondPortion = (DataSnapshot) dataSnapshots.get(1);

                    boolean hasFirst = snapshotFirstPortion.exists() && Boolean.TRUE.equals(snapshotFirstPortion.child("status").getValue(Boolean.class));
                    boolean hasChildSecondPortion = snapshotSecondPortion.exists();
                    boolean hasSecond = hasChildSecondPortion && Boolean.TRUE.equals(snapshotSecondPortion.child("status").getValue(Boolean.class));
                    if (hasFirst && hasSecond) {
                        progressBarSubscription.setVisibility(View.GONE);
                        String message = displayName + "\n" + context.getString(R.string.msg_you_already_subscription) + " " + context.getString(R.string.first_portion) + " e " + context.getString(R.string.second_portion);
                        Util.showAlertDialogMessage(context, layoutInflater, context.getString(R.string.warning), message, "#FDD835", urlImageUser, runnableResumeCamera);
                    } else if (checkSubscription && !hasChildSecondPortion) {
                        progressBarSubscription.setVisibility(View.GONE);
                        Util.showAlertDialogMessage(context, layoutInflater, context.getString(R.string.warning), displayName + "\n" + context.getString(R.string.second_portion_not_subscribe), "#E53935", urlImageUser, runnableResumeCamera);
                    } else {
                        registerBoth(hasFirst, hasSecond);
                    }
                }

                private void registerBoth(boolean hasFirst, boolean hasSecond) {
                    Map<String, Object> update = getStringObjectMap(hasFirst, hasSecond);

                    firebaseDatabase.getReference("campus").child(campusId).updateChildren(update).addOnSuccessListener(aVoid -> {
                        if (userStaffId != null)
                            Util.sendInfoTmpUserEventMeal(userStaffId, firebaseDatabase, campusId, cursusId, displayName, urlImageUser);
                        progressBarSubscription.setVisibility(View.GONE);
                        sharedViewModel.setUserIdLiveData(Long.valueOf(userId));
                        String message = displayName + "\n" + context.getString(R.string.msg_sucess_subscription);
                        CustomToastManager.showNotification(context, layoutInflater, context.getString(R.string.sucess), message, urlImageUser, () -> Util.showAlertDialogMessage(context, layoutInflater, context.getString(R.string.sucess), message, "#4CAF50", urlImageUser, () -> {
                        }));
                        if (runnableResumeCamera != null) runnableResumeCamera.run();
                    }).addOnFailureListener(e -> {
                        progressBarSubscription.setVisibility(View.GONE);
                        Util.showAlertDialogMessage(context, layoutInflater, context.getString(R.string.err), context.getString(R.string.msg_error_subscription) + ": " + e.getMessage(), "#E53935", urlImageUser, runnableResumeCamera);
                    });
                }

                @NonNull
                private Map<String, Object> getStringObjectMap(boolean hasFirst, boolean hasSecond) {
                    Map<String, Object> update = new HashMap<>();
                    Map<String, Object> updateStatus = new HashMap<>();
                    updateStatus.put("status", true);
                    updateStatus.put("quantity", portionQuantity);
                    updateStatus.put("createdBy", userStaffId);

                    if (!hasFirst)
                        update.put("cursus/" + cursusId + "/meals/" + mealId + "/subscriptions/" + userId, updateStatus);
                    if (!hasSecond)
                        update.put("cursus/" + cursusId + "/meals/" + mealId + "/subscriptions/-" + userId, updateStatus);
                    return update;
                }
            }).addOnFailureListener(e -> {
                progressBarSubscription.setVisibility(View.GONE);
                Util.showAlertDialogMessage(context, layoutInflater, context.getString(R.string.err), context.getString(R.string.msg_error_check_subscription) + ": " + e.getMessage(), "#E53935", urlImageUser, runnableResumeCamera);
            });
            return;
        }

        String uid = portionSelected == null ? userId : portionSelected + userId;
        // Verifica se o usuário já assinou
        subscriptionsRef.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean firstPortion = portionSelected == null;
                if (snapshot.exists()) {
                    boolean isAlreadyReceived = Boolean.TRUE.equals(snapshot.child("status").getValue(Boolean.class));
                    if (isAlreadyReceived) {
                        progressBarSubscription.setVisibility(View.GONE);
                        String message = displayName + "\n" + context.getString(R.string.msg_you_already_subscription) + " " + (firstPortion ? context.getString(R.string.first_portion) : context.getString(R.string.second_portion));
                        Util.showAlertDialogMessage(context, layoutInflater, context.getString(R.string.warning), message, firstPortion ? null : "#FDD835", urlImageUser, firstPortion ? () -> {
                            runnableResumeCamera.run();
                            Util.showAlertDialogBuild(context.getString(R.string.second_portion), null, context, () -> {
                                progressBarSubscription.setVisibility(View.VISIBLE);
                                DaoSusbscriptionFirebase.subscription(firebaseDatabase, portionQuantity, checkSubscription, "-", mealId, userStaffId, userId, userLogin, displayName, cursusId, campusId, urlImageUser, context, layoutInflater, progressBarSubscription, sharedViewModel, runnableResumeCamera);
                            });
                        } : runnableResumeCamera);
                    } else
                        registerSubscription();
                } else {
                    if (firstPortion)
                        registerSubscription();
                    else if (checkSubscription) {
                        progressBarSubscription.setVisibility(View.GONE);
                        Util.showAlertDialogMessage(context, layoutInflater, context.getString(R.string.warning), displayName + "\n" + context.getString(R.string.second_portion_not_subscribe), "#E53935", urlImageUser, runnableResumeCamera);
                    } else
                        registerSubscription();
                }
            }

            private void registerSubscription() {
                Map<String, Object> update = new HashMap<>();
                Map<String, Object> updateStatus = new HashMap<>();
                updateStatus.put("status", true);
                updateStatus.put("quantity", portionQuantity);
                updateStatus.put("createdBy", userStaffId);
                update.put("cursus/" + cursusId + "/meals/" + mealId + "/subscriptions/" + uid, updateStatus);
                DatabaseReference campusReference = firebaseDatabase.getReference("campus")
                        .child(campusId);

                campusReference.updateChildren(update)
                        .addOnSuccessListener(aVoid -> {
                            if (userStaffId != null)
                                Util.sendInfoTmpUserEventMeal(userStaffId, firebaseDatabase, campusId, cursusId, displayName, urlImageUser);
                            progressBarSubscription.setVisibility(View.GONE);
                            sharedViewModel.setUserIdLiveData(Long.valueOf(userId));
                            String message = displayName + "\n" + context.getString(R.string.msg_sucess_subscription);
//                            Util.showAlertDialogMessage(context, layoutInflater, context.getString(R.string.sucess), message, "#4CAF50", urlImageUser, runnableResumeCamera);

                            // Exibe a notificação rápida no topo direito.
                            // Se clicada, ela roda o código interno que abre o AlertDialog completo com foto!
                            CustomToastManager.showNotification(
                                    context,
                                    layoutInflater,
                                    context.getString(R.string.sucess),
                                    message,
                                    urlImageUser,
                                    () -> {
                                        // Isso só roda SE o usuário clicar no Toast antes de sumir
                                        Util.showAlertDialogMessage(context, layoutInflater, context.getString(R.string.sucess), message, "#4CAF50", urlImageUser, () -> {
                                            // Não faz nada ao fechar o diálogo manual, a câmera já foi resumida
                                        });
                                    }
                            );

                            // Importante: Libera a câmera imediatamente sem prender a tela!
                            if (runnableResumeCamera != null) {
                                runnableResumeCamera.run();
                            }
                        })
                        .addOnFailureListener(e -> {
                            progressBarSubscription.setVisibility(View.GONE);
                            String message = context.getString(R.string.msg_error_subscription) + ": " + e.getMessage();
                            Util.showAlertDialogMessage(context, layoutInflater, context.getString(R.string.err), message, "#E53935", urlImageUser, runnableResumeCamera);
                        });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressBarSubscription.setVisibility(View.GONE);
                String message = context.getString(R.string.msg_error_check_subscription) + ": " + error.toException();
                Util.showAlertDialogMessage(context, layoutInflater, context.getString(R.string.err), message, "#E53935", urlImageUser, runnableResumeCamera);
            }
        });
    }
}
