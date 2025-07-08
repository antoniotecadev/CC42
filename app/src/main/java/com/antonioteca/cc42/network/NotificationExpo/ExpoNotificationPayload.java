package com.antonioteca.cc42.network.NotificationExpo;

import java.util.Map;

public class ExpoNotificationPayload {
    private String title;
    private String body;
    private Map<String, Object> data;
    private String image;

    // Construtor, getters e setters
    public ExpoNotificationPayload(String title, String body, Map<String, Object> data, String image) {
        this.title = title;
        this.body = body;
        this.data = data;
        this.image = image;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public Map<String, Object> getData() {
        return data;
    }

    public void setData(Map<String, Object> data) {
        this.data = data;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
}
