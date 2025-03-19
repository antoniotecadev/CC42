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
        private String icon;
        private String title;
        private String body;
        private String image;

        public Notification(String icon, String title, String body, String image) {
            this.icon = icon;
            this.title = title;
            this.body = body;
            this.image = image;
        }

        public String getIcon() {
            return icon;
        }

        public String getTitle() {
            return title;
        }

        public String getBody() {
            return body;
        }

        public String getImage() {
            return image;
        }
    }

    public static class Data {

        private String key0;
        private String key1;
        private String key2;
        private String key3;
        private String key4;
        private String key5;
        private String title;
        private String body;
        private String image;

        public Data(String key0, String key1, String key2, String key3, String key4, String key5, Notification notification) {
            this.key0 = key0;
            this.key1 = key1;
            this.key2 = key2;
            this.key3 = key3;
            this.key4 = key4;
            this.key5 = key5;
            this.title = notification.getTitle();
            this.body = notification.getBody();
            this.image = notification.getImage();
        }
    }
}