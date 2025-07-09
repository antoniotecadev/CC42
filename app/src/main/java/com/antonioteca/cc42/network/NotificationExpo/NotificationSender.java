package com.antonioteca.cc42.network.NotificationExpo;

import android.util.Log;

import androidx.annotation.NonNull;

import com.antonioteca.cc42.dao.daoapi.ExpoPushServiceApi;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class NotificationSender {

    private final FirebaseDatabase database;
    private final ExpoPushServiceApi expoPushServiceApi;
    private static final String TAG = "NotificationSender";
    private final ExecutorService executorService = Executors.newFixedThreadPool(1); // Para executar tarefas em background

    public NotificationSender() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(ExpoPushServiceApi.EXPO_PUSH_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        expoPushServiceApi = retrofit.create(ExpoPushServiceApi.class);
        database = FirebaseDatabase.getInstance();
    }

    public void sendExpoNotificationToGroup(String campusId, String cursusId, ExpoNotificationPayload payload) {
        executorService.execute(() -> {
            try {
                List<String> tokens = getAllNotificationTokens(campusId, cursusId);
                if (tokens.isEmpty()) {
                    Log.d(TAG, "Nenhum token encontrado para enviar notificações.");
                    return;
                }

                List<ExpoPushMessage> messages = new ArrayList<>();
                for (String token : tokens) {
                    Map<String, Object> data = payload.getData() != null ? payload.getData() : new HashMap<>();
                    messages.add(new ExpoPushMessage(token, payload.getTitle(), payload.getBody(), data, payload.getImage()));
                }

                int batchSize = 100;
                for (int i = 0; i < messages.size(); i += batchSize) {
                    List<ExpoPushMessage> batch = messages.subList(i, Math.min(i + batchSize, messages.size()));
                    sendBatchNotifications(batch);
                }

            } catch (Exception e) {
                Log.e(TAG, "Erro ao enviar notificações para o grupo: " + e.getMessage(), e);
            } finally {
                shutdown();
            }
        });
    }

    private List<String> getAllNotificationTokens(String campusId, String cursusId) throws InterruptedException {
        final List<String> allTokens = new ArrayList<>();
        final CountDownLatch latch = new CountDownLatch(2); // Usamos um latch para esperar por duas chamadas assíncronas
        // Buscar tokens de Staff
        DatabaseReference staffRef = database.getReference("campus/" + campusId + "/tokenIOSNotification/staff");
        staffRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot tokenSnapshot : dataSnapshot.getChildren()) {
                        String token = tokenSnapshot.getValue(String.class);
                        if (token != null && !token.isEmpty()) {
                            allTokens.add(token);
                        }
                    }
                }
                latch.countDown();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.w(TAG, "Falha ao buscar tokens de staff.", databaseError.toException());
                latch.countDown();
            }
        });

        // Buscar tokens de Student
        DatabaseReference studentRef = database.getReference("campus/" + campusId + "/tokenIOSNotification/student/cursus/" + cursusId);
        studentRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot tokenSnapshot : dataSnapshot.getChildren()) {
                        String token = tokenSnapshot.getValue(String.class);
                        if (token != null && !token.isEmpty()) {
                            allTokens.add(token);
                        }
                    }
                }
                latch.countDown();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.w(TAG, "Falha ao buscar tokens de estudante.", databaseError.toException());
                latch.countDown();
            }
        });
        // Espera até que ambas as buscas no Firebase sejam concluídas (ou timeout)
        boolean finish = (!latch.await(60, TimeUnit.SECONDS));// Timeout de 60 segundos
        if (!finish)
            Log.w(TAG, "Timeout ao buscar tokens do Firebase.");
        return allTokens;
    }

    // Método para liberar recursos quando a classe não for mais necessária
    public void shutdown() {
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    private void sendBatchNotifications(List<ExpoPushMessage> batch) {
        expoPushServiceApi.sendPushNotifications(batch).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                if (response.isSuccessful()) {
                    Log.d(TAG, "Lote de notificações expo enviado com sucesso.");
                } else {
                    Log.e(TAG, "Erro ao enviar lote de notificações expo: " + response.code() + " - " + response.message());
                }
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable throwable) {
                Log.e(TAG, "Erro ao enviar lote de notificações expo: " + throwable.getMessage(), throwable);
            }
        });
    }
}