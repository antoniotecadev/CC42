package com.antonioteca.cc42.model;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;
import java.util.Objects;

/**
 * Classe que representam os dados da aplicação
 */

public class User {

    @SerializedName("id")
    public Long uid;

    public String email;
    public String login;

    @SerializedName("displayname")
    public String displayName;

    public String url;

    private String kind;

    public Image image;

    public List<Campus> campus;

    @SerializedName("projects_users")
    public List<ProjectUser> projectsUsers;

    public Coalition coalition;

    @SerializedName("staff?")
    private boolean isStaff;

    @Expose(serialize = false, deserialize = false)
    public int ratingValue = 0;

    @Expose(serialize = false, deserialize = false)
    public Boolean present = null;

    @Expose(serialize = false, deserialize = false)
    public Boolean subscription = null;

    @Expose(serialize = false, deserialize = false)
    private final SharedPreferences preferences;
    @Expose(serialize = false, deserialize = false)
    private final SharedPreferences.Editor editor;

    public User(Context context) {
        preferences = context.getSharedPreferences("MyAppPrefsUser", MODE_PRIVATE);
        editor = preferences.edit();
    }

    public boolean saveUser(User user, Coalition coalition) {
        editor.putLong("uid", user.uid);
        editor.putString("email", user.email);
        editor.putString("login", user.login);
        editor.putString("display_name", user.displayName);
        editor.putString("url", user.url.trim());
        if (user.image != null && user.image.link != null)
            editor.putString("image_link", user.image.link.trim());
        editor.putInt("campus_id", user.campus.get(0).id);
        editor.putString("campus_name", user.campus.get(0).name.trim());
        if (!user.isStaff)
            editor.putInt("cursus_id", user.projectsUsers.get(0).cursusIds.get(0));
        if (coalition != null) {
            editor.putString("name_coalition", coalition.name);
            editor.putString("image_url_coalition", coalition.image_url.trim());
            editor.putString("color_coalition", coalition.color.trim());
        }
        editor.putBoolean("staff?", user.isStaff);
        return commit(); // ou editor.apply() se preferir.
    }

    public Long getUid() {
        return preferences.getLong("uid", 0);
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

    public String getKind() {
        return kind;
    }

    public String getImage() {
        return preferences.getString("image_link", null);
    }

    public String getUrlImageUser() {
        return image.link;
    }

    public int getCampusId() {
        return preferences.getInt("campus_id", 0);
    }

    public String getCampusName() {
        return preferences.getString("campus_name", null);
    }

    public int getCursusId() {
        return preferences.getInt("cursus_id", 0);
    }

    public boolean isStaff() {
        return preferences.getBoolean("staff?", false);
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

    public Boolean isPresent() {
        return present;
    }

    public void setPresent(Boolean present) {
        this.present = present;
    }


    public Boolean isSubscription() {
        return subscription;
    }

    public void setSubscription(Boolean subscription) {
        this.subscription = subscription;
    }

    public boolean getSubscribedToTopicMealNotification() {
        return preferences.getBoolean("subscribedToTopicMealNotification", false);
    }

    public void setSubscribedToTopicMealNotification(boolean value) {
        editor.putBoolean("subscribedToTopicMealNotification", value);
    }

    // equals e hashCode são cruciais para DiffUtil.areContentsTheSame
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(uid, user.uid) && // Garante que o ID também seja comparado
                Objects.equals(present, user.present) &&
                Objects.equals(subscription, user.subscription);
        // Compare todos os campos relevantes para o conteúdo visual
    }

    @Override
    public int hashCode() {
        return Objects.hash(uid, present, subscription);
        // Use os mesmos campos do equals
    }
}

class Image {
    public String link;
}

class Campus {
    public int id;
    public String name;
}

class ProjectUser {
    @SerializedName("cursus_ids")
    public List<Integer> cursusIds;
}