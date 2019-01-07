package com.qisiemoji.apksticker.domain;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;

import java.util.List;

@JsonObject
public class RecommendItems {
    @JsonField(name = "data")
    public List<RecommendItem> infos;
    @JsonField
    public int page;
}
