package com.tpw.homeshell.backuprestore;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.os.Handler;
import android.os.Process;
import android.util.Log;

import com.tpw.homeshell.backuprestore.BackupManager.OnRestoreFinishListener;
import com.tpw.homeshell.AllAppsList;
import com.tpw.homeshell.LauncherProvider;

import com.tpw.homeshell.appfreeze.AppFreezeUtil;
import com.tpw.homeshell.ApplicationInfo;
import com.tpw.homeshell.Hideseat;
import java.util.Collections;

public class BackupBroadcastReceiver extends BroadcastReceiver {

    private static final String ACTION_APPSTORE =
            "com.tpw.vos.wireless.appstore.restoreAppDone";

    private static final String ACTION_XIAOYUNMI =
            "com.tpw.xiaoyunmi.systembackup.RestoreAllApp";

    private static final String ACTION_HOMESHELL_BACKUP =
            "com.tpw.homeshell.systembackup.RestoreAllApp";

    private static final String ACTION_BACKUP_APP_LIST = "com.tpw.homeshell.backupAppList";

    private static final String RESTORE_DB_FILE = "restore.db";

    private static final String TAG = "HOMESHELL/BackupBroadcastReceiver";

    private Context mContext;

    final Handler restorehandler = new Handler();

    @Override
    public void onReceive(Context context, Intent intent) {
        mContext = context;
        String action = intent.getAction();
        Log.d(TAG, "action = " + action);
        if (ACTION_APPSTORE.equals(action)) {
            ArrayList<String> appListNotFound = intent.getStringArrayListExtra("packageNames");

            for (String packageName : appListNotFound) {
                Log.d(TAG, "appListNotFound packageName = " + packageName);
                BackupManager.getInstance().addRestoreDoneApp(context, packageName);

                BackupManager.getInstance().addRestoreDownloadHandledApp(packageName);
            }
        }else if (ACTION_XIAOYUNMI.equals(action)) {
            boolean downloadnow = intent.getBooleanExtra("downloadnow", false);
            Log.d(TAG, "downloadnow = " + downloadnow);
            BackupManager.setIsRestoreAppFlag(context, downloadnow);

            restorehandler.post(new Runnable() {
                @Override
                public void run() {
                    doRestore();
                }
            });
        }else if (ACTION_HOMESHELL_BACKUP.equals(action)) {
            final Handler handler = new Handler();
            handler.post(new Runnable() {

                @Override
                public void run() {
                    requestDownLoadAppFromAppStore();

                }
            });
        }
    }

    private void requestDownLoadAppFromAppStore() {
        String recordStr = BackupUitil.getBackupAppListString("");

        String filteredAppList = getfilteredAppList(recordStr);
        Log.d(TAG, "requestDownLoadAppFromAppStore  filteredAppList = " + filteredAppList);

        if (filteredAppList.equals("{}")) {
            Log.d(TAG, "No application, set inrestore false");
            BackupManager.setRestoreFlag(mContext, false);
            BackupManager.getInstance().setIsInRestore(false);
            return;
        }

        addNeedRestoreAppList(filteredAppList);
        BackupManager.getInstance().setOnRestoreFinishListener(new OnRestoreFinishListener() {

            @Override
            public void onRestoreFinish() {
                Log.d(TAG, "onRestoreFinish");
                BackupManager.setRestoreFlag(mContext, false);
                BackupManager.getInstance().setIsInRestore(false);
            }
        });

        String requestStr = convertToRequestStr(filteredAppList);

        String action = ACTION_BACKUP_APP_LIST;
        String appList = BackupUitil.ACTION_BACKUP_APP_LIST_INTENT_KEY;
        Intent intent = new Intent(action);
        intent.putExtra(appList, requestStr);
        Log.d(TAG, "requestDownLoadAppFromAppStore  appList = " + requestStr);
        mContext.sendBroadcast(intent);
    }

    private void addNeedRestoreAppList(String appList) {
        JSONObject json = null;
        try {
            json = new JSONObject(appList);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (json != null) {
            Iterator<String> iter = json.keys();
            while (iter.hasNext()) {
                String key = (String) iter.next();
                BackupManager.getInstance().addNeedRestoreApp(key);
            }
        }
    }

    private String convertToRequestStr(String str) {
        String retStr = "";
        JSONObject json = null;
        try {
            json = new JSONObject(str);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (json != null) {
            Iterator<String> iter = json.keys();
            while (iter.hasNext()) {
                String key = (String) iter.next();
                try {
                    retStr = retStr + key + "#" + json.get(key) + ";";
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

        return retStr;
    }

    private String getfilteredAppList(String recordStr) {
        String filteredStr = "";
        JSONObject json = null;
        try {
            json = new JSONObject(recordStr);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if (json != null) {
            List<ResolveInfo> allAppInfo = AllAppsList.getAllActivity(mContext);
            for (ResolveInfo resolveInfo : allAppInfo) {
                String packageName = resolveInfo.activityInfo.packageName;
                json.remove(packageName);
            }
            json.remove("com.android.browser");
            json.remove("com.tpw.homeshell");

            List<ApplicationInfo> allFrozenApps = null;
            if (Hideseat.isHideseatEnabled()) {
                allFrozenApps = AppFreezeUtil.getAllFrozenApps(mContext.getApplicationContext());
            } else {
                allFrozenApps = Collections.emptyList();
            }
            for (ApplicationInfo info : allFrozenApps) {
                if (info.componentName == null) {
                    continue;
                }
                String pkgName = info.componentName.getPackageName();
                json.remove(pkgName);
            }

            filteredStr = json.toString();
        }

        return filteredStr;
    }

    protected void doRestore() {
        Log.d(TAG, "start final doRestore");
        if ((BackupUitil.isRestoring == true) && (BackupUitil.postCount <= 50)) {
            Log.d(TAG, "in restoring status");
            BackupUitil.postCount++;
            restorehandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    doRestore();
                }
            }, 100);
            return;
        }
        BackupUitil.postCount = 0;
        BackupUitil.isRestoring = false;
        if (prepareRestoreFile() == true) {
            BackupManager.setRestoreFlag(mContext, true);
            LauncherProvider.clearLoadDefaultWorkspaceFlag();
            restartHomeshell();
        } else {
            BackupManager.setRestoreFlag(mContext, false);
        }
    }

    private void restartHomeshell() {
        Log.d(TAG, "restartHomeshell");
        Process.killProcess(Process.myPid());
    }

    private boolean prepareRestoreFile() {
        Log.d(TAG, "prepareRestoreFile start");

        File file = mContext.getDatabasePath(LauncherProvider.DATABASE_NAME);
        File backupFileDir = new File(mContext.getFilesDir() + "/backup/");
        backupFileDir.mkdir();
        File backupFile = new File(backupFileDir, RESTORE_DB_FILE);

        if ((backupFile == null) || (backupFile.exists() == false)) {
            return false;
        }
        try {
            BackupUitil.copyFile(backupFile, file);
        } catch (IOException e1) {
            e1.printStackTrace();
            Log.d(TAG, "prepareRestoreFile end with exception");
            return false;
        }
        Log.d(TAG, "prepareRestoreFile end");
        return true;
    }

}
