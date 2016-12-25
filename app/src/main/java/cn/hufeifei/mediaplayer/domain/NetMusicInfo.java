package cn.hufeifei.mediaplayer.domain;

import java.io.Serializable;

/**
 * 网络音乐数据资源项
 * Created by Holmofy on 2016/12/7.
 */

public class NetMusicInfo extends NetMusicBriefInfo implements Serializable {

    public String getBigPicLink() {
        return bigPicLink;
    }

    public void setBigPicLink(String bigPicLink) {
        this.bigPicLink = bigPicLink;
    }

    public String getPublishTime() {
        return publishTime;
    }

    public void setPublishTime(String publishTime) {
        this.publishTime = publishTime;
    }

    public int getHot() {
        return hot;
    }

    public void setHot(int hot) {
        this.hot = hot;
    }

    public boolean isNew() {
        return isNew;
    }

    public void setIsNew(boolean isNew) {
        this.isNew = isNew;
    }

    public String getAlbumTitle() {
        return albumTitle;
    }

    public void setAlbumTitle(String albumTitle) {
        this.albumTitle = albumTitle;
    }

    //歌曲海报小图url
    private String bigPicLink;

    //发布时间
    private String publishTime;

    //歌曲热度
    private int hot;

    //是否为最新资源
    private boolean isNew;

    //专辑名
    private String albumTitle;

}
