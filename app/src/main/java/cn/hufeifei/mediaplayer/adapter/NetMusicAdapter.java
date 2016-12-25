package cn.hufeifei.mediaplayer.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.xutils.x;

import java.util.List;

import cn.hufeifei.mediaplayer.R;

import cn.hufeifei.mediaplayer.domain.NetMusicInfo;


/**
 * 网络音乐列表适配器
 * Created by Holmofy on 2016/12/8.
 */

public class NetMusicAdapter extends BaseAdapter {
    /**
     * 上下文对象
     */
    private Context context;

    /**
     * 网络音乐列表项
     */
    private List<NetMusicInfo> list;

    public NetMusicAdapter(Context context, List<NetMusicInfo> list) {
        this.context = context;
        this.list = list;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = View.inflate(context, R.layout.net_music_list_item, null);
            holder = new ViewHolder();
            holder.textTitle = (TextView) convertView.findViewById(R.id.textTitle);
            holder.textArtist = (TextView) convertView.findViewById(R.id.textArtist);
            holder.textPublishTime = (TextView) convertView.findViewById(R.id.textPublishTime);
            holder.textMusicHot = (TextView) convertView.findViewById(R.id.music_hot);
            holder.textAlbum = (TextView) convertView.findViewById(R.id.textAlbum);

            holder.imgView = (ImageView) convertView.findViewById(R.id.imageView);
            holder.imgIcon = (ImageView) convertView.findViewById(R.id.musicNewIcon);
            holder.textMusicMv = (TextView) convertView.findViewById(R.id.textMusicMv);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        NetMusicInfo item = list.get(position);
        holder.textTitle.setText(item.getTitle());
        holder.textArtist.setText(item.getArtistName());
        holder.textPublishTime.setText(item.getPublishTime());
        holder.textMusicHot.setText("热度：" + item.getHot());
        holder.textAlbum.setText(item.getAlbumTitle());
        if (item.isNew()) {
            holder.imgIcon.setVisibility(View.VISIBLE);
        } else {
            holder.imgIcon.setVisibility(View.GONE);
        }
        if (item.isHasMv()) {
            holder.textMusicMv.setVisibility(View.VISIBLE);
        } else {
            holder.textMusicMv.setVisibility(View.GONE);
        }
        x.image().bind(holder.imgView, item.getBigPicLink());
        return convertView;
    }

    private class ViewHolder {
        TextView textTitle;
        TextView textArtist;
        TextView textPublishTime;
        TextView textMusicHot;
        TextView textAlbum;
        ImageView imgView;
        ImageView imgIcon;
        TextView textMusicMv;
    }
}
