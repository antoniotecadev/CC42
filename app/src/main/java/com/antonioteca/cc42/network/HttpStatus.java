package com.antonioteca.cc42.network;

import android.util.Log;

/**
 * Representar um conjunto fixo de valores
 */

public enum HttpStatus {

    OK(200, "OK"),
    THE_REQUEST_IS_MALFORMED(400, "The request is malformed"),
    UNAUTHORIZED(401, "Unauthorized"),
    FORBIDDEN(403, "Forbidden"),
    NOT_FOUND(404, "Not Found"),
    UNPROCESSABLE_ENTITY(422, "Unprocessable entity"),
    INTERNAL_SERVER_ERROR(500, "Internal Server Error");

    private final int code;
    private final String description;

    HttpStatus(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public int getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public boolean isServerError() {
        return this.code >= 500 && this.code < 600;
    }

    public boolean isClientError() {
        return this.code >= 400 && this.code < 500;
    }

    // Optional: Method to get HttpStatus by code
    private static HttpStatus fromCode(int code) {
        for (HttpStatus status : HttpStatus.values()) {
            if (status.getCode() == code) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown HTTP status code: " + code);
    }

    public static HttpStatus handleResponse(int statusCode) {
        HttpStatus status = HttpStatus.fromCode(statusCode);
        switch (status) {
            case OK:
                Log.i("HTTP_OK", "Request was successful: " + status.getDescription());
                return OK;
            case THE_REQUEST_IS_MALFORMED:
                Log.e("REQUEST_IS_MALFORMED", "Formated: " + status.getDescription());
                return THE_REQUEST_IS_MALFORMED;
            case UNAUTHORIZED:
                Log.e("UNAUTHORIZED", "Unauthorized access: " + status.getDescription());
                return UNAUTHORIZED;
            case FORBIDDEN:
                Log.e("FORBIDDEN", "Resource forbidden: " + status.getDescription());
                return FORBIDDEN;
            case NOT_FOUND:
                Log.e("NOT_FOUND", "Resource not found: " + status.getDescription());
                return NOT_FOUND;
            case UNPROCESSABLE_ENTITY:
                Log.e("UNPROCESSABLE_ENTITY", "Resource unprocessable: " + status.getDescription());
                return UNPROCESSABLE_ENTITY;
            case INTERNAL_SERVER_ERROR:
                Log.e("INTERNAL_SERVER_ERROR", "Server error: " + status.getDescription());
                return INTERNAL_SERVER_ERROR;
            default:
                throw new IllegalStateException("Unexpected value: " + status);
        }
    }
}
