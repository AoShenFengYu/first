package com.qisiemoji.apksticker.whatsapp.gifpick;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.qisiemoji.apksticker.R;
import com.qisiemoji.apksticker.util.DisplayUtil;

import java.util.LinkedList;

/**
 * Created by xm180319 on 2018/3/26.
 */

public class GifPickAdapter extends RecyclerView.Adapter<BaseGifPickViewHolder<GifPickItem>>{
    private LinkedList<GifPickItem> dataSet = new LinkedList<GifPickItem>();
    private Context context;
    private int clickItem = -1;
    private Handler handler;

    public GifPickAdapter(Context context,Handler handler) {
        this.context = context;
        this.handler = handler;
    }

    public void setDataSet(LinkedList<GifPickItem> dataSet) {
        this.dataSet = dataSet;
    }

    public LinkedList<GifPickItem> getDataSet() {
        return dataSet;
    }

    @Override
    public BaseGifPickViewHolder<GifPickItem> onCreateViewHolder(ViewGroup parent, int viewType) {
        return onCreateGifDisplayHolder(parent);
    }

    public Bitmap obtainBitmap(){
        if(dataSet == null){
            return null;
        }
        if(clickItem < 0 || clickItem >= dataSet.size()){
            return null;
        }
        return dataSet.get(clickItem).bitmap;
    }

    @Override
    public void onBindViewHolder(BaseGifPickViewHolder<GifPickItem> holder, final int position) {
        holder.fillView(dataSet.get(position));
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clickItem = position;
                notifyDataSetChanged();
                handler.sendEmptyMessage(GifPickActivity.MSG_PICK);
            }
        });

        if(position == clickItem){
            holder.tick.setVisibility(View.VISIBLE);
        }else{
            holder.tick.setVisibility(View.GONE);
        }

        ViewGroup.LayoutParams params = holder.itemView.getLayoutParams();
        int width = DisplayUtil.getScreenWidth(context)/GifPickActivity.GIF_PICK_SPAN_COUNT;
        params.height = width;
//        params.height = width*info.tinyInfo.height/info.tinyInfo.width;
        holder.itemView.setLayoutParams(params);
    }

    @Override
    public int getItemCount() {
        return dataSet.size();
    }

    private BaseGifPickViewHolder onCreateGifDisplayHolder(ViewGroup parent) {
        Context context = parent.getContext();
        View itemView = LayoutInflater.from(context).inflate(R.layout.gif_pick_item, parent, false);
        GifPickViewHolder viewHolder = new GifPickViewHolder(itemView, context);
        itemView.setTag(viewHolder);
        return viewHolder;
    }
}
