package cn.hufeifei.mediaplayer.utils;

import android.text.TextUtils;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xutils.x;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import cn.hufeifei.mediaplayer.R;
import cn.hufeifei.mediaplayer.domain.TVItem;

/**
 * 直播xml内容解析器
 * 读取资源文件xml文件夹下的addrs.xml文件
 * Created by Holmofy on 2016/12/19.
 */

public class TVItemParser {

    public static class TVType implements Serializable {
        public String name;
        public ArrayList<TVItem> tvItemList;
    }

    private TVItemParser() {
    }

    public static List<TVType> getTypes() throws XmlPullParserException, IOException {
        XmlPullParser parser = x.app().getResources().getXml(R.xml.addrs);

        List<TVType> TVTypes = null;

        ArrayList<TVItem> tvItemList = null;

        TVItem tvItem = null;
        int eventType = parser.getEventType();
        while (eventType != XmlPullParser.END_DOCUMENT) {
            switch (eventType) {
                case XmlPullParser.START_TAG: {
                    switch (parser.getName()) {
                        case "resources":
                            TVTypes = new ArrayList<>();
                            break;
                        case "type": {
                            tvItemList = new ArrayList<>();
                            String name = parser.getAttributeValue(0);
                            if (!TextUtils.isEmpty(name)) {
                                TVType TVType = new TVType();
                                TVType.tvItemList = tvItemList;
                                TVType.name = name;
                                TVTypes.add(TVType);
                            }
                        }
                        break;
                        case "address": {
                            tvItem = new TVItem();
                            String name = parser.getAttributeValue(0);
                            tvItem.setName(name);
                            if (tvItemList != null) {
                                tvItemList.add(tvItem);
                            }
                        }
                        break;
                    }
                }
                break;
                case XmlPullParser.TEXT:
                    String url = parser.getText();
                    if (tvItem != null) {
                        tvItem.setResUrl(url.trim());
                    }
                    break;
                case XmlPullParser.END_TAG:
                    switch (parser.getName()) {
                        case "type":
                            tvItemList = null;
                            break;
                        case "address":
                            tvItem = null;
                            break;
                    }
                    break;
            }
            eventType = parser.next();
        }
        return TVTypes;
    }
}
