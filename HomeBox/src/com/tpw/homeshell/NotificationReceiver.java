
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
import com.tpw.homeshell.IconDigitalMarkHandler;

public class NotificationReceiver extends BroadcastReceiver {
   private static String TAG = "NotificationReceiver";
   private IconDigitalMarkHandler mIconDigitalMarkHandler = null;

    @Override
    public void onReceive(Context context, Intent intent) {
        //to update icon mark in this receiver only before workspace is not loaded, this is needed in the following case:
        //the homeshell is killed or not yet launched, and we need to update icon mark. 
        //in this case, system will launch LauncherApplicaiton and call NotificationReceiver.onRecive(). After LauncherApplicaiton
        //is launched, LauncherModel.onReceive() will take over
        Log.d(TAG, " NotificationReceiver:intent=" + intent);
        LauncherApplication app = (LauncherApplication)context.getApplicationContext();
        LauncherModel model = app.getModel();
        if (model != null && model.isWorkspaceLoaded()) {
            //Log.d(TAG, " model.isWorkspaceLoaded()"+model.isWorkspaceLoaded());
            return;
        }
        //Log.d(TAG, "222  model.isWorkspaceLoaded()"+model.isWorkspaceLoaded());
        mIconDigitalMarkHandler = IconDigitalMarkHandler.getInstance();
        mIconDigitalMarkHandler.handleNotificationIntent(context, intent);
    }
}
