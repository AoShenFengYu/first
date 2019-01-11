/*
 * Copyright (c) WhatsApp Inc. and its affiliates.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.qisiemoji.apksticker.whatsapp.viewholders;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.qisiemoji.apksticker.R;
import com.qisiemoji.apksticker.util.DensityUtil;
import com.qisiemoji.apksticker.whatsapp.StickerPack;
import com.qisiemoji.apksticker.whatsapp.StickerPackDetailsActivity;
import com.qisiemoji.apksticker.whatsapp.create_sticker_pack.CreateStickerPackDetailActivity;

public class MainWaType3ViewHolder extends BaseWaMainViewHolder<StickerPack> {

    private Context context;
    public View container;
    public TextView titleView;
    public TextView publisherView;
    public TextView filesizeView;
    public ImageView addButton;
    public LinearLayout imageRowView;
    public RelativeLayout progressLayout;
    public ProgressBar progressBar;
    public ImageView closeBtn;

    public MainWaType3ViewHolder(Context context, final View itemView) {
        super(itemView);
        this.context = context;
        container = itemView;
        titleView = itemView.findViewById(R.id.sticker_pack_title);
        publisherView = itemView.findViewById(R.id.sticker_pack_publisher);
        filesizeView = itemView.findViewById(R.id.sticker_pack_filesize);
        addButton = itemView.findViewById(R.id.add_button_on_list);
        imageRowView = itemView.findViewById(R.id.sticker_packs_list_item_image_list);

        progressLayout = itemView.findViewById(R.id.loading_layout);
        progressBar = itemView.findViewById(R.id.loading_progressBar);
        closeBtn = itemView.findViewById(R.id.loading_close);
    }

    @Override
    public void fillView(final StickerPack pack) {
        if(pack == null){
            return;
        }
        publisherView.setText(pack.publisher);
//      filesizeView.setText(Formatter.formatShortFileSize(context, pack.getTotalSize()));
        filesizeView.setVisibility(View.GONE);

        titleView.setText(pack.name);
        container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), StickerPackDetailsActivity.class);
                intent.putExtra(StickerPackDetailsActivity.EXTRA_SHOW_UP_BUTTON, true);
                intent.putExtra(StickerPackDetailsActivity.EXTRA_STICKER_PACK_DATA, pack);
                view.getContext().startActivity(intent);

//                Intent intent = CreateStickerPackDetailActivity.preview(view.getContext(), pack);
//                view.getContext().startActivity(intent);
            }
        });
        imageRowView.removeAllViews();
        //if this sticker pack contains less stickers than the max, then take the smaller size.
        int actualNumberOfStickersToShow = 0;
        if(pack.getStickers() != null){
            actualNumberOfStickersToShow = Math.min(5, pack.getStickers().size());
        }
        for (int i = 0; i < actualNumberOfStickersToShow; i++) {
            final SimpleDraweeView rowImage = (SimpleDraweeView) LayoutInflater.from(context).inflate(R.layout.sticker_pack_list_item_image, imageRowView, false);
            rowImage.setImageURI(pack.getStickers().get(i).imageFileUrl);
            final LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) rowImage.getLayoutParams();
            lp.setMargins(0, lp.topMargin, 10, lp.bottomMargin);
            lp.width = DensityUtil.dp2px(context,50);
            lp.height = DensityUtil.dp2px(context,50);
            rowImage.setLayoutParams(lp);
            imageRowView.addView(rowImage);
        }
    }
}