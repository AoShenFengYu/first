package com.qisiemoji.apksticker.whatsapp.gifpick;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.giphy.sdk.core.models.Media;
import com.qisiemoji.apksticker.R;
import com.qisiemoji.apksticker.recyclerview.SpacesItemDecoration;
import com.qisiemoji.apksticker.util.GifDecoder;
import com.qisiemoji.apksticker.whatsapp.manager.WAStickerManager;

import java.io.File;
import java.io.FileInputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.LinkedList;

import static com.qisiemoji.apksticker.whatsapp.manager.WAStickerManager.EXTRA_SELECTED_LIST;
import static com.qisiemoji.apksticker.whatsapp.manager.WAStickerManager.RESULT_CODE_FINISH_EDIT_IMAGE;

public class GifPickActivity extends AppCompatActivity{
    public static final int GIF_PICK_SPAN_COUNT = 3;

    public static final int USER_DOWNLOAD_SUCCESS = 100;

    public static final int USER_DOWNLOAD_FAIL = 102;

    private RecyclerView recyclerView;

    private ImageView img;

    private TextView add;

    private GifPickAdapter adapter;

    private Handler handler;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gifpick);
        handler = new MyHandler(this);
        recyclerView = findViewById(R.id.gifpick_recycleview);
        img = findViewById(R.id.gifpick_img);
        add = findViewById(R.id.gifpick_add);
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clickConfirm();
            }
        });
        adapter = new GifPickAdapter(this);
        GridLayoutManager mLayoutManager = new GridLayoutManager(this,GIF_PICK_SPAN_COUNT);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.addItemDecoration(new SpacesItemDecoration(4));
        recyclerView.setAdapter(adapter);
    }

    private void clickConfirm(){
        if(adapter.obtainBitmap() == null){
            Toast.makeText(this,"Sorry,pick one please.",Toast.LENGTH_LONG).show();
            return;
        }
        WAStickerManager.SaveTempImageFileTask task = new WAStickerManager
                .SaveTempImageFileTask(GifPickActivity.this, adapter.obtainBitmap()
                , new WAStickerManager.SaveTempImageFileCallback() {
            @Override
            public void onFinishSaved(String path) {
                Intent intent = new Intent();
                ArrayList<String> urls = new ArrayList<>();
                urls.add(path);
                intent.putStringArrayListExtra(EXTRA_SELECTED_LIST, urls);
                setResult(RESULT_CODE_FINISH_EDIT_IMAGE, intent);
                finish();
            }
        });
        task.execute();
    }


    @Override
    protected void onResume() {
        super.onResume();
        Media media = getIntent().getParcelableExtra("media");
        if(media == null){
            return;
        }
        Thread thread = new GifPickDownloadRunnable(this,handler,media);
        thread.start();

        String url = "http://media2.giphy.com/media/"+media.getId()+"/100.gif";
        Glide.with(this).load(url).into(img);
//        Glide.with(this).load("https://media3.giphy.com/media/GCvktC0KFy9l6/200w.gif").into(img);

    }

    private void updateUI(File file){
        if(file == null){
            return;
        }
        try{
            FileInputStream fis = null;
            fis = new FileInputStream(file);
            GifDecoder gd = new GifDecoder();
            int result = gd.read(fis);
            if(result != 0){
                return;
            }
            LinkedList<GifPickItem> list = new LinkedList<>();
            for(int i = 0;i < gd.getFrameCount();i++){
                list.add(new GifPickItem((gd.getFrame(i))));
            }
            adapter.setDataSet(list);
            adapter.notifyDataSetChanged();
        }catch (Exception e){

        }
    }

    private static class MyHandler extends Handler {
        WeakReference<GifPickActivity> thisLayout;

        File file = null;
        MyHandler(GifPickActivity layout) {
            thisLayout = new WeakReference<>(layout);
        }

        @Override
        public void handleMessage(Message msg) {
            final GifPickActivity theLayout = thisLayout.get();
            if (theLayout == null) {
                return;
            }

            switch (msg.what) {
                case USER_DOWNLOAD_SUCCESS:
                    file = (File)msg.obj;
                    theLayout.updateUI(file);
                    break;
                case USER_DOWNLOAD_FAIL:
                    break;
            }
        }
    }
}
