
package com.tpw.homeshell.editmode;

import java.util.ArrayList;
import java.util.Iterator;

import com.tpw.homeshell.Launcher;
import com.tpw.homeshell.PagedViewCellLayout;
import com.tpw.homeshell.WidgetPreviewLoader;
import com.tpw.homeshell.model.LauncherModel;
import com.tpw.homeshell.themeutils.ThemeUtils;

//import tpw.v3.gadget.GadgetInfo;
import android.annotation.SuppressLint;
import android.appwidget.AppWidgetProviderInfo;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.TextView;
import android.os.Process;
import com.tpw.homeshell.R;

public class WidgetPreviewAdapter extends BaseAdapter {
    private ArrayList<AppsCustomizeAsyncTask> mRunningTasks = new ArrayList<AppsCustomizeAsyncTask>();
    private static final int ITEM_PER_SCREEN = 3;
    private WidgetPreviewLoader mWidgetPreviewLoader;
    private LayoutInflater mInflater;
    private Launcher mLauncher;
    private ArrayList<Object> mWidgets;
    private PagedViewCellLayout mWidgetSpacingLayout;
    private PackageManager mPackageManager;
    private Bitmap mLoadingBmp;

    public WidgetPreviewAdapter(Launcher launcher) {
        super();
        mWidgetPreviewLoader = new WidgetPreviewLoader(launcher);
        mInflater = LayoutInflater.from(launcher);
        mLauncher = launcher;
        mWidgets = new ArrayList<Object>();
        onPackagesUpdated(LauncherModel.getSortedWidgetsAndShortcuts(launcher));
        int previewSize = (int) launcher.getResources().getDimension(
                R.dimen.preview_item_image_size);
        mWidgetPreviewLoader.setPreviewSize(previewSize, previewSize);
        mWidgetPreviewLoader.setEditModeStyle(true);
        mPackageManager = launcher.getPackageManager();
        mLoadingBmp = Bitmap.createBitmap(previewSize, previewSize, Config.ARGB_4444);
        Canvas cvs = new Canvas();
        cvs.setBitmap(mLoadingBmp);
        cvs.drawColor(0x0c0c0c0c, PorterDuff.Mode.CLEAR);
        cvs.setBitmap(null);
    }

    @Override
    public int getCount() {
        synchronized (mWidgets) {
            return mWidgets.size();
        }
    }

    @Override
    public Object getItem(int position) {
        synchronized (mWidgets) {
            return mWidgets.get(position);
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder vh;
        if (convertView == null) {
            vh = new ViewHolder();
            convertView = mInflater.inflate(R.layout.preview_item, parent, false);
            vh.previewImgView = (ImageView) convertView.findViewById(R.id.preview_image);
            vh.titleTextView = (TextView) convertView.findViewById(R.id.preview_title);
            convertView.setTag(vh);
        } else {
            vh = (ViewHolder) convertView.getTag();
        }
        synchronized (mWidgets) {
            Object widget = mWidgets.get(position);
            String label = null;
            if (widget instanceof AppWidgetProviderInfo) {
                label = ((AppWidgetProviderInfo) widget).label;
            } else if (widget instanceof ResolveInfo) {
                label = ((ResolveInfo) widget).loadLabel(mPackageManager).toString();
            } /*else if (widget instanceof GadgetInfo) {
                GadgetInfo info = (GadgetInfo) widget;
                label = getGadgetLabel(info);
            }*/
            vh.titleTextView.setText(label);
            vh.position = position;
    
            vh.previewImgView.setScaleType(ScaleType.FIT_CENTER);
            Bitmap bmp = mWidgetPreviewLoader.getCachedPreview(widget);
            if ((bmp != null) && (bmp.isRecycled() == false)) {
                vh.previewImgView.setImageBitmap(bmp);
            } else {
                vh.previewImgView.setImageBitmap(mLoadingBmp);
                prepareLoadWidgetPreviewsTask(position, widget, vh.previewImgView);
                android.util.Log.e("cache", "geting...");
            }
        }
        return convertView;
    }

    public void onPackagesUpdated(ArrayList<Object> widgetsAndShortcuts) {
        synchronized (mWidgets) {
            // Get the list of widgets and shortcuts
            mWidgets.clear();
            for (Object o : widgetsAndShortcuts) {
                if (o instanceof AppWidgetProviderInfo) {
                    AppWidgetProviderInfo widget = (AppWidgetProviderInfo) o;
                    widget.label = widget.label.trim();
                    if (widget.minWidth > 0 && widget.minHeight > 0) {
                        // Ensure that all widgets we show can be added on a
                        // workspace of this size
                        int[] spanXY = Launcher.getSpanForWidget(mLauncher, widget);
                        int[] minSpanXY = Launcher.getMinSpanForWidget(mLauncher, widget);
                        int minSpanX = Math.min(spanXY[0], minSpanXY[0]);
                        int minSpanY = Math.min(spanXY[1], minSpanXY[1]);
                        if (minSpanX <= LauncherModel.getCellCountX() &&
                                minSpanY <= LauncherModel.getCellCountY()) {
                            mWidgets.add(widget);
                        }
                    }
                } else if (o instanceof ResolveInfo) {
                    mWidgets.add(o);
                } /*else if (o instanceof GadgetInfo) {
                    GadgetInfo gadget = (GadgetInfo) o;
                    mWidgets.add(gadget);
                }*/
            }
        }
    }
    /*private String getGadgetLabel(GadgetInfo info) {
        int[] cellSpan = new int[]{info.spanX, info.spanY};
        int hSpan = Math.min(cellSpan[0], LauncherModel.getCellCountX());
        int vSpan = Math.min(cellSpan[1], LauncherModel.getCellCountY());
        String dimensionsFormatString = mLauncher.getString(R.string.widget_dims_format);
        String gadetName = ThemeUtils.getGadgetName(mLauncher, info);
        String result = gadetName +"_" + String.format(dimensionsFormatString, hSpan, vSpan);
        return result;
    }*/

    /**
     * Creates and executes a new AsyncTask to load a page of widget previews.
     */
    @SuppressLint("NewApi")
    private void prepareLoadWidgetPreviewsTask(int position, Object widget,
            final ImageView previewImageView) {

        // Prune all tasks that are no longer needed
        Iterator<AppsCustomizeAsyncTask> iter = mRunningTasks.iterator();
        while (iter.hasNext()) {
            AppsCustomizeAsyncTask task = (AppsCustomizeAsyncTask) iter.next();
            int taskPage = task.page;
            if (taskPage < position - ITEM_PER_SCREEN ||
                    taskPage > position + ITEM_PER_SCREEN) {
                task.cancel(false);
                iter.remove();
            } /*
               * else {
               * task.setThreadPriority(getThreadPriorityForPage(taskPage)); }
               */
        }

        // We introduce a slight delay to order the loading of side pages so
        // that we don't thrash
        AsyncTaskPageData pageData = new AsyncTaskPageData(position, widget,
                new AsyncTaskCallback() {
                    @Override
                    public void run(AppsCustomizeAsyncTask task, AsyncTaskPageData data) {
                        try {
                            loadWidgetPreviewsInBackground(task, data);
                        } finally {
                            if (task.isCancelled()) {
                                data.cleanup(true);
                            }
                        }
                    }
                },
                new AsyncTaskCallback() {
                    @Override
                    public void run(AppsCustomizeAsyncTask task, AsyncTaskPageData data) {
                        mRunningTasks.remove(task);
                        if (task.isCancelled())
                            return;
                        // do cleanup inside onSyncWidgetPageItems
                        // onSyncWidgetPageItems(data);
                        if ((data.generatedImage != null) && (data.generatedImage.isRecycled() == false)) {
                            previewImageView.setImageBitmap(data.generatedImage);
                        }
                    }
                }, mWidgetPreviewLoader, previewImageView);

        // Ensure that the task is appropriately prioritized and runs in
        // parallel
        AppsCustomizeAsyncTask t = new AppsCustomizeAsyncTask(position,
                AsyncTaskPageData.Type.LoadWidgetPreviewData);
        t.setThreadPriority(Process.THREAD_PRIORITY_LESS_FAVORABLE);
        t.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, pageData);
        mRunningTasks.add(t);
    }

    private void loadWidgetPreviewsInBackground(AppsCustomizeAsyncTask task,
            AsyncTaskPageData data) {
        // loadWidgetPreviewsInBackground can be called without a task to load a
        // set of widget
        // previews synchronously
        if (task != null) {
            // Ensure that this task starts running at the correct priority
            task.syncThreadPriority();
        }
        if (!task.isCancelled()) {
            data.generatedImage = mWidgetPreviewLoader.getPreview(data.item);
        }
    }

    class ViewHolder {
        public int position;
        public ImageView previewImgView;
        public TextView titleTextView;
        public ImageView previewChecked;
    }

}

/**
 * A simple callback interface which also provides the results of the task.
 */
interface AsyncTaskCallback {
    void run(AppsCustomizeAsyncTask task, AsyncTaskPageData data);
}

/**
 * The data needed to perform either of the custom AsyncTasks.
 */
class AsyncTaskPageData {
    enum Type {
        LoadWidgetPreviewData
    }

    AsyncTaskPageData(int p, Object l, int cw, int ch, AsyncTaskCallback bgR,
            AsyncTaskCallback postR, WidgetPreviewLoader w, ImageView previewImageView) {
        page = p;
        item = l;
        maxImageWidth = cw;
        maxImageHeight = ch;
        doInBackgroundCallback = bgR;
        postExecuteCallback = postR;
        widgetPreviewLoader = w;
        previewView = previewImageView;
    }

    AsyncTaskPageData(int p, Object l, AsyncTaskCallback bgR,
            AsyncTaskCallback postR, WidgetPreviewLoader w, ImageView previewImageView) {
        this(p, l, 0, 0, bgR, postR, w, previewImageView);
    }

    void cleanup(boolean cancelled) {
        // Clean up any references to source/generated bitmaps
        if (generatedImage != null) {
            if (cancelled) {
                widgetPreviewLoader.recycleBitmap(item, generatedImage);
            }
        }
        generatedImage = null;
    }

    int page;
    Object item;
    Bitmap generatedImage;
    int maxImageWidth;
    int maxImageHeight;
    AsyncTaskCallback doInBackgroundCallback;
    AsyncTaskCallback postExecuteCallback;
    WidgetPreviewLoader widgetPreviewLoader;
    ImageView previewView;
}

/**
 * A generic template for an async task used in AppsCustomize.
 */
class AppsCustomizeAsyncTask extends AsyncTask<AsyncTaskPageData, Void, AsyncTaskPageData> {
    AppsCustomizeAsyncTask(int p, AsyncTaskPageData.Type ty) {
        page = p;
        threadPriority = Process.THREAD_PRIORITY_DEFAULT;
        dataType = ty;
    }

    @Override
    protected AsyncTaskPageData doInBackground(AsyncTaskPageData... params) {
        if (params.length != 1)
            return null;
        // Load each of the widget previews in the background
        params[0].doInBackgroundCallback.run(this, params[0]);
        return params[0];
    }

    @Override
    protected void onPostExecute(AsyncTaskPageData result) {
        // All the widget previews are loaded, so we can just callback to
        // inflate the page
        result.postExecuteCallback.run(this, result);
    }

    void setThreadPriority(int p) {
        threadPriority = p;
    }

    void syncThreadPriority() {
        Process.setThreadPriority(threadPriority);
    }

    // The page that this async task is associated with
    AsyncTaskPageData.Type dataType;
    int page;
    int threadPriority;
}
