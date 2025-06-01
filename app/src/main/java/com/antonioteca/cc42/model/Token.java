package com.antonioteca.cc42.model;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.antonioteca.cc42.network.HttpException;
import com.antonioteca.cc42.network.HttpStatus;
import com.antonioteca.cc42.repository.TokenRepository;
import com.google.gson.annotations.SerializedName;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

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

    public boolean isTokenExpired(Long tokenExpirationTime) {
        return System.currentTimeMillis() > tokenExpirationTime;
    }

    public void getRefreshTokenUserSave(Context context, CallBackToken callBackToken) {
        TokenRepository tokenRepository = new TokenRepository(context);
        tokenRepository.getRefreshTokenUser(getRefreshToken(), new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<Token> call, @NonNull Response<Token> response) {
                if (response.isSuccessful()) {
                    Token token = response.body();
                    if (token != null) {
                        tokenRepository.saveAcessToken(token);
                        callBackToken.onTokenReceived(true);
                    } else
                        callBackToken.onTokenReceived(false);
                } else {
                    HttpStatus httpStatus = HttpStatus.handleResponse(response.code());
                    Toast.makeText(context, httpStatus.getCode() + ": " + httpStatus.getDescription(), Toast.LENGTH_LONG).show();
                    callBackToken.onTokenReceived(false);
                }
            }

            @Override
            public void onFailure(@NonNull Call<Token> call, @NonNull Throwable throwable) {
                HttpException httpException = HttpException.handleException(throwable, context);
                Toast.makeText(context, httpException.getCode() + ": " + httpException.getDescription(), Toast.LENGTH_LONG).show();
                callBackToken.onTokenReceived(false);
            }
        });
    }
}

