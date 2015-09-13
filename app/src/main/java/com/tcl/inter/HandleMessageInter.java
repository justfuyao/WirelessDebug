package com.tcl.inter;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

public interface HandleMessageInter {
    int handleMSGRead(ByteBuffer buffer, InetSocketAddress socketAddress);

    ByteBuffer handleMSGWrite(String destAdd, int port);

}
