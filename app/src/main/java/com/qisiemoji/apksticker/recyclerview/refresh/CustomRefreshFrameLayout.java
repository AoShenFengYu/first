package com.qisiemoji.apksticker.recyclerview.refresh;

import android.content.Context;
import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.AttributeSet;
import android.view.View;

import com.qisiemoji.apksticker.R;
import com.qisiemoji.apksticker.adapters.RefreshRecyclerViewAdapter;
import com.qisiemoji.apksticker.recyclerview.ptr.PtrFrameLayout;
import com.qisiemoji.apksticker.recyclerview.ptr.gif.GifImageView;

/**
 * Created by dingchenghao on 2017/4/5.
 * Email: dingchenghao@baofeng.com | godcok@163.com
 */
public class CustomRefreshFrameLayout extends PtrFrameLayout {
    private int loadingTextResId = R.string.str_loading;
    private int dataEndResId = R.string.str_footer_end;
    private AnimHeaderView header;
    private LoadingFooter.State mState;
    private String str;
    private boolean isNeedShowFooterView = true;
    private int pullDownRefreshResId;
    private int pullDownReleaseResId;
    private int pullDownLoading;
    private int footerBackgroundColor = -1;// 如：0xffffffff
    private int footerTextColor = -1;//如：0xffffffff
    private String netError;
    private View.OnClickListener mErrorListener;
    private Handler handler;
    public void setNetError(String netError) {
        this.netError = netError;
    }

    public void setErrorListener(View.OnClickListener errorListener) {
        mErrorListener = errorListener;
    }

    public CustomRefreshFrameLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        initViews();
    }

    public CustomRefreshFrameLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initViews();
    }

    /**
     * 刷新完成
     * @param isCanLoadMore 是否可以加载更多
     */
    public void refreshComplete(boolean isCanLoadMore) {
        setLoadMoreEnable(isCanLoadMore);
        super.refreshComplete();

    }

    /**
     * 设置底部背景颜色
     * @param footerBackgroundColor 如：0xffffffff
     */
    public void setFooterBackgroundColor(int footerBackgroundColor) {
        this.footerBackgroundColor = footerBackgroundColor;
    }

    /**
     * 设置为正在加载数据
     */
    public void setLoadingStatus(boolean updateFooter) {
        setLoadingStatus();
        if (updateFooter) {
            setFooterViewState(getContext(), (RecyclerView) mContent, 0, LoadingFooter.State.Loading, null, null);
        }
    }

    /**
     * 设置底部文字颜色
     * @param footerTextColor 如：0xffffffff
     */
    public void setFooterTextColor(int footerTextColor) {
        this.footerTextColor = footerTextColor;
    }

    public void loadRefreshTime() {
        if (header != null) {
            header.loadRefreshTime();
        }
    }

    /**
     * 设置下拉文案
     *
     * @param pullDownRefreshResId
     */
    public void setPullDownRefreshResId(int pullDownRefreshResId) {
        this.pullDownRefreshResId = pullDownRefreshResId;
        if (header != null) {
            header.setPullDownRefreshResId(pullDownRefreshResId);
        }
    }

    /**
     * 下拉释放文案
     *
     * @param pullDownReleaseResId
     */
    public void setPullDownReleaseResIdResId(int pullDownReleaseResId) {
        this.pullDownReleaseResId = pullDownReleaseResId;
        if (header != null) {
            header.setPullDownReleaseResIdResId(pullDownReleaseResId);
        }
    }

    /**
     * 下拉加载中
     *
     * @param pullDownLoading
     */
    public void setPullDownLoading(int pullDownLoading) {
        this.pullDownLoading = pullDownLoading;
        if (header != null) {
            header.setPullDownLoading(pullDownLoading);
        }
    }


    public void setNeedShowFooterView(boolean isNeedShowFooterView) {
        this.isNeedShowFooterView = isNeedShowFooterView;
        if (!isNeedShowFooterView) {
            if (mContent instanceof RecyclerView) {
                RecyclerView.Adapter outerAdapter = ((RecyclerView) mContent).getAdapter();
                if (outerAdapter instanceof RefreshRecyclerViewAdapter) {
                    ((RefreshRecyclerViewAdapter) outerAdapter).removeFooterViewWhitOutNotify();
                }
            }
        }
        reset();
    }

    private void setFooterViewState(Context instance, RecyclerView recyclerView, int pageSize,
                                    LoadingFooter.State state, View.OnClickListener errorListener, String str) {
        if (instance == null || recyclerView == null || !isNeedShowFooterView) {
            return;
        }
        this.str = str;
        RecyclerView.Adapter outerAdapter = recyclerView.getAdapter();
        if (outerAdapter == null || !(outerAdapter instanceof RefreshRecyclerViewAdapter)) {
            return;
        }
        RefreshRecyclerViewAdapter recyclerViewAdapter = (RefreshRecyclerViewAdapter) outerAdapter;
        //只有一页的时候，就别加什么FooterView了
        if (recyclerViewAdapter.getInnerAdapter().getItemCount() < pageSize) {
            return;
        }
        LoadingFooter footerView;
        //已经有footerView了
        if (recyclerViewAdapter.getFooterViewsCount() > 0) {
            footerView = (LoadingFooter) recyclerViewAdapter.getFooterView();
            footerView.setState(state, str);

            if (state == LoadingFooter.State.Error) {
                footerView.setOnClickListener(errorListener);
            }
        } else {
            footerView = new LoadingFooter(instance);
            footerView.setLoadingTextResId(loadingTextResId);
            footerView.setDataEndResId(dataEndResId);
            if (footerBackgroundColor != -1) {
                footerView.setBackgroundColor(footerBackgroundColor);
            }
            if (footerTextColor != -1) {
                footerView.setTextColor(footerTextColor);
            }
            footerView.setState(state, str);
            if (state == LoadingFooter.State.Error) {
                footerView.setOnClickListener(errorListener);
            }
            recyclerViewAdapter.addFooterView(footerView);
            notifyAddFooter();
        }
    }

    private void notifyAddFooter() {
        if (!(mContent instanceof RecyclerView)) {
            return;
        }
        RecyclerView recyclerView = (RecyclerView) mContent;
        if (recyclerView.isComputingLayout()) {
            if(handler == null){
                handler = new Handler();
            }
            handler.post(new Runnable() {
                @Override
                public void run() {
                    notifyAddFooter();
                }
            });

        } else {
            RecyclerView.Adapter outerAdapter = recyclerView.getAdapter();
            if (outerAdapter == null) {
                return;
            }
            outerAdapter.notifyDataSetChanged();
        }
    }

    @Override
    protected void reset() {
        super.reset();
        if (mState == LoadingFooter.State.TheEnd || mState == LoadingFooter.State.Error || mState == LoadingFooter.State.ALL) {
            setFooterViewState(getContext(), (RecyclerView) mContent, 0, mState, null, str);
        } else {
            setFooterViewState(getContext(), (RecyclerView) mContent, 0, LoadingFooter.State.Normal, null, null);
        }
    }

    /**
     * 网络错误
     */
    public void setNetWorkError() {
        setFooterViewState(getContext(), (RecyclerView) mContent, 0, LoadingFooter.State.Error, mErrorListener, netError);
        mState = LoadingFooter.State.Error;
    }

    /**
     * 加载错误
     */
    public void setLoadFail() {
        setFooterViewState(getContext(), (RecyclerView) mContent, 0, LoadingFooter.State.ALL, null, getResources().getString(R.string.str_footer_fail));
        mState = LoadingFooter.State.ALL;
    }

    /**
     * 已经到达最底部，但是还可以上拉
     */
    public void setLastFooter() {
        setFooterViewState(getContext(), (RecyclerView) mContent, 0, LoadingFooter.State.ALL, null, null);
        mState = LoadingFooter.State.ALL;
    }

    /**
     * 是否允许加载更多
     * @param loadEnable
     */
    public void setLoadMoreEnable(boolean loadEnable) {
        setLoadMoreEnable(loadEnable, null);
    }

    /**
     * 设置为true后 不会加载更多
     *
     * @param neverLoadMore
     */
    @Override
    public void setNeverLoadMore(boolean neverLoadMore) {
        super.setNeverLoadMore(neverLoadMore);
        setLoadMoreEnable(!neverLoadMore, null);
    }

    public void setLoadMoreEnable(boolean loadEnable, String str) {
        super.setLoadMoreEnable(loadEnable);
        if (loadEnable) {
            super.setNeverLoadMore(!loadEnable);
        }
        if (!loadEnable) {
            setFooterViewState(getContext(), (RecyclerView) mContent, 0, LoadingFooter.State.TheEnd, null, str);
            mState = LoadingFooter.State.TheEnd;
        } else {
            mState = LoadingFooter.State.Normal;
            reset();
        }
    }

    @Override
    protected void changeLoadMoreStatus(RecyclerView recyclerView, boolean loadEnable) {
        super.changeLoadMoreStatus(recyclerView, loadEnable);
//        LoadingFooter.State state = getFooterViewState(recyclerView);
//        if (state == LoadingFooter.State.Loading) {
//            /*the state is Loading, just wait...*/
//            return;
//        }
        if (loadEnable) {
            /*Loading more ...*/
            setFooterViewState(getContext(), recyclerView, 0, LoadingFooter.State.Loading, null, null);
        } else {
            /*the end ...*/
            setFooterViewState(getContext(), recyclerView, 0, mState, null, null);
        }
    }

    public void setLoadingTextResId(int loadingTextResId) {
        this.loadingTextResId = loadingTextResId;
    }

    public void setDataEndResId(int dataEndResId) {
        this.dataEndResId = dataEndResId;
    }

    @Override
    protected void onScrollStateChanged(RecyclerView recyclerView) {
        super.onScrollStateChanged(recyclerView);
        RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
        lastVisibleItemPosition = -1;
        if(layoutManager instanceof StaggeredGridLayoutManager){
            int[] span = ((StaggeredGridLayoutManager)layoutManager).findLastVisibleItemPositions(null);
            if(span != null && span.length>0){
                lastVisibleItemPosition = span[0];
            }
        }
//        if (layoutManager instanceof SuitedLayoutManager) {
//            //这里还需计最后一个可见的未知是否小于最后一个位置
//            lastVisibleItemPosition = -1;
//            boolean isNull = recyclerView.getChildCount() == 0;
//            if (isNull) {
//                return;
//            }
//            lastVisibleItemPosition = ((SuitedLayoutManager) layoutManager).getmLastVisiblePosition();
//        }
    }

    @Override
    protected void onRefreshPrepare() {
        super.onRefreshPrepare();
        setFooterViewState(getContext(), (RecyclerView) mContent, 0, LoadingFooter.State.Normal, null, null);
        header.onUIRefreshPrepare(null);
    }

    @Override
    public boolean canChildScrollUp(View view) {
//        if (mContent instanceof RecyclerView) {
//            if (((RecyclerView) mContent).getLayoutManager() instanceof SuitedLayoutManager) {
//                SuitedLayoutManager layoutManager = (SuitedLayoutManager) ((RecyclerView) mContent).getLayoutManager();
//                return ((RecyclerView) mContent).getChildCount() > 0
//                        && (layoutManager.findFirstVisibleItemPosition() > 0 || ((RecyclerView) mContent).getChildAt(0)
//                        .getTop() < mContent.getPaddingTop());
//            }
//        }

        return super.canChildScrollUp(view);
    }

    public void setTimeName(String time_name) {
        if (header == null) {
            return;
        }
        header.setSaveTimeName(time_name);
    }

    public GifImageView getGifImageView() {
        if (header != null) {
            return header.getGifImageView();
        }
        return null;
    }

    public void clear() {
        if (header == null) {
            return;
        }
        header.clear();
    }

    private void initViews() {
        header = new AnimHeaderView(getContext());
        if (pullDownRefreshResId != 0) {
            header.setPullDownRefreshResId(pullDownRefreshResId);
        }
        if (pullDownReleaseResId != 0) {
            header.setPullDownReleaseResIdResId(pullDownReleaseResId);
        }
        if (pullDownLoading != 0) {
            header.setPullDownLoading(pullDownLoading);
        }
        setHeaderView(header);
    }


}
