package com.qisiemoji.apksticker.whatsapp;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.qisiemoji.apksticker.R;
import com.qisiemoji.apksticker.domain.ResultData;
import com.qisiemoji.apksticker.recyclerview.ptr.listener.IRefreshListener;
import com.qisiemoji.apksticker.recyclerview.refresh.CustomRefreshFrameLayout;
import com.qisiemoji.apksticker.request.RequestManager;
import com.qisiemoji.apksticker.util.PrivacyHelper;
import com.qisiemoji.apksticker.util.SharedPreferencesUtils;
import com.qisiemoji.apksticker.util.StringUtil;
import com.qisiemoji.apksticker.whatsapp.search.SearchActivity;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;


public class StickerPackListActivity extends AddStickerPackActivity implements IRefreshListener {

    public static final int USER_DOWNLOAD_SUCCESS = 100;
    public static final int USER_DOWNLOADING = 101;
    public static final int USER_DOWNLOAD_FAIL = 102;

    private static String PRIVACY_POLICY = "To improve your using experience, %s " +
            "needs to collect necessary data. Please accept our privacy policy " +
            "to continue to use our service. For the details, please refer to Pr" +
            "ivacy Policy.";
    private final static String PRIVACY_URL = "http://www.kikatech.com/privacy/";

    private final static String PRIVACY_ITEM = "privacy_agree";

    private AlertDialog mPolicyDialog;
    public static final String EXTRA_STICKER_PACK_LIST_DATA = "sticker_pack_list";
    private LinearLayoutManager packLayoutManager;
    private RecyclerView packRecyclerView;
    private StickerPackListAdapter allStickerPacksListAdapter;
    private WhiteListCheckAsyncTask whiteListCheckAsyncTask;
    public static ArrayList<StickerPack> stickerPackList = new ArrayList<>();

    private int page;

    private Handler handler;

    private View progressBar;

    private RelativeLayout searchLayout;

    protected CustomRefreshFrameLayout waveChannel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fresco.initialize(this);
        setContentView(R.layout.activity_sticker_pack_list);
        packRecyclerView = findViewById(R.id.sticker_pack_list);
        searchLayout = findViewById(R.id.sticker_search_layout);
        searchLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(StickerPackListActivity.this, SearchActivity.class);
                StickerPackListActivity.this.startActivity(intent);
            }
        });
        handler = new MyHandler(this);
        if (PrivacyHelper.isEu(this)) {
            initPolicyAlertDialog();
        }
        statistic("show");
        if(!SharedPreferencesUtils.getBoolean(this,"install_statistic")){
            statistic("install");
            SharedPreferencesUtils.setBoolean(this,"install_statistic",true);
        }
        allStickerPacksListAdapter = new StickerPackListAdapter(this,stickerPackList, onAddButtonClickedListener);
        packRecyclerView.setAdapter(allStickerPacksListAdapter);
        packLayoutManager = new LinearLayoutManager(this);
        packLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(
                packRecyclerView.getContext(),
                packLayoutManager.getOrientation()
        );
        packRecyclerView.addItemDecoration(dividerItemDecoration);
        packRecyclerView.setLayoutManager(packLayoutManager);
        waveChannel = findViewById(R.id.wave_channel);
        waveChannel.disableWhenHorizontalMove(true);
        waveChannel.setTimeName(this.getClass().getSimpleName());
        waveChannel.setLoadingTextResId(R.string.str_footer_loading);
        waveChannel.setListener(this);
        waveChannel.setRefreshEnable(true);
        waveChannel.setLoadMoreEnable(true);
        //waveChannel.setLoadMoreEnable(false,"ssss");
        progressBar = findViewById(R.id.entry_activity_progress);

        reset();
        fetch();
    }

    @Override
    protected void onPause() {
        super.onPause();
//        if (whiteListCheckAsyncTask != null && !whiteListCheckAsyncTask.isCancelled()) {
//            whiteListCheckAsyncTask.cancel(true);
//        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (waveChannel != null) {
            waveChannel.setListener(null);
            waveChannel.removeAllViews();
            waveChannel.clearAnimation();
        }
    }

    /**
     * 获取数据
     */
    private void fetch(){
        waveChannel.setLoadingStatus();
        Call<ResultData<StickerPacksData>> call;
        call = RequestManager.getInstance(this).stickerApi().fetchWaStore(page, 20);
        call.enqueue(new RequestManager.Callback<ResultData<StickerPacksData>>() {

            @Override
            public void success(Response<ResultData<StickerPacksData>> response, ResultData<StickerPacksData> result) {
                if(waveChannel != null){
                    waveChannel.refreshComplete();
                }

                if (result == null || result.data == null || result.data.info == null
                        || result.data.info.stickerPacks == null || result.data.info.stickerPacks.size() == 0) {
                    RequestManager.removeCache(RequestManager.getInstance(StickerPackListActivity.this).getKikaClient()
                            , response.raw().request());
                    if(progressBar != null){
                        progressBar.setVisibility(View.GONE);
                    }
                    return;
                }
                if(result.data.info.stickerPacks.size() < 20){
                    if(waveChannel != null){
                        waveChannel.setLoadMoreEnable(false);
                    }
                }
                showStickerPackList(result.data.info.stickerPacks);
            }

            @Override
            public void onFailure(Call<ResultData<StickerPacksData>> call, Throwable t) {
                super.onFailure(call, t);
                if(waveChannel != null){
                    waveChannel.refreshComplete();
                }
                if(progressBar != null){
                    progressBar.setVisibility(View.GONE);
                }
            }
        });
    }

    private void showStickerPackList(ArrayList<StickerPack> stickerPacks) {
        if(waveChannel == null || allStickerPacksListAdapter == null){
            return;
        }
        if(progressBar != null){
            progressBar.setVisibility(View.GONE);
        }
        page++;
        Sticker sticker = null;

        for(StickerPack pack:stickerPacks){
            pack.trayImageFile = "tryImage"+pack.trayImageUrl.substring(pack.trayImageUrl.lastIndexOf("."));
            if(pack.stickers == null){
                continue;
            }
            for(int i=0;i<pack.stickers.size();i++){
                sticker = pack.stickers.get(i);
                sticker.imageFileName = "file_"+String.valueOf(i)+".webp";
            }
        }
        StickerContentProvider.addWaStickerPackToMatcher(stickerPacks);
        stickerPackList.addAll(stickerPacks);
        allStickerPacksListAdapter.setStickerPackList(stickerPackList);
        allStickerPacksListAdapter.notifyDataSetChanged();

        //搜索所有已经添加至whatsapp的stickerpack
//        whiteListCheckAsyncTask = new WhiteListCheckAsyncTask(this);
//        whiteListCheckAsyncTask.execute(stickerPackList.toArray(new StickerPack[stickerPackList.size()]));
    }


    private final StickerPackListAdapter.OnAddButtonClickedListener onAddButtonClickedListener
            = new StickerPackListAdapter.OnAddButtonClickedListener() {
        @Override
        public void onAddButtonClicked(StickerPack pack) {
            if(pack == null){
                return;
            }
            statistic("download");
            //点击添加到whatsapp后，开始下载图片
            WaStickerDownloadRunnable thread = new WaStickerDownloadRunnable(StickerPackListActivity.this,handler,pack);
            thread.start();
            pack.startDownload(thread);
            notifyDataSet();
        }

        @Override
        public void onCloseDownload(StickerPack pack){
            if(pack == null){
                return;
            }
            pack.endDownload();
            notifyDataSet();
        }
    };

    private void initPolicyAlertDialog() {
        boolean isAgree = SharedPreferencesUtils.getBoolean(this,PRIVACY_ITEM,false);
        if (isAgree) {
            return;
        }

        View dialogContent = getLayoutInflater().inflate(R.layout.dialog_privacy, null);
        TextView tvContent = dialogContent.findViewById(R.id.tv_content);
        String strContent = String.format(PRIVACY_POLICY, StringUtil.getStickerName(this));
        SpannableString spannableString = new SpannableString(strContent);
        int spanStart = strContent.length() - 15;
        int spanEnd = strContent.length() - 1;
        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                toWebView();
            }
        };
        spannableString.setSpan(clickableSpan, spanStart, spanEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        ForegroundColorSpan colorSpan = new ForegroundColorSpan(Color.parseColor("#19B4AD"));
        spannableString.setSpan(colorSpan, spanStart, spanEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        tvContent.setText(spannableString);
        tvContent.setMovementMethod(LinkMovementMethod.getInstance());
        TextView btAgree = dialogContent.findViewById(R.id.btn_agree);
        btAgree.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                agreePolicy();
            }
        });
        AlertDialog.Builder builder = new AlertDialog.Builder(this,R.style.CustomDialog);
        mPolicyDialog = builder.setView(dialogContent).setCancelable(false).create();
        if (null != mPolicyDialog.getWindow()) {
            Window dialogWindow = mPolicyDialog.getWindow();
            dialogWindow.setDimAmount(0.9f);
            WindowManager.LayoutParams lp = dialogWindow.getAttributes();
            DisplayMetrics d = getResources().getDisplayMetrics(); // 获取屏幕宽、高用
            lp.width = (int) (d.widthPixels * 0.8); // 高度设置为屏幕的0.6
            dialogWindow.setAttributes(lp);
        }
        mPolicyDialog.show();
    }

    private void agreePolicy() {
        mPolicyDialog.dismiss();
        SharedPreferencesUtils.setBoolean(this,PRIVACY_ITEM,true);
    }

    private void toWebView() {
        Uri uri = Uri.parse(PRIVACY_URL);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    private void notifyDataSet(){
        if(allStickerPacksListAdapter == null){
            return;
        }
        allStickerPacksListAdapter.notifyDataSetChanged();
    }

    @Override
    public void onLoadMore(int newState) {
        fetch();
    }

    @Override
    public void onRefresh(boolean isPassive) {
        reset();
        fetch();
    }

    private void reset(){
        page = 0;
        stickerPackList.clear();
        if(allStickerPacksListAdapter != null){
            allStickerPacksListAdapter.notifyDataSetChanged();
        }

        if(progressBar != null){
            progressBar.setVisibility(View.VISIBLE);
        }

        if(waveChannel != null){
            waveChannel.setLoadMoreEnable(true);
        }
    }

    private void statistic(String item){
//        Tracker.Extra extra = TrackerCompat.getExtra(this);
//        TrackerCompat.getTracker().logEventRealTime("sticker_store_apk_main",item,"type",extra);
    }

    static class WhiteListCheckAsyncTask extends AsyncTask<StickerPack, Void, List<StickerPack>> {
        private final WeakReference<StickerPackListActivity> stickerPackListActivityWeakReference;

        WhiteListCheckAsyncTask(StickerPackListActivity stickerPackListActivity) {
            this.stickerPackListActivityWeakReference = new WeakReference<>(stickerPackListActivity);
        }

        @Override
        protected final List<StickerPack> doInBackground(StickerPack... stickerPackArray) {
            final StickerPackListActivity stickerPackListActivity = stickerPackListActivityWeakReference.get();
            if (stickerPackListActivity == null) {
                return Arrays.asList(stickerPackArray);
            }
            for (StickerPack stickerPack : stickerPackArray) {
                stickerPack.setIsWhitelisted(WhitelistCheck.isWhitelisted(stickerPackListActivity, stickerPack.identifier));
            }
            return Arrays.asList(stickerPackArray);
        }

        @Override
        protected void onPostExecute(List<StickerPack> stickerPackList) {
            final StickerPackListActivity stickerPackListActivity = stickerPackListActivityWeakReference.get();
            if (stickerPackListActivity != null) {
                stickerPackListActivity.allStickerPacksListAdapter.setStickerPackList(stickerPackList);
                stickerPackListActivity.allStickerPacksListAdapter.notifyDataSetChanged();
            }
        }
    }

    private static class MyHandler extends Handler {
        WeakReference<StickerPackListActivity> thisLayout;

        StickerPack pack = null;
        MyHandler(StickerPackListActivity layout) {
            thisLayout = new WeakReference<>(layout);
        }

        @Override
        public void handleMessage(Message msg) {
            final StickerPackListActivity theLayout = thisLayout.get();
            if (theLayout == null) {
                return;
            }

            switch (msg.what) {
                case USER_DOWNLOAD_SUCCESS:
                    pack = (StickerPack)msg.obj;
                    pack.endDownload();
                    theLayout.addStickerPackToWhatsApp(pack.identifier, pack.name);
                    break;
                case USER_DOWNLOADING:
                    pack = (StickerPack)msg.obj;
                    theLayout.notifyDataSet();
                    break;
                case USER_DOWNLOAD_FAIL:
                    pack = (StickerPack)msg.obj;
                    pack.endDownload();
                    theLayout.notifyDataSet();
                    break;
                default:
                    break;
            }
        }
    }
}
