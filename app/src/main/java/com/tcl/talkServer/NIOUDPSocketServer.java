package com.tcl.talkServer;

import com.tcl.config.Configuration;
import com.tcl.inter.HandleMessageInter;
import com.tcl.utils.LogExt;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;
import java.util.Set;

public class NIOUDPSocketServer {
    private static final String TAG = "fuyao-NIOUDPSocketServer";

    private Selector mSelector;
    private DatagramChannel mDatagramChannel;
    private ByteBuffer mReadByteBuffer = ByteBuffer.allocate(Configuration.UDP_BUFFER_SIZE);
    private Thread mThread = null;
    private boolean mStop = false;

    private int mDestPort = -1;
    private String mDestAdd = "";

    public int getPort() {
        return mDestPort;
    }

    public NIOUDPSocketServer(int port) {
        mDestPort = port;
    }

    public void start() {
        mThread = new Thread(mRequestRunnable, "Thread-NIOUDPSocketServer");
        mThread.start();
        mStop = false;
    }

    public void stop() {
        mStop = true;
        mSelector.wakeup();
    }

    private HandleMessageInter mHandleMessageInter = null;

    public void setHandleMessageInter(HandleMessageInter h) {
        mHandleMessageInter = h;
    }

    Runnable mRequestRunnable = new Runnable() {
        @Override
        public void run() {

            try {
                mSelector = Selector.open();
                mDatagramChannel = DatagramChannel.open();
                mDatagramChannel.configureBlocking(false);
                mDatagramChannel.socket().bind(new InetSocketAddress(Configuration.UDP_PORT));
                mDatagramChannel.register(mSelector, SelectionKey.OP_READ);
            } catch (IOException e1) {
                LogExt.e(TAG, "IOException", e1);
            }

            while (true) {
                try {
                    int num = mSelector.select();
                    if (mStop) {
                        mDatagramChannel.close();
                        mDatagramChannel = null;
                        mSelector.close();
                        mSelector = null;
                        mStop = false;
                        break;
                    }

                    if (num == 0) {
                        continue;
                    }

                    Set<SelectionKey> keys = mSelector.selectedKeys();
                    Iterator<SelectionKey> it = keys.iterator();
                    while (it.hasNext()) {
                        SelectionKey key = it.next();
                        it.remove();
                        if (key.isReadable()) {
                            DatagramChannel dChannel = (DatagramChannel) key.channel();
                            mReadByteBuffer.clear();
                            InetSocketAddress socketAddress = (InetSocketAddress) dChannel.receive(mReadByteBuffer);
                            mReadByteBuffer.flip();
                            LogExt.d(TAG, "read size: " + mReadByteBuffer.limit() + " socketAddress :" + socketAddress);

                            if (null != mHandleMessageInter && mReadByteBuffer.limit() > 0) {
                                mHandleMessageInter.handleMSGRead(mReadByteBuffer, socketAddress);
                            } else {
                                LogExt.e(TAG, "mHandleMessageInter is null or read 0 byte!!!!!");
                            }
                        } else if (key.isWritable()) {
                            LogExt.d(TAG, "isWritable ");
                            DatagramChannel dChannel = (DatagramChannel) key.channel();
                            if (null != mHandleMessageInter) {
                                ByteBuffer buffer = mHandleMessageInter.handleMSGWrite(mDestAdd, mDestPort);
                                if (buffer != null) {
                                    buffer.flip();
                                    LogExt.d(TAG, "write to " + mDestPort + " " + buffer.toString() + " neirong shi " + new String(buffer.array()));
                                    dChannel.send(buffer, new InetSocketAddress(InetAddress.getByName(mDestAdd), mDestPort));
                                }
                            } else {
                                LogExt.e(TAG, "mHandleMessageInter is null !!!!!");
                            }
                            dChannel.register(mSelector, SelectionKey.OP_READ);
                        }
                    }
                } catch (IOException e) {
                    LogExt.e(TAG, "IOException", e);
                }
            }
        }
    };

    public String getSubTag(String tag) {
        return "[" + Thread.currentThread().getId() + "-" + Thread.currentThread().getName() + "]";
    }
}
