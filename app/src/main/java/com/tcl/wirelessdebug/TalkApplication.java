package com.tcl.wirelessdebug;

import android.app.Application;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import com.tcl.bean.AbstractMessage;
import com.tcl.bean.MessageUtils;
import com.tcl.bean.ReceiveUDPMessage;
import com.tcl.bean.SendUDPMessage;
import com.tcl.bean.User;
import com.tcl.config.Configuration;
import com.tcl.inter.DispatchMessageInter;
import com.tcl.inter.HandleMessageInter;
import com.tcl.inter.OnMessageSendListener;
import com.tcl.talkServer.NIOUDPSocketServer;
import com.tcl.talkclient.NIOUDPSocketClient;
import com.tcl.utils.IPv4v6Utils;
import com.tcl.utils.LogExt;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TalkApplication extends Application implements HandleMessageInter, OnMessageSendListener {
    private static final String TAG = "fuyao-TalkApplication";

    private static TalkApplication mTalkApplication = null;

    private Map<String, AbstractMessage> mOutUDPMessages = Collections.synchronizedMap(new HashMap<String, AbstractMessage>());

    private Map<Long, AbstractMessage> mReceiveUDPMessages = Collections.synchronizedMap(new HashMap<Long, AbstractMessage>());

    private NIOUDPSocketClient mUDPClient = null;

    private NIOUDPSocketServer mUDPServer = null;

    public static final int MSG_TIME_OUT = 1;

    private Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_TIME_OUT:
                    AbstractMessage udp = (AbstractMessage) msg.obj;
                    if (udp != null) {
                        if (udp.getResendTime() < Configuration.MSG_RESEND_MAX_TIME) {
                            LogExt.d(TAG, "~~~~~~~~~~~~~~~ resend time is " + udp.getResendTime() + " msg is " + udp);
                            udp.addResendTime();
                            notifyClientSend(udp);
                            udp.addResendTime();
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

        String passWord = sPreferences.getString("password", "");
        if (TextUtils.isEmpty(passWord)) {
            return null;
        }

        String ip = IPv4v6Utils.getLocalIPAddress();
        return new User(name, passWord, ip, true);
    }

    private void saveUser(User u) {
        SharedPreferences sPreferences = getSharedPreferences("user", MODE_PRIVATE);
        Editor editor = sPreferences.edit();
        editor.putString("name", u.getUserName());
        editor.putString("password", u.getPassword());
        editor.apply();
    }

    public static TalkApplication getTalkApplication() {
        return mTalkApplication;
    }

    public void receiveMessgage(int readType, AbstractMessage msg) {
        switch (readType) {
            case MessageUtils.RECEIVE_TYPE_UDP:
                receiveUDPMessage(msg);
                break;
            case MessageUtils.RECEIVE_TYPE_TCP:
                break;
        }
    }

    private void receiveUDPMessage(AbstractMessage msg) {
        createUDPServerIfNeed(msg);
    }

    private void createUDPServerIfNeed(AbstractMessage msg) {
        if (null == mUDPServer) {
            mUDPServer = new NIOUDPSocketServer(msg.getPort());
            mUDPServer.setHandleMessageInter(this);
            mUDPServer.start();
            Log.e(TAG, "createUDPServerIfNeed create a UDPServer");
        }
    }

    public void sendMessage(int writeType, AbstractMessage msg) {
        switch (writeType) {
            case MessageUtils.WRITE_TYPE_UDP:
                sendUPDMessage(msg);
                break;

            default:
                break;
        }
    }

    private void sendUPDMessage(AbstractMessage msg) {
        msg.productSendMsg();
        mOutUDPMessages.put(String.valueOf(msg.getCRC8()), msg);
        NIOUDPSocketClient client = createUDPClientIfNeed();
        client.sendMsg(msg);
    }

    private void notifyClientSend(AbstractMessage msg) {
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
        mUser = restoreUser();
    }

    private void startSendMsgTimeout(AbstractMessage msg) {
        AbstractMessage tempMsg = mOutUDPMessages.get(String.valueOf(msg.getCRC8()));
        if (null != tempMsg) {
            LogExt.d(TAG, "******************startSendMsgTimeout " + tempMsg);
            mHandler.sendMessageDelayed(mHandler.obtainMessage(MSG_TIME_OUT, tempMsg), Configuration.MSG_TIMEOUT);
        }
    }

    private void stopSendMsgTimeout(AbstractMessage msg) {
        AbstractMessage tempMsg = mOutUDPMessages.remove(String.valueOf(msg.getReturnCRC8()));
        if (null != tempMsg) {
            LogExt.d(TAG, "~~~~~~~~~~~~~~~~~~stopSendMsgTimeout " + tempMsg);
            mHandler.removeMessages(MSG_TIME_OUT, tempMsg);
        }
    }

    private void sendReturnMsg(AbstractMessage msg) {
        msg.productSendMsg();
        notifyClientSend(msg);
    }

    // if this msg is return msg, we will stop the send msg's time out.
    // if this msg is normal msg, we will return a msg back tell other we
    // received the msg
    // if this msg we already received, func will return 1
    // if is new msg,func will return 0
    private int doPreDispatch(AbstractMessage msg) {
        int msgType = msg.getType();
        // received return MSG,so stop time out do send it again
        if (msgType > MessageUtils.TYPE_RETURN_BASE && msgType < MessageUtils.TYPE_RETURN_END) {
            stopSendMsgTimeout(msg);
            LogExt.d(TAG, "handleMSGRead received return msg: " + msg);
        } else {
            // we already received this MSG, we direct send return MSG
            AbstractMessage sendMsg = new SendUDPMessage();
            sendMsg.setDstIpAdd(msg.getSrcIpAdd());
            sendMsg.setSrcIpAdd(mUser.getIP());
            sendMsg.setSrcName(mUser.getUserName());
            sendMsg.setType(MessageUtils.coverType2ReturnType(msgType));
            sendMsg.setReturnCRC8(msg.getCRC8());
            sendMsg.setTime(System.currentTimeMillis());
            sendMsg.setPort(Configuration.UDP_PORT);
            sendReturnMsg(sendMsg);
        }

        AbstractMessage tempMsg = mReceiveUDPMessages.get(msg.getTime());
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
        AbstractMessage msg = new ReceiveUDPMessage();
        msg.setPort(socketAddress.getPort());
        int result = msg.parseBuffer(buffer);
        LogExt.d(TAG, "handleMSGRead " + msg);
        if (MessageUtils.PARSE_RESULT_DATA_OK == result) {
            int msgType = msg.getType();
            if (doPreDispatch(msg) > 0) {
                int size = mDispatchMessageInters.size();
                if (size > 0) {
                    switch (msgType) {
                        case MessageUtils.TYPE_SAY_HELLO:
                        case MessageUtils.TYPE_RETURN_SAY_HELLO:
                            for (int i = 0; i < size; i++) {
                                mDispatchMessageInters.get(i).onMSGOnline(msg);
                            }
                            break;
                        case MessageUtils.TYPE_TALK_MSG:
                            for (int i = 0; i < size; i++) {
                                mDispatchMessageInters.get(i).onMSGTalk(msg);
                            }
                            break;
                        case MessageUtils.TYPE_RETURN_TALK_MSG:
                            for (int i = 0; i < size; i++) {
                                mDispatchMessageInters.get(i).onMSGSendOK(msg);
                            }
                            break;

                        default:
                            break;
                    }
                }
            }
        }
        return result;
    }

    @Deprecated
    @Override
    public ByteBuffer handleMSGWrite(String destAdd, int port) {
        AbstractMessage msg = null;
        LogExt.d(TAG, "handleUDPMSGWrite destAdd = " + destAdd + " port = " + port);
        LogExt.d(TAG, "handleUDPMSGWrite return null");
        return null;
    }

    @Override
    public void onMsgSendOK(AbstractMessage msg) {
        if (MessageUtils.TYPE_BASE < msg.getType() && msg.getType() < MessageUtils.TYPE_BASE_END) {
            startSendMsgTimeout(msg);
        }
    }

    @Override
    public void onMsgSendError(AbstractMessage msg) {
        // TODO Auto-generated method stub

    }

}
