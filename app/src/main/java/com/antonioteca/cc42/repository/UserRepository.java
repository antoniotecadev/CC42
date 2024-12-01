package com.antonioteca.cc42.repository;

import android.content.Context;

import com.antonioteca.cc42.dao.daoapi.DaoApiUser;
import com.antonioteca.cc42.model.Coalition;
import com.antonioteca.cc42.model.Token;
import com.antonioteca.cc42.model.User;
import com.antonioteca.cc42.network.RetrofitClientApi;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;

/**
 * Classe que gerencia a lógica de negócios e a comunicação entre os dados (Model) e o ViewModel
 * O repositório será responsável por realizar as chamadas de API
 */

public class UserRepository {

    private final User user;
    private final Token token;
    private final DaoApiUser daoApiUser;

    public UserRepository(Context context) {
        user = new User(context);
        token = new Token(context);
        daoApiUser = RetrofitClientApi.getApiService().create(DaoApiUser.class);
    }

    public boolean saveUser(User user) {
        return this.user.saveUser(user, user.coalition);
    }

    public void getUser(Callback<User> callback) {
        Call<User> userCall = daoApiUser.getUser("Bearer " + token.getAccessToken());
        userCall.enqueue(callback);
    }

    public void getCoalition(long userId, Callback<List<Coalition>> callback) {
        Call<List<Coalition>> coalitionCall = daoApiUser.getCoalition(userId, "Bearer " + token.getAccessToken());
        coalitionCall.enqueue(callback);
    }
}
