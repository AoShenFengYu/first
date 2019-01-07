package com.qisiemoji.apksticker.activities;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.qisiemoji.apksticker.BuildConfig;
import com.qisiemoji.apksticker.R;
import com.qisiemoji.apksticker.adapters.MainAdapter;
import com.qisiemoji.apksticker.adapters.RefreshRecyclerViewAdapter;
import com.qisiemoji.apksticker.domain.RecommendItem;
import com.qisiemoji.apksticker.domain.RecommendItems;
import com.qisiemoji.apksticker.domain.ResultData;
import com.qisiemoji.apksticker.recyclerview.SpacesItemDecoration;
import com.qisiemoji.apksticker.recyclerview.ptr.listener.IRefreshListener;
import com.qisiemoji.apksticker.recyclerview.refresh.CustomRefreshFrameLayout;
import com.qisiemoji.apksticker.request.RequestManager;
import com.qisiemoji.apksticker.util.DensityUtil;
import com.qisiemoji.apksticker.util.PrivacyHelper;
import com.qisiemoji.apksticker.util.SharedPreferencesUtils;
import com.qisiemoji.apksticker.util.StringUtil;
import com.qisiemoji.apksticker.views.ActivateDialog;
import com.qisiemoji.apksticker.whatsapp.WhitelistCheck;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements IRefreshListener,View.OnClickListener {

    private static String PRIVACY_POLICY = "To improve your using experience, %s " +
            "needs to collect necessary data. Please accept our privacy policy " +
            "to continue to use our service. For the details, please refer to Pr" +
            "ivacy Policy.";
    private final static String PRIVACY_URL = "http://www.kikatech.com/privacy/";

    private final static String PRIVACY_ITEM = "privacy_agree";

    private AlertDialog mPolicyDialog,waDialog;

    private RecyclerView recyclerView;

    private MainAdapter adapter;

    private RefreshRecyclerViewAdapter refreshAdapter;

    protected CustomRefreshFrameLayout waveChannel;

    private RelativeLayout waBtnLayout;

    private int page;

    private ArrayList<RecommendItem> dataSet = new ArrayList<RecommendItem>();

    public static final String EXTRA_STICKER_PACK_ID = "sticker_pack_id";
    public static final String EXTRA_STICKER_PACK_AUTHORITY = "sticker_pack_authority";
    public static final String EXTRA_STICKER_PACK_NAME = "sticker_pack_name";
    public static final int ADD_PACK = 200;
    protected void addStickerPackToWhatsApp(String identifier, String stickerPackName) {
        statistic("whatsapp");
        Intent intent = new Intent();
        intent.setAction("com.whatsapp.intent.action.ENABLE_STICKER_PACK");
        intent.putExtra(EXTRA_STICKER_PACK_ID, identifier);
        intent.putExtra(EXTRA_STICKER_PACK_AUTHORITY,
                BuildConfig.CONTENT_PROVIDER_AUTHORITY);
        intent.putExtra(EXTRA_STICKER_PACK_NAME, stickerPackName);
        try {
            startActivityForResult(intent, ADD_PACK);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, "whatsapp error", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.i("xthwa","resultcode="+resultCode);
        if (requestCode == ADD_PACK) {
            // add fail
            if (resultCode == Activity.RESULT_CANCELED) {
                if (data != null) {
                    final String validationError = data.getStringExtra("validation_error");
                    if (validationError != null) {
                        Log.i("xthwa","validationError="+validationError);
                    }
                } else {
                    //show update dialog
                    showWADialog();
                }
            }else{
                // added
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (PrivacyHelper.isEu(this)) {
            initPolicyAlertDialog();
        }else{
            if(!SharedPreferencesUtils.getBoolean(this,"show_kika_dialog",false)){
                showDialog();
                SharedPreferencesUtils.setBoolean(this,"show_kika_dialog",true);
            }
        }
        statistic("show");
        if(!SharedPreferencesUtils.getBoolean(this,"install_statistic")){
            statistic("install");
            SharedPreferencesUtils.setBoolean(this,"install_statistic",true);
        }
        recyclerView = findViewById(R.id.sticker_recyclerview);
        waBtnLayout = findViewById(R.id.main_activity_wa_btn_layout);
        waBtnLayout.setOnClickListener(this);

        adapter = new MainAdapter(this);
        refreshAdapter = new RefreshRecyclerViewAdapter<>(adapter);
        refreshAdapter.footerVisible(true);

        generateLocalData();
        adapter.setDataSet(dataSet);

        GridLayoutManager mLayoutManager = new GridLayoutManager(this, 1);
        recyclerView.setLayoutManager(mLayoutManager);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(),
                mLayoutManager.getOrientation());
        recyclerView.addItemDecoration(dividerItemDecoration);
        recyclerView.addItemDecoration(new SpacesItemDecoration(DensityUtil.dp2px(this, 8)));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapter);

        waveChannel = findViewById(R.id.wave_channel);
        waveChannel.disableWhenHorizontalMove(true);
        waveChannel.setTimeName(this.getClass().getSimpleName());
        waveChannel.setLoadingTextResId(R.string.str_footer_loading);
        waveChannel.setListener(this);
        waveChannel.setRefreshEnable(true);
        waveChannel.setLoadMoreEnable(true);
        fetch();
        //waveChannel.setLoadMoreEnable(false,"ssss");
    }

    @Override
    protected void onResume() {
        super.onResume();
        adapter.notifyItemChanged(0);
        adapter.notifyDataSetChanged();

    }

    private void showDialog(){
//        String str = FeatureConfig.getInstance().getString("sticker2_apkstyle","0");
//        if("0".equals(str)){
        final ActivateDialog dialog = new ActivateDialog(this,R.style.CustomDialog);
        dialog.show();
//        }else{
//            final ActivateDialog2 dialog = new ActivateDialog2(this,R.style.CustomDialog);
//            dialog.show();
//        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (waveChannel != null) {
            waveChannel.setListener(null);
            waveChannel.removeAllViews();
            waveChannel.clearAnimation();
        }
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
        dataSet.clear();
        generateLocalData();
        if(adapter != null){
            adapter.notifyDataSetChanged();
        }
    }

    private void generateLocalData(){
        RecommendItem item = new RecommendItem();
        item.setShowType(1);
        dataSet.add(item);
    }

    /**
     * 请求gif数据
     */
    private void fetch() {
        Call<ResultData<RecommendItems>> call;
        call = RequestManager.getInstance(this).stickerApi().fetchHomePage(page, 20);
        call.enqueue(new RequestManager.Callback<ResultData<RecommendItems>>() {

            @Override
            public void success(Response<ResultData<RecommendItems>> response, ResultData<RecommendItems> result) {
                if (result == null || result.data == null || result.data.infos == null
                        || result.data.infos.size() == 0) {
                    RequestManager.removeCache(RequestManager.getInstance(MainActivity.this).getKikaClient()
                            , response.raw().request());
                    if(waveChannel != null){
                        waveChannel.refreshComplete(true);
                    }
                    return;
                }
                updateUI(result.data.infos);
            }

            @Override
            public void onFailure(Call<ResultData<RecommendItems>> call, Throwable t) {
                super.onFailure(call, t);
                if(waveChannel != null){
                    waveChannel.refreshComplete(true);
                }
            }

//            @Override
//            public void unauthenticated(Response<ResultData<Sticker2.Stickers>> response) {
//                super.unauthenticated(response);
//            }
//
//            @Override
//            public void clientError(Response<ResultData<Sticker2.Stickers>> response
//                    , RequestManager.Error error, String message) {
//                super.clientError(response, error, message);
//            }
//
//            @Override
//            public void serverError(Response<ResultData<Sticker2.Stickers>> response, String message) {
//                super.serverError(response, message);
//            }
//
//            @Override
//            public void networkError(IOException e) {
//                super.networkError(e);
//            }
//
//            @Override
//            public void unexpectedError(Throwable e) {
//                super.unexpectedError(e);
//            }
        });
    }

    private void updateUI(List<RecommendItem> infos) {
        if (adapter == null || waveChannel == null) {
            return;
        }
        page++;
        waveChannel.refreshComplete(true);
        dataSet.addAll(infos);
        adapter.notifyDataSetChanged();
    }

    private void statistic(String item){
//        Tracker.Extra extra = TrackerCompat.getExtra(this);
//        extra.put("packageName",getPackageName());
//        TrackerCompat.getTracker().logEventRealTime("sticker_apk_main",item,"type",extra);
    }

    private void showWADialog() {
        View dialogContent = getLayoutInflater().inflate(R.layout.dialog_wa, null);
        TextView update = dialogContent.findViewById(R.id.dialog_wa_update);
        TextView cancel = dialogContent.findViewById(R.id.dialog_wa_cancel);

        update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final PackageManager packageManager = getPackageManager();
                final boolean whatsAppInstalled = WhitelistCheck.isPackageInstalled(WhitelistCheck.CONSUMER_WHATSAPP_PACKAGE_NAME, packageManager);
                final boolean smbAppInstalled = WhitelistCheck.isPackageInstalled(WhitelistCheck.SMB_WHATSAPP_PACKAGE_NAME, packageManager);
                final String playPackageLinkPrefix = "http://play.google.com/store/apps/details?id=";
                if (whatsAppInstalled && smbAppInstalled) {
                    launchPlayStoreWithUri("https://play.google.com/store/apps/developer?id=WhatsApp+Inc.");
                } else if (whatsAppInstalled) {
                    launchPlayStoreWithUri(playPackageLinkPrefix + WhitelistCheck.CONSUMER_WHATSAPP_PACKAGE_NAME);
                } else if (smbAppInstalled) {
                    launchPlayStoreWithUri(playPackageLinkPrefix + WhitelistCheck.SMB_WHATSAPP_PACKAGE_NAME);
                }
            }
        });
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(waDialog != null){
                    waDialog.dismiss();
                }
            }
        });

        AlertDialog.Builder builder = new AlertDialog.Builder(this,R.style.CustomDialog);
        waDialog = builder.setView(dialogContent).setCancelable(true).create();
        if (null != waDialog.getWindow()) {
            Window dialogWindow = waDialog.getWindow();
            dialogWindow.setDimAmount(0.4f);
            WindowManager.LayoutParams lp = dialogWindow.getAttributes();
            DisplayMetrics d = getResources().getDisplayMetrics(); // 获取屏幕宽、高用
            lp.width = (int) (d.widthPixels * 0.8); // 高度设置为屏幕的0.6
            dialogWindow.setAttributes(lp);
        }
        waDialog.show();
    }

    private void launchPlayStoreWithUri(String uriString) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(uriString));
        intent.setPackage("com.android.vending");
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, R.string.cannot_find_play_store, Toast.LENGTH_LONG).show();
        }
    }

    private void initPolicyAlertDialog() {
        boolean isAgree = SharedPreferencesUtils.getBoolean(this,PRIVACY_ITEM,false);
        if (isAgree) {
            if(!SharedPreferencesUtils.getBoolean(this,"show_kika_dialog",false)){
                showDialog();
                SharedPreferencesUtils.setBoolean(this,"show_kika_dialog",true);
            }
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

        if(!SharedPreferencesUtils.getBoolean(this,"show_kika_dialog",false)){
            showDialog();
            SharedPreferencesUtils.setBoolean(this,"show_kika_dialog",true);
        }
    }

    private void toWebView() {
        Uri uri = Uri.parse(PRIVACY_URL);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.main_activity_wa_btn_layout:
                addStickerPackToWhatsApp("1",getString(R.string.app_name));
                break;
        }
    }
}
