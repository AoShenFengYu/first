/*
 * Copyright (c) WhatsApp Inc. and its affiliates.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.qisiemoji.apksticker.whatsapp;

import android.os.Parcel;
import android.os.Parcelable;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;

import java.util.ArrayList;
import java.util.List;
@JsonObject
public class StickerPack implements Parcelable {
    @JsonField(name = "identifier")
    public String identifier;
    @JsonField(name = "name")
    public String name;
    @JsonField(name = "publisher")
    public String publisher;
    @JsonField(name = "tray_image_file")
    public String trayImageFile;
    public String trayImageUrl;
    @JsonField(name = "publisher_email")
    public String publisherEmail;
    @JsonField(name = "publisher_website")
    public String publisherWebsite;
    @JsonField(name = "privacy_policy_website")
    public String privacyPolicyWebsite;
    @JsonField(name = "license_agreement_website")
    public String licenseAgreementWebsite;
    @JsonField(name = "description")
    public String description;
    @JsonField(name = "stickers")
    public List<Sticker> stickers;
    @JsonField
    public String iosAppStoreLink;
    @JsonField
    public long totalSize;
    @JsonField
    public String androidPlayStoreLink;
    @JsonField
    private boolean isWhitelisted;

    public int status;
    public int totle;
    public int count;
    public int showType;

    public List<StickerPack> showList;

    public WaStickerDownloadRunnable thread;

    public StickerPack(){

    }

    public StickerPack(int showType){
        this.showType = showType;
    }

    public StickerPack(String identifier, String name, String publisher, String trayImageFile, String publisherEmail, String publisherWebsite, String privacyPolicyWebsite, String licenseAgreementWebsite) {
        this.identifier = identifier;
        this.name = name;
        this.publisher = publisher;
        this.trayImageFile = trayImageFile;
        this.publisherEmail = publisherEmail;
        this.publisherWebsite = publisherWebsite;
        this.privacyPolicyWebsite = privacyPolicyWebsite;
        this.licenseAgreementWebsite = licenseAgreementWebsite;
    }

    void setIsWhitelisted(boolean isWhitelisted) {
        this.isWhitelisted = isWhitelisted;
    }

    boolean getIsWhitelisted() {
        return isWhitelisted;
    }

    protected StickerPack(Parcel in) {
        identifier = in.readString();
        name = in.readString();
        publisher = in.readString();
        trayImageFile = in.readString();
        publisherEmail = in.readString();
        publisherWebsite = in.readString();
        privacyPolicyWebsite = in.readString();
        licenseAgreementWebsite = in.readString();
        iosAppStoreLink = in.readString();
        stickers = in.createTypedArrayList(Sticker.CREATOR);
        totalSize = in.readLong();
        androidPlayStoreLink = in.readString();
        isWhitelisted = in.readByte() != 0;
    }

    public static final Creator<StickerPack> CREATOR = new Creator<StickerPack>() {
        @Override
        public StickerPack createFromParcel(Parcel in) {
            return new StickerPack(in);
        }

        @Override
        public StickerPack[] newArray(int size) {
            return new StickerPack[size];
        }
    };

    public void setStickers(List<Sticker> stickers) {
        this.stickers = stickers;
        totalSize = 0;
        for (Sticker sticker : stickers) {
            totalSize += sticker.size;
        }
    }

    public void setAndroidPlayStoreLink(String androidPlayStoreLink) {
        this.androidPlayStoreLink = androidPlayStoreLink;
    }

    public void setIosAppStoreLink(String iosAppStoreLink) {
        this.iosAppStoreLink = iosAppStoreLink;
    }

    public void endDownload(){
        status = 0;
        if(thread != null){
            thread.interrupt();
            thread.cancel();
            thread = null;
        }
    }

    public void startDownload(WaStickerDownloadRunnable thread){
        this.thread = thread;
        status = StickerPackListAdapter.STICKER_PACK_DOWNLOADING;
    }

    public List<Sticker> getStickers() {
        return stickers;
    }

    public long getTotalSize() {
        return totalSize;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(identifier);
        dest.writeString(name);
        dest.writeString(publisher);
        dest.writeString(trayImageFile);
        dest.writeString(publisherEmail);
        dest.writeString(publisherWebsite);
        dest.writeString(privacyPolicyWebsite);
        dest.writeString(licenseAgreementWebsite);
        dest.writeString(iosAppStoreLink);
        dest.writeTypedList(stickers);
        dest.writeLong(totalSize);
        dest.writeString(androidPlayStoreLink);
        dest.writeByte((byte) (isWhitelisted ? 1 : 0));
    }
}
