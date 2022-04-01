package coreclasses.dataclasses;

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
    public static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @SerializedName(ID)
    private final Integer id;

    @SerializedName(TYPE)
    private final String type;

    @SerializedName(USER)
    private final UserEntity user;

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

    public int getId() { return id; }

    public String getUserNickname() { return user.getNickname(); }

    public int getUserId() { return user.getId(); }

    public String getDescription() { return description; }

    public String getStartAt() { return dateFormat.format(startAt); }

    public String getEndAt() { return dateFormat.format(endAt); }

    public String getType() { return type; }

    public boolean hasEnded(){
        if(endAt == null)
            return false;

        return new Date().after(endAt);
    }
}

