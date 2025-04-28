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

public class DaoEventFirebase {

    // Agora, vamos adicionar o participante quando o QR Code for lido
    public static void markAttendance(
            FirebaseDatabase firebaseDatabase,
            String eventId,
            Long registeredBy,
            String userId,
            String displayName,
            String cursusId,
            String campusId,
            String urlImageUser,
            Context context,
            LayoutInflater layoutInflater,
            ProgressBar progressBarMarkAttendance,
            FloatingActionButton fabOpenCameraScannerQrCode,
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
                if (snapshot.exists()) {
                    Util.setInvisibleProgressBar(progressBarMarkAttendance, fabOpenCameraScannerQrCode, sharedViewModel);
                    String message = displayName + "\n" + context.getString(R.string.msg_you_already_mark_attendance_event);
                    Util.showAlertDialogMessage(context, layoutInflater, context.getString(R.string.warning), message, "#FDD835", urlImageUser, runnableResumeCamera);
                } else {
                    Map<String, Object> participantData = new HashMap<>();
                    participantData.put(String.valueOf(userId), true);
                    participantData.put("registeredBy", registeredBy);

                    // Atualiza o nó do participante no evento
                    Map<String, Object> eventUpdates = new HashMap<>();
//                    eventUpdates.put("cursus/" + cursusId + "/events/" + eventId + "/status", "pendente"); // ou "iniciado" ou "finalizado"
                    eventUpdates.put("cursus/" + cursusId + "/events/" + eventId + "/participants/" + userId, participantData);

                    // Referência ao Firebase para adicionar o cadete
                    DatabaseReference campusReference = firebaseDatabase.getReference("campus")
                            .child(campusId);

                    // Execute a operação atômica para armazenar o evento e os participantes
                    campusReference.updateChildren(eventUpdates)
                            .addOnSuccessListener(aVoid -> {
                                sharedViewModel.setUserIdLiveData(Long.valueOf(userId));
                                Util.setInvisibleProgressBar(progressBarMarkAttendance, fabOpenCameraScannerQrCode, sharedViewModel);
                                String message = displayName + "\n" + context.getString(R.string.msg_sucess_mark_attendance_event);
                                Util.showAlertDialogMessage(context, layoutInflater, context.getString(R.string.sucess), message, "#4CAF50", urlImageUser, runnableResumeCamera);
                            })
                            .addOnFailureListener(e -> {
                                Util.setInvisibleProgressBar(progressBarMarkAttendance, fabOpenCameraScannerQrCode, sharedViewModel);
                                String message = context.getString(R.string.msg_error_mark_attendance_event) + ": " + e.getMessage();
                                Util.showAlertDialogMessage(context, layoutInflater, context.getString(R.string.err), message, "#E53935", urlImageUser, runnableResumeCamera);
                            });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Util.setInvisibleProgressBar(progressBarMarkAttendance, fabOpenCameraScannerQrCode, sharedViewModel);
                String message = context.getString(R.string.msg_error_check_attendance_event) + ": " + error.toException();
                Util.showAlertDialogMessage(context, layoutInflater, context.getString(R.string.err), message, "#E53935", urlImageUser, runnableResumeCamera);
            }
        });
    }

    /*public static void sinchronizationAttendanceList(
            UserViewModel userViewModel, MutableLiveData<List<String>> userIdsWhoMarkedAttendanceMutableLiveData,
            List<Long> userIdsWhoMarkedAttendanceLocal, List<String> userIdsWhoMarkedAttendance,
            FirebaseDatabase firebaseDatabase,
            String campusId,
            String cursusId,
            String eventId,
            SwipeRefreshLayout swipeRefreshLayout,
            Context context,
            LayoutInflater layoutInflater
    ) {
        Toast.makeText(context, R.string.synchronization, Toast.LENGTH_LONG).show();
        swipeRefreshLayout.setRefreshing(true);
        Map<String, Object> userUpdates = new HashMap<>();
        for (Long userId : userIdsWhoMarkedAttendanceLocal) {
            userIdsWhoMarkedAttendance.add(String.valueOf(userId));
            userUpdates.put("cursus/" + cursusId + "/events/" + eventId + "/participants/" + userId, true);
        }
        DatabaseReference campusReference = firebaseDatabase.getReference("campus")
                .child(campusId);

        campusReference.updateChildren(userUpdates)
                .addOnSuccessListener(aVoid -> {
                    swipeRefreshLayout.setRefreshing(false);
                    String message = context.getString(R.string.msg_attendance_list_synchronized);
                    userIdsWhoMarkedAttendanceMutableLiveData.postValue(userIdsWhoMarkedAttendance);
                    Util.showAlertDialogMessage(context, layoutInflater, context.getString(R.string.sucess), message, "#4CAF50", null);
                    userViewModel.deleteLocalAttendanceList(Integer.parseInt(campusId), Integer.parseInt(cursusId), Long.parseLong(eventId), context, layoutInflater);
                })
                .addOnFailureListener(e -> {
                    swipeRefreshLayout.setRefreshing(false);
                    String message = context.getString(R.string.error_msg_attendance_list_synchronized) + ": " + e.getMessage();
                    Util.showAlertDialogMessage(context, layoutInflater, context.getString(R.string.err), message, "#E53935", null);
                });
    }*/

   /* public static void markEventAsStarted(
            FirebaseDatabase firebaseDatabase,
            String campusId,
            String cursusId,
            String eventId,
            Context context,
            LayoutInflater layoutInflater,
            ProgressBar progressBarMarkEventAsStarted
    ) {
        DatabaseReference eventRef = firebaseDatabase.getReference("campus")
                .child(campusId)
                .child("cursus")
                .child(cursusId)
                .child("events")
                .child(eventId);

        eventRef.child("status").setValue("iniciado").addOnSuccessListener(unused -> {
            progressBarMarkEventAsStarted.setVisibility(View.VISIBLE);
            String message = context.getString(R.string.msg_sucess_mark_event_started);
            //Util.showAlertDialogMessage(context, layoutInflater, context.getString(R.string.sucess), message, "#4CAF50");
        }).addOnFailureListener(e -> {
            progressBarMarkEventAsStarted.setVisibility(View.VISIBLE);
            String message = context.getString(R.string.msg_error_mark_event_started) + ": " + e.getMessage();
            //Util.showAlertDialogMessage(context, layoutInflater, context.getString(R.string.err), message, "#E53935");
        });
    }

    public static void markEventAsStarted_(String campusId, String eventId, Context context) {
        FirebaseDataBaseInstance firebaseDataBaseInstance = FirebaseDataBaseInstance.getInstance();
        DatabaseReference eventRef = firebaseDataBaseInstance.database.getReference("campus")
                .child(campusId)
                .child("events")
                .child(eventId);

        eventRef.child("participants").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // Verifique se todos os cadetes marcaram presença
                    int totalParticipants = (int) snapshot.getChildrenCount();
                    int presentParticipants = 0;

                    for (DataSnapshot participantSnapshot : snapshot.getChildren()) {
                        // Conta os participantes que marcaram presença
                        if (participantSnapshot.exists()) {
                            presentParticipants++;
                        }
                    }

                    // Se todos os participantes marcaram presença
                    if (totalParticipants == presentParticipants) {
                        // Atualiza o status do evento para "iniciado"
                        eventRef.child("status").setValue("iniciado");
                        Toast.makeText(context, "Evento iniciado. Não é mais possível registrar presença.", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Firebase", "Erro ao verificar presença", error.toException());
            }
        });
    }

    /*
     * 1.2. Impedir Registros Após Evento Iniciado
     * Quando um cadete tentar registrar presença após o evento ser marcado como iniciado, você deve verificar o status do evento.
     * Se o status for "iniciado", você impede o registro de presença e exibe uma mensagem adequada.
     * */

    /*public void markAttendance_(String eventId, String campusId, String userId, String userName, String profileUrl, String profilePicUrl, Context context) {
        FirebaseDataBaseInstance firebaseDataBaseInstance = FirebaseDataBaseInstance.getInstance();
        DatabaseReference eventRef = firebaseDataBaseInstance.database.getReference("campus")
                .child(campusId)
                .child("events")
                .child(eventId);

        eventRef.child("status").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                String status = snapshot.getValue(String.class);
                if ("iniciado".equals(status)) {
                    // Se o evento estiver iniciado, não permita a presença
                    Toast.makeText(context, "O evento já foi iniciado. Não é mais possível registrar presença.", Toast.LENGTH_SHORT).show();
                } else {
                    // Registra a presença
                    // Lógica para registrar a presença do cadete
                    // ...
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.e("Firebase", "Erro ao consultar status do evento", error.toException());
            }
        });
    }

    /*
     * 2. Marcar o Evento Como Finalizado
     * Quando o evento termina, todos os cadetes devem escanear novamente o QR Code para marcar que terminaram o evento.
     * Após isso, o evento será marcado como finalizado, e nenhum cadete poderá registrar que terminou.

     * 2.1. Marcar Evento como Finalizado
     * Após todos os cadetes marcarem que terminaram, você pode atualizar o status do evento para "finalizado" e impedir que mais cadetes marquem como "terminado".
     * A ideia é a mesma de quando o evento foi iniciado — você vai verificar se todos marcaram como "terminado" e, em seguida, atualizar o status.
     * */

    /*public void markEventAsFinished(String campusId, String eventId, Context context) {
        FirebaseDataBaseInstance firebaseDataBaseInstance = FirebaseDataBaseInstance.getInstance();
        DatabaseReference eventRef = firebaseDataBaseInstance.database.getReference("campus")
                .child(campusId)
                .child("events")
                .child(eventId);

        eventRef.child("participants").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // Verifica se todos os cadetes marcaram como terminado
                    int totalParticipants = (int) snapshot.getChildrenCount();
                    int finishedParticipants = 0;

                    for (DataSnapshot participantSnapshot : snapshot.getChildren()) {
                        // Verifica se o cadete marcou como "terminado"
                        if (participantSnapshot.child("finished").getValue(Boolean.class)) {
                            finishedParticipants++;
                        }
                    }

                    // Se todos marcaram como "terminado"
                    if (totalParticipants == finishedParticipants) {
                        // Atualiza o status do evento para "finalizado"
                        eventRef.child("status").setValue("finalizado");
                        Toast.makeText(context, "Evento finalizado. Não é mais possível marcar como terminado.", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.e("Firebase", "Erro ao verificar status de término", error.toException());
            }
        });
    }

    /*
     * 2.2. Marcar o Evento Como "Terminado" pelo Cadete
     * Quando o cadete escanear o QR Code para marcar como "terminado", você precisa garantir que ele só possa fazer isso se o evento estiver em status "iniciado".
     * Após ele marcar, a função deve alterar o estado para "terminado" para esse cadete.
     * */

   /* public void markEventFinished(String eventId, String campusId, String userId) {
        FirebaseDataBaseInstance firebaseDataBaseInstance = FirebaseDataBaseInstance.getInstance();
        DatabaseReference participantRef = firebaseDataBaseInstance.database.getReference("campus")
                .child(campusId)
                .child("events")
                .child(eventId)
                .child("participants")
                .child(userId);

        participantRef.child("finished").setValue(true);  // Marca que o cadete terminou o evento

        // Se todos os cadetes marcaram "terminado", finalize o evento
        markEventAsFinished(campusId, eventId, null);  // Chama a função para verificar se todos terminaram
    }
    /*
     * 3. Impedir Que Cadetes Marquem Depois do Evento Finalizado
     * Quando o evento for finalizado, você deve impedir qualquer marcação de presença ou de término.
     * Isso pode ser feito da mesma forma que fizemos para o evento iniciado, verificando o status.
     * */

    /*public void markAttendance__(String eventId, String campusId, String userId, String userName, String profileUrl, String profilePicUrl, Context context) {
        FirebaseDataBaseInstance firebaseDataBaseInstance = FirebaseDataBaseInstance.getInstance();
        DatabaseReference eventRef = firebaseDataBaseInstance.database.getReference("campus")
                .child(campusId)
                .child("events")
                .child(eventId);

        eventRef.child("status").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                String status = snapshot.getValue(String.class);
                if ("finalizado".equals(status)) {
                    // Se o evento estiver finalizado, não permita a presença
                    Toast.makeText(context, "O evento já foi finalizado. Não é mais possível marcar presença.", Toast.LENGTH_SHORT).show();
                } else {
                    // Lógica para registrar a presença do cadete
                    // ...
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.e("Firebase", "Erro ao consultar status do evento", error.toException());
            }
        });
    }*/
}
