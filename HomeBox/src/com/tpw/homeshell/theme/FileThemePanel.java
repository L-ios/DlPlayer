package com.tpw.homeshell.theme;

import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.util.Log;

import com.tpw.homeshell.utils.HLog;
import com.tpw.homeshell.utils.ParseApkPackage;
import commonlibs.utils.ACA;

/**
 * 文件流量面板
 */
public class FileThemePanel implements IThemePanel {
    private Resources mResouces;
    private String mPackageName;
    private FlowPanel mFlowPanel;
    private String mFilePath;

    public FileThemePanel(Context context, String filePath) {
        mFilePath = filePath;
        init(context);
    }
    
    @Override
    public Resources getRes() {
        if(mResouces == null)
            throw new RuntimeException("not init");
        return mResouces;
    }

    @Override
    public String getPackageName() {
        if(mResouces == null)
            throw new RuntimeException("not init");
        return mPackageName;
    }
    
    public void init(Context context) {
//        AssetManager newAm = new AssetManager();
        try{
            AssetManager newAm = AssetManager.class.getConstructor(null).newInstance(null);

//            int cookie = ACA.AssetManager.addAssetPath(newAm, mFilePath);
            int cookie = (Integer) AssetManager.class.getMethod("addAssetPath", String.class).invoke(
                    null, mFilePath);
            mResouces = new Resources(newAm, context.getResources().getDisplayMetrics(),
                    context.getResources().getConfiguration());

            if(cookie != 0) {
                XmlResourceParser parser = newAm.openXmlResourceParser(cookie, 
                        "AndroidManifest.xml");
                mPackageName = ParseApkPackage.parsePackageName(parser, parser);
            }
        }catch(Exception ex){
            throw new RuntimeException(ex);
        }

        mFlowPanel = ThemeResouces.getFlowPanelWithPackge(mPackageName);
        HLog.d("FileThemePanel", "init pkgName : " + mPackageName +
               " panel type : " + mFlowPanel);
    }
    
    @Override
    public FlowPanel getPanelType() {
       return mFlowPanel;
    }
}
