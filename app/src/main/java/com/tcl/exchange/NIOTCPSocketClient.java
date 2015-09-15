package com.tcl.exchange;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Set;

import com.tcl.config.Configuration;

import android.util.Log;

public class NIOTCPSocketClient implements Runnable {
    private static final String TAG = "fuyao-NIOSocketClient";

    private static int idleCounter = 0;
    private Selector selector;
    private SocketChannel socketChannel;
    private ByteBuffer temp = ByteBuffer.allocate(1024);

    public static void main(String[] args) throws IOException {
        NIOTCPSocketClient client = new NIOTCPSocketClient();
        new Thread(client).start();
        // client.sendFirstMsg();
    }

    public NIOTCPSocketClient() throws IOException {
        // 同样的,注册闹钟.
        this.selector = Selector.open();

        // 连接远程server
        socketChannel = SocketChannel.open();
        socketChannel.configureBlocking(false);
        Boolean isConnected = socketChannel.connect(new InetSocketAddress("166.166.166.219", 9797));
        Log.d(TAG, "NIOSocketClient construct " + isConnected);
        if (isConnected) {
            socketChannel.register(selector, SelectionKey.OP_WRITE);
        } else {
            socketChannel.register(selector, SelectionKey.OP_CONNECT);
        }
    }

    public void sendFirstMsg() throws IOException {
        Log.d(TAG, "sendFirstMsg");
        String msg = "Hello NIO.";
        socketChannel.write(ByteBuffer.wrap(msg.getBytes()));
    }

    @Override
    public void run() {
        while (true) {
            try {
                // 阻塞,等待事件发生,或者1秒超时. num为发生事件的数量.
                int num = this.selector.select(1000);
                Log.d(TAG, "selector size is " + num + " idleCounter = " + idleCounter);
                if (num == 0) {
                    // idleCounter++;
                    // if (idleCounter > 10) {
                    // // 如果server断开了连接,发送消息将失败.
                    // try {
                    // this.sendFirstMsg();
                    // } catch (ClosedChannelException e) {
                    // e.printStackTrace();
                    // this.socketChannel.close();
                    // return;
                    // }
                    // }
                    continue;
                    // } else {
                    // idleCounter = 0;
                }

                Set<SelectionKey> keys = this.selector.selectedKeys();
                Iterator<SelectionKey> it = keys.iterator();
                while (it.hasNext()) {
                    SelectionKey key = it.next();
                    it.remove();
                    if (key.isConnectable()) {
                        Log.d(TAG, "isConnectable");
                        // socket connected
                        SocketChannel sc = (SocketChannel) key.channel();
                        if (sc.isConnectionPending()) {
                            sc.finishConnect();
                        }
                        socketChannel.register(selector, SelectionKey.OP_WRITE);
                    } else if (key.isReadable()) {
                        Log.d(TAG, "isReadable");
                        SocketChannel sc = (SocketChannel) key.channel();
                        this.temp = ByteBuffer.allocate(1024);
                        int count = sc.read(temp);
                        Log.d(TAG, "isReadable read out " + count);
                        if (count < 0) {
                            sc.close();
                            continue;
                        }
                        // 切换buffer到读状态,内部指针归位.
                        temp.flip();
                        String msg = Charset.defaultCharset().decode(temp).toString();
                        Log.d(TAG, "Client received [" + msg + "] from server address:" + sc.socket().getRemoteSocketAddress());// getRemoteAddress());

                        Thread.sleep(1000);
                        // echo back.
                        // sc.write(ByteBuffer.wrap(msg.getBytes(Charset.forName("UTF-8"))));

                        // 清空buffer
                        temp.clear();
                    } else if (key.isWritable()) {
                        Log.d(TAG, "isWritable");
                        SocketChannel sc = (SocketChannel) key.channel();
                        Log.d(TAG, "Client send [" + "client say hello" + "] to server address:" + sc.socket().getRemoteSocketAddress());// getRemoteAddress());
                        sc.write(ByteBuffer.wrap("client say hello".getBytes()));
                        socketChannel.register(selector, SelectionKey.OP_READ);
                    }
                }
            } catch (IOException e) {
                Log.e(TAG, "IOException", e);
            } catch (InterruptedException e) {
                Log.e(TAG, "InterruptedException", e);
            }
        }
    }

    public static class ClientData {
        public SocketAddress clientAddress;
        public ByteBuffer buffer = ByteBuffer.allocate(Configuration.UDP_BUFFER_SIZE);
    }

}
