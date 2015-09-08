package com.tcl.bean;

import android.os.Parcel;
import android.os.Parcelable;

public class User implements Parcelable {
    public static final int STATUS_ONLINE = 1;
    public static final int STATUS_OFFLINE = 2;

    private boolean mIsOwn = false;

    private String mUserName = "Default Name";
    private String mPassword = "123456";

    private String mIp = "";
    private int mStatus = STATUS_ONLINE;

    public static String coverStatus2String(int status) {
        String ret = "unkown";
        switch (status) {
            case STATUS_ONLINE:
                ret = "Online";
                break;
            case STATUS_OFFLINE:
                ret = "Offline";
                break;
            default:
                break;
        }
        return ret;
    }

    public User(User u) {
        mUserName = u.getUserName();
        mPassword = u.getPassword();
        mIp = u.getIP();
        mStatus = u.getStatus();
        mIsOwn = u.getIsOwn();
    }

    public User(String name, String ip, int status) {
        mUserName = name;
        mIp = ip;
        mStatus = status;
    }

    public User(String name, String password, String ip, boolean isOwn) {
        mUserName = name;
        mPassword = password;
        mIp = ip;
        mIsOwn = isOwn;
    }

    public User(Parcel parcel) {
        mUserName = parcel.readString();
        mPassword = parcel.readString();
        mIp = parcel.readString();
        mStatus = parcel.readInt();
        mIsOwn = parcel.readInt() == 1;
    }

    public boolean getIsOwn() {
        return mIsOwn;
    }

    public int getStatus() {
        return mStatus;
    }

    public String getUserName() {
        return mUserName;
    }

    public String getPassword() {
        return mPassword;
    }

    public String getIP() {
        return mIp;
    }

    public final static Creator<User> CREATOR = new Creator<User>() {

        @Override
        public User createFromParcel(Parcel parcel) {
            return new User(parcel);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };

    @Override
    public int describeContents() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mUserName);
        dest.writeString(mPassword);
        dest.writeString(mIp);
        dest.writeInt(mStatus);
        dest.writeInt(mIsOwn == true ? 1 : 0);
    }
}
