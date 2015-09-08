package com.tcl.utils;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.regex.Pattern;

public class IPv4v6Utils {

    /** * 获取本地IP地址 * @author YOLANDA * @return */
    public static String getLocalIPAddress() {
        String ipAddress = "";

        try {
            Enumeration<NetworkInterface> netfaces = NetworkInterface.getNetworkInterfaces(); // 遍历所用的网络接口
            while (netfaces.hasMoreElements()) {
                NetworkInterface nif = netfaces.nextElement();// 得到每一个网络接口绑定的地址
                Enumeration<InetAddress> inetAddresses = nif.getInetAddresses(); // 遍历每一个接口绑定的所有ip
                while (inetAddresses.hasMoreElements()) {
                    InetAddress ip = inetAddresses.nextElement();

                    if (!ip.isLoopbackAddress() && isIPv4Address(ip.getHostAddress())) {
                        ipAddress = ip.getHostAddress().toString();
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return ipAddress;
    }

    /*
     * 这里要加上一点说明，因为在21开始，HTTPClient被抛弃了，Google推荐使用URLConnect，这里的Ipv4也被抛弃了，为了兼容以后的版本
     * ，我把HTTPClient的一些源码直接拿到项目中来用，所以这里出现了Ipv4的检查的源码
     */
    /** * Ipv4地址检查 */
    private static final Pattern IPV4_PATTERN = Pattern
            .compile("^(([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\.){3}([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])$");

//    public static String matchIPV4(String in) {
//        Matcher temp = IPV4_PATTERN.matcher(in);
//        if(temp.find()){
//            return temp.group();
//        }
//        return null;
//    }

    /**
     * * 检查是否是有效的IPV4地址 *
     * 
     * @param input
     *            the address string to check for validity *
     * @return true if the input parameter is a valid IPv4 address
     */
    public static boolean isIPv4Address(final String input) {
        return IPV4_PATTERN.matcher(input).matches();
    }

    /* ===========以下是IPv6的检查，，暂时用不到========== */
    // 未压缩过的IPv6地址检查
    private static final Pattern IPV6_STD_PATTERN = Pattern.compile("^[0-9a-fA-F]{1,4}(:[0-9a-fA-F]{1,4}){7}$");

    // 检查参数是否有效的标准(未压缩的)IPv6地址
    public static boolean isIPv6StdAddress(final String input) {
        return IPV6_STD_PATTERN.matcher(input).matches();
    }

    // 压缩过的IPv6地址检查
    private static final Pattern IPV6_HEX_COMPRESSED_PATTERN = Pattern.compile("^(([0-9A-Fa-f]{1,4}(:[0-9A-Fa-f]{1,4}){0,5})?)" + "::"
            + "(([0-9A-Fa-f]{1,4}(:[0-9A-Fa-f]{1,4}){0,5})?)$");

    // 检查参数是否有效压缩IPv6地址
    public static boolean isIPv6HexCompressedAddress(final String input) {
        int colonCount = 0;
        for (int i = 0; i < input.length(); i++) {
            if (input.charAt(i) == ':') {
                colonCount++;
            }
        }
        return colonCount <= 7 && IPV6_HEX_COMPRESSED_PATTERN.matcher(input).matches();
    }

    // 检查是否是压缩或者未压缩过的IPV6地址
    public static boolean isIPv6Address(final String input) {
        return isIPv6StdAddress(input) || isIPv6HexCompressedAddress(input);
    }

}
