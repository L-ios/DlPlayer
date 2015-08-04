package com.tpw.homeshell;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;

import com.tpw.homeshell.LauncherSettings.Favorites;
import com.tpw.homeshell.appgroup.AppGroupManager;
import com.tpw.homeshell.backuprestore.BackupManager;
import com.tpw.homeshell.backuprestore.BackupRecord;
import com.tpw.homeshell.backuprestore.BackupUitil;
import com.tpw.homeshell.model.LauncherModel;
import com.tpw.homeshell.model.LauncherModel.Callbacks;
import com.tpw.homeshell.utils.Utils;
import com.tpw.homeshell.utils.Utils.DownloadTaskHandler;

public class AppDownloadManager {
    private static final boolean DEBUG = true;
    private static final String TAG = "AppDownloadManager";
    private static final String TYPE_ICON_URI = "icon";
    public static final String TYPE_PACKAGENAME = "packagename";
    public static final String TYPE_PACKAGENAME_OLD = "packageName";
    private static final String TYPE_APPID = "id";
    public static final String TYPE_ACTION = "action";
    public static final String TYPE_PROGRESS = "progress";

    private static final int DOWNLOAD_COMPLETE_PROGRESS = 100;
    private static final int DOWNLOAD_NOT_START_PROGRESS = 0;
    private static final int DOWNLOAD_INVALID_PROGRESS = -99;

    public static final long DOWNLOAD_ICON_PRESS_INTERVAL = 1200;

    public static final int DOWNLOAD_TYPE_DOWNLOAD = 0;
    public static final int DOWNLOAD_TYPE_UPDATE = 1;
    
    
    //topwise zyf add for folderonline
    public static final String ACTION_ONLINE_DOWNLOAD_TASK="download.online.task";
    public static final String ACTION_ONLINE_DOWNLOAD_START = "online.download.start";
    public static final String ACTION_ONLINE_DOWNLOAD_RUNNING = "online.download.running";
    public static final String ACTION_ONLINE_DOWNLOAD_PAUSE = "online.download.pause";
    public static final String ACTION_ONLINE_DOWNLOAD_FAIL = "online.download.fail";
    public static final String ACTION_ONLINE_DOWNLOAD_CANCEL = "online.download.cancel";
    
    public static final int OP_ONLINE_START = 0;
    public static final int OP_ONLINE_RUNING = 1;
    public static final int OP_ONLINE_PAUSE = 2;
    public static final int OP_ONLINE_FAIL = 3;
    public static final int OP_ONLINE_CANCEL = 4;
    
    public static final String ONLINE_PAUSE_EXTRA="pause.extra";
    
    public static final int ONLINE_PAUSE_BY_USER=1;
    public static final int ONLINE_PAUSE_COMMON=0;
    
    public static final String EXTRA_DOWNLOAD_URL ="fol.download.url";
    //topwise zyf add end

    public static final String ACTION_HS_DOWNLOAD_TASK = "homeshell.downlaod.task";
    public static final String ACTION_HS_DOWNLOAD_PAUSE = "homeshell.app.download.pause";
    public static final String ACTION_HS_DOWNLOAD_RUNNING = "homeshell.app.download.running";
    public static final String ACTION_HS_DOWNLOAD_CANCEL = "homeshell.app.download.cancel";

    public static final String ACTION_APP_DWONLOAD_TASK = "appstore.downlaod.task";
    public static final String ACTION_APP_DOWNLOAD_START = "app.download.start";
    public static final String ACTION_APP_DOWNLOAD_RUNNING = "app.download.running";
    // pause and fail use the same action now from appstore
    public static final String ACTION_APP_DOWNLOAD_PAUSE = "app.download.pause";
    public static final String ACTION_APP_DOWNLOAD_FAIL = "app.download.fail";
    public static final String ACTION_APP_DOWNLOAD_CANCEL = "app.download.cancel";
    public static final String ACTION_APP_APPSTORE_SERVICE = "com.tpw.appstore.appinstall.bridge";

    public static final int OP_NULL = -1;
    public static final int OP_APPSTORE_START = 0;
    public static final int OP_APPSTORE_RUNING = 1;
    public static final int OP_APPSTORE_PAUSE = 2;
    public static final int OP_APPSTORE_FAIL = 3;
    public static final int OP_APPSTORE_CANCEL = 4;

    public static class AppDownloadStatus {
        public static final int STATUS_WAITING = 0;
        public static final int STATUS_DOWNLOADING = 1;
        public static final int STATUS_PAUSED = 3;
        public static final int STATUS_INSTALLING = 4;
        public static final int STATUS_INSTALLED = 5;
        public static final int STATUS_NO_DOWNLOAD = 6;
    }

    private Context mContext;
    private IBinder mBinder;
    private LauncherModel mLauncherModel;
    private boolean mIsRequstUnBindService;
    // private ArrayList<ItemInfo> mRealRemovedList = new ArrayList<ItemInfo>();
    private AppStoreServiceConnection mServiceConn = new AppStoreServiceConnection();
    private Drawable mDefaultIcon = null;
    private ArrayList<String> mDownloadingList = new ArrayList<String>();
    int mMaxFolderXCount = ConfigManager.getFolderMaxCountX();
    int mMaxFolderCount = ConfigManager.getFolderMaxItemsCount();
    HashMap<Long, ItemInfo> mRenewFolderMap = new HashMap<Long, ItemInfo>();

    private Map<String, Long> packageDownloadCancelTimeByHS;

    public void updatepPckageDownloadCancelTimeByHS(String pkg){
        long now = System.currentTimeMillis();
        packageDownloadCancelTimeByHS.put(pkg, now);
    }

    private boolean needToDoRunning(String pkg){
        Long now = System.currentTimeMillis();
        Long then = packageDownloadCancelTimeByHS.get(pkg);
        if( then == null ) return true;
        else return false;//return now - then > 2500;
    }

    private int getDownloadType(String packagename) {
        Log.d(TAG, "AppDownload : getDownloadType begin");
        int type = this.DOWNLOAD_TYPE_DOWNLOAD;

        Collection<ItemInfo> apps = mLauncherModel.getAllAppItems();
        if (apps != null) {
            for (ItemInfo info : apps) {
                if (info instanceof ShortcutInfo) {
                    if (((ShortcutInfo)info).itemType == Favorites.ITEM_TYPE_APPLICATION) {
                        if (((ShortcutInfo) info).intent != null
                               &&((ShortcutInfo) info).intent.getComponent() != null
                               &&((ShortcutInfo) info).intent.getComponent().getPackageName() != null
                               &&((ShortcutInfo) info).intent.getComponent().getPackageName().equals(packagename)) {
                            type = DOWNLOAD_TYPE_UPDATE;
                            break;
                        }
                    }
                }
            }
        }
        Log.d(TAG, "AppDownload : getDownloadType end");
        return type;
    }

    public void appDownloadStart(Intent intent) {
        Log.d(TAG, "AppDownload : appDownloadStart begin");
        String pkgName = intent.getStringExtra(TYPE_PACKAGENAME);
        updateDownloadCount(pkgName,true);
        if (BackupManager.getInstance().isInRestore()) {
            BackupManager.getInstance().addRestoreDownloadHandledApp(pkgName);
        }
        AppGroupManager.getInstance().handleSingleApp(pkgName);
        int download_type = getDownloadType(pkgName);
        if (download_type == DOWNLOAD_TYPE_DOWNLOAD) {
            doDownloadStart(intent);
        } else if (download_type == DOWNLOAD_TYPE_UPDATE) {
            doUpdateStart(intent);
        }
        Log.d(TAG, "AppDownload : appDownloadStart end");
    }

    public void appDownloadRunning(Intent intent) {
        Log.d(TAG, "AppDownload : appDownloadRunning begin");
        String pkgName = intent.getStringExtra(TYPE_PACKAGENAME);
        int progress = intent.getIntExtra(TYPE_PROGRESS, 0);
        //app store send intent in every progress,
        //it impacts the homeshell performance to draw each progress
        //so only run update every 4 progress
        /*if ((progress > 4) && (progress%4 != 0)) {
            return;
        }*/
        if( !needToDoRunning(pkgName) ) return;
        int download_type = getDownloadType(pkgName);
        if (download_type == this.DOWNLOAD_TYPE_DOWNLOAD) {
            doDownloadRunning(intent);
        } else if (download_type == this.DOWNLOAD_TYPE_UPDATE) {
            doUpdateRunning(intent);
        }
        Log.d(TAG, "AppDownload : appDownloadRunning end");

    }

    public void appDownloadPause(Intent intent) {
        Log.d(TAG, "AppDownload : appDownloadPause begin");
        String pkgName = intent.getStringExtra(TYPE_PACKAGENAME);
        updateDownloadCount(pkgName,false);
        int download_type = getDownloadType(pkgName);
        if (download_type == this.DOWNLOAD_TYPE_DOWNLOAD) {
            doDownloadPause(intent);
        } else if (download_type == this.DOWNLOAD_TYPE_UPDATE) {
            doUpdatePause(intent);
        }
        Log.d(TAG, "AppDownload : appDownloadPause end");

    }

    public void appDownloadFail(Intent intent) {
        Log.d(TAG, "AppDownload : appDownloadFail begin");
        String pkgName = intent.getStringExtra(TYPE_PACKAGENAME);
        updateDownloadCount(pkgName,false);
        int download_type = getDownloadType(pkgName);
        if (download_type == this.DOWNLOAD_TYPE_DOWNLOAD) {
            doDownloadFail(intent);
        } else if (download_type == this.DOWNLOAD_TYPE_UPDATE) {
            doUpdateFail(intent);
        }
        removeTask(pkgName);
        Log.d(TAG, "AppDownload : appDownloadFail begin");

    }

    public void appDownloadCancel(Intent intent) {
        Log.d(TAG, "AppDownload : appDownloadCancel begin");
        String pkgName = intent.getStringExtra(TYPE_PACKAGENAME);
        updateDownloadCount(pkgName,false);
        int download_type = getDownloadType(pkgName);
        if (download_type == this.DOWNLOAD_TYPE_DOWNLOAD) {
            doDownloadCancel(intent);
        } else if (download_type == this.DOWNLOAD_TYPE_UPDATE) {
            doUpdateCancel(intent);
        }
        removeTask(pkgName);
        Log.d(TAG, "AppDownload : appDownloadCancel end");

    }

    public void appDownloadRemove(String packageName) {
        Log.d(TAG, "AppDownload : appDownloadRemove begin");
        Log.d(TAG, "appDownloadRemove packageName = " + packageName);
        if (TextUtils.isEmpty(packageName)) {
            Log.d(TAG, "appDownloadRemove, pkgName == null");
            return;
        }
        List<ShortcutInfo> shortcutinfos = this.getShortcutInfosByPackageName(
                packageName, this.DOWNLOAD_TYPE_DOWNLOAD);
        if (shortcutinfos.isEmpty() == false) {
            updateDownloadCount(packageName,false);
            for (ShortcutInfo shortcutinfo : shortcutinfos) {
                shortcutinfo.intent.setComponent(new ComponentName(packageName,"appstore"));
                this.notifyUIRemoveIcon(shortcutinfo);
            }
        }
        removeTask(packageName);
        Log.d(TAG, "AppDownload : appDownloadRemove end");
    }

    private void doDownloadStart(Intent intent) {
        Log.d(TAG, "AppDownload : doDownloadStart begin");
        boolean exist = false;
        String pkgName = intent.getStringExtra(TYPE_PACKAGENAME);
        final String appId = intent.getStringExtra(TYPE_APPID);
        if (TextUtils.isEmpty(pkgName)) {
            Log.w(TAG, "doDownloadStart, pkgName == null, return");
            return;
        }
        exist = existDownloading(pkgName);
        String iconUrl = intent.getStringExtra(TYPE_ICON_URI);
        Log.d(TAG, "doDownloadStart pkgName = " + pkgName + ", iconUrl = " + iconUrl);
        if (exist) {
            updateIconOnStart(pkgName, appId, this.DOWNLOAD_TYPE_DOWNLOAD);
            if (!TextUtils.isEmpty(iconUrl)) {
                Log.d(TAG, "doDownloadStart(): update the icon.");
                List<ShortcutInfo> shortcutinfos = this.getShortcutInfosByPackageName(pkgName,
                                this.DOWNLOAD_TYPE_DOWNLOAD);
                for (ShortcutInfo shortcutInfo : shortcutinfos) {
                    asyncUpdateIcon(shortcutInfo, iconUrl);
                }
            }
            final String finalpkgname = pkgName;
            final Runnable r = new Runnable() {
                @Override
                public void run() {
                    BackupManager.getInstance().addRestoreDoneApp(mContext, finalpkgname);
                }
            };
            LauncherModel.runOnMainThread(r);
        } else {
        	packageDownloadCancelTimeByHS.remove(pkgName);
            addIconOnStart(pkgName, iconUrl, appId);
        }

        if (mLauncherModel != null) {
            mLauncherModel.checkInstallingState();
        }

        Log.d(TAG, "AppDownload : doDownloadStart end");
    }

    private void doUpdateStart(Intent intent) {
        Log.d(TAG, "AppDownload : doUpdateStart begin");
        String pkgName = intent.getStringExtra(TYPE_PACKAGENAME);
        final String appId = intent.getStringExtra(TYPE_APPID);
        if (TextUtils.isEmpty(pkgName)) {
            Log.w(TAG, "doUpdateStart, pkgName == null, return");
            return;
        }
        String iconUrl = intent.getStringExtra(TYPE_ICON_URI);
        Log.d(TAG, "doUpdateStart pkgName = " + pkgName);
        updateIconOnStart(pkgName, appId, this.DOWNLOAD_TYPE_UPDATE);
        Log.d(TAG, "AppDownload : doUpdateStart end");
    }

    private void doDownloadRunning(Intent intent) {
        Log.d(TAG, "AppDownload : doDownloadRunning begin");
        String packageName = intent.getStringExtra(TYPE_PACKAGENAME);
        Log.d(TAG, "doDownloadRunning packageName = " + packageName);
        int progress = intent.getIntExtra(TYPE_PROGRESS, 0);
        Log.d(TAG, "doDownloadRunning : progress = " + progress);
        if (TextUtils.isEmpty(packageName)) {
            Log.d(TAG, "doDownloadRunning, pkgName == null");
            return;
        }

        List<ShortcutInfo> shortcutinfos = this.getShortcutInfosByPackageName(
                packageName, this.DOWNLOAD_TYPE_DOWNLOAD);

        if (shortcutinfos.isEmpty()) {// maybe the homeshell killed, and
            Log.d(TAG, "doDownloadRunning do not find package : " + packageName
                    + " progress : " + progress);
            if (isDownloadingByProgress(progress)) {
                updateDownloadCount(packageName,false);
                appDownloadStart(intent);
            }
            return;
        } else {
            if(shortcutinfos.get(0).getAppDownloadStatus()==AppDownloadStatus.STATUS_NO_DOWNLOAD){
                this.updateDownloadCount(packageName,true);
            }
            for (ShortcutInfo shortcutinfo : shortcutinfos) {
                this.updateShortcutInfo(shortcutinfo,
                        AppDownloadStatus.STATUS_DOWNLOADING, progress);
                this.notifyUIUpdateIcon(shortcutinfo);
            }
        }
        Log.d(TAG, "AppDownload : doDownloadRunning end");
    }

    private void doUpdateRunning(Intent intent) {
        Log.d(TAG, "AppDownload : doUpdateRunning begin");
        String packageName = intent.getStringExtra(TYPE_PACKAGENAME);
        Log.d(TAG, "doUpdateRunning packageName = " + packageName);
        int progress = intent.getIntExtra(TYPE_PROGRESS, 0);
        if (TextUtils.isEmpty(packageName)) {
            Log.d(TAG, "doUpdateRunning, pkgName == null");
            return;
        }
        List<ShortcutInfo> shortcutinfos = this.getShortcutInfosByPackageName(
                packageName, this.DOWNLOAD_TYPE_UPDATE);

        if (shortcutinfos.isEmpty()) {
            Log.d(TAG, "doUpdateRunning do not find package : " + packageName
                    + " progress : " + progress + "and return");
             updateDownloadCount(packageName,false);
            return;
        } else {
            if(shortcutinfos.get(0).getAppDownloadStatus()==AppDownloadStatus.STATUS_NO_DOWNLOAD){
                this.updateDownloadCount(packageName,true);
            }
            for (ShortcutInfo shortcutinfo : shortcutinfos) {
                this.updateShortcutInfo(shortcutinfo,
                        AppDownloadStatus.STATUS_DOWNLOADING, progress);
                this.notifyUIUpdateIcon(shortcutinfo);
            }
        }
        Log.d(TAG, "AppDownload : doUpdateRunning end");
    }

    private void doDownloadPause(Intent intent) {
        Log.d(TAG, "AppDownload : doDownloadPause begin");
        String packageName = intent.getStringExtra(TYPE_PACKAGENAME);
        Log.d(TAG, "doDownloadPause packageName = " + packageName);
        int progress = intent.getIntExtra(TYPE_PROGRESS, 0);
        if (TextUtils.isEmpty(packageName)) {
            Log.d(TAG, "doDownloadPause, pkgName == null");
            return;
        }
        List<ShortcutInfo> shortcutinfos = this.getShortcutInfosByPackageName(
                packageName, this.DOWNLOAD_TYPE_DOWNLOAD);

        if (shortcutinfos.isEmpty() == false) {
            for (ShortcutInfo shortcutinfo : shortcutinfos) {
                updateShortcutInfo(shortcutinfo, AppDownloadStatus.STATUS_PAUSED);
                this.notifyUIUpdateIcon(shortcutinfo);
            }
        }
        Log.d(TAG, "AppDownload : doDownloadPause end");
    }

    private void doUpdatePause(Intent intent) {
        Log.d(TAG, "AppDownload : doUpdatePause begin");
        String packageName = intent.getStringExtra(TYPE_PACKAGENAME);
        Log.d(TAG, "doUpdatePause packageName = " + packageName);
        int progress = intent.getIntExtra(TYPE_PROGRESS, 0);
        if (TextUtils.isEmpty(packageName)) {
            Log.d(TAG, "doUpdatePause, pkgName == null");
            return;
        }
        List<ShortcutInfo> shortcutinfos = this.getShortcutInfosByPackageName(
                packageName, this.DOWNLOAD_TYPE_UPDATE);

        if (shortcutinfos.isEmpty() == false && shortcutinfos.get(0).isDownloading() ) {
            for (ShortcutInfo shortcutinfo : shortcutinfos) {
                updateShortcutInfo(shortcutinfo, AppDownloadStatus.STATUS_PAUSED);
                this.notifyUIUpdateIcon(shortcutinfo);
            }
        }
        Log.d(TAG, "AppDownload : doUpdatePause end");
    }

    // pause and fail is the same from appstore
    private void doDownloadFail(Intent intent) {
        Log.d(TAG, "AppDownload : doDownloadFail begin");
        doDownloadPause(intent);
        Log.d(TAG, "AppDownload : doDownloadFail end");
    }

    // pause and fail is the same from appstore
    private void doUpdateFail(Intent intent) {
        Log.d(TAG, "AppDownload : doUpdateFail begin");
        doUpdatePause(intent);
        Log.d(TAG, "AppDownload : doUpdateFail end");
    }

    private void doDownloadCancel(Intent intent) {
        Log.d(TAG, "AppDownload : doDownloadCancel begin");
        String packageName = intent.getStringExtra(TYPE_PACKAGENAME);
        Log.d(TAG, "doDownloadCancel packageName = " + packageName);
        if (TextUtils.isEmpty(packageName)) {
            Log.d(TAG, "doDownloadCancel, pkgName == null");
            return;
        }
        List<ShortcutInfo> shortcutinfos = this.getShortcutInfosByPackageName(
                packageName, this.DOWNLOAD_TYPE_DOWNLOAD);
        if (shortcutinfos.isEmpty() == false) {
            for (ShortcutInfo shortcutinfo : shortcutinfos) {
                shortcutinfo.intent.setComponent(new ComponentName(packageName,
                        "appstore"));
                // removeAppFromModel(shortcutinfo);
                this.notifyUIRemoveIcon(shortcutinfo);
            }
        }
        Log.d(TAG, "AppDownload : doDownloadCancel end");
    }

    private void doUpdateCancel(Intent intent) {
        Log.d(TAG, "AppDownload : doUpdateCancel begin");

        String packageName = intent.getStringExtra(TYPE_PACKAGENAME);
        Log.d(TAG, "doUpdateCancel packageName = " + packageName);
        if (TextUtils.isEmpty(packageName)) {
            Log.d(TAG, "doUpdateCancel, pkgName == null");
            return;
        }
        List<ShortcutInfo> shortcutinfos = this.getShortcutInfosByPackageName(
                packageName, this.DOWNLOAD_TYPE_UPDATE);

        if (shortcutinfos.isEmpty() == false) {
            for (ShortcutInfo shortcutinfo : shortcutinfos) {
                updateShortcutInfo(shortcutinfo, AppDownloadStatus.STATUS_INSTALLED);
                this.notifyUIUpdateIcon(shortcutinfo);
            }
        }

        Log.d(TAG, "AppDownload : doUpdateCancel end");
    }

    private void updateIconOnStart(String pkgname, String appId,
            int downloadtype) {
        Log.d(TAG, "AppDownload : updateIconOnStart begin");
        List<ShortcutInfo> shortcutinfos = this.getShortcutInfosByPackageName(pkgname,
                downloadtype);
        if (shortcutinfos.isEmpty() == false) {
            for (ShortcutInfo shortcutinfo : shortcutinfos) {
                shortcutinfo.setAppDownloadStatus(AppDownloadStatus.STATUS_WAITING);
                this.notifyUIUpdateIcon(shortcutinfo);
            }
        }
        Log.d(TAG, "AppDownload : updateIconOnStart end");
    }

    private ItemInfo createFolderForDownloadItem(long oldid) {
        Log.d(TAG, "createFolderForDownloadItem in");
        FolderInfo newitem = null;
        BackupRecord record = BackupUitil.getBackupRecordById(oldid);
        ScreenPosition realPosition = null;
        if (record != null) {
            int oldContainer = Integer.parseInt(record.getField(Favorites.CONTAINER));
            int oldCellX = Integer.parseInt(record.getField(Favorites.CELLX));
            int oldCellY = Integer.parseInt(record.getField(Favorites.CELLY));
            int oldScreen = Integer.parseInt(record.getField(Favorites.SCREEN));
            String oldTitle = record.getField(Favorites.TITLE);

            if (oldContainer == Favorites.CONTAINER_DESKTOP) {
                Log.d(TAG, "old folder in desktop");
                realPosition = LauncherModel.isCellEmtpy(oldScreen, oldCellX, oldCellY);
                if (realPosition == null) {
                    Log.d(TAG, "the old postion is occupied, find new position");
                    realPosition = LauncherModel.findEmptyCell();
                }

                if (realPosition != null) {
                    Log.d(TAG, "new position is screen:" + realPosition.s + " x:" + realPosition.x
                                      + " y:" + realPosition.y);
                    newitem = new FolderInfo();
                    newitem.cellX = realPosition.x;
                    newitem.cellY = realPosition.y;
                    newitem.screen = realPosition.s;
                    newitem.container = Favorites.CONTAINER_DESKTOP;
                } else {
                    Log.d(TAG, "no postion for new folder is found");
                }
            } else if (oldContainer == Favorites.CONTAINER_HOTSEAT) {
                Log.d(TAG, "old folder in hotseat");
                //find the position in hotseat
                int position = LauncherModel.getHotSeatPosition(oldScreen);
                if (position < ConfigManager.getHotseatMaxCount()) {
                    newitem = new FolderInfo();
                    newitem.cellX = position;
                    newitem.cellY = oldCellY;
                    newitem.screen = position;
                    newitem.container = Favorites.CONTAINER_HOTSEAT;
                } else {
                    //hotseat is full, put the folder on workspace
                    realPosition = LauncherModel.findEmptyCell();
                    if (realPosition != null) {
                        newitem = new FolderInfo();
                        newitem.cellX = realPosition.x;
                        newitem.cellY = realPosition.y;
                        newitem.screen = realPosition.s;
                        newitem.container = Favorites.CONTAINER_DESKTOP;
                    } else {
                        Log.d(TAG, "no postion for new folder is found");
                    }
                }
            } else if (oldContainer == Favorites.CONTAINER_HIDESEAT) {
                //folder in hideseat isn't supported, so don't need to handle this case
                Log.d(TAG, "folder in hideseat!? it is impossible.");
            }
            if (newitem != null) {
                newitem.title = oldTitle;
            }
        }
        if (newitem != null) {
            Log.d(TAG, "newitem isn't null, before add to db");
            LauncherModel.addItemToDatabase(mContext, newitem, newitem.container, newitem.screen, newitem.cellX, newitem.cellY, false);
            Log.d(TAG, "newitem isn't null, after add to db");
            final Callbacks callbacks = LauncherModel.mCallbacks != null ? LauncherModel.mCallbacks.get() : null;
            final ArrayList<ItemInfo> infos = new ArrayList<ItemInfo>(1);
            infos.add(newitem);

            //final Callbacks callbacks = LauncherModel.mCallbacks != null ? LauncherModel.mCallbacks.get() : null;
            final HashMap<Long, FolderInfo> folderinfos = new HashMap<Long, FolderInfo>(1);
            folderinfos.put(newitem.id, (FolderInfo)newitem);
            final Runnable r = new Runnable() {
                @Override
                public void run() {
                    if (callbacks != null) {
                        callbacks.bindItemsAdded(infos);
                        callbacks.bindFolders(folderinfos);
                    }
                }
            };
            LauncherModel.runOnMainThread(r);
            mRenewFolderMap.put(oldid, newitem);
        }
        Log.d(TAG, "createFolderForDownloadItem out");
        return newitem;
    }
    private ItemInfo findOrCreateFolderForDownloadItem(long container) {
        Log.d(TAG, "findOrCreateFolderForDownloadItem in");
        ItemInfo iteminfo = mRenewFolderMap.get(container);

        if (iteminfo == null) {
            Log.d(TAG, "need create a new folder");
            iteminfo = createFolderForDownloadItem(container);
        }
        Log.d(TAG, "findOrCreateFolderForDownloadItem out");
        return iteminfo;
    }

    private ScreenPosition getPosition(BackupRecord record, long container) {
        int screen = Integer.parseInt(record.getField(Favorites.SCREEN));
        int cellX = Integer.parseInt(record.getField(Favorites.CELLX));
        int cellY = Integer.parseInt(record.getField(Favorites.CELLY));

        ScreenPosition p = null;
        if (container == Favorites.CONTAINER_DESKTOP) {
            p = LauncherModel.isCellEmtpy(screen, cellX, cellY);
        } else if (container > 0) {
            Log.d(TAG, "it is in a folder, folder id is " + container);
            ItemInfo modelItem = null;

            modelItem = LauncherModel.getSBgItemsIdMap().get(container);
            if (modelItem == null || modelItem.itemType != Favorites.ITEM_TYPE_FOLDER || ((FolderInfo) modelItem).contents == null
                            || ((FolderInfo) modelItem).contents.size() >= mMaxFolderCount) {
                Log.d(TAG, "can't find the folder id, create the folder");
                modelItem = findOrCreateFolderForDownloadItem(container);
            }

            if (modelItem != null){
                Log.d(TAG, "new folder id container is " + modelItem.id);
                record.setField(Favorites.CONTAINER, String.valueOf(modelItem.id));

                //check and find a position in folder
                boolean isExist = false;
                for(ItemInfo item: ((FolderInfo)modelItem).contents) {
                    if ((item.cellX == cellX) && (item.cellY == cellY)){
                        isExist = true;
                        break;
                    }
                }

                if (isExist == true) {
                    if (((FolderInfo)modelItem).contents.size() < mMaxFolderCount) {
                        cellX = ((FolderInfo)modelItem).contents.size() % mMaxFolderXCount;
                        cellY = ((FolderInfo)modelItem).contents.size() / mMaxFolderXCount;
                        //cellX = maxX + 1;
                        p = new ScreenPosition(0, cellX, cellY);
                    }
                } else {
                    p = new ScreenPosition(0, cellX, cellY);
                }
            } else {
                Log.d(TAG, "find or create new folder failed");
            }
        } else if (container == Favorites.CONTAINER_HOTSEAT) {
            Log.d(TAG, "restore a hotseat item, item.screen is " + screen);
            //the app in dock must be restored in it's orig position
            int position = LauncherModel.getHotSeatPosition(screen);
            Log.d(TAG, "new position is " + position);
            if (position < ConfigManager.getHotseatMaxCount()) {
                p = new ScreenPosition(position, position, cellY);
            }
        }
        else if (container == Favorites.CONTAINER_HIDESEAT) {
            p = LauncherModel.getHideSeatPosition(screen, cellX, cellY);
        }

        if (p != null)
            Log.d(TAG, "position[screen" + screen + "][x " + cellX + "]["
                    + cellY + "] is not empty");
        else
            Log.d(TAG, "position[screen" + screen + "][x" + cellX + "]["
                    + cellY + "] is empty");

        return p;
    }

    private void addIconOnStart(String pkgName, String iconUrl, String appId) {
        Log.d(TAG, "AppDownload : addIconOnStart begin");
        ScreenPosition screenPosition = null;
        long container = LauncherSettings.Favorites.CONTAINER_DESKTOP;
        try {
            // if in restore, we restore the position information
            if (BackupManager.getInstance().isInRestore()) {
                Log.d(TAG,
                        "in restore, we restore the position information, pkgName = "
                                + pkgName);
                BackupRecord record = BackupUitil.getBackupRecord(pkgName);
                if (record != null) {
                    container = Long.parseLong(record.getField(Favorites.CONTAINER));
                    screenPosition = getPosition(record, container);
                    Log.d(TAG, "container before relog " + container);
                    container = Long.parseLong(record.getField(Favorites.CONTAINER));
                    Log.d(TAG, "container after relog " + container);
                }
            }
        } catch (Exception e) {
            screenPosition = null;
            container = LauncherSettings.Favorites.CONTAINER_DESKTOP;
        }

        if (screenPosition == null) {
            Collection<ItemInfo> allapps = LauncherModel.getAllAppItems();
            if (allapps != null) {
                for (ItemInfo info : allapps) {
                    if (info.itemType == Favorites.ITEM_TYPE_VPINSTALL) {
                        String vpPkgName = ((ShortcutInfo)info).intent.getStringExtra(TYPE_PACKAGENAME);
                        if ((vpPkgName != null) && (vpPkgName.equals(pkgName))) {
                            Log.d(TAG, "find vpinstall item");
                            //put the download item on this vp install item's position
                            screenPosition = new ScreenPosition(info.screen, info.cellX, info.cellY);
                            container = info.container;
                            //remove the vp install item
                            ((ShortcutInfo)info).intent.setComponent(new ComponentName(pkgName,
                                    "appstore"));
                            notifyUIRemoveIcon((ShortcutInfo)info);
                            break;
                        }
                    }
                }
            }
        }

        if (screenPosition == null) {
            container = LauncherSettings.Favorites.CONTAINER_DESKTOP;
            screenPosition = LauncherModel.findEmptyCell();
            if (screenPosition == null) {
                updateDownloadCount(pkgName,false);
                Log.d(TAG, "addIconOnStart, screenPositon == null");
                return;
            }
        }

        createShortcutInfoAndAddToScreen(pkgName, iconUrl, appId,
                screenPosition, container);

        Log.d(TAG, "AppDownload : addIconOnStart end");
    }

    private void createShortcutInfoAndAddToScreen(String pkgName,
            String iconUrl, String appId, ScreenPosition screenPosition,
            long containerType) {
        Log.d(TAG, "AppDownload : createShortcutInfoAndAddToScreen begin");
        ShortcutInfo shortcutinfo = this.createDownloadShortcutInfo(pkgName,
                iconUrl, appId, screenPosition, containerType);
        addAppToModel(shortcutinfo, containerType);

        Drawable orgIcon = shortcutinfo.mIcon;
        shortcutinfo.setIcon(((LauncherApplication)mContext.getApplicationContext()).getIconManager().buildUnifiedIcon(orgIcon));

        this.addShortcutToScreen(shortcutinfo, iconUrl);

        if (containerType == LauncherSettings.Favorites.CONTAINER_HOTSEAT) {
            LauncherModel.rebindHotseat(mContext);
        }
        Log.d(TAG, "AppDownload : createShortcutInfoAndAddToScreen end");

    }

    private ShortcutInfo createDownloadShortcutInfo(String pkgName,
            String iconUrl, String appId, ScreenPosition screenPosition,
            long containerType) {
        Log.d(TAG, "AppDownload : createDownloadShortcutInfo begin");

        ShortcutInfo shortcutInfo = new ShortcutInfo();// (LauncherSettings.Favorites.ITEM_TYPE_SHORTCUT_DOWNLOADING,
        // pkgName);
        // ---- need new intent, not do ---
        shortcutInfo.title = mContext.getString(R.string.paused);
        shortcutInfo.intent = new Intent();
        shortcutInfo.intent.putExtra(TYPE_PACKAGENAME, pkgName);
        shortcutInfo.itemType = Favorites.ITEM_TYPE_SHORTCUT_DOWNLOADING;
        shortcutInfo.customIcon = true;
        shortcutInfo.screen = screenPosition.s;
        shortcutInfo.cellX = screenPosition.x;
        shortcutInfo.cellY = screenPosition.y;
        shortcutInfo.container = LauncherSettings.Favorites.CONTAINER_DESKTOP;
        shortcutInfo.mDownloadType = DOWNLOAD_TYPE_DOWNLOAD;
        if (containerType == Favorites.CONTAINER_HOTSEAT) {
            shortcutInfo.container = LauncherSettings.Favorites.CONTAINER_HOTSEAT;
        } else {
            shortcutInfo.container = LauncherSettings.Favorites.CONTAINER_DESKTOP;
        }
        shortcutInfo.setIcon(mDefaultIcon);

        shortcutInfo.setAppDownloadStatus(AppDownloadStatus.STATUS_WAITING);
        // shortcutInfo.title = shortcutInfo.getItemTitle();
        shortcutInfo.setAppId(appId);
        Log.d(TAG, "AppDownload : createDownloadShortcutInfo end");
        return shortcutInfo;
    }

    private void addShortcutToScreen(ShortcutInfo shortcutInfo, String iconUrl) {
        Log.d(TAG, "AppDownload : addShortcutToScreen begin");
             //don't add the shortcutinfo to screen if it is in folder,
        //it will be added in launchermodel
        if (shortcutInfo.container < 0) {
            ArrayList<ItemInfo> addApps = new ArrayList<ItemInfo>();
            addApps.add(shortcutInfo);
            notifyUIAddIcon(addApps);
        }
        else {
            String pkgname = ((ShortcutInfo)shortcutInfo).getPackageName();
            BackupManager.getInstance().addRestoreDoneApp(mContext, pkgname);
        }
        if (!TextUtils.isEmpty(iconUrl)) {
            asyncUpdateIcon(shortcutInfo, iconUrl);
        }
        Log.d(TAG, "AppDownload : addShortcutToScreen end");
    }

    private void asyncUpdateIcon(final ShortcutInfo appItem, String iconUrl) {
        Log.d(TAG, "AppDownload : asyncUpdateIcon begin");
        if (TopwiseConfig.YUNOS_CTA_SUPPORT) {
            return;
        }
        Utils.asyncGetBitmapFromRemoteUri(iconUrl, new DownloadTaskHandler() {
            @Override
            public void onDownloadFinished(final Bitmap iconBmp) {
                Log.d(TAG, "AppDownload : asyncUpdateIcon : icon download finish "
                           + (iconBmp == null ? "(icon=null)" : "successfully"));
                if (iconBmp == null ||
                    iconBmp.getWidth() <= 0 || iconBmp.getHeight() <= 0) {
                    return;
                }
                mLauncherModel.post(new Runnable() {
                    @Override
                    public void run() {
                        BitmapDrawable bd = new BitmapDrawable(mContext.getResources(), iconBmp);
                        Drawable origicon = bd.getCurrent();
                        Drawable icon = ((LauncherApplication)mContext.getApplicationContext()).getIconManager().buildUnifiedIcon(origicon);

                        appItem.setIcon(icon);

                        updateAppToModel(appItem);
                        saveOriginalIconToDB(appItem.id, origicon);
                        mLauncherModel.notifyUIUpdateDownloadIcon(appItem);
                    }
                });
            }
        });
        Log.d(TAG, "AppDownload : asyncUpdateIcon end");
    }

    private void updateShortcutInfo(ShortcutInfo shortcutinfo, int status,
            int progress) {
        Log.d(TAG, "AppDownload : updateShortcutInfo begin");
        shortcutinfo.setAppDownloadStatus(status);
        if (progress != DOWNLOAD_INVALID_PROGRESS) {
            shortcutinfo.setProgress(progress);
        }
        if (isDownloadComplete(progress)) {
            shortcutinfo
                    .setAppDownloadStatus(AppDownloadStatus.STATUS_INSTALLING);
        }
        Log.d(TAG, "AppDownload : updateShortcutInfo end");
    }

    private void updateShortcutInfo(ShortcutInfo shortcutinfo, int status) {
        updateShortcutInfo(shortcutinfo, status, DOWNLOAD_INVALID_PROGRESS);
    }

    public List<ShortcutInfo> getShortcutInfosByPackageName(String pkgname,
            int downloadtype) {
        Log.d(TAG, "AppDownload : getShortcutInfosByPackageName begin");
        List<ShortcutInfo> shortcutinfos = new ArrayList<ShortcutInfo>(1);
        Collection<ItemInfo> apps = this.mLauncherModel.getAllAppItems();
        // ---- need all items include folders ----
        for (ItemInfo info : apps) {
            if (info instanceof ShortcutInfo) {
                String packagename = null;
                Intent intent = ((ShortcutInfo) info).intent;
                if (intent == null) {
                    continue;
                }
                if (downloadtype == this.DOWNLOAD_TYPE_DOWNLOAD) {
                    if (info.itemType == LauncherSettings.Favorites.ITEM_TYPE_SHORTCUT_DOWNLOADING) {
                        packagename = intent.getStringExtra(TYPE_PACKAGENAME);
                    }
                } else {
                    if ((info.itemType == LauncherSettings.Favorites.ITEM_TYPE_APPLICATION)
                         && (intent.getComponent() != null)) {
                        packagename = intent.getComponent().getPackageName();
                    }
                }
                if (packagename != null && packagename.equals(pkgname)) {
                    shortcutinfos.add((ShortcutInfo) info);
                }
            }
        }
        Log.d(TAG, "AppDownload : getShortcutInfosByPackageName end");
        return shortcutinfos;
    }

    public List<ShortcutInfo> getShortcutInfosByPackageName(String pkgname) {
        List<ShortcutInfo> shortcutinfos = new ArrayList<ShortcutInfo>(1);
        Collection<ItemInfo> apps = LauncherModel.getAllAppItems();
        for (ItemInfo info : apps) {
            Log.d(TAG,
                    "getShortcutInfoByPackageName : apps = " + info.toString());
            String packagename = null;
            Intent intent = null;
            if (info instanceof ShortcutInfo) {
                intent = ((ShortcutInfo) info).intent;
                if (intent == null) {
                    continue;
                }
                packagename = intent.getStringExtra(TYPE_PACKAGENAME);
                if (packagename == null) {
                    packagename = intent.getComponent().toString();
                }
                if (packagename != null && packagename.equals(pkgname)) {
                    shortcutinfos.add((ShortcutInfo) info);
                }
            }
        }
        return shortcutinfos;
    }

    private void notifyUIAddIcon(ArrayList<ItemInfo> addApps) {
        Log.d(TAG, "AppDownload : notifyUIAddIcon begin");
        this.mLauncherModel.notifyUIAddIcon(addApps);
        Log.d(TAG, "AppDownload : notifyUIAddIcon end");
    }

    private void notifyUIUpdateIcon(ShortcutInfo appItem) {
        notifyUIUpdateIcon(appItem,false);
    }

    private void notifyUIUpdateIcon(ShortcutInfo appItem,boolean updateModeData) {
        Log.d(TAG, "AppDownload : notifyUIUpdateIcon begin");
        ArrayList<ItemInfo> apps = new ArrayList<ItemInfo>();
        apps.add(appItem);
        if (updateModeData) {
            this.updateAppToModel(appItem);
        }
        this.mLauncherModel.notifyUIUpdateIcon(apps);
        Log.d(TAG, "AppDownload : notifyUIUpdateIcon end");
    }


    private void notifyUIRemoveIcon(ShortcutInfo appItem) {
        Log.d(TAG, "AppDownload : notifyUIRemoveIcon begin");
        ArrayList<ItemInfo> apps = new ArrayList<ItemInfo>();
        apps.add(appItem);
        this.mLauncherModel.notifyUIRemoveIcon(apps, true, true);
        Log.d(TAG, "AppDownload : notifyUIRemoveIcon end");
    }

    private void addAppToModel(ShortcutInfo shortcutinfo, long containerType) {
        Log.d(TAG, "AppDownload : addAppToModel begin");
        LauncherModel.addItemToDatabase(mContext, shortcutinfo, containerType,
                shortcutinfo.screen, shortcutinfo.cellX, shortcutinfo.cellY,
                false);
        Log.d(TAG, "AppDownload : addAppToModel end");
    }

    private void updateAppToModel(ShortcutInfo shortcutinfo) {
        Log.d(TAG, "AppDownload : updateAppToModel begin");
        LauncherModel.updateItemInDatabaseForAppDownload(mContext,
                shortcutinfo);
        Log.d(TAG, "AppDownload : updateAppToModel end");

    }

    private void saveOriginalIconToDB(long itemId,  Drawable orginIcon) {
        final ContentValues contentValues = new ContentValues();
        ItemInfo.writeBitmap(contentValues, orginIcon);
        LauncherModel.updateItemById(mContext, itemId, contentValues, false);
    }

    private boolean isDownloadingByProgress(int progress) {
        Log.d(TAG, "AppDownload : isDownloadingByProgress begin");
        boolean ret = false;
        if (progress > DOWNLOAD_NOT_START_PROGRESS
                && progress < DOWNLOAD_COMPLETE_PROGRESS) {
            ret = true;
        }
        Log.d(TAG, "AppDownload : isDownloadingByProgress end");
        return ret;
    }

    private boolean isDownloadComplete(int progress) {
        Log.d(TAG, "AppDownload : isDownloadComplete begin");
        boolean ret = false;
        if (progress == DOWNLOAD_COMPLETE_PROGRESS) {
            ret = true;
        }
        Log.d(TAG, "AppDownload : isDownloadComplete end");
        return ret;
    }

    public void bindAppStoreService() {
        if (DEBUG) {
            Log.i(TAG, "bindAppStoreService, start");
        }
        try {
            mContext.bindService(new Intent(ACTION_APP_APPSTORE_SERVICE),
                    mServiceConn, Context.BIND_AUTO_CREATE);
        } catch (Exception e) {
            Log.e(TAG, "bindAppStoreService Error ");
        }
    }

    public void unBindAppStoreService() {
        if (mBinder != null) {
            mContext.unbindService(mServiceConn);
            mIsRequstUnBindService = true;
        }
    }

    private class AppStoreServiceConnection implements ServiceConnection {

        @Override
        public void onServiceConnected(ComponentName className,
                IBinder boundService) {
            mBinder = boundService;
            if (DEBUG) {
                Log.i(TAG, "onServiceConnected, mBinder = " + boundService);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            if (DEBUG) {
                Log.i(TAG, "onServiceDisconnected ");
            }
            mBinder = null;
            if (!mIsRequstUnBindService) {
                syncAppDownloadIcon();
            }
        }

    }

    public void syncAppDownloadIcon() {
        Log.d(TAG, "AppDownload : syncAppDownloadIcon begin");
        List<ShortcutInfo> appDownloadApps = getAllDownloadingShortcutInfos();// getAppDownloadingByPackageName("");
        if (appDownloadApps.size() == 0) {
            return;
        }
        for (ShortcutInfo shortcutinfo : appDownloadApps) {
            if(shortcutinfo.getAppDownloadStatus() == AppDownloadStatus.STATUS_WAITING || shortcutinfo.getAppDownloadStatus() == AppDownloadStatus.STATUS_DOWNLOADING){
                updateShortcutInfo(shortcutinfo, AppDownloadStatus.STATUS_PAUSED);

                String pkgname = null;
                if (shortcutinfo.intent != null) {
                    pkgname = shortcutinfo.intent.getStringExtra(TYPE_PACKAGENAME);
                }
                if (pkgname != null) {
                    updateDownloadCount(pkgname, false);
                }
                this.notifyUIUpdateIcon(shortcutinfo);
            }
        }
        appDownloadApps.clear();
        Log.d(TAG, "AppDownload : syncAppDownloadIcon end");

    }
    public ArrayList<ShortcutInfo> getAllDownloadingShortcutInfos() {
        Log.d(TAG, "AppDownload : getAllDownloadingShortcutInfos begin");
        ShortcutInfo shortcutinfo = null;
        Collection<ItemInfo> apps = this.mLauncherModel.getAllAppItems();
        ArrayList<ShortcutInfo> downloadlist = new ArrayList<ShortcutInfo>();
        for (ItemInfo info : apps) {
            Log.d(TAG,
                    "AppDownload : getAllDownloadingShortcutInfos appsinfo : "
                            + info.toString());
            if (info instanceof ShortcutInfo) {
                shortcutinfo = (ShortcutInfo) info;
                if (shortcutinfo.itemType == LauncherSettings.Favorites.ITEM_TYPE_SHORTCUT_DOWNLOADING
                        ||shortcutinfo.getAppDownloadStatus() == AppDownloadStatus.STATUS_DOWNLOADING
                        || shortcutinfo.getAppDownloadStatus() == AppDownloadStatus.STATUS_WAITING
                        || shortcutinfo.getAppDownloadStatus() == AppDownloadStatus.STATUS_INSTALLING
                        || shortcutinfo.getAppDownloadStatus() == AppDownloadStatus.STATUS_PAUSED
                        || shortcutinfo.getAppDownloadStatus() == AppDownloadStatus.STATUS_INSTALLED) {
                    downloadlist.add(shortcutinfo);
                }
            }
        }
        Log.d(TAG, "AppDownload : getAllDownloadingShortcutInfos end");
        return downloadlist;
    }

    public boolean isDownloadStatus() {
        boolean ret = false;
        if(mDownloadingList!=null){
            ret = mDownloadingList.size() > 0;
        }
        return ret;
    }

    public synchronized void updateDownloadCount(String packagename, boolean add){
        Log.d(TAG,"updateDownloadCount, packagename = "+packagename);
        if(TextUtils.isEmpty(packagename)){
            Log.d(TAG,"updateDownloadCount, packagename is null");
            return;
        }
        if(add){
            if(!mDownloadingList.contains(packagename)){
                mDownloadingList.add(packagename);
                Log.d(TAG,"updateDownloadCount, added packagename = "+packagename);
            }
        }else{
            if(mDownloadingList.contains(packagename)){
                mDownloadingList.remove(packagename);
                Log.d(TAG,"updateDownloadCount, removed packagename = "+packagename);
            }
        }
        Log.d(TAG,"mDownloadCount : "+mDownloadingList.size());
    }
    public boolean existDownloading(String pkgname) {
        Log.d(TAG, "AppDownload : existDownloading begin");
        if (TextUtils.isEmpty(pkgname)) {
            Log.w(TAG, "doUpdateStart, pkgName == null, return");
            return false;
        }
        boolean exist = false;
        String packagename = null;
        ArrayList<ShortcutInfo> apps = getAllDownloadingShortcutInfos();
        for (ShortcutInfo info : apps) {
            Log.d(TAG, "existDownloading : info = " + info.toString());
            packagename = info.intent.getStringExtra(TYPE_PACKAGENAME);
            if (pkgname.equals(packagename)) {
                exist = true;
                break;
            }
        }
        Log.d(TAG, "AppDownload : existDownloading : exist = " + exist);
        Log.d(TAG, "AppDownload : existDownloading end");
        return exist;
    }
    
    private static AppDownloadManager instance = new AppDownloadManager();
    private Map<String, AppDownloadTask> mMapDownloadTasks;
    private PackageStateReceiver mPackageStateReceiver;
    public static AppDownloadManager getInstance() {
        return instance;
    }
    
    public void setup(Context context, LauncherModel model,
            DeferredHandler handler) {
        mContext = context;
        mLauncherModel = model;
        mIsRequstUnBindService = false;
        mDefaultIcon = ((LauncherApplication)mContext.getApplicationContext()).getIconManager().getDefaultIcon();
        Log.d(TAG, "mMaxFolderXCount = " + mMaxFolderXCount + ", mMaxFolderCount = " + mMaxFolderCount);
        packageDownloadCancelTimeByHS = new HashMap<String, Long>();
    }
    
    private AppDownloadManager() {
        mMapDownloadTasks = Collections.synchronizedMap(new HashMap<String, AppDownloadTask>());
        mPackageStateReceiver = new PackageStateReceiver();
    }
    
    public BroadcastReceiver getPackageStateReceiver() {
        return mPackageStateReceiver;
    }
    
    public synchronized AppDownloadTask getAppDownloadTask(int opType, Intent intent) {
        if(intent == null || opType == OP_NULL) {
            return null;
        }
        String pkgName = intent.getStringExtra(TYPE_PACKAGENAME);
        if(TextUtils.isEmpty(pkgName)) {
            return null;
        }
        AppDownloadTask task = mMapDownloadTasks.get(pkgName);
        if(task == null) {
            task = new AppDownloadTask(opType, intent);
            mMapDownloadTasks.put(pkgName, task);
        } else {
            task.setOp(opType);
            task.setIntent(intent);
        }
        Log.d(TAG, "sxsexe9654------> AppDownloadTask cache count : " + mMapDownloadTasks.size());
        return task;
    }
    
    public void removeTask(String pkgName) {
        if(mMapDownloadTasks != null && mMapDownloadTasks.containsKey(pkgName)) {
            mMapDownloadTasks.remove(pkgName);
        }
        Log.d(TAG, "sxsexe9654------> AppDownloadTask cache count : " + mMapDownloadTasks.size());
    }
    public void clearTask() {
        if(mMapDownloadTasks != null) {
            mMapDownloadTasks.clear();
        }
        Log.d(TAG, "sxsexe9654------> AppDownloadTask cache count : " + mMapDownloadTasks.size());
    }
    
    private class PackageStateReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            final String packageName = intent.getData().getSchemeSpecificPart();
            if(TextUtils.isEmpty(packageName)) {
                return;
            }
            if (Intent.ACTION_PACKAGE_CHANGED.equals(action)
                    || Intent.ACTION_PACKAGE_REMOVED.equals(action)
                    || Intent.ACTION_PACKAGE_ADDED.equals(action)) {
                removeTask(packageName);
            }
        }
    }

    private class AppDownloadTask implements Runnable {

        private int mOp;
        private Intent mIntent;
        private int mLastProgress;
        private int mCurrentProgress;

        public AppDownloadTask(int op, Intent intent) {
            mOp = op;
            mIntent = intent;
        }
        public void setOp(int op) {
            this.mOp = op;
        }
        public void setIntent(Intent intent) {
            mCurrentProgress = intent.getIntExtra(TYPE_PROGRESS, 0);
            this.mIntent = intent;
        }

        @Override
        public void run() {
            switch (mOp) {
            case OP_APPSTORE_START: // app store download start
                Log.d(TAG, "AppDownload : AppDownloadTask START");
                bindAppStoreService();
                appDownloadStart(mIntent);
                break;
            case OP_APPSTORE_RUNING: // app store download downloading
                Log.d(TAG, "sxsexe9654------>AppDownload : AppDownloadTask RUNNING lastProgress" + mLastProgress
                        + " mCurrentProgress " + mCurrentProgress
                        + " pkg " + mIntent.getStringExtra(TYPE_PACKAGENAME));
                /*if((mCurrentProgress > 0 && (mCurrentProgress - mLastProgress) >=5) || mCurrentProgress == 100) {
                    mLastProgress = mCurrentProgress;
                    appDownloadRunning(mIntent);
                }*/
                appDownloadRunning(mIntent);
                break;
            case OP_APPSTORE_PAUSE: // app store download pause
                Log.d(TAG, "AppDownload : AppDownloadTask PAUSE");
                appDownloadPause(mIntent);
                break;
            case OP_APPSTORE_FAIL: // app store download fail
                Log.d(TAG, "AppDownload : AppDownloadTask FAIL");
                appDownloadFail(mIntent);
                break;
            case OP_APPSTORE_CANCEL: // app store download cancel
                Log.d(TAG, "AppDownload : AppDownloadTask CANCEL");
                appDownloadCancel(mIntent);
                break;
            }
        }

    }
    //topwise zyf add for folderonlie
    public class OnlineDownloadTask implements Runnable {

        private int mOp;
        private Intent mIntent;

        public OnlineDownloadTask(int op, Intent intent) {
            mOp = op;
            mIntent = intent;
            //zyf log
            Log.d("zyfonline", "OnlineDownloadTask intent = "+intent);
            //zyf log end
        }

        @Override
        public void run() {
            switch (mOp) {
            case OP_ONLINE_START: 
                Log.d(TAG, " OnlineDownloadTask START");
                //更新数据库状态
                //更新图标
                onlineDownloadStart(mIntent);
                break;
            case OP_ONLINE_RUNING: 
                Log.d("zyfonline", "OnlineDownloadTask RUNNING");
                onlineDownloadRunning(mIntent);
                break;
            case OP_ONLINE_PAUSE: 
                Log.d("zyfonline", "OnlineDownloadTask PAUSE");
                onlineDownloadPause(mIntent);
                break;
            case OP_ONLINE_FAIL: 
                Log.d("zyfonline", "OnlineDownloadTask FAIL");
                onlineDownloadFail(mIntent);
                break;
            case OP_ONLINE_CANCEL: 
                Log.d("zyfonline", "OnlineDownloadTask CANCEL");
                onlinepDownloadCancel(mIntent);
                break;
            }
        }

    }
    private void onlineDownloadStart(Intent intent)
    {
    	Log.d("zyfonline", "onlineDownloadStart............");
    	//String url=intent.getStringExtra(EXTRA_DOWNLOAD_URL);
    	//String pkgName =this.mLauncherModel.getPkgnameByUrl(url);
    	//Log.d("zyfonline", "onlineDownloadStart.....url="+url+" , pkgName="+pkgName);
    	
    	String pkgName = intent.getStringExtra(TYPE_PACKAGENAME);
    	if(pkgName==null)
    		return;
    	//int progress = intent.getIntExtra(TYPE_PROGRESS, 0);
    	
    	//更新图标显示
    	ShortcutInfo shortcutinfo =getShortcutInfoByPackageName(pkgName,this.mLauncherModel);
    	Log.d("zyfonline", "onlineDownloadStart............shortcutinfo="+shortcutinfo);
    	if(shortcutinfo!=null)
    	{
			//更新数据库
	    	updateToModelDb(pkgName,ApkInfo.APKINFO_STATE_DOWNLOAD_WAIT);
	    	if(shortcutinfo.getAppDownloadStatus()!=AppDownloadStatus.STATUS_WAITING)
	    	{
				shortcutinfo.setAppDownloadStatus(AppDownloadStatus.STATUS_WAITING);
	    		Log.d("zyfonline", "onlineDownloadStart : AppDownloadStatus() = "+shortcutinfo.getAppDownloadStatus());
	    		notifyUIUpdateIconForOnline(shortcutinfo);
	    	}
    	}
    }
    private void onlineDownloadRunning(Intent intent)
    {
    	Log.d("zyfonline", "onlineDownloadRunning............");
    	String url=intent.getStringExtra(EXTRA_DOWNLOAD_URL);
    	ApkInfo apkinfo=this.mLauncherModel.getApkinfoByUrl(url);
    	if(apkinfo==null)
    		return;
    	String pkgName =apkinfo.mPkgname;
    	//String pkgName = intent.getStringExtra(TYPE_PACKAGENAME);
    	int progress = intent.getIntExtra(TYPE_PROGRESS, 0);
    	
    	//更新图标显示
    	ShortcutInfo shortcutinfo =getShortcutInfoByPackageName(pkgName,this.mLauncherModel);
    	Log.d("zyfonline", "onlineDownloadRunning............shortcutinfo="+shortcutinfo+",progress="+progress);
    	if(shortcutinfo!=null)
    	{
    		if(progress==DOWNLOAD_COMPLETE_PROGRESS)//下载完成，进入安装
    		{
    			//更新数据库
    	    	updateToModelDb(pkgName,ApkInfo.APKINFO_STATE_DOWNLOAD_COMPLETE);

	    		String fullpath="";
	    		if(apkinfo.mType==ItemInfo.ITEM_EXTRA_TYPE_APPS)//app
	  			{
	  				fullpath=ApkInfo.DESTPATH_UNZIP_APP+apkinfo.mFileName;
	  			}
  				else if(apkinfo.mType==ItemInfo.ITEM_EXTRA_TYPE_GAMES)//game
  	  			{
  					fullpath=ApkInfo.DESTPATH_UNZIP_GAME+apkinfo.mFileName;
  	  			}
	    		Log.d("zyfonline","onlineDownloadRunning fullpath = "+fullpath);
    			shortcutinfo.setAppDownloadStatus(AppDownloadStatus.STATUS_INSTALLING);
    			Intent noinstallIntent=new Intent("com.tpw.install");
    			noinstallIntent.putExtra(com.tpw.homeshell.vpinstall.VPUtils.TYPE_PACKAGEPATH,fullpath);
    			noinstallIntent.putExtra(com.tpw.homeshell.vpinstall.VPUtils.TYPE_PACKAGENAME,pkgName);
    			shortcutinfo.intent =noinstallIntent;
    			this.mLauncherModel.startVPSilentInstall(shortcutinfo);
    			
    		}
    		else//未下载完，更新进度
    		{
    			updateToModelDb(pkgName,ApkInfo.APKINFO_STATE_DOWNLOAD);
    			updateShortcutInfo(shortcutinfo,AppDownloadStatus.STATUS_DOWNLOADING, progress);
    		}
    		Log.d("zyfonline", "onlineDownloadRunning : AppDownloadStatus() = "+shortcutinfo.getAppDownloadStatus());
    		notifyUIUpdateIconForOnline(shortcutinfo);
    	}
    }
    private void onlineDownloadPause(Intent intent)
    {
    	String url=intent.getStringExtra(EXTRA_DOWNLOAD_URL);
    	ApkInfo apkinfo=this.mLauncherModel.getApkinfoByUrl(url);
    	if(apkinfo==null)
    		return;
    	String pkgName =apkinfo.mPkgname;
    	//String pkgName = intent.getStringExtra(TYPE_PACKAGENAME);
    	int progress = intent.getIntExtra(TYPE_PROGRESS, 0);
    	int pauseextra=intent.getIntExtra(ONLINE_PAUSE_EXTRA, ONLINE_PAUSE_COMMON);
    	//更新数据库
    	if(pauseextra==ONLINE_PAUSE_COMMON)
    		updateToModelDb(pkgName,ApkInfo.APKINFO_STATE_PAUSE);
    	else if(pauseextra==ONLINE_PAUSE_BY_USER)
    		updateToModelDb(pkgName,ApkInfo.APKINFO_STATE_USER_PAUSE);
    	//更新图标显示
    	ShortcutInfo shortcutinfo =getShortcutInfoByPackageName(pkgName,this.mLauncherModel);
    	if(shortcutinfo!=null)
    	{
    		updateShortcutInfo(shortcutinfo,AppDownloadStatus.STATUS_PAUSED, progress);
    		notifyUIUpdateIconForOnline(shortcutinfo);
    	}
    }
    private void onlineDownloadFail(Intent intent)
    {
    	String url=intent.getStringExtra(EXTRA_DOWNLOAD_URL);
    	ApkInfo apkinfo=this.mLauncherModel.getApkinfoByUrl(url);
    	if(apkinfo==null)
    		return;
    	String pkgName =apkinfo.mPkgname;
    	//String pkgName = intent.getStringExtra(TYPE_PACKAGENAME);
    	int progress = intent.getIntExtra(TYPE_PROGRESS, 0);
    	updateToModelDb(pkgName,ApkInfo.APKINFO_STATE_PAUSE);
    	//更新图标显示
    	ShortcutInfo shortcutinfo =getShortcutInfoByPackageName(pkgName,this.mLauncherModel);
    	if(shortcutinfo!=null)
    	{
    		updateShortcutInfo(shortcutinfo,AppDownloadStatus.STATUS_PAUSED, progress);
    		notifyUIUpdateIconForOnline(shortcutinfo);
    	}
    }
    private void onlinepDownloadCancel(Intent intent)
    {
    	String url=intent.getStringExtra(EXTRA_DOWNLOAD_URL);
    	ApkInfo apkinfo=this.mLauncherModel.getApkinfoByUrl(url);
    	if(apkinfo==null)
    		return;
    	String pkgName =apkinfo.mPkgname;
    	//String pkgName = intent.getStringExtra(TYPE_PACKAGENAME);
    	int progress = intent.getIntExtra(TYPE_PROGRESS, 0);
    	updateToModelDb(pkgName,ApkInfo.APKINFO_STATE_PAUSE);
    	//更新图标显示
    	ShortcutInfo shortcutinfo =getShortcutInfoByPackageName(pkgName,this.mLauncherModel);
    	if(shortcutinfo!=null)
    	{
    		updateShortcutInfo(shortcutinfo,AppDownloadStatus.STATUS_PAUSED, progress);
    		notifyUIUpdateIconForOnline(shortcutinfo);
    	}
    }
    private void updateToModelDb(String pkgname, int state) {
        Log.d("zyfonline", "AppDownload : updateToModelDb begin pkgname = "+pkgname);
        this.mLauncherModel.updateItemInDatabaseForOnlineDownload(mContext,
                pkgname,state);
        Log.d("zyfonline", "AppDownload : updateToModelDb end");

    } 
    private void notifyUIUpdateIconForOnline(ShortcutInfo appItem) {
        Log.d("zyfonline", "AppDownload : notifyUIUpdateIcon begin");
        ArrayList<ItemInfo> apps = new ArrayList<ItemInfo>();
        apps.add(appItem);
        this.mLauncherModel.notifyUIUpdateIcon(apps);
    }
    public ShortcutInfo getShortcutInfoByPackageName(String pkgname,LauncherModel lmodel) {
        return lmodel.findItemInfoByPkgName(pkgname);
    }
  //topwise zyf add end
}

