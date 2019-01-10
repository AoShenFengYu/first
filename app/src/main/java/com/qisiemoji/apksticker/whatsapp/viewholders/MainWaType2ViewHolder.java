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
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.TextView;

import com.qisiemoji.apksticker.R;
import com.qisiemoji.apksticker.util.FragmentUtil;
import com.qisiemoji.apksticker.whatsapp.create_sticker_pack.CreateStickerPackDetailActivity;
import com.qisiemoji.apksticker.whatsapp.StickerPack;
import com.qisiemoji.apksticker.whatsapp.fragment.CreateStickerPackDialogFragment;

import static com.qisiemoji.apksticker.whatsapp.manager.WAStickerManager.EXTRA_STICKER_PACK_DATA;

public class MainWaType2ViewHolder extends BaseWaMainViewHolder<StickerPack> {

    private Context context;

    private TextView create;

    public MainWaType2ViewHolder(final Context context, final View itemView) {
        super(itemView);
        this.context = context;
        create = itemView.findViewById(R.id.viewhodler_wa_main2_create);
        create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDialogFragment();
            }

            private void showDialogFragment() {
                if (!(context instanceof FragmentActivity)) {
                    return;
                }

                FragmentActivity activity = (FragmentActivity) context;

                CreateStickerPackDialogFragment fragment = CreateStickerPackDialogFragment.newInstance();
                fragment.setCallBack(new CreateStickerPackDialogFragment.CreateStickerPackDialogFragmentCallBack() {
                    @Override
                    public void onClickCreateButton(String stickerPackName, String author) {
                        startCreateStickerPackDetailActivity(null, stickerPackName, author);
                    }

                    @Override
                    public void onClickCancelButton() {
                    }
                });
                FragmentUtil.showDialogFragment(activity.getSupportFragmentManager(), fragment, CreateStickerPackDialogFragment.DIALOG_FRAGMENT);
            }

            private void startCreateStickerPackDetailActivity(StickerPack stickerPack, String packName, String author) {
                Intent intent;
                if (stickerPack == null) {
                    intent = CreateStickerPackDetailActivity.create(context, packName, author);
                } else {
                    intent = CreateStickerPackDetailActivity.edit(context, stickerPack, false);
                }
                context.startActivity(intent);
            }
        });
    }

    @Override
    public void fillView(final StickerPack pack) {
        if (pack == null) {
            return;
        }
    }
}