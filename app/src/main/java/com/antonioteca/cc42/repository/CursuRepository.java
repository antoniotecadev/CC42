package com.antonioteca.cc42.repository;

import android.content.Context;

import androidx.annotation.NonNull;

import com.antonioteca.cc42.dao.daoapi.DaoApiCursu;
import com.antonioteca.cc42.model.Cursu;
import com.antonioteca.cc42.model.Token;
import com.antonioteca.cc42.network.RetrofitClientApi;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CursuRepository {

    private final Token token;
    private final Context context;
    private final DaoApiCursu daoApiCursu;

    public CursuRepository(Context context) {
        this.context = context;
        token = new Token(context);
        daoApiCursu = RetrofitClientApi.getApiService(context).create(DaoApiCursu.class);
    }

    public void getCursus(@NonNull Callback<List<Cursu>> callback) {
        if (token.isTokenExpired(token.getTokenExpirationTime())) {
            token.getRefreshTokenUserSave(context, (success) -> {
                if (success)
                    extracted(callback);
                else
                    callback.onResponse(null, Response.success(new ArrayList<>()));
            });
        } else
            extracted(callback);
    }

    private void extracted(Callback<List<Cursu>> callback) {
        Call<List<Cursu>> cursusCall = daoApiCursu.getCursus("Bearer " + token.getAccessToken(), 1, 100);
        cursusCall.enqueue(callback);
    }
}
