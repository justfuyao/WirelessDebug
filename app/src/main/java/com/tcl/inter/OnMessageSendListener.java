package com.tcl.inter;

import com.tcl.bean.AbstractMessage;


public interface OnMessageSendListener {
    void onMsgSendOK(AbstractMessage msg);
    void onMsgSendError(AbstractMessage msg);
}
