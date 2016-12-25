package cn.hufeifei.mediaplayer.fragment.player;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * 音乐播放界面中的音效控制子界面
 * Created by Holmofy on 2016/12/7.
 */

public class SoundEffectFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        TextView textView = new TextView(getActivity());
        textView.setText("音效控制页面");
        textView.setGravity(Gravity.CENTER);
        textView.setTextColor(0xffffffff);
        textView.setTextSize(30);
        return textView;
    }
}
