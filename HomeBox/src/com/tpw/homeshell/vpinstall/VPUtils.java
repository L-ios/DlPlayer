package com.tpw.homeshell.vpinstall;

import android.os.Environment;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Xml;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import com.tpw.homeshell.ConfigManager;
import com.tpw.homeshell.FastBitmapDrawable;
import com.tpw.homeshell.FolderInfo;
import com.tpw.homeshell.ItemInfo;
import com.tpw.homeshell.LauncherApplication;
import com.tpw.homeshell.LauncherSettings.Favorites;
import com.tpw.homeshell.icon.IconManager;
import com.tpw.homeshell.icon.IconUtils;
import com.tpw.homeshell.model.LauncherModel;
import com.tpw.homeshell.model.LauncherModel.Callbacks;
import com.tpw.homeshell.ScreenPosition;
import com.tpw.homeshell.ShortcutInfo;
import com.tpw.homeshell.AppDownloadManager.AppDownloadStatus;
import commonlibs.utils.ACA;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.content.res.Resources.NotFoundException;
import android.content.res.XmlResourceParser;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.content.ComponentName;

public class VPUtils{
    //sdcard to /system/etc/property/vp-app
    public static final String VPINSTALLDIR = "/system/etc/property/vp-app";

    private static final String TAG = "VPUtils";
    private LauncherApplication mApp;
    public static final String TYPE_PACKAGENAME = "packagename";
    public static final String TYPE_PACKAGEPATH= "packagepath";
    private static final int VPINSTALL_ITEM_TYPE_FOLDER = 0;
    private static final int VPINSTALL_ITEM_TYPE_APK = 1;
    int mMaxFolderCountX = ConfigManager.getFolderMaxCountX();
    int mMaxFolderCount =  ConfigManager.getFolderMaxItemsCount();
    private int vpfolderItemCount = 0;

    public static class VPInstallStatus {
        public static final int STATUS_NORMAL = 0;
        public static final int STATUS_lOADING = 1;
    }
    public VPUtils(LauncherApplication app) {
        mApp = app;
    }

    public String getVPInstallDir() {
        Log.d(TAG, "apk dir is " + VPINSTALLDIR);
        return VPINSTALLDIR;
        //return Environment.getExternalStorageDirectory() + "/" + VPINSTALLDIR;
    }

    public List<PackageInfo> ScanVPInstallDir() {
        File dir = new File(getVPInstallDir());
        File[] fs = dir.listFiles();

        if (fs == null )
            return null;

        List<File> files = Arrays.asList(dir.listFiles());
        if (files == null || files.size() == 0)
            return null;

        Collections.sort(files, new Comparator<File>() {
            public int compare(File f1, File f2) {
                return f1.getName().compareToIgnoreCase(f2.getName());
            }
        });

        List<PackageInfo> packageList = new ArrayList<PackageInfo>();
        Set<String> set = new HashSet<String>();
        PackageManager pm = mApp.getContext().getPackageManager();
        for (File f : files) {
            String name = f.getName().toLowerCase();
            if (name.endsWith(".apk")) {
                PackageInfo packageInfo = pm.getPackageArchiveInfo(f.getAbsolutePath(),
                        PackageManager.GET_ACTIVITIES);
                if (packageInfo == null) {
                    continue;
                }
                packageInfo.applicationInfo.sourceDir = f.getAbsolutePath();
                packageInfo.applicationInfo.publicSourceDir = f.getAbsolutePath();

                if (set.add(packageInfo.packageName) == true) {
                    packageList.add(packageInfo);
                } else {
                    Log.d(TAG, "package " + packageInfo.packageName + " is exist");
                }
                Log.d(TAG, "package name is " + packageInfo.packageName);
            }
        }
        set.clear();
        return packageList;
    }

    public ShortcutInfo createVPInstallShortcutInfo(PackageInfo pkgInfo,
             ScreenPosition screenPosition, long containerType) {
        Log.d(TAG, "createVPInstallShortcutInfo begin");
        if ((pkgInfo == null) || (screenPosition == null)) {
            return null;
        }
        ShortcutInfo shortcutInfo = new ShortcutInfo();
        PackageManager pm = LauncherApplication.getContext().getPackageManager();
        if (pkgInfo.applicationInfo == null) {
            Log.d(TAG, "pkgInfo.applicationInfo is null");
            return null;
        }

        shortcutInfo.title = pkgInfo.applicationInfo.loadLabel(pm);
        shortcutInfo.intent = new Intent();

        ComponentName comname = new ComponentName(pkgInfo.packageName, "vpinstall");
        Log.d(TAG, "component name is: " + comname);
        shortcutInfo.intent.setComponent(comname);
        shortcutInfo.intent.putExtra(TYPE_PACKAGENAME, pkgInfo.packageName);
        shortcutInfo.intent.putExtra(TYPE_PACKAGEPATH, pkgInfo.applicationInfo.sourceDir);
        shortcutInfo.itemType = Favorites.ITEM_TYPE_VPINSTALL;
        shortcutInfo.screen = screenPosition.s;
        shortcutInfo.cellX = screenPosition.x;
        shortcutInfo.cellY = screenPosition.y;
        shortcutInfo.container = containerType;
        shortcutInfo.customIcon = true;

        String apkPath = pkgInfo.applicationInfo.sourceDir;
        //PackageParser packageParser = new PackageParser(apkPath);
        DisplayMetrics metrics = new DisplayMetrics();
        metrics.setToDefaults();
        Drawable icon = null;
//        if (packageParser != null) {
//        PackageParser.Package mPkgInfo = packageParser.parsePackage(new File(apkPath), apkPath, metrics, 0);
//        ApplicationInfo info = mPkgInfo.applicationInfo;
        ApplicationInfo info = pkgInfo.applicationInfo;
        Resources pRes = mApp.getResources();
//        AssetManager assmgr = new AssetManager();
//        assmgr.addAssetPath(apkPath);
        try{
            AssetManager assmgr = AssetManager.class.getConstructor(null).newInstance(null);
            AssetManager.class.getMethod("addAssetPath", String.class).invoke(
                    null, apkPath);

            Resources res = null;
            if (pRes != null) {
                res = new Resources(assmgr, pRes.getDisplayMetrics(), pRes.getConfiguration());
            }
            if ((info != null) && (info.icon != 0) && (res != null)) {
                icon = res.getDrawable(info.icon);
            }
    } catch (Exception ex) {
        Log.e(TAG, "getAppOriginalIcon error");
    }
//        }

        //Drawable icon = pkgInfo.applicationInfo.loadIcon(pm);

        Drawable finalicon = null;

        if (icon == null) {
            Log.d(TAG, "icon from package info is null");
            finalicon = ((LauncherApplication)mApp.getApplicationContext()).getIconManager().getDefaultIcon();
        } else {
            Bitmap bitmap = ((BitmapDrawable) icon).getBitmap();
            finalicon = new FastBitmapDrawable(bitmap.copy(Bitmap.Config.ARGB_8888, true));
            bitmap.recycle();
        }
        if (finalicon == null) {
            return null;
        }

        //cancel the mark icon
        /*
        Bitmap newBitmap = ((FastBitmapDrawable)finalicon).getBitmap();
        //paint a mark picture on the app icon, means it is a vp app
        int id = mApp.getResources().getIdentifier("hand", "drawable", "com.tpw.homeshell");
        Bitmap markBitmap = BitmapFactory.decodeResource(mApp.getResources(), id);

        if (markBitmap != null) {
            Matrix matrix = new Matrix();
            Bitmap resizeBmp;
            //resize the mark same as the size of app icon
            if ((newBitmap.getWidth() != markBitmap.getWidth()) ||
                (newBitmap.getHeight() != markBitmap.getHeight())) {
                matrix.postScale(((float)newBitmap.getWidth())/((float)markBitmap.getWidth()),
                                 ((float)newBitmap.getHeight())/((float)markBitmap.getHeight()));
                resizeBmp = Bitmap.createBitmap(markBitmap, 0, 0, markBitmap.getWidth(),
                        markBitmap.getHeight(), matrix, true);
            } else {
                resizeBmp = markBitmap;
            }

            Canvas canvas = new Canvas(newBitmap);
            Paint paint = new Paint();
            canvas.drawBitmap(resizeBmp, 0, 0, paint);
            canvas.save(Canvas. ALL_SAVE_FLAG);
            canvas.restore();
            markBitmap.recycle();
            resizeBmp.recycle();
        }
       */
        shortcutInfo.setIcon(finalicon);
        //shortcutInfo.setAppId(appId);
        Log.d(TAG, "createVPInstallShortcutInfo end");
        return shortcutInfo;
    }

    private final class InstallerRunable implements Runnable {
        private final ShortcutInfo info;

        private InstallerRunable(ShortcutInfo info) {
            this.info = info;
        }

        @Override
        public void run() {
            new VPInstaller(mApp, mApp.getModel()).installSilently(info.intent.getStringExtra(VPUtils.TYPE_PACKAGEPATH), info);
        }
    }

    public boolean installSilently(ShortcutInfo shortcutInfo) {
        boolean result = true;
        Log.d(TAG, "installSilently path is " + shortcutInfo.intent.getStringExtra(TYPE_PACKAGEPATH));
        //VPInstaller installer = new VPInstaller(mApp, null);
        if (shortcutInfo.getVPInstallStatus() == VPInstallStatus.STATUS_NORMAL) {
            shortcutInfo.setVPInstallStatus(VPInstallStatus.STATUS_lOADING);
            shortcutInfo.setProgress(100);
            //Toast.makeText(context, "start intall app " + shortcutInfo.title, Toast.LENGTH_SHORT).show();
        } else {
            Log.d(TAG, shortcutInfo.intent.getStringExtra(TYPE_PACKAGENAME) + " already in install");
            return false;
        }
        new Thread(new InstallerRunable(shortcutInfo)).start();
        return result;
    }

    public FolderInfo CheckOrMakeVPInstallFolder(HashMap<Long, FolderInfo> folders) {
        // See if a vp install placeholder was created for us already
        FolderInfo vpfolder = null;
        for(FolderInfo folder: folders.values()) {
            if (folder.title.equals("vpinstall")) {
                vpfolder = folder;
            }
        }
        if ((vpfolder != null) && (vpfolder.contents.size() >= mMaxFolderCount)) {
            //vpfolder is full, the vp item has to be put in workspace
            return null;
        }
        if (vpfolder == null) {
            // No placeholder -- create a new instance
            vpfolder = new FolderInfo();
            vpfolder.title = "vpinstall";
            ScreenPosition pos = LauncherModel.findEmptyCell();
            if (pos == null) {
                return null;
            }
            vpfolder.cellX = pos.x;
            vpfolder.cellY = pos.y;
            vpfolder.screen = pos.s;
            vpfolder.container = Favorites.CONTAINER_DESKTOP;
            LauncherModel.addItemToDatabase(mApp, vpfolder, Favorites.CONTAINER_DESKTOP, pos.s, pos.x, pos.y, false);
            final Callbacks callbacks = LauncherModel.mCallbacks != null ? LauncherModel.mCallbacks.get() : null;
            final ArrayList<ItemInfo> infos = new ArrayList<ItemInfo>(1);
            infos.add(vpfolder);

            //final Callbacks callbacks = LauncherModel.mCallbacks != null ? LauncherModel.mCallbacks.get() : null;
            final HashMap<Long, FolderInfo> folderinfos = new HashMap<Long, FolderInfo>(1);
            folderinfos.put(vpfolder.id, vpfolder);
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
        }
        vpfolderItemCount = vpfolder.contents.size();
        return vpfolder;
    }

    public ScreenPosition findPositionInFolder(FolderInfo vpfolder) {
        Log.d(TAG, "folder content size is " + vpfolderItemCount);
        if (vpfolderItemCount >= mMaxFolderCount) {
            Log.d(TAG, "return null position");
            return null;
        }

        int x = vpfolderItemCount % mMaxFolderCountX;
        int y = vpfolderItemCount / mMaxFolderCountX;
        ScreenPosition pos = new ScreenPosition(0, x, y);
        vpfolderItemCount++;
        return pos;
    }

    //get vp items from vp_install_items.xml and check whether the items are exist in special path 
    public List<VPInstallItem> getVPInstallList() {
        List<VPInstallItem> packageList = new ArrayList<VPInstallItem>();
        //XmlResourceParser parser = null;
        XmlPullParser parser = null;
        FileInputStream in = null;
        File xmlfile = null;
        try {
            /*
            int xmlid = mApp.getResources().getIdentifier("vp_install_items", "xml", "com.tpw.homeshell");
            parser = mApp.getResources().getXml(xmlid);
            */
            //get xml from sdcard begin
            xmlfile = new File(getVPInstallDir() + "/" + "vp_install_items.xml");
            if ((xmlfile == null) || (xmlfile.exists() == false)) {
                return packageList;
            }
            in = new FileInputStream(xmlfile);
            parser = Xml.newPullParser();
            parser.setInput(in, null);
            //get xml from sdcard end

            int event = parser.getEventType();
            String currentFolderName = null;
            int currentFolderPosition = 0;
            VPInstallItem currentFolderItem = null;
            PackageManager pm = mApp.getPackageManager();
            VPInstallItem currentFavoriteItem = null;

            while(event!=XmlPullParser.END_DOCUMENT){
                switch(event){
                    case XmlPullParser.START_DOCUMENT:
                        Log.d(TAG, "doc start " + parser.getName());
                        break;
                    case XmlPullParser.START_TAG:
                        Log.d(TAG, "tag start " + parser.getName());
                        if (parser.getName().equals("folder")) {
                            VPInstallItem vpFolderItem = new VPInstallItem();
                            vpFolderItem.position = null;
                            Log.d(TAG, "folder attribute count is " + parser.getAttributeCount());
                            if (parser.getAttributeCount() > 0) {
                                //folder title
                                currentFolderName = parser.getAttributeValue(0);
                            }
                            if (parser.getAttributeCount() >= 4) {
                                //get folder's position
                                int screen = Integer.valueOf(parser.getAttributeValue(1));
                                int cellx = Integer.valueOf(parser.getAttributeValue(2));
                                int celly = Integer.valueOf(parser.getAttributeValue(3));
                                vpFolderItem.position = LauncherModel.isCellEmtpy(screen, cellx, celly);
                            }
                            if (vpFolderItem.position == null) {
                                vpFolderItem.position = LauncherModel.findEmptyCell();
                            }
                            currentFolderPosition = 0;
                            Log.d(TAG, "folder name is " + currentFolderName);

                            vpFolderItem.name = currentFolderName;
                            vpFolderItem.itemType = VPINSTALL_ITEM_TYPE_FOLDER;
                            packageList.add(vpFolderItem);
                            currentFolderItem = vpFolderItem;
                        } else if (parser.getName().equals("favorite")) {
                            if (currentFolderPosition >= mMaxFolderCount) {
                                break;
                            }
                            VPInstallItem vpInstallItem = new VPInstallItem();
                            vpInstallItem.name = parser.getAttributeValue(0);
                            String itemAbsolutePath = null;
                            if (vpInstallItem.name.startsWith("/")) {
                                Log.d(TAG, "item path is special");
                                itemAbsolutePath = vpInstallItem.name;
                            } else {
                                Log.d(TAG, "item path is scanpath");
                                itemAbsolutePath = getVPInstallDir() + "/" + vpInstallItem.name;
                            }
                            Log.d(TAG, "install item name is " + vpInstallItem.name);
                            Log.d(TAG, "install item path is " + itemAbsolutePath);
                            PackageInfo packageInfo = pm.getPackageArchiveInfo(itemAbsolutePath,
                                    PackageManager.GET_ACTIVITIES);
                            if ((packageInfo != null) &&
                                (isVPInstallItemExist(packageInfo) == false) &&
                                (isInCurrentList(vpInstallItem.name, packageList) == false)){
                                packageInfo.applicationInfo.sourceDir = itemAbsolutePath;
                                packageInfo.applicationInfo.publicSourceDir = itemAbsolutePath;
                                vpInstallItem.foldername = currentFolderName;
                                vpInstallItem.itemType = VPINSTALL_ITEM_TYPE_APK;
                                vpInstallItem.position = new ScreenPosition(0, currentFolderPosition % mMaxFolderCountX, currentFolderPosition / mMaxFolderCountX);
                                currentFolderPosition++;
                                vpInstallItem.pkgInfo = packageInfo;
                                packageList.add(vpInstallItem);
                                currentFavoriteItem = vpInstallItem;
                            } else {
                                Log.d(TAG, "can't get package info of " + vpInstallItem.name);
                            }
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        Log.d(TAG, "tag end " + parser.getName());
                        if (parser.getName().equals("folder")) {
                            //check empty folder, if folder is empty, remove it from list
                            if (currentFolderPosition == 0 || currentFolderPosition == 1) {
                                if ((currentFolderPosition == 1) &&
                                    (currentFolderItem != null) &&
                                    (currentFolderItem.position != null) &&
                                    (currentFavoriteItem != null) &&
                                    (currentFavoriteItem.position != null)) {
                                    //set folder's postion to the only favorite item
                                    currentFavoriteItem.position.s = currentFolderItem.position.s;
                                    currentFavoriteItem.position.x = currentFolderItem.position.x;
                                    currentFavoriteItem.position.y = currentFolderItem.position.y;
                                }
                                Log.d(TAG, "the folder " + currentFolderItem.name + " is empty. remove it");
                                packageList.remove(currentFolderItem);
                            }
                        }
                        break;
                    case XmlPullParser.END_DOCUMENT:
                        Log.d(TAG, "doc end " + parser.getName());
                        break;
                }
                event = parser.next();
            }
        } catch (XmlPullParserException e) {
            Log.d(TAG, "parser xml XmlPullParserException");
            e.printStackTrace();
        } catch (IOException e) {
            Log.d(TAG, "parser xml IOException");
            e.printStackTrace();
        } catch (NotFoundException e) {
            Log.d(TAG, "parser xml NotFoundException");
            e.printStackTrace();
        } catch (NullPointerException e) {
            Log.d(TAG, "parser xml NullPointerException");
            e.printStackTrace();
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        Log.d(TAG, "item count in list is " + packageList.size());
        return packageList;
    }

    private boolean isInCurrentList(String name, List<VPInstallItem> packageList) {
        for (int i = 0; i < packageList.size(); i++) {
            if (name.equals(packageList.get(i).name) == true) {
                Log.d(TAG, name + " is already in current package list");
                return true;
            }
        }
        return false;
    }

    public boolean isVPInstallItemExist(PackageInfo pkgInfo) {
            boolean ret = false;
            String packagename = pkgInfo.packageName;
            ArrayList<ItemInfo> allApps = LauncherModel.getAllAppItems();
            for(ItemInfo info: allApps) {
                if ((info.itemType == Favorites.ITEM_TYPE_NOSPACE_APPLICATION) ||
                    (info.itemType == Favorites.ITEM_TYPE_APPLICATION)) {
                    if (((ShortcutInfo)info).intent.getComponent().getPackageName().equals(packagename)) {
                        ret = true;
                        Log.d(TAG, packagename + " is installed");
                    }
                } else if ((info.itemType == Favorites.ITEM_TYPE_VPINSTALL) ||
                           (info.itemType == Favorites.ITEM_TYPE_SHORTCUT_DOWNLOADING)){
                    if (((ShortcutInfo)info).intent.getStringExtra(TYPE_PACKAGENAME) != null) {
                        if (((ShortcutInfo)info).intent.getStringExtra(TYPE_PACKAGENAME).equals(packagename)) {
                            ret = true;
                            Log.d(TAG, packagename + " is exist");
                        }
                    }
                }
            }
            return ret;
    }

    public class VPInstallItem {
        int itemType;
        PackageInfo pkgInfo;
        String name;
        String foldername;
        ScreenPosition position;
    }

    public ArrayList<ItemInfo> createVPInstallShortcutInfos() {
        List<VPInstallItem> vpInstallList = getVPInstallList();
        ArrayList<ItemInfo> iteminfos = new ArrayList<ItemInfo>();
        HashMap<String, Long> createdFolderMap = new HashMap<String, Long>();
        //create all folder items
        Log.d(TAG, "create all folder items first");
        for (int i = 0; i < vpInstallList.size(); i++) {
            VPInstallItem vpInstallItem = vpInstallList.get(i);
            if ((vpInstallItem != null) && (vpInstallItem.itemType == VPINSTALL_ITEM_TYPE_FOLDER)) {
                FolderInfo vpfolder = new FolderInfo();
                if (vpInstallItem.position == null) {
                    vpInstallItem.position = LauncherModel.findEmptyCell();
                }
                if (vpInstallItem.position == null) {
                    continue;
                }
                vpfolder.title = vpInstallItem.name;
                vpfolder.cellX = vpInstallItem.position.x;
                vpfolder.cellY = vpInstallItem.position.y;
                vpfolder.screen = vpInstallItem.position.s;
                vpfolder.container = Favorites.CONTAINER_DESKTOP;
                LauncherModel.addItemToDatabase(mApp, vpfolder, Favorites.CONTAINER_DESKTOP,
                                                                  vpInstallItem.position.s, vpInstallItem.position.x,
                                                                  vpInstallItem.position.y, false);
                iteminfos.add(vpfolder);
                createdFolderMap.put(vpfolder.title.toString(), vpfolder.id);
            }
        }
        //create all vp install app items
        Log.d(TAG, "create all vp install app items");
        for (int i = 0; i < vpInstallList.size(); i++) {
            VPInstallItem vpInstallItem = vpInstallList.get(i);
            if ((vpInstallItem != null) && (vpInstallItem.itemType == VPINSTALL_ITEM_TYPE_APK)) {
                Log.d(TAG, "item's folder name is " + vpInstallItem.foldername);
                //find the folder
                FolderInfo myfolder = null;
                long folderid = -1;
                if (createdFolderMap.containsKey(vpInstallItem.foldername)) {
                    folderid = createdFolderMap.get(vpInstallItem.foldername);
                    Log.d(TAG, "my folder id is " + folderid);
                    myfolder = LauncherModel.getSBgFolders().get(folderid);
                } else {
                    Log.d(TAG, "can not find folder " + vpInstallItem.foldername);
                }

                boolean isFolderNotNull = (myfolder != null);
                long container = Favorites.CONTAINER_DESKTOP;
                ScreenPosition pos = vpInstallItem.position;
                if (isFolderNotNull) {
                    container = myfolder.id;
                }
                if (pos == null) {
                    pos = LauncherModel.findEmptyCell();
                }
                ShortcutInfo shortcutInfo = createVPInstallShortcutInfo(vpInstallItem.pkgInfo, pos, container);
                if (shortcutInfo == null) {
                    continue;
                }

                Log.d(TAG, "before add item to db");
                LauncherModel.addItemToDatabase(mApp, shortcutInfo, shortcutInfo.container, pos.s, pos.x, pos.y, false);
                iteminfos.add(shortcutInfo);
                Drawable orgIcon = shortcutInfo.mIcon;
                shortcutInfo.setIcon(mApp.getIconManager().buildUnifiedIcon(orgIcon));
            }
        }
        createdFolderMap.clear();
        vpInstallList.clear();
        return iteminfos;
    }


    public static CharSequence getVpinstallLabel(String appPackageFilePath) {
        if (TextUtils.isEmpty(appPackageFilePath)) {
            return null;
        }
        PackageManager pm = LauncherApplication.getContext().getPackageManager();
        PackageInfo packageInfo = pm.getPackageArchiveInfo(appPackageFilePath, PackageManager.GET_ACTIVITIES);
        if (packageInfo == null || packageInfo.applicationInfo == null) {
            return null;
        }
        packageInfo.applicationInfo.sourceDir = appPackageFilePath;
        packageInfo.applicationInfo.publicSourceDir = appPackageFilePath;
        return packageInfo.applicationInfo.loadLabel(pm);
    }

    public static Drawable getAppOriginalIcon(Context context, ShortcutInfo info) {
        if ((info == null) || (info.intent == null) ||
            (info.intent.getComponent() == null) || (context == null)) {
            return null;
        }
        String pkgName = info.intent.getComponent().getPackageName();
        return getAppOriginalIcon(context, pkgName);
    }

    public static Drawable getAppOriginalIcon(Context context, String pkgName) {
        Log.d(TAG, "getAppOriginalIcon pkgName=" + pkgName);
        if (TextUtils.isEmpty(pkgName)) {
            return null;
        }
        if (context == null) {
            context = LauncherApplication.getContext();
        }
        PackageManager pm = context.getApplicationContext().getPackageManager();
        PackageInfo pkginfo = null;
        Log.d(TAG, "getAppOriginalIcon for " + pkgName);
        try {
            pkginfo = pm.getPackageInfo(pkgName, 0);
            Log.d(TAG, "apk is installed");
        } catch (Exception e) {
            Log.d(TAG, "PackageManager.getPackageInfo failed for " + pkgName);
            pkginfo = null;
        }
        if ((pkginfo == null) || (pkginfo.applicationInfo == null)) {
            return null;
        }
        try {
            String apkPath = pkginfo.applicationInfo.sourceDir;
            Object packageParser = ACA.PackageParser.getInstance(apkPath);
//            PackageParser packageParser = new PackageParser(apkPath);
            DisplayMetrics metrics = new DisplayMetrics();
            metrics.setToDefaults();
            Drawable icon = null;
//            if (packageParser != null) {
//                PackageParser.Package mPkgInfo = packageParser.parsePackage(new File(apkPath), apkPath, metrics, 0);
          if (packageParser != null) {
              Object objmPkgInfo = ACA.PackageParser.parsePackage(packageParser, new File(apkPath), apkPath, metrics, 0);
              ApplicationInfo appinfo = ACA.PackageParser.Package.applicationInfo(objmPkgInfo);
            Resources pRes = context.getApplicationContext().getResources();
//            AssetManager assmgr = new AssetManager();
//            assmgr.addAssetPath(apkPath);
            AssetManager assmgr = AssetManager.class.getConstructor(null).newInstance(null);
            ACA.AssetManager.addAssetPath(assmgr, apkPath);

            Resources res = null;
            if (pRes != null) {
                res = new Resources(assmgr, pRes.getDisplayMetrics(), pRes.getConfiguration());
            }
            if ((appinfo != null) && (appinfo.icon != 0) && (res != null)) {
                try {
                    icon = res.getDrawable(appinfo.icon);
                    if (icon != null) {
                        icon = IconUtils.scaleImg(icon);
                    }
                } catch (OutOfMemoryError e) {
                    // TODO: handle exception
                }
            }
            }
            Log.d(TAG, "getAppOriginalIcon out");
            return icon;
        } catch (Exception ex) {
            Log.e(TAG, "getAppOriginalIcon error");
            return null;
        }
    }
}
