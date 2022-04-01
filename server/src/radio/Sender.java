package radio;

import de.jarnbjo.ogg.FileStream;
import de.jarnbjo.ogg.LogicalOggStream;
import java.io.*;
import java.net.Socket;
import java.util.*;

public class Sender extends Thread {

    private final FileStream fs;
    private Socket s;

    public Sender(File file) throws IOException {
        fs = new FileStream(new RandomAccessFile(file, "r"));
    }

    public void setSocket(Socket s){
        this.s = s;
    }

    @Override
    public void run() {
        System.out.println("[Sender] Starting...");
        final PrintWriter out;
        try {
            out = new PrintWriter(s.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        System.out.println("[Sender] output set, starting to emit to client...");
        try {
            /*for (LogicalOggStream stream : (Collection<LogicalOggStream>) fs.getLogicalStreams()) {
                byte[] nextPacket = stream.getNextOggPacket();


                while (nextPacket != null) {
                    out.write(nextPacket);
                    out.flush();

                    nextPacket = stream.getNextOggPacket();

                    try {
                        Thread.sleep(40);
                    } catch (InterruptedException ignored){}
                }
            }*/

            for(LogicalOggStream stream: (Collection<LogicalOggStream>) fs.getLogicalStreams()){

                byte[] nextPacket = stream.getNextOggPacket();

                while (nextPacket != null) {

                    String finalNextPacket = Base64.getEncoder().encodeToString(nextPacket);
                    if (out != null) {
                        out.println(finalNextPacket);
                        out.flush();
                    } else { // debugging purposes
                        System.out.println("out is null, couldn't send");
                    }

                    nextPacket = stream.getNextOggPacket();

                    // todo wait until someone request to the server to play
                    try {
                        Thread.sleep(40);
                    } catch (InterruptedException ignored) {
                    }

                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
