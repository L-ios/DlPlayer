
package com.tpw.homeshell.editmode;

import android.content.Context;
import android.database.Cursor;
import android.graphics.BitmapFactory;

public class ThemesPreviewAdapter extends ThemeWallpaperBaseAdapter {
    private static final String TAG = "ThemesPreviewAdapter";

    public ThemesPreviewAdapter(Context context) {
        super(context, ThemeWallpaperBaseAdapter.DATACHANGE_TYPE_THEME);
    }

    @Override
    protected void populateItem(BaseAttr item, Cursor c) {
        ThemeAttr theme = (ThemeAttr) item;
        theme.id = c.getString(c.getColumnIndex(ThemeWallpaperBaseAdapter.ID));
        //theme.isSystem = c.getInt(c.getColumnIndex(ThemeWallpaperBaseAdapter.IS_SYSTEM)) == 1;
        theme.name = c.getString(c.getColumnIndex(ThemeWallpaperBaseAdapter.NAME));
        theme.packageName = c.getString(c.getColumnIndex(ThemeWallpaperBaseAdapter.PACKAGE_NAME));
        byte[] data = c.getBlob(c.getColumnIndex(ThemeWallpaperBaseAdapter.THUMBNAIL));
        item.thumbnail = BitmapFactory.decodeByteArray(data, 0, data.length);
        theme.checked = c.getInt(c.getColumnIndex(IS_CHECKED)) == 1;
    }

    @Override
    protected BaseAttr getNewItem() {
        return new ThemeAttr();
    }

    class ThemeAttr extends BaseAttr {
        public String packageName;
    }
}
