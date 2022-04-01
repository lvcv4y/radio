package com.b_rap_radio.server.dataclasses;

public class Album {
    private final String name, imageUrl;

    public Album(String name, String imageUrl) {
        this.name = name;
        this.imageUrl = imageUrl;
    }

    public String getName() {
        return name;
    }

    public String getImageUrl() {
        return imageUrl;
    }
}
