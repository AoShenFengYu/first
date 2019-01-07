package com.qisiemoji.apksticker.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.qisiemoji.apksticker.R;
import com.qisiemoji.apksticker.domain.StickerItem;
import com.qisiemoji.apksticker.util.CommonUtil;
import com.qisiemoji.apksticker.util.DensityUtil;
import com.qisiemoji.apksticker.viewholders.BaseMainViewHolder;
import com.qisiemoji.apksticker.viewholders.ChildLocalViewHolder;

import java.util.ArrayList;

public class LocalStickerAdapter extends RecyclerView.Adapter<BaseMainViewHolder<StickerItem>> {

    private Context context;

    private ArrayList<StickerItem> dataSet = new ArrayList<StickerItem>();

    public LocalStickerAdapter(Context context){
        this.context = context;
        for(int i = 0;i<12;i++){
            StickerItem item = new StickerItem();
            item.setUrl("file_"+i);
            dataSet.add(item);
        }
    }

    @Override
    public BaseMainViewHolder<StickerItem> onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        View itemView = LayoutInflater.from(context).inflate(R.layout.viewholder_child_local, parent, false);
        ViewGroup.LayoutParams params = itemView.getLayoutParams();
        params.height = DensityUtil.dp2px(context,80);
        params.width = DensityUtil.dp2px(context,80);
        itemView.setLayoutParams(params);
        ChildLocalViewHolder viewHolder = new ChildLocalViewHolder(itemView, context);
        itemView.setTag(viewHolder);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(BaseMainViewHolder<StickerItem> holder, int position) {
        if(position>7){
            int kikaState = CommonUtil.kikaState(context);
            if(kikaState==3){
                dataSet.get(position).setLocked(false);
            }else{
                dataSet.get(position).setLocked(true);
            }
        }
        holder.fillView(dataSet.get(position));
    }

    @Override
    public int getItemCount() {
        return dataSet.size();
    }
}
