package com.antonioteca.cc42.dao.daoapi;

import com.antonioteca.cc42.network.NotificationExpo.ExpoPushMessage;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface ExpoPushServiceApi {
    String EXPO_PUSH_URL = "https://exp.host/--/api/v2/"; // Base URL

    @Headers({
            "Accept: application/json",
            "Accept-Encoding: gzip, deflate",
            "Content-Type: application/json"
    })
    @POST("push/send")
    Call<Void> sendPushNotifications(@Body List<ExpoPushMessage> messages);
}
