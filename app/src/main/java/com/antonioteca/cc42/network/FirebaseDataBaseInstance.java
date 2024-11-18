package com.antonioteca.cc42.network;

import com.google.firebase.database.FirebaseDatabase;

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
}
