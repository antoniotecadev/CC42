package com.antonioteca.cc42.model;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class BlockedUser {
    public String userId;
    public String reason;
    public Long blockedAt;
    public boolean isBlocked;

    public BlockedUser() {
        // Obrigatório para o Firebase deserializar
    }
}