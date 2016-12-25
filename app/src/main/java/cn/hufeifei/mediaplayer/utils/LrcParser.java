package cn.hufeifei.mediaplayer.utils;

import android.text.TextUtils;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;

import cn.hufeifei.mediaplayer.domain.Lyric;


/**
 * 歌词解析工具
 * Created by Holmofy on 2016/12/1.
 */

public class LrcParser {

    /**
     * 歌词文件
     */
    private File lrcFile;

//    /**
//     * 歌词时间戳的正则匹配表达式
//     */
//    private static final String PATTERN = "(\\[\\d{2}:\\d{2}\\.\\d{2}\\])(.*)";

    /**
     * 歌词标题
     */
    private String title;
    /**
     * 解码方式
     * 默认以UTF-8解码
     */
    private String decode = "UTF-8";

    public String getTitle() {
        return title;
    }

    public String getAlbum() {
        return album;
    }

    public String getArtist() {
        return artist;
    }

    public List<Lyric> getLyricList() {
        return lyricList;
    }

    /**
     * 歌曲专辑
     */
    private String album;
    /**
     * 歌曲演唱者
     */
    private String artist;
    /**
     * 歌词行列表
     */
    private List<Lyric> lyricList;
    /**
     * 歌词时间戳正则模版
     */
    private Pattern pattern;

    private LrcParser(File lrcFile) {
        this.lrcFile = lrcFile;
    }

    public static LrcParser createLrcParser(String path) {
        if (path != null && !path.equals("")) {
            return createLrcParser(new File(path));
        } else {
            return null;
        }
    }

    public static LrcParser createLrcParser(File file) {
        LrcParser parser = null;
        if (file == null) {
            throw new NullPointerException("file引用为空");
        }
        try {
            parser = new LrcParser(file);
            parser.decode = EncodeDetector.getCharset(file);
            parser.lyricList = new ArrayList<>();
            parser.parse();
            return parser;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return parser;
    }

    /**
     * 歌词解析
     *
     * @throws IOException
     */
    private void parse() throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(lrcFile), this.decode));
        String line = "";
        while ((line = reader.readLine()) != null) {
            parseLine(line);
        }
        reader.close();
        sort();
        computeDuration();
    }

    /**
     * 解析行
     *
     * @param line 行字符串
     */
    private void parseLine(String line) {
        if (line == null || line.equals("")) {
            return;
        } else if (line.startsWith("[ti:")) {
            //title歌词标题
            this.title = line.substring(4, line.length() - 1);
        } else if (line.startsWith("[al:")) {
            //album歌曲专辑
            this.album = line.substring(4, line.length() - 1);
        } else if (line.startsWith("[ar:")) {
            //artist歌曲演唱者
            this.artist = line.substring(4, line.length() - 1);
        } else if (Character.isDigit(line.charAt(1))) {
            //第二个字符为数字，说明是时间戳
            String[] result = line.split("\\]");
            if (result.length == 1) {
                //该句歌词只有时间戳没有歌词内容
                Lyric lyric = new Lyric();
                lyric.setTimeStamp(TimeUtil.getTimeStamp(result[0].substring(1)));//将时间戳前的"["去掉
                lyric.setContent("");
                lyricList.add(lyric);
            } else {
                //可能该句歌词有多个时间戳，遍历时间戳，创建歌词对象
                for (int i = 0; i < result.length - 1; i++) {
                    Lyric lyric = new Lyric();
                    lyric.setTimeStamp(TimeUtil.getTimeStamp(result[i].substring(1)));
                    lyric.setContent(result[result.length - 1]);
                    lyricList.add(lyric);
                }
            }
        }
    }

    /**
     * 对解析出来的歌词列表对象进行排序
     */
    private void sort() {
        Collections.sort(lyricList, new Comparator<Lyric>() {
            @Override
            public int compare(Lyric lhs, Lyric rhs) {
                return lhs.getTimeStamp() < rhs.getTimeStamp() ? -1 : 1;
            }
        });
    }

    /**
     * 根据排好序的列表计算时间间隔
     */
    private void computeDuration() {
        for (int i = 0; i < lyricList.size() - 1; i++) {
            Lyric lyric = lyricList.get(i);
            long nextTimeStamp = lyricList.get(i + 1).getTimeStamp();
            lyric.setDuration(nextTimeStamp - lyric.getTimeStamp());
        }
    }

}