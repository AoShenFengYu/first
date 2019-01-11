package com.qisiemoji.apksticker.whatsapp.manager;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.WorkerThread;
import android.support.v4.app.FragmentManager;
import android.text.TextUtils;
import android.util.Log;

import com.bluelinelabs.logansquare.LoganSquare;
import com.qisiemoji.apksticker.util.FileUtils2;
import com.qisiemoji.apksticker.util.FragmentUtil;
import com.qisiemoji.apksticker.util.SharedPreferencesUtils;
import com.qisiemoji.apksticker.whatsapp.Sticker;
import com.qisiemoji.apksticker.whatsapp.StickerContentProvider;
import com.qisiemoji.apksticker.whatsapp.StickerPack;
import com.qisiemoji.apksticker.whatsapp.fragment.WhatsAppVersionNotSupportDialogFragment;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class WAStickerManager {

    private static WAStickerManager sInstance;

    private static final String SHOW_CONTOUR_CROP_TIP = "show_contour_crop_tip";
    private static final String CREATE_PACK_AUTHOR = "create_pack_author";
    private static final String STICKER_PACK_FOLDER_NAME = "sticker_pack_folder_name";
    private static final String FILE_STICKER_PACK_PUBLISH_NAME = ".sticker_pack_publish_info";
    private static final String FILE_STICKER_PACK_LOCAL_NAME = ".sticker_pack_local_info";
    private static final String FILE_STICKER_PACK_SEARCH_LOCAL_NAME = ".sticker_pack_search_local_info";
    private static final int USER_DEDINED_START_FOLDER = 10000;

    public static final int REQUEST_CODE_CAMERA = 1888;
    public static final int REQUEST_TAKE_PHOTO = 1889;
    public static final int REQUEST_CODE_PERMISSION_STORAGE = 100;
    public static final int REQUEST_CODE_PERMISSION_CAMERA = 101;
    public static final int REQUEST_CODE_SELECT_ALBUM_STICKERS = 123;
    public static final int REQUEST_CODE_EDIT_IMAGE = 124;
    public static final int RESULT_CODE_FINISH_EDIT_IMAGE = 111;

    public static final String EXTRA_SELECTED_LIST = "selected_list";
    public static final String EXTRA_SELECTED_IMAGE_PATH = "selected_image_path";
    public static final String EXTRA_STICKER_PACK_DATA = "sticker_pack";

    private static final String GP_URL = "https://play.google.com/store/apps/details?id=com.qisiemoji.inputmethod.sticker.supermaker&referrer=utm_source=whatsapp_sticker&utm_medium=cpc&utm_term=sticker&utm_content=sticker_name&utm_campaign=spring_sale";

    private static final String CONSUMER_WHATSAPP_PACKAGE_NAME = "com.whatsapp";
    private static final String SMB_WHATSAPP_PACKAGE_NAME = "com.whatsapp.w4b";
    private static final int MIN_SUPPORT_WA_STICKER_VERSION_CODE = 452563;

    /**
     * Publish : for wa publish
     * Local : for local operation
     **/
    public enum FileStickerPackType {
        Publish, Local, SearchLocal
    }

    /**
     * For MainActivity to check whether to update content
     * Create : create pack
     * Update : update pack
     * Publish : publish pack
     **/
    public enum LastOperatedStickerPackState {
        None, Create, Update, Publish
    }

    private LastOperatedStickerPackState mLastOperatedStickerPackState = LastOperatedStickerPackState.None;

    private final Object mObject = new Object();

    public static WAStickerManager getInstance() {
        if (sInstance == null) {
            synchronized (WAStickerManager.class) {
                if (sInstance == null) {
                    sInstance = new WAStickerManager();
                }
            }
        }
        return sInstance;
    }

    private WAStickerManager() {

    }

    public List<StickerPack> getStickerPacks(Context context, FileStickerPackType type) {
        return queryAll(context, type);
    }

    public String getNextNewStickerPacksFolderName(Context context) {
        int originalFolderIndex = SharedPreferencesUtils.getInt(context.getApplicationContext()
                , STICKER_PACK_FOLDER_NAME, USER_DEDINED_START_FOLDER);
        int newFolderIndex = originalFolderIndex + 1;
        SharedPreferencesUtils.setInt(context, STICKER_PACK_FOLDER_NAME, newFolderIndex);
        return String.valueOf(newFolderIndex);
    }

    @WorkerThread
    public List<StickerPack> queryAll(Context context, FileStickerPackType type) {
        synchronized (mObject) {
            String fileName = (type == FileStickerPackType.Publish) ? FILE_STICKER_PACK_PUBLISH_NAME :
                    (type == FileStickerPackType.Local) ? FILE_STICKER_PACK_LOCAL_NAME : FILE_STICKER_PACK_SEARCH_LOCAL_NAME;

            File file = FileUtils2.getPrivateFile(context, fileName);
            List<StickerPack> list = new ArrayList<>();
            if (FileUtils2.isFileExist(file)) {
                FileInputStream fis = null;
                try {
                    fis = new FileInputStream(file);
                    list.addAll(LoganSquare.parseList(fis, StickerPack.class));
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    FileUtils2.closeQuietly(fis);
                }
            }
            return list;
        }
    }

    @WorkerThread
    public boolean saveAll(@NonNull Context context, List<StickerPack> packs, FileStickerPackType type) {
        synchronized (mObject) {
            String fileName = (type == FileStickerPackType.Publish) ? FILE_STICKER_PACK_PUBLISH_NAME :
                    (type == FileStickerPackType.Local) ? FILE_STICKER_PACK_LOCAL_NAME : FILE_STICKER_PACK_SEARCH_LOCAL_NAME;

            File file = FileUtils2.getPrivateFile(context, fileName);
            FileUtils2.delete(file);
            try {
                FileUtils2.createFileIfNecessary(file);
            } catch (IOException e) {
                return false;
            }

            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(file);
                LoganSquare.serialize(packs, fos, StickerPack.class);
                return true;
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (fos != null) {
                    try {
                        fos.close();
                    } catch (IOException e) {
                        //do nothing
                    }
                }
            }
        }
        return false;
    }

    @WorkerThread
    public int delete(@NonNull Context context, @NonNull String identifier, FileStickerPackType type) {
        synchronized (mObject) {
            List<StickerPack> temp = queryAll(context, type);
            int count = 0;
            for (int i = temp.size() - 1; i >= 0; i--) {
                if (TextUtils.equals(temp.get(i).identifier, identifier)) {
                    temp.remove(i);
                    count++;
                    break;
                }
            }
            saveAll(context, temp, type);
            return count;
        }
    }

    @WorkerThread
    public boolean delete(@NonNull Context context, FileStickerPackType type) {
        synchronized (mObject) {
            String fileName = (type == FileStickerPackType.Publish) ? FILE_STICKER_PACK_PUBLISH_NAME :
                    (type == FileStickerPackType.Local) ? FILE_STICKER_PACK_LOCAL_NAME : FILE_STICKER_PACK_SEARCH_LOCAL_NAME;

            File file = FileUtils2.getPrivateFile(context, fileName);
            return FileUtils2.delete(file);
        }
    }

    @WorkerThread
    public boolean save(@NonNull Context context, StickerPack pack, FileStickerPackType type) {
        return save(context, pack, type, true);
    }

    @WorkerThread
    public boolean save(@NonNull Context context, StickerPack pack, FileStickerPackType type, boolean update) {
        if (pack == null) {
            return false;
        }

        synchronized (mObject) {
            boolean result;
            List<StickerPack> list = queryAll(context, type);
            int size = list.size();
            int p = -1;
            for (int i = 0; i < size; i++) {
                StickerPack g = list.get(i);
                if (TextUtils.equals(g.identifier, pack.identifier)) {
                    if (update) {
                        list.set(i, pack);
                    } else {
                        list.remove(i);
                    }
                    p = i;
                    break;
                }
            }
            if (!update || p == -1) {
                list.add(pack);
            }
            result = saveAll(context, list, type);
            return result;
        }
    }

    @WorkerThread
    public boolean add(@NonNull Context context, List<StickerPack> packs, FileStickerPackType type) {
        synchronized (mObject) {

            boolean result;
            List<StickerPack> list = queryAll(context, type);
            int size = list.size();
            int groupSize = packs.size();
            for (int i = size - 1; i >= 0; i--) {
                StickerPack g = list.get(i);
                for (int j = 0; j < groupSize; j++) {
                    if (TextUtils.equals(g.identifier, packs.get(j).identifier)) {
                        list.remove(i);
                        break;
                    }
                }

            }
            list.addAll(packs);
            result = saveAll(context, list, type);
            return result;
        }
    }

    @WorkerThread
    public boolean update(@NonNull Context context, StickerPack pack, FileStickerPackType type) {
        synchronized (mObject) {
            List<StickerPack> list = queryAll(context, type);
            int size = list.size();
            for (int i = 0; i < size; i++) {
                StickerPack g = list.get(i);
                if (TextUtils.equals(g.identifier, pack.identifier)) {
                    list.set(i, pack);
                    break;
                }
            }
            return saveAll(context, list, type);
        }
    }

    public boolean isCreateStickerPackEnable() {
        return true;
    }

    public interface CreateStickerPackTaskCallback {
        void onFinishCreated(StickerPack pack);
    }

    public static class PublishStickerPackTask extends AsyncTask<Void, Void, StickerPack> {

        private static final int IMAGE_HEIGHT = 512;
        private static final int IMAGE_WIDTH = 512;
        private static final long MAX_STICKER_SIZE = 100000;
        private static final long MAX_ICON_SIZE = 50000;

        private static final String FILE_ = "file_";
        private static final String WEBP = ".webp";
        private static final String TRAY_IMAGE_FILE_NAME = "file.webp";

        private WeakReference<Context> contextRef;
        private CreateStickerPackTaskCallback callback;
        private StickerPack stickerPack;

        public PublishStickerPackTask(Context context, StickerPack stickerPack, CreateStickerPackTaskCallback callback) {
            this.contextRef = new WeakReference<>(context);
            this.callback = callback;
            this.stickerPack = stickerPack;
        }

        @Override
        protected StickerPack doInBackground(Void... voids) {
            if (contextRef == null || contextRef.get() == null) {
                return null;
            }
            Context context = contextRef.get();
            List<Sticker> stickers = new ArrayList<>();
            String identifierFolderPath = FileUtils2.getFileDir(context, StickerContentProvider.STICKERS_FILE
                    + File.separator + stickerPack.identifier) + File.separator;
            String srcTrayImageUrl = stickerPack.trayImageFile;
            if (srcTrayImageUrl != null) {
                String trayImagePath = identifierFolderPath + TRAY_IMAGE_FILE_NAME;
                File trayImageFile = new File(trayImagePath);
                if (!trayImageFile.exists()) {
                    generateWebp(srcTrayImageUrl, trayImagePath, MAX_ICON_SIZE);
                }
            }

            for (int index = 0; index < stickerPack.stickers.size(); index++) {
                String srcStickerUrl = stickerPack.stickers.get(index).getImageFileUrl();
                if (TextUtils.isEmpty(srcStickerUrl)) {
                    continue;
                }

                String fileName = FILE_ + String.valueOf(index) + WEBP;
                String filePath = identifierFolderPath + fileName;
                File file = new File(filePath);
                long fileSize;

                if (!file.exists()) {
                    fileSize = generateWebp(srcStickerUrl, filePath, MAX_STICKER_SIZE);
                } else {
                    fileSize = file.length();
                }

                Sticker sticker = new Sticker(fileName, new ArrayList<String>());
                sticker.setSize(fileSize);
                sticker.setImageFileUrl(filePath);
                stickers.add(sticker);
            }

            StickerPack pack = new StickerPack(stickerPack.identifier, stickerPack.name, stickerPack.publisher, TRAY_IMAGE_FILE_NAME, "", "", "", "");
            pack.setStickers(stickers);
            pack.androidPlayStoreLink = GP_URL;

            // manager in file
            WAStickerManager.getInstance().save(context.getApplicationContext(), pack, WAStickerManager.FileStickerPackType.Publish);

            // add pack info to StickerContentProvider's MATCHER
            StickerContentProvider.addStickerPackToMatcher(pack);
            return pack;
        }

        @Override
        protected void onPostExecute(StickerPack stickerPack) {
            super.onPostExecute(stickerPack);
            if (callback != null) {
                callback.onFinishCreated(stickerPack);
            }
        }

        private long generateWebp(String srcFilePath, String destFilePath, long imageMax) {
            // orignal bmp
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            Bitmap originalBmp = BitmapFactory.decodeFile(srcFilePath, options);
            if (originalBmp == null) {
                return 0;
            }
            // center inside bmp
            Bitmap targetBmp = getCenterInsideBitmap(originalBmp, IMAGE_WIDTH, IMAGE_HEIGHT);

            // check file size
            int compressQuality = 105; // from 100 to check, subtract 5 every time
            long streamLength = imageMax;
            while (streamLength >= imageMax && compressQuality > 0) {
                compressQuality -= 5;
                ByteArrayOutputStream bmpStream = new ByteArrayOutputStream();
                targetBmp.compress(Bitmap.CompressFormat.WEBP, compressQuality, bmpStream);
                byte[] bmpPicByteArray = bmpStream.toByteArray();
                streamLength = bmpPicByteArray.length;
            }

            try {
                // save webp to specific location
                File waFile = new File(destFilePath);
                FileOutputStream outputStreamWebp = new FileOutputStream(waFile);
                targetBmp.compress(Bitmap.CompressFormat.WEBP, compressQuality, outputStreamWebp);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

            return streamLength;
        }

        private Bitmap getCenterInsideBitmap(Bitmap inputBmp, int targetWidth, int targetHeight) {
            // scale bmp in 512*512 region
            float scale = Math.min(1f * targetWidth / inputBmp.getWidth(), 1f * targetHeight / inputBmp.getHeight());
            Bitmap scaleBmp = getScaleBitmap(inputBmp, scale);
            Bitmap outputBmp = Bitmap.createBitmap(targetWidth, targetHeight, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(outputBmp);
            Paint paint = new Paint();
            paint.setAntiAlias(true);
            Matrix matrix = new Matrix();
            int sbw = scaleBmp.getWidth();
            int sbh = scaleBmp.getHeight();
            Point targetCenter = new Point(targetWidth / 2, targetHeight / 2);
            Point scaleBmpCenter = new Point(sbw / 2, sbh / 2);
            matrix.postScale(1f, 1f, targetCenter.x, targetCenter.y);
            matrix.postTranslate(targetCenter.x - scaleBmpCenter.x, targetCenter.y - scaleBmpCenter.y);
            canvas.drawBitmap(scaleBmp, matrix, paint);
            return outputBmp;
        }

        private Bitmap getScaleBitmap(Bitmap origin, float scale) {
            if (origin == null) {
                return null;
            }
            int height = origin.getHeight();
            int width = origin.getWidth();
            Matrix matrix = new Matrix();
            matrix.postScale(scale, scale);
            Bitmap newBM = Bitmap.createBitmap(origin, 0, 0, width, height, matrix, false);
            return newBM;
        }
    }

    public interface SaveTempImageFileCallback {
        void onFinishSaved(String path);
    }

    public static class SaveTempImageFileTask extends AsyncTask<Void, Void, String> {

        private WeakReference<Context> contextRef;
        private Bitmap bitmap;
        private SaveTempImageFileCallback callback;

        public SaveTempImageFileTask(Context context, Bitmap bitmap, SaveTempImageFileCallback callback) {
            this.contextRef = new WeakReference<>(context);
            this.bitmap = bitmap;
            this.callback = callback;
        }

        @Override
        protected String doInBackground(Void... voids) {
            if (contextRef == null || contextRef.get() == null) {
                return null;
            }
            Context context = contextRef.get();

            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String filePath = FileUtils2.getFileDir(context, StickerContentProvider.STICKERS_TEMP_FILE) + File.separator + "PNG_" + timeStamp + ".png";

            try {
                File file = new File(filePath);
                FileOutputStream outputStreamWebp = new FileOutputStream(file);
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStreamWebp);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

            return filePath;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if (callback != null) {
                callback.onFinishSaved(s);
            }
        }
    }

    private boolean isWhatsAppVersionSupported(@NonNull Context context) {
        try {
            boolean consumerResult = isWhatsAppVersionSupportedFromProvider(context, CONSUMER_WHATSAPP_PACKAGE_NAME);
            boolean smbResult = isWhatsAppVersionSupportedFromProvider(context, SMB_WHATSAPP_PACKAGE_NAME);
            return consumerResult || smbResult;
        } catch (Exception e) {
            return false;
        }
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private boolean isWhatsAppVersionSupportedFromProvider(@NonNull Context context, String whatsappPackageName) {
        final PackageManager packageManager = context.getPackageManager();
        if (isPackageInstalled(whatsappPackageName, packageManager)) {
            try {
                PackageInfo packageInfo = packageManager.getPackageInfo(whatsappPackageName, 0);
                boolean supported = packageInfo.versionCode >= MIN_SUPPORT_WA_STICKER_VERSION_CODE;
                return supported;
            } catch (PackageManager.NameNotFoundException e) {
            }
        }
        return false;
    }

    private boolean isPackageInstalled(String packageName, PackageManager packageManager) {
        try {
            final ApplicationInfo applicationInfo = packageManager.getApplicationInfo(packageName, 0);
            //noinspection SimplifiableIfStatement
            if (applicationInfo != null) {
                return applicationInfo.enabled;
            } else {
                return false;
            }
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    public boolean showWhatsAppVersionNotSupportDailogIfNeed(Context context, FragmentManager fragmentManager) {
        if (isWhatsAppVersionSupported(context)) {
            return false;
        }
        WhatsAppVersionNotSupportDialogFragment fragment = WhatsAppVersionNotSupportDialogFragment.newInstance();
        FragmentUtil.showDialogFragment(fragmentManager, fragment, WhatsAppVersionNotSupportDialogFragment.DIALOG_FRAGMENT);
        return true;
    }

    public String getCreatPackAuthorIfExist(Context context) {
        return SharedPreferencesUtils.getString(context.getApplicationContext(), CREATE_PACK_AUTHOR, null);
    }

    public void setCreatPackAuthor(Context context, String author) {
        SharedPreferencesUtils.setString(context, CREATE_PACK_AUTHOR, author);
    }

    public boolean isShowContourCropTip(Context context) {
        return SharedPreferencesUtils.getBoolean(context.getApplicationContext(), SHOW_CONTOUR_CROP_TIP, true);
    }

    public void setShowContourCropTip(Context context, boolean show) {
        SharedPreferencesUtils.setBoolean(context.getApplicationContext(), SHOW_CONTOUR_CROP_TIP, show);
    }

    public void setLastOperatedStickerPackStateByPriority(LastOperatedStickerPackState state) {
        // id try to set "Update/Publish" to state , but it's origin state is "Create" => keep
        if ((state == LastOperatedStickerPackState.Update || state == LastOperatedStickerPackState.Publish) &&
                mLastOperatedStickerPackState == LastOperatedStickerPackState.Create) {
            return;
        }
        mLastOperatedStickerPackState = state;
    }

    public LastOperatedStickerPackState getLastOperatedStickerPackState() {
        return mLastOperatedStickerPackState;
    }

}
