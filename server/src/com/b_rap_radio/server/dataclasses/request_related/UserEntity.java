package com.b_rap_radio.server.dataclasses.request_related;

import com.google.gson.annotations.SerializedName;

import java.util.Collections;
import java.util.List;


public class UserEntity {
    public static final String ID = "id";
    public static final String PSEUDO = "nickname";
    public static final String EMAIL = "email";
    public static final String STATUS = "status";
    public static final String CREDITS = "credits";
    public static final String CURRENT_SANCTIONS = "current_sanctions";

    @SerializedName(ID)
    private final Integer id;

    @SerializedName(PSEUDO)
    private final String nickname;

    @SerializedName(EMAIL)
    private final String email;

    @SerializedName(STATUS)
    private final List<String> status;

    @SerializedName(CREDITS)
    private final Integer credits;


    public UserEntity(Integer id, String email, String nick, List<String> status, Integer credits) {
        this.id = id;
        this.nickname = nick;
        this.email = email;
        this.status = status;
        this.credits = credits;
    }

    public UserEntity(Integer id, String email, String nick, String status ,Integer credits) {
        this(id, nick, email, Collections.singletonList(status), credits);
    }

    public int getId() { return id; }

    public String getNickname() { return nickname; }

    public String getEmail() { return email; }

    public List<String> getStatus() { return status; }

    public int getCredits() { return credits; }

}
