package com.tcl.wirelessdebug;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.app.Activity;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.AsyncTaskLoader;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.tcl.bean.MessageUtils;
import com.tcl.config.Configuration;
import com.tcl.database.DatabaseManager;
import com.tcl.database.Msg;
import com.tcl.database.User;
import com.tcl.inter.DispatchMessageInter;
import com.tcl.utils.LogExt;

public class TalkActivity extends Activity implements DispatchMessageInter, OnClickListener, ServiceConnection, LoaderCallbacks<Object> {
    private static final String TAG = "fuyao-TalkActivity";

    TalkApplication mTalkApplication = null;
    private TextView mIpAddTextView = null;
    private ListView mTalkListView = null;
    private TalkMsgAdapter mTalkMsgAdapter = null;

    private Button mSendButton = null;
    private EditText mTalkEditText = null;
    private User mOwnUser = null;
    private User mDestUser = null;
    private List<Msg> mTalkMsgs = Collections.synchronizedList(new ArrayList<Msg>());

    ExchangeMsgService mExchangeMsgService = null;
    DatabaseManager mDatabaseManager = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.talk_layout);
        mTalkApplication = TalkApplication.getTalkApplication();
        mIpAddTextView = (TextView) findViewById(R.id.ip_add);
        mTalkListView = (ListView) findViewById(R.id.talk_info);
        mTalkMsgAdapter = new TalkMsgAdapter();
        mTalkListView.setAdapter(mTalkMsgAdapter);

        mSendButton = (Button) findViewById(R.id.send);
        mSendButton.setOnClickListener(this);
        mTalkEditText = (EditText) findViewById(R.id.send_text);

        mOwnUser = mTalkApplication.getUser();
        mDestUser = getIntent().getParcelableExtra("user");
        mIpAddTextView.setText("Talk with: " + mDestUser.get_Name() + "\n" + "IP:" + mDestUser.get_IpAddress() + "\nPort:" + Configuration.UDP_PORT);
        mDatabaseManager = TalkApplication.getDatabaseProxy();

        getLoaderManager().restartLoader(MyAsynLoader.ID_QUERY_ALL_MSGS, null, this);
        bindService(new Intent(getApplicationContext(), ExchangeMsgService.class), this, BIND_AUTO_CREATE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(this);
        if (null != mExchangeMsgService) {
            mExchangeMsgService.removeDispatchMessageInter(this);
            mExchangeMsgService = null;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    class TalkMsgAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return mTalkMsgs == null ? 0 : mTalkMsgs.size();
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
            TextView textView;
            if (null == convertView) {
                textView = new TextView(TalkActivity.this);
            } else {
                textView = (TextView) convertView;
            }
            textView.setText(mTalkMsgs.get(position).getDisplayString());
            if (mTalkMsgs.get(position).get_IsReceive() == Msg.MSG_SEND_TYPE_RECEIVE) {
                if (Build.VERSION.SDK_INT >= 14) {
                    textView.setGravity(Gravity.END);
                } else {
                    textView.setGravity(Gravity.RIGHT);
                }
            } else {
                if (Build.VERSION.SDK_INT >= 14) {
                    textView.setGravity(Gravity.START);
                } else {
                    textView.setGravity(Gravity.LEFT);
                }
            }
            return textView;
        }
    }

    // @Override
    // public void onMSGTalk(Msg msg) {
    // onNewMsg(msg, false, Msg.SEND_STATUS_DEFAULT);
    // }

    @Override
    public void onNewMsg(Msg msg) {
        if (null != msg) {
            if (MessageUtils.TYPE_TALK_MSG == msg.get_SendType()) {
                if (msg.get_UserUID().equals(mDestUser.get_UID())) {
                    addNewMsg(msg);
                }
            } else if (MessageUtils.TYPE_RETURN_TALK_MSG == msg.get_SendType()) {
                String msgUID = msg.get_MsgUID();
                if (null != msgUID) {
                    for (Msg tempMsg : mTalkMsgs) {
                        if (tempMsg.get_SendType() == MessageUtils.TYPE_TALK_MSG && msgUID.equals(tempMsg.get_MsgUID())) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mTalkMsgAdapter.notifyDataSetChanged();
                                }
                            });
                            break;
                        }
                    }
                }
            }
        }
    }

    private void addNewMsg(Msg msg) {
        mTalkMsgs.add(msg);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mTalkMsgAdapter.notifyDataSetChanged();
                mTalkListView.setSelection(mTalkMsgAdapter.getCount() - 1);
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.send:
                if (null != mExchangeMsgService) {
                    Msg sendMsg = new Msg();
                    sendMsg.set_SendType(MessageUtils.TYPE_TALK_MSG);
                    sendMsg.setDstUser(mDestUser);
                    sendMsg.setSrcUser(mOwnUser);
                    sendMsg.setPort(Configuration.UDP_PORT);
                    sendMsg.set_Timestamps(System.currentTimeMillis());
                    String content = mTalkEditText.getText().toString();
                    mTalkEditText.setText("");
                    sendMsg.setExtraMsg(content);
                    sendMsg.set_IsReceive(Msg.MSG_SEND_TYPE_SEND);
                    mExchangeMsgService.sendMessage(MessageUtils.WRITE_TYPE_UDP, sendMsg);
                    addNewMsg(sendMsg);
                } else {
                    Toast.makeText(this, "service is not connect!", Toast.LENGTH_SHORT).show();
                }
                break;

            default:
                break;
        }
    }

    @Override
    public Loader<Object> onCreateLoader(int id, Bundle args) {
        LogExt.d(TAG, "onCreateLoader id " + id);
        return new MyAsynLoader<Object>(getApplicationContext(), id, args, mOwnUser, mDestUser, mDatabaseManager);
    }

    @Override
    public void onLoadFinished(Loader<Object> loader, Object data) {
        int id = loader.getId();
        switch (id) {
            case MyAsynLoader.ID_QUERY_ALL_MSGS:
                mTalkMsgs = (List<Msg>) data;
                LogExt.d(TAG, "onLoadFinished mTalkMsgs is " + (mTalkMsgs == null ? "null" : mTalkMsgs.size()));
                mTalkMsgAdapter.notifyDataSetChanged();
                mTalkListView.setSelection(mTalkMsgAdapter.getCount() - 1);
                break;

            default:
                break;
        }
    }

    @Override
    public void onLoaderReset(Loader<Object> loader) {
    }

    private static class MyAsynLoader<Object> extends AsyncTaskLoader<Object> {

        public static final int ID_QUERY_ALL_MSGS = 2;

        private int id = -1;
        private Bundle args = null;
        private DatabaseManager dManager = null;
        private User srcUser = null;
        private User dstUser = null;

        public int getId() {
            return id;
        }

        public Bundle getArgs() {
            return args;
        }

        public MyAsynLoader(Context context, int id, Bundle args, User srcUser, User dstUser, DatabaseManager manager) {
            super(context);
            this.id = id;
            this.args = args;
            this.dManager = manager;
            this.srcUser = srcUser;
            this.dstUser = dstUser;
        }

        @Override
        public Object loadInBackground() {
            Object ret = null;
            LogExt.d(TAG, "MyAsynLoader loadInBackground id  = " + id);
            switch (id) {
                case ID_QUERY_ALL_MSGS:
                    ret = (Object) dManager.queryUserAllTalkMsg(srcUser, dstUser);
                    break;

                default:
                    break;
            }
            return ret;
        }

        @Override
        protected void onStartLoading() {
            forceLoad();
        }
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        mExchangeMsgService = ((ExchangeMsgService.ExchangeBinder) service).getService();
        mExchangeMsgService.addDispatchMessageInter(this);
        mExchangeMsgService.listenMessage(MessageUtils.RECEIVE_TYPE_UDP, Configuration.UDP_PORT);
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        if (null != mExchangeMsgService) {
            mExchangeMsgService.removeDispatchMessageInter(this);
            mExchangeMsgService = null;
        }
    }

}
