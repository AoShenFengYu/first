package com.qisiemoji.apksticker.whatsapp.fragment;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;

public abstract class BasicDialogFragment extends DialogFragment {

    public interface AutoDismissCallBack {
        void onDialogAutoDismiss();
    }

    public interface ButtonCallBack {
        void onClickPositiveButton();
        void onClickNegativeButton();
    }

    protected Activity mActivity;
    protected Resources mRes;
    protected AlertDialog mDialog;

    private ButtonCallBack mButtonCallBack;
    private AutoDismissCallBack mAutoDismissCallBack;

    private boolean mCanceledOnTouchOutside = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        int nStyle = DialogFragment.STYLE_NORMAL, nTheme = 0;
        setStyle(nStyle, nTheme);

        mActivity = getActivity();
        mRes = getResources();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = LayoutInflater.from(mActivity).inflate(getLayoutResource(), null, false);
        setupViews(view);
        mDialog = builder(view).create();
        mDialog.setCanceledOnTouchOutside(mCanceledOnTouchOutside);

        // auto dismiss
        if (autoDismiss()) {
            mDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                @Override
                public void onShow(DialogInterface dialog) {
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (mDialog.isShowing()) {
                                if (mAutoDismissCallBack != null) {
                                    mAutoDismissCallBack.onDialogAutoDismiss();
                                }
                                mDialog.dismiss();
                            }
                        }
                    }, getAutoDismissTimeout());
                }
            });
        }

        return mDialog;
    }

    protected AlertDialog.Builder builder(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setView(view);
        builder = setPositiveButton(builder);
        builder = setNegativeButton(builder);
        return builder;
    }

    protected AlertDialog.Builder setPositiveButton(AlertDialog.Builder builder) {
        return builder;
    }

    protected AlertDialog.Builder setNegativeButton(AlertDialog.Builder builder) {
        return builder;
    }

    protected void enablePositiveBtn(boolean enable) {
        if (mDialog == null || mDialog.getButton(AlertDialog.BUTTON_POSITIVE) == null) {
            return;
        }
        mDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(enable);
    }

    protected void enableNegativeBtn(boolean enable) {
        if (mDialog == null || mDialog.getButton(AlertDialog.BUTTON_NEGATIVE) == null) {
            return;
        }
        mDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setEnabled(enable);
    }

    public abstract int getLayoutResource();

    protected void setupViews(View view) {
    }

    public void setAutoDismissCallBack(AutoDismissCallBack cb) {
        mAutoDismissCallBack = cb;
    }

    public int getAutoDismissTimeout() {
        return 0;
    }

    public boolean autoDismiss() {
        return false;
    }

    public void setButtonCallBack(ButtonCallBack cb) {
        mButtonCallBack = cb;
    }

    protected class OnPositiveButtonClickListener implements DialogInterface.OnClickListener {

        @Override
        public void onClick(DialogInterface dialog, int which) {
            if (mButtonCallBack != null) {
                mButtonCallBack.onClickPositiveButton();
            }
        }
    }

    protected class OnNegativeButtonClickListener implements DialogInterface.OnClickListener {

        @Override
        public void onClick(DialogInterface dialog, int which) {
            if (mButtonCallBack != null) {
                mButtonCallBack.onClickNegativeButton();
            }
        }
    }

    public void setCanceledOnTouchOutside(boolean canceledOnTouchOutside) {
        mCanceledOnTouchOutside = canceledOnTouchOutside;
    }
}
