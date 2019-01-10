package com.qisiemoji.apksticker.whatsapp.create_sticker_pack;

public class StickerItem {

    private String imageUrl;

    public StickerItem(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
