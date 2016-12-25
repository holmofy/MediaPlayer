package cn.hufeifei.mediaplayer.fragment.main;

import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

import cn.hufeifei.mediaplayer.R;
import cn.hufeifei.mediaplayer.fragment.music.NetMusicListFragment;

/**
 * 网络音乐界面
 * Created by Holmofy on 2016/12/6.
 */
@ContentView(R.layout.fragment_net_music_page)
public class NetMusicFragment extends Fragment {

    private View rootView;

    @ViewInject(R.id.viewPager)
    private ViewPager viewPager;

    private class MusicType {
        MusicType(String name, String id) {
            this.name = name;
            this.id = id;
        }

        String name;
        String id;
    }

    private MusicType[] types = new MusicType[]{
            //新歌榜
            new MusicType("新歌榜", "1"),
            //热歌榜
            new MusicType("热歌榜", "2"),
            //摇滚乐
            new MusicType("摇滚乐", "11"),
            //爵士乐
            new MusicType("爵士乐", "12"),
            //流行乐
            new MusicType("流行乐", "16"),
            //欧美金曲
            new MusicType("欧美金曲", "21"),
            //经典老歌
            new MusicType("经典老歌", "22"),
            //情歌对唱
            new MusicType("情歌对唱", "23"),
            //影视金曲
            new MusicType("影视金曲", "24"),
            //网络歌曲榜
            new MusicType("网络歌曲", "25")//
    };

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (rootView == null) {
            rootView = x.view().inject(this, inflater, container);
            viewPager.setAdapter(new MusicListAdapter());
        }
        return rootView;
    }

    private class MusicListAdapter extends PagerAdapter {
        FragmentManager fragmentManager;


        MusicListAdapter() {
            fragmentManager = getFragmentManager();
        }

        @Override
        public int getCount() {
            return types.length;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            Fragment fragment = fragmentManager.findFragmentByTag(types[position].id);
            if (fragment == null) {
                fragment = new NetMusicListFragment();
                Bundle data = new Bundle();
                data.putString(NetMusicListFragment.DATA_MUSIC_TYPE, types[position].id);
                fragment.setArguments(data);
                fragmentManager.beginTransaction().add(container.getId(), fragment, types[position].id).commit();
            } else {
                fragmentManager.beginTransaction().attach(fragment).commit();
            }
            return fragment;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            fragmentManager.beginTransaction().detach((Fragment) object).commit();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return ((Fragment) object).getView() == view;
        }


        @Override
        public CharSequence getPageTitle(int position) {
            return types[position].name;
        }
    }
}
