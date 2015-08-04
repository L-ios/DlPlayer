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


package com.tpw.homeshell.model;

import android.app.Notification;

import java.io.File;
import java.lang.ref.WeakReference;
import java.net.URISyntaxException;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.io.Serializable;

//topwise zyf add for fixedfolder
import android.content.res.AssetManager;
import android.content.res.Resources;

import com.tpw.homeshell.ApkInfo;
import com.tpw.homeshell.base64code;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
//topwise zyf add end





import android.appwidget.AppWidgetHost;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.Intent.ShortcutIconResource;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Parcelable;
import android.os.Process;
import android.os.RemoteException;
import android.os.SystemClock;
import android.provider.CallLog.Calls;
//import android.os.SystemProperties;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.tpw.homeshell.AgedModeUtil;
import com.tpw.homeshell.AllAppsList;
import com.tpw.homeshell.AppDownloadManager;
import com.tpw.homeshell.AppDownloadManager.AppDownloadStatus;
import com.tpw.homeshell.ApplicationInfo;
import com.tpw.homeshell.CheckVoiceCommandPressHelper;
import com.tpw.homeshell.ConfigManager;
import com.tpw.homeshell.DeferredHandler;
import com.tpw.homeshell.DropTarget;
import com.tpw.homeshell.ExappUtil;
import com.tpw.homeshell.FastBitmapDrawable;
import com.tpw.homeshell.FileAndInfo;
import com.tpw.homeshell.FolderIcon;
import com.tpw.homeshell.FolderInfo;
import com.tpw.homeshell.GadgetCardHelper;
import com.tpw.homeshell.GadgetItemInfo;
import com.tpw.homeshell.Hideseat;
import com.tpw.homeshell.InstallWidgetReceiver;
import com.tpw.homeshell.LauncherGadgetHelper;
import com.tpw.homeshell.InstallWidgetReceiver.WidgetMimeTypeHandlerData;
import com.tpw.homeshell.ItemInfo;
import com.tpw.homeshell.Launcher;
import com.tpw.homeshell.LauncherAppWidgetInfo;
import com.tpw.homeshell.LauncherApplication;
import com.tpw.homeshell.LauncherProvider;
import com.tpw.homeshell.LauncherProvider.updateArgs;
import com.tpw.homeshell.LauncherSettings;
import com.tpw.homeshell.LauncherSettings.Favorites;
import com.tpw.homeshell.R;
import com.tpw.homeshell.ScreenPosition;
import com.tpw.homeshell.ShortcutAndWidgetContainer;
import com.tpw.homeshell.ShortcutInfo;
import com.tpw.homeshell.TopwiseConfig;
import com.tpw.homeshell.UserTrackerHelper;
import com.tpw.homeshell.UserTrackerMessage;
import com.tpw.homeshell.Utilities;
import com.tpw.homeshell.WidgetPreviewLoader;
import com.tpw.homeshell.appfreeze.AppFreezeUtil;
import com.tpw.homeshell.appgroup.AppGroupManager;
import com.tpw.homeshell.backuprestore.BackupManager;
import com.tpw.homeshell.backuprestore.BackupRecord;
import com.tpw.homeshell.backuprestore.BackupUitil;
import com.tpw.homeshell.icon.BubbleTextView;
import com.tpw.homeshell.icon.IconManager;
import com.tpw.homeshell.icon.IconManager.IconCursorInfo;
import com.tpw.homeshell.setting.HomeShellSetting;
import com.tpw.homeshell.themeutils.ThemeUtils;
import com.tpw.homeshell.utils.ToastManager;
import com.tpw.homeshell.utils.Utils;
import com.tpw.homeshell.vpinstall.IInstallStateListener;
import com.tpw.homeshell.vpinstall.VPInstaller;
import com.tpw.homeshell.vpinstall.VPInstaller.AppKey;
import com.tpw.homeshell.vpinstall.VPUtils;
import com.tpw.homeshell.vpinstall.VPUtils.VPInstallStatus;
import com.tpw.homeshell.IconDigitalMarkHandler;

import commonlibs.utils.ACA;


//topwise zyf add
import com.tpw.homeshell.FixedFolderInfo;

import android.util.Xml;

import com.tpw.online.DBOperate;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.provider.Settings;
//topwise zyf add end

/**
 * Maintains in-memory state of the Launcher. It is expected that there should be only one
 * LauncherModel object held in a static. Also provide APIs for updating the database state
 * for the Launcher.
 */
public class LauncherModel extends BroadcastReceiver implements IInstallStateListener{

    static final boolean DEBUG_LOADERS = true; 
    static final String TAG = "Launcher.Model";

    public static final String NEW_APPS_PAGE_KEY = "apps.new.page";
    public static final String NEW_APPS_LIST_KEY = "apps.new.list";
    public static HomeShellSetting homeshellSetting = null;
    public static final String ACTION_RELOAD_DOWNLOADING = "com.tpw.homeshell.action.RELOAD_DOWNLOADING_APPS";
    public static final String ACTION_HOMESHELL_LAYOUT_CHANGE = "com.tpw.homeshell.action.LAYOUT_CHANGE";
    public static final String EXTRA_COUNTX = "countX";
    public static final String EXTRA_COUNTY = "countY";

    public static final String ACTION_UPDATE_LAYOUT = "com.tpw.homeshell.action.UPDATE_LAYOUT";

    public static final String ACTION_APP_LAUNCHED = "com.yunos.NOTIFY_HOMESHELL_APP_LAUNCHED";
    
    public static final String ACTION_AGED_MODE_CHANGED = "aged_mode_changed";

    private static final int ITEMS_CHUNK = 6; // batch size for the workspace icons

    private static int mMaxScreenCount = ConfigManager.getScreenMaxCount();

    private final boolean mAppsCanBeOnExternalStorage;
    private int mBatchSize; // 0 is all apps at once
    private int mAllAppsLoadDelay; // milliseconds between batches

    private final LauncherApplication mApp;
    private VPUtils mVPUtils;
    private final Object mLock = new Object();
    private static DeferredHandler mHandler = new DeferredHandler();
    private LoaderTask mLoaderTask;
    private boolean mIsLoaderTaskRunning;
    private volatile boolean mFlushingWorkerThread;

    // Specific runnable types that are run on the main thread deferred handler, this allows us to
    // clear all queued binding runnables when the Launcher activity is destroyed.
    private static final int MAIN_THREAD_NORMAL_RUNNABLE = 0;
    private static final int MAIN_THREAD_BINDING_RUNNABLE = 1;
    private static final int MAIN_THREAD_THEME_CHANGE_RUNNABLE = 2;

    private boolean mIsLoadingAndBindingWorkspace;

    private static final HandlerThread sWorkerThread = new HandlerThread("launcher-loader");
    static {
        sWorkerThread.start();
    }
    private static final Handler sWorker = new Handler(sWorkerThread.getLooper());

    // theme changed flag
    private boolean mThemeChanged = false;
    // We start off with everything not loaded.  After that, we assume that
    // our monitoring of the package manager provides all updates and we never
    // need to do a requery.  These are only ever touched from the loader thread.
    private boolean mWorkspaceLoaded;
    private boolean mAllAppsLoaded;

    private boolean mClearAllDownload =  false;

    // When we are loading pages synchronously, we can't just post the binding of items on the side
    // pages as this delays the rotation process.  Instead, we wait for a callback from the first
    // draw (in Workspace) to initiate the binding of the remaining side pages.  Any time we start
    // a normal load, we also clear this set of Runnables.
    static final ArrayList<Runnable> mDeferredBindRunnables = new ArrayList<Runnable>();

    public static WeakReference<Callbacks> mCallbacks;

    // < only access in worker thread >
    private AllAppsList mBgAllAppsList;

    private List<ResolveInfo> appsOnBoot = null;

    // The lock that must be acquired before referencing any static bg data structures.  Unlike
    // other locks, this one can generally be held long-term because we never expect any of these
    // static data structures to be referenced outside of the worker thread except on the first
    // load after configuration change.
    static final Object sBgLock = new Object();

    // sBgItemsIdMap maps *all* the ItemInfos (shortcuts, folders, and widgets) created by
    // LauncherModel to their ids
    public static final HashMap<Long, ItemInfo> sBgItemsIdMap = new HashMap<Long, ItemInfo>();

    // sBgWorkspaceItems is passed to bindItems, which expects a list of all folders and shortcuts
    //       created by LauncherModel that are directly on the home screen (however, no widgets or
    //       shortcuts within folders).
    public static final ArrayList<ItemInfo> sBgWorkspaceItems = new ArrayList<ItemInfo>();

    // sBgNoSpaceItems is all applications these has no space in workspace.
    // These applications are waiting for the empty cell and will be placed in workspace
    // if space available.
    static final ArrayList<ItemInfo> sBgNoSpaceItems = new ArrayList<ItemInfo>();
    // sBgAppWidgets is all LauncherAppWidgetInfo created by LauncherModel. Passed to bindAppWidget()
    public static final ArrayList<LauncherAppWidgetInfo> sBgAppWidgets =
        new ArrayList<LauncherAppWidgetInfo>();

    // sBgFolders is all FolderInfos created by LauncherModel. Passed to bindFolders()
    static final HashMap<Long, FolderInfo> sBgFolders = new HashMap<Long, FolderInfo>();

    //topwise zyf add for fixedfolder
    static final ArrayList<FolderInfo> sFixedFoldersItems = new ArrayList<FolderInfo>();
    static ArrayList<FileAndInfo> sAlapks= null;
    public static final HashMap<String, ItemInfo> sApkItemsMap = new HashMap<String, ItemInfo>();
    static final String DESTPATH_UNZIP_APP = "/data/exres/app/";
    static final String DESTPATH_UNZIP_GAME = "/data/exres/game/";
    //static final String FOLDER_ONLINE_XML_PATH="/data/system/online/";
    
	public static final String PRENAME="apkinfos";
	public static final String LASTNAME=".xml";
	
	private List<ApkInfo> mApkinfos=null;
	private final HashMap<String, ApkInfo> mApkinfosMap = new HashMap<String, ApkInfo>();
	private List<ShortcutInfo> mOnlineShortcutInfos=null;
    //topwise zyf add end

    private Bitmap mDefaultIcon;

    private static int mCellCountX;
    private static int mCellCountY;

    protected int mPreviousConfigMcc;
    
    private AppDownloadManager mAppDownloadMgr;
    private AppGroupManager mAppGroupMgr;

	//topwise zyf add for notify
	private int mNewAppNum=0;
	private int mNewGameNum=0;
	private int mWorkspaceLoadCount=0;
	private int mFixedfolderLoadCount=0;
	//topwise zyf add for notify end

    private ArrayList<String> allComponentList = new ArrayList<String>();

    private boolean mReceiveAppDownloadinRestore = false;
    private IconManager mIconManager = null;
    private final PackageUpdateTaskQueue mPackageUpdateTaskQueue = new PackageUpdateTaskQueue();

    private static final int INVALID_MARK_TYPE = -1;
    private static int mMarkType = INVALID_MARK_TYPE;
    private static boolean mShowNewMarkInit;
    private static boolean mShowNewMark;

    private static Runnable checkNoSpaceListR = new Runnable() {
        @Override
        public void run() {
            checkNoSpaceList();
        }
    };

    public interface Callbacks {
        public boolean setLoadOnResume();
        public int getCurrentWorkspaceScreen();
        public void startBinding();
        public void bindItems(ArrayList<ItemInfo> shortcuts, int start, int end);
        public void bindFolders(HashMap<Long,FolderInfo> folders);
        public void finishBindingItems();
        public void bindAppWidget(LauncherAppWidgetInfo info);
        public void bindAllApplications(ArrayList<ApplicationInfo> apps);
        public void bindAppsAdded(ArrayList<ApplicationInfo> apps);
        public void bindAppsUpdated(ArrayList<ApplicationInfo> apps);
        public void bindComponentsRemoved(ArrayList<String> packageNames,
                        ArrayList<ApplicationInfo> appInfos,
                        boolean matchPackageNamesOnly);
        public void bindPackagesUpdated(ArrayList<Object> widgetsAndShortcuts);
        public boolean isAllAppsVisible();
        public boolean isAllAppsButtonRank(int rank);
        public void onPageBoundSynchronously(int page);
        public void bindRemoveScreen(int screen);
        public void bindItemsUpdated(ArrayList<ItemInfo> items);
        public void bindDownloadItemsRemoved(ArrayList<ItemInfo> items, boolean permanent);
        public void bindItemsRemoved(final ArrayList<ItemInfo> items);
        public void bindItemsAdded(ArrayList<ItemInfo> items);
        public void bindRebuildHotseat(ArrayList<ItemInfo> items);

        public void bindItemsChunkUpdated(ArrayList<ItemInfo> items, int start, int end, boolean themeChange);


        public void bindRebuildHideseat(ArrayList<ItemInfo> items);

        public void startVPInstallActivity(Intent intent, Object tag);

        public void bindItemsViewRemoved(ArrayList<ItemInfo> items);
        public void bindItemsViewAdded(ArrayList<ItemInfo> items);
        public void bindWorkspaceItemsViewMoved(ArrayList<ItemInfo> items);
        public void resetGridSize(int countX, int countY);
        public void checkAndRemoveEmptyCell();
        public void closeFolderWithoutExpandAnimation();
        public boolean isInEditScreenMode();
        public void collectCurrentViews();
        public void reLayoutCurrentViews();
    }

    public LauncherModel(LauncherApplication app) {
        mAppsCanBeOnExternalStorage = !Environment.isExternalStorageEmulated();
        mApp = app;
        mBgAllAppsList = new AllAppsList();
        mIconManager = app.getIconManager();
        final Resources res = app.getResources();
        mAllAppsLoadDelay = res.getInteger(R.integer.config_allAppsBatchLoadDelay);
        mBatchSize = res.getInteger(R.integer.config_allAppsBatchSize);
        Configuration config = res.getConfiguration();
        mPreviousConfigMcc = config.mcc;
        mAppDownloadMgr = AppDownloadManager.getInstance();
        mAppDownloadMgr.setup(mApp, this, mHandler);
        mAppGroupMgr = AppGroupManager.getInstance();
        mVPUtils = new VPUtils(mApp);
        Log.d(TAG, "max screen count is " + mMaxScreenCount);
    }

    /** Runs the specified runnable immediately if called from the main thread, otherwise it is
     * posted on the main thread handler. */
    public static void runOnMainThread(Runnable r) {
        runOnMainThread(r, 0);
    }
    private static void runOnMainThread(Runnable r, int type) {
        if (sWorkerThread.getThreadId() == Process.myTid()) {
            // If we are on the worker thread, post onto the main handler
            mHandler.post(r,type);
        } else {
            r.run();
        }
    }

    /** Runs the specified runnable immediately if called from the worker thread, otherwise it is
     * posted on the worker thread handler. */
    private static void runOnWorkerThread(Runnable r) {
        if (sWorkerThread.getThreadId() == Process.myTid()) {
            r.run();
        } else {
            // If we are not on the worker thread, then post to the worker handler
            sWorker.post(r);
        }
    }
    public void destroy() {
        appsOnBoot.clear();
        appsOnBoot = null;
        mAppDownloadMgr.unBindAppStoreService();
    }
    public Drawable getFallbackIcon() {
        return mIconManager.getDefaultIcon();
    }

    public void unbindItemInfosAndClearQueuedBindRunnables() {
        if (sWorkerThread.getThreadId() == Process.myTid()) {
            throw new RuntimeException("Expected unbindLauncherItemInfos() to be called from the " +
                    "main thread");
        }

        // Clear any deferred bind runnables
        mDeferredBindRunnables.clear();
        // Remove any queued bind runnables
        mHandler.cancelAllRunnablesOfType(MAIN_THREAD_BINDING_RUNNABLE);
        // Unbind all the workspace items
        unbindWorkspaceItemsOnMainThread();
    }

    /** Unbinds all the sBgWorkspaceItems and sBgAppWidgets on the main thread */
    void unbindWorkspaceItemsOnMainThread() {
        // Ensure that we don't use the same workspace items data structure on the main thread
        // by making a copy of workspace items first.
        final ArrayList<ItemInfo> tmpWorkspaceItems = new ArrayList<ItemInfo>();
        final ArrayList<ItemInfo> tmpAppWidgets = new ArrayList<ItemInfo>();
        synchronized (sBgLock) {
            tmpWorkspaceItems.addAll(sBgWorkspaceItems);
            tmpAppWidgets.addAll(sBgAppWidgets);
        }
        Runnable r = new Runnable() {
                @Override
                public void run() {
                   for (ItemInfo item : tmpWorkspaceItems) {
                       item.unbind();
                   }
                   for (ItemInfo item : tmpAppWidgets) {
                       item.unbind();
                   }
                }
            };
        runOnMainThread(r);
    }

    /**
     * Adds an item to the DB if it was not created previously, or move it to a new
     * <container, screen, cellX, cellY>
     */
    public static void addOrMoveItemInDatabase(Context context, ItemInfo item,
            long container,
            int screen, int cellX, int cellY) {
        Log.d(TAG, "add or move item in database");
        if(item.itemFlags == Favorites.ITEM_FLAGS_EDIT_FOLDER) {
            Log.d(TAG,"prevent the editfoldericon write to database");
            return;
        }
        if (item.container == ItemInfo.NO_ID) {
            // From all apps
            addItemToDatabase(context, item, container, screen, cellX, cellY, false);
        } else {
            // From somewhere else
            moveItemInDatabase(context, item, container, screen, cellX, cellY);
        }
    }

    static void checkItemInfoLocked(
            final long itemId, final ItemInfo item, StackTraceElement[] stackTrace) {
        //This function just throw runtime exception if some error happens,
        //so cancel this function to avoid runtime exception crash
        return;

        /*
        ItemInfo modelItem = sBgItemsIdMap.get(itemId);
        if (modelItem != null && item != modelItem) {
            // check all the data is consistent
            if (modelItem instanceof ShortcutInfo && item instanceof ShortcutInfo) {
                ShortcutInfo modelShortcut = (ShortcutInfo) modelItem;
                ShortcutInfo shortcut = (ShortcutInfo) item;
                if (modelShortcut.title.toString().equals(shortcut.title.toString()) &&
                        modelShortcut.intent.filterEquals(shortcut.intent) &&
                        modelShortcut.id == shortcut.id &&
                        modelShortcut.itemType == shortcut.itemType &&
                        modelShortcut.container == shortcut.container &&
                        modelShortcut.screen == shortcut.screen &&
                        modelShortcut.cellX == shortcut.cellX &&
                        modelShortcut.cellY == shortcut.cellY &&
                        modelShortcut.spanX == shortcut.spanX &&
                        modelShortcut.spanY == shortcut.spanY &&
                        ((modelShortcut.dropPos == null && shortcut.dropPos == null) ||
                        (modelShortcut.dropPos != null &&
                                shortcut.dropPos != null &&
                                modelShortcut.dropPos[0] == shortcut.dropPos[0] &&
                        modelShortcut.dropPos[1] == shortcut.dropPos[1]))) {
                    // For all intents and purposes, this is the same object
                    return;
                }
            }

            // the modelItem needs to match up perfectly with item if our model is
            // to be consistent with the database-- for now, just require
            // modelItem == item or the equality check above
            String msg = "item: " + ((item != null) ? item.toString() : "null") +
                    "modelItem: " +
                    ((modelItem != null) ? modelItem.toString() : "null") +
                    "Error: ItemInfo passed to checkItemInfo doesn't match original";
            RuntimeException e = new RuntimeException(msg);
            if (stackTrace != null) {
                e.setStackTrace(stackTrace);
            }
            throw e;
        }
        */
    }

    public static void checkItemInfo(final ItemInfo item) {
        /*
        final StackTraceElement[] stackTrace = new Throwable().getStackTrace();
        final long itemId = item.id;
        Runnable r = new Runnable() {
            public void run() {
                synchronized (sBgLock) {
                    checkItemInfoLocked(itemId, item, stackTrace);
                }
            }
        };
        runOnWorkerThread(r);
        */
    }

    static void updateItemInDatabaseHelper(Context context, final ContentValues values,
            final ItemInfo item, final String callingFunction, final boolean checkNoSpaceList) {
        final long itemId = item.id;
        final Uri uri = LauncherSettings.Favorites.getContentUri(itemId, false);
        final ContentResolver cr = context.getContentResolver();
        final Context contextfinal = context;

        //final StackTraceElement[] stackTrace = new Throwable().getStackTrace();
        Runnable r = new Runnable() {
            public void run() {
                cr.update(uri, values, null, null);

                // Lock on mBgLock *after* the db operation
                synchronized (sBgLock) {
                    //checkItemInfoLocked(itemId, item, stackTrace);

                    if (item.container != LauncherSettings.Favorites.CONTAINER_DESKTOP &&
                            item.container != LauncherSettings.Favorites.CONTAINER_HOTSEAT &&
                            item.container != LauncherSettings.Favorites.CONTAINER_HIDESEAT) {
                        // Item is in a folder, make sure this folder exists
                        if (!sBgFolders.containsKey(item.container)) {
                            // An items container is being set to a that of an item which is not in
                            // the list of Folders.
                            String msg = "item: " + item + " container being set to: " +
                                    item.container + ", not in the list of folders";
                            Log.e(TAG, msg);
                            Launcher.dumpDebugLogsToConsole();
                        }
                    }

                    // Items are added/removed from the corresponding FolderInfo elsewhere, such
                    // as in Workspace.onDrop. Here, we just add/remove them from the list of items
                    // that are on the desktop, as appropriate
                    ItemInfo modelItem = sBgItemsIdMap.get(itemId);
                    if (modelItem == null) {
                        Log.d(TAG, "modelItem is null, itemId is "  + itemId);
                        //sBgWorkspaceItems.remove(item);
                    } else {
                        if (modelItem.container == LauncherSettings.Favorites.CONTAINER_DESKTOP ||
                                modelItem.container == LauncherSettings.Favorites.CONTAINER_HOTSEAT ||
                                modelItem.container == LauncherSettings.Favorites.CONTAINER_HIDESEAT) {
                            switch (modelItem.itemType) {
                                case LauncherSettings.Favorites.ITEM_TYPE_APPLICATION:
                                case LauncherSettings.Favorites.ITEM_TYPE_SHORTCUT:
                                case LauncherSettings.Favorites.ITEM_TYPE_FOLDER:
                                case LauncherSettings.Favorites.ITEM_TYPE_VPINSTALL:
                                //add download item to sBgWorkspaceItems if it is moved from folder to workspace
                                case LauncherSettings.Favorites.ITEM_TYPE_SHORTCUT_DOWNLOADING:
                                //topwise zyf add for fixedfolder
                                case LauncherSettings.Favorites.ITEM_TYPE_FIXED_FOLDER:
                                //topwise zyf add end
                                    if (!sBgWorkspaceItems.contains(modelItem)) {
                                        sBgWorkspaceItems.add(modelItem);
                                    }
                                    break;
                                default:
                                    break;
                            }
                        } else {
                            sBgWorkspaceItems.remove(modelItem);
                        }
                    }
                }

                if (checkNoSpaceList) {
                    //run checkNoSpaceList after all update happened
                    postCheckNoSpaceList();
                }

                sWorker.removeCallbacks(mCheckInvalidPosItemsRunnable);
                sWorker.postDelayed(mCheckInvalidPosItemsRunnable, 3000);
            }
        };
        runOnWorkerThread(r);
    }

    static void updateItemInDatabaseHelper(Context context, final ContentValues values,
                final ItemInfo item, final String callingFunction) {
        updateItemInDatabaseHelper(context, values, item, callingFunction, true);
    }

    public void flushWorkerThread() {
        mFlushingWorkerThread = true;
        Runnable waiter = new Runnable() {
                public void run() {
                    synchronized (this) {
                        notifyAll();
                        mFlushingWorkerThread = false;
                    }
                }
            };

        synchronized(waiter) {
            runOnWorkerThread(waiter);
            if (mLoaderTask != null) {
                synchronized(mLoaderTask) {
                    mLoaderTask.notify();
                }
            }
            boolean success = false;
            while (!success) {
                try {
                    waiter.wait();
                    success = true;
                } catch (InterruptedException e) {
                }
            }
        }
    }

    /**
     * Move an item in the DB to a new <container, screen, cellX, cellY>
     */
    public static void moveItemInDatabase(Context context, final ItemInfo item,
            final long container,
            final int screen, final int cellX, final int cellY) {
        Log.d(TAG, "moveItemInDatabase in");
        int oldscreen = -1;
        long oldcontainer = item.container;
        String transaction = "DbDebug    move item (" + item.title + ") in db, id: " + item.id +
                " (" + item.container + ", " + item.screen + ", " + item.cellX + ", " + item.cellY +
                ") --> " + "(" + container + ", " + screen + ", " + cellX + ", " + cellY + ")";
        //Launcher.sDumpLogs.add(transaction);
        Log.d(TAG, transaction);
        item.container = container;
        item.cellX = cellX;
        item.cellY = cellY;

        // We store hotseat items in canonical form which is this orientation invariant position
        // in the hotseat
        if (context instanceof Launcher && screen < 0 &&
                container == LauncherSettings.Favorites.CONTAINER_HOTSEAT) {
            if (oldcontainer != LauncherSettings.Favorites.CONTAINER_HOTSEAT) {
                oldscreen = item.screen;
            }
            item.screen = ((Launcher) context).getHotseat().getOrderInHotseat(cellX, cellY);
        }
        else if (context instanceof Launcher && screen < 0 &&
                container == LauncherSettings.Favorites.CONTAINER_HIDESEAT) {
            if (oldcontainer != LauncherSettings.Favorites.CONTAINER_HIDESEAT) {
                oldscreen = item.screen;
            }
            //item.screen = ((Launcher) context).getHotseat().getOrderInHotseat(cellX, cellY);
        }
        else {
            if ((item.screen != screen) && (screen >= 0)) {
                oldscreen = item.screen;
            }
            Log.d(TAG, "oldscreen is " + oldscreen);
            item.screen = screen;
        }

        final ContentValues values = new ContentValues();
        values.put(LauncherSettings.Favorites.CONTAINER, item.container);
        values.put(LauncherSettings.Favorites.CELLX, item.cellX);
        values.put(LauncherSettings.Favorites.CELLY, item.cellY);
        values.put(LauncherSettings.Favorites.SCREEN, item.screen);

        updateItemInDatabaseHelper(context, values, item, "moveItemInDatabase");
    }

    /**
     * Move and/or resize item in the DB to a new <container, screen, cellX, cellY, spanX, spanY>
     */
    public static void modifyItemInDatabase(Context context,
            final ItemInfo item, final long container,
            final int screen, final int cellX, final int cellY, final int spanX, final int spanY,
            boolean checkNoSpaceList) {
        Log.d(TAG, "modify item in db");
        String transaction = "DbDebug    Modify item (" + item.title + ") in db, id: " + item.id +
                " (" + item.container + ", " + item.screen + ", " + item.cellX + ", " + item.cellY +
                ") --> " + "(" + container + ", " + screen + ", " + cellX + ", " + cellY + ")";
        //Launcher.sDumpLogs.add(transaction);
        Log.d(TAG, transaction);
        item.cellX = cellX;
        item.cellY = cellY;
        item.spanX = spanX;
        item.spanY = spanY;

        // We store hotseat items in canonical form which is this orientation invariant position
        // in the hotseat
        if (context instanceof Launcher && screen < 0 &&
                container == LauncherSettings.Favorites.CONTAINER_HOTSEAT) {
            item.screen = ((Launcher) context).getHotseat().getOrderInHotseat(cellX, cellY);
        }
        else if(context instanceof Launcher && screen < 0 &&
                container == LauncherSettings.Favorites.CONTAINER_HIDESEAT) {
            //item.screen = ((Launcher) context).getHotseat().getOrderInHotseat(cellX, cellY);
        }
        else {
            item.screen = screen;
        }

        final ContentValues values = new ContentValues();
        values.put(LauncherSettings.Favorites.CONTAINER, item.container);
        values.put(LauncherSettings.Favorites.CELLX, item.cellX);
        values.put(LauncherSettings.Favorites.CELLY, item.cellY);
        values.put(LauncherSettings.Favorites.SPANX, item.spanX);
        values.put(LauncherSettings.Favorites.SPANY, item.spanY);
        values.put(LauncherSettings.Favorites.SCREEN, item.screen);
        values.put(LauncherSettings.Favorites.ITEM_TYPE, item.itemType);
        updateItemInDatabaseHelper(context, values, item, "modifyItemInDatabase", checkNoSpaceList);
    }

    public static void modifyItemInDatabase(Context context,
            final ItemInfo item, final long container,
            final int screen, final int cellX, final int cellY, final int spanX, final int spanY) {
        modifyItemInDatabase(context, item, container, screen, cellX, cellY, spanX, spanY, true);
    }

    public static void updateItemInDatabaseForAppDownload(Context context,
            final ShortcutInfo item) {
        Log.d(TAG, "update item in data base for app download");
        final ContentValues values = new ContentValues();
        item.onAddToDatabase(values);
        item.updateValuesWithCoordinates(values, item.cellX, item.cellY);
        LauncherApplication application = (LauncherApplication)context.getApplicationContext();
        Drawable origIcon = item.mIcon;
        if(origIcon!=null){
            item.writeBitmap(values, origIcon);
        }
        updateItemInDatabaseHelper(context, values, item, "updateItemInDatabase");
    }
    public static ArrayList<ItemInfo> getSbgWorkspaceItems() {
        final ArrayList<ItemInfo> tmpWorkspaceItems = new ArrayList<ItemInfo>();
        synchronized (sBgLock) {
            tmpWorkspaceItems.addAll(sBgWorkspaceItems);
        }
        return tmpWorkspaceItems;
    }
    public static HashMap<Long, ItemInfo> getSBgItemsIdMap() {
        HashMap<Long, ItemInfo> itemsIdMap = new HashMap<Long, ItemInfo>();
        synchronized (sBgLock) {
            itemsIdMap.putAll(sBgItemsIdMap);
        }
        return itemsIdMap;
    }
    public static ArrayList<ItemInfo> getAllAppItems() {
        final ArrayList<ItemInfo> allAppItems = new ArrayList<ItemInfo>();
        synchronized (sBgLock) {
            allAppItems.addAll(sBgItemsIdMap.values());
        }
        return allAppItems;
    }
    private static ArrayList<FolderInfo> getAllFolderItems() {
        final ArrayList<FolderInfo> allFolderItems = new ArrayList<FolderInfo>();
        synchronized (sBgLock) {
            allFolderItems.addAll(sBgFolders.values());
        }
        return allFolderItems;
    }
    public static HashMap<Long, FolderInfo> getSBgFolders() {
        HashMap<Long, FolderInfo> folders = new HashMap<Long, FolderInfo>();
        synchronized (sBgLock) {
            folders.putAll(sBgFolders);
        }
        return folders;
    }
    //find emptycells which the amount is reqCount , by default spanX=1,spanY=1
    public static List<ScreenPosition> findEmptyCells(int reqCount) {
        Log.d(TAG,"findEmptyCells in");
        if(reqCount == 0 ) {
            return null;
        }
        int count = 0;
        List<ScreenPosition> posList = new ArrayList<ScreenPosition>();
        int xCount = LauncherModel.getCellCountX();
        int yCount = LauncherModel.getCellCountY();
        int scrnCount = mMaxScreenCount;
        boolean[][][] occupied = new boolean[scrnCount][xCount][yCount];
        initOccupied(occupied);
        ForLable:for(int scr = 1; scr < scrnCount; scr++) {
            for(int yIndex = 0; yIndex < yCount; yIndex++) {
                for(int xIndex = 0; xIndex < xCount; xIndex++) {
                    if(!occupied[scr][xIndex][yIndex]) {
                        ScreenPosition pos = new ScreenPosition(scr,xIndex,yIndex);
                        posList.add(pos);
                        count++;
                        if(count >= reqCount) {
                            break ForLable;
                        }
                    }
                }
            }
        }
        return posList;
    }
    /**
     * Update an item to the database in a specified container.
     */
    public static void updateItemInDatabase(Context context, final ItemInfo item) {
        Log.d(TAG, "update item in data base");
        final ContentValues values = new ContentValues();
        final Context finalContext = context;

        Runnable r = new Runnable() {
            @Override
            public void run() {
                item.onAddToDatabase(values);

                // the app icon store in database should be original icon
                /*save the original icon to database if it is a sd app*/
                if (item instanceof ShortcutInfo) {
                    switch (item.itemType) {
                    case LauncherSettings.Favorites.ITEM_TYPE_APPLICATION: {
                        if (((ShortcutInfo) item).isSDApp == 1) {
                            Drawable origIcon = VPUtils.getAppOriginalIcon(finalContext, (ShortcutInfo) item);
                            if (origIcon != null) {
                                ItemInfo.writeBitmap(values, origIcon);
                            } else {
                                if (values.containsKey(LauncherSettings.Favorites.ICON)) {
                                    values.remove(LauncherSettings.Favorites.ICON);
                                }
                            }
                        } else {
                            values.put(LauncherSettings.Favorites.ICON, new byte[0]);
                        }
                    }
                        break;
                    case LauncherSettings.Favorites.ITEM_TYPE_SHORTCUT:
                    case LauncherSettings.Favorites.ITEM_TYPE_BOOKMARK:
                    case LauncherSettings.Favorites.ITEM_TYPE_SHORTCUT_DOWNLOADING:
                    case LauncherSettings.Favorites.ITEM_TYPE_VPINSTALL: {
                        if (values.containsKey(LauncherSettings.Favorites.ICON)) {
                            values.remove(LauncherSettings.Favorites.ICON);
                        }
                    }
                        break;
                    default:
                        break;
                    }
                }

                item.updateValuesWithCoordinates(values, item.cellX, item.cellY);
                updateItemInDatabaseHelper(finalContext, values, item, "updateItemInDatabase");
            }
        };
        runOnWorkerThread(r);
    }

    static boolean isIntentDataSame(Intent intentA, Intent intentB) {
        if ((intentA == null) || (intentB == null)) {
            return false;
        }
        if ((intentA.getData() == null) && (intentB.getData() == null)) {
            Log.d(TAG, "data all null, return true");
            return true;
        }
        if ((intentA.getData() != null) &&
            (intentB.getData() != null) &&
            (intentA.getData().toString().equals(intentB.getData().toString()))) {
            Log.d(TAG, "data to string same, return true");
            return true;
        }
        Log.d(TAG, "data not same, return false");
        return false;
    }

    static boolean isIntentExtraSame (Intent intentA, Intent intentB) {
        if ((intentA == null) || (intentB == null)) {
            return false;
        }
        if ((intentA.getExtras() == null) && (intentB.getExtras() == null)) {
            Log.d(TAG, "extra all null, return true");
            return true;
        }
        if (intentA.toUri(0) != null) {
            Log.d(TAG, "intent A toUri is " + intentA.toUri(0).toString());
        }
        if (intentB.toUri(0) != null) {
            Log.d(TAG, "intent B toUri is " + intentB.toUri(0).toString());
        }
        if (intentA.getExtras() != null) {
            Log.d(TAG, "intentA extra is " + intentA.getExtras().toString());
        } else {
             Log.d(TAG, "intentA extra is null");
        }
        if (intentB.getExtras() != null) {
            Log.d(TAG, "intentB extra is " + intentB.getExtras().toString());
        } else {
             Log.d(TAG, "intentB extra is null");
        }

        if ((intentA.getExtras() != null) &&
            (intentB.getExtras() != null) &&
            (intentA.getExtras().toString().equals(intentB.getExtras().toString()))) {
            Log.d(TAG, "extra to string same, return true");
            return true;
        }

        Log.d(TAG, "extra not same return false");
        return false;
    }

    /**
     * Returns true if the shortcuts already exists in the database.
     * we identify a shortcut by its title and intent.
     */
    static long shortcutExists(Context context, String title, Intent intent) {
        long result = -1;
        Log.d(TAG, "shortcutExists in");
        if (intent == null) {
            return result;
        }
        ArrayList<ItemInfo> allApps = getAllAppItems();
        for(ItemInfo info: allApps) {
            if((info instanceof ShortcutInfo) &&
                (info.itemType == Favorites.ITEM_TYPE_APPLICATION ||
                info.itemType == Favorites.ITEM_TYPE_NOSPACE_APPLICATION ||
                info.itemType == Favorites.ITEM_TYPE_BOOKMARK ||
                info.itemType == Favorites.ITEM_TYPE_SHORTCUT)){
                Intent itemIntent = ((ShortcutInfo)info).intent;
                if ((itemIntent != null) &&
                   (itemIntent.getComponent() != null)) {
                   if ((intent.getComponent() != null)) {
                       //if (intent.getComponent().getPackageName().equals(((ShortcutInfo)info).intent.getComponent().getPackageName())) {
                        if (intent.getComponent().toString().equals(itemIntent.getComponent().toString())) {
                            Log.d(TAG, "same component, item id is " + info.id);
                            if ((isIntentDataSame(intent, itemIntent) == true) &&
                                (isIntentExtraSame(intent, itemIntent) == true)) {
                                result = info.id;
                                break;
                            }
                            if (info.itemType == Favorites.ITEM_TYPE_APPLICATION ||
                                info.itemType == Favorites.ITEM_TYPE_NOSPACE_APPLICATION){
                                if ((info.title != null) && (title != null) &&
                                    (info.title.equals(title))) {
                                    result = info.id;
                                    break;
                                }
                            }
                        }
                        //to filter out the shortcut that has same package name and title as app's
                        else if (info.itemType == Favorites.ITEM_TYPE_APPLICATION ||
                                info.itemType == Favorites.ITEM_TYPE_NOSPACE_APPLICATION) {
                            String itemPkgName = itemIntent.getComponent().getPackageName();
                            String shortcutPkgName = intent.getComponent().getPackageName();
                            if ((itemPkgName != null) && (shortcutPkgName != null) &&
                                (itemPkgName.equals(shortcutPkgName))) {
                                if ((info.title != null) && (title != null) &&
                                    (info.title.equals(title))) {
                                    result = info.id;
                                    break;
                                }
                            }
                        }
                   } else {
                       if (itemIntent.toUri(0).equals(intent.toUri(0))) {
                           result = info.id;
                           break;
                       }
                   }
               } else if (itemIntent != null) {
                   if (itemIntent.toUri(0).equals(intent.toUri(0))) {
                       Log.d(TAG, "find same intent");
                       result = info.id;
                       break;
                   }
               } else {
                   Log.d(TAG, "info intent is null");
               }
           }
        }
        Log.d(TAG, "shortcutExists out");
        return result;
    }

    /**
     * Returns an ItemInfo array containing all the items in the LauncherModel.
     * The ItemInfo.id is not set through this function.
     */
    public static ArrayList<ItemInfo> getItemsInLocalCoordinates(Context context) {
        ArrayList<ItemInfo> items = new ArrayList<ItemInfo>();
        final ContentResolver cr = context.getContentResolver();
        Cursor c = cr.query(LauncherSettings.Favorites.CONTENT_URI, new String[] {
                LauncherSettings.Favorites.ITEM_TYPE, LauncherSettings.Favorites.CONTAINER,
                LauncherSettings.Favorites.SCREEN, LauncherSettings.Favorites.CELLX, LauncherSettings.Favorites.CELLY,
                LauncherSettings.Favorites.SPANX, LauncherSettings.Favorites.SPANY }, null, null, null);

        final int itemTypeIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.ITEM_TYPE);
        final int containerIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.CONTAINER);
        final int screenIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.SCREEN);
        final int cellXIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.CELLX);
        final int cellYIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.CELLY);
        final int spanXIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.SPANX);
        final int spanYIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.SPANY);

        try {
            while (c.moveToNext()) {
                ItemInfo item = new ItemInfo();
                item.cellX = c.getInt(cellXIndex);
                item.cellY = c.getInt(cellYIndex);
                item.spanX = c.getInt(spanXIndex);
                item.spanY = c.getInt(spanYIndex);
                item.container = c.getInt(containerIndex);
                item.itemType = c.getInt(itemTypeIndex);
                item.screen = c.getInt(screenIndex);

                items.add(item);
            }
        } catch (Exception e) {
            items.clear();
        } finally {
            c.close();
        }

        return items;
    }

    /**
     * Find a folder in the db, creating the FolderInfo if necessary, and adding it to folderList.
     */
    public FolderInfo getFolderById(Context context,
            HashMap<Long, FolderInfo> folderList, long id) {
        final ContentResolver cr = context.getContentResolver();
        Cursor c = cr.query(LauncherSettings.Favorites.CONTENT_URI, null,
                "_id=? and (itemType=? or itemType=?)",
                new String[] { String.valueOf(id),
                        String.valueOf(LauncherSettings.Favorites.ITEM_TYPE_FOLDER)}, null);

        try {
            if (c.moveToFirst()) {
                final int itemTypeIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.ITEM_TYPE);
                final int titleIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.TITLE);
                final int containerIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.CONTAINER);
                final int screenIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.SCREEN);
                final int cellXIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.CELLX);
                final int cellYIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.CELLY);

                FolderInfo folderInfo = null;
                switch (c.getInt(itemTypeIndex)) {
                    case LauncherSettings.Favorites.ITEM_TYPE_FOLDER:
                        folderInfo = findOrMakeFolder(folderList, id);
                        break;
                    //topwise zyf add for fixedfolder
                    case LauncherSettings.Favorites.ITEM_TYPE_FIXED_FOLDER:
                        folderInfo = findOrMakeFixedFolder(folderList, id);
                        break;
                    //topwise zyf add end
                }
                if (folderInfo != null) {
                    folderInfo.title = c.getString(titleIndex);
                    folderInfo.id = id;
                    folderInfo.container = c.getInt(containerIndex);
                    folderInfo.screen = c.getInt(screenIndex);
                    folderInfo.cellX = c.getInt(cellXIndex);
                    folderInfo.cellY = c.getInt(cellYIndex);
                }
                return folderInfo;
            }
        } finally {
            c.close();
        }

        return null;
    }

    /**
     * Add an item to the database in a specified container. Sets the container, screen, cellX and
     * cellY fields of the item. Also assigns an ID to the item.
     */
    public static void addItemToDatabase(Context context, final ItemInfo item,
            final long container,
            final int screen, final int cellX, final int cellY, final boolean notify) {
        Log.d(TAG, "add item to db screen:" + screen
                         + " cellX:" + cellX + " cellY:" + cellY
                         + " container:" + container);
        item.container = container;
        item.cellX = cellX;
        item.cellY = cellY;
        // We store hotseat items in canonical form which is this orientation invariant position
        // in the hotseat
        if (context instanceof Launcher && screen < 0 &&
                container == LauncherSettings.Favorites.CONTAINER_HOTSEAT) {
            item.screen = ((Launcher) context).getHotseat().getOrderInHotseat(cellX, cellY);
        }
        else if(context instanceof Launcher && screen < 0 &&
                container == LauncherSettings.Favorites.CONTAINER_HIDESEAT) {
            //item.screen = ((Launcher) context).getHotseat().getOrderInHotseat(cellX, cellY);
        }
        else {
            item.screen = screen;
        }

        final ContentValues values = new ContentValues();
        final ContentResolver cr = context.getContentResolver();
        item.onAddToDatabase(values);

        LauncherApplication app = (LauncherApplication) context.getApplicationContext();
        item.id = app.getLauncherProvider().generateNewId();
        values.put(LauncherSettings.Favorites._ID, item.id);
        item.updateValuesWithCoordinates(values, item.cellX, item.cellY);

        // the app icon store in database should be original icon
        /*save the original icon to database if it is a sd app*/
        if ((item instanceof ShortcutInfo) &&
            (item.itemType == LauncherSettings.Favorites.ITEM_TYPE_APPLICATION)) {
            if (((ShortcutInfo)item).isSDApp == 1) {
                Drawable origIcon = VPUtils.getAppOriginalIcon(context, (ShortcutInfo)item);
                if (origIcon != null) {
                    ItemInfo.writeBitmap(values, origIcon);
                } else {
                    if(values.containsKey(LauncherSettings.Favorites.ICON)) {
                        values.remove(LauncherSettings.Favorites.ICON);
                    }
                }
            } else {
                values.put(LauncherSettings.Favorites.ICON, new byte[0]);
            }
        }
        if(item instanceof GadgetItemInfo) {
            values.put(LauncherSettings.Favorites.TITLE, ((GadgetItemInfo)item).title.toString());
        }

        Runnable r = new Runnable() {
            public void run() {
                String transaction = "DbDebug    Add item (" + item.title + ") to db, id: "
                        + item.id + " (" + container + ", " + screen + ", " + cellX + ", "
                        + cellY + ")";
                //Launcher.sDumpLogs.add(transaction);
                Log.d(TAG, transaction);

                cr.insert(notify ? LauncherSettings.Favorites.CONTENT_URI :
                        LauncherSettings.Favorites.CONTENT_URI_NO_NOTIFICATION, values);

                // Lock on mBgLock *after* the db operation
                synchronized (sBgLock) {
                    checkItemInfoLocked(item.id, item, null);
                    sBgItemsIdMap.put(item.id, item);
                    switch (item.itemType) {
                        case LauncherSettings.Favorites.ITEM_TYPE_FOLDER:
                        //topwise zyf add for fixedfolder
                        case LauncherSettings.Favorites.ITEM_TYPE_FIXED_FOLDER:
                        //topwise zyf add end
                            sBgFolders.put(item.id, (FolderInfo) item);
                            // Fall through
                        case LauncherSettings.Favorites.ITEM_TYPE_APPLICATION:
                        case LauncherSettings.Favorites.ITEM_TYPE_SHORTCUT:
                        case LauncherSettings.Favorites.ITEM_TYPE_SHORTCUT_DOWNLOADING:                       
                        case LauncherSettings.Favorites.ITEM_TYPE_ALIAPPWIDGET:
                        case LauncherSettings.Favorites.ITEM_TYPE_VPINSTALL:
                            if (item.container == LauncherSettings.Favorites.CONTAINER_DESKTOP ||
                                    item.container == LauncherSettings.Favorites.CONTAINER_HOTSEAT ||
                                    item.container == LauncherSettings.Favorites.CONTAINER_HIDESEAT){
                                if (sBgWorkspaceItems.contains(item) == false) {
                                    sBgWorkspaceItems.add(item);
                                }
                            } else {
                                if (!sBgFolders.containsKey(item.container)) {
                                    // Adding an item to a folder that doesn't exist.
                                    String msg = "adding item: " + item + " to a folder that " +
                                            " doesn't exist";
                                    Log.e(TAG, msg);
                                    Launcher.dumpDebugLogsToConsole();
                                } else {
                                    Log.d(TAG, "add to folder, item id is " + item.id);
                                    final FolderInfo finalFolder = sBgFolders.get(item.container);
                                    //'Folder add' has UI operation, I have to run belong code in UI
                                    Runnable UIrun = new Runnable() {
                                        public void run() {
                                            finalFolder.add((ShortcutInfo)item);
                                        }
                                    };
                                    if ((finalFolder != null) && (finalFolder.contents != null) && (finalFolder.contents.contains(item) == false)) {
                                        runOnMainThread(UIrun);
                                    }
                                }
                            }
                            break;
                        case LauncherSettings.Favorites.ITEM_TYPE_GADGET:
                            sBgWorkspaceItems.add(item);
                            sBgItemsIdMap.put(item.id, item);
                            UserTrackerHelper.sendUserReport(UserTrackerMessage.MSG_ADD_GADGET, item.title.toString());
                            break;
                        case LauncherSettings.Favorites.ITEM_TYPE_APPWIDGET:
                            sBgAppWidgets.add((LauncherAppWidgetInfo) item);
                            break;
                        case LauncherSettings.Favorites.ITEM_TYPE_NOSPACE_APPLICATION:
                            sBgNoSpaceItems.add(item);
                            break;
                    }
                }
                sWorker.removeCallbacks(mCheckInvalidPosItemsRunnable);
                sWorker.postDelayed(mCheckInvalidPosItemsRunnable, 3000);
            }
        };
        runOnWorkerThread(r);
    }

    /**
     * Creates a new unique child id, for a given cell span across all layouts.
     */
    public static int getCellLayoutChildId(
            long container, int screen, int localCellX, int localCellY, int spanX, int spanY) {
        return (((int) container & 0xFF) << 24)
                | (screen & 0xFF) << 16 | (localCellX & 0xFF) << 8 | (localCellY & 0xFF);
    }

    public static int getCellCountX() {
        return mCellCountX;
    }

    public static int getCellCountY() {
        return mCellCountY;
    }

    public static int getCellMaxCountX() {
        return ConfigManager.DEFAULT_CELL_MAX_COUNT_X;
    }

    public static int getCellMaxCountY() {
       if (((LauncherApplication)LauncherApplication.getContext()).getIconManager().supprtCardIcon()) {
           return ConfigManager.DEFAULT_CELL_MAX_COUNT_Y - 1;
       }

        return ConfigManager.DEFAULT_CELL_MAX_COUNT_Y;
    }

    /**
     * Updates the model orientation helper to take into account the current layout dimensions
     * when performing local/canonical coordinate transformations.
     */
    public static void updateWorkspaceLayoutCells(int shortAxisCellCount,
            int longAxisCellCount) {
        mCellCountX = shortAxisCellCount;
        mCellCountY = longAxisCellCount;
    }
    //remove big card resource when deleting the icon
    //if we don't delete the icon, the drawbale will not be released
    private static void removeCardBackground(Context context, final ItemInfo item) {
        LauncherApplication app = (LauncherApplication)context.getApplicationContext();
        if ((app == null) ||!(app.getIconManager().supprtCardIcon()) ||
                                (context == null) || (item == null)) {
            return;
        }

        // get component name firstly
        ComponentName component = null;
        String pkgName = null;
        if (item instanceof ShortcutInfo) {
            Intent it = ((ShortcutInfo)item).intent;
            if (it != null) {
                // for downloading type, get package name then create component name with
                // predifend class name
                if (item.itemType == LauncherSettings.Favorites.ITEM_TYPE_SHORTCUT_DOWNLOADING){
                   pkgName = it.getStringExtra(AppDownloadManager.TYPE_PACKAGENAME);
                   component = new ComponentName(pkgName, IconManager.DOWNLOAD_CLASS);
                } else {
                   component = it.getComponent() ;
                }
            }
        }
        if (component == null) {
            return ;
        }
        int count = 0;
        // if there is other shortcut sharing the same component, don't remove big icon
        ArrayList<ItemInfo> allApps = getAllAppItems();
        for(ItemInfo info: allApps) {
            if(info instanceof ShortcutInfo) {
                Intent itemIntent = ((ShortcutInfo)info).intent;
                if (itemIntent != null && info.itemType == LauncherSettings.Favorites.ITEM_TYPE_SHORTCUT_DOWNLOADING){
                   pkgName = itemIntent.getStringExtra(AppDownloadManager.TYPE_PACKAGENAME);
                   ComponentName cmpnt = new ComponentName(pkgName, IconManager.DOWNLOAD_CLASS);
                   if ((cmpnt != null) && (component.toString().equals(cmpnt.toString()))) {
                       count ++;
                   }
                } else if ((itemIntent != null) && (itemIntent.getComponent() != null)) {
                    if (component.toString().equals(itemIntent.getComponent().toString())) {
                       count ++;
                    }
                }
                if (count > 0) {
                    break;
                }
            }
        }
        if (count < 1) {
            app.getIconManager().clearCardBackgroud(((ShortcutInfo) item).intent);
        }
    }
    /**
     * Removes the specified item from the database
     * @param context
     * @param item
     */
    public static void deleteItemFromDatabase(Context context,
            final ItemInfo item) {
        Log.d(TAG, "deleteItemFromDatabase");
        final int screen = item.screen;
        final long container = item.container;
        final ContentResolver cr = context.getContentResolver();
        final Uri uriToDelete = LauncherSettings.Favorites.getContentUri(item.id, false);
        final Context contextFinal = context;

        synchronized (sBgLock) {
            sBgWorkspaceItems.remove(item);
        }

        Runnable r = new Runnable() {
            public void run() {
                String transaction = "DbDebug    Delete item (" + item.title + ") from db, id: "
                        + item.id + " (" + item.container + ", " + item.screen + ", " + item.cellX +
                        ", " + item.cellY + ")";
                //Launcher.sDumpLogs.add(transaction);
                Log.d(TAG, transaction);

                cr.delete(uriToDelete, null, null);

                if ((BackupManager.getInstance().isInRestore()) &&
                    (item.itemType == Favorites.ITEM_TYPE_SHORTCUT_DOWNLOADING)) {
                    if (((ShortcutInfo)item).intent != null) {
                        Log.d(TAG, "remove downloading package from restore list");
                        String packageName = ((ShortcutInfo)item).intent.getStringExtra(AppDownloadManager.TYPE_PACKAGENAME);
                        BackupManager.getInstance().addRestoreDoneApp(contextFinal, packageName);
                    }
                }

                if (item.itemType == LauncherSettings.Favorites.ITEM_TYPE_VPINSTALL) {
                    String pkgName = ((ShortcutInfo)item).intent.getStringExtra(VPUtils.TYPE_PACKAGENAME);
                    UserTrackerHelper.sendUserReport(UserTrackerMessage.MSG_VP_ITEM_DELETE , pkgName);
                }

                // Lock on mBgLock *after* the db operation
                synchronized (sBgLock) {
                    switch (item.itemType) {
                        case LauncherSettings.Favorites.ITEM_TYPE_FOLDER:
                            sBgFolders.remove(item.id);
                            for (ItemInfo info: sBgItemsIdMap.values()) {
                                if (info.container == item.id) {
                                    // We are deleting a folder which still contains items that
                                    // think they are contained by that folder.
                                    String msg = "deleting a folder (" + item + ") which still " +
                                            "contains items (" + info + ")";
                                    Log.e(TAG, msg);
                                    Launcher.dumpDebugLogsToConsole();
                                }
                            }
                            sBgWorkspaceItems.remove(item);
                            break;
                        case LauncherSettings.Favorites.ITEM_TYPE_APPLICATION:
                        case LauncherSettings.Favorites.ITEM_TYPE_SHORTCUT:
                        case LauncherSettings.Favorites.ITEM_TYPE_SHORTCUT_DOWNLOADING:
                        case LauncherSettings.Favorites.ITEM_TYPE_ALIAPPWIDGET:
                        case LauncherSettings.Favorites.ITEM_TYPE_VPINSTALL:
                        case LauncherSettings.Favorites.ITEM_TYPE_GADGET:
                            UserTrackerHelper.sendUserReport(UserTrackerMessage.MSG_REMOVE_GADGET, item.title.toString());
                            sBgWorkspaceItems.remove(item);
                            break;
                        case LauncherSettings.Favorites.ITEM_TYPE_APPWIDGET:
                            sBgAppWidgets.remove((LauncherAppWidgetInfo) item);
                            break;
                        case LauncherSettings.Favorites.ITEM_TYPE_NOSPACE_APPLICATION:
                            sBgNoSpaceItems.remove(item);
                            break;
                    }
                    sBgItemsIdMap.remove(item.id);
                    try {
                        removeCardBackground(contextFinal, item);
                    } catch (Exception e) {
                    }
                    // client yun icon lost after restore
                    //sBgDbIconCache.remove(item);

                    //only workspace call checkEmptyScreen
                    /*
                    if (container == LauncherSettings.Favorites.CONTAINER_DESKTOP) {
                        checkEmptyScreen(contextFinal, screen);
                    } else */
                    if (container >= 0) {
                        FolderInfo folder = sBgFolders.get(item.container);
                        if ((folder != null) && (folder.contents != null)){
                            folder.contents.remove(item);
                        }
                    }
                    //run checkNoSpaceList after delete complete
                    //to avoid item overlap
                    boolean canCheck = true;
                    if (item.container == Favorites.CONTAINER_DESKTOP) {
                        canCheck = !isScreenEmpty(item.screen);
                    }
                    if (canCheck == true) {
                        postCheckNoSpaceList();
                    }
                }

                sWorker.removeCallbacks(mCheckInvalidPosItemsRunnable);
                sWorker.postDelayed(mCheckInvalidPosItemsRunnable, 3000);
            }
        };
        runOnWorkerThread(r);
    }

    /**
     * Removes the specified item from the another table in database . when
     * uninstall app in one mode,also need delete the app record from table of
     * another mode
     * 
     * @param context
     * @param item
     */
    public static void deleteItemFromAnotherTable(Context context, final ItemInfo item) {
        if (!(item instanceof ShortcutInfo)) {
            return;
        }
        final ContentResolver cr = context.getContentResolver();
        Runnable r = new Runnable() {
            public void run() {
                long id = -1;
                final Cursor c = cr
                        .query(
                                LauncherProvider.getDbAgedModeState() ? LauncherSettings.Favorites.CONTENT_URI_NORMAL_MODE
                                        : LauncherSettings.Favorites.CONTENT_URI_AGED_MODE,
                                null, "intent=?", new String[] {
                                    ((ShortcutInfo) item).intent.toUri(0)
                                }, null);
                try {
                    if (c.moveToNext()) {
                        final int idIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites._ID);
                        id = c.getLong(idIndex);
                    }
                } finally {
                    c.close();
                }
                if (id < 0) {
                    return;
                } else {
                    Uri uriToDelete = LauncherSettings.Favorites.getContentUriForAnotherTable(id,
                            false);
                    int count = cr.delete(uriToDelete, null, null);
                }
            }
        };
        runOnWorkerThread(r);
    }
    static void checkNoSpaceList() {
        Log.d(TAG, "sBgNoSpaceItems size is " + sBgNoSpaceItems.size());
        while (sBgNoSpaceItems.size() != 0) {
            ScreenPosition p = findEmptyCell();
            if (p == null) {
                break;
            }
            ItemInfo item = null;
            synchronized (sBgLock) {
                if (!sBgNoSpaceItems.isEmpty()) {
                    item = sBgNoSpaceItems.get(0);
                } else {
                    return;
                }
                sBgNoSpaceItems.remove(item);
                if (!sBgWorkspaceItems.contains(item)) {
                    sBgWorkspaceItems.add(item);
                }
            }
            item.itemType = LauncherSettings.Favorites.ITEM_TYPE_APPLICATION;
            item.container = LauncherSettings.Favorites.CONTAINER_DESKTOP;
            modifyItemInDatabase(LauncherApplication.getContext(), item, LauncherSettings.Favorites.CONTAINER_DESKTOP,
                                 p.s, p.x, p.y, item.spanX, item.spanY, false);

            final Callbacks callbacks = mCallbacks != null ? mCallbacks.get() : null;
            final ArrayList<ItemInfo> infos = new ArrayList<ItemInfo>(1);
            infos.add(item);
            final Runnable r = new Runnable() {
                @Override
                public void run() {
                    if (callbacks != null) {
                        callbacks.bindItems(infos, 0, infos.size());
                    }
                }
            };
            mHandler.post(r);
        }
    }

    public static void postCheckNoSpaceList() {
        sWorker.removeCallbacks(checkNoSpaceListR);
        sWorker.post(checkNoSpaceListR);
    }

    public static boolean addItemToNoSpaceList(final ShortcutInfo item, int container) {
        synchronized (sBgLock) {
            if (!sBgNoSpaceItems.contains(item)) {
                sBgWorkspaceItems.remove(item);
                sBgNoSpaceItems.add(item);
            } else {
                return false;
            }
        }
        item.itemType = LauncherSettings.Favorites.ITEM_TYPE_NOSPACE_APPLICATION;
        item.container = container;
        modifyItemInDatabase(LauncherApplication.getContext(), item, item.container,
                             -1, -1, -1, item.spanX, item.spanY, false);
        return true;
    }

    public static boolean removeItemFromNoSpaceList(final ShortcutInfo item, int container, int s, int x, int y) {
        synchronized (sBgLock) {
            if (sBgNoSpaceItems.contains(item)) {
                sBgNoSpaceItems.remove(item);
                if (!sBgWorkspaceItems.contains(item)) {
                    sBgWorkspaceItems.add(item);
                }
            } else {
                return false;
            }
        }
        item.itemType = LauncherSettings.Favorites.ITEM_TYPE_APPLICATION;
        item.container = container;
        modifyItemInDatabase(LauncherApplication.getContext(), item, item.container,
                             s, x, y, item.spanX, item.spanY, false);
        return true;
    }

    public static Map<String, Collection<ShortcutInfo>> getAllNoSpaceItemsWithPackages(Collection<String> pkgNames) {
        if (pkgNames == null || pkgNames.isEmpty()) return Collections.emptyMap();
        Map<String, Collection<ShortcutInfo>> result = new HashMap<String, Collection<ShortcutInfo>>();
        for (String pkgName : pkgNames) {
            if (!TextUtils.isEmpty(pkgName)) {
                result.put(pkgName, new ArrayList<ShortcutInfo>());
            }
        }
        if (result.isEmpty()) return result;

        ArrayList<ItemInfo> noSpaceItems = new ArrayList<ItemInfo>();
        synchronized (sBgLock) {
            noSpaceItems.addAll(sBgNoSpaceItems);
        }
        for (ItemInfo item : noSpaceItems) {
            if (item instanceof ShortcutInfo) {
                Intent intent = ((ShortcutInfo) item).intent;
                ComponentName cmpt = intent != null ? intent.getComponent() : null;
                if (cmpt != null && result.containsKey(cmpt.getPackageName())) {
                    result.get(cmpt.getPackageName()).add((ShortcutInfo) item);
                }
            }
        }
        return result;
    }

    /**
     * Remove the contents of the specified folder from the database
     */
    static void deleteFolderContentsFromDatabase(Context context, final FolderInfo info) {
        final ContentResolver cr = context.getContentResolver();

        Runnable r = new Runnable() {
            public void run() {
                cr.delete(LauncherSettings.Favorites.getContentUri(info.id, false), null, null);
                // Lock on mBgLock *after* the db operation
                synchronized (sBgLock) {
                    sBgItemsIdMap.remove(info.id);
                    sBgFolders.remove(info.id);
                    //sBgDbIconCache.remove(info);
                    sBgWorkspaceItems.remove(info);
                }

                cr.delete(LauncherSettings.Favorites.CONTENT_URI_NO_NOTIFICATION,
                        LauncherSettings.Favorites.CONTAINER + "=" + info.id, null);
                // Lock on mBgLock *after* the db operation
                synchronized (sBgLock) {
                    for (ItemInfo childInfo : info.contents) {
                        sBgItemsIdMap.remove(childInfo.id);
                        //sBgDbIconCache.remove(childInfo);
                    }
                }
                sWorker.removeCallbacks(mCheckInvalidPosItemsRunnable);
                sWorker.postDelayed(mCheckInvalidPosItemsRunnable, 3000);
            }
        };
        runOnWorkerThread(r);
    }

    /**
     * Set this as the current Launcher activity object for the loader.
     */
    public void initialize(Callbacks callbacks) {
        synchronized (mLock) {
            mCallbacks = new WeakReference<Callbacks>(callbacks);
        }
    }

    private void onExternalAppavailable(String[] packages) {
        Log.d(TAG, "onExternalAppavailable begin");
        if (mWorkspaceLoaded == false) {
            Log.d(TAG, "workspace no loaded");
            return;
        }
        for(String packagename: packages) {
            Log.d(TAG, "avaliable package name is " + packagename);
            long itemid = -1;
            ArrayList<ItemInfo> allApps = getAllAppItems();
            for(ItemInfo info: allApps) {
                if ((info.itemType == LauncherSettings.Favorites.ITEM_TYPE_NOSPACE_APPLICATION) ||
                    (info.itemType == LauncherSettings.Favorites.ITEM_TYPE_APPLICATION)) {
                    if (((ShortcutInfo)info).intent.getComponent().getPackageName().equals(packagename)) {
                        itemid = info.id;
                        break;
                    }
                }
            }
            //topwise zyf add for exapp
            FileAndInfo fai=null;
            if((fai=getExappApk(packagename))!=null)
            {
            	break;
            }
            //topwise zyf add end
            //topwise zyf add for folderonline
            ApkInfo apkinfo=getApkinfoByPkg(packagename);
        	if(apkinfo!=null)
        	{
        		break;
        	}
            //topwise zyf add end
            if (itemid == -1) {
                //create a item
                runOnWorkerThread(new PackageUpdatedTask(PackageUpdatedTask.OP_ADD, new String[] { packagename }));
            } else {
                Log.d(TAG, "find the item");
                ShortcutInfo info = (ShortcutInfo)sBgItemsIdMap.get(itemid);
                if (info != null) {
                    Drawable icon = mApp.getIconManager().getAppUnifiedIcon(info,null);
                    info.setIcon(icon);
                    try {
                        if ((info.intent != null) && (info.intent.getComponent() != null)) {
                            int appFlags = mApp.getPackageManager().getApplicationInfo(info.intent.getComponent().getPackageName(), 0).flags;
                            ((ShortcutInfo)info).isSDApp = Utils.isSdcardApp(appFlags)?1:0;
                            ((ShortcutInfo)info).customIcon = (((ShortcutInfo)info).isSDApp==0)?false:true;

                            boolean isInAppList = false;
                            for(ApplicationInfo appinfo: mBgAllAppsList.data) {
                                if (appinfo.componentName.equals(info.intent.getComponent())) {
                                    Log.d(TAG, "it is in all app list");
                                    isInAppList = true;
                                    break;
                                }
                            }
                            if (isInAppList == false) {
                                Log.d(TAG, "lit isn't in all app list");
                                mBgAllAppsList.addPackage(mApp, info.intent.getComponent().getPackageName());
                                mBgAllAppsList.added.clear();
                            }
                        }
                    } catch (Exception e) {
                        Log.d(TAG, "PackageManager.getApplicationInfo failed");
                    }
                    final ItemInfo finalinfo = info;
                    updateItemInDatabase(mApp, finalinfo);
                    final Callbacks callbacks = mCallbacks != null ? mCallbacks.get() : null;
                    final ArrayList<ItemInfo> infos = new ArrayList<ItemInfo>(1);
                    infos.add(info);
                    final Runnable r = new Runnable() {
                        @Override
                        public void run() {
                            if (callbacks != null) {
                                Log.d(TAG, "onExternalAppavailable call bindItemsUpdated");
                                callbacks.bindItemsUpdated(infos);
                            }
                        }
                    };
                    runOnMainThread(r);
                }
            }
        }
        Log.d(TAG, "onExternalAppavailable end");
    }

    /**
     * Call from the handler for ACTION_PACKAGE_ADDED, ACTION_PACKAGE_REMOVED and
     * ACTION_PACKAGE_CHANGED.
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        if (DEBUG_LOADERS) Log.d(TAG, "onReceive intent=" + intent);

        final String action = intent.getAction();

        if (Intent.ACTION_PACKAGE_CHANGED.equals(action)
                || Intent.ACTION_PACKAGE_REMOVED.equals(action)
                || Intent.ACTION_PACKAGE_ADDED.equals(action)) {
            final String packageName = intent.getData().getSchemeSpecificPart();
            final boolean replacing = intent.getBooleanExtra(Intent.EXTRA_REPLACING, false);

            int op = PackageUpdatedTask.OP_NONE;

            if (packageName == null || packageName.length() == 0) {
                // they sent us a bad intent
                return;
            }

            if (Intent.ACTION_PACKAGE_CHANGED.equals(action)) {
                op = PackageUpdatedTask.OP_UPDATE;
              //topwise zyf add for exapp ok
                FileAndInfo fai=null;
                if((fai=getExappApk(packageName))!=null)
                {
                	return;
                }
                //topwise zyf add  end
                //topwise zyf add for folderonline
                ApkInfo apkinfo=getApkinfoByPkg(packageName);
            	if(apkinfo!=null)
            	{
            		return;
            	}
                //topwise zyf add end
            } else if (Intent.ACTION_PACKAGE_REMOVED.equals(action)) {
            	 //topwise zyf add for exapp ok
                FileAndInfo fai=null;
                if((fai=getExappApk(packageName))!=null)
                {
                	
                	Intent noinstallIntent=new Intent("com.tpw.install");
         			//noinstallIntent.putExtra("pkgname", fai.mPkgInfo.applicationInfo.packageName);
         			//noinstallIntent.putExtra("apkpath", fai.mFilePath);

         			noinstallIntent.putExtra(com.tpw.homeshell.vpinstall.VPUtils.TYPE_PACKAGEPATH,fai.mFilePath);
         			noinstallIntent.putExtra(com.tpw.homeshell.vpinstall.VPUtils.TYPE_PACKAGENAME,fai.mPkgInfo.applicationInfo.packageName);

         			ShortcutInfo shortcutInfo=(ShortcutInfo)(sApkItemsMap.get(packageName));
         			if(shortcutInfo!=null)
         				shortcutInfo.intent =noinstallIntent;
                	return;
                }
                // //topwise zyf add end
              //topwise zyf add for folderonline
                ApkInfo apkinfo=getApkinfoByPkg(packageName);
            	if(apkinfo!=null)
            	{
            		Intent noinstallIntent=new Intent("com.tpw.install");
         			noinstallIntent.putExtra(com.tpw.homeshell.vpinstall.VPUtils.TYPE_PACKAGEPATH,getOnlineApkFullPath(apkinfo));
         			noinstallIntent.putExtra(com.tpw.homeshell.vpinstall.VPUtils.TYPE_PACKAGENAME,packageName);

         			ShortcutInfo shortcutInfo=(ShortcutInfo)(sApkItemsMap.get(packageName));
         			if(shortcutInfo!=null)
         				shortcutInfo.intent =noinstallIntent;
            		return;
            	}
                //topwise zyf add end
                if (!replacing) {
                    op = PackageUpdatedTask.OP_REMOVE;
                }
                // else, we are replacing the package, so a PACKAGE_ADDED will be sent
                // later, we will update the package at this time
            } else if (Intent.ACTION_PACKAGE_ADDED.equals(action)) {
                if (!replacing) {
                    op = PackageUpdatedTask.OP_ADD;
                    //topwise zyf add for exapp ok
                    FileAndInfo fai=null;
                    if((fai=getExappApk(packageName))!=null)
                    {
                    	return;
                    }
                    //topwise zyf add  end
                    //topwise zyf add for folderonline
                    ApkInfo apkinfo=getApkinfoByPkg(packageName);
                	if(apkinfo!=null)
                	{
                		return;
                	}
                    //topwise zyf add end
                } else {
                    op = PackageUpdatedTask.OP_UPDATE;
                }
            }

            if (op != PackageUpdatedTask.OP_NONE) {
                PackageUpdatedTask task = new PackageUpdatedTask(op, new String[] { packageName });
                mPackageUpdateTaskQueue.enqueue(task);

                if ((op == PackageUpdatedTask.OP_ADD) || (op == PackageUpdatedTask.OP_UPDATE)) {
                    if (BackupManager.getInstance().isInRestore()) {
                        final Context finalcontext = context;
                        sWorker.post(new Runnable() {
                            @Override
                            public void run() {
                                BackupManager.getInstance().addRestoreDoneApp(finalcontext, packageName);
                            }
                        });
                    }
                }
            }
        }else if( AppDownloadManager.ACTION_APP_DWONLOAD_TASK.equals(action) ||
                  AppDownloadManager.ACTION_HS_DOWNLOAD_TASK.equals(action) ){
            String type = intent.getStringExtra(AppDownloadManager.TYPE_ACTION);
            Log.w("zyflauncher", "LaunchMode : nReceiver, ACTION_APP_DWONLOAD_TASK, type = "
            +type+" , action = "+action+" , intent = "+intent);
            if(TextUtils.isEmpty(type)){
                Log.w(TAG, "LaunchMode : nReceiver, ACTION_APP_DWONLOAD_TASK, type == null");
                return;
            }

            Log.d(TAG,"AppDownLoad action:" + action + " type:" + type);
            //start app download
            int op = AppDownloadManager.OP_NULL;
            if(AppDownloadManager.ACTION_APP_DOWNLOAD_START.equals(type)){
                op = AppDownloadManager.OP_APPSTORE_START;
                if (BackupManager.getInstance().isInRestore()) {
                    mReceiveAppDownloadinRestore = true;
                }
            } else if(AppDownloadManager.ACTION_APP_DOWNLOAD_RUNNING.equals(type) ){
                op = AppDownloadManager.OP_APPSTORE_RUNING;
            } else if(AppDownloadManager.ACTION_APP_DOWNLOAD_FAIL.equals(type) ){
                op = AppDownloadManager.OP_APPSTORE_FAIL;
                //checkRestoreDoneIfInRestore(intent);
            } else if(AppDownloadManager.ACTION_APP_DOWNLOAD_CANCEL.equals(type) ){
                op = AppDownloadManager.OP_APPSTORE_CANCEL;
                //checkRestoreDoneIfInRestore(intent);
            }
            Runnable r = mAppDownloadMgr.getAppDownloadTask(op, intent);
            if(r != null) {
                post(r);
            }
        }
 		//topwise zyf add for folderonline
        else if( AppDownloadManager.ACTION_ONLINE_DOWNLOAD_TASK.equals(action))
        {
        	String type = intent.getStringExtra(AppDownloadManager.TYPE_ACTION);
        	Log.d("zyfonline","type = "+type);
            if(AppDownloadManager.ACTION_ONLINE_DOWNLOAD_START.equals(type) ){
                post(mAppDownloadMgr.new OnlineDownloadTask(AppDownloadManager.OP_ONLINE_START, intent));
            } else if(AppDownloadManager.ACTION_ONLINE_DOWNLOAD_RUNNING.equals(type) ){
                post(mAppDownloadMgr.new OnlineDownloadTask(AppDownloadManager.OP_ONLINE_RUNING, intent));
            } else if(AppDownloadManager.ACTION_ONLINE_DOWNLOAD_PAUSE.equals(type) ){
                post(mAppDownloadMgr.new OnlineDownloadTask(AppDownloadManager.OP_ONLINE_PAUSE, intent));
            } else if(AppDownloadManager.ACTION_ONLINE_DOWNLOAD_FAIL.equals(type) ){
                post(mAppDownloadMgr.new OnlineDownloadTask(AppDownloadManager.OP_ONLINE_FAIL, intent));
            } else if(AppDownloadManager.ACTION_ONLINE_DOWNLOAD_CANCEL.equals(type) ){
                post(mAppDownloadMgr.new OnlineDownloadTask(AppDownloadManager.OP_ONLINE_CANCEL, intent));
            }
        }
        //topwise zyf add end
        else if (Intent.ACTION_EXTERNAL_APPLICATIONS_AVAILABLE.equals(action)) {
            // First, schedule to add these apps back in.
            final String[] packages = intent.getStringArrayExtra(Intent.EXTRA_CHANGED_PACKAGE_LIST);
            Runnable r = new Runnable() {
                public void run() {
                    onExternalAppavailable(packages);
                }
            };
            sWorker.post(r);
            //enqueuePackageUpdated(new PackageUpdatedTask(PackageUpdatedTask.OP_ADD, packages));
            // Then, rebind everything.
            //startLoaderFromBackground();
        }

        //for layout change
        else if (ACTION_HOMESHELL_LAYOUT_CHANGE.equals(action)) {
            Log.d(TAG, "receive action homeshell layout change");
            final int countX = intent.getIntExtra(EXTRA_COUNTX, mCellCountX);
            final int countY = intent.getIntExtra(EXTRA_COUNTY, mCellCountY);
            if (LauncherApplication.getLauncher() == null) {
                return;
            }

            if (checkGridSize(countX, countY)) {
                Utils.showLoadingDialog(LauncherApplication.getLauncher());
                sWorker.postDelayed(new Runnable() {

                    @Override
                    public void run() {
                        changeLayout(countX, countY);
                        Utils.dismissLoadingDialog();
                    }
                },200);
            }
        }

        /*
        else if (Intent.ACTION_EXTERNAL_APPLICATIONS_UNAVAILABLE.equals(action)) {
            String[] packages = intent.getStringArrayExtra(Intent.EXTRA_CHANGED_PACKAGE_LIST);
            enqueuePackageUpdated(new PackageUpdatedTask(
                        PackageUpdatedTask.OP_UNAVAILABLE, packages));
        }
        */
        //topwise zyf add  
        else if ("com.tpw.homerefresh".equals(action)) {
        	Runnable r = new Runnable() {
                @Override
                public void run() 
                {
		            if(mApp.getApplicationContext()!=null)
		            {
				        	fillFixedFoldersItems(mApp.getApplicationContext());
				            fillExappList(mApp.getApplicationContext());
				
				            clearExapp(mApp.getApplicationContext(),LauncherProvider.APPS_FOLDER_RESID,FileAndInfo.EXAPP_TYPE_APP);
				            clearExapp(mApp.getApplicationContext(),LauncherProvider.GAMES_FOLDER_RESID,FileAndInfo.EXAPP_TYPE_GAME);
				            clearOnlineApp(mApp.getApplicationContext(),LauncherProvider.APPS_FOLDER_RESID,FileAndInfo.EXAPP_TYPE_APP);
				            clearOnlineApp(mApp.getApplicationContext(),LauncherProvider.GAMES_FOLDER_RESID,FileAndInfo.EXAPP_TYPE_GAME);
				            

								//topwise zyf add for hide folder
								FolderInfo fi_app=addFixedFolder(mApp.getApplicationContext(),LauncherProvider.APPS_FOLDER_RESID,FileAndInfo.EXAPP_TYPE_APP);
								FolderInfo fi_game=addFixedFolder(mApp.getApplicationContext(),LauncherProvider.GAMES_FOLDER_RESID,FileAndInfo.EXAPP_TYPE_GAME);
						

								final ArrayList<ItemInfo> items=new ArrayList<ItemInfo>();
								if(fi_app!=null)
									items.add(fi_app);
								if(fi_game!=null)
									items.add(fi_game);
								if(items.size()>0)
								{
									final Callbacks oldCallbacks = mCallbacks.get();
									final Runnable r = new Runnable() {
										@Override
										public void run() {
											Callbacks callbacks = tryGetCallbacks(oldCallbacks);
											if (callbacks != null) {
											    callbacks.bindItems(items, 0, items.size());
											}
										}
									};
									runOnMainThread(r, MAIN_THREAD_BINDING_RUNNABLE);
								}
								//topwise zyf add for hide folder end
								//add for notify
							   mNewAppNum=0;
							   mNewGameNum=0;
							   //add end
				
								//topwise zyf add for exapp 添加进文件夹
				            Log.d("zyflauncher","homerefresh............addExapp()");
				            addExapp(mApp.getApplicationContext(),LauncherProvider.APPS_FOLDER_RESID,FileAndInfo.EXAPP_TYPE_APP);
				            addExapp(mApp.getApplicationContext(),LauncherProvider.GAMES_FOLDER_RESID,FileAndInfo.EXAPP_TYPE_GAME);
				            //添加桌面切换图标
				            //for folderonline
				            addOnlineApp(mApp.getApplicationContext());
				            //folderonline end
				          checkFolderAndUpdateByUI();
		            }
	            }
            };
            runOnMainThread(r);
	        //topiwse zyf add for notify
            sWorker.postDelayed(mCheckToNotify, 3000);
	        //add end
        }
        //topwise zyf add end
        else if (Intent.ACTION_LOCALE_CHANGED.equals(action)) {
            // If we have changed locale we need to clear out the labels in all apps/workspace.
            //restore the deleted icon code in launcher model
            //FancyIconsHelper.clearCache();
            mApp.getIconManager().clearFancyIconCache();

            GadgetCardHelper.onLocaleChanged();
            //LauncherGadgetHelper.cleanUp();

            CheckVoiceCommandPressHelper.checkEnvironment();

            forceReload();

        } else if (ACTION_AGED_MODE_CHANGED.equals(action)) {
            int agedMode = intent.getIntExtra("aged_mode", 0);
            boolean isAgedMode = (agedMode == AgedModeUtil.AGED_MODE_FLAG_IN_MSG);
            AgedModeUtil.setAgedMode(isAgedMode);
            Log.d(AgedModeUtil.TAG, "Receive the agedmode change message in launcherModel"
                    + isAgedMode);
            stopAllDownloadItems();
            Launcher launcher = LauncherApplication.getLauncher();
            if (launcher != null) {
                if (launcher.isStarted()) {
                    Launcher.mIsAgedMode = isAgedMode;
                    Log.d(AgedModeUtil.TAG,
                            "Launcher has started, so call onAgedModeChanged in onReceive method of LauncherModel"
                                    + isAgedMode);
                    mApp.onAgedModeChanged(isAgedMode, true, true);
                }
                //else if (!launcher.isFinishing()) {
                    //launcher.finish();
                //}
            }
        } else if (ACTION_UPDATE_LAYOUT.equals(action)) {
            String[] pkgNameArr = intent.getStringArrayExtra(HomeShellSetting.EXTRA_NOTIFICATION_PACKAGE_NAMES);
            final Set<String> notifPckgs = pkgNameArr != null ? new HashSet<String>(Arrays.asList(pkgNameArr)) : null;
            sWorker.post(new Runnable() {
                @Override
                public void run() {
                    Log.d(TAG, "handle ACTION_UPDATE_LAYOUT");
                    ArrayList<ItemInfo> allApps = getAllAppItems();
                    ArrayList<ItemInfo> apps = new ArrayList<ItemInfo>();
                    for (ItemInfo info : allApps) {
                        if (info.isNewItem()) {
                            apps.add(info);
                        } else if (notifPckgs != null &&
                                   info instanceof ShortcutInfo) {
                            Intent intent = ((ShortcutInfo) info).intent;
                            if (intent != null) {
                                ComponentName cmpt = intent.getComponent();
                                if (cmpt != null && notifPckgs.contains(cmpt.getPackageName())) {
                                    apps.add(info);
                                }
                            }
                        }
                    }
                    notifyUIUpdateIcon(apps);
                }
            });
        } else if (IconDigitalMarkHandler.ACTION_APPLICATION_NOTIFICATION.equals(action)) {
            IconDigitalMarkHandler.getInstance().handleNotificationIntent(context, intent);
        } else if (Intent.ACTION_PACKAGE_DATA_CLEARED.equals(action)) {
            IconDigitalMarkHandler.getInstance().handleNotificationIntent(context, intent);
        } else if(ACTION_APP_LAUNCHED.equals(action)) {
            final String comp = intent.getExtras().getString("comp");
            handleAppLaunched(comp);
        } else if (HomeShellSetting.ACTION_ON_MARK_TYPE_CHANGE.equals(action)) {
            updateNotificationMarkType();
            return;
        } else if (HomeShellSetting.ACTION_ON_SHOW_NEW_MARK_CHANGE.equals(action)) {
            updateShowNewMark();
            return;
        } else if (Intent.ACTION_UNREAD_CHANGED.equals(action)) {  //start, add by zl 20150721 for show misscall count & unread sms/mms count
        	IconDigitalMarkHandler.getInstance().handleNotificationIntent(context, intent);
        }  //end, add by zl 20150721
        /*
        else if (Intent.ACTION_CONFIGURATION_CHANGED.equals(action)) {
             // Check if configuration change was an mcc/mnc change which would affect app resources
             // and we would need to clear out the labels in all apps/workspace. Same handling as
             // above for ACTION_LOCALE_CHANGED
             Configuration currentConfig = context.getResources().getConfiguration();
             if (mPreviousConfigMcc != currentConfig.mcc) {
                   Log.d(TAG, "Reload apps on config change. curr_mcc:"
                       + currentConfig.mcc + " prevmcc:" + mPreviousConfigMcc);
                   forceReload();
             }
             // Update previousConfig
             mPreviousConfigMcc = currentConfig.mcc;
        }
        */
    }
    
    private void handleAppLaunched(String comp){
        if(!TextUtils.isEmpty(comp)) {
            final String[] strs = comp.split("/");
            sWorker.post(new Runnable() {
                
                @Override
                public void run() {
                    Collection<ItemInfo> apps = getAllAppItems();
                    String pkg = strs[0];
                    Intent intent2 = null;
                    String pkgName = null;
                    ContentValues values =  new ContentValues();
                    values.put(LauncherSettings.Favorites.IS_NEW, 0);
                    
                    for (ItemInfo info : apps) {
                        if (info instanceof ShortcutInfo) {
                            intent2 = ((ShortcutInfo) info).intent;
                            if (intent2 == null || intent2.getComponent() == null || !info.isNewItem()) {
                                continue;
                            }
                            pkgName = intent2.getComponent().getPackageName();
                            if (pkg.equals(pkgName)) {
                                info.setIsNewItem(false);
                                updateItemById(LauncherApplication.getContext(), info.id, values, true);
                            }
                        }
                    }
                }
            });
        }
    }

    private void forceReload() {
        resetLoadedState(true, true);

        // Do this here because if the launcher activity is running it will be restarted.
        // If it's not running startLoaderFromBackground will merely tell it that it needs
        // to reload.
        startLoaderFromBackground();
    }

    public void resetLoadedState(boolean resetAllAppsLoaded, boolean resetWorkspaceLoaded) {
        synchronized (mLock) {
            // Stop any existing loaders first, so they don't set mAllAppsLoaded or
            // mWorkspaceLoaded to true later
            stopLoaderLocked();
            if (resetAllAppsLoaded) mAllAppsLoaded = false;
            if (resetWorkspaceLoaded) mWorkspaceLoaded = false;
        }
    }

    public void setThemeChanged(boolean flag) {
        mThemeChanged = flag;
    }
    /**
     * When the launcher is in the background, it's possible for it to miss paired
     * configuration changes.  So whenever we trigger the loader from the background
     * tell the launcher that it needs to re-run the loader when it comes back instead
     * of doing it now.
     */
    public void startLoaderFromBackground() {
        boolean runLoader = false;
        if (mCallbacks != null) {
            Callbacks callbacks = mCallbacks.get();
            if (callbacks != null) {
                // Only actually run the loader if they're not paused.
                if (!callbacks.setLoadOnResume()) {
                    runLoader = true;
                }
            }
        }
        if (runLoader) {
            startLoader(false, -1);
        }
    }

    // If there is already a loader task running, tell it to stop.
    // returns true if isLaunching() was true on the old task
    private boolean stopLoaderLocked() {
        boolean isLaunching = false;
        LoaderTask oldTask = mLoaderTask;
        if (oldTask != null) {
            if (oldTask.isLaunching()) {
                isLaunching = true;
            }
            oldTask.stopLocked();
        }
        return isLaunching;
    }

    public void startLoader(boolean isLaunching, int synchronousBindPage) {
        synchronized (mLock) {
            if (DEBUG_LOADERS) {
                Log.d(TAG, "startLoader isLaunching=" + isLaunching
                        + " mCallbacks " + mCallbacks
                        + " mCallbacks.get() " + (mCallbacks == null ? null : mCallbacks.get())
                        + " sWorkerThread.isAlive " + (sWorkerThread == null ? null : sWorkerThread.isAlive())
                        + " sWorkerThread.isInterrupted " + (sWorkerThread == null ? null : sWorkerThread.isInterrupted())
                        );
            }

            // Clear any deferred bind-runnables from the synchronized load process
            // We must do this before any loading/binding is scheduled below.
            mDeferredBindRunnables.clear();

            // Don't bother to start the thread if we know it's not going to do anything
            if (mCallbacks != null && mCallbacks.get() != null) {
                // If there is already one running, tell it to stop.
                // also, don't downgrade isLaunching if we're already running
                isLaunching = isLaunching || stopLoaderLocked();
                mLoaderTask = new LoaderTask(mApp, isLaunching);
                if (synchronousBindPage > -1 && mAllAppsLoaded && mWorkspaceLoaded) {
                    Log.d(TAG, "runBindSynchronousPage");
                    mLoaderTask.runBindSynchronousPage(synchronousBindPage);
                } else {
                    Log.d(TAG, "startLoader sWorker.post LoaderTask");
                    sWorkerThread.setPriority(Thread.NORM_PRIORITY);
                    sWorker.post(mLoaderTask);
                }
            }
        }
    }

    class SwitchDB implements Runnable {
        boolean agedMode;

        public SwitchDB(boolean agedMode) {
            this.agedMode = agedMode;
        }
        @Override
        public void run() {
            // TODO Auto-generated method stub
            Log.d("AgedModeUtil.TAG", "switch database,database now agedMode state:" + agedMode);
            mApp.getLauncherProvider().switchDB(agedMode);
        }

    };

    public void bindRemainingSynchronousPages() {
        // Post the remaining side pages to be loaded
        if (!mDeferredBindRunnables.isEmpty()) {
            for (final Runnable r : mDeferredBindRunnables) {
                mHandler.post(r, MAIN_THREAD_BINDING_RUNNABLE);
            }
            mDeferredBindRunnables.clear();
        }
    }

    public void stopLoader() {
        synchronized (mLock) {
            if (mLoaderTask != null) {
                mLoaderTask.stopLocked();
            }
        }
    }

    public boolean isAllAppsLoaded() {
        return mAllAppsLoaded;
    }

    public boolean isLoadingWorkspace() {
        /*
        synchronized (mLock) {
            if (mLoaderTask != null) {
                return mLoaderTask.isLoadingWorkspace();
            }
        }
        return false;
        */
        return mIsLoadingAndBindingWorkspace;
    }

    /**
     * Runnable for the thread that loads the contents of the launcher:
     *   - workspace icons
     *   - widgets
     *   - all apps icons
     */
    private class LoaderTask implements Runnable {
        private static final String ACTION_HOMESHELL_BACKUP =  "com.tpw.homeshell.systembackup.RestoreAllApp";
        private Context mContext;
        private boolean mIsLaunching;
        //private boolean mIsLoadingAndBindingWorkspace;
        private boolean mStopped;
        private boolean mLoadAndBindStepFinished;
        private boolean mIsInStartBinding = false;
        private boolean mIsCurrentScreenBinded = false;

        private HashMap<Object, CharSequence> mLabelCache;

        LoaderTask(Context context, boolean isLaunching) {
            mContext = context;
            mIsLaunching = isLaunching;
            mLabelCache = new HashMap<Object, CharSequence>();
        }

        boolean isLaunching() {
            return mIsLaunching;
        }

        /*
        boolean isLoadingWorkspace() {
            return mIsLoadingAndBindingWorkspace;
        }
        */

        private void loadCurrentScreenWorkspace(int screenID, List<Long> itemsToRemove,
                                            List<ResolveInfo> itemsAllApps, List<ShortcutInfo> itemsInDBForApp,
                                            List<FolderInfo> folderToRemove) {
            Log.d(TAG, "loadCurrentScreenWorkspace in load screen " + screenID);
            final long t = DEBUG_LOADERS ? SystemClock.uptimeMillis() : 0;

            final Context context = mContext;
            final ContentResolver contentResolver = context.getContentResolver();
            final PackageManager manager = context.getPackageManager();
            final AppWidgetManager widgets = AppWidgetManager.getInstance(context);
            final boolean isSafeMode = manager.isSafeMode();
            boolean isWorkspaceContainAliWidget = false;

            //added by lixuhui 2015/02/10 get the misscall count & unread sms/mms count 
            //function to prevent the homeshell process start by notifications when the homeshell process is die or not start, in case to reduce the process start/die and cpu consume
            int misscallCount = UnreadUtils.getMissedCallCount(context);
            int unreadSmsCount = UnreadUtils.getUnreadSmsCount(context) + UnreadUtils.getUnreadMmsCount(context);
            //end
            //final Map<String, GadgetInfo> gadgets = ThemeUtils.listGadgets(mContext);

            synchronized (sBgLock) {
                final Cursor c = contentResolver.query(
                        LauncherSettings.Favorites.CONTENT_URI,
                        null,
                        "container=? or (screen=? and container<>?) or container>?",
                        new String[] {
                            String.valueOf(-101),
                            String.valueOf(screenID),
                            String.valueOf(Favorites.CONTAINER_HIDESEAT),
                            String.valueOf(-1)
                        },
                        null);
                // +1 for the hotseat (it can be larger than the workspace)
                // Load workspace in reverse order to ensure that latest items are loaded first (and
                // before any earlier duplicates)

                try {
                    waitThemeService();
                } catch (Exception e) {
                    Log.d(TAG,"waitThemeService met exception: " + e );
                }

                // +1 for hotseat , +6 for hideseat
                /* HIDESEAT_SCREEN_NUM_MARKER: see ConfigManager.java */
                final ItemInfo occupied[][][] =
                        new ItemInfo[mMaxScreenCount+ 1 + ConfigManager.getHideseatScreenMaxCount()][mCellCountX + 1][mCellCountY + 1];
                try {
                    final int idIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites._ID);
                    final int intentIndex = c.getColumnIndexOrThrow
                            (LauncherSettings.Favorites.INTENT);
                    final int titleIndex = c.getColumnIndexOrThrow
                            (LauncherSettings.Favorites.TITLE);
                    final int iconTypeIndex = c.getColumnIndexOrThrow(
                            LauncherSettings.Favorites.ICON_TYPE);
                    final int iconIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.ICON);
                    final int iconPackageIndex = c.getColumnIndexOrThrow(
                            LauncherSettings.Favorites.ICON_PACKAGE);
                    final int iconResourceIndex = c.getColumnIndexOrThrow(
                            LauncherSettings.Favorites.ICON_RESOURCE);
                    final int containerIndex = c.getColumnIndexOrThrow(
                            LauncherSettings.Favorites.CONTAINER);
                    final int itemTypeIndex = c.getColumnIndexOrThrow(
                            LauncherSettings.Favorites.ITEM_TYPE);
                    final int appWidgetIdIndex = c.getColumnIndexOrThrow(
                            LauncherSettings.Favorites.APPWIDGET_ID);
                    final int screenIndex = c.getColumnIndexOrThrow(
                            LauncherSettings.Favorites.SCREEN);
                    final int cellXIndex = c.getColumnIndexOrThrow
                            (LauncherSettings.Favorites.CELLX);
                    final int cellYIndex = c.getColumnIndexOrThrow
                            (LauncherSettings.Favorites.CELLY);
                    final int spanXIndex = c.getColumnIndexOrThrow
                            (LauncherSettings.Favorites.SPANX);
                    final int spanYIndex = c.getColumnIndexOrThrow(
                            LauncherSettings.Favorites.SPANY);
                    final int msgNumIndex = c.getColumnIndexOrThrow(
                            LauncherSettings.Favorites.MESSAGE_NUM);
                    final int isNewIndex = c.getColumnIndexOrThrow(
                            LauncherSettings.Favorites.IS_NEW);
                    final int canDeleteIndex = c.getColumnIndexOrThrow(
                            LauncherSettings.Favorites.CAN_DELEDE);
                    final int isSDAppIndex = c.getColumnIndexOrThrow(
                            LauncherSettings.Favorites.IS_SDAPP);

                    ShortcutInfo info;
                    String intentDescription;
                    LauncherAppWidgetInfo appWidgetInfo;
                    int container;
                    long id;
                    Intent intent;

                    while (!mStopped && c.moveToNext()) {
                        try {
                            int itemType = c.getInt(itemTypeIndex);
                            id = c.getLong(idIndex);

                            if (c.getInt(containerIndex) == LauncherSettings.Favorites.CONTAINER_DESKTOP) {
                                if ((c.getInt(cellXIndex) >= mCellCountX) ||
                                     (c.getInt(cellYIndex) >= mCellCountY) ||
                                     (c.getInt(spanXIndex) + c.getInt(cellXIndex) > mCellCountX) ||
                                     (c.getInt(spanYIndex) + c.getInt(cellYIndex) > mCellCountY) ||
                                     (c.getInt(screenIndex) >= mMaxScreenCount)) {
                                    itemsToRemove.add(id);
                                    Log.d(TAG, "item position error, id is " + id);
                                    continue;
                                }
                            }

                            switch (itemType) {
                            case LauncherSettings.Favorites.ITEM_TYPE_NOSPACE_APPLICATION:
                            case LauncherSettings.Favorites.ITEM_TYPE_APPLICATION:
                            case LauncherSettings.Favorites.ITEM_TYPE_SHORTCUT:
                            case LauncherSettings.Favorites.ITEM_TYPE_SHORTCUT_DOWNLOADING:
                            case LauncherSettings.Favorites.ITEM_TYPE_VPINSTALL:
                                intentDescription = c.getString(intentIndex);
                                try {
                                    intent = Intent.parseUri(intentDescription, 0);
                                } catch (URISyntaxException e) {
                                    itemsToRemove.add(id);
                                    continue;
                                }
                                //remove all download item when age mode change
                                if ((itemType == LauncherSettings.Favorites.ITEM_TYPE_SHORTCUT_DOWNLOADING) && (mClearAllDownload == true)) {
                                    itemsToRemove.add(id);
                                    continue;
                                }

                                if (itemType == LauncherSettings.Favorites.ITEM_TYPE_VPINSTALL) {
                                    Log.d(TAG, "start check vp install item");
                                    PackageInfo packageInfoInstall = null;
                                    try {
                                        packageInfoInstall = manager.getPackageInfo(intent.getStringExtra("packagename"), 0);
                                    } catch (NameNotFoundException e) {
                                        Log.d(TAG, "the package is not installed");
                                    }
                                    if (packageInfoInstall != null) {
                                        Log.d(TAG, packageInfoInstall.packageName + " is installed");
                                        itemsToRemove.add(id);
                                        continue;
                                    }
                                    if (intent == null) {
                                        itemsToRemove.add(id);
                                        continue;
                                    }
                                    if ((intent.getComponent() == null) || (intent.getComponent().equals(""))){
                                        Log.d(TAG, "component is null");
                                        ComponentName compName = new ComponentName(intent.getStringExtra(VPUtils.TYPE_PACKAGENAME), "vpinstall");
                                        intent.setComponent(compName);
                                    }
                                }

                                if ((itemType == LauncherSettings.Favorites.ITEM_TYPE_APPLICATION) ||
                                    (itemType == LauncherSettings.Favorites.ITEM_TYPE_NOSPACE_APPLICATION)) {
                                    ResolveInfo appInfo = AllAppsList.findActivityInfo(itemsAllApps, intent.getComponent());
                                    if (appInfo == null) {
                                        if (c.getInt(isSDAppIndex) == 0) {
                                            itemsToRemove.add(id);
                                            continue;
                                        }else if (BackupManager.getInstance().isInRestore()) {
                                            if ((intent == null) || (intent.getComponent() == null) ||
                                                 (BackupUitil.isInRestoreList(context, intent.getComponent().getPackageName()) == true)) {
                                                itemsToRemove.add(id);
                                                continue;
                                            }
                                        }
                                    }
                                    if ((intent == null) || (allComponentList.contains(intent.toString()))) {
                                        itemsToRemove.add(id);
                                        continue;
                                    } else {
                                        allComponentList.add(intent.toString());
                                    }
                                    info = getShortcutInfo(manager, intent, context, c, iconIndex,
                                            titleIndex, mLabelCache);
                                    info.itemType = itemType;
                                } else {
                                    info = getShortcutInfo(c, context, iconTypeIndex,
                                            iconPackageIndex, iconResourceIndex, iconIndex,
                                            titleIndex);

                                    // Update the title of VP install apps.
                                    String appPackageFilePath = intent.getStringExtra(VPUtils.TYPE_PACKAGEPATH);
                                    if (!TextUtils.isEmpty(appPackageFilePath)) {
                                        CharSequence vpLabel = VPUtils.getVpinstallLabel(appPackageFilePath);
                                        if (!TextUtils.isEmpty(vpLabel)) {
                                            info.title = vpLabel;
                                        }
                                    }

                                    // App shortcuts that used to be automatically added to Launcher
                                    // didn't always have the correct intent flags set, so do that
                                    // here
                                    if (intent.getAction() != null &&
                                        intent.getCategories() != null &&
                                        intent.getAction().equals(Intent.ACTION_MAIN) &&
                                        intent.getCategories().contains(Intent.CATEGORY_LAUNCHER)) {
                                        intent.addFlags(
                                            Intent.FLAG_ACTIVITY_NEW_TASK |
                                            Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
                                    }
                                }

                                if (info != null) {
                                    info.intent = intent;
                                    info.id = c.getLong(idIndex);
                                    container = c.getInt(containerIndex);
                                    info.container = container;
                                    info.screen = c.getInt(screenIndex);
                                    info.cellX = c.getInt(cellXIndex);
                                    info.cellY = c.getInt(cellYIndex);
                                    info.isNew = c.getInt(isNewIndex);
                                    info.messageNum = c.getInt(msgNumIndex);

                                    //added by lixuhui 2015/02/10 get the misscall count & unread sms/mms count 
                                    //function to prevent the homeshell process start by notifications when the homeshell process is die or not start, in case to reduce the process start/die and cpu consume
                                    if(intent != null){
                                        ComponentName component = intent.getComponent();
                                        if(component != null){
                                            if("com.yunos.alicontacts/.activities.DialtactsContactsActivity".equals(component.flattenToShortString())){
                                                UnreadUtils.syncItemMessageNum(contentResolver, info, misscallCount);
                                            }else if("com.android.mms/.ui.ConversationList".equals(component.flattenToShortString())){
                                                UnreadUtils.syncItemMessageNum(contentResolver, info, unreadSmsCount);
                                            }
                                        }
                                    }
                                    //end
                                    // check & update map of what's occupied
                                    if (itemType != LauncherSettings.Favorites.ITEM_TYPE_NOSPACE_APPLICATION) {
                                        if ((info.screen < 0) || (info.cellX < 0) || (info.cellY < 0) ||
                                            (!checkItemPlacement(occupied, info))) {
                                            //if the item place is occupied by other item,
                                            //or it's position invalid,
                                            //remove it from db, if the item is an app,
                                            //it can be added in below operation,
                                            //other types will not be recovered
                                            itemsToRemove.add(info.id);
                                            break;
                                        }
                                    } else {
                                        Log.d(TAG, "it is a no space item");
                                    }

                                    switch (container) {
                                    case LauncherSettings.Favorites.CONTAINER_DESKTOP:
                                    case LauncherSettings.Favorites.CONTAINER_HOTSEAT:
                                    //add hide icon container
                                    case LauncherSettings.Favorites.CONTAINER_HIDESEAT:
                                        if (itemType == LauncherSettings.Favorites.ITEM_TYPE_NOSPACE_APPLICATION) {
                                            sBgNoSpaceItems.add(info);
                                        } else {
                                            sBgWorkspaceItems.add(info);
                                        }
                                        break;
                                    default:
                                        //if an item in a folder, it's container is folder's id
                                        //so the item's container must not less than 0
                                        if (container >= 0) {
                                            // Item is in a user folder
                                        	//topwise zyf add for fixedfolder
                                        	ItemInfo itInfo= findFixedFolderInArr(container);
                                        	if(itInfo!=null&&itInfo.itemType==LauncherSettings.Favorites.ITEM_TYPE_FIXED_FOLDER)
                                        	{
                                        		FolderInfo folderInfo =
                                        				findOrMakeFixedFolder(sBgFolders, container);
                                                folderInfo.add(info);
                                        	}
                                        	else {
                                        		FolderInfo folderInfo =
                                                        findOrMakeFolder(sBgFolders, container);
                                                folderInfo.add(info);
    										}
                                        	
                                        	/*
                                            FolderInfo folderInfo =
                                                    findOrMakeFolder(sBgFolders, container);
                                            folderInfo.add(info);
                                            */
                                        	//topwise zyf add end
                                        }
                                        break;
                                    }
                                    sBgItemsIdMap.put(info.id, info);

                                    // if the shortcut is for applicaiton, add it to itemsInDBForApp
                                    if ((info.itemType == LauncherSettings.Favorites.ITEM_TYPE_APPLICATION) ||
                                        (info.itemType == LauncherSettings.Favorites.ITEM_TYPE_NOSPACE_APPLICATION)){
                                        itemsInDBForApp.add(info);
                                    }

                                    if (info.itemType != LauncherSettings.Favorites.ITEM_TYPE_NOSPACE_APPLICATION) {
                                        if (mIconManager.supprtCardIcon() == true) {
                                            mIconManager.getAppCardBackgroud(info);
                                        }
                                    }
                                } else {
                                    // Failed to load the shortcut, probably because the
                                    // activity manager couldn't resolve it (maybe the app
                                    // was uninstalled), or the db row was somehow screwed up.
                                    // Delete it.
                                    id = c.getLong(idIndex);
                                    Log.e(TAG, "Error loading shortcut " + id + ", removing it");
                                    contentResolver.delete(LauncherSettings.Favorites.getContentUri(
                                                id, false), null, null);
                                }
                                break;

                            case LauncherSettings.Favorites.ITEM_TYPE_FOLDER:
                                id = c.getLong(idIndex);
                                FolderInfo folderInfo = findOrMakeFolder(sBgFolders, id);

                                folderInfo.title = c.getString(titleIndex);
                                updateFolderTitle(folderInfo);
                                folderInfo.id = id;
                                container = c.getInt(containerIndex);
                                folderInfo.container = container;
                                folderInfo.screen = c.getInt(screenIndex);
                                folderInfo.cellX = c.getInt(cellXIndex);
                                folderInfo.cellY = c.getInt(cellYIndex);

                                // check & update map of what's occupied
                                if (!checkItemPlacement(occupied, folderInfo)) {
                                    itemsToRemove.add(folderInfo.id);
                                    if (sBgFolders.containsKey(folderInfo.id)) {
                                        sBgFolders.remove(folderInfo.id);
                                    }
                                    if (sBgItemsIdMap.containsKey(folderInfo.id)) {
                                        sBgItemsIdMap.remove(folderInfo.id);
                                    }
                                    break;
                                }
                                switch (container) {
                                    case LauncherSettings.Favorites.CONTAINER_DESKTOP:
                                    case LauncherSettings.Favorites.CONTAINER_HOTSEAT:
                                    case LauncherSettings.Favorites.CONTAINER_HIDESEAT:
                                        sBgWorkspaceItems.add(folderInfo);
                                        break;
                                }

                                sBgItemsIdMap.put(folderInfo.id, folderInfo);
                                sBgFolders.put(folderInfo.id, folderInfo);
                                break;
//topwise zyf add for fixedfolder
                            case LauncherSettings.Favorites.ITEM_TYPE_FIXED_FOLDER:
                            {
                                id = c.getLong(idIndex);
                                FolderInfo fi = findOrMakeFixedFolder(sBgFolders, id);
                                fi.title = c.getString(titleIndex);
                                updateFolderTitle(fi);
                                fi.id = id;
                                container = c.getInt(containerIndex);
                                fi.container = container;
                                fi.screen = c.getInt(screenIndex);
                                fi.cellX = c.getInt(cellXIndex);
                                fi.cellY = c.getInt(cellYIndex);
                              //topwise zyf add for multi-language
								   String tempintent=c.getString(intentIndex);
									if(tempintent!=null)
									{
										if(tempintent.equals(LauncherSettings.Favorites.INTNET_APPS))
										{
											fi.itemExtraType=ItemInfo.ITEM_EXTRA_TYPE_APPS;
										}
										else if(tempintent.equals(LauncherSettings.Favorites.INTNET_GAMES)) 
										{
											fi.itemExtraType=ItemInfo.ITEM_EXTRA_TYPE_GAMES;
										}
									}
//topwise zyf add for multi-language end

                                // check & update map of what's occupied
                                if (!checkItemPlacement(occupied, fi)) {
                                    itemsToRemove.add(fi.id);
                                    if (sBgFolders.containsKey(fi.id)) {
                                        sBgFolders.remove(fi.id);
                                    }
                                    if (sBgItemsIdMap.containsKey(fi.id)) {
                                        sBgItemsIdMap.remove(fi.id);
                                    }
                                    break;
                                }
                                switch (container) {
                                    case LauncherSettings.Favorites.CONTAINER_DESKTOP:
                                    case LauncherSettings.Favorites.CONTAINER_HOTSEAT:
                                    case LauncherSettings.Favorites.CONTAINER_HIDESEAT:
                                        sBgWorkspaceItems.add(fi);
                                        break;
                                }

                                sBgItemsIdMap.put(fi.id, fi);
                                sBgFolders.put(fi.id, fi);
                            }
                                break;
//topwise zyf add end
                            case LauncherSettings.Favorites.ITEM_TYPE_APPWIDGET:
                                // Read all Launcher-specific widget details
                                int appWidgetId = c.getInt(appWidgetIdIndex);
                                // to restore widget which is bound to the system application
                                intentDescription = c.getString(intentIndex);
                                try {
                                    intent = Intent.parseUri(intentDescription, 0);
                                } catch (URISyntaxException e) {
                                    itemsToRemove.add(id);
                                    continue;
                                }

                                AppWidgetProviderInfo provider =
                                        widgets.getAppWidgetInfo(appWidgetId);
                                if (BackupManager.getInstance().isInRestore()) {
                                    try {
                                        Log.d(TAG, "widget loading in restore mode");
                                        AppWidgetHost host = new AppWidgetHost(context, Launcher.APPWIDGET_HOST_ID);
                                        int tmpID = host.allocateAppWidgetId();
                                        final AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(mContext);
                                        ACA.AppWidgetManager.bindAppWidgetId(appWidgetManager, tmpID, intent.getComponent());
                                        appWidgetId = tmpID;
                                        //update db
                                        provider = widgets.getAppWidgetInfo(appWidgetId);
                                    } catch (Exception e) {
                                        Log.d(TAG, "restore system's widget met:" + e);
                                    }
                                }

                                if (!isSafeMode && (provider == null || provider.provider == null ||
                                        provider.provider.getPackageName() == null)) {
                                    String log = "Deleting widget that isn't installed anymore: id="
                                        + id + " appWidgetId=" + appWidgetId;
                                    Log.e(TAG, log);
                                    //Launcher.sDumpLogs.add(log);
                                    itemsToRemove.add(id);
                                } else {
                                    appWidgetInfo = new LauncherAppWidgetInfo(appWidgetId,
                                            provider.provider);
                                    appWidgetInfo.id = id;
                                    appWidgetInfo.screen = c.getInt(screenIndex);
                                    appWidgetInfo.cellX = c.getInt(cellXIndex);
                                    appWidgetInfo.cellY = c.getInt(cellYIndex);
                                    appWidgetInfo.spanX = c.getInt(spanXIndex);
                                    appWidgetInfo.spanY = c.getInt(spanYIndex);
                                    int[] minSpan = Launcher.getMinSpanForWidget(context, provider);
                                    appWidgetInfo.minSpanX = minSpan[0];
                                    appWidgetInfo.minSpanY = minSpan[1];

                                    container = c.getInt(containerIndex);
                                    if (container != LauncherSettings.Favorites.CONTAINER_DESKTOP &&
                                        container != LauncherSettings.Favorites.CONTAINER_HOTSEAT &&
                                        container != LauncherSettings.Favorites.CONTAINER_HIDESEAT) {
                                        Log.e(TAG, "Widget found where container != " +
                                            "CONTAINER_DESKTOP nor CONTAINER_HOTSEAT nor CONTAINER_HIDESEAT- ignoring!");
                                        continue;
                                    }
                                    appWidgetInfo.container = c.getInt(containerIndex);

                                    // check & update map of what's occupied
                                    if (!checkItemPlacement(occupied, appWidgetInfo)) {
                                        break;
                                    }
                                    sBgItemsIdMap.put(appWidgetInfo.id, appWidgetInfo);
                                    sBgAppWidgets.add(appWidgetInfo);
                                }
                                break;
                            /*case LauncherSettings.Favorites.ITEM_TYPE_GADGET:
                                String strType = c.getString(titleIndex);
                                    if (!TextUtils.isEmpty(strType)) {
                                        GadgetInfo ginfo = gadgets.get(strType);
                                        if (ginfo != null) {
                                            GadgetItemInfo gi = new GadgetItemInfo(ginfo);
                                            gi.id = c.getLong(idIndex);
                                            gi.screen = c.getInt(screenIndex);
                                            gi.cellX = c.getInt(cellXIndex);
                                            gi.cellY = c.getInt(cellYIndex);
                                            gi.spanX = c.getInt(spanXIndex);
                                            gi.spanY = c.getInt(spanYIndex);
                                            sBgWorkspaceItems.add(gi);
                                            sBgItemsIdMap.put(gi.id, gi);
                                        }
                                    }
                                break;*/
                            }
                        } catch (Exception e) {
                            Log.w(TAG, "Desktop items loading interrupted:", e);
                        }
                    }
                } finally {
                    c.close();
                }

                if (mStopped != true) {
                    //check folder should be done when mStopped isn't true
                    //Sometimes Launcher activity is started two times when phone power up
                    //and loadworkspace will be teminated during first start.
                    //empty folder in first screen not removed in restore
                    Log.d(TAG, "check empty folder");
                    for (FolderInfo info: sBgFolders.values()) {
                        if ((info != null) && (info.contents != null)) {
                            Log.d(TAG, "folder " + info.title + " size is " + info.contents.size());
                            if (info.contents.size() == 0) {
                                if(folderToRemove.contains(info) == false) {
                                    //topwise zyf add for hide folder
									if(!isShowHideFolder(info))
									//topwise zyf add for hide folder end
									folderToRemove.add(info);
                                }
                            } else if ((info.contents.size() == 1) &&
                                       (BackupManager.getInstance().isInRestore() == false)){
                                Log.d(TAG, "folder id is " + info.id);
                                ShortcutInfo itemInFolder = info.contents.get(0);
                                if (itemInFolder != null) {
                                    //replace the folder with the item in it
                                    itemInFolder.cellX = info.cellX;
                                    itemInFolder.cellY = info.cellY;
                                    itemInFolder.container = info.container;
                                    itemInFolder.screen = info.screen;
                                    updateItemInDatabase(mContext, itemInFolder);
                                }
                                if(folderToRemove.contains(info) == false) {
                                    //topwise zyf add for hide folder
									if(!isShowHideFolder(info))
									//topwise zyf add for hide folder end
									folderToRemove.add(info);
									
                                }
                            }
                        }
                    }
                    //since all items in folders is loaded, remove empty folders to
                    //avoid they are displayed in workspace
                    for (FolderInfo info: folderToRemove) {
                        final ItemInfo iteminfo = (ItemInfo) info;
                        Log.d(TAG, "remove folder " + info.title);
                        deleteItemFromDatabase(mApp, iteminfo);
                    }
                }

                folderToRemove.clear();

                if (DEBUG_LOADERS) {
                    Log.d(TAG, "loaded one screen " + screenID + " workspace in " + (SystemClock.uptimeMillis()-t) + "ms");
                }
            }
            Log.d(TAG, "loadOneScreenWorkspace out");
        }

        private void bindCurrentScreenWorkspace(int synchronizeBindPage) {
            final long t = SystemClock.uptimeMillis();
            Runnable r;

            // Don't use these two variables in any of the callback runnables.
            // Otherwise we hold a reference to them.
            final Callbacks oldCallbacks = mCallbacks.get();
            if (oldCallbacks == null) {
                // This launcher has exited and nobody bothered to tell us.  Just bail.
                Log.w(TAG, "LoaderTask running with no launcher");
                return;
            }

            final boolean isLoadingSynchronously = (synchronizeBindPage > -1);
            final int currentScreen = 0;//isLoadingSynchronously ? synchronizeBindPage :
                //oldCallbacks.getCurrentWorkspaceScreen();

            // Load all the items that are on the current page first (and in the process, unbind
            // all the existing workspace items before we call startBinding() below.
            if (mIsInStartBinding == false) {
                unbindWorkspaceItemsOnMainThread();
            }
            ArrayList<ItemInfo> workspaceItems = new ArrayList<ItemInfo>();
            ArrayList<LauncherAppWidgetInfo> appWidgets =
                    new ArrayList<LauncherAppWidgetInfo>();
            HashMap<Long, FolderInfo> folders = new HashMap<Long, FolderInfo>();
            HashMap<Long, ItemInfo> itemsIdMap = new HashMap<Long, ItemInfo>();
            synchronized (sBgLock) {
                workspaceItems.addAll(sBgWorkspaceItems);
                appWidgets.addAll(sBgAppWidgets);
                folders.putAll(sBgFolders);
                itemsIdMap.putAll(sBgItemsIdMap);
            }

            ArrayList<ItemInfo> currentWorkspaceItems = new ArrayList<ItemInfo>();
            ArrayList<ItemInfo> otherWorkspaceItems = new ArrayList<ItemInfo>();
            ArrayList<LauncherAppWidgetInfo> currentAppWidgets =
                    new ArrayList<LauncherAppWidgetInfo>();
            ArrayList<LauncherAppWidgetInfo> otherAppWidgets =
                    new ArrayList<LauncherAppWidgetInfo>();
            HashMap<Long, FolderInfo> currentFolders = new HashMap<Long, FolderInfo>();
            HashMap<Long, FolderInfo> otherFolders = new HashMap<Long, FolderInfo>();

            // Separate the items that are on the current screen, and all the other remaining items
            filterCurrentWorkspaceItems(currentScreen, workspaceItems, currentWorkspaceItems,
                    otherWorkspaceItems);
            filterCurrentAppWidgets(currentScreen, appWidgets, currentAppWidgets,
                    otherAppWidgets);
            filterCurrentFolders(currentScreen, itemsIdMap, folders, currentFolders,
                    otherFolders);
            sortWorkspaceItemsSpatially(currentWorkspaceItems);

            // Tell the workspace that we're about to start binding items
            r = new Runnable() {
                public void run() {
                    Callbacks callbacks = tryGetCallbacks(oldCallbacks);
                    if (callbacks != null) {
                        callbacks.startBinding();
                    }
                }
            };
            if (mIsInStartBinding == false) {
                runOnMainThread(r, MAIN_THREAD_BINDING_RUNNABLE);
                mIsInStartBinding = true;
            }

            // Load items on the current page
            bindWorkspaceItems(oldCallbacks, currentWorkspaceItems, currentAppWidgets,
                    currentFolders, null);

            // Tell the workspace that we're done binding items
            r = new Runnable() {
                public void run() {
                    // If we're profiling, ensure this is the last thing in the queue.
                    if (DEBUG_LOADERS) {
                        Log.d(TAG, "bound one screen " + currentScreen + " workspace in "
                            + (SystemClock.uptimeMillis()-t) + "ms");
                    }
                }
            };

            runOnMainThread(r, MAIN_THREAD_BINDING_RUNNABLE);

            mIsCurrentScreenBinded = true;
        }

        private void loadAndBindWorkspace() {
            mIsLoadingAndBindingWorkspace = true;

            // Load the workspace
            if (DEBUG_LOADERS) {
                Log.d(TAG, "loadAndBindWorkspace mWorkspaceLoaded=" + mWorkspaceLoaded);
            }
            if (!mWorkspaceLoaded) {
                //topwise zyf add for notify
                mWorkspaceLoadCount++;
                //topwise zyf add end
                loadWorkspace();
                synchronized (LoaderTask.this) {
                    if (mStopped) {
                        return;
                    }
                    mWorkspaceLoaded = true;
                }
            }

            boolean isMaxYSupported = true;
            if (mCellCountY == ConfigManager.getCellMaxCountY()) {
                isMaxYSupported = checkGridSize(mCellCountX, mCellCountY);
            }
            if (isMaxYSupported == true) {
                // Bind the workspace
                bindWorkspace(-1);
            } else {
                changeLayout(mCellCountX, mCellCountY - 1);
                bindWorkspace(-1);
                if (homeshellSetting != null) {
                    homeshellSetting.updateLayoutPreference();
                }
            }

            if (DEBUG_LOADERS) Log.d(TAG,"isInRestore="+
                    BackupManager.getInstance().isInRestore());
            if (DEBUG_LOADERS) Log.d(TAG,"mThemeChanged="+ mThemeChanged);
            if (BackupManager.getInstance().isInRestore() && !mThemeChanged) {
                if (DEBUG_LOADERS) Log.d(TAG, "In restore mode," +
                    "send a broadcast to homeshell self");
                // set it to false here, if it's will be used by others, we need redesign
                mThemeChanged = false;
                if (BackupManager.getIsRestoreAppFlag(mContext)) {
                    if (DEBUG_LOADERS) Log.d(TAG, "sendBroadcast(ACTION_HOMESHELL_BACKUP)");
                    mContext.sendBroadcast(new Intent(ACTION_HOMESHELL_BACKUP));
                    if (DEBUG_LOADERS) Log.d(TAG, "Disable VPInstall flag");
                    ConfigManager.setVPInstallEnable(false);
                }
            }
        }

        private void waitForIdle() {
            // Wait until the either we're stopped or the other threads are done.
            // This way we don't start loading all apps until the workspace has settled
            // down.
            synchronized (LoaderTask.this) {
                final long workspaceWaitTime = DEBUG_LOADERS ? SystemClock.uptimeMillis() : 0;

                mHandler.postIdle(new Runnable() {
                        public void run() {
                            synchronized (LoaderTask.this) {
                                mLoadAndBindStepFinished = true;
                                if (DEBUG_LOADERS) {
                                    Log.d(TAG, "done with previous binding step");
                                }
                                LoaderTask.this.notify();
                            }
                        }
                    });

                while (!mStopped && !mLoadAndBindStepFinished && !mFlushingWorkerThread) {
                    try {
                        // Just in case mFlushingWorkerThread changes but we aren't woken up,
                        // wait no longer than 1sec at a time
                        this.wait(1000);
                    } catch (InterruptedException ex) {
                        // Ignore
                    }
                }
                if (DEBUG_LOADERS) {
                    Log.d(TAG, "waited "
                            + (SystemClock.uptimeMillis()-workspaceWaitTime)
                            + "ms for previous step to finish binding");
                }
            }
        }

        void runBindSynchronousPage(int synchronousBindPage) {
            if (synchronousBindPage < 0) {
                // Ensure that we have a valid page index to load synchronously
                throw new RuntimeException("Should not call runBindSynchronousPage() without " +
                        "valid page index");
            }
            if (!mAllAppsLoaded || !mWorkspaceLoaded) {
                // Ensure that we don't try and bind a specified page when the pages have not been
                // loaded already (we should load everything asynchronously in that case)
                throw new RuntimeException("Expecting AllApps and Workspace to be loaded");
            }
            synchronized (mLock) {
                if (mIsLoaderTaskRunning) {
                    // Ensure that we are never running the background loading at this point since
                    // we also touch the background collections
                    throw new RuntimeException("Error! Background loading is already running");
                }
            }

            // XXX: Throw an exception if we are already loading (since we touch the worker thread
            //      data structures, we can't allow any other thread to touch that data, but because
            //      this call is synchronous, we can get away with not locking).

            // The LauncherModel is static in the LauncherApplication and mHandler may have queued
            // operations from the previous activity.  We need to ensure that all queued operations
            // are executed before any synchronous binding work is done.
            mHandler.flush();

            // Divide the set of loaded items into those that we are binding synchronously, and
            // everything else that is to be bound normally (asynchronously).
            bindWorkspace(synchronousBindPage);
            // XXX: For now, continue posting the binding of AllApps as there are other issues that
            //      arise from that.
            onlyBindAllApps();
        }

        public void run() {
            synchronized (mLock) {
                mIsLoaderTaskRunning = true;
            }
            Log.d(TAG, "run loadertask");
            // Optimize for end-user experience: if the Launcher is up and // running with the
            // All Apps interface in the foreground, load All Apps first. Otherwise, load the
            // workspace first (default).
            final Callbacks cbk = mCallbacks.get();
            final boolean loadWorkspaceFirst = cbk != null ? (!cbk.isAllAppsVisible()) : true;

            keep_running: {
                // Elevate priority when Home launches for the first time to avoid
                // starving at boot time. Staring at a blank home is not cool.
                synchronized (mLock) {
                    if (DEBUG_LOADERS) Log.d(TAG, "Setting thread priority to " +
                            (mIsLaunching ? "DEFAULT" : "BACKGROUND"));
                    android.os.Process.setThreadPriority(mIsLaunching
                            ? Process.THREAD_PRIORITY_DEFAULT : Process.THREAD_PRIORITY_BACKGROUND);
                }
                if (loadWorkspaceFirst) {
                    if (DEBUG_LOADERS) Log.d(TAG, "step 1: loading workspace");
                    loadAndBindWorkspace();
                } else {
                    if (DEBUG_LOADERS) Log.d(TAG, "step 1: special: loading all apps");
                    loadAndBindAllApps();
                }

                if (mStopped) {
                    break keep_running;
                }

                // Whew! Hard work done.  Slow us down, and wait until the UI thread has
                // settled down.
                synchronized (mLock) {
                    if (mIsLaunching) {
                        if (DEBUG_LOADERS) Log.d(TAG, "Setting thread priority to BACKGROUND");
                        android.os.Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
                    }
                }
                waitForIdle();

                // second step
                if (loadWorkspaceFirst) {
                    if (DEBUG_LOADERS) Log.d(TAG, "step 2: loading all apps");
                    loadAndBindAllApps();
                } else {
                    if (DEBUG_LOADERS) Log.d(TAG, "step 2: special: loading workspace");
                    loadAndBindWorkspace();
                }

                try {
                    findAndFixInvalidItems();
                } catch (Exception ex) {
                    Log.e(TAG, "findAndFixInvalidItems exception");
                }

                //After update, set mThemeChanged to false, means the theme change finish
                setThemeChanged(false);

                // Restore the default thread priority after we are done loading items
                synchronized (mLock) {
                    android.os.Process.setThreadPriority(Process.THREAD_PRIORITY_DEFAULT);
                }
            }

            // client yun icon lost after restore
            // Update the saved icons if necessary
            //if (DEBUG_LOADERS) Log.d(TAG, "Comparing loaded icons to database icons");
            //synchronized (sBgLock) {
            //    for (Object key : sBgDbIconCache.keySet()) {
            //        updateSavedIcon(mContext, (ShortcutInfo) key, sBgDbIconCache.get(key));
            //    }
            //    sBgDbIconCache.clear();
            //}

            // Send broadcast to app store.
            mContext.sendBroadcast(new Intent(ACTION_RELOAD_DOWNLOADING));
            if (DEBUG_LOADERS) Log.d(TAG, "Send reload downloading broadcast.");

            // Clear out this reference, otherwise we end up holding it until all of the
            // callback runnables are done.
            mContext = null;

            synchronized (mLock) {
                // If we are still the last one to be scheduled, remove ourselves.
                if (mLoaderTask == this) {
                    mLoaderTask = null;
                }
                mIsLoaderTaskRunning = false;
            }
        }

        public void stopLocked() {
            synchronized (LoaderTask.this) {
                mStopped = true;
                this.notify();
            }
        }

        // check & update map of what's occupied; used to discard overlapping/invalid items
        private boolean checkItemPlacement(ItemInfo occupied[][][], ItemInfo item) {
            int containerIndex = item.screen;
            if (item.container == LauncherSettings.Favorites.CONTAINER_HOTSEAT) {
                // Return early if we detect that an item is under the hotseat button
                if (mCallbacks == null) {
                    return false;
                }
                Callbacks cb = mCallbacks.get();
                if (cb == null || cb.isAllAppsButtonRank(item.screen)) {
                    return false;
                }
                // We use the last index to refer to the hotseat and the screen as the rank, so
                // test and update the occupied state accordingly
                if (occupied[mMaxScreenCount][item.screen][0] != null) {
                    Log.e(TAG, "Error loading shortcut into hotseat " + item
                        + " into position (" + item.screen + ":" + item.cellX + "," + item.cellY
                        + ") occupied by " + occupied[mMaxScreenCount][item.screen][0]);
                    return false;
                } else {
                    occupied[mMaxScreenCount][item.screen][0] = item;
                    return true;
                }
            }
            //add hide icon container
            if (item.container == LauncherSettings.Favorites.CONTAINER_HIDESEAT) {
                // Return early if we detect that an item is under the hotseat button
                if (mCallbacks == null) {
                    return false;
                }
                Callbacks cb = mCallbacks.get();
                if (cb == null || cb.isAllAppsButtonRank(item.screen)) {
                    return false;
                }
                
                return true;
                /*
                int screenIndex = mMaxScreenCount + 1 + item.screen;
                // We use the last index to refer to the hotseat and the screen as the rank, so
                // test and update the occupied state accordingly
                if (occupied[screenIndex][item.cellX][item.cellY] != null) {
                    Log.e(TAG, "Error loading shortcut into hotseat " + item
                        + " into position (" + item.screen + ":" + item.cellX + "," + item.cellY
                        + ") occupied by " + occupied[screenIndex][item.cellX][item.cellY]);
                    return false;
                } else {
                    occupied[screenIndex][item.cellX][item.cellY] = item;
                    return true;
                }
                */
            }
            
            else if (item.container != LauncherSettings.Favorites.CONTAINER_DESKTOP) {
                // Skip further checking if it is not the hotseat or workspace container
                return true;
            }

            // Check if any workspace icons overlap with each other
            for (int x = item.cellX; x < (item.cellX+item.spanX); x++) {
                for (int y = item.cellY; y < (item.cellY+item.spanY); y++) {
                    if (occupied[containerIndex][x][y] != null) {
                        Log.e(TAG, "Error loading shortcut " + item
                            + " into cell (" + containerIndex + "-" + item.screen + ":"
                            + x + "," + y
                            + ") occupied by "
                            + occupied[containerIndex][x][y]);
                        return false;
                    }
                }
            }
            for (int x = item.cellX; x < (item.cellX+item.spanX); x++) {
                for (int y = item.cellY; y < (item.cellY+item.spanY); y++) {
                    occupied[containerIndex][x][y] = item;
                }
            }

            return true;
        }

        private boolean findAndUpdateDownloadItem(ResolveInfo resolveInfo) {
            Log.d(TAG, "findAndUpdateDownloadItem in");
            ApplicationInfo app = new ApplicationInfo(mContext.getPackageManager(), resolveInfo,null);
            ArrayList<ItemInfo> allItems = getAllAppItems();
            for (ItemInfo info: allItems) {
                if (info.itemType == LauncherSettings.Favorites.ITEM_TYPE_SHORTCUT_DOWNLOADING){
                    if(app.componentName.getPackageName().equals(((ShortcutInfo)info).intent.getStringExtra(AppDownloadManager.TYPE_PACKAGENAME))) {
                        Log.d(TAG, "it is a downloading item");
                        info.itemType = LauncherSettings.Favorites.ITEM_TYPE_APPLICATION;
                        info.setIsNewItem(true);
                        info.title = app.title;

                        ((ShortcutInfo)info).intent = app.intent;
                        ((ShortcutInfo)info).setAppDownloadStatus(AppDownloadStatus.STATUS_INSTALLED);

                        LauncherApplication application = (LauncherApplication)mContext.getApplicationContext();
                        Drawable icon = mApp.getIconManager().getAppUnifiedIcon(app,null);
                        ((ShortcutInfo) info).setIcon(icon);

                        ((ShortcutInfo)info).isSDApp = Utils.isSdcardApp(resolveInfo.activityInfo.applicationInfo.flags)?1:0;
                        ((ShortcutInfo)info).customIcon = (((ShortcutInfo)info).isSDApp==0)?false:true;
                        final ContentValues values = new ContentValues();
                        final ContentResolver cr = mContext.getContentResolver();
                        info.onAddToDatabase(values);

                        if (((ShortcutInfo)info).isSDApp == 1) {
                            Drawable origIcon = VPUtils.getAppOriginalIcon(mContext, (ShortcutInfo)info);
                            if (origIcon != null) {
                                ItemInfo.writeBitmap(values, origIcon);
                            } else {
                                if(values.containsKey(LauncherSettings.Favorites.ICON)) {
                                    values.remove(LauncherSettings.Favorites.ICON);
                                }
                            }
                        } else {
                            values.put(LauncherSettings.Favorites.ICON, new byte[0]);
                        }

                        cr.update(LauncherSettings.Favorites.CONTENT_URI_NO_NOTIFICATION, values,
                                "_id =?", new String[] { String.valueOf(info.id) });
                        return true;
                    }
                }
            }
            Log.d(TAG, "findAndUpdateDownloadItem out");
            return false;
        }

        private void waitThemeService() {
            final PackageManager pm = mContext.getPackageManager();
            boolean supportTheme = false;
            int count = 0;
            if (pm != null) {
                try {
                    pm.getPackageInfo("com.yunos.theme.themeservice", PackageManager.GET_SERVICES);
                    supportTheme = true;
                } catch (NameNotFoundException e) {
                    e.printStackTrace();
                }
            }
            if (supportTheme) {
                Object srv = ACA.ServiceManager.getService("assetredirectionex");
                Log.d(TAG, "theme service existed:" + srv);
                while (count < 10) {
                    if (srv == null) {
                        try {
                            Thread.sleep(200);
                        } catch (Exception e) {
                        }
                        srv = ACA.ServiceManager.getService("assetredirectionex");
                        count ++;
                        Log.d(TAG, "after sleep "+ (200*count)+"ms, theme service existed:" + srv);
                    } else {
                        break;
                    }
                }
            }
        }
        private void loadWorkspace() {
            Log.d(TAG, "loadWorkspace in");
            final long t = DEBUG_LOADERS ? SystemClock.uptimeMillis() : 0;

            final Context context = mContext;
            final ContentResolver contentResolver = context.getContentResolver();
            final PackageManager manager = context.getPackageManager();
            final AppWidgetManager widgets = AppWidgetManager.getInstance(context);
            final boolean isSafeMode = manager.isSafeMode();
            boolean isWorkspaceContainAliWidget = false;

            // Make sure the default workspace is loaded, if needed
            mApp.getLauncherProvider().loadDefaultFavoritesIfNecessary(0);
            //towise zyf add for fixedfolder
            fillFixedFoldersItems(context);
            fillExappList(context);
            //towise zyf add end
            //final Map<String, GadgetInfo> gadgets = ThemeUtils
            //        .listGadgets(mContext);

            synchronized (sBgLock) {
                sBgWorkspaceItems.clear();
                sBgAppWidgets.clear();
                sBgFolders.clear();
                sBgItemsIdMap.clear();
                //sBgDbIconCache.clear();
                sBgNoSpaceItems.clear();
                final ArrayList<Long> itemsToRemove = new ArrayList<Long>();
                Log.d(TAG, "before get all app list");
                if(appsOnBoot!=null){
                    appsOnBoot.clear();
                    appsOnBoot = null;
                }
                appsOnBoot = AllAppsList.getAllActivity(context);
                final List<ResolveInfo> itemsAllApps = new ArrayList<ResolveInfo>(appsOnBoot);
                Log.d(TAG, "after get all app list");
                final List<ShortcutInfo> itemsInDBForApp = new ArrayList<ShortcutInfo>();
                final List<ResolveInfo>  itemsNotInDB = new ArrayList<ResolveInfo>();

                final Callbacks oldCallbacks = mCallbacks.get();
                if (oldCallbacks == null) {
                    // This launcher has exited and nobody bothered to tell us.  Just bail.
                    Log.w(TAG, "LoaderTask running with no launcher");
                    return;
                }

                final int currentScreen = 0;//oldCallbacks.getCurrentWorkspaceScreen();

                final List<FolderInfo> folderToRemove = new ArrayList<FolderInfo>();
                loadCurrentScreenWorkspace(currentScreen, itemsToRemove, itemsAllApps,
                                                         itemsInDBForApp, folderToRemove);
                boolean isMaxYSupported = true;
                if (mCellCountY == ConfigManager.getCellMaxCountY()) {
                    isMaxYSupported = checkGridSize(mCellCountX, mCellCountY);
                }
                if (isMaxYSupported == true) {
                	if (ConfigManager.DEFAULT_FIND_EMPTY_SCREEN_START != 0)//add by huangweiwei, topwise, 2015-7-21
                    bindCurrentScreenWorkspace(currentScreen);
                }

                //added by lixuhui 2015/02/10 get the misscall count & unread sms/mms count 
                //function to prevent the homeshell process start by notifications when the homeshell process is die or not start, in case to reduce the process start/die and cpu consume
                int misscallCount = UnreadUtils.getMissedCallCount(context);
                int unreadSmsCount = UnreadUtils.getUnreadSmsCount(context) + UnreadUtils.getUnreadMmsCount(context);
                //end
                final Cursor c = contentResolver.query(
                                        LauncherSettings.Favorites.CONTENT_URI,
                                        null,
                                        "container<>? and (screen<>? or container=?) and container<?",
                                        new String[] {
                                            String.valueOf(-101),
                                            String.valueOf(currentScreen),
                                            String.valueOf(Favorites.CONTAINER_HIDESEAT),
                                            String.valueOf(0)},
                                        null);

                // +1 for the hotseat (it can be larger than the workspace)
                // Load workspace in reverse order to ensure that latest items are loaded first (and
                // before any earlier duplicates)

                // +1 for hotseat , +6 for hideseat
                /* HIDESEAT_SCREEN_NUM_MARKER: see ConfigManager.java */
                final ItemInfo occupied[][][] =
                        new ItemInfo[mMaxScreenCount+ 1 + ConfigManager.getHideseatScreenMaxCount()][mCellCountX + 1][mCellCountY + 1];

                final List<ShortcutInfo> hideseatItems = new ArrayList<ShortcutInfo>();
                try {
                    final int idIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites._ID);
                    final int intentIndex = c.getColumnIndexOrThrow
                            (LauncherSettings.Favorites.INTENT);
                    final int titleIndex = c.getColumnIndexOrThrow
                            (LauncherSettings.Favorites.TITLE);
                    final int iconTypeIndex = c.getColumnIndexOrThrow(
                            LauncherSettings.Favorites.ICON_TYPE);
                    final int iconIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.ICON);
                    final int iconPackageIndex = c.getColumnIndexOrThrow(
                            LauncherSettings.Favorites.ICON_PACKAGE);
                    final int iconResourceIndex = c.getColumnIndexOrThrow(
                            LauncherSettings.Favorites.ICON_RESOURCE);
                    final int containerIndex = c.getColumnIndexOrThrow(
                            LauncherSettings.Favorites.CONTAINER);
                    final int itemTypeIndex = c.getColumnIndexOrThrow(
                            LauncherSettings.Favorites.ITEM_TYPE);
                    final int appWidgetIdIndex = c.getColumnIndexOrThrow(
                            LauncherSettings.Favorites.APPWIDGET_ID);
                    final int screenIndex = c.getColumnIndexOrThrow(
                            LauncherSettings.Favorites.SCREEN);
                    final int cellXIndex = c.getColumnIndexOrThrow
                            (LauncherSettings.Favorites.CELLX);
                    final int cellYIndex = c.getColumnIndexOrThrow
                            (LauncherSettings.Favorites.CELLY);
                    final int spanXIndex = c.getColumnIndexOrThrow
                            (LauncherSettings.Favorites.SPANX);
                    final int spanYIndex = c.getColumnIndexOrThrow(
                            LauncherSettings.Favorites.SPANY);
                    final int msgNumIndex = c.getColumnIndexOrThrow(
                            LauncherSettings.Favorites.MESSAGE_NUM);
                    final int isNewIndex = c.getColumnIndexOrThrow(
                            LauncherSettings.Favorites.IS_NEW);
                    final int canDeleteIndex = c.getColumnIndexOrThrow(
                            LauncherSettings.Favorites.CAN_DELEDE);
                    final int isSDAppIndex = c.getColumnIndexOrThrow(
                            LauncherSettings.Favorites.IS_SDAPP);
                    //final int uriIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.URI);
                    //final int displayModeIndex = c.getColumnIndexOrThrow(
                    //        LauncherSettings.Favorites.DISPLAY_MODE);

                    ShortcutInfo info;
                    String intentDescription;
                    LauncherAppWidgetInfo appWidgetInfo;
                    int container;
                    long id;
                    Intent intent;

                    while (!mStopped && c.moveToNext()) {
                        try {
                            int itemType = c.getInt(itemTypeIndex);
                            id = c.getLong(idIndex);
                            if (c.getInt(containerIndex) == LauncherSettings.Favorites.CONTAINER_DESKTOP) {
                                if ((c.getInt(cellXIndex) >= mCellCountX) ||
                                     (c.getInt(cellYIndex) >= mCellCountY) ||
                                     (c.getInt(spanXIndex) + c.getInt(cellXIndex) > mCellCountX) ||
                                     (c.getInt(spanYIndex) + c.getInt(cellYIndex) > mCellCountY) ||
                                     (c.getInt(screenIndex) >= mMaxScreenCount)) {
                                    itemsToRemove.add(id);
                                    Log.d(TAG, "item position error, id is " + id);
                                    continue;
                                }
                            }

                            switch (itemType) {
                            case LauncherSettings.Favorites.ITEM_TYPE_NOSPACE_APPLICATION:
                            case LauncherSettings.Favorites.ITEM_TYPE_APPLICATION:
                            case LauncherSettings.Favorites.ITEM_TYPE_SHORTCUT:
                            case LauncherSettings.Favorites.ITEM_TYPE_SHORTCUT_DOWNLOADING:
                            case LauncherSettings.Favorites.ITEM_TYPE_VPINSTALL:
                                intentDescription = c.getString(intentIndex);
                                try {
                                    intent = Intent.parseUri(intentDescription, 0);
                                } catch (URISyntaxException e) {
                                    itemsToRemove.add(id);
                                    continue;
                                }
                                //remove all download item when age mode change
                                if ((itemType == LauncherSettings.Favorites.ITEM_TYPE_SHORTCUT_DOWNLOADING) && (mClearAllDownload == true)) {
                                    itemsToRemove.add(id);
                                    continue;
                                }
                                if (itemType == LauncherSettings.Favorites.ITEM_TYPE_VPINSTALL) {
                                    Log.d(TAG, "start check vp install item");
                                    PackageInfo packageInfoInstall = null;
                                    try {
                                        packageInfoInstall = manager.getPackageInfo(intent.getStringExtra("packagename"), 0);
                                    } catch (NameNotFoundException e) {
                                        Log.d(TAG, "the package is not installed");
                                    }
                                    if (packageInfoInstall != null) {
                                        Log.d(TAG, packageInfoInstall.packageName + " is installed");
                                        itemsToRemove.add(id);
                                        continue;
                                    }
                                    if (intent == null) {
                                        itemsToRemove.add(id);
                                        continue;
                                    }
                                    if ((intent.getComponent() == null) || (intent.getComponent().equals(""))){
                                        Log.d(TAG, "component is null");
                                        ComponentName compName = new ComponentName(intent.getStringExtra(VPUtils.TYPE_PACKAGENAME), "vpinstall");
                                        intent.setComponent(compName);
                                    }
                                }

                                if ((itemType == LauncherSettings.Favorites.ITEM_TYPE_APPLICATION) ||
                                    (itemType == LauncherSettings.Favorites.ITEM_TYPE_NOSPACE_APPLICATION)) {
                                    ResolveInfo appInfo = AllAppsList.findActivityInfo(itemsAllApps, intent.getComponent());
                                    if (appInfo == null) {
                                        if (Hideseat.isHideseatEnabled() &&
                                            intent != null && intent.getComponent() != null &&
                                            AppFreezeUtil.isPackageFrozen(mContext, intent.getComponent().getPackageName())) {
                                            // ignore frozen apps
                                        } else {
                                            if (c.getInt(isSDAppIndex) == 0) {
                                                itemsToRemove.add(id);
                                                continue;
                                            } else if (BackupManager.getInstance().isInRestore()) {
                                                // TODO need to check if it's an app installed on sdcard
                                                // if not installed on sdcard, remove item from db
                                                if ((intent == null) || (intent.getComponent() == null) ||
                                                     (BackupUitil.isInRestoreList(context, intent.getComponent().getPackageName()) == true)) {
                                                    Log.d(TAG, "sd app and in restore list");
                                                    itemsToRemove.add(id);
                                                    continue;
                                                }
                                            }
                                        }
                                    }

                                     if ((intent == null) || (allComponentList.contains(intent.toString()))) {
                                        itemsToRemove.add(id);
                                        continue;
                                    } else {
                                        allComponentList.add(intent.toString());
                                    }
                                    info = getShortcutInfo(manager, intent, context, c, iconIndex,
                                            titleIndex, mLabelCache);
                                    info.itemType = itemType;
                                } else {
                                    info = getShortcutInfo(c, context, iconTypeIndex,
                                            iconPackageIndex, iconResourceIndex, iconIndex,
                                            titleIndex);

                                    String appPackageFilePath = intent.getStringExtra(VPUtils.TYPE_PACKAGEPATH);
                                    if (!TextUtils.isEmpty(appPackageFilePath)) {
                                        CharSequence vpLabel = VPUtils.getVpinstallLabel(appPackageFilePath);
                                        if (!TextUtils.isEmpty(vpLabel)) {
                                            info.title = vpLabel;
                                        }
                                    }

                                    // App shortcuts that used to be automatically added to Launcher
                                    // didn't always have the correct intent flags set, so do that
                                    // here
                                    if (intent.getAction() != null &&
                                        intent.getCategories() != null &&
                                        intent.getAction().equals(Intent.ACTION_MAIN) &&
                                        intent.getCategories().contains(Intent.CATEGORY_LAUNCHER)) {
                                        intent.addFlags(
                                            Intent.FLAG_ACTIVITY_NEW_TASK |
                                            Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
                                    }
                                }

                                if (info != null) {
                                    info.intent = intent;
                                    info.id = c.getLong(idIndex);
                                    container = c.getInt(containerIndex);
                                    info.container = container;
                                    info.screen = c.getInt(screenIndex);
                                    info.cellX = c.getInt(cellXIndex);
                                    info.cellY = c.getInt(cellYIndex);
                                    info.isNew = c.getInt(isNewIndex);
                                    info.messageNum = c.getInt(msgNumIndex);

                                    //added by lixuhui 2015/02/10 get the misscall count & unread sms/mms count 
                                    ////function to prevent the homeshell process start by notifications when the homeshell process is die or not start, in case to reduce the process start/die and cpu consume
                                    if(intent != null){
                                        ComponentName component = intent.getComponent();
                                        if(component != null){
                                            if("com.yunos.alicontacts/.activities.DialtactsContactsActivity".equals(component.flattenToShortString())){
                                                UnreadUtils.syncItemMessageNum(contentResolver, info, misscallCount);
                                            }else if("com.android.mms/.ui.ConversationList".equals(component.flattenToShortString())){
                                                UnreadUtils.syncItemMessageNum(contentResolver, info, unreadSmsCount);
                                            }
                                        }
                                    }
                                    //end
                                    // check & update map of what's occupied
                                    if (itemType != LauncherSettings.Favorites.ITEM_TYPE_NOSPACE_APPLICATION) {
                                        if ((info.screen < 0) || (info.cellX < 0) || (info.cellY < 0) ||
                                            (!checkItemPlacement(occupied, info))) {
                                            //if the item place is occupied by other item,
                                            //or it's position invalid,
                                            //remove it from db, if the item is an app,
                                            //it can be added in below operation,
                                            //other types will not be recovered
                                            itemsToRemove.add(info.id);
                                            break;
                                        }
                                    } else {
                                        Log.d(TAG, "it is a no space item");
                                    }

                                    // Check whether the item is frozen or not. frozen item will
                                    // be added to hideseat in method reorderHideseatItemsInDB().
                                    if (container == Favorites.CONTAINER_HIDESEAT ||
                                        (AppFreezeUtil.isPackageFrozen(mContext, info) &&
                                                (itemType == Favorites.ITEM_TYPE_APPLICATION ||
                                                 itemType == Favorites.ITEM_TYPE_NOSPACE_APPLICATION ||
                                                 itemType == Favorites.ITEM_TYPE_SHORTCUT_DOWNLOADING))) {
                                        sBgWorkspaceItems.add(info);
                                        hideseatItems.add(info);
                                    }
                                    else {
                                        switch (container) {
                                        case LauncherSettings.Favorites.CONTAINER_DESKTOP:
                                        case LauncherSettings.Favorites.CONTAINER_HOTSEAT:
                                        //add hide icon container
                                        case LauncherSettings.Favorites.CONTAINER_HIDESEAT:
                                            if (itemType == LauncherSettings.Favorites.ITEM_TYPE_NOSPACE_APPLICATION) {
                                                sBgNoSpaceItems.add(info);
                                            } else {
                                                sBgWorkspaceItems.add(info);
                                            }
                                            break;
                                        default:
                                            //if an item in a folder, it's container is folder's id
                                            //so the item's container must not less than 0
                                            if (container >= 0) {
                                                // Item is in a user folder
                                                //topwise zyf add for fixedfolder
                                        	ItemInfo itInfo= findFixedFolderInArr(container);
                                        	if(itInfo!=null&&itInfo.itemType==LauncherSettings.Favorites.ITEM_TYPE_FIXED_FOLDER)
                                        	{
                                        		FolderInfo folderInfo =
                                        				findOrMakeFixedFolder(sBgFolders, container);
                                                folderInfo.add(info);
                                        	}
                                        	else {
                                        		FolderInfo folderInfo =
                                                        findOrMakeFolder(sBgFolders, container);
                                                folderInfo.add(info);
    										}
                                        	//topwise zyf add end
                                        	/*
                                            FolderInfo folderInfo =
                                                    findOrMakeFolder(sBgFolders, container);
                                            folderInfo.add(info);
                                            */
                                            }
                                            break;
                                        }
                                    }
                                    sBgItemsIdMap.put(info.id, info);

                                    // if the shortcut is for applicaiton, add it to itemsInDBForApp
                                    if ((info.itemType == LauncherSettings.Favorites.ITEM_TYPE_APPLICATION) ||
                                        (info.itemType == LauncherSettings.Favorites.ITEM_TYPE_NOSPACE_APPLICATION)){
                                        itemsInDBForApp.add(info);
                                    }

                                    if (info.itemType != LauncherSettings.Favorites.ITEM_TYPE_NOSPACE_APPLICATION) {
                                        if (mIconManager.supprtCardIcon() == true) {
                                            mIconManager.getAppCardBackgroud(info);
                                        }
                                    }
                                    // now that we've loaded everthing re-save it with the
                                    // icon in case it disappears somehow.
                                    //queueIconToBeChecked(sBgDbIconCache, info, c, iconIndex);
                                } else {
                                    // Failed to load the shortcut, probably because the
                                    // activity manager couldn't resolve it (maybe the app
                                    // was uninstalled), or the db row was somehow screwed up.
                                    // Delete it.
                                    id = c.getLong(idIndex);
                                    Log.e(TAG, "Error loading shortcut " + id + ", removing it");
                                    contentResolver.delete(LauncherSettings.Favorites.getContentUri(
                                                id, false), null, null);
                                }
                                break;

                            case LauncherSettings.Favorites.ITEM_TYPE_FOLDER:
                                id = c.getLong(idIndex);
                                FolderInfo folderInfo = findOrMakeFolder(sBgFolders, id);

                                folderInfo.title = c.getString(titleIndex);
                                updateFolderTitle(folderInfo);
                                folderInfo.id = id;
                                container = c.getInt(containerIndex);
                                folderInfo.container = container;
                                folderInfo.screen = c.getInt(screenIndex);
                                folderInfo.cellX = c.getInt(cellXIndex);
                                folderInfo.cellY = c.getInt(cellYIndex);

                                // check & update map of what's occupied
                                if (!checkItemPlacement(occupied, folderInfo)) {
                                    itemsToRemove.add(folderInfo.id);
                                    if (sBgFolders.containsKey(folderInfo.id)) {
                                        sBgFolders.remove(folderInfo.id);
                                    }
                                    if (sBgItemsIdMap.containsKey(folderInfo.id)) {
                                        sBgItemsIdMap.remove(folderInfo.id);
                                    }
                                    break;
                                }
                                switch (container) {
                                    case LauncherSettings.Favorites.CONTAINER_DESKTOP:
                                    case LauncherSettings.Favorites.CONTAINER_HOTSEAT:
                                    case LauncherSettings.Favorites.CONTAINER_HIDESEAT:
                                        sBgWorkspaceItems.add(folderInfo);
                                        break;
                                }

                                sBgItemsIdMap.put(folderInfo.id, folderInfo);
                                sBgFolders.put(folderInfo.id, folderInfo);
                                break;
                                
                            //topwise zyf add for fixedfolder


                            case LauncherSettings.Favorites.ITEM_TYPE_FIXED_FOLDER:
									{
                                id = c.getLong(idIndex);
                                FolderInfo fi = findOrMakeFixedFolder(sBgFolders, id);

                                fi.title = c.getString(titleIndex);
                                updateFolderTitle(fi);
                               fi.id = id;
                                container = c.getInt(containerIndex);
                                fi.container = container;
                                fi.screen = c.getInt(screenIndex);
                                fi.cellX = c.getInt(cellXIndex);
                                fi.cellY = c.getInt(cellYIndex);
                              //topwise zyf add for multi-language
								   String tempintent=c.getString(intentIndex);
									if(tempintent!=null)
									{
										if(tempintent.equals(LauncherSettings.Favorites.INTNET_APPS))
										{
											fi.itemExtraType=ItemInfo.ITEM_EXTRA_TYPE_APPS;
										}
										else if(tempintent.equals(LauncherSettings.Favorites.INTNET_GAMES)) 
										{
											fi.itemExtraType=ItemInfo.ITEM_EXTRA_TYPE_GAMES;
										}
									}
//topwise zyf add for multi-language end

                                // check & update map of what's occupied
                                 if (!checkItemPlacement(occupied, fi)) {
                                    itemsToRemove.add(fi.id);
                                    if (sBgFolders.containsKey(fi.id)) {
                                        sBgFolders.remove(fi.id);
                                    }
                                    if (sBgItemsIdMap.containsKey(fi.id)) {
                                        sBgItemsIdMap.remove(fi.id);
                                    }
                                    break;
                                }
                                switch (container) {
                                    case LauncherSettings.Favorites.CONTAINER_DESKTOP:
                                    case LauncherSettings.Favorites.CONTAINER_HOTSEAT:
                                    case LauncherSettings.Favorites.CONTAINER_HIDESEAT:
                                        sBgWorkspaceItems.add(fi);
                                        break;
                                }

                                sBgItemsIdMap.put(fi.id, fi);
                                sBgFolders.put(fi.id, fi);
                            }
                                break;
                           //topwise zyf add end


                            case LauncherSettings.Favorites.ITEM_TYPE_APPWIDGET:
                                // Read all Launcher-specific widget details
                                int appWidgetId = c.getInt(appWidgetIdIndex);
                                intentDescription = c.getString(intentIndex);
                                try {
                                    intent = Intent.parseUri(intentDescription, 0);
                                } catch (URISyntaxException e) {
                                    itemsToRemove.add(id);
                                    continue;
                                }

                                AppWidgetProviderInfo provider =
                                        widgets.getAppWidgetInfo(appWidgetId);
                                if (BackupManager.getInstance().isInRestore()) {
                                    try {
                                        Log.d(TAG, "widget loading in restore mode");
                                        AppWidgetHost host = new AppWidgetHost(context, Launcher.APPWIDGET_HOST_ID);
                                        int tmpID = host.allocateAppWidgetId();
                                        final AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(mContext);
                                        ACA.AppWidgetManager.bindAppWidgetId(appWidgetManager, tmpID, intent.getComponent());
                                        appWidgetId = tmpID;
                                        //update db
                                        provider = widgets.getAppWidgetInfo(appWidgetId);
                                    } catch (Exception e) {
                                        Log.d(TAG, "restore system's widget met:" + e);
                                    }
                                }

                                if (!isSafeMode && (provider == null || provider.provider == null ||
                                        provider.provider.getPackageName() == null)) {
                                    String log = "Deleting widget that isn't installed anymore: id="
                                        + id + " appWidgetId=" + appWidgetId;
                                    Log.e(TAG, log);
                                    //Launcher.sDumpLogs.add(log);
                                    itemsToRemove.add(id);
                                } else {
                                    appWidgetInfo = new LauncherAppWidgetInfo(appWidgetId,
                                            provider.provider);
                                    appWidgetInfo.id = id;
                                    appWidgetInfo.screen = c.getInt(screenIndex);
                                    appWidgetInfo.cellX = c.getInt(cellXIndex);
                                    appWidgetInfo.cellY = c.getInt(cellYIndex);
                                    appWidgetInfo.spanX = c.getInt(spanXIndex);
                                    appWidgetInfo.spanY = c.getInt(spanYIndex);
                                    int[] minSpan = Launcher.getMinSpanForWidget(context, provider);
                                    appWidgetInfo.minSpanX = minSpan[0];
                                    appWidgetInfo.minSpanY = minSpan[1];

                                    container = c.getInt(containerIndex);
                                    if (container != LauncherSettings.Favorites.CONTAINER_DESKTOP &&
                                        container != LauncherSettings.Favorites.CONTAINER_HOTSEAT &&
                                        container != LauncherSettings.Favorites.CONTAINER_HIDESEAT) {
                                        Log.e(TAG, "Widget found where container != " +
                                            "CONTAINER_DESKTOP nor CONTAINER_HOTSEAT nor CONTAINER_HIDESEAT- ignoring!");
                                        continue;
                                    }
                                    appWidgetInfo.container = c.getInt(containerIndex);

                                    // check & update map of what's occupied
                                    if (!checkItemPlacement(occupied, appWidgetInfo)) {
                                        break;
                                    }
                                    sBgItemsIdMap.put(appWidgetInfo.id, appWidgetInfo);
                                    sBgAppWidgets.add(appWidgetInfo);
                                }
                                break;
                            /*case LauncherSettings.Favorites.ITEM_TYPE_GADGET:
                                String strType = c.getString(titleIndex);
                                    if (!TextUtils.isEmpty(strType)) {
                                        GadgetInfo ginfo = gadgets.get(strType);
                                        if (ginfo != null) {
                                            GadgetItemInfo gi = new GadgetItemInfo(ginfo);
                                            gi.id = c.getLong(idIndex);
                                            gi.screen = c.getInt(screenIndex);
                                            gi.cellX = c.getInt(cellXIndex);
                                            gi.cellY = c.getInt(cellYIndex);
                                            gi.spanX = c.getInt(spanXIndex);
                                            gi.spanY = c.getInt(spanYIndex);
                                            sBgWorkspaceItems.add(gi);
                                            sBgItemsIdMap.put(gi.id, gi);
                                        }
                                    }
                                break;*/
                            }
                        } catch (Exception e) {
                            Log.w(TAG, "Desktop items loading interrupted:", e);
                        }
                    }
                    //topwise zyf add for exapp 添加进文件夹
                    Runnable r = new Runnable() {
                        @Override
                        public void run() {
		          		              //add for notify
		          		          	  mNewAppNum=0;
		          		        	  mNewGameNum=0;
		          		        	  //add end
									//topwise zyf add for hide folder
									addFixedFolder(mContext,LauncherProvider.APPS_FOLDER_RESID,FileAndInfo.EXAPP_TYPE_APP);
            						addFixedFolder(mContext,LauncherProvider.GAMES_FOLDER_RESID,FileAndInfo.EXAPP_TYPE_GAME);
									//topwise zyf add for hide folder end
                    addExapp(mContext,LauncherProvider.APPS_FOLDER_RESID,FileAndInfo.EXAPP_TYPE_APP);
                    addExapp(mContext,LauncherProvider.GAMES_FOLDER_RESID,FileAndInfo.EXAPP_TYPE_GAME);
                    //添加桌面切换图标
                    //for folderonline
                    addOnlineApp(mContext);
                    //folderonline end
                        }
                    };
                    runOnMainThread(r);

					//topiwse zyf add for notify
	                //mFixedfolderLoadCount++;
	               // if(mWorkspaceLoadCount-mFixedfolderLoadCount<=0)
	                   // checkToNotify(mApp.getContext(),mFixedfolderLoadCount);
	                	 //topiwse zyf add for notify
	                    	sWorker.postDelayed(mCheckToNotify, 3000);
	        	        //add end
			        //add end
                    //topwise zyf add end
                } finally {
                    c.close();
                }

                if (mStopped != true) {
                    try {
                        if (Hideseat.isHideseatEnabled()) {
                            List<ShortcutInfo> out_nospaceItems = new ArrayList<ShortcutInfo>(0);
                            reorderHideseatItemsInDB(hideseatItems, out_nospaceItems);
                            // deal with no-space items. (very rare situation)
                            for (ShortcutInfo item : out_nospaceItems) {
                                if (item.itemType == Favorites.ITEM_TYPE_APPLICATION) {
                                    item.itemType = Favorites.ITEM_TYPE_NOSPACE_APPLICATION;
                                    item.screen = item.cellX = item.cellY = -1;
                                    if (sBgWorkspaceItems.remove(item)) {
                                        sBgNoSpaceItems.add(item);
                                    }
                                    updateItemInDatabase(mContext, item);
                                } else {
                                    deleteItemFromDatabase(mContext, item);
                                }
                            }
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Hide-seat items reordering interrupted:", e);
                    } finally {
                        hideseatItems.clear();
                    }

                    if (itemsToRemove.size() > 0) {
                        ContentProviderClient client = contentResolver.acquireContentProviderClient(
                                        LauncherSettings.Favorites.CONTENT_URI);
                        // Remove dead items
                        for (long id : itemsToRemove) {
                            if (DEBUG_LOADERS) {
                                Log.d(TAG, "Removed id = " + id);
                            }
                            // Don't notify content observers
                            try {
                                client.delete(LauncherSettings.Favorites.getContentUri(id, false),
                                        null, null);
                            } catch (RemoteException e) {
                                Log.w(TAG, "Could not remove id = " + id);
                            }
                        }
                        if (client != null) {
                            client.release();
                        }
                        mClearAllDownload = false;
                    }

                    // check if an application in itemsAllApps exists in itemsInDBForApp
                    if (DEBUG_LOADERS) Log.d(TAG,"to check if any apps are not in db");
                    for(ResolveInfo app : itemsAllApps){
                        boolean find = false;
                        ComponentName appComponent = new ComponentName(app.activityInfo.packageName,
                            app.activityInfo.name);

                        for(ShortcutInfo appInDB : itemsInDBForApp) {
                            ComponentName componentName = appInDB.intent.getComponent();
                            if (appComponent!=null && appComponent.equals(componentName)) {
                                find = true;
                                break;
                            }
                        }
                        if(!find) {
                        	//topwise zyf add for exapp 
                        	if(appComponent!=null&&appComponent.getPackageName()!=null
                        			&&getExappApk(appComponent.getPackageName())!=null)
                        	{
                        		Log.d("zyflauncher","itemsNotInDB but is exapp apk!!!");
                        	}
                        	else
                        	//topwise zyf add for folderonline
                        		
                        	if(appComponent!=null&&appComponent.getPackageName()!=null&&
                        		getApkinfoByPkg(appComponent.getPackageName())!=null)
                        	{
                        		Log.d("zyflauncher","itemsNotInDB but is online apk!!!");
                        	}else
                             //topwise zyf add end
                        	//topwise zyf add end
                            itemsNotInDB.add(app);
                            if (DEBUG_LOADERS) Log.d(TAG, "title : " + app.loadLabel(manager)
                                    + " pkgName : " + app.activityInfo.packageName
                                    + " className : " + app.activityInfo.name);
                        }
                    }

                    if (itemsNotInDB.size() > 0) {
                        Collections.sort(itemsNotInDB, new ResolveInfo.DisplayNameComparator(manager));
                    }
                    boolean agedState = LauncherProvider.getDbAgedModeState();
                    boolean loadCompleted = true;
                    for(ResolveInfo app : itemsNotInDB) {
                        LauncherApplication launcherApp = (LauncherApplication) mContext.getApplicationContext();
                        if (mStopped == true) {
                            loadCompleted = false;
                            break;
                        }
                        if (findAndUpdateDownloadItem(app) == false) {
                            if (agedState) {
                                PackageManager pm = mContext
                                        .getPackageManager();
                                ApplicationInfo appInfo = new ApplicationInfo(
                                        pm, app, mLabelCache);
                                ShortcutInfo info = getShortcutInfo(pm,
                                        appInfo, context);
                                if (info == null) {
                                    continue;
                                }
                                // add all the other app beyond to the default
                                // app to default folder
                                if (LauncherProvider.agedDefaultFolderId != -1
                                        && sBgFolders.get(LauncherProvider.agedDefaultFolderId) != null) {
                                    FolderInfo folderInfo = sBgFolders.get(LauncherProvider.agedDefaultFolderId);
                                    moveToFolderNextPos(LauncherProvider.sMaxPosAfterLoadFav);
                                    info.container = folderInfo.id;
                                    addItemToDatabase(mContext, info, info.container,
                                            LauncherProvider.sMaxPosAfterLoadFav.s,
                                            LauncherProvider.sMaxPosAfterLoadFav.x,
                                            LauncherProvider.sMaxPosAfterLoadFav.y, false);
                                } else {
                                    ScreenPosition p = launcherApp.getModel().findEmptyCell();
                                    if (p == null) {
                                        p = new ScreenPosition(-1, -1, -1);
                                        info.itemType = LauncherSettings.Favorites.ITEM_TYPE_NOSPACE_APPLICATION;
                                    }
                                    info.container = LauncherSettings.Favorites.CONTAINER_DESKTOP;
                                    addItemToDatabase(mContext, info,
                                            info.container, p.s, p.x, p.y,
                                            false);
                                }
                            } else {

                                ScreenPosition p= launcherApp.getModel().findEmptyCell();

                            if(p == null){
                                p = new ScreenPosition(-1, -1, -1);
                                // no space now, we shall add it to nospace list
                            }
                            PackageManager pm =mContext.getPackageManager();
                            ApplicationInfo appInfo = new ApplicationInfo(pm, app, mLabelCache);
                            ShortcutInfo info = getShortcutInfo(pm, appInfo, context);
                            if (info == null) {
                                continue;
                            }

                            info.container = LauncherSettings.Favorites.CONTAINER_DESKTOP;
                            if (p.s == -1) {
                                info.itemType = LauncherSettings.Favorites.ITEM_TYPE_NOSPACE_APPLICATION;
                            } else {
                                info.itemType = LauncherSettings.Favorites.ITEM_TYPE_APPLICATION;
                            }
                            info.screen = p.s;
                            info.cellX = p.x;
                            info.cellY = p.y;
                            info.spanX = 1;
                            info.spanY = 1;

                                //We don't set new flag if the app is found in loadworkspace
                                info.isNew = 0;
                                addItemToDatabase(mContext, info, info.container, p.s, p.x, p.y, false);
                            }
                        }
                    }
                    if (agedState && loadCompleted) {
                        LauncherProvider.agedDefaultFolderId = -1;
                    }
                    List<ApplicationInfo> allFrozenApps = null;
                    if (Hideseat.isHideseatEnabled()) {
                        allFrozenApps = AppFreezeUtil.getAllFrozenApps(context);
                    } else {
                        allFrozenApps = Collections.emptyList();
                    }
                    for (ApplicationInfo info : allFrozenApps) {
                        boolean foundInDB = false;
                        if (info.componentName == null) continue;
                        String pkgName = info.componentName.getPackageName();
                        String clsName = info.componentName.getClassName();
                        for(ShortcutInfo appInDB : itemsInDBForApp) {
                            ComponentName componentName = appInDB.intent.getComponent();
                            if (componentName == null) continue;
                            if (componentName.equals(info.componentName) ||
                                (TextUtils.isEmpty(clsName) && componentName.getPackageName().equals(pkgName))) {
                                foundInDB = true;
                                break;
                            }
                        }
                        if (!foundInDB) {
                            // the app is not found in database
                            ShortcutInfo si = getShortcutInfo(context.getPackageManager(), info, context);
                            if (si == null) {
                                continue;
                            }

                            si.container = LauncherSettings.Favorites.CONTAINER_HIDESEAT;
                            ScreenPosition p = LauncherModel.findEmptyCellInHideSeat();
                            if (p == null) {
                                // MARK
                                // no more sapce in hide-seat now, completely ignore the app
                                // p = new ScreenPosition(-1, -1, -1);
                                continue;
                            }
                            si.screen = p.s;
                            si.cellX = p.x;
                            si.cellY = p.y;
                            si.spanX = 1;
                            si.spanY = 1;
                            si.isNew = 0;
                            addItemToDatabase(mContext, si, si.container, p.s, p.x, p.y, false);
                        }
                    }

                    sWorker.post(new Runnable() {
                        @Override
                        public void run() {
                            if (ConfigManager.isVPInstallEnable() == true) {
                                ConfigManager.setVPInstallEnable(false);
                                vpInstallInit();
                            }
                        }
                    });

                    sWorker.post(new Runnable() {
                        @Override
                        public void run() {
                            vpInstallItemsCheck();
                        }
                    });

                    //if (BackupManager.getInstance().isInRestore() == false) {
                        Log.d(TAG, "check empty folder");
                        for (FolderInfo info: sBgFolders.values()) {
                            if ((info != null) && (info.contents != null)) {
                                Log.d(TAG, "folder " + info.title + " size is " + info.contents.size());
                                if (info.contents.size() == 0) {
                                    if(folderToRemove.contains(info) == false) {
										//topwise zyf add for hide folder
										if(!isShowHideFolder(info))
										//topwise zyf add for hide folder end
                                        	folderToRemove.add(info);
										
                                    }
                                }

                                else if((info.contents.size() == 1) &&
                                           (BackupManager.getInstance().isInRestore() == false)){
                                    ShortcutInfo itemInFolder = info.contents.get(0);
                                    if (itemInFolder != null) {
                                        //replace the folder with the item in it
                                        itemInFolder.cellX = info.cellX;
                                        itemInFolder.cellY = info.cellY;
                                        itemInFolder.container = info.container;
                                        itemInFolder.screen = info.screen;
                                        updateItemInDatabase(mContext, itemInFolder);
                                    }
                                    if(folderToRemove.contains(info) == false) {
										//topwise zyf add for hide folder
										if(!isShowHideFolder(info))
										//topwise zyf add for hide folder end
										folderToRemove.add(info);
										
                                    }
                                }
                            }
                        }

                        for (FolderInfo info: folderToRemove) {
                            final ItemInfo iteminfo = (ItemInfo) info;
                            Log.d(TAG, "remove folder " + info.title);
                            deleteItemFromDatabase(mApp, iteminfo);
                        }
                    //}
                }

                // to clear
                folderToRemove.clear();
                itemsToRemove.clear();
                itemsAllApps.clear();
                itemsInDBForApp.clear();
                itemsNotInDB.clear();
                allComponentList.clear();

                if (DEBUG_LOADERS) {
                    Log.d(TAG, "loaded workspace in " + (SystemClock.uptimeMillis()-t) + "ms");
                    Log.d(TAG, "workspace layout: ");
                    for (int y = 0; y < mCellCountY; y++) {
                        String line = "";
                        for (int s = 0; s < mMaxScreenCount; s++) {
                            if (s > 0) {
                                line += " | ";
                            }
                            for (int x = 0; x < mCellCountX; x++) {
                                line += ((occupied[s][x][y] != null) ? "#" : ".");
                            }
                        }
                        Log.d(TAG, "[ " + line + " ]");
                    }
                }
            }
            Log.d(TAG, "loadWorkspace out");
        }

        private void reorderHideseatItemsInDB(List<ShortcutInfo> hideseatItems, List<ShortcutInfo> out_nospaceItems) {
            // sort items by their current positions
            Collections.sort(hideseatItems, Hideseat.sItemOrderComparator);
            Iterator<ShortcutInfo> itr = hideseatItems.listIterator();
            // check positions one by one
            for (ScreenPosition pos : Hideseat.getScreenPosGenerator()) {
                if (itr.hasNext()) {
                    ShortcutInfo item = itr.next();
                    if (item.screen != pos.s ||
                        item.cellX != pos.x || item.cellY != pos.y) {
                        // position mismatch, need to update database
                        Log.w("Hideseat", String.format("reorderHideseatItemsInDB item %s: (%d,%d,%d) should be (%d,%d,%d)",
                                item.title, item.screen, item.cellX, item.cellY, pos.s, pos.x, pos.y));
                        if (item.itemType == Favorites.ITEM_TYPE_NOSPACE_APPLICATION) {
                            item.itemType = Favorites.ITEM_TYPE_APPLICATION;
                        }
                        item.container = Favorites.CONTAINER_HIDESEAT;
                        item.screen = pos.s;
                        item.cellX = pos.x;
                        item.cellY = pos.y;
                        updateItemInDatabase(mContext, item);
                    }
                } else {
                    // all items in hide-seat are processed
                    return;
                }
            }
            // hide-seat space is not enough
            while (itr.hasNext()) {
                out_nospaceItems.add(itr.next());
            }
        }

        /*
         * update the name of the following default folders:
         * tools
         * recommendation
         * games
         */
        private void updateFolderTitle(FolderInfo folder) {
            try {
                String title = (String) folder.title;
                if (DEBUG_LOADERS) Log.d(TAG, "entering updateFolderTitle: title="+title);
                Resources res = mContext.getResources();
                final String GAME_CN = res.getString(R.string.games_cn);
                final String GAME_EN = res.getString(R.string.games_en);
                final String GAME_TW = res.getString(R.string.games_tw);
                final String TOOLS_CN = res.getString(R.string.tools_cn);
                final String TOOLS_EN = res.getString(R.string.tools_en);
                final String TOOLS_TW = res.getString(R.string.tools_tw);
                final String RECOMMEND_APP_CN = res.getString(R.string.recommend_app_cn);
                final String RECOMMEND_APP_EN = res.getString(R.string.recommend_app_en);
                final String RECOMMEND_APP_TW = res.getString(R.string.recommend_app_tw);
                int resId = -1;
                if (TOOLS_CN.equals(title) || TOOLS_EN.equals(title) || TOOLS_TW.equals(title)) {
                    resId = R.string.tools;
                } else if (GAME_CN.equals(title) || GAME_EN.equals(title) || GAME_TW.equals(title)) {
                    resId = R.string.games;
                } else if (RECOMMEND_APP_CN.equals(title) || RECOMMEND_APP_EN.equals(title) || RECOMMEND_APP_TW.equals(title)) {
                    resId = R.string.recommend_app;
                }

                if (resId != -1) {
                    folder.title = res.getString(resId);
                    if (DEBUG_LOADERS) Log.d(TAG, "title="+title+" folder.title="+folder.title);
                }
            }catch (Exception e) {
                e.printStackTrace();
            }
        }

        /**
         * Binds all loaded data to actual views on the main thread.
         */
        private void bindWorkspace(int synchronizeBindPage) {
            final long t = SystemClock.uptimeMillis();
            Runnable r;

            // Don't use these two variables in any of the callback runnables.
            // Otherwise we hold a reference to them.
            final Callbacks oldCallbacks = mCallbacks.get();
            if (oldCallbacks == null) {
                // This launcher has exited and nobody bothered to tell us.  Just bail.
                Log.w(TAG, "LoaderTask running with no launcher");
                return;
            }

            final boolean isLoadingSynchronously = (synchronizeBindPage > -1);
            final int currentScreen = 0;//isLoadingSynchronously ? synchronizeBindPage :
                //oldCallbacks.getCurrentWorkspaceScreen();

            // Load all the items that are on the current page first (and in the process, unbind
            // all the existing workspace items before we call startBinding() below.
            if (mIsInStartBinding == false) {
                unbindWorkspaceItemsOnMainThread();
            }
            ArrayList<ItemInfo> workspaceItems = new ArrayList<ItemInfo>();
            ArrayList<LauncherAppWidgetInfo> appWidgets =
                    new ArrayList<LauncherAppWidgetInfo>();
            HashMap<Long, FolderInfo> folders = new HashMap<Long, FolderInfo>();
            HashMap<Long, ItemInfo> itemsIdMap = new HashMap<Long, ItemInfo>();
            synchronized (sBgLock) {
                workspaceItems.addAll(sBgWorkspaceItems);
                appWidgets.addAll(sBgAppWidgets);
                folders.putAll(sBgFolders);
                itemsIdMap.putAll(sBgItemsIdMap);
            }

            ArrayList<ItemInfo> currentWorkspaceItems = new ArrayList<ItemInfo>();
            ArrayList<ItemInfo> otherWorkspaceItems = new ArrayList<ItemInfo>();
            ArrayList<LauncherAppWidgetInfo> currentAppWidgets =
                    new ArrayList<LauncherAppWidgetInfo>();
            ArrayList<LauncherAppWidgetInfo> otherAppWidgets =
                    new ArrayList<LauncherAppWidgetInfo>();
            HashMap<Long, FolderInfo> currentFolders = new HashMap<Long, FolderInfo>();
            HashMap<Long, FolderInfo> otherFolders = new HashMap<Long, FolderInfo>();

            // Separate the items that are on the current screen, and all the other remaining items
            filterCurrentWorkspaceItems(currentScreen, workspaceItems, currentWorkspaceItems,
                    otherWorkspaceItems);
            filterCurrentAppWidgets(currentScreen, appWidgets, currentAppWidgets,
                    otherAppWidgets);
            filterCurrentFolders(currentScreen, itemsIdMap, folders, currentFolders,
                    otherFolders);
            sortWorkspaceItemsSpatially(currentWorkspaceItems);
            sortWorkspaceItemsSpatially(otherWorkspaceItems);

            // Tell the workspace that we're about to start binding items
            r = new Runnable() {
                public void run() {
                    Callbacks callbacks = tryGetCallbacks(oldCallbacks);
                    if (callbacks != null) {
                        callbacks.startBinding();
                    }
                }
            };

            if (mIsInStartBinding == false) {
                runOnMainThread(r, MAIN_THREAD_BINDING_RUNNABLE);
                mIsInStartBinding = true;
            }
            if (mIsCurrentScreenBinded == false) {
                // Load items on the current page
                bindWorkspaceItems(oldCallbacks, currentWorkspaceItems, currentAppWidgets,
                        currentFolders, null);
                if (isLoadingSynchronously) {
                    r = new Runnable() {
                        public void run() {
                            Log.d(TAG, "isLoadingSynchronously");
                            Callbacks callbacks = tryGetCallbacks(oldCallbacks);
                            if (callbacks != null) {
                                callbacks.onPageBoundSynchronously(currentScreen);
                            }
                        }
                    };
                    runOnMainThread(r, MAIN_THREAD_BINDING_RUNNABLE);
                }
            }

            // Load all the remaining pages (if we are loading synchronously, we want to defer this
            // work until after the first render)
            mDeferredBindRunnables.clear();
            bindWorkspaceItems(oldCallbacks, otherWorkspaceItems, otherAppWidgets, otherFolders,
                    (isLoadingSynchronously ? mDeferredBindRunnables : null));

            // Tell the workspace that we're done binding items
            r = new Runnable() {
                public void run() {
                    Callbacks callbacks = tryGetCallbacks(oldCallbacks);
                    if (callbacks != null) {
                        callbacks.finishBindingItems();
                    }

                    // If we're profiling, ensure this is the last thing in the queue.
                    if (DEBUG_LOADERS) {
                        Log.d(TAG, "bound workspace in "
                            + (SystemClock.uptimeMillis()-t) + "ms");
                    }

                    mIsLoadingAndBindingWorkspace = false;
                }
            };
            if (isLoadingSynchronously) {
                mDeferredBindRunnables.add(r);
            } else {
                runOnMainThread(r, MAIN_THREAD_BINDING_RUNNABLE);
            }

            mIsInStartBinding = false;
            mIsCurrentScreenBinded= false;
        }

        private void loadAndBindAllApps() {
            if (DEBUG_LOADERS) {
                Log.d(TAG, "loadAndBindAllApps mAllAppsLoaded=" + mAllAppsLoaded);
            }
            if (!mAllAppsLoaded) {
                loadAllAppsByBatch();
                synchronized (LoaderTask.this) {
                    if (mStopped) {
                        return;
                    }
                    mAllAppsLoaded = true;
                }
            } else {
                onlyBindAllApps();
            }
        }

        private void onlyBindAllApps() {
            final Callbacks oldCallbacks = mCallbacks.get();
            if (oldCallbacks == null) {
                // This launcher has exited and nobody bothered to tell us.  Just bail.
                Log.w(TAG, "LoaderTask running with no launcher (onlyBindAllApps)");
                return;
            }

            // shallow copy
            @SuppressWarnings("unchecked")
            final ArrayList<ApplicationInfo> list
                    = (ArrayList<ApplicationInfo>) mBgAllAppsList.data.clone();
            Runnable r = new Runnable() {
                public void run() {
                    final long t = SystemClock.uptimeMillis();
                    final Callbacks callbacks = tryGetCallbacks(oldCallbacks);
                    if (callbacks != null) {
                        callbacks.bindAllApplications(list);
                    }
                    if (DEBUG_LOADERS) {
                        Log.d(TAG, "bound all " + list.size() + " apps from cache in "
                                + (SystemClock.uptimeMillis()-t) + "ms");
                    }
                }
            };
            boolean isRunningOnMainThread = !(sWorkerThread.getThreadId() == Process.myTid());
            if (oldCallbacks.isAllAppsVisible() && isRunningOnMainThread) {
                r.run();
            } else {
                mHandler.post(r);
            }
        }

        private void loadAllAppsByBatch() {
            final long t = DEBUG_LOADERS ? SystemClock.uptimeMillis() : 0;

            // Don't use these two variables in any of the callback runnables.
            // Otherwise we hold a reference to them.
            final Callbacks oldCallbacks = mCallbacks.get();
            if (oldCallbacks == null) {
                // This launcher has exited and nobody bothered to tell us.  Just bail.
                Log.w(TAG, "LoaderTask running with no launcher (loadAllAppsByBatch)");
                return;
            }

            final Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
            mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);

            final PackageManager packageManager = mContext.getPackageManager();
            List<Object> apps = null; // contains ResolveInfo and ApplicationInfo objects

            int N = Integer.MAX_VALUE;

            int startIndex;
            int i=0;
            int batchSize = -1;
            while (i < N && !mStopped) {
                if (i == 0) {
                    mBgAllAppsList.clear();
                    final long qiaTime = DEBUG_LOADERS ? SystemClock.uptimeMillis() : 0;
                    if (appsOnBoot == null) {
                        appsOnBoot = packageManager.queryIntentActivities(mainIntent, 0);
                    }
                    apps = new ArrayList<Object>(appsOnBoot);
                    if (Hideseat.isHideseatEnabled()) {
                        List<ShortcutInfo> hideseatItems = new ArrayList<ShortcutInfo>();
                        final ArrayList<ItemInfo> tmpWorkspaceItems = new ArrayList<ItemInfo>();
                        synchronized (sBgLock) {
                            tmpWorkspaceItems.addAll(sBgWorkspaceItems);
                        }
                        for (ItemInfo info : tmpWorkspaceItems) {
                            if (info instanceof ShortcutInfo && info.container == Favorites.CONTAINER_HIDESEAT) {
                                hideseatItems.add((ShortcutInfo) info);
                            }
                        }
                        apps.addAll(AppFreezeUtil.getAllFrozenApps(mContext, hideseatItems.toArray(new ShortcutInfo[0])));
                    }
                    if (DEBUG_LOADERS) {
                        Log.d(TAG, "queryIntentActivities took "
                                + (SystemClock.uptimeMillis()-qiaTime) + "ms");
                    }
                    N = apps.size();
                    if (DEBUG_LOADERS) {
                        Log.d(TAG, "queryIntentActivities got " + N + " apps");
                    }
                    if (N == 0) {
                        // There are no apps?!?
                        return;
                    }
                    if (mBatchSize == 0) {
                        batchSize = N;
                    } else {
                        batchSize = mBatchSize;
                    }
                    /*
                    final long sortTime = DEBUG_LOADERS ? SystemClock.uptimeMillis() : 0;
                    Collections.sort(apps,
                            new LauncherModel.ShortcutNameComparator(packageManager, mLabelCache));
                    if (DEBUG_LOADERS) {
                        Log.d(TAG, "sort took "
                                + (SystemClock.uptimeMillis()-sortTime) + "ms");
                    }
                    */
                }

                final long t2 = DEBUG_LOADERS ? SystemClock.uptimeMillis() : 0;

                startIndex = i;
                for (int j=0; i<N && j<batchSize; j++) {
                    // This builds the icon bitmaps.
                    Object obj = apps.get(i);
                    if (obj instanceof ResolveInfo) {
                        mBgAllAppsList.add(new ApplicationInfo(packageManager, (ResolveInfo) obj, mLabelCache));
                    } else if (obj instanceof ApplicationInfo) {
                        mBgAllAppsList.add((ApplicationInfo) obj);
                    }
                    i++;
                }

                final boolean first = i <= batchSize;
                final Callbacks callbacks = tryGetCallbacks(oldCallbacks);
                final ArrayList<ApplicationInfo> added = mBgAllAppsList.added;
                mBgAllAppsList.added = new ArrayList<ApplicationInfo>();

                mHandler.post(new Runnable() {
                    public void run() {
                        final long t = SystemClock.uptimeMillis();
                        if (callbacks != null) {
                            if (first) {
                                callbacks.bindAllApplications(added);
                            } else {
                                callbacks.bindAppsAdded(added);
                            }
                            if (DEBUG_LOADERS) {
                                Log.d(TAG, "bound " + added.size() + " apps in "
                                    + (SystemClock.uptimeMillis() - t) + "ms");
                            }
                        } else {
                            Log.i(TAG, "not binding apps: no Launcher activity");
                        }
                    }
                });

                if (DEBUG_LOADERS) {
                    Log.d(TAG, "batch of " + (i-startIndex) + " icons processed in "
                            + (SystemClock.uptimeMillis()-t2) + "ms");
                }

                if (mAllAppsLoadDelay > 0 && i < N) {
                    try {
                        if (DEBUG_LOADERS) {
                            Log.d(TAG, "sleeping for " + mAllAppsLoadDelay + "ms");
                        }
                        Thread.sleep(mAllAppsLoadDelay);
                    } catch (InterruptedException exc) { }
                }
            }

            if (DEBUG_LOADERS) {
                Log.d(TAG, "cached all " + N + " apps in "
                        + (SystemClock.uptimeMillis()-t) + "ms"
                        + (mAllAppsLoadDelay > 0 ? " (including delay)" : ""));
            }
        }

        public void dumpState() {
            synchronized (sBgLock) {
                Log.d(TAG, "mLoaderTask.mContext=" + mContext);
                Log.d(TAG, "mLoaderTask.mIsLaunching=" + mIsLaunching);
                Log.d(TAG, "mLoaderTask.mStopped=" + mStopped);
                Log.d(TAG, "mLoaderTask.mLoadAndBindStepFinished=" + mLoadAndBindStepFinished);
                Log.d(TAG, "mItems size=" + sBgWorkspaceItems.size());
            }
        }
    }

    public void post(Runnable r){
        sWorker.post(r);
    }

    private interface PackageUpdatedTaskListener {
        /**
         * This method will be called in UI thread when the target
         * <code>PackageUpdatedTask</code> is finish.
         */
        void onPackageUpdatedTaskFinished(PackageUpdatedTask task);
    }

    private class PackageUpdatedTask implements Runnable {
        int mOp;
        String[] mPackages;

        public static final int OP_NONE = 0;
        public static final int OP_ADD = 1;
        public static final int OP_UPDATE = 2;
        public static final int OP_REMOVE = 3; // uninstlled
        public static final int OP_UNAVAILABLE = 4; // external media unmounted

        private PackageUpdatedTaskListener mListener;

        public PackageUpdatedTask(int op, String[] packages) {
            mOp = op;
            mPackages = packages;
            mListener = null;
        }

        @Override
        public String toString() {
            return String.format("PackageUpdatedTask[op=%d,pkgs=%s]", mOp,
                                 mPackages != null ? Arrays.asList(mPackages) : "null");
        }

        public void setListener(PackageUpdatedTaskListener listener) {
            this.mListener = listener;
        }

        public void run() {
            final Context context = mApp;

            final String[] packages = mPackages;
            final int N = packages.length;

            // SD app reinstall in SD card usb mode
            //run this part of code in worker thread to avoid
            //sBgItemsIdMap ConcurrentModificationException
            Log.d(TAG, "op is " + mOp);
            if (mOp == PackageUpdatedTask.OP_ADD) {
                //actually there is only one item in packages
                //so we just handle the only item
                if (N >0) {
                    ArrayList<ItemInfo> allApps = getAllAppItems();
                    for(ItemInfo info: allApps) {
                        if ((info.itemType == LauncherSettings.Favorites.ITEM_TYPE_NOSPACE_APPLICATION) ||
                            (info.itemType == LauncherSettings.Favorites.ITEM_TYPE_APPLICATION)) {
                            if ((((ShortcutInfo)info).intent != null) &&
                                 (((ShortcutInfo)info).intent.getComponent() != null) &&
                                 (((ShortcutInfo)info).intent.getComponent().getPackageName() != null) &&
                                 (((ShortcutInfo)info).intent.getComponent().getPackageName().equals(packages[0]))) {
                                Log.d(TAG, "the installing app in app list");
                                mOp = PackageUpdatedTask.OP_UPDATE;
                                break;
                            }
                        }
                    }
                }
            }

            switch (mOp) {
                case OP_ADD:
                    for (int i=0; i<N; i++) {
                        if (DEBUG_LOADERS) Log.d(TAG, "mAllAppsList.addPackage " + packages[i]);
                        boolean added = mBgAllAppsList.addPackage(context, packages[i]);
                        if(!added){
                            Log.e(TAG,"addPackage failed");
                            if(mAppDownloadMgr!=null){
                                mAppDownloadMgr.appDownloadRemove(packages[i]);
                            }
                        } else {
                            final int index = i;
                            mAppGroupMgr.handleSingleApp(packages[index]);
                        }
                    }
                    break;
                case OP_UPDATE:
                    for (int i=0; i<N; i++) {
                        if (DEBUG_LOADERS) Log.d(TAG, "mAllAppsList.updatePackage " + packages[i]);
                        mBgAllAppsList.updatePackage(context, packages[i]);
                        LauncherApplication app =
                                (LauncherApplication) context.getApplicationContext();
                        WidgetPreviewLoader.removeFromDb(
                                app.getWidgetPreviewCacheDb(), packages[i]);
                    }
                    break;
                case OP_REMOVE:
                case OP_UNAVAILABLE:
                    for (int i=0; i<N; i++) {
                        if (DEBUG_LOADERS) Log.d(TAG, "mAllAppsList.removePackage " + packages[i]);
                        mBgAllAppsList.removePackage(packages[i]);
                        LauncherApplication app =
                                (LauncherApplication) context.getApplicationContext();
                        WidgetPreviewLoader.removeFromDb(
                                app.getWidgetPreviewCacheDb(), packages[i]);
                    }
                    break;
            }

            ArrayList<ApplicationInfo> added = null;
            ArrayList<ApplicationInfo> modified = null;
            final ArrayList<ApplicationInfo> removedApps = new ArrayList<ApplicationInfo>();

            if (mBgAllAppsList.added.size() > 0) {
                added = new ArrayList<ApplicationInfo>(mBgAllAppsList.added);
                mBgAllAppsList.added.clear();
            }
            if (mBgAllAppsList.modified.size() > 0) {
                modified = new ArrayList<ApplicationInfo>(mBgAllAppsList.modified);
                mBgAllAppsList.modified.clear();
            }
            if (mBgAllAppsList.removed.size() > 0) {
                removedApps.addAll(mBgAllAppsList.removed);
                mBgAllAppsList.removed.clear();
            }

            final Callbacks callbacks = mCallbacks != null ? mCallbacks.get() : null;
            if (callbacks == null) {
                Log.w(TAG, "Nobody to tell about the new app.  Launcher is probably loading.");
                return;
            }

            if (added != null) {
                final ArrayList<ApplicationInfo> addedFinal = added;
                if (DEBUG_LOADERS) Log.d(TAG, "added isn't null");
                parseAddedApps(context, added);
                mHandler.post(new Runnable() {
                    public void run() {
                        Callbacks cb = mCallbacks != null ? mCallbacks.get() : null;
                        if (callbacks == cb && cb != null) {
                            callbacks.bindAppsAdded(addedFinal);
                        }
                    }
                });
            }

            if (modified != null) {
                final ArrayList<ApplicationInfo> modifiedFinal = modified;
                if (DEBUG_LOADERS) Log.d(TAG, "modified isn't null");
                parseUpdatedApps(context, modified);
                mHandler.post(new Runnable() {
                    public void run() {
                        Callbacks cb = mCallbacks != null ? mCallbacks.get() : null;
                        if (callbacks == cb && cb != null) {
                            callbacks.bindAppsUpdated(modifiedFinal);
                        }
                    }
                });
            }

            // If a package has been removed, or an app has been removed as a result of
            // an update (for example), make the removed callback.
            if (mOp == OP_REMOVE || !removedApps.isEmpty()) {
                if (DEBUG_LOADERS) Log.d(TAG, "removed isn't null");
                final boolean permanent = (mOp == OP_REMOVE);
                final ArrayList<String> removedPackageNames =
                        new ArrayList<String>(Arrays.asList(packages));
                // Can't enter edit mode after app update.
                if(mAppDownloadMgr!=null) {
                    if ((mOp == OP_REMOVE) && (removedPackageNames.size() > 0)) {
                        for (String packagename: removedPackageNames) {
                            Log.d(TAG, "remove by package name:" + packagename);
                            mAppDownloadMgr.updateDownloadCount(packagename, false);
                        }
                    } else if (!removedApps.isEmpty()) {
                        for (ApplicationInfo appinfo: removedApps) {
                            if ((appinfo != null) &&
                                (appinfo.componentName != null) &&
                                (appinfo.componentName.getPackageName() != null)) {
                                Log.d(TAG, "remove by appinfo:" + appinfo.componentName.getPackageName());
                                mAppDownloadMgr.updateDownloadCount(appinfo.componentName.getPackageName(), false);
                            }
                        }
                    }
                }
                mHandler.post(new Runnable() {
                    public void run() {
                        Callbacks cb = mCallbacks != null ? mCallbacks.get() : null;
                        if (callbacks == cb && cb != null) {
                            callbacks.bindComponentsRemoved(removedPackageNames,
                                    removedApps, permanent);
                        }
                    }
                });
            }

            final PackageUpdatedTaskListener listener = mListener;
            if (listener != null) {
                mHandler.post(new Runnable() {
                    public void run() {
                        listener.onPackageUpdatedTaskFinished(PackageUpdatedTask.this);
                    }
                });
            }
        }

       private void restoreWidget(Context context, ApplicationInfo app) {
           String packageName = app.componentName.getPackageName();
           Log.d(TAG, "restoreWidget pkgName = " + packageName);

           for (Entry<String,BackupRecord> r : LauncherApplication.mBackupRecordMap.entrySet()) {
               int itemType = Integer.parseInt(r.getValue().getField(Favorites.ITEM_TYPE));
               if (itemType != Favorites.ITEM_TYPE_APPWIDGET) {
                   continue;
               }

               String intentStr = r.getValue().getField(Favorites.INTENT);
               if (TextUtils.isEmpty(intentStr)) {
                   continue;
               }
               Log.d(TAG, "restoreWidget: intentStr"+intentStr);

               try {
                   Intent intent = Intent.parseUri(intentStr, 0);
                   final ComponentName name = intent.getComponent();
                   if (name == null) {
                       Log.e(TAG, "ComponentName == Null");
                       Log.i(TAG, "intent = " + intent.toString());
                       continue;
                   }
                   Log.e(TAG, "restoreWidget ComponentName = " + name);
                   if (name.getPackageName().equalsIgnoreCase(packageName)) {

                       int container = Integer.parseInt(r.getValue().getField(Favorites.CONTAINER));
                       if (container != Favorites.CONTAINER_DESKTOP) {
                           Log.e(TAG, "Widget found where container "
                                   + "!= CONTAINER_DESKTOP -- ignoring!");
                           continue;
                       }
                       AppWidgetHost host = new AppWidgetHost(mApp, Launcher.APPWIDGET_HOST_ID);
                       int appWidgetId = host.allocateAppWidgetId();
                       final AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(mApp);
                       final AppWidgetProviderInfo provider = appWidgetManager.getAppWidgetInfo(appWidgetId);
                       ACA.AppWidgetManager.bindAppWidgetId(appWidgetManager, appWidgetId, intent.getComponent());
                       if (provider == null || provider.provider == null) {
                           Log.e(TAG, "widget provider is null for package:" + packageName);
                           continue;
                       }
                       
                       Log.e(TAG, "restoreWidget appWidgetId= " + appWidgetId);

                       LauncherAppWidgetInfo appWidgetInfo;
                       appWidgetInfo = new LauncherAppWidgetInfo(appWidgetId, provider.provider);
                       appWidgetInfo.id = Long.parseLong(r.getValue().getField(Favorites._ID));
                       appWidgetInfo.screen = Integer.parseInt(r.getValue().getField(Favorites.SCREEN));
                       appWidgetInfo.cellX = Integer.parseInt(r.getValue().getField(Favorites.CELLX));
                       appWidgetInfo.cellY = Integer.parseInt(r.getValue().getField(Favorites.CELLY));
                       appWidgetInfo.spanX = Integer.parseInt(r.getValue().getField(Favorites.SPANX));
                       appWidgetInfo.spanY = Integer.parseInt(r.getValue().getField(Favorites.SPANY));
                       appWidgetInfo.deletable = Integer.parseInt(r.getValue().getField(Favorites.CAN_DELEDE)) == 1 ? true : false;
                       appWidgetInfo.messageNum = Integer.parseInt(r.getValue().getField(Favorites.MESSAGE_NUM));
                       //appWidgetInfo.itemTitle = r.getValue().getField(Favorites.TITLE);
                       //appWidgetInfo.packageName = intent.getComponent().getPackageName();
                       //appWidgetInfo.className = intent.getComponent().getClassName();
                       int[] minSpan = Launcher.getMinSpanForWidget(context, provider);
                       appWidgetInfo.minSpanX = minSpan[0];
                       appWidgetInfo.minSpanY = minSpan[1];
                       appWidgetInfo.container = container;

                       Log.d(TAG, "restoreWidget screen= " + appWidgetInfo.screen);
                       Log.e(TAG, "restoreWidget cellX= " + appWidgetInfo.cellX);
                       Log.e(TAG, "restoreWidget cellY= " + appWidgetInfo.cellY);
                       Log.d(TAG, "restoreWidget pkgname = " + packageName + " start bind widget");
                       //Log.d(TAG, "itemTitle = " + appWidgetInfo.itemTitle);

                       ScreenPosition p = LauncherModel.isCellEmtpy(appWidgetInfo.screen, appWidgetInfo.cellX,
                                appWidgetInfo.cellY, appWidgetInfo.spanX, appWidgetInfo.spanY);
                       if (p == null) {
                           Log.d(TAG, "widget position is occuppied");
                           p = findEmptyCell(ConfigManager.DEFAULT_FIND_EMPTY_SCREEN_START, appWidgetInfo.cellX, appWidgetInfo.cellY);
                           if (p != null) {
                               appWidgetInfo.screen = p.s;
                               appWidgetInfo.cellX  = p.x;
                               appWidgetInfo.cellY  = p.y;
                           }
                       } else {
                           Log.d(TAG, "restoreWidget no space on screen");
                           continue;
                       }

                       //add to database
                       Log.d(TAG, "restoreWidget write to database and list");
                       addItemToDatabase(context, appWidgetInfo, container, p.s, p.x, p.y, false);
                       // update items on ui
                       final Callbacks callbacks = mCallbacks != null ? mCallbacks.get() : null;
                       final ArrayList<ItemInfo> infos = new ArrayList<ItemInfo>(1);
                       infos.add(appWidgetInfo);
                       final Runnable rnbl = new Runnable() {
                           @Override
                           public void run() {
                               if (callbacks != null) {
                                   Log.d(TAG, "restoreWidget call binditems");
                                   callbacks.bindItems(infos, 0, infos.size());
                               }
                           }
                       };
                       runOnMainThread(rnbl);
                   }
               } catch (Exception e) {
                   e.printStackTrace();
               }
           }
        }
        private void parseAddedApps(final Context context, ArrayList<ApplicationInfo> added) {
            if (DEBUG_LOADERS) Log.d(TAG, "parseAddedApps in");
            if (added.size() > 0) {
                int itemSize = added.size();
                boolean isDownloadingItem = false;

                for(int i = 0; i <  itemSize; i++){
                    ApplicationInfo app = (ApplicationInfo)added.get(i);
                    isDownloadingItem = false;

                    /*
                    try {
                        if (BackupManager.getInstance().isInRestore()) {
                            restoreWidget(context,app);
                        }
                    } catch (Exception e) {
                        
                    }
                    */
                    ArrayList<ItemInfo> allApps = getAllAppItems();
                    for (ItemInfo info: allApps) {
                        //find the app in download list first
                        if (info.itemType == LauncherSettings.Favorites.ITEM_TYPE_SHORTCUT_DOWNLOADING){
                            if(app.componentName.getPackageName().equals(((ShortcutInfo)info).intent.getStringExtra(AppDownloadManager.TYPE_PACKAGENAME))) {
                                Log.d(TAG, "it is a downloading item");
                                isDownloadingItem = true;
                                info.setIsNewItem(!Utils.isRestoreApp(app.componentName.getPackageName()));
                                info.title = app.title;

                                ((ShortcutInfo)info).intent = app.intent;
                                info.itemType = LauncherSettings.Favorites.ITEM_TYPE_APPLICATION;
                                ((ShortcutInfo)info).setAppDownloadStatus(AppDownloadStatus.STATUS_INSTALLED);
                                mAppDownloadMgr.updateDownloadCount(app.componentName.getPackageName(),false);
                                LauncherApplication application = (LauncherApplication)context.getApplicationContext();

                                Drawable icon = mApp.getIconManager().getAppUnifiedIcon(app,null);
                                ((ShortcutInfo) info).setIcon(icon);
                                mApp.getIconManager().clearCardBackgroud(((ShortcutInfo)info).intent);
                                try {
                                    int appFlags = context.getPackageManager().getApplicationInfo(app.componentName.getPackageName(), 0).flags;
                                    ((ShortcutInfo)info).isSDApp = Utils.isSdcardApp(appFlags)?1:0;
                                    ((ShortcutInfo)info).customIcon = (((ShortcutInfo)info).isSDApp==0)?false:true;
                                } catch (NameNotFoundException e) {
                                    Log.d(TAG, "PackageManager.getApplicationInfo failed");
                                }

                                final ContentValues values = new ContentValues();
                                final ContentResolver cr = context.getContentResolver();
                                ((ShortcutInfo) info).onAddToDatabase(values);

                                /*save the original icon to database if it is a sd app*/
                                if (((ShortcutInfo) info).isSDApp == 1) {
                                    Drawable origIcon = VPUtils.getAppOriginalIcon(context, (ShortcutInfo)info);
                                    if (origIcon != null) {
                                        ItemInfo.writeBitmap(values, origIcon);
                                    } else {
                                        if(values.containsKey(LauncherSettings.Favorites.ICON)) {
                                            values.remove(LauncherSettings.Favorites.ICON);
                                        }
                                    }
                                } else {
                                    values.put(LauncherSettings.Favorites.ICON, new byte[0]);
                                }

                                if (info.container == Favorites.CONTAINER_HIDESEAT) {
                                    Hideseat.freezeApp((ShortcutInfo) info);
                                } else {
                                    if (AppFreezeUtil.isPackageFrozen(LauncherApplication.getContext(), (ShortcutInfo) info)) {
                                        ShortcutInfo shortcutInfo = (ShortcutInfo)info;
                                        Intent intent = shortcutInfo.intent;
                                        ComponentName cmpt = intent != null ? intent.getComponent() : null;
                                        String pkgName = cmpt != null ? cmpt.getPackageName() : null;
                                        AppFreezeUtil.asyncUnfreezePackage(LauncherApplication.getContext(), pkgName);
                                    }
                                }

                                cr.update(LauncherSettings.Favorites.CONTENT_URI_NO_NOTIFICATION, values,
                                        "_id =?", new String[] { String.valueOf(info.id) });
                                final Callbacks callbacks = mCallbacks != null ? mCallbacks.get() : null;
                                final ArrayList<ItemInfo> infos = new ArrayList<ItemInfo>(1);
                                infos.add(info);
                                final Runnable r = new Runnable() {
                                    @Override
                                    public void run() {
                                        if (callbacks != null) {
                                            callbacks.bindItemsUpdated(infos);
                                        }
                                    }
                                };
                                runOnMainThread(r);
                                break;
                            }
                        }
                        else if (info.itemType == LauncherSettings.Favorites.ITEM_TYPE_VPINSTALL) {
                            if(app.componentName.getPackageName().equals(((ShortcutInfo)info).intent.getStringExtra(VPUtils.TYPE_PACKAGENAME))) {
                                Log.d(TAG, "it is a vpinstall item");
                                String pkgName = ((ShortcutInfo)info).intent.getStringExtra(VPUtils.TYPE_PACKAGENAME);
                                UserTrackerHelper.sendUserReport(UserTrackerMessage.MSG_VP_ITEM_INSTALL_SUCCESS , pkgName);
                                isDownloadingItem = true;
                                info.itemType = LauncherSettings.Favorites.ITEM_TYPE_APPLICATION;
                                // Do not show new icon on VPInstall app.
                                info.setIsNewItem(false);
                                info.title = app.title;
                                ((ShortcutInfo)info).intent = app.intent;
                                ((ShortcutInfo)info).setVPInstallStatus(VPInstallStatus.STATUS_NORMAL);

                                LauncherApplication application = (LauncherApplication)context.getApplicationContext();

                                Drawable icon = mApp.getIconManager().getAppUnifiedIcon(app,null);
                                ((ShortcutInfo) info).setIcon(icon);

                                try {
                                    int appFlags = context.getPackageManager().getApplicationInfo(app.componentName.getPackageName(), 0).flags;
                                    ((ShortcutInfo)info).isSDApp = Utils.isSdcardApp(appFlags)?1:0;
                                    ((ShortcutInfo)info).customIcon = (((ShortcutInfo)info).isSDApp==0)?false:true;
                                } catch (NameNotFoundException e) {
                                    Log.d(TAG, "PackageManager.getApplicationInfo failed");
                                }

                                final ContentValues values = new ContentValues();
                                final ContentResolver cr = context.getContentResolver();
                                ((ShortcutInfo) info).onAddToDatabase(values);

                                if (((ShortcutInfo) info).isSDApp == 1) {
                                    Drawable origIcon = VPUtils.getAppOriginalIcon(context, (ShortcutInfo)info);
                                    if (origIcon != null) {
                                        ItemInfo.writeBitmap(values, origIcon);
                                    } else {
                                        if(values.containsKey(LauncherSettings.Favorites.ICON)) {
                                            values.remove(LauncherSettings.Favorites.ICON);
                                        }
                                    }
                                } else {
                                    values.put(LauncherSettings.Favorites.ICON, new byte[0]);
                                }
                                VPInstallDrawingList.remove(info);

                                if (info.container == Favorites.CONTAINER_HIDESEAT) {
                                    Hideseat.freezeApp((ShortcutInfo) info);
                                }

                                cr.update(LauncherSettings.Favorites.CONTENT_URI_NO_NOTIFICATION, values,
                                        "_id =?", new String[] { String.valueOf(info.id) });
                                final Callbacks callbacks = mCallbacks != null ? mCallbacks.get() : null;
                                final ArrayList<ItemInfo> infos = new ArrayList<ItemInfo>(1);
                                infos.add(info);
                                final ItemInfo finalvpinfo = info;
                                final Runnable r = new Runnable() {
                                    @Override
                                    public void run() {
                                        if (callbacks != null) {
                                            callbacks.bindItemsUpdated(infos);
                                            Intent intent = new Intent(((ShortcutInfo)finalvpinfo).intent);
                                            callbacks.startVPInstallActivity(intent, (ShortcutInfo)finalvpinfo);
                                        }
                                    }
                                };
                                runOnMainThread(r);
                                break;
                            }
                        }
                    }

                    //create a new shortcut info if the app no in download list
                    if (isDownloadingItem == false) {
                        int startScreen = obtainStartScreen();
                        ScreenPosition p = findEmptyCell(startScreen);
                        if (p == null) {
                            p = new ScreenPosition(-1, -1, -1);
                            Toast.makeText(context, context.getString(R.string.application_not_show_no_space),
                                                   Toast.LENGTH_SHORT).show();
                        }
                        getNewItemInfoByApplicationInfo(context, p, app);
                    }
                }
            }
        }

        private ShortcutInfo getNewItemInfoByApplicationInfo(final Context context,
                ScreenPosition p, ApplicationInfo app) {
            ShortcutInfo info = getShortcutInfo(context.getPackageManager(), app, context);

            if (info == null) {
                return null;
            }

            int screen = p.s;
            int x = p.x;
            int y = p.y;
            long container = LauncherSettings.Favorites.CONTAINER_DESKTOP;
            //topwise zyf add for exapp
            FileAndInfo fai=null;
            if(info.intent!=null
            		&&info.intent.getComponent()!=null
            		&&(fai=getExappApk(info.intent.getComponent().getPackageName()))!=null)
            {
            	int extratype=getExtraTypeByType(fai.mExappType);
            	if(extratype!=0)
            	{
            		ItemInfo ii=findFixedFolderByExtraType(extratype);
            		info.container = ii.id;
            	}
            }
            else
            //topwise zyf add end
            info.container = container;
            info.screen = screen;
            info.cellX = x;
            info.cellY = y;
            info.spanX = 1;
            info.spanY = 1;

            if (info.isSystemApp == false) {
                info.setIsNewItem(true);
            } else {
                info.setIsNewItem(false);
            }

            if (screen == -1) {
                info.itemType = LauncherSettings.Favorites.ITEM_TYPE_NOSPACE_APPLICATION;
            }

            if (isDuplicateItem(info)) {
                return null;
            }

            //addItemToDatabase will set Id
            addItemToDatabase(context, info, container,
                    screen, x, y, false);
            final Callbacks callbacks = mCallbacks != null ? mCallbacks.get() : null;
            final ArrayList<ItemInfo> infos = new ArrayList<ItemInfo>(1);
            infos.add(info);
            if (info.itemType != LauncherSettings.Favorites.ITEM_TYPE_NOSPACE_APPLICATION) {
                final Runnable r = new Runnable() {
                    @Override
                    public void run() {
                        if (callbacks != null) {
                            callbacks.bindItems(infos, 0, infos.size());
                        }
                    }
                };
                runOnMainThread(r);
            }
            return info;
        }

        private void parseUpdatedApps(final Context context, ArrayList<ApplicationInfo> added) {
            if (DEBUG_LOADERS) Log.d(TAG, "parseUpdatedApps in");
            if (added.size() > 0) {
                int itemSize = added.size();

                for(int i = 0; i <  itemSize; i++){
                    ApplicationInfo app = (ApplicationInfo)added.get(i);
                    //find the app in download list first
                    ArrayList<ItemInfo> allApps = getAllAppItems();
                    for (ItemInfo info: allApps) {
                        if (info.itemType == LauncherSettings.Favorites.ITEM_TYPE_APPLICATION) {
                            Log.d(TAG, "app.componentName.getPackageName() is " + app.componentName.getPackageName());
                            Log.d(TAG, "item component name is " + ((ShortcutInfo)info).intent.getComponent().getPackageName());
                            if((app.componentName.compareTo(((ShortcutInfo)info).intent.getComponent())==0) ||
                              ((AppFreezeUtil.isPackageFrozen(context, (ShortcutInfo)info) == true)&&
                              TextUtils.isEmpty(app.componentName.getClassName()) &&
                              app.componentName.getPackageName().equals(((ShortcutInfo)info).intent.getComponent().getPackageName()))) {
                                final ShortcutInfo shortcutInfo = (ShortcutInfo) info;
                                //for update item, don't set it new
                                //info.setIsNewItem(true);
                                if (AppFreezeUtil.isPackageFrozen(context, shortcutInfo) == false) {
                                    if (app.needsUpdate()) {
                                        // intent, component name and title of the app-info
                                        // will be updated
                                        app.update(context.getPackageManager());
                                    }
                                    shortcutInfo.title = app.title;
                                    shortcutInfo.intent = app.intent;
                                } else {
                                    app.intent = shortcutInfo.intent;
                                }
                                shortcutInfo.setAppDownloadStatus(AppDownloadStatus.STATUS_INSTALLED);
                                mAppDownloadMgr.updateDownloadCount(app.componentName.getPackageName(),false);
                                LauncherApplication application = (LauncherApplication)context.getApplicationContext();

                                final IconManager iconManager = mApp.getIconManager();
                                Drawable icon = iconManager.getAppUnifiedIcon(app,null);
                                shortcutInfo.setIcon(icon);
                                // Do not clear card background in worker thread. Because icon
                                // might flicker when it redraws in main thread before the new
                                // backgroud is generated and set.
                                //mApp.getIconManager().clearCardBackgroud(((ShortcutInfo)info).intent);

                                final ContentValues values = new ContentValues();
                                final ContentResolver cr = context.getContentResolver();
                                info.onAddToDatabase(values);

                                // the app icon store in database should be original icon
                                /*save the original icon to database if it is a sd app*/
                                if (shortcutInfo.isSDApp == 1) {
                                    Drawable origIcon = VPUtils.getAppOriginalIcon(context, shortcutInfo);
                                    if (origIcon != null) {
                                        ItemInfo.writeBitmap(values, origIcon);
                                    } else {
                                        if(values.containsKey(LauncherSettings.Favorites.ICON)) {
                                            values.remove(LauncherSettings.Favorites.ICON);
                                        }
                                    }
                                } else {
                                    values.put(LauncherSettings.Favorites.ICON, new byte[0]);
                                }
                                // froze the app in hideseat when finish download
                                if (shortcutInfo.container == Favorites.CONTAINER_HIDESEAT) {
                                    if (!AppFreezeUtil.isPackageFrozen(context, shortcutInfo)) {
                                        Hideseat.freezeApp(shortcutInfo);
                                    }
                                } else {
                                    boolean isFrozen = AppFreezeUtil.isPackageFrozen(LauncherApplication.getContext(), (ShortcutInfo) info);
                                    boolean isInHideseat = Hideseat.containsSamePackageOf(info);
                                    if (isFrozen && !isInHideseat) {
                                        Intent intent = shortcutInfo.intent;
                                        ComponentName cmpt = intent != null ? intent.getComponent() : null;
                                        String pkgName = cmpt != null ? cmpt.getPackageName() : null;
                                        AppFreezeUtil.asyncUnfreezePackage(LauncherApplication.getContext(), pkgName);
                                    }
                                }

                                cr.update(LauncherSettings.Favorites.CONTENT_URI_NO_NOTIFICATION, values,
                                        "_id =?", new String[] { String.valueOf(info.id) });
                                final Callbacks callbacks = mCallbacks != null ? mCallbacks.get() : null;
                                final ArrayList<ItemInfo> infos = new ArrayList<ItemInfo>(1);
                                infos.add(info);
                                final Runnable r = new Runnable() {
                                    @Override
                                    public void run() {
                                        if (callbacks != null) {
                                            // Clear card background in main thread instead of worker
                                            // thread to avoid icon flicker.
                                            Intent intent = shortcutInfo.intent;
                                            if (intent != null) {
                                                iconManager.clearCardBackgroud(shortcutInfo.intent);
                                            }
                                            callbacks.bindItemsUpdated(infos);
                                        }
                                    }
                                };
                                runOnMainThread(r);
                            }
                        }
                    }
                    if (app.needsUpdate()) {
                        app.update(context.getPackageManager());
                    }
                }
            }
        }
    }

    // Returns a list of ResolveInfos/AppWindowInfos in sorted order
    public static ArrayList<Object> getSortedWidgetsAndShortcuts(Context context) {
        PackageManager packageManager = context.getPackageManager();
        final ArrayList<Object> widgetsAndShortcuts = new ArrayList<Object>();        
        widgetsAndShortcuts.addAll(filterAppShortcut(packageManager));
        Collections.sort(widgetsAndShortcuts,
            new LauncherModel.WidgetAndShortcutNameComparator(packageManager));
        widgetsAndShortcuts.addAll(AppWidgetManager.getInstance(context).getInstalledProviders());
        //widgetsAndShortcuts.addAll(0, ThemeUtils.listGadgets(context).values());
        return widgetsAndShortcuts;
    }

    private static List<ResolveInfo> filterAppShortcut(PackageManager pm) {
        List<ResolveInfo> result = new ArrayList<ResolveInfo>();
        Intent shortcutsIntent = new Intent(Intent.ACTION_CREATE_SHORTCUT);
        List<ResolveInfo> list = pm.queryIntentActivities(shortcutsIntent, 0);
        for (ResolveInfo ri : list) {
            if (checkResolveInfo(ri)) {
                result.add(ri);
            }
        }
        return result;
    }

    private static boolean checkResolveInfo(ResolveInfo ri) {
        return ri != null && ri.activityInfo != null &&
                "alias.DialShortcut".equals(ri.activityInfo.name) &&
                "com.yunos.alicontacts".equals(ri.activityInfo.packageName);
    }

    public ShortcutInfo getShortcutInfo(PackageManager packageManager,
            ApplicationInfo app, Context context) {
        ComponentName componentName = app.componentName;

        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        intent.setComponent(componentName);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        ShortcutInfo info = getShortcutInfo(packageManager, intent, context);
        if (info != null) {
            info.intent = intent;
            if (info.title == null || info.title.length() == 0) {
                info.title = app.title;
            }
        }
        return info;
    }

    /**
     * This is called from the code that adds shortcuts from the intent receiver.  This
     * doesn't have a Cursor, but
     */
    public ShortcutInfo getShortcutInfo(PackageManager manager, Intent intent, Context context) {
        return getShortcutInfo(manager, intent, context, null, -1, -1, null);
    }

    /**
     * Make an ShortcutInfo object for a shortcut that is an application.
     *
     * If c is not null, then it will be used to fill in missing data like the title and icon.
     */
    public ShortcutInfo getShortcutInfo(PackageManager manager, Intent intent, Context context,
            Cursor c, int iconIndex, int titleIndex, HashMap<Object, CharSequence> labelCache) {
        Drawable icon = null;
        final ShortcutInfo info = new ShortcutInfo();

        ComponentName componentName = intent.getComponent();
        if (componentName == null) {
            Log.d(TAG, "componentName is null");
            return null;
        }

        try {
            PackageInfo pi = manager.getPackageInfo(componentName.getPackageName(), 0);
            if (!pi.applicationInfo.enabled) {
                // If we return null here, the corresponding item will be removed from the launcher
                // db and will not appear in the workspace.
                Log.d(TAG, "pi.applicationInfo.enabled is false");
                info.container = LauncherSettings.Favorites.CONTAINER_HIDESEAT;
                // return null;
            }
        } catch (NameNotFoundException e) {
            Log.d(TAG, "getPackInfo failed for package " + componentName.getPackageName());
        }

        // TODO: See if the PackageManager knows about this case.  If it doesn't
        // then return null & delete this.

        // the resource -- This may implicitly give us back the fallback icon,
        // but don't worry about that.  All we're doing with usingFallbackIcon is
        // to avoid saving lots of copies of that in the database, and most apps
        // have icons anyway.

        // Attempt to use queryIntentActivities to get the ResolveInfo (with IntentFilter info) and
        // if that fails, or is ambiguious, fallback to the standard way of getting the resolve info
        // via resolveActivity().
        ResolveInfo resolveInfo = null;
        ComponentName oldComponent = intent.getComponent();
        Intent newIntent = new Intent(intent.getAction(), null);
        newIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        newIntent.setPackage(oldComponent.getPackageName());
        List<ResolveInfo> infos = manager.queryIntentActivities(newIntent, 0);
        for (ResolveInfo i : infos) {
            ComponentName cn = new ComponentName(i.activityInfo.packageName,
                    i.activityInfo.name);
            if (cn.equals(oldComponent)) {
                resolveInfo = i;
            }
        }
        if (resolveInfo != null) {
            newIntent.setComponent(oldComponent);
        }
        
        // from the resource
        if (resolveInfo != null) {
            ComponentName key = LauncherModel.getComponentNameFromResolveInfo(resolveInfo);
            if (labelCache != null && labelCache.containsKey(key)) {
                info.title = labelCache.get(key);
            } else {
                info.title = resolveInfo.activityInfo.loadLabel(manager);
                if (labelCache != null) {
                    labelCache.put(key, info.title);
                }
            }
        }
        // from the db
        if (info.title == null) {
            if (c != null) {
                info.title =  c.getString(titleIndex);
            }
        }
        // fall back to the class name of the activity
        if (info.title == null) {
            info.title = componentName.getClassName();
        }
        info.itemType = LauncherSettings.Favorites.ITEM_TYPE_APPLICATION;

        if (c != null) {
            try {
                int index = c.getColumnIndexOrThrow(LauncherSettings.Favorites.IS_SDAPP);
                info.isSDApp = c.getInt(index);
            } catch (Exception ex) {
            }
        }
        if (resolveInfo != null) {
            info.isSDApp = Utils.isSdcardApp(resolveInfo.activityInfo.applicationInfo.flags)?1:0;
        }
        
        info.intent = intent;
        IconCursorInfo cursorinfo = new IconCursorInfo(c,iconIndex);
        icon = mApp.getIconManager().getAppUnifiedIcon(info,cursorinfo);
        info.setIcon(icon);
        info.customIcon = (info.isSDApp==0)?false:true;
        
        info.setSystemAppFlag(resolveInfo);
        return info;
    }

    /**
     * Returns the set of workspace ShortcutInfos with the specified intent.
     */
    public static ArrayList<ItemInfo> getWorkspaceShortcutItemInfosWithIntent(
            Intent intent) {
        ArrayList<ItemInfo> items = new ArrayList<ItemInfo>();
        synchronized (sBgLock) {
            for (ItemInfo info : sBgWorkspaceItems) {
                if (info instanceof ShortcutInfo) {
                    ShortcutInfo shortcut = (ShortcutInfo) info;
                    if (shortcut.intent.toUri(0).equals(intent.toUri(0))) {
                        items.add(shortcut);
                    }
                }
            }
        }
        return items;
    }

    /**
     * Make an ShortcutInfo object for a shortcut that isn't an application.
     */
    private ShortcutInfo getShortcutInfo(Cursor c, Context context,
            int iconTypeIndex, int iconPackageIndex, int iconResourceIndex, int iconIndex,
            int titleIndex) {
        Drawable icon = null;
        final ShortcutInfo info = new ShortcutInfo();
        info.itemType = LauncherSettings.Favorites.ITEM_TYPE_SHORTCUT;
        if (c == null) {
            return info;
        }
        int type = c.getInt(c.getColumnIndexOrThrow(LauncherSettings.Favorites.ITEM_TYPE));
        
        if(type==LauncherSettings.Favorites.ITEM_TYPE_SHORTCUT_DOWNLOADING){
            info.itemType = LauncherSettings.Favorites.ITEM_TYPE_SHORTCUT_DOWNLOADING;
            info.setAppDownloadStatus(AppDownloadStatus.STATUS_PAUSED);
        }
        else if (type==LauncherSettings.Favorites.ITEM_TYPE_VPINSTALL) {
            info.itemType = LauncherSettings.Favorites.ITEM_TYPE_VPINSTALL;
        }
        

        // TODO: If there's an explicit component and we can't install that, delete it.

        info.title = c.getString(titleIndex);

        int iconType = c.getInt(iconTypeIndex);
        switch (iconType) {
        case LauncherSettings.Favorites.ICON_TYPE_RESOURCE:
            String packageName = c.getString(iconPackageIndex);
            String resourceName = c.getString(iconResourceIndex);
            PackageManager packageManager = context.getPackageManager();
            info.customIcon = false;
            // the resource
            try {
                Resources resources = packageManager.getResourcesForApplication(packageName);
                if (resources != null) {
                    //final int id = resources.getIdentifier(resourceName, null, null);
                    //icon = Utilities.createIconDrawable(mIconManager.getFullResIcon(resources, id), context);
                }
            } catch (Exception e) {
                // drop this.  we have other places to look for icons
            }
            // the db
            Log.d(TAG,"getshortinfo step 1");
            IconCursorInfo cursorinfo = new IconCursorInfo(c,iconIndex,true);
            icon = mIconManager.getAppUnifiedIcon(info, cursorinfo);
            break;
        case LauncherSettings.Favorites.ICON_TYPE_BITMAP:
            Log.d(TAG,"getshortinfo step 2");
            IconCursorInfo cursor_info = new IconCursorInfo(c,iconIndex,true);
            icon = mApp.getIconManager().getAppUnifiedIcon(info,cursor_info);
            if(mApp.getIconManager().isDefaultIcon(icon)){
                info.customIcon = false;
                info.usingFallbackIcon = true;  
            }else{
                info.customIcon = true;
            }
            break;
        default:
            Log.d(TAG,"getshortinfo step 3");
            icon = getFallbackIcon();
            info.usingFallbackIcon = true;
            info.customIcon = false;
            break;
        }

//        Drawable newIcon = mIconManager.buildUnifiedIcon(icon);
//        if (newIcon == null) {
//            newIcon = icon;
//        } else if (!newIcon.equals(icon)) {
//          //@@@@@@ need confirm 
//          //((FastBitmapDrawable)icon).getBitmap().recycle();
//            icon = null;
//        }
        Log.d(TAG,"getshortinfo step 4");
        info.setIcon(icon);

        if (c != null) {
            try {
                int index = c.getColumnIndexOrThrow(LauncherSettings.Favorites.IS_SDAPP);
                info.isSDApp = c.getInt(index);
            } catch (Exception ex) {
            }
        }

        return info;
    }

    public void vpInstallItemsCheck() {
        if (mIsLoaderTaskRunning == true) {
            Log.d(TAG, "load task is running, wait");
            sWorker.post(new Runnable() {
                @Override
                public void run() {
                    vpInstallItemsCheck();
                }
            });
            return;
        }
        Log.d(TAG, "vp install item check start");
        //item list to be removed
        final ArrayList<ItemInfo> removeinfos = new ArrayList<ItemInfo>();
        List<PackageInfo> pkginfolist = null;
        ArrayList<ItemInfo> allApps = getAllAppItems();
        for(ItemInfo item: allApps) {
            if (item.itemType == Favorites.ITEM_TYPE_VPINSTALL) {
                Intent vpintent = ((ShortcutInfo)item).intent;
                if (vpintent == null) {
                    removeinfos.add(item);
                    continue;
                }
                String path = vpintent.getStringExtra(VPUtils.TYPE_PACKAGEPATH);
                if ((path ==null) || (path.equals(""))) {
                    removeinfos.add(item);
                    continue;
                }
                File vpfile = new File(path);
                if ((vpfile != null) && (vpfile.exists() == true)) {
                    continue;
                }
                Log.d(TAG, "vp item's apk not find " + path);
                //if the apk can't find by path, find the same component name apk in /system/etc/property/vp-app
                //and update vp item's path if same component name apk is found, or remove the vp item
                String compname = vpintent.getStringExtra(VPUtils.TYPE_PACKAGENAME);
                boolean componentfound = false;
                //create pkginfolist only when it is needed
                if (pkginfolist == null) {
                    pkginfolist = mVPUtils.ScanVPInstallDir();
                }
                if (pkginfolist == null) {
                    Log.d(TAG, "scan vp install dir failed");
                    removeinfos.add(item);
                    //creat a empty list
                    pkginfolist = new ArrayList<PackageInfo>();
                    continue;
                }
                for(PackageInfo pkgInfo: pkginfolist) {
                    if ((pkgInfo.packageName != null) && (pkgInfo.packageName.equals(compname) == true)) {
                        Log.d(TAG, "vp item's new path is " + pkgInfo.applicationInfo.sourceDir);
                        vpintent.putExtra(VPUtils.TYPE_PACKAGEPATH, pkgInfo.applicationInfo.sourceDir);
                        componentfound = true;
                        final ItemInfo finalitem = item;
                        final Intent intent = new Intent(vpintent);
                        //run in another runnable to avoid block worker thread
                        Runnable r = new Runnable() {
                            @Override
                            public void run() {
                                final ContentValues values = new ContentValues();
                                values.put(LauncherSettings.BaseLauncherColumns.INTENT, intent.toUri(0));
                                updateItemInDatabaseHelper(mApp, values, finalitem, "vpInstallItemsCheck");
                            }
                        };
                        runOnWorkerThread(r);
                        continue;
                    }
                }
                if (componentfound == false) {
                    removeinfos.add(item);
                }
            }
        }

        //remove all invalide vp items
        if (removeinfos.size() > 0) {
            notifyUIRemoveIcon(removeinfos, true, false);
        }

        if (pkginfolist != null) {
            pkginfolist.clear();
        }
        Log.d(TAG, "vp install item check finish");
    }

    public void vpInstallInit() {
        if (mIsLoaderTaskRunning == true) {
            Log.d(TAG, "load task is running, wait");
            sWorker.post(new Runnable() {
                @Override
                public void run() {
                    vpInstallInit();
                }
            });
            return;
        }
        //get all vp package info from special dir
        final ArrayList<ItemInfo> infos = mVPUtils.createVPInstallShortcutInfos();
        if (infos == null) {
            return;
        }
        Log.d(TAG, "infos list size is " + infos.size());
        final ArrayList<ItemInfo> folderinfos = new ArrayList<ItemInfo>();
        final ArrayList<ItemInfo> apkinfos = new ArrayList<ItemInfo>();
        final HashMap<Long, FolderInfo> folderinfoshash = new HashMap<Long, FolderInfo>();
        for (ItemInfo info: infos) {
            if (info.itemType == Favorites.ITEM_TYPE_FOLDER) {
                folderinfos.add(info);
                folderinfoshash.put(info.id, (FolderInfo) info);
            } else if (info.itemType == Favorites.ITEM_TYPE_VPINSTALL) {
                if (info.container < 0) {
                    apkinfos.add(info);
                }
            }
        }
        final Callbacks callbacks = LauncherModel.mCallbacks != null ? LauncherModel.mCallbacks.get() : null;
        final Runnable r = new Runnable() {
            @Override
            public void run() {
                if (callbacks != null) {
                    Log.d(TAG, "bind folders first");
                    callbacks.bindItemsAdded(folderinfos);
                    callbacks.bindFolders(folderinfoshash);
                    Log.d(TAG, "then bind vp items");
                    if (apkinfos.size() > 0) {
                        callbacks.bindItemsAdded(apkinfos);
                    }
                    folderinfos.clear();
                    folderinfoshash.clear();
                    apkinfos.clear();
                    infos.clear();
                }
            }
        };
        if ((infos != null) && (infos.size() > 0)) {
            LauncherModel.runOnMainThread(r);
        }
    }

    ArrayList<ItemInfo> VPInstallDrawingList = new ArrayList<ItemInfo>();
    Runnable mVPInstallDrawLoading = new Runnable() {
        public void run() {
            final ArrayList<ItemInfo> drawingList = new ArrayList<ItemInfo>();
            for (ItemInfo item: VPInstallDrawingList) {
               int progress = ((ShortcutInfo)item).getProgress();
               ((ShortcutInfo)item).setProgress(progress%100 + 5);
               drawingList.add(item);
            }
            final Callbacks callbacks = mCallbacks != null ? mCallbacks.get() : null;
            final Runnable r = new Runnable() {
                @Override
                public void run() {
                    if (callbacks != null) {
                        callbacks.bindItemsUpdated(drawingList);
                    }
                }
            };
            if (VPInstallDrawingList.size() > 0) {
                runOnMainThread(r);
                sWorker.postDelayed(mVPInstallDrawLoading, 500);
            }
        }
    };
    public void startVPSilentInstall(ShortcutInfo shortcutinfo) {
        final ShortcutInfo finalInfo = shortcutinfo;
        Runnable r = new Runnable() {
            public void run() {
                Log.d(TAG, "start startVPSilentInstall in worker thread");
                if (mVPUtils.installSilently(finalInfo) == true) {
                    final Callbacks callbacks = mCallbacks != null ? mCallbacks.get() : null;
                    final ArrayList<ItemInfo> infos = new ArrayList<ItemInfo>(1);
                    infos.add(finalInfo);
                    final Runnable r = new Runnable() {
                        @Override
                        public void run() {
                            if (callbacks != null) {
                                callbacks.bindItemsUpdated(infos);
                            }
                        }
                    };
                    runOnMainThread(r);
                    VPInstallDrawingList.add(finalInfo);
                    sWorker.removeCallbacks(mVPInstallDrawLoading);
                    sWorker.post(mVPInstallDrawLoading);
                }
            }
        };
        runOnWorkerThread(r);
    }

    Object vplock  = new Object();

    @Override
    public void onInstallStateChange(AppKey appKey, int state) {
        Log.d(TAG, "onInstallStateChange is called");
        Log.d(TAG, "appKey is " + appKey.packageName + " state is " + state);
        synchronized(vplock) {
            final AppKey finalappKey = appKey;
            final int finalstate = state;
            if ((state != VPInstaller.INSTALL_FAILED) && (state != VPInstaller.INSTALLED)) {
                return;
            }
            sWorker.post(new Runnable(){
                @Override
                public void run() {
                    Log.d(TAG, "appKey is " + finalappKey.packageName + " state is " + finalstate);
                                        //topwise zyf add for exapp
                    
                    if(getExappApk(finalappKey.packageName)!=null)
            		{
                    	for (ItemInfo info: sApkItemsMap.values()) 
                    	{

                        	Log.d("VPInstaller", "info is " + info);
                            if(finalappKey.packageName.equals(((ShortcutInfo)info).intent.getStringExtra(VPUtils.TYPE_PACKAGENAME))) {
                                ((ShortcutInfo) info).setVPInstallStatus(VPInstallStatus.STATUS_NORMAL);
                                ((ShortcutInfo) info).setAppDownloadStatus(AppDownloadStatus.STATUS_INSTALLED);
                                boolean ret = VPInstallDrawingList.remove(info);
                                final Callbacks callbacks = mCallbacks != null ? mCallbacks.get() : null;
                                final ArrayList<ItemInfo> infos = new ArrayList<ItemInfo>();
                                infos.add(info);
                                final Runnable r = new Runnable() {
                                    @Override
                                    public void run() {
                                        if (callbacks != null) {
                                            Log.d("VPInstaller", "onInstallStateChange bindItemsUpdated");
                                            /*
                                            if (finalstate == VPInstaller.INSTALLED) {
                                                ToastManager.makeToast(ToastManager.APP_EXIST);
                                            } else
                                            */
                                            if (finalstate == VPInstaller.INSTALL_FAILED){
                                                if (Utils.isInUsbMode()) {
                                                    ToastManager.makeToast(ToastManager.APP_UNAVAILABLE_IN_USB);
                                                } else {
                                                    ToastManager.makeToast(ToastManager.APP_NOT_FOUND);
                                                }
                                            }
                                            callbacks.bindItemsUpdated(infos);
                                            sWorker.postDelayed(new Runnable() {

                                                @Override
                                                public void run() {
                                                	 Context context = LauncherApplication.getContext();
                                                     runApk(finalappKey.packageName,context);
                                                }
                                            }, 2000);
                                           
                                        }
                                    }
                                };
                                runOnMainThread(r);
                            }
                        
                    	}
                    	return;
            		}
                    else//add for folderonline
                    {
                    	ShortcutInfo shortcutinfo=findItemInfoByPkgName(finalappKey.packageName);
                    	Log.d("VPInstaller", "shortcutinfo is " + shortcutinfo);
                    	if(shortcutinfo!=null)
                    	{
                    		shortcutinfo.setVPInstallStatus(VPInstallStatus.STATUS_NORMAL);
                    		shortcutinfo.setAppDownloadStatus(AppDownloadStatus.STATUS_INSTALLED);
                            boolean ret = VPInstallDrawingList.remove(shortcutinfo);
                            final Callbacks callbacks = mCallbacks != null ? mCallbacks.get() : null;
                            final ArrayList<ItemInfo> infos = new ArrayList<ItemInfo>();
                            infos.add(shortcutinfo);
                            final Runnable r = new Runnable() {
                                @Override
                                public void run() {
                                    if (callbacks != null) {
                                        Log.d("VPInstaller", "onInstallStateChange bindItemsUpdated");
                                        /*
                                        if (finalstate == VPInstaller.INSTALLED) {
                                            ToastManager.makeToast(ToastManager.APP_EXIST);
                                        } else
                                        */
                                        if (finalstate == VPInstaller.INSTALL_FAILED){
                                            if (Utils.isInUsbMode()) {
                                                ToastManager.makeToast(ToastManager.APP_UNAVAILABLE_IN_USB);
                                            } else {
                                                ToastManager.makeToast(ToastManager.APP_NOT_FOUND);
                                            }
                                        }
                                        callbacks.bindItemsUpdated(infos);
                                        
                                        sWorker.postDelayed(new Runnable() {

                                            @Override
                                            public void run() {
                                            	Context context = LauncherApplication.getContext();                
                                                runApk(finalappKey.packageName,context);
                                            }
                                        }, 2000);
                                        
                                    }
                                }
                            };
                            runOnMainThread(r);
                        
                    	}
                    }
                    ////topwise zyf add  end

						ArrayList<ItemInfo> allApps = getAllAppItems();
                    for (ItemInfo info: allApps) {
                        if (info.itemType == Favorites.ITEM_TYPE_VPINSTALL) {
                            if(finalappKey.packageName.equals(((ShortcutInfo)info).intent.getStringExtra(VPUtils.TYPE_PACKAGENAME))) {
                                ((ShortcutInfo) info).setVPInstallStatus(VPInstallStatus.STATUS_NORMAL);
                                boolean ret = VPInstallDrawingList.remove(info);
                                final Callbacks callbacks = mCallbacks != null ? mCallbacks.get() : null;
                                final ArrayList<ItemInfo> infos = new ArrayList<ItemInfo>();
                                infos.add(info);
                                final Runnable r = new Runnable() {
                                    @Override
                                    public void run() {
                                        if (callbacks != null) {
                                            Log.d(TAG, "onInstallStateChange bindItemsUpdated");
                                            /*
                                            if (finalstate == VPInstaller.INSTALLED) {
                                                ToastManager.makeToast(ToastManager.APP_EXIST);
                                            } else
                                            */
                                            if (finalstate == VPInstaller.INSTALL_FAILED){
                                                if (Utils.isInUsbMode()) {
                                                    ToastManager.makeToast(ToastManager.APP_UNAVAILABLE_IN_USB);
                                                } else {
                                                    ToastManager.makeToast(ToastManager.APP_NOT_FOUND);
                                                }
                                            }
                                            callbacks.bindItemsUpdated(infos);
                                        }
                                    }
                                };
                                runOnMainThread(r);
                            }
                        }
                    }
                }
            });
        }
    }

    public void checkInstallingState() {
        final Runnable r = new Runnable(){
            @Override
            public void run() {
                sWorker.removeCallbacks(mInstallingItemsCheck);
                sWorker.postDelayed(mInstallingItemsCheck, 5000);
            }
        };
        runOnWorkerThread(r);
    }

    public void stopCheckInstallingState() {
        final Runnable r = new Runnable(){
            @Override
            public void run() {
                sWorker.removeCallbacks(mInstallingItemsCheck);
            }
        };
        runOnWorkerThread(r);
    }

    //this runnable is used to check all installing items install state
    //if the item is installed, change the item's statue from download to normal app
    //to avoid launcher model miss package add intent from system
    //if package add intent is missed, launcher model can't enter edit mode.
    Runnable mInstallingItemsCheck = new Runnable() {
        public void run() {
            ArrayList<ShortcutInfo> downloadItemsList = new ArrayList<ShortcutInfo>();
            Log.d(TAG, "mInstallingItemsCheck in");
            ArrayList<ItemInfo> allApps = getAllAppItems();
            for(ItemInfo item: allApps) {
                if (item.itemType == Favorites.ITEM_TYPE_SHORTCUT_DOWNLOADING) {
                    downloadItemsList.add((ShortcutInfo)item);
                }
            }
            ArrayList<ShortcutInfo> rmList = new ArrayList<ShortcutInfo>();

            for (ShortcutInfo instInfo: downloadItemsList) {
                if (instInfo == null) {
                    continue;
                }
                //check app is installed
                Context context = LauncherApplication.getContext();
                PackageManager pm = context.getPackageManager();
                Intent intent = instInfo.intent;
                if ((intent == null) || (intent.getStringExtra(AppDownloadManager.TYPE_PACKAGENAME) == null)) {
                    rmList.add(instInfo);
                    continue;
                }
                String pkgname = intent.getStringExtra(AppDownloadManager.TYPE_PACKAGENAME);
                Log.d(TAG, "check " + pkgname + " is installed");
                PackageInfo pkgInfo = null;
                boolean isInstalled = false;
                try {
                    pkgInfo = pm.getPackageInfo(pkgname, 0);
                    Log.d(TAG, "apk is installed");
                    isInstalled = true;
                    if (AppFreezeUtil.isPackageFrozen(context, pkgname)) {
                        Log.d(TAG, "apk is frozen");
                    }
                } catch (NameNotFoundException e) {
                    Log.d(TAG, "PackageManager.getPackageInfo failed for " + pkgname);
                    isInstalled = false;
                }
                if (isInstalled == true) {
                    //change shortcut info's state
                    Log.d(TAG, "call app add");
                    PackageUpdatedTask task = new PackageUpdatedTask(PackageUpdatedTask.OP_ADD, new String[] { pkgname });
                    mPackageUpdateTaskQueue.enqueue(task);
                    rmList.add(instInfo);
                }
            }

            //remove not installing item from downloadItemsList
            for(ShortcutInfo rmInfo: rmList) {
                downloadItemsList.remove(rmInfo);
            }
            rmList.clear();

            if (isDownloadItemsListAvaliable(downloadItemsList) == true){
                sWorker.removeCallbacks(mInstallingItemsCheck);
                sWorker.postDelayed(mInstallingItemsCheck, 20000);
            } else {
                if (downloadItemsList.size() > 0) {
                    downloadItemsList.clear();
                }
            }
            Log.d(TAG, "mInstallingItemsCheck out");
        }
    };

    private boolean isDownloadItemsListAvaliable(ArrayList<ShortcutInfo> downloadItemsList) {
        boolean ret = false;
        if (downloadItemsList.size() <= 0) {
            return ret;
        }
        for (ShortcutInfo instInfo: downloadItemsList) {
            if (instInfo != null) {
                ret = true;
                break;
            }
        }
        return ret;
    }

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

    public void installShortcutInWorkerThread(Context contextin, Intent datain,
            String namein, final Intent intent, final SharedPreferences sharedPrefs) {
        if(namein == null || TextUtils.isEmpty(namein.trim())) {
            Toast.makeText(contextin, contextin.getString(R.string.shortcut_name_empty), Toast.LENGTH_LONG).show();
            return;
        }
        Log.d(TAG, "installShortcutInWorkerThread in");
        final Context context = contextin;
        final Intent data = datain;
        final String name  = namein;

        //run in worker thread for all operations about bg list and db, to avoid async issue
        Runnable r = new Runnable() {
            public void run() {
                long shortcutid = shortcutExists(context, name, intent);
                Log.d(TAG, "shortcutid is " + shortcutid);
                boolean duplicate = data.getBooleanExtra(Launcher.EXTRA_SHORTCUT_DUPLICATE, true);
                if ((shortcutid != -1) && (duplicate == false)) {
                    ItemInfo existItem = sBgItemsIdMap.get(shortcutid);
                    boolean isSame = true;
                    if ((existItem != null) && (existItem.itemType == Favorites.ITEM_TYPE_SHORTCUT)) {
                        String newName = data.getStringExtra(Intent.EXTRA_SHORTCUT_NAME);
                        if ((newName != null) && (existItem.title != null) &&
                                !(existItem.title.toString().equals(newName.toString()))) {
                            //same shortcut url but different title
                            //need to update title
                            isSame = false;
                            existItem.title = newName;
                            updateItemInDatabase(context, existItem);
                            //update the title in UI
                            final Callbacks oldCallbacks = mCallbacks != null ? mCallbacks.get() : null;
                            final ArrayList<ItemInfo> items = new ArrayList<ItemInfo>();
                            items.add(existItem);
                            final Runnable r = new Runnable() {
                                @Override
                                public void run() {
                                    Callbacks callbacks = tryGetCallbacks(oldCallbacks);
                                    if (callbacks != null) {
                                        callbacks.bindItemsUpdated(items);
                                    }
                                }
                            };
                            runOnMainThread(r, MAIN_THREAD_BINDING_RUNNABLE);
                            Toast.makeText(context, context.getString(R.string.shortcut_updated, name),
                                        Toast.LENGTH_SHORT).show();
                        }
                    }
                    if (isSame == true) {
                        Toast.makeText(context, context.getString(R.string.shortcut_duplicate, name),
                                                   Toast.LENGTH_SHORT).show();
                    }
                } else {
                    ScreenPosition pos = findEmptyCell();
                    if (pos != null) {
                        final int screen = pos.s;

                        if (intent != null) {
                            if (intent.getAction() == null) {
                                intent.setAction(Intent.ACTION_VIEW);
                            } else if (intent.getAction().equals(Intent.ACTION_MAIN) &&
                                    intent.getCategories() != null &&
                                    intent.getCategories().contains(Intent.CATEGORY_LAUNCHER)) {
                                intent.addFlags(
                                    Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
                            }

/*
                            new Thread("setNewAppsThread") {
                                public void run() {
                                    // If the new app is going to fall into the same page as before,
                                    // then just continue adding to the current page
                                    final int newAppsScreen = sharedPrefs.getInt(
                                            NEW_APPS_PAGE_KEY, screen);
                                    SharedPreferences.Editor editor = sharedPrefs.edit();
                                    if (newAppsScreen == -1 || newAppsScreen == screen) {
                                        addToStringSet(sharedPrefs,
                                            editor, NEW_APPS_LIST_KEY, intent.toUri(0));
                                    }
                                    editor.putInt(NEW_APPS_PAGE_KEY, screen);
                                    editor.commit();
                                }
                            }.start();
*/
                            ShortcutInfo info = addShortcut(context, data,
                                    LauncherSettings.Favorites.CONTAINER_DESKTOP, screen,
                                    pos.x, pos.y, false);
                            if (info == null) {
                                Toast.makeText(context, "Create info error",
                                                   Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(context, context.getString(R.string.shortcut_added, name),
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    } else {
                        Toast.makeText(context, context.getString(R.string.completely_out_of_space),
                                               Toast.LENGTH_SHORT).show();
                    }
                }
            }
        };
        runOnWorkerThread(r);
    }

    public ShortcutInfo addShortcut(Context context, Intent data,
            long container, int screen,
            int cellX, int cellY, boolean notify) {
        final ShortcutInfo info = infoFromShortcutIntent(context, data, null);
        if (info == null) {
            return null;
        }
        info.isNew = 1;
        addItemToDatabase(context, info, container, screen, cellX, cellY, notify);
        //save original icon in db and then render the icon by theme
        Drawable orgIcon = info.mIcon;
        info.setIcon(mApp.getIconManager().buildUnifiedIcon(orgIcon));

        final Callbacks callbacks = mCallbacks != null ? mCallbacks.get() : null;
        final ArrayList<ItemInfo> infos = new ArrayList<ItemInfo>(1);
        infos.add(info);
        final Runnable r = new Runnable() {
            @Override
            public void run() {
                if (callbacks != null) {
                    callbacks.bindItems(infos, 0, infos.size());
                }
            }
        };
        mHandler.post(r);

        return info;
    }

    public void unInstallShortcutInWorkerThread(Context contextin,
            Intent datain,
                final SharedPreferences sharedPrefs) {
        Log.d(TAG, "unInstallShortcutInWorkerThread in");
        final Context context = contextin;
        final Intent data = datain;

        //run in worker thread for all operations about bg list and db, to avoid async issue
        Runnable r = new Runnable() {
            public void run() {
                String name = data.getStringExtra(Intent.EXTRA_SHORTCUT_NAME);
                if (name == null) {
                    name = data.getStringExtra("label");
                }
                final ArrayList<ItemInfo> infos = new ArrayList<ItemInfo>();
                if(name != null) {
                    ArrayList<ItemInfo> allApps = getAllAppItems();
                    for(ItemInfo info: allApps) {
                        if ((info.itemType == LauncherSettings.Favorites.ITEM_TYPE_SHORTCUT) &&
                             (info.title.equals(name))) {
                            Log.d(TAG, "find the delete item: id " + info.id);
                            infos.add(info);
                            break;
                        }
                    }
                }

                final Callbacks callbacks = mCallbacks != null ? mCallbacks.get() : null;
                Runnable r = new Runnable() {
                    @Override
                    public void run() {
                        if (callbacks != null) {
                            callbacks.bindItemsRemoved(infos);
                        }
                    }
                };
                mHandler.post(r);
            }
        };
        runOnWorkerThread(r);
    }


    /**
     * Attempts to find an AppWidgetProviderInfo that matches the given component.
     */
    AppWidgetProviderInfo findAppWidgetProviderInfoWithComponent(Context context,
            ComponentName component) {
        List<AppWidgetProviderInfo> widgets =
            AppWidgetManager.getInstance(context).getInstalledProviders();
        for (AppWidgetProviderInfo info : widgets) {
            if (info.provider.equals(component)) {
                return info;
            }
        }
        return null;
    }

    private static void initOccupied(boolean[][][] occupied) {
        // TODO Auto-generated method stub
        int screen;
        int x;
        int y;
        int spanX;
        int spanY;

        List<ItemInfo> workspaceItems, appWidgets;
        synchronized (sBgLock) {
            workspaceItems = new ArrayList<ItemInfo>(sBgWorkspaceItems);
            appWidgets = new ArrayList<ItemInfo>(sBgAppWidgets);
        }

        for (ItemInfo info : workspaceItems) {
            if (info.container == LauncherSettings.Favorites.CONTAINER_DESKTOP) {
                screen = info.screen;
                if (!(screen > -1 && screen < mMaxScreenCount)) {
                    continue;
                }

                x = info.cellX;
                y = info.cellY;
                spanX = info.spanX;
                spanY = info.spanY;

                if ((x >= mCellCountX) ||
                    (y >= mCellCountY) ||
                    (x + spanX > mCellCountX) ||
                    (y + spanY > mCellCountY) ||
                    (x < 0) || (y < 0)) {
                    Log.d(TAG, "initOccupied item position error " + info.id);
                    continue;
                }

                for (int i = 0; i < spanX; i++) {
                    for (int j = 0; j < spanY; j++) {
                        occupied[screen][x + i][y + j] = true;
                    }
                }
            }
        }
        workspaceItems.clear();

        for (ItemInfo info : appWidgets) {
            if (info.container == LauncherSettings.Favorites.CONTAINER_DESKTOP) {
                screen = info.screen;
                x = info.cellX;
                y = info.cellY;
                spanX = info.spanX;
                spanY = info.spanY;
                if ((x >= mCellCountX) ||
                    (y >= mCellCountY) ||
                    (x + spanX > mCellCountX) ||
                    (y + spanY > mCellCountY) ||
                    (x < 0) || (y < 0) ||
                    (screen < 0) || (screen >= mMaxScreenCount)) {
                    Log.d(TAG, "initOccupied item position error " + info.id);
                    continue;
                }
                for (int i = 0; i < spanX; i++) {
                    for (int j = 0; j < spanY; j++) {
                        occupied[screen][x + i][y + j] = true;
                    }
                }
            }
        }
        appWidgets.clear();
    }

    private static boolean checkAvaluable(boolean[][] bs, int x, int y, int spanX,
            int spanY) {
        // TODO Auto-generated method stub
        for (int i = 0; i < spanY; i++) {
            for (int j = 0; j < spanX; j++) {
                if (bs[x + j][y + i]) {
                    return false;
                }
            }
        }
        return true;
    }

    private static ScreenPosition getAvailableInScreen(int screenId, boolean[][] bs, int spanX,
            int spanY) {
        // TODO Auto-generated method stub
        int checkEndX = LauncherModel.getCellCountX() - spanX;
        int checkEndY = LauncherModel.getCellCountY() - spanY;
        for (int i = 0; i <= checkEndY; i++) {
            for (int j = 0; j <= checkEndX; j++) {
                if (checkAvaluable(bs, j, i, spanX, spanY)) {
                    ScreenPosition res = new ScreenPosition(screenId, j, i);
                    return res;
                }
            }
        }
        return null;
    }

    static ScreenPosition isCellEmtpy(int screen, int x, int y, int spanX, int spanY) {
        ScreenPosition p = new ScreenPosition(screen, x, y);
        //todo, check if position is occupied
        int xCount = LauncherModel.getCellCountX();
        int yCount = LauncherModel.getCellCountY();
        int scrnCount = mMaxScreenCount;

        if (x<xCount && y<yCount&&screen<scrnCount&&spanX<=xCount&&spanY<=yCount) {
            boolean[][][] occupied = new boolean[scrnCount][xCount][yCount];
            initOccupied(occupied);
            if (checkAvaluable(occupied[screen], x, y, spanX, spanY)) {
                return p;
            }
        }
        
        return null;
    }

    public static ScreenPosition isCellEmtpy(int screen, int x, int y) {
        ScreenPosition p = new ScreenPosition(screen, x, y);
        //todo, check if position is occupied
        int xCount = LauncherModel.getCellCountX();
        int yCount = LauncherModel.getCellCountY();
        int scrnCount = mMaxScreenCount;

        if (x<xCount && y<yCount&&screen<scrnCount) {
            boolean[][][] occupied = new boolean[scrnCount][xCount][yCount];
            initOccupied(occupied);
            if (occupied[screen][x][y]) {
                return null;
            }
        }
        
        return p;
    }
    static ScreenPosition findEmptyCell(int startScreen, int spanX, int spanY){
        Log.d(TAG, "findEmptyCell in");
        int xCount = LauncherModel.getCellCountX();
        int yCount = LauncherModel.getCellCountY();
        int scrnCount = mMaxScreenCount;

        if (spanX>xCount || spanY>yCount) {
            Log.d(TAG, "findEmptyCell,input is wrong:spanX="+spanX+",spanY="+spanY);
            return null;
        }

        boolean[][][] occupied = new boolean[scrnCount][xCount][yCount];
        initOccupied(occupied);
         // the main screen (index = 0) is special case, installed application and shotcuts will not be on it.
        for (int i = startScreen; i < scrnCount; i++) {
            ScreenPosition res = getAvailableInScreen(i, occupied[i], spanX, spanX);
            if (res != null) {
                Log.d(TAG, "findEmptyCell out screen:" + res.s
                                 + " cellx:" + res.x + " celly:" + res.y);
                return res;
            }
        }
        Log.d(TAG, "findEmptyCell out null");
        return null;
    }

    public static ScreenPosition findEmptyCellInHideSeat() {
        ScreenPosition[] pos = findEmptyCellsInHideSeat(1);
        if (pos == null) return null;
        else return pos[0];
    }

    public static ScreenPosition[] findEmptyCellsInHideSeat(int count) {
        if (count <= 0) return new ScreenPosition[0];

        final int xCount = ConfigManager.getHideseatMaxCountX();
        final int yCount = ConfigManager.getHideseatMaxCountY();
        final int scrnCount = ConfigManager.getHideseatItemsMaxCount() / (xCount * yCount);

        boolean[][][] occupied = new boolean[scrnCount][xCount][yCount];

        final ArrayList<ItemInfo> tmpWorkspaceItems = new ArrayList<ItemInfo>();
        synchronized (sBgLock) {
            tmpWorkspaceItems.addAll(sBgWorkspaceItems);
        }

        for (ItemInfo info : tmpWorkspaceItems) {
            if (info.container == LauncherSettings.Favorites.CONTAINER_HIDESEAT) {
                if (info.screen < 0 || info.screen >= scrnCount) continue;
                if ((info.cellX >= xCount) || (info.cellY >= yCount) ||
                    (info.cellX < 0) || (info.cellY < 0)) {
                    continue;
                }
                occupied[info.screen][info.cellX][info.cellY] = true;
            }
        }

        List<ScreenPosition> rst = new ArrayList<ScreenPosition>(count);
        for (int screenId = 0; screenId < scrnCount; screenId++) {
            for (int y = 0; y < yCount; y++) {
                for (int x = 0; x < xCount; x++) {
                    if (!occupied[screenId][x][y]) {
                        rst.add(new ScreenPosition(screenId, x, y));
                        if (rst.size() == count) {
                            return rst.toArray(new ScreenPosition[count]);
                        }
                    }
                }
            }
        }
        return null;
    }

    private static int obtainStartScreen() {
        // check if only one empty celllayout in workspace
        int startScreen = ConfigManager.DEFAULT_FIND_EMPTY_SCREEN_START;
        List<ItemInfo> workspaceItems;
        synchronized (sBgLock) {
            workspaceItems = new ArrayList<ItemInfo>(sBgWorkspaceItems);
        }
        for (ItemInfo info : workspaceItems) {
            if (info.container == LauncherSettings.Favorites.CONTAINER_DESKTOP && info.screen != ConfigManager.getDefaultScreen()) {
                startScreen = ConfigManager.DEFAULT_FIND_EMPTY_SCREEN_START;
                break;
            }
            startScreen = ConfigManager.getDefaultScreen();
        }
        return startScreen;
    }

    public static ScreenPosition findEmptyCell() {
        int startScreen = obtainStartScreen();
        return findEmptyCell(startScreen, 1, 1);
    }

    public static ScreenPosition findEmptyCell(int startScreen) {
        return findEmptyCell(startScreen, 1, 1);
    } 
    /**
     * 
     * @param startScreen
     * @param direction 0:left, 1:right
     * @return
     */
    public static ScreenPosition findEmptyCellForFling(int currentScr, int direction) {
        Log.d(TAG, "sxsexe-----------> ++++findEmptyCellForFling");
        Launcher launcher = LauncherApplication.getLauncher();
        if (launcher == null || launcher.getWorkspace() == null) {
            return null;
        }
        int screenCount = launcher.getWorkspace().getNormalScreenCount();
        
        if(direction == 0 && currentScr == 0) {
            return null;
        }
        if(direction == 1 && currentScr == (screenCount - 1)) {
            return null;
        }
        
        int xCount = LauncherModel.getCellCountX();
        int yCount = LauncherModel.getCellCountY();
        int scrnCount = screenCount;

        boolean[][][] occupied = new boolean[scrnCount][xCount][yCount];
        initOccupied(occupied);
        
        if(direction == 1) {
            for (int i = currentScr + 1; i < scrnCount; i++) {
                ScreenPosition res = getAvailableInScreen(i, occupied[i], 1, 1);
                if (res != null) {
                    Log.d(TAG, "sxsexe----------->findEmptyCellForFling out screen:" + res.s
                            + " cellx:" + res.x + " celly:" + res.y);
                    return res;
                }
            }
        } else if(direction == 0) {
            for (int i = currentScr - 1; i >= 0; i--) {
                ScreenPosition res = getAvailableInScreen(i, occupied[i], 1, 1);
                if (res != null) {
                    Log.d(TAG, "sxsexe----------->findEmptyCellForFling out screen:" + res.s
                            + " cellx:" + res.x + " celly:" + res.y);
                    return res;
                }
            }
        }
        
        Log.d(TAG, "sxsexe-----------> findEmptyCellForFling out null");
        return null;
    }

    public static int getHotSeatPosition(int oldposition) {
        int count = 0;
        int newposition = 0;
        int maxHotseatCount = ConfigManager.getHotseatMaxCount();
        ItemInfo[] infos = new ItemInfo[maxHotseatCount];
        ArrayList<ItemInfo> allItems = getAllAppItems();
        for (ItemInfo info: allItems) {
            if (info.container == LauncherSettings.Favorites.CONTAINER_HOTSEAT) {
                infos[info.screen] = info;
                count++;
                if (count >= maxHotseatCount) {
                    break;
                }
            }
        }

        if (count >= ConfigManager.getHotseatMaxCount()) {
            newposition = count;
        } else {
            if (oldposition <= count) {
                if (infos[oldposition] == null) {
                    newposition = oldposition;
                } else {
                    newposition = count;
                }
            } else {
                newposition = count;
            }
        }
        return newposition;
    }

    public static void rebindHotseat(Context context) {
        //get all hotseat items
        final ArrayList<ItemInfo> infos = new ArrayList<ItemInfo>();
        ArrayList<ItemInfo> allApps = getAllAppItems();
        for (ItemInfo info: allApps) {
            if ((info != null) &&
                 (info.container == LauncherSettings.Favorites.CONTAINER_HOTSEAT)) {
                 infos.add(info);
            }
        }

        Collections.sort(infos, new Comparator<ItemInfo>() {
            @Override
            public int compare(ItemInfo lhs, ItemInfo rhs) {
                int cellCountX = LauncherModel.getCellCountX();
                int cellCountY = LauncherModel.getCellCountY();
                int screenOffset = cellCountX * cellCountY;
                int containerOffset = screenOffset * (mMaxScreenCount + 1); // +1 hotseat
                long lr = (lhs.container * containerOffset + lhs.screen * screenOffset +
                        lhs.cellY * cellCountX + lhs.cellX);
                long rr = (rhs.container * containerOffset + rhs.screen * screenOffset +
                        rhs.cellY * cellCountX + rhs.cellX);
                return (int) (lr - rr);
            }
        });

        final Callbacks callbacks = mCallbacks != null ? mCallbacks.get() : null;
        final Runnable r = new Runnable() {
            @Override
            public void run() {
                if (callbacks != null) {
                    callbacks.bindRebuildHotseat(infos);
                }
            }
        };
        mHandler.post(r);
    }

    public static ScreenPosition getHideSeatPosition(int screen, int x, int y) {
        Log.d(TAG, "getHideSeatPosition in screen: " + screen + " x: " + x + " y: " + y);
        ScreenPosition p = null;

        final int mMaxHideSeatScrCount = ConfigManager.getHideseatScreenMaxCount();
        final int mMaxHideSeatXCount = ConfigManager.getHideseatMaxCountX();
        final int mMaxHideSeatYCount = ConfigManager.getHideseatMaxCountY();

        int xCount = mMaxHideSeatXCount;
        int yCount = mMaxHideSeatYCount;
        int scrnCount = mMaxHideSeatScrCount;
        int maxHideSeatCount = scrnCount * xCount * yCount;

        if ((x < xCount) && (y < yCount) && (screen < scrnCount)) {
            boolean[][][] occupied = new boolean[scrnCount][xCount][yCount];
            initHideSeatOccupied(occupied);
            if (occupied[screen][x][y]) {
                Log.d(TAG, "the original position is occupied");
                for (int i = 0; i < mMaxHideSeatScrCount; i++) {
                    for (int j = 0; j < mMaxHideSeatYCount; j++) {
                        for (int k = 0; k < mMaxHideSeatXCount; k++) {
                            if (occupied[i][k][j] == false) {
                                p = new ScreenPosition(i, k, j);
                                break;
                            }
                        }
                        if (p != null) {
                            break;
                        }
                    }
                    if (p != null) {
                        break;
                    }
                }
            } else {
                p = new ScreenPosition(screen, x, y);
            }
        }
        return p;
    }

    public static void initHideSeatOccupied(boolean[][][] occupied) {
        final int mMaxHideSeatScrCount = ConfigManager.getHideseatScreenMaxCount();
        final int mMaxHideSeatXCount = ConfigManager.getHideseatMaxCountX();
        final int mMaxHideSeatYCount = ConfigManager.getHideseatMaxCountY();
        ArrayList<ItemInfo> allApps = getAllAppItems();
        for (ItemInfo hideseatinfo: allApps) {
            if (hideseatinfo.container == LauncherSettings.Favorites.CONTAINER_HIDESEAT) {
                if ((hideseatinfo.screen < mMaxHideSeatScrCount) &&
                    (hideseatinfo.cellX < mMaxHideSeatXCount) &&
                    (hideseatinfo.cellY < mMaxHideSeatYCount)) {
                    occupied[hideseatinfo.screen][hideseatinfo.cellX][hideseatinfo.cellY] = true;
                }
            }
        }
    }

    private static boolean isScreenEmpty(int screen) {
        boolean isempty = true;
        final ArrayList<ItemInfo> tmpWorkspaceItems = new ArrayList<ItemInfo>();
        final ArrayList<ItemInfo> tmpAppWidgets = new ArrayList<ItemInfo>();
        synchronized (sBgLock) {
            tmpWorkspaceItems.addAll(sBgWorkspaceItems );
            tmpAppWidgets.addAll( sBgAppWidgets );
        }
        for (ItemInfo item: tmpWorkspaceItems) {
            if ((item.screen == screen) &&
                (item.container == LauncherSettings.Favorites.CONTAINER_DESKTOP)) {
                isempty = false;
                break;
            }
        }
        if (isempty == true) {
            for (ItemInfo item: tmpAppWidgets) {
                if (item.screen == screen) {
                    isempty = false;
                    break;
                }
            }
        }
        return isempty;
    }

    public static void checkEmptyScreen(Context context, int screen) {
        if ((screen <0) || (screen >= mMaxScreenCount)) {
            return;
        }
        final int srn = screen;
        final Context finalContext = context;
        final ContentResolver cr = context.getContentResolver();
        Runnable r = new Runnable() {
            public void run() {
                Log.d(TAG, "check empty screen start");
                boolean isempty = true;
                final ArrayList<ItemInfo> tmpWorkspaceItems = new ArrayList<ItemInfo>();
                final ArrayList<ItemInfo> tmpAppWidgets = new ArrayList<ItemInfo>();
                synchronized (sBgLock) {
                    tmpWorkspaceItems.addAll(sBgWorkspaceItems);
                    tmpAppWidgets.addAll(sBgAppWidgets);
                }
                for (ItemInfo item: tmpWorkspaceItems) {
                    if ((item.screen == srn) &&
                        (item.container == LauncherSettings.Favorites.CONTAINER_DESKTOP)) {
                        Log.d(TAG, "item screen is " + item.screen);
                        Log.d(TAG, "item id is " + item.id);
                        isempty = false;
                        break;
                    }
                }
                if (isempty == true) {
                    for (ItemInfo item: tmpAppWidgets) {
                        if (item.screen == srn) {
                            isempty = false;
                            break;
                        }
                    }
                }
                //update lists screen number first
                if (isempty == true) {
                    for (ItemInfo item: tmpWorkspaceItems) {
                        if ((item.screen > srn) &&
                            (item.container == LauncherSettings.Favorites.CONTAINER_DESKTOP)) {
                                item.screen -= 1;
                        }
                    }
                    for (ItemInfo item: tmpAppWidgets) {
                        if (item.screen > srn) {
                            item.screen -= 1;
                        }
                    }
                }
                if (isempty == true) {
                    Log.d(TAG, "the screen is empty");
                    forScreenRemoveUpdate(cr, srn);
                    checkNoSpaceList();
                }
                Log.d(TAG, "check empty screen finish");
            }
        };
        runOnWorkerThread(r);
    }

    static void forScreenRemoveUpdate(ContentResolver cr, int screenid) {
        Log.d(TAG, "forScreenRemoveUpdate in");
        Cursor c = cr.query(LauncherSettings.Favorites.CONTENT_URI,
                new String[] { "_id", "screen"}, "screen>? and container=?",
                new String[] { String.valueOf(screenid), String.valueOf(LauncherSettings.Favorites.CONTAINER_DESKTOP)},
                null);
        if (c == null) {
            return;
        }
        int screenIndex = c.getColumnIndexOrThrow(
                LauncherSettings.Favorites.SCREEN);
        int idIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites._ID);

        try {
            c.moveToFirst();
            Log.d(TAG, "c count is " + c.getCount());
            if (c.getCount() > 0) {
                ArrayList<updateArgs> updatelist = new ArrayList<updateArgs>();
                for (int i = 0; i < c.getCount(); i++) {
                    ContentValues values = new ContentValues();
                    values.put("screen", c.getInt(screenIndex) - 1);
                    int itemId = c.getInt(idIndex);
                    Uri uri = LauncherSettings.Favorites.getContentUri(itemId, false);
                    updateArgs args = new updateArgs(uri, values, null, null);
                    updatelist.add(args);
                    //cr.update(uri, values, null, null);
                    c.moveToNext();
                }
                LauncherApplication app = (LauncherApplication) LauncherApplication.getContext().getApplicationContext();
                if ((app != null) && (app.getLauncherProvider() != null)) {
                    Log.d(TAG, "before call bulkupdate");
                    app.getLauncherProvider().bulkUpdate(updatelist);
                }
                updatelist.clear();
            }

        } finally {
            c.close();
        }
        final int screen = screenid;
        final Callbacks callbacks = mCallbacks != null ? mCallbacks.get() : null;
        final Runnable r = new Runnable() {
            @Override
            public void run() {
                if (callbacks != null) {
                    callbacks.bindRemoveScreen(screen);
                }
            }
        };
        runOnMainThread(r);
        Log.d(TAG, "forScreenRemoveUpdate out");
    }

    /**
     * modify item's "isNew" in db
     */
    public static void modifyItemNewStatusInDatabase(Context context,
            final ItemInfo item, boolean isNew) {
        Log.d(TAG, "modify item in db");
        String transaction = "DbDebug    Modify item new status (" + item.title + ") in db, id: " + item.id +
                " (" + item.isNewItem()+ ") --> " + "(" + isNew+ ")";
        //Launcher.sDumpLogs.add(transaction);
        Log.d(TAG, transaction);
        item.setIsNewItem(isNew);

        final ContentValues values = new ContentValues();
        values.put(LauncherSettings.BaseLauncherColumns.IS_NEW, item.isNewItem()?1:0);

        updateItemInDatabaseHelper(context, values, item, "modifyItemNewStatusInDatabase");
    }

    /**
     * This method is used to update itemInfo in database and itemList, and may update view
     * on workspace
     *
     * @param context
     * @param itemId: use it to get itemInfo from sBgItemsIdMap
     * @param values: contains the values which are changed
     * @param updateUI: true, after updating itemInfo in database and ItemList, will update
     *                  corresponding view on workspace
     */
    public static void updateItemById(Context context, long itemId, ContentValues values, boolean updateUI) {
        updateItemById(context, itemId, values, updateUI, false);
    }

    public static void updateItemById(Context context, long itemId, ContentValues values, boolean updateUI, boolean postIdle) {
        Log.d(TAG, "updateItemById: itemId="+itemId+",values="+values);
        if(values == null) {
            Log.e(TAG, "updateItemById: values = null");
            return;
        }

        ItemInfo modelItem = null;
        synchronized (sBgLock) {
            modelItem = sBgItemsIdMap.get(itemId);
            if (modelItem == null) {
                Log.e(TAG, "updateItemById: fail to find item which id is "+itemId);
                return;
            }

            if (values.containsKey(LauncherSettings.Favorites.MESSAGE_NUM)) {
                Long numObj = values.getAsLong(LauncherSettings.Favorites.MESSAGE_NUM);
                if (numObj!=null) {
                    Log.d(TAG, "old number = "+ modelItem.messageNum+ ", new number = "+numObj.intValue());
                    modelItem.messageNum = numObj.intValue();
                }
            }
        }

        //Update database
        updateItemInDatabaseHelper(context, values, modelItem, "updateItemById");

        if (updateUI) {
            Log.d(TAG, "need to update corresponding view on workspace");
            final ItemInfo finalitem = modelItem;
            final Callbacks callbacks = mCallbacks != null ? mCallbacks.get() : null;
            Runnable r = new Runnable() {
                @Override
                public void run() {
                    ArrayList<ItemInfo> items = new ArrayList<ItemInfo>(1);
                    items.add(finalitem);
                    if (callbacks != null) {
                        callbacks.bindItemsUpdated(items);
                    }
                }
            };
            if (postIdle) {
                postRunnableIdle(r);
            } else {
                mHandler.post(r);
            }
          //runOnMainThread(r);
        }
    }

    public static void postRunnableIdle(Runnable r) {
        mHandler.postIdle(r);
    }

    /**
     * Returns a list of all the widgets that can handle configuration with a particular mimeType.
     */
    List<WidgetMimeTypeHandlerData> resolveWidgetsForMimeType(Context context, String mimeType) {
        final PackageManager packageManager = context.getPackageManager();
        final List<WidgetMimeTypeHandlerData> supportedConfigurationActivities =
            new ArrayList<WidgetMimeTypeHandlerData>();

        final Intent supportsIntent =
            new Intent(InstallWidgetReceiver.ACTION_SUPPORTS_CLIPDATA_MIMETYPE);
        supportsIntent.setType(mimeType);

        // Create a set of widget configuration components that we can test against
        final List<AppWidgetProviderInfo> widgets =
            AppWidgetManager.getInstance(context).getInstalledProviders();
        final HashMap<ComponentName, AppWidgetProviderInfo> configurationComponentToWidget =
            new HashMap<ComponentName, AppWidgetProviderInfo>();
        for (AppWidgetProviderInfo info : widgets) {
            configurationComponentToWidget.put(info.configure, info);
        }

        // Run through each of the intents that can handle this type of clip data, and cross
        // reference them with the components that are actual configuration components
        final List<ResolveInfo> activities = packageManager.queryIntentActivities(supportsIntent,
                PackageManager.MATCH_DEFAULT_ONLY);
        for (ResolveInfo info : activities) {
            final ActivityInfo activityInfo = info.activityInfo;
            final ComponentName infoComponent = new ComponentName(activityInfo.packageName,
                    activityInfo.name);
            if (configurationComponentToWidget.containsKey(infoComponent)) {
                supportedConfigurationActivities.add(
                        new InstallWidgetReceiver.WidgetMimeTypeHandlerData(info,
                                configurationComponentToWidget.get(infoComponent)));
            }
        }
        return supportedConfigurationActivities;
    }

    public ShortcutInfo infoFromShortcutIntent(Context context, Intent data,
            Drawable fallbackIcon) {
        Intent intent = data.getParcelableExtra(Intent.EXTRA_SHORTCUT_INTENT);
        String name = data.getStringExtra(Intent.EXTRA_SHORTCUT_NAME);

        Parcelable bitmap = data.getParcelableExtra(Intent.EXTRA_SHORTCUT_ICON);

        if (intent == null) {
            // If the intent is null, we can't construct a valid ShortcutInfo, so we return null
            Log.e(TAG, "Can't construct ShorcutInfo with null intent");
            return null;
        }

        Drawable icon = null;
        boolean customIcon = false;
        ShortcutIconResource iconResource = null;

        if (bitmap != null && bitmap instanceof Bitmap) {
            icon = Utilities.createIconDrawable(new FastBitmapDrawable((Bitmap)bitmap), context);
            customIcon = true;
        } else {
            Parcelable extra = data.getParcelableExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE);
            if (extra != null && extra instanceof ShortcutIconResource) {
                try {
                    iconResource = (ShortcutIconResource) extra;
                    final PackageManager packageManager = context.getPackageManager();
                    Resources resources = packageManager.getResourcesForApplication(
                            iconResource.packageName);
                    final int id = resources.getIdentifier(iconResource.resourceName, null, null);
                    icon = Utilities.createIconDrawable(
                            mIconManager.getFullResIcon(resources, id), context);
                } catch (Exception e) {
                    Log.w(TAG, "Could not load shortcut icon: " + extra);
                }
            }
        }

        final ShortcutInfo info = new ShortcutInfo();

        if (icon == null) {
            if (fallbackIcon != null) {
                icon = fallbackIcon;
            } else {
                icon = getFallbackIcon();
                info.usingFallbackIcon = true;
            }
        }
        
        info.setIcon(icon);

        info.title = name;
        info.intent = intent;
        info.customIcon = customIcon;
        info.iconResource = iconResource;
        return info;
    }

    /**
     * Return an existing FolderInfo object if we have encountered this ID previously,
     * or make a new one.
     */
    private static FolderInfo findOrMakeFolder(HashMap<Long, FolderInfo> folders, long id) {
        // See if a placeholder was created for us already
        FolderInfo folderInfo = folders.get(id);
        if (folderInfo == null) {
            // No placeholder -- create a new instance
            folderInfo = new FolderInfo();
            folders.put(id, folderInfo);
        }
        return folderInfo;
    }
    //topwise zyf add for fixedfolder
    private static FolderInfo findOrMakeFixedFolder(HashMap<Long, FolderInfo> folders, long id) {
        // See if a placeholder was created for us already;
        FolderInfo folderInfo = folders.get(id);
        if (folderInfo == null) {
            // No placeholder -- create a new instance
            folderInfo = new FixedFolderInfo();
            folders.put(id, folderInfo);
        }
        return folderInfo;
    }
    //topwise zyf add end

    public static final Comparator<ApplicationInfo> getAppNameComparator() {
        final Collator collator = Collator.getInstance();
        return new Comparator<ApplicationInfo>() {
            public final int compare(ApplicationInfo a, ApplicationInfo b) {
                int result = collator.compare(a.title.toString(), b.title.toString());
                if (result == 0) {
                    result = a.componentName.compareTo(b.componentName);
                }
                return result;
            }
        };
    }
    public static final Comparator<ApplicationInfo> APP_INSTALL_TIME_COMPARATOR
            = new Comparator<ApplicationInfo>() {
        public final int compare(ApplicationInfo a, ApplicationInfo b) {
            if (a.firstInstallTime < b.firstInstallTime) return 1;
            if (a.firstInstallTime > b.firstInstallTime) return -1;
            return 0;
        }
    };
    public static final Comparator<AppWidgetProviderInfo> getWidgetNameComparator() {
        final Collator collator = Collator.getInstance();
        return new Comparator<AppWidgetProviderInfo>() {
            public final int compare(AppWidgetProviderInfo a, AppWidgetProviderInfo b) {
                return collator.compare(a.label.toString(), b.label.toString());
            }
        };
    }

    public static ComponentName getComponentNameFromResolveInfo(ResolveInfo info) {
        if (info.activityInfo != null) {
            return new ComponentName(info.activityInfo.packageName, info.activityInfo.name);
        } else {
            return new ComponentName(info.serviceInfo.packageName, info.serviceInfo.name);
        }
    }
    public static class ShortcutNameComparator implements Comparator<ResolveInfo> {
        private Collator mCollator;
        private PackageManager mPackageManager;
        private HashMap<Object, CharSequence> mLabelCache;
        ShortcutNameComparator(PackageManager pm) {
            mPackageManager = pm;
            mLabelCache = new HashMap<Object, CharSequence>();
            mCollator = Collator.getInstance();
        }
        ShortcutNameComparator(PackageManager pm, HashMap<Object, CharSequence> labelCache) {
            mPackageManager = pm;
            mLabelCache = labelCache;
            mCollator = Collator.getInstance();
        }
        public final int compare(ResolveInfo a, ResolveInfo b) {
            CharSequence labelA, labelB;
            ComponentName keyA = LauncherModel.getComponentNameFromResolveInfo(a);
            ComponentName keyB = LauncherModel.getComponentNameFromResolveInfo(b);
            if (mLabelCache.containsKey(keyA)) {
                labelA = mLabelCache.get(keyA);
            } else {
                labelA = a.loadLabel(mPackageManager).toString();

                mLabelCache.put(keyA, labelA);
            }
            if (mLabelCache.containsKey(keyB)) {
                labelB = mLabelCache.get(keyB);
            } else {
                labelB = b.loadLabel(mPackageManager).toString();

                mLabelCache.put(keyB, labelB);
            }
            return mCollator.compare(labelA, labelB);
        }
    };
    public static class WidgetAndShortcutNameComparator implements Comparator<Object> {
        private Collator mCollator;
        private PackageManager mPackageManager;
        private HashMap<Object, String> mLabelCache;
        WidgetAndShortcutNameComparator(PackageManager pm) {
            mPackageManager = pm;
            mLabelCache = new HashMap<Object, String>();
            mCollator = Collator.getInstance();
        }
        public final int compare(Object a, Object b) {
            String labelA, labelB;
            if (mLabelCache.containsKey(a)) {
                labelA = mLabelCache.get(a);
            } else {
                labelA = (a instanceof AppWidgetProviderInfo) ?
                    ((AppWidgetProviderInfo) a).label :
                    ((ResolveInfo) a).loadLabel(mPackageManager).toString();
                mLabelCache.put(a, labelA);
            }
            if (mLabelCache.containsKey(b)) {
                labelB = mLabelCache.get(b);
            } else {
                labelB = (b instanceof AppWidgetProviderInfo) ?
                    ((AppWidgetProviderInfo) b).label :
                    ((ResolveInfo) b).loadLabel(mPackageManager).toString();
                mLabelCache.put(b, labelB);
            }
            return mCollator.compare(labelA, labelB);
        }
    };

    public void dumpState() {
        Log.d(TAG, "mCallbacks=" + mCallbacks);
        ApplicationInfo.dumpApplicationInfoList(TAG, "mAllAppsList.data", mBgAllAppsList.data);
        ApplicationInfo.dumpApplicationInfoList(TAG, "mAllAppsList.added", mBgAllAppsList.added);
        ApplicationInfo.dumpApplicationInfoList(TAG, "mAllAppsList.removed", mBgAllAppsList.removed);
        ApplicationInfo.dumpApplicationInfoList(TAG, "mAllAppsList.modified", mBgAllAppsList.modified);
        if (mLoaderTask != null) {
            mLoaderTask.dumpState();
        } else {
            Log.d(TAG, "mLoaderTask=null");
        }
    }

    /**
     * Retrieves groups of <code>ShortcutInfo</code> which are corresponding to given package names.
     * <p><strong>NOTE THAT</strong> this method only returns items whose type are
     * <code>ITEM_TYPE_APPLICATION</code> or <code>ITEM_TYPE_NOSPACE_APPLICATION</code>.
     * @param pkgNames a group of package names
     * @return a map from package name to collection of <code>ShortcutInfo</code>
     */
    public static Map<String, Collection<ShortcutInfo>> getAllShortcutInfoByPackageNames(Collection<String> pkgNames) {
        if (pkgNames == null || pkgNames.isEmpty()) return Collections.emptyMap();

        List<ItemInfo> allitems = null;
        synchronized (sBgLock) {
            allitems = new ArrayList<ItemInfo>(sBgItemsIdMap.values());
            allitems.addAll(sBgNoSpaceItems);
        }

        Map<String, Collection<ShortcutInfo>> rst = new HashMap<String, Collection<ShortcutInfo>>(pkgNames.size());
        for (String pkgName : pkgNames) {
            rst.put(pkgName, new HashSet<ShortcutInfo>(1));
        }
        Set<String> pkgSet = rst.keySet();

        for (ItemInfo item : allitems) {
            if (item instanceof ShortcutInfo) {
                if (item.itemType != Favorites.ITEM_TYPE_APPLICATION &&
                    item.itemType != Favorites.ITEM_TYPE_NOSPACE_APPLICATION) {
                    continue;
                }
                Intent intent = ((ShortcutInfo) item).intent;
                if (intent == null || intent.getComponent() == null) continue;
                String pkgName = intent.getComponent().getPackageName();
                if (pkgSet.contains(pkgName)) {
                    rst.get(pkgName).add((ShortcutInfo) item);
                }
            }
        }
        return rst;
    }

    public void notifyUIAddIcon(final ArrayList<ItemInfo> apps){
        Log.d(TAG,"LauncherMode : notifyUIAddIcon begin");
        final Callbacks callbacks = mCallbacks != null ? mCallbacks.get() : null;

        if (callbacks != null) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (callbacks == mCallbacks.get()) {
                        callbacks.bindItemsAdded(apps);
                    }
                }
            });
        }
        Log.d(TAG,"LauncherMode : notifyUIAddIcon end");
    }

    public void notifyUIUpdateIcon(final ArrayList<ItemInfo> apps) {
        notifyUIUpdateIcon(apps,false);
    }

    private void notifyUIUpdateIcon(final ArrayList<ItemInfo> apps,final boolean isThemeChange){
        Log.d(TAG,"LauncherMode : notifyUIUpdateIcon begin");
        final Callbacks oldCallbacks = mCallbacks != null ? mCallbacks.get() : null;
        int N = apps.size();
        int updateItemsChunk = 50;
        if (oldCallbacks == null) {
            return;
        }
        for (int i = 0; i < N; i += updateItemsChunk) {
            final int start = i;
            final int chunkSize = (i+updateItemsChunk <= N) ? updateItemsChunk : (N-i);
            final Runnable r = new Runnable() {
                @Override
                public void run() {
                    Callbacks callbacks = tryGetCallbacks(oldCallbacks);
                    if (callbacks != null) {
                        callbacks.bindItemsChunkUpdated(apps, start, start+chunkSize, isThemeChange);
                    }
                }
            };
            if(isThemeChange) {
                runOnMainThread(r, MAIN_THREAD_THEME_CHANGE_RUNNABLE);
            } else {
                ShortcutInfo info = (ShortcutInfo) apps.get(0);
                if (info != null
                        && info.getAppDownloadStatus() != AppDownloadStatus.STATUS_NO_DOWNLOAD) {
                    mHandler.postIdle(r, MAIN_THREAD_BINDING_RUNNABLE);
                } else {
                    runOnMainThread(r, MAIN_THREAD_BINDING_RUNNABLE);
                }
            }
        }
        Log.d(TAG,"LauncherMode : notifyUIUpdateIcon end");
    }

    public void notifyUIUpdateDownloadIcon(ShortcutInfo item){
        Log.d(TAG,"LauncherMode : notifyUIUpdateDownloadIcon begin");
        final Callbacks oldCallbacks = mCallbacks != null ? mCallbacks.get() : null;
        if (oldCallbacks == null) {
            return;
        }
        final ArrayList<ItemInfo> infos = new ArrayList<ItemInfo>(1);
        infos.add(item);
        final IconManager iconManager = mIconManager;
        final ShortcutInfo finalitem = item;
        final Runnable r = new Runnable() {
            @Override
            public void run() {
                Intent intent = finalitem.intent;
                if (intent != null) {
                    iconManager.clearCardBackgroud(finalitem.intent);
                }
                Callbacks callbacks = tryGetCallbacks(oldCallbacks);
                if (callbacks != null) {
                    callbacks.bindItemsUpdated(infos);
                }
            }
        };
        runOnMainThread(r, MAIN_THREAD_BINDING_RUNNABLE);
        Log.d(TAG,"LauncherMode : notifyUIUpdateDownloadIcon end");
    }

    public void notifyUIRemoveIcon(final ArrayList<ItemInfo> apps, final boolean permanent,
            final boolean isFromAppInstall){
        Log.d(TAG,"LauncherMode : notifyUIRemoveIcon begin");
        final Callbacks callbacks = mCallbacks != null ? mCallbacks.get() : null;

        mHandler.post(new Runnable(){
            @Override
            public void run() {
                if(callbacks != null && callbacks == mCallbacks.get()) {
                    callbacks.bindDownloadItemsRemoved(apps, permanent);
                }
            }
        });
        Log.d(TAG,"LauncherMode : notifyUIRemoveIcon end");
    }

    public boolean isDownloadStatus(){
        if(mAppDownloadMgr!=null&&mAppDownloadMgr.isDownloadStatus()){
            return true;
        }
        return false;
    }

    public boolean isEmptyCellCanBeRemoved(){
        if (BackupManager.getInstance().isInRestore()){
            return false;
        }
        return true;
    }

    public static boolean checkGridSize(int countX, int countY) {
        int maxCountX = LauncherModel.getCellMaxCountX();
        if (countX > maxCountX) {
            return false;
        }

        int maxCountY = LauncherModel.getCellMaxCountY();
        if (countY > maxCountY) {
            return false;
        }

        return true;
    }

    public void onFontChanged() {
        bindItemOnThemeChange(getSbgWorkspaceItems());
    }
    
    public void onThemeChange() {
        post(new Runnable() {
            @Override
            public void run() {
                mHandler.cancelAllRunnablesOfType(MAIN_THREAD_THEME_CHANGE_RUNNABLE);
                mIconManager.notifyThemeChanged();
                Launcher.sReloadingForThemeChangeg = true;
                mApp.sendBroadcast(new Intent(HomeShellSetting.THEME_CHANGED_ACTION));

                boolean isCountYSupport = true;
                if (mCellCountY == ConfigManager.getCellMaxCountY()) {
                    isCountYSupport = checkGridSize(mCellCountX, mCellCountY);
                }

                if (isCountYSupport == true) {
                    reloadWorkspace();
                } else {
                    changeLayout(mCellCountX, mCellCountY - 1);
                    reloadWorkspace();
                    if (homeshellSetting != null) {
                        homeshellSetting.updateLayoutPreference();
                    }
                }
                final ArrayList<Object> widgetsAndShortcuts = getSortedWidgetsAndShortcuts(mApp);
                final Callbacks callbacks = mCallbacks != null ? mCallbacks.get() : null;
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        Callbacks cb = mCallbacks != null ? mCallbacks.get() : null;
                        if (callbacks == cb && cb != null) {
                            callbacks.bindPackagesUpdated(widgetsAndShortcuts);
                        }
                    }
                });
            }
        });
    }

    private void reloadWorkspace(){
        loadItemOnThemeChange(getAllAppItems());
        FolderIcon.onThemeChanged();
        bindItemOnThemeChange(getSbgWorkspaceItems());
    }

    private void loadItemOnThemeChange(List<ItemInfo> list) {
        //FancyIconsHelper.clearCache();
        //ThemeResources.refresh();
        Context context = mApp;
        //mIconManager.notifyThemeChanged();
        boolean hasBanner = false;
        boolean isUsbMode = Utils.isInUsbMode();
        HashMap<Long, Bitmap> iconsFromDB = getPrimaryIconFromDB(context);

        //Map<String, GadgetInfo> gadgets = ThemeUtils.listGadgets(context);
        for (ItemInfo info : list) {
            if (info instanceof ShortcutInfo) {
                Bitmap iconDB = iconsFromDB.get(info.id);
                Drawable icon = mIconManager.getAppUnifiedIcon(info,null);
                if(mIconManager.isDefaultIcon(icon) && iconDB!=null){
                    icon = new FastBitmapDrawable(iconDB);
                    mIconManager.addAppIconToCache(((ShortcutInfo) info).intent, icon);
                }
                ((ShortcutInfo) info).setIcon(icon);
                //create card bg in worker thread
                //to avoid card create in UI thread
                if (mIconManager.supprtCardIcon() == true) {
                    mIconManager.getAppCardBackgroud(info);
                }
            } else if (info instanceof FolderInfo) {
            	//topwise zyf add for exapp
            	boolean isFixedFolder=((FolderInfo)info).itemType==LauncherSettings.Favorites.ITEM_TYPE_FIXED_FOLDER;
            	if(isFixedFolder)
            	{
            		reloadExappIcon(context,(FolderInfo)info,iconsFromDB);
            	}
            	else
            	{
            	//topwise zyf add end
                for (ShortcutInfo si : ((FolderInfo)info).contents) {
                     if(si.isEditFolderShortcut()) {
                         continue;
                     }
                     Bitmap iconDB = iconsFromDB.get(si.id);
                     Drawable icon = mIconManager.getAppUnifiedIcon(si,null);
                     if(mIconManager.isDefaultIcon(icon) && iconDB!=null){
                        icon = new FastBitmapDrawable(iconDB);
                     }
                     si.setIcon(icon);
                }
                //topwise zyf add for exapp
              	 }
              	//topwise zyf add end
            } /*else if (info instanceof GadgetItemInfo) {
                // gadget support theme changed
                GadgetItemInfo gadgetItemInfo = (GadgetItemInfo) info;
                GadgetInfo gadgetInfo = gadgets
                        .get(gadgetItemInfo.gadgetInfo.label);
                gadgetItemInfo.gadgetInfo = gadgetInfo;
            }*/
        }

        Log.d(TAG, "loadItemOnThemeChange hasBanner : " + hasBanner + " isUsbMode : " + isUsbMode);
//        if (!hasBanner) {
//            AliAppWidgetInfo info = AliAppWidgetInfo.getInfo("banner");
//            info.screen = 0;
//            info.cellX = 0;
//            info.cellY = 0;
//            info.spanX = 4;
//            info.spanY = 1;
//            if (isCellEmtpy(info.screen, info.cellX, info.cellY, info.spanX, info.spanY) != null) {
//                addItemToDatabase(mApp, info, info.container,
//                        info.screen, info.cellX, info.cellY, false);
//                ArrayList<ItemInfo> banner = new ArrayList<ItemInfo>();
//                banner.add(info);
//                notifyUIAddIcon(banner);
//            }
//        }
    }

    /**
     * Find the item's id and icon in db that ICON_TYPE is ICON_TYPE_BITMAP
     * @param context
     * @return hashmap\uff0ckey is id\uff0cvalue is icon(Bitmap)
     */
    private HashMap<Long, Bitmap> getPrimaryIconFromDB(Context context) {
        HashMap<Long, Bitmap> idIconMap = new HashMap<Long, Bitmap>();
        String key = LauncherSettings.Favorites.ICON_TYPE;
        ContentResolver cr = context.getContentResolver();
        Cursor cursor = cr.query(Favorites.CONTENT_URI, 
                new String[] {Favorites._ID, Favorites.ICON,Favorites.TITLE }, 
                key + "=?",
                new String[] { String.valueOf(Favorites.ICON_TYPE_BITMAP) },
                null);

        if (cursor != null) {
            int idIndex = cursor.getColumnIndexOrThrow(LauncherSettings.Favorites._ID);
            int iconIndex = cursor.getColumnIndexOrThrow(LauncherSettings.Favorites.ICON);
    
            while (cursor.moveToNext()) {
                Long id = cursor.getLong(idIndex);
                IconCursorInfo cursorInfo = new IconCursorInfo(cursor,iconIndex);
                Drawable cursorIcon = (FastBitmapDrawable)mApp.getIconManager().getIconFromCursor(cursorInfo);
                if (cursorIcon == null) {
                    cursorIcon = (FastBitmapDrawable)mApp.getIconManager().getDefaultIcon();
                }
                if (cursorIcon != null) {
                    cursorIcon = ((LauncherApplication)context.getApplicationContext()).getIconManager().buildUnifiedIcon(cursorIcon, ThemeUtils.ICON_TYPE_APP_TEMPORARY);
                }
                if (cursorIcon != null) {
                    Bitmap bitmap = ((FastBitmapDrawable) cursorIcon).getBitmap();
                    idIconMap.put(id, bitmap);
                }
            }
            cursor.close();
        }

        return idIconMap;
    }

    private void bindItemOnThemeChange(ArrayList<ItemInfo> list) {
        notifyUIUpdateIcon(list,true);
    }

    public static void reArrageScreen(Context context, final List<Integer> newIndexs) {
        // TODO Auto-generated method stub
        Log.d(TAG, "forScreenRemoveUpdate in");
        final Map<Integer, Integer> screenExchangeMap = getScreenExchangeMap(
                getOldScreenIndexs(), newIndexs);
        if (screenExchangeMap == null) {
            return;
        }

        Map<ItemInfo, ItemInfo> map = new HashMap<ItemInfo, ItemInfo>();
        final ArrayList<ItemInfo> tmpWorkspaceItems = new ArrayList<ItemInfo>();
        final ArrayList<ItemInfo> tmpAppWidgets = new ArrayList<ItemInfo>();
        synchronized (sBgLock) {
            tmpWorkspaceItems.addAll(sBgWorkspaceItems);
            tmpAppWidgets.addAll(sBgAppWidgets);
        }
        for (int i = 0; i < tmpWorkspaceItems.size(); i++) {
            ItemInfo item = tmpWorkspaceItems.get(i);
            if (item.container != LauncherSettings.Favorites.CONTAINER_DESKTOP
                    || map.containsKey(item)) {
                continue;
            }
            map.put(item, item);
            item.screen = screenExchangeMap.get(item.screen);
        }

        for (int i = 0; i < tmpAppWidgets.size(); i++) {
            ItemInfo item = tmpAppWidgets.get(i);
            if (item.container != LauncherSettings.Favorites.CONTAINER_DESKTOP
                    || map.containsKey(item)) {
                continue;
            }
            map.put(item, item);
            item.screen = screenExchangeMap.get(item.screen);
        }
        map.clear();

        final ContentResolver cr = context.getContentResolver();

        runOnWorkerThread(new Runnable() {
            @Override
            public void run() {
                Cursor c = cr
                        .query(LauncherSettings.Favorites.CONTENT_URI,
                        new String[] { "_id", "screen" },
                        "container=?",
                        new String[] {
                                String.valueOf(LauncherSettings.Favorites.CONTAINER_DESKTOP) },
                        null);
                if (c == null) {
                    return;
                }

                int idIndex = c
                        .getColumnIndexOrThrow(LauncherSettings.Favorites._ID);
                int screenIndex = c
                        .getColumnIndexOrThrow(LauncherSettings.Favorites.SCREEN);

                try {
                    c.moveToFirst();
                    if (c.getCount() > 0) {
                        ArrayList<updateArgs> updatelist = new ArrayList<updateArgs>();
                        for (int i = 0; i < c.getCount(); i++) {
                            int oldScreen = c.getInt(screenIndex);
                            if (oldScreen < 0) {
                                continue;
                            }

                            int newScreen = screenExchangeMap.get(oldScreen);
                            if (oldScreen == newScreen) {
                                c.moveToNext();
                                continue;
                            }
                            ContentValues values = new ContentValues();
                            values.put("screen", newScreen);
                            int itemId = c.getInt(idIndex);
                            Uri uri = LauncherSettings.Favorites.getContentUri(
                                    itemId, false);
                            //cr.update(uri, values, null, null);
                            updateArgs args = new updateArgs(uri, values, null, null);
                            updatelist.add(args);
                            c.moveToNext();
                        }
                        if (updatelist.size() > 0) {
                            LauncherApplication app = (LauncherApplication) LauncherApplication.getContext().getApplicationContext();
                            if ((app != null) && (app.getLauncherProvider() != null)) {
                                Log.d(TAG, "before call bulkupdate");
                                app.getLauncherProvider().bulkUpdate(updatelist);
                            }
                            updatelist.clear();
                        }
                    }

                } finally {
                    c.close();
                }
            }
        });
    }

    private static Map<Integer, Integer> getScreenExchangeMap(
            List<Integer> oldIndexs,
            List<Integer> newIndexs) {
        Log.d(TAG, "getScreenExchangeMap" + oldIndexs + "," + newIndexs);
        if (newIndexs == null || oldIndexs == null
                || oldIndexs.size() != newIndexs.size()) {
            return null;
        }

        Map<Integer, Integer> map = new HashMap<Integer, Integer>();
        int count = newIndexs.size();
        int[] l2pMap = new int[count];
        for (int i = 0; i < count; i++) {
            int j = newIndexs.get(i);
            l2pMap[j] = i;
        }

        for (int i = 0; i < count; i++) {
            map.put(oldIndexs.get(i), l2pMap[i]);
        }

        Log.d(TAG, "getScreenExchangeMap, result map" + map);
        return map;
    }

    private static List<Integer> getOldScreenIndexs() {
        List<Integer> screens = new ArrayList<Integer>();
        final ArrayList<ItemInfo> tmpWorkspaceItems = new ArrayList<ItemInfo>();
        final ArrayList<ItemInfo> tmpAppWidgets = new ArrayList<ItemInfo>();
        synchronized (sBgLock) {
            tmpWorkspaceItems.addAll(sBgWorkspaceItems);
            tmpAppWidgets.addAll(sBgAppWidgets);
        }
        for (int i = 0; i < tmpWorkspaceItems.size(); i++) {
            ItemInfo item = tmpWorkspaceItems.get(i);
            if (item.container == LauncherSettings.Favorites.CONTAINER_DESKTOP
                    && !screens.contains(item.screen)) {
                Log.d(TAG, "" + item.screen + ":" + item);
                screens.add(item.screen);
            }
        }

        for (int i = 0; i < tmpAppWidgets.size(); i++) {
            ItemInfo item = tmpAppWidgets.get(i);
            if (item.container == LauncherSettings.Favorites.CONTAINER_DESKTOP
                    && !screens.contains(item.screen)) {
                screens.add(item.screen);
            }
        }

        Collections.sort(screens);
        return screens;
    }

    public static void sendStatus() {
        // TODO Auto-generated method stub
        UserTrackerHelper.iconStatus(sBgWorkspaceItems);
        List<FolderInfo> list = new ArrayList<FolderInfo>();

        synchronized (sBgLock) {
            for (Long key : sBgFolders.keySet()) {
                list.add(sBgFolders.get(key));
            }
        }
        UserTrackerHelper.folderStatus(list);

        int wcount = 0;
        int scount = 0;
        ArrayList<ItemInfo> allApps = getAllAppItems();
        for (ItemInfo item : allApps) {
            if ((item.itemType == Favorites.ITEM_TYPE_GADGET) || (item.itemType == Favorites.ITEM_TYPE_APPWIDGET)) {
                wcount++;
            }
            if (item.itemType == Favorites.ITEM_TYPE_SHORTCUT) {
                scount++;
            }
        }
        allApps.clear();
        UserTrackerHelper.wigdetStatus(wcount);
        UserTrackerHelper.shortcutStatus(scount);
    }

    public static void checkFolderAndUpdate() {
        Log.d(TAG, "checkFolderAndUpdate in");
        final ArrayList<FolderInfo> folderToRemove = new ArrayList<FolderInfo>();
        final ArrayList<ItemInfo> itemsToUpdate = new ArrayList<ItemInfo>();
        ArrayList<FolderInfo> allFolders = getAllFolderItems();
        for (FolderInfo info: allFolders) {
            if ((info != null) && (info.contents != null)) {
                Log.d(TAG, "folder " + info.title + " size is " + info.contents.size());
                if (info.contents.size() == 1){
                    ShortcutInfo itemInFolder = info.contents.get(0);
                    if (itemInFolder != null) {
                        //replace the folder with the item in it
                        itemInFolder.cellX = info.cellX;
                        itemInFolder.cellY = info.cellY;
                        itemInFolder.container = info.container;
                        itemInFolder.screen = info.screen;
                        updateItemInDatabase(LauncherApplication.getContext(), itemInFolder);
                        itemsToUpdate.add(itemInFolder);
                        info.contents.remove(itemInFolder);
                        final Callbacks callbacks = mCallbacks != null ? mCallbacks.get() : null;
                        final Runnable r = new Runnable() {
                            @Override
                            public void run() {
                                if (callbacks != null) {
                                    callbacks.closeFolderWithoutExpandAnimation();
                                }
                            }
                        };
                        mHandler.post(r);
                    }
					//topwise zyf add for hide folder
					if(!isShowHideFolder(info))
					//topwise zyf add for hide folder end 
					 folderToRemove.add(info);
				     
                } else if (info.contents.size() == 0) {
                   //topwise zyf add for hide folder
					if(!isShowHideFolder(info))
					//topwise zyf add for hide folder end 
					folderToRemove.add(info);
				
                }
            }
        }

        final ArrayList<ItemInfo> FoldersToViewRemoved = new ArrayList<ItemInfo>();
        for (FolderInfo info: folderToRemove) {
            final ItemInfo iteminfo = (ItemInfo) info;
            FoldersToViewRemoved.add(iteminfo);
            Log.d(TAG, "remove folder id " + info.id);
            deleteItemFromDatabase(LauncherApplication.getContext(), iteminfo);
        }

        final Callbacks callbacks = mCallbacks != null ? mCallbacks.get() : null;
        final Runnable rFolderRemove = new Runnable() {
            @Override
            public void run() {
                if (callbacks != null) {
                    callbacks.bindItemsViewRemoved(FoldersToViewRemoved);
                }
            }
        };

        final Runnable rItemRemove = new Runnable() {
            @Override
            public void run() {
                if (callbacks != null) {
                    callbacks.bindItemsViewRemoved(itemsToUpdate);
                }
            }
        };

        final Runnable rItemCreate = new Runnable() {
            @Override
            public void run() {
                if (callbacks != null) {
                    callbacks.bindItemsViewAdded(itemsToUpdate);
                }
            }
        };

        final Runnable checkEmptyScreen = new Runnable() {
            @Override
            public void run() {
                if (callbacks != null) {
                    callbacks.checkAndRemoveEmptyCell();
                }
            }
        };
        if (folderToRemove.size() > 0) {
            runOnMainThread(rItemRemove);
            runOnMainThread(rFolderRemove);
            runOnMainThread(rItemCreate);
            runOnMainThread(checkEmptyScreen);
        }
        folderToRemove.clear();
    }

    public static void checkFolderAndUpdateByUI() {
        Runnable r = new Runnable() {
            public void run() {
                checkFolderAndUpdate();
            }
        };
        runOnWorkerThread(r);
    }

    public static void restoreDownloadHandled() {
        final Runnable r = new Runnable() {
            @Override
            public void run() {
                checkFolderAndUpdate();
            }
        };
        sWorker.post(r);
    }

    private static boolean isDuplicateItem(ItemInfo item) {
        if (item instanceof ShortcutInfo &&
                (item.itemType == LauncherSettings.Favorites.ITEM_TYPE_APPLICATION ||
                        item.itemType == LauncherSettings.Favorites.ITEM_TYPE_NOSPACE_APPLICATION)) {
            ShortcutInfo shortcutInfo = (ShortcutInfo) item;
            checkComponentBlock: {
                if (shortcutInfo.intent == null || shortcutInfo.intent.getComponent() == null) {
                    break checkComponentBlock;
                }
                ComponentName componentName = shortcutInfo.intent.getComponent();
                Log.d(TAG, "Checking duplicate item: componentName = " + componentName);
                ArrayList<ItemInfo> allApps = getAllAppItems();
                for (ItemInfo itemInfo : allApps) {
                    if (itemInfo.itemType != LauncherSettings.Favorites.ITEM_TYPE_APPLICATION
                                    && itemInfo.itemType != LauncherSettings.Favorites.ITEM_TYPE_NOSPACE_APPLICATION) {
                        continue;
                    }
                    if (itemInfo instanceof ShortcutInfo) {
                        ShortcutInfo compareShortcutInfo = (ShortcutInfo) itemInfo;
                        if (compareShortcutInfo.intent != null
                                        && componentName.equals(compareShortcutInfo.intent.getComponent())) {
                            Log.w(TAG, "Ignore a duplicate item. componentName = " + componentName);
                            return true;
                        }
                    }
                }
            }
        }
        Log.d(TAG, "No duplicate item.");
        return false;
    }

    public void changeLayoutForAgedModeChanged(boolean isAgedMode, final int countx,
            final int county) {
        //save new countx and county to sharedpreference
        mCellCountX = countx;
        mCellCountY = county;
        Launcher launcher = LauncherApplication.getLauncher();
        if (launcher != null) {
            Log.d(AgedModeUtil.TAG, "Change the UI because of the agedModeState change to:"
                    + isAgedMode + ",the des layout is:" + countx + "," + county);
            ConfigManager.setCellCountX(mCellCountX);
            ConfigManager.setCellCountY(mCellCountY);
            if (isAgedMode) {
                ConfigManager.adjustToThreeLayout();
                launcher.adjustToThreeLayout();
                BubbleTextView.adjustToThreeLayout();
            } else {
                ConfigManager.adjustFromThreeLayout();
                launcher.adjustFromThreeLayout();
                BubbleTextView.adjustFromThreeLayout();
            }
            launcher.resetGridSize(mCellCountX, mCellCountY);
        } else {
            Log.d(AgedModeUtil.TAG,
                    "the launcher is null when changeLayoutForAgedModeChanged,so do not change the layout,the desAgedState:"
                            + isAgedMode + "des layout:" + countx + "," + county);
        }
    }

    private void stopAllDownloadItems() {
        final Context tmpcontext = LauncherApplication.getContext();
        ArrayList<ItemInfo> allApps = getAllAppItems();
        for (ItemInfo item: allApps) {
            if (item.itemType == LauncherSettings.Favorites.ITEM_TYPE_SHORTCUT_DOWNLOADING) {
                String pkgName = ((ShortcutInfo)item).intent.
                                     getStringExtra(AppDownloadManager.TYPE_PACKAGENAME);
                Intent intent = new Intent(AppDownloadManager.ACTION_HS_DOWNLOAD_TASK);
                intent.putExtra(AppDownloadManager.TYPE_ACTION,
                                    AppDownloadManager.ACTION_HS_DOWNLOAD_CANCEL);
                intent.putExtra(AppDownloadManager.TYPE_PACKAGENAME, pkgName);
                tmpcontext.sendBroadcast(intent);
            }
        }
    }

    public void clearDownloadItems() {
        final Runnable r = new Runnable(){
            @Override
            public void run() {
                mClearAllDownload = true;
            }
        };
        runOnWorkerThread(r);
    }

    //for icon layout change
    public void changeLayout(final int countx, final int county) {
        Log.d(TAG, "new countx is " + countx + " new county is " + county);
        //create current layout data
        //screen + 1 for hotseat, + 6 for hideseat
        /* HIDESEAT_SCREEN_NUM_MARKER: see ConfigManager.java */
        final ItemInfo occupied[][][] = new ItemInfo[mMaxScreenCount + 1 + ConfigManager.getHideseatScreenMaxCount()][mCellCountX + 1][mCellCountY + 1];
        createCurrentLayoutData(occupied);
        //calculate new position
        if (mCellCountX == countx) {
            //calcNewLayoutDataSameX(occupied, countx, county);
            calcNewLayoutData(occupied, countx, county);
        } else {
            Log.d(TAG, "unsupport layout at present");
            calcNewLayoutData(occupied, countx, county);
        }
        //save new countx and county to sharedpreference
        mCellCountX = countx;
        mCellCountY = county;
        //call UI re-bind
        Runnable run = new Runnable() {
            public void run() {
                updateLayoutData();
            }
        };
        runOnWorkerThread(run);
        final Callbacks oldCallbacks = mCallbacks != null ? mCallbacks.get() : null;
        Runnable r = new Runnable() {
            @Override
            public void run() {
                Callbacks callbacks = tryGetCallbacks(oldCallbacks);
                if (callbacks != null) {
                    oldCallbacks.resetGridSize(mCellCountX, mCellCountY);
					oldCallbacks.startBinding();
                    oldCallbacks.collectCurrentViews();
                    oldCallbacks.reLayoutCurrentViews();
                    callbacks.checkAndRemoveEmptyCell();
                    Utils.dismissLoadingDialog();
                }
            }
        };
        runOnMainThread(r);
        
        final int currentScreen = 0;

        final ArrayList<ItemInfo> workspaceItems = new ArrayList<ItemInfo>();
        final ArrayList<LauncherAppWidgetInfo> appWidgets =
                new ArrayList<LauncherAppWidgetInfo>();
        final HashMap<Long, FolderInfo> folders = new HashMap<Long, FolderInfo>();
        final HashMap<Long, ItemInfo> itemsIdMap = new HashMap<Long, ItemInfo>();
        synchronized (sBgLock) {
            workspaceItems.addAll(sBgWorkspaceItems);
            appWidgets.addAll(sBgAppWidgets);
                folders.putAll(sBgFolders);
            itemsIdMap.putAll(sBgItemsIdMap);
        }

        final ArrayList<ItemInfo> currentWorkspaceItems = new ArrayList<ItemInfo>();
        final ArrayList<ItemInfo> otherWorkspaceItems = new ArrayList<ItemInfo>();
        final ArrayList<LauncherAppWidgetInfo> currentAppWidgets =
                new ArrayList<LauncherAppWidgetInfo>();
        final ArrayList<LauncherAppWidgetInfo> otherAppWidgets =
                new ArrayList<LauncherAppWidgetInfo>();
        final HashMap<Long, FolderInfo> currentFolders = new HashMap<Long, FolderInfo>();
        final HashMap<Long, FolderInfo> otherFolders = new HashMap<Long, FolderInfo>();

        filterCurrentWorkspaceItems(currentScreen, workspaceItems, currentWorkspaceItems,
                otherWorkspaceItems);
        filterCurrentAppWidgets(currentScreen, appWidgets, currentAppWidgets,
                otherAppWidgets);
        filterCurrentFolders(currentScreen, itemsIdMap, folders, currentFolders,
                otherFolders);
        sortWorkspaceItemsSpatially(currentWorkspaceItems);
        sortWorkspaceItemsSpatially(otherWorkspaceItems);

        bindWorkspaceItems(oldCallbacks, currentWorkspaceItems, currentAppWidgets,
                currentFolders, null);
        bindWorkspaceItems(oldCallbacks, otherWorkspaceItems, otherAppWidgets,
                otherFolders, null);
    }

    public void updateLayoutData() {
        Log.d(TAG, "updateLayoutData in");
        ArrayList<updateArgs> updatelist = new ArrayList<updateArgs>();
        ArrayList<ItemInfo> allApps = getAllAppItems();
        for (ItemInfo item: allApps) {
            if ((item.container == Favorites.CONTAINER_DESKTOP) ||
                 (item.container > 0)){
                ContentValues values = new ContentValues();
                values.put("screen", item.screen);
                values.put("cellY", item.cellY);
                values.put("cellX", item.cellX);
                values.put("spanX", item.spanX);
                values.put("spanY", item.spanY);
                values.put("container", item.container);
                Uri uri = LauncherSettings.Favorites.getContentUri(item.id, false);
                updateArgs args = new updateArgs(uri, values, null, null);
                updatelist.add(args);
            }
        }
        Log.d(TAG, "update db start");
        LauncherApplication app = (LauncherApplication) LauncherApplication.getContext();
        if ((app != null) && (app.getLauncherProvider() != null)) {
            Log.d(TAG, "before call bulkupdate");
            app.getLauncherProvider().bulkUpdate(updatelist);
        }
        updatelist.clear();
        ConfigManager.setCellCountX(mCellCountX);
        ConfigManager.setCellCountY(mCellCountY);
        Log.d(TAG, "updateLayoutData out");
    }

    /** Filters the set of items who are directly or indirectly (via another container) on the
     * specified screen. */
    public void filterCurrentWorkspaceItems(int currentScreen,
            ArrayList<ItemInfo> allWorkspaceItems,
            ArrayList<ItemInfo> currentScreenItems,
            ArrayList<ItemInfo> otherScreenItems) {
        // Purge any null ItemInfos
        Iterator<ItemInfo> iter = allWorkspaceItems.iterator();
        while (iter.hasNext()) {
            ItemInfo i = iter.next();
            if (i == null) {
                iter.remove();
            }
        }

        // If we aren't filtering on a screen, then the set of items to load is the full set of
        // items given.
        if (currentScreen < 0) {
            currentScreenItems.addAll(allWorkspaceItems);
        }

        // Order the set of items by their containers first, this allows use to walk through the
        // list sequentially, build up a list of containers that are in the specified screen,
        // as well as all items in those containers.
        Set<Long> itemsOnScreen = new HashSet<Long>();
        Collections.sort(allWorkspaceItems, new Comparator<ItemInfo>() {
            @Override
            public int compare(ItemInfo lhs, ItemInfo rhs) {
                return (int) (lhs.container - rhs.container);
            }
        });
        for (ItemInfo info : allWorkspaceItems) {
            if (info.container == LauncherSettings.Favorites.CONTAINER_DESKTOP) {
                if (info.screen == currentScreen) {
                    currentScreenItems.add(info);
                    itemsOnScreen.add(info.id);
                } else {
                    otherScreenItems.add(info);
                }
            } else if (info.container == LauncherSettings.Favorites.CONTAINER_HOTSEAT) {
                currentScreenItems.add(info);
                itemsOnScreen.add(info.id);
            } else if (info.container == LauncherSettings.Favorites.CONTAINER_HIDESEAT) {
                otherScreenItems.add(info);
            } else {
                if (itemsOnScreen.contains(info.container)) {
                    currentScreenItems.add(info);
                    itemsOnScreen.add(info.id);
                } else {
                    otherScreenItems.add(info);
                }
            }
        }
    }

    /** Filters the set of widgets which are on the specified screen. */
    private void filterCurrentAppWidgets(int currentScreen,
            ArrayList<LauncherAppWidgetInfo> appWidgets,
            ArrayList<LauncherAppWidgetInfo> currentScreenWidgets,
            ArrayList<LauncherAppWidgetInfo> otherScreenWidgets) {
        // If we aren't filtering on a screen, then the set of items to load is the full set of
        // widgets given.
        if (currentScreen < 0) {
            currentScreenWidgets.addAll(appWidgets);
        }

        for (LauncherAppWidgetInfo widget : appWidgets) {
            if (widget == null) continue;
            if (widget.container == LauncherSettings.Favorites.CONTAINER_DESKTOP &&
                    widget.screen == currentScreen) {
                currentScreenWidgets.add(widget);
            } else {
                otherScreenWidgets.add(widget);
            }
        }
    }

    /** Filters the set of folders which are on the specified screen. */
    private void filterCurrentFolders(int currentScreen,
            HashMap<Long, ItemInfo> itemsIdMap,
            HashMap<Long, FolderInfo> folders,
            HashMap<Long, FolderInfo> currentScreenFolders,
            HashMap<Long, FolderInfo> otherScreenFolders) {
        // If we aren't filtering on a screen, then the set of items to load is the full set of
        // widgets given.
        if (currentScreen < 0) {
            currentScreenFolders.putAll(folders);
        }

        for (long id : folders.keySet()) {
            ItemInfo info = itemsIdMap.get(id);
            FolderInfo folder = folders.get(id);
            if (info == null || folder == null) continue;
            if (info.container == LauncherSettings.Favorites.CONTAINER_DESKTOP &&
                    info.screen == currentScreen) {
                currentScreenFolders.put(id, folder);
            } else {
                otherScreenFolders.put(id, folder);
            }
        }
    }

    /** Sorts the set of items by hotseat, workspace (spatially from top to bottom, left to
     * right) */
    private void sortWorkspaceItemsSpatially(ArrayList<ItemInfo> workspaceItems) {
        // XXX: review this
        Collections.sort(workspaceItems, new Comparator<ItemInfo>() {
            @Override
            public int compare(ItemInfo lhs, ItemInfo rhs) {
                int cellCountX = LauncherModel.getCellCountX();
                int cellCountY = LauncherModel.getCellCountY();
                int screenOffset = cellCountX * cellCountY;
                int containerOffset = screenOffset * (mMaxScreenCount + 1); // +1 hotseat
                long lr = (lhs.container * containerOffset + lhs.screen * screenOffset +
                        lhs.cellY * cellCountX + lhs.cellX);
                long rr = (rhs.container * containerOffset + rhs.screen * screenOffset +
                        rhs.cellY * cellCountX + rhs.cellX);
                return (int) (lr - rr);
            }
        });
    }

    private void bindWorkspaceItems(final Callbacks oldCallbacks,
            final ArrayList<ItemInfo> workspaceItems,
            final ArrayList<LauncherAppWidgetInfo> appWidgets,
            final HashMap<Long, FolderInfo> folders,
            ArrayList<Runnable> deferredBindRunnables) {

        final boolean postOnMainThread = (deferredBindRunnables != null);

        // Bind the workspace items
        int N = workspaceItems.size();
        for (int i = 0; i < N; i += ITEMS_CHUNK) {
            final int start = i;
            final int chunkSize = (i+ITEMS_CHUNK <= N) ? ITEMS_CHUNK : (N-i);
            final Runnable r = new Runnable() {
                @Override
                public void run() {
                    Callbacks callbacks = tryGetCallbacks(oldCallbacks);
                    if (callbacks != null) {
                        callbacks.bindItems(workspaceItems, start, start+chunkSize);
                    }
                }
            };
            if (postOnMainThread) {
                deferredBindRunnables.add(r);
            } else {
                runOnMainThread(r, MAIN_THREAD_BINDING_RUNNABLE);
            }
        }

        // Bind the folders
        if (!folders.isEmpty()) {
            final Runnable r = new Runnable() {
                public void run() {
                    Callbacks callbacks = tryGetCallbacks(oldCallbacks);
                    if (callbacks != null) {
                        callbacks.bindFolders(folders);
                    }
                }
            };
            if (postOnMainThread) {
                deferredBindRunnables.add(r);
            } else {
                runOnMainThread(r, MAIN_THREAD_BINDING_RUNNABLE);
            }
        }

        // Bind the widgets, one at a time
        N = appWidgets.size();
        for (int i = 0; i < N; i++) {
            final LauncherAppWidgetInfo widget = appWidgets.get(i);
            final Runnable r = new Runnable() {
                public void run() {
                    Callbacks callbacks = tryGetCallbacks(oldCallbacks);
                    if (callbacks != null) {
                        callbacks.bindAppWidget(widget);
                    }
                }
            };
            if (postOnMainThread) {
                deferredBindRunnables.add(r);
            } else {
                runOnMainThread(r, MAIN_THREAD_BINDING_RUNNABLE);
            }
        }
    }

    /**
     * Gets the callbacks object.  If we've been stopped, or if the launcher object
     * has somehow been garbage collected, return null instead.  Pass in the Callbacks
     * object that was around when the deferred message was scheduled, and if there's
     * a new Callbacks object around then also return null.  This will save us from
     * calling onto it with data that will be ignored.
     */
    Callbacks tryGetCallbacks(Callbacks oldCallbacks) {
        synchronized (mLock) {
            if (mCallbacks == null) {
                return null;
            }
            final Callbacks callbacks = mCallbacks.get();
            if (callbacks != oldCallbacks) {
                return null;
            }
            if (callbacks == null) {
                Log.w(TAG, "no mCallbacks");
                return null;
            }
            return callbacks;
        }
    }

    public void calcNewLayoutDataSameX(ItemInfo[][][] occupied, int countx, int county) {
        int padding = 0;
        int currentLineCount = 0;
        int newLineCount = 0;
        int currentMaxScreen = 0;
        //ArrayList<updateArgs> updatelist = new ArrayList<updateArgs>();
        ArrayList<ItemInfo> handledItems = new ArrayList<ItemInfo>();
        for (int lscreen = 0; lscreen < mMaxScreenCount; lscreen++) {
            for (int lcelly = 0; lcelly < mCellCountY; lcelly++) {
                for (int lcellx = 0; lcellx < mCellCountX; lcellx++) {
                    ItemInfo item = occupied[lscreen][lcellx][lcelly];
                    if ((item == null) || (item.container != Favorites.CONTAINER_DESKTOP)) {
                        continue;
                    }
                    if (handledItems.contains(item) == true) {
                        Log.d(TAG, "the item is handled " + item.id);
                        continue;
                    }
                    handledItems.add(item);
                    Log.d(TAG, "calc item id:" + item.id + " new postion");
                    Log.d(TAG, "current position is screen:" + item.screen +
                                                    " cellY:" + item.cellY);
                    currentLineCount = item.screen * mCellCountY + item.cellY + padding;
                    int leftLine = county - currentLineCount % county;
                    if (leftLine < item.spanY) {
                        //no enough space for this block
                        padding = padding + leftLine;
                        //start in a new screen
                        newLineCount = currentLineCount + leftLine;
                    } else {
                        newLineCount = currentLineCount;
                    }

                    item.screen = newLineCount / county;
                    item.cellY = newLineCount % county;
                    Log.d(TAG, "new position is screen:" + item.screen +
                            " cellY:" + item.cellY);
                    if (item.screen > currentMaxScreen) {
                        currentMaxScreen = item.screen;
                    }
                }
            }
        }
        handledItems.clear();
        Log.d(TAG, "update db finish");
    }

    public void calcNewLayoutData(ItemInfo[][][] occupied, int countX, int countY) {
        Log.d(TAG, "calcNewLayoutData in");
        ItemInfo[][][] newoccupied = new ItemInfo[mMaxScreenCount + 4][countX + 1][countY + 1];
        int padding = 0;
        int currentPosCount = 0;
        int newPosCount = 0;
        int currentMaxScreen = 0;
        //ArrayList<updateArgs> updatelist = new ArrayList<updateArgs>();
        ArrayList<ItemInfo> handledItems = new ArrayList<ItemInfo>();
        int emptycount = 0;    //for remove empty line
        int lastScreenLastLineEmptyCount = 0;
        for (int lscreen = 0; lscreen < mMaxScreenCount; lscreen++) {
            //lixuhui
            boolean lastScreenLastLineEmpty = (lastScreenLastLineEmptyCount == mCellCountX) ? true : false;
            lastScreenLastLineEmptyCount = 0;
            //end
            for (int lcelly = 0; lcelly < mCellCountY; lcelly++) {
                for (int lcellx = 0; lcellx < mCellCountX; lcellx++) {
                    ItemInfo item = occupied[lscreen][lcellx][lcelly];
                    if ((item == null) || (item.container != Favorites.CONTAINER_DESKTOP)) {
                        //don't remove the empty in first screen
                        if ((lscreen > 0) && (item == null)){
                            emptycount++;
                            Log.d(TAG, "emptycount after inc is " + emptycount);
                            if ((emptycount >= countX) && (lcellx == countX - 1)){
                                emptycount -= countX;
                                padding -= countX;
                            }
                        }
                        //lixuhui
                        //when change layout from 4X4 to 4X5 and the every page will add one row, 
                        //if the next page first item can not fill in the last row,it will cause this page add a null row,
                        //and we change the layout from 4X5 to 4X4, it will cause all page items position after this page will add 4, so it may be cause add a blank page in the screen. so we need to deal with this condition to display better
                        //for example: in the first has a 4X4 widget(widget1) and the second page also has a 4X4 widget(widget2), in this condition, in the first page will add a null row.
                        //and then we change the layout from 4X5 to 4X4, in this condition, the widget2's position will increate 4(from 16 to 20), it will cause the second page does not have the enough zone to display the widget2, so the widget2 will display in third page, the second will be a blank page. 
                        if(lcelly == (mCellCountY - 1) && (mCellCountY > countY)){
                            lastScreenLastLineEmptyCount ++;
                        }
                        //end
                        //only support desktop now. no support hotseat and hideseat
                        continue;
                    }
                    emptycount = 0;
                    if (handledItems.contains(item) == true) {
                        Log.d(TAG, "the item is handled " + item.id);
                        continue;
                    }
                    handledItems.add(item);
                    Log.d(TAG, "calc item id:" + item.id + " new postion");
                    Log.d(TAG, "current position is screen:" + item.screen +
                                                    " cellY:" + item.cellY +
                                                    " cellX:" + item.cellX);

                    //for the widget larger than cellx or celly
                    if (item.spanX > countX) {
                        item.spanX = countX;
                    }
                    if (item.spanY > countY) {
                        item.spanY = countY;
                    }

                    //lixuhui
                    if(lastScreenLastLineEmpty) padding -= mCellCountX;
                    //end
                    currentPosCount = item.screen * mCellCountY * mCellCountX
                                           + item.cellY * mCellCountX
                                           + item.cellX + padding;
                    Log.d(TAG, "padding is " + padding);
                    int leftLine = countY - (currentPosCount % (countX * countY)) /countX;
                    int leftPosInLine = countX - (currentPosCount % (countX * countY)) % countX;

                    if (leftLine < item.spanY) {
                        //no enough space for this block
                        Log.d(TAG, "leftLine is " + leftLine);
                        padding = padding + leftLine * countX - (currentPosCount % (countX * countY)) % countX;
                        //start in a new screen
                        Log.d(TAG, "before calc new newPosCount is " + newPosCount);
                        newPosCount = currentPosCount + leftLine * countX - (currentPosCount % (countX * countY)) % countX;
                        Log.d(TAG, "new newPosCount is " + newPosCount);
                    } else if (leftPosInLine < item.spanX) {
                        //no enough space in current line, find new position in next line
                        newPosCount = currentPosCount + leftPosInLine;
                        padding = padding + leftPosInLine;
                    } else {
                        newPosCount = currentPosCount;
                    }
                    //find a new block to place the item
                    int newscreen = newPosCount / (countX * countY);
                    int newx = (newPosCount % (countX * countY)) % countX;
                    int newy = (newPosCount % (countX * countY)) / countX;
                    //check whether new block position occupied by other items
                    //and find a new block position if it is occupied
                    while(true) {
                        boolean occupy = false;
                        for (int x = 0; x < item.spanX; x++) {
                            for (int y = 0; y < item.spanY; y++) {
                                if (((newx + x) < countX) &&
                                    ((newy + y) < countY) &&
                                    (newscreen < mMaxScreenCount) &&
                                    (newoccupied[newscreen][newx + x][newy + y] == null)) {
                                    occupy = false;
                                } else {
                                    occupy = true;
                                    break;
                                }
                            }
                            if (occupy == true) {
                                if ((countX - newx) > item.spanX) {
                                    newx++;
                                } else {
                                    newx = countX;
                                }
                                newy = newy + newx /countX;
                                newx = newx % countX;

                                newscreen = newscreen + newy /countY;
                                newy = newy % countY;
                                break;
                            }
                        }

                        if (occupy == false) {
                            break;
                        }

                        if (newscreen >= mMaxScreenCount) {
                            Log.d(TAG, "out of the max screen count");
                            //for items out of max screen, leave them handled in handleNoSpaceItems
                            newscreen = -1;
                            newx = -1;
                            newy = -1;
                            break;
                        }
                    }
                    item.screen = newscreen;
                    item.cellY = newy;
                    item.cellX = newx;
                    if ((newscreen > -1) && (newscreen < mMaxScreenCount)) {
                        for (int x = 0; x < item.spanX; x++) {
                            for (int y = 0; y < item.spanY; y++) {
                                if (((x + newx) < countX) &&
                                    ((y + newy) < countY)) {
                                        newoccupied[newscreen][newx + x][newy + y] = item;
                                        if (item.screen > currentMaxScreen) {
                                            currentMaxScreen = item.screen;
                                        }
                                }
                            }
                        }
                    }
                    Log.d(TAG, "new position is screen:" + item.screen +
                            " cellY:" + item.cellY +
                            " cellX:" + item.cellX);
                }
            }
        }
        handledItems.clear();

        handleNoSpaceItems(newoccupied, currentMaxScreen + 1, countX, countY);

        Log.d(TAG, "calcNewLayoutData out");
    }

    public void handleNoSpaceItems(ItemInfo[][][] newoccupied, int screen, int countX, int countY) {
        ArrayList<ItemInfo> noSpaceItemsList = new ArrayList<ItemInfo>();
        ArrayList<ItemInfo> removeList = new ArrayList<ItemInfo>();
        ArrayList<ItemInfo> noSpaceFoldersList = new ArrayList<ItemInfo>();
        ArrayList<ItemInfo> allApps = new ArrayList<ItemInfo>();
        synchronized (sBgLock) {
            allApps.addAll(sBgItemsIdMap.values());
        }
        for (ItemInfo item: allApps) {
            if (item.screen == -1) {
                //delete no space widget and gadget
                if ((item.itemType == Favorites.ITEM_TYPE_APPWIDGET) ||
                   (item.itemType == Favorites.ITEM_TYPE_GADGET)) {
                    removeList.add(item);
                } else {
                    if (item.itemType == Favorites.ITEM_TYPE_FOLDER) {
                        noSpaceFoldersList.add(item);
                    } else {
                        noSpaceItemsList.add(item);
                    }
                }
            }
        }
        Log.d(TAG, "no space item count is " + noSpaceItemsList.size());
        if (removeList.size() > 0) {
            //remove no space widget
            for (ItemInfo wItem: removeList) {
                deleteItemFromDatabase(mApp, wItem);
            }
            removeList.clear();
        }
        if ((noSpaceItemsList.size() == 0) &&
           (noSpaceFoldersList.size() == 0)){
            return;
        }
        //for no space folder, find an empty position
        //if no empty postion, delete the folder and add items in it to no space item list
        for (ItemInfo noSpaceFolderItem: noSpaceFoldersList) {
            ScreenPosition position = findEmptyCellInOccupied(newoccupied, screen, countX, countY);
            if (position != null) {
                noSpaceFolderItem.screen = position.s;
                noSpaceFolderItem.cellX = position.x;
                noSpaceFolderItem.cellY = position.y;
                newoccupied[noSpaceFolderItem.screen][noSpaceFolderItem.cellX][noSpaceFolderItem.cellY] = noSpaceFolderItem;
            } else {
                for(ItemInfo itemInFolder: ((FolderInfo)noSpaceFolderItem).contents) {
                    itemInFolder.container = Favorites.CONTAINER_DESKTOP;
                    itemInFolder.screen = -1;
                    itemInFolder.cellX = -1;
                    itemInFolder.cellY = -1;
                    noSpaceItemsList.add(itemInFolder);
                }
                ((FolderInfo)noSpaceFolderItem).contents.clear();
                deleteItemFromDatabase(mApp, noSpaceFolderItem);
            }
        }
        noSpaceFoldersList.clear();

        //Put all noSpaceItems in a new folder.
        //Find an empty position for the new folder,
        //if no empty position, find a single item in workspace
        //put no space items and the single item in a new folder
        //in the single item's position.
        //If no empty position or single item found, I have to leave
        //these nospace item in -1 state.
        FolderInfo newfolder = null;
        int listcount = noSpaceItemsList.size();
        for (int i = 0; i < listcount; i++) {
            ItemInfo noSpaceItem = noSpaceItemsList.get(i);
            synchronized (sBgLock) {
                if (sBgWorkspaceItems.contains(noSpaceItem)) {
                    sBgWorkspaceItems.remove(noSpaceItem);
                }
            }
            if ((newfolder == null) || (newfolder.contents.size() >= ConfigManager.getFolderMaxItemsCount())) {
                ScreenPosition position = findEmptyCellInOccupied(newoccupied, screen, countX, countY);
                if (position != null) {
                    if ((listcount - i) == 1) {
                        //only one item left
                        noSpaceItem.screen = position.s;
                        noSpaceItem.cellX = position.x;
                        noSpaceItem.cellY = position.y;
                        newoccupied[noSpaceItem.screen][noSpaceItem.cellX][noSpaceItem.cellY] = noSpaceItem;
                    } else {
                        //create a new folder
                        newfolder = new FolderInfo();
                        newfolder.container = Favorites.CONTAINER_DESKTOP;
                        newfolder.screen = position.s;
                        newfolder.cellX = position.x;
                        newfolder.cellY = position.y;
                        newfolder.title = noSpaceItem.title + mApp.getResources().getString(R.string.folder_name_etc);
                        addItemToDatabase(mApp, newfolder, newfolder.container, newfolder.screen,
                                                     newfolder.cellX, newfolder.cellY, false);
                        Log.d(TAG, "the newfolder id is " + newfolder.id);
                        synchronized (sBgLock) {
                            sBgFolders.put(newfolder.id, newfolder);
                            sBgItemsIdMap.put(newfolder.id, newfolder);
                            if (sBgWorkspaceItems.contains(newfolder) == false) {
                                sBgWorkspaceItems.add(newfolder);
                            }
                        }

                        //put the no space item in newfolder
                        newfolder.contents.add((ShortcutInfo)noSpaceItem);
                        noSpaceItem.container = newfolder.id;
                        noSpaceItem.screen = 0;
                        //the no space item is the first item in newfolder
                        //so x and y is 0
                        noSpaceItem.cellX = 0;
                        noSpaceItem.cellY = 0;
                        synchronized (sBgLock) {
                            if (sBgNoSpaceItems.contains(noSpaceItem) == true) {
                                sBgNoSpaceItems.remove(noSpaceItem);
                            }
                        }
                        if (noSpaceItem.itemType == Favorites.ITEM_TYPE_NOSPACE_APPLICATION) {
                            noSpaceItem.itemType = Favorites.ITEM_TYPE_APPLICATION;
                        }
                        newoccupied[newfolder.screen][newfolder.cellX][newfolder.cellY] = newfolder;
                    }
                } else {
                    //no empty for newfolder, find a item in workspace and create a new folder at the item's position
                    ItemInfo firstSingleItem = null;
                    for (int s = screen - 1; s > 0; s--) {
                        for (int y = countY -1; y >= 0; y--) {
                            for (int x = countX -1; x >= 0; x--) {
                                ItemInfo item = newoccupied[s][x][y];
                                if (item != null) {
                                    if ((item.itemType == Favorites.ITEM_TYPE_APPLICATION) ||
                                        (item.itemType == Favorites.ITEM_TYPE_VPINSTALL) ||
                                        (item.itemType == Favorites.ITEM_TYPE_SHORTCUT_DOWNLOADING) ||
                                        (item.itemType == Favorites.ITEM_TYPE_SHORTCUT)) {
                                        firstSingleItem = item;
                                        break;
                                    }
                                }
                            }
                            if (firstSingleItem != null) {
                                break;
                            }
                        }
                        if (firstSingleItem != null) {
                            break;
                        }
                    }
                    if (firstSingleItem != null) {
                        newfolder = new FolderInfo();
                        newfolder.container = Favorites.CONTAINER_DESKTOP;
                        newfolder.screen = firstSingleItem.screen;
                        newfolder.cellX = firstSingleItem.cellX;
                        newfolder.cellY = firstSingleItem.cellY;
                        newfolder.title = firstSingleItem.title + mApp.getResources().getString(R.string.folder_name_etc);
                        addItemToDatabase(mApp, newfolder, newfolder.container, newfolder.screen,
                                                     newfolder.cellX, newfolder.cellY, false);
                        Log.d(TAG, "the newfolder id is " + newfolder.id);
                        synchronized (sBgLock) {
                            sBgFolders.put(newfolder.id, newfolder);
                            sBgItemsIdMap.put(newfolder.id, newfolder);
                            if (sBgWorkspaceItems.contains(newfolder) == false) {
                                sBgWorkspaceItems.add(newfolder);
                            }
                        }
                        //put the firstSingleItem in newfolder;
                        newfolder.contents.add((ShortcutInfo)firstSingleItem);
                        firstSingleItem.container = newfolder.id;
                        firstSingleItem.screen = 0;
                        firstSingleItem.cellX = 0;
                        firstSingleItem.cellY = 0;
                        synchronized (sBgLock) {
                            if (sBgWorkspaceItems.contains(firstSingleItem)) {
                                sBgWorkspaceItems.remove(firstSingleItem);
                            }
                        }
                        //put the no space item in newfolder
                        newfolder.contents.add((ShortcutInfo)noSpaceItem);
                        noSpaceItem.screen = 0;
                        //the no space item is the first item in newfolder
                        //so x and y is 0
                        noSpaceItem.cellX = 1;
                        noSpaceItem.cellY = 0;
                        newoccupied[newfolder.screen][newfolder.cellX][newfolder.cellY] = newfolder;
                    }
                }
            } else {
                //put the no space item in newfolder
                newfolder.contents.add((ShortcutInfo)noSpaceItem);
                noSpaceItem.container = newfolder.id;
                noSpaceItem.screen = 0;
                noSpaceItem.cellX = newfolder.contents.size() % ConfigManager.getFolderMaxCountY();
                noSpaceItem.cellY = newfolder.contents.size() / ConfigManager.getFolderMaxCountY();
                synchronized (sBgLock) {
                    if (sBgNoSpaceItems.contains(noSpaceItem)) {
                        sBgNoSpaceItems.remove(noSpaceItem);
                    }
                }
                if (noSpaceItem.itemType == Favorites.ITEM_TYPE_NOSPACE_APPLICATION) {
                    noSpaceItem.itemType = Favorites.ITEM_TYPE_APPLICATION;
                }
            }
        }
        noSpaceItemsList.clear();
    }

    public ScreenPosition findEmptyCellInOccupied(ItemInfo[][][] newoccupied, int screen, int countX, int countY) {
        ScreenPosition position = null;
        for (int s = screen - 1; s > 0; s--) {
            for (int y = countY -1; y >= 0; y--) {
                for (int x = countX -1; x >= 0; x--) {
                    if (newoccupied[s][x][y] == null) {
                        position = new ScreenPosition(s, x, y);
                        break;
                    }
                }
                if (position != null) {
                    break;
                }
            }
            if (position != null) {
                break;
            }
        }
        return position;
    }

    public static void createCurrentLayoutData(ItemInfo[][][] occupied) {
        ArrayList<ItemInfo> lostItems = new ArrayList<ItemInfo>();
        ArrayList<ItemInfo> allItems = new ArrayList<ItemInfo>();
        synchronized (sBgLock) {
            allItems.addAll(sBgItemsIdMap.values());
        }
        for(ItemInfo item: allItems) {
            int containerIndex = item.screen;
            if ((containerIndex < 0) || (containerIndex >= mMaxScreenCount + 4) ||
                (item.cellX >= mCellCountX + 1) || (item.cellY >= mCellCountY + 1) ||
                (item.cellX < 0) || (item.cellY < 0)) {
                Log.d(TAG, "item position error, mCellCountX " + mCellCountX);
                Log.d(TAG, "mCellCountY " + mCellCountY);
                Log.d(TAG, "item.cellX is " + item.cellX);
                Log.d(TAG, "item.cellY is " + item.cellY);
                lostItems.add(item);
                continue;
            }
            if (item.container == LauncherSettings.Favorites.CONTAINER_HOTSEAT) {
                if (item.screen >= mCellCountX + 1) {
                    Log.d(TAG, "hotseat position error, item.screen is " + item.screen);
                    Log.d(TAG, "mCellCountX " + mCellCountX);
                    lostItems.add(item);
                    continue;
                }
                if (occupied[mMaxScreenCount][item.screen][0] != null) {
                    Log.e(TAG, "Error loading shortcut into hotseat " + item
                        + " into position (" + item.screen + ":" + item.cellX + "," + item.cellY
                        + ") occupied by " + occupied[mMaxScreenCount][item.screen][0]);
                    lostItems.add(item);
                } else {
                    occupied[mMaxScreenCount][item.screen][0] = item;
                }
            } else if (item.container == LauncherSettings.Favorites.CONTAINER_HIDESEAT) {
                int screenIndex = mMaxScreenCount + 1 + item.screen;
                if (occupied[screenIndex][item.cellX][item.cellY] != null) {
                    Log.e(TAG, "Error loading shortcut into hotseat " + item
                        + " into position (" + item.screen + ":" + item.cellX + "," + item.cellY
                        + ") occupied by " + occupied[screenIndex][item.cellX][item.cellY]);
                    lostItems.add(item);
                } else {
                    occupied[screenIndex][item.cellX][item.cellY] = item;
                }
            } else if (item.container == LauncherSettings.Favorites.CONTAINER_DESKTOP) {
                // Check if any workspace icons overlap with each other
                boolean isOccupied = false;
                for (int x = item.cellX; x < (item.cellX+item.spanX); x++) {
                    for (int y = item.cellY; y < (item.cellY+item.spanY); y++) {
                        if (occupied[containerIndex][x][y] != null) {
                            Log.e(TAG, "Error loading shortcut " + item
                                + " into cell (" + containerIndex + "-" + item.screen + ":"
                                + x + "," + y
                                + ") occupied by "
                                + occupied[containerIndex][x][y]);
                            lostItems.add(item);
                            isOccupied = true;
                        }
                    }
                }
                if (isOccupied == false) {
                    for (int x = item.cellX; x < (item.cellX+item.spanX); x++) {
                        for (int y = item.cellY; y < (item.cellY+item.spanY); y++) {
                           occupied[containerIndex][x][y] = item;
                        }
                    }
                }
            }
        }

        //TODO: find new postion for lostItems
        lostItems.clear();
    }

    /**
     * check if showing the new mark icon or not
     * 
     * @return
     */
    public static boolean isShowNewMarkIcon() {
        if (mShowNewMarkInit) {
            return mShowNewMark;
        }
        return updateShowNewMark();
    }

    private static boolean updateShowNewMark() {
        mShowNewMarkInit = true;
        String spKey = LauncherApplication.getSharedPreferencesKey();
        SharedPreferences sp = LauncherApplication.getContext().getSharedPreferences(spKey,
                 Context.MODE_PRIVATE);
        //modify by huangxunwan for config Show New Mark Icon 20150617
        return mShowNewMark = sp.getBoolean(HomeShellSetting.DB_SHOW_NEW_MARK_ICON, TopwiseConfig.SUPPORT_SHOW_NEW_MARK_ICON);
    }

    /** check if it's needed to show notification mark */
    public static boolean showNotificationMark() {
        if (Utils.isYunOS2_9System())
            return false;
        return getNotificationMarkType() != HomeShellSetting.NO_NOTIFICATION;
    }

    /**
     * if user upgrades from an old version, checkout it's earlier value
     * @return return the type how we show notifications
     */
    public static int getNotificationMarkType(){
        if (mMarkType != INVALID_MARK_TYPE) {
            return mMarkType;
        }
        return updateNotificationMarkType();
    }

    private static int updateNotificationMarkType(){
        String spKey = LauncherApplication.getSharedPreferencesKey();
        SharedPreferences sp = LauncherApplication.getContext().getSharedPreferences(spKey,
                Context.MODE_PRIVATE);
        mMarkType = sp.getInt(HomeShellSetting.KEY_NOTIFICATION_MARK_PREF_NEW, INVALID_MARK_TYPE);
        if( mMarkType != INVALID_MARK_TYPE ){ // if it doesn't exist
            return mMarkType;
        }
        boolean showNotifcationOld = sp.getBoolean(HomeShellSetting.KEY_NOTIFICATION_MARK_PREF_OLD, true);
        return mMarkType = showNotifcationOld ? HomeShellSetting.ALL_NOTIFICATION : HomeShellSetting.NO_NOTIFICATION;
    }

    public void deleteItemsInDatabaseByPackageName(ArrayList<String> packages) {
        if ((packages == null) || (packages.size() <= 0)) {
            return;
        }
        final ArrayList<String> finalpackages = packages;
        Runnable r = new Runnable() {
            public void run() {
                Log.d(TAG, "deleteItemsInDatabaseByPackageName runnable in");
                ArrayList<ItemInfo> delItems = new ArrayList<ItemInfo>();
                for (String pkgname: finalpackages) {
                    if (pkgname == null) {
                        continue;
                    }
                    Hideseat.removePackageFromFrozenList(pkgname);
                    ArrayList<ItemInfo> allApps = getAllAppItems();
                    for (ItemInfo item: allApps) {
                        if ((item.itemType == Favorites.ITEM_TYPE_APPLICATION) ||
                           (item.itemType == Favorites.ITEM_TYPE_NOSPACE_APPLICATION)) {
                            Intent intent = ((ShortcutInfo)item).intent;
                            if ((intent == null) || (intent.getComponent() == null) ||
                                (intent.getComponent().getPackageName() == null)) {
                                continue;
                            }

                            if (pkgname.equals(intent.getComponent().getPackageName())) {
                                delItems.add(item);
                            }
                        }
                    }
                }

                for (ItemInfo delitem: delItems) {
                    deleteItemFromDatabase(mApp, delitem);
                    deleteItemFromAnotherTable(mApp, delitem);
                }
            }
        };
        runOnWorkerThread(r);
    }

    private static void findAndFixInvalidItems() {
        Log.d(TAG, "findAndFixInvalidItems in ");
        ArrayList<ItemInfo> invalidItems = new ArrayList<ItemInfo>();
        final ArrayList<ItemInfo> needBindItems = new ArrayList<ItemInfo>();
        final ArrayList<ItemInfo> needUpdateItems = new ArrayList<ItemInfo>();
        final ArrayList<ItemInfo> needRemoveViewItems = new ArrayList<ItemInfo>();
        final HashMap<Long, FolderInfo> needBindFolderItems = new HashMap<Long, FolderInfo>();

        scanInvalidPositionItems(invalidItems);
        if (invalidItems.size() > 0) {
            Log.d(TAG, "invalidItems.size is " + invalidItems.size());
            recoverInvalidPostionItems(invalidItems, needBindItems, needBindFolderItems, needUpdateItems, needRemoveViewItems);

            //update in db
            for (ItemInfo item: needUpdateItems) {
                updateItemInDatabase(LauncherApplication.getContext(), item);
            }

            final Callbacks callbacks = mCallbacks != null ? mCallbacks.get() : null;
            if (callbacks != null) {
                final Runnable r = new Runnable() {
                    @Override
                    public void run() {
                        callbacks.bindItemsViewRemoved(needRemoveViewItems);
                        callbacks.bindItemsAdded(needBindItems);
                        callbacks.bindFolders(needBindFolderItems);
                    }
                };
                runOnMainThread(r);
            }
        }
        invalidItems.clear();
        Log.d(TAG, "findAndFixInvalidItems out");
    }

    //This function is used to check sBgItemsIdMap,
    //and find the invalid position items,
    //such as items in folder but the folder isn't exist,
    //items that position is out of range of screen
    private static void scanInvalidPositionItems(ArrayList<ItemInfo> invalidItems) {
        if (invalidItems == null) {
            return;
        }

        ArrayList<ItemInfo> allApps = getAllAppItems();
        for (ItemInfo item: allApps) {
            if (item == null) {
                continue;
            }

            if (isInvalidPosition(item) == true) {
                Log.d(TAG, "invalid position item: " + item.id);
                invalidItems.add(item);
                continue;
            }

            if (isInDeletedFolder(item) == true) {
                Log.d(TAG, "in deleted folder item: " + item.id);
                invalidItems.add(item);
                continue;
            }
        }
    }

    //if the item is in folder and the folder isn't in sBgFolders
    //it means the item is lost.
    //this function can be invoked only after sBgFolders build complete.
    private static boolean isInDeletedFolder(ItemInfo item) {
        if (item.container < 0 ) {
            return false;
        }

        synchronized (sBgLock) {
            if (sBgFolders.containsKey(item.container)) {
                ItemInfo folderinfo = sBgFolders.get(item.container);
                if (isInvalidPosition(folderinfo) == false) {
                    return false;
                } else {
                    sBgFolders.remove(folderinfo.id);
                    if (sBgItemsIdMap.containsKey(folderinfo.id)) {
                        sBgItemsIdMap.remove(folderinfo.id);
                    }
                }
            }
        }
        return true;
    }

    //If new container type added, the new type must be added to this function
    private static boolean isInvalidPosition(ItemInfo item) {
        // valid cellX/Y lower bounds change to -1
        if (item.container == Favorites.CONTAINER_DESKTOP) {
            if (item.itemType == Favorites.ITEM_TYPE_NOSPACE_APPLICATION) {
                //no space app's screen, cellX and cellY all should be -1
                if ((item.screen != -1) || (item.cellX != -1) || (item.cellY != -1)) {
                    return true;
                }
            } else if ((item.screen >= ConfigManager.getScreenMaxCount()) ||
                (item.screen < 0) ||
                (item.cellX >= ConfigManager.getCellMaxCountX()) ||
                (item.cellX < -1) ||
                (item.cellY >= ConfigManager.getCellMaxCountY()) ||
                (item.cellY < -1)) {
                return true;
            }
        }
        else if (item.container == Favorites.CONTAINER_HOTSEAT) {
            //in hotseat, item's screen and cellx are same
            if ((item.screen >= ConfigManager.getHotseatMaxCountX()) ||
                (item.screen < -1) ||
                (item.cellX >= ConfigManager.getHotseatMaxCountX()) ||
                (item.cellX < -1)) {
                return true;
            }
        }
        else if (item.container == Favorites.CONTAINER_HIDESEAT) {
            if ((item.screen >= ConfigManager.getHideseatScreenMaxCount()) ||
                (item.screen < 0) ||
                (item.cellX >= ConfigManager.getHideseatMaxCountX()) ||
                (item.cellX < -1) ||
                (item.cellY >= ConfigManager.getHideseatMaxCountY()) ||
                (item.cellY < -1)) {
                return true;
            }
        }
        else if (item.container >= 0) {
            if ((item.cellX >= ConfigManager.getFolderMaxCountX()) ||
                (item.cellX < -1) ||
                (item.cellY >= ConfigManager.getFolderMaxCountY()) ||
                (item.cellY < -1)) {
                return true;
            }
        }
        else {
            return true;
        }

        return false;
    }

    private static void recoverInvalidPostionItems(ArrayList<ItemInfo> invalidItems,
                                            ArrayList<ItemInfo> needBindItems,
                                            HashMap<Long, FolderInfo> needBindFolderItems,
                                            ArrayList<ItemInfo> needUpdateItems,
                                            ArrayList<ItemInfo> needRemoveViewItems) {
        ArrayList<ItemInfo> noSpaceItemsList = new ArrayList<ItemInfo>();
        ArrayList<ItemInfo> removeList = new ArrayList<ItemInfo>();

        for (ItemInfo invalidItem: invalidItems) {
            if (invalidItem == null) {
                continue;
            }
            Log.d(TAG, "invalidItem id is " + invalidItem.id);

            ScreenPosition newPos = findEmptyCell(ConfigManager.DEFAULT_FIND_EMPTY_SCREEN_START,
                                                                    invalidItem.spanX, invalidItem.spanX);
            if (newPos != null) {
                Log.d(TAG, "find a new position in workspace");
                invalidItem.container = Favorites.CONTAINER_DESKTOP;
                invalidItem.screen = newPos.s;
                invalidItem.cellX = newPos.x;
                invalidItem.cellY = newPos.y;
                needBindItems.add(invalidItem);
                //find new position, the item is a workspace items
                synchronized (sBgLock) {
                    if (sBgWorkspaceItems.contains(invalidItem) == false) {
                        sBgWorkspaceItems.add(invalidItem);
                    }
                }
            } else {
                Log.d(TAG, "no space for the invalid item " + invalidItem.id);
                if ((invalidItem.itemType == Favorites.ITEM_TYPE_GADGET) ||
                    (invalidItem.itemType == Favorites.ITEM_TYPE_APPWIDGET) ||
                    (invalidItem.itemType == Favorites.ITEM_TYPE_ALIAPPWIDGET)) {
                    //no space for these items, just remove them
                    removeList.add(invalidItem);
                } else if (invalidItem.itemType == Favorites.ITEM_TYPE_FOLDER) {
                    //since no space for folder, there has no space for items in the folder too
                    //delet the folder and find new folder for items in it
                    for(ItemInfo itemInFolder: ((FolderInfo)invalidItem).contents) {
                        noSpaceItemsList.add(itemInFolder);
                    }
                    removeList.add(invalidItem);
                } else {
                    //find or create a folder for these nospace items
                    noSpaceItemsList.add(invalidItem);
                }
            }
        }

        if (removeList.size() > 0) {
            //remove no space widget
            for (ItemInfo wItem: removeList) {
                deleteItemFromDatabase(LauncherApplication.getContext(), wItem);
            }
            removeList.clear();
        }

        if (noSpaceItemsList.size() <= 0) {
            return;
        }

        //create current layout data
        //screen + 1 for hotseat, + 6 for hideseat
        /* HIDESEAT_SCREEN_NUM_MARKER: see ConfigManager.java */
        final ItemInfo occupied[][][] = new ItemInfo[mMaxScreenCount + 1 + ConfigManager.getHideseatScreenMaxCount()][mCellCountX + 1][mCellCountY + 1];
        createCurrentLayoutData(occupied);

        //Put all noSpaceItems in a new folder.
        //find a single item in workspace
        //put no space items and the single item in a new folder
        //in the single item's position.
        //If no empty position or single item found, I have to leave
        //these nospace item in -1 state.
        FolderInfo newfolder = null;

        for (ItemInfo noSpaceItem: noSpaceItemsList) {
            if ((newfolder == null) || (newfolder.contents.size() >= ConfigManager.getFolderMaxItemsCount())) {
                //no empty for newfolder, find a item in workspace and create a new folder at the item's position
                ItemInfo firstSingleItem = null;
                for (int s = ConfigManager.getScreenMaxCount() - 1; s > 0; s--) {
                    for (int y = ConfigManager.getCellMaxCountY() -1; y >= 0; y--) {
                        for (int x = ConfigManager.getCellMaxCountX() -1; x >= 0; x--) {
                            ItemInfo item = occupied[s][x][y];
                            if (item != null) {
                                if ((item.itemType == Favorites.ITEM_TYPE_APPLICATION) ||
                                    (item.itemType == Favorites.ITEM_TYPE_VPINSTALL) ||
                                    (item.itemType == Favorites.ITEM_TYPE_SHORTCUT_DOWNLOADING) ||
                                    (item.itemType == Favorites.ITEM_TYPE_SHORTCUT)) {
                                    firstSingleItem = item;
                                    break;
                                }
                            }
                        }
                        if (firstSingleItem != null) {
                            break;
                        }
                    }
                    if (firstSingleItem != null) {
                        break;
                    }
                }
                if (firstSingleItem != null) {
                    newfolder = new FolderInfo();
                    newfolder.container = Favorites.CONTAINER_DESKTOP;
                    newfolder.screen = firstSingleItem.screen;
                    newfolder.cellX = firstSingleItem.cellX;
                    newfolder.cellY = firstSingleItem.cellY;
                    newfolder.title = firstSingleItem.title + LauncherApplication.getContext().getResources().getString(R.string.folder_name_etc);

                    addItemToDatabase(LauncherApplication.getContext(), newfolder, newfolder.container, newfolder.screen,
                                                 newfolder.cellX, newfolder.cellY, false);
                    Log.d(TAG, "the newfolder id is " + newfolder.id);
                    synchronized (sBgLock) {
                        sBgFolders.put(newfolder.id, newfolder);
                        sBgItemsIdMap.put(newfolder.id, newfolder);
                        if (!sBgWorkspaceItems.contains(newfolder)) {
                            sBgWorkspaceItems.add(newfolder);
                        }
                    }

                    //put the firstSingleItem in newfolder;
                    newfolder.contents.add((ShortcutInfo)firstSingleItem);
                    firstSingleItem.container = newfolder.id;
                    firstSingleItem.screen = 0;
                    firstSingleItem.cellX = 0;
                    firstSingleItem.cellY = 0;
                    synchronized (sBgLock) {
                        if (sBgWorkspaceItems.contains(firstSingleItem)) {
                            sBgWorkspaceItems.remove(firstSingleItem);
                        }
                    }
                    //put the no space item in newfolder
                    newfolder.contents.add((ShortcutInfo)noSpaceItem);
                    noSpaceItem.container = newfolder.id;
                    noSpaceItem.screen = 0;
                    //the no space item is the second item in newfolder
                    //so x and y is 0
                    noSpaceItem.cellX = 1;
                    noSpaceItem.cellY = 0;
                    if (noSpaceItem.itemType == Favorites.ITEM_TYPE_NOSPACE_APPLICATION) {
                        noSpaceItem.itemType = Favorites.ITEM_TYPE_APPLICATION;
                    }
                    occupied[newfolder.screen][newfolder.cellX][newfolder.cellY] = newfolder;

                    needBindItems.add(newfolder);
                    needBindFolderItems.put(newfolder.id, newfolder);
                    needRemoveViewItems.add(firstSingleItem);
                    needUpdateItems.add(noSpaceItem);
                    needUpdateItems.add(firstSingleItem);
                } else {
                    //no single item in workspace, my god
                    //remove no app items and set app item as no space item
                    if ((noSpaceItem.itemType == Favorites.ITEM_TYPE_APPLICATION) ||
                        (noSpaceItem.itemType == Favorites.ITEM_TYPE_NOSPACE_APPLICATION)){
                        noSpaceItem.itemType = Favorites.ITEM_TYPE_NOSPACE_APPLICATION;
                        noSpaceItem.container = Favorites.CONTAINER_DESKTOP;
                        noSpaceItem.screen = -1;
                        noSpaceItem.cellX = -1;
                        noSpaceItem.cellY = -1;
                        updateItemInDatabase(LauncherApplication.getContext(), noSpaceItem);
                    } else {
                        deleteItemFromDatabase(LauncherApplication.getContext(), noSpaceItem);
                    }
                }
            } else {
                //put the no space item in newfolder
                newfolder.contents.add((ShortcutInfo)noSpaceItem);
                noSpaceItem.container = newfolder.id;
                noSpaceItem.screen = 0;
                noSpaceItem.cellX = newfolder.contents.size() % ConfigManager.getFolderMaxCountY();
                noSpaceItem.cellY = newfolder.contents.size() / ConfigManager.getFolderMaxCountY();
                if (noSpaceItem.itemType == Favorites.ITEM_TYPE_NOSPACE_APPLICATION) {
                    noSpaceItem.itemType = Favorites.ITEM_TYPE_APPLICATION;
                }
                needUpdateItems.add(noSpaceItem);
            }
        }
    }

    static Runnable mCheckInvalidPosItemsRunnable = new Runnable() {
        public void run() {
            try {
                findAndFixInvalidItems();
                checkAndFixOverlapItems();
            } catch (Exception ex) {
                Log.e(TAG, "findAndFixInvalidItems exception");
            }
        }
    };

    public static int calcEmptyCell(int startScreen) {
        Log.d(TAG, "calcEmptyCell in");
        int xCount = LauncherModel.getCellCountX();
        int yCount = LauncherModel.getCellCountY();
        int scrnCount = mMaxScreenCount;
        int ecCount = 0;

        boolean[][][] occupied = new boolean[scrnCount][xCount][yCount];
        initOccupied(occupied);
         // the main screen (index = 0) is special case, installed application and shotcuts will not be on it.
        for (int scr = startScreen; scr < scrnCount; scr++) {
            for (int x = 0; x < xCount; x++) {
                for (int y = 0; y < yCount; y++) {
                    if (occupied[scr][x][y] == false) {
                        ecCount++;
                    }
                }
            }
        }
        Log.d(TAG, "calcEmptyCell out");
        return ecCount;
    }

    public static void getEmptyPosList(ArrayList<ScreenPosition> posList, int startScreen, int endScreen) {
        Log.d(TAG, "getEmptyPosList in");
        int xCount = LauncherModel.getCellCountX();
        int yCount = LauncherModel.getCellCountY();
        int scrnCount = mMaxScreenCount;
        if (posList == null) {
            posList = new ArrayList<ScreenPosition>();
        }
        if (startScreen < 0) {
            startScreen = 0;
        } else if (startScreen >= mMaxScreenCount) {
            startScreen = mMaxScreenCount - 1;
        }
        if (endScreen < 0) {
            endScreen = 0;
        } else if (endScreen >= mMaxScreenCount) {
            endScreen = mMaxScreenCount - 1;
        }
        Log.d(TAG, "start Screen is " + startScreen);
        Log.d(TAG, "end Screen is " + endScreen);
        boolean[][][] occupied = new boolean[scrnCount][xCount][yCount];
        initOccupied(occupied);
        if (startScreen < endScreen) {
            Log.d(TAG, "start less than end");
            for (int scr = startScreen; scr <= endScreen; scr++) {
                for (int y = 0; y < yCount; y++) {
                    for (int x = 0; x < xCount; x++) {
                        if (occupied[scr][x][y] == false) {
                            posList.add(new ScreenPosition(scr, x, y));
                        }
                    }
                }
            }
        } else {
            Log.d(TAG, "start equal or large than end");
            for (int scr = startScreen; scr >= endScreen; scr--) {
                for (int y = 0; y < yCount; y++) {
                    for (int x = 0; x < xCount; x++) {
                        if (occupied[scr][x][y] == false) {
                            posList.add(new ScreenPosition(scr, x, y));
                        }
                    }
                }
            }
        }
        Log.d(TAG, "getEmptyPosList out");
    }

    public AppDownloadManager getAppDownloadManager(){
        return mAppDownloadMgr;
    }

    public static void checkAndFixOverlapItems(){
        Log.d(TAG, "checkAndFixOverlapItems in");
        int screen;
        int x;
        int y;
        int spanX;
        int spanY;
        int xCount = mCellCountX;
        int yCount = mCellCountY;
        int scrnCount = mMaxScreenCount;
        boolean isOccupied = false;
        boolean[][][] occupied = new boolean[scrnCount][xCount][yCount];

        ArrayList<ItemInfo> overlapItems = new ArrayList<ItemInfo>();
        final ArrayList<ItemInfo> updateViewItems = new ArrayList<ItemInfo>();
        final ArrayList<ItemInfo> removeItems = new ArrayList<ItemInfo>();
        final ArrayList<ItemInfo> removeViewItems = new ArrayList<ItemInfo>();

        final ArrayList<ItemInfo> tmpWorkspaceItems = new ArrayList<ItemInfo>();
        final ArrayList<ItemInfo> tmpAppWidgets = new ArrayList<ItemInfo>();
        synchronized (sBgLock) {
            tmpWorkspaceItems.addAll(sBgWorkspaceItems);
            tmpAppWidgets.addAll(sBgAppWidgets);
        }
        for (ItemInfo info : tmpWorkspaceItems) {
            isOccupied = false;
            if (info.container == LauncherSettings.Favorites.CONTAINER_DESKTOP) {
                screen = info.screen;
                if (!(screen > -1 && screen < scrnCount)) {
                    continue;
                }

                x = info.cellX;
                y = info.cellY;
                spanX = info.spanX;
                spanY = info.spanY;
                if ((x >= mCellCountX) ||
                    (y >= mCellCountY) ||
                    (x + spanX > mCellCountX) ||
                    (y + spanY > mCellCountY) ||
                    (x < 0) || (y < 0)) {
                    Log.d(TAG, "initOccupied item position error " + info.id);
                    continue;
                }

                for (int i = 0; i < spanX; i++) {
                    for (int j = 0; j < spanY; j++) {
                        if (occupied[screen][x + i][y + j] == true) {
                            Log.d(TAG, "item is overlap " +info.id);
                            overlapItems.add(info);
                            isOccupied = true;
                            break;
                        } else {
                            occupied[screen][x + i][y + j] = true;
                        }
                    }
                    if (isOccupied == true) {
                        break;
                    }
                }
            }
        }

        //find out the overlap items in sBgAppWidgets
        for (ItemInfo info : tmpAppWidgets) {
            isOccupied = false;
            if (info.container == LauncherSettings.Favorites.CONTAINER_DESKTOP) {
                screen = info.screen;
                x = info.cellX;
                y = info.cellY;
                spanX = info.spanX;
                spanY = info.spanY;
                if ((x >= mCellCountX) ||
                    (y >= mCellCountY) ||
                    (x + spanX > mCellCountX) ||
                    (y + spanY > mCellCountY) ||
                    (x < 0) || (y < 0) ||
                    (screen < 0) || (screen >= mMaxScreenCount)) {
                    Log.d(TAG, "initOccupied item position error " + info.id);
                    continue;
                }
                for (int i = 0; i < spanX; i++) {
                    for (int j = 0; j < spanY; j++) {
                        if (occupied[screen][x + i][y + j] == true) {
                            Log.d(TAG, "item is overlap " +info.id);
                            overlapItems.add(info);
                            isOccupied = true;
                            break;
                        } else {
                            occupied[screen][x + i][y + j] = true;
                        }
                    }
                    if (isOccupied == true) {
                        break;
                    }
                }
            }
        }

        if (overlapItems.size() == 0) {
            return;
        }

        for (ItemInfo info: overlapItems) {
            //find new position for overlap item
            ScreenPosition pos = findEmptyCell(1, info.spanX, info.spanY);
            if (pos == null) {
                //if no position, change app type to no-space app
                //and delete other type items
                if (info.itemType == Favorites.ITEM_TYPE_APPLICATION) {
                    info.container = Favorites.CONTAINER_DESKTOP;
                    info.screen = -1;
                    info.cellX = -1;
                    info.cellY = -1;
                    info.itemType = Favorites.ITEM_TYPE_NOSPACE_APPLICATION;
                    updateItemInDatabase(LauncherApplication.getContext(), info);
                    removeViewItems.add(info);
                } else {
                    if (info.itemType == Favorites.ITEM_TYPE_FOLDER) {
                        //change app type to no-space app
                        //and delete other type items in the folder
                        FolderInfo folder = (FolderInfo)info;
                        if (folder.contents != null) {
                            for (ItemInfo iteminfolder: folder.contents) {
                                if (iteminfolder.itemType == Favorites.ITEM_TYPE_APPLICATION) {
                                    iteminfolder.container = Favorites.CONTAINER_DESKTOP;
                                    iteminfolder.screen = -1;
                                    iteminfolder.cellX = -1;
                                    iteminfolder.cellY = -1;
                                    iteminfolder.itemType = Favorites.ITEM_TYPE_NOSPACE_APPLICATION;
                                    updateItemInDatabase(LauncherApplication.getContext(), iteminfolder);
                                    removeViewItems.add(iteminfolder);
                                } else {
                                    removeItems.add(iteminfolder);
                                }
                            }
                        }
                    }
                    removeItems.add(info);
                }
            } else {
                info.container = Favorites.CONTAINER_DESKTOP;
                info.screen = pos.s;
                info.cellX = pos.x;
                info.cellY = pos.y;
                updateItemInDatabase(LauncherApplication.getContext(), info);
                updateViewItems.add(info);
            }
        }

        final Callbacks callbacks = mCallbacks != null ? mCallbacks.get() : null;
        if (callbacks != null) {
            final Runnable r = new Runnable() {
                @Override
                public void run() {
                    callbacks.bindItemsRemoved(removeItems);
                    callbacks.bindItemsViewRemoved(removeViewItems);
                    callbacks.bindWorkspaceItemsViewMoved(updateViewItems);
                }
            };
            runOnMainThread(r);
        }

        overlapItems.clear();
        Log.d(TAG, "checkAndFixOverlapItems out");
    }
    public boolean isWorkspaceLoaded() {
        return mWorkspaceLoaded;
    }

    public void removeComponentFormAllAppList(String pkgName) {
        final String packageName = pkgName;
        final Runnable r = new Runnable(){
            @Override
            public void run() {
                mBgAllAppsList.removePackageFromData(packageName);
            }
        };
        runOnWorkerThread(r);
    }


    public static void startLoadAppGroupInfo(long delayTime) {
        final AppGroupManager manager = AppGroupManager.getInstance();
        manager.setStatus(true);
        sWorker.postDelayed(new Runnable() {

            @Override
            public void run() {
                manager.loadAppGroupInfosFromServer();
            }
        }, delayTime);
    }

	public void moveIconsToWorkspace() {
        ShortcutAndWidgetContainer container = mApp.getLauncher().getHotseat()
                .getLayout().getShortcutAndWidgetContainer();
        if (container.getChildCount() > 3) {
            Log.d(TAG,
                    "move more 3 in hotseat to workspace!The amount is:"
                            + (container.getChildCount() - 3));
            List<ScreenPosition> posList = findEmptyCells(container
                    .getChildCount() - 3);
            for (int i = 3; i < container.getChildCount(); i++) {
                ScreenPosition pos = posList.get(i - 3);
                ItemInfo item = (ItemInfo) container.getChildAt(i).getTag();
                item.container = LauncherSettings.Favorites.CONTAINER_DESKTOP;
                item.screen = pos.s;
                item.cellX = pos.x;
                item.cellY = pos.y;
            }
        }
    }
    private void moveToFolderNextPos(ScreenPosition pos) {
        int maxX = ConfigManager.getFolderMaxCountX();
        int maxY = ConfigManager.getFolderMaxCountY();
        if (pos.x < maxX - 1) {
            pos.x = pos.x + 1;
        } else if (pos.x == maxX - 1) {
            if (pos.y < maxY - 1) {
                pos.y = pos.y + 1;
                pos.x = 0;
            } else if (pos.y == maxY - 1) {
                pos.s = pos.s + 1;
                pos.x = pos.y = 0;
            } else {
                Log.d(AgedModeUtil.TAG, "error favorites xml");
            }
        } else {
            Log.d(AgedModeUtil.TAG, "error favorites xml");
        }
    }

    public void switchDbForAgedMode(boolean agedMode) {
        SwitchDB sw = new SwitchDB(agedMode);
        sWorker.post(sw);
    }

    public PackageUpdateTaskQueue getPackageUpdateTaskQueue() {
        return mPackageUpdateTaskQueue;
    }

    public static final class PackageUpdateTaskQueue {

        private static final String TAG = "PackageUpdateTaskQueue";

        // the fields below need to be synchronized to "this"
        private final List<PackageUpdatedTask> mQueue = new ArrayList<PackageUpdatedTask>();
        private int mLock = 0; // suspend tasks when mLock > 0

        private PackageUpdateTaskQueue() {
        }

        public synchronized void reset() {
            mQueue.clear();
            mLock = 0;
        }

        /**
         * When this method get called, <code>LauncherModel</code> will suspend
         * incoming package add/remove tasks, until the last {@link #releaseLock()}
         * get called. Method {@link #retainLock(String)} and {@link #releaseLock()}
         * must be called in pairs.<p/>This method is thread-safe.
         * @param tag used to print log
         */
        public synchronized void retainLock(String tag) {
            mLock++;
            Log.d(TAG, String.format("retainLock: count=%d tag=%s", mLock, tag));
        }

        /**
         * See {@link #retainLock(String)}.<p/>
         * This method is thread-safe.
         */
        public void releaseLock() {
            final List<PackageUpdatedTask> copiedTasks = new ArrayList<PackageUpdatedTask>(0);
            synchronized (this) {
                mLock--;
                Log.d(TAG, String.format("releaseLock: count=%d", mLock));
                if (mLock < 0) {
                    Log.e(TAG, "releaseLock: count is negative");
                    mLock = 0;
                }
                // if mLock is down to zero, execute all tasks in mQueue
                if (mLock == 0 && !mQueue.isEmpty()) {
                    copiedTasks.addAll(mQueue);
                    mQueue.clear();
                }
            }

            if (!copiedTasks.isEmpty()) {
                Log.d(TAG, String.format("releaseLock: prepare to run %d tasks", copiedTasks.size()));
                final int[] index = new int[] { 0 }; // Use an array to wrap the index so it
                                                     // can be modified in a runnable.
                final PackageUpdatedTaskListener callback = new PackageUpdatedTaskListener() {
                    @Override
                    public void onPackageUpdatedTaskFinished(PackageUpdatedTask task) {
                        // when the current task is done, schedule the next.
                        task.setListener(null);
                        if (index[0] < copiedTasks.size() - 1) {
                            index[0]++;
                            Log.d(TAG, String.format("releaseLock: run task #" + index[0]));
                            PackageUpdatedTask nextTask = copiedTasks.get(index[0]);
                            nextTask.setListener(this);
                            sWorker.post(nextTask);
                        } else {
                            copiedTasks.clear();
                            Log.d(TAG, String.format("releaseLock: all tasks done"));
                        }
                    }
                };
                // start the first task
                Log.d(TAG, String.format("releaseLock: run task #" + index[0]));
                PackageUpdatedTask firstTask = copiedTasks.get(index[0]);
                firstTask.setListener(callback);
                sWorker.post(firstTask);
            }
        }

        private synchronized void enqueue(PackageUpdatedTask task) {
            if (mLock > 0 &&
                    (task.mOp == PackageUpdatedTask.OP_ADD ||
                     task.mOp == PackageUpdatedTask.OP_REMOVE)) {
                // task needs to wait in queue until releaseLock() is called
                Log.d(TAG, "enqueue: task=" + task);
                mQueue.add(task);
            } else {
                // normal situation, schedule to worker thread immediately
                sWorker.post(task);
            }
        }
    }

  //topwise zyf add for fixedfolder
    ItemInfo getItemInfoById(Context context, int id) {
        final ContentResolver cr = context.getContentResolver();
        Cursor c = null;

        try {
        	c = cr.query(LauncherSettings.Favorites.CONTENT_URI, null,
                    "_id=? ",
                    new String[] { String.valueOf(id)}, null);
            
            if (c.moveToFirst()) {
                final int itemTypeIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.ITEM_TYPE);
                final int titleIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.TITLE);
                final int containerIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.CONTAINER);
                final int screenIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.SCREEN);
                final int cellXIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.CELLX);
                final int cellYIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.CELLY);

                ItemInfo itemInfo=new ItemInfo();
                itemInfo.id=id;
                itemInfo.itemType=c.getInt(itemTypeIndex);
                itemInfo.container=c.getInt(containerIndex);
                itemInfo.title=c.getString(titleIndex);
                itemInfo.screen= c.getInt(screenIndex);
                itemInfo.cellX = c.getInt(cellXIndex);
                itemInfo.cellY = c.getInt(cellYIndex);
                return itemInfo;
            }
        }
        catch(Exception ex)
        {
        	Log.e("zyflauncher", "getItemInfoById error : "+ex);
        }finally {
        	if(c!=null)
        		c.close();
        }

        return null;
    }
    private void fillExappList(Context context)
    {
    	try
    	{
    		if(sAlapks!=null)
    			sAlapks.clear();
    		ArrayList<FileAndInfo> alFileAndPkg=new ArrayList<FileAndInfo>();
    		sAlapks= getDirApkList(DESTPATH_UNZIP_APP,context,alFileAndPkg,FileAndInfo.EXAPP_TYPE_APP);
    		sAlapks= getDirApkList(DESTPATH_UNZIP_GAME,context,alFileAndPkg,FileAndInfo.EXAPP_TYPE_GAME);
    	}catch(Exception e)
    	{}
    }
    public static FileAndInfo getExappApk(String pkgname)
    {
        if(pkgname!=null)
        {
       	 FileAndInfo fi=getFileAndInfo(sAlapks,pkgname);
       	 if(fi!=null)
       	 {
       		 return fi;
       	 }
        }
    	return null;
    }
    private static FileAndInfo getFileAndInfo(ArrayList<FileAndInfo> list,String pkgname)
    {
    	if(list==null||list.size()<=0)
    		return null;
    	for(int i=0;i<list.size();i++)
    	{
    		FileAndInfo fi=list.get(i);
    		if(fi!=null&&fi.mPkgInfo.applicationInfo.packageName.equals(pkgname))
    		{
    			return fi;
    		}
    	}
    	return null;
    }
    private void fillFixedFoldersItems(Context context)
    {
    	sFixedFoldersItems.clear();
    	final ContentResolver cr = context.getContentResolver();
        Cursor c = null;

        try {
        	c = cr.query(LauncherSettings.Favorites.CONTENT_URI, null,
                    "itemType=? ",
                    new String[] { String.valueOf(LauncherSettings.Favorites.ITEM_TYPE_FIXED_FOLDER)}, null);
            
            while (c.moveToNext()) {
            	final int idIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites._ID);
                final int itemTypeIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.ITEM_TYPE);
                final int titleIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.TITLE);
                final int containerIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.CONTAINER);
                final int screenIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.SCREEN);
                final int cellXIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.CELLX);
                final int cellYIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.CELLY);
                final int intentIndex=c.getColumnIndexOrThrow(LauncherSettings.Favorites.INTENT);

                FolderInfo itemInfo=new FixedFolderInfo();
                itemInfo.id= c.getLong(idIndex);
                itemInfo.itemType=c.getInt(itemTypeIndex);
                itemInfo.container=c.getInt(containerIndex);
                itemInfo.title=c.getString(titleIndex);
                itemInfo.screen= c.getInt(screenIndex);
                itemInfo.cellX = c.getInt(cellXIndex);
                itemInfo.cellY = c.getInt(cellYIndex);
                String tempintent=c.getString(intentIndex);
                Log.d("zyfonline","tempintent = "+tempintent);
                if(tempintent!=null)
                {
                	if(tempintent.equals(LauncherSettings.Favorites.INTNET_APPS))
                	{
                		itemInfo.itemExtraType=ItemInfo.ITEM_EXTRA_TYPE_APPS;
                	}
                	else if(tempintent.equals(LauncherSettings.Favorites.INTNET_GAMES)) {
                		itemInfo.itemExtraType=ItemInfo.ITEM_EXTRA_TYPE_GAMES;
					}
                }
                sFixedFoldersItems.add(itemInfo);
            }
        }
        catch(Exception ex)
        {
        	Log.e("zyflauncher", "fillFixedFoldersItems error : "+ex);
        }finally {
        	if(c!=null)
        		c.close();
        }
    }
    private ItemInfo findFixedFolderInArr(long id)
    {
    	ItemInfo itemInfofind=null;
    	if(sFixedFoldersItems==null&&sFixedFoldersItems.size()<=0)
    		return null;
    	for(int i=0;i<sFixedFoldersItems.size();i++)
    	{
    		ItemInfo itemInfo=sFixedFoldersItems.get(i);
    		if(itemInfo.id==id)
    		{
    			itemInfofind=itemInfo;
    			break;
    		}
    	}
    	return itemInfofind;
    }
    public static ItemInfo findFixedFolderByTitle(String title)
    {
    	ItemInfo itemInfofind=null;
    	if(sFixedFoldersItems==null&&sFixedFoldersItems.size()<=0)
    		return null;
    	for(int i=0;i<sFixedFoldersItems.size();i++)
    	{
    		ItemInfo itemInfo=sFixedFoldersItems.get(i);
    		if(itemInfo.title.equals(title))
    		{
    			itemInfofind=itemInfo;
    			break;
    		}
    	}
    	return itemInfofind;
    }
    public static ItemInfo findFixedFolderByExtraType(int extratype)
    {
    	ItemInfo itemInfofind=null;
    	if(sFixedFoldersItems==null&&sFixedFoldersItems.size()<=0)
    		return null;
    	for(int i=0;i<sFixedFoldersItems.size();i++)
    	{
    		ItemInfo itemInfo=sFixedFoldersItems.get(i);
    		if(itemInfo.itemExtraType==extratype)
    		{
    			itemInfofind=itemInfo;
    			break;
    		}
    	}
    	return itemInfofind;
    }
    public static String getTitleByType(Context context,int type)
    {
    	String title=null;
    	switch(type)
    	{
	    	case FileAndInfo.EXAPP_TYPE_APP:
	    		title=context.getString(R.string.title_folder_recommend_app);
	    		break;
	    	case FileAndInfo.EXAPP_TYPE_GAME:
	    		title=context.getString(R.string.title_folder_games);
	    		break;
	    		default:
	    			break;
    	}
    	return title;
    }
    public static int getExtraTypeByType(int type)
    {
    	int extratype=0;
    	switch(type)
    	{
	    	case FileAndInfo.EXAPP_TYPE_APP:
	    		extratype=ItemInfo.ITEM_EXTRA_TYPE_APPS;
	    		break;
	    	case FileAndInfo.EXAPP_TYPE_GAME:
	    		extratype=ItemInfo.ITEM_EXTRA_TYPE_GAMES;
	    		break;
	    		default:
	    			break;
    	}
    	return extratype;
    }
    public static int getExtraTypeByResID(int resid)
    {
    	int extratype=0;
    	switch(resid)
    	{
	    	case LauncherProvider.APPS_FOLDER_RESID:
	    		extratype=ItemInfo.ITEM_EXTRA_TYPE_APPS;
	    		break;
	    	case LauncherProvider.GAMES_FOLDER_RESID:
	    		extratype=ItemInfo.ITEM_EXTRA_TYPE_GAMES;
	    		break;
	    		default:
	    			break;
    	}
    	return extratype;
    }
    private static final String LAST_NAME=".apk";
    private PackageInfo getApkInfo(String archiveFilePath,Context context)
    {
    	try{
         PackageManager pm = context.getPackageManager();    
         PackageInfo info = pm.getPackageArchiveInfo(archiveFilePath, PackageManager.GET_ACTIVITIES);    
         return info;
    	}catch(Exception e)
    	{
    		Log.e(TAG, "getApkInfo error : "+e);
    	}
         return null;
    }
    public static ArrayList<String> getDirFilerFiles(String strpath,String lastName)
    {
    ArrayList<String> list=new ArrayList<String>();
    try {
    	
    	File file=new File(strpath);
    	String fname="";
    	Log.e(TAG, "getDirFilerFiles file : "+file);
    	if(file.exists()&&file.isDirectory())
    	{
    		File[] fs= file.listFiles();
    		Log.e(TAG, "getDirFilerFiles fs : "+fs);
    		if(fs!=null&&fs.length>0)
    		{
    			for(int i=0;i<fs.length;i++)
    			{
    				fname=fs[i].getName();
    				if(fname.toLowerCase().endsWith(lastName))
    				{
    					list.add(fs[i].getAbsolutePath());
    				}
    			}
    		}
    	}
    } catch (Exception e) {
    				// TODO Auto-generated catch block
    				Log.e(TAG, "getDirFilerFiles error : "+e);
    			}  
    	return list;
    }
	 private ArrayList<FileAndInfo> getDirApkList(String strpath,Context context,ArrayList<FileAndInfo> alFileAndPkg,int type)
	    {
	    	ArrayList<String> alFiles=getDirFilerFiles(strpath,LAST_NAME);
	    	//ArrayList<FileAndInfo> alFileAndPkg=new ArrayList<FileAndInfo>();
	    	if(alFiles!=null&&alFileAndPkg!=null)
	    	{
		    	for(int i=0;i<alFiles.size();i++)
		    	{
		    		PackageInfo pkginfo=getApkInfo(alFiles.get(i),context);
		            if(pkginfo!=null)
		            {
		            	FileAndInfo fp=new FileAndInfo();
		            	fp.mFilePath=alFiles.get(i);
		            	fp.mPkgInfo=pkginfo;
		            	fp.mExappType=type;
		            	alFileAndPkg.add(fp);
		            }
		    	}
	    	}
	    	return alFileAndPkg;
	    }
	//zyf add for folderonline
	 public ShortcutInfo findItemInfoByPkgName(String pkgname)
	 {
		 
		 ShortcutInfo findShortcutInfo=null;
		 /*
		 if(sFixedFoldersItems!=null)
		 {
			 for(int i=0;i<sFixedFoldersItems.size();i++)
			 {
				 ShortcutInfo shortcutInfo=findItemInfoByPkgName(pkgname,
						 (FolderInfo)sFixedFoldersItems.get(i));
				 if(shortcutInfo!=null)
				 {
					 findShortcutInfo=shortcutInfo;
					 break;
				 }
			 }
		 }
		 else
		 {
			 return null;
		 }
		 */
		 if(mOnlineShortcutInfos!=null)
		 {
			 for(int i=0;i<mOnlineShortcutInfos.size();i++)
			 {
				 ShortcutInfo shorcutinfo=mOnlineShortcutInfos.get(i);
				 Intent intent = shorcutinfo.intent;
	             if (intent == null) {
	                 continue;
	             }
	             if(intent.getAction().equals("com.tpw.online")||intent.getAction().equals("com.tpw.install"))
	             {
	            	String pkg=intent.getStringExtra(com.tpw.homeshell.vpinstall.VPUtils.TYPE_PACKAGENAME);
	            	if(pkgname.equals(pkg))
	            	{
	            		findShortcutInfo=shorcutinfo;
	            		break;
	            	}
	             }
			 }
		 }
		 return findShortcutInfo;
		 
		 
	 }
	 public String getOnlineApkFullPath(ApkInfo apkinfo)
	 {
		 String fullpath="";
		 if(apkinfo.mType==ItemInfo.ITEM_EXTRA_TYPE_APPS)//app
			{
				fullpath=ApkInfo.DESTPATH_UNZIP_APP+apkinfo.mFileName;
			}
			else if(apkinfo.mType==ItemInfo.ITEM_EXTRA_TYPE_GAMES)//game
 			{
				fullpath=ApkInfo.DESTPATH_UNZIP_GAME+apkinfo.mFileName;
 			}
		 return fullpath;
	 }
	 public ApkInfo getApkinfoByPkg(String pkg)
	 {
		if(mApkinfos==null)
			return null;
		ApkInfo info=null;
		for(int i=0;i<mApkinfos.size();i++)
		{
			ApkInfo apkinfo=mApkinfos.get(i);
			if(apkinfo.mPkgname.equals(pkg))
			{
				info=apkinfo;
				break;
			}
		}
		return info;
	 }
	 public ApkInfo getApkinfoByUrl(String url)
	 {
		if(mApkinfos==null)
			return null;
		ApkInfo info=null;
		for(int i=0;i<mApkinfos.size();i++)
		{
			ApkInfo apkinfo=mApkinfos.get(i);
			if(apkinfo.mUrl.equals(url))
			{
				info=apkinfo;
				break;
			}
		}
		return info;
	 }
	 public String getPkgnameByUrl(String url)
	 {
		if(mApkinfos==null)
			return null;
		String pkgname=null;
		for(int i=0;i<mApkinfos.size();i++)
		{
			ApkInfo apkinfo=mApkinfos.get(i);
			if(apkinfo.mUrl.equals(url))
			{
				pkgname=apkinfo.mPkgname;
				break;
			}
		}
		return pkgname;
	 }
	//zyf add end
	 public static ShortcutInfo findItemInfoByPkgName(String pkgname,FolderInfo folderInfo)
	 {
		 try
		 {
			 Log.d("zyfonline", "findItemInfoByPkgName : folderInfo = "+folderInfo);
			 Log.d("zyfonline", "findItemInfoByPkgName : folderInfo = "+folderInfo.contents.size());
			 for(int i=0;i<folderInfo.contents.size();i++)
			 {
				 ShortcutInfo si=folderInfo.contents.get(i);
				 Log.d("zyfonline", "findItemInfoByPkgName : si.intent.getAction() = "+si.intent.getAction());
				 if((si.intent!=null&&
						 (si.intent.getComponent()!=null)&&si.intent.getComponent().getPackageName().equals(pkgname))
					 ||(si.intent.getAction()!=null&&
					 (si.intent.getAction().equals("com.tpw.install")||si.intent.getAction().equals("com.tpw.online"))
					 &&si.intent.getStringExtra(com.tpw.homeshell.vpinstall.VPUtils.TYPE_PACKAGENAME).equals(pkgname)))
				 {
					 return si;
				 }
			 }
		 }catch(Exception e)
		 {
			 
		 }
		 return null;
	 }
	 public void clearExapp(Context context,int resid,int type)
	 {
		 try
		 {
         	int extratype=getExtraTypeByResID(resid);
         	
         	ItemInfo itmeinfo= findFixedFolderByExtraType(extratype);
         	FolderInfo folderInfo =findOrMakeFixedFolder(sBgFolders, itmeinfo.id);
         	int children_count= folderInfo.contents.size();
         	if(folderInfo!=null&&children_count>0)
             {
         		ArrayList<ShortcutInfo> remover_contents = new ArrayList<ShortcutInfo>();
             	for(int i=0;i<children_count;i++)
                 {
             		ShortcutInfo shortcutInfo =folderInfo.contents.get(i);
             		Intent intent=shortcutInfo.intent;
             		if(intent!=null&&intent.getAction()!=null&&intent.getAction().equals("com.tpw.install"))
             		{
             			remover_contents.add(shortcutInfo);
             		}
                 }
             	for(int i=0;i<remover_contents.size();i++)
             	{
             		ShortcutInfo shortcutInfo =remover_contents.get(i);
             		folderInfo.contents.remove(shortcutInfo);
             		folderInfo.remove(shortcutInfo);
             		sApkItemsMap.remove(shortcutInfo);
             	}
             }
		 }catch(Exception e)
		 {
			 Log.e("zyflauncher","clearExapp() error : "+e);
		 }
	 }
	 public void addExapp(Context context,int resid,int type)
	 {
		 try
		 {
         if(sAlapks.size()>0)
         {
         	PackageManager packageManager =context.getPackageManager();
         	int extratype=getExtraTypeByResID(resid);
         	
         	ItemInfo itmeinfo= findFixedFolderByExtraType(extratype);
         	FolderInfo folderInfo =findOrMakeFixedFolder(sBgFolders, itmeinfo.id);
         	int children_count= folderInfo.contents.size();
         	int maxCountX = ConfigManager.getFolderMaxCountX();
 	        int maxCountY = ConfigManager.getFolderMaxCountY();
         	if(itmeinfo!=null&&children_count>=0)//add for hide folder
             {
             	for(int i=0;i<sAlapks.size();i++)
                 {
             		FileAndInfo fai =sAlapks.get(i);
             		PackageInfo pkginfo=null;
             		if(fai!=null)
             			pkginfo=fai.mPkgInfo;
             		if(fai.mExappType!=type)
             			continue;
                 	if(pkginfo!=null)
                 	{    
                 		android.content.pm.ApplicationInfo applicationInfo=pkginfo.applicationInfo;
                 		if(applicationInfo!=null)
                 		{

                 			//去除重复
                 			if(checkAlreadInFolder(pkginfo,folderInfo))
                 			{
                 				continue;
                 			}
                 			ShortcutInfo shortcutInfo = new ShortcutInfo();
                 			fillExappApkIconAndTitle(context,fai,mApp,shortcutInfo);
                 			Log.d("zyflauncher","addExapp() 111111111");
                 			Intent noinstallIntent=new Intent("com.tpw.install");

                 			noinstallIntent.putExtra(com.tpw.homeshell.vpinstall.VPUtils.TYPE_PACKAGEPATH,fai.mFilePath);
                 			noinstallIntent.putExtra(com.tpw.homeshell.vpinstall.VPUtils.TYPE_PACKAGENAME,fai.mPkgInfo.applicationInfo.packageName);

                 			if(!Utilities.isApkInstalled(fai.mPkgInfo.applicationInfo.packageName,context))
                 			{
                 				shortcutInfo.messageNum=1;
                 				shortcutInfo.setIsNewItem(true);
                 			}
                 			else
                 			{
                 				shortcutInfo.messageNum=0;
                 				shortcutInfo.setIsNewItem(false);
                 			}
                 			
                 			shortcutInfo.intent =noinstallIntent;
                 			shortcutInfo.container =itmeinfo.id;
                 			Log.d("zyflauncher","addExapp() 222222");
                 			int temp_count=children_count+1;

                 			int pageindex=0;
                 			int pagemaxcount=maxCountX*maxCountY;
                 			if(temp_count%pagemaxcount==0)
                 			{
                 				pageindex=temp_count/pagemaxcount-1;
                 			}
                 			else {
                 				pageindex=temp_count/pagemaxcount;
							}
                 			shortcutInfo.screen =pageindex;
                 			temp_count=temp_count%pagemaxcount;
                 			Log.d("zyflauncher","addExapp() 3333");
                 	        
                 			if(temp_count%maxCountX==0)
                 			{
                 				shortcutInfo.cellX = maxCountX-1;
                 				if(temp_count==0)
                 				{
                 					shortcutInfo.cellY =maxCountY-1;
                 				}
                 				else 
                 				{
                 					shortcutInfo.cellY = temp_count/maxCountX-1;
								}	
                 			}
                 			else
                 			{
                 				shortcutInfo.cellX = temp_count%maxCountX-1;
                 				shortcutInfo.cellY = temp_count/maxCountX;
                 			}
                 			children_count++;
                 			
                            folderInfo.add(shortcutInfo);
                            Log.d("zyflauncher","addExapp() 4444");
                            sApkItemsMap.put(fai.mPkgInfo.applicationInfo.packageName, shortcutInfo);
                 		}
                 	}
                 }
             }
         }
		 }catch(Exception e)
		 {
			 Log.e("zyflauncher","addExapp() error : "+e);
		 }
	 }
	 //topwise zyf add for folderonline
	 public void clearOnlineApp(Context context,int resid,int type)
	 {
		 try
		 {
         	int extratype=getExtraTypeByResID(resid);
         	
         	ItemInfo itmeinfo= findFixedFolderByExtraType(extratype);
         	FolderInfo folderInfo =findOrMakeFixedFolder(sBgFolders, itmeinfo.id);
         	int children_count= folderInfo.contents.size();
         	if(folderInfo!=null&&children_count>0)
             {
         		ArrayList<ShortcutInfo> remover_contents = new ArrayList<ShortcutInfo>();
             	for(int i=0;i<children_count;i++)
                 {
             		ShortcutInfo shortcutInfo =folderInfo.contents.get(i);
             		Intent intent=shortcutInfo.intent;
             		if(intent!=null&&intent.getAction()!=null&&intent.getAction().equals("com.tpw.online"))
             		{
             			remover_contents.add(shortcutInfo);
             		}
                 }
             	for(int i=0;i<remover_contents.size();i++)
             	{
             		ShortcutInfo shortcutInfo =remover_contents.get(i);
             		folderInfo.contents.remove(shortcutInfo);
             		folderInfo.remove(shortcutInfo);
             		mOnlineShortcutInfos.remove(shortcutInfo);
             	}
             }
		 }catch(Exception e)
		 {
			 Log.e("zyflauncher","clearOnlineApp() error : "+e);
		 }
	 }
	 public void addOnlineApp(Context context)
	 {
		 try
		 {
		 if(mOnlineShortcutInfos!=null)
			 mOnlineShortcutInfos.clear();
		 mOnlineShortcutInfos=new ArrayList<ShortcutInfo>();
		 //判断当前数据是否已加载进数据库了
		int version= -1;//SystemProperties.getInt("persist.sys.fol.xml.ver", -1);
		if(version==-1)
			return;
		Log.d("zyfonline","addOnlineApp() version = "+version);
		DBOperate dboperate=new DBOperate(context);
		List<ApkInfo> apkinfos=dboperate.getInfos(version);
		Log.d("zyfonline","addOnlineApp() ... apkinfos.size() = "+apkinfos.size());
		if(apkinfos==null||apkinfos.size()<=0)
		{
			Log.d("zyfonline","FOLDER_ONLINE_XML_PATH = "+ApkInfo.FOLDER_ONLINE_XML_PATH);
			String fname=getFolderonlineFilePath(ApkInfo.FOLDER_ONLINE_XML_PATH,PRENAME,LASTNAME);
			Log.d("zyfonline","fname = "+fname);
			apkinfos=getApkInfoListFromFile(fname);
			Log.d("zyfonline","apkinfos = "+apkinfos);
			dboperate.saveApkInfos(version,apkinfos);
			dboperate.deleteOtherVersion(version);//删除其它的版本信息
		}
		dboperate.closeDb();
		
		mApkinfos=apkinfos;
		 
		 Log.d("zyfonline","addOnlineApp() apkinfos.size() = "+apkinfos.size());
		 if(apkinfos==null||apkinfos.size()<=0)
		 {
			 return;
		 }
		 
      	 int maxCountX = ConfigManager.getFolderMaxCountX();
	     int maxCountY = ConfigManager.getFolderMaxCountY();
      	
		 ItemInfo itmeinfo_app= findFixedFolderByExtraType(ItemInfo.ITEM_EXTRA_TYPE_APPS);
      	 ItemInfo itmeinfo_game= findFixedFolderByExtraType(ItemInfo.ITEM_EXTRA_TYPE_GAMES);
      	 Log.d("zyfonline","itmeinfo_app = "+itmeinfo_app+" , itmeinfo_game = "+itmeinfo_game);
      	 
      	 
      	 
      	 FolderInfo folderInfo_app =null;
     	 FolderInfo folderInfo_game =null;
     	 int children_count_app= 0;
      	 int children_count_game= 0;
      	 
      	 if(itmeinfo_app!=null)
      	 {
      		 folderInfo_app =findOrMakeFixedFolder(sBgFolders, itmeinfo_app.id);
      		 children_count_app= folderInfo_app.contents.size();
      	 }
      	if(itmeinfo_game!=null)
      	{
      		folderInfo_game =findOrMakeFixedFolder(sBgFolders, itmeinfo_game.id);
      		children_count_game= folderInfo_game.contents.size();
      	}
      	
			Log.d("zyfonline","folderInfo_app = "+folderInfo_app
  					+" , folderInfo_game = "+folderInfo_game);

		 for(int i=0;i<apkinfos.size();i++)
		 {
			 ApkInfo apkinfo=apkinfos.get(i);
			//去除重复
			if(checkAlreadInFolder(apkinfo,folderInfo_app,folderInfo_game))
			{
				continue;
			}
			//将已下载和已安装的APK过滤掉
			 if(ExappUtil.isFileExists(DESTPATH_UNZIP_APP+apkinfo.mFileName)||
						ExappUtil.isFileExists(DESTPATH_UNZIP_GAME+apkinfo.mFileName)||
						Utilities.isApkInstalled(apkinfo.mPkgname,mApp))
			 {
				 continue;
			 }
			 
			 ShortcutInfo shortcutInfo = new ShortcutInfo();
			 shortcutInfo.setIcon(getOnlineIcon(apkinfo.mIconData));
			 shortcutInfo.title=apkinfo.mApkName;
			 int state=apkinfo.mState;
			 Log.d("zyfonline","addOnlineApp : state = "+state);
			 
			 shortcutInfo.messageNum=0;
			 shortcutInfo.setIsNewItem(false);
			 apkinfo.mIsNew=false;
			 switch(state)
			 {
				 case ApkInfo.APKINFO_STATE_ORI:
					 shortcutInfo.setAppDownloadStatus(AppDownloadStatus.STATUS_NO_DOWNLOAD);
					 shortcutInfo.messageNum=1;
      				 shortcutInfo.setIsNewItem(true);
      				 apkinfo.mIsNew=true;
					 break;
				 case ApkInfo.APKINFO_STATE_DOWNLOAD_WAIT:
					 shortcutInfo.setAppDownloadStatus(AppDownloadStatus.STATUS_WAITING);
					 sendDownloadBroadcast(apkinfo.mUrl,getOnlineApkFullPath(apkinfo));
					 break;
				 case ApkInfo.APKINFO_STATE_DOWNLOAD:
					 shortcutInfo.setAppDownloadStatus(AppDownloadStatus.STATUS_DOWNLOADING);
					 sendDownloadBroadcast(apkinfo.mUrl,getOnlineApkFullPath(apkinfo));
					 break;
				 case ApkInfo.APKINFO_STATE_PAUSE:
					 shortcutInfo.setAppDownloadStatus(AppDownloadStatus.STATUS_PAUSED);
					 sendDownloadBroadcast(apkinfo.mUrl,getOnlineApkFullPath(apkinfo));
					 break;
				 case ApkInfo.APKINFO_STATE_USER_PAUSE:
					 shortcutInfo.setAppDownloadStatus(AppDownloadStatus.STATUS_PAUSED);
					 break;
				 case ApkInfo.APKINFO_STATE_DOWNLOAD_COMPLETE:
					 //shortcutInfo.setAppDownloadStatus(AppDownloadStatus.STATUS_NO_DOWNLOAD);
					 break;
				 case ApkInfo.APKINFO_STATE_INSTALL:
					 shortcutInfo.setAppDownloadStatus(AppDownloadStatus.STATUS_INSTALLING);
					 break;
				 case ApkInfo.APKINFO_STATE_INSTALL_COMPLETE:
					 shortcutInfo.setAppDownloadStatus(AppDownloadStatus.STATUS_INSTALLED);
					 break;
					 default:
						 shortcutInfo.setAppDownloadStatus(AppDownloadStatus.STATUS_NO_DOWNLOAD);
						 break;
			 }

  			 Intent onlineIntent=new Intent("com.tpw.online");

  			onlineIntent.putExtra(ExappUtil.PACKAGENAME,apkinfo.mPkgname);
  			onlineIntent.putExtra(ExappUtil.DOWNLOAD_URL, apkinfo.mUrl);
  			
  			
  			//onlineIntent.putExtra(name, value)

  			shortcutInfo.intent =onlineIntent;
  			
  			if(apkinfo.mType==ItemInfo.ITEM_EXTRA_TYPE_APPS&&folderInfo_app!=null)//app
  			{
  				onlineIntent.putExtra(ExappUtil.SAVE_PATH, DESTPATH_UNZIP_APP+apkinfo.mFileName);

  				shortcutInfo.container =itmeinfo_app.id;
  				int temp_count=children_count_app+1;
  	  			int pageindex=0;
  	  			int pagemaxcount=maxCountX*maxCountY;
  	  			if(temp_count%pagemaxcount==0)
  	  			{
  	  				pageindex=temp_count/pagemaxcount-1;
  	  			}
  	  			else 
  	  			{
  	  				pageindex=temp_count/pagemaxcount;
  				}
  	  			shortcutInfo.screen =pageindex;
  	  			temp_count=temp_count%pagemaxcount;

  	  			if(temp_count%maxCountX==0)
  	  			{
  	  				shortcutInfo.cellX = maxCountX-1;
  	  				if(temp_count==0)
  	  				{
  	  					shortcutInfo.cellY =maxCountY-1;
  	  				}
  	  				else 
  	  				{
  	  					shortcutInfo.cellY = temp_count/maxCountX-1;
  					}	
  	  			}
  	  			else
  	  			{
  	  				shortcutInfo.cellX = temp_count%maxCountX-1;
  	  				shortcutInfo.cellY = temp_count/maxCountX;
  	  			}
  	  			children_count_app++;
					//for notify
					if(shortcutInfo.isNewItem())
					{
						mNewAppNum++;
					}
					//end
  	  			folderInfo_app.add(shortcutInfo);
  	  			mOnlineShortcutInfos.add(shortcutInfo);
  	  			sApkItemsMap.put(apkinfo.mPkgname, shortcutInfo);
  	  			Log.d("zyfonline","folderInfo_app add  shortcutInfo : "+shortcutInfo);
  			}
  			else if(apkinfo.mType==ItemInfo.ITEM_EXTRA_TYPE_GAMES&&folderInfo_game!=null)//game
  			{
  				onlineIntent.putExtra(ExappUtil.SAVE_PATH, DESTPATH_UNZIP_GAME+apkinfo.mFileName);
  				
  				shortcutInfo.container =itmeinfo_game.id;
  				int temp_count=children_count_game+1;
  	  			int pageindex=0;
  	  			int pagemaxcount=maxCountX*maxCountY;
  	  			if(temp_count%pagemaxcount==0)
  	  			{
  	  				pageindex=temp_count/pagemaxcount-1;
  	  			}
  	  			else 
  	  			{
  	  				pageindex=temp_count/pagemaxcount;
  				}
  	  			shortcutInfo.screen =pageindex;
  	  			temp_count=temp_count%pagemaxcount;

  	  			if(temp_count%maxCountX==0)
  	  			{
  	  				shortcutInfo.cellX = maxCountX-1;
  	  				if(temp_count==0)
  	  				{
  	  					shortcutInfo.cellY =maxCountY-1;
  	  				}
  	  				else 
  	  				{
  	  					shortcutInfo.cellY = temp_count/maxCountX-1;
  					}	
  	  			}
  	  			else
  	  			{
  	  				shortcutInfo.cellX = temp_count%maxCountX-1;
  	  				shortcutInfo.cellY = temp_count/maxCountX;
  	  			}
  	  			children_count_game++;
					//for notify
					if(shortcutInfo.isNewItem())
					{
						mNewGameNum++;
					}
					//end
  	  			folderInfo_game.add(shortcutInfo);
  	  			mOnlineShortcutInfos.add(shortcutInfo);
  	  			sApkItemsMap.put(apkinfo.mPkgname, shortcutInfo);
  	  			Log.d("zyfonline","folderInfo_game add  shortcutInfo : "+shortcutInfo);
  			}
		 }
		 }catch(Exception e)
		 {
			 Log.e("zyfonline","addOnlineApp error : "+e);
		 }
	 }
	 private Bitmap getOnlineBitmap(String strori,int width,int height)
	 {
		 byte[] bytes=base64code.base64decode(strori);
		 BitmapFactory.Options options = new BitmapFactory.Options();
	     if(bytes!=null)
	     {
	    	Bitmap bp= BitmapFactory.decodeByteArray(bytes, 0, bytes.length, options);
	    	int srcWidth = bp.getWidth();
            int srcHeight = bp.getHeight();

            if (srcWidth == 0 || srcHeight == 0) {
                return bp;
            }
            if(width==0||height==0)
            {
            	return bp;
            }
            int dstWidth = width, dstHeight = height;
            if ((float) srcWidth / width >= (float) srcHeight / height) {
                dstHeight = srcHeight * width / srcWidth;
            } else {
                dstWidth = srcWidth * height / srcHeight;
            }
            return Bitmap.createScaledBitmap(bp, dstWidth, dstHeight, true);
	     }
	     return null;
	 }
	 private Drawable getOnlineIcon(Bitmap bp)
	 {
		 Drawable icon = null;
		 icon = new FastBitmapDrawable(bp);
		 if(icon!=null)
			{
			  icon = mApp.getIconManager().buildUnifiedIcon(icon, ThemeUtils.ICON_TYPE_BROSWER_SHORTCUT);	
			}
	     return icon;
	 }
	private Bitmap getOnlineIcon(String oristr,int destw,int desth) {
		Bitmap bmp = getOnlineBitmap(oristr, destw, desth);
		/*
		 * Drawable drawable=null; if(bmp!=null) { drawable= getOnlineIcon(bmp);
		 * } return
		 * drawable==null?mApp.getIconManager().getDefaultIcon():drawable;
		 */
		return bmp;
	}
	 private Drawable getOnlineIcon(String oristr)
	 {
		 Bitmap bmp= getOnlineBitmap(oristr,0,0);
		 Drawable drawable=null;
		 if(bmp!=null)
		 {
			 drawable= getOnlineIcon(bmp);
		 }
		 return drawable==null?mApp.getIconManager().getDefaultIcon():drawable;
	 }
	 private void sendDownloadBroadcast(String url,String fullpath)
	 {
		 Intent intent_download=new Intent(ExappUtil.ACTION_TPW_FOLDERONLINE_DOWNLOAD_ONE);
		 intent_download.putExtra(ExappUtil.EXTAR_SAVE_FILEPATH,fullpath);
		 intent_download.putExtra(ExappUtil.EXTRA_DOWNLOAD_URL, url);
		 mApp.sendBroadcast(intent_download);
	 }
	 /*
	 private boolean savePic(String strori,String savepath)
	    {
	    	boolean b=true;
	    	byte[] bytes=base64code.base64decde(strori);
	    	if(bytes!=null)
	    	{
	    		FileOutputStream fos=null;
	    		try {
	    			fos=new FileOutputStream(savepath);
	        		fos.write(bytes, 0, bytes.length);
				} catch (Exception e) {
					// TODO: handle exception
				}
	    		finally{
	    			if(fos!=null)
						try {
							fos.close();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
	    		}
	    	}
	    	return b;
	    }
	    */
	 public static String getFolderonlineFilePath(String dir,String prename,String lastname)
	   {
		   File file=new File(dir);
		   if(file.exists()&&file.isDirectory())
		   {
			   for(File f : file.listFiles())
			   {
				  String name=f.getName();
				  if(f.isFile()&&name.startsWith(prename)&&name.endsWith(lastname))
				  {
					  return f.getAbsolutePath();
				  }
			   }
		   }
		   return null;
	   }
	 private ArrayList<ApkInfo> getApkInfoListFromFile(String path)
	    {
		 if(path==null)
			 return null;
	   	 InputStream inputStream=null;

	   	 XmlPullParser xmlParser = Xml.newPullParser();
	   	 ArrayList<ApkInfo> apkInfos=new ArrayList<ApkInfo>();
	   	 ApkInfo apkInfo=null;
	   	 
	   	 try {
	   		Log.d("zyfonline","getApkInfoListFromFile() path = "+path);
	   		File file=new File(path);
	   		if(!file.exists())
	   		{
	   			return null;
	   		}
	   		
	   		inputStream=new FileInputStream(path);
	   		xmlParser.setInput(inputStream, "utf-8");

	   		int evtType=xmlParser.getEventType();
	   		while(evtType!=XmlPullParser.END_DOCUMENT){
	   			switch(evtType){
	   			case XmlPullParser.START_TAG:
	   			{
	   				String tag = xmlParser.getName();
	   				Log.d("zyfonline", "parseXML START_TAG: tag = "+tag);
	   				
	   				if (tag.equalsIgnoreCase("TaskInfo")) 
	   				{
	   					apkInfo=new ApkInfo();
	   					 String apkname=xmlParser.getAttributeValue(null, "apkname");
	   					 if(apkname!=null&&!(apkname.trim().isEmpty()))
	   					 {
	   						 apkInfo.mApkName=apkname.trim();
	   					 }
	   					 String pkgname=xmlParser.getAttributeValue(null, "pkgname");
	   					 if(pkgname!=null&&!(pkgname.trim().isEmpty()))
	   					 {
	   						 apkInfo.mPkgname=pkgname.trim();
	   					 }
	   					 String filename=xmlParser.getAttributeValue(null, "filename");
	   					 if(filename!=null&&!(filename.trim().isEmpty()))
	   					 {
	   						 apkInfo.mFileName=filename.trim();
	   					 }
	   					 
	   					 String strsize=xmlParser.getAttributeValue(null, "size");
	   					 if(strsize!=null&&!(strsize.trim().isEmpty()))
	   					 {
	   						 int size=Integer.parseInt(strsize.trim());
	   						 apkInfo.mFileSize=size;
	   					 }
	   					 String url=xmlParser.getAttributeValue(null, "url");
	   					 if(url!=null&&!(url.trim().isEmpty()))
	   					 {
	   						 apkInfo.mUrl=url.trim();
	   						 Log.d("zyfonline", "parseXML : url = "+url);
	   					 }
	   					 String type=xmlParser.getAttributeValue(null, "type");
	   					 if(type!=null&&!(type.trim().isEmpty()))
	   					 {
	   						 Log.d("zyfonline", "parseXML : type = "+type);
	   						 apkInfo.mType=Integer.parseInt(type);
	   					 }
						 String describe = xmlParser.getAttributeValue(null, "describe");
						if (describe != null && !(describe.trim().isEmpty())) {
							apkInfo.mDescribe = describe.trim();
							Log.d("zyfonline", "parseXML : describe = " + describe);
						}
	   					 String icondata=xmlParser.getAttributeValue(null, "icondata");
	   					 if(icondata!=null&&!icondata.trim().isEmpty())
	   					 {
	   						 apkInfo.mIconData=icondata;
	   					 }
	   				}
	   			}
	   			break;

	   			case XmlPullParser.END_TAG:
	   			{
	   				String tag = xmlParser.getName();
	   				Log.d("zyfonline", "parseXML END_TAG: tag = "+tag);
	   				if(tag.equalsIgnoreCase("TaskInfo"))
	   				{
	   					//如果已经在下载完或已安装的情况下，不再重复载入
	   					if(ExappUtil.isFileExists(DESTPATH_UNZIP_APP+apkInfo.mFileName)||
	   							ExappUtil.isFileExists(DESTPATH_UNZIP_GAME+apkInfo.mFileName)||
	   							Utilities.isApkInstalled(apkInfo.mPkgname,mApp))
	   					{
	   						Log.d("zyfonline", "apkInfo.mPkgname is in exapp dir");
	   					}
	   					else
	   					{
	   						apkInfo.mState=ApkInfo.APKINFO_STATE_ORI;
	   						apkInfos.add(apkInfo);
	   						mApkinfosMap.put(apkInfo.mPkgname, apkInfo);
	   					}
	   				}
	   			}
	   			break;
	   			default:
	   				break;
	   			}
	   				//如果xml没有结束，则导航到下一个节点
	   				evtType=xmlParser.next();
	   			}
	   	}catch(FileNotFoundException fnfe){
	   		Log.e("zyfonline", "parseXML FileNotFoundException : "+fnfe);
	   		apkInfos=null;
	   	}catch (XmlPullParserException xppe) {
	   		Log.e("zyfonline", "parseXML XmlPullParserException : "+xppe);
	   		apkInfos=null;
	   	}catch (IOException ie) {
	   		Log.e("zyfonline", "parseXML error : "+ie);
	   		apkInfos=null;
	   	}
	   	 catch(NumberFormatException nbfe)
	   	 {
	   		Log.e("zyfonline", "parseXML error : "+nbfe);
	   		apkInfos=null;
	   	 }
	   	 finally{
	   		 if(inputStream!=null)
	   		 {
	   			 try {
	   				inputStream.close();
	   				inputStream=null;
	   			} catch (IOException e) {
	   				// TODO Auto-generated catch block
	   				e.printStackTrace();
	   			} 
	   		 }
	   	 }
	   	return apkInfos;
	   	 
	    }
	 //topwise zyf add end
	 public static boolean runApk(String pkgname,Context context)
	    {
	    	try{
	         PackageManager pm = context.getPackageManager();    
	         Intent  intent = pm.getLaunchIntentForPackage(pkgname);
	         Log.d(TAG, "runApk intent : "+intent);
	         if(intent != null){    
	             context.startActivity(intent);
	         }  
	    	}catch(Exception e)
	    	{
	    		Log.e(TAG, "runApk error : "+e);
	    		return false;
	    	}
	         return true;
	    }
	 public void fillExappApkIconAndTitle(Context context,FileAndInfo fai,LauncherApplication lapp,ShortcutInfo shortcutInfo)
	 {
		 Drawable iconDrawable=null;
		 if(fai==null||lapp==null)
			 return;
  		PackageInfo pkginfo=null;
  		if(fai!=null)
  			pkginfo=fai.mPkgInfo;
  		
      	if(pkginfo!=null)
      	{    
      		android.content.pm.ApplicationInfo applicationInfo=pkginfo.applicationInfo;
      		if(applicationInfo!=null)
      		{
      			Resources pRes = context.getResources();
      	        AssetManager assmgr = new AssetManager();
      	        assmgr.addAssetPath(fai.mFilePath);
      	        Resources res = new Resources(assmgr, pRes.getDisplayMetrics(), pRes.getConfiguration());
      	        shortcutInfo.title = res.getText(applicationInfo.labelRes);
      			if(lapp!=null)
      			{
          			//iconDrawable = mApp.getIconManager().getAppUnifiedIcon(
          			//			new ComponentName(fai.mPkgInfo.applicationInfo.packageName,fai.mPkgInfo.applicationInfo.className));
      				iconDrawable=res.getDrawable(applicationInfo.icon);
      				if(iconDrawable!=null)
      				{
	      				iconDrawable = lapp.getIconManager().buildUnifiedIcon(iconDrawable, ThemeUtils.ICON_TYPE_BROSWER_SHORTCUT);	
      				}
      				if(iconDrawable==null)
          			{
          				iconDrawable=lapp.getIconManager().getDefaultIcon();
                    }
      			}
      			else
      			{
      				iconDrawable=mApp.getIconManager().getDefaultIcon();
      			}
      			shortcutInfo.setIcon(iconDrawable);
      		}
      	}
	 }
	 public boolean reloadExappIcon(Context context,FolderInfo info,HashMap<Long, Bitmap> iconsFromDB)
	 {
		 try
		 {
	       for (ShortcutInfo si : info.contents) 
	       {
              if(si.isEditFolderShortcut()) {
                  continue;
              }
	    	  Intent intent=si.intent;
	    	  String pkgname=null;
	    	  if(intent!=null&&intent.getComponent()!=null&&intent.getComponent().getPackageName()!=null)
			  {
		  			pkgname=intent.getComponent().getPackageName();
			  }
	    	  else if(intent.getAction()!=null&&intent.getAction().equals("com.tpw.install"))
	    	  {
	    		   pkgname=intent.getStringExtra(com.tpw.homeshell.vpinstall.VPUtils.TYPE_PACKAGENAME);
	    	  }
	    	  //add for folderonline
	    	  else if(intent.getAction()!=null&&intent.getAction().equals("com.tpw.online"))
	    	  {
	    		   pkgname=intent.getStringExtra(com.tpw.homeshell.vpinstall.VPUtils.TYPE_PACKAGENAME);
	    	  }
	    	  //folderonline end
	  	       if(pkgname!=null)
	  	       {
	  	    	  FileAndInfo fai=null;
	               if((fai=getExappApk(pkgname))!=null)
	               {
	             	  fillExappApkIconAndTitle(context,fai,mApp,si);
	               }
	             //add for folderonline
	               else if(intent.getAction()!=null&&intent.getAction().equals("com.tpw.online"))
	 	    	   {
	            	   ApkInfo apkinfo=mApkinfosMap.get(pkgname);
	            	   si.setIcon(getOnlineIcon(apkinfo.mIconData));
	 	    	   }
	             //folderonline end
	               else
	               {
	            	     Bitmap iconDB = iconsFromDB.get(si.id);
	                     Drawable icon = mIconManager.getAppUnifiedIcon(si,null);
	                     if(mIconManager.isDefaultIcon(icon) && iconDB!=null){
	                        icon = new FastBitmapDrawable(iconDB);
	                     }
	                     si.setIcon(icon);
	               }
	  	       }
	       }
		 }
		 catch(Exception e)
		 {
			 Log.e("zyflauncher","reloadExappIcon : e = "+e);
			 return false;
		 }
	       return true;
	 }
    //topwise zyf add end
	//topwise zyf add for folderonline
	public void updateItemInDatabaseForOnlineDownload(Context context,
            String pkgname,int state) {
        Log.d(TAG, "update item in data base for online download");
        DBOperate dboperate=new DBOperate(context);
        dboperate.updataInfoState(pkgname, state);
        dboperate.closeDb();
    }
	//topwise zyf add end

	//topwise zyf add for notify
	public static final String ACTION_OPEN_FIXEDFOLDER="com.open.fixedfolder";
	public static final String EXTRA_OPEN_FIXEDFOLDER_TYPE="extra.open.fixedfolder.type";
    public static final int EXTRA_TYPE_APPS=1;
    public static final int EXTRA_TYPE_GAMES=2;
	public static final String SYS_TPW_NOTIFICATION_CODE="persist.sys.notification.code";
	public static final int INT_NOTIFICATION_CODE_SPLIT_LEN=5;
	
	public static final int NOT_SHOW_CONDITION_ONECE=1;
	public static final int NOT_SHOW_CONDITION_MUST_VIEW=NOT_SHOW_CONDITION_ONECE+1;
	public static final int NOT_SHOW_CONDITION_CLICK_APP=NOT_SHOW_CONDITION_MUST_VIEW+1;
	
	//public static final String SYSTEM_NOTIFICATION_SHOW="folder.online.show";
	public static final String SYSTEM_NOTIFICATION_APPS_SHOW="folder.online.apps.show";
	public static final String SYSTEM_NOTIFICATION_GAMES_SHOW="folder.online.games.show";

	private void checkToNotify(Context context,int icount)//仅展讯可用参数icount
	{
	    boolean sound=true;
		if(context==null)
			return;
		if(mNewAppNum==0&&mNewGameNum==0)
			return;
/*
		if(icount==1)
		{
			sound=false;
		}
*/
		boolean isappsshow=Settings.System.getInt(context.getContentResolver(), 
				SYSTEM_NOTIFICATION_GAMES_SHOW, 1)==1?true:false;
		boolean isgamesshow=Settings.System.getInt(context.getContentResolver(), 
				SYSTEM_NOTIFICATION_APPS_SHOW, 1)==1?true:false;
    	if(!isappsshow&&!isgamesshow)
    	{
    		return;
    	}
    	String sercode=null;//SystemProperties.get(SYS_TPW_NOTIFICATION_CODE,null);
		NotificationCode nc=getNotificationCode(sercode);
		if(nc!=null)
		{
			Log.d("zyfonline","checkToNotify : mNewAppNum = "+mNewAppNum+" , mNewGameNum = "+mNewGameNum);
			if(mNewAppNum>0&&isappsshow)
			{
				showNotification(context,EXTRA_TYPE_APPS,nc,sound);
				setNotificationIsShow(context,SYSTEM_NOTIFICATION_APPS_SHOW,NOT_SHOW_CONDITION_ONECE);
			}
			if(mNewGameNum>0&&isgamesshow)
			{
				showNotification(context,EXTRA_TYPE_GAMES,nc,sound);
				setNotificationIsShow(context,SYSTEM_NOTIFICATION_GAMES_SHOW,NOT_SHOW_CONDITION_ONECE);
			}
		}
    	
	}
	 private void showNotification(Context context,int type,NotificationCode nc,boolean sound)
	 {
		 Intent intent_notify=new Intent(ACTION_OPEN_FIXEDFOLDER);
		 String contentTitle="";
		 String ticker="";
		 String contentText="";
		 int smallIcon=0;
		 Bitmap largeIcon=null;
		 
		 if(type==EXTRA_TYPE_GAMES)
		 {
			 intent_notify.putExtra(EXTRA_OPEN_FIXEDFOLDER_TYPE,EXTRA_TYPE_GAMES);
			 contentTitle=context.getString(R.string.game_content_title);
			 ticker=context.getString(R.string.game_ticker);
			 contentText=context.getString(R.string.game_content_text);
			 smallIcon=R.drawable.draw_games;
		 }else if (type==EXTRA_TYPE_APPS){
			 intent_notify.putExtra(EXTRA_OPEN_FIXEDFOLDER_TYPE,EXTRA_TYPE_APPS);
			 contentTitle=context.getString(R.string.app_content_title);
			 ticker=context.getString(R.string.app_ticker);
			 contentText=context.getString(R.string.app_content_text);
			 smallIcon=R.drawable.draw_apps;
		}
		 
		 if(mApkinfos!=null)
		 {
			 for (ApkInfo apkinfo : mApkinfos)
			 {
				 Log.e("zyftest"," showNotification apkinfo = "+apkinfo);
				 Log.e("zyftest"," showNotification apkinfo.mIsNew = "+apkinfo.mIsNew+
						 " , apkinfo.mType = "+apkinfo.mType+" , type = "+type);
				 if(apkinfo.mIsNew&&apkinfo.mType==type)
				 {
					 final Resources res = context.getResources();
					 int iconWidth = (int) res.getDimension(android.R.dimen.notification_large_icon_width);
					 int iconHeight = (int) res.getDimension(android.R.dimen.notification_large_icon_height);
					 largeIcon=getOnlineIcon(apkinfo.mIconData,iconWidth,iconHeight);
					 String strDesc=apkinfo.mDescribe;
					 if(strDesc!=null&&!strDesc.equals(""))
					 {
						 String splitstr="[|]";//分隔符受正则表达式的影响，不能乱动
						 String split="|";
						 
						 if(strDesc.contains(split))
						 {
							 String splits[]=strDesc.split(splitstr);
							 if(splits.length>=2)
							 {
								 contentTitle=splits[0];
								 contentText=splits[1];
								 ticker=splits[0];
							 }
						 }
						 else
						 {
							 ticker=apkinfo.mDescribe;
							 contentTitle=apkinfo.mDescribe;
							 contentText=apkinfo.mDescribe;
						 }
					 }
					break;
				 }
			 }
		 }
		 
		 int iextratype=intent_notify.getIntExtra(EXTRA_OPEN_FIXEDFOLDER_TYPE, -1);
		
    	int requestcode=(int)System.currentTimeMillis();
		
		 Notification.Builder builder = new Notification.Builder(context)
        .setContentTitle(contentTitle)
        .setTicker(ticker)
        .setContentText(contentText)
        .setSmallIcon(smallIcon)
        .setAutoCancel(true)
        .setPriority(Notification.PRIORITY_MAX)
        .setOnlyAlertOnce(false)
        .setDefaults(Notification.DEFAULT_ALL)
        .setContentIntent(PendingIntent.getBroadcast(context, requestcode, intent_notify, 0));
        //.setDeleteIntent(PendingIntent.getBroadcast(context, 0, deleteIntent, 0))
        //.build();
		 
		 if(sound)
		 {
			builder.setDefaults(Notification.DEFAULT_ALL
					|(Notification.DEFAULT_SOUND)
					|(Notification.DEFAULT_VIBRATE)
					|(Notification.DEFAULT_LIGHTS));
		 }
		 else
		{
			builder.setDefaults(Notification.DEFAULT_ALL
					 &(~Notification.DEFAULT_SOUND)
					 &(~Notification.DEFAULT_VIBRATE)
					 &(~Notification.DEFAULT_LIGHTS));
		}
		 
		 if(largeIcon!=null)
			 builder.setLargeIcon(largeIcon);
        
        Notification notification=builder.getNotification();
		
		if(!nc.mCanclear)
			notification.flags |= Notification.FLAG_NO_CLEAR; 
		
		 NotificationManager notificationManager =
                 (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(smallIcon, notification);
	 }
	 public void setNotificationIsShow(Context context,String prop,int step)
	 {
		 String sercode=null;//SystemProperties.get(SYS_TPW_NOTIFICATION_CODE,null);
		 NotificationCode nc=getNotificationCode(sercode);
		 if(nc!=null)
		 {
			if(nc.mNotShowCondition==step)
			{
				// Log.d("Launcher.Model","SYS_NOTIFICATION_SHOW  "+SYS_NOTIFICATION_SHOW);
				//SystemProperties.set(SYS_NOTIFICATION_SHOW,"0");
				 Settings.System.putInt(context.getContentResolver(), 
						 prop, 0);
			}
		 }
	 }
	 
	 class NotificationCode{
		   public long mTime;
		   public String mVersion;
		   public boolean mPopup;
		   public boolean mCanclear;
		   public int mNotShowCondition;//1:仅弹一次 2、必须查阅 3、必须点击应用
	 }
	 private NotificationCode getNotificationCode(String sercode)
	 {
		 	NotificationCode nc=null;
		 	if(sercode==null)
		 	{
		 		return null;
		 	}
			String items[]=sercode.split(":");
			if(items.length<=0)
			{
				return null;
			}
			else if(items.length==INT_NOTIFICATION_CODE_SPLIT_LEN){
				String strdate=items[0];
				String version=items[1];
				String popup=items[2];
				String canclear=items[3];
				String notshowcondition=items[4];
				
				try {
					nc=new NotificationCode();
					nc.mTime=Long.valueOf(strdate);
					nc.mVersion=version;
					nc.mPopup=(Integer.parseInt(popup)==1);
					nc.mCanclear=(Integer.parseInt(canclear)==1);
					nc.mNotShowCondition=Integer.parseInt(notshowcondition);
				} catch (NumberFormatException e) {
					// TODO: handle exception
					nc=null;
				}
			}
			return nc;
	 }
	 public FolderInfo getFolderInfo(long id)//HashMap<Long, FolderInfo> sBgFolders = new HashMap<Long, FolderInfo>();
	 {
		 if(sBgFolders==null)
			 return null;
		  return sBgFolders.get(id);
	 }
	 private boolean checkAlreadInFolder(ApkInfo info,FolderInfo folderinfo_app,FolderInfo folderinfo_game)
	 {
		 if(info.mType == ItemInfo.ITEM_EXTRA_TYPE_APPS)
		 {
			 return checkAlreadInFolder(info,folderinfo_app);
		 }
		 else if(info.mType == ItemInfo.ITEM_EXTRA_TYPE_GAMES)
		 {
			 return checkAlreadInFolder(info,folderinfo_game);
		 }
		 return false;
	 }
	 private boolean checkAlreadInFolder(ApkInfo info,FolderInfo folderinfo)
	 {
		 if(folderinfo==null||info==null)
			 return false;
		 String pkgname=null;
		 for (ShortcutInfo s : folderinfo.contents) {
			 pkgname= getPackagename(s);
			 if(pkgname==null)
			 {
				 continue;
			 }
			 if(info.mPkgname.equals(pkgname))
			 {
				 return true;
			 }
         }
		 return false;
	 }
	 private boolean checkAlreadInFolder(PackageInfo info,FolderInfo folderinfo)
	 {
		 if(folderinfo==null||info==null)
			 return false;
		 String pkgname=null;
		 for (ShortcutInfo s : folderinfo.contents) {
			 pkgname= getPackagename(s);
			 if(pkgname==null)
			 {
				 continue;
			 }
			 if(info.packageName.equals(pkgname))
			 {
				 return true;
			 }
         }
		 return false;
	 }
	 public String getPackagename(ShortcutInfo s)
	 {
		 if(s==null)
		 {
			 return null;
		 }
		 Intent intent=s.intent;
		 if(intent==null)
		 {
			 return null;
		 }
		 String pkgName=null;
		 ComponentName cn = intent.getComponent();
		 if(cn!=null)
		 {
			 pkgName=cn.getPackageName();
		 }
		 if(pkgName==null)
		 {
			 pkgName = intent.getStringExtra(FileAndInfo.TYPE_PACKAGENAME);
		 }
		 return pkgName;
	 }
     //topiwse zyf add for notify
     Runnable mCheckToNotify = new Runnable() {
         public void run() {
        	 	if(mApp.getContext()!=null)
        	 		checkToNotify(mApp.getContext(),-1);
         	}
         };
     //add end
	 //add end
//topwise zyf add for hide folder
	 public FolderInfo addFixedFolder(Context context,int resid,int type)
	 {
		 FolderInfo fi =null;
		 int extratype=getExtraTypeByResID(resid);
      	 ItemInfo itmeinfo= findFixedFolderByExtraType(extratype);
      	 String flag=getFlagByType(type);
      	 String zpflag=android.os.SystemProperties.get(flag, "NG");
      	 //String onlineflag=getOnlineFlag();
      	 //String onlineflagval=android.os.SystemProperties.get(onlineflag, "");
		 Log.d("zyftest","addFixedFolder type = "+type+" , itmeinfo = "+itmeinfo+" , flag "+flag+" : "+zpflag);
      	 if(itmeinfo==null&&(zpflag.equals("OK")/*||!onlineflagval.equals("")*/))
      	 {
             ScreenPosition sposition=findEmptyCell(0);
			 if(sposition==null)
				return null;
			 Log.d("zyftest","addFixedFolder sposition.s="+sposition.s+" ( "+sposition.x+" , "+sposition.y+" )");
             long fixedfolderid=mApp.getLauncherProvider().loadFixedFolder(sposition);
             fi = findOrMakeFixedFolder(sBgFolders, fixedfolderid);
             fi.title =context.getResources().getString(resid);
             fi.id = fixedfolderid;
             fi.container = LauncherSettings.Favorites.CONTAINER_DESKTOP;
             fi.screen = sposition.s;
             fi.cellX = sposition.x;
             fi.cellY = sposition.y;
             fi.itemExtraType=type;
             
             //add iteminfo to sFixedFoldersItems
             sFixedFoldersItems.add(fi);
              Log.d("zyftest","addFixedFolder sFixedFoldersItems.add "+fi);
             sBgWorkspaceItems.add(fi);
             sBgItemsIdMap.put(fi.id, fi);
             sBgFolders.put(fi.id, fi);
             
      	 }
      	 return fi;
	 }
    public static String getFlagByType(int type)
    {
    	String flag="persist.sys.zp.flag1";
    	switch(type)
    	{
	    	case ItemInfo.ITEM_EXTRA_TYPE_APPS:
	    		flag="persist.sys.zp.flag";
	    		break;
	    	case ItemInfo.ITEM_EXTRA_TYPE_GAMES:
	    		flag="persist.sys.zp.flag1";
	    		break;
	    		default:
	    			break;
    	}
    	return flag;
    }
    public static String getOnlineFlag()
    {
    	return "persist.sys.fol.xml.ver";
    }

	public static boolean isShowHideFolder(FolderInfo info)
	{
		boolean show=false;
		if(info.itemExtraType==ItemInfo.ITEM_EXTRA_TYPE_APPS||info.itemExtraType==ItemInfo.ITEM_EXTRA_TYPE_GAMES)
		{
		  	 String flag=getFlagByType(info.itemExtraType);
		  	 String zpflag="NG";//android.os.SystemProperties.get(flag, "NG");
			 if(zpflag.equals("OK"))
			 {
				show=true;
				Log.d("zyftest", "folderToRemove.......... : "+info+"  NOT TO REMOVE!!");
			 }
		}
		return show;
	}
//topwise zyf add for hide folder end
}
