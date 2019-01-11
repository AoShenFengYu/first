package com.qisiemoji.apksticker.whatsapp.fragment;

import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatEditText;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextWatcher;
import android.view.View;

import com.qisiemoji.apksticker.R;
import com.qisiemoji.apksticker.whatsapp.manager.WAStickerManager;

public class CreateStickerPackDialogFragment extends BasicDialogFragment {
    public static final String DIALOG_FRAGMENT = "create_sticker_pack_dialog_fragment";

    public static CreateStickerPackDialogFragment newInstance() {
        CreateStickerPackDialogFragment f = new CreateStickerPackDialogFragment();
        return f;
    }

    private AppCompatEditText mStickerPackName;
    private AppCompatEditText mAuthorName;

    public interface CreateStickerPackDialogFragmentCallBack {
        void onClickCreateButton(String stickerPackName, String author);

        void onClickCancelButton();
    }

    private CreateStickerPackDialogFragmentCallBack mCreateStickerPackDialogFragmentCallBack;

    public void setCallBack(CreateStickerPackDialogFragmentCallBack callBack) {
        mCreateStickerPackDialogFragmentCallBack = callBack;
    }

    @Override
    public int getLayoutResource() {
        return R.layout.dialogfg_create_sticker_pack;
    }

    @Override
    protected AlertDialog.Builder setPositiveButton(AlertDialog.Builder builder) {
        builder.setPositiveButton(getResources().getText(R.string.string_create),
                new OnPositiveButtonClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        super.onClick(dialog, which);
                        WAStickerManager.getInstance().setCreatPackAuthor(mActivity, mAuthorName.getText().toString());
                        if (mCreateStickerPackDialogFragmentCallBack != null) {
                            mCreateStickerPackDialogFragmentCallBack.onClickCreateButton(mStickerPackName.getText().toString(), mAuthorName.getText().toString());
                        }
                    }
                });
        return builder;
    }

    @Override
    protected AlertDialog.Builder setNegativeButton(AlertDialog.Builder builder) {
        builder.setNegativeButton(getResources().getText(R.string.string_cancel),
                new OnNegativeButtonClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        super.onClick(dialog, which);
                        if (mCreateStickerPackDialogFragmentCallBack != null) {
                            mCreateStickerPackDialogFragmentCallBack.onClickCancelButton();
                        }
                    }
                });
        return builder;
    }

    @Override
    public void onResume() {
        super.onResume();

        updateCreateButtonState();
    }

    @Override
    protected void setupViews(View view) {
        InputFilter filter = new InputFilter() {
            @Override
            public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                if (source.equals(" ") || source.toString().contentEquals("\n")) {
                    return "";
                } else {
                    return null;
                }
            }
        };
        mStickerPackName = view.findViewById(R.id.stickerpack);
        mStickerPackName.setFilters(new InputFilter[]{filter});
        mStickerPackName.addTextChangedListener(new UploadTextWatcher());
        mAuthorName = view.findViewById(R.id.author);
        mAuthorName.setFilters(new InputFilter[]{filter});
        mAuthorName.addTextChangedListener(new UploadTextWatcher());
    }

    private class UploadTextWatcher implements TextWatcher {

        UploadTextWatcher() {
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            updateCreateButtonState();
        }
    }

    private void updateCreateButtonState() {
        if (mDialog == null) {
            return;
        }
        if (mStickerPackName.getText().toString().length() == 0 ||
                mAuthorName.getText().toString().length() == 0) {
            mDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
        } else {
            mDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
        }
    }
}
