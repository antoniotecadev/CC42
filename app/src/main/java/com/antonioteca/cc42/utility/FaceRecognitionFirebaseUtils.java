package com.antonioteca.cc42.utility;

import android.content.Context;
import android.view.View;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;

import com.antonioteca.cc42.viewmodel.SharedViewModel;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FaceRecognitionFirebaseUtils {
    private final DatabaseReference databaseRef;

    public FaceRecognitionFirebaseUtils(String campusId, String cursusId) {
        databaseRef = FirebaseDatabase.getInstance().getReference("campus")
                .child(campusId)
                .child("cursus")
                .child(cursusId)
                .child("face_embeddings");
    }

    public void saveEmbedding(String userId, float[] embedding, SharedViewModel sharedViewModel, ProgressBar progressBar, Context context) {
        Map<String, Object> data = new HashMap<>();
        data.put("embedding", convertEmbeddingForList(embedding));

        databaseRef.child(userId).setValue(data)
                .addOnSuccessListener(unused -> {
                    progressBar.setVisibility(View.INVISIBLE);
                    Util.showAlertDialogBuild("Sucesso", "Face ID registado com sucesso", context, () ->
                            sharedViewModel.setUserFaceIdContinueCaptureLiveData(true)
                    );
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.INVISIBLE);
                    Util.showAlertDialogBuild("Erro", e.getMessage(), context, () ->
                            sharedViewModel.setUserFaceIdContinueCaptureLiveData(true)
                    );
                });
    }

    public void getAllEmbeddings(ValueEventListener listener) {
        databaseRef.addListenerForSingleValueEvent(listener);
    }

    @NonNull
    private List<Float> convertEmbeddingForList(@NonNull float[] embedding) {
        List<Float> list = new ArrayList<>();
        for (float value : embedding) list.add(value);
        return list;
    }

    @NonNull
    public float[][] convertListForEmbedding(@NonNull List<Object> embeddinList) {
        float[][] embedding = new float[embeddinList.size()][192];
        if (!embeddinList.isEmpty() && embeddinList.get(0) instanceof Map) {
            for (int i = 0; i < embeddinList.size(); i++) {
                Map<String, Object> userMap = (Map<String, Object>) embeddinList.get(i);
                if (userMap.containsKey("embedding")) {
                    List<Object> row = (List<Object>) userMap.get("embedding");
                    if (row != null) {
                        for (int j = 0; j < row.size(); j++) {
                            Object valueInRow = row.get(j);
                            if (valueInRow instanceof Double) {
                                embedding[i][j] = ((Double) valueInRow).floatValue();
                            } else if (valueInRow instanceof Float) {
                                embedding[i][j] = (Float) valueInRow;
                            } else {
                                embedding[i][j] = 0.0f;
                            }
                        }
                    }
                }
            }
        }
        return embedding;
    }
}
