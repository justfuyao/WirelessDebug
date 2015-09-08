package com.tcl.bean;

import java.nio.ByteBuffer;

import com.tcl.utils.CaculateUtil;

public class SendUDPMessage extends AbstractMessage {
    private static final String TAG = "fuyao-SendUDPMessage";

    Object lock = new Object();

    @Override
    public int getResendTime() {
        synchronized (lock) {
            return mResendTime;
        }
    }

    @Override
    public void addResendTime() {
        synchronized (lock) {
            mResendTime++;
        }
    }

    @Override
    public String toString() {
        return "SendUDPMessage " + super.toString();
    }

    @Override
    protected int continueParse(byte[] msgs, int length) {
        throw new UnsupportedOperationException("not support continueParse");
    }

    // Send buffer base
    // #length-4###CRC8-4####IP_S-32###IP_D-32####type-1####time-8##
    // |_________|_________|_________|__________|_________|_________|
    @Override
    protected int continueProductMsg() {
        switch (mType) {
            case MessageUtils.TYPE_SAY_HELLO:
                // length-4,crc8-4,ipS-32,ipD-32,type-1,time-8
                mLength = MessageUtils.BASE_TOTAL_BYTE_OFFSET;
                mByteBuffer = ByteBuffer.allocate(mLength);
                break;
            case MessageUtils.TYPE_TALK_MSG:
                // length-4,crc8-4,ipS-32,ipD-32,type-1,content-length
                mLength = MessageUtils.BASE_TOTAL_BYTE_OFFSET + mContent.length;
                mByteBuffer = ByteBuffer.allocate(mLength);
                mByteBuffer.position(MessageUtils.BASE_TOTAL_BYTE_OFFSET);
                mByteBuffer.put(mContent);
                break;
            case MessageUtils.TYPE_RETURN_SAY_HELLO:
            case MessageUtils.TYPE_RETURN_TALK_MSG:
                // length-4,crc8-4,ipS-32,ipD-32,type-1,crc8
                mLength = MessageUtils.BASE_TOTAL_BYTE_OFFSET + MessageUtils.CRC8_BYTE_SIZE;
                mByteBuffer = ByteBuffer.allocate(mLength);
                mByteBuffer.position(MessageUtils.BASE_TOTAL_BYTE_OFFSET);
                mByteBuffer.put(CaculateUtil.bigIntToByte(mCRC8, MessageUtils.CRC8_BYTE_SIZE));
                break;
            default:
                throw new IllegalArgumentException("prepareProductMsg not support type " + MessageUtils.coverType2String(mType));
        }
        mByteBuffer.position(MessageUtils.LENGTH_BYTE_OFFSET);
        mByteBuffer.put(CaculateUtil.bigIntToByte(mLength, MessageUtils.LENGTH_BYTE_SIZE));

        return MessageUtils.PRODUCT_MSG_OK;
    }

    @Override
    protected String getTag() {
        return TAG;
    }
}
