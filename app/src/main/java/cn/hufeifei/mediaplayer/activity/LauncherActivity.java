package cn.hufeifei.mediaplayer.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.webkit.WebSettings;
import android.webkit.WebView;

import java.util.Timer;
import java.util.TimerTask;



import cn.hufeifei.mediaplayer.R;

/**
 * 应用启动界面
 *
 * @author Holmofy
 */
@SuppressLint("SetJavaScriptEnabled")
public class LauncherActivity extends Activity {
    /**
     * 启动页面中用来防止广告的WebView
     */
    WebView advWebView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launcher);
        advWebView = (WebView) findViewById(R.id.advWebView);
        WebSettings setting = advWebView.getSettings();
        // 允许直接访问文件
        setting.setAllowFileAccess(true);
        // 禁止缩放
        setting.setBuiltInZoomControls(false);
        // 允许JavaScript脚本
        setting.setJavaScriptEnabled(true);
        advWebView.loadUrl(getResources().getString(R.string.adv_url));
        // 三秒后进入MainActivity
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                startMainActivity();
            }
        }, 2000);
    }

    @Override
    protected void onDestroy() {
        //防止用户销毁该页面后，仍然进入主界面
        isStart = true;
        super.onDestroy();
    }

    /**
     * 检测是否已经启动MainActivity的标识位
     */
    private boolean isStart = false;

    /**
     * 启动主界面
     */
    private void startMainActivity() {
        if (!isStart) {
            isStart = true;
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        }
    }

    /**
     * 用户触摸屏幕直接进入MainActivity
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        startMainActivity();
        return super.onTouchEvent(event);
    }
}
