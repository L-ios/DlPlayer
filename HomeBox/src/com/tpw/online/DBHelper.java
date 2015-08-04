package com.tpw.online;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


public class DBHelper extends SQLiteOpenHelper{

	public DBHelper(Context context) {
        super(context, "online.db", null, 1);
    }
    
	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub
		db.execSQL("create table apksinfo(_id integer PRIMARY KEY AUTOINCREMENT, " +
				"version integer," +
				"pkgname char," +
				"apkname char," +
				"filename char," +
				"filesize integer," +
				"type integer," +
				"icondata text," +
				"url char," +
				"state integer," +
				"describe char" +
				")");
	}

	@Override
	public void onUpgrade(SQLiteDatabase arg0,
			int arg1, int arg2) {
		// TODO Auto-generated method stub
		
	}

}
