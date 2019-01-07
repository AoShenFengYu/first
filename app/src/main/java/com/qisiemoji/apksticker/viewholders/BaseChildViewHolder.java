package com.qisiemoji.apksticker.viewholders;


import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.qisiemoji.apksticker.domain.BaseItem;
import com.qisiemoji.apksticker.domain.ChildItem;

public abstract class BaseChildViewHolder<E extends ChildItem> extends RecyclerView.ViewHolder {

    public BaseChildViewHolder(View itemView) {
        super(itemView);
    }

    public abstract void fillView(E entity);

}
