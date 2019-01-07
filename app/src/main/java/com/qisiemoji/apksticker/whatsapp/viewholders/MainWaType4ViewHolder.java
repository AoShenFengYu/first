package com.qisiemoji.apksticker.whatsapp.viewholders;

import android.content.Context;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.qisiemoji.apksticker.R;
import com.qisiemoji.apksticker.recyclerview.SpacesItemDecoration;
import com.qisiemoji.apksticker.util.DensityUtil;
import com.qisiemoji.apksticker.whatsapp.ChildWaStickerAdapter;
import com.qisiemoji.apksticker.whatsapp.StickerPack;

public class MainWaType4ViewHolder extends BaseWaMainViewHolder<StickerPack> {

    private Context context;
    private RecyclerView recyclerView;
    private TextView title;
    private ChildWaStickerAdapter adapter;

    public MainWaType4ViewHolder(View itemView, Context context) {
        super(itemView);
        this.context = context;
        title = itemView.findViewById(R.id.type1_title);
        recyclerView = itemView.findViewById(R.id.type1_recyclerview);

        adapter = new ChildWaStickerAdapter(context);
        GridLayoutManager mLayoutManager = new GridLayoutManager(context,3);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.addItemDecoration(new SpacesItemDecoration(DensityUtil.dp2px(context,4)));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void fillView(StickerPack entity) {
        adapter.setDataSet(entity.showList);
        adapter.notifyDataSetChanged();
    }

}
