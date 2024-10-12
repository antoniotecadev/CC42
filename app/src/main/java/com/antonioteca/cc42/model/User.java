package com.antonioteca.cc42.model;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Classe que representam os dados da aplicação
 */

public class User {

    @SerializedName("id")
    public String uid;

    public String email;
    public String login;

    @SerializedName("displayname")
    public String displayName;

    public String url;

    public Image image;

    public List<Campus> campus;

    public Coalition coalition;

    private final SharedPreferences preferences;
    private final SharedPreferences.Editor editor;

    public User(Context context) {
        preferences = context.getSharedPreferences("MyAppPrefsUser", MODE_PRIVATE);
        editor = preferences.edit();
    }

    public boolean saveUser(User user, Coalition coalition) {
        editor.putString("uid", user.uid);
        editor.putString("email", user.email);
        editor.putString("login", user.login);
        editor.putString("display_name", user.displayName);
        editor.putString("url", user.url.trim());
        editor.putString("image_link", user.image.link.trim());
        editor.putString("campus_id", user.campus.get(0).id);
        if (coalition != null) {
            editor.putString("name_coalition", coalition.name);
            editor.putString("image_url_coalition", coalition.image_url.trim());
            editor.putString("color_coalition", coalition.color.trim());
        }
        return commit(); // ou editor.apply() se preferir
    }

    public String getUid() {
        return preferences.getString("uid", null);
    }

    public String getEmail() {
        return preferences.getString("email", null);
    }

    public String getLogin() {
        return preferences.getString("login", null);
    }

    public String getDisplayName() {
        return preferences.getString("display_name", null);
    }

    public String getUrl() {
        return preferences.getString("url", null);
    }

    public String getImage() {
        return preferences.getString("image_link", null);
    }

    public String getCampusId() {
        return preferences.getString("campus_id", null);
    }

    public void setCoalition(Coalition coalition) {
        this.coalition = coalition;
    }

    public void clear() {
        editor.clear();
        commit();
    }

    public boolean commit() {
        return editor.commit(); // ou editor.apply() se preferir
    }
}

class Image {
    public String link;
}

class Campus {
    public String id;
}