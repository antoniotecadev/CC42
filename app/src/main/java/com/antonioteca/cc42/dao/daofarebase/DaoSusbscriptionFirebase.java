package com.antonioteca.cc42.dao.daofarebase;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;

import com.antonioteca.cc42.R;
import com.antonioteca.cc42.utility.Util;
import com.antonioteca.cc42.viewmodel.SharedViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class DaoSusbscriptionFirebase {

    // Assinar
    public static void subscription(
            FirebaseDatabase firebaseDatabase,
            String mealId,
            String userId,
            String userLogin,
            String displayName,
            String cursusId,
            String campusId,
            Context context,
            LayoutInflater layoutInflater,
            ProgressBar progressBarMarkAttendance,
            FloatingActionButton fabOpenCameraScannerQrCode,
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
        // Verifica se o usuário já assinou
        subscriptionsRef.child(String.valueOf(userId)).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Util.setInvisibleProgressBar(progressBarMarkAttendance, fabOpenCameraScannerQrCode, sharedViewModel);
                    String message = displayName + "\n" + context.getString(R.string.msg_you_already_subscription);
                    Util.showAlertDialogMessage(context, layoutInflater, context.getString(R.string.warning), message, "#FDD835", runnableResumeCamera);
                } else {
                    Map<String, Object> update = new HashMap<>();
                    update.put("cursus/" + cursusId + "/meals/" + mealId + "/subscriptions/" + userId, true);

                    DatabaseReference campusReference = firebaseDatabase.getReference("campus")
                            .child(campusId);

                    campusReference.updateChildren(update)
                            .addOnSuccessListener(aVoid -> {
                                sharedViewModel.setUserIdLiveData(Long.valueOf(userId));
                                Util.setInvisibleProgressBar(progressBarMarkAttendance, fabOpenCameraScannerQrCode, sharedViewModel);
                                String message = displayName + "\n" + context.getString(R.string.msg_sucess_subscription);
                                Util.showAlertDialogMessage(context, layoutInflater, context.getString(R.string.sucess), message, "#4CAF50", runnableResumeCamera);
                            })
                            .addOnFailureListener(e -> {
                                Util.setInvisibleProgressBar(progressBarMarkAttendance, fabOpenCameraScannerQrCode, sharedViewModel);
                                String message = context.getString(R.string.msg_error_subscription) + ": " + e.getMessage();
                                Util.showAlertDialogMessage(context, layoutInflater, context.getString(R.string.err), message, "#E53935", runnableResumeCamera);
                            });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Util.setInvisibleProgressBar(progressBarMarkAttendance, fabOpenCameraScannerQrCode, sharedViewModel);
                String message = context.getString(R.string.msg_error_check_subscription) + ": " + error.toException();
                Util.showAlertDialogMessage(context, layoutInflater, context.getString(R.string.err), message, "#E53935", runnableResumeCamera);
            }
        });
    }
}
