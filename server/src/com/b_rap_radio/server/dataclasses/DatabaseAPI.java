package com.b_rap_radio.server.dataclasses;

import com.b_rap_radio.server.dataclasses.request_related.*;
import org.mindrot.jbcrypt.BCrypt;

import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.text.ParseException;
import java.util.*;

public class DatabaseAPI {

    private enum ColumnNames {
        // global
        ID("id"),

        // users table
        USER_NICKNAME("nickname"),
        USER_EMAIL("email"),
        USER_CREDITS("credits"), // for now, useless, may be to delete
        USER_CREATION_DATE("creation_date"),
        USER_PWD("password"),

        // Sanctions table
        SANCTION_TYPE("type"),
        SANCTION_DESC("description"),
        SANCTION_START("start_at"),
        SANCTION_END("end_at"),
        SANCTION_USER_ID("targeted_user_id"),

        // Special users table
        SPECIAL_STATUS("status"),
        SPECIAL_USER_ID("linked_user_id"),

        // Staff users table
        STAFF_STATUS("status"),
        STAFF_USER_ID("linked_user_id"),
        STAFF_KEY("staff_key"),

        // Album table
        ALBUM_TITLE("title"),
        ALBUM_IMG_URL("image_url"),

        // Artist table
        ARTIST_NAME("name"),
        ARTIST_SPOTIFY_ID("spotify_link_id"),
        ARTIST_DEEZER_ID("deezer_link_id"),

        // Music-Artist table
        MUSICARTIST_ARTIST_ID("artist_id"),
        MUSICARTIST_MUSIC_ID("music_id"),

        // Music table
        MUSIC_TITLE("title"),
        MUSIC_ALBUM_ID("album_id");


        public final String value;
        ColumnNames(String val){
            this.value = val;
        }


        @Override
        public String toString() {
            return value;
        }
    }

    // location of database (contains username & password)
    public static final String URL = "jdbc:mysql://localhost:3306/brap?serverTimezone=UTC";

    // tables' name
    public static final String USER_TABLE = "api_user";
    public static final String SANCTIONS_TABLE = "api_sanction";
    public static final String SPECIAL_USERS_TABLE = "api_specialuser";
    public static final String STAFF_TABLE = "api_staffuser";
    public static final String ALBUM_TABLE = "api_album";
    public static final String ARTIST_TABLE = "api_artist";
    public static final String MUSICARTIST_TABLE = "api_music_authors";
    public static final String MUSIC_TABLE = "api_music";


    private static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, "appUser", "1correkt");
    }

    // User's calls (get infos; login; register)

    public static Request loginRequest(final String id, final String password) {

        if(id == null || password == null)
            return new Request(RequestTypes.LOGIN, "MISSING ARGS");

        Request ans;
        try (Connection conn = getConnection();
             final PreparedStatement st = conn.prepareStatement(
                     String.format("SELECT * FROM %s WHERE %s = ? OR %s = ? LIMIT 1",
                     USER_TABLE, ColumnNames.USER_NICKNAME, ColumnNames.USER_EMAIL)
             )
        ){

            st.setString(1, id);
            st.setString(2, id);

            try(final ResultSet results = st.executeQuery()){

                if(results.next()){

                    final String hash = results.getString(6);

                    if(BCrypt.checkpw(password, hash)){ // good pwd
                        ans = new Request(RequestTypes.LOGIN);

                        final UserEntity user = new UserEntity(results.getInt(1), results.getString(3),
                                results.getString(2), getStatus(results.getInt(1)), results.getInt(4));

                        ans.addUserArgs(user);
                        ans.setSanctionArgs(getSanctions(results.getInt(1)));
                        ans.addStringArgs("LOGGED");

                    } else { // wrong pwd
                        //conn.close();
                        ans = new Request(RequestTypes.LOGIN, "BAD PWD");
                    }

                } else {
                    ans = new Request(RequestTypes.LOGIN, "NOT FOUND");
                }
            }

        } catch(SQLException e){
            e.printStackTrace();
            ans = new Request(RequestTypes.LOGIN, "SERVER ERROR");
        }

        return ans;
    }

    public static Request registerRequest(final String nickname, final String email, final String password){
        //!\ this function does not format text /!\\
        if(nickname == null || email == null || password == null)
            return new Request(RequestTypes.REGISTER, "MISSING ARGS");

        Request ans;

        try(Connection conn = getConnection();
            PreparedStatement st = conn.prepareStatement(
                    String.format("SELECT %s, %s FROM %s WHERE %s = ? OR %s = ? LIMIT 1", USER_TABLE,
                            ColumnNames.USER_NICKNAME, ColumnNames.USER_EMAIL, ColumnNames.USER_EMAIL, ColumnNames.USER_NICKNAME)
            )
        ){

            st.setString(1, email);
            st.setString(2, nickname);

            try(final ResultSet resultSet = st.executeQuery()) {

                if (resultSet.next()) {

                    if (nickname.equals(resultSet.getString(1))) { // same nickname
                        ans = new Request(RequestTypes.REGISTER, "NICKNAME ALREADY USED");
                    } else {
                        ans = new Request(RequestTypes.REGISTER, "EMAIL ALREADY USED");
                    }
                } else { // no already existing entries

                    try(
                            PreparedStatement insertSt = conn.prepareStatement(
                                    String.format("INSERT INTO %s(%s, %s, %s, %s, %s) VALUES(NOW(), ?, ?, ?, ?)",
                                            USER_TABLE, ColumnNames.USER_CREATION_DATE, ColumnNames.USER_EMAIL,
                                            ColumnNames.USER_NICKNAME,ColumnNames.USER_PWD, ColumnNames.USER_CREDITS
                                    )
                            )
                    ) {
                        insertSt.setString(1, email);
                        insertSt.setString(2, nickname);
                        insertSt.setString(3, password);
                        insertSt.setInt(4, 0); // TODO credits default value ?

                        insertSt.executeUpdate();

                        ans = new Request(RequestTypes.REGISTER);
                        ans.addStringArgs("DONE");
                    }
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
            ans = new Request(RequestTypes.REGISTER, "SERVER ERROR");
        }

        return ans;
    }


    // Database's get requests (retrieve data from differents tables)

    private static List<SanctionEntity> getSanctions(int id) throws SQLException {
        if(id <= 0){
            return new ArrayList<>();
        }

        try(final Connection conn = getConnection();
            final PreparedStatement st = conn.prepareStatement(String.format(
                    "SELECT * FROM %s WHERE %s = ? LIMIT 1", SANCTIONS_TABLE, ColumnNames.SANCTION_USER_ID
            ))
        ) {

            st.setInt(1, id);

            try(final ResultSet resultSet = st.executeQuery()) {

                final ArrayList<SanctionEntity> results = new ArrayList<>();

                while (resultSet.next()) {
                    try {
                        SanctionEntity s = new SanctionEntity(resultSet.getInt(1), resultSet.getString(2), null,
                                resultSet.getString(3), resultSet.getString(4), resultSet.getString(5));
                        results.add(s);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }

                return results;
            }
        }
    }

    private static List<String> getStatus(int id) throws SQLException {
        if(id <= 0){
            return new ArrayList<>();
        }

        try(final Connection conn = getConnection();
            final PreparedStatement st = conn.prepareStatement(
                    String.format(
                            "SELECT %s FROM %s WHERE %s = ? LIMIT 1",
                            ColumnNames.SPECIAL_STATUS, SPECIAL_USERS_TABLE, ColumnNames.SPECIAL_USER_ID
                    )
            )
        ){

            st.setInt(1, id);

            try(final ResultSet resultSet = st.executeQuery()) {

                final List<String> results = new ArrayList<>();

                while (resultSet.next()) {
                    results.add(resultSet.getString(1));
                }

                if (results.size() == 0)
                    results.add("MEMBER");

                conn.close();
                return results;
            }
        }
    }

    public static Request getUserInfos(final int id) {
        if(id <= 0)
            return new Request(RequestTypes.GET_ALL_INFOS, "BAD ARGS");

        try(final Connection conn = getConnection();

            final PreparedStatement st = conn.prepareStatement(
                    String.format(
                            "SELECT %s, %s, %s FROM %s WHERE %s LIMIT 1", ColumnNames.USER_EMAIL,
                            ColumnNames.USER_NICKNAME, ColumnNames.USER_CREDITS, USER_TABLE, ColumnNames.ID
                    )
            )
        ) {

            st.setInt(1, id);

            try(final ResultSet resultSet = st.executeQuery()) {

                UserEntity u = null;

                if (resultSet.next()) {
                    List<String> status = null;

                    try {
                        status = getStatus(id);
                    } catch (SQLException ignored) {
                    }

                    u = new UserEntity(id, resultSet.getString(1), resultSet.getString(2),
                            status, resultSet.getInt(3));
                }

                final Request ans = new Request(RequestTypes.GET_ALL_INFOS);
                ans.addUserArgs(u);
                return ans;
            }
        } catch (SQLException e){
            e.printStackTrace();
            return new Request(RequestTypes.GET_ALL_INFOS, "SERVER ERROR");
        }
    }

    public static Request getUserInfos(final String emailOrNickname) {
        if(emailOrNickname == null)
            return new Request(RequestTypes.GET_ALL_INFOS, "MISSING ARGS");

        try(final Connection conn = getConnection();
            final PreparedStatement st = conn.prepareStatement(
                    String.format(
                            "SELECT %s, %s, %s, %s FROM %s WHERE %s = ? OR %s = ? LIMIT 1",
                            ColumnNames.ID, ColumnNames.USER_EMAIL, ColumnNames.USER_NICKNAME, ColumnNames.USER_CREDITS,
                            USER_TABLE, ColumnNames.USER_EMAIL, ColumnNames.USER_NICKNAME
                    )
            )
        ) {

            st.setString(1, emailOrNickname);
            st.setString(2, emailOrNickname);

            try(final ResultSet resultSet = st.executeQuery()) {

                UserEntity u = null;

                if (resultSet.next()) {
                    List<String> status = null;

                    try {
                        status = getStatus(resultSet.getInt(1));
                    } catch (SQLException ignored) {
                    }

                    u = new UserEntity(resultSet.getInt(1),
                            resultSet.getString(2), resultSet.getString(3),
                            status, resultSet.getInt(4));
                }

                final Request ans = new Request(RequestTypes.GET_ALL_INFOS);
                ans.addUserArgs(u);
                return ans;
            }
        } catch (SQLException e){
            e.printStackTrace();
            return new Request(RequestTypes.GET_ALL_INFOS, "SERVER ERROR");
        }
    }

    // admin's calls (login, managing users, sanctions,...)

    public static Request staffLoginRequest(final String id, final String password, final String staffKey) {

        if(staffKey == null)
            return new Request(RequestTypes.LOGIN, "MISSING ARGS");

        final Request normalLoginAnswer = loginRequest(id, password);
        if(normalLoginAnswer.isError())
            return normalLoginAnswer;

        Request ans;

        try (final Connection conn = getConnection();
             final PreparedStatement st = conn.prepareStatement(
                     String.format(
                             "SELECT %s, %s FROM %s WHERE %s = ? LIMIT 1",
                             ColumnNames.STAFF_STATUS, ColumnNames.STAFF_KEY, STAFF_TABLE, ColumnNames.STAFF_USER_ID
                     )
             )
        ){

            st.setInt(1, normalLoginAnswer.getUserArgs().get(0).getId());
            try(final ResultSet resultSet = st.executeQuery()) {


                if (resultSet.next()) {

                    if (resultSet.getString(2).equals(staffKey)) {
                        ans = new Request(RequestTypes.LOGIN);
                        ans.addStringArgs(normalLoginAnswer.getUserArgs().get(0).getNickname(), resultSet.getString(1));
                    } else {
                        ans = new Request(RequestTypes.LOGIN, "BAD KEY");
                    }

                } else {
                    ans = new Request(RequestTypes.LOGIN, "NOT FOUND");
                }
            }

        } catch(SQLException e){
            e.printStackTrace();
            ans = new Request(RequestTypes.LOGIN, "SERVER ERROR");
        }

        return ans;
    }

    public static Request addSanctionRequest(final SanctionEntity s){

        if(s == null)
            return new Request(RequestTypes.ADD_SANCTION, "MISSING ARGS");

        if(s.getType() == null || s.getUserId() <= 0 || s.getStartAt() == null || s.getEndAt() == null || s.getDescription() == null)
            return new Request(RequestTypes.ADD_SANCTION, "MISSING ARGS");

        try {
            SanctionEntity.dateFormat.parse(s.getStartAt());
            SanctionEntity.dateFormat.parse(s.getEndAt());
        } catch (ParseException e){
            return new Request(RequestTypes.ADD_SANCTION, "BAD DATE FORMAT");
        }

        Request ans;

        try (final Connection conn = getConnection();
             final PreparedStatement st = conn.prepareStatement(
                     String.format(
                             "INSERT INTO %s(%s, %s, %s, %s, %s) VALUES(?, ?, ?, ?, ?)",
                             SANCTIONS_TABLE, ColumnNames.SANCTION_TYPE, ColumnNames.SANCTION_DESC,
                             ColumnNames.SANCTION_USER_ID, ColumnNames.SANCTION_START, ColumnNames.SANCTION_END
                     )
             )
        ){

            st.setString(1, s.getType());
            st.setString(2, s.getDescription());
            st.setInt(3, s.getUserId());
            st.setString(4, s.getStartAt());
            st.setString(5, s.getEndAt());
            st.executeUpdate();

            ans = new Request(RequestTypes.ADD_SANCTION);
            ans.addStringArgs("DONE");

        } catch(SQLException e){
            e.printStackTrace();
            ans = new Request(RequestTypes.ADD_SANCTION, "SERVER ERROR");
        }

        return ans;
    }

    public static Request deleteSanctionRequest(final int id){

        if(id <= 0)
            return new Request(RequestTypes.DELETE_SANCTION, "MISSING ARGS");

        Request ans;

        try (final Connection conn = getConnection();
             final PreparedStatement selectSt = conn.prepareStatement(
                     String.format("SELECT %s FROM %s WHERE %s = ? LIMIT 1", ColumnNames.ID, SANCTIONS_TABLE, ColumnNames.ID)
             )
        ){

            selectSt.setInt(1, id);

            try(final ResultSet resultSet = selectSt.executeQuery()) {


                if (resultSet.next()) {
                    final PreparedStatement deleteSt = conn.prepareStatement(
                            String.format("DELETE FROM %s WHERE %s = ? LIMIT 1", SANCTIONS_TABLE, ColumnNames.ID)
                    );
                    deleteSt.setInt(1, resultSet.getInt(1));
                    deleteSt.executeUpdate();

                    ans = new Request(RequestTypes.DELETE_SANCTION);
                    ans.addStringArgs("DONE");

                } else {
                    ans = new Request(RequestTypes.DELETE_SANCTION, "NOT FOUND");
                }
            }

        } catch (SQLException e){
            e.printStackTrace();
            ans = new Request(RequestTypes.DELETE_SANCTION, "SERVER ERROR");
        }

        return ans;
    }

    public static Request deleteUserRequest(final int id){

        if(id <= 0)
            return new Request(RequestTypes.DELETE_USER, "MISSING ARGS");

        Request ans;

        try (final Connection conn = getConnection();
             final PreparedStatement selectSt = conn.prepareStatement(
                     String.format("SELECT %s FROM %s WHERE %s = ? LIMIT 1", ColumnNames.ID, USER_TABLE, ColumnNames.ID)
             )
        ){

            selectSt.setInt(1, id);

            try(final ResultSet resultSet = selectSt.executeQuery()) {

                if (resultSet.next()) {

                    final PreparedStatement deleteSt = conn.prepareStatement(
                            String.format("DELETE FROM %s WHERE %s = ? LIMIT 1", USER_TABLE, ColumnNames.ID)
                    );
                    deleteSt.setInt(1, resultSet.getInt(1));
                    deleteSt.executeUpdate();

                    ans = new Request(RequestTypes.DELETE_USER);
                    ans.addStringArgs("DONE");

                } else {
                    ans = new Request(RequestTypes.DELETE_USER, "NOT FOUND");
                }
            }

        } catch (SQLException e){
            e.printStackTrace();
            ans = new Request(RequestTypes.DELETE_USER, "SERVER ERROR");
        }

        return ans;
    }

    public static Request fetchFromDatabase(final String dbName, final int from, final int to){
        if(dbName == null || from <= 0 || to <= 0)
            return new Request(RequestTypes.DB_FETCHER, "MISSING ARGS");

        final List<String> dbList = Arrays.asList("SANCTIONS", "USERS", "STAFF", "MUSIC");

        if(!dbList.contains(dbName) || from > to)
            return new Request(RequestTypes.DB_FETCHER, "BAD ARGS");

        Request ans = null;

        try (final Connection conn = getConnection()){
            switch (dbName) {
                case "SANCTIONS":

                    ans = new Request(RequestTypes.GET_SANCTIONS);
                    try (
                            final PreparedStatement st = conn.prepareStatement(
                                    String.format(
                                            "SELECT * FROM %s WHERE %s BETWEEN ? AND ?;", SANCTIONS_TABLE, ColumnNames.ID
                                    )
                            )
                    ) {

                        st.setInt(1, from);
                        st.setInt(2, to);
                        try (final ResultSet results = st.executeQuery()) {

                            while (results.next()) {
                                final Request userReq = getUserInfos(results.getInt(6));
                                UserEntity user = null;

                                if (!userReq.isError() && userReq.getUserArgs().size() > 0)
                                    user = userReq.getUserArgs().get(0);


                                SanctionEntity s = null;
                                try {
                                    s = new SanctionEntity(results.getInt(1), results.getString(2),
                                            user, results.getString(3), results.getString(4), results.getString(5));
                                } catch (ParseException ignored) {
                                } // should not happened (strings come from db, so correctly parsed (?)

                                ans.addSanctionArgs(s);
                            }
                        }
                    }

                    break;
                case "USERS":
                    ans = new Request(RequestTypes.GET_USERS);
                    try (
                            final PreparedStatement st = conn.prepareStatement(
                                    String.format(
                                            "SELECT * FROM %s WHERE %s BETWEEN ? AND ?;", SANCTIONS_TABLE, ColumnNames.ID
                                    )
                            )
                    ) {
                        st.setInt(1, from);
                        st.setInt(2, to);

                        try (final ResultSet results = st.executeQuery()) {

                            while (results.next()) {
                                List<String> status = null;
                                try {
                                    status = getStatus(results.getInt(1));
                                } catch (SQLException ignored) {
                                }

                                ans.addUserArgs(new UserEntity(results.getInt(1), results.getString(3),
                                        results.getString(2), status, results.getInt(4)));
                            }
                        }
                    }

                    break;
                case "STAFF":  // dbName == STAFF
                    ans = new Request(RequestTypes.GET_STAFF);
                    try (final PreparedStatement st = conn.prepareStatement(
                            String.format(
                                    "SELECT %s, %s, %s FROM %s WHERE %s BETWEEN ? AND ?",
                                    ColumnNames.ID, ColumnNames.STAFF_STATUS, ColumnNames.STAFF_USER_ID, STAFF_TABLE,
                                    ColumnNames.ID
                            )
                    )) {
                        st.setInt(1, from);
                        st.setInt(2, to);

                        try (final ResultSet results = st.executeQuery()) {

                            while (results.next()) {
                                final Request userReq = getUserInfos(results.getInt(3));
                                String email = null, nickname = null;

                                if (!userReq.isError() && userReq.getUserArgs().size() > 0) {
                                    final UserEntity u = userReq.getUserArgs().get(0);
                                    email = u.getEmail();
                                    nickname = u.getNickname();
                                }

                                ans.addUserArgs(new UserEntity(results.getInt(1), email, nickname,
                                        results.getString(2), 0));
                            }
                        }
                    }
                    break;
                case "MUSIC":
                    ans = new Request(RequestTypes.GET_MUSIC);
                    try (final PreparedStatement st = conn.prepareStatement(
                            String.format(
                                    "SELECT * FROM %s WHERE %s BETWEEN ? AND ?",
                                    MUSIC_TABLE, ColumnNames.ID
                            )
                    )) {
                        st.setInt(1, from);
                        st.setInt(2, to);

                        try (final ResultSet results = st.executeQuery()) {

                            while (results.next()) {
                                Album album = getAlbumInfos(results.getInt(3));
                                if (album == null)
                                    continue;

                                ans.addIntArgs(results.getInt(1));
                                ans.addMusicInfosArgs(new MusicInfos(
                                        results.getString(2), album.getName(), getAuthorsStringFromMusic(results.getInt(1)),
                                        album.getImageUrl(), -1, -1, -1, (long) -1, (long) -1
                                ));
                            }
                        }
                    }
                    break;
            }
        } catch(SQLException e){
            e.printStackTrace();
            ans = new Request(RequestTypes.DB_FETCHER, "SERVER ERROR");
        }

        return ans;
    }

    // music

    public static int musicAlreadyExist(String title, int albumId, Integer[] artistsId){

        final HashSet<Integer> artistSet = new HashSet<>(Arrays.asList(artistsId));

        // id title album_id
        try(final Connection conn = getConnection();
            final PreparedStatement st = conn.prepareStatement(
                    String.format( // only retrieving id because title and album are directly compared with WHERE condition (only authors left)
                            "SELECT %s FROM %s WHERE %s = ? AND %s = ?",
                            ColumnNames.ID, MUSIC_TABLE, ColumnNames.MUSIC_TITLE, ColumnNames.MUSIC_ALBUM_ID
                    )
            )
        ) {

            st.setString(1, title);
            st.setInt(2, albumId);

            try(final ResultSet resultSet = st.executeQuery()) {

                while(resultSet.next()) {
                    // retrieving music author's id and compare to artistsId[] (Artist object returned by
                    // getAuthorsFromMusic does not contains their id, because it would be used nowhere besides here

                    try(final PreparedStatement st1 = conn.prepareStatement(
                                String.format(
                                        "SELECT %s FROM %s WHERE %s = ?",
                                        ColumnNames.MUSICARTIST_ARTIST_ID, MUSICARTIST_TABLE, ColumnNames.MUSICARTIST_MUSIC_ID
                                )
                        )
                    ){
                        st1.setInt(1, resultSet.getInt(1));
                        try(final ResultSet otherMusicArtistsSet = st.executeQuery()){

                            HashSet<Integer> otherMusicArtistsIds = new HashSet<>();

                            while(otherMusicArtistsSet.next())
                                otherMusicArtistsIds.add(otherMusicArtistsSet.getInt(1));

                            if(otherMusicArtistsIds.equals(artistSet)){
                                return 1;
                            }
                        }
                    }

                }

                return 0;
            }

        } catch (SQLException e){
            e.printStackTrace();
        }

        return -1;
    }

    public static Request addMusic(String title, int artistId, int albumId, File audioFile){
        return addMusic(title, new Integer[]{artistId}, albumId, audioFile);
    }

    public static Request addMusic(String title, Integer[] artistsId, int albumId, File audioFile){
        final FileInputStream is;
        try {
           is = new FileInputStream(audioFile);
        } catch (IOException e){
            e.printStackTrace();
            return new Request(RequestTypes.ADD_MUSIC, "CANT READ FILE");
        }


        // verify arguments
        if(title == null || artistsId == null || albumId <= 0 || artistsId.length <= 0)
            return new Request(RequestTypes.ADD_MUSIC, "BAD ARGS");

        if(!audioFile.exists())
            return new Request(RequestTypes.ADD_MUSIC, "FILE NOT FOUND");

        Album album = getAlbumInfos(albumId);
        if(album == null)
            return new Request(RequestTypes.ADD_MUSIC, "ALBUM NOT FOUND");


        StringBuilder authorsBuilder = new StringBuilder();

        for(int i : artistsId){
            if(i <= 0)
                return new Request(RequestTypes.ADD_MUSIC, "BAD ARGS");

            Artist artist = getArtistFromId(i);
            if(artist == null)
                return new Request(RequestTypes.ADD_MUSIC, "ARTIST NOT FOUND");

            authorsBuilder.append(artist.getName());
            authorsBuilder.append(", ");
        }

        authorsBuilder.delete(authorsBuilder.length() - 2, authorsBuilder.length()); // delete last ", "

        int temp = musicAlreadyExist(title, albumId, artistsId);

        if(temp == -1)
            return new Request(RequestTypes.ADD_MUSIC, "SERVER ERROR");

        if(temp == 1)
            return new Request(RequestTypes.ADD_MUSIC, "ALREADY EXIST");

        // end args verification

        Request ans = new Request(RequestTypes.ADD_MUSIC);

        try (final Connection conn = getConnection();
             final PreparedStatement musicSt = conn.prepareStatement(
                     String.format(
                             "INSERT INTO %s( %s, %s) VALUES(?, ?)",
                             MUSIC_TABLE, ColumnNames.MUSIC_TITLE, ColumnNames.MUSIC_ALBUM_ID
                     ), Statement.RETURN_GENERATED_KEYS
             )
        ) {
            musicSt.setString(1, title);
            musicSt.setInt(2, albumId);

            if(musicSt.executeUpdate() > 0){
                ResultSet rs = musicSt.getGeneratedKeys();

                if(rs.next()) {
                    int musicId = rs.getInt(1);

                    for(Integer artistId : artistsId) {

                        try (final PreparedStatement artistSt = conn.prepareStatement(
                                String.format(
                                        "INSERT INTO %s( %s, %s) VALUES(?, ?)",
                                        MUSICARTIST_TABLE, ColumnNames.MUSICARTIST_MUSIC_ID, ColumnNames.MUSICARTIST_ARTIST_ID
                                )
                        )) {

                            artistSt.setInt(1, musicId);
                            artistSt.setInt(2, artistId);
                            artistSt.executeUpdate();
                        }
                    }

                    final File outputFile = new File(getFileName(musicId));

                    if(outputFile.exists())
                        return new Request(RequestTypes.ADD_MUSIC, "FILE ALREADY EXIST");

                    try {
                        OutputStream os = new FileOutputStream(outputFile);

                        // making sure that first 4 bytes are "OggS" (if not, add it, to make the Opus file header valid)
                        byte[] buffer = new byte[4];

                        if(is.read(buffer) <= 0)
                            throw new IOException();

                        System.out.println();

                        if(!Arrays.equals(buffer, "OggS".getBytes())){
                            os.write("OggS".getBytes());
                            os.flush();
                        }

                        os.write(buffer);
                        os.flush();

                        buffer = new byte[1024];


                        while(is.read(buffer) > 0) {
                            os.write(buffer);
                            os.flush();
                        }

                    } catch (IOException e){
                        e.printStackTrace();
                        return new Request(RequestTypes.ADD_MUSIC, "FAILED CREATING FILE");
                    }

                    ans.addStringArgs("DONE");

                } else {
                    System.out.println("Added music row, but couldn't access to it's created id");
                    return new Request(RequestTypes.ADD_MUSIC, "UNCOMPLETED MISSING ARTISTS");
                }

            } else {
                return new Request(RequestTypes.ADD_MUSIC, "SERVER ERROR");
            }


        } catch (SQLException e){ // file not found shouldn't happen
            e.printStackTrace();
            return new Request(RequestTypes.ADD_MUSIC, "SERVER ERROR");
        }


        return ans;
    }

    private static String getFileName(int musicId){
        return PlayList.PLAYLIST_PATH.concat(String.valueOf(musicId)).concat(".opus");
    }

    public static Music getMusic(final int id) {
        if(id < 0)
            return null;

        try(final Connection conn = getConnection();
            final PreparedStatement st = conn.prepareStatement(
                    String.format(
                            "SELECT * FROM %s WHERE %s = ? LIMIT 1",
                            MUSIC_TABLE, ColumnNames.ID
                    )
            )
        ) {

            st.setInt(1, id);

            try(final ResultSet resultSet = st.executeQuery()) {

                if(resultSet.next()) {
                    System.out.println();
                    return getMusicFromResultSet(resultSet);
                }
            }

        } catch (SQLException e){
            e.printStackTrace();
        }

        return null;
    }


    private static Music getMusicFromResultSet(ResultSet musicRow) {
        // id ; title ; album_id
        try {
            String authors = getAuthorsStringFromMusic(musicRow.getInt(1));
            String musicTitle = musicRow.getString(2);
            Album album = getAlbumInfos(musicRow.getInt(3));

            if(album == null)
                return null;


            final File file = new File(getFileName(musicRow.getInt(1)));

            if(!file.exists()){
                System.out.printf("File not found for music : %s of %s%n", musicTitle, authors);
                return null;
            }

            return new Music(musicRow.getString(2), album.getName(), authors, file, 0L, album.getImageUrl());


        } catch (SQLException | IOException | UnsupportedAudioFileException e){
            e.printStackTrace();
        }

        return null;
    }


    // artist

    private static String getAuthorsStringFromMusic(int musicId){
        final List<Artist> authors = getAuthorsFromMusic(musicId);

        if(authors == null)
            return null;

        if(authors.size() == 0)
            authors.add(new Artist("Inconnu", null, null)); // default author (when not found in db (== unknown ?))

        final Iterator<Artist> iterator = authors.iterator();
        final StringBuilder builder = new StringBuilder(iterator.next().getName());

        while(iterator.hasNext()){
            builder.append(", ");
            builder.append(iterator.next().getName());
        }

        return builder.toString();
    }

    private static List<Artist> getAuthorsFromMusic(int musicId){
        List<Artist> authors = new ArrayList<>();
        try(
                final Connection conn = getConnection();
                final PreparedStatement st = conn.prepareStatement(String.format(
                        "SELECT %s FROM %s WHERE %s = ? ORDER BY %s",
                        ColumnNames.MUSICARTIST_ARTIST_ID, MUSICARTIST_TABLE, ColumnNames.MUSICARTIST_MUSIC_ID, ColumnNames.ID
                ))
        ){
            st.setInt(1, musicId);
            try(final ResultSet resultSet = st.executeQuery()){

                while(resultSet.next()){
                    Artist author = getArtistFromId(resultSet.getInt(1));
                    authors.add(author);
                }
            }

        } catch (SQLException e){
            e.printStackTrace();
            return null;
        }

        return authors;
    }

    private static Artist getArtistFromId(final int artistId){
        if(artistId <= 0)
            return null;

        try(final Connection conn = getConnection();

            final PreparedStatement st = conn.prepareStatement(
                    String.format(
                            "SELECT %s, %s, %s FROM %s WHERE %s = ? LIMIT 1", ColumnNames.ARTIST_NAME,
                            ColumnNames.ARTIST_SPOTIFY_ID, ColumnNames.ARTIST_DEEZER_ID, ARTIST_TABLE, ColumnNames.ID
                    )
            )
        ) {

            st.setInt(1, artistId);

            try(final ResultSet resultSet = st.executeQuery()) {

                if (resultSet.next()) {
                    return getArtistFromResultSet(resultSet);
                }

            }
        } catch (SQLException e){
            e.printStackTrace();
        }

        return null;
    }

    public static int getArtistIdFromName(final String name){
        try(final Connection conn = getConnection();

            final PreparedStatement st = conn.prepareStatement(
                    String.format(
                            "SELECT %s FROM %s WHERE %s = ? LIMIT 1", ColumnNames.ID, ARTIST_TABLE, ColumnNames.ARTIST_NAME
                    )
            )
        ) {

            st.setString(1, name);

            try(final ResultSet resultSet = st.executeQuery()) {

                if (resultSet.next()) {
                    return resultSet.getInt(1);
                }

            }
        } catch (SQLException e){
            e.printStackTrace();
        }

        return -1;
    }

    public static int createArtist(String name, String spotifyId, String deezerId){
        if(name == null || getArtistIdFromName(name) != -1) // already exists
            return -1;

        if(spotifyId == null)
            spotifyId = "NULL";

        if(deezerId == null)
            deezerId = "NULL";

        try (final Connection conn = getConnection();
             final PreparedStatement st = conn.prepareStatement(
                     String.format(
                             "INSERT INTO %s(%s, %s, %s) VALUES(?, ?, ?)",
                             ARTIST_TABLE, ColumnNames.ARTIST_NAME, ColumnNames.ARTIST_SPOTIFY_ID, ColumnNames.ARTIST_DEEZER_ID
                     ), Statement.RETURN_GENERATED_KEYS
             )
        ) {

            st.setString(1, name);
            st.setString(2, spotifyId);
            st.setString(3, deezerId);
            if(st.executeUpdate() > 0){
                ResultSet rs = st.getGeneratedKeys();

                if(rs.next())
                    return rs.getInt(1);
                else
                    return -2;

            } else
                return -3;



        } catch (SQLException e){
            e.printStackTrace();
        }

        return -1;
    }


    private static Artist getArtistFromResultSet(final ResultSet artistRow) throws SQLException {
        // (id,) name, spotify_id, deezer_id
        return new Artist(artistRow.getString(1), artistRow.getString(2), artistRow.getString(3));
    }

    // albums

    private static Album getAlbumInfos(final int id){

        if(id <= 0)
            return null;

        try(final Connection conn = getConnection();
            final PreparedStatement st = conn.prepareStatement(
                    String.format(
                            "SELECT * FROM %s WHERE %s = ? LIMIT 1",
                            ALBUM_TABLE, ColumnNames.ID
                    )
            )
        ) {

            st.setInt(1, id);

            try(final ResultSet resultSet = st.executeQuery()) {

                if (resultSet.next()) {
                    // id ; name ; image_url
                    return new Album(resultSet.getString(2), resultSet.getString(3));
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static int getAlbumIdFromTitle(String title){

        try(final Connection conn = getConnection();
            final PreparedStatement st = conn.prepareStatement(
                    String.format(
                            "SELECT %s FROM %s WHERE %s = ? LIMIT 1",
                             ColumnNames.ID, ALBUM_TABLE, ColumnNames.ALBUM_TITLE
                    )
            )
        ) {

            st.setString(1, title);

            try(final ResultSet resultSet = st.executeQuery()) {

                if (resultSet.next()) {
                    // id ; name ; image_url
                    return resultSet.getInt(1);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return -1;
    }

    public static int createAlbum(String title, String imageUrl){
        if(title == null || imageUrl == null || getAlbumIdFromTitle(title) != -1) // already exists
            return -1;

        try (final Connection conn = getConnection();
             final PreparedStatement st = conn.prepareStatement(
                     String.format(
                             "INSERT INTO %s(%s, %s) VALUES(?, ?)",
                             ALBUM_TABLE, ColumnNames.ALBUM_TITLE, ColumnNames.ALBUM_IMG_URL
                     ), Statement.RETURN_GENERATED_KEYS
             )
        ) {

            st.setString(1, title);
            st.setString(2, imageUrl);

            if(st.execute()){
                ResultSet rs = st.getGeneratedKeys();

                if(rs.next())
                    return rs.getInt(1);
                else
                    return -2;
            } else
                return -3;

        } catch (SQLException e){
            e.printStackTrace();
        }

        return -1;
    }


    /* TODO LIST
     - self delete
     - modify sanctions
     - modify user
     - modify artists' spotify / deezer id
     - modify albums' image link

     - Optimize : creating 2 conn obj (bc if not doesnt work) for sanctions & staff ; staff fetching all infos from user
                  but only use email & nickname
     */
}
