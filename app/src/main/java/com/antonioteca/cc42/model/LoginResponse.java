package com.antonioteca.cc42.model;

import com.google.gson.annotations.SerializedName;

public class LoginResponse {
    @SerializedName("userWithCoalition")
    public User user;

    @SerializedName("tokenResponse")
    public Token token;
    public String firebaseToken;
}
