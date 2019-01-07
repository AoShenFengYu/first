package com.qisiemoji.apksticker.request;

import com.qisiemoji.apksticker.domain.RecommendItems;
import com.qisiemoji.apksticker.domain.ResultData;
import com.qisiemoji.apksticker.whatsapp.StickerPack;
import com.qisiemoji.apksticker.whatsapp.StickerPacks;
import com.qisiemoji.apksticker.whatsapp.StickerPacksData;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface StickerApi {

    @GET("stickers2/advise")
    Call<ResultData<RecommendItems>> fetchHomePage(@Query("page") int offset,
                                                   @Query("size") int limite);

    @GET("stickers2/publish")
    Call<ResultData<StickerPacksData>> fetchWaStore(@Query("page") int offset,
                                                    @Query("size") int limite);
}
