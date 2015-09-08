package com.tcl.wirelessdebug;

public class TalkMessage {
    private String mIpString = "";
    private String mNameString = "";
    private String mContentString = "";
    private String mTimeString = "";

    public TalkMessage(String ip, String name, String content, String time) {
        mIpString = ip;
        mNameString = name;
        mContentString = content;
        mTimeString = time;
    }

    public String getIp() {
        return mIpString;
    }

    public String getName() {
        return mNameString;
    }

    public String getContent() {
        return mContentString;
    }

    public String getTime() {
        return mTimeString;
    }
}
