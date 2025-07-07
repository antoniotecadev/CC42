package com.antonioteca.cc42.dao.daoapi;

import com.antonioteca.cc42.network.NotificationFirebase.FCMessage;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface DaoApiMeal {
    @POST("api/notifications")
    Call<Void> sendFCMNotification(@Body FCMessage fcmMessage);
}
