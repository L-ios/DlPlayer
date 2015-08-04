package com.tpw.homeshell;

import android.content.pm.PackageInfo;

public class FileAndInfo {
	public String mFilePath;
	public PackageInfo mPkgInfo;
	public boolean mInstalled;
	public int mExappType;
	public static final int EXAPP_TYPE_APP=1;
	public static final int EXAPP_TYPE_GAME=2;

    public static final String TYPE_PACKAGENAME = "packagename";
    public static final String TYPE_PACKAGEPATH= "packagepath";
    public static final String INTENT_TPW_INSTALL= "com.tpw.install";
    public static final String INTENT_TPW_ONLINE= "com.tpw.online";
}
