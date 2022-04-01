package com.b_rap_radio.server.dataclasses;

import java.util.ArrayList;
import java.util.List;

public class SoundUserList {
    private final List<RadioUser> userlist;
    private static SoundUserList instance = null;

    public static SoundUserList getInstance() {
        if(instance == null) instance = new SoundUserList();
        return instance;
    }

    private SoundUserList(){
        this.userlist = new ArrayList<>();
    }

    synchronized public void remove(RadioUser user){ this.userlist.remove(user); }

    synchronized public void add(RadioUser user){ this.userlist.add(user); }

    synchronized public boolean contains(RadioUser user){return this.userlist.contains(user); }

    synchronized public List<RadioUser> getUsers(){
        return new ArrayList<>(userlist);
    }
}
