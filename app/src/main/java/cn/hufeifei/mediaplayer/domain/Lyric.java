package cn.hufeifei.mediaplayer.domain;

/**
 * 歌词类
 * Created by Holmofy on 2016/11/29.
 */

public class Lyric {
    /**
     * 歌词内容
     */
    private String content;

    /**
     * 歌词显示的时间戳
     */
    private long timeStamp;

    /**
     * 歌词显示的时长
     */
    private long duration;

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    @Override
    public String toString() {
        return "Lyric{" +
                "content='" + content + '\'' +
                ", timeStamp=" + timeStamp +
                ", duration=" + duration +
                '}';
    }
}
