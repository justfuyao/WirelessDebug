package com.tcl.wirelessdebug;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.text.TextUtils;

import com.tcl.config.Configuration;
import com.tcl.database.Constants;
import com.tcl.database.DaoMaster;
import com.tcl.database.DaoMaster.OpenHelper;
import com.tcl.database.DaoSession;
import com.tcl.database.DatabaseManager;
import com.tcl.database.User;
import com.tcl.utils.IPv4v6Utils;

public class TalkApplication extends Application {
    private static final String TAG = "fuyao-TalkApplication";

    private static TalkApplication mTalkApplication = null;

    private static DatabaseManager mDatabaseManager = null;

    private static DaoMaster mDaoMaster;

    private static DaoSession mDaoSession;

    private String mDeviceId = null;

    private User mUser = null;

    private User mBroadcastUser = null;

    public User getBroadCastUser() {
        if (null == mBroadcastUser) {
            mBroadcastUser = new User();
            mBroadcastUser.set_IpAddress(Configuration.UDP_BROADCAST_ADDRESS);
            mBroadcastUser.set_UID(Configuration.UDP_BROADCAST_UID);
            mBroadcastUser.set_Name(Configuration.UDP_BROADCAST_NAME);
        }
        return mBroadcastUser;
    }

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
        User user = new User(name, ip, uid);
        user.setUserStatus(User.USER_STATUS_ONLINE);
        return user;
    }

    private void saveUser(User u) {
        SharedPreferences sPreferences = getSharedPreferences("user", MODE_PRIVATE);
        Editor editor = sPreferences.edit();
        editor.putString("name", u.get_Name());
        editor.putString("uid", u.get_UID());
        editor.apply();
    }

    public String getDeviceId() {
        return mDeviceId;
    }

    public static TalkApplication getTalkApplication() {
        return mTalkApplication;
    }

    public static DatabaseManager getDatabaseProxy() {
        return mDatabaseManager;
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

    @Override
    public void onCreate() {
        super.onCreate();
        if (null == mTalkApplication) {
            mTalkApplication = this;
        }
        if (null == mDatabaseManager) {
            mDatabaseManager = DatabaseManager.getInstance(this);
        }
        mUser = restoreUser();

        mDeviceId = IPv4v6Utils.getDeviceId(this);
    }

}
