package com.tcl.bean;


public class MessageUtils {
    private static final String TAG = "fuyao-MessageUtils";

    // receive buffer base
    // #length-4###CRC8-4####IP_S-32###UID_S-8####IP_D-32####UID_D-8####type-1####time-8##
    // |_________|_________|_________|_________|__________|__________|_________|_________|
    public static final int LENGTH_BYTE_SIZE = 4;
    public static final int CRC8_BYTE_SIZE = 4;
    public static final int IP_SCR_BYTE_SIZE = 32;
    // TODO:now use time instead caculate uid
    public static final int UID_SRC_BYTE_SIZE = 16;
    public static final int IP_DST_BYTE_SIZE = IP_SCR_BYTE_SIZE;
    // TODO:now use time instead caculate uid
    public static final int UID_DST_BYTE_SIZE = UID_SRC_BYTE_SIZE;
    public static final int TYPE_BYTE_SIZE = 1;
    public static final int TIME_BYTE_SIZE = 8;

    public static final int LENGTH_BYTE_OFFSET = 0;
    public static final int CRC8_BYTE_OFFSET = LENGTH_BYTE_SIZE;
    public static final int IP_SRC_BYTE_OFFSET = CRC8_BYTE_OFFSET + CRC8_BYTE_SIZE;
    public static final int UID_SRC_BYTE_OFFSET = IP_SRC_BYTE_OFFSET + IP_SCR_BYTE_SIZE;
    public static final int IP_DST_BYTE_OFFSET = UID_SRC_BYTE_OFFSET + UID_SRC_BYTE_SIZE;
    public static final int UID_DST_BYTE_OFFSET = IP_DST_BYTE_OFFSET + IP_DST_BYTE_SIZE;
    public static final int TYPE_BYTE_OFFSET = UID_DST_BYTE_OFFSET + UID_DST_BYTE_SIZE;
    public static final int TIME_BYTE_OFFSET = TYPE_BYTE_OFFSET + TYPE_BYTE_SIZE;
    public static final int BASE_TOTAL_BYTE_OFFSET = TIME_BYTE_OFFSET + TIME_BYTE_SIZE;

    public static final int WRITE_TYPE_TCP = 1;
    public static final int WRITE_TYPE_UDP = 2;
    public static final int WRITE_TYPE_DEFAULT = WRITE_TYPE_UDP;

    public static final int RECEIVE_TYPE_TCP = 3;
    public static final int RECEIVE_TYPE_UDP = 4;

    public static final int TYPE_BASE = 0;
    public static final int TYPE_INIT = TYPE_BASE + 1;
    public static final int TYPE_SAY_HELLO = TYPE_BASE + 2;
    public static final int TYPE_DISCONNECT = TYPE_BASE + 3;
    public static final int TYPE_TALK_MSG = TYPE_BASE + 4;
    public static final int TYPE_EXCUTE_MSG = TYPE_BASE + 5;
    public static final int TYPE_REGISTER = TYPE_BASE + 6;
    public static final int TYPE_LOGIN = TYPE_BASE + 7;
    public static final int TYPE_BASE_END = TYPE_LOGIN + 1;

    public static final int TYPE_RETURN_BASE = TYPE_BASE_END + 1;
    public static final int TYPE_RETURN_SAY_HELLO = TYPE_RETURN_BASE + 1;
    public static final int TYPE_RETURN_TALK_MSG = TYPE_RETURN_BASE + 2;
    public static final int TYPE_RETURN_END = TYPE_RETURN_TALK_MSG + 1;

    public static final int RETURN_OK = 1;
    public static final int RETURN_NOK = 2;

    public static final int PARSE_RESULT_UNDO = -1;
    public static final int PARSE_RESULT_DATA_ERROR = 0;
    public static final int PARSE_RESULT_DATA_OK = 1;
    public static final int PARSE_RESULT_DATA_NOT_ENOUGH = 2;

    public static final int PRODUCT_MSG_ERROR = 1;
    public static final int PRODUCT_MSG_OK = 2;

    public static final int SEND_STATUS_PENDING = 1;
    public static final int SEND_STATUS_SENDING = 2;
    public static final int SEND_STATUS_OK = 3;

    public static String coverParseResult2String(int result) {
        String ret = "unkown parse result";
        switch (result) {
            case PARSE_RESULT_UNDO:
                ret = "parse result undo";
                break;
            case PARSE_RESULT_DATA_ERROR:
                ret = "parse result data error";
                break;
            case PARSE_RESULT_DATA_OK:
                ret = "parse result data ok";
                break;
            case PARSE_RESULT_DATA_NOT_ENOUGH:
                ret = "parse result data error";
                break;
            default:
                break;
        }
        return ret;
    }

    public static int coverType2ReturnType(int type) {
        int ret = TYPE_BASE;
        switch (type) {
            case TYPE_SAY_HELLO:
                ret = TYPE_RETURN_SAY_HELLO;
                break;
            case TYPE_TALK_MSG:
                ret = TYPE_RETURN_TALK_MSG;
                break;
            default:
                throw new IllegalArgumentException("not support type:" + coverType2String(type));
        }
        return ret;
    }

    public static String coverType2String(int type) {
        String ret = "unkown type";
        switch (type) {
            case TYPE_BASE:
                ret = "type base";
                break;
            case TYPE_INIT:
                ret = "type init";
                break;
            case TYPE_SAY_HELLO:
                ret = "type say hello";
                break;
            case TYPE_DISCONNECT:
                ret = "type disconnect";
                break;
            case TYPE_TALK_MSG:
                ret = "type talk msg";
                break;
            case TYPE_EXCUTE_MSG:
                ret = "type excute msg";
                break;
            case TYPE_REGISTER:
                ret = "type register";
                break;
            case TYPE_LOGIN:
                ret = "type login";
                break;
            case TYPE_RETURN_SAY_HELLO:
                ret = "type return say hello";
                break;
            case TYPE_RETURN_TALK_MSG:
                ret = "type return talk msg";
                break;
            default:
                break;
        }
        return ret;
    }

}
