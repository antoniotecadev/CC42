package com.antonioteca.cc42.model;

import android.os.Parcel;
import android.os.Parcelable;

public class Meal implements Parcelable {
    private String id;
    private String type;
    private String name;
    private int quantity;
    private String date;
    private String pathImage;

    public Meal() {
    }

    public Meal(String id, String type, String name, int quantity, String date, String pathImage) {
        this.id = id;
        this.type = type;
        this.name = name;
        this.quantity = quantity;
        this.date = date;
        this.pathImage = pathImage;
    }

    protected Meal(Parcel in) {
        id = in.readString();
        type = in.readString();
        name = in.readString();
        quantity = in.readInt();
        date = in.readString();
        pathImage = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(type);
        dest.writeString(name);
        dest.writeInt(quantity);
        dest.writeString(date);
        dest.writeString(pathImage);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Meal> CREATOR = new Creator<>() {
        @Override
        public Meal createFromParcel(Parcel in) {
            return new Meal(in);
        }

        @Override
        public Meal[] newArray(int size) {
            return new Meal[size];
        }
    };

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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
