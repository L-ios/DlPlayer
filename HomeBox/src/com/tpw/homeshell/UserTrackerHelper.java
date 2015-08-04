package com.tpw.homeshell;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

//import com.tpw.ams.ta.StatConfig;
//import com.tpw.ams.ta.TA;
//import com.tpw.ams.ta.Tracker;
//import com.tpw.ams.ta.utils.MapUtils;
//import com.tpw.ams.ta.utils.StringUtils;

public class UserTrackerHelper {

    private static final boolean DEBUG = true;
    private static final String TAG = "UserTrackerHelper";

    private static final String LABEL_SCREEN_STATUS = "SCREEN_STATUS";
    private static final String LABEL_FOLDER_STATUS = "FOLDER_STATUS";
    private static final String LABEL_ICON_STATUS = "ICON_STATUS";
    //private static Tracker mTracker;
    private static Map<Class<? extends Activity>, String> mPageNameCache = new HashMap<Class<? extends Activity>, String>();

    public static void bindPageName(Activity activity, String pageName) {
        if (mPageNameCache == null) {
            mPageNameCache = new HashMap<Class<? extends Activity>, String>();
        }
        if (activity != null) {
            mPageNameCache.put(activity.getClass(), pageName);
        }
        /*if (mTracker != null) {
            mTracker.bindPageName(mPageNameCache);
        }*/
    }

    public static void bindPageName(Class<? extends Activity> activityClass, String pageName) {
        if (mPageNameCache == null) {
            mPageNameCache = new HashMap<Class<? extends Activity>, String>();
        }
        if (activityClass != null) {
            mPageNameCache.put(activityClass, pageName);
        }
        /*if (mTracker != null) {
            mTracker.bindPageName(mPageNameCache);
        }*/
    }

    public static void sendUserReport(Class<? extends Activity> activityClass, String controlName) {
        if (DEBUG) {
            Log.d(TAG, "sendUserEvent, controlName=" + controlName);
        }
        /*if (mTracker != null) {
            mTracker.ctrlClicked(activityClass, controlName);
        }*/
    }

    public static void sendUserReport(Class<? extends Activity> activityClass, String controlName,
            Map<String, String> pParams) {
        if (DEBUG) {
            Log.d(TAG, "sendUserEvent, controlName=" + controlName);
        }
        /*if (mTracker != null) {
            mTracker.ctrlClicked(activityClass, controlName, pParams);
        }*/
    }

    public static void commitEvent(String eventName, Map<String, String> pParams) {
        if (DEBUG) {
            Log.d(TAG, "commitEvent, eventName =" + eventName);
        }
        /*if (mTracker != null) {
            mTracker.commitEvent(eventName, pParams);
        }*/
    }

    public static void sendUserReport(Activity activity, String controlName) {
        if (DEBUG) {
            Log.d(TAG, "sendUserEvent, controlName=" + controlName);
        }
        /*if (mTracker != null) {
            mTracker.ctrlClicked(activity.getClass(), controlName);
        }*/
    }

    public static void sendUserReport(Activity activity, String controlName,
            Map<String, String> pParams) {
        if (DEBUG) {
            Log.d(TAG, "sendUserEvent, controlName=" + controlName);
        }
        /*if (mTracker != null) {
            mTracker.ctrlClicked(activity.getClass(), controlName, pParams);
        }*/
    }

    public static void sendUserReport(String controlName) {
        if (DEBUG) {
            Log.d(TAG, "sendUserEvent, controlName=" + controlName);
        }
        /*if (mTracker != null) {
            mTracker.ctrlClicked(Launcher.class, controlName);
        }*/
    }

    public static void sendUserReport(String controlName, String param) {
        if (DEBUG) {
            Log.d(TAG, "sendUserEvent, controlName=" + controlName + ",param=" + param);
        }

        /*Map<String, String> map = null;
        if (param != null) {
            map = StringUtils.convertStringAToMap(param);
        }
        if (mTracker != null) {
            mTracker.ctrlClicked(Launcher.class, controlName, map);
        }*/

    }

    public static void sendUserReport(String controlName, Map<String, String> param) {
        if (DEBUG) {
            Log.d(TAG, "sendUserEvent, controlName=" + controlName + ",param=" + param);
        }

        /*if (mTracker != null) {
            mTracker.ctrlClicked(Launcher.class, controlName, param);
        }*/

    }

    public static void init(Context context) {
        if (DEBUG) {
            Log.d(TAG, "init");
        }

        /*StatConfig.getInstance().setContext(context);
        StatConfig.getInstance().turnOnDebug();
        mTracker = TA.getInstance().getTracker("21710403");
        TA.getInstance().setDefaultTracker(mTracker);*/

    }

    public static void deinit() {
        if (DEBUG) {
            Log.d(TAG, "deinit");
        }
    }

    public static void pageEnter(Activity pActivity) {
        if (DEBUG) {
            Log.d(TAG, "pageEnter, name=" + pActivity);
        }
        /*if (mTracker != null) {
            mTracker.activityStart(pActivity);
        }*/
    }

    public static void pageLeave(Activity pActivity) {
        if (DEBUG) {
            Log.d(TAG, "pageLeave, name=" + pActivity);
        }
       /* if (mTracker != null) {
            mTracker.activityStop(pActivity);
        }*/
    }

    public static void pageEnter(String pPageName) {
        if (DEBUG) {
            Log.d(TAG, "pageEnter, pPageName=" + pPageName);
        }
        /*if (mTracker != null) {
            mTracker.pageEnter(pPageName);
        }*/
    }

    public static void pageLeave(String pPageName) {
        if (DEBUG) {
            Log.d(TAG, "pageLeave, pPageName=" + pPageName);
        }
        /*if (mTracker != null) {
            mTracker.pageLeave(pPageName);
        }*/
    }

    public static void folderStatus(List<FolderInfo> list) {
        if (DEBUG) {
            Log.d(TAG, "folderStatus:");
        }
        /*Properties lProperties = new Properties();
        lProperties.setProperty("count", (list == null ? 0 : list.size()) + "");
        Map<String, String> map = MapUtils.convertPropertiesToMap(lProperties);
        if (mTracker != null) {
            mTracker.commitEvent(LABEL_FOLDER_STATUS, map);
        }*/
    }

    public static void iconStatus(List<ItemInfo> list) {
        if (DEBUG) {
            Log.d(TAG, "iconStatus:");
        }
        /*Properties lProperties = new Properties();
        lProperties.setProperty("count", (list == null ? 0 : list.size()) + "");
        Map<String, String> map = MapUtils.convertPropertiesToMap(lProperties);
        if (mTracker != null) {
            mTracker.commitEvent(LABEL_ICON_STATUS, map);
        }*/
    }

    public static void screenStatus(int screens) {
        if (DEBUG) {
            Log.d(TAG, "screenStatus:" + screens);
        }
        /*Properties lProperties = new Properties();
        lProperties.setProperty("count", screens + "");
        Map<String, String> map = MapUtils.convertPropertiesToMap(lProperties);
        if (mTracker != null) {
            mTracker.commitEvent(LABEL_SCREEN_STATUS, map);
        }*/
    }

    public static void wigdetStatus(int count) {
        if (DEBUG) {
            Log.d(TAG, "wigdetStatus:");
        }
        /*Properties lProperties = new Properties();
        lProperties.setProperty("status", count + "");
        Map<String, String> map = MapUtils.convertPropertiesToMap(lProperties);
        if (mTracker != null) {
            mTracker.commitEvent(UserTrackerMessage.MSG_WIDGET_STATUS, map);
        }*/
    }

    public static void shortcutStatus(int count) {
        if (DEBUG) {
            Log.d(TAG, "shortcutStatus:");
        }
        /*Properties lProperties = new Properties();
        lProperties.setProperty("status", count + "");
        Map<String, String> map = MapUtils.convertPropertiesToMap(lProperties);
        if (mTracker != null) {
            mTracker.commitEvent(UserTrackerMessage.MSG_SHORTCUT_STATUS, map);
        }*/
    }

    public static void sendLauncherEffectsResult(int position) {
        if (DEBUG) {
            Log.d(TAG, "Effects position:" + position);
        }
        /*Properties lProperties = new Properties();
        lProperties.setProperty("position", String.valueOf(position));
        Map<String, String> map = MapUtils.convertPropertiesToMap(lProperties);
        if (mTracker != null) {
            mTracker.commitEvent(UserTrackerMessage.MSG_LAUNCHER_SETTING_EFFECTS_RESULT, map);
        }*/
    }

    public static void sendLauncherLayoutResult(int position) {
        if (DEBUG) {
            Log.d(TAG, "Layout position:" + position);
        }
        /*Properties lProperties = new Properties();
        lProperties.setProperty("position", String.valueOf(position));
        Map<String, String> map = MapUtils.convertPropertiesToMap(lProperties);
        if (mTracker != null) {
            mTracker.commitEvent(UserTrackerMessage.MSG_LAUNCHER_SETTING_LAYOUT_RESULT, map);
        }*/
    }

    public static void entryPageBegin(String pageLabel) {
        if (DEBUG) {
            Log.d(TAG, "entryLauncherBegin:" + pageLabel);
        }
        /*if (mTracker != null) {
            mTracker.commitEventBegin(pageLabel, null);
        }*/
    }

    public static void entryPageEnd(String pageLabel) {
        if (DEBUG) {
            Log.d(TAG, "entryLauncherEnd:" + pageLabel);
        }
        /*if (mTracker != null) {
            mTracker.commitEventEnd(pageLabel, null);
        }*/
    }
}
