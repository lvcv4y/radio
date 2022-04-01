package brap.radio.brap_radio.models;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import brap.radio.brap_radio.service.ClientService;

public class NotificationReceiver extends BroadcastReceiver {
    public static final String BUTTON_PRESSED_ACTION_NAME = "BUTTON_PRESSED";

    @Override
    public void onReceive(Context context, Intent intent) {
        String actionType = intent.getStringExtra(BUTTON_PRESSED_ACTION_NAME);
        Intent serviceIntent = new Intent(context, ClientService.class);
        serviceIntent.putExtra(BUTTON_PRESSED_ACTION_NAME, actionType);
        context.startService(serviceIntent);
    }
}
