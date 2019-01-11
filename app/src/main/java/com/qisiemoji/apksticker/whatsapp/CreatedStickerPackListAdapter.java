/*
 * Copyright (c) WhatsApp Inc. and its affiliates.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.qisiemoji.apksticker.whatsapp;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.qisiemoji.apksticker.R;
import com.qisiemoji.apksticker.util.DensityUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class CreatedStickerPackListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context context;

    private List<StickerPack> stickerPacks = new ArrayList<>();

    public interface CreatedStickerPackListAdapterListener {
        void onClickPack(StickerPack stickerPack);
    }
    private CreatedStickerPackListAdapterListener onClickListener;

    CreatedStickerPackListAdapter(Context context, CreatedStickerPackListAdapterListener onClickListener) {
        this.context = context;
        this.onClickListener = onClickListener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull final ViewGroup viewGroup, final int viewType) {
        final Context context = viewGroup.getContext();
        final LayoutInflater layoutInflater = LayoutInflater.from(context);
        return new ItemViewHolder(layoutInflater.inflate(R.layout.item_view_created_sticker_pack_list, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder viewHolder, final int index) {
        if (viewHolder instanceof ItemViewHolder) {
            ((ItemViewHolder)viewHolder).bind(stickerPacks.get(index), onClickListener);
        }

    }

    @Override
    public int getItemCount() {
        return stickerPacks.size();
    }

    public void setStickerPackList(List<StickerPack> stickerPackList) {
        this.stickerPacks.clear();
        this.stickerPacks.addAll(stickerPackList);
    }

    private class ItemViewHolder extends RecyclerView.ViewHolder {

        View itemView;
        ImageView icon;
        TextView packName;
        TextView author;
        TextView published;

        ItemViewHolder(final View itemView) {
            super(itemView);
            this.itemView = itemView;
            icon = itemView.findViewById(R.id.icon);
            packName = itemView.findViewById(R.id.pack_name);
            author = itemView.findViewById(R.id.author);
            published = itemView.findViewById(R.id.published);

        }

        void bind(final StickerPack stickerPack, final CreatedStickerPackListAdapterListener listener) {
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onClickPack(stickerPack);
                }
            });
            Glide.with(context)
                    .load(stickerPack.trayImageFile)
                    .placeholder(R.drawable.ic_launcher)
                    .into(icon);
            packName.setText(stickerPack.name);
            author.setText(stickerPack.publisher);
            published.setVisibility(stickerPack.getIsWhitelisted() ? View.VISIBLE : View.INVISIBLE);
        }
    }
}
