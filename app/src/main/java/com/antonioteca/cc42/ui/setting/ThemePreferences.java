package com.antonioteca.cc42.ui.setting;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatDelegate;

public class ThemePreferences {
    private static final String PREF_NAME = "theme_pref";
    private static final String KEY_THEME = "theme_mode";

    private final SharedPreferences sharedPreferences;

    public ThemePreferences(Context context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void setThemeMode(int mode) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(KEY_THEME, mode);
        editor.apply();
    }

    public int getThemeMode() {
        return sharedPreferences.getInt(KEY_THEME, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
    }
}
