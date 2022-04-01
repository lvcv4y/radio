package com.b_rap_radio.server.dataclasses;

public class Artist {
    private final String name, spotify_id, deezer_id;

    public Artist(String name, String spotify_id, String deezer_id) {
        this.name = name;
        this.spotify_id = spotify_id;
        this.deezer_id = deezer_id;
    }

    public String getName() {
        return name;
    }

    public String getSpotify_id() {
        return spotify_id;
    }

    public String getDeezer_id() {
        return deezer_id;
    }
}
