package cn.hufeifei.mediaplayer.fragment.main;

import android.app.Fragment;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.Event;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

import java.util.ArrayList;

import cn.hufeifei.mediaplayer.R;
import cn.hufeifei.mediaplayer.activity.MusicPlayerActivity;
import cn.hufeifei.mediaplayer.adapter.LocalMediaAdapter;
import cn.hufeifei.mediaplayer.domain.LocalMediaItem;
import cn.hufeifei.mediaplayer.domain.LocalMusicInfo;

/**
 * 本地音乐界面
 * Created by Holmofy on 2016/12/6.
 */
@ContentView(R.layout.fragment_local_page)
public class LocalMusicFragment extends Fragment {

    private View rootView;
    /**
     * 该页面中的控件
     */
    @ViewInject(R.id.listView)
    private ListView listView;

    @ViewInject(R.id.textTip)
    private TextView textTip;

    @ViewInject(R.id.progressBar)
    private ProgressBar progressBar;

    //本地视频列表
    private ArrayList<LocalMediaItem> mediaItems;

    /**
     * 列表适配器
     */
    private ListAdapter adapter;

    /**
     * 主线程Handler
     */
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (mediaItems != null && mediaItems.size() != 0) {
                //有视频
                textTip.setVisibility(TextView.GONE);
                adapter = new LocalMediaAdapter(getActivity(), mediaItems);
                listView.setAdapter(adapter);
            } else {
                //没有视频
                textTip.setText(R.string.text_nomusic_tip);
                textTip.setVisibility(TextView.VISIBLE);
            }
            progressBar.setVisibility(ProgressBar.GONE);
        }
    };

    @Event(value = R.id.listView, type = AdapterView.OnItemClickListener.class)
    private void OnItemClick(AdapterView<?> parent, View view, int position, long id) {
        //如果是音乐数据就用音乐播放界面播放
        Intent intent = new Intent(getActivity(), MusicPlayerActivity.class);
        intent.putExtra(MusicPlayerActivity.EXTRA_FROM_NET, false);
        intent.putExtra(MusicPlayerActivity.EXTRA_MEDIA_POSITION, position);
        intent.putExtra(MusicPlayerActivity.EXTRA_MEDIA_DATA, mediaItems);
        getActivity().startActivity(intent);
    }

    /**
     * 初始化视频数据
     */
    private void initVideoData() {
        mediaItems = new ArrayList<>();
        //开启子线程对文件进行检索
        new Thread() {
            @Override
            public void run() {
                loadLocalFile();
            }
        }.start();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (rootView == null) {
            //使用xUtils进行view注入
            rootView = x.view().inject(this, inflater, container);
            initVideoData();
        }
        return rootView;
    }

    /**
     * 加载本地音频文件
     */
    private void loadLocalFile() {
        ContentResolver resolver = x.app().getApplicationContext().getContentResolver();
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String[] obj = new String[]{                    //查询的内容
                MediaStore.Audio.Media._ID,             //歌曲ID
                MediaStore.Audio.Media.DISPLAY_NAME,    //文件名
                MediaStore.Audio.Media.SIZE,            //文件大小
                MediaStore.Audio.Media.DURATION,        //文件时长
                MediaStore.Audio.Media.DATA,            //数据地址
                MediaStore.Audio.Media.TITLE,           //歌曲名
                MediaStore.Audio.Media.ARTIST,          //歌曲演唱者
                MediaStore.Audio.Media.ALBUM_ID         //歌曲专辑ID
        };
        Cursor cursor = resolver.query(uri, obj, null, null, null);
        if (cursor == null) {
            throw new RuntimeException("未找到数据");
        }
        while (cursor.moveToNext()) {
            LocalMusicInfo item = new LocalMusicInfo();
            mediaItems.add(item);      //将查询到的内容放入列表中
            item.setSongId(cursor.getLong(0));
            item.setFileName(cursor.getString(1));
            item.setSize(cursor.getLong(2));
            item.setDuration(cursor.getLong(3));
            item.setData(cursor.getString(4));
            item.setTitle(cursor.getString(5));
            item.setArtist(cursor.getString(6));
            item.setAlbumId(cursor.getLong(7));
        }
        handler.sendEmptyMessage(0x0001);
        cursor.close();
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("mediaItems", mediaItems);
    }


    @Override
    public void onViewStateRestored(Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (mediaItems == null) {
            savedInstanceState.getSerializable("mediaItems");
        }
    }
}
