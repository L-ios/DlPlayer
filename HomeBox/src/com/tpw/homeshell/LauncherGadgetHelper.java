
package com.tpw.homeshell;

//import tpw.v3.gadget.GadgetHelper;
//import tpw.v3.gadget.GadgetInfo;
//import tpw.v3.gadget.GadgetView;

import android.content.ComponentName;
import android.content.Context;
import android.view.View;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class LauncherGadgetHelper {
    //private static ArrayList<WeakReference<GadgetView>> sGadgetViews = new ArrayList<WeakReference<GadgetView>>();

    /**
     * get gadget by GadgetInfo
     * 
     * @param context
     * @param info
     * @return GadgetView
     */
    /*public static View getGadget(Context context, GadgetInfo info) {
        Context appContext = context.getApplicationContext();
        GadgetView v = GadgetHelper.getGadget(appContext == null ? context : appContext, info);
        if (v != null)
            synchronized (sGadgetViews) {
                sGadgetViews.add(new WeakReference<GadgetView>(v));
            }
        return v;
    }*/

    /**
     * get gadget card by ComponentName
     * 
     * @param context
     * @param cn
     * @return GadgetView
     */
    /*public static View getGadget(Context context, ComponentName cn) {
        Context appContext = context.getApplicationContext();
        GadgetView v = GadgetHelper.getGadget(appContext == null ? context : appContext, cn);
        return v;
    }

    public static void cleanUp() {
        synchronized (sGadgetViews) {
            for (WeakReference<GadgetView> r : sGadgetViews) {
                GadgetView v = r.get();
                if (v != null)
                    v.cleanUp();
            }
            sGadgetViews.clear();
        }
    }*/

}
