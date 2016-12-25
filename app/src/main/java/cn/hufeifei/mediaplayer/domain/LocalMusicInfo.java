package cn.hufeifei.mediaplayer.domain;

/**
 * 本地音乐数据项
 * 在媒体数据项的基础上添加了歌曲标题以及歌曲演唱者
 * Created by Holmofy on 2016/12/6.
 */

public class LocalMusicInfo extends LocalMediaItem {
    /**
     * 歌曲在MediaStore中的ID
     */
    private long songId;

    public long getSongId() {
        return songId;
    }

    public void setSongId(long songId) {
        this.songId = songId;
    }

    /**
     * 歌曲名
     */
    private String title;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * 歌曲演唱者
     */
    private String artist;

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    /**
     * 歌曲专辑ID，主要用来获取Mp3专辑缩略图
     */
    private long albumId;

    public long getAlbumId() {
        return albumId;
    }

    public void setAlbumId(long albumId) {
        this.albumId = albumId;
    }
}
