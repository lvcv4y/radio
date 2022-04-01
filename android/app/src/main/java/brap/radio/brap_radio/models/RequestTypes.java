package brap.radio.brap_radio.models;

import androidx.annotation.NonNull;

public enum RequestTypes {
    LOCAL_PLAYING("LOCAL_PLAYING"),
    GET_MUSIC_INFOS("GET_MUSIC_INFOS"),
    PLAY_NEXT("PLAY_NEXT"),
    NEGATIVE_VOTE("VOTE_N"),
    POSITIVE_VOTE("VOTE_P");

    private final String value;
    RequestTypes(String value){
        this.value = value;
    }
    
    @NonNull
    @Override
    public String toString() {
        return value;
    }
}
