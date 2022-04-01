package coreclasses.dataclasses;

import controllers.AlertBox;
import coreclasses.RequestListener;
import coreclasses.RequestTypes;
import coreclasses.thread_classes.TCPClient;
import javafx.application.Platform;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class CurrentData implements RequestListener {
    public static final int FETCHERS_SLEEP_TIME = 100;
    public static final int HISTORIQUE_SIZE = 50;

    private static CurrentData instance;

    public static CurrentData getInstance() {
        if(instance == null) instance = new CurrentData();
        return instance;
    }

    private String username;

    private String key;

    private String password;

    private String freestyleBeatDir;

    private String backTrackDir;

    private boolean isFreestyleOn;

    private boolean isBackTrackOn;

    private boolean isBackTrackLoopOn;

    private final List<Refreshable> listeners;

    private final List<MusicInfos> playlist;

    private final List<MusicInfos> historique;

    private final List<UserEntity> users;

    private final List<UserEntity> staff;

    private final List<SanctionEntity> sanctions;

    private final List<Event> events;

    private final List<UserEntity> channelUsers;

    private Event currentEvent;

    private MusicInfos currentMusic;

    private final Map<String, String> messages;

    private final TCPClient client;

    private final Map<String, Fetcher> fetchers;

    private boolean fetchSanctions;
    private boolean fetchEvents;

    private CurrentData(){
        listeners = new ArrayList<>();
        playlist = new ArrayList<>();
        historique = new ArrayList<>();
        users = new ArrayList<>();
        staff = new ArrayList<>();
        sanctions = new ArrayList<>();
        events = new ArrayList<>();
        messages = new HashMap<>();
        channelUsers = new ArrayList<>();
        fetchers = new HashMap<>();

        client = TCPClient.getInstance();
        client.registerListener(this);

        fetchSanctions = false;
        fetchEvents = false;
    }

    public void addListener(Refreshable r){
        if(!listeners.contains(r))
            listeners.add(r);
    }

    public void removeListener(Refreshable r){
        listeners.remove(r);
    }

    public void login(String name, String password, String key){
        this.username = name;
        this.password = password;
        this.key = key;
    }

    public void resetLists(){
        playlist.clear();
        historique.clear();
        users.clear();
        sanctions.clear();
        staff.clear();
        messages.clear();
    }

    public void resetLoginInfos(){
        this.username = null;
        this.password = null;
        this.key = null;
    }

    public void hardReset(){
        resetLists();
        resetLoginInfos();
    }


    public void fetchHistorique() throws IllegalStateException {
        verifyLoggedIn();
        if(!fetchers.containsKey("HISTORIQUE")){
            final Fetcher fetcher = new Fetcher(new Request(RequestTypes.GET_HISTORY.getName()), key);
            new Thread(fetcher).start();
            fetchers.put("HISTORIQUE", fetcher);
        }
    }

    public void fetchPlaylist() throws IllegalStateException {
        verifyLoggedIn();
        if(!fetchers.containsKey("PLAYLIST")){
            final Fetcher fetcher = new Fetcher(new Request(RequestTypes.GET_PLAYLIST.getName()), key);
            new Thread(fetcher).start();
            fetchers.put("PLAYLIST", fetcher);
        }
    }

    public void fetchUsers() throws IllegalStateException {
        System.out.println("[CURRENT DATA] starting fetching user process");
        verifyLoggedIn();
        if(!fetchers.containsKey("USERS")){
            final Fetcher fetcher = new Fetcher(new Request(RequestTypes.GET_USERS.getName()), key);
            new Thread(fetcher).start();
            fetchers.put("USERS", fetcher);
        }
    }

    public void fetchStaff() throws IllegalStateException {
        verifyLoggedIn();
        if(!fetchers.containsKey("STAFF")){
            final Fetcher fetcher = new Fetcher(new Request(RequestTypes.GET_STAFF.getName()), key);
            new Thread(fetcher).start();
            fetchers.put("STAFF", fetcher);
        }
    }

    public void fetchSanctions() throws IllegalStateException {
        fetchSanctions = true;
        verifyLoggedIn();
        if(!fetchers.containsKey("SANCTIONS")){
            final Fetcher fetcher = new Fetcher(new Request(RequestTypes.GET_SANCTIONS.getName()), key);
            new Thread(fetcher).start();
            fetchers.put("SANCTIONS", fetcher);
        }
    }

    public void fetchEvents() throws IllegalStateException {
        fetchEvents = true;
        verifyLoggedIn();
        if(!fetchers.containsKey("EVENTS")){
            final Fetcher fetcher = new Fetcher(new Request(RequestTypes.GET_EVENT.getName()), key);
            new Thread(fetcher).start();
            fetchers.put("EVENTS", fetcher);
        }
    }

    private void stopFetcher(final String name){
        if(fetchers.containsKey(name)){
            System.out.println("[FETCHER STOPPER] fetcher spotted, ca dégage");
            fetchers.get(name).stop();
            fetchers.remove(name);
        } else {
            System.out.println("[FETCHER STOPPER] no fetcher spotted ?");
        }
    }

    public void verifyLoggedIn() throws IllegalStateException {
        if(key == null || username == null || password == null)
            throw new IllegalStateException("NOT LOGGED IN");
    }

    @Override
    public void onReceived(Request r) {
        final RequestTypes type = RequestTypes.getFromName(r.getType());

        if(type == null){
            // TODO LOG
            return;
        }

        if(r.isError()){
            if(!RequestTypes.LOGIN.getName().equals(r.getType()))
                Platform.runLater(() -> AlertBox.requestError(r));
        }

        switch(type){

            case LOGIN:
                break;

            case ADMIN_CHAT_MSG:
                final List<String> stringArgs = r.getStringArgs();
                if (stringArgs != null) {
                    if(stringArgs.size() >= 2){
                        new Pinger(listeners, ref -> ref.onNewAdminChatMessage(stringArgs.get(0), stringArgs.get(1)));
                    } else {
                        // TODO log ?
                    }
                } else {
                    // TODO log ?
                }
                break;

            case ADD_EVENT:
                final List<Event> eventsToAdd = r.getEventArgs();
                if (eventsToAdd != null) {
                    events.addAll(eventsToAdd);
                    for(Event e : eventsToAdd){
                        new Pinger(listeners, ref -> ref.onAddEvent(e));
                    }
                } //TODO log ?
                break;

            case ADD_PLAYLIST:
                List<MusicInfos> playListArgs = r.getMusicArgs();
                if (playListArgs != null) {
                    playlist.addAll(playListArgs);

                    for(MusicInfos infos : playListArgs){
                        new Pinger(listeners, ref -> ref.onAddInPlayList(infos));
                    }
                } //TODO log ?
                break;

            case ADD_SANCTION:
                List<SanctionEntity> sanctionsArgs = r.getSanctionArgs();
                if (sanctionsArgs != null) {
                    sanctions.addAll(sanctionsArgs);

                    for(SanctionEntity s : sanctionsArgs){
                        new Pinger(listeners, ref -> ref.onAddSanction(s));
                    }
                } //TODO log ?
                break;

            case ADD_USER:
                List<UserEntity> userArgs = r.getUserArgs();
                if (userArgs != null) {
                    users.addAll(userArgs);

                    for(UserEntity u : userArgs){
                        new Pinger(listeners, ref -> ref.onAddUser(u));
                    }
                } //TODO log ?
                break;

            case ADD_HISTORY:
                List<MusicInfos> historiqueArgs = r.getMusicArgs();
                if (historiqueArgs != null) {
                    historique.addAll(historiqueArgs);

                    for(MusicInfos infos : historiqueArgs){
                        new Pinger(listeners, ref -> ref.onAddInHistorique(infos));
                    }
                } //TODO log ?

                while(historique.size() > HISTORIQUE_SIZE)
                    historique.remove(0);
                break;


            case DELETE_EVENT:
                final List<Event> eventToDelete = r.getEventArgs();
                if(eventToDelete != null){
                    for(Event e : eventToDelete){
                        events.removeIf(e1 -> {
                            if(e1.getTitle().equals(e.getTitle()) && e1.getDescription().equals(e.getDescription())) {
                                new Pinger(listeners, ref -> ref.onDeleteEvent(e1));
                                return true;
                            }
                            return false;
                        });


                    }
                }
                break;

            case DELETE_SANCTION:
                final List<SanctionEntity> sanctionToDelete = r.getSanctionArgs();
                if(sanctionToDelete != null){
                    for(SanctionEntity s : sanctionToDelete){
                        sanctions.removeIf( s1 -> {
                            if(s.getId() == s1.getId()) {
                                new Pinger(listeners, ref -> ref.onDeleteSanction(s1));
                                return true;
                            }
                            return false;
                        });
                    }
                }
                break;

            case DELETE_USER:
                final List<UserEntity> userToDelete = r.getUserArgs();
                if(userToDelete != null){
                    for(UserEntity u : userToDelete){
                        users.removeIf(u1 -> {
                            if(u.getId() == u1.getId()) {
                                new Pinger(listeners, ref -> ref.onDeleteUser(u1));
                                return true;
                            }
                            return false;
                        });
                    }
                }
                break;

            case DELETE_PLAYLIST:
                final List<MusicInfos> playListToDelete = r.getMusicArgs();
                if(playListToDelete != null){
                    for(MusicInfos m : playListToDelete){
                        playlist.removeIf(m1 -> {
                            if (m1.getTitle().equals(m.getTitle()) && m1.getAuthors().equals(m.getAuthors())) {
                                new Pinger(listeners, ref -> ref.onDeleteInPlayList(m1));
                                return true;
                            }
                            return false;
                        });
                    }
                }
                break;

            case DELETE_HISTORY:
                final List<MusicInfos> historiqueToDelete = r.getMusicArgs();
                if(historiqueToDelete != null){
                    for(MusicInfos m : historiqueToDelete){

                        historique.removeIf(m1 -> {
                                    if (m1.getTitle().equals(m.getTitle()) && m1.getAuthors().equals(m.getAuthors())) {
                                        new Pinger(listeners, ref -> ref.onDeleteInHistorique(m1));
                                        return true;
                                    }
                                    return false;
                                }
                        );
                    }
                }
                break;


            case GET_EVENT:
                if (r.isError()) {
                    if ("OUT OF BOUND".equals(r.getErrorMessage())) {
                        stopFetcher("EVENTS");
                    }
                    // TODO log ?
                } else {
                    if(r.getEventArgs() != null){
                        events.addAll(r.getEventArgs());
                        new Pinger(listeners, ref -> ref.onEventsGotten(events));

                        if(r.getEventArgs().size() != Fetcher.INCREMENTER){
                            stopFetcher("EVENTS");
                        }
                    }
                }
                break;

            case GET_PLAYLIST:
                if (r.isError()) {
                    if ("OUT OF BOUND".equals(r.getErrorMessage())) {
                        stopFetcher("PLAYLIST");
                    }
                    // TODO log ?
                } else {
                    if (r.getMusicArgs() != null) {
                        playlist.addAll(r.getMusicArgs());
                        new Pinger(listeners, ref -> ref.onPlaylistGotten(playlist));

                        if (r.getMusicArgs().size() != Fetcher.INCREMENTER) {
                            stopFetcher("PLAYLIST");
                        }
                    }
                }
                break;

            case GET_USERS:
                System.out.println("[CURRENT DATA] received answer");
                if (r.getUserArgs() != null) {
                    System.out.println("[CURRENT DATA] containing users");
                    users.addAll(r.getUserArgs());
                    new Pinger(listeners, ref -> ref.onUsersGotten(users));

                    if (r.getUserArgs().size() != Fetcher.INCREMENTER) {
                        System.out.println("[CURRENT DATA] not same size -> end of the list -> stopping fetcher");
                        stopFetcher("USERS");
                    }
                } else {
                    System.out.println("[CURRENT DATA] users null, stopping fetcher");
                    stopFetcher("USERS");
                }
                break;

            case GET_STAFF:
                if (r.getUserArgs() != null) {
                    staff.addAll(r.getUserArgs());
                    new Pinger(listeners, ref -> ref.onStaffGotten(staff));

                    if (r.getUserArgs().size() != Fetcher.INCREMENTER) {
                        stopFetcher("STAFF");
                    }
                } else {
                    stopFetcher("STAFF");
                }
                break;

            case GET_SANCTIONS:
                if (r.getSanctionArgs() != null) {
                    sanctions.addAll(r.getSanctionArgs());
                    new Pinger(listeners, ref -> ref.onSanctionsGotten(sanctions));

                    if (r.getSanctionArgs().size() != Fetcher.INCREMENTER) {
                        stopFetcher("SANCTIONS");
                    }
                } else {
                    stopFetcher("SANCTIONS");
                }
                break;

            case GET_HISTORY:
                if(!r.isError()){
                    if(r.getMusicArgs() != null){
                        historique.addAll(r.getMusicArgs());
                        new Pinger(listeners, ref -> ref.onHistoriqueGotten(historique));
                    }
                } // TODO LOG
                break;

            case PLAYING:
                if(!r.isError()) {

                    if(r.getMusicArgs() != null){
                        if(r.getMusicArgs().size() > 0){
                            currentMusic = r.getMusicArgs().get(0);
                            currentEvent = null;
                            new Pinger(listeners, ref -> ref.onNewPlaying(currentMusic));
                        }
                    }

                    final MusicInfos previousMusic = playlist.get(0);
                    playlist.remove(0);
                    historique.add(previousMusic);

                    while (historique.size() > HISTORIQUE_SIZE)
                        historique.remove(0);
                }

                break;

            case CLEAR_EVENTS:
                events.clear();
                new Pinger(listeners, ref -> ref.onEventsGotten(events));
                break;


            case CURRENT_EVENT:
                final List<Event> currentEventAsList = r.getEventArgs();
                if(currentEventAsList != null){
                    if(currentEventAsList.size() > 0){
                        currentEvent = currentEventAsList.get(0);
                        currentMusic = null;
                        new Pinger(listeners, ref -> ref.onChangingEvent(currentEvent));
                    } else {
                        // TODO log ?
                    }
                } else {
                    // TODO log ?
                }
                break;

            case CHANNEL_CONNECT:
                final List<UserEntity> connectingChannelUsers = r.getUserArgs();
                if(connectingChannelUsers != null){
                    for(UserEntity u : connectingChannelUsers){
                        channelUsers.add(u);
                        new Pinger(listeners, ref -> ref.onUserConnectingToChannel(u));
                    }
                }
                break;

            case CHANNEL_DISCONNECT:
                final List<UserEntity> disconnectingChannelUsers = r.getUserArgs();
                if(disconnectingChannelUsers != null){
                    for(UserEntity u : disconnectingChannelUsers){
                        channelUsers.removeIf(u1 -> u1.getNickname().equals(u.getNickname()));
                        new Pinger(listeners, ref -> ref.onUserDisconnectingToChannel(u));
                    }
                }
                break;

            case GET_CHANNEL_USERS:
                final List<UserEntity> currentChannelUsers = r.getUserArgs();
                if(currentChannelUsers != null){
                    channelUsers.clear();
                    channelUsers.addAll(currentChannelUsers);
                }
                break;

                default:
                Platform.runLater(() -> AlertBox.showErrorBox("Erreur - Requête inconnue",
                        "Requête inconnue reçue", ("Une requête inconnue a été reçue. Cela peut être dû au fait que " +
                                "vous possédez une version obselète du client, ou a une typo dans le code du " +
                                "serveur (type de la requete : ").concat(r.getType()).concat(")")));
                break;
        }
    }


    // GETTERS

    public List<MusicInfos> getPlaylist() { return playlist; }

    public List<MusicInfos> getHistorique() { return historique; }

    public List<UserEntity> getUsers() { return users; }

    public List<UserEntity> getStaff() { return staff; }

    public List<SanctionEntity> getSanctions() { return sanctions; }

    public Map<String, String> getMessages() { return messages; }

    public String getUsername() { return username; }

    public String getKey() { return key; }

    public String getPassword() { return password; }

    public List<Event> getEvents() { return events; }

    public String getFreestyleBeatDir() { return freestyleBeatDir; }

    public String getBackTrackDir() { return backTrackDir; }

    public boolean isFreestyleOn() { return isFreestyleOn; }

    public boolean isBackTrackOn() { return isBackTrackOn; }

    public boolean isBackTrackLoopOn() { return isBackTrackLoopOn; }

    public Event getCurrentEvent() { return currentEvent; }

    public MusicInfos getCurrentMusic() { return currentMusic; }

    // SETTERS

    public void setFreestyleBeatDir(String freestyleBeatDir) { this.freestyleBeatDir = freestyleBeatDir; }

    public void setBackTrackDir(String backTrackDir) { this.backTrackDir = backTrackDir; }

    public void setFreestyleOn(boolean freestyleOn) {
        isFreestyleOn = freestyleOn;
        new Pinger(listeners, e -> e.onFreestyleStatusChange(freestyleOn));
    }

    public void setBackTrackOn(boolean backTrackOn) {
        isBackTrackOn = backTrackOn;
        new Pinger(listeners, e -> e.onBackTrackStatusChange(backTrackOn));
    }

    public void setBackTrackLoopOn(boolean backTrackLoopOn) { isBackTrackLoopOn = backTrackLoopOn; }


    private static class Fetcher implements Runnable {
        private static final int INCREMENTER = 10;
        public static final int TIME_TO_SLEEP = 1000;
        private static final TCPClient client = TCPClient.getInstance();

        private final Request r;
        private int i;
        private boolean running;

        public Fetcher(Request r, final String key) {
            this.r = r;
            r.addStringArgs(key);

            this.i = 1;
            this.running = true;
        }

        @Override
        public void run() {
            System.out.println("[FETCHER] starting...");
            while(running){
                r.setIntArgs(Arrays.asList(i, i + INCREMENTER));
                i += INCREMENTER;
                System.out.println("[FETCHER] asking server for objects of id from ".concat(String.valueOf(i - INCREMENTER)).concat(" to ")
                .concat(String.valueOf(i)));
                client.query(r);

                try {
                    TimeUnit.MILLISECONDS.sleep(TIME_TO_SLEEP);
                } catch (InterruptedException ignored) { }
            }
        }

        public void stop(){this.running = false;
            System.out.println("[FETCHER] stopping...");
        }
    }

    public static class Pinger implements Runnable {
        private final List<Refreshable> refreshables;
        public final Pingable pinger;

        public Pinger(List<Refreshable> refreshables, Pingable pinger) {
            this.refreshables = refreshables;
            this.pinger = pinger;
            Platform.runLater(this);
        }

        @Override
        public void run() {
            for(Refreshable r : refreshables){
                pinger.ping(r);
            }
        }
    }

    // INNER INTERFACE

    public interface Refreshable {
        default void onFreestyleStatusChange(boolean newValue){}
        default void onBackTrackStatusChange(boolean newValue){}

        default void onNewChatMessage(String name, String message){}
        default void onNewAdminChatMessage(String name, String message){}

        default void onAddInHistorique(MusicInfos infos){}
        default void onDeleteInHistorique(MusicInfos infos){}
        default void onHistoriqueGotten(List<MusicInfos> currentHistorique){}

        default void onAddInPlayList(MusicInfos infos){}
        default void onDeleteInPlayList(MusicInfos infos){}
        default void onPlaylistGotten(List<MusicInfos> currentPlaylist){}
        default void onNewPlaying(MusicInfos newPlayingMusic){}

        default void onAddEvent(Event event){}
        default void onDeleteEvent(Event event){}
        default void onEventsGotten(List<Event> currentEvents){}
        default void onChangingEvent(Event newCurrentEvent){}

        default void onAddUser(UserEntity user){}
        default void onDeleteUser(UserEntity user){}
        default void onUsersGotten(List<UserEntity> currentUsers){}

        default void onAddStaff(UserEntity user){}      // TODO
        default void onDeleteStaff(UserEntity user){}  // make staff addable and deletable
        default void onStaffGotten(List<UserEntity> currentStaffs){}

        default void onAddSanction(SanctionEntity sanction){}
        default void onDeleteSanction(SanctionEntity sanction){}
        default void onSanctionsGotten(List<SanctionEntity> currentSanctions){}

        default void onUserConnectingToChannel(UserEntity connectingUser){}
        default void onUserDisconnectingToChannel(UserEntity disconnectingUser){}
        default void onGettingChannelUserList(List<UserEntity> connectedUsers){}
    }

    @FunctionalInterface
    private interface Pingable {
        void ping(Refreshable r);
    }
}
