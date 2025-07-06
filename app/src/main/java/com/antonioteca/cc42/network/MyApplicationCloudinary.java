package com.antonioteca.cc42.network;

import static com.antonioteca.cc42.network.FirebaseDataBaseInstance.fetchApiKeyFromDatabase;

import android.app.Application;
import android.content.Context;

import com.cloudinary.android.MediaManager;

import java.util.HashMap;
import java.util.Map;

public class MyApplicationCloudinary extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        // initCloudinary(this);
    }

//    private void initCloudinary(Context context) {
//        fetchApiKeyFromDatabase("cloudinary", context, apiKey -> {
//            Map<String, String> config = new HashMap<>();
//            config.put("cloud_name", "cc42");
//            config.put("api_key", "926854887914134");
//            config.put("api_secret", apiKey);
//            MediaManager.init(this, config);
//        });
//    }
}
