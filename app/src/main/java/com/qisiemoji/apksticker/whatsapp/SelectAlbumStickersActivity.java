package com.qisiemoji.apksticker.whatsapp;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Rect;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.qisiemoji.apksticker.R;
import com.qisiemoji.apksticker.util.DensityUtil;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class SelectAlbumStickersActivity extends AddStickerPackActivity {

    public static final int RESULT_CODE_FINISH_SELECT = 111;
    public static final String EXTRA_SELECTED_LIST = "selected_list";
    public static final String EXTRA_SELECTED_ITEM_MIN = "selected_item_min";
    public static final String EXTRA_SELECTED_ITEM_MAX = "selected_item_max";

    private static final int SELECTED_ITEM_MIN = 4; // FIXME current include icon
    private static final int SELECTED_ITEM_MAX = 31; // FIXME current include icon

    private static final int PERMISSION_STORAGE_REQUEST_CODE = 100;

    private AppCompatTextView mDone;
    private RecyclerView mRecyclerView;
    private SelectStickerAdapter mSelectStickerAdapter;
    private ScanStickerImagesTask mScanStickerImagesTask;

    private ArrayList<StickerItem> mAllItems = new ArrayList<>();
    private ArrayList<String> mSelectedImageUrls = new ArrayList<>();
    private int mSelectedIteMin;
    private int mSelectedIteMax;

    private interface ScanStickersCallback {
        void onFinishScan(ArrayList<StickerItem> items);
    }

    private interface SelectStickerItemCallback {
        void onClick(StickerItem item);
    }

    private SelectStickerItemCallback mSelectStickerItemCallback = new SelectStickerItemCallback() {
        @Override
        public void onClick(StickerItem stickerItem) {
            if (stickerItem.check) {
                stickerItem.check = false;
                mSelectedImageUrls.remove(stickerItem.imageUrl);
                mSelectStickerAdapter.notifyItemChanged(stickerItem.index);
            } else if (mSelectedImageUrls.size() < mSelectedIteMax) {
                stickerItem.check = true;
                mSelectedImageUrls.add(stickerItem.imageUrl);
                mSelectStickerAdapter.notifyItemChanged(stickerItem.index);
            } else if (mSelectedIteMax == 1) {
                for (StickerItem unselectedItem : mAllItems) {
                    if (unselectedItem.check) {
                        unselectedItem.check = false;
                        mSelectedImageUrls.remove(unselectedItem.imageUrl);
                        mSelectStickerAdapter.notifyItemChanged(unselectedItem.index);
                        break;
                    }
                }

                stickerItem.check = true;
                mSelectedImageUrls.add(stickerItem.imageUrl);
                mSelectStickerAdapter.notifyItemChanged(stickerItem.index);
            }
            updateDoneButton();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_album_sticker);

        mSelectedIteMin = getIntent().getExtras().getInt(EXTRA_SELECTED_ITEM_MIN, SELECTED_ITEM_MIN);
        mSelectedIteMax = getIntent().getExtras().getInt(EXTRA_SELECTED_ITEM_MAX, SELECTED_ITEM_MAX);

        GridLayoutManager manager = new GridLayoutManager(this, 4);
        mSelectStickerAdapter = new SelectStickerAdapter(this, mAllItems, mSelectStickerItemCallback);
        mRecyclerView = findViewById(R.id.recycler_view);
        mRecyclerView.setLayoutManager(manager);
        RecyclerViewSpacesItemDecoration itemDecoration =
                new RecyclerViewSpacesItemDecoration(DensityUtil.dp2px(this, 4), 0, 0, DensityUtil.dp2px(this, 4));
        itemDecoration.handleExtraTop(DensityUtil.dp2px(this, 12), 4);
        mRecyclerView.addItemDecoration(itemDecoration);
        mRecyclerView.setAdapter(mSelectStickerAdapter);

        mDone = findViewById(R.id.done);
        mDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.putStringArrayListExtra(EXTRA_SELECTED_LIST, mSelectedImageUrls);
                setResult(RESULT_CODE_FINISH_SELECT, intent);
                finish();
            }
        });

        askForStoragePermission();
    }

    @Override
    protected void onDestroy() {
        if (mScanStickerImagesTask != null) {
            mScanStickerImagesTask.cancel(true);
        }
        super.onDestroy();
    }

    private void onPermissionGranted() {
        updateDoneButton();
        startScanPhotoImagesTask();
    }

    private void updateDoneButton() {
        if (mSelectedImageUrls.size() < mSelectedIteMin) {
            mDone.setClickable(false);
            mDone.setEnabled(false);
            mDone.setTextColor(Color.parseColor("#93D6D3"));
        } else {
            mDone.setClickable(true);
            mDone.setEnabled(true);
            mDone.setTextColor(Color.parseColor("#0DB4AD"));
        }
        mDone.setText("Done(" + mSelectedImageUrls.size() + "/" + mSelectedIteMax + ")");
    }

    static class StickerItem {
        private String imageUrl;
        private boolean check;
        private int index;

        StickerItem(String imageUrl, boolean check, int index) {
            this.imageUrl = imageUrl;
            this.check = check;
            this.index = index;
        }
    }

    static class SelectStickerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        ArrayList<StickerItem> stickerItems;
        SelectStickerItemCallback selectStickerItemCallback;

        SelectStickerAdapter(Context context, ArrayList<StickerItem> items, SelectStickerItemCallback cb) {
            stickerItems = items;
            selectStickerItemCallback = cb;
        }

        public void setStickerItems(ArrayList<StickerItem> items) {
            stickerItems = items;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            return new ItemHolder(inflater.inflate(ItemHolder.LAYOUT, parent, false), selectStickerItemCallback);
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            ((ItemHolder) holder).bind(stickerItems.get(position));
        }

        @Override
        public int getItemCount() {
            return stickerItems.size();
        }
    }


    static class ItemHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        static final int LAYOUT = R.layout.item_view_select_stickers;

        SelectStickerItemCallback callback;
        AppCompatImageView imageView;
        AppCompatImageView check;
        StickerItem stickerItem;

        ItemHolder(View itemView, SelectStickerItemCallback cb) {
            super(itemView);
            imageView = itemView.findViewById(R.id.image);
            check = itemView.findViewById(R.id.check);
            callback = cb;
        }

        void bind(StickerItem item) {
            stickerItem = item;

            imageView.setImageDrawable(null);
            imageView.setOnClickListener(this);
            Glide.with(imageView.getContext())
                    .load(stickerItem.imageUrl)
                    .into(imageView);

            check.setImageResource(stickerItem.check ? R.drawable.cumstom_sticker_ic_box_p :
                    R.drawable.cumstom_sticker_ic_box_n);
        }

        @Override
        public void onClick(View v) {
            if (callback != null) {
                callback.onClick(stickerItem);
            }
        }
    }

    private void startScanPhotoImagesTask() {
        mSelectedImageUrls.clear();
        mScanStickerImagesTask = new ScanStickerImagesTask(new WeakReference<>(SelectAlbumStickersActivity.this.getApplicationContext())
                , mSelectedImageUrls, new ScanStickersCallback() {
            @Override
            public void onFinishScan(ArrayList<StickerItem> items) {
                mAllItems = items;
                mSelectStickerAdapter.setStickerItems(mAllItems);
                mSelectStickerAdapter.notifyDataSetChanged();
            }
        });
        mScanStickerImagesTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    static class ScanStickerImagesTask extends AsyncTask<Void, Void, ArrayList<StickerItem>> {
        WeakReference<Context> context;
        ScanStickersCallback callback;
        private ArrayList<String> selectedImageUrls;

        ScanStickerImagesTask(WeakReference<Context> c, ArrayList<String> selectedUrls, ScanStickersCallback cb) {
            this.context = c;
            this.selectedImageUrls = selectedUrls;
            this.callback = cb;
        }

        @Override
        protected ArrayList<StickerItem> doInBackground(Void... voids) {
            return loadAllSystemImages();
        }

        @Override
        protected void onPostExecute(ArrayList<StickerItem> items) {
            super.onPostExecute(items);
            callback.onFinishScan(items);
        }

        private ArrayList<StickerItem> loadAllSystemImages() {
            Context context = this.context.get();
            if (context == null) {
                return new ArrayList<>();
            }

            Uri uri;
            Cursor cursor;
            int column_index_data;
            ArrayList<String> listOfAllImages = new ArrayList<String>();
            String absolutePathOfImage;
            uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
            String[] projection = {MediaStore.MediaColumns.DATA};
            String orderBy = MediaStore.Images.ImageColumns.DATE_ADDED + " DESC";

            cursor = context.getContentResolver().query(uri, projection, null,
                    null, orderBy);

            column_index_data = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
            while (cursor.moveToNext()) {
                absolutePathOfImage = cursor.getString(column_index_data);

                listOfAllImages.add(absolutePathOfImage);
            }

            ArrayList<StickerItem> items = new ArrayList<>();
            for (int i = 0; i < listOfAllImages.size(); i++) {
                String s = listOfAllImages.get(i);
                boolean checked = (selectedImageUrls != null) && selectedImageUrls.contains(s);
                StickerItem item = new StickerItem(s, checked, i);
                items.add(item);
            }
            return items;
        }
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
                            ActivityCompat.requestPermissions(SelectAlbumStickersActivity.this,
                                    new String[]
                                            {Manifest.permission.READ_EXTERNAL_STORAGE
                                                    , Manifest.permission.WRITE_EXTERNAL_STORAGE}
                                    , PERMISSION_STORAGE_REQUEST_CODE);
                        }
                    });
                    builder.show();
                } else {
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.READ_EXTERNAL_STORAGE
                                    , Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            PERMISSION_STORAGE_REQUEST_CODE);
                }
            } else {
                onPermissionGranted();
            }
        } else {
            onPermissionGranted();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {

        switch (requestCode) {
            case PERMISSION_STORAGE_REQUEST_CODE: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    onPermissionGranted();
                } else {
                    Toast.makeText(this, "No permission for READ_EXTERNAL_STORAGE", Toast.LENGTH_LONG).show();
                    finish();
                }
                break;
            }
        }
    }

    private class RecyclerViewSpacesItemDecoration extends RecyclerView.ItemDecoration {

        private int leftSpace;
        private int rightSpace;
        private int topSpace;
        private int bottmSpace;

        // for first row
        private int extraTopSpace = 0;
        private int extraHandleCount = 0;

        public RecyclerViewSpacesItemDecoration(int leftSpace, int rightSpace, int topSpace, int bottmSpace) {
            this.leftSpace = leftSpace;
            this.rightSpace = rightSpace;
            this.topSpace = topSpace;
            this.bottmSpace = bottmSpace;
        }

        public void handleExtraTop(int space, int count) {
            extraTopSpace = space;
            extraHandleCount = count;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view,
                                   RecyclerView parent, RecyclerView.State state) {
            outRect.left = leftSpace;
            outRect.right = rightSpace;
            outRect.top = topSpace;
            outRect.bottom = bottmSpace;

            if (extraTopSpace != 0 && extraHandleCount != 0) {
                if (parent.getChildLayoutPosition(view) < extraHandleCount) {
                    outRect.top = extraTopSpace;
                }
            }
        }
    }
}
