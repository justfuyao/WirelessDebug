package com.tcl.bean;

import java.nio.ByteBuffer;

import com.tcl.utils.CaculateUtil;
import com.tcl.utils.LogExt;

public abstract class AbstractMessage {

    int mType = MessageUtils.TYPE_INIT;

    long mTime = 0l;

    int mParseResult = MessageUtils.PARSE_RESULT_UNDO;
    int mLength = -1;

    String mSrcName = "no name";
    String mSrcPassword = "";
    String mSrcIpAdd = "";

    int mPort = -1;

    int mResendTime = 0;

    String mDstName = "";
    String mDstIpAdd = "";

    int mCRC8 = -1;

    int mReturnCrc8 = 0;

    byte[] mContent = null;

    ByteBuffer mByteBuffer = null;

    public ByteBuffer getByteBuffer() {
        return mByteBuffer;
    }

    public String getDstIpAdd() {
        return mDstIpAdd;
    }

    public String getSrcIpAdd() {
        return mSrcIpAdd;
    }

    public byte[] getContent() {
        return mContent;
    }

    public String getSrcName() {
        return mSrcName;
    }

    public int getPort() {
        return mPort;
    }

    public int getType() {
        return mType;
    }

    public int getCRC8() {
        return mCRC8;
    }

    public int getReturnCRC8() {
        return mReturnCrc8;
    }

    public AbstractMessage setType(int type) {
        mType = type;
        return this;
    }

    public AbstractMessage setSrcIpAdd(String ip) {
        mSrcIpAdd = ip;
        return this;
    }

    public AbstractMessage setCRC8(int crc8) {
        mCRC8 = crc8;
        return this;
    }

    public AbstractMessage setReturnCRC8(int crc8) {
        mReturnCrc8 = crc8;
        return this;
    }

    public AbstractMessage setSrcName(String n) {
        mSrcName = n;
        return this;
    }

    public AbstractMessage setDstName(String n) {
        mDstName = n;
        return this;
    }

    public AbstractMessage setDstIpAdd(String ip) {
        mDstIpAdd = ip;
        return this;
    }

    public AbstractMessage setPort(int port) {
        mPort = port;
        return this;
    }

    public AbstractMessage setContent(byte[] c) {
        mContent = c;
        return this;
    }

    // receive buffer base
    // #length-4###CRC8-4####IP_S-32###IP_D-32####type-1####time-8##
    // |_________|_________|_________|__________|_________|_________|
    public final int parseBuffer(ByteBuffer buffer) {
        if (null != buffer) {
            int scrLength = buffer.limit();
            if (scrLength > 4) {
                byte[] msgs = new byte[scrLength];
                buffer.get(msgs);
                LogExt.d(getTag(), LogExt.bytesToHexString(msgs));
                mLength = CaculateUtil.bigBytesToInt(msgs);
                if (mLength > scrLength) {
                    mParseResult = MessageUtils.PARSE_RESULT_DATA_NOT_ENOUGH;
                } else {
                    mCRC8 = CaculateUtil.bigBytesToInt(msgs, MessageUtils.CRC8_BYTE_OFFSET);
                    msgs[MessageUtils.CRC8_BYTE_OFFSET] = 0;
                    msgs[MessageUtils.CRC8_BYTE_OFFSET + 1] = 0;
                    msgs[MessageUtils.CRC8_BYTE_OFFSET + 2] = 0;
                    msgs[MessageUtils.CRC8_BYTE_OFFSET + 3] = 0;
                    int tempCRC8 = CaculateUtil.caculateCRC8(msgs, MessageUtils.LENGTH_BYTE_OFFSET, msgs.length - 1);
                    if (mCRC8 == tempCRC8) {
                        mSrcIpAdd = new String(msgs, MessageUtils.IP_SRC_BYTE_OFFSET, MessageUtils.IP_SCR_BYTE_SIZE).trim();
                        mDstIpAdd = new String(msgs, MessageUtils.IP_DST_BYTE_OFFSET, MessageUtils.IP_DST_BYTE_SIZE).trim();
                        mType = msgs[MessageUtils.TYPE_BYTE_OFFSET];
                        mTime = CaculateUtil.bytes2long(msgs, MessageUtils.TIME_BYTE_OFFSET);
                        mParseResult = continueParse(msgs, scrLength);
                    } else {
                        mParseResult = MessageUtils.PARSE_RESULT_DATA_ERROR;
                    }
                }
            } else {
                mParseResult = MessageUtils.PARSE_RESULT_DATA_NOT_ENOUGH;
            }
        } else {
            mParseResult = MessageUtils.PARSE_RESULT_DATA_ERROR;
        }
        LogExt.d(getTag(), "parseBuffer return " + MessageUtils.coverParseResult2String(mParseResult));
        return mParseResult;
    }

    // will wirte crc8,dstip,srcip,type,time,
    public final int productSendMsg() {
        continueProductMsg();
        mByteBuffer.position(MessageUtils.IP_SRC_BYTE_OFFSET);
        mByteBuffer.put(mSrcIpAdd.getBytes());
        mByteBuffer.position(MessageUtils.IP_DST_BYTE_OFFSET);
        mByteBuffer.put(mDstIpAdd.getBytes());
        mByteBuffer.position(MessageUtils.TYPE_BYTE_OFFSET);
        mByteBuffer.put(CaculateUtil.bigIntToByte(mType, MessageUtils.TYPE_BYTE_SIZE));
        mByteBuffer.position(MessageUtils.TIME_BYTE_OFFSET);
        mTime = System.currentTimeMillis();
        mByteBuffer.put(CaculateUtil.long2Byte(mTime));
        mByteBuffer.position(MessageUtils.LENGTH_BYTE_OFFSET);
        byte[] temps = new byte[mLength];
        mByteBuffer.get(temps);
        mCRC8 = CaculateUtil.caculateCRC8(temps, 0, temps.length - 1);
        mByteBuffer.position(MessageUtils.CRC8_BYTE_OFFSET);
        mByteBuffer.putInt(mCRC8);
        mByteBuffer.position(mLength);
        mByteBuffer.flip();
        LogExt.d(getTag(), "productMsg: " + this);
        return MessageUtils.PRODUCT_MSG_OK;
    }

    protected abstract String getTag();

    protected abstract int continueParse(byte[] msgs, int length);

    public abstract int getResendTime();

    public abstract void addResendTime();

    // we need write length buffer and those buffer after time block
    protected abstract int continueProductMsg();

    @Override
    public String toString() {
        return "Length:" + mLength + " SrcIp:" + mSrcIpAdd + " SrcName:" + mSrcName + " DstIp:" + mDstIpAdd + "" + " DstName:" + mDstName + " port:" + mPort
                + " type:" + MessageUtils.coverType2String(mType) + " CRC8:" + mCRC8 + " returnCRC8:" + mReturnCrc8 + " ResentTime:" + mResendTime
                + " content:" + String.valueOf(mContent);
    }

}
