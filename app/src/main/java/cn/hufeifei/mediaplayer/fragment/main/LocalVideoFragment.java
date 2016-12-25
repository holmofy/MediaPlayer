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
import cn.hufeifei.mediaplayer.activity.SysMediaPlayerActivity;
import cn.hufeifei.mediaplayer.adapter.LocalMediaAdapter;
import cn.hufeifei.mediaplayer.domain.LocalMediaItem;

/**
 * 本地视频页面
 * Created by Holmofy on 2016/12/6.
 */

@ContentView(R.layout.fragment_local_page)
public class LocalVideoFragment extends Fragment {

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
    private ArrayList<LocalMediaItem> musicItems;

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
            if (musicItems != null && musicItems.size() != 0) {
                //有视频
                textTip.setVisibility(TextView.GONE);
                adapter = new LocalMediaAdapter(getActivity(), musicItems);
                listView.setAdapter(adapter);
            } else {
                //没有视频
                textTip.setText(R.string.text_novideo_tip);
                textTip.setVisibility(TextView.VISIBLE);
            }
            progressBar.setVisibility(ProgressBar.GONE);
        }
    };


    @Event(value = R.id.listView, type = AdapterView.OnItemClickListener.class)
    private void OnItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent intent;
        //如果是视频数据，就使用视频播放界面
        intent = new Intent(getActivity(), SysMediaPlayerActivity.class);
        intent.putExtra(SysMediaPlayerActivity.EXTRA_MEDIA_POSITION, position);
        intent.putExtra(SysMediaPlayerActivity.EXTRA_MEDIA_DATA, musicItems);
        getActivity().startActivity(intent);
    }

    /**
     * 初始化视频数据
     */
    private void initVideoData() {
        musicItems = new ArrayList<>();
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
     * 加载本地视频数据文件
     */
    private void loadLocalFile() {
        ContentResolver resolver = x.app().getApplicationContext().getContentResolver();
        Uri uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
        String[] obj = new String[]{                    //查询的内容
                MediaStore.Video.Media.DISPLAY_NAME,    //文件名
                MediaStore.Video.Media.SIZE,            //文件大小
                MediaStore.Video.Media.DURATION,        //文件时长
                MediaStore.Video.Media.DATA,            //数据地址
        };
        Cursor cursor = resolver.query(uri, obj, null, null, null);
        if (cursor == null) {
            throw new RuntimeException("未找到数据");
        }
        while (cursor.moveToNext()) {
            LocalMediaItem item = new LocalMediaItem();
            musicItems.add(item);      //将查询到的内容放入列表中
            item.setFileName(cursor.getString(0));
            item.setSize(cursor.getLong(1));
            item.setDuration(cursor.getLong(2));
            item.setData(cursor.getString(3));
        }
        handler.sendEmptyMessage(0x0001);
        cursor.close();
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("musicItems", musicItems);
    }


    @Override
    public void onViewStateRestored(Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (musicItems == null) {
            savedInstanceState.getSerializable("musicItems");
        }
    }
}
