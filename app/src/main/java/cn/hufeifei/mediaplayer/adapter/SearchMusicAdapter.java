package cn.hufeifei.mediaplayer.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

import cn.hufeifei.mediaplayer.R;
import cn.hufeifei.mediaplayer.domain.NetMusicBriefInfo;

/**
 * 搜索到的音乐数据显示适配器
 * Created by Holmofy on 2016/12/16.
 */

public class SearchMusicAdapter extends BaseAdapter {
    private Context context;
    private List<NetMusicBriefInfo> list;

    public SearchMusicAdapter(Context context, List<NetMusicBriefInfo> list) {
        this.context = context;
        this.list = list;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = View.inflate(context, R.layout.search_music_item, null);
            holder = new ViewHolder();
            holder.musicTitle = (TextView) convertView.findViewById(R.id.music_title);
            holder.musicArtist = (TextView) convertView.findViewById(R.id.music_artist);
            holder.musicMv = (TextView) convertView.findViewById(R.id.music_mv);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        NetMusicBriefInfo info = list.get(position);
        holder.musicTitle.setText(info.getTitle());
        holder.musicArtist.setText(info.getArtistName());
        holder.musicMv.setVisibility(info.isHasMv() ? View.VISIBLE : View.GONE);
        return convertView;
    }

    private class ViewHolder {
        TextView musicTitle;
        TextView musicMv;
        TextView musicArtist;
    }
}
