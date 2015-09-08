package com.tcl.utils;

import android.util.Log;

public class LogExt {
    public static void e(String tag, String msg) {
        Log.e(tag, TimeUtil.format2TimeString(System.currentTimeMillis()) + " " + getSubTag() + " " + msg);
    }

    public static void d(String tag, String msg) {
        Log.d(tag, TimeUtil.format2TimeString(System.currentTimeMillis()) + " " + getSubTag() + " " + msg);
    }

    public static void e(String tag, String msg, Exception e) {
        Log.e(tag, TimeUtil.format2TimeString(System.currentTimeMillis()) + " " + getSubTag() + " " + msg, e);
    }

    public static String getSubTag() {
        return getSubTag("");
    }

    public static String getSubTag(String tag) {
        return "[" + Thread.currentThread().getId() + "-" + Thread.currentThread().getName() + "]";
    }

    public static String bytesToHexString(byte src) {
        byte[] bt = new byte[1];
        bt[0] = src;
        return bytesToHexString(bt);
    }

    public static String bytesToHexString(byte[] src) {
        StringBuilder stringBuilder = new StringBuilder();
        if (src == null || src.length <= 0) {
            return null;
        }
        for (int i = 0; i < src.length; i++) {
            int v = src[i] & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            if (i == (src.length - 1)) {
                stringBuilder.append(hv);
            } else {
                stringBuilder.append(hv).append("-");
            }
        }
        return stringBuilder.toString();
    }
}
