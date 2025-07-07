package com.antonioteca.cc42.dao.daofarebase;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;

import com.antonioteca.cc42.R;
import com.antonioteca.cc42.model.MealQrCode;
import com.antonioteca.cc42.utility.Util;
import com.antonioteca.cc42.viewmodel.SharedViewModel;
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
            FirebaseDatabase firebaseDatabase,
            List<MealQrCode> listMealQrCode,
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
                .child(mealId == null ? listMealQrCode.get(0).id() : mealId)
                .child("subscriptions");
        // Verifica se o usuário já assinou
        subscriptionsRef.child(String.valueOf(userId)).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Util.setInvisibleProgressBar(progressBarSubscription, sharedViewModel);
                    String message = displayName + "\n" + context.getString(R.string.msg_you_already_subscription);
                    Util.showAlertDialogMessage(context, layoutInflater, context.getString(R.string.warning), message, "#FDD835", urlImageUser, runnableResumeCamera);
                } else {
                    Map<String, Object> update = new HashMap<>();
                    if (mealId == null)
                        for (MealQrCode mealQrCode : listMealQrCode)
                            update.put("cursus/" + cursusId + "/meals/" + mealQrCode.id() + "/subscriptions/" + userId, true);
                    else
                        update.put("cursus/" + cursusId + "/meals/" + mealId + "/subscriptions/" + userId, true);

                    DatabaseReference campusReference = firebaseDatabase.getReference("campus")
                            .child(campusId);

                    campusReference.updateChildren(update)
                            .addOnSuccessListener(aVoid -> {
                                if (userStaffId != null)
                                    Util.sendInfoTmpUserEventMeal(userStaffId, firebaseDatabase, campusId, cursusId, displayName, urlImageUser);
                                sharedViewModel.setUserIdLiveData(Long.valueOf(userId));
                                Util.setInvisibleProgressBar(progressBarSubscription, sharedViewModel);
                                String message = displayName + "\n" + context.getString(R.string.msg_sucess_subscription);
                                Util.showAlertDialogMessage(context, layoutInflater, context.getString(R.string.sucess), message, "#4CAF50", urlImageUser, runnableResumeCamera);
                            })
                            .addOnFailureListener(e -> {
                                Util.setInvisibleProgressBar(progressBarSubscription, sharedViewModel);
                                String message = context.getString(R.string.msg_error_subscription) + ": " + e.getMessage();
                                Util.showAlertDialogMessage(context, layoutInflater, context.getString(R.string.err), message, "#E53935", urlImageUser, runnableResumeCamera);
                            });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Util.setInvisibleProgressBar(progressBarSubscription, sharedViewModel);
                String message = context.getString(R.string.msg_error_check_subscription) + ": " + error.toException();
                Util.showAlertDialogMessage(context, layoutInflater, context.getString(R.string.err), message, "#E53935", urlImageUser, runnableResumeCamera);
            }
        });
    }
}
