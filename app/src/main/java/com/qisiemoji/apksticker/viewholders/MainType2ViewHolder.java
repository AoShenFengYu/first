package com.qisiemoji.apksticker.viewholders;

import android.content.Context;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.qisiemoji.apksticker.R;
import com.qisiemoji.apksticker.adapters.ChildStickerAdapter;
import com.qisiemoji.apksticker.adapters.LocalStickerAdapter;
import com.qisiemoji.apksticker.domain.RecommendItem;
import com.qisiemoji.apksticker.recyclerview.SpacesItemDecoration;
import com.qisiemoji.apksticker.util.DensityUtil;

public class MainType2ViewHolder extends BaseMainViewHolder<RecommendItem> {

    private Context context;
    private RecyclerView recyclerView;
    private TextView title;
    private ChildStickerAdapter adapter;

    public MainType2ViewHolder(View itemView, Context context) {
        super(itemView);
        this.context = context;
        title = itemView.findViewById(R.id.type1_title);
        recyclerView = itemView.findViewById(R.id.type1_recyclerview);

        adapter = new ChildStickerAdapter(context);
        GridLayoutManager mLayoutManager = new GridLayoutManager(context,4);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.addItemDecoration(new SpacesItemDecoration(DensityUtil.dp2px(context,8)));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void fillView(RecommendItem entity) {
        title.setText(entity.getLabel());
        adapter.setDataSet(entity.getChildren());
        adapter.notifyDataSetChanged();
    }

}
