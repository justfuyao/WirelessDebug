package com.tcl.utils;

public class CaculateUtil {

    public static int caculateCRC8(byte[] data, int srcPos, int dstPos) {
        if (data == null || data.length < dstPos || dstPos < srcPos) {
            return -1;
        }

        byte[] temp = new byte[dstPos - srcPos + 1];
        System.arraycopy(data, srcPos, temp, 0, dstPos - srcPos + 1);
        return CRC8.compute(temp);
    }

    public static int unsignedBytesToInt(byte b0, byte b1, byte b2, byte b3) {
        return (unsignedByteToInt(b0) + (unsignedByteToInt(b1) << 8)) + (unsignedByteToInt(b2) << 16) + (unsignedByteToInt(b3) << 24);
    }

    public static int unsignedBytesToInt(byte b0, byte b1) {
        return (unsignedByteToInt(b0) + (unsignedByteToInt(b1) << 8));
    }

    public static int unsignedByteToInt(byte b) {
        return b & 0xFF;
    }

    // TODO : 大小端换算

    // 只有16位，32位或者更高的才有
    //
    // 付尧 2014-11-3 22:33:02
    // 嗯
    // 22:36:06
    // 马启耿
    // 2014-11-3 22:36:06
    // 假设有一个32位的整数，有4个字节，他的存储方式为
    //
    // address 0 1 2 3
    // value b0 b1 b2 b3 小端
    // value b3 b2 b1 b0 大端
    // value = (b3<<24)+(b2<<16)+(b1<<8)+b0;

    /**
     * 小端算法 int to byte[] 支持 1或者 4 个字节
     * 
     * @param i
     * @param len
     * @return
     */
    public static byte[] littleIntToByte(int i, int len) {
        byte[] abyte = null;
        if (len == 1) {
            abyte = new byte[len];
            abyte[0] = (byte) (0xff & i);
        } else {
            abyte = new byte[len];
            abyte[0] = (byte) (0xff & i);
            abyte[1] = (byte) ((0xff00 & i) >> 8);
            abyte[2] = (byte) ((0xff0000 & i) >> 16);
            abyte[3] = (byte) ((0xff000000 & i) >> 24);
        }
        return abyte;
    }

    public static int littleBytesToInt(byte[] bytes) {
        int addr = 0;
        if (bytes.length == 1) {
            addr = bytes[0] & 0xFF;
        } else {
            addr = bytes[0] & 0xFF;
            addr |= ((bytes[1] << 8) & 0xFF00);
            addr |= ((bytes[2] << 16) & 0xFF0000);
            addr |= ((bytes[3] << 24) & 0xFF000000);
        }
        return addr;
    }

    /**
     * 大端算法 int to byte[] 支持 1或者 4 个字节
     * 
     * @param i
     * @param len
     * @return
     */
    public static byte[] bigIntToByte(int i, int len) {
        byte[] abyte = null;
        if (len == 1) {
            abyte = new byte[len];
            abyte[0] = (byte) (0xff & i);
        } else {
            abyte = new byte[len];
            abyte[0] = (byte) ((i >>> 24) & 0xff);
            abyte[1] = (byte) ((i >>> 16) & 0xff);
            abyte[2] = (byte) ((i >>> 8) & 0xff);
            abyte[3] = (byte) (i & 0xff);
        }
        return abyte;
    }

    public static int bigBytesToInt(byte[] bytes) {
        if (bytes == null || bytes.length < 4) {
            throw new IllegalArgumentException();
        }
        int addr = 0;
        if (bytes.length == 1) {
            addr = bytes[0] & 0xFF;
        } else {
            addr = bytes[0] & 0xFF;
            addr = (addr << 8) | (bytes[1] & 0xff);
            addr = (addr << 8) | (bytes[2] & 0xff);
            addr = (addr << 8) | (bytes[3] & 0xff);
        }
        return addr;
    }

    public static int bigBytesToInt(byte[] bytes, int offset) {
        if (bytes == null || bytes.length < (offset + 4)) {
            throw new IllegalArgumentException();
        }
        int addr = 0;
        addr = bytes[offset] & 0xFF;
        addr = (addr << 8) | (bytes[offset + 1] & 0xff);
        addr = (addr << 8) | (bytes[offset + 2] & 0xff);
        addr = (addr << 8) | (bytes[offset + 3] & 0xff);
        return addr;
    }

    public static byte[] long2Byte(long x) {
        byte[] abyte = new byte[8];
        abyte[0] = (byte) (x >> 56);
        abyte[1] = (byte) (x >> 48);
        abyte[2] = (byte) (x >> 40);
        abyte[3] = (byte) (x >> 32);
        abyte[4] = (byte) (x >> 24);
        abyte[5] = (byte) (x >> 16);
        abyte[6] = (byte) (x >> 8);
        abyte[7] = (byte) (x >> 0);
        return abyte;
    }

    public static long bytes2long(byte[] bytes, int offset) {
        if (bytes == null || bytes.length < (offset + 7)) {
            throw new IllegalArgumentException();
        }
        long l = 0l;
        l = bytes[offset] & 0xFF;
        l = (l << 8) | (bytes[offset + 1] & 0xff);
        l = (l << 8) | (bytes[offset + 2] & 0xff);
        l = (l << 8) | (bytes[offset + 3] & 0xff);
        l = (l << 8) | (bytes[offset + 4] & 0xff);
        l = (l << 8) | (bytes[offset + 5] & 0xff);
        l = (l << 8) | (bytes[offset + 6] & 0xff);
        l = (l << 8) | (bytes[offset + 7] & 0xff);
        return l;
    }

    /**
     * Convert hex string to byte[]
     * 
     * @param hexString
     *            the hex string
     * @return byte[]
     */
    public static byte[] hexStringToBytes(String hexString) {
        if (hexString == null || hexString.equals("")) {
            return null;
        }
        hexString = hexString.toUpperCase();
        int length = hexString.length() / 2;
        char[] hexChars = hexString.toCharArray();
        byte[] d = new byte[length];
        for (int i = 0; i < length; i++) {
            int pos = i * 2;
            d[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));
        }
        return d;
    }

    /**
     * Convert char to byte
     * 
     * @param c
     *            char
     * @return byte
     */
    private static byte charToByte(char c) {
        return (byte) "0123456789ABCDEF".indexOf(c);
    }
}
