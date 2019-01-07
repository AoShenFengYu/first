package com.qisiemoji.apksticker.whatsapp.fragment;

import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.AppCompatTextView;
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
    private AppCompatTextView mStickerPackCount;
    private AppCompatTextView mAuthorCount;
    private View mAuthorInfo;
    private View mExpend;
    private View mClearAuthor;

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
        mStickerPackCount = view.findViewById(R.id.stickerpack_count);
        mAuthorCount = view.findViewById(R.id.author_count);
        updateTextCount(mStickerPackCount, 0, 30);
        updateTextCount(mAuthorCount, 0, 30);

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
        mStickerPackName.addTextChangedListener(new UploadTextWatcher(mStickerPackName, mStickerPackCount,30));
        mAuthorName = view.findViewById(R.id.author);
        mAuthorName.setFilters(new InputFilter[]{filter});
        mAuthorName.addTextChangedListener(new UploadTextWatcher(mAuthorName, mAuthorCount,30));
        mExpend = view.findViewById(R.id.expend);
        mExpend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mExpend.setVisibility(View.GONE);
                showHideAuthorInfo(true, true);
            }
        });
        mAuthorInfo = view.findViewById(R.id.author_info);
        mClearAuthor = view.findViewById(R.id.clear_author);
        mClearAuthor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                WAStickerManager.getInstance().setCreatPackAuthor(getContext(), null);
                mAuthorName.setText(null);
                mAuthorName.requestFocus();
            }
        });
        String originalAuthor = WAStickerManager.getInstance().getCreatPackAuthorIfExist(getContext());
        if (originalAuthor != null) {
            mAuthorName.setText(originalAuthor);
            showHideAuthorInfo(false, false);
        } else {
            mExpend.setVisibility(View.GONE);
            mClearAuthor.setVisibility(View.GONE);
            showHideAuthorInfo(true, false);
        }
    }

    private class UploadTextWatcher implements TextWatcher {

        AppCompatEditText editText;
        AppCompatTextView textView;
        int max;

        CharSequence temp;
        int selectionStart;
        int selectionEnd;
        int count;

        UploadTextWatcher(AppCompatEditText et, AppCompatTextView tv, int max) {
            this.editText = et;
            this.textView = tv;
            this.max = max;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            this.temp = s;
            this.count = count;
        }

        @Override
        public void afterTextChanged(Editable s) {
            selectionStart = editText.getSelectionStart();
            selectionEnd = editText.getSelectionEnd();

            if (temp.length() > max) {
                int tempSelection;
                if (selectionStart > max || selectionEnd > max) {
                    tempSelection = max;
                } else {
                    int exceedCount = temp.length() - max;
                    int availableCount = count - exceedCount;
                    tempSelection = selectionEnd - count + availableCount;
                }

                if (selectionStart - 1 < 0) {
                    // 如果 selectionStart - 1 = -1，則至少把第一個刪除掉
                    s.delete(0, selectionEnd == 0 ? 1 : selectionEnd);

                } else {
                    s.delete(selectionStart - 1, selectionEnd);
                }

                editText.setText(s);
                editText.setSelection(tempSelection);
            }

            updateTextCount(textView, s.length(), max);
            updateCreateButtonState();
        }
    }

    private void updateTextCount(AppCompatTextView textView, int num, int max) {
        textView.setText("(" + num + "/" + max + ")");
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

    private void showHideAuthorInfo(boolean show, boolean clear) {
        mAuthorInfo.setVisibility(show ? View.VISIBLE : View.GONE);
        mClearAuthor.setVisibility(clear ? View.VISIBLE : View.GONE);
    }
}
