package cn.hufeifei.mediaplayer.activity;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.text.format.Formatter;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;

import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.Event;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.hufeifei.mediaplayer.App;
import cn.hufeifei.mediaplayer.R;
import cn.hufeifei.mediaplayer.fragment.main.LocalMusicFragment;
import cn.hufeifei.mediaplayer.fragment.main.LocalVideoFragment;
import cn.hufeifei.mediaplayer.fragment.main.NetMusicFragment;
import cn.hufeifei.mediaplayer.fragment.main.NetVideoFragment;
import cn.hufeifei.mediaplayer.service.MusicPlayService;
import cn.hufeifei.mediaplayer.utils.Constant;
import cn.hufeifei.mediaplayer.utils.FileUtils;

@ContentView(R.layout.activity_main)
public class MainActivity extends Activity {

    /**
     * 侧滑菜单
     */
    private SlidingMenu slidingMenu;

    /**
     * 单选按钮组
     */
    @ViewInject(R.id.radioGroup)
    private RadioGroup radioGroup;

    @ViewInject(R.id.btn_main_menu)
    private ImageButton btnMainMenu;

    @ViewInject(R.id.btn_music)
    private ImageButton btnMusic;

    @ViewInject(R.id.btn_search)
    private Button btnSearch;

    /**
     * 子页面集合
     */
    private ArrayList<Fragment> fragments;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //使用xUtils进行view注入
        x.view().inject(this);

        initFragments();
        initSlidingMenu();

        radioGroup.check(R.id.radioButton_localVideo);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    /**
     * 初始化子页面
     */
    private void initFragments() {
        fragments = new ArrayList<>(4);
        fragments.add(new LocalVideoFragment());
        fragments.add(new LocalMusicFragment());
        fragments.add(new NetVideoFragment());
        fragments.add(new NetMusicFragment());
    }

    /**
     * 对SlidingMenu初始化
     */
    private void initSlidingMenu() {
        //创建侧滑菜单
        slidingMenu = new SlidingMenu(this);
        slidingMenu.setMode(SlidingMenu.LEFT);
        slidingMenu.setTouchModeAbove(SlidingMenu.TOUCHMODE_NONE);
        int width = getWindowManager().getDefaultDisplay().getWidth();
        slidingMenu.setBehindWidth((int) (width * 0.7));
        slidingMenu.setFadeDegree(0.4f);
        slidingMenu.attachToActivity(this, SlidingMenu.SLIDING_CONTENT);
        slidingMenu.setMenu(R.layout.main_menu);
        Button btnExit = (Button) slidingMenu.findViewById(R.id.btn_exit);
        btnExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MusicPlayService service = ((App) (getApplication())).getMusicService();
                if (service != null) {
                    //如果后台音乐在播放，则关闭后台音乐服务
                    service.stopSelf();
                }
                finish();//关闭该界面
            }
        });
        ListView menuBtnList = (ListView) slidingMenu.findViewById(R.id.menu_btn_list);

        String[] btnText = new String[]{    //按钮的文本
                "清除图片缓存",   //0
                "清除歌词缓存",   //1
                "添加快捷方式",   //2
                "播放历史",       //3
                "反馈问题",       //4
                "检查更新",       //5
                "关于作者"        //6
        };
        int[] btnIcon = new int[]{          //按钮图标
                R.drawable.btn_clear_pic_cache, //0
                R.drawable.btn_clear_lyric,     //1
                R.drawable.btn_create_shortcut, //2
                R.drawable.btn_history,         //3
                R.drawable.btn_question,        //4
                R.drawable.btn_check_update,    //5
                R.drawable.btn_about_author     //6
        };

        List<Map<String, Object>> btnList = new ArrayList<>(btnText.length);
        for (int i = 0; i < btnText.length; i++) {
            Map<String, Object> map = new HashMap<>(2);
            map.put("img", btnIcon[i]);
            map.put("text", btnText[i]);
            btnList.add(map);
        }
        ListAdapter adapter = new SimpleAdapter(this, btnList,  //按钮列表
                R.layout.menu_btn_item,         //布局
                new String[]{"img", "text"},     //key
                new int[]{R.id.img, R.id.text}   //id
        );
        menuBtnList.setAdapter(adapter);
        menuBtnList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0: {
                        //清除图片缓存
                        File cacheDir = new File(getExternalCacheDir(), "xUtils_img");
                        long size = FileUtils.deleteDir(cacheDir);
                        if (size == 0) {
                            Toast.makeText(MainActivity.this, "没有图片缓存", Toast.LENGTH_SHORT).show();
                        } else {
                            String strSize = Formatter.formatShortFileSize(MainActivity.this, size);
                            Toast.makeText(MainActivity.this, "清除了" + strSize + "图片缓存", Toast.LENGTH_SHORT).show();
                        }
                    }
                    break;
                    case 1: {
                        //清除歌词缓存
                        File lyricDir = Constant.getLyricDir();
                        long size = FileUtils.deleteDir(lyricDir);
                        if (size == 0) {
                            Toast.makeText(MainActivity.this, "没有歌词缓存", Toast.LENGTH_SHORT).show();
                        } else {
                            String strSize = Formatter.formatShortFileSize(MainActivity.this, size);
                            Toast.makeText(MainActivity.this, "清除了" + strSize + "歌词缓存", Toast.LENGTH_SHORT).show();
                        }
                        break;
                    }
                    case 2:
                        //添加快捷方式
                        addShortcut(getString(R.string.app_name));
                        Toast.makeText(MainActivity.this, "创建成功", Toast.LENGTH_SHORT).show();
                        break;
                    case 3:
                        //历史记录
                        showHistory();
                        break;
                    case 4:
                        //反馈问题
                        Toast.makeText(MainActivity.this, "该模块还在开发中", Toast.LENGTH_SHORT).show();
                        break;
                    case 5:
                        //检查更新
                        Toast.makeText(MainActivity.this, "该模块还在开发中", Toast.LENGTH_SHORT).show();
                        break;
                    case 6:
                        //关于作者
                        Toast.makeText(MainActivity.this, "该模块还在开发中", Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        });
    }

    /**
     * 显示历史记录页面
     */
    private void showHistory() {
        startActivity(new Intent(this, HistoryActivity.class));
    }

    private void addShortcut(String name) {
        Intent addShortcutIntent = new Intent("com.android.launcher.action.INSTALL_SHORTCUT");

        // 不允许重复创建
        addShortcutIntent.putExtra("duplicate", false);// 经测试不是根据快捷方式的名字判断重复的
        // 应该是根据快链的Intent来判断是否重复的,即Intent.EXTRA_SHORTCUT_INTENT字段的value
        // 但是名称不同时，虽然有的手机系统会显示Toast提示重复，仍然会建立快链
        // 屏幕上没有空间时会提示
        // 名字
        addShortcutIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, name);

        // 图标
        addShortcutIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, Intent.ShortcutIconResource.fromContext(MainActivity.this, R.drawable.ic_launcher));

        // 设置关联程序
        Intent launcherIntent = new Intent(Intent.ACTION_MAIN);
        launcherIntent.setClass(MainActivity.this, MainActivity.class);
        launcherIntent.addCategory(Intent.CATEGORY_LAUNCHER);

        addShortcutIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, launcherIntent);

        // 发送广播
        sendBroadcast(addShortcutIntent);
    }

    //不设置type，type默认为View.OnClickListener.class;
    @Event(value = {R.id.btn_main_menu, R.id.btn_music, R.id.btn_search})
    private void onClick(View view) {
        if (view == btnMainMenu) {
            slidingMenu.toggle();
        } else if (view == btnSearch) {
            Intent intent = new Intent(MainActivity.this, SearchActivity.class);
            startActivity(intent);
        } else if (view == btnMusic) {
            Intent intent = new Intent(this, MusicPlayerActivity.class);
            intent.putExtra("notification", true);//该意图与点击任务栏进入界面效果相同
            startActivity(intent);
        }
    }


    /**
     * 页面选项改变，更换不同的fragment
     *
     * @param group     单选按钮组
     * @param checkedId 点选按钮的id
     */
    @Event(value = R.id.radioGroup, type = RadioGroup.OnCheckedChangeListener.class)
    private void onCheckedChange(RadioGroup group, int checkedId) {
        int index = 0;
        switch (checkedId) {
            case R.id.radioButton_localVideo:
                index = 0;
                break;
            case R.id.radioButton_localMusic:
                index = 1;
                break;
            case R.id.radioButton_netVideo:
                index = 2;
                break;
            case R.id.radioButton_netMusic:
                index = 3;
                break;
        }
        //设置相应的fragment
        getFragmentManager().beginTransaction().replace(R.id.content, fragments.get(index)).commit();
    }

}
