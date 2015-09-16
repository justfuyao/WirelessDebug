package com.tcl.wirelessdebug;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.app.Activity;
import android.content.ComponentName;
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
import com.tcl.database.Msg;
import com.tcl.database.User;
import com.tcl.inter.DispatchMessageInter;
import com.tcl.utils.TimeUtil;

public class TalkActivity extends Activity implements DispatchMessageInter, OnClickListener, ServiceConnection {

    TalkApplication mTalkApplication = null;
    private TextView mIpAddTextView = null;
    private ListView mTalkListView = null;
    private TalkMsgAdapter mTalkMsgAdapter = null;

    private Button mSendButton = null;
    private EditText mTalkEditText = null;
    private User mOwn = null;
    private User mDestUser = null;
    private List<Msg> mTalkMsgs = Collections.synchronizedList(new ArrayList<Msg>());

    ExchangeMsgService mExchangeMsgService = null;

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

        mOwn = mTalkApplication.getUser();
        mDestUser = getIntent().getParcelableExtra("user");
        mIpAddTextView.setText("Talk with: " + mDestUser.get_Name() + "\n" + "IP:" + mDestUser.get_IpAddress() + "\nPort:" + Configuration.UDP_PORT);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
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
            return mTalkMsgs.size();
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
            // textView.setText(mTalkMsgs.get(position).getDisplayString());
            // if (!mTalkMsgs.get(position).isOwn) {
            // if (Build.VERSION.SDK_INT >= 14) {
            // textView.setGravity(Gravity.END);
            // } else {
            // textView.setGravity(Gravity.RIGHT);
            // }
            // } else {
            // if (Build.VERSION.SDK_INT >= 14) {
            // textView.setGravity(Gravity.START);
            // } else {
            // textView.setGravity(Gravity.LEFT);
            // }
            // }
            return textView;
        }
    }

    // @Override
    // public void onMSGTalk(Msg msg) {
    // onNewMsg(msg, false, Msg.SEND_STATUS_DEFAULT);
    // }

    @Override
    public void onNewMsg(Msg msg) {
        // TODO Auto-generated method stub

    }

    // @Override
    // public void onMSGSendOK(Msg msg) {
    // synchronized (mTalkMsgs) {
    // for (Msg talkmsg : mTalkMsgs) {
    // if (msg.getReturnCRC8() == talkmsg.msg.getCRC8()) {
    // talkmsg.setSendStatus(Msg.SEND_STATUS_OK);
    // runOnUiThread(new Runnable() {
    // @Override
    // public void run() {
    // mTalkMsgAdapter.notifyDataSetChanged();
    // }
    // });
    // break;
    // }
    // }
    // }
    // }
    //
    // private void addNewMsg(Msg msg, boolean isOwn, int status) {
    // mTalkMsgs.add(new Msg(msg, isOwn, status));
    // runOnUiThread(new Runnable() {
    // @Override
    // public void run() {
    // mTalkMsgAdapter.notifyDataSetChanged();
    // mTalkListView.setSelection(mTalkMsgAdapter.getCount() - 1);
    // }
    // });
    // }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.send:
                if (null != mExchangeMsgService) {
                    Msg sendMsg = new Msg();
                    sendMsg.set_SendType(MessageUtils.TYPE_TALK_MSG);
                    sendMsg.setDstAddress(mDestUser.get_IpAddress());
                    sendMsg.setSrcUser(TalkApplication.getTalkApplication().getUser());
                    sendMsg.setPort(Configuration.UDP_PORT);
                    sendMsg.set_Timestamps(System.currentTimeMillis());
                    String content = mTalkEditText.getText().toString();
                    mTalkEditText.setText("");
                    // sendMsg.setContent(content == null ? "".getBytes() :
                    // content.getBytes());
                    mExchangeMsgService.sendMessage(MessageUtils.WRITE_TYPE_UDP, sendMsg);
                    // onNewMsg(sendMsg, true, );
                } else {
                    Toast.makeText(this, "service is not connect!", Toast.LENGTH_SHORT).show();
                }
                break;

            default:
                break;
        }
    }

    // class Msg {
    //
    // Msg msg;
    // boolean isOwn;
    //
    // static final int SEND_STATUS_DEFAULT = -1;
    // static final int SEND_STATUS_OK = 1;
    // static final int SEND_STATUS_SENDING = 2;
    //
    // // -1 no need status, 1 send ok, 2 sending
    // int sendStatus = SEND_STATUS_DEFAULT;
    //
    // public void setSendStatus(int s) {
    // sendStatus = s;
    // }
    //
    // public Msg(Msg m, boolean i, int status) {
    // msg = m;
    // isOwn = i;
    // sendStatus = status;
    // }
    //
    // public String getDisplayString() {
    // String content = msg. == null ? "null" : new String(msg.getContent());
    // String ret = TimeUtil.format2TimeString(msg.getTime()) + "     " +
    // coverStatus2String(sendStatus) + "\n" + mOwn.get_IpAddress() + "\n" +
    // content;
    // return ret;
    // }
    //
    // public String coverStatus2String(int status) {
    // String ret = "";
    // switch (status) {
    // case SEND_STATUS_DEFAULT:
    //
    // break;
    // case SEND_STATUS_SENDING:
    // ret = "Sending...";
    // break;
    // case SEND_STATUS_OK:
    // ret = "Send OK";
    // break;
    // default:
    // break;
    // }
    // return ret;
    // }
    // }

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
