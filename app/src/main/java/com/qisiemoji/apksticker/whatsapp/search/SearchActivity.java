package com.qisiemoji.apksticker.whatsapp.search;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;

import com.qisiemoji.apksticker.R;
import com.qisiemoji.apksticker.whatsapp.manager.WAStickerManager;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import static com.qisiemoji.apksticker.whatsapp.manager.WAStickerManager.EXTRA_SELECTED_LIST;
import static com.qisiemoji.apksticker.whatsapp.manager.WAStickerManager.REQUEST_CODE_EDIT_IMAGE;
import static com.qisiemoji.apksticker.whatsapp.manager.WAStickerManager.REQUEST_CODE_SELECT_ALBUM_STICKERS;
import static com.qisiemoji.apksticker.whatsapp.manager.WAStickerManager.RESULT_CODE_FINISH_EDIT_IMAGE;


public class SearchActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int PERMISSION_STORAGE_REQUEST_CODE = 100;
    public static final int SCAN_GIF_FILES_SUCCESS = 1000;
    public static final int SCAN_GIF_FILES_FAIL = 2000;

    private Handler handler;

//    private RelativeLayout header;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        handler = new MyHandler(this);
        setContentView(R.layout.activity_search);
        //有自动搜索则不弹出软键盘
//        if(!TextUtils.isEmpty(getIntent().getStringExtra("tag"))){
//            getWindow().setSoftInputMode( WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
//        }

//        header = (RelativeLayout)findViewById(R.id.search_header);
        //沉浸式
        if (Build.VERSION.SDK_INT >= 21) {
            View decorView = getWindow().getDecorView();
            int option = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
            decorView.setSystemUiVisibility(option);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }else{
//            header.setVisibility(View.GONE);
        }
        initFragment();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);ArrayList<String> list = data.getStringArrayListExtra(EXTRA_SELECTED_LIST);
        if(fragment == null){
            return;
        }
        if (list != null && list.size() > 0) {
            ((SearchFragment)fragment).handleReceivedImagePath(list.get(0));
        }
    }

    private Fragment fragment;
    private void initFragment(){
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        fragment = fm.findFragmentByTag("searchfragment");
        if(fragment == null){
            fragment = new SearchFragment();
            if(!TextUtils.isEmpty(getIntent().getStringExtra("tag"))){
                Bundle bundle = new Bundle();
                bundle.putString("tag",getIntent().getStringExtra("tag"));
                fragment.setArguments(bundle);
            }
            ft.add(R.id.activity_search_container,fragment,"searchfragment");
            ft.commitAllowingStateLoss();
        }else{
            ft.show(fragment);
            ft.commitAllowingStateLoss();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onClick(View v) {

    }

    private static class MyHandler extends Handler {
        WeakReference<SearchActivity> thisLayout;

        MyHandler(SearchActivity layout) {
            thisLayout = new WeakReference<SearchActivity>(layout);
        }

        @Override
        public void handleMessage(Message msg) {
            final SearchActivity theLayout = thisLayout.get();
            if (theLayout == null) {
                return;
            }

            switch (msg.what) {
                case SCAN_GIF_FILES_SUCCESS:
                    break;
                case SCAN_GIF_FILES_FAIL:
                    break;
            }
        }
    }
}