package com.qisiemoji.apksticker.util;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.WorkerThread;
import android.text.TextUtils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PackageUtil {

    public final static String KIKA_PACKAGENAME = "com.qisiemoji.inputmethod";

    public static void uninstallAPK(Context mContext, String packageName) {
        Uri uri = Uri.parse("package:" + packageName);
        Intent intent = new Intent(Intent.ACTION_DELETE, uri);
        mContext.startActivity(intent);
    }

    public static String getApkName(Context context, String pkgName) {
        if (context == null || TextUtils.isEmpty(pkgName)) {
            return null;
        }
        String name = null;
        PackageManager pm = context.getPackageManager();
        try {
            ApplicationInfo applicationInfo = pm.getApplicationInfo(pkgName, 0);
            name = (String) pm.getApplicationLabel(applicationInfo);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return name;
    }

    public static int getVersionCode(Context context) {
        int verCode = -1;
        String pckName = context.getPackageName();
        try {
            verCode = context.getPackageManager().getPackageInfo(pckName, 0).versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return verCode;
    }

    public static int getVersionCode(@NonNull Context context, @NonNull String packageName) {
        if (TextUtils.isEmpty(packageName)) {
            return -1;
        }
        PackageManager pm = context.getPackageManager();
        try {
            PackageInfo info = pm.getPackageInfo(packageName, PackageManager.GET_CONFIGURATIONS);
            return info.versionCode;
        } catch (Exception e) {
        }
        return -1;
    }

    public static boolean startActivityFromAnotherApp(Context context, String pkg, String cls) {
        try {
            if (TextUtils.isEmpty(cls)) {
                Intent intent = context.getPackageManager().getLaunchIntentForPackage(pkg);
                if (intent != null) {
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent);
                    return true;
                }
            } else {
                Intent intent = new Intent();
                intent.setComponent(new ComponentName(pkg, cls));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean hasDownloaded(Context context, String pkgName) {
        if (TextUtils.isEmpty(pkgName)) {
            return false;
        }
        PackageInfo packageInfo;
        try {
            packageInfo = context.getPackageManager().getPackageInfo(
                    pkgName, 0);
        } catch (Exception e) {
            packageInfo = null;
//            e.printStackTrace();
        }
        return packageInfo != null;
    }

    public static boolean isPackageInstalled(Context context, String packageName) {
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(packageName, 0);
            if (packageInfo != null) {
                return true;
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Get installed packages in this device.
     * First use PackageManager.getInstalledPackages(PackageManager.GET_ACTIVITIES) to query,
     * If error occurred use Runtime.getRuntime().exec("pm list packages") instead
     *
     * @param context context
     * @return {@link PackageInfo} list
     */
    @WorkerThread
    public static List<PackageInfo> queryInstalledPackages(Context context) {
        final PackageManager pm = context.getPackageManager();
        try {
            return pm.getInstalledPackages(0);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Process process;
        List<PackageInfo> result = new ArrayList<>();
        BufferedReader bufferedReader = null;

        try {
            process = Runtime.getRuntime().exec("pm list packages");
            bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                final String packageName = line.substring(line.indexOf(':') + 1);
                final PackageInfo packageInfo = pm.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
                result.add(packageInfo);
            }
            process.waitFor();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return result;
    }

    private final static String THEME_PACKAGE_NAME_PREFIX = "com.ikeyboard.theme";

    /**
     * 获取已经安装的theme apk
     * @param context
     * @return
     */
    public static Set<String> getInstalledThemePkgs(Context context){
        Set<String> hashSet = new HashSet<>();
        List<PackageInfo> packageInfos = PackageUtil.queryInstalledPackages(context);
        for (PackageInfo packageInfo : packageInfos) {
            if (packageInfo.packageName.startsWith(THEME_PACKAGE_NAME_PREFIX)) {
                hashSet.add(packageInfo.packageName);
            }
        }
        return hashSet;
    }
}
