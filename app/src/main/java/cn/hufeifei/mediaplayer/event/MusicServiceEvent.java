package cn.hufeifei.mediaplayer.event;


import cn.hufeifei.mediaplayer.service.MusicPlayService;

/**
 * 在音乐服务状态发生改变时发送该事件对象
 * 如：音乐准备完成，音乐播放完成等
 * Created by Holmofy on 2016/12/7.
 */
public class MusicServiceEvent {
    //音乐准备好了
    public static final int ACTION_MUSIC_PREPARED = 0x0001;

    public static final int ACTION_RESUME_BIND = 0x0002;

    public static final int ACTION_MUSIC_ITEM_CHANGED = 0x0003;

    public static final int ACTION_MUSIC_PAUSE = 0x0004;

    public static final int ACTION_MUSIC_START = 0x0005;

    public MusicServiceEvent(MusicPlayService service, int action) {
        this.action = action;
        this.service = service;
    }

    private int action;

    public int getAction() {
        return action;
    }

    private MusicPlayService service;

    public MusicPlayService getService() {
        return service;
    }
}