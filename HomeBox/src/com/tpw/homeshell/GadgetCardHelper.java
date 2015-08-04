
package com.tpw.homeshell;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import com.tpw.homeshell.CardNotificationPanelView;
import com.tpw.homeshell.LauncherSettings.Favorites;
import com.tpw.homeshell.R;
import com.tpw.homeshell.CardNotificationListenerService;
import com.tpw.homeshell.CardNotificationListenerService.StatusBarNotificationListener;
import com.tpw.homeshell.icon.BubbleTextView;
import com.tpw.homeshell.model.LauncherModel;
import com.tpw.homeshell.utils.PrivacySpaceHelper;
import com.tpw.homeshell.utils.Utils;

import commonlibs.utils.ACA;
//import tpw.v3.gadget.GadgetView;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.service.notification.StatusBarNotification;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

final public class GadgetCardHelper implements
        StatusBarNotificationListener {
    private static final HashSet<String> sConstGadgetCard = new HashSet<String>();
    private static final HashSet<String> sRevertGadgetCard = new HashSet<String>();
    private static final String TAG = "GadgetCardHelper";
    private static GadgetCardHelper sInscance;
    private Context mContext;
    private View mPrivacyCard;
    private CardNotificationPanelView mNotificationPanel;
    private CardNotificationListenerService mSNL;
    private String mPkgName;
    private HashMap<ComponentName, WeakReference<View>> mGadgetCache = new HashMap<ComponentName, WeakReference<View>>();
    private HashMap<String, WeakReference<View>> mBubbleTextViewCache = new HashMap<String, WeakReference<View>>();
    private HashSet<ComponentName> mEmptyGadgetCard = new HashSet<ComponentName>();
    private Map<String, Integer> mDisabledPackages = new HashMap<String, Integer>();
    private static boolean sInit = false;
    private ComponentName mCnName = null;
    static {
        sConstGadgetCard.add("fm.xiami.yunos");
        sConstGadgetCard.add("com.android.calendar");
        sConstGadgetCard.add("com.android.settings");
        sConstGadgetCard.add("com.yunos.weatherservice");
        sConstGadgetCard.add("com.android.mms");
        sConstGadgetCard.add("com.tpw.wireless.vos.appstore");

        sRevertGadgetCard.add("com.yunos.weatherservice");
    }

    private GadgetCardHelper(Context context) {
        mContext = context;
    }

    private View getPrivacyCard(ComponentName cn, final View v) {
        if (mPrivacyCard == null) {
            //mPrivacyCard = LauncherGadgetHelper.getGadget(mContext, new ComponentName("com.yunos.privacy", ""));
        }
        if (mPrivacyCard != null)
            mPrivacyCard.setOnClickListener(new View.OnClickListener() {
                public void onClick(View gadget) {
                    try {
                        ShortcutInfo info = (ShortcutInfo) v.getTag();
                        LauncherApplication.getLauncher().startActivitySafely(v, info.intent, info);
                    } catch (Exception e) {
                    }
                }
            });
        return mPrivacyCard;
    }

    public static GadgetCardHelper getInstance(Context context) {
        if (sInscance == null)
            sInscance = new GadgetCardHelper(context.getApplicationContext());
        return sInscance;
    }

    private void clearViewCache() {
        mBubbleTextViewCache.clear();
        mEmptyGadgetCard.clear();
    }

    private void clearGadgetCache() {
        mGadgetCache.clear();
        if (mPrivacyCard != null) {
            //if (mPrivacyCard instanceof GadgetView)
            //    ((GadgetView) mPrivacyCard).cleanUp();
            mPrivacyCard = null;
        }
    }

    public static void onThemeChanged() {
        if (sInscance != null) {
            sInscance.clearViewCache();
            sInscance.clearGadgetCache();
        }
    }

    public static void onLocaleChanged() {
        if (sInscance != null)
            sInscance.clearGadgetCache();
    }

    public View getCardView(ComponentName cn) {
        return getCardView(cn, null, null, null, 0);
    }

    public View getCardView(ComponentName cn, View iconView, Drawable drawable, CharSequence appName, int msgNum) {
        // Note: this method should keep consistency with method hasCardView().
        if (cn == null)
            return null;
        boolean fetchAppName = TextUtils.isEmpty(appName);
        if (drawable == null || fetchAppName) {
            Intent intent = new Intent();
            intent.setComponent(cn);
            PackageManager pm = mContext.getPackageManager();
            ResolveInfo r = pm.resolveActivity(intent, 0);
            ActivityInfo info = null;
            if (r != null && (info = r.activityInfo) != null) {
                if (drawable == null)
                    drawable = info.loadIcon(pm);
                if (fetchAppName)
                    appName = info.loadLabel(pm);
            }
        }
        String pkgName = mPkgName = cn.getPackageName();
        if (sRevertGadgetCard.contains(pkgName) && msgNum <= 0) {
            int notifications = getNotificationCount(cn, iconView);
            if (notifications > 0) {
                mNotificationPanel = getNotificationView(pkgName, drawable, appName);
                return mNotificationPanel;
            }
        }
        if (isCardAvaliable(cn, msgNum)) {
            boolean privacy = false;
            View v = (privacy = isPrivacy(cn)) ? getPrivacyCard(cn, iconView) : getGadget(cn);
            //if (v instanceof GadgetView)
            //    ((GadgetView) v).rebind();
            if (v != null)
                return v;
            // we cache ComponentName which can not pair it's own gadget card to
            // boost process of get card
            if (!privacy)
                mEmptyGadgetCard.add(cn);
        }
        if (msgNum <= 0) {
            int notifications = getNotificationCount(cn, iconView);
            if (notifications > 0) {
                mNotificationPanel = getNotificationView(pkgName, drawable, appName);
                return mNotificationPanel;
            }
        }
        return null;
    }

    /**
     * This method is used to determine whether an icon has corresponding
     * card view or not.
     */
    public boolean hasCardView(ComponentName cn, View iconView, int msgNum) {
        if (cn == null)
            return false;
        if (Utils.isYunOS2_9System())
            return false;
        String pkgName = cn.getPackageName();
        if (sRevertGadgetCard.contains(pkgName) && msgNum <= 0) {
            int notifications = getNotificationCount(cn, iconView);
            if (notifications > 0) {
                return true;
            }
        }
        if (isCardAvaliable(cn, msgNum)) {
            return true;
        }
        if (msgNum <= 0) {
            int notifications = getNotificationCount(cn, iconView);
            if (notifications > 0) {
                return true;
            }
        }
        return false;
    }

    private boolean isCardAvaliable(ComponentName cn, int msgNum) {
        if (cn != null && !mEmptyGadgetCard.contains(cn)) {
            String pkgName = cn.getPackageName();
            if ("com.yunos.alicontacts".equals(pkgName)) {
                return "com.yunos.alicontacts.activities.DialtactsContactsActivity".equals(cn.getClassName());
            }
            return sConstGadgetCard.contains(pkgName) || msgNum > 0;
        }
        return false;
    }

    private boolean shouldIgnoreNotification(ComponentName cn) {
        String pkgName = cn.getPackageName();
        String clazzName = cn.getClassName();
        if (mDisabledPackages.containsKey(pkgName) || "com.yunos.alicontacts".equals(pkgName)
                && "com.yunos.alicontacts.activities.DialtactsContactsActivity".equals(clazzName))
            return true;
        return false;
    }

    private boolean isPrivacy(ComponentName cn) {
        return PrivacySpaceHelper.getInstance(mContext).isPackageLocked(cn.getPackageName());
    }

    private View getGadget(ComponentName cn) {
        View gadget = null;
        /*WeakReference<View> ref = mGadgetCache.get(cn);
        if (ref != null)
            gadget = ref.get();
        if (gadget == null) {
            Log.d(TAG, "get gadget card: " + cn);
            gadget = LauncherGadgetHelper.getGadget(mContext, cn);
            if (gadget != null)
                mGadgetCache.put(cn, new WeakReference<View>(gadget));
        }*/
        return gadget;
    }

    public int getNotificationCount(ComponentName cn, View view) {
        if (cn == null)
            return 0;
        String pkg = cn.getPackageName();
        if (pkg == null)
            return 0;
        if (shouldIgnoreNotification(cn))
            return 0;
        View refView = null;
        // add listener
        WeakReference<View> reference = mBubbleTextViewCache.get(pkg);
        if (reference != null) {
            refView = reference.get();
            if (refView != null && refView.getParent() == null)
                refView = null;
        }
        if (refView == null && view != null) {
            ShortcutInfo info = (ShortcutInfo) view.getTag();
            // ignore shortcut
            if (info == null || info.itemType != Favorites.ITEM_TYPE_APPLICATION)
                return 0;
            mBubbleTextViewCache.put(pkg, new WeakReference<View>(view));
        } else if (refView != null && view != null && refView != view) {
            ShortcutInfo cnRef = (ShortcutInfo) refView.getTag();
            if (cnRef != null && cnRef.intent != null) {
                if (cn.equals(cnRef.intent.getComponent()))
                    mBubbleTextViewCache.put(pkg, new WeakReference<View>(view));
                else
                    return 0;
            } else {
                return 0;
            }
        }

        if (mSNL == null) {
            mSNL = CardNotificationListenerService.getInstance();
            if (mSNL != null) {
                if (mCnName != null) {
                    Iterator<String> iter = mBubbleTextViewCache.keySet().iterator();
                    while (iter.hasNext()) {
                        Object key = iter.next();
                        WeakReference<View> referenceLocal = mBubbleTextViewCache.get(key);
                        View viewLocal = null;
                        if (referenceLocal != null) {
                            viewLocal = referenceLocal.get();
                            if (viewLocal != null) {
                                viewLocal.postInvalidate();
                            }
                        }
                    }
                }
                mSNL.setListener(this);
            } else {
                // should unbind NotificationListenerService,and when luancher is in foreground, 
                //restore binding NotificationListenerService
//                if (!sInit) {
//                    try {
//                        Intent intent = new Intent(mContext, CardNotificationListenerService.class);
//                        mCnName = mContext.startService(intent);
//                        sInit = true;
//                    } catch (Exception e) {
//                        Log.e(TAG, "start CardNotificationListenerService failed", e);
//                    }
//                }
                return 0;
            }
        }
        return mSNL.getNotificationCount(pkg);
    }

    public CardNotificationPanelView getNotificationView(String pkg, Drawable draw, CharSequence appName) {
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);
        CardNotificationPanelView notificationView = (CardNotificationPanelView) inflater.inflate(
                R.layout.card_show_notification, null);
        notificationView.setPackage(pkg);
        notificationView.setAppName(appName);
        notificationView.setIcon(draw);
        notificationView.setHelper(sInscance);
        notificationView.initStatusBarNotificationList(mSNL.getStatusBarNotificationList(pkg));
        return notificationView;
    }

    public void cancelNotification(StatusBarNotification sbn) {
        mSNL.cancelNotification(sbn.getPackageName(), sbn.getTag(), sbn.getId());
    }

    private void refreshIconView(String pkg) {
        View view = null;
        WeakReference<View> reference = mBubbleTextViewCache.get(pkg);
        if (reference != null) {
            view = reference.get();
            if (view instanceof BubbleTextView) {
                final BubbleTextView target = (BubbleTextView) view;
                LauncherModel.postRunnableIdle(new Runnable() {
                    @Override
                    public void run() {
                        target.updateTitleForIndicator();
                        target.postInvalidate();
                    }
                });
            }
        }
    }

    public void onLaunchActivity(ComponentName cn) {
        if (cn == null)
            return;
        String pkgName = cn.getPackageName();
        if (mSNL != null) {
            int count = mSNL.getNotificationCount(pkgName);
            if (count > 0) {
                // hide icon message number after user clicks the icon
                mDisabledPackages.put(pkgName, count);
            }
        }
    }
    public void onListenerConnected() {
        mSNL = CardNotificationListenerService.getInstance();
        Iterator<Entry<String, Integer>> itr =  mDisabledPackages.entrySet().iterator();
        while (itr.hasNext()) {
            Entry<String, Integer> entry = itr.next();
            int newCount = mSNL.getNotificationCount(entry.getKey());
            // if new notifications come up, removes the package from mDisabledPackages
            if (newCount > entry.getValue()) {
                itr.remove();
            }
        }
        ArrayList<String> pkgList = mSNL.getRefreshIconPkgList();
        for (String pkgName : pkgList) {
            refreshIconView(pkgName);
        }
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        String pkgName = sbn.getPackageName();
        mDisabledPackages.remove(pkgName);
        if (null != mNotificationPanel && (mPkgName.equals(pkgName))) {
            mNotificationPanel.addNotification(sbn);
        }
        refreshIconView(pkgName);
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        if (ACA.Notification.showType(sbn.getNotification()) == ACA.Notification.DEFAULT_SHOW) {
            return;
        }

        String pkgName = sbn.getPackageName();
        if (null != mNotificationPanel && (mPkgName.equals(pkgName))) {
            mNotificationPanel.removeNotification(sbn);
        }
        refreshIconView(pkgName);
    }
}
