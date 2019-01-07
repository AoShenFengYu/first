package com.qisiemoji.apksticker.domain;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;

import java.util.ArrayList;
@JsonObject
public class RecommendItem extends BaseItem {

    @JsonField
    public String label;
    @JsonField(name="package")
    public String packageName;
    @JsonField(name="style_type")
    public int showType;
    @JsonField(name="object")
    private ArrayList<ChildItem> children;

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public int getShowType() {
        return showType;
    }

    public void setShowType(int showType) {
        this.showType = showType;
    }

    public ArrayList<ChildItem> getChildren() {
        return children;
    }

    public void setChildren(ArrayList<ChildItem> children) {
        this.children = children;
    }
}
