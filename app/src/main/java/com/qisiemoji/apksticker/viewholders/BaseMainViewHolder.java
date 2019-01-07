package com.qisiemoji.apksticker.viewholders;


import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.qisiemoji.apksticker.domain.BaseItem;
import com.qisiemoji.apksticker.domain.StickerItem;

public abstract class BaseMainViewHolder<E extends BaseItem> extends RecyclerView.ViewHolder {

    public BaseMainViewHolder(View itemView) {
        super(itemView);
    }

    public abstract void fillView(E entity);

}
