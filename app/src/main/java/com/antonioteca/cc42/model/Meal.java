package com.antonioteca.cc42.model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

public class Meal implements Parcelable {
    private String id;
    private String type;
    private String name;
    private int quantity;
    private String createdDate;
    private String pathImage;
    private String createdBy;

    public Meal() {
    }

    public Meal(String id, String type, String name, int quantity, String pathImage, String createdBy, String createdDate) {
        this.id = id;
        this.type = type;
        this.name = name;
        this.quantity = quantity;
        this.pathImage = pathImage;
        this.createdBy = createdBy;
        this.createdDate = createdDate;
    }

    protected Meal(Parcel in) {
        id = in.readString();
        type = in.readString();
        name = in.readString();
        quantity = in.readInt();
        createdDate = in.readString();
        pathImage = in.readString();
        createdBy = in.readString();
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(type);
        dest.writeString(name);
        dest.writeInt(quantity);
        dest.writeString(createdDate);
        dest.writeString(pathImage);
        dest.writeString(createdBy);
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

    public String getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(String date) {
        this.createdDate = date;
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

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }
}
