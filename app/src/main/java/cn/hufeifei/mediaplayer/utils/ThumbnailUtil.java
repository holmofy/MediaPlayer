package cn.hufeifei.mediaplayer.utils;

import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.media.ThumbnailUtils;
import android.provider.MediaStore;

/**
 * 缩略图工具类
 * Created by Holmofy on 2016/12/10.
 */

public class ThumbnailUtil {

    private ThumbnailUtil() {
    }

    /**
     * 获取视频缩略图
     *
     * @param videoPath 视频路径
     * @return 返回缩略图
     */
    public static Bitmap getVideoThumbnail(String videoPath) {
        MediaMetadataRetriever media = new MediaMetadataRetriever();
        media.setDataSource(videoPath);
        Bitmap bitmap = media.getFrameAtTime();
        media.release();
        return bitmap;
    }

    public static final int MINI_KIND = MediaStore.Video.Thumbnails.MINI_KIND;

    public static final int MICRO_KIND = MediaStore.Video.Thumbnails.MICRO_KIND;

    /**
     * 获取视频缩略图
     *
     * @param videoPath 视频路径
     *                  //@param width     缩略图宽度
     *                  //@param height    缩略图高度
     * @param kind      类型 MediaStore.Images.Thumbnails.MINI_KIND ,MICRO_KIND,
     * @return 缩略图
     */
    public static Bitmap getVideoThumbnail(String videoPath, /*int width, int height,*/ int kind) {
        return ThumbnailUtils.createVideoThumbnail(videoPath, kind);
//        bitmap = ThumbnailUtils.extractThumbnail(bitmap, width, height, ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
    }

}
