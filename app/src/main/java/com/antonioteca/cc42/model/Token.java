package com.antonioteca.cc42.model;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.annotations.SerializedName;

/**
 * Classe que representam os dados da aplicação
 */

public class Token {

    @SerializedName("access_token")
    public String accessToken;

    @SerializedName("refresh_token")
    public String refreshToken;

    @SerializedName("expires_in")
    public Long tokenExpirationTime;

    private final SharedPreferences preferences;
    private final SharedPreferences.Editor editor;

    public Token(Context context) {
        preferences = context.getSharedPreferences("MyAppPrefsToken", MODE_PRIVATE);
        editor = preferences.edit();
    }

    public String getAccessToken() {
        return preferences.getString("access_token", null);
    }

    public void setAccessToken(String accessToken) {
        editor.putString("access_token", accessToken);
    }

    public String getRefreshToken() {
        return preferences.getString("refresh_token", null);
    }

    public void setRefreshToken(String refreshToken) {
        editor.putString("refresh_token", refreshToken);
    }

    public Long getTokenExpirationTime() {
        return preferences.getLong("token_expiration_time", 0);
    }

    public void setTokenExpirationTime(Long expiresIn) {
        tokenExpirationTime = System.currentTimeMillis() + (expiresIn * 1000); // Converter o tempo de expiração em milissegundos
        editor.putLong("token_expiration_time", tokenExpirationTime);
    }

    public boolean commit() {
        return editor.commit(); // ou editor.apply() se preferir
    }

    public boolean clear() {
        editor.clear();
        return commit();
    }
}
