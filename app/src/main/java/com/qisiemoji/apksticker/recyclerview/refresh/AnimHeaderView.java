package com.qisiemoji.apksticker.recyclerview.refresh;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.AnimationDrawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.qisiemoji.apksticker.R;
import com.qisiemoji.apksticker.recyclerview.ptr.PtrFrameLayout;
import com.qisiemoji.apksticker.recyclerview.ptr.PtrUIHandler;
import com.qisiemoji.apksticker.recyclerview.ptr.gif.GifImageView;
import com.qisiemoji.apksticker.recyclerview.ptr.indicator.PtrIndicator;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Created by dingchenghao on 2017/4/5.
 * Email: dingchenghao@baofeng.com | godcok@163.com
 */
public class AnimHeaderView extends FrameLayout implements PtrUIHandler {
    //    private String time="最后更新：2017-01-01";
    private String time_str      = "最后更新：今天 09:00";
    private ImageView iv_loading    = null;
    private TextView tv_load       = null;
    private TextView tv_load_date  = null;
    private GifImageView mGifImageView = null;
    private String name          = "bf_refresh";
    private String url           = null;
    private SharedPreferences mSharedPreferences;
    private int pullDownRefreshResId = R.string.str_pull_refresh;
    private int pullDownReleaseResId = R.string.str_free_refresh;
    private int pullDownLoading = R.string.str_loading;
    public AnimHeaderView(Context context) {
        super(context);
        init();
    }


    public AnimHeaderView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    /**
     * 设置下拉文案
     * @param pullDownRefreshResId
     */
    public void setPullDownRefreshResId(int pullDownRefreshResId) {
        this.pullDownRefreshResId = pullDownRefreshResId;
    }

    /**
     * 下拉释放文案
     * @param pullDownReleaseResId
     */
    public void setPullDownReleaseResIdResId(int pullDownReleaseResId) {
        this.pullDownReleaseResId = pullDownReleaseResId;
    }

    /**
     * 下拉加载中
     * @param pullDownLoading
     */
    public void setPullDownLoading(int pullDownLoading) {
        this.pullDownLoading = pullDownLoading;
    }

    /**
     * 清除所有
     */
    public void clear() {
        mSharedPreferences.edit().clear().apply();
    }

    public void setSaveTimeName(String name) {
        this.name = name;
        displayTime();

//
//        if (sameDate(new Date(time), new Date(System.currentTimeMillis()))) {
//            time_str = getResources().getString(R.string.str_refresh_time_today, getTime(time, "HH:mm"));
//            tv_load_date.setText(time_str);
//        } else {
//            time_str = getResources().getString(R.string.str_refresh_time, getTime(time, "yyyy-MM-dd"));
//            tv_load_date.setText(time_str);
//        }
    }

    private void displayTime(){
        long time = mSharedPreferences.getLong("time" + name, System.currentTimeMillis());
        long current_time = System.currentTimeMillis();
        long delta = Math.abs(time - current_time);
//        if(delta > 24 * 60 * 60 * 1000) {
//            time_str = getResources().getString(R.string.str_refresh_time, getTime(time, "yyyy-MM-dd"));
//            tv_load_date.setText(time_str);
//        } else if(delta >= 60 * 60 * 1000){
//            time_str = getResources().getString(R.string.str_refresh_time_hour, String.valueOf(delta / (60 * 60 * 1000)));
//            tv_load_date.setText(time_str);
//        } else if(delta >= 5 * 60 * 1000) {
//            time_str = getResources().getString(R.string.str_refresh_time_min, String.valueOf(delta / (60 * 1000)));
//            tv_load_date.setText(time_str);
//        } else {
//            tv_load_date.setText(R.string.str_refresh_time_just);
//        }


        if (delta >= 5 * 60 * 1000) {
            if (sameDate(new Date(time), new Date(current_time))) {
                time_str = getResources().getString(R.string.str_refresh_time_today,
                        getTime(time, "HH:mm"));
                tv_load_date.setText(time_str);
            } else {
                time_str = getResources().getString(R.string.str_refresh_time,
                        getTime(time, "yyyy-MM-dd"));
                tv_load_date.setText(time_str);
            }
        } else {
            tv_load_date.setText(R.string.str_refresh_time_just);
        }
    }


    private void init() {
        mSharedPreferences = getContext().getSharedPreferences("bf_refresh", Context.MODE_MULTI_PROCESS);

        View header = LayoutInflater.from(getContext()).inflate(R.layout.view_ptr_animor_header, this);
        iv_loading = (ImageView) header.findViewById(R.id.img_loading);
        mGifImageView = (GifImageView) header.findViewById(R.id.ad_gifImageView);
        tv_load = (TextView) header.findViewById(R.id.tv_load);
        tv_load_date = (TextView) header.findViewById(R.id.tv_load_date);

    }

    public GifImageView getGifImageView() {
        return mGifImageView;
    }

    private long timeDelta(Date d1, Date d2) {
        if (null == d1 || null == d2)
            return 0;
        Calendar cal1 = Calendar.getInstance();
        cal1.setTime(d1);
        cal1.set(Calendar.HOUR_OF_DAY, 0);
        cal1.set(Calendar.MINUTE, 0);
        cal1.set(Calendar.SECOND, 0);
        cal1.set(Calendar.MILLISECOND, 0);
        Calendar cal2 = Calendar.getInstance();
        cal2.setTime(d2);
        cal2.set(Calendar.HOUR_OF_DAY, 0);
        cal2.set(Calendar.MINUTE, 0);
        cal2.set(Calendar.SECOND, 0);
        cal2.set(Calendar.MILLISECOND, 0);
        return Math.abs(cal1.getTimeInMillis() - cal2.getTimeInMillis());
    }



    private boolean sameDate(Date d1, Date d2) {
        if (null == d1 || null == d2)
            return false;
        Calendar cal1 = Calendar.getInstance();
        cal1.setTime(d1);
        cal1.set(Calendar.HOUR_OF_DAY, 0);
        cal1.set(Calendar.MINUTE, 0);
        cal1.set(Calendar.SECOND, 0);
        cal1.set(Calendar.MILLISECOND, 0);
        Calendar cal2 = Calendar.getInstance();
        cal2.setTime(d2);
        cal2.set(Calendar.HOUR_OF_DAY, 0);
        cal2.set(Calendar.MINUTE, 0);
        cal2.set(Calendar.SECOND, 0);
        cal2.set(Calendar.MILLISECOND, 0);
        return cal1.getTime().equals(cal2.getTime());
    }

    private void saveTime(long time) {
        mSharedPreferences.edit().putLong("time" + name, time).apply();
    }

    private String getTime(long time, String format) {
        Date currentTime = new Date();
        currentTime.setTime(time);

        SimpleDateFormat formatter = new SimpleDateFormat(format, Locale.getDefault());
        return formatter.format(currentTime);
    }

    @Override
    public void onUIReset(PtrFrameLayout frame) {
//        mAnimation.reset();
        ((AnimationDrawable) iv_loading.getBackground()).stop();
        tv_load.setText(pullDownRefreshResId); //下拉刷新
    }

    @Override
    public void onUIRefreshPrepare(PtrFrameLayout frame) {
        ((AnimationDrawable) iv_loading.getBackground()).start();
    }

    @Override
    public void onUIRefreshBegin(PtrFrameLayout frame) {
        ((AnimationDrawable) iv_loading.getBackground()).start();
        loadRefreshTime();
        tv_load.setText(pullDownLoading); //加载中...
    }

    public void loadRefreshTime() {
        long time = System.currentTimeMillis();
//        time_str = getResources().getString(R.string.str_refresh_time_today, getTime(time, "HH:mm"));
        saveTime(time);

    }

    @Override
    public void onUIRefreshComplete(PtrFrameLayout frame) {
//        tv_load_date.setText(time_str);
        ((AnimationDrawable) iv_loading.getBackground()).stop();
    }

    @Override
    public void onUIPositionChange(PtrFrameLayout frame, boolean isUnderTouch, byte status,
                                   PtrIndicator ptrIndicator) {
        final int mOffsetToRefresh = frame.getOffsetToRefresh();
        final int currentPos = ptrIndicator.getCurrentPosY();
        final int lastPos = ptrIndicator.getLastPosY();
        if (isUnderTouch) {
            displayTime();
        }
        if (currentPos < mOffsetToRefresh && lastPos >= mOffsetToRefresh) {
            if (isUnderTouch && status == PtrFrameLayout.PTR_STATUS_PREPARE) {
                tv_load.setText(pullDownRefreshResId); //下拉刷新
            }
        } else if (currentPos > mOffsetToRefresh && lastPos <= mOffsetToRefresh) {
            if (isUnderTouch && status == PtrFrameLayout.PTR_STATUS_PREPARE) {
                tv_load.setText(pullDownReleaseResId); //释放刷新
            }
        }
    }
}
