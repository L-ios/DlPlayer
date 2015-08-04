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

import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.Log;

import com.tpw.homeshell.AppDownloadManager.AppDownloadStatus;
import com.tpw.homeshell.LauncherSettings.Favorites;
import com.tpw.homeshell.smartsearch.HanziToPinyin;
import com.tpw.homeshell.utils.HLog;
import com.tpw.homeshell.utils.Utils;
import com.tpw.homeshell.vpinstall.VPUtils.VPInstallStatus;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a launchable icon on the workspaces and in folders.
 */
public class ShortcutInfo extends ItemInfo implements Cloneable{

	private final String TAG = "ShortcutInfo";

	/**
	 * The intent used to start the application.
	 */
    public Intent intent;

	/**
	 * Indicates whether the icon comes from an application's resource (if
	 * false) or from a custom Bitmap (if true.)
	 */
    public boolean customIcon;

	/**
	 * Indicates whether we're using the default fallback icon instead of
	 * something from the app.
	 */
    public boolean usingFallbackIcon;

	/**
	 * If isShortcut=true and customIcon=false, this contains a reference to the
	 * shortcut icon as an application's resource.
	 */
    public Intent.ShortcutIconResource iconResource;

	/**
	 * The application icon.
	 */
	public Drawable mIcon;

	/**
	 * donwload progress
	 * */
	private int mProgress = -1;
	/**
	 * download status
	 * */
	private int mAppDownloadStatus = AppDownloadStatus.STATUS_NO_DOWNLOAD;
	
	public int mDownloadType = -1;

    private int mVPInstallStatus = VPInstallStatus.STATUS_NORMAL;

    public int isSDApp = 0;
    
    public boolean isSystemApp;

	private static final String URI_APPSTORE_DETAIL = "alimarket://details?id=";

    public ShortcutInfo() {
		itemType = LauncherSettings.BaseLauncherColumns.ITEM_TYPE_SHORTCUT;
	}

	public ShortcutInfo(ShortcutInfo info) {
		super(info);
		title = info.title.toString();
		intent = new Intent(info.intent);
		if (info.iconResource != null) {
			iconResource = new Intent.ShortcutIconResource();
			iconResource.packageName = info.iconResource.packageName;
			iconResource.resourceName = info.iconResource.resourceName;
		}
		mIcon = info.mIcon; // TODO: should make a copy here. maybe we don't
							// need this ctor at all
		customIcon = info.customIcon;
        isSDApp = info.isSDApp;
	}

	/** TODO: Remove this. It's only called by ApplicationInfo.makeShortcut. */
	public ShortcutInfo(ApplicationInfo info) {
		super(info);
		title = info.title.toString();
		intent = new Intent(info.intent);
		customIcon = false;
	}

	public void setIcon(Drawable b) {
		mIcon = b;
		
	}


	/**
	 * Creates the application intent based on a component name and various
	 * launch flags. Sets {@link #itemType} to
	 * {@link LauncherSettings.BaseLauncherColumns#ITEM_TYPE_APPLICATION}.
	 * 
	 * @param className
	 *            the class name of the component representing the intent
	 * @param launchFlags
	 *            the launch flags
	 */
	final void setActivity(ComponentName className, int launchFlags) {
		intent = new Intent(Intent.ACTION_MAIN);
		intent.addCategory(Intent.CATEGORY_LAUNCHER);
		intent.setComponent(className);
		intent.setFlags(launchFlags);
		itemType = LauncherSettings.BaseLauncherColumns.ITEM_TYPE_APPLICATION;
	}

	@Override
    public void onAddToDatabase(ContentValues values) {
		super.onAddToDatabase(values);
		if(title != null) {
		    title = Utils.trimUTFSpace(title);
		}
		String titleStr = title != null ? title.toString() : null;
		values.put(LauncherSettings.BaseLauncherColumns.TITLE, titleStr);

		String uri = intent != null ? intent.toUri(0) : null;
		values.put(LauncherSettings.BaseLauncherColumns.INTENT, uri);

		if (isEditFolderShortcut()) {
		    Log.d(TAG,"the button for batch operation the icons,no need to write bitmap to database, do nothing");
		} else if (customIcon) {
			values.put(LauncherSettings.BaseLauncherColumns.ICON_TYPE,
					LauncherSettings.BaseLauncherColumns.ICON_TYPE_BITMAP);
			writeBitmap(values, mIcon);
		} else {
			if (!usingFallbackIcon) {
				writeBitmap(values, mIcon);
			}
			values.put(LauncherSettings.BaseLauncherColumns.ICON_TYPE,
					LauncherSettings.BaseLauncherColumns.ICON_TYPE_RESOURCE);
			if (iconResource != null) {
				values.put(LauncherSettings.BaseLauncherColumns.ICON_PACKAGE,
						iconResource.packageName);
				values.put(LauncherSettings.BaseLauncherColumns.ICON_RESOURCE,
						iconResource.resourceName);
			}
		}
        values.put(LauncherSettings.BaseLauncherColumns.IS_SDAPP, isSDApp);

        if (titleStr != null && !titleStr.isEmpty()) {
            LauncherProvider.updateTitlePinyin(values, titleStr);
        }
	}

	@Override
	public String toString() {
		return "ShortcutInfo(title=" + title + "intent=" + intent
				+ "id=" + this.id + " type=" + this.itemType + " container="
				+ this.container + " screen=" + screen + " cellX=" + cellX
				+ " cellY=" + cellY + " spanX=" + spanX + " spanY=" + spanY
				+ ")";
	}

	public static void dumpShortcutInfoList(String tag, String label,
			ArrayList<ShortcutInfo> list) {
		Log.d(tag, label + " size=" + list.size());
		for (ShortcutInfo info : list) {
			Log.d(tag, "   title=\"" + info.title + " icon=" + info.mIcon
					+ " customIcon=" + info.customIcon);
		}
	}

	/**
	 * only support appdownload's status.
	 * */
	public void setAppDownloadStatus(int status) {
		mAppDownloadStatus = status;
	}

	/**
	 * only support app download's status
	 * */
	public int getAppDownloadStatus() {
		return mAppDownloadStatus;
	}

    public void setVPInstallStatus(int status) {
        mVPInstallStatus = status;
    }

    public int getVPInstallStatus() {
        return mVPInstallStatus;
    }

	/**
	 * set app store appId,
	 * */
	public void setAppId(String appId) {
		intent.setData(Uri.parse(URI_APPSTORE_DETAIL + appId));
	}

	public int getProgress(){
		return mProgress;
	}
	public void setProgress(int progress) {
		if (progress > 100) {
			Log.w(TAG, "setProgress, progress error : curent progress = "
					+ mProgress + " progress = " + progress);
			return;
		}
		HLog.i(TAG, "setProgress progress : " + progress);
		mProgress = progress;
	}
	
	public boolean isDownloading() {
		if(this.itemType == LauncherSettings.Favorites.ITEM_TYPE_SHORTCUT_DOWNLOADING){
			return true;
		}else if(this.mAppDownloadStatus == AppDownloadStatus.STATUS_DOWNLOADING ||
		         this.mAppDownloadStatus == AppDownloadStatus.STATUS_INSTALLING ||
		         this.mAppDownloadStatus == AppDownloadStatus.STATUS_WAITING ||
		         this.mAppDownloadStatus == AppDownloadStatus.STATUS_PAUSED ){
            return true;
        }else{
        	return false;
        }
    }
	
	public Intent createDownloadIntent(){
		Intent i = new Intent();
		i.setData(intent.getData());
		return i;
	}
	
    public void setSystemAppFlag(ResolveInfo resolveInfo) {
        if(resolveInfo != null) {
            isSystemApp = (resolveInfo.activityInfo.applicationInfo.flags & android.content.pm.ApplicationInfo.FLAG_SYSTEM) > 0;
        } else {
            isSystemApp = false;
        }
	}
    public boolean isEditFolderShortcut() {
        return itemFlags == Favorites.ITEM_FLAGS_EDIT_FOLDER;
    }
    @Override
    public ShortcutInfo clone() {
        ShortcutInfo info = null;
        try {
            info = (ShortcutInfo) super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return info;
    }

    public boolean isDeletable() {
        deletable =  !(isSystemApp || mAppDownloadStatus == AppDownloadStatus.STATUS_INSTALLING);
        return deletable;
    }
    
    //get package name only for download item and application item
    public String getPackageName(){
        if(intent == null){
            return null;
        }
        String pkgname = null;
        if(itemType == LauncherSettings.Favorites.ITEM_TYPE_APPLICATION){
            ComponentName component = intent.getComponent();
            if(component != null){
                pkgname = component.getPackageName();
            }
        }else if(itemType == LauncherSettings.Favorites.ITEM_TYPE_SHORTCUT_DOWNLOADING){
            pkgname = intent.getStringExtra(AppDownloadManager.TYPE_PACKAGENAME);
        }
        return pkgname;
    }
}

