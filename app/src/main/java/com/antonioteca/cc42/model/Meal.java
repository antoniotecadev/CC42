package com.antonioteca.cc42.model;

public class Meal {
    private String id;
    private String name;
    private String description;
    private String date;
    private int quantity;
    private String pathImage;

    public Meal() {
    }

    public Meal(String id, String name, String description, String date, int quantity, String pathImage) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.date = date;
        this.quantity = quantity;
        this.pathImage = pathImage;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getPathImage() {
        return pathImage;
    }

    public void setPathImage(String pathImage) {
        this.pathImage = pathImage;
    }
}
