package com.antonioteca.cc42.network.NotificationFirebase;

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
        private String image;

        public Notification(String title, String body, String image) {
            this.title = title;
            this.body = body;
            this.image = image;
        }
    }

    public static class Data {
        private String key1;
        private String key2;
        private String key3;
        private String key4;

        public Data(String key1, String key2, String key3, String key4) {
            this.key1 = key1;
            this.key2 = key2;
            this.key3 = key3;
            this.key4 = key4;
        }
    }
}