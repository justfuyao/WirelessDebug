package com.tcl.talkclient;

import com.tcl.bean.AbstractMessage;
import com.tcl.config.Configuration;
import com.tcl.inter.HandleMessageInter;
import com.tcl.inter.OnMessageSendListener;
import com.tcl.utils.LogExt;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.concurrent.ConcurrentLinkedQueue;

public class NIOUDPSocketClient {
    private static final String TAG = "fuyao-NIOUDPSocketClient";

    private ConcurrentLinkedQueue<AbstractMessage> mOutUDPMsgs = new ConcurrentLinkedQueue<AbstractMessage>();

    private DatagramChannel mDatagramChannel;
    private ByteBuffer mReadByteBuffer = ByteBuffer.allocate(Configuration.UDP_BUFFER_SIZE);
    private Thread mSendThread = null;
    private Object mSendLock = new Object();
    private Thread mReceiveThread = null;

    private boolean mStop = false;

    private HandleMessageInter mHandleMessageInter = null;

    public void setHandleMessageInter(HandleMessageInter h) {
        mHandleMessageInter = h;
    }

    private OnMessageSendListener mOnMessageSendListener = null;

    public void setOnMessageSendListener(OnMessageSendListener l) {
        mOnMessageSendListener = l;
    }

    public void start() {
        if (!mStop) {
            mSendThread = new Thread(mSendRunnable, "NIOUDPClient-SendThread");
            mSendThread.start();

            // mReceiveThread = new Thread(mReceiveRunnable,
            // "NIOUDPClient-ReceiveThread");
            // mReceiveThread.start();
            mStop = false;
        }
    }

    public void stop() {
        if (!mStop) {
            mStop = true;
            synchronized (mSendLock) {
                mSendLock.notify();
            }
        }
    }

    public NIOUDPSocketClient() {

        try {
            mDatagramChannel = DatagramChannel.open();
            // mDatagramChannel.socket().bind(new
            // InetSocketAddress(Configuration.UDP_PORT));
            mDatagramChannel.socket().setBroadcast(true);
            mDatagramChannel.configureBlocking(false);

        } catch (IOException e) {
            LogExt.e(TAG, "construct ioexception", e);
        }

    }

    public void sendMsg(AbstractMessage msg) {
        mOutUDPMsgs.add(msg);
        synchronized (mSendLock) {
            mSendLock.notify();
        }

    }

    Runnable mSendRunnable = new Runnable() {
        @Override
        public void run() {
            while (true) {
                AbstractMessage msg;
                while ((msg = mOutUDPMsgs.poll()) != null && !mStop) {
                    try {
                        LogExt.d(
                                TAG,
                                "send to " + msg.getDstIpAdd() + "  " + msg.getPort() + " " + msg.getByteBuffer().toString() + " neirong shi "
                                        + LogExt.bytesToHexString(msg.getByteBuffer().array()));
                        mDatagramChannel.send(msg.getByteBuffer(), new InetSocketAddress(InetAddress.getByName(msg.getDstIpAdd()), msg.getPort()));
                        if (null != mOnMessageSendListener) {
                            mOnMessageSendListener.onMsgSendOK(msg);
                        }
                    } catch (IOException e) {
                        if (null != mOnMessageSendListener) {
                            mOnMessageSendListener.onMsgSendError(msg);
                        }
                        LogExt.e(TAG, "IOException", e);
                    }
                }
                if (mStop) {
                    break;
                }
                synchronized (mSendLock) {
                    try {
                        mSendLock.wait();
                    } catch (InterruptedException e) {
                        LogExt.e(TAG, "InterruptedException", e);
                    }
                }
            }
        }
    };

    Runnable mReceiveRunnable = new Runnable() {
        @Override
        public void run() {
            while (true) {
                LogExt.e(TAG, "mReceiveRunnable begin");
                if (mStop) {
                    break;
                }
                try {
                    mReadByteBuffer.clear();
                    InetSocketAddress socketAddress = (InetSocketAddress) mDatagramChannel.receive(mReadByteBuffer);
                    LogExt.d(TAG, "1 position is " + mReadByteBuffer.position());
                    mReadByteBuffer.flip();
                    LogExt.d(TAG, "2 position is " + mReadByteBuffer.position());
                    LogExt.e(TAG, "mReceiveRunnable received !");
                    if (null != mHandleMessageInter) {
                        mHandleMessageInter.handleMSGRead(mReadByteBuffer, socketAddress);
                    } else {
                        LogExt.e(TAG, "mHandleMessageInter is null !!!!!");
                    }
                } catch (IOException e) {
                    LogExt.e(TAG, "IOException", e);
                }
                LogExt.e(TAG, "mReceiveRunnable end");
            }
        }
    };

}
