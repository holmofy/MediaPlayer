package cn.hufeifei.mediaplayer.activity;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.ClipDrawable;
import android.graphics.drawable.LayerDrawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import java.util.ArrayList;

import cn.hufeifei.mediaplayer.App;
import cn.hufeifei.mediaplayer.R;
import cn.hufeifei.mediaplayer.domain.LocalMediaItem;
import cn.hufeifei.mediaplayer.domain.TVItem;
import cn.hufeifei.mediaplayer.service.MusicPlayService;
import cn.hufeifei.mediaplayer.utils.NetSpeedUtil;
import cn.hufeifei.mediaplayer.utils.TimeUtil;

/**
 * 系统媒体播放器页面
 * 使用Android平台自带的MediaPlayer播放媒体
 */
public class SysMediaPlayerActivity extends Activity {
    //当传入媒体播放列表时，当前需要播放的媒体数据的位置
    public static final String EXTRA_MEDIA_POSITION = "media_list_position";
    //媒体数据
    public static final String EXTRA_MEDIA_DATA = "media_data";
    //是否为网络直播
    public static final String EXTRA_IS_TV_ITEM = "media_is_tv_item";
    //网络直播资源
    public static final String EXTRA_MEDIA_TV_ITEM = "media_tv_item";
    //控制面板刷新消息
    private static final int MSG_REFRESH = 0x0001;
    //控制面板隐藏消息
    private static final int MSG_HIDE = 0x0002;
    //自动隐藏的时间间隔，5秒后自动隐藏控制面板
    private static final long HIDE_DURATION = 5000;

    //监听视频是否出现卡顿
//    private static final int MSG_CHECK_BLOCK = 0x0003;

    /**
     * 界面控件
     */
    private VideoView videoView;//视频播放控件

    //媒体控制控件
    private RelativeLayout mediaRootView;
    private TextView mediaName;
    private TextView mediaSysPowerText;
    private ImageView mediaSysPowerImage;
    private TextView mediaSysTime;
    private ImageButton mediaSoundBtn;
    private SeekBar mediaSoundSeekbar;
    private ImageButton mediaSwitchPlayer;
    private TextView mediaCurrTime;
    private SeekBar mediaCurrTimeSeekbar;
    private TextView mediaDuration;
    private ImageButton mediaReturnBtn;
    private ImageButton mediaPrevBtn;
    private ImageButton mediaPauseBtn;
    private ImageButton mediaNextBtn;
    private ImageButton mediaResizeBtn;
    private RelativeLayout mediaLoadingMask;
    private TextView mediaLoadingSpeed;

    private MediaController mediaController;
    private VideoStateListener videoListener;

    /**
     * 音频管理器来调节播放音量
     */
    private AudioManager audioManager;

    /**
     * 系统电量的广播接收器
     */
    private SysPowerReceiver receiver;

    /**
     * 当传入媒体播放列表时，当前需要播放的媒体数据的位置
     */
    private int mediaPosition;
    /**
     * 媒体数据
     */
    private ArrayList<LocalMediaItem> videoItems;
    /**
     * 电视直播项
     */
    private TVItem tvItem;
    /**
     * 其他应用调用该播放器时传入的uri，或者播放网络资源的uri
     */
    private Uri dataUri;
    /**
     * 该资源是否是网络资源
     */
    private boolean isNetResource;
    /**
     * 视频有没有准备好
     */
    private boolean isVideoPrepared;

    private Handler handler = new Handler() {


        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_REFRESH:
                    refreshUI();
                    handler.removeMessages(MSG_REFRESH);
                    handler.sendEmptyMessageDelayed(MSG_REFRESH, 1000);
                    break;
                case MSG_HIDE:
                    mediaRootView.setVisibility(View.GONE);
                    this.removeMessages(MSG_HIDE);
                    break;
            }
        }
    };

    //上一次播放的位置
    long lastPosition;

    private void refreshUI() {

        //设置播放控制面板中时间控件的文本
        mediaSysTime.setText(TimeUtil.getCurrentTime());
        if (isVideoPrepared) {
            //设置播放进度
            mediaCurrTimeSeekbar.setProgress(videoView.getCurrentPosition());
            //设置缓冲进度
            int secondProgress = videoView.getBufferPercentage() * videoView.getDuration() / 100;
            mediaCurrTimeSeekbar.setSecondaryProgress(secondProgress);
            //设置当前播放时间
            mediaCurrTime.setText(TimeUtil.formatMilliSecond(videoView.getCurrentPosition()));
        }
        if (isNetResource && videoView.isPlaying()) {
            long currentPosition = videoView.getCurrentPosition();
            if (currentPosition - lastPosition < 1000 / 2) {
                //因为每秒钟刷新一次，如果当前位置减去上一次位置小于1000说明视频卡住了
                mediaLoadingMask.setVisibility(View.VISIBLE);
            } else {
                //否则就说明视频没卡，那么显示网速的进度就应该消失
                mediaLoadingMask.setVisibility(View.GONE);
            }
            lastPosition = currentPosition;
        }

        //设置当前网速
        String speed = NetSpeedUtil.getCurrentNetSpeed(SysMediaPlayerActivity.this);
        mediaLoadingSpeed.setText("玩命加载中..." + speed);
    }

    //暂停可能正在播放的后台音乐
    private void pauseMusic() {
        MusicPlayService service = ((App) getApplication()).getMusicService();
        if (service != null) {
            if (service.isPlaying()) {
                ((App) getApplication()).setMusicPlayed(true);
                service.pause();
            }
        }
    }

    //恢复播放视频前的音乐状态
    private void resumeMusic() {
        MusicPlayService service = ((App) getApplication()).getMusicService();
        if (service != null && ((App) getApplication()).isMusicPlayed()) {
            service.start();
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //暂停可能正在播放的后台音乐
        pauseMusic();
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_sys_media_player);
        audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_BATTERY_OKAY);
        filter.addAction(Intent.ACTION_BATTERY_CHANGED);
        filter.addAction(Intent.ACTION_BATTERY_LOW);
        receiver = new SysPowerReceiver();
        registerReceiver(receiver, filter);//注册广播
        findView();
        getPlayData();//获取播放数据
        initViewData();//根据播放数据初始化各控件数据
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(receiver);
        handler.removeCallbacksAndMessages(null);
        super.onDestroy();
        if (!isStartVitamio) {
            //未启动Vitamio，则恢复播放视频之前可能正在播放的后台音乐
            resumeMusic();
        }
    }

    private LocalMediaItem getMediaItem() {
        return videoItems.get(mediaPosition);
    }

    private void getPlayData() {
        Intent intent = getIntent();
        dataUri = intent.getData();
        if (dataUri != null) {
            //其他应用调用该播放面板或者播放网络资源时传入uri进行播放
            videoView.setVideoURI(dataUri);
            //因为资源只有一个，因此不需要上一个和下一个按钮
            mediaPrevBtn.setEnabled(false);
            mediaNextBtn.setEnabled(false);
        } else if (intent.getBooleanExtra(EXTRA_IS_TV_ITEM, false)) {
            tvItem = (TVItem) intent.getSerializableExtra(EXTRA_MEDIA_TV_ITEM);
            dataUri = Uri.parse(tvItem.getResUrl());
            videoView.setVideoURI(dataUri);
            //因为资源只有一个，因此不需要上一个和下一个按钮
            mediaPrevBtn.setEnabled(false);
            mediaNextBtn.setEnabled(false);
        } else {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                mediaPosition = bundle.getInt(EXTRA_MEDIA_POSITION);
                videoItems = (ArrayList<LocalMediaItem>) bundle.getSerializable(EXTRA_MEDIA_DATA);
                LocalMediaItem mediaItem = videoItems.get(mediaPosition);
                if (mediaItem != null) {
                    videoView.setVideoPath(mediaItem.getData());
                }
            } else {
                Toast.makeText(this, "播放数据读取失败", Toast.LENGTH_SHORT).show();
            }
        }
        checkUri();
    }

    /**
     * 检查uri是否为网络视频资源
     */
    private void checkUri() {
        if (dataUri != null) {
            String url = dataUri.toString().toLowerCase();
            if (url.startsWith("http")   //http协议
                    || url.startsWith("rtsp")//实时流媒体传输控制协议
                    || url.startsWith("rtmp")//实时消息传输协议
                    || url.startsWith("mms")) {//MultiMediaServer多媒体服务协议
                isNetResource = true;
            }
        } else {
            isNetResource = false;
        }
    }

    private void findView() {
        videoView = (VideoView) findViewById(R.id.videoView);
        mediaRootView = (RelativeLayout) findViewById(R.id.media_rootView);
        mediaName = (TextView) findViewById(R.id.media_name);
        mediaSysPowerText = (TextView) findViewById(R.id.media_sys_power_text);
        mediaSysPowerImage = (ImageView) findViewById(R.id.media_sys_power_image);
        mediaSysTime = (TextView) findViewById(R.id.media_sys_time);
        mediaSoundBtn = (ImageButton) findViewById(R.id.media_sound_btn);
        mediaSoundSeekbar = (SeekBar) findViewById(R.id.media_sound_seekbar);
        mediaSwitchPlayer = (ImageButton) findViewById(R.id.media_switch_player);
        mediaCurrTime = (TextView) findViewById(R.id.media_curr_time);
        mediaCurrTimeSeekbar = (SeekBar) findViewById(R.id.media_curr_time_seekbar);
        mediaDuration = (TextView) findViewById(R.id.media_duration);
        mediaReturnBtn = (ImageButton) findViewById(R.id.media_return_btn);
        mediaPrevBtn = (ImageButton) findViewById(R.id.media_prev_btn);
        mediaPauseBtn = (ImageButton) findViewById(R.id.media_pause_btn);
        mediaNextBtn = (ImageButton) findViewById(R.id.media_next_btn);
        mediaResizeBtn = (ImageButton) findViewById(R.id.media_resize_btn);
        mediaLoadingMask = (RelativeLayout) findViewById(R.id.media_loading_mask);
        mediaLoadingSpeed = (TextView) findViewById(R.id.media_loading_speed);

        videoListener = new VideoStateListener();
        videoView.setOnPreparedListener(videoListener);//添加视频加载完成的监听
        videoView.setOnErrorListener(videoListener);//添加视频错误的监听
        videoView.setOnCompletionListener(videoListener);//添加视频播放完成的监听
        videoView.setOnInfoListener(videoListener);//添加视频播放信息的监听来处理卡顿的情况

        //媒体播放控制控件的监听器
        mediaController = new MediaController();

        mediaSoundBtn.setOnClickListener(mediaController);
        mediaSwitchPlayer.setOnClickListener(mediaController);
        mediaReturnBtn.setOnClickListener(mediaController);
        mediaPrevBtn.setOnClickListener(mediaController);
        mediaPauseBtn.setOnClickListener(mediaController);
        mediaNextBtn.setOnClickListener(mediaController);
        mediaResizeBtn.setOnClickListener(mediaController);

        mediaSoundSeekbar.setOnSeekBarChangeListener(mediaController);
        mediaCurrTimeSeekbar.setOnSeekBarChangeListener(mediaController);
    }

    private void initViewData() {
        //设置播放的媒体名称
        if (dataUri != null) {
            mediaName.setText(dataUri.toString());
        } else {
            mediaName.setText(getMediaItem().getFileName());
        }

        //只有视频准备好了，才能获取视频的时间信息
        if (isVideoPrepared) {
            loadMediaInfo();
        }

        //获取音频信息
        int maxVolumn = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        int currentVolumn = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        //设置声音进度条
        mediaSoundSeekbar.setMax(maxVolumn);
        mediaSoundSeekbar.setProgress(currentVolumn);
        this.setSoundIcon(currentVolumn);

        //设置控制面板刷新
        handler.removeMessages(MSG_REFRESH);
        handler.sendEmptyMessageDelayed(MSG_REFRESH, 1000);
        handler.sendEmptyMessageDelayed(MSG_HIDE, HIDE_DURATION);
    }

    private long lastBackTime;

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            long currTime = System.currentTimeMillis();
            if (currTime - lastBackTime > 2000) {
                Toast.makeText(this, "再按一次退出播放", Toast.LENGTH_SHORT).show();
                lastBackTime = currTime;
            } else {
                //两秒内连按两次返回键，退出播放界面
                closeSysPlayer();
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private class VideoStateListener implements MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener, MediaPlayer.OnInfoListener {

        /**
         * 视频加载完毕
         *
         * @param mp
         */
        @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
        @Override
        public void onPrepared(MediaPlayer mp) {
            isVideoPrepared = true;
            //设置背景颜色为透明
            mediaLoadingMask.setBackgroundColor(0x00000000);
            mediaLoadingMask.setVisibility(View.GONE);
            loadMediaInfo();
            mp.start();
        }

        @Override
        public boolean onError(MediaPlayer mp, int what, int extra) {
            //系统播放器无法播放，调用Vitamio万能解码播放器播放
            startVitamio();
            closeSysPlayer();
            return true;
        }

        @Override
        public void onCompletion(MediaPlayer mp) {
            //视频播放完成后，按钮改变状态
            mediaPauseBtn.setImageResource(R.drawable.media_btn_continue_selector);
        }

        @Override
        public boolean onInfo(MediaPlayer mp, int what, int extra) {
            switch (what) {
                case MediaPlayer.MEDIA_INFO_BUFFERING_START:
                    //播放卡顿，开始缓冲，显示缓冲进度条
                    mediaLoadingMask.setVisibility(View.VISIBLE);
                    break;
                case MediaPlayer.MEDIA_INFO_BUFFERING_END:
                    //卡顿结束，重新恢复播放，隐藏缓冲进度条
                    mediaLoadingMask.setVisibility(View.GONE);
                    break;
            }
            return true;
        }
    }


    //是否启动了Vitamio播放器，便于后面销毁该界面是恢复音乐
    //如果启动了，就不恢复音乐，没启动就恢复音乐
    private boolean isStartVitamio;

    private void startVitamio() {
        Intent intent = new Intent(this, VitamioPlayerActivity.class);
        if (videoItems != null && videoItems.size() > 0) {
            intent.putExtra(VitamioPlayerActivity.EXTRA_MEDIA_POSITION, mediaPosition);
            intent.putExtra(VitamioPlayerActivity.EXTRA_MEDIA_DATA, videoItems);
        } else if (tvItem != null) {
            intent.putExtra(VitamioPlayerActivity.EXTRA_IS_TV_ITEM, true);
            intent.putExtra(VitamioPlayerActivity.EXTRA_MEDIA_TV_ITEM, tvItem);
        } else if (dataUri != null) {
            intent.setData(dataUri);
        }
        this.startActivity(intent);
        isStartVitamio = true;
    }

    /**
     * 关闭系统播放器
     */
    private void closeSysPlayer() {
        videoView.stopPlayback();
        handler.removeCallbacksAndMessages(null);
        SysMediaPlayerActivity.this.finish();
    }

    private void loadMediaInfo() {
        //设置播放进度条
        mediaCurrTimeSeekbar.setMax(videoView.getDuration());
        mediaCurrTimeSeekbar.setProgress(0);
        //设置进度文本
        mediaDuration.setText(TimeUtil.formatMilliSecond(videoView.getDuration()));
        mediaCurrTime.setText(TimeUtil.formatMilliSecond(0));
    }

    private void setSoundIcon(int currentVolumn) {
        int maxVolumn = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        //设置声音按钮图标
        if (currentVolumn <= 0) {
            //静音
            mediaSoundBtn.setImageResource(R.drawable.sound_mute_selector);
        } else if (currentVolumn <= maxVolumn / 3) {
            //小音量
            mediaSoundBtn.setImageResource(R.drawable.sound_small_selector);
        } else if (currentVolumn <= 2 * maxVolumn / 3) {
            //中音量
            mediaSoundBtn.setImageResource(R.drawable.sound_medium_selector);
        } else {
            //大音量
            mediaSoundBtn.setImageResource(R.drawable.sound_large_selector);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (mediaRootView.isShown()) {
                    //媒体控制面板已经显示了就让它隐藏
                    mediaRootView.setVisibility(View.GONE);
                } else {
                    //没有显示就让它显示
                    mediaRootView.setVisibility(View.VISIBLE);
                    //一定时间后，自动隐藏
                    handler.sendEmptyMessageDelayed(MSG_HIDE, HIDE_DURATION);
                }
                break;
        }
        return true;
    }

    private class MediaController implements View.OnClickListener, SeekBar.OnSeekBarChangeListener {
        //android.widget.MediaController
        /**
         * 是否按下了静音按钮
         */
        private boolean isMute = false;
        /**
         * 静音前的音量
         */
        private int lastVolumn;

        @Override
        public void onClick(View v) {
            if (v == mediaSoundBtn) {
                if (!isMute) {
                    //如果之前没有按下静音按钮按下声音按钮后静音
                    lastVolumn = mediaSoundSeekbar.getProgress();
                    mediaSoundSeekbar.setProgress(0);
                    isMute = true;
                } else {
                    //已经按下了静音按钮，则还原静音前的音量
                    mediaSoundSeekbar.setProgress(lastVolumn);
                    isMute = false;
                }
            } else if (v == mediaSwitchPlayer) {

            } else if (v == mediaReturnBtn) {
                //结束播放并返回
                closeSysPlayer();
            } else if (v == mediaPrevBtn) {
                mediaLoadingMask.setVisibility(View.VISIBLE);
                mediaPosition--;
                if (mediaPosition < 0) {
                    //越界判断
                    mediaPosition = videoItems.size() - 1;
                }
                videoView.stopPlayback();
                videoView.setVideoPath(getMediaItem().getData());
            } else if (v == mediaPauseBtn) {
                if (videoView.isPlaying()) {
                    //正在播放，则将视频暂停
                    videoView.pause();
                    mediaPauseBtn.setImageResource(R.drawable.media_btn_continue_selector);
                } else {
                    //已经暂停，则将视频继续播放
                    videoView.start();
                    mediaPauseBtn.setImageResource(R.drawable.media_btn_pause_normal);
                }
            } else if (v == mediaNextBtn) {
                mediaLoadingMask.setVisibility(View.VISIBLE);
                mediaPosition++;
                if (mediaPosition >= videoItems.size()) {
                    mediaPosition = 0;
                }
                videoView.stopPlayback();
                videoView.setVideoPath(getMediaItem().getData());
            } else if (v == mediaResizeBtn) {

            }
            //用户对按钮操作后，一定时间内自动隐藏控制面板
            handler.removeMessages(MSG_HIDE);
            handler.sendEmptyMessageDelayed(MSG_HIDE, HIDE_DURATION);
        }

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (seekBar == mediaSoundSeekbar) {
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, progress, 0);
                setSoundIcon(progress);
                isMute = false;
            } else if (seekBar == mediaCurrTimeSeekbar) {
                if (fromUser) {
                    //是用户改变了进度条的值
                    videoView.seekTo(progress);
                }
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            //用户正在拖动进度条时，不能隐藏控制面板
            handler.removeMessages(MSG_HIDE);
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            //用户停止拖动进度条，用户误操作后，一定时间内自动隐藏控制面板
            handler.sendEmptyMessageDelayed(MSG_HIDE, HIDE_DURATION);
        }
    }

    private class SysPowerReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case Intent.ACTION_BATTERY_CHANGED:
                case Intent.ACTION_BATTERY_LOW:
                case Intent.ACTION_BATTERY_OKAY:
                    LayerDrawable layer = (LayerDrawable) context.getResources().getDrawable(R.drawable.media_sys_power);
                    ClipDrawable clip = (ClipDrawable) layer.findDrawableByLayerId(R.id.media_sys_power_level);
                    int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);//当前电量
                    int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, 100);//最大电量
                    clip.setLevel(10000 * level / scale);
                    mediaSysPowerImage.setImageDrawable(layer);
                    mediaSysPowerText.setText(100 * level / scale + "%");
                    break;
            }
        }
    }
}