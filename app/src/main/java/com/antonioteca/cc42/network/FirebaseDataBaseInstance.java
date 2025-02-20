package com.antonioteca.cc42.network;

import android.content.Context;

import androidx.annotation.NonNull;

import com.antonioteca.cc42.utility.Util;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class FirebaseDataBaseInstance {


    // Instância única do FirebaseDatabase
    private static FirebaseDataBaseInstance instance;
    public FirebaseDatabase database;

    // Construtor privado para evitar criação externa
    private FirebaseDataBaseInstance() {
        // Inicializa o FirebaseDatabase
        database = FirebaseDatabase.getInstance();
    }

    // Método para obter a instância do Singleton
    public static synchronized FirebaseDataBaseInstance getInstance() {
        if (instance == null) {
            instance = new FirebaseDataBaseInstance();
        }
        return instance;
    }

    public static void fetchApiKeyFromDatabase(String source, Context context, ApiKeyCallback callback) {
        FirebaseDatabase firebaseDatabase = FirebaseDataBaseInstance.getInstance().database;
        DatabaseReference databaseRef = firebaseDatabase.getReference("api_keys").child(source).child("secret");
        databaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String apiKey = dataSnapshot.getValue(String.class); // Obter a chave
                    callback.onApiKeyReceived(apiKey);
                } else {
                    Util.showAlertDialogBuild("ERROR", "API not found", context, null);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Util.showAlertDialogBuild("ERROR", "API: " + databaseError.getMessage(), context, null);
            }
        });
    }
}
