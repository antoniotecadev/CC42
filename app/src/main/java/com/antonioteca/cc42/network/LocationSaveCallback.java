package com.antonioteca.cc42.network;

import com.antonioteca.cc42.model.Location;

public interface LocationSaveCallback {
    void onSuccess();

    void onError(Exception e);

    void onComplete(Location location);
}
