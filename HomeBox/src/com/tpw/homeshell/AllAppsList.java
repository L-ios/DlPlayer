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
import java.util.Collections;
import java.util.List;

import com.tpw.homeshell.appfreeze.AppFreezeUtil;
import com.tpw.homeshell.model.LauncherModel;
import com.tpw.homeshell.utils.Utils;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Process;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;

/**
 * Stores the list of all applications for the all apps view.
 */
public class AllAppsList {
    public static final int DEFAULT_APPLICATIONS_NUMBER = 42;

    private static final String TAG = "AllAppsList";
    
    /** The list off all apps. */
    public ArrayList<ApplicationInfo> data =
            new ArrayList<ApplicationInfo>(DEFAULT_APPLICATIONS_NUMBER);
    /** The list of apps that have been added since the last notify() call. */
    public ArrayList<ApplicationInfo> added =
            new ArrayList<ApplicationInfo>(DEFAULT_APPLICATIONS_NUMBER);
    /** The list of apps that have been removed since the last notify() call. */
    public ArrayList<ApplicationInfo> removed = new ArrayList<ApplicationInfo>();
    /** The list of apps that have been modified since the last notify() call. */
    public ArrayList<ApplicationInfo> modified = new ArrayList<ApplicationInfo>();
    
    /**
     * Boring constructor.
     */
    public AllAppsList() {
    }

    /**
     * Add the supplied ApplicationInfo objects to the list, and enqueue it into the
     * list to broadcast when notify() is called.
     *
     * If the app is already in the list, doesn't add it.
     */
    public void add(ApplicationInfo info) {
        if (findActivity(data, info.componentName)) {
            Log.d(TAG,"found activity in AllAppsList.add:"+info.componentName,new Exception());
            return;
        }
        data.add(info);
        added.add(info);
    }
    
    public void clear() {
        data.clear();
        // TODO: do we clear these too?
        added.clear();
        removed.clear();
        modified.clear();
    }

    public int size() {
        return data.size();
    }

    public ApplicationInfo get(int index) {
        return data.get(index);
    }

    /**
     * Add the icons for the supplied apk called packageName.
     */
    public boolean addPackage(Context context, String packageName) {
        boolean ret = false;
        final List<ResolveInfo> matches = findActivitiesForPackage(context, packageName);

        if (matches.size() > 0) {
            for (ResolveInfo info : matches) {
                add(new ApplicationInfo(context.getPackageManager(), info, null));
            }
            ret = true;
        } else if (AppFreezeUtil.isPackageFrozen(context, packageName)) {
            PackageManager pm = context.getPackageManager();
            PackageInfo pkgInfo = null;
            try {
                pkgInfo = pm.getPackageInfo(packageName, 0);
            } catch (NameNotFoundException e) {
            }
            if (pkgInfo != null && pkgInfo.applicationInfo != null) {
                ComponentName componentName = new ComponentName(packageName, "");
                String label = pkgInfo.applicationInfo.loadLabel(pm).toString();
                add(new ApplicationInfo(pm, componentName, label, null));
                ret = true;
            }
            Log.d(TAG, "addPackage: frozen package=" + packageName + " added=" + ret);
        }
        return ret;
    }

    /**
     * Remove the apps for the given apk identified by packageName.
     */
    public void removePackage(String packageName) {
        final List<ApplicationInfo> data = this.data;
        for (int i = data.size() - 1; i >= 0; i--) {
            ApplicationInfo info = data.get(i);
            final ComponentName component = info.intent.getComponent();
            if (packageName.equals(component.getPackageName())) {
                removed.add(info);
                data.remove(i);
            }
        }
    }

    public void removePackageFromData(String packageName) {
        if (packageName == null) {
            return;
        }
        Log.d(TAG, "removePackageFromData " + packageName);
        final List<ApplicationInfo> data = this.data;
        for (int i = data.size() - 1; i >= 0; i--) {
            ApplicationInfo info = data.get(i);
            final ComponentName component = info.intent.getComponent();
            if (packageName.equals(component.getPackageName())) {
                data.remove(i);
            }
        }
    }

    /**
     * Add and remove icons for this package which has been updated.
     */
    public void updatePackage(Context context, String packageName) {
        final List<ResolveInfo> matches = findActivitiesForPackage(context, packageName);
        if (matches.size() > 0) {
            ApplicationInfo lastRemoved = null;

            // Find disabled/removed activities and remove them from data and add them
            // to the removed list.
            for (int i = data.size() - 1; i >= 0; i--) {
                final ApplicationInfo applicationInfo = data.get(i);
                final ComponentName component = applicationInfo.intent.getComponent();
                if (packageName.equals(component.getPackageName())) {
                    String className = component.getClassName();
                    if (TextUtils.isEmpty(className)) {
                        Intent newIntent = context.getPackageManager().getLaunchIntentForPackage(packageName);
                        if (newIntent != null) applicationInfo.intent = newIntent;
                    } else if (!findActivity(matches, component)) {
                        removed.add(applicationInfo);
                        data.remove(i);
                        lastRemoved = applicationInfo;
                    }
                }
            }

            // Find enabled activities and add them to the adapter
            // Also updates existing activities with new labels/icons
            int count = matches.size();
            for (int i = 0; i < count; i++) {
                final ResolveInfo info = matches.get(i);
                ApplicationInfo applicationInfo = findApplicationInfoLocked(
                        info.activityInfo.applicationInfo.packageName,
                        info.activityInfo.name);
                if (applicationInfo == null) {
                    ApplicationInfo appinfo = new ApplicationInfo(context.getPackageManager(), info,null);
                    if (findItemInModel(appinfo) == true) {
                        if (!findActivity(data, appinfo.componentName)) {
                            data.add(appinfo);
                            modified.add(appinfo);
                        }
                    } else if (count == 1 && lastRemoved != null) {
                        // In this case, we update the old app-info instead of create
                        // a new object.
                        removed.remove(lastRemoved);
                        data.add(lastRemoved);
                        modified.add(lastRemoved);
                        // the old app-info will be updated in LauncherModel.parseUpdatedApps().
                        lastRemoved.updateLater(info);
                    } else {
                        add(appinfo);
                    }
                } else {
                    modified.add(applicationInfo);
                }
            }
        } else if (Hideseat.isHideseatEnabled() &&
                   AppFreezeUtil.isPackageFrozen(context, packageName)) {
            for (int i = data.size() - 1; i >= 0; i--) {
                final ApplicationInfo info = data.get(i);
                final ComponentName component = info.intent.getComponent();
                if (packageName.equals(component.getPackageName())) {
                    modified.add(info);
                }
            }
        } else {
            // Remove all data for this package, as long as the package is not frozen by hide-seat
            for (int i = data.size() - 1; i >= 0; i--) {
                final ApplicationInfo applicationInfo = data.get(i);
                final ComponentName component = applicationInfo.intent.getComponent();
                if (packageName.equals(component.getPackageName())) {
                    removed.add(applicationInfo);
                    data.remove(i);
                }
            }
        }
    }

    //install sd app after removed sd card, item in installing state after installed
    private boolean findItemInModel(ApplicationInfo appInfo) {
        boolean ret = false;
        if ((appInfo == null) ||
            (appInfo.intent == null) ||
            (appInfo.intent.getComponent() == null)) {
            return ret;
        }
        String componentname = appInfo.intent.getComponent().toString();
        if (componentname == null) {
            return ret;
        }
        Log.d(TAG, "appinfo component name is " + componentname.toString());
        for (ItemInfo item: LauncherModel.getAllAppItems()) {
            if (item instanceof ShortcutInfo) {
                Intent intent = ((ShortcutInfo)item).intent;
                if ((intent != null) && (intent.getComponent() != null)) {
                    if (intent.getComponent().toString().equals(componentname)) {
                        ret = true;
                        break;
                    }
                }
            }
        }
        return ret;
    }

    /**
     * Query the package manager for MAIN/LAUNCHER activities in the supplied package.
     */
    public static List<ResolveInfo> findActivitiesForPackage(Context context, String packageName) {
        final PackageManager packageManager = context.getPackageManager();

        final Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        mainIntent.setPackage(packageName);

        final List<ResolveInfo> apps = packageManager.queryIntentActivities(mainIntent, 0);
        return apps != null ? apps : new ArrayList<ResolveInfo>();
    }

    /**
     * Returns whether <em>apps</em> contains <em>component</em>.
     */
    private static boolean findActivity(List<ResolveInfo> apps, ComponentName component) {
        final String className = component.getClassName();
        for (ResolveInfo info : apps) {
            final ActivityInfo activityInfo = info.activityInfo;
            if (activityInfo.name.equals(className)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns whether <em>apps</em> contains <em>component</em>.
     */
    private static boolean findActivity(ArrayList<ApplicationInfo> apps, ComponentName component) {
        final int N = apps.size();
        for (int i=0; i<N; i++) {
            final ApplicationInfo info = apps.get(i);
            if (info.componentName.equals(component)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Find an ApplicationInfo object for the given packageName and className.
     */
    private ApplicationInfo findApplicationInfoLocked(String packageName, String className) {
        for (ApplicationInfo info: data) {
            final ComponentName component = info.intent.getComponent();
            if (packageName.equals(component.getPackageName())
                    && className.equals(component.getClassName())) {
                return info;
            }
        }
        return null;
    }
    
    // get all applicaitons with ACTION_MAIN & CATEGORY_LAUNCHER
    public static List<ResolveInfo> getAllActivity(Context context){
        Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        PackageManager pm = context.getPackageManager();

        List<ResolveInfo> apps = null;
        try{
            apps =  pm.queryIntentActivities(mainIntent, 0);
        }catch(Exception ex){
            if(ex.getCause() instanceof RemoteException){
                //java.lang.RuntimeException: Package manager has died
                Log.e(TAG, "Package manager has died, so kill myself");
                Process.killProcess(Process.myPid());
            }
        }
        if (apps == null) {
            Log.e(TAG, "getAllApps apps is null");
            return new ArrayList<ResolveInfo>();
        }

        //load and display current screen first during homeshell start
        //cancel the sort to save launching time
        //Collections.sort(apps, new ResolveInfo.DisplayNameComparator(pm));
        return  apps;
    }
    public static ResolveInfo findActivityInfo(List<ResolveInfo> apps,
            ComponentName component) {
        final int N = apps.size();
        for (int i=0; i<N; i++) {
            final ResolveInfo info = apps.get(i);
            final ComponentName other = new ComponentName(info.activityInfo.packageName,
                    info.activityInfo.name);
            if (other.equals(component)) {
                return info;
            }
        }
        return null;
    }
}
