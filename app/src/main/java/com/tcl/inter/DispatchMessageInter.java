package com.tcl.inter;

import com.tcl.bean.AbstractMessage;

public interface DispatchMessageInter {
    public void onMSGLogin(AbstractMessage msg);

    public void onMSGRegister(AbstractMessage msg);

    public void onMSGOnline(AbstractMessage msg);

    public void onMSGOffline(AbstractMessage msg);

    public void onMSGTalk(AbstractMessage msg);

}
