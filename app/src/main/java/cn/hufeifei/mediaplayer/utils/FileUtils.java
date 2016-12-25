package cn.hufeifei.mediaplayer.utils;

import java.io.File;

/**
 * 文件操作工具类
 * Created by Holmofy on 2016/12/24.
 */

public class FileUtils {
    private FileUtils() {
    }

    /**
     * 删除目录并返回删除目录中所有的文件的总大小
     * 在已知输入参数dir是目录的情况下调用该方法
     *
     * @param dir 删除目录所有文件
     * @return 返回删除目录中文件的总大小 byte数
     */
    public static long deleteDir(File dir) {
        if (!dir.isDirectory()) {
            throw new RuntimeException("deleteDir输入参数不是目录");
        }
        long size = 0;
        for (File file : dir.listFiles()) {
            if (file.isFile()) {
                size += file.length();
                if (!file.delete()) {
                    size -= file.length();
                }
            } else {
                size += deleteDir(file);
            }
        }
        dir.delete();//删除目录下所有文件后，即可删除该目录
        return size;
    }
}
