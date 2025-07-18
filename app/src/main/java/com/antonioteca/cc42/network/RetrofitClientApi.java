package com.antonioteca.cc42.network;

import android.content.Context;

import java.io.File;
import java.util.concurrent.TimeUnit;

import okhttp3.Cache;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Configurar o Retrofit e retorna a instância do RetrofitClientApi
 */

public class RetrofitClientApi {

    private static final String BASE_URL = "https://api.intra.42.fr";
    private static Retrofit retrofit = null;

    public static Retrofit getApiService(Context context) {

        if (retrofit == null) {

            // Configurar cache do OkHttp
            int cacheSize = 10 * 1024 * 1024; // 10 MB
            File cacheDir = new File(context.getCacheDir(), "http-cache");
            Cache cache = new Cache(cacheDir, cacheSize);

            HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
            interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .cache(cache)
                    .addInterceptor(interceptor)
                    .addInterceptor(chain -> { // Opcional: Interceptor para forçar cache ou logar
                        okhttp3.Request request = chain.request();
                        // Se não houver conexão, tente usar o cache
                        if (!isNetworkAvailable(context)) {
                            request = request.newBuilder()
                                    .header("Cache-Control", "public, only-if-cached, max-stale=" + 60 * 60 * 24 * 2) // 2 dias
                                    .build();
                        }
                        return chain.proceed(request);
                    })
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(okHttpClient)
                    .build();
        }
        return retrofit;
    }

    // Método auxiliar para verificar a disponibilidade da rede
    private static boolean isNetworkAvailable(Context context) {
        android.net.ConnectivityManager connectivityManager
                = (android.net.ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        android.net.NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();
    }
}



