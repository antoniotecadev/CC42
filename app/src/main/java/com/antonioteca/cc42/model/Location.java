package com.antonioteca.cc42.model;

import com.google.firebase.database.Exclude;

public class Location {
    public String areaId;
    public String areaName;
    public String pushToken;
    public long lastUpdated;
    /**
     * Percentual (0.0 - 1.0) relativo à largura/altura do container onde a imagem é desenhada.
     * top e left são a posição do canto superior esquerdo da área (em percentuais).
     * width e height também em percentuais do contêiner.
     */
    @Exclude
    public float top;
    @Exclude
    public float left;
    @Exclude
    public float width;
    @Exclude
    public float height;
    @Exclude
    public int color; // android color int (opcional, para debug/visual)

    public Location() {
    }

    public Location(String areaId, String areaName, String pushToken, long lastUpdated) {
        this.areaId = areaId;
        this.areaName = areaName;
        this.pushToken = pushToken;
        this.lastUpdated = lastUpdated;
    }

    public Location(String id, String name, float top, float left, float width, float height, int color) {
        this.areaId = id;
        this.areaName = name;
        this.top = top;
        this.left = left;
        this.width = width;
        this.height = height;
        this.color = color;
    }
}
