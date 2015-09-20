package com.tcl.database;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.util.Log;

import com.tcl.bean.MessageUtils;
import com.tcl.database.UserDao.Properties;
import com.tcl.utils.LogExt;
import com.tcl.wirelessdebug.TalkApplication;

import de.greenrobot.dao.async.AsyncOperation;
import de.greenrobot.dao.async.AsyncOperationListener;
import de.greenrobot.dao.async.AsyncSession;
import de.greenrobot.dao.query.LazyList;
import de.greenrobot.dao.query.Query;
import de.greenrobot.dao.query.QueryBuilder;

public class DatabaseManager {
    private static final String TAG = "fuyao-DatabaseManager";

    private static DatabaseManager mDatabaseProxy = null;

    private MsgDao mMsgDao;
    private UserDao mUserDao;
    private Context mContext;
    private DaoSession mDaoSession;
    private DaoMaster mDaoMaster;

    private static final Object lockObject = new Object();

    private DatabaseManager(Context c) {
        mContext = c.getApplicationContext();
        mDaoMaster = TalkApplication.getDaoMaster(mContext);
        mDaoSession = TalkApplication.getDaoSession(mContext);
        mMsgDao = mDaoSession.getMsgDao();
        mUserDao = mDaoSession.getUserDao();
        // mAsyncSession.setListener(mProxyListenerThread);
        // mAsyncSession.setListenerMainThread(mProxyListenerMainThread);
    }

    public void startAsyncSession() {
        // if (null == mAsyncSession) {
        // LogExt.d(TAG,"startAsyncSession init");
        // mAsyncSession = mDaoSession.startAsyncSession();
        // mAsyncSession.setListener(mProxyListenerThread);
        // mAsyncSession.setListenerMainThread(mProxyListenerMainThread);
        // }
    }

    public static DatabaseManager getInstance(Context c) {
        if (null == mDatabaseProxy) {
            synchronized (lockObject) {
                if (null == mDatabaseProxy) {
                    DatabaseManager tempManager = new DatabaseManager(c);
                    mDatabaseProxy = tempManager;
                }
            }
        }
        return mDatabaseProxy;
    }

    private AsyncOperationListener mProxyListenerMainThread = new AsyncOperationListener() {
        @Override
        public void onAsyncOperationCompleted(AsyncOperation operation) {
            for (AsyncOperationListener l : mListenerMainThreads) {
                l.onAsyncOperationCompleted(operation);
            }
        }
    };

    private AsyncOperationListener mProxyListenerThread = new AsyncOperationListener() {
        @Override
        public void onAsyncOperationCompleted(AsyncOperation operation) {
            for (AsyncOperationListener l : mListenerThreads) {
                l.onAsyncOperationCompleted(operation);
            }
        }
    };

    private List<AsyncOperationListener> mListenerMainThreads = new ArrayList<>();

    public void addListenerMainThread(AsyncOperationListener listenerMainThread) {
        if (!mListenerMainThreads.contains(listenerMainThread)) {
            mListenerMainThreads.add(listenerMainThread);
        }
    }

    public void removeListenerMainThread(AsyncOperationListener listenerMainThread) {
        if (mListenerMainThreads.contains(listenerMainThread)) {
            mListenerMainThreads.remove(listenerMainThread);
        }
    }

    List<AsyncOperationListener> mListenerThreads = new ArrayList<>();

    public void addListenerThread(AsyncOperationListener listenerThread) {
        if (!mListenerThreads.contains(listenerThread)) {
            mListenerThreads.add(listenerThread);
        }
    }

    public void removeListenerThread(AsyncOperationListener listenerThread) {
        if (mListenerThreads.contains(listenerThread)) {
            mListenerThreads.remove(listenerThread);
        }
    }

    public List<Msg> queryMsg(String uid) {
        Query<Msg> query = mMsgDao.queryBuilder().where(MsgDao.Properties._MsgUID.eq(uid)).build().forCurrentThread();
        List<Msg> msgs = query.list();
        return msgs;
    }

    public List<Msg> asynQueryMsg(String uid) {
        List<Msg> msgs = null;
        AsyncSession mAsyncSession = mDaoSession.startAsyncSession();
        Query<Msg> query = mMsgDao.queryBuilder().where(MsgDao.Properties._MsgUID.eq(uid)).build();
        AsyncOperation operation = mAsyncSession.queryList(query);
        operation.waitForCompletion();
        if (!operation.isFailed()) {
            msgs = (List<Msg>) operation.getResult();
        } else {
            LogExt.e(TAG, "asynQueryMsg error:" + operation.getThrowable());
        }
        return msgs;
    }

    public long asynInsertMsg(Msg msg) {
        AsyncSession mAsyncSession = mDaoSession.startAsyncSession();
        AsyncOperation operation = mAsyncSession.insert(msg);
        operation.waitForCompletion();
        long ret = -1l;
        if (!operation.isFailed()) {
            if (null != operation.getResult()) {
                ret = Long.valueOf(String.valueOf(operation.getResult()));
                LogExt.d(TAG, "asynInsertMsg ok id is " + ret + " msg : " + msg);
            }
        } else {
            LogExt.e(TAG, "asynInsertMsg error:" + operation.getThrowable() + " msg : " + msg);
        }
        return ret;
    }

    public void insertOrUpdateUser(User user) {
        Query<User> query = mUserDao.queryBuilder().where(Properties._UID.eq(user.get_UID())).build().forCurrentThread();
        User tempUser = query.unique();
        if (null != tempUser) {
            if (tempUser.compare(user) < 0) {
                mUserDao.update(user);
            }
        } else {
            mUserDao.insert(user);
        }
    }

    public long asynInsertOrUpdateUser(User user) {
        LogExt.d(TAG, "asynInsertOrUpdateUser user " + user);
        long id = 0;
        AsyncSession mAsyncSession = mDaoSession.startAsyncSession();
        Query<User> query = mUserDao.queryBuilder().where(Properties._UID.eq(user.get_UID())).build();
        AsyncOperation operation = mAsyncSession.queryUnique(query);
        operation.waitForCompletion();
        if (!operation.isFailed()) {
            User tempUser = (User) operation.getResult();
            tempUser.setUserStatus(User.USER_STATUS_ONLINE);
            operation = mAsyncSession.refresh(tempUser);
            operation.waitForCompletion();
        } else {
            LogExt.e(TAG, "asynInsertOrUpdateUserAndNotify 1 error:" + operation.getThrowable());
            operation = mAsyncSession.insertOrReplace(user);
            operation.waitForCompletion();
        }
        if (!operation.isFailed()) {
            if (null != operation.getResult()) {
                id = (long) operation.getResult();
            }
            LogExt.d(TAG, "asynInsertOrUpdateUserAndNotify 2 id " + id);
        } else {
            LogExt.e(TAG, "asynInsertOrUpdateUserAndNotify 2 error:" + operation.getThrowable());
        }
        return id;
    }

    public List<User> queryAllUsers() {
        Query<User> query = mUserDao.queryBuilder().build().forCurrentThread();
        return query.list();
    }

    public List<User> asynQueryAllUsers() {
        List<User> lists = null;
        AsyncSession mAsyncSession = mDaoSession.startAsyncSession();
        Query<User> query = mUserDao.queryBuilder().build();
        AsyncOperation operation = mAsyncSession.queryList(query);
        operation.waitForCompletion();
        if (!operation.isFailed()) {
            lists = (List<User>) operation.getResult();
        } else {
            LogExt.e(TAG, "asynQueryAllUsers error:" + operation.getThrowable());
        }
        return lists;
    }

    public List<Msg> queryUserAllTalkMsg(User srcUser, User dstUser) {
        Log.d(TAG, "queryUserAllTalkMsg srcUser is " + srcUser + " dstUser is " + dstUser);
        Query<Msg> query = mMsgDao.queryBuilder()
                .where(MsgDao.Properties._UserUID.eq(dstUser.get_UID()), MsgDao.Properties._SendType.eq(MessageUtils.TYPE_TALK_MSG)).build().forCurrentThread();
        List<Msg> l = query.list();
        for (Msg msg : l) {
            Log.d(TAG, "queryUserAllMsg msg is " + msg);
            if (msg.get_IsReceive() == Msg.MSG_SEND_TYPE_RECEIVE) {
                msg.setSrcUser(dstUser);
            } else {
                msg.setSrcUser(srcUser);
            }
        }
        return l;
    }

    public List<Msg> queryUserAllMsg(User user) {
        Log.d(TAG, "queryUserAllMsg user is " + user);
        Query<Msg> query = mMsgDao.queryBuilder().where(MsgDao.Properties._UserUID.eq(user.get_UID())).build().forCurrentThread();
        List<Msg> l = query.list();
        for (Msg msg : l) {
            Log.d(TAG, "queryUserAllMsg msg is " + msg);
        }
        return l;
    }

    public LazyList<Msg> queryUserAllMsg(User user, String orderBy) {
        QueryBuilder<Msg> qb = mMsgDao.queryBuilder();
        qb.where(MsgDao.Properties._UserUID.eq(user.get_UID()));
        qb.limit(30);
        LazyList<Msg> l = qb.listLazy();
        return l;
    }

}
