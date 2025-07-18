package com.antonioteca.cc42.repository;

import static com.antonioteca.cc42.network.FirebaseDataBaseInstance.fetchApiKeyFromDatabase;

import android.content.Context;

import androidx.annotation.NonNull;

import com.antonioteca.cc42.dao.daoapi.DaoApiToken;
import com.antonioteca.cc42.model.Token;
import com.antonioteca.cc42.network.NetworkConstants;
import com.antonioteca.cc42.network.RetrofitClientApi;

import retrofit2.Call;
import retrofit2.Callback;

/**
 * Classe que gerencia a lógica de negócios e a comunicação entre os dados (Model) e o ViewModel
 * O repositório será responsável por realizar as chamadas de API
 */

public class TokenRepository {

    private final Token token;
    private final DaoApiToken daoApiToken;

    public TokenRepository(Context context) {
        token = new Token(context);
        daoApiToken = RetrofitClientApi.getApiService(context).create(DaoApiToken.class);
    }
//    AO LOGAR NO CLIENTE
//    public void getAccessTokenUser(String code, Context context, Callback<Token> callback) {
//        fetchApiKeyFromDatabase("intra", context, apiKey -> {
//            Call<Token> tokenCall = daoApiToken.getAccessToken(
//                    "authorization_code",  // grant_type
//                    NetworkConstants.UID, // client_id
//                    apiKey, // client_secret
//                    code,                           // code recebido
//                    NetworkConstants.SCHEME_HOST    // redirect_uri
//            );
//            tokenCall.enqueue(callback); // Executar a chamada de forma assíncrona
//        });
//    }

    public boolean saveAcessToken(@NonNull Token t) {
        token.setAccessToken(t.accessToken);
        token.setRefreshToken(t.refreshToken);
        token.setTokenExpirationTime(t.tokenExpirationTime /* Segundos */);
        return token.commit();
    }

    public void getRefreshTokenUser(String refreshToken, Context context, Callback<Token> callback) {
        fetchApiKeyFromDatabase("intra", context, apiKey -> {
            Call<Token> call = daoApiToken.getRefreshToken(
                    "refresh_token",
                    refreshToken,
                    NetworkConstants.UID,
                    apiKey
            );
            call.enqueue(callback); // Executar a chamada de forma assíncrona
        });
    }
}
