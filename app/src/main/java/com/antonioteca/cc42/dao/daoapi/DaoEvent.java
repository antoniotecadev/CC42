package com.antonioteca.cc42.dao.daoapi;

import com.antonioteca.cc42.model.Event;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Path;

public interface DaoEvent {

    @GET("/v2/campus/{campus_id}/events")
    Call<List<Event>> getEvents(
            @Path("campus_id") String campusId,
            @Header("Authorization") String accessToken
    );
}
