package com.tcl.talkServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.tcl.config.Configuration;
import com.tcl.inter.HandleMessageInter;
import com.tcl.utils.LogExt;

public class NIOTCPSocketServer {
    private static final String TAG = "fuyao-NIOSocketServer";

    private ConcurrentLinkedQueue<SelectionKey> mConnectionQueue = new ConcurrentLinkedQueue<SelectionKey>();

    private ConcurrentLinkedQueue<Connection> mConnectedQueue = new ConcurrentLinkedQueue<Connection>();

    private final int m_processNum = 3;
    private final int m_worksNum = 3;

    private final int TALK_MAX_THREAD = 10;

    private ServerSocketChannel channel;
    private Selector selector;// for connection
    private List<Selector> mRequestQueueSelector = new ArrayList<Selector>();
    ByteBuffer mByteBuffer;

    private HandleMessageInter mHandleMessageInter = null;

    public void setHandleMessageInter(HandleMessageInter h) {
        mHandleMessageInter = h;
    }

    public void listen(int port) throws IOException {
        mByteBuffer = ByteBuffer.allocate(Configuration.TCP_BUFFER_SIZE);

        selector = Selector.open();
        channel = ServerSocketChannel.open();
        channel.configureBlocking(false);
        channel.socket().bind(new InetSocketAddress(port));
        channel.register(selector, SelectionKey.OP_ACCEPT);

        new Thread(new ConnectionHander(), "ConnectionHander-Thread").start();

        creatRequestHanders();
    }

    void creatRequestHanders() {
        try {
            for (int i = 0; i < m_processNum; ++i) {
                Selector slt = Selector.open();
                mRequestQueueSelector.add(slt);
                RequestHander req = new RequestHander();
                req.setSelector(slt);
                new Thread(req, "RequestHander-Thread---" + (i + 1)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void handleNewTalk(SocketChannel channel, Selector sc) {
        synchronized (this) {
            int lenth = mConnectedQueue.size();

            if (lenth >= TALK_MAX_THREAD) {
                Connection connection = mConnectedQueue.poll();
                connection.runnable.stop();
                lenth--;
            }

            ExchangeRunnable talk = new ExchangeRunnable();
            Thread thread = new Thread(talk, "TalkHanlder-Thread---" + (lenth + 1));
            mConnectedQueue.add(new Connection(channel, sc, thread, talk));
            thread.start();
        }
    }

    class ExchangeRunnable implements Runnable {

        private Selector selector;
        private boolean stop = false;

        public void setSelector(Selector slt) {
            selector = slt;
        }

        public void stop() {
            stop = true;
        }

        @Override
        public void run() {
            while (true) {
                try {
                    selector.select();

                    if (stop) {
                        break;
                    }
                    Set<SelectionKey> keys = selector.selectedKeys();
                    Iterator<SelectionKey> it = keys.iterator();
                    while (it.hasNext()) {
                        SelectionKey keytmp = it.next();
                        it.remove();
                        if (keytmp.isReadable()) {
                            LogExt.d(TAG, "RequestHander isReadable  mRequestQueue add a new SelectionKey");

                            SocketChannel channel = (SocketChannel) keytmp.channel();

                            int length = 0;
                            int sum = 0;
                            while ((length = channel.read(mByteBuffer)) > 0) {
                                sum += length;

                            }
                            if (null != mHandleMessageInter) {
//                                mHandleMessageInter.handleMSGRead(mByteBuffer, channel.);
                            } else {
                                LogExt.e(TAG, "mHandleMessageInter is null !!!!!");
                            }
                        }
                        if (keytmp.isWritable()) {
                            LogExt.d(TAG, "RequestHander isWritable");
                            if (null != mHandleMessageInter) {
//                                mHandleMessageInter.handleMSGWrite();
                            } else {
                                LogExt.e(TAG, "mHandleMessageInter is null !!!!!");
                            }
                        }
                    }

                } catch (IOException e) {
                    LogExt.e(TAG, "ExchangeRunnable IOException", e);
                }
            }
        }
    }

    class ConnectionHander implements Runnable {

        int idx = 0;
        int exceptionTime = 0;

        @Override
        public void run() {

            LogExt.d(TAG, "ConnectionHander in");
            while (true) {

                try {
                    LogExt.d(TAG, "ConnectionHander while in 1");
                    selector.select();
                    LogExt.d(TAG, "ConnectionHander while in 2");
                    Set<SelectionKey> selectKeys = selector.selectedKeys();
                    Iterator<SelectionKey> it = selectKeys.iterator();

                    while (it.hasNext()) {
                        SelectionKey key = it.next();
                        it.remove();
                        if (key.isAcceptable()) {
                            LogExt.d(TAG, "ConnectionHander mConnectionQueue add a new SelectionKey idx = " + idx);
                            mConnectionQueue.add(key);
                            int num = mRequestQueueSelector.size();
                            // 唤醒request的进程处理
                            mRequestQueueSelector.get(idx).wakeup();
                            idx = (idx + 1) % num;
                        }
                    }
                    LogExt.d(TAG, "ConnectionHander while out 3");
                } catch (IOException e) {
                    LogExt.e(TAG, "ConnectionHander IOException", e);
                }

            }
        }
    }

    // 监视读操作
    class RequestHander implements Runnable {
        private Selector selector;

        public void setSelector(Selector slt) {
            selector = slt;
        }

        public void run() {
            try {
                SelectionKey key;
                LogExt.d(TAG, "RequestHander");
                while (true) {
                    LogExt.d(TAG, "RequestHander while in 1");
                    selector.select();
                    LogExt.d(TAG, "RequestHander while in 2");

                    Selector curSelector = null;
                    SocketChannel sc = null;
                    while ((key = mConnectionQueue.poll()) != null) {
                        LogExt.d(TAG, "RequestHander mConnectionQueue poll a SelectionKey");
                        ServerSocketChannel ssc = (ServerSocketChannel) key.channel();
                        sc = ssc.accept();// 接受一个连接
                        if (null == sc) {
                            continue;
                        }
                        sc.configureBlocking(false);
                        sc.socket().setReceiveBufferSize(Configuration.TCP_BUFFER_SIZE);
                        curSelector = Selector.open();
                        sc.register(curSelector, SelectionKey.OP_READ);
                        LogExt.d(TAG, "RequestHander a connected line");
                        handleNewTalk(sc, curSelector);
                    }
                    LogExt.d(TAG, "RequestHander while out 3");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    class Connection {
        SocketChannel sChannel = null;
        Selector selector = null;
        Thread thread = null;
        ExchangeRunnable runnable = null;

        public Connection(SocketChannel channel, Selector s, Thread t, ExchangeRunnable r) {
            sChannel = channel;
            selector = s;
            thread = t;
            runnable = r;
        }

        public void start() {
            thread.start();
        }

        public void stop() {
            runnable.stop();
            selector.wakeup();
            try {
                sChannel.close();
                selector.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
