package com.qisiemoji.apksticker.whatsapp;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.qisiemoji.apksticker.R;
import com.qisiemoji.apksticker.util.FragmentUtil;
import com.qisiemoji.apksticker.whatsapp.create_sticker_pack.CreateStickerPackDetailActivity;
import com.qisiemoji.apksticker.whatsapp.fragment.CreateStickerPackDialogFragment;
import com.qisiemoji.apksticker.whatsapp.manager.WAStickerManager;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CreatedStickerPackListActivity extends AddStickerPackActivity {

    private RecyclerView packRecyclerView;
    private CreatedStickerPackListAdapter createdStickerPackListAdapter;
    private WhiteListCheckAsyncTask whiteListCheckAsyncTask;
    private ArrayList<StickerPack> stickerPacks = new ArrayList<>();

    private interface QueryStickerPackTaskCallback {
        void onFinished(List<StickerPack> packs, boolean create);
    }

    private interface WhiteListCheckTaskCallback {
        void onFinishChecked(List<StickerPack> packs, boolean create);
    }

    private QueryStickerPackTaskCallback mQueryStickerPackTaskCallback = new QueryStickerPackTaskCallback() {
        @Override
        public void onFinished(List<StickerPack> packs, boolean create) {
            List<StickerPack> reverseOrderPacks = new ArrayList<>();
            for (StickerPack pack : packs) {
                reverseOrderPacks.add(0, pack);
            }
            stickerPacks.clear();
            stickerPacks.addAll(reverseOrderPacks);
            showStickerPackList(stickerPacks, create);
        }
    };

    private WhiteListCheckTaskCallback mWhiteListCheckTaskCallback = new WhiteListCheckTaskCallback() {
        @Override
        public void onFinishChecked(List<StickerPack> packs, boolean create) {
            createdStickerPackListAdapter.setStickerPackList(packs);
            packRecyclerView.setAdapter(createdStickerPackListAdapter);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.created_sticker_pack_list);
        Fresco.initialize(this);
        overridePendingTransition(0, 0);
        packRecyclerView = findViewById(R.id.sticker_pack_list);
        LinearLayoutManager packLayoutManager = new LinearLayoutManager(this);
        packLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(
                packRecyclerView.getContext(),
                packLayoutManager.getOrientation()
        );
        packRecyclerView.addItemDecoration(dividerItemDecoration);
        packRecyclerView.setLayoutManager(packLayoutManager);

        createdStickerPackListAdapter = new CreatedStickerPackListAdapter(this, new CreatedStickerPackListAdapter.CreatedStickerPackListAdapterListener() {
            @Override
            public void onClickPack(StickerPack stickerPack) {
                Intent intent = CreateStickerPackDetailActivity.edit(CreatedStickerPackListActivity.this, stickerPack);
                CreatedStickerPackListActivity.this.startActivity(intent);
            }
        });
        packRecyclerView.setAdapter(createdStickerPackListAdapter);

        View creatPack = findViewById(R.id.create_pack_content);
        creatPack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialogFragment();
            }
        });

        startQueryStickerPackTask(true);
    }

    @Override
    protected void onResume() {
        super.onResume();

        startQueryStickerPackTask(true);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (whiteListCheckAsyncTask != null && !whiteListCheckAsyncTask.isCancelled()) {
            whiteListCheckAsyncTask.cancel(true);
        }
    }

    private void startQueryStickerPackTask(boolean create) {
        QueryStickerPackTask task = new QueryStickerPackTask(this, mQueryStickerPackTaskCallback, create);
        task.execute();
    }

    private void showStickerPackList(ArrayList<StickerPack> stickerPackList, boolean create) {
        whiteListCheckAsyncTask = new WhiteListCheckAsyncTask(this, mWhiteListCheckTaskCallback, create);
        whiteListCheckAsyncTask.execute(stickerPackList.toArray(new StickerPack[stickerPackList.size()]));
    }

    static class WhiteListCheckAsyncTask extends AsyncTask<StickerPack, Void, List<StickerPack>> {

        private final WeakReference<Context> contextWeakReference;
        private WhiteListCheckTaskCallback callback;
        private boolean create;

        WhiteListCheckAsyncTask(Context context, WhiteListCheckTaskCallback checkTaskCallback, boolean create) {
            this.contextWeakReference = new WeakReference<>(context);
            this.callback = checkTaskCallback;
            this.create = create;
        }

        @Override
        protected final List<StickerPack> doInBackground(StickerPack... stickerPackArray) {
            final Context context = contextWeakReference.get();
            if (context == null) {
                return Arrays.asList(stickerPackArray);
            }

            for (StickerPack stickerPack : stickerPackArray) {
                stickerPack.setIsWhitelisted(WhitelistCheck.isWhitelisted(context, stickerPack.identifier));
            }
            return Arrays.asList(stickerPackArray);
        }

        @Override
        protected void onPostExecute(List<StickerPack> stickerPackList) {
            if (callback != null) {
                callback.onFinishChecked(stickerPackList, create);
            }
        }
    }

    private static class QueryStickerPackTask extends AsyncTask<Void, Void, List<StickerPack>> {

        private WeakReference<Context> contextWeakReference;
        private QueryStickerPackTaskCallback callback;
        private boolean create;

        private QueryStickerPackTask(Context context, QueryStickerPackTaskCallback callback, boolean create) {
            this.contextWeakReference = new WeakReference<>(context);
            this.callback = callback;
            this.create = create;
        }

        @Override
        protected List<StickerPack> doInBackground(Void... voids) {
            Context context = contextWeakReference.get();
            if (context != null) {
                return WAStickerManager.getInstance().queryAll(context, WAStickerManager.FileStickerPackType.Local);
            } else {
                return null;
            }
        }

        @Override
        protected void onPostExecute(List<StickerPack> stickerPacks) {
            super.onPostExecute(stickerPacks);
            if (callback != null && stickerPacks != null) {
                callback.onFinished(stickerPacks, create);
            }
        }
    }

    private void showDialogFragment() {
        if (!(this instanceof FragmentActivity)) {
            return;
        }

        FragmentActivity activity = (FragmentActivity) this;

        CreateStickerPackDialogFragment fragment = CreateStickerPackDialogFragment.newInstance();
        fragment.setCallBack(new CreateStickerPackDialogFragment.CreateStickerPackDialogFragmentCallBack() {
            @Override
            public void onClickCreateButton(String stickerPackName, String author) {
                startCreateStickerPackDetailActivity(null, stickerPackName, author);
            }

            @Override
            public void onClickCancelButton() {
            }
        });
        FragmentUtil.showDialogFragment(activity.getSupportFragmentManager(), fragment, CreateStickerPackDialogFragment.DIALOG_FRAGMENT);
    }

    private void startCreateStickerPackDetailActivity(StickerPack stickerPack, String packName, String author) {
        Intent intent;
        if (stickerPack == null) {
            intent = CreateStickerPackDetailActivity.create(this, packName, author);
        } else {
            intent = CreateStickerPackDetailActivity.edit(this, stickerPack);
        }
        startActivity(intent);
    }
}
