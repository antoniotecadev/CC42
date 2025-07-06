package com.antonioteca.cc42.dao.daoapi;

import com.antonioteca.cc42.model.Token;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface DaoApiToken {
//    AO LOGAR NO CLIENTE
//    @FormUrlEncoded //  setHeader("Content-Type", "application/x-www-form-urlencoded")
//    @POST("/oauth/token") //    load("POST", url)
//    Call<Token> getAccessToken( // setBodyParameter
//                                @Field("grant_type") String grantType, //   authorization_code
//                                @Field("client_id") String clientId, //  u-s1t7xz-9cd0m13...
//                                @Field("client_secret") String clientSecret, // u-s1t7xz-9cd0m13...
//                                @Field("code") String code, //  u-s1t7xz-9cd0m13...
//                                @Field("redirect_uri") String redirectUri //    cc42://checkcadet42
//    );

    @FormUrlEncoded
    @POST("/oauth/token")
    Call<Token> getRefreshToken(
            @Field("grant_type") String grantType, //   refresh_token
            @Field("refresh_token") String refreshToken,
            @Field("client_id") String clientId,
            @Field("client_secret") String clientSecret
    );
}
