package brap.radio.brap_radio;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import androidx.annotation.NonNull;

import brap.radio.brap_radio.service.ClientService;
import io.flutter.embedding.android.FlutterActivity;
import io.flutter.embedding.engine.FlutterEngine;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;

public class MainActivity extends FlutterActivity {
    public static final String CHANNEL_NAME = "brap.radio/service_api";
    private ClientService service = null;
    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            service = ((ClientService.ClientBinder) iBinder).getService();
            if(channel != null) service.setChannel(channel);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            service = null;
        }
    };

    private MethodChannel channel;

    @Override
    public void configureFlutterEngine(@NonNull FlutterEngine flutterEngine) {
        super.configureFlutterEngine(flutterEngine);
        channel = new MethodChannel(flutterEngine.getDartExecutor().getBinaryMessenger(), CHANNEL_NAME);
        channel.setMethodCallHandler(this::onMethodCall);
    }

    @Override
    public void cleanUpFlutterEngine(@NonNull FlutterEngine flutterEngine) {
        super.cleanUpFlutterEngine(flutterEngine);
        channel = null;
        if(service != null) service.setChannel(null);
    }

    public void onMethodCall(@NonNull MethodCall call, @NonNull MethodChannel.Result result) {

        switch(call.method){

            case "stopMusicThreads":
                if(service != null) {
                    service.shutdownMusicThreads();
                } else {
                    result.error("SERVICE_NULL", "Service is null (not bound or not started)", null);
                }
                break;

            case "startMusicThreads":
                if(service != null) {
                    service.initMusicThreads();
                } else {
                    result.error("SERVICE_NULL", "Service is null (not bound or not started) or missing argument", null);
                }
                break;

            case "mute":
                final boolean muteStatus = (boolean) call.arguments;
                if(service != null) {
                    service.setMuteStatus(muteStatus);
                } else {
                    result.error("SERVICE_NULL", "Service is null (not bound or not started)", null);
                }
                break;

            case "sendServerRequest":
                if(service != null && service.isTcpClientRunning()){
                    service.tcpQuery((String) call.arguments);
                } else {
                    result.error("SERVICE_NULL", "Service is null (not bound or not started) or not connected", null);
                }
                break;

            case "disconnect":
                if(service != null){
                    service.shutdownTcpClient();
                } else {
                    result.error("SERVICE_NULL", "Service is null (not bound or not started)", null);
                }
                break;

            case "vote":
                try {
                    final int voteStatus = (int) call.arguments; // can't directly cast to short sadly
                    if(service != null) {
                        service.setVoteStatus((short) voteStatus);
                    } else {
                        result.error("SERVICE_NULL", "Service is null (not bound or not started)", null);
                    }
                } catch (ClassCastException e){
                    // we'll just ignore it for the moment (print to debug in case)
                    e.printStackTrace();
                }
                break;


            case "connect":
                System.out.println("[MainAct] connect call received");
                if(service != null){
                    System.out.println("[MainAct] call service init");
                   service.initTcpClient();
                } else {
                    result.error("SERVICE_NULL", "Service is null (not bound or not started)", null);
                }
                break;

            case "initService":
                initService();
                break;

            default:
                result.notImplemented();
                break;
        }
    }

    public void initService(){
        Intent startServiceIntent = new Intent(this, ClientService.class);
        startService(startServiceIntent);

        if(service == null){
            Intent bindingServiceIntent = new Intent(this, ClientService.class);
            bindService(bindingServiceIntent, serviceConnection, BIND_AUTO_CREATE);
        }
    }
}
