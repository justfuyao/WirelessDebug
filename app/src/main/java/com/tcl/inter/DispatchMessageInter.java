package com.tcl.inter;

import com.tcl.bean.AbstractMessage;

public interface DispatchMessageInter {
    void onMSGLogin(AbstractMessage msg);

    void onMSGRegister(AbstractMessage msg);

    void onMSGOnline(AbstractMessage msg);

    void onMSGOffline(AbstractMessage msg);

    void onMSGTalk(AbstractMessage msg);

    void onMSGSendOK(AbstractMessage msg);

}
