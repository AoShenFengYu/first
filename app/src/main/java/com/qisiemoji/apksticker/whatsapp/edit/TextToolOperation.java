package com.qisiemoji.apksticker.whatsapp.edit;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.text.TextPaint;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.qisiemoji.apksticker.R;
import com.qisiemoji.apksticker.util.DensityUtil;
import com.qisiemoji.apksticker.util.RectUtil;
import com.qisiemoji.apksticker.views.imgedit.TextBean;
import com.qisiemoji.apksticker.whatsapp.manager.EditImageManager;

import java.util.ArrayList;
import java.util.List;

public class TextToolOperation extends BaseToolOperation {

    public static final int STICKER_BTN_HALF_SIZE = 30;
    public static final int PADDING = 41;
    public static final int TEXT_TOP_PADDING = 10;
    public static final int CHAR_MIN_HEIGHT = 60;

    private float mMoveX = 0;
    private float mMoveY = 0;
    private float mDownX = 0;
    private float mDownY = 0;
    private float mLastX = 0;
    private float mLastY = 0;

    //文字
    private TextPaint mHintPaint = new TextPaint();
    private TextPaint mBorderPaint = new TextPaint();
    private TextPaint mTextPaint = new TextPaint();
    private Paint mHelpPaint = new Paint();
    private Rect mTextRect = new Rect();// warp text rect record
    private RectF mHelpBoxRect = new RectF();
    private Rect mDeleteRect = new Rect();//删除按钮位置
    private Rect mRotateRect = new Rect();//旋转按钮位置
    private RectF mDeleteDstRect = new RectF();
    private RectF mRotateDstRect = new RectF();
    private Bitmap mDeleteBitmap;
    private Bitmap mRotateBitmap;
    private int mCurrentMode = IDLE_MODE;
    private String mHint;
    private boolean mEnableTextBorder;
    //控件的几种模式
    private static final int IDLE_MODE = 2;//正常
    private static final int MOVE_MODE = 3;//移动模式
    private static final int ROTATE_MODE = 4;//旋转模式
    private static final int DELETE_MODE = 5;//删除模式
    private LinearLayout mInvisibleContainer;
    public int mLayoutX = 0;
    public int mLayoutY = 0;
    public float mRotateAngle = 0;
    public float mScale = 1;
    private boolean mIsShowHelpBox = true;
    //是否需要自动换行
    private List<String> mTextContents = new ArrayList<String>(2);//存放所写的文字内容
    private String mText;
    //是否开启旋转模式
    private boolean mIsSuperMode = true;
    private int mTextColor;

    public interface TextToolOperationCallback {
        void onTextToolOperationDelete(TextToolOperation textToolOperation);
        void onTextToolOperationClick(TextToolOperation textToolOperation, boolean borderEnable);
        void onTextToolOperationBorderUpdated(boolean enable);
    }

    private TextToolOperationCallback mTextToolOperationCallback;

    public TextToolOperation(Context context, ViewGroup viewGroup, TextToolOperationCallback callback) {
        super(context, viewGroup);
        mTextToolOperationCallback = callback;

        mDeleteBitmap = BitmapFactory.decodeResource(context.getResources(),
                R.drawable.sc_ic_edit_delete);
        mRotateBitmap = BitmapFactory.decodeResource(context.getResources(),
                R.drawable.sc_ic_scale);
        mDeleteRect.set(0, 0, mDeleteBitmap.getWidth(), mDeleteBitmap.getHeight());
        mRotateRect.set(0, 0, mRotateBitmap.getWidth(), mRotateBitmap.getHeight());
//        mDeleteDstRect = new RectF(0, 0, STICKER_BTN_HALF_SIZE << 1, STICKER_BTN_HALF_SIZE << 1);
//        mRotateDstRect = new RectF(0, 0, STICKER_BTN_HALF_SIZE << 1, STICKER_BTN_HALF_SIZE << 1);
        mDeleteDstRect = new RectF(0, 0, mDeleteBitmap.getWidth(), mDeleteBitmap.getHeight());
        mRotateDstRect = new RectF(0, 0, mRotateBitmap.getWidth(), mRotateBitmap.getHeight());

        mEnableTextBorder = false;
        mTextColor = EditImageManager.getInstance().getTextToolColor();
        mTextPaint.setColor(mTextColor);
        mTextPaint.setTextAlign(Paint.Align.CENTER);
        mTextPaint.setTextSize(context.getResources().getDimensionPixelSize(R.dimen.default_text_tool_text_size));
        mTextPaint.setAntiAlias(true);
        mTextPaint.setTextAlign(Paint.Align.LEFT);

        mBorderPaint.setColor(Color.RED);
        mBorderPaint.setTextAlign(Paint.Align.CENTER);
        mBorderPaint.setTextSize(context.getResources().getDimensionPixelSize(R.dimen.default_text_tool_text_size));
        mBorderPaint.setStrokeWidth(8);
        mBorderPaint.setStyle(Paint.Style.STROKE);
        mBorderPaint.setAntiAlias(true);
        mBorderPaint.setTextAlign(Paint.Align.LEFT);

        mHint = " ";
        mHintPaint.setColor(Color.WHITE);
        mHintPaint.setTextAlign(Paint.Align.CENTER);
        mHintPaint.setTextSize(context.getResources().getDimensionPixelSize(R.dimen.default_text_tool_text_size));
        mHintPaint.setAntiAlias(true);
        mHintPaint.setShadowLayer(5.0f, 3.0f, 3.0f, Color.GRAY);
        mHintPaint.setTextAlign(Paint.Align.LEFT);

        mHelpPaint.setColor(mContext.getResources().getColor(R.color.edit_text_helper_rect_line_color));
        mHelpPaint.setStyle(Paint.Style.STROKE);
        mHelpPaint.setAntiAlias(true);
        mHelpPaint.setStrokeWidth(DensityUtil.dp2px(mContext, 2));
    }

    @Override
    public void onTouchDown(float x, float y) {
        mDownX = x;
        mDownY = y;
        mMoveX = 0;
        mMoveY = 0;

        if (mDeleteDstRect.contains(x, y)) {// 删除模式
            mIsShowHelpBox = true;
            mCurrentMode = DELETE_MODE;
        } else if (mRotateDstRect.contains(x, y)) {// 旋转按钮
            mIsShowHelpBox = true;
            mCurrentMode = ROTATE_MODE;
            mLastX = mRotateDstRect.centerX();
            mLastY = mRotateDstRect.centerY();
        } else if (mHelpBoxRect.contains(x, y)) {// 移动模式
            mIsShowHelpBox = true;
            mCurrentMode = MOVE_MODE;
            mLastX = x;
            mLastY = y;
        } else {
            mCurrentMode = IDLE_MODE;
            mViewGroup.getParent().requestDisallowInterceptTouchEvent(false);
        }
        mHasFocus = true;
    }

    @Override
    public void onTouchUp(float x, float y) {
        if (mTextToolOperationCallback != null) {
            mTextToolOperationCallback.onTextToolOperationClick(this, mEnableTextBorder);
        }

        if (mCurrentMode == DELETE_MODE) {// 删除选定贴图
            if (mTextToolOperationCallback != null) {
                mTextToolOperationCallback.onTextToolOperationDelete(this);
            }
            mViewGroup.invalidate();
        } else if (mCurrentMode == MOVE_MODE) {
            moveEditTextView(x, y);
        }
        mCurrentMode = IDLE_MODE;// 返回空闲状态
    }

    @Override
    public void onTouchMove(float x, float y) {
        mMoveX += Math.abs(x - mDownX);
        mMoveY += Math.abs(y - mDownY);
        mDownX = x;
        mDownY = y;

        if (mCurrentMode == MOVE_MODE) {// 移动贴图
            mCurrentMode = MOVE_MODE;
            float dx = x - mLastX;
            float dy = y - mLastY;
            mLayoutX += dx;
            mLayoutY += dy;
            mViewGroup.invalidate();
            mLastX = x;
            mLastY = y;
        } else if (mCurrentMode == ROTATE_MODE) {// 旋转 缩放文字操作
            mCurrentMode = ROTATE_MODE;
            float dx = x - mLastX;
            float dy = y - mLastY;
            updateRotateAndScale(dx, dy);
            mViewGroup.invalidate();
            mLastX = x;
            mLastY = y;
        }
    }

    @Override
    public boolean handleTouchEvent(float x, float y) {
        if (mDeleteDstRect.contains(x, y)) {// 删除模式
            return true;
        } else if (mRotateDstRect.contains(x, y)) {// 旋转按钮
            return true;
        } else if (mHelpBoxRect.contains(x, y)) {// 移动模式
            return true;
        }
        return false;
    }

    private void addEditTextView(float x, float y) {
        if (mInvisibleContainer != null) {
            mViewGroup.removeView(mInvisibleContainer);
        }

        mInvisibleContainer = new LinearLayout(mContext);
        mInvisibleContainer.setVisibility(View.INVISIBLE);

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        int shift = mContext.getResources().getDimensionPixelOffset(R.dimen.edit_image_text_shift);
        params.topMargin = (y - shift) < 0 ? 0 : (int) (y - shift);
        params.leftMargin = (x - shift) < 0 ? 0 : (int) (x - shift);
        mViewGroup.addView(mInvisibleContainer, params);
    }

    private void moveEditTextView(float x, float y) {
        if (mInvisibleContainer != null) {
            mViewGroup.removeView(mInvisibleContainer);
        }

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        int shift = mContext.getResources().getDimensionPixelOffset(R.dimen.edit_image_text_shift);
        params.topMargin = (y - shift) < 0 ? 0 : (int) (y - shift);
        params.leftMargin = (x - shift) < 0 ? 0 : (int) (x - shift);
        mViewGroup.addView(mInvisibleContainer, params);
    }

    @Override
    public void onDraw(Canvas canvas, boolean finalDest) {
        drawContent(canvas, finalDest);
    }

    private void updateRotateAndScale(final float dx, final float dy) {
        float c_x = mHelpBoxRect.centerX();
        float c_y = mHelpBoxRect.centerY();

        float x = mRotateDstRect.centerX();
        float y = mRotateDstRect.centerY();

        float n_x = x + dx;
        float n_y = y + dy;

        float xa = x - c_x;
        float ya = y - c_y;

        float xb = n_x - c_x;
        float yb = n_y - c_y;

        float srcLen = (float) Math.sqrt(xa * xa + ya * ya);
        float curLen = (float) Math.sqrt(xb * xb + yb * yb);

        float scale = curLen / srcLen;// 计算缩放比

        mScale *= scale;

        float newWidth = mHelpBoxRect.width() * mScale;

        if (newWidth < 70) {
            mScale /= scale;
            return;
        }

        if (mHelpBoxRect.width() > mViewGroup.getHeight() && scale > 1) {
            mScale /= scale;
        }

        double cos = (xa * xb + ya * yb) / (srcLen * curLen);
        if (cos > 1 || cos < -1)
            return;
        float angle = (float) Math.toDegrees(Math.acos(cos));
        float calMatrix = xa * yb - xb * ya;// 行列式计算 确定转动方向

        int flag = calMatrix > 0 ? 1 : -1;
        angle = flag * angle;

        mRotateAngle += angle;
    }

    private void drawContent(Canvas canvas, boolean finalDest) {

        drawText(canvas, finalDest);

        if (!mIsSuperMode || finalDest || !mHasFocus) {
            return;
        }

        int offsetValue = ((int) mDeleteDstRect.width()) >> 1;
        mDeleteDstRect.offsetTo(mHelpBoxRect.left - offsetValue, mHelpBoxRect.top - offsetValue);
        mRotateDstRect.offsetTo(mHelpBoxRect.right - offsetValue, mHelpBoxRect.bottom - offsetValue);

        RectUtil.rotateRect(mDeleteDstRect, mHelpBoxRect.centerX(),
                mHelpBoxRect.centerY(), mRotateAngle);
        RectUtil.rotateRect(mRotateDstRect, mHelpBoxRect.centerX(),
                mHelpBoxRect.centerY(), mRotateAngle);

        if (!mIsShowHelpBox) {
            return;
        }

        canvas.save();
        canvas.rotate(mRotateAngle, mHelpBoxRect.centerX(), mHelpBoxRect.centerY());
        canvas.drawRoundRect(mHelpBoxRect, 10, 10, mHelpPaint);
        canvas.restore();


        canvas.drawBitmap(mDeleteBitmap, mDeleteRect, mDeleteDstRect, null);
        canvas.drawBitmap(mRotateBitmap, mRotateRect, mRotateDstRect, null);
    }

    private void drawText(Canvas canvas, boolean finalDest) {
        TextBean textBean = new TextBean(mLayoutX, mLayoutY, mScale, mRotateAngle, mText, mTextColor);
        drawText(canvas, textBean, finalDest);
    }

    public void drawText(Canvas canvas, TextBean textBean, boolean finalDest) {
        mTextContents.clear();

        if (textBean.text != null && !"".equals(textBean.text)) {
            String[] splits = textBean.text.split("\n");
            for (String text : splits) {
                mTextContents.add(text);
            }
        }

        int text_height = 0;

        mTextRect.set(0, 0, 0, 0);
        Rect tempRect = new Rect();
        if (isListEmpty(mTextContents)) {
            String text = mHint;
            mTextPaint.getTextBounds(text, 0, text.length(), tempRect);
            text_height = Math.max(CHAR_MIN_HEIGHT, tempRect.height());
            if (tempRect.height() <= 0) {
                tempRect.set(0, 0, 0, text_height);
            }
            RectUtil.rectAddV(mTextRect, tempRect, TEXT_TOP_PADDING);
        } else {
            for (int i = 0; i < mTextContents.size(); i++) {
                String text = mTextContents.get(i);
                mTextPaint.getTextBounds(text, 0, text.length(), tempRect);
                text_height = Math.max(CHAR_MIN_HEIGHT, tempRect.height());
                if (tempRect.height() <= 0) {
                    tempRect.set(0, 0, 0, text_height);
                }
                RectUtil.rectAddV(mTextRect, tempRect, TEXT_TOP_PADDING);
            }
        }

        mTextRect.offset(textBean.x, textBean.y - text_height);

        mHelpBoxRect.set(mTextRect.left - PADDING, mTextRect.top - PADDING
                , mTextRect.right + PADDING, mTextRect.bottom + PADDING);
        RectUtil.scaleRect(mHelpBoxRect, textBean.scale);

        canvas.save();
        canvas.scale(textBean.scale, textBean.scale, mHelpBoxRect.centerX(), mHelpBoxRect.centerY());
        canvas.rotate(textBean.rotate, mHelpBoxRect.centerX(), mHelpBoxRect.centerY());

        int draw_text_y = textBean.y;
        if (isListEmpty(mTextContents) && !finalDest) {
            canvas.drawText(mHint, textBean.x, draw_text_y, mHintPaint);
        } else {
            for (int i = 0; i < mTextContents.size(); i++) {
                if (mEnableTextBorder) {
                    canvas.drawText(mTextContents.get(i), textBean.x, draw_text_y, mBorderPaint);
                }
                canvas.drawText(mTextContents.get(i), textBean.x, draw_text_y, mTextPaint);
                draw_text_y += text_height + TEXT_TOP_PADDING;
            }
        }
        canvas.restore();
    }

    public boolean isListEmpty(List list) {
        if (list == null) {
            return true;
        }
        return list.size() == 0;
    }

    public void setText(String text) {
        mIsShowHelpBox = true;
        this.mText = text;
        mViewGroup.invalidate();
    }

    public void setTextColor(int color) {
        mTextColor = color;
        mTextPaint.setColor(color);
        mBorderPaint.setColor(mTextColor == Color.WHITE ? Color.BLACK : Color.WHITE);
        mViewGroup.invalidate();
    }

    @Override
    public boolean shouldKeep() {
        return !TextUtils.isEmpty(mText);
    }

    @Override
    public void clear() {
    }

    public int getTextColor() {
        return mTextColor;
    }

    public void createText() {
        mHasFocus = true;
        mCurrentMode = IDLE_MODE;
        mLayoutX = (mViewGroup.getWidth() - mContext.getResources().getDimensionPixelOffset(R.dimen.default_text_tool_width)) / 2 + PADDING;
        mLayoutY = (mViewGroup.getHeight() - mContext.getResources().getDimensionPixelOffset(R.dimen.default_text_tool_height)) / 2 + PADDING;
        addEditTextView(mLayoutX, mLayoutY);

        if (mTextToolOperationCallback != null) {
            mTextToolOperationCallback.onTextToolOperationBorderUpdated(mEnableTextBorder);
        }

        mViewGroup.invalidate();
    }

    public void enableTextBorder(boolean enable) {
        mEnableTextBorder = enable;
        mBorderPaint.setColor(mTextColor == Color.WHITE ? Color.BLACK : Color.WHITE);
        mViewGroup.invalidate();
        if (mTextToolOperationCallback != null) {
            mTextToolOperationCallback.onTextToolOperationBorderUpdated(mEnableTextBorder);
        }
    }
}
