package com.qisiemoji.apksticker.whatsapp.edit.widget;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;

import com.qisiemoji.apksticker.R;
import com.qisiemoji.apksticker.util.DensityUtil;

public class CircleColorImageView extends android.support.v7.widget.AppCompatImageView {

    private int mColor;
    private int mMeaningSize;

    public CircleColorImageView(Context context) {
        this(context, null);
    }

    public CircleColorImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CircleColorImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        setImageDrawable(getResources().getDrawable(R.drawable.round_corner_with_border));
//        disableBorder();
    }

    public void setColor(int color) {
        mColor = color;
        if (getDrawable() instanceof GradientDrawable) {
            ((GradientDrawable)getDrawable().mutate()).setColor(color);
        }
    }

    public int getColor() {
        return mColor;
    }

    public void setMeaningSize(int size) {
        mMeaningSize = size;
    }

    public int getMeaningSize() {
        return mMeaningSize;
    }

    public void enableBorder() {
        if (getDrawable() instanceof GradientDrawable) {
            ((GradientDrawable)getDrawable().mutate()).setStroke(DensityUtil.dp2px(getContext(), 1), getResources().getColor(R.color.white_color_item_border));
        }
    }

    public void disableBorder() {
        if (getDrawable() instanceof GradientDrawable) {
            ((GradientDrawable)getDrawable().mutate()).setStroke(DensityUtil.dp2px(getContext(), 1), mColor);
        }
    }
}
