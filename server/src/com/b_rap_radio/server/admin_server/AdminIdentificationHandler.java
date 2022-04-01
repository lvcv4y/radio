package com.b_rap_radio.server.admin_server;

import com.b_rap_radio.server.dataclasses.DatabaseAPI;
import com.b_rap_radio.server.io_thread_classes.WebClient;
import com.b_rap_radio.server.dataclasses.request_related.JSONConverter;
import com.b_rap_radio.server.dataclasses.request_related.Request;
import com.b_rap_radio.server.dataclasses.request_related.SanctionEntity;
import com.google.gson.JsonParseException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;

public class AdminIdentificationHandler implements Runnable {
    private final Socket s;
    private boolean running;

    public AdminIdentificationHandler(Socket client){
        this.s = client;
        this.running = true;
    }

    @Override
    public void run() {
        BufferedReader input;
        PrintWriter output;

        try {
            input = new BufferedReader(new InputStreamReader(s.getInputStream()));
            output = new PrintWriter(s.getOutputStream());
        } catch (IOException e){
            // TODO LOG
            return;
        }

        while(running && !s.isClosed()){
            try {
                Request r;
                String strReq = input.readLine();

                try {
                    r = JSONConverter.getRequestFromJSON(String.valueOf(strReq));
                } catch (JsonParseException e) {
                    e.printStackTrace();
                    continue;
                }

                /*
                 * SOUT FOR DEBUGGING PROPOSES
                 */
                System.out.println("RECEIVED NEW RES :");
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
                System.out.println("MUSIC INFOS :");
                System.out.println(String.valueOf(r.getMusicArgs()));
                System.out.println("SANCTIONS :");
                System.out.println(String.valueOf(r.getSanctionArgs()));
                System.out.println("#=====ARGS=====#");
                // END SOUT

                if("LOGIN".equals(r.getType())){

                    System.out.println("login process...");

                    List<String> args = r.getStringArgs();

                    if(args != null) {
                        if(args.size() >= 3){

                            Request servReq = DatabaseAPI.staffLoginRequest(args.get(0), args.get(1), args.get(2));

                            if("LOGIN".equals(servReq.getType()) && !servReq.isError()){

                                List<String> servArgs = servReq.getStringArgs();

                                if(servArgs != null){
                                    if(servArgs.size() == 2){
                                        System.out.println("logged");
                                        AdminUser newUser = new AdminUser(servArgs.get(0), servArgs.get(1), s);

                                        Request ans = new Request("LOGIN");
                                        ans.addStringArgs("LOGGED");
                                        output.println(JSONConverter.getJSONFromRequest(ans));
                                        output.flush();


                                        new Thread(new TCPAdminHandler(newUser)).start();
                                        AdminUserList.getInstance().add(newUser);
                                        return;
                                    } else {
                                        System.out.println("first else selected (servArgs.size() != 2)");
                                        // TODO LOG
                                    }

                                } else {

                                    // TODO LOG
                                }

                            } else if(servReq.isError()){
                                output.println(JSONConverter.getJSONFromRequest(servReq));
                            } else {
                                // TODO LOG
                            }

                        } else {
                            output.println(JSONConverter.getJSONFromRequest(new Request("LOGIN", "MISSING ARGS")));
                        }

                    } else {
                        output.println(JSONConverter.getJSONFromRequest(new Request("LOGIN", "ARGS NULL")));
                    }

                } else {
                    output.println(JSONConverter.getJSONFromRequest(new Request("ERROR", "UNKNOWN CMD")));
                }

                output.flush();
            } catch (IOException | JsonParseException e) {
                e.printStackTrace();
                output.println(JSONConverter.getJSONFromRequest(new Request("ERROR", "SERVER ERROR")));
                output.flush();
                break; // todo le virer et handle l'error ? (break pour eviter de saturer console lorsque connection reset
                // TODO LOG
            }
        }

        output.close();


        try {
            input.close();
        } catch (IOException ignored) { }

        try {
            s.close();
        } catch (IOException ignored) { }
    }

    public void shutdown(){this.running = false;}
}
