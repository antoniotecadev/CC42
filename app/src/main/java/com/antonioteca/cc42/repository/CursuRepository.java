package com.antonioteca.cc42.repository;

import android.content.Context;

import com.antonioteca.cc42.dao.daoapi.DaoApiCursu;
import com.antonioteca.cc42.model.Cursu;
import com.antonioteca.cc42.model.Token;
import com.antonioteca.cc42.network.RetrofitClientApi;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;

public class CursuRepository {
    private final Cursu cursu;
    private final Token token;
    private final DaoApiCursu daoApiCursu;

    public CursuRepository(Context context) {
        cursu = new Cursu();
        token = new Token(context);
        daoApiCursu = RetrofitClientApi.getApiService().create(DaoApiCursu.class);
    }

    public void getCursus(Callback<List<Cursu>> callback) {
        Call<List<Cursu>> cursusCall = daoApiCursu.getCursus("Bearer " + token.getAccessToken(), 1, 100);
        cursusCall.enqueue(callback);
    }
}
