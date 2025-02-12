package com.antonioteca.cc42.network.NotificationFirebase;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface FCMService {
    @POST("v1/projects/cadet-check-cc42/messages:send")
    Call<Void> sendMessage(
            @Header("Authorization") String authorization,
            @Body FCMessage message
    );
}
