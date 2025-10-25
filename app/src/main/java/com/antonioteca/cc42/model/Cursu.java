package com.antonioteca.cc42.model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

public class Cursu implements Parcelable {
    private int id;
    private String name;

    private String created_at;

    public Cursu() {
    }

    public Cursu(Parcel in) {
        id = in.readInt();
        name = in.readString();
        created_at = in.readString();
    }

    public static final Creator<Cursu> CREATOR = new Creator<>() {
        @Override
        public Cursu createFromParcel(Parcel in) {
            return new Cursu(in);
        }

        @Override
        public Cursu[] newArray(int size) {
            return new Cursu[size];
        }
    };

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getCreated_at() {
        return created_at;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(name);
        dest.writeString(created_at);
    }
}
