package com.tcl.wirelessdebug;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.tcl.bean.AbstractMessage;
import com.tcl.bean.MessageUtils;
import com.tcl.bean.ReceiveUDPMessage;
import com.tcl.bean.SendUDPMessage;
import com.tcl.bean.User;
import com.tcl.config.Configuration;
import com.tcl.inter.DispatchMessageInter;
import com.tcl.utils.LogExt;

public class UsersActivity extends Activity implements DispatchMessageInter, OnClickListener, OnItemClickListener {
    private static final String TAG = "fuyao-UsersActivity";

    private ListView mUsersListView = null;
    private Button mRefreshButton = null;
    private Button mModifyButton = null;
    private TextView mSelfTextView = null;

    List<User> mTalkUsers = Collections.synchronizedList(new ArrayList<User>());
    UserAdapter mUserAdapter = null;
    TalkApplication mTalkApplication = null;
    AbstractMessage mSendMsg = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.users_layout);
        mRefreshButton = (Button) findViewById(R.id.refresh);
        mRefreshButton.setOnClickListener(this);

        mModifyButton = (Button) findViewById(R.id.modify);
        mModifyButton.setOnClickListener(this);

        mSelfTextView = (TextView) findViewById(R.id.self);

        mTalkApplication = TalkApplication.getTalkApplication();
        mTalkApplication.addDispatchMessageInter(this);
        final User user = mTalkApplication.getUser();
        String selfText = "I'm " + user.getUserName() + "\n" + user.getIP() + "\n";
        mSelfTextView.setText(selfText);

        mUsersListView = (ListView) findViewById(R.id.users);
        mUserAdapter = new UserAdapter();
        mUsersListView.setAdapter(mUserAdapter);
        mUsersListView.setOnItemClickListener(this);

        AbstractMessage receiveMsg = new ReceiveUDPMessage();
        receiveMsg.setPort(Configuration.UDP_PORT);
        receiveMsg.setSrcIpAdd(user.getIP());
        receiveMsg.setSrcName(user.getUserName());
        mTalkApplication.receiveMessgage(MessageUtils.RECEIVE_TYPE_UDP, receiveMsg);

        mSendMsg = new SendUDPMessage();
        mSendMsg.setType(MessageUtils.TYPE_SAY_HELLO);
        mSendMsg.setDstIpAdd(Configuration.UDP_BROADCAST_ADDRESS);
        mSendMsg.setPort(Configuration.UDP_PORT);
        mSendMsg.setSrcIpAdd(user.getIP());
        mSendMsg.setSrcName(user.getUserName());
        mTalkApplication.sendMessage(MessageUtils.WRITE_TYPE_UDP, mSendMsg);

    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        mTalkApplication.removeDispatchMessageInter(this);
        super.onDestroy();
    }

    class UserAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return mTalkUsers.size();
        }

        @Override
        public Object getItem(int position) {
            return position;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            TextView textView = null;
            if (null == convertView) {
                textView = new TextView(UsersActivity.this);
            } else {
                textView = (TextView) convertView;
            }
            User user = mTalkUsers.get(position);
            int num = user.getMsgNum();
            String numString = num > 10 ? "99+" : String.valueOf(num);
            String msgString = user.getMsgNum() > 0 ? (numString + " New Message") : "";
            String diString = user.getUserName() + "\n" + User.coverStatus2String(user.getStatus()) + "\n" + user.getIP() + "      " + msgString;
            textView.setText(diString);
            return textView;
        }
    }

    @Override
    public void onMSGLogin(AbstractMessage msg) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onMSGRegister(AbstractMessage msg) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onMSGOnline(AbstractMessage msg) {
        User tUser = new User(msg.getSrcName(), msg.getSrcIpAdd(), User.STATUS_ONLINE);
        LogExt.d(TAG, "onMSGOnline msg " + msg);

        if (!msg.getSrcIpAdd().equals(mTalkApplication.getUser().getIP())) {
            boolean has = false;
            synchronized (mTalkUsers) {
                for (User tempUser : mTalkUsers) {
                    LogExt.d(TAG, "onMSGOnline tempUser ip:" + tempUser.getIP());
                    if (tempUser.getIP().equals(msg.getSrcIpAdd())) {
                        tempUser.setUserName(msg.getSrcName());
                        has = true;
                        break;
                    }
                }
            }
            if (!has) {
                mTalkUsers.add(tUser);
            }
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mUserAdapter.notifyDataSetChanged();
                }
            });
        }

    }

    @Override
    public void onMSGOffline(AbstractMessage msg) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onMSGTalk(AbstractMessage msg) {
        if (!msg.getSrcIpAdd().equals(mTalkApplication.getUser().getIP())) {
            synchronized (mTalkUsers) {
                for (User tempUser : mTalkUsers) {
                    LogExt.d(TAG, "onMSGOnline tempUser ip:" + tempUser.getIP());
                    if (tempUser.getIP().equals(msg.getSrcIpAdd())) {
                        tempUser.setMsgNum(tempUser.getMsgNum() + 1);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mUserAdapter.notifyDataSetChanged();
                            }
                        });
                        break;
                    }
                }
            }

        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.refresh:
                mTalkApplication.sendMessage(MessageUtils.WRITE_TYPE_UDP, mSendMsg);
                break;
            case R.id.modify:

                break;
            default:
                break;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        User talkUser = mTalkUsers.get(position);
        Intent intent = new Intent();
        talkUser.setMsgNum(0);
        mUserAdapter.notifyDataSetChanged();
        intent.putExtra("user", talkUser);
        intent.setClass(this, TalkActivity.class);
        startActivity(intent);
    }

    @Override
    public void onMSGSendOK(AbstractMessage msg) {
        // TODO Auto-generated method stub
        
    }
}
