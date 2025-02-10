package com.antonioteca.cc42.model;

import com.google.gson.annotations.SerializedName;

public class Subscription {

    @SerializedName("user")
    private User users;

    public User getUsers() {
        return users;
    }
}
