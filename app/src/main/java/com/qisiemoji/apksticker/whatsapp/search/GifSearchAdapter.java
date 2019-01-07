package com.qisiemoji.apksticker.whatsapp.search;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.giphy.sdk.core.models.Media;
import com.qisiemoji.apksticker.R;

import java.util.LinkedList;

/**
 * Created by xm180319 on 2018/3/26.
 */

public class GifSearchAdapter extends RecyclerView.Adapter<BaseSearchViewHolder<Media>>{
    private LinkedList<Media> dataSet = new LinkedList<Media>();
    private Context context;
    private GifSearchClickListener listener;

    public GifSearchAdapter(Context context, GifSearchClickListener listener) {
        this.context = context;
        this.listener = listener;
    }

    public void setDataSet(LinkedList<Media> dataSet) {
        this.dataSet = dataSet;
    }

    public LinkedList<Media> getDataSet() {
        return dataSet;
    }

    @Override
    public BaseSearchViewHolder<Media> onCreateViewHolder(ViewGroup parent, int viewType) {
        return onCreateGifDisplayHolder(parent);
    }

    @Override
    public void onBindViewHolder(BaseSearchViewHolder<Media> holder, final int position) {
        holder.fillView(dataSet.get(position));
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(listener != null){
                    listener.clickItem(dataSet.get(position));
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return dataSet.size();
    }

    private BaseSearchViewHolder onCreateGifDisplayHolder(ViewGroup parent) {
        Context context = parent.getContext();
        View itemView = LayoutInflater.from(context).inflate(R.layout.gif_display_item, parent, false);
        GifSearchItemViewHolder viewHolder = new GifSearchItemViewHolder(itemView, context);
        itemView.setTag(viewHolder);
        return viewHolder;
    }
}
