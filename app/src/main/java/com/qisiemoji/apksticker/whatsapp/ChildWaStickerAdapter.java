package com.qisiemoji.apksticker.whatsapp;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.qisiemoji.apksticker.R;
import com.qisiemoji.apksticker.domain.ChildItem;
import com.qisiemoji.apksticker.util.DensityUtil;
import com.qisiemoji.apksticker.util.DisplayUtil;
import com.qisiemoji.apksticker.viewholders.BaseChildViewHolder;
import com.qisiemoji.apksticker.viewholders.ChildType2ViewHolder;
import com.qisiemoji.apksticker.whatsapp.viewholders.BaseWaChildViewHolder;
import com.qisiemoji.apksticker.whatsapp.viewholders.ChildWaType4ViewHolder;

import java.util.ArrayList;
import java.util.List;

public class ChildWaStickerAdapter extends RecyclerView.Adapter<BaseWaChildViewHolder<StickerPack>> {

    private Context context;

    private List<StickerPack> dataSet = new ArrayList<StickerPack>();

    public ChildWaStickerAdapter(Context context){
        this.context = context;
    }

    public void setDataSet(List<StickerPack> dataSet) {
        this.dataSet = dataSet;
    }

    @Override
    public BaseWaChildViewHolder<StickerPack>  onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        View itemView = LayoutInflater.from(context).inflate(R.layout.viewholder_child_type2, parent, false);
        ViewGroup.LayoutParams params = itemView.getLayoutParams();
        params.width = DisplayUtil.getScreenWidth(context)/3;
        params.height = params.width;
        itemView.setLayoutParams(params);
        ChildWaType4ViewHolder viewHolder = new ChildWaType4ViewHolder(itemView, context);
        itemView.setTag(viewHolder);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(BaseWaChildViewHolder<StickerPack> holder, int position) {
        holder.fillView(dataSet.get(position));
    }

    @Override
    public int getItemCount() {
        return dataSet.size();
    }
}
