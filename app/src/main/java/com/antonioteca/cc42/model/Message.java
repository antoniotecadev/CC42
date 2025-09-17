package com.antonioteca.cc42.model;

import com.google.firebase.database.IgnoreExtraProperties;

// Classe Pojo para a Mensagem
@IgnoreExtraProperties // Importante para o Firebase mapear correctamente
public class Message {
    public String title;
    public String message;
    public Long timestamp;
    public Long createdBy;

    // Construtor vazio necessário para o Firebase
    public Message() {
    }

    public Message(String title, String message, Long timestamp, Long createdBy) {
        this.title = title;
        this.message = message;
        this.timestamp = timestamp;
        this.createdBy = createdBy;
    }

    // Getters são necessários para o Firebase (mesmo que públicos)
    public String getTitle() {
        return title;
    }

    public String getMessage() {
        return message;
    }

    public Long getTimestamp() {
        return timestamp;
    }
    public Long getCreatedBy() {
        return createdBy;
    }
}
