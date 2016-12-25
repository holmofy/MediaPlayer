package cn.hufeifei.mediaplayer.fragment.player;

import android.app.Fragment;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.Event;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

import java.io.File;
import java.util.List;

import cn.hufeifei.mediaplayer.R;
import cn.hufeifei.mediaplayer.domain.Lyric;
import cn.hufeifei.mediaplayer.event.MusicServiceEvent;
import cn.hufeifei.mediaplayer.service.MusicPlayService;
import cn.hufeifei.mediaplayer.utils.Constant;
import cn.hufeifei.mediaplayer.utils.LrcParser;
import cn.hufeifei.mediaplayer.utils.RequestUtil;
import cn.hufeifei.mediaplayer.view.MusicLyricView;

/**
 * 音乐播放界面中的歌词子界面
 * Created by Holmofy on 2016/12/7.
 */
@ContentView(R.layout.fragment_music_lyric)
public class MusicLyricFragment extends Fragment {
    private View rootView;

    @ViewInject(R.id.music_lyric)
    private MusicLyricView musicLyric;

    @ViewInject(R.id.btn_lyric_dir)
    private Button btnLyricDir;

    private Handler handler;

    private List<Lyric> lyricList;


    private MusicPlayService service;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //注册事件总线
        EventBus.getDefault().register(this);
    }


    @Override
    public void onDestroy() {
        //注销事件总线
        EventBus.getDefault().unregister(this);
        handler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (rootView == null) {
            rootView = x.view().inject(this, inflater, container);
            handler = new RefreshHandler();
            handler.sendEmptyMessage(0x0001);
            setViewData();
        }
        return rootView;
    }

    private void setViewData() {
        if (musicLyric != null) {
            if (lyricList != null) {
                btnLyricDir.setVisibility(View.GONE);
            } else {
                btnLyricDir.setVisibility(View.VISIBLE);
            }
            //设置新的歌词，并刷新歌词界面
            musicLyric.setLyricList(lyricList);
            handler.removeCallbacksAndMessages(null);
            handler.sendEmptyMessage(0x0001);
            musicLyric.invalidate();
        }
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onServiceEvent(MusicServiceEvent event) {
        if (event.getAction() == MusicServiceEvent.ACTION_RESUME_BIND   //Activity重新连上后台服务
                || event.getAction() == MusicServiceEvent.ACTION_MUSIC_PREPARED) {
            //音乐准备好后，即可解析歌词
            service = event.getService();
            initLyric();
            setViewData();
        }
    }


    @Event(R.id.btn_lyric_dir)
    private void onClick(View v) {
        //调用文件管理器，进入歌词文件夹
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setDataAndType(Uri.fromFile(Constant.getLyricDir()), "file/*");
        getActivity().startActivity(intent);
    }

    private class RefreshHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {

            if (service != null) {
                int position = service.getCurrentPosition();
                if (position >= 0) {
                    //如果歌曲为准备好(播放下一曲时可能发生),position=-1;
                    musicLyric.refreshLyric(position);
                }
                this.removeCallbacksAndMessages(null);
                this.sendEmptyMessage(0x0001);
            }
        }
    }

    /**
     * 因为此处获取歌词文件涉及到异步网络请求，
     * 所以需要回调方法来处理请求到的歌词文件
     */
    public void initLyric() {
        lyricList = null;
        if (service != null) {
            service.getLyricFile(new RequestUtil.CallBack<File>() {

                @Override
                public void onSuccess(File lrcFile) {
                    LrcParser parser = LrcParser.createLrcParser(lrcFile);
                    if (parser != null) {
                        lyricList = parser.getLyricList();
                        setViewData();
                    }
                }

                //请求失败
                @Override
                public void onError(String errorInfo) {

                }
            });
        }
    }

//    /**
//     * 歌词初始化
//     */
//    public void initLyric() {
//        String title = service.getMusicTitle();
//        String lrcFileName = title + ".lrc";//歌词文件名
//        //获取歌词文件夹
//        File lyricDir = getLyricDir();
//        String[] lyrics = lyricDir.list();
//        if (Arrays.asList(lyrics).contains(lrcFileName)) {
//            //说明该歌曲有歌词，有歌词就进行解析
//            LrcParser parser = LrcParser.createLrcParser(new File(lyricDir, lrcFileName));
//            if (parser != null) {
//                //将歌词设置到控件中
//                lyricList = parser.getLyricList();
//            }
//        } else {
//            //重置歌词，防止切换歌曲时，仍绘制上一曲的歌词
//            lyricList = null;
//        }
//    }
}
