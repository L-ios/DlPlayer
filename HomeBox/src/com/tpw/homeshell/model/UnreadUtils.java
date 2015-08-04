/**
 */

package com.tpw.homeshell.model;

import android.database.Cursor;
import android.net.Uri;
import android.provider.CallLog.Calls;
import android.util.Log;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.ContentResolver;
import com.tpw.homeshell.ShortcutInfo;
import com.tpw.homeshell.LauncherSettings;

public class UnreadUtils {
    
    private static final boolean DEBUG = false;
    private static final String TAG = "Utils";
    
    public static int getMissedCallCount(Context context) {
        int count = 0;
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(
                    Calls.CONTENT_URI,
                    null,
                    Calls.NEW + "='" + 1 + "'" + " AND " + Calls.TYPE + "='"
                            + Calls.MISSED_TYPE + "'", null, null);
            if (null != cursor) {
                count = cursor.getCount();
            }
            if (DEBUG) {
                Log.d(TAG, "getMissedCallCount:count=" + count);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if(cursor != null){
                cursor.close();
            }
            if (DEBUG) {
                Log.i(TAG, "getMissedCallCount:countE=" + count);
            }
        }
        return count;
    }

    public static int getUnreadSmsCount(Context context) {
        int count = 0;
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(Uri.parse("content://sms"), null,
                    "type = 1 and read = 0", null, null);
            if (null != cursor) {
                count = cursor.getCount();
            }
            if (DEBUG) {
                Log.d(TAG, "getUnreadSmsCount:count=" + count);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if(cursor != null){
                cursor.close();
            }
            if (DEBUG) {
                Log.i(TAG, "getUnreadSmsCount:countE=" + count);
            }
        }
        return count;
    }

    public static int getUnreadMmsCount(Context context) {
        int count = 0;
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(Uri.parse("content://mms/inbox"),
                    null, "read = 0", null, null);
            if (null != cursor) {
                count = cursor.getCount();
            }
            if (DEBUG) {
                Log.d(TAG, "getUnreadMmsCount:count=" + count);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if(cursor != null){
                cursor.close();
            }
            if (DEBUG) {
                Log.d(TAG, "getUnreadMmsCount:countE=" + count);
            }
        }
        return count;
    }
    
    public static void syncItemMessageNum(ContentResolver contentResolver, ShortcutInfo info, int messageNum){
//        int missCallCount = getMissedCallCount(context);
//        int unreadSmsCount = getUnreadSmsCount(context) + getUnreadMmsCount(context);
        if (messageNum != info.messageNum) {
            info.messageNum = messageNum;
            final Uri uri = LauncherSettings.Favorites.getContentUri(info.id, false);
            ContentValues values = new ContentValues();
            values.put(LauncherSettings.Favorites.MESSAGE_NUM, messageNum);
            contentResolver.update(uri, values, null, null);
        }
    }
}
