package com.antonioteca.cc42.model;

import com.google.gson.annotations.SerializedName;

public class Subscription {

    public String grade;
    @SerializedName("user")
    private User users;

    public User getUsers() {
        return users;
    }
}
