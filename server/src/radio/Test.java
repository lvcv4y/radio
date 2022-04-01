package radio;

import com.b_rap_radio.server.dataclasses.DatabaseAPI;
import com.b_rap_radio.server.dataclasses.request_related.Request;
import de.jarnbjo.ogg.FileStream;
import de.jarnbjo.ogg.LogicalOggStream;
import org.jitsi.impl.neomedia.codec.audio.opus.Opus;
import org.mindrot.jbcrypt.BCrypt;

import javax.sound.sampled.*;
import java.io.*;
import java.nio.ByteBuffer;
import java.util.*;


public class Test {

    public static class T {
        @Override
        public String toString() {
            return "test";
        }
    }


    public static void main(String[] args) throws IOException, UnsupportedAudioFileException, InterruptedException {

        /*ServerSocket serverSocket = new ServerSocket(12345);
        Sender sender = new Sender(new File("D://playlist/opus/2.opus"));
        System.out.println("[Main] creating serversocket and sender");

        new Thread(() -> {
            try {
                System.out.println("[Thread-0] Waiting for receiver client...");
                Socket s = serverSocket.accept();
                System.out.println("[Thread-0] Client accepted ; starting sender");
                sender.setSocket(s);
                sender.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();

        System.out.println("[Main] Connecting Receiver socket...");
        Socket s1 = new Socket("192.168.1.105", 12345);
        System.out.println("[Main] Connected ! Starting Receiver Thread...");
        new ReceiverAndPlayer(s1).start();*/

        /*
        List<byte[]> list = new ArrayList<>();

        File playlist = new File(PlayList.PLAYLIST_PATH);

        for (File f: playlist.listFiles()){

            list.clear();

            if(!"UmllbiBkJ1Nww6ljaWFsI07DqXBhbCM0NDQgTnVpdHM=.opus".equals(f.getName())){

                FileStream oggFile = new FileStream(new RandomAccessFile(f, "r"));

                for (LogicalOggStream stream : (Collection<LogicalOggStream>) oggFile.getLogicalStreams()) {
                    try {
                        byte[] nextPacket = stream.getNextOggPacket();
                        while (nextPacket != null) {
                            list.add(nextPacket);
                            nextPacket = stream.getNextOggPacket();
                        }
                    } catch (EndOfOggStreamException e){
                        break;
                    }
                }

                oggFile.close();

                if(!f.delete()){
                    System.out.println("can't delete " + f.getName());
                    return;
                }

                if(!f.createNewFile()){
                    System.out.println("can't create new file");
                    return;
                }

                OutputStream os = new FileOutputStream(f);
                os.write("OggS".getBytes());
                os.flush();

                for(byte[] b : list) {
                    os.write(b);
                    os.flush();
                }

                os.close();
            }

        }
        */
        /*
        List<MusicToAdd> files = Arrays.asList(
                new MusicToAdd("60 Années", "Lithopédion", "Damso"),
                new MusicToAdd("ASB", "Ce monde est cruel", "Vald, Maes"),
                new MusicToAdd("BXL ZOO", "QALF Infinity","Damso, Hamza"),
                new MusicToAdd("Desiigner", "La Menace Fantôme","Freeze Corleone"),
                new MusicToAdd("fahrenheit 451", "don dada mixtape vol 1", "Alpha Wann"),
                new MusicToAdd("Feu de bois", "Lithopédion", "Damso"),
                new MusicToAdd("Freeze Raël", "La Menace Fantôme", "Freeze Corleone"),
                new MusicToAdd("Gris", "XEU","Vald"),
                new MusicToAdd("Hors ligne", "La Menace Fantôme","Freeze Corleone"),
                new MusicToAdd("Ignorant", "Ce monde est cruel", "Vald"),
                new MusicToAdd("Ipséité (Bonus Track)", "Lithopédion", "Damso"),
                new MusicToAdd("la lune attire la mer", "don dada mixtape vol 1", "Alpha Wann"),
                new MusicToAdd("mitsubishi", "don dada mixtape vol 1","Alpha Wann"),
                new MusicToAdd("N. J Respect R", "Ipséité","Damso"),
                new MusicToAdd("No Friends", "XEU", "Vald"),
                new MusicToAdd("NQNTMQMQMB", "Ce monde est cruel", "Vald"),
                new MusicToAdd("philly flingo", "don dada mixtape vol 1", "Alpha Wann"),
                new MusicToAdd("Primitif", "XEU","Vald"),
                new MusicToAdd("Rap catéchisme", "La Menace Fantôme","Freeze Corleone, Alpha Wann"),
                new MusicToAdd("Réflexions basses", "XEU", "Vald"),
                new MusicToAdd("Rituel", "XEU", "Vald, Sirius"),
                new MusicToAdd("Γ. Mosaïque solitaire", "Ipséité", "Damso"),
                new MusicToAdd("Δ. Dieu ne ment jamais", "Ipséité","Damso"),
                new MusicToAdd("Ζ. Kietu", "Ipséité","Damso"),
                new MusicToAdd("Π. VANTABLACK", "QALF Infinity", "Damso"),
                new MusicToAdd("Σ. MOROSE", "QALF Infinity", "Damso"),
                new MusicToAdd("Φ. THEVIE RADIO", "QALF Infinity", "Damso")
        );*/

        System.out.println(BCrypt.hashpw("ellijahestvrmtgay", BCrypt.gensalt(10)));
        // $2a$10$4nX7O1qpE5I7SKY4nQWXg.9CsYqMiqvif.KAf/IQU2o/eOg2KovKu
    }

    public static File f(String name){
        return new File("D://playlist/newoggs/" + name + ".opus");
    }

    public static class MusicToAdd {
        private final Integer[] authors;
        private final Integer album;
        private final String title;
        private final File file;

        public MusicToAdd(String title, String album, String authors){
            this.title = title;
            this.file = f(title);

            if(!file.exists())
                throw new IllegalStateException("file for " + title + " doesn't exist");

            int albumId = DatabaseAPI.getAlbumIdFromTitle(album);
            if(albumId <= 0)
                throw new IllegalStateException("album \"" + album + "\" not found");

            this.album = albumId;

            List<Integer> authorsId = new ArrayList<>();
            for(String author : authors.split(",")){
                int id = DatabaseAPI.getArtistIdFromName(author.trim());
                if(id <= 0)
                    throw new IllegalStateException("artist \"" + author + "\" not found");

                authorsId.add(id);
            }

            this.authors = authorsId.toArray(new Integer[0]);
        }

        public Request add(){
            return DatabaseAPI.addMusic(title, authors, album, file);
        }
    }



    public static class OpusAudioPlayer {
        private static final int BUFFER_SIZE = 1024*1024;
        private static final int INPUT_BITRATE = 48000;
        private static final int OUTPUT_BITRATE = 48000;

        private final FileStream oggFile;
        private final long opusState;


        private final ByteBuffer decodeBuffer = ByteBuffer.allocate(BUFFER_SIZE);

        private final AudioFormat audioFormat = new AudioFormat(OUTPUT_BITRATE, 16, 2, true, false);

        public OpusAudioPlayer(File audioFile) throws IOException {
            oggFile = new FileStream(new RandomAccessFile(audioFile, "r"));
            opusState = Opus.decoder_create(INPUT_BITRATE, 2);
        }

        private byte[] decode(byte[] packetData) {
            int frameSize = Opus.decoder_get_nb_samples(opusState, packetData, 0, packetData.length);
            int decodedSamples = Opus.decode(opusState, packetData, 0, packetData.length,
                    decodeBuffer.array(), 0, 2880, 0);
            if (decodedSamples < 0) {
                System.out.println("Decode error: " + decodedSamples);
                decodeBuffer.clear();
                return null;
            }
            decodeBuffer.position(decodedSamples * 2 * 2); // 2 bytes per sample, * number_of_channels (here, 2)
            decodeBuffer.flip();

            byte[] decodedData = new byte[decodeBuffer.remaining()];
            decodeBuffer.get(decodedData);
            decodeBuffer.flip();
            return decodedData;
        }

        public void play() {
            try {
                SourceDataLine speaker = AudioSystem.getSourceDataLine(audioFormat);
                speaker.open();
                speaker.start();
                for (LogicalOggStream stream : (Collection<LogicalOggStream>) oggFile.getLogicalStreams()) {
                    byte[] nextPacket = stream.getNextOggPacket();
                    while (nextPacket != null) {
                        byte[] decodedData = decode(nextPacket);
                        if(decodedData != null) {
                            // Write packet to SourceDataLine
                            speaker.write(decodedData, 0, decodedData.length);
                        }
                        nextPacket = stream.getNextOggPacket();
                    }
                }
                speaker.drain();
                speaker.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}


