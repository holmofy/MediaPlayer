package cn.hufeifei.mediaplayer.activity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;

import android.webkit.URLUtil;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListPopupWindow;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.Event;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cn.hufeifei.mediaplayer.R;
import cn.hufeifei.mediaplayer.adapter.SearchMusicAdapter;
import cn.hufeifei.mediaplayer.domain.NetMusicBriefInfo;
import cn.hufeifei.mediaplayer.utils.DensityUtil;
import cn.hufeifei.mediaplayer.utils.PreferencesUtil;
import cn.hufeifei.mediaplayer.utils.RequestUtil;

@ContentView(R.layout.activity_search)
public class SearchActivity extends Activity {

    private static final String SEARCH_OPTION = "search-option";

    private static final String OPTION_MUSIC = "音乐";

    private static final String OPTION_VIDEO = "视频";

    //网络请求成功的handler消息标志位
    private static final int MSG_QUERY_SUCCESS = 0x0001;

    //网络请求错误的Handler消息标志位
    private static final int MSG_QUERY_ERROR = 0x0002;

    @ViewInject(R.id.search_option)
    private Button searchOption;

    @ViewInject(R.id.search_text)
    private EditText searchText;

    @ViewInject(R.id.search_go_btn)
    private ImageButton searchGoBtn;

    @ViewInject(R.id.search_list)
    private ListView searchList;

    @ViewInject(R.id.no_net_tip)
    private TextView noNetTip;

    /**
     * 搜索到的音乐数据
     */
    private ArrayList<NetMusicBriefInfo> listMusic;

    private ItemClickListener listener = new ItemClickListener();

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_QUERY_SUCCESS:
                    switch (searchOption.getText().toString()) {
                        case OPTION_MUSIC:
                            showMusic();
                            break;
                        case OPTION_VIDEO:
                            showVideo();
                            break;
                    }
                    break;
                case MSG_QUERY_ERROR:
                    noNetTip.setVisibility(View.VISIBLE);
                    break;
            }
        }
    };

    /**
     * 显示搜索到的视频数据
     */
    private void showVideo() {
    }

    /**
     * 显示搜索到的音乐数据
     */
    private void showMusic() {
        if (listMusic != null && listMusic.size() != 0) {
            searchList.setAdapter(new SearchMusicAdapter(this, listMusic));
        }
    }


    /**
     * 进行网络请求，搜索相关资源
     *
     * @param s 搜索关键字
     */
    private void querySearch(String s) {
        String option = searchOption.getText().toString();
        if (option.equals(OPTION_MUSIC)) {
            //搜索音乐
            searchMusic(s);
        } else if (option.equals(OPTION_VIDEO)) {
            //搜索视频
            searchVideo(s);
        }
    }

    /**
     * 根据关键字搜寻视频资源
     *
     * @param keyWords 关键字
     */
    private void searchVideo(String keyWords) {
        if (keyWords.startsWith("http://")  //http协议
                || keyWords.startsWith("https://") //https协议
                || keyWords.startsWith("p2p://")  //p2p对等网络协议
                || keyWords.startsWith("rtsp://") //实时流传输协议
                || keyWords.startsWith("rtmp://") //实时消息传输协议
                || keyWords.startsWith("mms://")) {//流媒体传送协议
            //如果用户输入的关键字是链接，可以直接调用播放器播放
            Intent intent;
            //如果是视频数据，就使用视频播放界面
            intent = new Intent(this, SysMediaPlayerActivity.class);
            intent.setData(Uri.parse(keyWords));
            this.startActivity(intent);
        } else {
            //用户未输入协议，使用http协议测试链接是否有效
            String strUrl = "http://" + keyWords;
            try {
                URL url = new URL(strUrl);//检测用户输入是否能构造成一个url地址
                url.getQuery();
                //构造地址后，把地址传入播放界面
                Intent intent;
                //如果是视频数据，就使用视频播放界面
                intent = new Intent(this, SysMediaPlayerActivity.class);
                intent.setData(Uri.parse(strUrl));
                this.startActivity(intent);
            } catch (MalformedURLException e) {
                //发生异常说明用户输入无法构造成一个url地址

            }
        }
    }

    /**
     * 根据关键字搜寻音乐资源
     *
     * @param keyWords 关键字
     */
    private void searchMusic(String keyWords) {
        RequestUtil.searchMusic(keyWords, new RequestUtil.CallBack<List<NetMusicBriefInfo>>() {
            @Override
            public void onSuccess(List<NetMusicBriefInfo> obj) {
                listMusic = (ArrayList<NetMusicBriefInfo>) obj;
                handler.sendEmptyMessage(MSG_QUERY_SUCCESS);
            }

            @Override
            public void onError(String errorInfo) {
                handler.sendEmptyMessage(MSG_QUERY_ERROR);
            }
        });
    }

    private void initViewData() {
        searchList.setOnItemClickListener(listener);
        //初始化搜索选项，将保存在配置文件中的搜索选项设置到搜索选项的文本中
        searchOption.setText(PreferencesUtil.getPreferences().getString(this, SEARCH_OPTION, optionText[0]));
        setEditHint();
        searchText.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //再一次发送请求之前，将“网络错误”的提示隐去
                noNetTip.setVisibility(View.GONE);
                if (s.toString().endsWith("\r") || s.toString().endsWith("\n")) {
                    //用户回车了才开始搜索
                    String keyword = s.toString().trim();
                    searchText.setText(keyword);
                    searchText.clearFocus();
                    querySearch(keyword);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void setEditHint() {
        switch (searchOption.getText().toString()) {
            case OPTION_MUSIC:
                searchText.setHint("请输入音乐名或歌手名");
                break;
            case OPTION_VIDEO:
                searchText.setHint("请输入视频链接");
                break;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        x.view().inject(this);
        initViewData();
    }

    /**
     * 使用ListPopupWindow自定义下拉列表
     * 以下为相关字段
     */
    private ListPopupWindow popup;
    private final String[] optionText = new String[]{OPTION_MUSIC, OPTION_VIDEO};
    private int[] optionImg = new int[]{R.drawable.search_option_music, R.drawable.search_option_video};

    @Event(value = {R.id.search_option, R.id.search_go_btn})
    private void onClick(View view) {
        if (view == searchOption) {
            if (popup == null) {
                createPopupWindow();
            }
            popup.show();
        } else if (view == searchGoBtn) {
            querySearch(searchText.getText().toString().trim());
        }
    }

    private void createPopupWindow() {
        popup = new ListPopupWindow(this);
        List<HashMap<String, Object>> list = new ArrayList<HashMap<String, Object>>();
        for (int i = 0; i < optionImg.length; i++) {
            HashMap<String, Object> item = new HashMap<>();
            item.put("img", optionImg[i]);
            item.put("text", optionText[i]);
            list.add(item);
        }
        popup.setAdapter(new SimpleAdapter(this, list,//数据
                R.layout.search_option_item, //布局
                new String[]{"img", "text"}, //key
                new int[]{R.id.img, R.id.text}));//布局中的控件id
        popup.setAnchorView(searchOption);
        popup.setWidth(DensityUtil.dip2px(this, 100));
        popup.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        popup.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                searchOption.setText(optionText[position]);
                PreferencesUtil.getPreferences().putString(SearchActivity.this, SEARCH_OPTION, optionText[position]);
                searchList.setAdapter(null);
                popup.dismiss();
                //重新设置提示信息
                setEditHint();
            }
        });
    }

    private class ItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            switch (searchOption.getText().toString()) {
                case OPTION_MUSIC:
                    //如果是音乐数据就用音乐播放界面播放
                    Intent intent;
                    intent = new Intent(SearchActivity.this, MusicPlayerActivity.class);
                    intent.putExtra(MusicPlayerActivity.EXTRA_FROM_NET, true);
                    intent.putExtra(MusicPlayerActivity.EXTRA_MEDIA_POSITION, position);
                    intent.putExtra(MusicPlayerActivity.EXTRA_MEDIA_DATA, listMusic);
                    startActivity(intent);
                    break;
                case OPTION_VIDEO:
                    break;
            }
        }
    }
}
