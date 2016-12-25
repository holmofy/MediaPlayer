package cn.hufeifei.mediaplayer.fragment.music;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

import java.util.ArrayList;
import java.util.List;

import cn.hufeifei.mediaplayer.R;
import cn.hufeifei.mediaplayer.activity.MusicPlayerActivity;
import cn.hufeifei.mediaplayer.adapter.NetMusicAdapter;
import cn.hufeifei.mediaplayer.domain.NetMusicInfo;
import cn.hufeifei.mediaplayer.utils.RequestUtil;
import cn.hufeifei.mediaplayer.view.XListView;


/**
 * 网络歌曲榜单列表子页面
 * 内嵌在NetMusicFragment中
 */
@ContentView(R.layout.fragment_net_music_list)
public class NetMusicListFragment extends Fragment {

    public static final String DATA_MUSIC_TYPE = "music_type";

    private View rootView;

    //每次请求的数量
    private static final int SIZE = 20;

    private ArrayList<NetMusicInfo> netMusicList;

    @ViewInject(R.id.net_music_list)
    private XListView listView;

    @ViewInject(R.id.progressBar)
    private ProgressBar progressBar;

    @ViewInject(R.id.textTip)
    private TextView textTip;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (netMusicList == null || netMusicList.size() == 0) {
                //没有数据
                textTip.setVisibility(View.VISIBLE);
            } else if (netMusicList.size() / SIZE <= 1) {
                //有数据，而且是第一次加载数据，或者是下拉刷新重新加载数据时，重新设置适配器
                listView.setAdapter(new NetMusicAdapter(getActivity(), netMusicList));
            }
            progressBar.setVisibility(View.GONE);
        }
    };


    public NetMusicListFragment() {
    }


    /**
     * 该列表中歌曲的类型
     */
    private String type;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        netMusicList = new ArrayList<>(SIZE * 5);
    }

//
//    /**
//     * 获取网络数据
//     */
//    private void getNetData() {
//        RequestParams request = new RequestParams(Constant.BAIDU_MUSIC_BASE);
//        //添加请求参数
//        request.addQueryStringParameter(Constant.Param.METHOD, "baidu.ting.billboard.billList");//推荐列表
//        request.addQueryStringParameter(Constant.Param.TYPE, "1");//新歌榜
//        request.addQueryStringParameter(Constant.Param.SIZE, String.valueOf(SIZE));//设置请求数量
//        request.addQueryStringParameter(Constant.Param.OFFSET, String.valueOf(netMusicList.size()));//请求偏移量
//        x.http().get(request, new Callback.CommonCallback<String>() {
//            @Override
//            public void onSuccess(String result) {
////                Toast.makeText(getActivity(), "数据获取成功", Toast.LENGTH_SHORT).show();
//                //处理请求到的JSON数据
//                processResult(result);
//                handler.sendEmptyMessage(0x0001);
//            }
//
//            @Override
//            public void onError(Throwable ex, boolean isOnCallback) {
//                Toast.makeText(getActivity(), "网络请求错误", Toast.LENGTH_SHORT).show();
//                handler.sendEmptyMessage(0x0001);
//            }
//
//            @Override
//            public void onCancelled(Callback.CancelledException cex) {
//
//            }
//
//            @Override
//            public void onFinished() {
//
//            }
//        });
//    }
//
//    /**
//     * 处理请求到的JSON数据
//     */
//    private void processResult(String data) {
//        try {
//            JSONObject root = new JSONObject(data);
//            JSONArray arrRawData = root.getJSONArray("song_list");
//
//            for (int i = 0; i < arrRawData.length(); i++) {
//                JSONObject itemRaw = arrRawData.optJSONObject(i);
//                NetMusicInfo item = new NetMusicInfo();
//                item.setSmallPicLink(itemRaw.optString("pic_small"));
//                item.setPublishTime(itemRaw.optString("publishtime"));
//                item.setHot(itemRaw.optInt("hot"));
//                item.setIsNew(itemRaw.optString("is_new").equals("1"));
//                item.setHasMv(itemRaw.optString("has_mv").equals("1"));
//                item.setSongId(itemRaw.optInt("song_id"));
//                item.setTitle(itemRaw.optString("title"));
//                item.setAlbumTitle(itemRaw.optString("album_title"));
//                item.setArtistName(itemRaw.optString("artist_name"));
//                netMusicList.add(item);
//            }
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (rootView == null) {
            type = getArguments().getString(DATA_MUSIC_TYPE);
            //使用xUtils进行view注入
            rootView = x.view().inject(this, inflater, container);
            initListener();
            getNetData();
        }
        return rootView;
    }

    private void getNetData() {
        RequestUtil.getMusicList(type, SIZE, netMusicList.size(), new RequestUtil.CallBack<List<NetMusicInfo>>() {

            @Override
            public void onSuccess(List<NetMusicInfo> obj) {
                List<NetMusicInfo> musicItems = obj;
                if (musicItems == null || musicItems.size() == 0) {
                    //说明该列表数据已经请求完了
                    listView.setFooterText("已无更多数据加载");
                    listView.setPullLoadEnable(false);
                } else {
                    //将请求的数据添加到列表中
                    netMusicList.addAll(musicItems);
                    handler.sendEmptyMessage(0x0001);
                }
            }

            //网络请求失败
            @Override
            public void onError(String errorInfo) {
                handler.sendEmptyMessage(0x0001);
            }
        });
    }

    /**
     * 对XListView进行监听
     */
    private void initListener() {
        ItemClickListener listener = new ItemClickListener();
        listView.setOnItemClickListener(listener);
        listView.setPullLoadEnable(true);
        listView.setXListViewListener(new XListView.IXListViewListener() {

            @Override
            public void onRefresh() {
                //刷新内容后，使列表能继续加载
                listView.setPullLoadEnable(true);
                listView.setFooterText(R.string.xlistview_footer_hint_normal);
                //清除所有的内容
                netMusicList = new ArrayList<>(SIZE * 5);
                //重新获取数据
                getNetData();
                listView.stopRefresh();
            }

            @Override
            public void onLoadMore() {
                //获取更多数据
                getNetData();
                listView.stopLoadMore();
            }
        });
    }

    private class ItemClickListener implements AdapterView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Intent intent;
            intent = new Intent(getActivity(), MusicPlayerActivity.class);
            intent.putExtra(MusicPlayerActivity.EXTRA_FROM_NET, true);
            //音乐XListView中Header占了一个位置，所以实际点击的获取数据位置应该是position-1
            intent.putExtra(MusicPlayerActivity.EXTRA_MEDIA_POSITION, position - 1);
            intent.putExtra(MusicPlayerActivity.EXTRA_MEDIA_DATA, netMusicList);
            getActivity().startActivity(intent);
        }
    }
}
