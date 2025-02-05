package com.antonioteca.cc42.network;

import android.app.Application;

import com.cloudinary.android.MediaManager;

import java.util.HashMap;
import java.util.Map;

public class MyApplicationCloudinary extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        initCloudinary();
    }

    private void initCloudinary() {
        Map<String, String> config = new HashMap<>();
        config.put("cloud_name", "cc42");
        config.put("api_key", "926854887914134");
        config.put("api_secret", "7-Yu182b1ObCV-1AY7jvXWtZRhI"); // Opcional no cliente Android
        MediaManager.init(this, config);
    }
}
