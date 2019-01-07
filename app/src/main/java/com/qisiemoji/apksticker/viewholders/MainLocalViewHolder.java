package com.qisiemoji.apksticker.viewholders;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.qisiemoji.apksticker.R;
import com.qisiemoji.apksticker.adapters.LocalStickerAdapter;
import com.qisiemoji.apksticker.domain.RecommendItem;
import com.qisiemoji.apksticker.recyclerview.SpacesItemDecoration;
import com.qisiemoji.apksticker.util.CommonUtil;
import com.qisiemoji.apksticker.util.DensityUtil;
import com.qisiemoji.apksticker.util.StringUtil;

public class MainLocalViewHolder extends BaseMainViewHolder<RecommendItem> {

    private Context context;
    private RecyclerView recyclerView;
    private TextView title;
    private LocalStickerAdapter adapter;

    public MainLocalViewHolder(View itemView, Context context) {
        super(itemView);
        this.context = context;
        recyclerView = itemView.findViewById(R.id.main_local_recyclerview);
        title = itemView.findViewById(R.id.main_local_title);

        GridLayoutManager mLayoutManager = new GridLayoutManager(context,4);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.addItemDecoration(new SpacesItemDecoration(DensityUtil.dp2px(context,8)));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        adapter = new LocalStickerAdapter(context);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void fillView(RecommendItem entity) {
        title.setText(StringUtil.getStickerName(context));
        notifyDataSetChange();
    }

    public void notifyDataSetChange(){
        if(adapter == null){
            return;
        }
        adapter.notifyDataSetChanged();
    }

}
