package com.qisiemoji.apksticker.whatsapp.create_sticker_pack;

import android.content.Context;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.qisiemoji.apksticker.R;
import com.qisiemoji.apksticker.util.DisplayUtil;

import java.util.ArrayList;

public class CreateStickerPackDetailAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public static final int TYPE_STICKER = 0x1001;

    private ArrayList<StickerItem> stickerItems;
    private CreateStickerPackDetailAdapterCallback createStickerPackDetailAdapterCallback;

    public CreateStickerPackDetailAdapter(Context context, ArrayList<StickerItem> items, CreateStickerPackDetailAdapterCallback cb) {
        stickerItems = items;
        createStickerPackDetailAdapterCallback = cb;
    }

    public void setStickerItems(ArrayList<StickerItem> items) {
        stickerItems = items;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        switch (viewType) {
            default:
                return new StickerHolder(inflater.inflate(StickerHolder.LAYOUT, parent, false), createStickerPackDetailAdapterCallback);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        int viewType = getItemViewType(position);
        switch (viewType) {
            default:
                ((StickerHolder) holder).bind(stickerItems.get(position));
        }
    }

    @Override
    public int getItemCount() {
        return stickerItems.size();
    }

    @Override
    public int getItemViewType(int position) {
        return TYPE_STICKER;
    }

    static class StickerHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        static final int LAYOUT = R.layout.item_view_create_sticker_pack_detail;

        StickerItem stickerItem;
        CreateStickerPackDetailAdapterCallback callback;
        View imageContent;
        AppCompatImageView imageView;
        AppCompatImageView delete;

        StickerHolder(View itemView, CreateStickerPackDetailAdapterCallback cb) {
            super(itemView);
            callback = cb;
            imageContent = itemView.findViewById(R.id.image_content);
            imageView = itemView.findViewById(R.id.image);
            delete = itemView.findViewById(R.id.delete);


            int screenWidth = DisplayUtil.getScreenWidth(imageView.getContext());
            int dividerWidth = imageView.getContext().getResources().getDimensionPixelOffset(R.dimen.create_pack_rv_divider_width);
            int dividerCount = CreateStickerPackDetailActivity.SPAN_COUNT + 1;
            int itemSize = (screenWidth - (dividerCount * dividerWidth)) / CreateStickerPackDetailActivity.SPAN_COUNT;

            itemView.getLayoutParams().width = itemSize;
            itemView.getLayoutParams().height = itemSize;

            imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        }

        void bind(StickerItem item) {
            stickerItem = item;
            if (!TextUtils.isEmpty(stickerItem.getImageUrl())) {
                imageContent.setVisibility(View.VISIBLE);
                delete.setOnClickListener(this);
                imageView.setImageDrawable(null);
                Glide.with(imageView.getContext())
                        .load(stickerItem.getImageUrl())
                        .dontAnimate()
                        .into(imageView);
            } else {
                delete.setOnClickListener(null);
            }

            imageView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (callback == null) {
                return;
            }

            if (v.getId() == R.id.image) {
                if (TextUtils.isEmpty(stickerItem.getImageUrl())) {
                    callback.onClickAdd(getLayoutPosition(), stickerItem);
                } else {
                    callback.onClickEdit(getLayoutPosition(), stickerItem);
                }
            } else if (v.getId() == R.id.delete) {
                callback.onClickDelete(getLayoutPosition(), stickerItem);
            }
        }
    }

}