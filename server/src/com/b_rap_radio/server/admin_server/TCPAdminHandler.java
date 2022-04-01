package com.b_rap_radio.server.admin_server;

import com.b_rap_radio.server.dataclasses.*;
import com.b_rap_radio.server.dataclasses.request_related.*;
import com.google.gson.JsonParseException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TCPAdminHandler implements Runnable {

    private final AdminUser user;
    public boolean running;
    private final PrintWriter out;
    private ExecutorService executor;
    private boolean isConnectedToAudio;

    public TCPAdminHandler(AdminUser user) throws IOException {
        this.user = user;
        this.out = new PrintWriter(user.getSocket().getOutputStream());
        executor = Executors.newCachedThreadPool();
        this.user.registerHandler(this);
    }

    @Override
    public void run() {

        running = true;
        BufferedReader input;

        try {
            input = new BufferedReader(new InputStreamReader(user.getSocket().getInputStream()));
        } catch(IOException e){
            e.printStackTrace();
            // TODO LOG
            return;
        }


        while(running && !user.getSocket().isClosed()){

            try {

                Request r;
                String strReq = input.readLine();

                try {
                    r = JSONConverter.getRequestFromJSON(String.valueOf(strReq));
                } catch (JsonParseException e) {
                    // TODO LOG
                    continue;
                }

                final RequestTypes type = RequestTypes.getFromName(r.getType());

                if(type == null){
                    sendRequest(new Request(RequestTypes.ERROR.getName(), "UNKNOWN CMD"));
                    continue;
                }

                switch (type) {

                    case ADMIN_CHAT_MSG: {
                        final List<String> args = r.getStringArgs();

                        if (verifyArgs(args, 2)) {
                            executor.execute(new GeneralQuery(r, true, user));
                        } else {
                            sendRequest(new Request(type.getName(), "MISSING ARGS"));
                        }

                    }
                    break;

                    case ADD_EVENT: {
                        final List<Event> args = r.getEventArgs();
                        final EventList eventList = EventList.getInstance();
                        final List<Event> added = new ArrayList<>();
                        if (verifyArgs(args, 1)) {

                            for (Event e : args) {
                                if (!(e.getTitle() == null || e.getDescription() == null ||
                                        e.getStartAt() == null || e.getEndAt() == null)) {

                                    if (e.getEndAt().after(e.getStartAt()) && new Date().before(e.getStartAt())) {

                                        eventList.add(e);
                                        added.add(e);

                                    } else {
                                        final Request errAns = new Request(type.getName(), "UNCORRECT DATE");
                                        errAns.addEventArgs(e);
                                        sendRequest(errAns);
                                    }
                                } else {
                                    final Request errAns = new Request(type.getName(), "NULL FIELD");
                                    errAns.addEventArgs(e);
                                    sendRequest(errAns);
                                }
                            }

                            if(added.size() > 0) {
                                final Request ans = new Request(type.getName());
                                ans.setEventArgs(added);
                                executor.execute(new GeneralQuery(ans, true, user));
                            }

                            // TODO ping current radio users ??

                        } else {
                            sendRequest(new Request(type.getName(), "MISSING ARGS"));
                        }
                    }
                    break;

                    case ADD_SANCTION: {
                        final List<SanctionEntity> args = r.getSanctionArgs();
                        final List<SanctionEntity> added = new ArrayList<>();
                        if (verifyArgs(args, 1)) {
                            for (SanctionEntity s : args) {

                                if (s.getType() != null && s.getStartAt() != null && s.getEndAt() != null &&
                                        s.getDescription() != null && (s.getUserId() < 0 || s.getUserPseudo() != null)) {

                                    // verify that the targeted user exists (-> at least, its id)
                                    final Request userReq = DatabaseAPI.getUserInfos(s.getUserId());

                                    if(!userReq.isError() && userReq.getUserArgs() != null){

                                        s.setUser(userReq.getUserArgs().get(0));
                                        final Request apiAns = DatabaseAPI.addSanctionRequest(s);

                                        if (apiAns.isError()) {
                                            apiAns.addSanctionArgs(s);
                                            sendRequest(apiAns);

                                        } else {
                                            added.add(s);
                                        }

                                    } else {
                                        final Request errReq = new Request(type, "SERVER ERROR");
                                        errReq.addSanctionArgs(s);
                                        sendRequest(errReq);
                                    }

                                } else {
                                    final Request errAns = new Request(type.getName(), "NULL FIELD");
                                    errAns.addSanctionArgs(s);
                                    sendRequest(errAns);
                                }
                            }

                            final Request ans = new Request(type.getName());
                            ans.setSanctionArgs(added);
                            executor.execute(new GeneralQuery(ans, true, user));

                        } else {
                            sendRequest(new Request(type.getName(), "MISSING ARGS"));
                        }
                    }
                    break;

                    case DELETE_EVENT: {
                        final List<Event> args = r.getEventArgs();
                        final EventList eventList = EventList.getInstance();
                        final List<Event> deleted = new ArrayList<>();
                        if (verifyArgs(args, 1)) {

                            for (Event e : args) {

                                for (Event e1 : eventList.getEventList()) {

                                    if (e1.getTitle().equals(e.getTitle()) && e1.getDescription().equals(e.getDescription())) {
                                        eventList.delete(e1);
                                        deleted.add(e1);
                                    }
                                }
                            }

                            final Request ans = new Request(type.getName());
                            ans.setEventArgs(deleted);
                            executor.execute(new GeneralQuery(ans, true, user));

                            // TODO ping current radio users ??

                        } else {
                            sendRequest(new Request(type.getName(), "MISSING ARGS"));
                        }
                    }

                    break;

                    case DELETE_SANCTION: {
                        final List<SanctionEntity> args = r.getSanctionArgs();
                        final List<SanctionEntity> deleted = new ArrayList<>();

                        if (verifyArgs(args, 1)) {

                            for (SanctionEntity s : args) {
                                if (s.getId() < 0) {

                                    final Request apiAns = DatabaseAPI.deleteSanctionRequest(s.getId());
                                    if (apiAns.isError()) {
                                        apiAns.addSanctionArgs(s);
                                        sendRequest(apiAns);
                                    } else {
                                        deleted.add(s);
                                    }

                                } else {
                                    final Request errAns = new Request(type.getName(), "NULL FIELD");
                                    errAns.addSanctionArgs(s);
                                    sendRequest(errAns);
                                }
                            }

                            final Request ans = new Request(type.getName());
                            ans.setSanctionArgs(deleted);

                            executor.execute(new GeneralQuery(ans, true, user));

                        } else {
                            sendRequest(new Request(type.getName(), "MISSING ARGS"));
                        }
                    }
                    break;

                    case DELETE_USER: {
                        final List<UserEntity> args = r.getUserArgs();
                        final List<UserEntity> deleted = new ArrayList<>();

                        if (verifyArgs(args, 1)) {

                            for (UserEntity u : args) {
                                if (u.getId() < 0) {

                                    final Request apiAns = DatabaseAPI.deleteUserRequest(u.getId());
                                    if (apiAns.isError()) {
                                        apiAns.addUserArgs(u);
                                        sendRequest(apiAns);
                                    } else {
                                        deleted.add(u);
                                    }

                                } else {
                                    final Request errAns = new Request(type.getName(), "NULL FIELD");
                                    errAns.addUserArgs(u);
                                    sendRequest(errAns);
                                }
                            }

                            final Request ans = new Request(type.getName());
                            ans.setUserArgs(deleted);

                            executor.execute(new GeneralQuery(ans, true, user));

                        } else {
                            sendRequest(new Request(type.getName(), "MISSING ARGS"));
                        }
                    }
                    break;

                    case GET_EVENT: {

                        final List<Integer> args = r.getIntArgs();

                        if (verifyArgs(args, 1, 2)) {
                            try {

                                final EventList eventlist = EventList.getInstance();
                                final List<Event> events = argsGetter(eventlist.getEventList(), args);

                                final Request ans = new Request(type.getName());
                                ans.setEventArgs(events);
                                sendRequest(ans);


                            } catch (IllegalArgumentException e) {
                                final Request errAns = new Request(type.getName(), e.getMessage());
                                sendRequest(errAns);
                            }


                        } else {

                            if (args.size() > 2)
                                sendRequest(new Request(type.getName(), "TOO MUCH ARGS"));
                            else
                                sendRequest(new Request(type.getName(), "MISSING ARGS"));


                        }
                    }
                    break;

                    case GET_PLAYLIST: {

                        final List<Integer> args = r.getIntArgs();

                        if (verifyArgs(args, 1, 2)) {
                            try {

                                final PlayList playlist = PlayList.getInstance();
                                final List<Music> musics = argsGetter(playlist.getCurrentPlayListAsList(), args);
                                final List<MusicInfos> infos = new ArrayList<>();

                                for (Music m : musics) {
                                    infos.add(m.getInfos(0, 0));
                                }

                                final Request ans = new Request(type.getName());
                                ans.setMusicArgs(infos);
                                sendRequest(ans);


                            } catch (IllegalArgumentException e) {
                                final Request errAns = new Request(type.getName(), e.getMessage());
                                sendRequest(errAns);
                            }


                        } else {

                            if (args.size() > 2)
                                sendRequest(new Request(type.getName(), "TOO MUCH ARGS"));
                            else
                                sendRequest(new Request(type.getName(), "MISSING ARGS"));


                        }
                    }
                    break;

                    case GET_HISTORY: {

                        final List<Integer> args = r.getIntArgs();

                        if (verifyArgs(args, 1, 2)) {
                            try {

                                final Historique historique = Historique.getInstance();
                                final List<MusicInfos> infos = argsGetter(historique.getHistorique(), args);

                                final Request ans = new Request(type.getName());
                                ans.setMusicArgs(infos);
                                sendRequest(ans);


                            } catch (IllegalArgumentException e) {
                                final Request errAns = new Request(type.getName(), e.getMessage());
                                sendRequest(errAns);
                            }


                        } else {

                            if (args.size() > 2)
                                sendRequest(new Request(type.getName(), "TOO MUCH ARGS"));
                            else
                                sendRequest(new Request(type.getName(), "MISSING ARGS"));


                        }
                    }
                    break;

                    case GET_USERS: {
                        final List<Integer> args = r.getIntArgs();
                        if (verifyArgs(args, 2)) {

                            if (args.get(0) >= args.get(1)) {
                                sendRequest(new Request(type.getName(), "BAD ARGS"));
                            } else {
                                sendRequest(DatabaseAPI.fetchFromDatabase("USERS", args.get(0), args.get(1)));
                            }

                        } else {
                            sendRequest(new Request(type.getName(), "MISSING ARGS"));
                        }
                    }
                    break;

                    case GET_SANCTIONS: {
                        final List<Integer> args = r.getIntArgs();
                        if (verifyArgs(args, 2)) {

                            if (args.get(0) >= args.get(1)) {
                                sendRequest(new Request(type.getName(), "BAD ARGS"));
                            } else {
                                sendRequest(DatabaseAPI.fetchFromDatabase("SANCTIONS", args.get(0), args.get(1)));
                            }

                        } else {
                            sendRequest(new Request(type.getName(), "MISSING ARGS"));
                        }
                    }
                    break;

                    case GET_STAFF: {
                        final List<Integer> args = r.getIntArgs();
                        if (verifyArgs(args, 2)) {

                            if (args.get(0) >= args.get(1)) {
                                sendRequest(new Request(type.getName(), "BAD ARGS"));
                            } else {
                                sendRequest(DatabaseAPI.fetchFromDatabase("STAFF", args.get(0), args.get(1)));
                            }

                        } else {
                            sendRequest(new Request(type.getName(), "MISSING ARGS"));
                        }
                    }
                    break;

                    case GET_CHANNEL_USERS: {
                        final Request ans = new Request(type.getName());
                        final AdminUserList list = AdminUserList.getInstance();
                        int id = 0;
                        for(AdminUser u : list.getUsers()){
                            if(u.isConnectedToAudio()) {
                                ans.addUserArgs(new UserEntity(id, null, u.getNickname(), u.getStatus().getName(), 0));
                                id++;
                            }
                        }

                        break;
                    }


                    case START_STREAMING: {
                        final List<Event> args = r.getEventArgs();
                        if(verifyArgs(args, 1)){
                            final Event event = args.get(0);
                            // todo

                        } else {
                            sendRequest(new Request(type.getName(), "MISSING ARGS"));
                        }
                    }
                    break;


                    case GET_PORT: {
                        final Request ans;
                        if(user.isConnectedToAudio()) {
                            ans = new Request(type.getName());
                            ans.addIntArgs(user.getAudioHandler().getServerPort());
                        } else {
                            ans = new Request(type.getName(), "NOT CONNECTED");
                        }

                        sendRequest(ans);
                    }
                    break;

                    default:
                        sendRequest(new Request(RequestTypes.ERROR.getName(), "UNKNOWN CMD"));
                        break;
                }

            } catch (IOException | JsonParseException e){
                e.printStackTrace();
                sendRequest(new Request(RequestTypes.ERROR.getName(), "SERVER ERROR"));
                break;
                // TODO LOG
            }

        }

        user.unregisterHandler();
        executor.shutdown();

        try {
            input.close();
        } catch (IOException ignored) { }

        try {
            user.getSocket().close();
        } catch (IOException ignored) { }

        AdminUserList.getInstance().remove(user);
    }

    public void query(Request r){
        executor.execute(new Query(out, r));
    }

    public void sendRequest(Request r){
        /*System.out.println("SENDING RES");
        System.out.println("type -> ".concat(r.getType()));
        System.out.println("isError -> ".concat(String.valueOf(r.isError())));
        if(r.isError())
            System.out.println("error message -> ".concat(r.getErrorMessage()));

        System.out.println("#=====ARGS=====#");
        System.out.println("STRINGS :");
        System.out.println(String.valueOf(r.getStringArgs()));
        System.out.println("INTS :");
        System.out.println(String.valueOf(r.getIntArgs()));
        System.out.println("EVENTS :");
        System.out.println(String.valueOf(r.getEventArgs()));
        System.out.println("USER INFOS :");
        System.out.println(String.valueOf(r.getUserArgs()));
        System.out.println("SANCTIONS :");
        System.out.println(String.valueOf(r.getSanctionArgs()));
        System.out.println("#=====ARGS=====#");*/
        out.println(JSONConverter.getJSONFromRequest(r));
        out.flush();
    }


    private boolean verifyArgs(final List<?> args, final int i, final int j){

        if(args != null){
            return i <= args.size() && args.size() <= j;
        }

        return false;
    }

    // TODO anti null func (like verifyArgs, but verify for field not for length)

    private boolean verifyArgs(final List<?> args, final int n){
        return verifyArgs(args, n, n);
    }

    private <T>List<T> argsGetter(final List<T> globalArgsList, final List<Integer> args) throws IllegalArgumentException {

        if(args.size() == 1){

            int i = args.get(0);

            if(i < 0)
                throw new IllegalArgumentException("NEGATIVE ARG");

            if(i > globalArgsList.size())
                i = globalArgsList.size() - 1;

            return globalArgsList.subList(0, i);


        } else if(args.size() == 2){

            final int i = args.get(0);
            int j = args.get(1);

            if(i < 0 || j < 0)
                throw new IllegalArgumentException("NEGATIVE ARG");

            if(i > j)
                throw new IllegalArgumentException("START HIGHER END");

            if(i >= globalArgsList.size())
                throw new IllegalArgumentException("START TOO HIGH");

            if(j > globalArgsList.size())
                j = globalArgsList.size() - 1;

            return globalArgsList.subList(i, j);

        } else {
            throw new IllegalArgumentException("TOO MUCH ARGS");
        }
    }


    public static class GeneralQuery implements Runnable {
        private final Request r;
        private final AdminUserList list;
        private final AdminUser author;
        private final boolean autoPing;

        public GeneralQuery(Request r, boolean autoPing, AdminUser author) {
            this.r = r;
            this.author = author;
            this.list = AdminUserList.getInstance();
            this.autoPing = autoPing;
        }

        @Override
        public void run() {
            for(AdminUser user : list.getUsers()){

                if(user.equals(author)) {
                    if (autoPing) {
                        user.getHandler().query(r);
                    }
                } else {
                    user.getHandler().query(r);
                }
            }
        }
    }

    public static class Query implements Runnable {
        private final PrintWriter out;
        private final Request res;

        public Query(PrintWriter out, Request res) {
            this.out = out;
            this.res = res;
        }

        @Override
        public void run() {
            out.println(JSONConverter.getJSONFromRequest(res));
            out.flush();
        }
    }
}
