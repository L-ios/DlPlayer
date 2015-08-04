package com.tpw.homeshell;

import java.io.File;

public class ExappUtil {
	public static final String PACKAGENAME = "packagename";
	public static final String DOWNLOAD_URL = "downloadurl";
	public static final String SAVE_PATH="savepath";
	
	public static final String EXTAR_SAVE_FILEPATH="fol.download.filepath";
	public static final String EXTRA_DOWNLOAD_URL ="fol.download.url";
	public static final String EXTAR_IS_PAUSE_BY_USER="fol.download.ispausebyuser";
	public static final String ACTION_TPW_FOLDERONLINE_DOWNLOAD_ONE="com.tpw.fol.download";
	public static final String ACTION_TPW_DOWNLOAD_TO_PAUSE="com.tpw.fol.download.topause";
	
	public static boolean isFileExists(String path)
	{
		if(path==null)
			return false;
		File file=new File(path);
		return file.exists();
	}
}
