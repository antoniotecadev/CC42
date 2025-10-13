package com.antonioteca.cc42.viewmodel;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.antonioteca.cc42.R;
import com.antonioteca.cc42.model.Comment;
import com.antonioteca.cc42.model.Meal;
import com.antonioteca.cc42.utility.EventObserver;
import com.antonioteca.cc42.utility.Loading;
import com.antonioteca.cc42.utility.Util;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;
import java.util.Objects;

public class SharedViewModel extends ViewModel {

    private MutableLiveData<Boolean> reset;
    private MutableLiveData<Comment> commentMutableLiveData;
    private final MutableLiveData<EventObserver<Long>> longMutableLiveData = new MutableLiveData<>();
    //    private final MutableLiveData<EventObserver<String>> faceIDMutableLiveData = new MutableLiveData<>();
    private final MutableLiveData<EventObserver<Meal>> newMeal = new MutableLiveData<>();
    private final MutableLiveData<EventObserver<Meal>> updatedMeal = new MutableLiveData<>();
    private final MutableLiveData<Boolean> disabledMutableLiveData = new MutableLiveData<>();
    private final MutableLiveData<EventObserver<List<String>>> updatePathImage = new MutableLiveData<>();
    //    private final MutableLiveData<Boolean> faceIDContinueCaptureMutableLiveData = new MutableLiveData<>();

    public MutableLiveData<EventObserver<Meal>> getUpdatedMealLiveData() {
        return updatedMeal;
    }

    public void setUpdatedMeal(Meal meal) {
        updatedMeal.setValue(new EventObserver<>(meal));
    }

    public MutableLiveData<EventObserver<Meal>> getNewMealLiveData() {
        return newMeal;
    }

    public void setNewMeal(Meal meal) {
        newMeal.setValue(new EventObserver<>(meal));
    }

    public LiveData<EventObserver<List<String>>> getPathImageLiveData() {
        return updatePathImage;
    }

    public void setUpdatedPathImage(List<String> imageUrl) {
        updatePathImage.setValue(new EventObserver<>(imageUrl));
    }

    public LiveData<Boolean> disabledRecyclerView() {
        return disabledMutableLiveData;
    }

    public LiveData<EventObserver<Long>> getUserIdLiveData() {
        return longMutableLiveData;
    }

    public void setUserIdLiveData(Long userId) {
        longMutableLiveData.setValue(new EventObserver<>(userId));
    }

//    public LiveData<EventObserver<String>> getUserFaceIdLiveData() {
//        return faceIDMutableLiveData;
//    }

//    public void setUserFaceIdLiveData(String userId) {
//        faceIDMutableLiveData.setValue(new EventObserver<>(userId));
//    }

//    public LiveData<Boolean> getUserFaceIdContinueCaptureLiveData() {
//        return faceIDContinueCaptureMutableLiveData;
//    }

//    public void setUserFaceIdContinueCaptureLiveData(Boolean continueCaptureFaceID) {
//        faceIDContinueCaptureMutableLiveData.setValue(continueCaptureFaceID);
//    }

    public LiveData<Comment> getCommentLiveData(Context context, @NonNull FirebaseDatabase firebaseDatabase, String type, String typeId, String campusId, String cursusId, String userId) {
        commentMutableLiveData = new MutableLiveData<>();
        getComment(context, firebaseDatabase, type, typeId, campusId, cursusId, userId);
        return commentMutableLiveData;
    }

    public LiveData<Boolean> getResetLiveData() {
        reset = new MutableLiveData<>();
        return reset;
    }

    public void sendComment(Context context, @NonNull FirebaseDatabase firebaseDatabase, String type, String typeId, String campusId, String cursusId, String userId, Button buttonSendComment, @NonNull TextInputLayout commentInputLayout, boolean isAnonymous, ProgressBar progressBar) {
        String comment = Objects.requireNonNull(commentInputLayout.getEditText()).getText().toString().trim();
        if (comment.isEmpty()) {
            commentInputLayout.setFocusable(true);
            commentInputLayout.setErrorEnabled(true);
            commentInputLayout.setError(context.getString(R.string.comment_required));
            return;
        } else {
            commentInputLayout.setErrorEnabled(false);
            commentInputLayout.setFocusable(false);
            commentInputLayout.setError(null);
        }

        buttonSendComment.setEnabled(false);
        progressBar.setVisibility(View.VISIBLE);
        // Enviar comentário para a refeição
        DatabaseReference commentsRef = firebaseDatabase.getReference("campus")
                .child(campusId)
                .child("cursus")
                .child(cursusId)
                .child(type)
                .child(typeId)
                .child("comments");

        Comment cmt = new Comment();
        cmt.setComment(comment);
        cmt.setAnonymous(isAnonymous);
        commentsRef.child(userId).setValue(cmt).addOnSuccessListener(aVoid -> {
            buttonSendComment.setEnabled(true);
            commentMutableLiveData.setValue(cmt);
            progressBar.setVisibility(View.GONE);
            String message = context.getString(R.string.comment_sent_successfully);
            Toast.makeText(context, message, Toast.LENGTH_LONG).show();
        }).addOnFailureListener(e -> {
            buttonSendComment.setEnabled(true);
            progressBar.setVisibility(View.GONE);
            String message = context.getString(R.string.error_send_comment) + ": " + e.getMessage();
            Util.showAlertDialogBuild(context.getString(R.string.err), message, context, null);
        });
    }

    private void getComment(Context context, @NonNull FirebaseDatabase firebaseDatabase, String type, String typeId, String campusId, String cursusId, String userId) {

        // Referência para a refeição específica
        DatabaseReference commentsRef = firebaseDatabase.getReference("campus")
                .child(campusId)
                .child("cursus")
                .child(cursusId)
                .child(type)
                .child(typeId)
                .child("comments");

        commentsRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Comment comment = snapshot.getValue(Comment.class);
                    commentMutableLiveData.setValue(comment);
                } else
                    commentMutableLiveData.setValue(null);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                String message = context.getString(R.string.error_get_comment) + ": " + error.toException();
                Util.showAlertDialogBuild(context.getString(R.string.err), message, context, null);
            }
        });
    }

    public void rate(
            Context context,
            @NonNull FirebaseDatabase firebaseDatabase,
            Loading loading,
            ProgressBar progressBar,
            String campusId,
            String cursusId,
            String type,
            String typeId,
            String userId,
            int rating
    ) {
        // Referência para a refeição específica
        DatabaseReference mealRef = firebaseDatabase.getReference("campus")
                .child(campusId)
                .child("cursus")
                .child(cursusId)
                .child(type)
                .child(typeId);

        mealRef.child("ratings").child(userId).setValue(rating)
                .addOnSuccessListener(aVoid -> {
                    reset.setValue(true);
                    loading.isLoading = false;
                    progressBar.setVisibility(View.INVISIBLE);
                    Util.showAlertDialogMessage(context, LayoutInflater.from(context), "" + rating, context.getString(R.string.rating_submitted_successfully), "#4CAF50", null, null);
                })
                .addOnFailureListener(e -> {
                    loading.isLoading = false;
                    progressBar.setVisibility(View.INVISIBLE);
                    Util.showAlertDialogMessage(context, LayoutInflater.from(context), context.getString(R.string.err), e.getMessage(), "#E53935", null, null);
                });
    }
}
