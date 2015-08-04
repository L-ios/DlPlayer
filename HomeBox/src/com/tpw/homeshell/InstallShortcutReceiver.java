/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tpw.homeshell;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.json.JSONObject;
import org.json.JSONStringer;
import org.json.JSONTokener;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;
import android.os.Parcelable;

import com.tpw.homeshell.model.LauncherModel;

import com.tpw.homeshell.FileAndInfo;

public class InstallShortcutReceiver extends BroadcastReceiver {
    public static final String ACTION_INSTALL_SHORTCUT =
            "com.tpw.homeshell.action.INSTALL_SHORTCUT";
    public static final String ACTION_ORI_INSTALL_SHORTCUT =
            "com.android.launcher.action.INSTALL_SHORTCUT";
    public static final String TAG = "InstallShortcutReceiver";
    public static final String CATEGORY_SHORTCUT_CLOUDAPP =
            "com.tpw.homeshell.category.SHORTCUT_CLOUDAPP";
    private static final String INTENT = "intent";
    private static final String NEED_TOAST = "needToast";
    private static final String LABEL = "label";
    private static final String ICON = "icon";
    private static final String ITEMTYPE = "itemtype";
    public static final String NEW_APPS_PAGE_KEY = "apps.new.page";
    public static final String NEW_APPS_LIST_KEY = "apps.new.list";

    public static final String DATA_INTENT_KEY = "intent.data";
    public static final String LAUNCH_INTENT_KEY = "intent.launch";
    public static final String NAME_KEY = "name";
    public static final String ICON_KEY = "icon";
    public static final String ICON_RESOURCE_NAME_KEY = "iconResource";
    public static final String ICON_RESOURCE_PACKAGE_NAME_KEY = "iconResourcePackage";
    // The set of shortcuts that are pending install
    public static final String APPS_PENDING_INSTALL = "apps_to_install";

    public static final int NEW_SHORTCUT_BOUNCE_DURATION = 450;
    public static final int NEW_SHORTCUT_STAGGER_DELAY = 75;

    private static final int INSTALL_SHORTCUT_SUCCESSFUL = 0;
    private static final int INSTALL_SHORTCUT_IS_DUPLICATE = -1;
    private static final int INSTALL_SHORTCUT_NO_SPACE = -2;

    // A mime-type representing shortcut data
    public static final String SHORTCUT_MIMETYPE =
            "com.tpw.homeshell/shortcut";

    private static Object sLock = new Object();

    private static void addToStringSet(SharedPreferences sharedPrefs,
            SharedPreferences.Editor editor, String key, String value) {
        Set<String> strings = sharedPrefs.getStringSet(key, null);
        if (strings == null) {
            strings = new HashSet<String>(0);
        } else {
            strings = new HashSet<String>(strings);
        }
        strings.add(value);
        editor.putStringSet(key, strings);
    }

    private static void addToInstallQueue(
            SharedPreferences sharedPrefs, PendingInstallShortcutInfo info) {
        synchronized(sLock) {
            try {
                JSONStringer json = new JSONStringer()
                    .object()
                    .key(DATA_INTENT_KEY).value(info.data.toUri(0))
                    .key(LAUNCH_INTENT_KEY).value(info.launchIntent.toUri(0))
                    .key(NAME_KEY).value(info.name);
                if (info.icon != null) {
                    byte[] iconByteArray = ItemInfo.flattenBitmap(info.icon);
                    json = json.key(ICON_KEY).value(
                        Base64.encodeToString(
                            iconByteArray, 0, iconByteArray.length, Base64.DEFAULT));
                }
                if (info.iconResource != null) {
                    json = json.key(ICON_RESOURCE_NAME_KEY).value(info.iconResource.resourceName);
                    json = json.key(ICON_RESOURCE_PACKAGE_NAME_KEY)
                        .value(info.iconResource.packageName);
                }
                json = json.endObject();
                SharedPreferences.Editor editor = sharedPrefs.edit();
                addToStringSet(sharedPrefs, editor, APPS_PENDING_INSTALL, json.toString());
                editor.commit();
            } catch (org.json.JSONException e) {
                Log.d("InstallShortcutReceiver", "Exception when adding shortcut: " + e);
            }
        }
    }

    private static ArrayList<PendingInstallShortcutInfo> getAndClearInstallQueue(
            SharedPreferences sharedPrefs) {
        synchronized(sLock) {
            Set<String> strings = sharedPrefs.getStringSet(APPS_PENDING_INSTALL, null);
            if (strings == null) {
                return new ArrayList<PendingInstallShortcutInfo>();
            }
            ArrayList<PendingInstallShortcutInfo> infos =
                new ArrayList<PendingInstallShortcutInfo>();
            for (String json : strings) {
                try {
                    JSONObject object = (JSONObject) new JSONTokener(json).nextValue();
                    Intent data = Intent.parseUri(object.getString(DATA_INTENT_KEY), 0);
                    Intent launchIntent = Intent.parseUri(object.getString(LAUNCH_INTENT_KEY), 0);
                    String name = object.getString(NAME_KEY);
                    String iconBase64 = object.optString(ICON_KEY);
                    String iconResourceName = object.optString(ICON_RESOURCE_NAME_KEY);
                    String iconResourcePackageName =
                        object.optString(ICON_RESOURCE_PACKAGE_NAME_KEY);
                    if (iconBase64 != null && !iconBase64.isEmpty()) {
                        byte[] iconArray = Base64.decode(iconBase64, Base64.DEFAULT);
                        Bitmap b = BitmapFactory.decodeByteArray(iconArray, 0, iconArray.length);
                        data.putExtra(Intent.EXTRA_SHORTCUT_ICON, b);
                    } else if (iconResourceName != null && !iconResourceName.isEmpty()) {
                        Intent.ShortcutIconResource iconResource =
                            new Intent.ShortcutIconResource();
                        iconResource.resourceName = iconResourceName;
                        iconResource.packageName = iconResourcePackageName;
                        data.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, iconResource);
                    }
                    data.putExtra(Intent.EXTRA_SHORTCUT_INTENT, launchIntent);
                    PendingInstallShortcutInfo info =
                        new PendingInstallShortcutInfo(data, name, launchIntent);
                    infos.add(info);
                } catch (org.json.JSONException e) {
                    Log.d("InstallShortcutReceiver", "Exception reading shortcut to add: " + e);
                } catch (java.net.URISyntaxException e) {
                    Log.d("InstallShortcutReceiver", "Exception reading shortcut to add: " + e);
                }
            }
            sharedPrefs.edit().putStringSet(APPS_PENDING_INSTALL, new HashSet<String>()).commit();
            return infos;
        }
    }

    // Determines whether to defer installing shortcuts immediately until
    // processAllPendingInstalls() is called.
    private static boolean mUseInstallQueue = false;

    private static class PendingInstallShortcutInfo {
        Intent data;
        Intent launchIntent;
        String name;
        Bitmap icon;
        Intent.ShortcutIconResource iconResource;
        boolean mNeedToast;

        public PendingInstallShortcutInfo(Intent rawData, String shortcutName,
                Intent shortcutIntent) {
            data = rawData;
            name = shortcutName;
            launchIntent = shortcutIntent;
            mNeedToast = false;
        }
    }

    public void onReceive(Context context, Intent data) {
        Log.d("InstallShortcutReceiver", "receive install shortcut");
        if (data == null) {
            return;
        }

        if (!ACTION_INSTALL_SHORTCUT.equals(data.getAction()) &&
               !ACTION_ORI_INSTALL_SHORTCUT.equals(data.getAction())) {
            return;
        }
        Parcelable pIntent = data.getParcelableExtra(Intent.EXTRA_SHORTCUT_INTENT);
        if (pIntent == null) {
            pIntent = data.getParcelableExtra(INTENT);
            if (pIntent != null) {
                data.putExtra(Intent.EXTRA_SHORTCUT_INTENT, pIntent);
            } else {
                Log.d("InstallShortcutReceiver", "intent is null");
                return;
            }
        }
        Intent intent = null;
        if (pIntent instanceof Intent) {
            intent = (Intent) pIntent;
        }
        if (intent == null) {
            return;
        }

        if ((intent.getPackage() != null) && (intent.getPackage().contains("com.UCMobile"))) {
            Log.d(TAG, "intent package is com.UCMobile");
            return;
        }
        // This name is only used for comparisons and notifications, so fall back to activity name
        // if not supplied
        String name = data.getStringExtra(Intent.EXTRA_SHORTCUT_NAME);
        Log.d(TAG, "intent.getComponent() is " + intent.getComponent());
        if ((name == null) && (intent.getComponent() != null)) {
            try {
                PackageManager pm = context.getPackageManager();
                ActivityInfo info = pm.getActivityInfo(intent.getComponent(), 0);
                name = info.loadLabel(pm).toString();
            } catch (PackageManager.NameNotFoundException nnfe) {
                return;
            }
        }

        Parcelable pIcon = data.getParcelableExtra(Intent.EXTRA_SHORTCUT_ICON);
        Bitmap icon = null;
        if ((pIcon != null) && (pIcon instanceof Bitmap)) {
            icon = (Bitmap)pIcon;
        }
        Parcelable pRes = data.getParcelableExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE);
        Intent.ShortcutIconResource iconResource = null;
        if ((pRes != null) && (pRes instanceof Intent.ShortcutIconResource)) {
            iconResource = (Intent.ShortcutIconResource)pRes;
        }

        // Queue the item up for adding if launcher has not loaded properly yet
        boolean launcherNotLoaded = LauncherModel.getCellCountX() <= 0 ||
                LauncherModel.getCellCountY() <= 0;

        if (name == null) {
            name = data.getStringExtra(LABEL);
            data.putExtra(Intent.EXTRA_SHORTCUT_NAME, name);
        }
        //don't support shortcut duplicate mode
        data.putExtra(Launcher.EXTRA_SHORTCUT_DUPLICATE, false);
        //check and get icon from "icon"
        if (icon == null) {
            Log.d(TAG, "icon is null, get another");
            icon = data.getParcelableExtra(ICON);
            data.putExtra(Intent.EXTRA_SHORTCUT_ICON, icon);
        }
        PendingInstallShortcutInfo info = new PendingInstallShortcutInfo(data, name, intent);
        info.icon = icon;
        info.iconResource = iconResource;

        info.mNeedToast = data.getBooleanExtra(NEED_TOAST, true);
        int itemtype = LauncherSettings.Favorites.ITEM_TYPE_BOOKMARK;
        Set<String> categories = data.getCategories();
        if(categories != null){
            for (String catg : categories) {
                if (CATEGORY_SHORTCUT_CLOUDAPP.equals(catg)) {
                    Log.d(TAG, "it is a cloud app");
                    //itemtype = LauncherSettings.Favorites.ITEM_TYPE_CLOUDAPP;
                    break;
                }
            }
        }
        //data.putExtra(ITEMTYPE, itemtype);

        if (mUseInstallQueue || launcherNotLoaded) {
            String spKey = LauncherApplication.getSharedPreferencesKey();
            SharedPreferences sp = context.getSharedPreferences(spKey, Context.MODE_PRIVATE);
            addToInstallQueue(sp, info);
        } else {
            processInstallShortcut(context, info);
        }
    }

    static void enableInstallQueue() {
        mUseInstallQueue = true;
    }
    static void disableAndFlushInstallQueue(Context context) {
        mUseInstallQueue = false;
        flushInstallQueue(context);
    }
    static void flushInstallQueue(Context context) {
        String spKey = LauncherApplication.getSharedPreferencesKey();
        SharedPreferences sp = context.getSharedPreferences(spKey, Context.MODE_PRIVATE);
        ArrayList<PendingInstallShortcutInfo> installQueue = getAndClearInstallQueue(sp);
        Iterator<PendingInstallShortcutInfo> iter = installQueue.iterator();
        while (iter.hasNext()) {
            processInstallShortcut(context, iter.next());
        }
    }

    private static void processInstallShortcut(Context context,
            PendingInstallShortcutInfo pendingInfo) {
        Log.d(TAG, "processInstallShortcut in");
        String spKey = LauncherApplication.getSharedPreferencesKey();
        SharedPreferences sp = context.getSharedPreferences(spKey, Context.MODE_PRIVATE);

        final Intent data = pendingInfo.data;
        final Intent intent = pendingInfo.launchIntent;
        final String name = pendingInfo.name;

        // Lock on the app so that we don't try and get the items while apps are being added
        LauncherApplication app = (LauncherApplication) context.getApplicationContext();
        synchronized (app) {
        	//topwise zyf add exapp 
        	Log.d("zyfonline","processInstallShortcut intent = "+intent);
            //topwise zyf add end	
        	LauncherModel lmodel=app.getModel();
        	if(intent!=null&&intent.getComponent()!=null)
        	{
        		String pkgname=intent.getComponent().getPackageName();
        		
        		Log.d("zyfonline","processInstallShortcut intent.getComponent() = "+intent.getComponent());
	            if(pkgname!=null&&(lmodel.getExappApk(pkgname))!=null)
	            {
	            	Log.d("zyfonline","processInstallShortcut is exapp return!!!");
	            	return;
	            }
	          //topwise zyf add for folderonline
	            if(pkgname!=null)
	            {
		           ApkInfo apkinfo=lmodel.getApkinfoByPkg(pkgname);
		           Log.d("zyfonline","processInstallShortcut apkinfo = "+apkinfo);
		        	if(apkinfo!=null)
		        	{
		        		Log.d("zyfonline","processInstallShortcut is online apk return!!!");
		        		return;
		        	}
	            }
	          //topwise zyf add end
        	}
        	//topwise zyf add end
            app.getModel().installShortcutInWorkerThread(context, data, name, intent, sp);
        }
    }
}
