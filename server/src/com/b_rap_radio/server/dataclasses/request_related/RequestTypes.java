package com.b_rap_radio.server.dataclasses.request_related;

public enum RequestTypes {

    POSITIVE_VOTE("VOTE_P"), NEGATIVE_VOTE("VOTE_N"), GET_VOTES_INFOS("GET_VOTES_INFOS"), REFRESH_VOTES("REFRESH_VOTES"),
    MUTE("MUTE"), UNMUTE("UNMUTE"),
     END("END"), GET_MUSIC_INFOS("GET_MUSIC_INFOS"), ADD_MUSIC("ADD_MUSIC"),
    ADDED("ADDED"), ERROR("ERROR"), START_EVENT("START_EVENT"),

    START_STREAMING("START_STREAMING"), INSTANT_START_STREAMING("INSTANT_START_STREAMING"),


    CHAT_MSG("CHAT_MSG"), ADMIN_CHAT_MSG("ADMIN_CHAT_MSG"), PLAYING("PLAYING"), CLEAR_EVENTS("CLEAR_EVENTS"),
    CURRENT_EVENT("CURRENT_EVENT"),

    CHANNEL_CONNECT("CHANNEL_CONNECT"), CHANNEL_DISCONNECT("CHANNEL_DISCONNECT"), GET_CHANNEL_USERS("GET_CHANNEL_LIST"),

    ADD_SANCTION("ADD_SANCTION"), ADD_PLAYLIST("ADD_PLAYLIST"), ADD_HISTORY("ADD_HISTORY"), ADD_USER("ADD_USER"),
    ADD_EVENT("ADD_EVENT"),

    DELETE_SANCTION("DELETE_SANCTION"), DELETE_PLAYLIST("DELETE_PLAYLIST"), DELETE_HISTORY("DELETE_HISTORY"),
    DELETE_USER("DELETE_USER"), DELETE_EVENT("DELETE_EVENT"),


    GET_PLAYLIST("GET_PLAYLIST"), GET_USERS("GET_USERS"), GET_SANCTIONS("GET_SANCTIONS"),
    GET_EVENT("GET_EVENT"), GET_STAFF("GET_STAFF"), GET_HISTORY("GET_HISTORY"), GET_MUSIC("GET_MUSIC"),


    LOGIN("LOGIN"), REGISTER("REGISTER"), GET_ALL_INFOS("get_all_infos"),

    DB_FETCHER("DB FETCHER"),

    PLAY_NEXT("PLAY_NEXT"),

    GET_PORT("GET_PORT");

    private final String name;

    RequestTypes(String typeName) {
        this.name = typeName;
    }

    public String getName() { return name; }

    public static RequestTypes getFromName(final String name) {

        for (RequestTypes type : RequestTypes.values()) {
            if (type.name.equals(name))
                return type;
        }

        return null;
    }
}
