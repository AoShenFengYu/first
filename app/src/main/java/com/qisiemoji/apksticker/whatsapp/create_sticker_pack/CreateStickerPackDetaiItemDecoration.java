package com.qisiemoji.apksticker.whatsapp.create_sticker_pack;

import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;

public class CreateStickerPackDetaiItemDecoration extends RecyclerView.ItemDecoration {

    private int spanCount;
    private int spacing;
    private boolean includeEdge;

    public CreateStickerPackDetaiItemDecoration(int spanCount, int spacing, boolean includeEdge) {
        this.spanCount = spanCount;
        this.spacing = spacing;
        this.includeEdge = includeEdge;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        int position = parent.getChildAdapterPosition(view); // item position
        int column = position % spanCount; // item column

        if (includeEdge) {
            // spacing - column * ((1f / spanCount) * spacing)
            outRect.left = spacing - column * spacing / spanCount;

            // (column + 1) * ((1f / spanCount) * spacing)
            outRect.right = (column + 1) * spacing / spanCount;

            if (position < spanCount) {
                outRect.top = spacing;
            }

            outRect.bottom = spacing;

        } else {
            // column * ((1f / spanCount) * spacing)
            outRect.left = column * spacing / spanCount;

            // spacing - (column + 1) * ((1f /    spanCount) * spacing)
            outRect.right = spacing - (column + 1) * spacing / spanCount;

            if (position >= spanCount) {
                outRect.top = spacing;
            }
        }
    }
}