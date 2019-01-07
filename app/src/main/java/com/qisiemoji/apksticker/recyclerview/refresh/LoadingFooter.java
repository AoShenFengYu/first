package com.qisiemoji.apksticker.recyclerview.refresh;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewStub;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.qisiemoji.apksticker.R;


/**
 * Created by dingchenghao on 2017/4/5.
 * Email: dingchenghao@baofeng.com | godcok@163.com
 */
public class LoadingFooter extends RelativeLayout {

    protected State mState = State.Normal;
    private View mLoadingView;
    private View mErrorView;
    private TextView mErrorTextView;
    private View mTheEndView;
    private TextView mAllTextView;
    private TextView mTheEndTextView;
    private View mLoadAllView;
    private ImageView mLoadingImage;
    private TextView mLoadingText;

    public LoadingFooter(Context context) {
        super(context);
        init(context);
    }

    public LoadingFooter(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }
    private int loadingTextResId = R.string.str_loading;
    private int dataEndResId = R.string.str_footer_end;
    private int textColor = 0xffb1b1b1;
    public void setLoadingTextResId(int loadingTextResId) {
        this.loadingTextResId = loadingTextResId;
    }

    public void setDataEndResId(int dataEndResId) {
        this.dataEndResId = dataEndResId;
    }

    public LoadingFooter(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public void setBackgroundColor(int backgroundColor) {
        findViewById(R.id.loading_view).setBackgroundColor(backgroundColor);
    }

    public void setTextColor(int color) {
        textColor = color;
    }

    public void init(Context context) {
        inflate(context, R.layout.sample_common_list_footer, this);
        setOnClickListener(null);
        setState(State.Normal, true, null);
    }

    public State getState() {
        return mState;
    }

    public void setState(State status, String str) {
        setState(status, true, str);
    }

    /**
     * 设置状态
     *
     * @param status
     * @param showView 是否展示当前View
     */
    public void setState(State status, boolean showView, String str) {
        if (mState == status) {
            return;
        }
        mState = status;

        switch (status) {

            case Normal:
                setOnClickListener(null);
                if (mLoadingView != null) {
                    mLoadingView.setVisibility(GONE);
                }
                if (mTheEndView != null) {
                    mTheEndView.setVisibility(GONE);
                }
                if (mErrorView != null) {
                    mErrorView.setVisibility(GONE);
                }
                if (mLoadAllView != null) {
                    mLoadAllView.setVisibility(GONE);
                }
                if (mLoadingImage != null && mLoadingImage.getBackground() instanceof AnimationDrawable) {
                    ((AnimationDrawable) mLoadingImage.getBackground()).stop();
                }
                break;
            case Loading:
                setOnClickListener(null);
                if (mTheEndView != null) {
                    mTheEndView.setVisibility(GONE);
                }
                if (mErrorView != null) {
                    mErrorView.setVisibility(GONE);
                }
                if (mLoadAllView != null) {
                    mLoadAllView.setVisibility(GONE);
                }
                if (mLoadingView == null) {
                    ViewStub viewStub = (ViewStub) findViewById(R.id.loading_viewstub);
                    mLoadingView = viewStub.inflate();
                    mLoadingImage = (ImageView) mLoadingView.findViewById(R.id.loading_image);

                    mLoadingText = (TextView) mLoadingView.findViewById(R.id.loading_text);
                    mLoadingText.setTextColor(textColor);
                } else {
                    mLoadingView.setVisibility(VISIBLE);
                }
                if (mLoadingImage != null && mLoadingImage.getBackground() instanceof AnimationDrawable) {
                    ((AnimationDrawable) mLoadingImage.getBackground()).start();
                }
                mLoadingView.setVisibility(showView ? VISIBLE : GONE);
                mLoadingImage.setVisibility(View.VISIBLE);
                mLoadingText.setText(loadingTextResId);
                break;
            case ALL:
                if (mLoadingView != null) {
                    mLoadingView.setVisibility(GONE);
                }
                if (mErrorView != null) {
                    mErrorView.setVisibility(GONE);
                }
                if (mTheEndView != null) {
                    mTheEndView.setVisibility(GONE);
                }
                if (mLoadAllView == null) {
                    ViewStub viewStub = (ViewStub) findViewById(R.id.network_all_viewstub);
                    mLoadAllView = viewStub.inflate();
                    mAllTextView = (TextView) mLoadAllView.findViewById(R.id.loading_text);
                    mAllTextView.setTextColor(textColor);
                } else {
                    mLoadAllView.setVisibility(VISIBLE);
                }

                if (!TextUtils.isEmpty(str)) {
                    mAllTextView.setText(str);
                } else {
                    mAllTextView.setText(R.string.str_up_show_all);
                }
                break;
            case TheEnd:
                setOnClickListener(null);
                if (mLoadingView != null) {
                    mLoadingView.setVisibility(GONE);
                }
                if (mErrorView != null) {
                    mErrorView.setVisibility(GONE);
                }
                if (mLoadAllView != null) {
                    mLoadAllView.setVisibility(GONE);
                }
                if (mLoadingImage != null && mLoadingImage.getBackground() instanceof AnimationDrawable) {
                    ((AnimationDrawable) mLoadingImage.getBackground()).stop();
                }
                if (mTheEndView == null) {
                    ViewStub viewStub = (ViewStub) findViewById(R.id.end_viewstub);
                    mTheEndView = viewStub.inflate();
                    mTheEndTextView = (TextView) mTheEndView.findViewById(R.id.loading_text);
                    mTheEndTextView.setTextColor(textColor);
                } else {
                    mTheEndView.setVisibility(VISIBLE);
                }
                if (!TextUtils.isEmpty(str)) {
                    mTheEndTextView.setText(str);
                } else {
                    mTheEndTextView.setText(dataEndResId);
                }
                mTheEndView.setVisibility(showView ? VISIBLE : GONE);
                break;
            case Error:
                if (mLoadingView != null) {
                    mLoadingView.setVisibility(GONE);
                }
                if (mTheEndView != null) {
                    mTheEndView.setVisibility(GONE);
                }
                if (mLoadingImage != null && mLoadingImage.getBackground() instanceof AnimationDrawable) {
                    ((AnimationDrawable) mLoadingImage.getBackground()).stop();
                }
                if (mErrorView == null) {
                    ViewStub viewStub = (ViewStub) findViewById(R.id.error_viewstub);
                    mErrorView = viewStub.inflate();
                    mErrorTextView = (TextView) mErrorView.findViewById(R.id.loading_text);
                    mErrorTextView.setTextColor(textColor);
                } else {
                    mErrorView.setVisibility(VISIBLE);
                }
                if (!TextUtils.isEmpty(str)) {
                    mErrorTextView.setText(str);
                }
                mErrorView.setVisibility(showView ? VISIBLE : GONE);
                break;
            default:
                break;
        }
    }

    public static enum State {
        Normal/**正常*/
        , TheEnd/**加载到最底了*/
        , Loading/**加载中..*/
        , ALL/**已经到最后了，但是还可以上拉**/
        , Error /**网络异常*/
    }
}