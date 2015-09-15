package com.tcl.wirelessdebug;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import com.tcl.bean.MessageUtils;
import com.tcl.config.Configuration;
import com.tcl.database.Constants;
import com.tcl.database.DaoMaster;
import com.tcl.database.DaoMaster.OpenHelper;
import com.tcl.database.DaoSession;
import com.tcl.database.DatabaseManager;
import com.tcl.database.Msg;
import com.tcl.database.User;
import com.tcl.exchange.NIOUDPSocketClient;
import com.tcl.exchange.NIOUDPSocketServer;
import com.tcl.inter.DispatchMessageInter;
import com.tcl.inter.HandleMessageInter;
import com.tcl.inter.OnMessageSendListener;
import com.tcl.utils.IPv4v6Utils;
import com.tcl.utils.LogExt;

public class TalkApplication extends Application implements HandleMessageInter, OnMessageSendListener {
    private static final String TAG = "fuyao-TalkApplication";

    private static TalkApplication mTalkApplication = null;

    private static DatabaseManager mDatabaseProxy = null;

    private static DaoMaster mDaoMaster;

    private static DaoSession mDaoSession;

    private Map<String, Msg> mOutUDPMessages = Collections.synchronizedMap(new HashMap<String, Msg>());

    private Map<Long, Msg> mReceiveUDPMessages = Collections.synchronizedMap(new HashMap<Long, Msg>());

    private NIOUDPSocketClient mUDPClient = null;

    private NIOUDPSocketServer mUDPServer = null;

    public static final int MSG_TIME_OUT = 1;

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

    private User mUser = null;

    public User getUser() {
        return mUser;
    }

    public void setUser(User u) {
        mUser = u;
        saveUser(u);
    }

    private User restoreUser() {
        SharedPreferences sPreferences = getSharedPreferences("user", MODE_PRIVATE);
        String name = sPreferences.getString("name", "");
        if (TextUtils.isEmpty(name)) {
            return null;
        }

        String uid = sPreferences.getString("uid", "");
        if (TextUtils.isEmpty(uid)) {
            return null;
        }

        String ip = IPv4v6Utils.getLocalIPAddress();
        return new User(null, name, ip, uid);
    }

    private void saveUser(User u) {
        SharedPreferences sPreferences = getSharedPreferences("user", MODE_PRIVATE);
        Editor editor = sPreferences.edit();
        editor.putString("name", u.get_Name());
        editor.putString("uid", u.get_UID());
        editor.apply();
    }

    public static TalkApplication getTalkApplication() {
        return mTalkApplication;
    }

    public static DatabaseManager getDatabaseProxy() {
        return mDatabaseProxy;
    }

    // not threadSafe
    public static DaoMaster getDaoMaster(Context context) {
        if (mDaoMaster == null) {
            OpenHelper helper = new DaoMaster.DevOpenHelper(context, Constants.DB_NAME, null);
            mDaoMaster = new DaoMaster(helper.getWritableDatabase());
        }
        return mDaoMaster;
    }

    // not threadSafe
    public static DaoSession getDaoSession(Context context) {
        if (mDaoSession == null) {
            if (mDaoMaster == null) {
                mDaoMaster = getDaoMaster(context);
            }
            mDaoSession = mDaoMaster.newSession();
        }
        return mDaoSession;
    }


    public void listenMessage(int readType, int port){
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
        switch (writeType) {
            case MessageUtils.WRITE_TYPE_UDP:
                sendUPDMessage(msg);
                break;

            default:
                break;
        }
    }

    private void sendUPDMessage(Msg msg) {
        msg.productSendMsg();
        mOutUDPMessages.put(String.valueOf(msg.getCRC8()), msg);
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

    @Override
    public void onCreate() {
        super.onCreate();
        if (null == mTalkApplication) {
            mTalkApplication = this;
        }
        if (null == mDatabaseProxy) {
            mDatabaseProxy = DatabaseManager.getInstance(this);
        }
        mUser = restoreUser();
    }

    private void startSendMsgTimeout(Msg msg) {
        Msg tempMsg = mOutUDPMessages.get(String.valueOf(msg.getCRC8()));
        if (null != tempMsg) {
            LogExt.d(TAG, "******************startSendMsgTimeout " + tempMsg);
            mHandler.sendMessageDelayed(mHandler.obtainMessage(MSG_TIME_OUT, tempMsg), Configuration.MSG_TIMEOUT);
        }
    }

    private void stopSendMsgTimeout(Msg msg) {
        Msg tempMsg = mOutUDPMessages.remove(String.valueOf(msg.getReturnCRC8()));
        if (null != tempMsg) {
            LogExt.d(TAG, "~~~~~~~~~~~~~~~~~~stopSendMsgTimeout " + tempMsg);
            mHandler.removeMessages(MSG_TIME_OUT, tempMsg);
        }
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
    private int doPreDispatch(Msg msg) {
        int msgType = msg.get_Type();
        // received return MSG,so stop time out do send it again
        if (msgType > MessageUtils.TYPE_RETURN_BASE && msgType < MessageUtils.TYPE_RETURN_END) {
            stopSendMsgTimeout(msg);
            LogExt.d(TAG, "handleMSGRead received return msg: " + msg);
        } else {
            // we already received this MSG, we direct send return MSG
            Msg sendMsg = new Msg();
            sendMsg.setDstUser(msg.getSrcUser());
            sendMsg.setSrcUser(mUser);
            int type = MessageUtils.coverType2ReturnType(msgType);
            sendMsg.set_Type(type);
            sendMsg.setExtraReturnCrc8(msg.get_CRC8());
            sendMsg.set_Timestamps(System.currentTimeMillis());
            sendMsg.setPort(Configuration.UDP_PORT);
            sendReturnMsg(sendMsg);
        }

        Msg tempMsg = mReceiveUDPMessages.get(msg.getTime());
        if (msg.compare(tempMsg) > 0) {
            LogExt.d(TAG, "handleMSGRead received duplicate msg: " + msg);
            return 0;
        }

        mReceiveUDPMessages.put(msg.getTime(), msg);
        LogExt.d(TAG, "handleMSGRead received new msg: " + msg);
        return 1;
    }

    @Override
    public int handleMSGRead(ByteBuffer buffer, InetSocketAddress socketAddress) {
        Msg msg = new Msg();
        msg.setPort(socketAddress.getPort());
        int result = msg.parseBuffer(buffer);
        LogExt.d(TAG, "handleMSGRead " + msg);
        if (MessageUtils.PARSE_RESULT_DATA_OK == result) {
            if (doPreDispatch(msg) > 0) {
                int size = mDispatchMessageInters.size();
                if (size > 0) {
                    for (int i = 0; i < size; i++) {
                        mDispatchMessageInters.get(i).onNewMsg(msg);
                    }
                }
            }
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
        if (MessageUtils.TYPE_BASE < msg.getType() && msg.getType() < MessageUtils.TYPE_BASE_END) {
            startSendMsgTimeout(msg);
        }
    }

    @Override
    public void onMsgSendError(Msg msg) {
        // TODO Auto-generated method stub

    }

}
