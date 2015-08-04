
package com.tpw.homeshell.editmode;

import com.tpw.homeshell.CellLayout;
import com.tpw.homeshell.DragController;
import com.tpw.homeshell.DragSource;
import com.tpw.homeshell.FastBitmapDrawable;
import com.tpw.homeshell.ItemInfo;
import com.tpw.homeshell.Launcher;
import com.tpw.homeshell.LauncherApplication;
import com.tpw.homeshell.LauncherSettings;
import com.tpw.homeshell.PendingAddGadgetInfo;
import com.tpw.homeshell.PendingAddShortcutInfo;
import com.tpw.homeshell.UserTrackerHelper;
import com.tpw.homeshell.UserTrackerMessage;
import com.tpw.homeshell.WidgetPreviewLoader;
import com.tpw.homeshell.DropTarget.DragObject;
import com.tpw.homeshell.icon.IconManager;
import com.tpw.homeshell.themeutils.ThemeUtils;
import com.tpw.homeshell.PendingAddItemInfo;
import com.tpw.homeshell.PendingAddWidgetInfo;
import com.tpw.homeshell.Workspace;
import com.tpw.homeshell.R;
import com.tpw.homeshell.editmode.ThemesPreviewAdapter.ThemeAttr;
import com.tpw.homeshell.editmode.WallpapersPreviewAdapter.WallpaperAttr;

//import tpw.v3.gadget.GadgetInfo;
import android.appwidget.AppWidgetProviderInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ImageView;
import android.view.View.OnLongClickListener;

public class PreviewList extends HorizontalListView implements
        OnItemClickListener, DragSource, OnItemLongClickListener {

    private static final int OP_SET = 1;

    private Launcher mLauncher;
    private DragController mDragController;
    private Canvas mCanvas;
    private IconManager mIconManager = null;
    private Boolean mDraggingWidget;
    private PackageManager mPackageManager;
    private WidgetPreviewLoader mWidgetPreviewLoader;

    public PreviewList(Context context, AttributeSet attrs) {
        super(context, attrs);
        setOnItemClickListener(this);
        setOnItemLongClickListener(this);
        mCanvas = new Canvas();
        mIconManager = ((LauncherApplication) context.getApplicationContext())
                .getIconManager();
        mPackageManager = context.getPackageManager();
        mWidgetPreviewLoader = new WidgetPreviewLoader((Launcher) context);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,
            long id) {
        Adapter adapter = parent.getAdapter();
        if (adapter instanceof EffectsPreviewAdapter) {
            EffectsPreviewAdapter effectsAdapter = (EffectsPreviewAdapter) adapter;
            effectsAdapter.setEffectValue(position);
            effectsAdapter.notifyDataSetChanged();
            mLauncher.getWorkspace().animateScrollEffect(true);
        } else if (adapter instanceof WidgetPreviewAdapter) {
            WidgetPreviewAdapter widgetAdapter = (WidgetPreviewAdapter) adapter;
            Object info = adapter.getItem(position);
            if (info instanceof AppWidgetProviderInfo) {
                AppWidgetProviderInfo providerInfo = (AppWidgetProviderInfo) info;
                mLauncher.addAppWidget(providerInfo);
            } /*else if (info instanceof GadgetInfo) {
                GadgetInfo gadgetInfo = (GadgetInfo) info;
                mLauncher.addGadgetWidget(gadgetInfo);
            } */else if (info instanceof ResolveInfo) {
                ResolveInfo resolveInfo = (ResolveInfo) info;
                mLauncher.addShortcut(resolveInfo);
            }
            CellLayout cl = (CellLayout) mLauncher.getWorkspace().getChildAt(
                    mLauncher.getCurrentScreen());
            if (cl != null && cl.hasChild()) {
                cl.removeEditBtnContainer();
            }
        } else if (adapter instanceof WallpapersPreviewAdapter) {
            WallpapersPreviewAdapter wallpaperAdapter = (WallpapersPreviewAdapter) adapter;
            WallpaperAttr attr = (WallpaperAttr) wallpaperAdapter.getItem(position);
            if (attr != null) {
                Intent intent = new Intent("com.yunos.theme.thememanager.ACTION_MANAGE_THEME");
                intent.putExtra("type", "wallpaper");
                intent.putExtra("is_system", attr.isSystem);
                intent.putExtra("kid", attr.keyID);
                intent.putExtra("operation", OP_SET);
                mContext.startService(intent);
            }
        } else if (adapter instanceof ThemesPreviewAdapter) {
            ThemesPreviewAdapter themeAdapter = (ThemesPreviewAdapter) adapter;
            ThemeAttr attr = (ThemeAttr) themeAdapter.getItem(position);
            if(attr != null){
                Intent intent = new Intent("com.yunos.theme.thememanager.ACTION_MANAGE_THEME");
                intent.putExtra("type", "theme");
                intent.putExtra("package_name", attr.packageName);
                intent.putExtra("operation", OP_SET);

                mContext.startService(intent);
           }
        }
        //add by huangweiwei, topwise, 2015-7-1
        else if (adapter instanceof WidgetPagePreviewAdapter) {
        	WidgetPagePreviewAdapter widgetPageAdapter = (WidgetPagePreviewAdapter) adapter;
        	widgetPageAdapter.onClickItem(position);
        	widgetPageAdapter.notifyDataSetChanged();
        }
        //add end by huangweiwei, topwise, 2015-7-1
    }

    private boolean beginDraggingWidget(View view, Object itemInfo) {
        mDraggingWidget = true;
        // Get the widget preview as the drag representation
        ImageView image = (ImageView) view.findViewById(R.id.preview_image);
        // ImageView image = (ImageView) v.findViewById(R.id.widget_preview);
        // PendingAddItemInfo createItemInfo = (PendingAddItemInfo) v.getTag();

        // If the ImageView doesn't have a drawable yet, the widget preview
        // hasn't been loaded and
        // we abort the drag.
        if (image.getDrawable() == null) {
            mDraggingWidget = false;
            return false;
        }

        // Compose the drag image
        Bitmap preview;
        Bitmap outline;
        float scale = 1f;
        Point previewPadding = null;
        PendingAddItemInfo createItemInfo = null;
        if (itemInfo instanceof AppWidgetProviderInfo) {
            // This can happen in some weird cases involving multi-touch. We
            // can't start dragging
            // the widget if this is null, so we break out.
            createItemInfo = new PendingAddWidgetInfo(
                    (AppWidgetProviderInfo) itemInfo, null, null);

            PendingAddWidgetInfo createWidgetInfo = (PendingAddWidgetInfo) createItemInfo;
            createItemInfo = createWidgetInfo;
            int[] spanXY = Launcher.getSpanForWidget(mLauncher,
                    (AppWidgetProviderInfo) itemInfo);
            createItemInfo.spanX = spanXY[0];
            createItemInfo.spanY = spanXY[1];
            int spanX = createItemInfo.spanX;
            int spanY = createItemInfo.spanY;
            int[] minSpanXY = Launcher.getMinSpanForWidget((Context) mLauncher,
                    (AppWidgetProviderInfo) itemInfo);
            createItemInfo.minSpanX = minSpanXY[0];
            createItemInfo.minSpanY = minSpanXY[1];
            createItemInfo.itemType = LauncherSettings.Favorites.ITEM_TYPE_APPWIDGET;
            int[] size = mLauncher.getWorkspace().estimateItemSize(spanX,
                    spanY, createWidgetInfo, true);

            BitmapDrawable previewDrawable = (BitmapDrawable) image
                    .getDrawable();
            float minScale = 1.25f;
            int maxWidth, maxHeight;
            maxWidth = Math.min(
                    (int) (previewDrawable.getIntrinsicWidth() * minScale),
                    size[0]);
            maxHeight = Math.min(
                    (int) (previewDrawable.getIntrinsicHeight() * minScale),
                    size[1]);

            int[] previewSizeBeforeScale = new int[1];
            Bitmap itemBitmap = previewDrawable.getBitmap();
            if (itemBitmap.isRecycled()) {
                preview = mWidgetPreviewLoader.generateWidgetPreview(
                        createWidgetInfo.componentName,
                        createWidgetInfo.previewImage, ((AppWidgetProviderInfo) itemInfo).icon,
                        spanX, spanY, maxWidth, maxHeight, null,
                        previewSizeBeforeScale);
            } else {
                preview = Bitmap.createBitmap(itemBitmap);
            }

            if (preview == null) {
                return false;
            }
            int previewWidthInAppsCustomize = Math.min(previewSizeBeforeScale[0],
                    mWidgetPreviewLoader.maxWidthForWidgetPreview(spanX));
            // int previewWidthInAppsCustomize = previewSizeBeforeScale[0];
            scale = previewWidthInAppsCustomize / (float) preview.getWidth();

            // The bitmap in the AppsCustomize tray is always the the same size,
            // so there might be extra pixels around the preview itself - this
            // accounts for that
            if (previewWidthInAppsCustomize < previewDrawable
                    .getIntrinsicWidth()) {
                int padding = (previewDrawable.getIntrinsicWidth() - previewWidthInAppsCustomize) / 2;
                previewPadding = new Point(padding, 0);
            }

            UserTrackerHelper.sendUserReport(UserTrackerMessage.MSG_ADD_WIDGET,
                    createWidgetInfo.componentName == null ? ""
                            : createWidgetInfo.componentName.toString());
        } /*else if (itemInfo instanceof GadgetInfo) {
            createItemInfo = new PendingAddGadgetInfo((GadgetInfo) itemInfo);
            PendingAddGadgetInfo info = (PendingAddGadgetInfo) createItemInfo;
            int[] size = mLauncher.getWorkspace().estimateItemSize(info.spanX,
                    info.spanY, createItemInfo, true);
            preview = Bitmap.createBitmap(size[0], size[1],
                    Bitmap.Config.ARGB_8888);
            mCanvas.setBitmap(preview);
            mCanvas.save();
            Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
            mCanvas.drawBitmap(ThemeUtils.getGadgetPreview(getContext(),
                    info.gadgetInfo, size[0], size[1]), 0, 0, p);
            mCanvas.restore();
            mCanvas.setBitmap(null);

        } */else {
            ResolveInfo info = (ResolveInfo) itemInfo;
            createItemInfo = new PendingAddShortcutInfo(
                     info.activityInfo);
            createItemInfo.componentName = new ComponentName(info.activityInfo.packageName,
                     info.activityInfo.name);
            Drawable icon = mIconManager
                    .getFullResIcon(((PendingAddShortcutInfo) createItemInfo).shortcutActivityInfo);
            preview = Bitmap.createBitmap(icon.getIntrinsicWidth(),
                    icon.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);

            mCanvas.setBitmap(preview);
            mCanvas.save();
            WidgetPreviewLoader.renderDrawableToBitmap(icon, preview, 0, 0,
                    icon.getIntrinsicWidth(), icon.getIntrinsicHeight());
            mCanvas.restore();
            mCanvas.setBitmap(null);
            createItemInfo.spanX = createItemInfo.spanY = 1;
        }

        // Don't clip alpha values for the drag outline if we're using the
        // default widget preview
        boolean clipAlpha = !(createItemInfo instanceof PendingAddWidgetInfo && (((PendingAddWidgetInfo) createItemInfo).previewImage == 0));

        // Save the preview for the outline generation, then dim the preview
        outline = Bitmap.createScaledBitmap(preview, preview.getWidth(),
                preview.getHeight(), false);

        // Start the drag
        mLauncher.lockScreenOrientation();
        mLauncher.getWorkspace().onDragStartedWithItem(createItemInfo, outline,
                clipAlpha);
        mDragController.startDrag(image, preview, this, createItemInfo,
                DragController.DRAG_ACTION_COPY, previewPadding, scale);
        outline.recycle();
        preview.recycle();
        return true;
    }

    private boolean endDraggingWidget(View target, boolean success) {
        return true;
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view,
            int position, long id) {
        if ((!view.isInTouchMode())
                || (mLauncher.getWorkspace().isSwitchingState())
                || (!mLauncher.isDraggingEnabled())) {
            return false;
        }

        Adapter adapter = parent.getAdapter();
        Object info;
        if (adapter instanceof WidgetPreviewAdapter) {
            WidgetPreviewAdapter widgetAdapter = (WidgetPreviewAdapter) adapter;
            info = adapter.getItem(position);
            beginDraggingWidget(view, info);
        }
        return true;
    }

    @Override
    public boolean supportsFlingToDelete() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void onFlingToDeleteCompleted() {
        // TODO Auto-generated method stub

    }

    @Override
    public void onDropCompleted(View target, DragObject d,
            boolean isFlingToDelete, boolean success) {
        endDraggingWidget(target, success);
        if (!success) {
            boolean showOutOfSpaceMessage = false;
            if (target instanceof Workspace) {
                int currentScreen = mLauncher.getCurrentWorkspaceScreen();
                Workspace workspace = (Workspace) target;
                CellLayout layout = (CellLayout) workspace
                        .getChildAt(currentScreen);
                ItemInfo itemInfo = (ItemInfo) d.dragInfo;
                if (layout != null) {
                    layout.calculateSpans(itemInfo);
                    showOutOfSpaceMessage = !layout.findCellForSpan(null,
                            itemInfo.spanX, itemInfo.spanY);
                }
            }
            if (showOutOfSpaceMessage) {
                mLauncher.showOutOfSpaceMessage(false);
            }

            d.deferDragViewCleanupPostAnimation = false;
        } else {
            if (target instanceof Workspace) {
                int currentScreen = mLauncher.getCurrentWorkspaceScreen();
                Workspace workspace = (Workspace) target;
                CellLayout layout = (CellLayout) workspace
                        .getChildAt(currentScreen);
                layout.removeEditBtnContainer();
            }
        }
    }

    public void setup(Launcher launcher, DragController dragController) {
        mLauncher = launcher;
        mDragController = dragController;
    }
}
