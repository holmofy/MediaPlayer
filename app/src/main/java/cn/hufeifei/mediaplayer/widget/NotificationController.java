package cn.hufeifei.mediaplayer.widget;

/**
 * 使用任务栏提示来控制歌曲的播放
 * Created by Holmofy on 2016/11/26.
 */


import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.widget.RemoteViews;

import cn.hufeifei.mediaplayer.R;
import cn.hufeifei.mediaplayer.activity.MusicPlayerActivity;

/**
 * Notification控制器
 */
public class NotificationController {

    /**
     * 广播动作
     */
    public static final String ACTION_MUSIC_PLAY_OR_PAUSE = "cn.hff.intent.action.ACTION_MUSIC_PLAY_OR_PAUSE";

    public static final String ACTION_MUSIC_NEXT = "cn.hff.intent.action.ACTION_MUSIC_NEXT";

    public static final String ACTION_MUSIC_CLOSE_SERVICE = "cn.hff.intent.action.CLOSE_SERVICE";

    /**
     * 附加数据段，标识意图来自于任务栏
     */
    public static final String EXTRA_NOTIFICATION_KEY = "notification";

    /**
     * NOTIFICATION(任务栏提示)的id
     */
    private static final int NOTIFICATION_CONTROLLER = 0x0001;//任务栏中的控件
    /**
     * 上下文
     */
    private Context context;
    /**
     * 任务栏提示管理器
     */
    private NotificationManager manager;
    /**
     * 要显示的任务栏提示
     */
    private Notification notification;


    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public NotificationController(Context context) {
        this.context = context;
        manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        //自定义Notification
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.notification_remote_view);

        initRemoteViews(remoteViews);

        //点击该任务栏提示将直接启动MusicPlayerActivity界面
        Intent intent = new Intent(context, MusicPlayerActivity.class);
        intent.putExtra("notification", true);//标识该意图是任务栏提示传来的
        notification = new Notification.Builder(context)   //建造者模式
                .setContent(remoteViews)//自定义
                .setSmallIcon(R.drawable.local_music_item_img)//设置显示在任务栏上的小图标
                .setContentIntent(PendingIntent.getActivity(context, 0, intent, 0))//设置意图
                .setOngoing(true)//设置正在运行，从而用户不能通过侧滑来删除该提示
                .build();//构建Notification

    }

    private void initRemoteViews(RemoteViews remoteViews) {
        //继续播放按钮
        Intent intentContinue = new Intent();
        intentContinue.setAction(ACTION_MUSIC_PLAY_OR_PAUSE);
        intentContinue.putExtra(EXTRA_NOTIFICATION_KEY, true);
        PendingIntent pendingIntentContinue = PendingIntent.getBroadcast(context, 0, intentContinue, PendingIntent.FLAG_UPDATE_CURRENT);
        remoteViews.setOnClickPendingIntent(R.id.music_btn_continue, pendingIntentContinue);

        //下一曲按钮
        Intent intentNext = new Intent();
        intentNext.setAction(ACTION_MUSIC_NEXT);
        intentNext.putExtra(EXTRA_NOTIFICATION_KEY, true);
        PendingIntent pendingIntentNext = PendingIntent.getBroadcast(context, 1, intentNext, PendingIntent.FLAG_UPDATE_CURRENT);
        remoteViews.setOnClickPendingIntent(R.id.music_btn_next, pendingIntentNext);

        Intent intentClose = new Intent();
        intentClose.setAction(ACTION_MUSIC_CLOSE_SERVICE);
        intentClose.putExtra(EXTRA_NOTIFICATION_KEY, true);
        PendingIntent pendingIntentClose = PendingIntent.getBroadcast(context, 2, intentClose, PendingIntent.FLAG_UPDATE_CURRENT);
        remoteViews.setOnClickPendingIntent(R.id.music_btn_close, pendingIntentClose);
    }


    public void showNotification() {
        manager.notify(NOTIFICATION_CONTROLLER, notification);
    }

    public void cancelNotification() {
        manager.cancel(NOTIFICATION_CONTROLLER);
    }

    public void setMusicTitle(String title) {
        notification.contentView.setTextViewText(R.id.music_title, title);
    }

    public void setMusicArtist(String artist) {
        notification.contentView.setTextViewText(R.id.music_artist, artist);
    }

    public void setContinueOrPauseImg(int resId) {
        notification.contentView.setImageViewResource(R.id.music_btn_continue, resId);
    }

    public void setNotifyImg(BitmapDrawable drawable) {
        notification.contentView.setImageViewBitmap(R.id.notify_img, drawable.getBitmap());
    }

    public void setNotifyImg(int resId) {
        notification.contentView.setImageViewResource(R.id.notify_img, resId);
    }
}
