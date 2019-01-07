package com.qisiemoji.apksticker.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.qisiemoji.apksticker.R;
import com.qisiemoji.apksticker.domain.ChildItem;
import com.qisiemoji.apksticker.util.DensityUtil;
import com.qisiemoji.apksticker.viewholders.BaseChildViewHolder;
import com.qisiemoji.apksticker.viewholders.ChildType2ViewHolder;

import java.util.ArrayList;

public class ChildStickerAdapter extends RecyclerView.Adapter<BaseChildViewHolder<ChildItem>> {

    private Context context;

    private ArrayList<ChildItem> dataSet = new ArrayList<ChildItem>();

    public ChildStickerAdapter(Context context){
        this.context = context;
    }

    public void setDataSet(ArrayList<ChildItem> dataSet) {
        this.dataSet = dataSet;
    }

    @Override
    public BaseChildViewHolder<ChildItem>  onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        View itemView = LayoutInflater.from(context).inflate(R.layout.viewholder_child_type2, parent, false);
        ViewGroup.LayoutParams params = itemView.getLayoutParams();
        params.height = DensityUtil.dp2px(context,96);
        params.width = DensityUtil.dp2px(context,80);
        itemView.setLayoutParams(params);
        ChildType2ViewHolder viewHolder = new ChildType2ViewHolder(itemView, context);
        itemView.setTag(viewHolder);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(BaseChildViewHolder<ChildItem> holder, int position) {
        holder.fillView(dataSet.get(position));
    }

    @Override
    public int getItemCount() {
        return dataSet.size();
    }
}
