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

    public enum ToolType {
        Draw, Text
    }
    private ToolType mCurrentToolType = ToolType.Draw;

    public EditImageHelper(Context context, ViewGroup viewGroup) {
        mContext = context;
        mViewGroup = viewGroup;
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
