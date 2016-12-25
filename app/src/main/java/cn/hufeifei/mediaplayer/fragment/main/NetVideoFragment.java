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
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.xmlpull.v1.XmlPullParserException;
import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

import java.io.IOException;
import java.util.List;

import cn.hufeifei.mediaplayer.R;
import cn.hufeifei.mediaplayer.fragment.video.NetVideoListFragment;
import cn.hufeifei.mediaplayer.utils.TVItemParser;


/**
 * 网络视频页面
 * Created by Holmofy on 2016/12/6.
 */
@ContentView(R.layout.fragment_net_video_page)
public class NetVideoFragment extends Fragment {
    private View rootView;

    @ViewInject(R.id.viewPager)
    private ViewPager viewPager;

    @ViewInject(R.id.channelListView)
    private ListView channelListView;

    private List<TVItemParser.TVType> TVTypes;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (rootView == null) {
            rootView = x.view().inject(this, inflater, container);
            try {
                TVTypes = TVItemParser.getTypes();
            } catch (XmlPullParserException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            initViewData();
        }
        return rootView;
    }


    private void initViewData() {
        channelListView.setAdapter(new ChannelTypeAdapter());
        channelListView.setOnItemClickListener(new ItemClickListener());
        viewPager.setAdapter(new TVListAdapter());
        viewPager.addOnPageChangeListener(new PageChangeListener());
    }

    private class PageChangeListener implements ViewPager.OnPageChangeListener {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        }

        @Override
        public void onPageSelected(int position) {
            changePage(position);
        }

        @Override
        public void onPageScrollStateChanged(int state) {
        }
    }


    private class ItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            changePage(position);
            viewPager.setCurrentItem(position);
        }
    }

    private void changePage(int position) {
        View lastView = channelListView.getChildAt(currPosition);
        ChannelTypeAdapter.ViewHolder last = (ChannelTypeAdapter.ViewHolder) (lastView.getTag());
        last.channelTypeName.setTextColor(0xff666666);
        last.underline.setBackgroundColor(0x00000000);
        View newView = channelListView.getChildAt(position);
        ChannelTypeAdapter.ViewHolder curr = (ChannelTypeAdapter.ViewHolder) (newView.getTag());
        curr.channelTypeName.setTextColor(0xff06b659);
        curr.underline.setBackgroundColor(0xff06b659);
        currPosition = position;
    }

    private int currPosition;

    private class ChannelTypeAdapter extends BaseAdapter {
        class ViewHolder {
            TextView channelTypeName;
            View underline;
        }

        @Override
        public int getCount() {
            return TVTypes.size();
        }

        @Override
        public Object getItem(int position) {
            return TVTypes.get(position).name;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = View.inflate(getActivity(), R.layout.net_video_channel_type, null);
                holder = new ViewHolder();
                holder.channelTypeName = (TextView) convertView.findViewById(R.id.channelTypeName);
                holder.underline = convertView.findViewById(R.id.underline);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            holder.channelTypeName.setText((CharSequence) getItem(position));
            if (currPosition == position) {
                //被选中的绘制下划线
                holder.underline.setBackgroundColor(0xff06b659);
                //设置文本颜色
                holder.channelTypeName.setTextColor(0xff06b659);
            } else {
                //未选中的绘制透明
                holder.underline.setBackgroundColor(0x00000000);
            }
            return convertView;
        }
    }


    private class TVListAdapter extends PagerAdapter {
        FragmentManager fragmentManager;

        TVListAdapter() {
            fragmentManager = getFragmentManager();
        }

        @Override
        public int getCount() {
            return TVTypes.size();
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            Fragment fragment = fragmentManager.findFragmentByTag(TVTypes.get(position).name);
            if (fragment == null) {
                fragment = new NetVideoListFragment();
                Bundle bundle = new Bundle();
                bundle.putSerializable(NetVideoListFragment.DATA_KEY, TVTypes.get(position).tvItemList);
                fragment.setArguments(bundle);
                fragmentManager.beginTransaction().add(container.getId(), fragment, TVTypes.get(position).name).commit();
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

//
//        @Override
//        public CharSequence getPageTitle(int position) {
//            return TVTypes.get(position).name;
//        }
    }
}
