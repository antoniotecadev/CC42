package com.antonioteca.cc42.dao.daoapi;

import com.antonioteca.cc42.model.Cursu;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Query;

public interface DaoApiCursu {
    @GET("/v2/cursus")
    Call<List<Cursu>> getCursus(
            @Header("Authorization") String accessToken,
            @Query("page[number]") int pageNumber,  // Adiciona o número da página
            @Query("page[size]") int pageSize       // Adiciona o tamanho da página
    );
}
