package com.tcl.wirelessdebug;

import android.app.IntentService;
import android.content.Intent;
import android.os.IBinder;

public class CMDHandleService extends IntentService {

    public CMDHandleService(String name) {
        super(name);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO Auto-generated method stub
        return super.onBind(intent);
    }

    @Override
    public void onCreate() {
        // TODO Auto-generated method stub
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onStart(Intent intent, int startId) {
        // TODO Auto-generated method stub
        super.onStart(intent, startId);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // TODO Auto-generated method stub
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void setIntentRedelivery(boolean enabled) {
        // TODO Auto-generated method stub
        super.setIntentRedelivery(enabled);
    }

}
