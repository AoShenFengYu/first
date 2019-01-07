package com.qisiemoji.apksticker.whatsapp;

import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.v7.widget.Toolbar;

import com.qisiemoji.apksticker.R;

public abstract class ToolBarActivity extends BaseActivity {

    private Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutResource());

        mToolbar = findViewById(R.id.toolbar);
        if (mToolbar!= null) {
            setSupportActionBar(mToolbar);
        }

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    public void setToolbarTitle(String title) {
        if (mToolbar!= null) {
            mToolbar.setTitle(title);
            setSupportActionBar(mToolbar);
        }
    }

    public void setToolbarSubTitle(String subTitle) {
        if (mToolbar!= null) {
            mToolbar.setSubtitle(subTitle);
        }
    }

    public void seToolbarDisplayHomeAsUpEnabled(boolean enable) {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(enable);
        }
    }

    @LayoutRes
    protected abstract int getLayoutResource();
}
