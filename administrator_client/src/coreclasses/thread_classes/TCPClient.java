package coreclasses.thread_classes;

import coreclasses.RequestListener;
import coreclasses.RequestTypes;
import coreclasses.dataclasses.JSONConverter;
import coreclasses.dataclasses.Request;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class TCPClient implements Runnable {
    public static final String IP = "127.0.0.1";
    public static final int PORT = 60670;

    private static TCPClient instance;

    public static TCPClient getInstance() {
        if(instance == null) instance = new TCPClient();
        return instance;
    }

    private Socket s;
    private final List<RequestListener> listeners;
    private final List<Request> toDo;
    private PrintWriter out;
    private boolean running;

    private TCPClient() {
        listeners = new ArrayList<>();
        toDo = new ArrayList<>();
        out = null;
        s = null;
    }

    @Override
    public void run() {

        running = true;
        final BufferedReader input;
        try {
            input = new BufferedReader(new InputStreamReader(s.getInputStream()));
        } catch (IOException e) {
            e.printStackTrace();
            //TODO error msg
            return;
        }

        for(Request r : toDo){
            out.println(JSONConverter.getJSONFromRequest(r));
            out.flush();
        }

        while(running){
            try {
                String strReq = input.readLine();
                Request r = JSONConverter.getRequestFromJSON(strReq);

                System.out.println("receiving request...");
                if(RequestTypes.GET_USERS.getName().equals(r.getType()))
                    System.out.println("/!\\ it's a \"get users\" request /!\\");

                synchronized (listeners) {
                    for (RequestListener listener : listeners) {
                        try {
                            listener.onReceived(r);
                        } catch (NullPointerException ignored) { }
                    }
                }

            } catch (IOException e){
                e.printStackTrace();
                // TODO LOG
            }
        }
        out.close();

        try {
            input.close();
        } catch (IOException ignored) { }

        try {
            s.close();
        } catch (IOException ignored) { }


        s = null;
        out = null;
    }

    public void connect() throws IOException {
        s = new Socket(IP, PORT);
        out = new PrintWriter(s.getOutputStream());
        new Thread(this).start();
    }

    public void shutdown(){ this.running = false; }

    public boolean isRunning(){return running;}

    public void registerListener(RequestListener listener){
        synchronized (listeners) {
            if (!listeners.contains(listener)) {
                listeners.add(listener);
            }
        }
    }

    public void unregisterListener(RequestListener listener){
        synchronized (listeners) {
            listeners.remove(listener);
        }
    }

    public void query(Request res){

        /*
         * SOUT FOR DEBUGGING PROPOSES
         */
        /*System.out.println("**************************************************************");
        System.out.println("SENDING NEW RES :");
        System.out.println("type -> ".concat(res.getType()));
        System.out.println("isError -> ".concat(String.valueOf(res.isError())));
        if(res.isError())
            System.out.println("error message -> ".concat(res.getErrorMessage()));

        System.out.println("#=====ARGS=====#");
        System.out.println("STRINGS :");
        System.out.println(String.valueOf(res.getStringArgs()));
        System.out.println("INTS :");
        System.out.println(String.valueOf(res.getIntArgs()));
        System.out.println("EVENTS :");
        System.out.println(String.valueOf(res.getEventArgs()));
        System.out.println("MUSIC INFOS :");
        System.out.println(String.valueOf(res.getMusicArgs()));
        System.out.println("SANCTIONS :");
        System.out.println(String.valueOf(res.getSanctionArgs()));
        System.out.println("#=====ARGS=====#");
        // END SOUT*/

        if(out == null || s == null)
            toDo.add(res);
        else
            new Query(res, out);
    }

    private static class Query implements Runnable {

        private final Request r;
        private final PrintWriter out;

        public Query(Request res, PrintWriter output){
            r = res;
            out = output;
            new Thread(this).start();
        }

        @Override
        public void run() {
            out.println(JSONConverter.getJSONFromRequest(r));
            out.flush();
        }
    }
}
