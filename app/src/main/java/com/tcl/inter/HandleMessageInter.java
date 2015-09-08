package com.tcl.inter;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

public interface HandleMessageInter {
    public int handleMSGRead(ByteBuffer buffer, InetSocketAddress socketAddress);

    public ByteBuffer handleMSGWrite(String destAdd, int port);

}
