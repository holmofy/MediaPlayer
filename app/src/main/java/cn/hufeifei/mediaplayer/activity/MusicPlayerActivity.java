package cn.hufeifei.mediaplayer.activity;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.Event;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

import java.util.ArrayList;
import java.util.List;

import cn.hufeifei.mediaplayer.R;
import cn.hufeifei.mediaplayer.event.MusicServiceEvent;
import cn.hufeifei.mediaplayer.fragment.player.MusicLyricFragment;
import cn.hufeifei.mediaplayer.fragment.player.RecordAnimationFragment;
import cn.hufeifei.mediaplayer.fragment.player.SoundEffectFragment;
import cn.hufeifei.mediaplayer.service.MusicPlayService;
import cn.hufeifei.mediaplayer.utils.TimeUtil;

/**
 * 音乐播放界面
 */
@ContentView(R.layout.activity_music_player)
public class MusicPlayerActivity extends Activity implements ServiceConnection {
    /**
     * 用于传入数据
     */
    public static final String EXTRA_MEDIA_DATA = "SerializableMediaData";//可序列化
    public static final String EXTRA_MEDIA_POSITION = "MediaDataPosition";//Int型

    public static final String EXTRA_FROM_NET = "MediaFromNet";//boolean型

    /**
     * 刷新界面的Handler消息
     */
    private static final int MSG_REFRESH = 0x0001;

    @ViewInject(R.id.music_img_bg)
    private ImageView musicImgBg;

    @ViewInject(R.id.music_btn_return)
    private ImageButton musicBtnReturn;

    @ViewInject(R.id.music_title)
    private TextView musicTitle;

    @ViewInject(R.id.music_artist)
    private TextView musicArtist;

    @ViewInject(R.id.music_btn_more)
    private ImageButton musicBtnMore;

    @ViewInject(R.id.music_viewPager)
    private ViewPager musicViewPager;

    @ViewInject(R.id.music_radioGroup)
    private RadioGroup musicRadioGroup;

    @ViewInject(R.id.music_btn_restart)
    private ImageButton musicBtnRestart;

    @ViewInject(R.id.music_btn_prev)
    private ImageButton musicBtnPrev;

    @ViewInject(R.id.music_btn_continue)
    private ImageButton musicBtnContinue;

    @ViewInject(R.id.music_btn_next)
    private ImageButton musicBtnNext;

    @ViewInject(R.id.music_btn_mode)
    private ImageButton musicBtnMode;

    @ViewInject(R.id.music_currProgress)
    private TextView musicCurrProgress;

    @ViewInject(R.id.music_seekBar)
    private SeekBar musicSeekBar;

    @ViewInject(R.id.music_duration)
    private TextView musicDuration;

    /**
     * 服务相关字段
     */
    private MusicPlayService musicService;//后台服务的实例,通过该实例完全掌控后台服务


    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_REFRESH:
                    //设置歌曲当前播放位置
                    int curr = musicService.getCurrentPosition();
                    String currString = TimeUtil.formatMilliSecond(curr);
                    musicCurrProgress.setText(currString);
                    musicSeekBar.setProgress(curr);
                    sendEmptyMessageDelayed(MSG_REFRESH, 1000);
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //使用xUtils注入View
        x.view().inject(this);

        //设置事件监听器
        setListener();

        //使用EventBus来获取后台音乐播放服务的状态改变事件，先注册该类
        EventBus.getDefault().register(this);

        startAndBindService();
    }

    /**
     * 设置歌曲唱片背景图大小
     */
    public void setBgImg(Drawable drawable) {
        BitmapDrawable bd = (BitmapDrawable) drawable;
        Bitmap bmp = bd.getBitmap();
        Display display = getWindowManager().getDefaultDisplay();
        int width = display.getWidth();
        int height = display.getHeight();
        int bmpHeight = bmp.getHeight();
        int bmpWidth = width * bmpHeight / height;
        Bitmap newBmp = Bitmap.createBitmap(bmp, (bmp.getWidth() - bmpWidth) >> 1, 0, bmpWidth, bmpHeight);
        musicImgBg.setImageBitmap(newBmp);
    }


    /**
     * 设置监听器
     */
    private void setListener() {
        //给SeekBar注册监听事件
        SeekBarListener listener = new SeekBarListener();
        musicSeekBar.setOnSeekBarChangeListener(listener);
        //设置子页面
        musicViewPager.setAdapter(new MusicItemPageAdapter(getFragmentManager()));
        MusicPageListener musicPageListener = new MusicPageListener();
        musicViewPager.addOnPageChangeListener(musicPageListener);
        musicRadioGroup.setOnCheckedChangeListener(musicPageListener);
        musicRadioGroup.check(R.id.radioBtn2);
    }

    private class MusicPageListener implements ViewPager.OnPageChangeListener, RadioGroup.OnCheckedChangeListener {

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(int position) {
            switch (position) {
                case 0:
                    musicRadioGroup.check(R.id.radioBtn1);
                    break;
                case 1:
                    musicRadioGroup.check(R.id.radioBtn2);
                    break;
                case 2:
                    musicRadioGroup.check(R.id.radioBtn3);
                    break;
            }
        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }

        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            switch (checkedId) {
                case R.id.radioBtn1:
                    musicViewPager.setCurrentItem(0);
                    break;
                case R.id.radioBtn2:
                    musicViewPager.setCurrentItem(1);
                    break;
                case R.id.radioBtn3:
                    musicViewPager.setCurrentItem(2);
                    break;
            }
        }
    }

    /**
     * 音乐播放三个ViewPager子页面,代码来自于v4包中的FragmentPagerAdapter
     */
    private class MusicItemPageAdapter extends PagerAdapter {
        private static final String TAG = "FragmentPagerAdapter";

        private final FragmentManager mFragmentManager;
        private FragmentTransaction mCurTransaction = null;
        private Fragment mCurrentPrimaryItem = null;

        /**
         * 三个子界面：音效控制，唱片动画，歌词
         */
        private List<Fragment> fragments = new ArrayList<>(3);

        public MusicItemPageAdapter(FragmentManager fm) {
            mFragmentManager = fm;
            fragments.add(new SoundEffectFragment());
            fragments.add(new RecordAnimationFragment());
            fragments.add(new MusicLyricFragment());
        }

        /**
         * Return the Fragment associated with a specified position.
         */
        public Fragment getItem(int position) {
            return fragments.get(position);
        }

        @Override
        public int getCount() {
            return fragments.size();
        }

        @Override
        public void startUpdate(ViewGroup container) {
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            if (mCurTransaction == null) {
                mCurTransaction = mFragmentManager.beginTransaction();
            }

            final long itemId = getItemId(position);

            // Do we already have this fragment?
            String name = makeFragmentName(container.getId(), itemId);
            Fragment fragment = mFragmentManager.findFragmentByTag(name);
            if (fragment != null) {
                mCurTransaction.attach(fragment);
            } else {
                fragment = getItem(position);
                mCurTransaction.add(container.getId(), fragment, makeFragmentName(container.getId(), itemId));
            }
            if (fragment != mCurrentPrimaryItem) {
                fragment.setMenuVisibility(false);
                fragment.setUserVisibleHint(false);
            }

            return fragment;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            if (mCurTransaction == null) {
                mCurTransaction = mFragmentManager.beginTransaction();
            }
            mCurTransaction.detach((Fragment) object);
        }

        @Override
        public void setPrimaryItem(ViewGroup container, int position, Object object) {
            Fragment fragment = (Fragment) object;
            if (fragment != mCurrentPrimaryItem) {
                if (mCurrentPrimaryItem != null) {
                    mCurrentPrimaryItem.setMenuVisibility(false);
                    mCurrentPrimaryItem.setUserVisibleHint(false);
                }
                if (fragment != null) {
                    fragment.setMenuVisibility(true);
                    fragment.setUserVisibleHint(true);
                }
                mCurrentPrimaryItem = fragment;
            }
        }

        @Override
        public void finishUpdate(ViewGroup container) {
            if (mCurTransaction != null) {
                mCurTransaction.commitAllowingStateLoss();
                mCurTransaction = null;
                mFragmentManager.executePendingTransactions();
            }
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return ((Fragment) object).getView() == view;
        }

        @Override
        public Parcelable saveState() {
            return null;
        }

        @Override
        public void restoreState(Parcelable state, ClassLoader loader) {
        }

        /**
         * Return a unique identifier for the item at the given position.
         * <p>
         * <p>The default implementation returns the given position.
         * Subclasses should override this method if the positions of items can change.</p>
         *
         * @param position Position within this adapter
         * @return Unique identifier for the item at position
         */
        public long getItemId(int position) {
            return position;
        }

        private String makeFragmentName(int viewId, long id) {
            return "android:switcher:" + viewId + ":" + id;
        }
    }

    @Override
    protected void onDestroy() {
        //将EventBus注销掉
        EventBus.getDefault().unregister(this);
        //界面销毁，移除掉所有的消息
        handler.removeCallbacksAndMessages(null);
        //解绑服务
        unbindService(this);
        super.onDestroy();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onServiceEvent(MusicServiceEvent event) {
        switch (event.getAction()) {
            case MusicServiceEvent.ACTION_MUSIC_PREPARED:
                //音乐准备好了,再一次初始化视图数据，
                //因为有些控件数据需要在音乐准备完成后才能获取
                initViewData();
                //开始刷新界面
                handler.sendEmptyMessage(MSG_REFRESH);
                break;
            case MusicServiceEvent.ACTION_MUSIC_START:
                setContinueBtnImg();
                handler.sendEmptyMessage(MSG_REFRESH);
                break;
            case MusicServiceEvent.ACTION_MUSIC_PAUSE:
                setContinueBtnImg();
                handler.removeCallbacksAndMessages(null);
                break;
        }
    }

    /**
     * 音乐准备完成了，就可以通过服务获取音乐数据，此时即可初始化视图
     */
    private void initViewData() {
        //设置歌曲标题
        musicTitle.setText(musicService.getMusicTitle());
        //设置歌曲演唱者
        musicArtist.setText(musicService.getArtist());

        setContinueBtnImg();

        //设置音乐当前播放位置
        musicCurrProgress.setText(TimeUtil.formatMilliSecond(musicService.getCurrentPosition()));
        //设置音乐时长
        musicDuration.setText(TimeUtil.formatMilliSecond(musicService.getDuration()));
        //设置进度条最大值为歌曲时间长度
        musicSeekBar.setMax(musicService.getDuration());
        //设置进度条当前位置
        musicSeekBar.setProgress(musicService.getCurrentPosition());
        //初始化音乐播放模式
        setPlayModeImg();
    }

    /**
     * 设置中间的按钮是暂停状态还是继续状态
     */
    private void setContinueBtnImg() {
        //设置中间的按钮是播放状态还是暂停状态
        if (musicService.isPlaying()) {
            musicBtnContinue.setImageResource(R.drawable.music_controller_pause_selector);
        } else {
            musicBtnContinue.setImageResource(R.drawable.music_controller_continue_selector);
        }
    }

    /**
     * 设置播放模式按钮的图标
     */
    private void setPlayModeImg() {
        switch (musicService.getPlayMode()) {
            case MusicPlayService.PLAY_MODE_RANDOM:
                //随机播放
                musicBtnMode.setImageResource(R.drawable.music_controller_random_selector);
                break;
            case MusicPlayService.PLAY_MODE_REPEAT_ALL:
                //列表播放
                musicBtnMode.setImageResource(R.drawable.music_controller_repeat_all_selector);
                break;
            case MusicPlayService.PLAY_MODE_REPEAT_SINGLE:
                //单曲循环
                musicBtnMode.setImageResource(R.drawable.music_controller_repeat_single_selector);
                break;
        }
    }

    @Event(value = {                //使用xUtils注入事件
            R.id.music_btn_return,  //返回按钮
            R.id.music_btn_more,    //更多内容
            R.id.music_btn_restart, //
            R.id.music_btn_prev,    //上一曲按钮
            R.id.music_btn_continue,//暂停或继续按钮
            R.id.music_btn_next,    //下一曲按钮
            R.id.music_btn_mode     //播放模式按钮
    })
    private void onClick(View view) {
        if (view == musicBtnReturn) {
            this.finish();
        } else if (view == musicBtnMore) {

        } else if (view == musicBtnRestart) {
        } else if (view == musicBtnPrev) {
            musicService.prev();
        } else if (view == musicBtnContinue) {
            //换图片在Service回调的时候执行
            if (musicService.isPlaying()) {
                //正在播放就让音乐在听
                musicService.pause();
            } else {
                //已经暂停，则让音乐继续播放
                musicService.start();
            }
        } else if (view == musicBtnNext) {
            musicService.next();
        } else if (view == musicBtnMode) {
            //因为只有三种模式的取值为0x0001~0x0003
            musicService.setPlayMode((musicService.getPlayMode() + 1) % 3 + 1);
            setPlayModeImg();
        }
    }

    /**
     * 启动并且绑定服务
     */
    private void startAndBindService() {
        Intent srcIntent = getIntent();
        Intent serviceIntent = (Intent) srcIntent.clone();
        serviceIntent.setClass(this, MusicPlayService.class);
        startService(serviceIntent);
        bindService(serviceIntent, this, Service.BIND_AUTO_CREATE);
    }


    /**
     * 服务连接上
     *
     * @param name    组件名
     * @param service 服务返回的Binder对象
     */
    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        //服务连接上后，设置为成员变量
        this.musicService = ((MusicPlayService.LocalBinder) service).getService();
        if (musicService.isPrepared()) {
            //准备好了就发送准备好了的消息
            EventBus bus = EventBus.getDefault();
            //如果没有该事件对象则发送该事件,此处使用黏性事件，因为此时Fragment可能还没有创建，还未注册事件接收
            bus.postSticky(new MusicServiceEvent(musicService, MusicServiceEvent.ACTION_RESUME_BIND));
        }
        initViewData();
    }

    /**
     * 服务连接断了
     *
     * @param name 组件名
     */
    @Override
    public void onServiceDisconnected(ComponentName name) {
        //服务断连之后，将成员变量置为空
        this.musicService = null;
    }


    /**
     * 进度条的监听器
     */
    private class SeekBarListener implements SeekBar.OnSeekBarChangeListener {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (seekBar == musicSeekBar) {
                if (fromUser && musicService != null) {
                    //处理用户的进度条拖动事件
                    musicService.seekTo(progress);
                }
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    }
}
