package com.antonioteca.cc42.network.NotificationExpo;

import java.util.Map;

public class ExpoPushMessage {
    private String to;
    private String sound = "default"; // Valor padrão
    private String title;
    private String body;
    private Map<String, Object> data;
    private String image;

    // Construtor, getters e setters
    public ExpoPushMessage(String to, String title, String body, Map<String, Object> data, String image) {
        this.to = to;
        this.title = title;
        this.body = body;
        this.data = data;
        this.image = image;
    }

    // Getters e Setters para todos os campos
    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getSound() {
        return sound;
    }

    public void setSound(String sound) {
        this.sound = sound;
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
