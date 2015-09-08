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

        mTalkApplication = TalkApplication.getTalkApplication();
        mTalkApplication.addDispatchMessageInter(this);
        final User user = mTalkApplication.getUser();
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
            textView.setText(mTalkUsers.get(position).getUserName() + "\n" + User.coverStatus2String(mTalkUsers.get(position).getStatus()) + "\n"
                    + mTalkUsers.get(position).getIP());
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
            for (User tempUser : mTalkUsers) {
                LogExt.d(TAG, "onMSGOnline tempUser ip:" + tempUser.getIP());
                if (tempUser.getIP().equals(msg.getSrcIpAdd())) {
                    if (tempUser.getIsOwn()) {
                        return;
                    }
                    mTalkUsers.remove(tempUser);
                    mTalkUsers.add(tUser);
                    has = true;
                    break;
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
        // TODO Auto-generated method stub

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.refresh:
                mTalkApplication.sendMessage(MessageUtils.WRITE_TYPE_UDP, mSendMsg);
                break;

            default:
                break;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        User talkUser = mTalkUsers.get(position);
        Intent intent = new Intent();
        intent.putExtra("user", talkUser);
        intent.setClass(this, TalkActivity.class);
        startActivity(intent);
    }
}
