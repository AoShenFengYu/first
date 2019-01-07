package com.qisiemoji.apksticker.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.qisiemoji.apksticker.R;
import com.qisiemoji.apksticker.domain.RecommendItem;
import com.qisiemoji.apksticker.viewholders.BaseMainViewHolder;
import com.qisiemoji.apksticker.viewholders.MainLocalViewHolder;
import com.qisiemoji.apksticker.viewholders.MainType2ViewHolder;

import java.util.ArrayList;

public class MainAdapter extends RecyclerView.Adapter<BaseMainViewHolder<RecommendItem>> {
    public static final int MAIN_SHOW_TYPE_LOCAL = 1;
    public static final int MAIN_SHOW_TYPE2 = 2;

    private ArrayList<RecommendItem> dataSet = new ArrayList<RecommendItem>();

    private Context context;

    public MainAdapter(Context context){
        this.context = context;
    }

    public void setDataSet(ArrayList<RecommendItem> dataSet) {
        this.dataSet = dataSet;
    }

    @Override
    public BaseMainViewHolder<RecommendItem> onCreateViewHolder(ViewGroup parent, int viewType) {
        switch(viewType){
            case MAIN_SHOW_TYPE_LOCAL:
                return onCreateLocalHolder(parent);
            case MAIN_SHOW_TYPE2:
                return onCreateType1Holder(parent);
        }
        return onCreateLocalHolder(parent);
    }

    @Override
    public void onBindViewHolder(BaseMainViewHolder<RecommendItem> holder, int position) {
        holder.fillView(dataSet.get(position));
    }

    @Override
    public int getItemViewType(int position) {
        return dataSet.get(position).getShowType();
    }

    private BaseMainViewHolder onCreateLocalHolder(ViewGroup parent){
        Context context = parent.getContext();
        View itemView = LayoutInflater.from(context).inflate(R.layout.viewholder_main_local, null, false);
        MainLocalViewHolder viewHolder = new MainLocalViewHolder(itemView, context);
        itemView.setTag(viewHolder);
        return viewHolder;
    }

    private BaseMainViewHolder onCreateType1Holder(ViewGroup parent){
        Context context = parent.getContext();
        View itemView = LayoutInflater.from(context).inflate(R.layout.viewholder_main_type2, null, false);
        MainType2ViewHolder viewHolder = new MainType2ViewHolder(itemView, context);
        itemView.setTag(viewHolder);
        return viewHolder;
    }

    @Override
    public int getItemCount() {
        return dataSet.size();
    }
}
