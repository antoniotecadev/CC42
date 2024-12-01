package com.antonioteca.cc42.dao.daoapi;

import com.antonioteca.cc42.model.Coalition;
import com.antonioteca.cc42.model.User;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Path;

public interface DaoApiUser {

    @GET("/v2/me")
    Call<User> getUser(@Header("Authorization") String accessToken);

    @GET("/v2/users/{user_id}/coalitions")
    Call<List<Coalition>> getCoalition(
            @Path("user_id") long userId,
            @Header("Authorization") String accessToken
    );

    @GET("/v2/events/{event_id}/users")
    Call<List<User>> getUsersEvent(
            @Path("event_id") long eventId,
            @Header("Authorization") String accessToken
    );
}
