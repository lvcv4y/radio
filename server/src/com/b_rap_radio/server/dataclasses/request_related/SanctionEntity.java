package com.b_rap_radio.server.dataclasses.request_related;

import com.google.gson.annotations.SerializedName;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SanctionEntity {

    public static final String ID = "id";
    public static final String TYPE = "type";
    public static final String USER = "user";
    public static final String DESC = "description";
    public static final String START_AT = "start_at";
    public static final String END_AT = "end_at";
    public static final SimpleDateFormat dateFormat = JSONConverter.dateFormat;

    @SerializedName(ID)
    private final Integer id;

    @SerializedName(TYPE)
    private final String type;

    @SerializedName(USER)
    private UserEntity user;

    @SerializedName(DESC)
    private final String description;

    @SerializedName(START_AT)
    private final Date startAt;

    @SerializedName(END_AT)
    private final Date endAt;

    public SanctionEntity(Integer id, String type, UserEntity user, String description, Date startAt, Date endAt) {
        this.id = id;
        this.user = user;
        this.description = description;
        this.startAt = startAt;
        this.endAt = endAt;
        this.type = type;
    }

    public SanctionEntity(String type, UserEntity user, String description, String startAt, String endAt) throws ParseException {
        this(null, type, user, description, dateFormat.parse(startAt), dateFormat.parse(endAt));
    }

    public SanctionEntity(String type, UserEntity user, String description, Date startAt, Date endAt) {
        this(null, type, user, description, startAt, endAt);
    }

    public SanctionEntity(Integer id, String type, UserEntity user, String description, String startAt, String endAt) throws ParseException {
        this(id, type, user, description, dateFormat.parse(startAt), dateFormat.parse(endAt));
    }

    public int getId() { return id; }

    public String getUserPseudo() { return user.getNickname(); }

    public int getUserId() { return user.getId(); }

    public String getDescription() { return description; }

    public String getStartAt() {
        try {
            return dateFormat.format(startAt);
        } catch (NullPointerException ignored) {
            return null;
        }
    }

    public String getEndAt() {
        try {
            return dateFormat.format(endAt);
        } catch (NullPointerException ignored) {
            return null;
        }
    }

    public String getType() { return type; }

    public void setUser(UserEntity user) {this.user = user;}

    public boolean hasEnded(){
        if(endAt == null)
            return false;

        return new Date().after(endAt);
    }
}
