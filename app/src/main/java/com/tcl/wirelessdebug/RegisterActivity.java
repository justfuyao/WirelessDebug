package com.tcl.wirelessdebug;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.tcl.database.User;
import com.tcl.utils.IPv4v6Utils;

public class RegisterActivity extends Activity implements OnClickListener {
    private EditText mSetName = null;
    private EditText mSetPassword = null;
    private EditText mConfirmPassword = null;
    private Button mOkButton = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register_layout);

        mSetName = (EditText) findViewById(R.id.setName);
        mSetPassword = (EditText) findViewById(R.id.setPassword);
        mConfirmPassword = (EditText) findViewById(R.id.confirmPassword);

        mOkButton = (Button) findViewById(R.id.ok);
        mOkButton.setOnClickListener(this);
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ok:
                String name = mSetName.getText().toString();
                String password = mSetPassword.getText().toString();
                String cPassword = mConfirmPassword.getText().toString();

                if (TextUtils.isEmpty(name)) {
                    Toast.makeText(this, "Name can't be null", Toast.LENGTH_SHORT).show();
                } else if (TextUtils.isEmpty(password)) {
                    Toast.makeText(this, "Password can't be null", Toast.LENGTH_SHORT).show();
                } else if (TextUtils.isEmpty(cPassword)) {
                    Toast.makeText(this, "Please Confirm Password", Toast.LENGTH_SHORT).show();
                } else if (!cPassword.equals(password)) {
                    Toast.makeText(this, "Passwords are not same", Toast.LENGTH_SHORT).show();
                } else {
                    String uid = String.valueOf(System.currentTimeMillis());// UUID.randomUUID().toString();
                    TalkApplication.getTalkApplication().setUser(new User(null, name, IPv4v6Utils.getLocalIPAddress(), uid, User.USER_STATUS_ONLINE, 0));
                    startActivity(new Intent(getApplicationContext(), UsersActivity.class));
                    finish();
                }
                break;

            default:
                break;
        }
    }
}
