package cn.hufeifei.mediaplayer.domain;

/**
 * 网络音乐信息
 * Created by Holmofy on 2016/12/10.
 */

public class NetMusicItem {

    public String getPicPremiumLink() {
        return picPremiumLink;
    }

    public void setPicPremiumLink(String picPremiumLink) {
        this.picPremiumLink = picPremiumLink;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public int getSongId() {
        return songId;
    }

    public void setSongId(int songId) {
        this.songId = songId;
    }

    public String getLrcLink() {
        return lrcLink;
    }

    public void setLrcLink(String lrcLink) {
        this.lrcLink = lrcLink;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getFileLink() {
        return fileLink;
    }

    public void setFileLink(String fileLink) {
        this.fileLink = fileLink;
    }

    /**
     * 歌词链接
     */
    private String lrcLink;

    /**
     * 歌曲标题
     */
    private String title;

    /**
     * 歌曲文件链接
     */
    private String fileLink;
    /**
     * 超大海报，用于播放页面的旋转唱片动画
     */
    private String picPremiumLink;

    /**
     * 歌曲演唱者名字
     */
    private String author;

    /**
     * 歌曲ID
     */
    private int songId;
}
