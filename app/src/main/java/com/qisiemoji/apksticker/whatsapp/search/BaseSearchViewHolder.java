package com.qisiemoji.apksticker.whatsapp.search;

import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.giphy.sdk.core.models.Media;

/**
 * Created by xm180319 on 2018/3/26.
 */

public abstract class BaseSearchViewHolder<E extends Media> extends RecyclerView.ViewHolder {

    public BaseSearchViewHolder(View itemView) {
        super(itemView);
    }

    public abstract void fillView(E entity);
}
