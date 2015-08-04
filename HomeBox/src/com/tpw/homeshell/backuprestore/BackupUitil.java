package com.tpw.homeshell.backuprestore;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.json.JSONException;
import org.json.JSONObject;

import com.tpw.homeshell.LauncherApplication;
import com.tpw.homeshell.LauncherProvider;
import com.tpw.homeshell.LauncherSettings;
import com.tpw.homeshell.LauncherSettings.Favorites;
import com.tpw.homeshell.smartsearch.HanziToPinyin;
import com.tpw.homeshell.utils.HLog;
import com.tpw.homeshell.ConfigManager;
//import com.tpw.theme.common.jar.util.LOG;

import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;
import android.util.Log;
import android.util.Base64;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.PackageManager;

import android.content.res.AssetManager;
import android.content.res.Resources;

import android.util.DisplayMetrics;
import android.graphics.drawable.Drawable;
import com.tpw.homeshell.ItemInfo;

public class BackupUitil {
    public static final String TAG = "JC";
    static final String TABLE_FAVORITES = "favorites";
    //icon lost after restore
    static final int versionWithBase64 = 20140101;
    static final int maxScreen = ConfigManager.getScreenMaxCount();
    static final int maxCellX = ConfigManager.getCellMaxCountX();
    static final int maxCellY = ConfigManager.getCellMaxCountY();

    // SD app icon position change in restore
    public static final String ACTION_BACKUP_APP_LIST_INTENT_KEY = "backupAppList";

    //restore error
    public static boolean isRestoring = false;
    public static int postCount = 0;

    public static HashMap<String, BackupRecord> convertDBToBackupSet(Cursor c){
        Log.d(TAG, "convertDBToBackupSet");
        HashMap<String, BackupRecord> map = new HashMap<String, BackupRecord>();

        try {
            final int idIndex = c.getColumnIndexOrThrow(Favorites._ID);
            final int intentIndex = c.getColumnIndexOrThrow(Favorites.INTENT);
            final int titleIndex = c.getColumnIndexOrThrow(Favorites.TITLE);
            final int iconTypeIndex = c.getColumnIndexOrThrow(Favorites.ICON_TYPE);
            final int iconIndex = c.getColumnIndexOrThrow(Favorites.ICON);
            final int iconPackageIndex = c.getColumnIndexOrThrow(Favorites.ICON_PACKAGE);
            final int iconResourceIndex = c.getColumnIndexOrThrow(Favorites.ICON_RESOURCE);
            final int containerIndex = c.getColumnIndexOrThrow(Favorites.CONTAINER);
            final int itemTypeIndex = c.getColumnIndexOrThrow(Favorites.ITEM_TYPE);
            final int appWidgetIdIndex = c.getColumnIndexOrThrow(Favorites.APPWIDGET_ID);
            final int screenIndex = c.getColumnIndexOrThrow(Favorites.SCREEN);
            final int cellXIndex = c.getColumnIndexOrThrow(Favorites.CELLX);
            final int cellYIndex = c.getColumnIndexOrThrow(Favorites.CELLY);
            final int spanXIndex = c.getColumnIndexOrThrow(Favorites.SPANX);
            final int spanYIndex = c.getColumnIndexOrThrow(Favorites.SPANY);
            final int deletableIndex = c.getColumnIndexOrThrow(Favorites.CAN_DELEDE);
            final int messageNumIndex = c.getColumnIndexOrThrow(Favorites.MESSAGE_NUM);
            final int isNewIndex = c.getColumnIndexOrThrow(Favorites.IS_NEW);

            while (c.moveToNext()) {
                try {
                    String key = c.getString(idIndex);
                    JSONObject value = new JSONObject();
                    value.put(Favorites._ID, c.getString(idIndex));
                    value.put(Favorites.INTENT, c.getString(intentIndex));
                    value.put(Favorites.TITLE, c.getString(titleIndex));
                    value.put(Favorites.ICON_TYPE, c.getString(iconTypeIndex));
                    //value.put(Favorites.ICON, c.getBlob(iconIndex));
                    if (c.getInt(itemTypeIndex) == 1) {
                        if ((c.getBlob(iconIndex) != null) && (c.getBlob(iconIndex).length < 20 * 1024)){
                            // base64 icon restore exception.
                            try {
                                value.put(Favorites.ICON, Base64.encodeToString(c.getBlob(iconIndex), Base64.DEFAULT));
                            } catch(Exception ex) {
                                ex.printStackTrace();
                            }
                        }
                    }

                    value.put(Favorites.ICON_PACKAGE, c.getString(iconPackageIndex));
                    value.put(Favorites.ICON_RESOURCE, c.getString(iconResourceIndex));
                    value.put(Favorites.CONTAINER, c.getString(containerIndex));
                    value.put(Favorites.ITEM_TYPE, c.getString(itemTypeIndex));
                    value.put(Favorites.APPWIDGET_ID, c.getString(appWidgetIdIndex));
                    value.put(Favorites.SCREEN, c.getString(screenIndex));
                    value.put(Favorites.CELLX, c.getString(cellXIndex));
                    value.put(Favorites.CELLY, c.getString(cellYIndex));
                    value.put(Favorites.SPANX, c.getString(spanXIndex));
                    value.put(Favorites.SPANY, c.getString(spanYIndex));
                    value.put(Favorites.CAN_DELEDE, c.getString(deletableIndex));
                    //we don't need to backup number
                    value.put(Favorites.MESSAGE_NUM, 0);
                    value.put(Favorites.IS_NEW, c.getString(isNewIndex));

                    BackupRecord r = new BackupRecord(value);
                    Log.d("JC", "convertDBToBackupSet  put key = " + key);
                    map.put(key, r);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        catch (Exception e) {
            Log.w(TAG, "Desktop items loading interrupted:", e);
        }
        finally {
            c.close();
        }
        return map;
    }

    public static List<BackupRecord> getIncrementBackupSet(List<BackupRecord> newSet,
                                                           List<BackupRecord> oldSet){
        return null;
    }

    public static int convertBackupSetToDB(SQLiteDatabase db, HashMap<String, BackupRecord> recordSet, int appVersionCode, Context context) throws JSONException{
        Log.d(TAG, "convertBackupSetToDB");
        ContentValues[] rows = new ContentValues[recordSet.size()];
        Log.d(TAG, "recordSet.size() = " + recordSet.size());
        int i = 0;

        Iterator iter = recordSet.entrySet().iterator();
        int container = 0, x = 0, itemType = 0;
        int screen = 0;
        int y = 0;
        String intentcontent = null;

        List<String> downloadingPackageList = new ArrayList<String>();

        while (iter.hasNext()) {
            Map.Entry entry = (Map.Entry) iter.next();
            Object key = entry.getKey();
            BackupRecord backupRecord = (BackupRecord) entry.getValue();
            JSONObject record = backupRecord.getValue();
            ContentValues values = new ContentValues();

            screen = Integer.parseInt(getFieldValue(record, Favorites.SCREEN));
            x = Integer.parseInt(getFieldValue(record, Favorites.CELLX));
            y = Integer.parseInt(getFieldValue(record, Favorites.CELLY));
            itemType = Integer.parseInt(getFieldValue(record, Favorites.ITEM_TYPE));
            container = Integer.parseInt(getFieldValue(record, Favorites.CONTAINER));
            Log.d(TAG, "position: container:screen:x:y " + container + ":" + screen + ":" + x + ":" + y);
            if ((itemType != 100) && (container == -100)){
                if ((screen < 0) || (screen > maxScreen) ||
                    (x < 0) || (x >= maxCellX) ||
                    (y < 0) || (y >= maxCellY)) {
                    Log.d(TAG, "invalid position in recorder: screen:x:y " + screen + ":" + x + ":" + y);
                    continue;
                }
            }

            values.put(Favorites._ID, getFieldValue(record, Favorites._ID));
            values.put(Favorites.TITLE, getFieldValue(record, Favorites.TITLE));
            values.put(Favorites.ICON_TYPE, getFieldValue(record, Favorites.ICON_TYPE));

            values.put(Favorites.ICON_PACKAGE, getFieldValue(record, Favorites.ICON_PACKAGE));
            values.put(Favorites.ICON_RESOURCE, getFieldValue(record, Favorites.ICON_RESOURCE));
            Log.d(TAG, "container = " + container + ", TITLE=" + getFieldValue(record, Favorites.TITLE));
            values.put(Favorites.CONTAINER, container);
            intentcontent = getFieldValue(record, Favorites.INTENT);
            if (itemType == Favorites.ITEM_TYPE_BOOKMARK) { // this is bookmark on 2.0/2.1/2.3
                itemType = Favorites.ITEM_TYPE_SHORTCUT; // from codebase 2.5, item type for bookmark is set to 1
            } else if (itemType == Favorites.ITEM_TYPE_SHORTCUT_DOWNLOADING) {
                intentcontent = intentcontent.replace("packageName=", "packagename=");
                Log.d(TAG, "intentcontent = " + intentcontent);
                try {
                    Intent intent = Intent.parseUri(intentcontent, 0);
                    Log.d(TAG, "intent is null ?= " + (intent == null));
                    String packageName = intent.getStringExtra("packagename");
                    Log.d(TAG, "packageName = " + packageName);
                    downloadingPackageList.add(packageName);
                } catch (Exception e) {
                    Log.e(TAG, "intentcontent = " + intentcontent, e);
                    continue;
                }
            }

            else if (itemType == Favorites.ITEM_TYPE_ALIAPPWIDGET) {
                //item type 7 is localview, in 3.0 localview is replace by gadget
                //gadget item type is 10.
                itemType = Favorites.ITEM_TYPE_GADGET;
                //change to gadget name
                values.put(Favorites.TITLE, "clock_4x1");
            }
            else if (itemType == Favorites.ITEM_TYPE_VPINSTALL) {
                Intent vpIntent = null;
                try {
                    vpIntent = Intent.parseUri(intentcontent, 0);
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                    continue;
                }
                if (vpIntent == null) {
                    continue;
                }
                String vpPath = vpIntent.getStringExtra("packagepath");
                Log.d(TAG, "vp item path is " + vpPath);
                if ((vpPath == null) || (context == null) || (context.getPackageManager() == null)) {
                    continue;
                }
                PackageManager pm = context.getPackageManager();

                PackageInfo packageInfo = pm.getPackageArchiveInfo(vpPath, PackageManager.GET_ACTIVITIES);
                if (packageInfo == null) {
                    //if the apk file not exist, don't restore this item
                    Log.d(TAG, "can not find the apk file");
                    continue;
                }

                packageInfo.applicationInfo.sourceDir = vpPath;
                packageInfo.applicationInfo.publicSourceDir = vpPath;

                //Since vp item's icon can't backup to service because of size
                //retrieve the icon from apk file.
//                PackageParser packageParser = new PackageParser(vpPath);
                DisplayMetrics metrics = new DisplayMetrics();
                metrics.setToDefaults();
                Drawable icon = null;
//                if (packageParser != null) {
//                    PackageParser.Package mPkgInfo = packageParser.parsePackage(new File(vpPath), vpPath, metrics, 0);
                    ApplicationInfo info = packageInfo.applicationInfo;
                    Resources pRes = context.getResources();
//                    AssetManager assmgr = new AssetManager();
//                    assmgr.addAssetPath(vpPath);
                    try{
                        AssetManager assmgr = AssetManager.class.getConstructor(null).newInstance(null);
                        AssetManager.class.getMethod("addAssetPath", String.class).invoke(
                              null, vpPath);

                        Resources res = null;
                        if (pRes != null) {
                            res = new Resources(assmgr, pRes.getDisplayMetrics(), pRes.getConfiguration());
                        }
                        if ((info != null) && (info.icon != 0) && (res != null)) {
                            icon = res.getDrawable(info.icon);
                        }
                        if (icon != null) {
                            ItemInfo.writeBitmap(values, icon);
                        }
                    } catch (Exception ex) {
                        Log.e(TAG, "getAppOriginalIcon error");
                    }
//                }
            }

            if ((itemType == Favorites.ITEM_TYPE_SHORTCUT) && (appVersionCode >= versionWithBase64)) {
                Log.d(TAG, "icon is base64 encoded");
                String iconbase64 = getFieldValue(record, Favorites.ICON);
                if (iconbase64 != null) {
                    values.put(Favorites.ICON, Base64.decode(iconbase64, Base64.DEFAULT));
                }
            }
            else if (itemType != Favorites.ITEM_TYPE_VPINSTALL) {
                values.put(Favorites.ICON, getFieldValue(record, Favorites.ICON));
            }

            values.put(Favorites.INTENT, intentcontent);
            values.put(Favorites.ITEM_TYPE, itemType);
            values.put(Favorites.APPWIDGET_ID, getFieldValue(record, Favorites.APPWIDGET_ID));

            values.put(Favorites.SCREEN, screen);
            // on codebase 2.5, the cell in hotseat must be screen = x
            values.put(Favorites.CELLX, x);
            if (container == Favorites.getContainerHotseat()) {
                Log.d(TAG, "hostseat try to make sure screen = x");
                values.put(Favorites.SCREEN, x);
            }

            values.put(Favorites.CELLY, y);
            values.put(Favorites.SPANX, getFieldValue(record, Favorites.SPANX));
            values.put(Favorites.SPANY, getFieldValue(record, Favorites.SPANY));
            values.put(Favorites.URI, getFieldValue(record, Favorites.URI));
            values.put(Favorites.DISPLAY_MODE, getFieldValue(record, Favorites.DISPLAY_MODE));
            values.put(Favorites.CAN_DELEDE, getFieldValue(record, Favorites.CAN_DELEDE));
            //we don't need to restore unread message number
            values.put(Favorites.MESSAGE_NUM, 0);
            values.put(Favorites.IS_NEW, getFieldValue(record, Favorites.IS_NEW));

            if (itemType == LauncherSettings.BaseLauncherColumns.ITEM_TYPE_APPLICATION ||
                    itemType == LauncherSettings.Favorites.ITEM_TYPE_SHORTCUT ||
                    itemType == LauncherSettings.Favorites.ITEM_TYPE_VPINSTALL ||
                    itemType == LauncherSettings.Favorites.ITEM_TYPE_CLOUDAPP) {
                LauncherProvider.updateTitlePinyin(values, getFieldValue(record, Favorites.TITLE));
            }

            rows[i++] = values;
        }

        checkingDownloadingPackage(downloadingPackageList);

        db.beginTransaction();
        int total = 0;
        try {
            int numValues = i;
            for (i = 0; i < numValues; i++) {
                if (rows[i] == null) {
                    continue;
                }
                if (db.insert(TABLE_FAVORITES, null, rows[i]) < 0) {
                    return 0;
                } else {
                    total++;
                }
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
        Log.d(TAG, "total = " + total);
        return total;
    }

    public static void collectDiffInfo(HashMap<String, BackupRecord> origRecordList,
            HashMap<String, BackupRecord> newRecordList,
            HashMap<String, BackupRecord> addedList,
            HashMap<String, BackupRecord> modifiedList,
            HashMap<String, BackupRecord> deletedList){

        Iterator iter = newRecordList.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry entry = (Map.Entry) iter.next();
            String key = (String) entry.getKey();
            if (false == origRecordList.containsKey(key)) {
                addedList.put(key, (BackupRecord) entry.getValue());
            }else {
                BackupRecord newRecord = (BackupRecord) entry.getValue();
                BackupRecord origRecord = origRecordList.get(key);
                if (false == newRecord.getValue().toString().equals(origRecord.getValue().toString())) {
                    modifiedList.put(key, (BackupRecord) entry.getValue());
                }
            }
        }

        iter = origRecordList.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry entry = (Map.Entry) iter.next();
            String key = (String) entry.getKey();
            if (false == newRecordList.containsKey(key)) {
                deletedList.put(key, (BackupRecord) entry.getValue());
            }
        }
    }

    private static String getFieldValue(JSONObject obj, String key){
        String str = null;
        try {
            str = obj.getString(key);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return str;
    }

    public static void copyFile(File sourceFile,File targetFile)
            throws IOException{
        Log.d("JC", "prepareBackupFile copyFile" + sourceFile.toString()
                + " " + targetFile.toString());
        FileInputStream input = new FileInputStream(sourceFile);
        BufferedInputStream inBuff=new BufferedInputStream(input);

        FileOutputStream output = new FileOutputStream(targetFile);
        BufferedOutputStream outBuff=new BufferedOutputStream(output);

        byte[] b = new byte[1024 * 5];
        int len;
        while ((len =inBuff.read(b)) != -1) {
            outBuff.write(b, 0, len);
        }

        outBuff.flush();
        inBuff.close();
        input.close();
        outBuff.close();
        output.close();
    }

    public static BackupRecord getBackupRecord(String pkgName) {
        return getBackupRecord(pkgName, null);
    }

    public static BackupRecord getBackupRecordById(long searchId) {
        BackupRecord record = null;
        for (Entry<String, BackupRecord> r : LauncherApplication.mBackupRecordMap.entrySet()) {
            long itemid = Long.parseLong(r.getValue().getField(Favorites._ID));
            if (itemid == searchId) {
                record = r.getValue();
                break;
            }
        }
        return record;
    }

    public static BackupRecord getBackupRecord(String pkgName, String clsName) {
        BackupRecord record = null;
        for (Entry<String, BackupRecord> r : LauncherApplication.mBackupRecordMap.entrySet()) {
            String intentStr = r.getValue().getField(Favorites.INTENT);
            if (TextUtils.isEmpty(intentStr)) {
                continue;
            }
            try {
                Intent intent = Intent.parseUri(intentStr, 0);
                final ComponentName name = intent.getComponent();
                if (name != null) {
                    HLog.d(TAG, "pkgName=" + pkgName + " getPackageName()=" + intent.getComponent().getPackageName());
                    if (pkgName.equals(intent.getComponent().getPackageName())) {
                        // if clsName not assigned, return the first matched record with package name.
                        if (null == clsName) {
                            return r.getValue();
                        }

                        if (clsName.equals(intent.getComponent().getClassName())) {
                            return r.getValue();
                        }else {
                            continue;
                        }
                    } ;
                }

            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
        }
        return record;
    }

    public static boolean isInRestoreList(Context context, String packageName) {
        String recordStr = getBackupAppListString("");

        JSONObject json = null;
        try {
            json = new JSONObject(recordStr);
            if ((json != null) && (json.has(packageName) == true)) {
                Log.d(TAG, "package " + packageName + " in restore list.");
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static void removePackageFromAllAppListPreference(Context context, String packageName) {
        String recordStr = getBackupAppListString("");

        JSONObject json = null;
        try {
            json = new JSONObject(recordStr);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.d("BackupManager", "removePackageFromAllAppListPreference package name is " +  packageName);
        if ((json != null) && (json.has(packageName) == true)) {
            Log.d("BackupManager", "package " + packageName + " in json");
            json.remove(packageName);
            setBackupAppListString(json.toString());
        }
    }

    private static void checkingDownloadingPackage(List<String> downloadingPackageList) {
        if (downloadingPackageList == null || downloadingPackageList.size() == 0) {
            Log.d(TAG, "checkingDownloadingPackage(): No downloading package.");
            return;
        }
        Log.d(TAG, "checkingDownloadingPackage(): Handle checkingDownloadingPackage. Package count = " + downloadingPackageList.size());
        // Handle packages string.
        StringBuilder downloadingPsb = new StringBuilder();
        for (String packageStr : downloadingPackageList) {
            if (downloadingPsb.length() == 0) {
                downloadingPsb.append("\"").append(packageStr).append("\":\"").append(packageStr).append("\"");
            } else {
                downloadingPsb.append(",\"").append(packageStr).append("\":\"").append(packageStr).append("\"");
            }
        }

        // Put packages string to SharedPreferences.
        String allAppListString = getBackupAppListString("");
        if (!TextUtils.isEmpty(allAppListString)) {
            if (!allAppListString.startsWith("{") && !allAppListString.endsWith("}")) {
                Log.e(TAG, "checkingDownloadingPackage(): All app list is a error string. allAppListString = " + allAppListString, new Exception());
                return;
            }
            if (allAppListString.replace(" ", "").equals("{}")) {
                allAppListString = "{" + downloadingPsb.toString() + "}";
            } else {
                allAppListString = allAppListString.substring(0, (allAppListString.length() - 1))+ "," + downloadingPsb.toString() + "}";
            }
        } else {
            allAppListString = "{" + downloadingPsb + "}";
        }
        // save appList to SharedPreferences
        setBackupAppListString(allAppListString);
        Log.d(TAG, "checkingDownloadingPackage(): Put app list to shared preferences. AppList recordStr = " + allAppListString);
        downloadingPackageList.clear();
    }

    public static String getBackupAppListString(String defaultValue) {
        return LauncherApplication.getContext().getSharedPreferences(ACTION_BACKUP_APP_LIST_INTENT_KEY, Context.MODE_PRIVATE)
                        .getString(ACTION_BACKUP_APP_LIST_INTENT_KEY, defaultValue);
    }

    public static void setBackupAppListString(String recordStr) {
        LauncherApplication.getContext().getSharedPreferences(ACTION_BACKUP_APP_LIST_INTENT_KEY, Context.MODE_PRIVATE).edit()
                .putString(ACTION_BACKUP_APP_LIST_INTENT_KEY, recordStr).commit();
    }
}
