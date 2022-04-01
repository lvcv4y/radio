package com.b_rap_radio.server.dataclasses;

public enum PORTS {
    ADMIN_CHANNEL(60607),
    ADMIN_SERVER_LISTENER(60670),
    RADIO_TCP_LISTENER(32768),
    RADIO_SOUND_SERVER(32769);


    public final int value;
    PORTS(final int value){
        this.value = value;
    }


}
