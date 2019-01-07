/*
 * Copyright (c) WhatsApp Inc. and its affiliates.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.qisiemoji.apksticker.whatsapp.viewholders;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import com.qisiemoji.apksticker.R;
import com.qisiemoji.apksticker.whatsapp.StickerPack;

public class MainWaType2ViewHolder extends BaseWaMainViewHolder<StickerPack> {

    private Context context;

    private TextView create;

    public MainWaType2ViewHolder(Context context, final View itemView) {
        super(itemView);
        this.context = context;
        create = itemView.findViewById(R.id.viewhodler_wa_main2_create);
    }

    @Override
    public void fillView(final StickerPack pack) {
        if(pack == null){
            return;
        }
    }
}