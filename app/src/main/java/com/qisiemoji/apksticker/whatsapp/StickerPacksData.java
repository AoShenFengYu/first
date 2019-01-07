package com.qisiemoji.apksticker.whatsapp;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;

import java.util.ArrayList;

@JsonObject
public class StickerPacksData {
    @JsonField(name = "data")
    public StickerPacks info;
}
