package cn.hufeifei.mediaplayer.domain;

import java.io.Serializable;

/**
 * 歌曲的简略信息
 * Created by Holmofy on 2016/12/10.
 */

public class NetMusicBriefInfo implements Serializable {
    //歌曲标题
    private String title;

    //歌手名
    private String artistName;

    //是否有MV
    private boolean hasMv;

    //歌曲ID
    private int songId;

    public String getArtistName() {
        return artistName;
    }

    public void setArtistName(String artistName) {
        this.artistName = artistName;
    }

    public boolean isHasMv() {
        return hasMv;
    }

    public void setHasMv(boolean hasMv) {
        this.hasMv = hasMv;
    }

    public int getSongId() {
        return songId;
    }

    public void setSongId(int songId) {
        this.songId = songId;
    }

    public String getTitle() {

        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
