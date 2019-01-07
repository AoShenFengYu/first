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
import com.qisiemoji.apksticker.whatsapp.search.SearchActivity;

public class MainWaType1ViewHolder extends BaseWaMainViewHolder<StickerPack> {

    private Context context;

    private ImageView img;

    public MainWaType1ViewHolder(final Context context, final View itemView) {
        super(itemView);
        this.context = context;
        img = itemView.findViewById(R.id.viewholder_wa_main1_search);
        img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, SearchActivity.class);
                context.startActivity(intent);
            }
        });
    }

    @Override
    public void fillView(final StickerPack pack) {
        if(pack == null){
            return;
        }
    }
}