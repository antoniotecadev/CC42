package com.antonioteca.cc42.model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

public class Meal implements Parcelable {
    private String id;
    private transient int rating;
    private String type;
    private String name;
    private String description;
    private int quantity;
    private String createdDate;
    private String pathImage;
    private String createdBy;
    private transient boolean subscribed;

    public Meal() {
    }

    public Meal(String id, String type, String name, String description, int quantity, String pathImage, int rating, String createdBy, String createdDate) {
        this.id = id;
        this.type = type;
        this.name = name;
        this.description = description;
        this.quantity = quantity;
        this.pathImage = pathImage;
        this.rating = rating;
        this.createdBy = createdBy;
        this.createdDate = createdDate;
    }

    protected Meal(Parcel in) {
        id = in.readString();
        type = in.readString();
        name = in.readString();
        description = in.readString();
        quantity = in.readInt();
        pathImage = in.readString();
        rating = in.readInt();
        createdDate = in.readString();
        createdBy = in.readString();
        subscribed = in.readByte() != 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(type);
        dest.writeString(name);
        dest.writeString(description);
        dest.writeInt(quantity);
        dest.writeString(pathImage);
        dest.writeInt(rating);
        dest.writeString(createdDate);
        dest.writeString(createdBy);
        dest.writeByte((byte) (subscribed ? 1 : 0));
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCreatedDate() {
        return createdDate;
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

    public int getRating() {
        return rating;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public boolean isSubscribed() {
        return subscribed;
    }

    public void setSubscribed(boolean subscribed) {
        this.subscribed = subscribed;
    }
}
