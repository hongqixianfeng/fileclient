package com.nuts.framework.utils;

import java.util.Date;

public class DateUtils {
    public static Date getCurrentDate() {
        return new Date();
    }
    
    public static String dateToString(Date date) {
        return date.toString();
    }

    public static long getCurrentLongTime() {
        return System.currentTimeMillis();
    }

    public static String convertTimeToString(Long time) {
        if (time == null) {
            return "";
        }
        return new Date(time).toString();
    }

    public static String convertTimeToString(long time) {
        return new Date(time).toString();
    }
}
