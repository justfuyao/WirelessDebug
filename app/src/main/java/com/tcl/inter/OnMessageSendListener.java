package com.tcl.inter;

import com.tcl.bean.AbstractMessage;


public interface OnMessageSendListener {
    void onMsgSend(AbstractMessage msg);
}
