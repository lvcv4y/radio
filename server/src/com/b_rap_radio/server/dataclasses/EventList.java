package com.b_rap_radio.server.dataclasses;

import com.b_rap_radio.server.dataclasses.request_related.Event;
import com.b_rap_radio.server.dataclasses.request_related.Request;
import com.b_rap_radio.server.dataclasses.request_related.RequestTypes;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class EventList {
    public static final DayOfWeek REFRESH_DAY = DayOfWeek.MONDAY;
    private final List<Event> list;
    private boolean hasRefreshed;
    private static EventList instance = null;

    synchronized public static EventList getInstance() {
        if(instance == null) instance = new EventList();
        return instance;
    }

    private EventList(){
        list = new ArrayList<>();
        hasRefreshed = false;
    }

    synchronized public void refresh(){
        DayOfWeek currentDay = LocalDateTime.now().getDayOfWeek();
        if(REFRESH_DAY == currentDay){
            if(!hasRefreshed){
                list.clear();
                hasRefreshed = true;
            }
        } else {
            if(hasRefreshed) hasRefreshed = false;
        }

    }

    synchronized public Event getEvent(int id) {
        try {
            return list.get(id);
        } catch (IndexOutOfBoundsException ignored) {
            return null;
        }
    }

    synchronized public List<Event> getEventList(){ return this.list; }

    public synchronized void add(Event event){
        list.add(event);
        pingCurrentUsers(RequestTypes.ADD_EVENT, event);
    }

    public synchronized void delete(Event event) {
        list.remove(event);
        pingCurrentUsers(RequestTypes.DELETE_EVENT, event);
    }

    private void pingCurrentUsers(RequestTypes type, Event e){
        new Thread(() -> {
            final Request req = new Request(type);
            req.addEventArgs(e);
            UserList users = UserList.getInstance();
            for(RadioUser u : users.getUsers()){
                u.sendRequest(req);
            }
        }).start();
    }
}
