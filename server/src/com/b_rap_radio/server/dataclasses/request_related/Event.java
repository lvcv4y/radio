package com.b_rap_radio.server.dataclasses.request_related;

import com.google.gson.annotations.SerializedName;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Event {

    public static final SimpleDateFormat FORMAT = JSONConverter.dateFormat;

    public static final String TITLE = "title";
    public static final String DESC = "description";
    public static final String START_AT= "start_at";
    public static final String END_AT = "end_at";

    @SerializedName(TITLE)
    private final String title;

    @SerializedName(DESC)
    private final String description;

    @SerializedName(START_AT)
    private final Date startAt;

    @SerializedName(END_AT)
    private final Date endAt;


    public Event(String title, String description, Date startAt, Date endAt) {

        this.title = title;
        this.description = description;
        this.startAt = startAt;
        this.endAt = endAt;
    }

    public Event(String title, String description, String startAt, String endAt) throws ParseException {
        this(title, description, FORMAT.parse(startAt), FORMAT.parse(endAt));
    }
    // GETTERS

    public String getTitle() { return title; }

    public String getDescription() { return description; }

    public Date getStartAt() { return startAt; }

    public Date getEndAt() { return endAt; }
}
