
package com.tpw.homeshell;

import android.R.menu;
import android.app.Notification;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;

import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import com.tpw.homeshell.model.LauncherModel;
import com.tpw.homeshell.setting.HomeShellSetting;
import com.tpw.homeshell.utils.NotificationPriorityHelper;

public class CardNotificationListenerService extends NotificationListenerService {
    private static CardNotificationListenerService sInstance;
    private static final HashMap<String, String> sPackageAliasMap = new HashMap<String, String>();
    private static final HashSet<String> sPackageWhitelistSet = new HashSet<String>();
    private static final HashSet<String> sPackageBlacklistSet = new HashSet<String>();

    private SparseArray<StatusBarNotification> mNotificationMap;
    private HashMap<String, ArrayList<StatusBarNotification>> mNotificationPkgMap = new HashMap<String, ArrayList<StatusBarNotification>>();
    private ArrayList<String> mPkgList = new ArrayList<String>();
    private SoftReference<StatusBarNotificationListener> mListener;
    private static final boolean SUPPORT_CUSTOM = true;
    public static final boolean SUPPORT_IMMORTAL = false;
    public static final String TAG = "CardNotificationListenerService";

    static {
        addAlias("com.tpw.mobile.permission", "com.tpw.SecurityCenter");
        addAlias("com.yunos.privacy", "com.tpw.SecurityCenter");
        addWhitelist("com.tencent.mobileqq");
        addWhitelist("com.tencent.hd.qq");
        addWhitelist("com.tencent.qqlite");
        addWhitelist("com.tencent.android.pad");
        addBlacklist("com.android.mms");
        addBlacklist("com.tpw.wireless.vos.appstore");
    }

    private static void addAlias(String key, String value) {
        sPackageAliasMap.put(key, value);
    }

    private static void addWhitelist(String pkg) {
        sPackageWhitelistSet.add(pkg);
    }
    private static void addBlacklist(String pkg) {
        sPackageBlacklistSet.add(pkg);
    }
    private static boolean sInit = false;
    public void onListenerConnected() {
        if(sInit){
            StatusBarNotificationListener listener = (StatusBarNotificationListener)GadgetCardHelper.getInstance(getApplicationContext());
            setListener(listener);
            if (mListener != null && mListener.get() != null){
                mListener.get().onListenerConnected();
            }
        }
        sInit = true;
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        ensureInit();
        if (isNotificationIgnore(sbn)) {
            return;
        }
        String pkg = sbn.getPackageName();
        String alias = sPackageAliasMap.get(pkg);
        if (alias != null)
            pkg = alias;
        int key = Arrays.hashCode(new Object[] {
                pkg, sbn.getId(), sbn.getTag()
        });
        StatusBarNotification lastSbn = mNotificationMap.get(key);
        ArrayList<StatusBarNotification> list = mNotificationPkgMap.get(pkg);
        if (list == null) {
            list = new ArrayList<StatusBarNotification>();
            mNotificationPkgMap.put(pkg, list);
        }
        synchronized (list) {
            if (lastSbn != null) {
                list.remove(lastSbn);
            }
            list.add(sbn);
        }
        mNotificationMap.put(key, sbn);
        if (mListener != null && mListener.get() != null)
            mListener.get().onNotificationPosted(sbn);
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        ensureInit();
        if (isNotificationIgnore(sbn)) {
            return;
        }
        String pkg = sbn.getPackageName();
        String alias = sPackageAliasMap.get(pkg);
        if (alias != null)
            pkg = alias;
        int key = Arrays.hashCode(new Object[] {
                pkg, sbn.getId(), sbn.getTag()
        });
        StatusBarNotification noti = mNotificationMap.get(key);
        ArrayList<StatusBarNotification> list = mNotificationPkgMap.get(pkg);
        if (list != null) {
            synchronized (list) {
                list.remove(noti);
            }
            if (list.size() == 0)
                mNotificationPkgMap.remove(pkg);
        }
        mNotificationMap.remove(key);
        if (mListener != null && mListener.get() != null)
            mListener.get().onNotificationRemoved(sbn);
    }

    public static CardNotificationListenerService getInstance() {
        if (sInstance != null)
            sInstance.ensureInit();
        return sInstance;
    }

    private void ensureInit() {
        if (mNotificationMap == null) {
            mNotificationMap = new SparseArray<StatusBarNotification>();
            ArrayList<StatusBarNotification> notifications = new ArrayList<StatusBarNotification>();
            try {
                StatusBarNotification[] sbns = getActiveNotifications();
                mNotificationMap.clear();
                notifications.addAll(Arrays.asList(sbns));
            } catch (Exception e) {
            }
            for (StatusBarNotification sbn : notifications) {
                if (isNotificationIgnore(sbn)) {
                    continue;
                }
                String pkg = sbn.getPackageName();
                String alias = sPackageAliasMap.get(pkg);
                if (alias != null)
                    pkg = alias;
                mNotificationMap.put(Arrays.hashCode(new Object[] {
                        pkg, sbn.getId(), sbn.getTag()
                }), sbn);
                if (!mPkgList.contains(pkg)) {
                    mPkgList.add(pkg);
                }
                ArrayList<StatusBarNotification> list = mNotificationPkgMap.get(pkg);
                if (list == null) {
                    list = new ArrayList<StatusBarNotification>();
                    mNotificationPkgMap.put(pkg, list);
                }
                list.add(sbn);
            }
        }
    }

    /**
     * sync notifications when importance of notifications has changed
     * returns all package names that have status bar notification
     */
    public Set<String> flushAndReinit(){
        if ( mNotificationMap != null && mNotificationPkgMap != null ){
            ArrayList<StatusBarNotification> notifications = new ArrayList<StatusBarNotification>();
            try {
                StatusBarNotification[] sbns = getActiveNotifications();
                notifications.addAll(Arrays.asList(sbns));
            } catch (Exception e) {
                Log.e(TAG,"flushAndReinit failed",e);
                return Collections.emptySet();
            }

            mNotificationMap.clear();
            mNotificationPkgMap.clear();
            Set<String> result = new HashSet<String>();
            for (StatusBarNotification sbn : notifications) {
                String pkg = sbn.getPackageName();
                result.add(pkg);
                if (isNotificationIgnore(sbn)) {
                    continue;
                }
                String alias = sPackageAliasMap.get(pkg);
                if (alias != null)
                    pkg = alias;
                mNotificationMap.put(Arrays.hashCode(new Object[] {
                        pkg, sbn.getId(), sbn.getTag()
                }), sbn);
                ArrayList<StatusBarNotification> list = mNotificationPkgMap.get(pkg);
                if (list == null) {
                    list = new ArrayList<StatusBarNotification>();
                    mNotificationPkgMap.put(pkg, list);
                }
                list.add(sbn);
            }
            return result;
        } else {
            return Collections.emptySet();
        }
    }

    private boolean isNotificationIgnore(StatusBarNotification sbn) {
        if (sbn == null || (!sbn.isClearable() && !SUPPORT_IMMORTAL && !sPackageWhitelistSet.contains(sbn.getPackageName()))) {
            return true;
        }
        if(sPackageBlacklistSet.contains(sbn.getPackageName())){
            return true;
        }
        Notification notification = sbn.getNotification();
        if (notification.icon == 0) {
            return true;
        }
        Bundle extras = notification.extras;
        CharSequence titles = extras.getCharSequence(Notification.EXTRA_TITLE);
        CharSequence text = extras.getCharSequence(Notification.EXTRA_TEXT);
        if (!SUPPORT_CUSTOM && TextUtils.isEmpty(titles) && TextUtils.isEmpty(text)) {
            return true;
        }
        String pkg = sbn.getPackageName();
        if (pkg == null) {
            return true;
        }

        int notificationShowType = LauncherModel.getNotificationMarkType();
        if( notificationShowType == HomeShellSetting.NO_NOTIFICATION ){
            // if we don't show notifications, just ignore it
            return true;
        }else if( notificationShowType == HomeShellSetting.ALL_NOTIFICATION ){
            // if we need to show all notification, don't ignore
            return false;
        }else if( notificationShowType == HomeShellSetting.PART_NOTIFICATION ){
            if( !isNotificationImportant(pkg) ) return true;
        }

        return false;
    }

    private boolean isNotificationImportant(String pkgName){
        NotificationPriorityHelper nph = NotificationPriorityHelper.getInstance(getApplicationContext());
        return nph.isPkgImportant(pkgName);
    }

    @Override
    public IBinder onBind(Intent intent) {
        sInstance = this;
        return super.onBind(intent);
    }

    @Override
    public void onDestroy() {
        sInstance = null;
        if (null != mNotificationPkgMap) {
            mNotificationPkgMap.clear();
        }
        if (null != mNotificationMap) {
            mNotificationMap.clear();
        }
        if(null != mPkgList){
            mPkgList.clear();
        }
        super.onDestroy();
    }

    public void setListener(StatusBarNotificationListener listener) {
        mListener = listener == null ? null : new SoftReference<StatusBarNotificationListener>(
                listener);
    }

    public ArrayList<StatusBarNotification> getStatusBarNotificationList(String pkg) {
        return mNotificationPkgMap.get(pkg);
    }

    public int getNotificationCount(String pkg) {
        ArrayList<StatusBarNotification> list = mNotificationPkgMap.get(pkg);
        return list == null ? 0 : list.size();

    }
    public ArrayList<String> getRefreshIconPkgList(){
        return mPkgList;
    }

    public static interface StatusBarNotificationListener {
        public void onListenerConnected();
        
        public void onNotificationPosted(StatusBarNotification sbn);

        public void onNotificationRemoved(StatusBarNotification sbn);
    }
}
