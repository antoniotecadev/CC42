package com.antonioteca.cc42.network.NotificationFirebase;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.antonioteca.cc42.R;
import com.google.auth.oauth2.GoogleCredentials;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AccessTokenGenerator {
    public static void getAccessToken(Context context, TokenCallback tokenCallback) throws IOException {
        InputStream serviceAccount = context.getResources().openRawResource(R.raw.service_account);
        GoogleCredentials credentials = GoogleCredentials.fromStream(serviceAccount)
                .createScoped(Collections.singleton("https://www.googleapis.com/auth/firebase.messaging"));
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(() -> {
            try {
                credentials.refreshIfExpired();
                new Handler(Looper.getMainLooper()).post(() ->
                        tokenCallback.onTokenReceived(credentials.getAccessToken().getTokenValue()));
            } catch (IOException e) {
                e.printStackTrace();
                new Handler(Looper.getMainLooper()).post(() ->
                        tokenCallback.onTokenReceived(null));
            } finally {
                executorService.shutdown();
            }
        });
    }
}
