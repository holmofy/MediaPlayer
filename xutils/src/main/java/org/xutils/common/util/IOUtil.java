package org.xutils.common.util;

import android.database.Cursor;
import android.text.TextUtils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;

/**
 * 输入输出流操作的工具类
 */
public class IOUtil {

    private IOUtil() {
    }

    /**
     * 关闭指定的可关闭对象
     *
     * @param closeable 需要关闭的对象
     */
    public static void closeQuietly(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (Throwable ignored) {
                LogUtil.d(ignored.getMessage(), ignored);
            }
        }
    }

    /**
     * 关闭游标，可使用closeQuietly替代
     * Cursor extends Closeable
     *
     * @param cursor 游标对象
     */
    public static void closeQuietly(Cursor cursor) {
        if (cursor != null) {
            try {
                cursor.close();
            } catch (Throwable ignored) {
                LogUtil.d(ignored.getMessage(), ignored);
            }
        }
    }

    /**
     * 从输入流中读取所有的字节并返回
     *
     * @param in 输入流
     * @return 读取的字节数组
     * @throws IOException
     */
    public static byte[] readBytes(InputStream in) throws IOException {
        if (!(in instanceof BufferedInputStream)) {
            in = new BufferedInputStream(in);
        }
        ByteArrayOutputStream out = null;
        try {
            out = new ByteArrayOutputStream();
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) != -1) {
                out.write(buf, 0, len);
            }
            return out.toByteArray();
        } finally {
            closeQuietly(out);
        }
    }

    /**
     * 读取输入流中指定位置指定大小的字节
     *
     * @param in   输入流
     * @param skip 跳过的字节数，或者说从skip+1字节开始
     * @param size 需要读取的字节数
     * @return 读取的字节数组
     * @throws IOException
     */
    public static byte[] readBytes(InputStream in, long skip, int size) throws IOException {
        byte[] result;
        if (skip > 0) {
            //跳过skip个字节
            long skipped;
            while (skip > 0 && (skipped = in.skip(skip)) > 0) {
                skip -= skipped;
            }
        }
        result = new byte[size];
        for (int i = 0; i < size; i++) {
            result[i] = (byte) in.read();
        }
        return result;
    }

    /**
     * 以UTF-8编码格式从输入流中读取字符串
     *
     * @param in 输入流
     * @return 读取的字符串
     * @throws IOException
     */
    public static String readStr(InputStream in) throws IOException {
        return readStr(in, "UTF-8");
    }

    /**
     * 以指定的编码格式从输入流中字符串
     *
     * @param in      输入流
     * @param charset 编码格式
     * @return 读取的字符串
     * @throws IOException
     */
    public static String readStr(InputStream in, String charset) throws IOException {
        if (TextUtils.isEmpty(charset))
            charset = "UTF-8";

        if (!(in instanceof BufferedInputStream)) {
            in = new BufferedInputStream(in);
        }
        Reader reader = new InputStreamReader(in, charset);
        StringBuilder sb = new StringBuilder();
        char[] buf = new char[1024];
        int len;
        while ((len = reader.read(buf)) >= 0) {
            sb.append(buf, 0, len);
        }
        return sb.toString();
    }

    /**
     * 以UTF-8编码格式向输出流中写入指定字符串
     *
     * @param out 输出流
     * @param str 需要写入的字符串
     * @throws IOException
     */
    public static void writeStr(OutputStream out, String str) throws IOException {
        writeStr(out, str, "UTF-8");
    }

    /**
     * 以指定的编码格式向输出流中写入指定的字符串
     *
     * @param out     输出流
     * @param str     需要写入的字符串
     * @param charset 编码格式
     * @throws IOException
     */
    public static void writeStr(OutputStream out, String str, String charset) throws IOException {
        if (TextUtils.isEmpty(charset))
            charset = "UTF-8";

        Writer writer = new OutputStreamWriter(out, charset);
        writer.write(str);
        writer.flush();
    }

    /**
     * 从输入流拷贝到输出流
     * 相当于一个用来拷贝的管道
     *
     * @param in  输入流
     * @param out 输出流
     * @throws IOException
     */
    public static void copy(InputStream in, OutputStream out) throws IOException {
        if (!(in instanceof BufferedInputStream)) {
            in = new BufferedInputStream(in);
        }
        if (!(out instanceof BufferedOutputStream)) {
            out = new BufferedOutputStream(out);
        }
        int len = 0;
        byte[] buffer = new byte[1024];
        while ((len = in.read(buffer)) != -1) {
            out.write(buffer, 0, len);
        }
        out.flush();
    }
}
