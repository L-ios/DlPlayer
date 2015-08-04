package com.tpw.homeshell.utils;

import java.util.ArrayList;

import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;

/**
 * get the infomation of private packages which are locked
 */
public class PrivacySpaceHelper extends ContentObserver{

    public static final String TAG = "PrivacySpaceHelper";
    public static final String COLUMN_PKG = "package";
    public static final Uri APPLIST_URI = Uri.parse("content://com.tpw.provider.secure/applist");
    public static final Uri SETTING_URI = Uri.parse("content://com.tpw.provider.secure/settings");

    private static PrivacySpaceHelper sInstance = null;

    public  static PrivacySpaceHelper getInstance(Context context){
        if( sInstance == null ){
            sInstance = new PrivacySpaceHelper(context);
        }
        return sInstance;
    }

    public static void destroy(){
        if( sInstance != null ){
            sInstance.onDestroy();
            sInstance = null;
        }
    }

    private Context mContext;
    private ArrayList<String> mLockedPkgs;

    private PrivacySpaceHelper(Context context) {
        super(new Handler());
        mContext = context.getApplicationContext();
        mLockedPkgs = new ArrayList<String>();
        mContext.getContentResolver().registerContentObserver(APPLIST_URI, true, this);
        getLocksPackages();
    }

    public void getLocksPackages(){
        mLockedPkgs.clear();

        ContentResolver cr = mContext.getContentResolver();
        Cursor cursor = null;
        try{
            cursor = cr.query(APPLIST_URI,new String[] {COLUMN_PKG}, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    int colomnIndex = cursor.getColumnIndex(COLUMN_PKG);
                    String pname = cursor.getString(colomnIndex);
                    mLockedPkgs.add(pname);
                } while (cursor.moveToNext());
            }
        }catch(Exception e){
            Log.e(TAG,"read locked packages list failed",e);
        }finally{
            if( cursor != null ){
                cursor.close();
            }
        }
    }

    public boolean isPackageLocked(String pkg){
        return mLockedPkgs.contains(pkg);
    }

    @Override
    public void onChange(boolean selfChange) {
        super.onChange(selfChange);
        getLocksPackages();
    }

    private void onDestroy(){
        mContext.getContentResolver().unregisterContentObserver(this);
        mLockedPkgs.clear();
    }
}
