package com.qisiemoji.apksticker.domain;

public class StickerItem extends BaseItem{

    private String url;

    private boolean isLocked;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public boolean isLocked() {
        return isLocked;
    }

    public void setLocked(boolean locked) {
        isLocked = locked;
    }
}
