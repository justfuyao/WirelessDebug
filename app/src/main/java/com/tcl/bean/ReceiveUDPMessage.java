package com.tcl.bean;

import com.tcl.utils.CaculateUtil;

public class ReceiveUDPMessage extends AbstractMessage {
    private static final String TAG = "fuyao-ReceiveUDPMessage";

    @Override
    public String toString() {
        return "ReceiveUDPMessage " + super.toString();
    }

    @Override
    public int getResendTime() {
        throw new UnsupportedOperationException("not support getResendTime");
    }

    @Override
    public void addResendTime() {
        throw new UnsupportedOperationException("not support addResendTime");
    }

    @Override
    protected int continueParse(byte[] msgs, int length) {
        int ret = MessageUtils.PARSE_RESULT_DATA_OK;
        switch (mType) {
            case MessageUtils.TYPE_RETURN_TALK_MSG:
            case MessageUtils.TYPE_RETURN_SAY_HELLO:
                if ((length - MessageUtils.TIME_BYTE_OFFSET) == MessageUtils.TIME_BYTE_SIZE) {
                    mReturnCrc8 = CaculateUtil.bigBytesToInt(msgs, MessageUtils.BASE_TOTAL_BYTE_OFFSET);
                }
                break;

            case MessageUtils.TYPE_TALK_MSG:
                int size = length - MessageUtils.BASE_TOTAL_BYTE_OFFSET;
                if (size > 0) {
                    mContent = new byte[size];
                    System.arraycopy(msgs, MessageUtils.BASE_TOTAL_BYTE_OFFSET, mContent, 0, size);
                }
                break;
            default:
                break;
        }
        return ret;
    }

    @Override
    protected int continueProductMsg() {
        throw new UnsupportedOperationException("not support continueProductMsg");
    }

    @Override
    protected String getTag() {
        return TAG;
    }
}
