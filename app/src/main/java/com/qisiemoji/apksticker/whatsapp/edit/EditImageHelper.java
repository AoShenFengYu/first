package com.qisiemoji.apksticker.whatsapp.edit;

import android.content.Context;
import android.graphics.Canvas;
import android.view.ViewGroup;

import java.util.ArrayList;

public class EditImageHelper implements TextToolOperation.TextToolOperationCallback {

    private Context mContext;
    private ViewGroup mViewGroup;
    private BaseToolOperation mCurrentToolOperation;

    private ArrayList<BaseToolOperation> mToolOperations = new ArrayList<>();
    private ArrayList<BaseToolOperation> mTmpOperations = new ArrayList<>();

    public interface EditImageHelperListener {
        void onPreNextStateUpdated(boolean canPre, boolean canNext);
        void onClickExistTextToolOperation();
        void onClickTextToolOperation(int color, boolean borderEnable);
        void onNewCurrentToolNull();
        void onTextToolOperationBorderUpdated(boolean enable);
    }
    private EditImageHelperListener mEditImageHelperListener;

    public enum ToolType {
        Draw, Text
    }
    private ToolType mCurrentToolType = ToolType.Draw;

    public EditImageHelper(Context context, ViewGroup viewGroup) {
        mContext = context;
        mViewGroup = viewGroup;
    }

    public void setEditImageHelperListener(EditImageHelperListener listener) {
        mEditImageHelperListener = listener;
    }

    public void onTouchDown(float x, float y) {
        if (mCurrentToolOperation != null
                && mCurrentToolOperation instanceof TextToolOperation
                && !mCurrentToolOperation.shouldKeep()
                && !mCurrentToolOperation.handleTouchEvent(x, y)
                && !isInitialStateInTextOperation()) {
            mCurrentToolOperation.clear();
            mToolOperations.remove(mCurrentToolOperation);
        }

        BaseToolOperation alreadyExistOperation = null;
        // check whether touch the operation that can be "re"operation again ; ex : TextToolOperation
        for (int i = mToolOperations.size() - 1; i >= 0 ; i--) {
            if (mToolOperations.get(i).handleTouchEvent(x, y)) {
                alreadyExistOperation = mToolOperations.get(i);
                if (mEditImageHelperListener != null) {
                    mEditImageHelperListener.onClickExistTextToolOperation();
                }
                break;
            }
        }

        boolean addToToolOperations = false;
        if (alreadyExistOperation != null) {
            mToolOperations.remove(alreadyExistOperation);
            mCurrentToolOperation = alreadyExistOperation;
            addToToolOperations = true;
        } else {
            if (!isInitialStateInTextOperation()) {
                mCurrentToolOperation = null;
                addToToolOperations = true;
            }
        }

        // clear previous operation focus
        if (!isInitialStateInTextOperation()) {
            for (int i = 0; i < mToolOperations.size(); i++) {
                mToolOperations.get(i).setHasFocus(false);
            }
        }

        // add current operation to the list
        if (mCurrentToolOperation != null && addToToolOperations) {
            mToolOperations.add(mCurrentToolOperation);
            mCurrentToolOperation.onTouchDown(x, y);
        }
    }

    public void onTouchUp(float x, float y) {
        if (mCurrentToolOperation instanceof DrawToolOperation) {
            mTmpOperations.clear();
        }

        if (mEditImageHelperListener != null) {
            mEditImageHelperListener.onPreNextStateUpdated(canPre(), canNext());
        }
        if (mCurrentToolOperation != null) {
            mCurrentToolOperation.onTouchUp(x, y);
        }
    }

    public void onTouchMove(float x, float y) {
        if (mCurrentToolOperation != null) {
            mCurrentToolOperation.onTouchMove(x, y);
        }
    }

    public void onDraw(Canvas canvas, boolean finalDest) {
        for (BaseToolOperation tool : mToolOperations) {
            tool.onDraw(canvas, finalDest);
        }
    }

    private boolean canPre() {
        return mToolOperations.size() > 0;
    }

    private boolean canNext() {
        return mTmpOperations.size() > 0;
    }

    @Override
    public void onTextToolOperationDelete(TextToolOperation textToolOperation) {
        mToolOperations.remove(textToolOperation);
        if (mEditImageHelperListener != null) {
            mEditImageHelperListener.onPreNextStateUpdated(canPre(), canNext());
        }
    }

    @Override
    public void onTextToolOperationClick(TextToolOperation textToolOperation, boolean borderEnable) {
        if (mEditImageHelperListener != null) {
            mEditImageHelperListener.onClickTextToolOperation(textToolOperation.getTextColor(), borderEnable);
        }
    }

    @Override
    public void onTextToolOperationBorderUpdated(boolean enable) {
        if (mEditImageHelperListener != null) {
            mEditImageHelperListener.onTextToolOperationBorderUpdated(enable);
        }
    }

    public void setTextToolColor(int color) {
        if (mCurrentToolOperation instanceof TextToolOperation) {
            ((TextToolOperation)mCurrentToolOperation).setTextColor(color);
        }
    }

    public void createTextToolText() {
        if (mCurrentToolOperation != null
                && mCurrentToolOperation instanceof TextToolOperation
                && !mCurrentToolOperation.shouldKeep()) {
            mCurrentToolOperation.clear();
            mToolOperations.remove(mCurrentToolOperation);
        }

        // clear previous operation focus
        for (int i = 0; i < mToolOperations.size(); i++) {
            mToolOperations.get(i).setHasFocus(false);
        }

        mCurrentToolOperation = new TextToolOperation(mContext, mViewGroup, this); // newCurrentTool(false);
        if (mCurrentToolOperation != null && mCurrentToolOperation instanceof TextToolOperation) {
            mToolOperations.add(mCurrentToolOperation);
            ((TextToolOperation) mCurrentToolOperation).createText();
        }

        mTmpOperations.clear();
        if (mEditImageHelperListener != null) {
            mEditImageHelperListener.onPreNextStateUpdated(canPre(), canNext());
        }
    }

    public void enableTextToolTextBorder(boolean enable) {
        if (mCurrentToolOperation instanceof TextToolOperation) {
            ((TextToolOperation)mCurrentToolOperation).enableTextBorder(enable);
        }
    }

    public void setTextToolText(String text) {
        if (mCurrentToolOperation instanceof TextToolOperation) {
            ((TextToolOperation)mCurrentToolOperation).setText(text);
        }
    }

    public int getDrawCount() {
        int count = 0;
        for (BaseToolOperation operation : mToolOperations) {
            if (operation instanceof DrawToolOperation) {
                count = count + 1;
            }
        }
        return count;
    }

    public int getTextCount() {
        int count = 0;
        for (BaseToolOperation operation : mToolOperations) {
            if (operation instanceof TextToolOperation) {
                count = count + 1;
            }
        }
        return count;
    }

    private boolean isInitialStateInTextOperation() {
        // in TextOperation (empty) + only 1 textOperation
        if (mCurrentToolType == ToolType.Text &&
                mCurrentToolOperation instanceof TextToolOperation) {
            if (!mCurrentToolOperation.shouldKeep()) {
                return mToolOperations.size() == 1;
            }
        }
        return false;
    }
}
