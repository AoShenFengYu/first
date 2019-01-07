/*
 * Copyright (c) WhatsApp Inc. and its affiliates.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.qisiemoji.apksticker.whatsapp;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
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
import android.widget.Toast;

import com.facebook.drawee.view.SimpleDraweeView;
import com.qisiemoji.apksticker.R;
import com.qisiemoji.apksticker.util.DensityUtil;
import com.qisiemoji.apksticker.viewholders.BaseMainViewHolder;
import com.qisiemoji.apksticker.viewholders.MainType2ViewHolder;
import com.qisiemoji.apksticker.whatsapp.viewholders.BaseWaMainViewHolder;
import com.qisiemoji.apksticker.whatsapp.viewholders.MainWaType1ViewHolder;
import com.qisiemoji.apksticker.whatsapp.viewholders.MainWaType2ViewHolder;
import com.qisiemoji.apksticker.whatsapp.viewholders.MainWaType3ViewHolder;
import com.qisiemoji.apksticker.whatsapp.viewholders.MainWaType4ViewHolder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class StickerPackListAdapter extends RecyclerView.Adapter<BaseWaMainViewHolder<StickerPack>> {
    public static int STICKER_PACK_DOWNLOADING = 1;

    public final static int TYPE_SEARCH = 1;
    public final static int TYPE_CREATE = 2;
    public final static int TYPE_LIST = 3;
    public final static int TYPE_ICON = 4;

    private Context context;
    private ArrayList<StickerPack> stickerPacks = new ArrayList<>();
    private final OnAddButtonClickedListener onAddButtonClickedListener;

    public StickerPackListAdapter(Context context, @NonNull List<StickerPack> packs, @NonNull OnAddButtonClickedListener onAddButtonClickedListener) {
        this.context = context;
        createShowItem(packs);
        this.onAddButtonClickedListener = onAddButtonClickedListener;
    }

    private void createShowItem(List<StickerPack> packs){
        stickerPacks.clear();
        //search
//        stickerPacks.add(new StickerPack(TYPE_SEARCH));
        //create
        stickerPacks.add(new StickerPack(TYPE_CREATE));
        if(packs == null || packs.size() == 0){
            return;
        }
        //list
        StickerPack pack;
        ArrayList<StickerPack> showList = new ArrayList();
        showList.addAll(packs);
        for(int i = 0; showList.size()>0 && i < 4; i++){
            pack = showList.get(0);
            pack.showType = TYPE_LIST;
            stickerPacks.add(pack);
            showList.remove(0);
        }
        if(packs.size() == 0){
            return;
        }
        //icon
        StickerPack iconPack = new StickerPack(TYPE_ICON);
        iconPack.showList = showList;
        stickerPacks.add(iconPack);
    }

    public void clean(){
        if(stickerPacks != null){
            stickerPacks.clear();
        }
    }

    @NonNull
    @Override
    public BaseWaMainViewHolder<StickerPack> onCreateViewHolder(@NonNull final ViewGroup viewGroup, final int viewType) {
        switch (viewType){
            case TYPE_SEARCH:
                return onCreateSearchHolder(viewGroup);
            case TYPE_CREATE:
                return onCreateCreatorHolder(viewGroup);
            case TYPE_LIST:
                return onCreateListItemHolder(viewGroup);
            case TYPE_ICON:
                return onCreateIconHolder(viewGroup);
            default:
                return onCreateListItemHolder(viewGroup);
        }
    }

    @Override
    public int getItemViewType(int position) {
        return stickerPacks.get(position).showType;
    }

    @Override
    public void onBindViewHolder(@NonNull final BaseWaMainViewHolder<StickerPack> viewHolder, final int index) {
        viewHolder.fillView(stickerPacks.get(index));
    }

    private BaseWaMainViewHolder<StickerPack> onCreateSearchHolder(ViewGroup viewGroup){
        Context context = viewGroup.getContext();
        final LayoutInflater layoutInflater = LayoutInflater.from(context);
        final View stickerPackRow = layoutInflater.inflate(R.layout.viewholder_wa_main_type1, viewGroup, false);
        MainWaType1ViewHolder viewHolder = new MainWaType1ViewHolder(context,stickerPackRow);
        stickerPackRow.setTag(viewHolder);
        return viewHolder;
    }

    private BaseWaMainViewHolder<StickerPack> onCreateCreatorHolder(ViewGroup viewGroup){
        Context context = viewGroup.getContext();
        final LayoutInflater layoutInflater = LayoutInflater.from(context);
        final View stickerPackRow = layoutInflater.inflate(R.layout.viewholder_wa_main_type2, viewGroup, false);
        MainWaType2ViewHolder viewHolder = new MainWaType2ViewHolder(context,stickerPackRow);
        stickerPackRow.setTag(viewHolder);
        return viewHolder;
    }

    private BaseWaMainViewHolder<StickerPack> onCreateListItemHolder(ViewGroup viewGroup){
        Context context = viewGroup.getContext();
        final LayoutInflater layoutInflater = LayoutInflater.from(context);
        final View stickerPackRow = layoutInflater.inflate(R.layout.sticker_packs_list_item, viewGroup, false);
        MainWaType3ViewHolder viewHolder = new MainWaType3ViewHolder(context,stickerPackRow);
        stickerPackRow.setTag(viewHolder);
        return viewHolder;
    }

    private BaseWaMainViewHolder<StickerPack> onCreateIconHolder(ViewGroup parent){
        Context context = parent.getContext();
        View itemView = LayoutInflater.from(context).inflate(R.layout.viewholder_main_type2, null, false);
        MainWaType4ViewHolder viewHolder = new MainWaType4ViewHolder(itemView, context);
        itemView.setTag(viewHolder);
        return viewHolder;
    }

    /**
     * 更新add button的状态
     * @param progressLayout
     * @param progressBar
     * @param addButton
     * @param pack
     */
    private void setAddButtonAppearance(RelativeLayout progressLayout, ProgressBar progressBar,
                                        ImageView addButton, final StickerPack pack) {
        if (pack.getIsWhitelisted()) {
            progressLayout.setVisibility(View.GONE);
            addButton.setVisibility(View.VISIBLE);
            addButton.setImageResource(R.drawable.sticker_3rdparty_added);
            addButton.setClickable(false);
            addButton.setOnClickListener(null);
            setBackground(addButton, null);
        } else {
            if(pack.status == STICKER_PACK_DOWNLOADING){
                addButton.setVisibility(View.GONE);
                progressLayout.setVisibility(View.VISIBLE);
                if(pack.totle <= 0){
                    Toast.makeText(context,"Something wrong,Sorry.",Toast.LENGTH_SHORT).show();
                    return;
                }
                if(progressBar != null){
                    int progress = (int)(100 * pack.count/pack.totle);
                    progressBar.setProgress(progress);
                }
            }else{
                progressLayout.setVisibility(View.GONE);
                addButton.setVisibility(View.VISIBLE);
                addButton.setImageResource(R.drawable.sticker_3rdparty_add);
                addButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onAddButtonClickedListener.onAddButtonClicked(pack);
                    }
                });
                TypedValue outValue = new TypedValue();
                addButton.getContext().getTheme().resolveAttribute(android.R.attr.selectableItemBackground, outValue, true);
                addButton.setBackgroundResource(outValue.resourceId);
            }
        }
    }

    private void setBackground(View view, Drawable background) {
        if (Build.VERSION.SDK_INT >= 16) {
            view.setBackground(background);
        } else {
            view.setBackgroundDrawable(background);
        }
    }
    @Override
    public int getItemCount() {
        return stickerPacks.size();
    }

    public void setStickerPackList(List<StickerPack> stickerPackList) {
        createShowItem(stickerPackList);
    }

    public interface OnAddButtonClickedListener {
        void onAddButtonClicked(StickerPack stickerPack);

        void onCloseDownload(StickerPack stickerPack);
    }
}
