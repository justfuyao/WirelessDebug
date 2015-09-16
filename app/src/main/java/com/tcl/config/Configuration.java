package com.tcl.config;

public class Configuration {
    public static final int TCP_PORT = 9797;
    public static final int UDP_PORT = 9798;

    public static final int TCP_BUFFER_SIZE = 512;
    public static final int UDP_BUFFER_SIZE = 512;

    public static final int MSG_TIMEOUT = 8000;
    public static final int MSG_RESEND_MAX_TIME = 3;

    public static final String UDP_BROADCAST_ADDRESS = "255.255.255.255";
    public static final String UDP_BROADCAST_UID = "88888888";
    public static final String UDP_BROADCAST_NAME = "Broadcast";
}
