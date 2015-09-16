package com.tcl.bean;

import com.tcl.database.Msg;
import com.tcl.database.User;
import com.tcl.utils.CaculateUtil;

import java.nio.ByteBuffer;

/**
 * Created by android on 9/15/15.
 */
public class MsgExtraContent {

    public MsgExtraContent() {

    }

    private User mSrcUser = null;
    private User mDstUser = null;

    private byte[] mMsg;

    private int mReturnCRC8;

    private String mName;

    private int mPort = -1;

    private ByteBuffer mByteBuffer = null;

    public void setDstUser(User u) {
        mDstUser = u;
    }

    public void setSrcUser(User u) {
        mSrcUser = u;
    }

    public User getSrcUser() {
        return mSrcUser;
    }

    public User getDstUser() {
        return mDstUser;
    }

    public String getExtraName() {
        return mName;
    }

    public String getExtraMsg() {
        return (mMsg == null ? "" : new String(mMsg));
    }

    public int getExtraReturnCrc8() {
        return mReturnCRC8;
    }

    public void setExtraReturnCrc8(int crc8) {
        mReturnCRC8 = crc8;
    }

    public ByteBuffer getByteBuffer() {
        return mByteBuffer;
    }

    public void setByteBuffer(ByteBuffer b) {
        mByteBuffer = b;
    }

    public int getPort() {
        return mPort;
    }

    public void setPort(int port) {
        mPort = port;
    }

    public int productExtra(int type, Msg msg) {
        int length = 0;
        switch (type) {
            case MessageUtils.TYPE_SAY_HELLO:
                // length-4,crc8-4,ipS-32,ipD-32,type-1,time-8,name-length
                length = MessageUtils.BASE_TOTAL_BYTE_OFFSET + mSrcUser.get_Name().getBytes().length;
                mByteBuffer = ByteBuffer.allocate(length);
                mByteBuffer.position(MessageUtils.BASE_TOTAL_BYTE_OFFSET);
                mByteBuffer.put(mSrcUser.get_Name().getBytes());
                break;
            case MessageUtils.TYPE_TALK_MSG:
                // length-4,crc8-4,ipS-32,ipD-32,type-1,time-8,content-length
                length = MessageUtils.BASE_TOTAL_BYTE_OFFSET + mMsg.length;
                mByteBuffer = ByteBuffer.allocate(length);
                mByteBuffer.position(MessageUtils.BASE_TOTAL_BYTE_OFFSET);
                mByteBuffer.put(mMsg);
                break;
            case MessageUtils.TYPE_RETURN_SAY_HELLO:
                // length-4,crc8-4,ipS-32,ipD-32,type-1,time-8,crc8,name
                length = MessageUtils.BASE_TOTAL_BYTE_OFFSET + MessageUtils.CRC8_BYTE_SIZE + mSrcUser.get_Name().getBytes().length;
                mByteBuffer = ByteBuffer.allocate(length);
                mByteBuffer.position(MessageUtils.BASE_TOTAL_BYTE_OFFSET);
                mByteBuffer.put(CaculateUtil.bigIntToByte(mReturnCRC8, MessageUtils.CRC8_BYTE_SIZE));
                mByteBuffer.position(MessageUtils.BASE_TOTAL_BYTE_OFFSET + MessageUtils.CRC8_BYTE_SIZE);
                mByteBuffer.put(mSrcUser.get_Name().getBytes());
                break;
            case MessageUtils.TYPE_RETURN_TALK_MSG:
                // length-4,crc8-4,ipS-32,ipD-32,type-1,time-8,crc8
                length = MessageUtils.BASE_TOTAL_BYTE_OFFSET + MessageUtils.CRC8_BYTE_SIZE;
                mByteBuffer = ByteBuffer.allocate(length);
                mByteBuffer.position(MessageUtils.BASE_TOTAL_BYTE_OFFSET);
                mByteBuffer.put(CaculateUtil.bigIntToByte(mReturnCRC8, MessageUtils.CRC8_BYTE_SIZE));
                break;
            default:
                throw new IllegalArgumentException("prepareProductMsg not support type " + MessageUtils.coverType2String(msg.get_SendType()));
        }
        msg.set_Length(length);
        return MessageUtils.PRODUCT_MSG_OK;
    }

    public int parseExtra(int type, int length, byte[] msgs) {
        switch (type) {
        // length-4,crc8-4,ipS-32,ipD-32,type-1,time-8,crc8
            case MessageUtils.TYPE_RETURN_TALK_MSG:
                if ((length - MessageUtils.BASE_TOTAL_BYTE_OFFSET) >= MessageUtils.CRC8_BYTE_SIZE) {
                    mReturnCRC8 = CaculateUtil.bigBytesToInt(msgs, MessageUtils.BASE_TOTAL_BYTE_OFFSET);
                }
                break;

            // length-4,crc8-4,ipS-32,ipD-32,type-1,time-8,name-length
            case MessageUtils.TYPE_SAY_HELLO:
                int tempLen = length - MessageUtils.BASE_TOTAL_BYTE_OFFSET;
                if (tempLen > 0) {
                    mName = new String(msgs, MessageUtils.BASE_TOTAL_BYTE_OFFSET, length - MessageUtils.BASE_TOTAL_BYTE_OFFSET);
                    mSrcUser.set_Name(mName);
                }
                break;

            // length-4,crc8-4,ipS-32,ipD-32,type-1,time-8,crc8,name
            case MessageUtils.TYPE_RETURN_SAY_HELLO:
                if ((length - MessageUtils.BASE_TOTAL_BYTE_OFFSET) >= MessageUtils.CRC8_BYTE_SIZE) {
                    mReturnCRC8 = CaculateUtil.bigBytesToInt(msgs, MessageUtils.BASE_TOTAL_BYTE_OFFSET);
                    if ((length - MessageUtils.BASE_TOTAL_BYTE_OFFSET - MessageUtils.CRC8_BYTE_SIZE) > 0) {
                        mName = new String(msgs, MessageUtils.BASE_TOTAL_BYTE_OFFSET + MessageUtils.CRC8_BYTE_SIZE, length
                                - MessageUtils.BASE_TOTAL_BYTE_OFFSET - MessageUtils.CRC8_BYTE_SIZE);
                        mSrcUser.set_Name(mName);
                    }
                }
                break;

            // length-4,crc8-4,ipS-32,ipD-32,type-1,time-8,content
            case MessageUtils.TYPE_TALK_MSG:
                int size = length - MessageUtils.BASE_TOTAL_BYTE_OFFSET;
                if (size > 0) {
                    mMsg = new byte[size];
                    System.arraycopy(msgs, MessageUtils.BASE_TOTAL_BYTE_OFFSET, mMsg, 0, size);
                }
                break;
            default:
                break;
        }
        return MessageUtils.PARSE_RESULT_DATA_OK;
    }

    @Override
    public String toString() {
        return "SrcUser:" + mSrcUser + " DstUser:" + mDstUser + " mReturnCRC8:" + mReturnCRC8 + " mName:" + mName + " mPort:" + mPort + " ByteBuffer:"
                + mByteBuffer;
    }

}
