package com.tcl.wirelessdebug;

/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.tcl.bean.MessageUtils;
import com.tcl.config.Configuration;
import com.tcl.database.DatabaseManager;
import com.tcl.database.Msg;
import com.tcl.database.User;
import com.tcl.inter.DispatchMessageInter;
import com.tcl.inter.HandleMessageInter;
import com.tcl.inter.OnMessageSendListener;
import com.tcl.socket.NIOUDPSocketClient;
import com.tcl.socket.NIOUDPSocketServer;
import com.tcl.utils.LogExt;

public class ExchangeMsgService extends Service implements HandleMessageInter, OnMessageSendListener {
    private static final String TAG = "fuyaoExchangeMsgService";

    private volatile Looper mServiceLooper;
    private volatile ServiceHandler mServiceHandler;

    private Map<Long, Msg> mOutUDPMessages = Collections.synchronizedMap(new HashMap<Long, Msg>());

    private Map<Long, Msg> mReceiveUDPMessages = Collections.synchronizedMap(new HashMap<Long, Msg>());

    private NIOUDPSocketClient mUDPClient = null;
    private NIOUDPSocketServer mUDPServer = null;

    private DatabaseManager mDatabaseManager = null;

    public static final int MSG_TIME_OUT = 1;

    private static final int FLAG_SEND_MSG = 1;

    public static final int FLAG_RECEIVE_MSG = 2;

    public ExchangeMsgService() {
        super();
    }

    private Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_TIME_OUT:
                    Msg udp = (Msg) msg.obj;
                    if (udp != null) {
                        if (udp.get_SendTime() < Configuration.MSG_RESEND_MAX_TIME) {
                            LogExt.d(TAG, "~~~~~~~~~~~~~~~ resend time is " + udp.get_SendTime() + " msg is " + udp);
                            notifyClientSend(udp);
                            udp.addSendTime();
                        } else {
                            stopSendMsgTimeout(udp);
                        }
                    }
                    break;

                default:
                    break;
            }
        }

    };

    private void startSendMsgTimeout(Msg msg) {
        // Msg tempMsg = mOutUDPMessages.get(msg.get_Timestamps());
        // if (null != tempMsg) {
        // LogExt.d(TAG, "******************startSendMsgTimeout " + tempMsg);
        // mHandler.sendMessageDelayed(mHandler.obtainMessage(MSG_TIME_OUT,
        // tempMsg), Configuration.MSG_TIMEOUT);
        // }
    }

    private void stopSendMsgTimeout(Msg msg) {
        Msg tempMsg = mOutUDPMessages.remove(msg.get_Timestamps());
        if (null != tempMsg) {
            LogExt.d(TAG, "~~~~~~~~~~~~~~~~~~stopSendMsgTimeout " + tempMsg);
            mHandler.removeMessages(MSG_TIME_OUT, tempMsg);
        }
    }

    public void listenMessage(int readType, int port) {
        switch (readType) {
            case MessageUtils.RECEIVE_TYPE_UDP:
                receiveUDPMessage(port);
                break;
            case MessageUtils.RECEIVE_TYPE_TCP:
                break;
        }
    }

    private void receiveUDPMessage(int port) {
        createUDPServerIfNeed(port);
    }

    private void createUDPServerIfNeed(int port) {
        if (null == mUDPServer) {
            mUDPServer = new NIOUDPSocketServer(port);
            mUDPServer.setHandleMessageInter(this);
            mUDPServer.start();
            Log.e(TAG, "createUDPServerIfNeed create a UDPServer");
        }
    }

    public void sendMessage(int writeType, Msg msg) {
        mServiceHandler.sendMessage(mServiceHandler.obtainMessage(FLAG_SEND_MSG, writeType, 0, msg));
    }

    private void handleSendMsg(int writeType, Msg msg) {
        switch (writeType) {
            case MessageUtils.WRITE_TYPE_UDP:
                sendUPDMessage(msg);
                break;

            default:
                break;
        }
    }

    private void handleSendMsgInit(Msg msg) {
        msg.set_IsReceive(Msg.MSG_SEND_TYPE_SEND);
        msg.set_UserUID(msg.getDstUser().get_UID());
        msg.set_MsgUID(msg.get_Timestamps() + msg.get_UserUID());
    }

    private void handleReceiveMsgInit(Msg msg) {
        msg.set_IsReceive(Msg.MSG_SEND_TYPE_RECEIVE);
        msg.set_UserUID(msg.getSrcUser().get_UID());
        msg.set_MsgUID(msg.get_Timestamps() + msg.get_UserUID());
    }

    private void sendUPDMessage(Msg msg) {
        msg.productSendMsg();
        handleSendMsgInit(msg);
        mDatabaseManager.asynInsertMsg(msg);
        mOutUDPMessages.put(msg.get_Timestamps(), msg);
        NIOUDPSocketClient client = createUDPClientIfNeed();
        client.sendMsg(msg);
    }

    private void notifyClientSend(Msg msg) {
        NIOUDPSocketClient client = createUDPClientIfNeed();
        client.sendMsg(msg);
    }

    private NIOUDPSocketClient createUDPClientIfNeed() {
        if (null == mUDPClient) {
            mUDPClient = new NIOUDPSocketClient();
            mUDPClient.setHandleMessageInter(this);
            mUDPClient.setOnMessageSendListener(this);
            mUDPClient.start();
            Log.e(TAG, "createUDPClientIfNeed create a new UDPClient");
        }
        return mUDPClient;
    }

    private void sendReturnMsg(Msg msg) {
        msg.productSendMsg();
        notifyClientSend(msg);
    }

    // if this msg is return msg, we will stop the send msg's time out.
    // if this msg is normal msg, we will return a msg back tell other we
    // received the msg
    // if this msg we already received, func will return 1
    // if is new msg,func will return 0
    private int handleReceiveMsg(Msg msg) {
        int msgType = msg.get_SendType();
        // received return MSG,so stop time out do send it again
        if (msgType > MessageUtils.TYPE_RETURN_BASE && msgType < MessageUtils.TYPE_RETURN_END) {
            stopSendMsgTimeout(msg);
            LogExt.d(TAG, "handleMSGRead received return msg: " + msg);
        } else {
            // we already received this MSG, we direct send return MSG
            Msg sendMsg = new Msg();
            sendMsg.setDstUser(msg.getSrcUser());
            sendMsg.setSrcUser(TalkApplication.getTalkApplication().getUser());
            int type = MessageUtils.coverType2ReturnType(msgType);
            sendMsg.set_SendType(type);
            sendMsg.setExtraReturnCrc8(msg.get_CRC8());
            sendMsg.set_Timestamps(System.currentTimeMillis());
            sendMsg.setPort(Configuration.UDP_PORT);
            sendReturnMsg(sendMsg);
        }

        if (msgType == MessageUtils.TYPE_SAY_HELLO || msgType == MessageUtils.TYPE_RETURN_SAY_HELLO) {
            User srcUser = msg.getSrcUser();
            // mDatabaseManager.asynInsertOrUpdateUser(srcUser);
            mDatabaseManager.insertOrUpdateUser(srcUser);
            LogExt.d(TAG, "handleReceiveMsg srcUser insert out is " + srcUser);
        }

        // List<Msg> msgs = mDatabaseManager.asynQueryMsg(msg.get_MsgUID());
        List<Msg> msgs = mDatabaseManager.queryMsg(msg.get_MsgUID());
        if (null != msgs) {
            for (Msg tempMsg : msgs) {
                if (msg.compare(tempMsg) > 0) {
                    LogExt.d(TAG, "handleMSGRead received duplicate msg: " + msg);
                    return 0;
                }
            }
        }
        
        if (msgType == MessageUtils.TYPE_RETURN_TALK_MSG) {
//            tempMsg.set_SendTime(Msg.MSG_SEND_TIME_OK);
//            tempMsg.update();
            LogExt.d(TAG, "handleReceiveMsg receive duplicate msg " + msg);
        }
        
        
        long id = mDatabaseManager.asynInsertMsg(msg);
        LogExt.d(TAG, "handleMSGRead received new msg and insert id is " + id + " msg: " + msg);
        return 1;
    }

    private List<DispatchMessageInter> mDispatchMessageInters = new ArrayList<DispatchMessageInter>();

    public void addDispatchMessageInter(DispatchMessageInter d) {
        if (!mDispatchMessageInters.contains(d)) {
            mDispatchMessageInters.add(d);
        }
    }

    public void removeDispatchMessageInter(DispatchMessageInter d) {
        if (mDispatchMessageInters.contains(d)) {
            mDispatchMessageInters.remove(d);
        }
    }

    private final class ServiceHandler extends Handler {
        public ServiceHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            LogExt.d(TAG, "ServiceHandler handleMessage msg " + msg);
            mDatabaseManager.startAsyncSession();
            switch (msg.what) {
                case FLAG_SEND_MSG:
                    handleSendMsg(msg.arg1, (Msg) msg.obj);
                    break;
                case FLAG_RECEIVE_MSG:
                    Msg mmsg = (Msg) msg.obj;
                    if (handleReceiveMsg(mmsg) > 0) {
                        int size = mDispatchMessageInters.size();
                        if (size > 0) {
                            for (int i = 0; i < size; i++) {
                                mDispatchMessageInters.get(i).onNewMsg(mmsg);
                            }
                        }
                    }
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    public void onCreate() {
        // TODO: It would be nice to have an option to hold a partial wakelock
        // during processing, and to have a static startService(Context, Intent)
        // method that would launch the service & hand off a wakelock.

        super.onCreate();
        HandlerThread thread = new HandlerThread("IntentService[" + "ExchangeMsgService" + "]");
        thread.start();

        mServiceLooper = thread.getLooper();
        mServiceHandler = new ServiceHandler(mServiceLooper);

        mDatabaseManager = TalkApplication.getDatabaseProxy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // TODO: the ret value need change
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        mServiceLooper.quit();
    }

    @Override
    public int handleMSGRead(ByteBuffer buffer, InetSocketAddress socketAddress) {
        Msg msg = new Msg();
        msg.setPort(socketAddress.getPort());
        int result = msg.parseBuffer(buffer);
        handleReceiveMsgInit(msg);
        LogExt.d(TAG, "handleMSGRead " + msg);
        if (MessageUtils.PARSE_RESULT_DATA_OK == result) {
            mServiceHandler.sendMessage(mServiceHandler.obtainMessage(FLAG_RECEIVE_MSG, msg));
        }
        return result;
    }

    @Deprecated
    @Override
    public ByteBuffer handleMSGWrite(String destAdd, int port) {
        Msg msg = null;
        LogExt.d(TAG, "handleUDPMSGWrite destAdd = " + destAdd + " port = " + port);
        LogExt.d(TAG, "handleUDPMSGWrite return null");
        return null;
    }

    @Override
    public void onMsgSendOK(Msg msg) {
        if (MessageUtils.TYPE_BASE < msg.get_SendType() && msg.get_SendType() < MessageUtils.TYPE_BASE_END) {
            startSendMsgTimeout(msg);
        }
    }

    @Override
    public void onMsgSendError(Msg msg) {
        // TODO Auto-generated method stub

    }

    @Override
    public IBinder onBind(Intent intent) {
        return new ExchangeBinder();
    }

    public class ExchangeBinder extends Binder {
        ExchangeMsgService getService() {
            return ExchangeMsgService.this;
        }
    }

}
