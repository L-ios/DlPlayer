
package com.tpw.homeshell;

import java.io.Serializable;
import java.util.List;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.text.TextUtils;
import android.util.Log;
import android.net.Uri;

import com.tpw.app.TpwNotification;
import com.tpw.app.TpwNotification.IconData;
import com.tpw.homeshell.LauncherSettings.Favorites;
import com.tpw.homeshell.model.LauncherModel;
//import com.tpw.homeshell.datamodeItemInfoHelper;
//import com.tpw.homeshell.folder.UserFolderInfo;
//import com.tpw.homeshell.utils.Log;

public class IconDigitalMarkHandler {
    private final String TAG = "IconDigitalMarkHandler";
    private final int MSM_ID = 198311;
    public final static String ACTION_APPLICATION_NOTIFICATION = "com.tpw.action.application.notification";
    private static IconDigitalMarkHandler mInstance = null;

    
    public static IconDigitalMarkHandler getInstance() {
    	if (mInstance == null) {
    		mInstance = new IconDigitalMarkHandler();
    	}

    	return mInstance;
    }

    private IconDigitalMarkHandler() {

    }

    public void handleNotificationIntent(Context context, Intent intent) {
        
        Log.d(TAG, " NotificationReceiver:intent=" + intent);
        if (intent == null) {
            Log.e(TAG, "onReceiver intent is null.");
            return;
        }
        
        String action = intent.getAction();
        String className = null;
        String packageName = null;
        int number = 0;
        if (ACTION_APPLICATION_NOTIFICATION.equals(action)) {
            IconData iconData = null;
            Serializable serial = intent
                    .getSerializableExtra(TpwNotification.DATA_NAME_NOTIFICATION);
            if (serial == null || !(serial instanceof IconData)) {
                Log.e(TAG, "onReceiver serial == null || !(serial instanceof IconData)");
                return;
            }

            iconData = (IconData) serial;
            // homeshell only handles the number of unread messages
            if (iconData == null
                || iconData.id == TpwNotification.ID_UNREAD_MESSAGE
                || TextUtils.isEmpty(iconData.packageName)) {
                Log.e(TAG, "onReceiver iconData == null || packageName == null, id==5309");
                return;
            }

            Log.d(TAG, " onReceive pkgName :" + iconData.packageName
                    + " className : " + iconData.className + " number : "
                    + iconData.num + " type : " + iconData.type + " id: " + iconData.id);

            number = iconData.num;
            className = iconData.className;
            packageName = iconData.packageName;

            if (iconData.type == TpwNotification.TYPE_NOTIFICATION_CANCEL) {
                number = 0;
            }

            // On homeshell, dialer's package/class="com.android.contacts/DialtactsActivity"
            // when phone application broadcasting notification, packagename=com.android.phone",
            if (iconData.id == TpwNotification.ID_MISSED_CALL
                && (TpwNotification.PACKAGE_NAME_PHONE.equals(iconData.packageName)
			 || TpwNotification.PACKAGE_NAME_TELECOM.equals(iconData.packageName))) {
                className = "com.yunos.alicontacts.activities.DialtactsContactsActivity"; 
                packageName = "com.yunos.alicontacts";
                if (!checkPackage(context, packageName)) {
                    className = "com.yunos.alicontacts.activities.DialtactsActivity"; 
                    packageName = "com.yunos.alicontacts";
                    if (!checkPackage(context, packageName)) {
                        className = "com.android.contacts.TwelveKeyDialer";
                        packageName = "com.android.contacts";
                    }
                }
            } else if (iconData.id == MSM_ID
                && "com.tpw.SecurityCenter".equals(iconData.packageName)) {
                className = "com.yunos.alimms.ui.ConversationList";
                packageName = "com.yunos.alicontacts";
                if (!checkPackage(context, packageName)) {
                    className = "com.android.mms.ui.ConversationList";
                    packageName = "com.android.mms";
                }
            } else if("com.android.settings".equals(iconData.packageName)) {
                className = "com.android.settings.Settings";
                packageName = "com.android.settings";
            }
        // for DATA_CLEARD intent, homeshell need to clear number in database
        } else if (Intent.ACTION_PACKAGE_DATA_CLEARED.equals(action)) {
             Uri data = intent.getData();
             if (data == null) {
                 return ;
             }
             packageName = data.getEncodedSchemeSpecificPart();
             if (TextUtils.isEmpty(packageName)) {
                 return ;
             }
            // handle email and appstore right now, may add more later
            if("com.tpw.mobile.email".equals(packageName)) {
                className = "com.tpw.mobile.email.activity.Welcome";
            } else if("com.tpw.wireless.vos.appstore".equals(packageName)) {
                className = "com.tpw.wireless.vos.appstore.LogoActivity";
            }
        } else if (Intent.ACTION_UNREAD_CHANGED.equals(action)) { //start, add by zl 20150721 for show misscall count & unread sms/mms count 
        	final ComponentName componentName = (ComponentName) intent
                    .getExtra(Intent.EXTRA_UNREAD_COMPONENT);
            final int unreadNum = intent.getIntExtra(Intent.EXTRA_UNREAD_NUMBER, -1);
            
            if (componentName != null && unreadNum != -1) {
            	className = componentName.getClassName();
            	packageName = componentName.getPackageName();
            	number = unreadNum;
            }
            Log.i(TAG, "className = " + className + "  packageName = " + packageName + "  unreadNum = " + unreadNum);
        } //end, add by zl 20150721
        Log.d(TAG, " className="+className+",packageName="+packageName+",number="+number);
        if(!TextUtils.isEmpty(className) && !TextUtils.isEmpty(packageName)){
                // 1: we need to get ItemInfo according to className and packageName
                // 2: update the number of unread messages in database
                // 3: update view
                // 4: Do we need to update the ItemInfo list in dataModule

            NotificationTask notifTask = new NotificationTask(packageName,
                                                    className, number, context);
            Thread thrd = new Thread(notifTask);
            Log.d(TAG, " start thread for notification task");
            thrd.start();
        }
    }

     private boolean checkPackage(Context context, String pkgName) {
        List<ResolveInfo> list = AllAppsList.findActivitiesForPackage(context, pkgName);
        if (list == null || list.isEmpty()) {
            return false;
        }

        return true;
    }

    private class NotificationTask implements Runnable {

        private String mPackageName;
        private String mClassName;
        private int mMessageNum;
        private Context mContext;

        public NotificationTask(String packageName, String className,
                int messageNum, Context context) {
            mPackageName = packageName;
            mClassName = className;
            mMessageNum = messageNum;
            mContext = context;
        }

        @Override
        public void run() {
            //Log.d(TAG, ":entering NotificationTask.run(),to query database");
            Log.d(TAG, ":classname = " + mClassName);
            Log.d(TAG, ":packagename = " + mPackageName);
            long id = 0;
            String itDesc = null;
            final ContentResolver cr = mContext.getContentResolver();
            Cursor c = null;
            Intent intent;
            try {
                c = cr.query(LauncherSettings.Favorites.CONTENT_URI,
                    new String[] {Favorites._ID, Favorites.INTENT },null,
                    null, null);
                
                while (c != null && c.moveToNext()) {
                    itDesc = c.getString(1);
                    //Log.d(TAG, " itDesc = "+ itDesc);
             
                    if (itDesc!=null && itDesc.contains(mPackageName)) {
                        intent = Intent.parseUri(itDesc, 0);
                        ComponentName componentName = intent.getComponent();
                        String cls  = componentName.getClassName();
                        String pckg = componentName.getPackageName();
                        //Log.d(TAG, ":cls="+cls+",pckg="+pckg);
                        if (mClassName.equals(cls) && mPackageName.equals(pckg)) {
                            id = c.getInt(0);
                            Log.d(TAG, " find item in db for class:"+mClassName+",id="+id);
                            break;
                        }
                        
                    }
                } 
            } catch (Exception e) {
                Log.d(TAG, " NotificationTask query db and met exception:"+e);
            } finally {
                if (c!=null) c.close();
            }
            
            if (id > 0) {
                /*
                // pass true to let launcher module to update view automatically
                final Uri uri = LauncherSettings.Favorites.getContentUri(id, true);
                Log.d(TAG, " mMessageNum="+mMessageNum);
                final ContentValues values = new ContentValues();
                values.put(LauncherSettings.Favorites.MESSAGE_NUM, mMessageNum); 
                cr.update(uri, values, null, null);
                */
                final ContentValues values = new ContentValues();
                values.put(LauncherSettings.Favorites.MESSAGE_NUM, mMessageNum); 
                final Uri uri = LauncherSettings.Favorites.getContentUri(id, false);
                cr.update(uri, values, null, null);
                // to update item in items list in LauncherModule
                LauncherModel.updateItemById(mContext, id, values, true, true);
            }
        }
    }

    	
}
