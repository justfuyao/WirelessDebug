package com.tcl.database;

import com.tcl.config.Configuration;
import com.tcl.database.DaoSession;

import de.greenrobot.dao.AbstractDao;
import de.greenrobot.dao.DaoException;

// THIS CODE IS GENERATED BY greenDAO, EDIT ONLY INSIDE THE "KEEP"-SECTIONS

// KEEP INCLUDES - put your custom includes here
import com.tcl.bean.MessageUtils;
import com.tcl.bean.MsgExtraContent;
import com.tcl.utils.CaculateUtil;
import com.tcl.utils.LogExt;
import com.tcl.utils.TimeUtil;

import java.nio.ByteBuffer;

// KEEP INCLUDES END
/**
 * Entity mapped to table "MSG".
 */
public class Msg {

    /** Not-null value. */
    private String _MsgUID;
    /** Not-null value. */
    private String _UserUID;
    private long _Timestamps;
    private int _SendType;
    private Integer _IsReceive;
    private Integer _CRC8;
    private byte[] _Bytes;
    private Integer _Length;
    private Integer _SendTime;

    /** Used to resolve relations */
    private transient DaoSession daoSession;

    /** Used for active entity operations. */
    private transient MsgDao myDao;

    // KEEP FIELDS - put your custom fields here
    public static final int MSG_SEND_TYPE_SEND = 1;
    public static final int MSG_SEND_TYPE_RECEIVE = 2;

    public static final int MSG_SEND_TIME_MAX = Configuration.MSG_RESEND_MAX_TIME;
    public static final int MSG_SEND_TIME_OK = -1;

    private static final String TAG = "fuyao-Msg";

    MsgExtraContent mMsgExtraContent = new MsgExtraContent();

    // KEEP FIELDS END

    public Msg() {
    }

    public Msg(String _MsgUID, String _UserUID, long _Timestamps, int _SendType, Integer _IsReceive, Integer _CRC8, byte[] _Bytes, Integer _Length,
            Integer _SendTime) {
        this._MsgUID = _MsgUID;
        this._UserUID = _UserUID;
        this._Timestamps = _Timestamps;
        this._SendType = _SendType;
        this._IsReceive = _IsReceive;
        this._CRC8 = _CRC8;
        this._Bytes = _Bytes;
        this._Length = _Length;
        this._SendTime = _SendTime;
        parseExtraMsg();
    }

    /** called by internal mechanisms, do not call yourself. */
    public void __setDaoSession(DaoSession daoSession) {
        this.daoSession = daoSession;
        myDao = daoSession != null ? daoSession.getMsgDao() : null;
    }

    /** Not-null value. */
    public String get_MsgUID() {
        return _MsgUID;
    }

    /**
     * Not-null value; ensure this value is available before it is saved to the
     * database.
     */
    public void set_MsgUID(String _MsgUID) {
        this._MsgUID = _MsgUID;
    }

    /** Not-null value. */
    public String get_UserUID() {
        return _UserUID;
    }

    /**
     * Not-null value; ensure this value is available before it is saved to the
     * database.
     */
    public void set_UserUID(String _UserUID) {
        this._UserUID = _UserUID;
    }

    public long get_Timestamps() {
        return _Timestamps;
    }

    public void set_Timestamps(long _Timestamps) {
        this._Timestamps = _Timestamps;
    }

    public int get_SendType() {
        return _SendType;
    }

    public void set_SendType(int _SendType) {
        this._SendType = _SendType;
    }

    public Integer get_IsReceive() {
        return _IsReceive;
    }

    public void set_IsReceive(Integer _IsReceive) {
        this._IsReceive = _IsReceive;
    }

    public Integer get_CRC8() {
        return _CRC8;
    }

    public void set_CRC8(Integer _CRC8) {
        this._CRC8 = _CRC8;
    }

    public byte[] get_Bytes() {
        return _Bytes;
    }

    public void set_Bytes(byte[] _Bytes) {
        this._Bytes = _Bytes;
    }

    public Integer get_Length() {
        return _Length;
    }

    public void set_Length(Integer _Length) {
        this._Length = _Length;
    }

    public Integer get_SendTime() {
        return _SendTime;
    }

    public void set_SendTime(Integer _SendTime) {
        this._SendTime = _SendTime;
    }

    /**
     * Convenient call for {@link AbstractDao#delete(Object)}. Entity must
     * attached to an entity context.
     */
    public void delete() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }
        myDao.delete(this);
    }

    /**
     * Convenient call for {@link AbstractDao#update(Object)}. Entity must
     * attached to an entity context.
     */
    public void update() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }
        myDao.update(this);
    }

    /**
     * Convenient call for {@link AbstractDao#refresh(Object)}. Entity must
     * attached to an entity context.
     */
    public void refresh() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }
        myDao.refresh(this);
    }

    // KEEP METHODS - put your custom methods here

    public void setDstUser(User u) {
        mMsgExtraContent.setDstUser(u);
    }

    public void setSrcUser(User u) {
        mMsgExtraContent.setSrcUser(u);
    }

    public User getSrcUser() {
        return mMsgExtraContent.getSrcUser();
    }

    public User getDstUser() {
        return mMsgExtraContent.getDstUser();
    }

    public int getExtraReturnCrc8() {
        return mMsgExtraContent.getExtraReturnCrc8();
    }

    public void setExtraReturnCrc8(int crc8) {
        mMsgExtraContent.setExtraReturnCrc8(crc8);
    }

    public void setExtraMsg(String msg) {
        mMsgExtraContent.setExtraMsg(null == msg ? null : msg.getBytes());
    }

    public String getExtraMsgString() {
        return mMsgExtraContent.getExtraMsgString();
    }

    public void setByteBuffer(ByteBuffer b) {
        mMsgExtraContent.setByteBuffer(b);
    }

    public ByteBuffer getByteBuffer() {
        return mMsgExtraContent.getByteBuffer();
    }

    public int getPort() {
        return mMsgExtraContent.getPort();
    }

    public void setPort(int port) {
        mMsgExtraContent.setPort(port);
    }

    public void setDstAddress(String ip) {
        if (null == mMsgExtraContent.getDstUser()) {
            User user = new User();
            mMsgExtraContent.setDstUser(user);
        }
        mMsgExtraContent.getDstUser().set_IpAddress(ip);
    }

    public String getDstAddress() {
        return mMsgExtraContent.getDstUser().get_IpAddress();
    }

    public void addSendTime() {
        _SendTime++;
    }

    // will wirte crc8,dstip,srcip,type,time,
    public final int productSendMsg() {
        mMsgExtraContent.productExtra(_SendType, this);
        ByteBuffer byteBuffer = getByteBuffer();
        byteBuffer.position(MessageUtils.LENGTH_BYTE_OFFSET);
        byteBuffer.put(CaculateUtil.bigIntToByte(_Length, MessageUtils.LENGTH_BYTE_SIZE));
        byteBuffer.position(MessageUtils.IP_SRC_BYTE_OFFSET);
        byteBuffer.put(getSrcUser().get_IpAddress().getBytes());
        byteBuffer.position(MessageUtils.UID_SRC_BYTE_OFFSET);
        byteBuffer.put(getSrcUser().get_UID().getBytes());
        byteBuffer.position(MessageUtils.IP_DST_BYTE_OFFSET);
        byteBuffer.put(getDstUser().get_IpAddress().getBytes());
        byteBuffer.position(MessageUtils.UID_DST_BYTE_OFFSET);
        byteBuffer.put(getDstUser().get_UID().getBytes());
        byteBuffer.position(MessageUtils.TYPE_BYTE_OFFSET);
        byteBuffer.put(CaculateUtil.bigIntToByte(_SendType, MessageUtils.TYPE_BYTE_SIZE));
        byteBuffer.position(MessageUtils.TIME_BYTE_OFFSET);
        byteBuffer.put(CaculateUtil.long2Byte(_Timestamps));
        byteBuffer.position(MessageUtils.LENGTH_BYTE_OFFSET);
        byte[] temps = new byte[_Length];
        byteBuffer.get(temps);
        set_CRC8(CaculateUtil.caculateCRC8(temps, 0, temps.length - 1));
        byteBuffer.position(MessageUtils.CRC8_BYTE_OFFSET);
        byteBuffer.putInt(_CRC8);
        byteBuffer.position(_Length);
        byteBuffer.flip();
        _Bytes = getByteBuffer().array();
        LogExt.d(TAG, "productMsg: " + this);
        return MessageUtils.PRODUCT_MSG_OK;
    }

    // receive buffer base
    // #length-4###CRC8-4####IP_S-32###UID_S-8####IP_D-32#####UID_D-8###type-1####time-8##
    // |_________|_________|_________|__________|__________|_________|_________|_________|
    public void parseExtraMsg() {
        mMsgExtraContent.parseExtra(_SendType, _Length, _Bytes);
    }

    public final int parseBuffer(ByteBuffer buffer) {
        int ret = MessageUtils.PARSE_RESULT_DATA_ERROR;
        if (null != buffer) {
            int scrLength = buffer.limit();
            if (scrLength > 4) {
                _Bytes = new byte[scrLength];
                buffer.get(_Bytes);
                LogExt.d(TAG, LogExt.bytesToHexString(_Bytes));
                _Length = CaculateUtil.bigBytesToInt(_Bytes);
                if (_Length > scrLength) {
                    ret = MessageUtils.PARSE_RESULT_DATA_NOT_ENOUGH;
                } else {
                    _CRC8 = CaculateUtil.bigBytesToInt(_Bytes, MessageUtils.CRC8_BYTE_OFFSET);
                    _Bytes[MessageUtils.CRC8_BYTE_OFFSET] = 0;
                    _Bytes[MessageUtils.CRC8_BYTE_OFFSET + 1] = 0;
                    _Bytes[MessageUtils.CRC8_BYTE_OFFSET + 2] = 0;
                    _Bytes[MessageUtils.CRC8_BYTE_OFFSET + 3] = 0;
                    int tempCRC8 = CaculateUtil.caculateCRC8(_Bytes, MessageUtils.LENGTH_BYTE_OFFSET, _Bytes.length - 1);
                    if (_CRC8 == tempCRC8) {
                        User srcUser = new User();
                        srcUser.set_IpAddress(new String(_Bytes, MessageUtils.IP_SRC_BYTE_OFFSET, MessageUtils.IP_SCR_BYTE_SIZE).trim());
                        srcUser.set_UID(new String(_Bytes, MessageUtils.UID_SRC_BYTE_OFFSET, MessageUtils.UID_SRC_BYTE_SIZE).trim());
                        mMsgExtraContent.setSrcUser(srcUser);

                        User dstUser = new User();
                        dstUser.set_UID(new String(_Bytes, MessageUtils.UID_DST_BYTE_OFFSET, MessageUtils.UID_DST_BYTE_SIZE).trim());
                        dstUser.set_IpAddress(new String(_Bytes, MessageUtils.IP_DST_BYTE_OFFSET, MessageUtils.IP_DST_BYTE_SIZE).trim());
                        mMsgExtraContent.setDstUser(dstUser);

                        _SendType = _Bytes[MessageUtils.TYPE_BYTE_OFFSET];
                        _Timestamps = CaculateUtil.bytes2long(_Bytes, MessageUtils.TIME_BYTE_OFFSET);
                        ret = mMsgExtraContent.parseExtra(_SendType, scrLength, _Bytes);
                    } else {
                        ret = MessageUtils.PARSE_RESULT_DATA_ERROR;
                    }
                }
            } else {
                ret = MessageUtils.PARSE_RESULT_DATA_NOT_ENOUGH;
            }
        } else {
            ret = MessageUtils.PARSE_RESULT_DATA_ERROR;
        }
        LogExt.d(TAG, "parseBuffer return " + MessageUtils.coverParseResult2String(ret));
        return ret;
    }

    // compare msg's bytes, if is same, return >0, else return < 0
    public int compare(Msg msg) {
        if (null == msg) {
            return -1;
        } else {
            byte[] tempBytes = msg.get_Bytes();
            if (null != tempBytes && null != _Bytes) {
                int tempLength = tempBytes.length;
                if (tempLength == _Length) {
                    for (int i = 0; i < tempLength; i++) {
                        if (tempBytes[i] != _Bytes[i])
                            return -4;
                    }
                } else {
                    return -3;
                }
            } else {
                return -2;
            }
        }
        return 1;
    }

    public String getDisplayString() {
        StringBuilder sBuilder = new StringBuilder();
        User temp = null;

        temp = getSrcUser();
        if (null != temp) {
            sBuilder.append(temp.get_Name());
            sBuilder.append("\n");
            sBuilder.append(temp.get_IpAddress());
            sBuilder.append("  ");
            sBuilder.append(temp.get_UID());
            sBuilder.append("\n");
        }
        sBuilder.append(TimeUtil.format2TimeString(_Timestamps));
        sBuilder.append("\n");
        sBuilder.append(getExtraMsgString());
        return sBuilder.toString();
    }

    @Override
    public String toString() {
        return "_MsgUID:" + _MsgUID + " _UserUID:" + _UserUID + " _Timestamps = " + _Timestamps + " _Type:" + MessageUtils.coverType2String(_SendType)
                + " _IsReceive:" + _IsReceive + " CRC8:" + _CRC8 + " _Length:" + _Length + " _SendTime:" + _SendTime + " bytes:"
                + LogExt.bytesToHexString(_Bytes) + " ---------Extra:" + mMsgExtraContent;
    }
    // KEEP METHODS END

}
