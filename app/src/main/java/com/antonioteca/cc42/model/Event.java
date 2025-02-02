package com.antonioteca.cc42.model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

public class Event implements Parcelable {

    private long id;
    private String name;
    private String description;
    private String location;
    private String kind;
    private int max_people;
    private int nbr_subscribers;
    private String begin_at;
    private String end_at;
    private List<Integer> cursus_ids;

    public Event() {
    }

    protected Event(Parcel in) {
        id = in.readLong();
        name = in.readString();
        description = in.readString();
        location = in.readString();
        kind = in.readString();
        max_people = in.readInt();
        nbr_subscribers = in.readInt();
        begin_at = in.readString();
        end_at = in.readString();
        int size = in.readInt();
        cursus_ids = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            cursus_ids.add(in.readInt());
        }
    }

    public static final Creator<Event> CREATOR = new Creator<Event>() {
        @Override
        public Event createFromParcel(Parcel in) {
            return new Event(in);
        }

        @Override
        public Event[] newArray(int size) {
            return new Event[size];
        }
    };

    public long getId() {
        return id;
    }

    public void setId(long id) {
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

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public int getMax_people() {
        return max_people;
    }

    public void setMax_people(int max_people) {
        this.max_people = max_people;
    }

    public int getNbr_subscribers() {
        return nbr_subscribers;
    }

    public void setNbr_subscribers(int nbr_subscribers) {
        this.nbr_subscribers = nbr_subscribers;
    }

    public String getBegin_at() {
        return begin_at;
    }

    public void setBegin_at(String begin_at) {
        this.begin_at = begin_at;
    }

    public String getEnd_at() {
        return end_at;
    }

    public void setEnd_at(String end_at) {
        this.end_at = end_at;
    }

    public List<Integer> getCursus_ids() {
        return cursus_ids;
    }

    public void setCursus_ids(List<Integer> cursus_ids) {
        this.cursus_ids = cursus_ids;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel parcel, int i) {
        parcel.writeLong(id);
        parcel.writeString(name);
        parcel.writeString(description);
        parcel.writeString(location);
        parcel.writeString(kind);
        parcel.writeInt(max_people);
        parcel.writeInt(nbr_subscribers);
        parcel.writeString(begin_at);
        parcel.writeString(end_at);
        parcel.writeInt(cursus_ids != null ? cursus_ids.size() : 0);
        if (cursus_ids != null) {
            for (Integer id : cursus_ids) {
                parcel.writeInt(id);
            }
        }
    }
}
