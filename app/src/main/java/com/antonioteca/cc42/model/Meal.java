package com.antonioteca.cc42.model;

import android.os.Parcel;
import android.os.Parcelable;

public class Meal implements Parcelable {
    private String id;
    private String name;
    private String description;
    private int quantity;
    private String type;
    private String date;
    private String pathImage;

    public Meal() {
    }

    public Meal(String id, String name, String description, int quantity, String type, String date, String pathImage) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.quantity = quantity;
        this.type = type;
        this.date = date;
        this.pathImage = pathImage;
    }

    protected Meal(Parcel in) {
        id = in.readString();
        name = in.readString();
        description = in.readString();
        quantity = in.readInt();
        type = in.readString();
        date = in.readString();
        pathImage = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(name);
        dest.writeString(description);
        dest.writeInt(quantity);
        dest.writeString(type);
        dest.writeString(date);
        dest.writeString(pathImage);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Meal> CREATOR = new Creator<Meal>() {
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getPathImage() {
        return pathImage;
    }

    public void setPathImage(String pathImage) {
        this.pathImage = pathImage;
    }
}
