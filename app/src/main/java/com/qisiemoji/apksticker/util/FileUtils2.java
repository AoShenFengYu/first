package com.qisiemoji.apksticker.util;

import android.content.Context;
import android.content.res.AssetManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.nio.channels.FileChannel;

/**
 * Created by momo on 7/13/16.
 */
public final class FileUtils2 {
    public static final String TAG = "FileUtil";

    public static final String NAME_DICTIONARY_LOCAL = "dictLocal";
    public static final String NAME_DICTIONARY_SERVER = "dictServer";
    private static final String NAME_THEME = "theme";
    private final static String OBJECTS_DIR = "saved_objects";
    private static final String NAME_NAVIGATION = "navigation";
    private static final String NAME_EMOJI_LOCAL = "emojiLocal";
    private static final String NAME_SEARCH = "search";
    private static final String NAME_PACK_THEME = "pack_theme";
    private static final String NAME_PACK_THEME_CACHE = "pack_theme_cache";
    private static final String NAME_EMOJI_TTF = "emoji_ttf";
    private static final long MIN_USABLE_SPACE = 1024 * 1024 * 10;

    /**
     * Get cache dir if external cache dir is available, otherwise use {@link Context}.getCacheDir()
     *
     * @param context can't be null
     * @return cache dir
     */
    public static File getCacheDir(@NonNull Context context) {
        try {
            File[] files = ContextCompat.getExternalCacheDirs(context);
            if (files != null && files.length > 0) {
                File file = files[0];
                if (file != null) {
                    createFolderIfNecessary(file);
                    return file;
                }
            }
        } catch (Throwable e) {
        }
        return context.getCacheDir();
    }

    /**
     * Get file dir if external file dir is available, otherwise use {@link Context}.getFilesDir()
     *
     * @param context can't be null
     * @return file dir
     */
    public static File getFileDir(@NonNull Context context) {
        File[] files = null;
        try {
            files = ContextCompat.getExternalFilesDirs(context, null);
        } catch (Throwable e) {
        }
        File file;
        if (files != null && files.length > 0) {
            file = files[0];
            createFolderIfNecessary(file);
            if (isFolderExist(file)) {
                return file;
            }
        }
        file = context.getFilesDir();
        createFolderIfNecessary(file);
        return file;
    }

    public static File getCacheDir(@NonNull Context context, @NonNull String dirName) {
        File dir = getCacheDir(context);
        dir = new File(dir, dirName);

        createFolderIfNecessary(dir);

        return dir;
    }

    public static File getFileDir(@NonNull Context context, @NonNull String dirName) {
        File dir = getFileDir(context);
        dir = new File(dir, dirName);

        createFolderIfNecessary(dir);

        return dir;
    }

    public static File getInnerFileDir(@NonNull Context context, @NonNull String dirName) {
        File dir = context.getFilesDir();
        dir = new File(dir, dirName);

        createFolderIfNecessary(dir);

        return dir;
    }

    public static File getInnerCacheDir(@NonNull Context context, @NonNull String dirName) {
        File dir = context.getCacheDir();
        dir = new File(dir, dirName);
        createFolderIfNecessary(dir);
        return dir;
    }

    public static boolean isFileExist(File file) {
        return file != null && file.exists() && file.isFile();
    }

    public static boolean isFolderExist(File folder) {
        return folder != null && folder.exists() && folder.isDirectory();
    }

    public static boolean createFolderIfNecessary(File folder) {
        if (folder != null) {
            if (!folder.exists() || !folder.isDirectory()) {
                return folder.mkdirs();
            }
            return true;
        }
        return false;
    }

    public static boolean createFileIfNecessary(File file) throws IOException {
        if (file == null) {
            return false;
        }
        createFolderIfNecessary(file.getParentFile());
        if (isFolderExist(file.getParentFile())) {
            if (!file.exists() || !file.isFile()) {
                return file.createNewFile();
            }
            return true;
        }
        return false;
    }

    public static boolean deleteDir(File dir) {
        if (dir == null) {
            return false;
        }
        if (dir.isDirectory()) {
            String[] children = dir.list();
            if (children != null) {
                for (String aChildren : children) {
                    boolean success = deleteDir(new File(dir, aChildren));
                    if (!success) {
                        return false;
                    }
                }
            }
            return dir.delete();
        }
        // 目录此时为空，可以删除
        return dir.delete();
    }

    public static boolean copyFileFromAssetsFile(Context context, String fileName, String destPath) {
        AssetManager am = context.getResources().getAssets();
        InputStream inStream = null;
        FileOutputStream outStream = null;
        File file = new File(destPath);
        createFolderIfNecessary(file.getParentFile());
        try {
            createFileIfNecessary(file);
            int byteRead;
            inStream = am.open(fileName);
            outStream = new FileOutputStream(destPath);
            byte[] buffer = new byte[1024];
            while ((byteRead = inStream.read(buffer)) != -1) {
                outStream.write(buffer, 0, byteRead);
            }
        } catch (IOException e) {
            return false;
        } finally {
            if (inStream != null) {
                try {
                    inStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (outStream != null) {
                try {
                    outStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return true;

    }

    /**
     * get location for search /data/data/<packageName>/files/search/
     *
     * @param context
     * @return
     */
    public static File getSearchFileDir(@NonNull Context context) {
        return getInnerFileDir(context, "search");
    }

    /**
     * get cache dir for stickers is located at /sdcard/Android/data/<packageName>/cache/stickers/
     *
     * @param context
     * @return
     */
    public static File getStickerCacheDir(@NonNull Context context) {
        return getCacheDir(context, "stickers");
    }

    /**
     * get location for fonts /sdcard/Android/data/<packageName>/files/fonts/
     *
     * @param context
     * @return
     */
    public static File getFontFileDir(@NonNull Context context) {
        return getFileDir(context, "fonts");
    }

    /**
     * get location for stickers /sdcard/Android/data/<packageName>/files/stickers/
     *
     * @param context
     * @return
     */
    public static File getStickerFileDir(@NonNull Context context) {
        return getFileDir(context, "stickers");
    }

    public static File getMemeFileDir(@NonNull Context context) {
        return getFileDir(context, "memes");
    }

    public static File getPopupFileDir(@NonNull Context context) {
        return getFileDir(context, "popup");
    }

    public static String getStickerDownloadCacheFilePath(@NonNull Context context,
                                                         @NonNull String fileName) {
        File dir = getStickerCacheDir(context);
        return new File(dir, MD5.getMD5(fileName) + ".zip").getAbsolutePath();
    }

    public static String getStickerDownloadCacheDirPath(@NonNull Context context,
                                                        @NonNull String key) {
        File dir = getStickerCacheDir(context);
        dir = new File(dir, MD5.getMD5(key));
        createFolderIfNecessary(dir);
        return dir.getAbsolutePath();

    }

    public static String getStickerStoreDirPath(@NonNull Context context,
                                                @NonNull String key) {
        File dir = getStickerFileDir(context);
        dir = new File(dir, MD5.getMD5(key));
        createFolderIfNecessary(dir);
        return dir.getAbsolutePath();
    }

    public static File getDictionaryNavigation(@NonNull Context context) {
        return getFileDir(context, NAME_NAVIGATION);
    }

    public static File getEmojiLocal(@NonNull Context context) {
        return getFileDir(context, NAME_EMOJI_LOCAL);
    }

    public static File getSearchDir(@NonNull Context context) {
        return getFileDir(context, NAME_SEARCH);
    }


    /**
     * 存所有EmojiTTF最终文件的目录
     * @param context
     * @return
     */
    public static File getEmojiTTFFolder(@NonNull Context context) {
        return getFileDir(context, NAME_EMOJI_TTF);
    }

    /**
     * 存下载EmojiTTF文件7z包的目录
     * @param context
     * @return
     */
    public static File getEmojiTTFCacheFolder(@NonNull Context context) {
        return getCacheDir(context, NAME_EMOJI_TTF);
    }

    /**
     * 存下载EmojiTTF文件为{name}解压出文件的目录
     * @param context
     * @return
     */
    public static File getEmojiTTFUn7zSaveFolder(@NonNull Context context, @NonNull String name) {
        File folder = getEmojiTTFFolder(context);
        File file = new File(folder, name);
        createFolderIfNecessary(file);
        return file;
    }

    /**
     * EmojiTTF目录下名字为{name}的文件, 只存内置ttf emoji
     * @param context
     * @param name
     * @return
     */
    public static File getEmojiTTFFile(@NonNull Context context, @NonNull String name) {
        File folder = getEmojiTTFUn7zSaveFolder(context, name);
        return new File(folder, name + ".ttf");
    }

    /**
     * 下载的name为{name}的TTFEmoji文件
     * @param context
     * @param name
     * @return
     */
    public static File getEmojiTTFCacheFile(@NonNull Context context, @NonNull String name) {
        File dir = getEmojiTTFCacheFolder(context);
        return new File(dir, name + ".7z");
    }

    /**
     * 存所有PackTheme的目录
     * @param context
     * @return
     */
    public static File getPackThemesFolder(@NonNull Context context) {
        return FileUtils2.getFileDir(context, NAME_PACK_THEME);
    }

    /**
     * 所有PackTheme的下载目录
     * @param context
     * @return
     */
    public static File getPackThemesCacheFolder(@NonNull Context context) {
        return FileUtils2.getCacheDir(context, NAME_PACK_THEME_CACHE);
    }

    /**
     * 解压的某个PackTheme的目录名
     * @param context
     * @param fileName
     * @return
     */
    public static File getPackThemeFolder(@NonNull Context context, @NonNull String fileName) {
        File dir = getPackThemesFolder(context);
        return new File(dir, fileName);
    }

    /**
     * 下载的某个PackTheme文件名
     * @param context
     * @param fileName
     * @return
     */
    public static File getPackThemeCacheFile(@NonNull Context context, @NonNull String fileName) {
        File dir = getPackThemesCacheFolder(context);
        return new File(dir, fileName + ".7z");
    }

    /**
     * Move files in folder to another folder
     *
     * @param sourceFolder      folder path of source files
     * @param destinationFolder destination folder path
     */
    public static void moveFolder(@NonNull File sourceFolder, @NonNull File destinationFolder) {
        if (!isFolderExist(sourceFolder)) {
            return;
        }
        File[] files = sourceFolder.listFiles();

        if (files == null || files.length == 0) {
            return;
        }
        for (File file : files) {
            file.renameTo(new File(destinationFolder, file.getName()));
        }
    }

    /**
     * 将输入流写入文件
     *
     * @param file   文件
     * @param is     输入流
     * @param append 是否追加在文件末
     * @return {@code true}: 写入成功<br>{@code false}: 写入失败
     */
    public static boolean writeFileFromIS(File file, InputStream is, boolean append) {
        if (file == null || is == null) return false;
        if (!createOrExistsFile(file)) return false;
        OutputStream os = null;
        try {
            os = new BufferedOutputStream(new FileOutputStream(file, append));
            byte data[] = new byte[1024];
            int len;
            while ((len = is.read(data)) != -1) {
                os.write(data, 0, len);
            }
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } finally {
            closeIO(is, os);
        }
    }

    /**
     * 判断文件是否存在，不存在则判断是否创建成功
     *
     * @param file 文件
     * @return {@code true}: 存在或创建成功<br>{@code false}: 不存在或创建失败
     */
    public static boolean createOrExistsFile(File file) {
        if (file == null) return false;
        // 如果存在，是文件则返回true，是目录则返回false
        if (file.exists()) return file.isFile();
        if (!createOrExistsDir(file.getParentFile())) return false;
        try {
            return file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 判断目录是否存在，不存在则判断是否创建成功
     *
     * @param file 文件
     * @return {@code true}: 存在或创建成功<br>{@code false}: 不存在或创建失败
     */
    public static boolean createOrExistsDir(File file) {
        // 如果存在，是目录则返回true，是文件则返回false，不存在则返回是否创建成功
        return file != null && (file.exists() ? file.isDirectory() : file.mkdirs());
    }


    /**
     * 关闭IO
     *
     * @param closeables closeable
     */
    public static void closeIO(Closeable... closeables) {
        if (closeables == null) return;
        try {
            for (Closeable closeable : closeables) {
                if (closeable != null) {
                    closeable.close();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 复制或移动文件
     *
     * @param srcFile  源文件
     * @param destFile 目标文件
     * @param isMove   是否移动
     * @return {@code true}: 复制或移动成功<br>{@code false}: 复制或移动失败
     */
    public static boolean copyOrMoveFile(File srcFile, File destFile, boolean isMove) {
        if (!isFileExist(srcFile)) {
            return false;
        }
        if (isFileExist(destFile)) {
            return false;
        }
        try {
            createFolderIfNecessary(destFile.getParentFile());
            return writeFileFromIS(destFile, new FileInputStream(srcFile), false)
                    && !(isMove && !delete(srcFile));
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean fileRenameTo(File srcFile, File destFile, boolean isFallback) {
        if (!isFileExist(srcFile)) {
            return false;
        }
        if (!isFolderExist(destFile.getParentFile()) && !createFolderIfNecessary(destFile.getParentFile())) {
            return false;
        }
        delete(destFile);
        return !(!srcFile.renameTo(destFile) || (isFallback && !isFileExist(destFile))) || copyOrMoveFile(srcFile, destFile, false);
    }


    /**
     * Get content via a InputStream
     *
     * @param inputStream
     * @return
     */
    @Nullable
    public static String getStringFromInputStream(InputStream inputStream) {
        InputStreamReader inputStreamReader = null;
        StringBuilder builder = null;
        try {
            inputStreamReader = new InputStreamReader(inputStream, "utf-8");
            BufferedReader reader = new BufferedReader(inputStreamReader);
            builder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            closeQuietly(inputStream);
        }
        if (builder != null) {
            return builder.toString();
        }
        return null;
    }

//    public static void writeStringToFile(Context context, String fileName, String content) {
//        if (context == null || TextUtils.isEmpty(fileName) || TextUtils.isEmpty(content)) {
//            return;
//        }
//        FileOutputStream fileOutputStream = null;
//        try {
//            fileOutputStream = context.openFileOutput(fileName, Context.MODE_PRIVATE);
//            fileOutputStream.write(content.getBytes());
//        } catch (IOException e) {
//            e.printStackTrace();
//        } finally {
//            try {
//                if (fileOutputStream != null) {
//                    fileOutputStream.close();
//                }
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//    }

    public static File getPrivateFile(@NonNull Context context, String fileName) {
        return new File(context.getDir(OBJECTS_DIR, Context.MODE_PRIVATE), fileName);
    }

    public static String readStringFromFile(@NonNull Context context, String fileName) {
        File file = new File(context.getDir(OBJECTS_DIR, Context.MODE_PRIVATE), fileName);
        return readStringFromFile(file);
    }

    public static String readStringFromFile(File file) {
        if (file == null) {
            throw new IllegalArgumentException("file can NOT be null");
        }
        if (!isFileExist(file)) {
            return null;
        }
        BufferedReader reader = null;
        StringBuilder builder = new StringBuilder();
        try {
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
            String line;
            while ((line = reader.readLine()) != null) {
                if(builder.length() > 0){
                    builder.append("\n");
                }
                builder.append(line);
            }
        } catch (Exception e) {
        } finally {
            closeQuietly(reader);
        }
        return builder.toString();
    }

    public static File writeStringToFile(@NonNull Context context, @NonNull String fileName, @NonNull String object) {
        File file = new File(context.getDir(OBJECTS_DIR, Context.MODE_PRIVATE), fileName);
        OutputStreamWriter writer = null;
        try {
            createFileIfNecessary(file);
            writer = new OutputStreamWriter(new FileOutputStream(file, false));
            writer.write(object);
            writer.flush();
        } catch (Exception e) {
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return file;
    }

    public static void removeObject(Context context, String file) {
        if (file == null)
            return;
        File f = new File(context.getDir(OBJECTS_DIR, Context.MODE_PRIVATE), file);
        delete(f);
    }

    @Deprecated
    public static void saveObject(Context app, String fileName, Object o) {
        if (o == null || fileName == null) {
            return;
        }
        Object obj = getObject(app, fileName, o.getClass());
        if (obj != null && obj.equals(o)) {
            return;
        }
        String dir = app.getDir(OBJECTS_DIR, Context.MODE_PRIVATE)
                .getAbsolutePath();
        File file = new File(dir, fileName);
        if (isFileExist(file)) {
            file.delete();
            file = new File(dir, fileName);
        }
        ObjectOutputStream out = null;
        try {
            out = new ObjectOutputStream(new FileOutputStream(file));
            out.writeObject(o);
            out.flush();
        } catch (Exception e) {
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static Object getObject(Context app, String fileName, Class<?> clazz) {
        String dir = app.getDir(OBJECTS_DIR, Context.MODE_PRIVATE)
                .getAbsolutePath();
        File file = new File(dir, fileName);
        if (!isFileExist(file)) {
            return null;
        }
        ObjectInputStream in = null;
        try {
            in = new ObjectInputStream(new FileInputStream(
                    file));
            Object obj = in.readObject();
            if (obj != null && !clazz.isInstance(obj)) {
                return null;
            }
            return obj;
        } catch (Exception e) {
        } finally {
            closeQuietly(in);
        }
        return null;
    }

    public static boolean copy(File oldFile, File newFile) {
        FileChannel inputChannel = null;
        FileChannel outputChannel = null;
        boolean result = true;
        try {
            createFileIfNecessary(newFile);
//            int byteRead = 0;
            if (isFileExist(oldFile)) {
                inputChannel = new FileInputStream(oldFile).getChannel();
                outputChannel = new FileOutputStream(newFile).getChannel();
                outputChannel.transferFrom(inputChannel, 0, inputChannel.size());
//                byte[] buffer = new byte[1024];
//                while ((byteRead = inStream.read(buffer)) != -1) {
//                    outStream.write(buffer, 0, byteRead);
//                }
            } else {
                result = false;
            }
        } catch (Exception e) {
            result = false;
        } finally {
            closeQuietly(inputChannel);
            closeQuietly(outputChannel);
        }
        return result;
    }

    public static boolean copy(File oldFile, String newFilePath) {
        return copy(oldFile, new File(newFilePath));
    }

    public static boolean delete(File file) {
        return isFileExist(file) && file.delete();
    }

    public static boolean delete(String path) {
        if (TextUtils.isEmpty(path)) {
            return false;
        }
        File file = new File(path);
        return isFileExist(file) && file.delete();
    }


    @Nullable
    public static String getShareFileDir(@NonNull Context context) {
        File[] caches = null;
        try {
            caches = ContextCompat.getExternalCacheDirs(context);
        } catch (Throwable e) {
        }
        if (caches != null && caches.length > 0) {
            File file = new File(caches[0], "share_files");
            createFolderIfNecessary(file);
            return file.getAbsolutePath();
        }
        return null;
    }

//    public static File getApkSticker(@NonNull Context context) {
//        File file = new File(getStickerFileDir(context), "apk");
//        createFolderIfNecessary(file);
//        return file;
//    }

    /**
     * Get Image File folder, if new folder not exist load from old path
     *
     * @param context
     * @return
     */
    public static File getImageFolder(@NonNull Context context) {
        return getFileDir(context, "image-files");
    }

    public static File getImageCacheFolder(@NonNull Context context) {
        return getCacheDir(context, "image-files");
    }

    public static File getThemeCacheFolder(@NonNull Context context) {
        return getCacheDir(context, NAME_THEME);
    }

    public static File getThemeFolder(@NonNull Context context) {
        return getFileDir(context, NAME_THEME);
    }

    public static File getNotifyImageCacheFolder(@NonNull Context context) {
        return getCacheDir(context, "notify-image-files");
    }

    public static void closeQuietly(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException e) {
            }
        }
    }

    public static boolean shouldUseNewExtractMethod(@NonNull Context context) {
        return true;
    }

    public static boolean copyFromAssets(@NonNull Context context, @NonNull String assetPath, @NonNull String tempPath) {
        InputStream inputStream = null;
        FileOutputStream fileOutputStream = null;
        try {
            inputStream = context.getAssets().open(assetPath);
            fileOutputStream = new FileOutputStream(tempPath);
            int length;
            byte[] buffer = new byte[1024];
            while ((length = inputStream.read(buffer)) != -1) {
                fileOutputStream.write(buffer, 0, length);
            }
            fileOutputStream.flush();
            return true;
        } catch (Exception e) {
        } finally {
            closeQuietly(fileOutputStream);
            closeQuietly(inputStream);
        }
        return false;
    }

    @Nullable
    public static <T> T readObject(File file) {
        if (!FileUtils2.isFileExist(file)) {
            return null;
        }
        ObjectInputStream objectInputStream = null;
        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream(file);
            objectInputStream = new ObjectInputStream(inputStream);
            Object object = objectInputStream.readObject();
            return (T) object;
        } catch (Exception e) {
        } finally {
            closeIO(objectInputStream, inputStream);
        }
        return null;
    }

    public static <T> void saveObject(T o, @NonNull File file) {
        if (o == null) {
            return;
        }
        if (!(o instanceof Serializable)) {
            throw new RuntimeException("object must implements Serializable");

        }
        ObjectOutputStream objectOutputStream = null;
        OutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(file);
            objectOutputStream = new ObjectOutputStream(outputStream);
            objectOutputStream.writeObject(o);
            objectOutputStream.flush();
        } catch (Exception e) {
        } finally {
            closeIO(objectOutputStream, outputStream);
        }
    }

    public static void clearOldVersion(@NonNull File parent, @NonNull String fileHeader, @NonNull String currentVersion) {
        File[] files = parent.listFiles();
        if (files == null) {
            return;
        }
        for (int i = 0; i < files.length; i++) {
            String name = files[i].getName();
            if (name.startsWith(fileHeader)
                    && !name.equals(fileHeader + currentVersion)) {
                FileUtils2.delete(files[i]);
            }
        }
    }

    public static String getUpdateApkFilePath(@NonNull Context context,
                                              @NonNull String fileName) {
        File dir = getUpdateApkCacheDir(context);
        return new File(dir, MD5.getMD5(fileName) + ".apk").getAbsolutePath();
    }

    public static File getUpdateApkCacheDir(@NonNull Context context) {
        return new File(getCacheDir(context), "update_apk");
    }

    public static File getTinkerPatchFilePath(@NonNull Context context) {
        return new File(getFileDir(context), "tinker_patch");
    }

    public static String getStringFromAssetFile(final Context context, String fileName) {
        BufferedReader bufferedReader = null;
        String s = "";
        try {
            bufferedReader = new BufferedReader(new InputStreamReader(context.getAssets().open(fileName)));
            StringBuffer buffer = new StringBuffer();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                buffer.append(line);
            }
            s = buffer.toString();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return s;
    }

    public static File getNewPopupFileDir(@NonNull Context context){
        return getFileDir(context, "popupnew");
    }
}

