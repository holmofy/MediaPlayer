package cn.hufeifei.mediaplayer.adapter;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.format.Formatter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import cn.hufeifei.mediaplayer.R;
import cn.hufeifei.mediaplayer.domain.LocalMediaItem;
import cn.hufeifei.mediaplayer.domain.LocalMusicInfo;
import cn.hufeifei.mediaplayer.utils.ThumbnailUtil;
import cn.hufeifei.mediaplayer.utils.TimeUtil;

/**
 * 本地视频项的适配器
 * Created by Holmofy on 2016/10/26.
 */

public class LocalMediaAdapter extends BaseAdapter {
    /**
     * 上下文
     */
    private Context context;
    /**
     * 媒体列表项
     */
    private List<LocalMediaItem> mediaItems;

    /**
     * 是否是视频媒体
     */
    private boolean isMusic;

    /**
     * 构造器
     *
     * @param context    上下文
     * @param mediaItems 媒体列表项
     */
    public LocalMediaAdapter(Context context, List<LocalMediaItem> mediaItems) {
        this.context = context;
        this.mediaItems = mediaItems;
        if (mediaItems.get(0) instanceof LocalMusicInfo) {
            //如果传进来的列表是歌曲则设置isMusic = true
            isMusic = true;
        }
    }

    /**
     * 获取适配器列表项
     *
     * @return
     */
    @Override
    public int getCount() {
        return mediaItems.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    /**
     * 获取列表项视图
     *
     * @param position    列表项位置
     * @param convertView 转换视图
     * @param parent
     * @return
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = View.inflate(context, R.layout.local_media_list_item, null);
            holder = new ViewHolder();
            holder.textName = (TextView) convertView.findViewById(R.id.textName);
            holder.textDuration = (TextView) convertView.findViewById(R.id.textDuration);
            holder.textSize = (TextView) convertView.findViewById(R.id.textSize);
            holder.imgView = (ImageView) convertView.findViewById(R.id.imageView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        setThumbnail(holder.imgView, position);//设置缩略图
        //文件名
        holder.textName.setText(mediaItems.get(position).getFileName());
        //时长
        holder.textDuration.setText(TimeUtil.formatMilliSecond(mediaItems.get(position).getDuration()));
        //大小
        String size = Formatter.formatFileSize(context, mediaItems.get(position).getSize());
        holder.textSize.setText(size);
        return convertView;
    }

    /**
     * 设置缩略图
     *
     * @param imgView  需要设置缩略图的ImageView控件
     * @param position 该控件在列表中的位置
     */
    private void setThumbnail(ImageView imgView, int position) {
        if (!isMusic) {
            //视频文件
//            holder.imgView.setImageResource(R.drawable.local_video_item_img);
            Drawable drawable = imgView.getDrawable();
            if (drawable instanceof BitmapDrawable) {
                ((BitmapDrawable) drawable).getBitmap().recycle();//回收原有位图
            }
            //获取视频缩略图并设置上去
            imgView.setImageBitmap(ThumbnailUtil.getVideoThumbnail(mediaItems.get(position).getData(), ThumbnailUtil.MICRO_KIND));
        } else {
            //没有专辑封面则设置默认图片
            imgView.setImageResource(R.drawable.local_music_item_img);
        }
    }

    private class ViewHolder {
        TextView textName;
        TextView textDuration;
        TextView textSize;
        ImageView imgView;
    }
}
