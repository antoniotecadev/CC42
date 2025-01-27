package com.antonioteca.cc42.network;

import android.content.Context;
import android.util.Log;

import com.antonioteca.cc42.R;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class HttpException {

    private final int code;
    private final String description;

    public HttpException(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public int getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static HttpException handleException(Throwable e, Context context) {
        List<String> stringListException = new ArrayList<>();
        Log.e("HttpException", "An error occurred during the request:", e);
        if (e instanceof IOException) {
            Log.e(context.getString(R.string.net_err), "Network error occurred: " + e.getMessage());
            stringListException.add("Network error occurred");
        } else if (e instanceof JSONException) {
            Log.e(context.getString(R.string.jso_err), "Error parsing JSON response: " + e.getMessage());
            stringListException.add("Error parsing JSON response");
        } else {
            String message = e.getMessage() != null ? e.getMessage() : "";
            Log.e(context.getString(R.string.err), "Unknown error occurred: " + message);
            stringListException.add("Unknown error occurred: " + message);
        }
        return new HttpException(100, stringListException.get(0));
    }
}
