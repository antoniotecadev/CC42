package com.antonioteca.cc42.dao.daofarebase;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;

import com.antonioteca.cc42.R;
import com.antonioteca.cc42.utility.CustomToastManager;
import com.antonioteca.cc42.utility.Util;
import com.antonioteca.cc42.viewmodel.SharedViewModel;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class DaoEventFirebase {

    // Agora, vamos adicionar o participante quando o QR Code for lido
    public static void markAttendance(
            FirebaseDatabase firebaseDatabase,
            String eventId,
            String userStaffId,
            Long registeredBy,
            String userId,
            String displayName,
            String cursusId,
            String campusId,
            String urlImageUser,
            boolean eventAction,
            Context context,
            LayoutInflater layoutInflater,
            ProgressBar progressBarMarkAttendance,
            SharedViewModel sharedViewModel,
            Runnable runnableResumeCamera
    ) {

        // Cria ou atualiza a lista de participantes do evento
        DatabaseReference campusRef = firebaseDatabase.getReference("campus")
                .child(campusId)
                .child("cursus")
                .child(cursusId)
                .child("events")
                .child(eventId)
                .child("participants");  // Referência para os participantes do evento

        // Verifica se o usuário já registrou presença
        campusRef.child(String.valueOf(userId)).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String check = eventAction ? "checkin" : "checkout";
                boolean existsSnapshot = snapshot.exists();
                if (existsSnapshot && snapshot.child(check).getValue(Long.class) != null) {
                    progressBarMarkAttendance.setVisibility(View.GONE);
                    String message = displayName + "\n" + (eventAction ? context.getString(R.string.alreadyCheckedIn) : context.getString(R.string.alreadyCheckedOut));
                    Util.showAlertDialogMessage(context, layoutInflater, context.getString(R.string.warning), message, "#FDD835", urlImageUser, runnableResumeCamera);
                } else {
                    if (!eventAction) {
                        Long existingData = existsSnapshot ? snapshot.child("checkin").getValue(Long.class) : null;
                        if (existingData == null) {
                            progressBarMarkAttendance.setVisibility(View.GONE);
                            String message = displayName + "\n" + context.getString(R.string.needCheckinFirst);
                            Util.showAlertDialogMessage(context, layoutInflater, context.getString(R.string.warning), message, "#FDD835", urlImageUser, runnableResumeCamera);
                            return;
                        }
                    }

                    // Obtenha os dados existentes do snapshot se ele existir.
                    Map<String, Object> participantData;
                    if (existsSnapshot && snapshot.getValue() instanceof Map) {
                        //noinspection unchecked
                        participantData = new HashMap<>((Map<String, Object>) snapshot.getValue());
                    } else {
                        participantData = new HashMap<>();
                    }

                    // Adicione ou atualize a informação de check-in/check-out e registeredBy.
                    participantData.put(check, System.currentTimeMillis());
                    participantData.put("registeredBy", registeredBy);

                    // Atualiza o nó do participante no evento
                    Map<String, Object> eventUpdates = new HashMap<>();
                    eventUpdates.put("cursus/" + cursusId + "/events/" + eventId + "/participants/" + userId, participantData);

                    // Referência ao Firebase para adicionar o cadete
                    DatabaseReference campusReference = firebaseDatabase.getReference("campus")
                            .child(campusId);

                    // Execute a operação atômica para armazenar o evento e os participantes
                    campusReference.updateChildren(eventUpdates)
                            .addOnSuccessListener(aVoid -> {
                                if (userStaffId != null)
                                    Util.sendInfoTmpUserEventMeal(userStaffId, firebaseDatabase, campusId, cursusId, displayName, urlImageUser);
                                sharedViewModel.setUserIdLiveData(Long.valueOf(userId));
                                progressBarMarkAttendance.setVisibility(View.GONE);
                                String message = displayName + "\n" + (eventAction ? context.getString(R.string.checkinSuccessful) : context.getString(R.string.checkoutSuccessful));
//                                Util.showAlertDialogMessage(context, layoutInflater, context.getString(R.string.sucess), message, "#4CAF50", urlImageUser, runnableResumeCamera);
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
                                progressBarMarkAttendance.setVisibility(View.GONE);
                                String message = context.getString(R.string.errorMakingCheck) + ": " + e.getMessage();
                                Util.showAlertDialogMessage(context, layoutInflater, context.getString(R.string.err), message, "#E53935", urlImageUser, runnableResumeCamera);
                            });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressBarMarkAttendance.setVisibility(View.GONE);
                String message = context.getString(R.string.msg_error_check_attendance_event) + ": " + error.toException();
                Util.showAlertDialogMessage(context, layoutInflater, context.getString(R.string.err), message, "#E53935", urlImageUser, runnableResumeCamera);
            }
        });
    }
}
