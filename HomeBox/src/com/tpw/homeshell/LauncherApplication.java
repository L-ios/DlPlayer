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

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map.Entry;

//import tpw.content.res.ThemeResources;
import android.app.Application;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;

import com.tpw.homeshell.LauncherSettings.Favorites;
import com.tpw.homeshell.appgroup.AppGroupManager;
import com.tpw.homeshell.backuprestore.BackupManager;
import com.tpw.homeshell.backuprestore.BackupRecord;
import com.tpw.homeshell.backuprestore.BackupUitil;
import com.tpw.homeshell.favorite.RecommendTask;
import com.tpw.homeshell.icon.IconManager;
import com.tpw.homeshell.model.LauncherModel;
import com.tpw.homeshell.setting.ContinuousHomeShellReceiver;
import com.tpw.homeshell.setting.HomeShellSetting;

import com.tpw.homeshell.smartsearch.HanziToPinyin;
import com.tpw.homeshell.utils.Utils;
import com.tpw.homeshell.IconDigitalMarkHandler;
import com.tpw.homeshell.utils.Utils;


public class LauncherApplication extends Application implements
        ThemeChangedListener.IThemeChanged, FontChangedListener.IFontChanged {
    private LauncherModel mModel;
    private IconManager mIconManager = null;
    private WidgetPreviewLoader.CacheDb mWidgetPreviewCacheDb;
    private static boolean sIsScreenLarge;
    private static float sScreenDensity;
    private static int sScreenWidth = 0;
    private static int sScreenHeight = 0;
    private static int sLongPressTimeout = 300;
    private static final String sSharedPreferencesKey = "com.tpw.homeshell.prefs";
    private static final String TAG = "LauncherApplication";
    private boolean mIsCollectFavoriteData = false;
    private static final int OP_PRE_INIT_THEME = 2;
    public static final int SETTINGS_AGED_MODE = 1;

    private CheckVoiceCommandPressHelper mVuiHelper;

    private static final String RESTORE_DB_FILE = "restore.db";

    WeakReference<LauncherProvider> mLauncherProvider;
    private static Context mContext = null;;
    public static Launcher mLauncher = null;
    public static HomeShellSetting homeshellSetting = null;
    public static HashMap<String, BackupRecord> mBackupRecordMap; 

    private ContinuousHomeShellReceiver mContinuousHomeShellReceiver;

    //added by lixuhui 2015/02/10 get the misscall count & unread sms/mms count 
    //function to prevent the homeshell process start by notifications when the homeshell process is die or not start, in case to reduce the process start/die and cpu consume
    private NotificationReceiver mNotificationReceiver;
	
    private RecommendTask mRecommendTask;

    public void collectUsageData(long appId) {
        if (mIsCollectFavoriteData) {
            mRecommendTask.notifyAppClicked(appId);
        }
    }

    private void handleRestore() {
        if (BackupManager.getRestoreFlag(this)) {
            //set in restore flag, so that other part can use it to judge if homeshell
            //is in restore mode
            Log.d(TAG, "Set homeshell inRestore flag");
            BackupManager.getInstance().setIsInRestore(true);
//            BackupManager.setRestoreFlag(this, false);

            Cursor c = null;
            File restoreDBfile = new File(getApplicationContext().getFilesDir() 
                                    + "/backup/" + RESTORE_DB_FILE);
            if (restoreDBfile.exists() == true) {
                Log.d(TAG, "handleRestore read data from restore db");
                SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase(restoreDBfile, null);
                c = db.query("favorites", null, null, null, null, null, null);
                //convertDBToBackupSet will close cursor, so we don't need to close it
                mBackupRecordMap = BackupUitil.convertDBToBackupSet(c);
                db.close();
            } else {
                Log.d(TAG, "handleRestore read data from homeshell db");
                final ContentResolver contentResolver = getApplicationContext().getContentResolver();
                c = contentResolver.query(Favorites.CONTENT_URI, null, null, null, null);
                //convertDBToBackupSet will close cursor, so we don't need to close it
                mBackupRecordMap = BackupUitil.convertDBToBackupSet(c);
            }

            for (Entry<String, BackupRecord> r : LauncherApplication.mBackupRecordMap.entrySet()) {
                Log.d(TAG, r.getValue().getField(Favorites._ID));
                String intentStr = r.getValue().getField(Favorites.INTENT);
                if (TextUtils.isEmpty(intentStr)) {
                    continue;
                }
                try {
                    Intent intent = Intent.parseUri(intentStr, 0);
                    final ComponentName name = intent.getComponent();
                    if (name == null) {
                        Log.e(TAG, "ComponentName == Null");
                        Log.i(TAG, "intent = " + intent.toString());
                        continue;
                    }
                    Log.d(TAG, "onCreate() mBackupRecordMap getPackageName()=" + intent.getComponent().getPackageName());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
    @Override
    public void onCreate() {
        super.onCreate();
        mContext = getApplicationContext();

        ConfigManager.init();

            Intent intent = new Intent("com.yunos.theme.thememanager.ACTION_MANAGE_THEME");
            intent.putExtra("operation", OP_PRE_INIT_THEME);
            startService(intent);
        handleRestore();

        // set sIsScreenXLarge and sScreenDensity *before* creating icon cache
        try {
            sIsScreenLarge = getResources().getBoolean(R.bool.is_large_screen);
        } catch (Exception e) {
            sIsScreenLarge = false;
        }
        Log.d(TAG, "sIsScreenLarge="+sIsScreenLarge);
        sScreenDensity = getResources().getDisplayMetrics().density;
        sScreenWidth = getResources().getDisplayMetrics().widthPixels;
        sScreenHeight = getResources().getDisplayMetrics().heightPixels;

        mWidgetPreviewCacheDb = new WidgetPreviewLoader.CacheDb(this);
        mIconManager = new IconManager(this);
        mModel = new LauncherModel(this);
        mContinuousHomeShellReceiver = new ContinuousHomeShellReceiver(this);

        // Register intent receivers
        IntentFilter filter = new IntentFilter(Intent.ACTION_PACKAGE_ADDED);
        filter.addAction(Intent.ACTION_PACKAGE_REMOVED);
        filter.addAction(Intent.ACTION_PACKAGE_CHANGED);
         filter.addAction(Intent.ACTION_PACKAGE_DATA_CLEARED);
        filter.addDataScheme("package");
        registerReceiver(AppDownloadManager.getInstance().getPackageStateReceiver(), filter);
        registerReceiver(mModel, filter);
        filter = new IntentFilter();
        filter.addAction(Intent.ACTION_EXTERNAL_APPLICATIONS_AVAILABLE);
        filter.addAction(Intent.ACTION_EXTERNAL_APPLICATIONS_UNAVAILABLE);
        filter.addAction(Intent.ACTION_LOCALE_CHANGED);
        //topwise zyf add
        filter.addAction("com.tpw.homerefresh");
        //topwise zyf add end
        filter.addAction(Intent.ACTION_CONFIGURATION_CHANGED);
        registerReceiver(mModel, filter);
        filter = new IntentFilter();
        filter.addAction(LauncherModel.ACTION_APP_LAUNCHED);
        registerReceiver(mModel, filter);

        filter = new IntentFilter();
        filter.addAction(AppDownloadManager.ACTION_APP_DWONLOAD_TASK);
        registerReceiver(mModel, filter);
        
        //topwise zyf add for folderonline
        filter = new IntentFilter();
        filter.addAction(AppDownloadManager.ACTION_ONLINE_DOWNLOAD_TASK);
        registerReceiver(mModel, filter);
        //topwise zyf add end

        filter = new IntentFilter();
        filter.addAction(AppDownloadManager.ACTION_HS_DOWNLOAD_TASK);
        registerReceiver(mModel, filter);
        filter = new IntentFilter();
        filter.addAction(LauncherModel.ACTION_AGED_MODE_CHANGED);
        registerReceiver(mModel, filter);

        //for layout change
        filter = new IntentFilter();
        filter.addAction(LauncherModel.ACTION_HOMESHELL_LAYOUT_CHANGE);
        filter.addAction(LauncherModel.ACTION_UPDATE_LAYOUT);
        registerReceiver(mModel, filter);

        filter = new IntentFilter();
        filter.addAction(ContinuousHomeShellReceiver.CONTINUOUS_HOMESHELL_SHOW_ACTION);
        registerReceiver(mContinuousHomeShellReceiver, filter);

        filter = new IntentFilter();
        filter.addAction(IconDigitalMarkHandler.ACTION_APPLICATION_NOTIFICATION);
        filter.addAction(Intent.ACTION_UNREAD_CHANGED);  //add by zl 20150721 for show misscall count & unread sms/mms count
        registerReceiver(mModel, filter);

        filter = new IntentFilter();
        filter.addAction(HomeShellSetting.ACTION_ON_MARK_TYPE_CHANGE);
        registerReceiver(mModel, filter);

        filter = new IntentFilter();
        filter.addAction(HomeShellSetting.ACTION_ON_SHOW_NEW_MARK_CHANGE);
        registerReceiver(mModel, filter);

        IconDigitalMarkHandler.getInstance();
		
        //added by lixuhui 2015/02/10 get the misscall count & unread sms/mms count 
        //function to prevent the homeshell process start by notifications when the homeshell process is die or not start, in case to reduce the process start/die and cpu consume
        mNotificationReceiver = new NotificationReceiver();
        filter = new IntentFilter();
        filter.addAction(IconDigitalMarkHandler.ACTION_APPLICATION_NOTIFICATION);
        filter.addAction(Intent.ACTION_UNREAD_CHANGED);   //add by zl 20150721 for show misscall count & unread sms/mms count
        registerReceiver(mNotificationReceiver, filter);

        filter = new IntentFilter();
        filter.addAction(Intent.ACTION_PACKAGE_DATA_CLEARED);
        filter.addDataScheme("package");
        registerReceiver(mNotificationReceiver, filter);
		//end
        // Register for changes to the favorites
        ContentResolver resolver = getContentResolver();
        resolver.registerContentObserver(LauncherSettings.Favorites.CONTENT_URI, true,
                mFavoritesObserver);

        ThemeChangedListener.getInstance(getApplicationContext()).register(
                getApplicationContext());
        ThemeChangedListener.getInstance(getApplicationContext()).addListener(
                this);
        
        FontChangedListener.getInstance(getApplicationContext()).register(getApplicationContext());
        FontChangedListener.getInstance(getApplicationContext()).addListener(this);

        CheckVoiceCommandPressHelper.checkEnvironment();
        if(CheckVoiceCommandPressHelper.PUSH_TO_TALK_SUPPORT) {
            mVuiHelper = CheckVoiceCommandPressHelper.getInstance();
            filter = new IntentFilter();
            filter.addAction(CheckVoiceCommandPressHelper.BROADCAST_PUSHTALK_SWITCH_CHANGED);
            registerReceiver(mVuiHelper.mSwitchReceiver, filter);
            if(mVuiHelper.isVoiceSwitchOn()) {
                mVuiHelper.initVoiceService();
            }
        }

        /*
        HanziToPinyin.getInstance().initHanziPinyinForAllChars(getContext());
        */

        mRecommendTask = new RecommendTask(getApplicationContext());
        if (Utils.isYunOSInternational() || TopwiseConfig.YUNOS_CTA_SUPPORT) {
            AppGroupManager.switchOff();
        } else {
            AppGroupManager.switchOn();
        }
        if (AppGroupManager.isSwitchOn()) {
            AppGroupManager manager = AppGroupManager.getInstance();
            manager.initAppInfos();
            if (!manager.isLoadedSuccess()) {
                LauncherModel
                        .startLoadAppGroupInfo(AppGroupManager.DELAY_TIME_LONG);
            }
        }

        Launcher.mIsAgedMode = SETTINGS_AGED_MODE == Settings.Secure.getInt(
                getContext().getContentResolver(), "aged_mode", 0);
        checkOSUpdate();
    }

    public void checkOSUpdate(){
        Intent newIntent = new Intent("com.yunos.fota.action.AppUpdateService");
        newIntent.setPackage("com.tpw.fota");//targetSDK version is Android SDK 5.0 or later, have to add this line, or exception thrown!
        newIntent.putExtra("PackageName", "com.tpw.homeshell");
        newIntent.putExtra("AutoCheckType","AutoCheckInWifi");
        startService(newIntent);
    }
    /**
     * There's no guarantee that this function is ever called.
     */
    @Override
    public void onTerminate() {
        super.onTerminate();

        unregisterReceiver(mModel);
        unregisterReceiver(mContinuousHomeShellReceiver);

        //added by lixuhui 2015/02/10 get the misscall count & unread sms/mms count 
        //function to prevent the homeshell process start by notifications when the homeshell process is die or not start, in case to reduce the process start/die and cpu consume
        unregisterReceiver(mNotificationReceiver);
        //end
        ContentResolver resolver = getContentResolver();
        resolver.unregisterContentObserver(mFavoritesObserver);

        ThemeChangedListener.getInstance(getApplicationContext()).unregister(
                getApplicationContext());
        
        FontChangedListener.getInstance(getApplicationContext()).unregister(getApplicationContext());
        
        this.mModel.destroy();
        mContext = null;
    }

    /**
     * Receives notifications whenever the user favorites have changed.
     */
    private final ContentObserver mFavoritesObserver = new ContentObserver(new Handler()) {
        @Override
        public void onChange(boolean selfChange) {
            // If the database has ever changed, then we really need to force a reload of the
            // workspace on the next load
            mModel.resetLoadedState(false, true);
            mModel.startLoaderFromBackground();
        }
    };

    LauncherModel setLauncher(Launcher launcher) {
        mModel.initialize(launcher);
        mLauncher = launcher;
        return mModel;
    }

    public LauncherModel getModel() {
        return mModel;
    }

    public WidgetPreviewLoader.CacheDb getWidgetPreviewCacheDb() {
        return mWidgetPreviewCacheDb;
    }

   void setLauncherProvider(LauncherProvider provider) {
        mLauncherProvider = new WeakReference<LauncherProvider>(provider);
    }

    public LauncherProvider getLauncherProvider() {
        return mLauncherProvider.get();
    }

    public static String getSharedPreferencesKey() {
        return sSharedPreferencesKey;
    }

    public static boolean isScreenLarge() {
        return sIsScreenLarge;
    }

    public static boolean isScreenLandscape(Context context) {
        return context.getResources().getConfiguration().orientation ==
            Configuration.ORIENTATION_LANDSCAPE;
    }

    public static float getScreenDensity() {
        return sScreenDensity;
    }
    
    public static int getScreenWidth() {
        return sScreenWidth;
    }
    
    public static int getScreenHeight() {
        return sScreenHeight;
    }
    
    public static int getLongPressTimeout() {
        return AgedModeUtil.isAgedMode() ? 1000 : sLongPressTimeout;
    }
    
    public IconManager getIconManager(){
    	return mIconManager;
    }
    @Override
    public void onThemeChanged() {
        GadgetCardHelper.onThemeChanged();
        mRecommendTask.refreshFavoriteAppIcons();
        //ThemeResources.reset();
        mModel.onThemeChange();
        LauncherAnimUtils.onDestroyActivity();
	 if(LauncherApplication.getLauncher().mWidgetPageManager.isSupportWidgetPageHotseat()) {	
	        if (mLauncher != null) {
	        	mLauncher.mWidgetPageManager.setHotseat();//Added support for widget page, huangweiwei, topwise, 2014-12-31
	        }
	 }
    }
    public static Context getContext(){
    	return mContext;
    }
    public static Launcher getLauncher() {
        return mLauncher;
    }

    @Override
    public void onFontChanged() {
        mModel.onFontChanged();
    }
    public void onAgedModeChanged(boolean agedMode, boolean forceChangeLayout, boolean forceLoad) {
        if (agedMode == AgedModeUtil.isAgedMode() && !forceChangeLayout) {
            return;
        }
        SharedPreferences preference = PreferenceManager.getDefaultSharedPreferences(this);
        if (preference == null) {
            return;
        }
        HomeShellSetting.setFreezeValue(this, agedMode);
        HomeShellSetting.setLayoutValue(this, agedMode);
        String current = preference.getString(HomeShellSetting.KEY_PRE_LAYOUT_STYLE, "");
        int countX = 4;
        int countY = 4;
        if (current.equals("1")) {
            countY = 5;
        } else if (current.equals("2")) {
            countX = countY = 3;
        }
        mModel.changeLayoutForAgedModeChanged(agedMode, countX, countY);
        if (homeshellSetting != null) {
            homeshellSetting.updateLayoutPreference();
        }
        mModel.switchDbForAgedMode(agedMode);
        mModel.clearDownloadItems();
        if (forceLoad) {
            mModel.resetLoadedState(true, true);
            mModel.startLoader(false, -1);
        }
    }
}
