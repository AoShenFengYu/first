package com.qisiemoji.apksticker.whatsapp.viewholders;


import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.qisiemoji.apksticker.domain.BaseItem;
import com.qisiemoji.apksticker.whatsapp.StickerPack;

import java.util.List;

public abstract class BaseWaMainViewHolder<E extends StickerPack> extends RecyclerView.ViewHolder {

    public BaseWaMainViewHolder(View itemView) {
        super(itemView);
    }

    public abstract void fillView(E entity);

}
