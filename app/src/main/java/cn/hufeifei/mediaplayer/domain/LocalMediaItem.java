package cn.hufeifei.mediaplayer.domain;

import java.io.Serializable;

/**
 * 本地视频媒体项
 * Created by Holmofy on 2016/12/6.
 */

public class LocalMediaItem implements Serializable {
    /**
     * 文件名
     */
    private String fileName;

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    /**
     * 文件大小
     */
    private long size;

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    /**
     * 媒体时长
     */
    private long duration;

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    /**
     * 文件数据引用，就是一个uri
     */
    private String data;

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }
}
