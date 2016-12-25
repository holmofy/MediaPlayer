package cn.hufeifei.mediaplayer.domain;

import java.io.Serializable;

/**
 * 电视直播
 * Created by Holmofy on 2016/12/19.
 */

public class TVItem implements Serializable {
    private String name;

    private String resUrl;

    public String getResUrl() {
        return resUrl;
    }

    public void setResUrl(String resUrl) {
        this.resUrl = resUrl;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
