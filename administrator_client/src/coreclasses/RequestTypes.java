package coreclasses;

public enum RequestTypes {

    LOGIN("LOGIN"),

    END("END"), GET_MUSIC_INFOS("GET_MUSIC_INFOS"),
    ADDED("ADDED"), ERROR("ERROR"),

    CHAT_MSG("CHAT_MSG"), ADMIN_CHAT_MSG("ADMIN_CHAT_MSG"), PLAYING("PLAYING"), CLEAR_EVENTS("CLEAR_EVENTS"),
    CURRENT_EVENT("CURRENT_EVENT"),

    CHANNEL_CONNECT("CHANNEL_CONNECT"), CHANNEL_DISCONNECT("CHANNEL_DISCONNECT"), GET_CHANNEL_USERS("GET_CHANNEL_LIST"),

    ADD_SANCTION("ADD_SANCTION"), ADD_PLAYLIST("ADD_PLAYLIST"), ADD_HISTORY("ADD_HISTORY"), ADD_USER("ADD_USER"),
    ADD_EVENT("ADD_EVENT"),

    DELETE_SANCTION("DELETE_SANCTION"), DELETE_PLAYLIST("DELETE_PLAYLIST"), DELETE_HISTORY("DELETE_HISTORY"),
    DELETE_USER("DELETE_USER"), DELETE_EVENT("DELETE_EVENT"),

    GET_PLAYLIST("GET_PLAYLIST"), GET_USERS("GET_USERS"), GET_SANCTIONS("GET_SANCTIONS"),
    GET_EVENT("GET_EVENT"), GET_STAFF("GET_STAFF"), GET_HISTORY("GET_HISTORY"),

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

