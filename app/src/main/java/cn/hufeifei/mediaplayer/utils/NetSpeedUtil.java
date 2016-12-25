package cn.hufeifei.mediaplayer.utils;

import android.content.Context;
import android.net.TrafficStats;

/**
 * 工具类
 * Created by Holmofy on 2016/11/23.
 */

public class NetSpeedUtil {
    private NetSpeedUtil() {
    }

    private static long lastTotalRxBytes = 0;
    private static long lastTimeStamp = 0;

    /**
     * 获取应用当点网速
     *
     * @param context
     * @return
     */
    public static String getCurrentNetSpeed(Context context) {
        //使用TrafficStats进行流量统计，如果设备不支持流量统计则流量为0，否则把它转换成KB单位
        long nowTotalRxBytes = TrafficStats.getUidRxBytes(context.getApplicationInfo().uid) == TrafficStats.UNSUPPORTED ? 0 : (TrafficStats.getTotalRxBytes() / 1024);//转为KB
        long nowTimeStamp = System.currentTimeMillis();
        long speed = ((nowTotalRxBytes - lastTotalRxBytes) * 1000 / (nowTimeStamp - lastTimeStamp));//毫秒转换

        //保存当前事件供下一次使用
        lastTimeStamp = nowTimeStamp;
        lastTotalRxBytes = nowTotalRxBytes;
        return speed+"KB/S";
    }
}
