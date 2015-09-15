package com.tcl.database;

import android.content.Context;

import com.tcl.database.UserDao.Properties;
import com.tcl.wirelessdebug.TalkApplication;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.dao.async.AsyncOperation;
import de.greenrobot.dao.async.AsyncOperationListener;
import de.greenrobot.dao.async.AsyncSession;
import de.greenrobot.dao.query.LazyList;
import de.greenrobot.dao.query.Query;
import de.greenrobot.dao.query.QueryBuilder;

public class DatabaseManager {

    private static DatabaseManager mDatabaseProxy = null;

    private MsgDao mMsgDao;
    private UserDao mUserDao;
    private Context mContext;
    private DaoSession mDaoSession;
    private DaoMaster mDaoMaster;
    private AsyncSession mAsyncSession;

    private DatabaseManager(Context c) {
        mContext = c.getApplicationContext();
        mDaoMaster = TalkApplication.getDaoMaster(mContext);
        mDaoSession = TalkApplication.getDaoSession(mContext);
        mMsgDao = mDaoSession.getMsgDao();
        mUserDao = mDaoSession.getUserDao();
        mAsyncSession = mDaoSession.startAsyncSession();
        mAsyncSession.setListener(mProxyListenerThread);
        mAsyncSession.setListenerMainThread(mProxyListenerMainThread);
    }

    // not threadsafe
    public static DatabaseManager getInstance(Context c) {
        if (null == mDatabaseProxy) {
            mDatabaseProxy = new DatabaseManager(c);
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

    public AsyncOperation asynInsertOrUpdateUserAndNotify(User user) {
        Query<User> query = mUserDao.queryBuilder().where(Properties._UID.eq(user.get_UID())).build();
        AsyncOperation operation = mAsyncSession.queryUnique(query);
        operation = (AsyncOperation) operation.waitForCompletion();
        if (!operation.isFailed()) {
            User tempUser = (User) operation.getResult();
            tempUser.setUserStatus(User.USER_STATUS_ONLINE);
            operation = mAsyncSession.refresh(tempUser);
            operation = (AsyncOperation) operation.waitForCompletion();
        } else {
            operation = mAsyncSession.insertOrReplace(user);
            operation = (AsyncOperation) operation.waitForCompletion();
        }
        if (!operation.isFailed()) {
            // TODO: notify
        }
        return operation;
    }

    public AsyncOperation asynQueryAllUsers() {
        Query<User> query = mUserDao.queryBuilder().build();
        AsyncOperation operation = mAsyncSession.queryList(query);
        operation.waitForCompletion();
        return operation;
    }

    public LazyList<Msg> queryUserAllMsg(User user, String orderBy) {
        QueryBuilder<Msg> qb = mMsgDao.queryBuilder();
        qb.where(MsgDao.Properties._UserUID.eq(user.get_UID()));
        qb.limit(30);
        LazyList<Msg> l = qb.listLazy();
        return l;
    }

}
