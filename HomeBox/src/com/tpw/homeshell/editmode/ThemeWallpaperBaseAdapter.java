
package com.tpw.homeshell.editmode;

import java.lang.ref.SoftReference;
import java.util.ArrayList;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.SystemClock;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.TextView;
import android.widget.Toast;
import android.os.AsyncTask;

import com.tpw.homeshell.R;

public abstract class ThemeWallpaperBaseAdapter extends BaseAdapter {
    public final static int DATACHANGE_TYPE_WALLPAPER = 0;
    public final static int DATACHANGE_TYPE_THEME = 1;
    public final static int DATACHANGE_TYPE_ALL = 2;

    public final static String ID = "_id";
    public final static String IS_SYSTEM = "is_system";
    public final static String NAME = "label";
    public final static String PACKAGE_NAME = "theme_package";
    public final static String IS_CHECKED = "is_used";
    public final static String THUMBNAIL = "thumbnail";

    public final static int OP_TYPE_INSERT = 0;
    public final static int OP_TYPE_DELETE = 1;
    public final static int OP_TYPE_UPDATE = 2;

    protected Context mContext;
    protected LayoutInflater mInflater;
    protected ContentResolver mResolver;
    protected Uri mUri;

    private int mType;
    private long mCheckedID = -1;
    private SparseArray<SoftReference<BaseAttr>> mCache = new SparseArray<SoftReference<BaseAttr>>();

    private ArrayList<Long> mIds;

    private static final int RELOAD_THRESHOLD = 60 * 1000;

    private int mImageViewSize;
    private Matrix mMatrix = new Matrix();

    private View mListView;

    private View mLoadingLayout;

    private long mLastLoadTimeMillis;

    public ThemeWallpaperBaseAdapter(Context context, int type) {
        mContext = context;
        mInflater = LayoutInflater.from(mContext);
        mResolver = mContext.getContentResolver();
        mType = type;
        mUri = Uri.parse("content://" + "com.yunos.theme.thememanager.provider" + "/"
                + (type == DATACHANGE_TYPE_WALLPAPER ? "wallpapers" : "themes"));
        mIds = new ArrayList<Long>();
        mImageViewSize = (int) context.getResources().getDimension(R.dimen.preview_item_image_size);
        // Toast.makeText(mContext, R.string.prepare_list,
        // Toast.LENGTH_SHORT).show();
        // loadItemIds();

        IntentFilter filter = new IntentFilter("com.yunos.theme.NOTIFY_DATA_CHANGE");
        mContext.registerReceiver(new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                int refreshType = intent.getIntExtra("type", 0);
                final long id = intent.getLongExtra("id", 0);
                final int opType = intent.getIntExtra("op_type", OP_TYPE_UPDATE);

                if (refreshType == DATACHANGE_TYPE_ALL) {
                    new AsyncTask<Void, Void, Void>() {
                        @Override
                        protected Void doInBackground(Void... params) {
                            loadItemIds();
                            return null;
                        }

                        protected void onPostExecute(Void result) {
                            ThemeWallpaperBaseAdapter.this.notifyDataSetChanged();

                            ThemeWallpaperBaseAdapter.this.notifyDataSetInvalidated();
                        }
                    }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new Void[0]);

                    if (mListView != null && mLoadingLayout != null) {
                        mListView.setVisibility(View.VISIBLE);
                        mLoadingLayout.setVisibility(View.INVISIBLE);
                    }
                }
                else if (mType == refreshType) {
                    new AsyncTask<Void, Void, Void>() {
                        @Override
                        protected Void doInBackground(Void... params) {
                            synchronized (mIds) {
                                if (opType == OP_TYPE_INSERT) {
                                    BaseAttr ba = getItemById(id);
                                    if (ba != null && ba.thumbnail != null) {
                                        mIds.add(id);
                                    }
                                }
                                else if (opType == OP_TYPE_DELETE) {
                                    synchronized (mCache) {
                                        int i = 0;
                                        for (; i < mIds.size(); i++) {
                                            if (mIds.get(i).longValue() == id) {
                                                mCache.remove(mIds.get(i).intValue());
                                                mIds.remove(i);
                                                break;
                                            }
                                        }
                                    }
                                }
                                else if (opType == OP_TYPE_UPDATE) {
                                    synchronized (mCache) {
                                        int i = 0;
                                        int prevCheckedIndex = -1;
                                        int curCheckedIndex = -1;
                                        for (; i < mIds.size(); i++) {
                                            if (mIds.get(i).longValue() == mCheckedID) {
                                                prevCheckedIndex = i;
                                            }
                                            if (mIds.get(i).longValue() == id) {
                                                curCheckedIndex = i;
                                            }
                                        }
                                        if (prevCheckedIndex != -1)
                                            mCache.remove(mIds.get(prevCheckedIndex).intValue());
                                        if (curCheckedIndex != -1)
                                            mCache.remove(mIds.get(curCheckedIndex).intValue());
                                    }
                                }
                            }

                            return null;
                        }

                        protected void onPostExecute(Void result) {
                            ThemeWallpaperBaseAdapter.this.notifyDataSetChanged();
                        }
                    }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new Void[0]);

                    if (mListView != null && mLoadingLayout != null) {
                        mListView.setVisibility(View.VISIBLE);
                        mLoadingLayout.setVisibility(View.INVISIBLE);
                    }
                }
            }

        }, filter);
    }

    public void reload() {
        loadItemIds();
        ThemeWallpaperBaseAdapter.this.notifyDataSetChanged();
    }

    public boolean needReload() {
        if (getCount() <= 0 && System.currentTimeMillis() - mLastLoadTimeMillis > RELOAD_THRESHOLD) {
            return true;
        }
        return false;
    }

    private void loadItemIds() {
        mLastLoadTimeMillis = System.currentTimeMillis();
        String[] projection = new String[] {
                ID
        };
        Cursor c = null;
        synchronized (mIds) {
            try {
                c = mResolver.query(mUri, projection, null, null, null);
                synchronized (mCache) {
                    mCache.clear();
                }
                mIds.clear();
                if (c != null) {
                    while (c.moveToNext()) {
                        mIds.add(c.getLong(0));
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            } finally {
                if (c != null)
                    c.close();
            }
        }
    }

    @Override
    public int getCount() {
        int count = 0;

        synchronized (mIds) {
            count = mIds == null ? 0 : mIds.size();
        }

        return count;
    }

    private BaseAttr getItemAtPosition(int position) {
        BaseAttr item = null;
        SoftReference<BaseAttr> cachedItem = null;

        synchronized (mIds) {
            item = getItemById(mIds.get(position));
        }

        return item;
    }

    private BaseAttr getItemById(long lid) {
        int id = (int)lid;
        BaseAttr item = null;
        SoftReference<BaseAttr> cachedItem = null;
        synchronized (mCache) {
            cachedItem = mCache.get(id);
        }
        if (cachedItem != null) {
            item = cachedItem.get();
        }
        Cursor c = null;
        if (item == null) {
            try {
                c = mResolver.query(ContentUris.withAppendedId(mUri, id),
                        null, null, null, null);

                if (c != null) {
                    if (c.getCount() <= 0) {
                        return item;
                    }
                    if (c.moveToFirst()) {
                        item = getNewItem();
                        populateItem(item, c);
                        mCache.put(id, new SoftReference<BaseAttr>(item));
                        if (item.checked) {
                            mCheckedID = Long.valueOf(item.id);
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (c != null) {
                    c.close();
                }
            }
        }
        return item;
    }

    protected abstract void populateItem(BaseAttr item, Cursor cursor);

    protected BaseAttr getNewItem() {
        return new BaseAttr();
    }

    @Override
    public Object getItem(int position) {
        return getItemAtPosition(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder vh;
        BaseAttr item = getItemAtPosition(position);
        if (convertView == null) {
            vh = new ViewHolder();
            convertView = mInflater.inflate(R.layout.preview_item, parent, false);
            vh.previewImgView = (ImageView) convertView.findViewById(R.id.preview_image);
            vh.previewChecked = (ImageView) convertView.findViewById(R.id.preview_checked);
            vh.titleTextView = (TextView) convertView.findViewById(R.id.preview_title);
            convertView.setTag(vh);
        }
        else {
            vh = (ViewHolder) convertView.getTag();
        }

        if(item == null){
            return convertView;
        }

        if (mType == DATACHANGE_TYPE_WALLPAPER) {
            vh.titleTextView.setVisibility(View.GONE);
            FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) vh.previewChecked
                    .getLayoutParams();
            int marginTop = mContext.getResources().getDimensionPixelSize(
                    R.dimen.preview_checked_image_wallpaper_margin_top);
            int marginRight = mContext.getResources().getDimensionPixelSize(
                    R.dimen.preview_checked_image_wallpaper_margin_right);
            params.setMargins(params.leftMargin, marginTop, marginRight, params.bottomMargin);
        } else {
            vh.titleTextView.setText(item.name);
            FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) vh.previewChecked
                    .getLayoutParams();
            int marginTop = mContext.getResources().getDimensionPixelSize(
                    R.dimen.preview_checked_image_theme_margin_top);
            int marginRight = mContext.getResources().getDimensionPixelSize(
                    R.dimen.preview_checked_image_theme_margin_right);
            params.setMargins(params.leftMargin, marginTop, marginRight, params.bottomMargin);
            if(item.thumbnail != null){
                float scale = ((float) mImageViewSize) / item.thumbnail.getWidth();
                mMatrix.setScale(scale, scale);
                mMatrix.postTranslate(0, mImageViewSize - scale * item.thumbnail.getHeight());
                vh.previewImgView.setScaleType(ScaleType.MATRIX);
                vh.previewImgView.setImageMatrix(mMatrix);
            }
        }
        vh.previewImgView.setImageBitmap(item.thumbnail);
        if (item.checked) {
            vh.previewChecked.setVisibility(View.VISIBLE);
        }
        else {
            vh.previewChecked.setVisibility(View.INVISIBLE);
        }

        return convertView;
    }

    @Override
    public long getItemId(int position) {
        long id = 0;

        synchronized (mIds) {
            id = mIds == null ? -1 : mIds.get(position);
        }

        return id;
    }

    public void setView(View listView, View loadLayout) {
        mListView = listView;
        mLoadingLayout = loadLayout;
    }

    class ViewHolder {
        public int position;
        public ImageView previewImgView;
        public TextView titleTextView;
        public ImageView previewChecked;
    }

    class BaseAttr {
        String id;
        String name;
        boolean isSystem = false;
        Bitmap thumbnail;
        boolean checked = false;
    }

}
