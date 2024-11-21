package com.antonioteca.cc42.dao.daofarebase;

import android.content.Context;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.antonioteca.cc42.R;
import com.antonioteca.cc42.network.FirebaseDataBaseInstance;
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
            String userId,
            String userLogin,
            String displayName,
            String cursusId,
            String campusId,
            Context context,
            ProgressBar progressBarmarkAttendance,
            FloatingActionButton fabOpenCameraScannerQrCode,
            SharedViewModel sharedViewModel
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
                    Util.setInvisibleProgressBar(progressBarmarkAttendance, fabOpenCameraScannerQrCode, sharedViewModel);
                    String message = "Você já marcou presença neste evento, cadete " + displayName + "!";
                    Util.showAlertDialogBuild("EVENT", message, context, null);
                } else {

                    // Armazenamento de Dados de Participante
                    Map<String, Object> participantData = new HashMap<>();
                    participantData.put("uid", userId);
                    participantData.put("user", userLogin);
                    participantData.put("display_name", displayName);

                    // Crie os dados do evento e usuários
                    Map<String, Object> eventUpdates = new HashMap<>();
                    eventUpdates.put("cursus/" + cursusId + "/users/" + userId, participantData);
                    eventUpdates.put("cursus/" + cursusId + "/events/" + eventId + "/participants/" + userId, true);
                    eventUpdates.put("cursus/" + cursusId + "/events/" + eventId + "/status", "pendente"); // ou "iniciado" ou "finalizado"

                    // Referência ao Firebase para adicionar o cadete
                    DatabaseReference campusReference = firebaseDatabase.getReference("campus")
                            .child(campusId);

                    // Execute a operação atômica para armazenar o evento e os participantes
                    campusReference.updateChildren(eventUpdates)
                            .addOnSuccessListener(aVoid -> {
                                Util.setInvisibleProgressBar(progressBarmarkAttendance, fabOpenCameraScannerQrCode, sharedViewModel);
                                String message = "Presença registrada com sucesso! Bem-vindo ao evento, cadete " + displayName + "!";
                                Util.showAlertDialogBuild("EVENT", message, context, null);
                            })
                            .addOnFailureListener(e -> {
                                Util.setInvisibleProgressBar(progressBarmarkAttendance, fabOpenCameraScannerQrCode, sharedViewModel);
                                Util.showAlertDialogBuild(context.getString(R.string.err), "Erro ao armazenar evento e participante: " + e.getMessage(), context, null);
                            });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Util.setInvisibleProgressBar(progressBarmarkAttendance, fabOpenCameraScannerQrCode, sharedViewModel);
                Util.showAlertDialogBuild(context.getString(R.string.err), "Erro ao verificar presença" + error.toException(), context, null);
            }
        });
    }

    /*
     * 1.1. Marcar Evento como Iniciado
     * Quando todos os cadetes marcarem presença, você pode alterar o status do evento para "iniciado".
     * Vamos criar uma função que verifica se todos os participantes marcaram presença e, se isso for verdadeiro, altera o status do evento para "iniciado":
     * */

    public static void markEventAsStarted(String campusId, String eventId, Context context) {
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
            public void onCancelled(DatabaseError error) {
                Log.e("Firebase", "Erro ao verificar presença", error.toException());
            }
        });
    }

    /*
     * 1.2. Impedir Registros Após Evento Iniciado
     * Quando um cadete tentar registrar presença após o evento ser marcado como iniciado, você deve verificar o status do evento.
     * Se o status for "iniciado", você impede o registro de presença e exibe uma mensagem adequada.
     * */

    public void markAttendance_(String eventId, String campusId, String userId, String userName, String profileUrl, String profilePicUrl, Context context) {
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

    public void markEventAsFinished(String campusId, String eventId, Context context) {
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

    public void markEventFinished(String eventId, String campusId, String userId) {
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

    public void markAttendance__(String eventId, String campusId, String userId, String userName, String profileUrl, String profilePicUrl, Context context) {
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
    }

   /* FirebaseDatabase database = FirebaseDatabase.getInstance();

    // Referência para o evento e participantes
    DatabaseReference eventRef = database.getReference("campus")
            .child(campusId)
            .child("events")
            .child(eventId)
            .child("participants");

// Adiciona o cadete à lista de participantes do evento
eventRef.child(userId).

    setValue(true);

    // Referência para os dados do usuário (cadete)
    DatabaseReference userRef = database.getReference("users")
            .child(userId);

    // Dados do cadete
    Map<String, Object> userData = new HashMap<>();
userData.put("name",userName);
userData.put("profile_url",profileUrl);
userData.put("profile_pic",profilePicUrl);

// Armazenar os dados do cadete no nó de usuários
userRef.updateChildren(userData);

    DatabaseReference eventRef = database.getReference("campus")
            .child(campusId)
            .child("events")
            .child(eventId)
            .child("participants");*/

/*// Obter todos os IDs dos participantes
eventRef.addListenerForSingleValueEvent(new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot snapshot) {
            for (DataSnapshot participantSnapshot : snapshot.getChildren()) {
                String userId = participantSnapshot.getKey();  // O ID do participante

                // Agora você pode buscar os dados detalhados do usuário
                DatabaseReference userRef = database.getReference("users")
                        .child(userId);

                userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        String userName = snapshot.child("name").getValue(String.class);
                        String profileUrl = snapshot.child("profile_url").getValue(String.class);
                        String profilePicUrl = snapshot.child("profile_pic").getValue(String.class);

                        // Aqui você tem os dados completos do participante
                        System.out.println("Participante: " + userName);
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        System.err.println("Erro ao buscar dados do participante: " + error.getMessage());
                    }
                });
            }
        }

        @Override
        public void onCancelled(DatabaseError error) {
            System.err.println("Erro ao buscar participantes: " + error.getMessage());
        }
    });*/

}
