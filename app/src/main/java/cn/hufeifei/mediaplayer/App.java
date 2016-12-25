package cn.hufeifei.mediaplayer;

import android.app.Application;

import org.xutils.x;

import cn.hufeifei.mediaplayer.service.MusicPlayService;

/**
 * Application应用类，在该类中初始化xUtils框架
 * Created by Holmofy on 2016/12/6.
 */

public class App extends Application {

    public MusicPlayService getMusicService() {
        return musicService;
    }

    public void setMusicService(MusicPlayService musicService) {
        this.musicService = musicService;
    }

    private MusicPlayService musicService;


    public boolean isMusicPlayed() {
        return isMusicPlayed;
    }

    public void setMusicPlayed(boolean musicPlayed) {
        isMusicPlayed = musicPlayed;
    }

    //用户判断视频播放之前是否播放音乐了，
    // 如果播放了音乐，则恢复之前播放的音乐
    private boolean isMusicPlayed;

    @Override
    public void onCreate() {
        x.Ext.init(this);
//        x.Ext.setDebug(BuildConfig.DEBUG); //是否输出debug日志，开启debug会影响性能。
    }
}
