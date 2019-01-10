package com.qisiemoji.apksticker.whatsapp.create_sticker_pack;

import android.os.AsyncTask;

import com.qisiemoji.apksticker.whatsapp.StickerPack;
import com.qisiemoji.apksticker.whatsapp.StickerPackDetailsActivity;
import com.qisiemoji.apksticker.whatsapp.WhitelistCheck;

import java.lang.ref.WeakReference;

public class WhiteListCheckAsyncTask extends AsyncTask<StickerPack, Void, Boolean> {
    private final WeakReference<CreateStickerPackDetailActivity> weakReference;

    WhiteListCheckAsyncTask(CreateStickerPackDetailActivity activity) {
        this.weakReference = new WeakReference<>(activity);
    }

    @Override
    protected final Boolean doInBackground(StickerPack... stickerPacks) {
        StickerPack stickerPack = stickerPacks[0];
        final CreateStickerPackDetailActivity activity = weakReference.get();
        // noinspection SimplifiableIfStatement
        if (activity == null) {
            return false;
        }
        return WhitelistCheck.isWhitelisted(activity, stickerPack.identifier);
    }

    @Override
    protected void onPostExecute(Boolean isWhitelisted) {
        final CreateStickerPackDetailActivity activity = weakReference.get();
        if (activity != null) {
            activity.updateAddUI(isWhitelisted);
        }
    }
}