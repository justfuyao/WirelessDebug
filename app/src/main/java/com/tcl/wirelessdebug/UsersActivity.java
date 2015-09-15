package com.tcl.wirelessdebug;

import android.app.Activity;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.AsyncTaskLoader;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
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

import com.tcl.bean.MessageUtils;
import com.tcl.config.Configuration;
import com.tcl.database.DatabaseManager;
import com.tcl.database.Msg;
import com.tcl.database.User;
import com.tcl.inter.DispatchMessageInter;

import de.greenrobot.dao.async.AsyncOperation;
import de.greenrobot.dao.query.LazyList;

public class UsersActivity extends Activity implements DispatchMessageInter, OnClickListener, OnItemClickListener, LoaderCallbacks<Object> {
    private static final String TAG = "fuyao-UsersActivity";

    private ListView mUsersListView = null;
    private Button mRefreshButton = null;
    private Button mModifyButton = null;
    private TextView mSelfTextView = null;

    LazyList<User> mTalkUsers = null;
    UserAdapter mUserAdapter = null;
    TalkApplication mTalkApplication = null;
    Msg mSendMsg = null;
    DatabaseManager mDatabaseProxy = null;

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
        String selfText = "I'm " + user.get_Name() + "\n" + user.get_IpAddress() + "\n";
        mSelfTextView.setText(selfText);

        mUsersListView = (ListView) findViewById(R.id.users);
        mUserAdapter = new UserAdapter();
        mUsersListView.setAdapter(mUserAdapter);
        mUsersListView.setOnItemClickListener(this);

        mTalkApplication.listenMessage(MessageUtils.RECEIVE_TYPE_UDP, Configuration.UDP_PORT);

        mSendMsg = new Msg();
        mSendMsg.set_Type(MessageUtils.TYPE_SAY_HELLO);
        mSendMsg.setDstIpAdd(Configuration.UDP_BROADCAST_ADDRESS);
        mSendMsg.setPort(Configuration.UDP_PORT);
        mSendMsg.setSrcUser(user);
        mTalkApplication.sendMessage(MessageUtils.WRITE_TYPE_UDP, mSendMsg);

        mDatabaseProxy = TalkApplication.getDatabaseProxy();

        getLoaderManager().initLoader(MyAsynLoader.ID_QUERY_ALL_USERS, null, this);
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
            int num = user.get_UnreadMsgNum();
            String numString = num > 100 ? "99+" : String.valueOf(num);
            String msgString = num > 0 ? (numString + " New Message") : "";
            String diString = user.get_Name() + "\n" + User.coverStatus2String(user.get_Status()) + "\n" + user.get_IpAddress() + "      " + msgString;
            textView.setText(diString);
            return textView;
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
        mUserAdapter.notifyDataSetChanged();
        intent.putExtra("user", talkUser).setClass(this, TalkActivity.class);
        startActivity(intent);
    }

    @Override
    public void onNewMsg(User user, Msg msg) {
        switch (msg.get_Type()) {
            case MessageUtils.TYPE_SAY_HELLO:
            case MessageUtils.TYPE_RETURN_SAY_HELLO:
                getLoaderManager().restartLoader(MyAsynLoader.ID_QUERY_ALL_USERS, null, this);
                break;
            case MessageUtils.TYPE_TALK_MSG:
                break;
            case MessageUtils.TYPE_RETURN_TALK_MSG:
                break;
            default:
                break;
        }
    }

    @Override
    public Loader<Object> onCreateLoader(int id, Bundle args) {
        return new MyAsynLoader<>(getApplicationContext(), id, args, mDatabaseProxy);
    }

    @Override
    public void onLoadFinished(Loader<Object> loader, Object data) {
        int id = loader.getId();
        switch (id) {
            case MyAsynLoader.ID_QUERY_ALL_USERS:
                if (null != mTalkUsers) {
                    mTalkUsers.close();
                }
                mTalkUsers = (LazyList<User>) data;
                mUserAdapter.notifyDataSetChanged();
                break;

            default:
                break;
        }
    }

    @Override
    public void onLoaderReset(Loader<Object> loader) {

    }

    private static class MyAsynLoader<Object> extends AsyncTaskLoader<Object> {

        public static final int ID_QUERY_ALL_USERS = 1;

        private int id = -1;
        private Bundle args = null;
        private DatabaseManager dManager = null;

        public int getId() {
            return id;
        }

        public Bundle getArgs() {
            return args;
        }

        public MyAsynLoader(Context context, int id, Bundle args, DatabaseManager manager) {
            super(context);
            this.id = id;
            this.args = args;
            this.dManager = manager;
        }

        @Override
        public Object loadInBackground() {
            Object ret = null;
            switch (id) {
                case ID_QUERY_ALL_USERS:
                    AsyncOperation operation = dManager.asynQueryAllUsers();
                    ret = (Object) operation.waitForCompletion();
                    break;

                default:
                    break;
            }
            return ret;
        }

        @Override
        public void onCanceled(Object data) {
        }

        @Override
        public void cancelLoadInBackground() {

        }
    }

}
