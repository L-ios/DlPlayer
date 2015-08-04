package com.tpw.homeshell.theme;

import android.content.Context;
import android.content.res.Resources;

/**
 * 表示一个流量面板资源包
 */
public interface IThemePanel  {
    /**
     * 初始化
     */
    void init(Context context);
    /**
     * 得到主题包资源
     */
    Resources getRes();
    
    /**
     * 得到包名称
     * @param context
     */
    String getPackageName();
    
    /**
     * 得到流量面板类型
     */
    FlowPanel getPanelType();
}
