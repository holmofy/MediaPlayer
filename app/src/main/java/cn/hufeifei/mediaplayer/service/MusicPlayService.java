package cn.hufeifei.mediaplayer.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;
import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.image.ImageOptions;
import org.xutils.x;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import java.util.List;

import cn.hufeifei.mediaplayer.App;
import cn.hufeifei.mediaplayer.R;
import cn.hufeifei.mediaplayer.activity.MusicPlayerActivity;
import cn.hufeifei.mediaplayer.domain.LocalMusicInfo;
import cn.hufeifei.mediaplayer.domain.NetMusicBriefInfo;
import cn.hufeifei.mediaplayer.domain.NetMusicItem;
import cn.hufeifei.mediaplayer.event.MusicServiceEvent;
import cn.hufeifei.mediaplayer.utils.Constant;
import cn.hufeifei.mediaplayer.utils.PreferencesUtil;
import cn.hufeifei.mediaplayer.utils.RequestUtil;
import cn.hufeifei.mediaplayer.widget.NotificationController;


/**
 * 音乐后台播放服务
 * Created by Holmofy on 2016/11/24.
 */

public class MusicPlayService extends Service {
    /**
     * 播放模式
     */
    public static final int PLAY_MODE_REPEAT_ALL = 0x0001;//列表循环
    public static final int PLAY_MODE_REPEAT_SINGLE = 0x0002;//单曲循环
    public static final int PLAY_MODE_RANDOM = 0x0003;//随机播放

    /**
     * 服务停止后将这次的列表保存到文件中，便于下次进入未选择歌曲就进入该服务时，有歌曲列表
     */
    private static final String LOCAL_MUSIC_LIST_FILE_NAME = "localMusic.info";
    private static final String NET_MUSIC_LIST_FILE_NAME = "netMusic.info";

    //保存状态信息到缓存文件中
    private static final String MEDIA_POSITION = "media_position";
    private static final String IS_FROM_NET = "is_from_net";

    private File getNetFile() throws IOException {
        File netFile = new File(Constant.getMusicInfoDir(this), NET_MUSIC_LIST_FILE_NAME);
        if (!netFile.exists()) {
            netFile.createNewFile();
        }
        return netFile;
    }

    private File getLocalFile() throws IOException {
        File localFile = new File(Constant.getMusicInfoDir(this), LOCAL_MUSIC_LIST_FILE_NAME);
        if (!localFile.exists()) {
            localFile.createNewFile();
        }
        return localFile;
    }

    private void saveMusicList() {
        if (netMusicInfos != null && netMusicInfos.size() != 0) {
            ObjectOutputStream netOut = null;
            try {
                netOut = new ObjectOutputStream(new FileOutputStream(getNetFile()));
                netOut.writeObject(netMusicInfos);
                netOut.flush();
            } catch (Exception e) {
            } finally {
                if (netOut != null) {
                    try {
                        netOut.close();
                    } catch (IOException e) {
                    }
                }
            }
        }
        if (localMusicInfos != null && localMusicInfos.size() != 0) {
            ObjectOutputStream localOut = null;
            try {
                localOut = new ObjectOutputStream(new FileOutputStream(getLocalFile()));
                localOut.writeObject(localMusicInfos);
                localOut.flush();
            } catch (Exception e) {
            } finally {
                if (localOut != null) {
                    try {
                        localOut.close();
                    } catch (IOException e) {
                    }
                }
            }
        }
        PreferencesUtil.getCache().putInt(this, MEDIA_POSITION, mediaPosition);
        PreferencesUtil.getCache().putBoolean(this, IS_FROM_NET, isFromNet);
    }

    private void loadMusicList() {
        ObjectInputStream netInput = null;
        try {
            File netFile = getNetFile();
            netInput = new ObjectInputStream(new FileInputStream(netFile));
            netMusicInfos = (List<NetMusicBriefInfo>) netInput.readObject();
        } catch (Exception e) {
        } finally {
            if (netInput != null) {
                try {
                    netInput.close();
                } catch (IOException e) {
                }
            }
        }
        ObjectInputStream localInput = null;
        try {
            File localFile = getLocalFile();
            localInput = new ObjectInputStream(new FileInputStream(localFile));
            localMusicInfos = (List<LocalMusicInfo>) localInput.readObject();
        } catch (Exception e) {
        } finally {
            if (localInput != null) {
                try {
                    localInput.close();
                } catch (IOException e) {
                }
            }
        }
        mediaPosition = PreferencesUtil.getCache().getInt(this, MEDIA_POSITION, 0);
        isFromNet = PreferencesUtil.getCache().getBoolean(this, IS_FROM_NET, false);
        if ((localMusicInfos != null && localMusicInfos.size() != 0)//本地音乐列表
                || (netMusicInfos != null && netMusicInfos.size() != 0)) {
            if (isFromNet) {
                requestNetData(getCurrNetMusicItem().getSongId());
            } else {
                setDataSource(getCurrLocalMusicItem().getData());
            }
        }
    }


    /**
     * 当前的播放模式
     * 默认为-1,以便检测是否为第一次加载服务
     * 如果是第一次加载，则需要从配置文件中加载播放模式
     */
    private int playMode = -1;

    public int getPlayMode() {
        if (playMode < 0) {
            //说明服务可能是第一次加载(服务初始化播放值为-1)
            //那么需要从从配置文件中获取
            int value = PreferencesUtil.getPreferences().getInt(this, "playMode", -1);
            if (value < 0) {
                //说明之前在配置文件中未存储过播放模式
                playMode = PLAY_MODE_REPEAT_ALL;
            } else {
                playMode = value;
            }
        }
        return playMode;
    }

    public void setPlayMode(int playMode) {
        if (0x0001 <= playMode && playMode <= 0x0003) {
            this.playMode = playMode;
            PreferencesUtil.getPreferences().putInt(this, "playMode", playMode);
        }
    }

    ////////////////////////////////////////////////////////////
    private LocalBinder binder = new LocalBinder();

    /**
     * 创建公共Binder使得外部可以通过getService来获取后台服务
     * 从而完全操控服务
     */
    public class LocalBinder extends Binder {
        public MusicPlayService getService() {
            //返回后台服务的实例
            return MusicPlayService.this;
        }
    }

    ////////////////////////////////////////////////////////////
    /**
     * 用于音乐播放的媒体播放器
     */
    private MediaPlayer mediaPlayer;

    /**
     * 监听媒体播放
     */
    private MediaListener listener;

    public boolean isPrepared() {
        return isPrepared;
    }

    /**
     * 音乐是否准备好了
     */
    private boolean isPrepared;

    /**
     * 当传入媒体播放列表时，当前需要播放的媒体数据的位置
     * 因为在初始化的时候需要比较用户选择列表项的位置，用户可能选择的列表项位置为0
     * 初始值不能为0，所以设置为负数
     */
    private int mediaPosition = -1;
    /**
     * 本地音乐数据
     */
    private List<LocalMusicInfo> localMusicInfos;

    /**
     * 网络音乐数据
     */
    private List<NetMusicBriefInfo> netMusicInfos;

    /**
     * 网络请求后返回的数据
     */
    private NetMusicItem netMusicItem;

    /**
     * 数据是否来自网络
     */
    private boolean isFromNet;

    /**
     * 获取当前位置的媒体项
     *
     * @return 当前位置的媒体
     */
    private LocalMusicInfo getCurrLocalMusicItem() {
        return localMusicInfos.get(mediaPosition);
    }

    /**
     * 任务栏提示
     */
    private NotificationController notificationController;

    @Override
    public void onCreate() {
        ((App) getApplication()).setMusicService(this);
        listener = new MediaListener();
        loadMusicList();//这一步必须在创建监听器之后，因为这一步里面可能会用到监听器
        //服务一旦创建，就在任务栏显示播放音乐的图标
        notificationController = new NotificationController(this);
        notificationController.showNotification();
        registerNotificationReceiver();
    }

    /**
     * Notification广播接收器的注册
     */
    private NotificationBroadcastReceiver receiver;

    private void registerNotificationReceiver() {
        receiver = new NotificationBroadcastReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(NotificationController.ACTION_MUSIC_CLOSE_SERVICE);
        intentFilter.addAction(NotificationController.ACTION_MUSIC_NEXT);
        intentFilter.addAction(NotificationController.ACTION_MUSIC_PLAY_OR_PAUSE);
        registerReceiver(receiver, intentFilter);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return true;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        getIntentData(intent);
        return super.onStartCommand(intent, flags, startId);
    }

    /**
     * 获取Intent中的数据
     */
    private void getIntentData(Intent intent) {
        if (intent.getBooleanExtra(NotificationController.EXTRA_NOTIFICATION_KEY, false)) {
            //如果是点击任务栏提示进入的播放界面，此时没有传入任何数据，直接返回
            return;
        }
        Uri uriDataSource = null;//将要设置的媒体数据源
        boolean newIsFromNet = intent.getBooleanExtra(MusicPlayerActivity.EXTRA_FROM_NET, false);
        if (!newIsFromNet) {
            //数据不是来源于网络
            uriDataSource = getLocalMusicData(intent, newIsFromNet);
        } else {
            //来源于网络,则直接加载网络数据，加载时该方法会重新设置播放状态
            LoadNetMusicData(intent, newIsFromNet);
            return;
        }
        if (mediaPlayer == null || uriDataSource != null) {
            //原本媒体播放器值为空，说明是第一次启动服务当前没有播放媒体
            //uriDataSource不为空说明前面检测到这次数据源与上一次的数据源不相同，需要重新更改
            setDataSource(uriDataSource);
        }
    }

    private void LoadNetMusicData(Intent intent, boolean newIsFromNet) {
        //如果用户是第二次进入该界面，记录之前的媒体数据与上一次媒体数据进行比较，看是否重新设置媒体数据源
        //这一次新的数据源，与新的媒体项位置
        List<NetMusicBriefInfo> newNetMusicInfos = (List<NetMusicBriefInfo>) intent.getSerializableExtra(MusicPlayerActivity.EXTRA_MEDIA_DATA);
        int newPosition = intent.getIntExtra(MusicPlayerActivity.EXTRA_MEDIA_POSITION, 0);
        if (newNetMusicInfos != null) {
            //新数据源不为空才执行操作
            int newId = newNetMusicInfos.get(newPosition).getSongId();
            if (netMusicInfos == null || newIsFromNet != isFromNet) {
                isFromNet = newIsFromNet;
                //原来数据列表项为空或者原来的与新数据源不是同一个，则直接赋值
                requestNetData(newId);//异步请求数据，数据请求成功后自动设置到MediaPlayer中
                netMusicInfos = newNetMusicInfos;
                this.mediaPosition = newPosition;
            } else {
                //不为空，则进行对比
                int lastId = getCurrNetMusicItem().getSongId();
                if (lastId != newId) {
                    //说明用户这次选择的媒体与上次选择则不一样，所以需要重新设置数据源
                    //可能第二次进来，用户点击的列表项与上次不同，则这次需要重新播放该资源
                    requestNetData(newId);
                    this.netMusicInfos = newNetMusicInfos;
                    this.mediaPosition = newPosition;
                }
            }
        }
    }

    /**
     * 根据歌曲ID向服务器请求数据
     *
     * @param songId 歌曲ID
     */
    private void requestNetData(int songId) {
        RequestUtil.playMusic(songId, new RequestUtil.CallBack<NetMusicItem>() {
            @Override
            public void onSuccess(NetMusicItem obj) {
                netMusicItem = obj;
                setDataSource(netMusicItem.getFileLink());
            }

            //请求失败
            @Override
            public void onError(String errorInfo) {
                Toast.makeText(MusicPlayService.this, "歌曲请求失败", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private NetMusicBriefInfo getCurrNetMusicItem() {
        return netMusicInfos.get(mediaPosition);
    }

    /**
     * 获取Intent中的本地音乐数据
     *
     * @param intent 意图
     * @return 播放Uri
     */
    private Uri getLocalMusicData(Intent intent, boolean newIsFromNet) {
        Uri uriDataSource = null;//将要设置的媒体数据源
        //如果用户是第二次进入该界面，记录之前的媒体数据与上一次媒体数据进行比较，看是否重新设置媒体数据源
        //这一次新的数据源，与新的媒体项位置
        List<LocalMusicInfo> newLocalMusicInfos = (List<LocalMusicInfo>) intent.getSerializableExtra(MusicPlayerActivity.EXTRA_MEDIA_DATA);
        int newMediaPosition = intent.getIntExtra(MusicPlayerActivity.EXTRA_MEDIA_POSITION, 0);
        if (newLocalMusicInfos != null) {
            //新数据源不为空才执行操作
            String newData = newLocalMusicInfos.get(newMediaPosition).getData();
            if (localMusicInfos == null || newIsFromNet != isFromNet) {
                isFromNet = newIsFromNet;
                //原来数据列表项为空，则直接赋值
                uriDataSource = Uri.parse(newData);
                this.localMusicInfos = newLocalMusicInfos;
                this.mediaPosition = newMediaPosition;
            } else {
                //不为空，则进行对比
                String lastData = getCurrLocalMusicItem().getData();
                if (!lastData.equals(newData)) {
                    //说明用户这次选择的媒体与上次选择则不一样，所以需要重新设置数据源
                    //可能第二次进来，用户点击的列表项与上次不同，则这次需要重新播放该资源
                    uriDataSource = Uri.parse(newData);
                    this.localMusicInfos = newLocalMusicInfos;
                    this.mediaPosition = newMediaPosition;
                } else {
                    //否则说明用户这次选择的数据源与上次相同，则不用替换
                    uriDataSource = null;
                }
            }
        }
        return uriDataSource;
    }

    /**
     * 设置数据源
     *
     * @param dataSource 字符串形式的数据源
     */
    private void setDataSource(String dataSource) {
        setDataSource(Uri.parse(dataSource));
    }

    /**
     * 设置播放器的数据源
     *
     * @param dataSource 数据源uri
     */
    private void setDataSource(Uri dataSource) {
        try {
            if (mediaPlayer != null) {
                //如果原来的不为空，则释放资源，重新设置数据源
                mediaPlayer.stop();
                mediaPlayer.release();
                mediaPlayer = null;
            }
            //新建一个播放器，并设置监听
            mediaPlayer = new MediaPlayer();
            isPrepared = false;
            mediaPlayer.setDataSource(this, dataSource);
            mediaPlayer.setOnPreparedListener(listener);
            mediaPlayer.setOnCompletionListener(listener);
            mediaPlayer.setOnSeekCompleteListener(listener);
            mediaPlayer.setOnErrorListener(listener);
            mediaPlayer.setOnInfoListener(listener);
            mediaPlayer.prepareAsync();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "音频播放失败，请检查音频文件是否损坏", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDestroy() {
        //保存歌单
        saveMusicList();
        Log.e("Service", "onDestroy() called");
        //服务结束后关闭任务栏消息提示
        notificationController.cancelNotification();
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
        if (receiver != null) {
            unregisterReceiver(receiver);
        }
        super.onDestroy();
        ((App) getApplication()).setMusicService(null);
    }

    private class MediaListener implements MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener, MediaPlayer.OnSeekCompleteListener, MediaPlayer.OnErrorListener, MediaPlayer.OnInfoListener {

        @Override
        public void onPrepared(MediaPlayer mp) {
            //播放准备完毕
            isPrepared = true;
            EventBus bus = EventBus.getDefault();
            bus.post(new MusicServiceEvent(MusicPlayService.this, MusicServiceEvent.ACTION_MUSIC_PREPARED));
            start();
        }

        @Override
        public void onCompletion(MediaPlayer mp) {
            if (getPlayMode() == PLAY_MODE_REPEAT_SINGLE) {
                //单曲循环的，重新定位到0位置,并继续播放
                mp.seekTo(0);
                mp.start();
            } else {
                //歌曲播放完成自动播放下一曲
                next();
            }
        }

        @Override
        public void onSeekComplete(MediaPlayer mp) {
            //拖动到指定位置完成
        }

        @Override
        public boolean onError(MediaPlayer mp, int what, int extra) {
            Toast.makeText(MusicPlayService.this, "播放错误", Toast.LENGTH_SHORT).show();
            return true;
        }

        @Override
        public boolean onInfo(MediaPlayer mp, int what, int extra) {
            return false;
        }

    }

    /**
     * 获取当前歌曲的标题
     *
     * @return 音乐标题
     */
    public String getMusicTitle() {
        if (!isFromNet) {
            if (localMusicInfos != null) {
                return getCurrLocalMusicItem().getTitle();
            }
        } else if (netMusicItem != null) {
            //数据来自网络，并且网络数据已经获取成功
            return netMusicItem.getTitle();
        }
        return "";
    }

    /**
     * 获取当前歌曲的作者/演唱者
     *
     * @return 歌曲演唱者
     */
    public String getArtist() {
        if (!isFromNet) {
            if (localMusicInfos != null) {
                return getCurrLocalMusicItem().getArtist();
            }
        } else if (netMusicItem != null) {
            return netMusicItem.getAuthor();
        }
        return "";
    }

    /**
     * 开始或继续播放
     */
    public void start() {
        if (mediaPlayer != null && isPrepared) {
            mediaPlayer.start();
            String title, artist;
            if (isFromNet && netMusicItem != null) {
                title = netMusicItem.getTitle();
                artist = netMusicItem.getAuthor();
            } else {
                title = getCurrLocalMusicItem().getTitle();
                artist = getCurrLocalMusicItem().getArtist();
            }
            notificationController.setMusicTitle(title);
            notificationController.setMusicArtist(artist);
            notificationController.setContinueOrPauseImg(R.drawable.media_btn_pause_selector);
            notificationController.showNotification();
        }
        EventBus.getDefault().post(new MusicServiceEvent(this, MusicServiceEvent.ACTION_MUSIC_START));
    }

    /**
     * 暂停音乐
     */
    public void pause() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            notificationController.setContinueOrPauseImg(R.drawable.media_btn_continue_selector);
            notificationController.showNotification();
        }
        EventBus.getDefault().post(new MusicServiceEvent(this, MusicServiceEvent.ACTION_MUSIC_PAUSE));
    }

    /**
     * 获取当前播放的音乐的时长
     *
     * @return 时长
     */
    public int getDuration() {
        if (mediaPlayer != null && isPrepared) {
            return mediaPlayer.getDuration();
        }
        return -1;
    }

    /**
     * 得到当前音乐播放的位置
     *
     * @return 当前音乐播放的位置
     */
    public int getCurrentPosition() {
        if (mediaPlayer != null && isPrepared) {
            return mediaPlayer.getCurrentPosition();
        }
        return -1;
    }

    /**
     * 将音乐拖动到mesc毫秒的位置
     *
     * @param msec 指定拖动到多少毫秒
     */
    public void seekTo(int msec) {
        if (mediaPlayer != null && isPrepared) {
            mediaPlayer.seekTo(msec);
        }
    }

    /**
     * 音乐当前是否在播放中
     *
     * @return true表示正在播放中，false表示音乐已暂停或停止
     */
    public boolean isPlaying() {
        return mediaPlayer != null && mediaPlayer.isPlaying();
    }

    /**
     * 上一曲
     */
    public void prev() {
        int size;
        if (!isFromNet) {
            //本地音乐
            size = localMusicInfos.size();
        } else {
            size = netMusicInfos.size();
        }
        //只有从列表中进来的才能播放上一首,网络资源只能播放一次
        switch (getPlayMode()) {
            case PLAY_MODE_REPEAT_ALL:
            case PLAY_MODE_REPEAT_SINGLE:
                //对于列表循环和单曲循环上一曲为列表的上一项
                mediaPosition--;
                if (mediaPosition < 0) {
                    //越界检测
                    mediaPosition = size - 1;
                }
                break;
            case PLAY_MODE_RANDOM:
                //随机上一曲
                mediaPosition -= Math.random() * size;
                if (mediaPosition < 0) {
                    //越界检测
                    mediaPosition = size - mediaPosition;
                }
                break;
        }
        //重新设置数据源
        if (!isFromNet) {
            //本地音乐直接设置新的数据源
            setDataSource(getCurrLocalMusicItem().getData());
        } else {
            //网络音乐重新请求音乐
            requestNetData(getCurrNetMusicItem().getSongId());
        }
        EventBus.getDefault().post(new MusicServiceEvent(this, MusicServiceEvent.ACTION_MUSIC_ITEM_CHANGED));
    }

    /**
     * 下一曲
     */
    public void next() {
        int size = 0;
        if (!isFromNet) {
            size = localMusicInfos.size();
        } else {
            size = netMusicInfos.size();
        }
        //只有从列表中进来的才能播放下一首，网络资源只能播放一次
        switch (getPlayMode()) {
            case PLAY_MODE_REPEAT_ALL:
            case PLAY_MODE_REPEAT_SINGLE:
                //对于列表循环和单曲循环上一曲为列表的上一项
                mediaPosition++;
                if (mediaPosition > size - 1) {
                    //越界检测
                    mediaPosition = 0;
                }
                break;
            case PLAY_MODE_RANDOM:
                //随机上一曲
                mediaPosition += Math.random() * size;
                //越界检测
                mediaPosition %= size;
                break;
        }
        //重新设置数据源
        if (!isFromNet) {
            //本地数据，直接设置数据源
            setDataSource(getCurrLocalMusicItem().getData());
        } else {
            //网络数据，重新请求数据
            requestNetData(getCurrNetMusicItem().getSongId());
        }
        EventBus.getDefault().post(new MusicServiceEvent(this, MusicServiceEvent.ACTION_MUSIC_ITEM_CHANGED));
    }

    /**
     * 获取音乐的专辑唱片海报图
     *
     * @param callBack 获取后回调执行的内容
     */
    public void getMusicRecordImg(final RequestUtil.CallBack<Drawable> callBack) {
        if (isFromNet && netMusicItem != null) {
            ImageOptions imageOptions = new ImageOptions.Builder()
                    // 加载中或错误图片的ScaleType
                    //.setPlaceholderScaleType(ImageView.ScaleType.MATRIX)
                    // 默认自动适应大小
                    // .setSize(...)
                    .setIgnoreGif(false)
                    // 如果使用本地文件url, 添加这个设置可以在本地文件更新后刷新立即生效.
                    //.setUseMemCache(false)
                    .setImageScaleType(ImageView.ScaleType.CENTER).build();
            x.image().loadDrawable(netMusicItem.getPicPremiumLink(), imageOptions, new Callback.CommonCallback<Drawable>() {

                @Override
                public void onSuccess(Drawable result) {
                    callBack.onSuccess(result);
                    //将获取到的唱片图片也设置到任务栏提示中
                    notificationController.setNotifyImg((BitmapDrawable) result);
                    notificationController.showNotification();
                }

                @Override
                public void onError(Throwable ex, boolean isOnCallback) {
                    callBack.onError(ex.getMessage());
                    notificationController.setNotifyImg(R.drawable.music_record_img);
                    notificationController.showNotification();
                }

                @Override
                public void onCancelled(CancelledException cex) {
                }

                @Override
                public void onFinished() {
                }
            });
        } else {
            callBack.onError("本地音乐，暂未处理");
            notificationController.setNotifyImg(R.drawable.music_record_img);
            notificationController.showNotification();
        }
    }

    /**
     * 获取歌词文件，如果本地没有，则从网络下载
     *
     * @param callback 获取歌词后回调
     */
    public void getLyricFile(RequestUtil.CallBack<File> callback) {
        String title = getMusicTitle();
        String lrcFileName = title + ".lrc";//歌词文件名
        //获取歌词文件夹
        File lyricDir = Constant.getLyricDir();
        String[] lyrics = lyricDir.list();
        if (Arrays.asList(lyrics).contains(lrcFileName)) {
            //有歌曲同名的歌词文件
            callback.onSuccess(new File(lyricDir, lrcFileName));
        } else {
            //没有同名文件则从网络下载歌词
            if (isFromNet && netMusicItem != null) {
                //如果歌曲本来就是来自网络的
                lrcFileName = netMusicItem.getSongId() + ".lrc";//网络歌词名
                File lrcFile = new File(lyricDir, lrcFileName);
                if (lrcFile.exists()) {
                    //文件夹中已经包含从网络下载的歌词，则返回该文件
                    callback.onSuccess(lrcFile);
                    return;
                }
                downloadFile(netMusicItem.getLrcLink(), lrcFile.getAbsolutePath(), callback);
            } else {
                //对于本地音乐没有歌词的情况，网络查询歌曲信息后再请求歌词
                requestLyric();
            }
        }
    }

    private void requestLyric() {

    }

    private void downloadFile(String fileUrl, String localPath, final RequestUtil.CallBack<File> callBack) {
        RequestParams params = new RequestParams(fileUrl);
        params.setSaveFilePath(localPath);
        x.http().get(params, new Callback.CommonCallback<File>() {

            @Override
            public void onSuccess(File result) {
                callBack.onSuccess(result);
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                Toast.makeText(MusicPlayService.this, "歌词下载失败\n请检查网络连接", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(CancelledException cex) {

            }

            @Override
            public void onFinished() {

            }
        });
    }

    public class NotificationBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case NotificationController.ACTION_MUSIC_PLAY_OR_PAUSE:
                    Log.i("receiver", "onReceive: ACTION_MUSIC_PLAY_OR_PAUSE");
                    //播放或暂停广播
                    if (MusicPlayService.this.isPlaying()) {
                        MusicPlayService.this.pause();
                    } else {
                        MusicPlayService.this.start();
                    }
                    break;
                case NotificationController.ACTION_MUSIC_NEXT:
                    Log.i("receiver", "onReceive: ACTION_MUSIC_NEXT");
                    MusicPlayService.this.next();
                    break;
                case NotificationController.ACTION_MUSIC_CLOSE_SERVICE:
                    Log.i("receiver", "onReceive: ACTION_MUSIC_CLOSE_SERVICE");
                    MusicPlayService.this.stopSelf();
                    break;
            }
        }
    }
}