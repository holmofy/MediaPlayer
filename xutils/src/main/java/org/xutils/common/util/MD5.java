package org.xutils.common.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * MD5加密工具
 */
public final class MD5 {

    private MD5() {
    }

    private static final char hexDigits[] = {           //16进制字符
            '0', '1', '2', '3', '4', '5', '6', '7',     //
            '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'      //
    };

    /**
     * 将字节数组转换成16进制字符串
     *
     * @param bytes 需要转换的字节数组
     * @return 转换后的字符串
     */
    public static String toHexString(byte[] bytes) {
        if (bytes == null)
            return "";
        StringBuilder hex = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            hex.append(hexDigits[(b >> 4) & 0x0F]);
            hex.append(hexDigits[b & 0x0F]);
        }
        return hex.toString();
    }

    /**
     * 对文件进行加密
     *
     * @param file
     * @return
     * @throws IOException
     */
    public static String md5(File file) throws IOException {
        MessageDigest messagedigest = null;
        FileInputStream in = null;
        FileChannel ch = null;
        byte[] encodeBytes = null;
        try {
            messagedigest = MessageDigest.getInstance("MD5");//获取加密算法工具类
            in = new FileInputStream(file);
            ch = in.getChannel();
            //将文件内容读取到内存中(MappedByteBuffer对象的内存)
            MappedByteBuffer byteBuffer = ch.map(FileChannel.MapMode.READ_ONLY, 0, file.length());
            messagedigest.update(byteBuffer);
            encodeBytes = messagedigest.digest();
        } catch (NoSuchAlgorithmException neverHappened) {
            throw new RuntimeException(neverHappened);
        } finally {
            IOUtil.closeQuietly(in);
            IOUtil.closeQuietly(ch);
        }
        return toHexString(encodeBytes);
    }

    /**
     * 对字符串进行加密
     *
     * @param string 需要解密的字符串
     * @return 加密后的字符串
     */
    public static String md5(String string) {
        byte[] encodeBytes = null;
        try {
            encodeBytes = MessageDigest.getInstance("MD5").digest(string.getBytes("UTF-8"));
        } catch (NoSuchAlgorithmException neverHappened) {
            throw new RuntimeException(neverHappened);
        } catch (UnsupportedEncodingException neverHappened) {
            throw new RuntimeException(neverHappened);
        }

        return toHexString(encodeBytes);
    }
}
