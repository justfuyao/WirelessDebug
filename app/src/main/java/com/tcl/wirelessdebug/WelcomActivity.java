package com.tcl.wirelessdebug;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class WelcomActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (null != TalkApplication.getTalkApplication().getUser()) {
            startActivity(new Intent(this, UsersActivity.class));
        } else {
            startActivity(new Intent(this, RegisterActivity.class));
        }
        finish();
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
    }

    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
    }

}
