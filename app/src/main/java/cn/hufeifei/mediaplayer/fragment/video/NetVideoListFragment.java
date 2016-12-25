package cn.hufeifei.mediaplayer.fragment.video;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

import java.util.List;

import cn.hufeifei.mediaplayer.R;
import cn.hufeifei.mediaplayer.activity.VitamioPlayerActivity;
import cn.hufeifei.mediaplayer.domain.TVItem;

/**
 * 网络电视列表界面
 * Created by Holmofy on 2016/12/20.
 */
@ContentView(R.layout.fragment_net_video_list)
public class NetVideoListFragment extends Fragment {
    public static final String DATA_KEY = "fragment_data_key";

    private List<TVItem> TVItemList;

    private View root;

    @ViewInject(R.id.net_video_list)
    private ListView netVideoList;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (root == null) {
            root = x.view().inject(this, inflater, container);
            TVItemList = (List<TVItem>) getArguments().getSerializable(DATA_KEY);
        }
        return root;
    }


    @Override
    public void onResume() {
        super.onResume();
        loadData();
    }

    private void loadData() {
        netVideoList.setAdapter(new BaseAdapter() {
            @Override
            public int getCount() {
                return TVItemList.size();
            }

            @Override
            public Object getItem(int position) {
                return TVItemList.get(position);
            }

            @Override
            public long getItemId(int position) {
                return position;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                ViewHolder holder;
                if (convertView == null) {
                    convertView = View.inflate(getActivity(), R.layout.net_video_list_item, null);
                    holder = new ViewHolder();
                    holder.textTVName = (TextView) convertView.findViewById(R.id.textTVName);
                    convertView.setTag(holder);
                } else {
                    holder = (ViewHolder) convertView.getTag();
                }
                holder.textTVName.setText(TVItemList.get(position).getName());
                return convertView;
            }
        });
        netVideoList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent;
                //如果是视频数据，就使用视频播放界面
                intent = new Intent(getActivity(), VitamioPlayerActivity.class);
                intent.putExtra(VitamioPlayerActivity.EXTRA_IS_TV_ITEM, true);
                intent.putExtra(VitamioPlayerActivity.EXTRA_MEDIA_TV_ITEM, TVItemList.get(position));
                getActivity().startActivity(intent);
            }
        });
    }

    private class ViewHolder {
        TextView textTVName;
    }
}
