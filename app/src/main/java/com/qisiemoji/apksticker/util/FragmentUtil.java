package com.qisiemoji.apksticker.util;

import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;


public class FragmentUtil {

    public static void showDialogFragment(FragmentManager fm, DialogFragment newFragment, String strFragmentTag) {
        showDialogFragment(fm, newFragment, strFragmentTag, false);
    }

    public static void showDialogFragment(FragmentManager fm, DialogFragment newFragment, String strFragmentTag, boolean bReplace) {
        try {
            Fragment prev = fm.findFragmentByTag(strFragmentTag);
            if (prev != null) {
                if (!bReplace) {
                    return;
                } else { // remove original one
                    FragmentTransaction ft = fm.beginTransaction();
                    ft.remove(prev);
                    ft.commitAllowingStateLoss();
                }
            }
            newFragment.show(fm, strFragmentTag);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
