package com.b_rap_radio.server.dataclasses;

import com.b_rap_radio.server.admin_server.TCPAdminHandler;
import com.b_rap_radio.server.dataclasses.request_related.MusicInfos;
import com.b_rap_radio.server.dataclasses.request_related.Request;
import com.b_rap_radio.server.dataclasses.request_related.RequestTypes;
import com.sun.org.apache.xerces.internal.xs.ItemPSVI;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PlayList {

    public static final String PLAYLIST_PATH = "/home/pi/Documents/brap_server/db_registered/";//"D://playlist/db_registered/";

    private static PlayList instance;

    public static PlayList getInstance() {
        if(instance == null) instance = new PlayList();
        return instance;
    }

    public PlayList() {
        this.playlist = new ArrayList<>();
        tempPlaylistIndex = 0;
    }

    synchronized public void add(Music music) {
        this.playlist.add(music);

        // ping connected admins
        final Request res = new Request(RequestTypes.ADD_PLAYLIST.getName());
        res.addMusicInfosArgs(music.getInfos(0, 0));
        new Thread(new TCPAdminHandler.GeneralQuery(res, false, null)).start();
    }

    synchronized public void addBecauseEmpty(Music music){
        this.playlist.add(0, music);

        // ping connected admins
        final Request res = new Request(RequestTypes.ADD_PLAYLIST.getName());
        res.addMusicInfosArgs(music.getInfos(0, 0));
        new Thread(new TCPAdminHandler.GeneralQuery(res, false, null)).start();
    }

    synchronized public boolean isInPlayList(MusicInfos music) {

        for(Music infos : playlist){
            if(infos.getTitle().equals(music.getTitle()) && infos.getAuthors().equals(music.getAuthors()))
                return true;
        }

        return false;
    }

    synchronized public void next(){
        //this.playlist.remove(0);
        tempPlaylistIndex = (tempPlaylistIndex + 1) % 27;
    }

    synchronized public Music getMusic(int id) {
        try {
            return playlist.get(id);
        } catch (IndexOutOfBoundsException e) { return null; }
    }

    synchronized public Music getCurrentMusic() { return playlist.get(/*0*/tempPlaylistIndex); }

    synchronized public int getSize(){ return playlist.size(); }

    synchronized public Music[] getCurrentPlayListAsArray(){ return (Music[]) this.playlist.toArray(); }

    synchronized public List<Music> getCurrentPlayListAsList() { return this.playlist; }

    private final ArrayList<Music> playlist;
    private int tempPlaylistIndex;
}
