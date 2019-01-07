package com.qisiemoji.apksticker.util;

import android.content.Context;
import android.provider.Settings;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodManager;

import java.util.List;

public class IMEUtils {
    private static final long IME_ENABLED_CHECK_TIME_LIMIT = 1000L;
    private static long sIsThisImeEnabledCheckTime = 0;
    private static boolean sIsThisImeEnabled = false;

    public static InputMethodInfo getInputMethodInfo(final Context context) {
        InputMethodManager imm = (InputMethodManager)context.getSystemService(Context.INPUT_METHOD_SERVICE);
        return getInputMethodInfo(context, imm);
    }

    public static InputMethodInfo getInputMethodInfo(final Context context, final InputMethodManager imm) {
        String packageName = PackageUtil.KIKA_PACKAGENAME;
        if (packageName == null || imm == null)
            return null;

        List<InputMethodInfo> imis = null;
        try {
            imis = imm.getInputMethodList();
        } catch (Exception e) {
        }
        if (imis != null) {
            for (final InputMethodInfo imi : imis) {
                if (imi == null) {
                    continue;
                }
                if (packageName.equals(imi.getPackageName())) {
                    return imi;
                }
            }
        }
        return null;
    }
    public static boolean isThisImeEnabledSlowWay(final Context context,
                                                  final InputMethodManager imm) {
        if (System.currentTimeMillis() - sIsThisImeEnabledCheckTime < IME_ENABLED_CHECK_TIME_LIMIT && System.currentTimeMillis() >= sIsThisImeEnabledCheckTime) {
            return sIsThisImeEnabled;
        }
        boolean ret = false;

        if (context == null || imm == null)
            return false;
        final String packageName = context.getPackageName();
        if (packageName == null) {
            return false;
        }
        List<InputMethodInfo> immlist = imm.getEnabledInputMethodList();
        for (final InputMethodInfo imi : immlist) {
            if (imi != null) {
                if (packageName.equals(imi.getPackageName())) {
                    ret = true;
                    break;
                }
            }
        }
        sIsThisImeEnabled = ret;
        sIsThisImeEnabledCheckTime = System.currentTimeMillis();
        return ret;
    }
    /*
     * We may not be able to get our own {@link InputMethodInfo} just after this IME is installed
     * because {@link InputMethodManagerService} may not be aware of this IME yet.
     * Note: {@link RichInputMethodManager} has similar methods. Here in setup wizard, we can't
     * use it for the reason above.
     */

    /**
     * Check if the IME specified by the context is enabled.
     * CAVEAT: This may cause a round trip IPC.
     *
     * @param context package context of the IME to be checked.
     * @param imm     the {@link InputMethodManager}.
     * @return true if this IME is enabled.
     */
    /* package */
    public static boolean isThisImeEnabled(final Context context,
                                           final InputMethodManager imm) {
        if (System.currentTimeMillis() - sIsThisImeEnabledCheckTime < IME_ENABLED_CHECK_TIME_LIMIT && System.currentTimeMillis() >= sIsThisImeEnabledCheckTime) {
            return sIsThisImeEnabled;
        }
        boolean ret = false;

        if (context == null || imm == null)
            return false;

        final InputMethodInfo imi = getInputMethodInfo(context);
        if (imi == null) {
            return false;
        }
        final String enabledInputMethodListStr = Settings.Secure.getString(
                context.getContentResolver(), Settings.Secure.ENABLED_INPUT_METHODS);
        final String enabledInputMethodStrs[] = enabledInputMethodListStr.split(":");
        for (final String enabledInputMethodStr : enabledInputMethodStrs) {
            if (enabledInputMethodStr.startsWith(imi.getId())) {
                ret = true;
                break;
            }
        }
        sIsThisImeEnabled = ret;
        sIsThisImeEnabledCheckTime = System.currentTimeMillis();
        return ret;
    }

    /**
     * Check if the IME specified by the context is the current IME.
     * CAVEAT: This may cause a round trip IPC.
     *
     * @param context package context of the IME to be checked.
     * @param imm     the {@link InputMethodManager}.
     * @return true if this IME is the current IME.
     */
    /* package */
    public static boolean isThisImeCurrent(final Context context,
                                           final InputMethodManager imm) {
        if (context == null || imm == null)
            return false;
        final InputMethodInfo imi = getInputMethodInfo(context);
        final String currentImeId = Settings.Secure.getString(
                context.getContentResolver(), Settings.Secure.DEFAULT_INPUT_METHOD);
        return imi != null && imi.getId().equals(currentImeId);
    }

    /**
     * Get the package name of current IME.
     *
     * @param context package context of the IME to be checked.
     * @param imm     the {@link InputMethodManager}.
     * @return a package name of current IME
     */
    /* package */
    public static String getCurrentImeId(final Context context,
                                         final InputMethodManager imm) {
        if (context == null || imm == null)
            return null;

        return Settings.Secure.getString(context.getContentResolver(), Settings.Secure.DEFAULT_INPUT_METHOD);
    }

}

