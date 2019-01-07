package com.qisiemoji.apksticker.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.WorkerThread;

import java.util.HashMap;
import java.util.Map;

public class SharedPreferencesUtils {
    public static final String PREF_DEVICE_ID = "pref_device_id";

    public static final String NEW_SHARED_PREFERENCES = "NEW_SHARED_PREFERENCES";

    private static final Object sLock = new Object();

    private final static String[] UNUSED_ITEMS = {
            "pref_splash_last_check"
            , "gcm_registration_id"
            , "PROPERTY_GCM_NEED_UPDATE"
            , "PROPERTY_GCM_UPDATE_TIMESTAMP"
            , "appVersion"
            , "sp_show_menubar_guide_has_showed"
            , "pref_is_use_kika_engine"
            , "pref_gif_emoji_using_gif"
            , "pref_is_gif_emoji_user"
            , "pref_show_language_switch_key"
            , "pref_include_other_imes_in_language_switch_list"
            , "dynamic_key_area_user"
            , "pref_is_use_kika_engine_170103"
            , "need_update_search_rule"
            , "pref_check_night_mode_time"
            , "edit_sticker"
            , "checked_EmojiBlackandWhite"
            , "sp_active_time_appsflyer"
            , "rule_fetch_time"
            , "rule_expire_time"
            , "is_show_choose_keyboard"
            , "kb_search_history"
            , "update_theme_index_state"
            , "pref_is_use_kika_engine_170417"
            , "theme_anim_show_count"
            , "theme_icon_clicked"
            , "theme_anim_first_show_time"
            , "is_cache_theme"
            , "theme_cache_time"
            , "last_group_show_time"
    };

    private static Map<String, Object> sSharedPreferenceMap = new HashMap<>();

    public static void setBoolean(Context context, String str, boolean b) {
        synchronized (sLock) {
            if (sSharedPreferenceMap.containsKey(str)) {
                Object object = sSharedPreferenceMap.get(str);
                if (object instanceof Boolean) {
                    if ((boolean) object == b) {
                        return;
                    }
                }
            }
        }
        if (context == null) {
            return;
        }
        SharedPreferences sharedPreferences = context.getSharedPreferences(NEW_SHARED_PREFERENCES, Context.MODE_PRIVATE);
        if (sharedPreferences != null) {
            synchronized (sLock) {
                sSharedPreferenceMap.put(str, b);
            }
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean(str, b);
            editor.apply();
        }
    }

    public static boolean getBoolean(Context context, String str, boolean def) {
        synchronized (sLock) {
            if (sSharedPreferenceMap.containsKey(str)) {
                Object object = sSharedPreferenceMap.get(str);
                if (object instanceof Boolean) {
                    return (boolean) object;
                }
            }
        }
        if (context == null) {
            return def;
        }
        SharedPreferences sharedPreferences = context.getSharedPreferences(NEW_SHARED_PREFERENCES, Context.MODE_PRIVATE);
        if (sharedPreferences != null && sharedPreferences.contains(str)) {
            return sharedPreferences.getBoolean(str, def);
        }
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        if (sharedPreferences != null) {
            return sharedPreferences.getBoolean(str, def);
        }
        return def;
    }

    public static boolean getBoolean(Context context, String str) {
        synchronized (sLock) {
            if (sSharedPreferenceMap.containsKey(str)) {
                Object object = sSharedPreferenceMap.get(str);
                if (object instanceof Boolean) {
                    return (boolean) object;
                }
            }
        }
        if (context == null) {
            return false;
        }
        SharedPreferences sharedPreferences = context.getSharedPreferences(NEW_SHARED_PREFERENCES, Context.MODE_PRIVATE);
        if (sharedPreferences != null && sharedPreferences.contains(str)) {
            return sharedPreferences.getBoolean(str, false);
        }
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        if (sharedPreferences != null) {
            return sharedPreferences.getBoolean(str, false);
        }
        return false;
    }

    public static void setInt(Context context, String str, int i) {
        synchronized (sLock) {
            if (sSharedPreferenceMap.containsKey(str)) {
                Object object = sSharedPreferenceMap.get(str);
                if (object instanceof Integer) {
                    if ((int) object == i) {
                        return;
                    }
                }
            }
        }
        if (context == null) {
            return;
        }
        SharedPreferences sharedPreferences = context.getSharedPreferences(NEW_SHARED_PREFERENCES, Context.MODE_PRIVATE);
        if (sharedPreferences != null) {
            synchronized (sLock) {
                sSharedPreferenceMap.put(str, i);
            }
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt(str, i);
            editor.apply();
        }
    }

    public static int getInt(Context context, String str, int def) {
        synchronized (sLock) {
            if (sSharedPreferenceMap.containsKey(str)) {
                Object object = sSharedPreferenceMap.get(str);
                if (object instanceof Integer) {
                    return (int) object;
                }
            }
        }
        if (context == null) {
            return def;
        }
        SharedPreferences sharedPreferences = context.getSharedPreferences(NEW_SHARED_PREFERENCES, Context.MODE_PRIVATE);
        if (sharedPreferences != null && sharedPreferences.contains(str)) {
            return sharedPreferences.getInt(str, def);
        }
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        if (sharedPreferences != null) {
            return sharedPreferences.getInt(str, def);
        }
        return def;
    }

    public static int getInt(Context context, String str) {
        synchronized (sLock) {
            if (sSharedPreferenceMap.containsKey(str)) {
                Object object = sSharedPreferenceMap.get(str);
                if (object instanceof Integer) {
                    return (int) object;
                }
            }
        }
        if (context == null) {
            return -1;
        }
        SharedPreferences sharedPreferences = context.getSharedPreferences(NEW_SHARED_PREFERENCES, Context.MODE_PRIVATE);
        if (sharedPreferences != null && sharedPreferences.contains(str)) {
            return sharedPreferences.getInt(str, 0);
        }
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        if (sharedPreferences != null) {
            return sharedPreferences.getInt(str, 0);
        }
        return -1;
    }


    public static void setLong(Context context, String str, long l) {
        synchronized (sLock) {
            if (sSharedPreferenceMap.containsKey(str)) {
                Object object = sSharedPreferenceMap.get(str);
                if (object instanceof Long) {
                    if ((long) object == l) {
                        return;
                    }
                }
            }
        }
        if (context == null) {
            return;
        }
        SharedPreferences sharedPreferences = context.getSharedPreferences(NEW_SHARED_PREFERENCES, Context.MODE_PRIVATE);
        if (sharedPreferences != null) {
            synchronized (sLock) {
                sSharedPreferenceMap.put(str, l);
            }
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putLong(str, l);
            editor.apply();
        }
    }

    public static long getLong(Context context, String str, long def) {
        synchronized (sLock) {
            if (sSharedPreferenceMap.containsKey(str)) {
                Object object = sSharedPreferenceMap.get(str);
                if (object instanceof Long) {
                    return (long) object;
                }
            }
        }
        if (context == null) {
            return def;
        }
        SharedPreferences sharedPreferences = context.getSharedPreferences(NEW_SHARED_PREFERENCES, Context.MODE_PRIVATE);
        if (sharedPreferences != null && sharedPreferences.contains(str)) {
            return sharedPreferences.getLong(str, def);
        }
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        if (sharedPreferences != null) {
            return sharedPreferences.getLong(str, def);
        }
        return def;
    }

    public static long getLong(Context context, String str) {
        synchronized (sLock) {
            if (sSharedPreferenceMap.containsKey(str)) {
                Object object = sSharedPreferenceMap.get(str);
                if (object instanceof Long) {
                    return (long) object;
                }
            }
        }
        if (context == null) {
            return -1;
        }
        SharedPreferences sharedPreferences = context.getSharedPreferences(NEW_SHARED_PREFERENCES, Context.MODE_PRIVATE);
        if (sharedPreferences != null && sharedPreferences.contains(str)) {
            return sharedPreferences.getLong(str, 0);
        }
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        if (sharedPreferences != null) {
            return sharedPreferences.getLong(str, 0);
        }
        return -1;
    }

    public static void setFloat(Context context, String str, float f) {
        synchronized (sLock) {
            if (sSharedPreferenceMap.containsKey(str)) {
                Object object = sSharedPreferenceMap.get(str);
                if (object instanceof Float) {
                    if (Float.compare((float) object, f) == 0) {
                        return;
                    }
                }
            }
        }
        if (context == null) {
            return;
        }
        SharedPreferences sharedPreferences = context.getSharedPreferences(NEW_SHARED_PREFERENCES, Context.MODE_PRIVATE);
        if (sharedPreferences != null) {
            synchronized (sLock) {
                sSharedPreferenceMap.put(str, f);
            }
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putFloat(str, f);
            editor.apply();
        }
    }

    public static float getFloat(Context context, String str, float def) {
        synchronized (sLock) {
            if (sSharedPreferenceMap.containsKey(str)) {
                Object object = sSharedPreferenceMap.get(str);
                if (object instanceof Float) {
                    return (float) object;
                }
            }
        }
        if (context == null) {
            return def;
        }
        SharedPreferences sharedPreferences = context.getSharedPreferences(NEW_SHARED_PREFERENCES, Context.MODE_PRIVATE);
        if (sharedPreferences != null && sharedPreferences.contains(str)) {
            return sharedPreferences.getFloat(str, def);
        }
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        if (sharedPreferences != null) {
            return sharedPreferences.getFloat(str, def);
        }
        return def;
    }

    public static float getFloat(Context context, String str) {
        synchronized (sLock) {
            if (sSharedPreferenceMap.containsKey(str)) {
                Object object = sSharedPreferenceMap.get(str);
                if (object instanceof Float) {
                    return (float) object;
                }
            }
        }
        if (context == null) {
            return -1f;
        }
        SharedPreferences sharedPreferences = context.getSharedPreferences(NEW_SHARED_PREFERENCES, Context.MODE_PRIVATE);
        if (sharedPreferences != null && sharedPreferences.contains(str)) {
            return sharedPreferences.getFloat(str, 0f);
        }
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        if (sharedPreferences != null) {
            return sharedPreferences.getFloat(str, 0f);
        }
        return -1f;
    }

    public static void setString(Context context, String str, String s) {
        synchronized (sLock) {
            if (sSharedPreferenceMap.containsKey(str)) {
                Object object = sSharedPreferenceMap.get(str);
                if ((object instanceof String && ((String) object).equals(s))
                        || (object == null && s == null)) {
                    return;
                }
            }
        }
        if (context == null) {
            return;
        }
        SharedPreferences sharedPreferences = context.getSharedPreferences(NEW_SHARED_PREFERENCES, Context.MODE_PRIVATE);
        if (sharedPreferences != null) {
            synchronized (sLock) {
                sSharedPreferenceMap.put(str, s);
            }
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(str, s);
            editor.apply();
        }
    }

    public static String getString(Context context, String str, String def) {
        synchronized (sLock) {
            if (sSharedPreferenceMap.containsKey(str)) {
                Object object = sSharedPreferenceMap.get(str);
                if (str == null) {
                    return null;
                } else if (object instanceof String) {
                    return (String) object;
                }
            }
        }
        if (context == null) {
            return def;
        }
        SharedPreferences sharedPreferences = context.getSharedPreferences(NEW_SHARED_PREFERENCES, Context.MODE_PRIVATE);
        if (sharedPreferences != null && sharedPreferences.contains(str)) {
            return sharedPreferences.getString(str, def);
        }
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        if (sharedPreferences != null) {
            return sharedPreferences.getString(str, def);
        }
        return def;
    }

    public static String getString(Context context, String str) {
        synchronized (sLock) {
            if (sSharedPreferenceMap.containsKey(str)) {
                Object object = sSharedPreferenceMap.get(str);
                if (str == null) {
                    return null;
                } else if (object instanceof String) {
                    return (String) object;
                }
            }
        }
        if (context == null) {
            return null;
        }
        SharedPreferences sharedPreferences = context.getSharedPreferences(NEW_SHARED_PREFERENCES, Context.MODE_PRIVATE);
        if (sharedPreferences != null && sharedPreferences.contains(str)) {
            return sharedPreferences.getString(str, null);
        }
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        if (sharedPreferences != null) {
            return sharedPreferences.getString(str, null);
        }
        return null;
    }

    public static boolean contains(Context context, String str) {
        synchronized (sLock) {
            if (sSharedPreferenceMap.containsKey(str)) {
                return true;
            }
        }
        if (context == null) {
            return false;
        }
        boolean retVal = false;
        SharedPreferences sharedPreferences = context.getSharedPreferences(NEW_SHARED_PREFERENCES, Context.MODE_PRIVATE);
        if (sharedPreferences != null) {
            retVal = sharedPreferences.contains(str);
        }
        if (retVal) {
            return true;
        }
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        if (sharedPreferences != null) {
            return sharedPreferences.contains(str);
        }
        return false;
    }

    public static void remove(Context context, String str) {
        synchronized (sLock) {
            sSharedPreferenceMap.remove(str);
        }
        if (context == null) {
            return;
        }
        SharedPreferences sharedPreferences = context.getSharedPreferences(NEW_SHARED_PREFERENCES, Context.MODE_PRIVATE);
        if (sharedPreferences != null && sharedPreferences.contains(str)) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.remove(str);
            editor.apply();
            return;
        }
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        if (sharedPreferences != null) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.remove(str);
            editor.apply();
        }
    }

    @WorkerThread
    public static void removeUnusedItems(Context context) {
        if (context == null) {
            return;
        }
        if (UNUSED_ITEMS.length > 0) {
            SharedPreferences.Editor editor = null;
            SharedPreferences.Editor newEditor = null;
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
            SharedPreferences newSharedPreferences = context.getSharedPreferences(NEW_SHARED_PREFERENCES, Context.MODE_PRIVATE);
            if (sharedPreferences != null) {
                editor = sharedPreferences.edit();
            }
            if (newSharedPreferences != null) {
                newEditor = newSharedPreferences.edit();
            }
            boolean needApply = false;
            boolean newNeedApply = false;
            for (String item : UNUSED_ITEMS) {
                if (editor != null) {
                    needApply = true;
                    editor.remove(item);
                }
                if (newEditor != null) {
                    newNeedApply = true;
                    newEditor.remove(item);
                }
            }
            if (needApply) {
                editor.apply();
            }
            if (newNeedApply) {
                newEditor.apply();
            }
        }
    }
}
