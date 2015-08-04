package com.tpw.online;

import java.util.ArrayList;
import java.util.List;

import com.tpw.homeshell.ApkInfo;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;



public class DBOperate {
	    private DBHelper dbHelper;

	    public DBOperate(Context context) {
	        dbHelper = new DBHelper(context);
	    }


	    public  boolean isHasApkInfo(String pkgname) {
	        SQLiteDatabase database = dbHelper.getReadableDatabase();
	        String sql = "select count(*)  from apksinfo where pkgname=?";
	        Cursor cursor = database.rawQuery(sql, new String[] { pkgname });
	        cursor.moveToFirst();
	        int count = cursor.getInt(0);
	        cursor.close();
	        return count == 0;
	    }


	    public void saveApkInfos(int version,List<ApkInfo> infos) {
	    	if(infos==null)
	    		return;
	        SQLiteDatabase database = dbHelper.getWritableDatabase();
	        for (ApkInfo info : infos) {
	            String sql = "insert into apksinfo(" +
	            		"version,pkgname, apkname,filename,filesize,type,icondata,url,state,describe" +
	            		") values (?,?,?,?,?,?,?,?,?,?);";
	            Object[] bindArgs = { version, info.mPkgname,
	                    info.mApkName, info.mFileName,info.mFileSize, 
	                    info.mType,info.mIconData,info.mUrl,info.mState,info.mDescribe };
	            database.execSQL(sql, bindArgs);
	        }
	    }


	    public List<ApkInfo> getInfos(String pkgname) {
	        List<ApkInfo> list = new ArrayList<ApkInfo>();
	        SQLiteDatabase database = dbHelper.getReadableDatabase();
	        String sql = "select pkgname, apkname,filename,filesize,type,icondata,url,state,describe from apksinfo where pkgname=?";
	        Cursor cursor = database.rawQuery(sql, new String[] { pkgname });
	        while (cursor.moveToNext()) {
	        	ApkInfo info = new ApkInfo(
	        			cursor.getString(0),
	                    cursor.getString(1), 
	                    cursor.getString(2), 
	                    cursor.getInt(3),
	                    cursor.getInt(4),
	                    cursor.getString(5),
	                    cursor.getString(6),
	                    cursor.getInt(7),
	                    cursor.getString(8)
	                    );
	            list.add(info);
	        }
	        cursor.close();
	        return list;
	    }
	    public List<ApkInfo> getInfos(String pkgname,int version) {
	        List<ApkInfo> list = new ArrayList<ApkInfo>();
	        SQLiteDatabase database = dbHelper.getReadableDatabase();
	        String sql = "select pkgname, apkname,filename,filesize,type,icondata,url,state,describe " +
	        		"from apksinfo where pkgname=? and version=?";
	        Cursor cursor = database.rawQuery(sql, new String[] { pkgname, String.valueOf(version)});
	        while (cursor.moveToNext()) {
	        	ApkInfo info = new ApkInfo(
	        			cursor.getString(0),
	                    cursor.getString(1), 
	                    cursor.getString(2), 
	                    cursor.getInt(3),
	                    cursor.getInt(4),
	                    cursor.getString(5),
	                    cursor.getString(6),
	                    cursor.getInt(7),
	                    cursor.getString(8)
	                    );
	            list.add(info);
	        }
	        cursor.close();
	        return list;
	    }
	    
	    public List<ApkInfo> getInfos() {
	        List<ApkInfo> list = new ArrayList<ApkInfo>();
	        SQLiteDatabase database = dbHelper.getReadableDatabase();
	        String sql = "select pkgname, apkname,filename,filesize,type,icondata,url,state,describe from apksinfo";
	        Cursor cursor = database.rawQuery(sql, new String[] { });
	        while (cursor.moveToNext()) {
	        	ApkInfo info = new ApkInfo(
	        			cursor.getString(0),
	                    cursor.getString(1), 
	                    cursor.getString(2), 
	                    cursor.getInt(3),
	                    cursor.getInt(4),
	                    cursor.getString(5),
	                    cursor.getString(6),
	                    cursor.getInt(7),
	                    cursor.getString(8)
	                    );
	            list.add(info);
	        }
	        cursor.close();
	        return list;
	    }
	    public List<ApkInfo> getInfos(int version) {
	        List<ApkInfo> list = new ArrayList<ApkInfo>();
	        SQLiteDatabase database = dbHelper.getReadableDatabase();
	        String sql = "select pkgname, apkname,filename,filesize,type,icondata,url,state,describe " +
	        		"from apksinfo where version=?";
	        Cursor cursor = database.rawQuery(sql, new String[] {String.valueOf(version)});
	        while (cursor.moveToNext()) {
	        	ApkInfo info = new ApkInfo(
	        			cursor.getString(0),
	                    cursor.getString(1), 
	                    cursor.getString(2), 
	                    cursor.getInt(3),
	                    cursor.getInt(4),
	                    cursor.getString(5),
	                    cursor.getString(6),
	                    cursor.getInt(7),
	                    cursor.getString(8)
	                    );
	            list.add(info);
	        }
	        cursor.close();
	        return list;
	    }

	    public void updataInfoState(String pkgname,int state) {
	        SQLiteDatabase database = dbHelper.getReadableDatabase();
	        String sql = "update apksinfo set state=? where pkgname=?";
	        Object[] bindArgs = { state, pkgname };
	        database.execSQL(sql, bindArgs);
	    }

	    public void closeDb() {
	        dbHelper.close();
	    }


	    public void delete(String pkgname) {
	        SQLiteDatabase database = dbHelper.getReadableDatabase();
	        database.delete("apksinfo", "pkgname=?", new String[] { pkgname });
	        database.close();
	    }
	    public void deleteOtherVersion(int version) {
	        SQLiteDatabase database = dbHelper.getReadableDatabase();
	        database.delete("apksinfo", "version<>?", new String[] { String.valueOf(version) });
	        database.close();
	    }
}
