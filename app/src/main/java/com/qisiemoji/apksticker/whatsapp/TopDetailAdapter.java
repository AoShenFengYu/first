package com.qisiemoji.apksticker.whatsapp;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.qisiemoji.apksticker.R;
import com.qisiemoji.apksticker.util.DisplayUtil;
import com.qisiemoji.apksticker.whatsapp.gifpick.BaseGifPickViewHolder;
import com.qisiemoji.apksticker.whatsapp.gifpick.GifPickActivity;
import com.qisiemoji.apksticker.whatsapp.gifpick.GifPickItem;
import com.qisiemoji.apksticker.whatsapp.gifpick.GifPickViewHolder;

import java.util.ArrayList;
import java.util.LinkedList;

/**
 * Created by xm180319 on 2018/3/26.
 */

public class TopDetailAdapter extends RecyclerView.Adapter<TopDetailViewHolder>{
    private ArrayList<StickerPack> dataSet = new ArrayList<StickerPack> ();
    private StickerPack pack;
    private Context context;
    private Handler handler;

    public TopDetailAdapter(Context context, Handler handler,ArrayList<StickerPack> dataSet,StickerPack pack) {
        this.context = context;
        this.handler = handler;
        this.dataSet = dataSet;
        this.pack = pack;
    }

    @Override
    public TopDetailViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return onCreateGifDisplayHolder(parent);
    }

    @Override
    public void onBindViewHolder(TopDetailViewHolder holder, final int position) {
        Glide.with(context).load(dataSet.get(position).trayImageFile).into(holder.img);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(dataSet == null){
                    return;
                }
                if(pack == null || TextUtils.isEmpty(pack.identifier)){
                    return;
                }
                if(pack.identifier.equals(dataSet.get(position).identifier)){
                    return;
                }
                Message msg = handler.obtainMessage();
                msg.obj = dataSet.get(position);
                msg.what = StickerPackDetailsActivity.MSG_CLICK_PACK;
                handler.sendMessage(msg);
            }
        });
//        ViewGroup.LayoutParams params = holder.itemView.getLayoutParams();
//        int width = DisplayUtil.getScreenWidth(context)/GifPickActivity.GIF_PICK_SPAN_COUNT;
//        params.height = width;
////        params.height = width*info.tinyInfo.height/info.tinyInfo.width;
//        holder.itemView.setLayoutParams(params);
    }

    @Override
    public int getItemCount() {
        return dataSet.size();
    }

    private TopDetailViewHolder onCreateGifDisplayHolder(ViewGroup parent) {
        Context context = parent.getContext();
        View itemView = LayoutInflater.from(context).inflate(R.layout.top_detail_item, parent, false);
        TopDetailViewHolder viewHolder = new TopDetailViewHolder(itemView, context);
        itemView.setTag(viewHolder);
        return viewHolder;
    }
}
