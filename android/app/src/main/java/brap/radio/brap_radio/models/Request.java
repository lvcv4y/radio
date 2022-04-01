package brap.radio.brap_radio.models;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Request {

    public static final String TYPE = "type";
    public static final String IS_ERROR = "is_error";
    public static final String ERROR_MSG = "error_msg";
    public static final String STR_ARGS = "string_args";
    public static final String MUSIC_INFOS_ARGS = "music_args";

    @SerializedName(TYPE)
    private final String type;

    @SerializedName(IS_ERROR)
    private final boolean isError;

    @SerializedName(ERROR_MSG)
    private final String errorMessage;

    @SerializedName(STR_ARGS)
    private List<String> stringArgs;

    @SerializedName(MUSIC_INFOS_ARGS)
    private List<MusicInfos> musicArgs;


    public Request(String type, boolean isError, String errorMessage){
        this.type = type;
        this.isError = isError;
        this.errorMessage = errorMessage;
    }

    public Request(String type){
        this(type, false, null);
    }
    
    public Request(RequestTypes type){
        this(type.toString(), false, null);
    }
    
    public Request(RequestTypes type, String errorMessage){
        this(type.toString(), true, errorMessage);
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


    // GETTERS

    public String getType() { return type; }

    public boolean isError() { return isError; }

    public String getErrorMessage() { return errorMessage; }


    public List<String> getStringArgs() { return stringArgs; }

    public List<MusicInfos> getMusicArgs() { return musicArgs; }


    // SETTERS


    public void setStringArgs(List<String> stringArgs) { this.stringArgs = stringArgs; }

    public void setMusicArgs(List<MusicInfos> musicArgs) { this.musicArgs = musicArgs; }

}
