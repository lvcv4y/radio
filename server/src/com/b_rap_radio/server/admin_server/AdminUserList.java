package com.b_rap_radio.server.admin_server;

import java.util.ArrayList;
import java.util.List;

public class AdminUserList {

    private static AdminUserList instance;
    private final List<AdminUser> users;


    public static AdminUserList getInstance() {
        if (instance == null) instance = new AdminUserList();
        return instance;
    }

    private AdminUserList() {
        users = new ArrayList<>();
    }

    public synchronized void add(AdminUser user) {
        users.add(user);
    }

    public synchronized void remove(AdminUser user) {
        users.remove(user);
    }

    public synchronized List<AdminUser> getUsers() {
        return new ArrayList<>(users);
    }
}
