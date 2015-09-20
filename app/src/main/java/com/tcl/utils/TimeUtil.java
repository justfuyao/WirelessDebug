package com.tcl.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class TimeUtil {
    public static String getTimeZone() {
        TimeZone tz = TimeZone.getDefault();
        return tz.getID();
    }

    private static String FORMAT = "MM/dd/yyyy HH:mm:ss";

    private static String FORMAT1 = "HH:mm:ss";

    public static long formatTime(String time, String format) {
        long ret = 0l;
        SimpleDateFormat sdf = new SimpleDateFormat(FORMAT1);
        Date dt2 = null;
        try {
            dt2 = sdf.parse(time);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        ret = dt2 == null ? 0 : dt2.getTime();
        return ret;
    }

    public static String format2TimeString(long time) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(time);
        return calendar.get(Calendar.YEAR) + "-" + calendar.get(Calendar.MONTH) + "-" + calendar.get(Calendar.DATE) + " " + calendar.get(Calendar.HOUR_OF_DAY)
                + ":" + calendar.get(Calendar.MINUTE) + ":" + calendar.get(Calendar.SECOND) + "." + calendar.get(Calendar.MILLISECOND);
    }
}
