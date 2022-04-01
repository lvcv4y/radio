package com.b_rap_radio.server.dataclasses;

import java.util.ArrayList;
import java.util.List;

public class UserList {
    private final List<RadioUser> userlist;
    private static UserList instance = null;

    public static UserList getInstance() {
        if(instance == null) instance = new UserList();
        return instance;
    }

    private UserList(){
        this.userlist = new ArrayList<>();
    }

    synchronized public void remove(RadioUser user){ this.userlist.remove(user); }

    synchronized public void add(RadioUser user){ this.userlist.add(user); }

    synchronized public boolean contains(RadioUser user){return this.userlist.contains(user); }

    synchronized public List<RadioUser> getUsers(){
        return new ArrayList<>(userlist);
    }
}
