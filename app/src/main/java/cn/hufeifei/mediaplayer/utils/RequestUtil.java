package cn.hufeifei.mediaplayer.utils;

import android.text.TextUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.x;

import java.util.ArrayList;
import java.util.List;

import cn.hufeifei.mediaplayer.domain.NetMusicBriefInfo;
import cn.hufeifei.mediaplayer.domain.NetMusicInfo;
import cn.hufeifei.mediaplayer.domain.NetMusicItem;


/**
 * 用于向服务器发送请求的工具类
 * Created by Holmofy on 2016/12/8.
 */

public class RequestUtil {

    private RequestUtil() {
    }

    /**
     * 获取网络数据
     */
    public static void getMusicList(String type, int size, int offset, final CallBack<List<NetMusicInfo>> callBack) {
        final RequestParams request = new RequestParams(Constant.BAIDU_MUSIC_BASE);
        //添加请求参数
        request.addQueryStringParameter(Constant.Param.METHOD, "baidu.ting.billboard.billList");//推荐列表
        request.addQueryStringParameter(Constant.Param.TYPE, type);//请求的榜单类型
        request.addQueryStringParameter(Constant.Param.SIZE, String.valueOf(size));//设置请求数量
        request.addQueryStringParameter(Constant.Param.OFFSET, String.valueOf(offset));//请求偏移量
        x.http().get(request, new Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String result) {
                //将该请求对应返回的结果缓存到xml中
                PreferencesUtil.getCache().putString(x.app().getBaseContext(), request.toString(), result);
                //处理请求到的JSON数据
                callBack.onSuccess(processList(result));
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                //网络请求错误时，返回之前缓存的内容
                String result = PreferencesUtil.getCache().getString(x.app().getBaseContext(), request.toString(), null);
                if (!TextUtils.isEmpty(result)) {
                    //返回结果不为空，说明之前缓存过内容
                    callBack.onSuccess(processList(result));
                } else {
                    //未缓存过，则直接返回错误信息
                    callBack.onError(ex.getMessage());
                }
            }

            @Override
            public void onCancelled(Callback.CancelledException cex) {

            }

            @Override
            public void onFinished() {

            }
        });
    }

    /**
     * 处理请求到的JSON数据
     */
    private static List<NetMusicInfo> processList(String data) {
        try {
            JSONObject root = new JSONObject(data);
            JSONArray arrRawData = root.getJSONArray("song_list");

            //将网络请求的JSON转换成歌曲对象列表
            ArrayList<NetMusicInfo> netMusicList = new ArrayList<>(arrRawData.length());
            for (int i = 0; i < arrRawData.length(); i++) {
                JSONObject itemRaw = arrRawData.optJSONObject(i);
                NetMusicInfo item = new NetMusicInfo();
                item.setBigPicLink(itemRaw.optString("pic_big"));
                item.setPublishTime(itemRaw.optString("publishtime").trim());
                item.setHot(itemRaw.optInt("hot"));
                item.setIsNew(itemRaw.optString("is_new").equals("1"));
                item.setHasMv(itemRaw.optString("has_mv").equals("1"));
                item.setSongId(itemRaw.optInt("song_id"));
                item.setTitle(itemRaw.optString("title").trim());
                item.setAlbumTitle(itemRaw.optString("album_title").trim());
                item.setArtistName(itemRaw.optString("artist_name").trim());
                netMusicList.add(item);
            }
            return netMusicList;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public interface CallBack<T> {
        void onSuccess(T obj);

        void onError(String errorInfo);
    }

    public static void playMusic(int songId, final CallBack<NetMusicItem> callBack) {
        final RequestParams request = new RequestParams(Constant.BAIDU_MUSIC_BASE);
        //添加请求参数
        request.addQueryStringParameter(Constant.Param.METHOD, "baidu.ting.song.play");//根据ID播放歌曲
        request.addQueryStringParameter(Constant.Param.SONG_ID, String.valueOf(songId));//歌曲ID
        x.http().get(request, new Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String result) {
                //将该请求对应返回的结果缓存到xml中
                PreferencesUtil.getCache().putString(x.app().getBaseContext(), request.toString(), result);
                //处理请求到的JSON数据
                callBack.onSuccess(processMusic(result));
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                //网络请求错误时，返回之前缓存的内容
                String result = PreferencesUtil.getCache().getString(x.app().getBaseContext(), request.toString(), null);
                if (!TextUtils.isEmpty(result)) {
                    callBack.onSuccess(processMusic(result));
                } else {
                    callBack.onError(ex.getMessage());
                }
            }

            @Override
            public void onCancelled(Callback.CancelledException cex) {

            }

            @Override
            public void onFinished() {

            }
        });
    }

    private static NetMusicItem processMusic(String data) {
        try {
            JSONObject root = new JSONObject(data);
            JSONObject rawInfo = root.getJSONObject("songinfo");
            JSONObject rawBit = root.getJSONObject("bitrate");

            NetMusicItem info = new NetMusicItem();
            info.setPicPremiumLink(rawInfo.optString("pic_premium"));
            info.setAuthor(rawInfo.optString("author").trim());
            info.setSongId(rawInfo.optInt("song_id"));
            info.setLrcLink(rawInfo.optString("lrclink"));
            info.setTitle(rawInfo.optString("title").trim());
            info.setFileLink(rawBit.optString("file_link"));
            return info;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 根据歌曲关键字从网上查询
     *
     * @param keyword 关键字
     */
    public static void searchMusic(String keyword, final CallBack<List<NetMusicBriefInfo>> callBack) {
        final RequestParams request = new RequestParams(Constant.BAIDU_MUSIC_BASE);
        //添加请求参数
        request.addQueryStringParameter(Constant.Param.METHOD, "baidu.ting.search.catalogSug");//搜寻歌曲
        request.addQueryStringParameter(Constant.Param.QUERY, keyword);//请求歌曲关键字
        x.http().get(request, new Callback.CommonCallback<String>() {

            @Override
            public void onSuccess(String result) {
                callBack.onSuccess(processSearch(result));
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                callBack.onError(ex.getMessage());
            }

            @Override
            public void onCancelled(CancelledException cex) {

            }

            @Override
            public void onFinished() {

            }
        });
    }

    private static List<NetMusicBriefInfo> processSearch(String data) {
        try {
            JSONObject root = new JSONObject(data);
            JSONArray arrRawData = root.getJSONArray("song");
            if (arrRawData == null) {
                //没有数据
                return null;
            }

            //将网络请求的JSON转换成歌曲对象列表
            ArrayList<NetMusicBriefInfo> searchList = new ArrayList<>(arrRawData.length());
            for (int i = 0; i < arrRawData.length(); i++) {
                JSONObject itemRaw = arrRawData.optJSONObject(i);
                NetMusicBriefInfo item = new NetMusicBriefInfo();
                item.setTitle(itemRaw.getString("songname"));
                item.setArtistName(itemRaw.getString("artistname"));
                item.setHasMv(itemRaw.getString("has_mv").equals("1"));
                item.setSongId(itemRaw.getInt("songid"));
                searchList.add(item);
            }
            return searchList;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }
}