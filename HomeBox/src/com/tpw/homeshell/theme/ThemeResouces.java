package com.tpw.homeshell.theme;

import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;

/**
 * 主题包辅助函数
 */
public class ThemeResouces{
    private static final String LAYOUT = "layout";
    private static final String DRAWABLE = "drawable";
    private static final String COLOR = "color";
    private static final String STRING = "string";
    private static final String DIMEN = "dimen";

    public static Drawable getDrawable(IThemePanel tr, String name) {
        Resources r = tr.getRes();
        int res = r.getIdentifier(name, DRAWABLE, tr.getPackageName());
        if (res == 0) {
            return null;
        }
        return r.getDrawable(res);
    }

    public static int getColor(IThemePanel tr, String name){
        Resources r = tr.getRes();
        int res = r.getIdentifier(name, COLOR, tr.getPackageName());
        if (res == 0) {
            return 0;
        }
        return r.getColor(res);
    }

    public static  String getString(IThemePanel tr, String name){
        Resources r = tr.getRes();
        int res = r.getIdentifier(name, STRING, tr.getPackageName());
        if (res == 0) {
            return null;
        }
        return r.getString(res);
    }

    public static int getDimen(IThemePanel tr, String name){
        Resources r = tr.getRes();
        int res = r.getIdentifier(name, DIMEN, tr.getPackageName());
        if (res == 0) {
            return 0;
        }
        return r.getDimensionPixelSize(res);
    }
    
    public static int getLayoutResId(IThemePanel tr, String name) {
        Resources r = tr.getRes();
        return r.getIdentifier(name, LAYOUT, tr.getPackageName());
    }
    
    public static View inflate(IThemePanel tr, String name, LayoutInflater inflate) {
        Resources r = tr.getRes();
        int res = r.getIdentifier(name, LAYOUT, tr.getPackageName());
        if (res == 0) {
            return null;
        }
        XmlResourceParser parser = r.getLayout(res);
        return inflate.inflate(parser, null, false);
    }
    
    public static FlowPanel getFlowPanelWithPackge(String pkg) {
        FlowPanel type = FlowPanel.Unknown;
        if (pkg == null) {
            return type;
        }
        
        if (pkg.contains("waterruler")) {
            type = FlowPanel.WaterRuler;
        } else if (pkg.contains("dashboard")) {
            type = FlowPanel.DashBoard;
        } else if (pkg.contains("cctv5")) {
            type = FlowPanel.CCTV5;
        } else if (pkg.contains("acer")) {
            type = FlowPanel.Acer;
        } else if (pkg.contains("walker")) {
            type = FlowPanel.Walker;
        } else if (pkg.contains("glass")) {
            type = FlowPanel.Glass;
        } else if (pkg.contains("soundwave")) {
            type = FlowPanel.Soundwave;
        } else if (pkg.contains("radio")) {
            type = FlowPanel.Radio;
        } else if (pkg.contains("commando")) {
            type = FlowPanel.Commando;
        } else if (pkg.contains("military")) {
            type = FlowPanel.Military;
        }
        
        return type;
    }
}
