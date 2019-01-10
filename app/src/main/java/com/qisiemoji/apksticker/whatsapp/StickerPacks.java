package com.qisiemoji.apksticker.whatsapp;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;
import com.qisiemoji.apksticker.domain.RecommendItem;

import java.util.ArrayList;
import java.util.List;

@JsonObject
public class StickerPacks {
    @JsonField(name = "sticker_packs")
    public ArrayList<StickerPack> stickerPacks;

    @JsonField(name = "hot_words")
    public ArrayList<String> hotWords;

    @JsonField(name = "android_play_store_link")
    public String androidLink;

    @JsonField(name = "ios_app_store_link\"")
    public String iosLink;
}
