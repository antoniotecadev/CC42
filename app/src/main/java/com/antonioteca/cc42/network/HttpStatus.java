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
    METHOD_NOT_ALLOWED(405, "Method Not Allowed"),
    CONFLICT(409, "Conflict"),
    REQUEST_TIMEOUT(408, "Request Timeout"),
    LENGTH_REQUIRED(411, "Length Required"),
    PRECONDITION_FAILED(412, "Precondition Failed"),
    GONE(410, "Gone"),
    UNSUPPORTED_MEDIA_TYPE(415, "Unsupported Media Type"),
    UNPROCESSABLE_ENTITY(422, "Unprocessable entity"),
    TOO_MANY_REQUESTS(429, "Too Many Requests"),
    INTERNAL_SERVER_ERROR(500, "Internal Server Error"),
    NOT_IMPLEMENTED(501, "Not Implemented"),
    BAD_GATEWAY(502, "Bad Gateway"),
    SERVICE_UNAVAILABLE(503, "Service Unavailable"),
    GATEWAY_TIMEOUT(504, "Gateway Timeout"),
    HTTP_VERSION_NOT_SUPPORTED(505, "HTTP Version Not Supported"),
    INSUFFICIENT_STORAGE(507, "Insufficient Storage"),
    NETWORK_CONNECTIVITY_ERROR(511, "Network Connectivity Error");

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
        return switch (status) {
            //case OK: // Removido pois não é um código de erro
            //    Log.i("HTTP_OK", "Request was successful: " + status.getDescription());
            //    return OK;
            case THE_REQUEST_IS_MALFORMED -> {
                Log.e("REQUEST_IS_MALFORMED", "Formated: " + status.getDescription());
                yield THE_REQUEST_IS_MALFORMED;
            }
            case UNAUTHORIZED -> {
                Log.e("UNAUTHORIZED", "Unauthorized access: " + status.getDescription());
                yield UNAUTHORIZED;
            }
            case FORBIDDEN -> {
                Log.e("FORBIDDEN", "Resource forbidden: " + status.getDescription());
                yield FORBIDDEN;
            }
            case NOT_FOUND -> {
                Log.e("NOT_FOUND", "Resource not found: " + status.getDescription());
                yield NOT_FOUND;
            }
            case METHOD_NOT_ALLOWED -> {
                Log.e("METHOD_NOT_ALLOWED", "Method not allowed: " + status.getDescription());
                yield METHOD_NOT_ALLOWED;
            }
            case CONFLICT -> {
                Log.e("CONFLICT", "Conflict with current state of the resource: " + status.getDescription());
                yield CONFLICT;
            }
            case REQUEST_TIMEOUT -> {
                Log.e("REQUEST_TIMEOUT", "Request timeout: " + status.getDescription());
                yield REQUEST_TIMEOUT;
            }
            case LENGTH_REQUIRED -> {
                Log.e("LENGTH_REQUIRED", "Length required: " + status.getDescription());
                yield LENGTH_REQUIRED;
            }
            case PRECONDITION_FAILED -> {
                Log.e("PRECONDITION_FAILED", "Precondition failed: " + status.getDescription());
                yield PRECONDITION_FAILED;
            }
            case GONE -> {
                Log.e("GONE", "Resource is no longer available: " + status.getDescription());
                yield GONE;
            }
            case UNSUPPORTED_MEDIA_TYPE -> {
                Log.e("UNSUPPORTED_MEDIA_TYPE", "Unsupported media type: " + status.getDescription());
                yield UNSUPPORTED_MEDIA_TYPE;
            }
            case UNPROCESSABLE_ENTITY -> {
                Log.e("UNPROCESSABLE_ENTITY", "Resource unprocessable: " + status.getDescription());
                yield UNPROCESSABLE_ENTITY;
            }
            case TOO_MANY_REQUESTS -> {
                Log.e("TOO_MANY_REQUESTS", "Too many requests: " + status.getDescription());
                yield TOO_MANY_REQUESTS;
            }
            case INTERNAL_SERVER_ERROR -> {
                Log.e("INTERNAL_SERVER_ERROR", "Server error: " + status.getDescription());
                yield INTERNAL_SERVER_ERROR;
            }
            case NOT_IMPLEMENTED -> {
                Log.e("NOT_IMPLEMENTED", "Functionality not implemented: " + status.getDescription());
                yield NOT_IMPLEMENTED;
            }
            case BAD_GATEWAY -> {
                Log.e("BAD_GATEWAY", "Bad gateway: " + status.getDescription());
                yield BAD_GATEWAY;
            }
            case SERVICE_UNAVAILABLE -> {
                Log.e("SERVICE_UNAVAILABLE", "Service unavailable: " + status.getDescription());
                yield SERVICE_UNAVAILABLE;
            }
            case GATEWAY_TIMEOUT -> {
                Log.e("GATEWAY_TIMEOUT", "Gateway timeout: " + status.getDescription());
                yield GATEWAY_TIMEOUT;
            }
            case HTTP_VERSION_NOT_SUPPORTED -> {
                Log.e("HTTP_VERS_NOT_SUPPORTED", "HTTP version not supported: " + status.getDescription());
                yield HTTP_VERSION_NOT_SUPPORTED;
            }
            case INSUFFICIENT_STORAGE -> {
                Log.e("INSUFFICIENT_STORAGE", "Insufficient storage: " + status.getDescription());
                yield INSUFFICIENT_STORAGE;
            }
            case NETWORK_CONNECTIVITY_ERROR -> {
                Log.e("NETWORK_CONNEC_ERROR", "Network connectivity error: " + status.getDescription());
                yield NETWORK_CONNECTIVITY_ERROR;
            }
            default -> throw new IllegalStateException("Unexpected value: " + status);
        };
    }
}
