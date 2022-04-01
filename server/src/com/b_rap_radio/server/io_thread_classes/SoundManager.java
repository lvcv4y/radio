package com.b_rap_radio.server.io_thread_classes;

import com.b_rap_radio.server.admin_server.SoundStreamer;
import com.b_rap_radio.server.dataclasses.*;
import com.b_rap_radio.server.dataclasses.request_related.Event;
import com.b_rap_radio.server.dataclasses.request_related.Request;
import com.b_rap_radio.server.dataclasses.request_related.RequestTypes;

import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.IOException;

public class SoundManager implements Runnable {

    public static final String RANDOM_NAME = "RANDOM";
    private static SoundManager instance;
    private static final Object lock = new Object();

    public static SoundManager getInstance() throws IOException {
        if(instance == null) instance = new SoundManager();
        return instance;
    }

    public static void playNext(){
        synchronized (lock){
            lock.notifyAll();
        }
    }

    private Event currentEvent;
    private final UserList userList;
    private final Votes votes;
    private final Historique historique;
    private final SoundSender soundSender;
    private final PlayList playlist;
    private final SoundStreamer streamer;
    private boolean pauseMusic;
    private boolean running;

    public SoundManager() throws IOException {
        soundSender = new SoundSender();
        playlist = PlayList.getInstance();
        votes = Votes.getInstance();
        historique = Historique.getInstance();
        userList = UserList.getInstance();
        streamer = SoundStreamer.getInstance(this);
        running = true;
    }


    @Override
    public void run() {
        Music currentMusic;

        while(running){

            // todo wait until a client ask for the next music to play (-> no compromises with sleeping time)
            // maybe stop sleeping and try to send the max amount to each
            // (pros : no sleep issue, lag optimization ; cons : unsynchronized client)
            // or not (pros : sync client ; cons : lag issue (can be solved by sleeping less than needed),
            // sleep causing unsynchronous clients (bc of ping) (solvable using the fastest client as a reference frame
            // (the fastest one ask the server to play the next music, so the server will have to wait for him to finish))

            if(pauseMusic){

                final Request r = new Request(RequestTypes.CURRENT_EVENT.getName());
                r.addEventArgs(currentEvent);
                for (RadioUser u : userList.getUsers()) {
                    u.sendRequest(r);
                }

                try {
                    wait();
                } catch (InterruptedException | IllegalMonitorStateException ignored){ }
            }


            // if sender has been cutout, re-use the last music, otherwise take the next playlist song, chose one randomly if empty
            if(soundSender.getCurrentMusic() != null)
                currentMusic = soundSender.getCurrentMusic();

            else if (playlist.getSize() > 0)
                currentMusic = this.playlist.getCurrentMusic();

            else {
                /* TODO Jouer un classique ta capté*/

                try {
                    currentMusic = new Music("Rien d'spécial", "Népal", "Népal",
                            new File("C://Users/psyfi/Desktop/son.wav"), 0L, RANDOM_NAME);

                } catch (IOException | UnsupportedAudioFileException e) {
                    e.printStackTrace();
                    continue;
                }
                playlist.addBecauseEmpty(currentMusic);
            }

            System.out.println("[SoundManager] sleeping...");

            // wait until someone is listening (== send "play next" request)
            synchronized (lock){
                try {
                    lock.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            System.out.println("[SoundManager] waking up !");

            int i = soundSender.play(currentMusic);

            if(i == SoundSender.ERROR){

                // TODO real log
                System.out.println("Error in soundsender");
                soundSender.getLastException().printStackTrace();

            } else if(i == SoundSender.NO_ERROR) {

                historique.add(currentMusic.getInfos(votes.getNumberOfVotes(), votes.getNumberOfPositiveVotes()));
                playlist.next();
                votes.resetVotes();
            } else if(i == SoundSender.FORCED_CUT) { // pause request, -> streaming
                streamer.notify();
            }
        }
    }

    public void shutdown(){this.running = false;}

    public void instantPause(Event e){
        soundSender.instantStop();
        pause(e);
    }

    public void pause(Event e){
        currentEvent = e;
        pauseMusic = true;
    }

    public void unPause(){
        pauseMusic = false;
        notify();
    }
}
