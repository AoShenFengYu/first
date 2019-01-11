package com.qisiemoji.apksticker.whatsapp.create_sticker_pack;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.qisiemoji.apksticker.R;
import com.qisiemoji.apksticker.util.FragmentUtil;
import com.qisiemoji.apksticker.views.WaStickerDialog;
import com.qisiemoji.apksticker.whatsapp.AddStickerPackActivity;
import com.qisiemoji.apksticker.whatsapp.Sticker;
import com.qisiemoji.apksticker.whatsapp.StickerPack;
import com.qisiemoji.apksticker.whatsapp.StickerPackLoader;
import com.qisiemoji.apksticker.whatsapp.WaStickerDownloadRunnable;
import com.qisiemoji.apksticker.whatsapp.fragment.ChooseImageSourceDialogFragment;
import com.qisiemoji.apksticker.whatsapp.manager.WAStickerManager;

import java.util.ArrayList;
import java.util.List;

import static com.qisiemoji.apksticker.whatsapp.manager.WAStickerManager.REQUEST_CODE_PERMISSION_STORAGE;

/**
 * 新版sticker详情页，支持浏览、新建pack、修改pack的功能
 */
public class CreateStickerPackDetailActivity extends AddStickerPackActivity implements CreateStickerPackDetailHandler.Callback, ChooseImageSourceDialogFragment.ChooseImageSourceDialogFragmentCallback {

    /**
     * 空字串
     **/
    private static final String EMPTY = "";

    /**
     * Extra key. Do not change below values of below 3 lines as this is also used by WhatsApp
     */
    public static final String EXTRA_STICKER_PACK_ID = "sticker_pack_id";
    public static final String EXTRA_STICKER_PACK_AUTHORITY = "sticker_pack_authority";
    public static final String EXTRA_STICKER_PACK_NAME = "sticker_pack_name";

    /**
     * Option extra key
     **/
    private static final String EXTRA_STICKER_PACK_DATA = "sticker_pack";
    private static final String EXTRA_STICKER_PACK_AUTHOR = "author";
    private static final String EXTRA_SHOW_UP_BUTTON = "show_up_button";
    private static final String EXTRA_PREVIEW = "preview";

    /**
     * 建立Intent
     **/
    public static Intent create(Context context, String packName, String author) {
        Intent intent = new Intent(context, CreateStickerPackDetailActivity.class);
        intent.putExtra(EXTRA_STICKER_PACK_NAME, packName);
        intent.putExtra(EXTRA_STICKER_PACK_AUTHOR, author);
        return intent;
    }

    public static Intent edit(Context context, StickerPack stickerPack) {
        Intent intent = new Intent(context, CreateStickerPackDetailActivity.class);
        intent.putExtra(EXTRA_STICKER_PACK_DATA, stickerPack);
        intent.putExtra(EXTRA_SHOW_UP_BUTTON, false);
        return intent;
    }

    public static Intent preview(Context context, StickerPack stickerPack) {
        Intent intent = new Intent(context, CreateStickerPackDetailActivity.class);
        intent.putExtra(EXTRA_STICKER_PACK_DATA, stickerPack);
        intent.putExtra(EXTRA_SHOW_UP_BUTTON, true);
        intent.putExtra(EXTRA_PREVIEW, true);

        return intent;
    }

    /**
     * Icon
     **/
    private ImageView mPackTrayImageBG;
    private ImageView mPackTrayImageView;
    private TextView mPackNameTextView;
    private TextView mPackPublisherTextView;
    private TextView mPackSizeTextView;


    /**
     * Add to wa
     **/
    private View mAddToWaButton;
    private View mAlreadyAddedText;
    private WAStickerManager.PublishStickerPackTask mPublishStickerPackTask;

    /**
     * 動態設置span count，由於目前暫時固定span count，所以用不到
     **/
    private final ViewTreeObserver.OnGlobalLayoutListener mPageLayoutListener = new ViewTreeObserver.OnGlobalLayoutListener() {
        @Override
        public void onGlobalLayout() {
//            setNumColumns(mRecyclerView.getWidth() / mRecyclerView.getContext().getResources().getDimensionPixelSize(R.dimen.sticker_pack_details_image_size));
            setNumColumns(SPAN_COUNT);
        }
    };

    /**
     * List
     **/
    public static final int SPAN_COUNT = 4;

    private final RecyclerView.OnScrollListener mDividerScrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrollStateChanged(@NonNull final RecyclerView recyclerView, final int newState) {
            super.onScrollStateChanged(recyclerView, newState);
            updateDivider(recyclerView);
        }

        @Override
        public void onScrolled(@NonNull final RecyclerView recyclerView, final int dx, final int dy) {
            super.onScrolled(recyclerView, dx, dy);
            updateDivider(recyclerView);
        }

        private void updateDivider(RecyclerView recyclerView) {
            boolean showDivider = recyclerView.computeVerticalScrollOffset() > 0;
            if (mDivider != null) {
                mDivider.setVisibility(showDivider ? View.VISIBLE : View.INVISIBLE);
            }
        }
    };

    private final CreateStickerPackDetailAdapterCallback mAdapterCallback = new CreateStickerPackDetailAdapterCallback() {

        @Override
        public void onClickAdd(int index, StickerItem stickerItem) {
            showDialogFragment();
        }

        @Override
        public void onClickEdit(int index, StickerItem item) {
            ArrayList<String> urls = new ArrayList<>();
            urls.add(item.getImageUrl());
//            Intent intent = new Intent(CreateStickerPackDetailActivity.this, EditImageActivity.class);
//            intent.putStringArrayListExtra(EXTRA_SELECTED_LIST, urls);
//            startActivityForResult(intent, REQUEST_CODE_EDIT_IMAGE);
        }

        @Override
        public void onClickDelete(int index, StickerItem item) {
            for (int i = index; i < mAllItems.size(); i++) {
                StickerItem currentItem = mAllItems.get(i);
                StickerItem nextItem = null;
                Sticker currentSticker = mStickerPack.stickers.get(i);
                Sticker nextSticker = null;

                if (i + 1 < mAllItems.size()) {
                    nextItem = mAllItems.get(i + 1);
                }

                if (i + 1 < mStickerPack.stickers.size()) {
                    nextSticker = mStickerPack.stickers.get(i + 1);
                }

                if (nextItem == null) {
                    currentItem.setImageUrl(null);
                    currentSticker.setImageFileUrl(null);

                } else {
                    currentItem.setImageUrl(nextItem.getImageUrl());
                    currentSticker.setImageFileUrl(nextSticker.getImageFileUrl());
                }

                if (i == 0) {
                    // 如果在更新第一個item，那麼連Icon也要更新
                    mStickerPack.trayImageFile = currentItem.getImageUrl();
                }
            }

            updateList(index);
            WAStickerManager.getInstance().setLastOperatedStickerPackStateByPriority(WAStickerManager.LastOperatedStickerPackState.Update);
            WAStickerManager.getInstance().update(getApplicationContext(), mStickerPack, WAStickerManager.FileStickerPackType.Local);
        }

    };

    private RecyclerView mRecyclerView;
    private View mDivider;
    private GridLayoutManager mLayoutManager;
    private CreateStickerPackDetailAdapter mAdapter;

    /**
     * Data
     **/
    private StickerPack mStickerPack;
    private String mPackName;
    private String mAuthor;
    private boolean mIsButtonShownUp;
    private boolean mIsPreview;
    private ArrayList<StickerItem> mAllItems = new ArrayList<>();

    /**
     * 白名單的確認
     **/
    private WhiteListCheckAsyncTask mWhiteListCheckAsyncTask;

    /**
     * Task of adding sticker to wa
     **/
    private WAStickerManager.CreateStickerPackTaskCallback mCreateStickerPackTaskCallback = new WAStickerManager.CreateStickerPackTaskCallback() {
        @Override
        public void onFinishCreated(StickerPack pack) {
            addStickerPackToWhatsApp(pack.identifier, pack.name);
        }
    };

    /**
     * Download Sticker
     **/
    private CreateStickerPackDetailHandler mHandler;
    private boolean mIsDownloading;
    private WaStickerDialog mDownloadProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_sticker_pack_detail);

        settingIcon();
        settingButton();
        settingList();
        settingDownloader();

        initStickerPack();

        askForStoragePermission();
    }

    private void settingDownloader() {
        mHandler = new CreateStickerPackDetailHandler(this);

    }

    private void settingButton() {
        mAddToWaButton = findViewById(R.id.add_to_whatsapp_button);
        mAlreadyAddedText = findViewById(R.id.already_added_text);

        mAddToWaButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                if (WAStickerManager.getInstance().showWhatsAppVersionNotSupportDailogIfNeed(CreateStickerPackDetailActivity.this, CreateStickerPackDetailActivity.this.getSupportFragmentManager())) {
//                    return;
//                }
//
//                WAStickerManager.getInstance().setLastOperatedStickerPackStateByPriority(WAStickerManager.LastOperatedStickerPackState.Publish);
//                mPublishStickerPackTask = new WAStickerManager.PublishStickerPackTask(CreateStickerPackDetailActivity.this, mStickerPack, mCreateStickerPackTaskCallback);
//                mPublishStickerPackTask.execute();

                if (mIsDownloading || mStickerPack == null) {
                    return;
                }

                WaStickerDownloadRunnable thread = new WaStickerDownloadRunnable(CreateStickerPackDetailActivity.this, mHandler, mStickerPack);
                thread.start();
                mStickerPack.startDownload(thread);
                showDialog();
            }
        });
    }

    private void settingIcon() {
        mPackTrayImageBG = findViewById(R.id.tray_image_bg);
        mPackTrayImageView = findViewById(R.id.tray_image);
        mPackNameTextView = findViewById(R.id.pack_name);
        mPackPublisherTextView = findViewById(R.id.author);
        mPackSizeTextView = findViewById(R.id.pack_size);
    }

    private void settingList() {
        mRecyclerView = findViewById(R.id.sticker_list);
        mLayoutManager = new GridLayoutManager(this, SPAN_COUNT);
        mAdapter = new CreateStickerPackDetailAdapter(this, mAllItems, mAdapterCallback);
        mDivider = findViewById(R.id.divider);


        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.getViewTreeObserver().addOnGlobalLayoutListener(mPageLayoutListener);
        mRecyclerView.addOnScrollListener(mDividerScrollListener);

        int dividerWidth = getResources().getDimensionPixelOffset(R.dimen.create_pack_rv_divider_width);
        CreateStickerPackDetaiItemDecoration itemDecoration = new CreateStickerPackDetaiItemDecoration(SPAN_COUNT, dividerWidth, true);

        mRecyclerView.addItemDecoration(itemDecoration);
        mRecyclerView.setAdapter(mAdapter);
    }

    private void onStoragePermissionGranted() {
        updateList();
        updateIcon();
    }

    private void initStickerPack() {
        Intent intent = getIntent();

        mIsButtonShownUp = intent.getBooleanExtra(EXTRA_SHOW_UP_BUTTON, false);
        mIsPreview = intent.getBooleanExtra(EXTRA_PREVIEW, false);

        if (mIsPreview) {
            // preview
            mStickerPack = getIntent().getParcelableExtra(EXTRA_STICKER_PACK_DATA);
            mPackName = mStickerPack.name;
            mAuthor = mStickerPack.publisher;

        } else if (intent.hasExtra(EXTRA_STICKER_PACK_DATA)) {
            // edit
            mStickerPack = getIntent().getParcelableExtra(EXTRA_STICKER_PACK_DATA);
            mPackName = mStickerPack.name;
            mAuthor = mStickerPack.publisher;

        } else {
            // create
            mPackName = getIntent().getStringExtra(EXTRA_STICKER_PACK_NAME);
            mAuthor = getIntent().getStringExtra(EXTRA_STICKER_PACK_AUTHOR);
            List<Sticker> stickers = new ArrayList<>();
            String identifier = WAStickerManager.getInstance().getNextNewStickerPacksFolderName(this);
            for (int i = 0; i < 30; i++) {
                Sticker sticker = new Sticker(null, null);
                sticker.imageFileUrl = null;
                stickers.add(sticker);
            }
            mStickerPack = new StickerPack(identifier, mPackName, mAuthor, null, "", "", "", "");
            mStickerPack.setStickers(stickers);
            WAStickerManager.getInstance().setLastOperatedStickerPackStateByPriority(WAStickerManager.LastOperatedStickerPackState.Create);
            WAStickerManager.getInstance().save(getApplicationContext(), mStickerPack, WAStickerManager.FileStickerPackType.Local);
        }
    }

    /**
     * 列表更新, 會重建mAllItems
     **/
    private void updateList() {
        mAllItems.clear();
        for (int i = 0; i < mStickerPack.stickers.size(); i++) {
            StickerItem item = new StickerItem(mStickerPack.stickers.get(i).imageFileUrl);
            mAllItems.add(item);
        }
        mAdapter.setStickerItems(mAllItems, mIsPreview);
        setCanPublish(isValidContent(), true);
        mAdapter.notifyDataSetChanged();
    }

    /**
     * 列表更新
     **/
    private void updateList(int startIndex) {
        mAdapter.notifyItemRangeChanged(startIndex, mAdapter.getItemCount() - 1);

        updateIcon();
        setCanPublish(isValidContent(), true);
    }

    private void updateIcon() {
        mPackNameTextView.setText(mPackName);
        mPackPublisherTextView.setText(mAuthor);

        if (mStickerPack == null) {
            return;
        }
        Log.e("AAAAA", "AA=" + mStickerPack.trayImageFile);
        Log.e("AAAAA", "ＢＢ=" + StickerPackLoader.getStickerAssetUri(mStickerPack.identifier, mStickerPack.trayImageFile));
        Log.e("AAAAA", "cc=" + mStickerPack.trayImageUrl);

        Glide.with(mPackTrayImageView.getContext())
                .load(StickerPackLoader.getStickerAssetUri(mStickerPack.identifier, mStickerPack.trayImageFile))
                .dontTransform()
                .dontAnimate()
                .into(mPackTrayImageView);

        if (mIsPreview) {
            mPackTrayImageBG.setBackgroundColor(Color.TRANSPARENT);
        }
//        mPackTrayIcon.setImageURI(StickerPackLoader.getStickerAssetUri(mStickerPack.identifier, mStickerPack.trayImageFile));
//        mPackSizeTextView.setText(Formatter.formatShortFileSize(this, mStickerPack.getTotalSize()));
    }

    public void updateAddButton(Boolean isWhitelisted) {
        if (isWhitelisted) {
            mAddToWaButton.setVisibility(View.GONE);
            mAlreadyAddedText.setVisibility(View.VISIBLE);
        } else {
            mAddToWaButton.setVisibility(View.VISIBLE);
            mAlreadyAddedText.setVisibility(View.GONE);
        }
    }

    /**
     * 設置Add to wa按鈕的enable
     **/
    private void setCanPublish(boolean validContent, boolean canPublish) {

    }

    private boolean isValidContent() {
        if (mStickerPack == null || mStickerPack.trayImageFile == null || mStickerPack.stickers == null) {
            return false;
        }

        // Whatsapp nees 1 icon 3 stickers
        int stickerCount = 0;
        for (Sticker sticker : mStickerPack.stickers) {
            if (sticker.imageFileUrl != null) {
                stickerCount = stickerCount + 1;
            }
            if (stickerCount >= 3) {
                return true;
            }
        }
        return false;
    }

    private void askForStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Storage access");
                    builder.setPositiveButton(android.R.string.ok, null);
                    builder.setMessage("please confirm Storage access");
                    builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                            ActivityCompat.requestPermissions(CreateStickerPackDetailActivity.this,
                                    new String[]
                                            {Manifest.permission.READ_EXTERNAL_STORAGE
                                                    , Manifest.permission.WRITE_EXTERNAL_STORAGE}
                                    , REQUEST_CODE_PERMISSION_STORAGE);
                        }
                    });
                    builder.show();
                } else {
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.READ_EXTERNAL_STORAGE
                                    , Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            REQUEST_CODE_PERMISSION_STORAGE);
                }
            } else {
                onStoragePermissionGranted();
            }
        } else {
            onStoragePermissionGranted();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_CODE_PERMISSION_STORAGE: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    onStoragePermissionGranted();
                } else {
                    Toast.makeText(this, "No permission for READ_EXTERNAL_STORAGE", Toast.LENGTH_LONG).show();
                    finish();
                }
                break;
            }
            default:
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


    }

    private int getStickerCount() {
        if (mStickerPack == null) {
            return 0;
        }

        int stickerCount = 0;
        if (mStickerPack.trayImageFile != null) {
            stickerCount = stickerCount + 1;
        }

        if (mStickerPack.stickers == null) {
            return stickerCount;
        }

        for (Sticker sticker : mStickerPack.stickers) {
            if (sticker.imageFileUrl != null) {
                stickerCount = stickerCount + 1;
            }
        }
        return stickerCount;
    }

    private void setNumColumns(int spanCount) {
        if (mLayoutManager.getSpanCount() != spanCount) {
            mLayoutManager.setSpanCount(spanCount);
            if (mAdapter != null) {
                mAdapter.notifyDataSetChanged();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mWhiteListCheckAsyncTask = new WhiteListCheckAsyncTask(this);
        mWhiteListCheckAsyncTask.execute(mStickerPack);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mWhiteListCheckAsyncTask != null && !mWhiteListCheckAsyncTask.isCancelled()) {
            mWhiteListCheckAsyncTask.cancel(true);
            mWhiteListCheckAsyncTask = null;
        }
    }

    @Override
    protected void onDestroy() {
        if (mPublishStickerPackTask != null) {
            mPublishStickerPackTask.cancel(true);
            mPublishStickerPackTask = null;
        }
        super.onDestroy();
    }

    /**
     * 显示下载对话框，不可关闭
     */
    private void showDialog() {
        if (mStickerPack == null) {
            return;
        }

        mIsDownloading = true;
        mDownloadProgressDialog = new WaStickerDialog(this, R.style.CustomDialog, mStickerPack);
        mDownloadProgressDialog.setCancelable(false);
        mDownloadProgressDialog.show();
    }

    /**
     * CreateStickerPackDetailHandler 's Callback
     **/
    @Override
    public void onDownloadSuccess(StickerPack pack) {
        mIsDownloading = false;
        addStickerPackToWhatsApp(pack.identifier, pack.name);
        if (mDownloadProgressDialog != null) {
            mDownloadProgressDialog.dismiss();
        }
    }

    @Override
    public void onUpdateProgress(StickerPack pack) {
        if (mDownloadProgressDialog == null) {
            return;
        }
        mDownloadProgressDialog.updateProgress(pack.totle, pack.count);
    }

    @Override
    public void onDownloadError() {
        mIsDownloading = false;
        Toast.makeText(this, "Download error.", Toast.LENGTH_LONG).show();
        if (mDownloadProgressDialog != null) {
            mDownloadProgressDialog.dismiss();
        }
    }

    /**
     * 出發去外部取得圖片路徑
     **/
    private void showDialogFragment() {
        ChooseImageSourceDialogFragment fragment = ChooseImageSourceDialogFragment.newInstance();
        fragment.setCallBack(CreateStickerPackDetailActivity.this);
        FragmentUtil.showDialogFragment(CreateStickerPackDetailActivity.this.getSupportFragmentManager(), fragment, ChooseImageSourceDialogFragment.DIALOG_FRAGMENT);
    }

    /**
     * 處理從外部取得的圖片路徑
     **/
    @Override
    public void onGetImagePathFromOutside(ArrayList<String> imagePaths) {
        handleReceivedImagePaths(imagePaths);
    }

    private void handleReceivedImagePaths(ArrayList<String> imagePaths) {
        if (imagePaths == null || imagePaths.size() == 0) {
            return;
        }

        int lastIndex = -1;
        for (int i = 0; i < mAllItems.size(); i++) {
            StickerItem item = mAllItems.get(i);
            if (TextUtils.isEmpty(item.getImageUrl())) {
                lastIndex = i;
                break;
            }
        }

        if (lastIndex == -1) {
            // 滿了，無法再新增圖片
            return;
        }

        for (int i = 0; i < imagePaths.size(); i++) {
            if (lastIndex + i >= mAllItems.size()) {
                // 滿了，無法再新增圖片
                break;
            }

            String imagePath = imagePaths.get(i);
            if (TextUtils.isEmpty(imagePath)) {
                // 無效的圖片路徑，跳過
                imagePaths.remove(i);
                i--;
                continue;
            }

            int itemIndex = lastIndex + i;
            StickerItem item = mAllItems.get(itemIndex);
            item.setImageUrl(imagePath);
            mStickerPack.stickers.get(itemIndex).imageFileUrl = imagePath;

            if (itemIndex == 0) {
                // 第一張要順便塞給Icon
                mStickerPack.trayImageFile = imagePath;
            }
        }

        updateList(lastIndex);
        WAStickerManager.getInstance().setLastOperatedStickerPackStateByPriority(WAStickerManager.LastOperatedStickerPackState.Update);
        WAStickerManager.getInstance().update(getApplicationContext(), mStickerPack, WAStickerManager.FileStickerPackType.Local);
    }
}
