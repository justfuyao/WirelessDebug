package com.tcl.inter;

import com.tcl.database.Msg;

public interface DispatchMessageInter {
    void onNewMsg(Msg msg);
}
