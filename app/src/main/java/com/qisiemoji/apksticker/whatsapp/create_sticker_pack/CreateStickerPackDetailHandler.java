package com.qisiemoji.apksticker.whatsapp.create_sticker_pack;

import android.os.Handler;
import android.os.Message;

import com.qisiemoji.apksticker.whatsapp.StickerPack;
import com.qisiemoji.apksticker.whatsapp.StickerPackDetailsActivity;

import java.lang.ref.WeakReference;

public class CreateStickerPackDetailHandler extends Handler {

    public interface Callback {
        void onDownloadSuccess(StickerPack pack);

        void onUpdateProgress(StickerPack pack);

        void onDownloadError();
    }

    private static final int USER_DOWNLOAD_SUCCESS = 100;
    private static final int USER_DOWNLOADING = 101;
    private static final int USER_DOWNLOAD_FAIL = 102;

    WeakReference<Callback> thisLayout;

    StickerPack pack = null;

    public CreateStickerPackDetailHandler(Callback callback) {
        thisLayout = new WeakReference<>(callback);
    }

    @Override
    public void handleMessage(Message msg) {
        final Callback theLayout = thisLayout.get();
        if (theLayout == null) {
            return;
        }

        switch (msg.what) {
            case USER_DOWNLOAD_SUCCESS:
                pack = (StickerPack) msg.obj;
                pack.endDownload();
                theLayout.onDownloadSuccess(pack);
                break;
            case USER_DOWNLOADING:
                pack = (StickerPack) msg.obj;
                theLayout.onUpdateProgress(pack);
                break;
            case USER_DOWNLOAD_FAIL:
                pack = (StickerPack) msg.obj;
                pack.endDownload();
                theLayout.onDownloadError();
                break;
            default:
                break;
        }
    }

}
