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
        // length-4,crc8-4,ipS-32,ipD-32,type-1,time-8,crc8
            case MessageUtils.TYPE_RETURN_TALK_MSG:
                if ((length - MessageUtils.BASE_TOTAL_BYTE_OFFSET) >= MessageUtils.CRC8_BYTE_SIZE) {
                    mReturnCrc8 = CaculateUtil.bigBytesToInt(msgs, MessageUtils.BASE_TOTAL_BYTE_OFFSET);
                }
                break;

            // length-4,crc8-4,ipS-32,ipD-32,type-1,time-8,name-length
            case MessageUtils.TYPE_SAY_HELLO:
                if ((length - MessageUtils.BASE_TOTAL_BYTE_OFFSET) > 0) {
                    if ((length - MessageUtils.BASE_TOTAL_BYTE_OFFSET) > 0) {
                        mSrcName = new String(msgs, MessageUtils.BASE_TOTAL_BYTE_OFFSET, length - MessageUtils.BASE_TOTAL_BYTE_OFFSET);
                    }
                }
                break;

            // length-4,crc8-4,ipS-32,ipD-32,type-1,time-8,crc8,name
            case MessageUtils.TYPE_RETURN_SAY_HELLO:
                if ((length - MessageUtils.BASE_TOTAL_BYTE_OFFSET) >= MessageUtils.CRC8_BYTE_SIZE) {
                    mReturnCrc8 = CaculateUtil.bigBytesToInt(msgs, MessageUtils.BASE_TOTAL_BYTE_OFFSET);
                    if ((length - MessageUtils.BASE_TOTAL_BYTE_OFFSET - MessageUtils.CRC8_BYTE_SIZE) > 0) {
                        mSrcName = new String(msgs, MessageUtils.BASE_TOTAL_BYTE_OFFSET + MessageUtils.CRC8_BYTE_SIZE, length
                                - MessageUtils.BASE_TOTAL_BYTE_OFFSET - MessageUtils.CRC8_BYTE_SIZE);
                    }
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
