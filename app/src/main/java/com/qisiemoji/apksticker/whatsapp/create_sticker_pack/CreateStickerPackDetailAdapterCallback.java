package com.qisiemoji.apksticker.whatsapp.create_sticker_pack;

public interface CreateStickerPackDetailAdapterCallback {
    void onClickAdd(int index, StickerItem item);

    void onClickEdit(int index, StickerItem item);

    void onClickDelete(int index, StickerItem item);
}