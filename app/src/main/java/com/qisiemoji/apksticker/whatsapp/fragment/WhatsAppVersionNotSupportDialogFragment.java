package com.qisiemoji.apksticker.whatsapp.fragment;

import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;

import com.qisiemoji.apksticker.R;
import com.qisiemoji.apksticker.util.GooglePlay;

public class WhatsAppVersionNotSupportDialogFragment extends BasicDialogFragment {
    public static final String DIALOG_FRAGMENT = "wa_not_support_dialog_fragment";

    private static final String WHATS_APP_PACKAGE = "com.whatsapp";

    public static WhatsAppVersionNotSupportDialogFragment newInstance() {
        WhatsAppVersionNotSupportDialogFragment f = new WhatsAppVersionNotSupportDialogFragment();
        return f;
    }

    @Override
    public int getLayoutResource() {
        return R.layout.dialogfg_wa_version_not_support;
    }

    @Override
    protected AlertDialog.Builder setPositiveButton(AlertDialog.Builder builder) {
        builder.setPositiveButton(getResources().getText(R.string.string_update),
                new OnPositiveButtonClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        super.onClick(dialog, which);
                        GooglePlay.startGooglePlayOrByBrowser(mActivity, WHATS_APP_PACKAGE);
                    }
                });
        return builder;
    }

    @Override
    protected AlertDialog.Builder setNegativeButton(AlertDialog.Builder builder) {
        builder.setNegativeButton(getResources().getText(R.string.string_not_now),
                new OnNegativeButtonClickListener());
        return builder;
    }
}
