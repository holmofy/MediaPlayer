package cn.hufeifei.mediaplayer.utils;

import android.content.Context;
import android.os.Environment;

import java.io.File;

/**
 * 应用中使用到的数据常量
 * Created by Holmofy on 2016/12/7.
 */

public class Constant {
    private Constant() {
    }

    /**
     * 请求url
     */
    public static final String BAIDU_MUSIC_BASE = "http://tingapi.ting.baidu.com/v1/restserver/ting";

    /**
     * 请求参数
     */
    public static class Param {
        public static final String FORMAT = "format";

        public static final String METHOD = "method";

        public static final String TYPE = "type";

        public static final String SIZE = "size";

        public static final String QUERY = "query";

        public static final String SONG_ID = "songid";

        public static final String OFFSET = "offset";
    }


    /**
     * 存放歌词的文件夹的名字
     */
    private static final String MUSIC_LYRIC_DIR = "Lyric";

    public static File getLyricDir() {
        File musicDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC);
        File lyricDir = new File(musicDir, MUSIC_LYRIC_DIR);
        if (!lyricDir.exists())
            lyricDir.mkdirs();//如果目录不存在，就创建该目录
        return lyricDir;
    }

    private static final String MUSIC_INFO_DIR = "Info";

    public static File getMusicInfoDir(Context context) {
        File musicInfoDir = new File(context.getExternalCacheDir(), MUSIC_INFO_DIR);
        if (!musicInfoDir.exists())
            musicInfoDir.mkdirs();
        return musicInfoDir;
    }
}
