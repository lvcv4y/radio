package com.b_rap_radio.server.dataclasses.request_related;

import com.google.gson.annotations.SerializedName;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Request {

    public static final String TYPE = "type";
    public static final String IS_ERROR = "is_error";
    public static final String ERROR_MSG = "error_msg";
    public static final String INT_ARGS = "int_args";
    public static final String STR_ARGS = "string_args";
    public static final String MUSIC_INFOS_ARGS = "music_args";
    public static final String EVENT_ARGS = "event_args";
    public static final String USER_ENTITY_ARGS = "user_entity_args";
    public static final String SANCTION_ENTITY_ARGS = "sanction_entity_args";


    @SerializedName(TYPE)
    private final String type;

    @SerializedName(IS_ERROR)
    private final boolean isError;

    @SerializedName(ERROR_MSG)
    private final String errorMessage;

    @SerializedName(INT_ARGS)
    private List<Integer> intArgs;

    @SerializedName(STR_ARGS)
    private List<String> stringArgs;

    @SerializedName(MUSIC_INFOS_ARGS)
    private List<MusicInfos> musicArgs;

    @SerializedName(EVENT_ARGS)
    private List<Event> eventArgs;

    @SerializedName(USER_ENTITY_ARGS)
    private List<UserEntity> userArgs;

    @SerializedName(SANCTION_ENTITY_ARGS)
    private List<SanctionEntity> sanctionArgs;

    public Request(RequestTypes type, String errorMessage){this(type.getName(), true, errorMessage); }

    public Request(String type, boolean isError, String errorMessage){
        this.type = type;
        this.isError = isError;
        this.errorMessage = errorMessage;
    }

    public Request(String type){
        this(type, false, null);
    }

    public Request(String type, String errorMessage){
        this(type, true, errorMessage);
    }

    public Request(RequestTypes type, boolean isError, String errorMessage){
        this.type = type.getName();
        this.isError = isError;
        this.errorMessage = errorMessage;
    }

    public Request(RequestTypes type){
        this(type, false, null);
    }

    public void addIntArgs(Integer... args){
        if(intArgs == null)
            intArgs = new ArrayList<>();

        intArgs.addAll(Arrays.asList(args));
    }

    public void addStringArgs(String... args){
        if(stringArgs == null)
            stringArgs = new ArrayList<>();

        stringArgs.addAll(Arrays.asList(args));
    }

    public void addMusicInfosArgs(MusicInfos... args){
        if(musicArgs == null)
            musicArgs = new ArrayList<>();

        musicArgs.addAll(Arrays.asList(args));
    }

    public void addEventArgs(Event... args){
        if(eventArgs == null)
            eventArgs = new ArrayList<>();

        eventArgs.addAll(Arrays.asList(args));
    }

    public void addUserArgs(UserEntity... args){
        if(userArgs == null)
            userArgs = new ArrayList<>();

        userArgs.addAll(Arrays.asList(args));
    }

    public void addSanctionArgs(SanctionEntity... args){
        if(sanctionArgs == null)
            sanctionArgs = new ArrayList<>();

        sanctionArgs.addAll(Arrays.asList(args));
    }

    // GETTERS

    public String getType() { return type; }

    public boolean isError() { return isError; }

    public String getErrorMessage() { return errorMessage; }

    public List<Integer> getIntArgs() { return intArgs; }

    public List<String> getStringArgs() { return stringArgs; }

    public List<MusicInfos> getMusicArgs() { return musicArgs; }

    public List<Event> getEventArgs() { return eventArgs; }

    public List<UserEntity> getUserArgs() { return userArgs; }

    public List<SanctionEntity> getSanctionArgs() { return sanctionArgs; }

    // SETTERS

    public void setIntArgs(@NotNull List<Integer> intArgs) { this.intArgs = intArgs; }

    public void setStringArgs(@NotNull List<String> stringArgs) { this.stringArgs = stringArgs; }

    public void setMusicArgs(@NotNull List<MusicInfos> musicArgs) { this.musicArgs = musicArgs; }

    public void setEventArgs(@NotNull List<Event> eventArgs) { this.eventArgs = eventArgs; }

    public void setUserArgs(@NotNull List<UserEntity> userArgs) { this.userArgs = userArgs; }

    public void setSanctionArgs(List<SanctionEntity> sanctionArgs) { this.sanctionArgs = sanctionArgs; }
}