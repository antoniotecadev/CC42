package com.antonioteca.cc42.dao.daofarebase;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
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
            @NonNull FirebaseDatabase firebaseDatabase,
            List<MealQrCode> listMealQrCode,
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
                .child(mealId == null ? listMealQrCode.get(0).id() : mealId)
                .child("subscriptions");

        String uid = portionSelected == null ? userId : portionSelected + userId;
        // Verifica se o usuário já assinou
        subscriptionsRef.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    progressBarSubscription.setVisibility(View.GONE);
                    String message = displayName + "\n" + context.getString(R.string.msg_you_already_subscription);
                    Util.showAlertDialogMessage(context, layoutInflater, context.getString(R.string.warning), message, "#FDD835", urlImageUser, runnableResumeCamera);
                } else {
                    Map<String, Object> update = new HashMap<>();
                    update.put("cursus/" + cursusId + "/meals/" + mealId + "/subscriptions/" + uid, true);
//                    if (mealId == null)
//                        for (MealQrCode mealQrCode : listMealQrCode)
//                            update.put("cursus/" + cursusId + "/meals/" + mealQrCode.id() + "/subscriptions/" + userId, true);
//                    else {
//                        String uid = portionSelected == null ? userId : portionSelected + userId;
//                        update.put("cursus/" + cursusId + "/meals/" + mealId + "/subscriptions/" + uid, true);
//                    }
                    DatabaseReference campusReference = firebaseDatabase.getReference("campus")
                            .child(campusId);

                    campusReference.updateChildren(update)
                            .addOnSuccessListener(aVoid -> {
                                if (userStaffId != null)
                                    Util.sendInfoTmpUserEventMeal(userStaffId, firebaseDatabase, campusId, cursusId, displayName, urlImageUser);
                                progressBarSubscription.setVisibility(View.GONE);
                                if (portionSelected == null)
                                    sharedViewModel.setUserIdLiveData(Long.valueOf(userId));
                                String message = displayName + "\n" + context.getString(R.string.msg_sucess_subscription);
                                Util.showAlertDialogMessage(context, layoutInflater, context.getString(R.string.sucess), message, "#4CAF50", urlImageUser, runnableResumeCamera);
                            })
                            .addOnFailureListener(e -> {
                                progressBarSubscription.setVisibility(View.GONE);
                                String message = context.getString(R.string.msg_error_subscription) + ": " + e.getMessage();
                                Util.showAlertDialogMessage(context, layoutInflater, context.getString(R.string.err), message, "#E53935", urlImageUser, runnableResumeCamera);
                            });
                }
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
