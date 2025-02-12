package com.antonioteca.cc42.network.NotificationFirebase;

import android.content.Context;
import android.view.LayoutInflater;

import androidx.annotation.NonNull;

import com.antonioteca.cc42.R;
import com.antonioteca.cc42.network.HttpException;
import com.antonioteca.cc42.network.HttpStatus;
import com.antonioteca.cc42.utility.Util;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class FCMessage {
    private Message message;

    public FCMessage(Message message) {
        this.message = message;
    }

    public static class Message {
        private String topic;
        private Notification notification;
        private Data data;

        public Message(String topic, Notification notification, Data data) {
            this.topic = topic;
            this.notification = notification;
            this.data = data;
        }
    }

    public static class Notification {
        private String title;
        private String body;

        public Notification(String title, String body) {
            this.title = title;
            this.body = body;
        }
    }

    public static class Data {
        private String key1;
        private String key2;

        public Data(String key1, String key2) {
            this.key1 = key1;
            this.key2 = key2;
        }
    }
}