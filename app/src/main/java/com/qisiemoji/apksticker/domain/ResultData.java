package com.qisiemoji.apksticker.domain;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;

/**
 * Created by momo on 1/25/16.
 */
@JsonObject(fieldDetectionPolicy = JsonObject.FieldDetectionPolicy.NONPRIVATE_FIELDS)
public class ResultData<T> {

    @JsonField
    public int errorCode;

    @JsonField
    public String errorMsg;

    @JsonField
    public T data;

    public ResultData() {
    }

    @Override
    public String toString() {
        return "ResultData{" +
                "errorCode=" + errorCode +
                ", errorMsg='" + errorMsg + '\'' +
                ", data=" + data +
                '}';
    }
}

