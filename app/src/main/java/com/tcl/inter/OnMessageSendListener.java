package com.tcl.inter;


import com.tcl.database.Msg;

public interface OnMessageSendListener {
    void onMsgSendOK(Msg msg);
    void onMsgSendError(Msg msg);
}
