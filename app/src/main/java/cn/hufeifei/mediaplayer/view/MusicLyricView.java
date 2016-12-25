package cn.hufeifei.mediaplayer.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.widget.TextView;

import java.util.List;

import cn.hufeifei.mediaplayer.domain.Lyric;
import cn.hufeifei.mediaplayer.utils.DensityUtil;

/**
 * 自定义歌词控件
 * Created by Holmofy on 2016/11/29.
 */

public class MusicLyricView extends TextView {

    private static final int CURR_COLOR = 0xff209860;
    private static final int OTHER_COLOR = 0xffabaaab;

    /**
     * 歌词列表
     */
    private List<Lyric> lyricList;


    public void setLyricList(List<Lyric> lyricList) {
        this.lyricList = lyricList;
        currIndex = 0;//设置歌词时，必须将歌词索引置0，否则可能会出现数组越界的问题
    }

    /**
     * 画笔
     */
    private Paint lyricPaint;
    /**
     * 当前歌词的索引
     */
    private int currIndex = 0;
    private int lineHeight;

    private long currPostion;//歌曲当前播放位置

    private int width;
    private int height;


    public MusicLyricView(Context context) {
        this(context, null);
    }

    public MusicLyricView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MusicLyricView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        lyricPaint = new Paint();
        lyricPaint.setAntiAlias(true);
        lyricPaint.setTextAlign(Paint.Align.CENTER);
        lyricPaint.setTextSize(DensityUtil.dip2px(context, 20));
        lyricPaint.setColor(CURR_COLOR);//当前歌词颜色
        lineHeight = DensityUtil.dip2px(context, 30);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        width = w;
        height = h;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (lyricList != null && lyricList.size() != 0) {
            //根据当前播放时间，缓缓上移
            //  当前句已播放时间/当前句时长 = 上移高度/行高
            //  上移高度 = 当前句已播放时间*行高/当前句时长
            long translate = currPostion - lyricList.get(currIndex).getTimeStamp();
            long duration = lyricList.get(currIndex).getDuration();
            if (duration != 0) {
                translate = translate * lineHeight / duration;
            } else {
                translate = 0;
            }

            canvas.translate(0, -translate);

            lyricPaint.setColor(CURR_COLOR);//当前歌词颜色
            lyricPaint.setAlpha(255);
            //绘制当前句的歌词
            canvas.drawText(lyricList.get(currIndex).getContent(), width >> 1, height >> 1, lyricPaint);

            lyricPaint.setColor(OTHER_COLOR);
            int alpha = 255;    //离当前句越远越透明
            int count = (height >> 1) / lineHeight;
            //绘制当前句之前的歌词
            for (int i = currIndex - 1; i >= 0; i--) {
                int y = (height >> 1) - lineHeight * (currIndex - i);
                if (y < 0) {
                    break;
                } else {
                    lyricPaint.setAlpha(alpha - 255 * (currIndex - i) / count / 2);
                    canvas.drawText(lyricList.get(i).getContent(), width >> 1, y, lyricPaint);
                }
            }

            lyricPaint.setColor(OTHER_COLOR);
            alpha = 255;
            //绘制当前据之后的歌词
            for (int i = currIndex + 1; i < lyricList.size(); i++) {
                int y = (height >> 1) + lineHeight * (i - currIndex);
                if (y > height) {
                    break;
                } else {
                    lyricPaint.setAlpha(alpha - 255 * (i - currIndex) / count / 2);
                    canvas.drawText(lyricList.get(i).getContent(), width >> 1, y, lyricPaint);
                }
            }
        } else {
            canvas.drawText("该歌曲暂时没有歌词", width / 2, height / 2, lyricPaint);
        }
    }

    public void refreshLyric(int currentPosition) {
        this.currPostion = currentPosition;
        if (lyricList != null && lyricList.size() != 0) {
            for (int i = 1; i < lyricList.size(); i++) {
                if (lyricList.get(i).getTimeStamp() > currentPosition) {
                    //大于当前播放位置，说明当前歌曲播放位于上一句
                    int temp = i - 1;
                    if (lyricList.get(temp).getTimeStamp() < currentPosition) {
                        currIndex = temp;
                        invalidate();
                        return;
                    }
                }
            }
            //说明已经播放到最后一句了
            currIndex = lyricList.size() - 1;
        }
    }
}
