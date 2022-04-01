package brap.radio.brap_radio.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.session.MediaSession;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.support.v4.media.session.MediaSessionCompat;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import java.io.IOException;

import brap.radio.brap_radio.MainActivity;
import brap.radio.brap_radio.R;
import brap.radio.brap_radio.models.JSONConverter;
import brap.radio.brap_radio.models.MusicInfos;
import brap.radio.brap_radio.models.NotificationReceiver;
import brap.radio.brap_radio.models.Request;
import static brap.radio.brap_radio.App.SERVICE_CHANNEL_ID;

import brap.radio.brap_radio.models.RequestTypes;
import io.flutter.plugin.common.MethodChannel;


public class ClientService extends Service implements TcpListener {
    public static final String BUTTON_PRESSED_ACTION_NAME = "BUTTON_PRESSED";
    public static final int NOTIFICATION_ID = 1;

    private final IBinder binder = new ClientBinder();
    private MethodChannel channel;
    private Handler mainHandler;
    private TcpClientThread tcpClientThread;
    private MusicThreadsManager musicThreadsManager;
    private Bitmap notificationLargeIcon;
    private MediaSessionCompat prettySession;


    @Override
    public void onCreate() {
        super.onCreate();
        mainHandler = new Handler(Looper.getMainLooper());
        tcpClientThread = new TcpClientThread(this);
        tcpClientThread.addListener(this);
        musicThreadsManager = new MusicThreadsManager(this);
        notificationLargeIcon = BitmapFactory.decodeResource(getResources(), R.drawable.logo);

        startForeground(NOTIFICATION_ID, buildNotification());
        prettySession = new MediaSessionCompat(this, "prettyNotificationSession");

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String actionType = null;
        if(intent != null) actionType = intent.getStringExtra(BUTTON_PRESSED_ACTION_NAME);

        if(actionType != null){
            switch (actionType) {
                case "DISCONNECT":
                    shutdownMusicThreads();
                    break;

                case "MUTE" :
                    musicThreadsManager.switchMuted();
                    break;
                case "VOTE_P":
                    tcpClientThread.setVoteStatus((short) 1, true);
                    break;

                case "VOTE_N":
                    tcpClientThread.setVoteStatus((short) -1, true);
                    break;

            }

            refreshNotification();
        }

        return START_NOT_STICKY;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) { // triggered when app swiped out from background app list
        super.onTaskRemoved(rootIntent);
        tcpClientThread.shutdown();
        musicThreadsManager.shutdownThreads();
        stopSelf();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public class ClientBinder extends Binder {
        public ClientService getService(){
            return ClientService.this;
        }
    }

    public void setChannel(MethodChannel channel) {
        this.channel = channel;
    }

    private void invokeMethod(String method, Object arguments){
        if(channel != null){
            if(mainHandler != null)
                mainHandler.post(() -> {
                    try {
                        channel.invokeMethod(method, arguments);
                    } catch (IllegalArgumentException e){
                        e.printStackTrace();
                        channel.invokeMethod(method, null);
                    }
            });
            else
                channel.invokeMethod(method, arguments);
        }
    }

    public void onConnectionError(IOException e){
        shutdownMusicThreads(); // can't continue to play if tcp client disconnected
        invokeMethod("onConnectionError", e.getMessage());
    }

    @Override
    public void onReceivedRequest(String jsonReq) {
        invokeMethod("onReceivedRequest", jsonReq);
    }

    public void refreshMusicProgress(Double progress){
        invokeMethod("refreshMusicProgress", progress);
    }

    public void resetMusicProgress(){
        invokeMethod("resetMusicProgress", null);
    }

    public void refreshVoteStatus(short voteStatus){ // used by service to ping gui
        invokeMethod("refreshVoteStatus", (int) voteStatus);
    }

    public void setVoteStatus(short voteStatus){ // called by gui to ping service's threads
        tcpClientThread.setVoteStatus(voteStatus, true);
    }

    public void refreshMuteStatus(boolean isMute){ // used by service to ping gui
        invokeMethod("refreshMuteStatus", isMute);
    }

    public void setMuteStatus(boolean isMute){
        musicThreadsManager.setMuted(isMute);
    }

    public void playingRequest(MusicInfos infos){
        Request req = new Request(RequestTypes.LOCAL_PLAYING);
        req.addMusicInfosArgs(infos);
        onReceivedRequest(JSONConverter.getJSONFromRequest(req));
        refreshNotification();
    }


    public void addListenerToTcpClientThread(TcpListener listener){
        tcpClientThread.addListener(listener);
    }

    public void removeListenerFromTcpClientThread(TcpListener listener){
        tcpClientThread.removeListener(listener);
    }

    public void initTcpClient() {
        if(!tcpClientThread.isRunning()) {
            tcpClientThread.initAndStartThread();
        }
    }

    public void initMusicThreads() {
        musicThreadsManager.initThreads();
        //new LocalPlayer(this).start();
    }
    
    public boolean isTcpClientRunning() {
        return tcpClientThread.isRunning();
    }

    public void tcpQuery(String jsonReq){
        tcpClientThread.query(jsonReq);
    }

    public void onErrorOnTcpInit(){ // todo add enum param, and handle error
        System.out.println("error spotted by TCP Client !");
        invokeMethod("updateConnectionState", "[\"CONNECTION_ERROR\"]");
    }

    public void onSuccessOnTcpInit(){
        System.out.println("[ClientService] tcp initialized successfully");
        invokeMethod("updateConnectionState", "[\"CONNECTED\"]");
    }

    public void onErrorOnMusicReceiverInit(){
        invokeMethod("onErrorOnMusicReceiverInit", null);
    }

    public void shutdownTcpClient(){
        tcpClientThread.shutdown();
        invokeMethod("updateConnectionState", new String[]{"DISCONNECTED"});
    }

    public void shutdownMusicThreads(){
        musicThreadsManager.shutdownThreads();
        invokeMethod("stoppedMusicThreads", null);
        refreshNotification();
    }

    private Notification buildNotification(){
        PendingIntent toMainActivityIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, MainActivity.class), 0);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, SERVICE_CHANNEL_ID);
        builder.setSmallIcon(R.drawable.ic_round_radio_24)
                .setShowWhen(false)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(toMainActivityIntent);

        if(musicThreadsManager.isRunning()) { // radio is connected
            final Intent stopAndDisconnect = new Intent(this, NotificationReceiver.class);
            stopAndDisconnect.putExtra(BUTTON_PRESSED_ACTION_NAME, "DISCONNECT");

            final Intent muteOrUnMute = new Intent(this, NotificationReceiver.class);
            muteOrUnMute.putExtra(BUTTON_PRESSED_ACTION_NAME, "MUTE");

            PendingIntent positiveVote = null;
            PendingIntent negativeVote = null;

            if(tcpClientThread.getVoteStatus() != (short) 1){
                final Intent positiveVoteIntent = new Intent(this, NotificationReceiver.class);
                positiveVoteIntent.putExtra(BUTTON_PRESSED_ACTION_NAME, "VOTE_P");
                positiveVote = PendingIntent.getBroadcast(this, 1, positiveVoteIntent, 0);
            }

            if(tcpClientThread.getVoteStatus() != (short) -1){
                final Intent negativeVoteIntent = new Intent(this, NotificationReceiver.class);
                negativeVoteIntent.putExtra(BUTTON_PRESSED_ACTION_NAME, "VOTE_N");
                negativeVote = PendingIntent.getBroadcast(this, 2, negativeVoteIntent, 0);
            }

            int muteIcon = R.drawable.ic_round_volume_off_24;

            if(musicThreadsManager.isMuted())
                muteIcon = R.drawable.ic_round_volume_up_24;

            MusicInfos currentlyPlayingMusic = musicThreadsManager.getCurrentlyPlayingMusic();

            builder.setContentTitle(currentlyPlayingMusic.getTitle())
                    .setContentText(currentlyPlayingMusic.getAuthors())
                    .addAction(R.drawable.ic_round_stop_24, "Se déconnecter",
                        PendingIntent.getBroadcast(this, 3, stopAndDisconnect, 0))
                    .addAction(muteIcon, "Mute/Unmute", // todo change name ?
                            PendingIntent.getBroadcast(this, 4, muteOrUnMute, 0))
                    .addAction(positiveVote == null ?
                                    R.drawable.ic_round_thumb_up_24 :
                                    R.drawable.ic_outline_thumb_up_24,
                            "J'aime", positiveVote)
                    .addAction(negativeVote == null ?
                                    R.drawable.ic_round_thumb_down_24 :
                                    R.drawable.ic_outline_thumb_down_24,
                            "Je n'aime pas", negativeVote)
                    .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                            .setShowActionsInCompactView(1, 2)
                            .setMediaSession(prettySession.getSessionToken())
                    )
                    .setLargeIcon(notificationLargeIcon);

        } else {
            builder.setContentTitle("Déconnecté")
                    .setContentText("Vous êtes actuellement déconnecté(e) de la radio.");
        }

        return builder.build();
    }

    public void refreshNotification(){
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if(manager != null) manager.notify(NOTIFICATION_ID, buildNotification());
    }

    public void refreshNotificationLargeIcon(Bitmap newImage){
        if(newImage == null)
            notificationLargeIcon = BitmapFactory.decodeResource(getResources(), R.drawable.logo);
        else
            this.notificationLargeIcon = newImage;

        refreshNotification();
    }
}
