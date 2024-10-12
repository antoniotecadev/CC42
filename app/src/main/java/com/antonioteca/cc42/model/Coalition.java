package com.antonioteca.cc42.model;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.annotations.SerializedName;

public class Coalition {
    public String name;

    @SerializedName("cover_url")
    public String image_url;

    public String color;

    private final SharedPreferences preferences;

    public Coalition(Context context) {
        preferences = context.getSharedPreferences("MyAppPrefsUser", MODE_PRIVATE);
    }

    public String getName() {
        return preferences.getString("name_coalition", null);
    }

    public String getImageUrl() {
        return preferences.getString("image_url_coalition", null);
    }

    public String getColor() {
        return preferences.getString("color_coalition", null);
    }
}
