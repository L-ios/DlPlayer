package com.tpw.homeshell;

public class ApkInfo {
	 public String mPkgname;
	 public String mApkName;
	 public String mFileName;
	 public int mFileSize;
	 public int mType;
	 public String mIconData;
	 public String mUrl;
	 public int mState;//状态
	 public String mDescribe;
	 public boolean mIsNew;
	 
	 public static final int APKINFO_STATE_ORI=0;
	 public static final int APKINFO_STATE_DOWNLOAD_WAIT=1;
	 public static final int APKINFO_STATE_DOWNLOAD=2;
	 public static final int APKINFO_STATE_PAUSE=3;
	 public static final int APKINFO_STATE_USER_PAUSE=4;
	 public static final int APKINFO_STATE_DOWNLOAD_COMPLETE=5;
	 public static final int APKINFO_STATE_INSTALL=6;
	 public static final int APKINFO_STATE_INSTALL_COMPLETE=7;
	 
	public static final String DESTPATH_UNZIP_APP = "/data/exres/app/";
	public static final String DESTPATH_UNZIP_GAME = "/data/exres/game/";
	public static final String FOLDER_ONLINE_XML_PATH="/data/exres/online";;
	 
	 public ApkInfo()
	 {
	 }
	 
	 public ApkInfo(String pkgname,String apkname,String filename,int filesize,
			 int type,String icondata,String url,int state,String describe)
	 {
		 mPkgname=pkgname;
		 mApkName=apkname;
		 mFileName=filename;
		 mFileSize=filesize;
		 mType=type;
		 mIconData=icondata;
		 mUrl=url;
		 mState=state;
		 mDescribe=describe;
	 }
    @Override
    public String toString() {
        return "ApkInfo(mPkgname=" + mPkgname.toString() + "mApkName=" + mApkName + "mFileName=" + mFileName
                + " mFileSize=" + mFileSize + " mType=" + mType + " mUrl=" + mUrl
                + " mState=" + mState  + " mDescribe="+mDescribe+")";
    }
}
