package com.tcl.wirelessdebug;

import android.app.Activity;
import android.os.Bundle;
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

import com.tcl.bean.AbstractMessage;
import com.tcl.bean.MessageUtils;
import com.tcl.bean.SendUDPMessage;
import com.tcl.bean.User;
import com.tcl.config.Configuration;
import com.tcl.inter.DispatchMessageInter;
import com.tcl.utils.TimeUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TalkActivity extends Activity implements DispatchMessageInter, OnClickListener {

    TalkApplication mTalkApplication = null;
    private TextView mIpAddTextView = null;
    private ListView mTalkListView = null;
    private TalkMsgAdapter mTalkMsgAdapter = null;

    private Button mSendButton = null;
    private EditText mTalkEditText = null;
    User mOwn = null;
    User mDestUser = null;
    List<Msg> mTalkMsg = Collections.synchronizedList(new ArrayList<Msg>());

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
        mIpAddTextView.setText("IP:" + mDestUser.getIP() + "\nPort:" + Configuration.UDP_PORT);

        mTalkApplication.addDispatchMessageInter(this);
    }

    @Override
    protected void onDestroy() {
        mTalkApplication.removeDispatchMessageInter(this);
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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
            return mTalkMsg.size();
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
            textView.setText(mTalkMsg.get(position).msg);
            if (!mTalkMsg.get(position).isOwn) {
                textView.setGravity(Gravity.END);
            } else {
                textView.setGravity(Gravity.START);
            }
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
        // TODO Auto-generated method stub

    }

    @Override
    public void onMSGOffline(AbstractMessage msg) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onMSGTalk(AbstractMessage msg) {
        onNewMsg(
                TimeUtil.format2TimeString(System.currentTimeMillis()) + "\n" + msg.getSrcIpAdd() + "\n" + (msg.getContent() == null ? "" : new String(
                        msg.getContent())), false);
    }

    private void onNewMsg(String msg, boolean isOwn) {
        mTalkMsg.add(new Msg(msg, isOwn));
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
                AbstractMessage sendMsg = new SendUDPMessage();
                sendMsg.setType(MessageUtils.TYPE_TALK_MSG);
                sendMsg.setDstIpAdd(mDestUser.getIP());
                sendMsg.setPort(Configuration.UDP_PORT);
                sendMsg.setSrcIpAdd(mOwn.getIP());
                sendMsg.setSrcName(mOwn.getUserName());
                String content = mTalkEditText.getText().toString();
                mTalkEditText.setText("");
                sendMsg.setContent(content == null ? "".getBytes() : content.getBytes());
                mTalkApplication.sendMessage(MessageUtils.WRITE_TYPE_UDP, sendMsg);
                onNewMsg(TimeUtil.format2TimeString(System.currentTimeMillis()) + "\n" + mOwn.getIP() + "\n" + new String(sendMsg.getContent()), true);
                break;

            default:
                break;
        }
    }

    class Msg {
        String msg;
        boolean isOwn;

        public Msg(String m, boolean i) {
            msg = m;
            isOwn = i;
        }
    }
}
