package com.antonioteca.cc42.repository;

import android.content.Context;

import com.antonioteca.cc42.dao.daoapi.DaoApiEvent;
import com.antonioteca.cc42.model.Event;
import com.antonioteca.cc42.model.Token;
import com.antonioteca.cc42.model.User;
import com.antonioteca.cc42.network.RetrofitClientApi;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;

public class EventRepository {

    private final User user;
    private final Token token;
    private final DaoApiEvent daoEvent;

    public EventRepository(Context context) {
        user = new User(context);
        token = new Token(context);
        daoEvent = RetrofitClientApi.getApiService().create(DaoApiEvent.class);
    }

    public void getEvents(Callback<List<Event>> callback) {
        if (user.isStaff())
            getCampusEvents(callback);
        else
            getCursusEvents(callback);
    }

    private void getCursusEvents(Callback<List<Event>> callback) {
        Call<List<Event>> eventCall = daoEvent.getCursusEvents(
                user.getCampusId(),
                user.getCursusId(),
                "Bearer " + token.getAccessToken()
        );
        eventCall.enqueue(callback);
    }

    public void getCampusEvents(Callback<List<Event>> callback) {
        Call<List<Event>> eventCall = daoEvent.getCampusEvents(
                user.getCampusId(),
                "Bearer " + token.getAccessToken()
        );
        eventCall.enqueue(callback);
    }
}
