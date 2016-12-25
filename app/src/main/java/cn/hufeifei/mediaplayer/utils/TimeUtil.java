package cn.hufeifei.mediaplayer.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 与时间相关的工具类
 * Created by Holmofy on 2016/10/26.
 */

public class TimeUtil {
    /**
     * 私有构造，防止实例化
     */
    private TimeUtil() {
    }

    /**
     * 毫秒转化时分秒毫秒
     */
    public static String formatMilliSecond(long l) {
        int hour = 0, minute = 0;

        int second = (int) l / 1000;

        if (second > 60) {
            minute = second / 60;
            second = second % 60;
        }
        if (minute > 60) {
            hour = minute / 60;
            minute = minute % 60;
        }
        return (getTwoLength(hour) + ":" + getTwoLength(minute) + ":" + getTwoLength(second));
    }

    /**
     * 将数字转换成两位数的长度，前面不够补零
     *
     * @param data 传进来的数据
     * @return 将数据转换后返回
     */
    private static String getTwoLength(final int data) {
        if (data < 10) {
            return "0" + data;
        } else {
            return "" + data;
        }
    }

    private static SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");

    /**
     * 获取当前时间
     *
     * @return 返回当前时间的字符串
     */
    public static String getCurrentTime() {
        return timeFormat.format(new Date());
    }


    /**
     * 将时间戳字符串转换成长整型
     * mm:ss.SS  分：秒.毫秒
     *
     * @param timeStamp 字符串
     * @return -1标识转换失败
     */
    public static long getTimeStamp(String timeStamp) {
        timeStamp = timeStamp.trim();
        int minute = Integer.parseInt(timeStamp.substring(0, 2));
        int second = Integer.parseInt(timeStamp.substring(3, 5));
        int millSec = Integer.parseInt(timeStamp.substring(6, 8));
        return ((minute * 60) + second) * 1000 + millSec;
    }
}
