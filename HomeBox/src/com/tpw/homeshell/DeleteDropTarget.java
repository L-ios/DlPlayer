/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tpw.homeshell;

//import tpw.v3.gadget.GadgetView;
import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.drawable.TransitionDrawable;
import android.os.Vibrator;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;

import com.tpw.homeshell.CellLayout.LayoutParams;
import com.tpw.homeshell.LauncherSettings.Favorites;
import com.tpw.homeshell.icon.BubbleTextView;
import com.tpw.homeshell.icon.IconManager;
import com.tpw.homeshell.model.LauncherModel;

public class DeleteDropTarget extends ButtonDropTarget {
    private static int FLING_DELETE_ANIMATION_DURATION = 100;
    private static int FLING_MOVE_ANIMATION_DURATION = 10;
    private static float FLING_TO_DELETE_FRICTION = 0.035f;
    private static int MODE_FLING_DELETE_TO_TRASH = 0;
    private static int MODE_FLING_DELETE_ALONG_VECTOR = 1;

    private final int mFlingDeleteMode = MODE_FLING_DELETE_ALONG_VECTOR;

    /* private ColorStateList mOriginalTextColor; */
    private TransitionDrawable mCurrentDrawable;
    
    private Vibrator mVibrator;
    private boolean mDragEntered = false;
    
    public DeleteDropTarget(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DeleteDropTarget(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        Resources r = getResources();
        mCurrentDrawable = (TransitionDrawable) r.getDrawable(R.drawable.remove_target_selector);
        mHoverColor = r.getColor(R.color.delete_target_hover_tint);
/*
        // Get the drawable
        mOriginalTextColor = getTextColors();

        // Get the hover color
        mHoverColor = r.getColor(R.color.delete_target_hover_tint);
        mUninstallDrawable = (TransitionDrawable) 
                r.getDrawable(R.drawable.uninstall_target_selector);
        mRemoveDrawable = (TransitionDrawable) r.getDrawable(R.drawable.remove_target_selector);

        mRemoveDrawable.setCrossFadeEnabled(true);
        mUninstallDrawable.setCrossFadeEnabled(true);

        // The current drawable is set to either the remove drawable or the uninstall drawable 
        // and is initially set to the remove drawable, as set in the layout xml.

        // Remove the text in the Phone UI in landscape
        int orientation = getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            if (!LauncherApplication.isScreenLarge()) {
                setText("");
            }
        }*/
    }

    private boolean isAllAppsApplication(DragSource source, Object info) {
        return /*(source instanceof AppsCustomizePagedView) && */(info instanceof ApplicationInfo);
    }
    private boolean isAllAppsWidget(DragSource source, Object info) {
        if (source instanceof AppsCustomizePagedView) {
            if (info instanceof PendingAddItemInfo) {
                PendingAddItemInfo addInfo = (PendingAddItemInfo) info;
                switch (addInfo.itemType) {
                    case LauncherSettings.Favorites.ITEM_TYPE_SHORTCUT:
                    case LauncherSettings.Favorites.ITEM_TYPE_APPWIDGET:
                        return true;
                }
            }
        }
        return false;
    }
    private boolean isDragSourceWorkspaceOrFolder(DragObject d) {
        return (d.dragSource instanceof Workspace) || (d.dragSource instanceof Folder);
    }
    private boolean isWorkspaceOrFolderApplication(DragObject d) {
        return isDragSourceWorkspaceOrFolder(d) && (d.dragInfo instanceof ShortcutInfo);
    }
    private boolean isWorkspaceOrFolderWidget(DragObject d) {
        return isDragSourceWorkspaceOrFolder(d) && (d.dragInfo instanceof LauncherAppWidgetInfo);
    }
    private boolean isWorkspaceFolder(DragObject d) {
        return (d.dragSource instanceof Workspace) && (d.dragInfo instanceof FolderInfo);
    }
   
    private boolean isWorkspaceGadget(DragObject d) {
        return (d.dragSource instanceof Workspace) && (d.dragInfo instanceof GadgetItemInfo);
    }

    private boolean isHideseatApplication(DragObject d) {
        return (d.dragSource instanceof Hideseat) && (d.dragInfo instanceof ShortcutInfo);
    }

    private void setHoverColor() {
        mCurrentDrawable.startTransition(mTransitionDuration);
        setTextColor(mHoverColor);
    }
    private void resetHoverColor() {
        mCurrentDrawable.resetTransition();
        /* setTextColor(mOriginalTextColor); */
    }

    @Override
    public boolean acceptDrop(DragObject d) {
        // We can remove everything including App shortcuts, folders, widgets, etc.
        return true;
    }

    @Override
    public void onDragStart(DragSource source, Object info, int dragAction) {
       /* boolean isVisible = true;
        boolean isUninstall = false;*/
        if(mLauncher.isItemUnDeletable(info)) {
            return;
        }
        mActive = true;
        /*
        // If we are dragging a widget from AppsCustomize, hide the delete target
        if (isAllAppsWidget(source, info)) {
            isVisible = false;
        }

        // If we are dragging an application from AppsCustomize, only show the control if we can
        // delete the app (it was downloaded), and rename the string to "uninstall" in such a case
        if (isAllAppsApplication(source, info)) {
            ApplicationInfo appInfo = (ApplicationInfo) info;
            if ((appInfo.flags & ApplicationInfo.DOWNLOADED_FLAG) != 0) {
                isUninstall = true;
            } else {
                isVisible = false;
            }
        }

        if (isUninstall) {
            setCompoundDrawablesRelativeWithIntrinsicBounds(mUninstallDrawable, null, null, null);
        } else {
            setCompoundDrawablesRelativeWithIntrinsicBounds(mRemoveDrawable, null, null, null);
        }
        mCurrentDrawable = (TransitionDrawable) getCurrentDrawable();

        mActive = isVisible;
        resetHoverColor();
        ((ViewGroup) getParent()).setVisibility(isVisible ? View.VISIBLE : View.GONE);
        if (getText().length() > 0) {
            setText(isUninstall ? R.string.delete_target_uninstall_label
                : R.string.delete_target_label);
        }*/
    }

    @Override
    public void onDragEnd() {
        super.onDragEnd();
        mActive = false;
    }

    public void onDragEnter(DragObject d) {
        if(mLauncher.isItemUnDeletable(d.dragInfo)) {
            return;
        }
        super.onDragEnter(d);
        setHoverColor();
        if(!mDragEntered) {
            mDragEntered = true;
            if(mVibrator == null)
                mVibrator = (Vibrator)mLauncher.getSystemService(Context.VIBRATOR_SERVICE);
            long [] pattern = {300,100};
            mVibrator.vibrate(pattern,-1);
        }
    }

    public void onDragExit(DragObject d) {
        super.onDragExit(d);
        if(mLauncher.isItemUnDeletable(d.dragInfo)) {
            return;
        }
        if (!d.dragComplete) {
            resetHoverColor();
        } else {
            // Restore the hover color if we are deleting
            d.dragView.setColor(mHoverColor);
        }
        if(mDragEntered) {
            mDragEntered = false;
            mVibrator.cancel();
        }
    }

    private void animateToTrashAndCompleteDrop(final DragObject d) {
        DragLayer dragLayer = mLauncher.getDragLayer();
        Rect from = new Rect();
        dragLayer.getViewRectRelativeToSelf(d.dragView, from);
        // Rect to = getIconRect(d.dragView.getMeasuredWidth(), d.dragView.getMeasuredHeight(),
        // mCurrentDrawable.getIntrinsicWidth(), mCurrentDrawable.getIntrinsicHeight());
        Resources r = mLauncher.getResources();
        int left = r.getDimensionPixelSize(R.dimen.delete_droptarget_padding);
	    int top = r.getDimensionPixelSize(R.dimen.delete_droptarget_padding);
	    int right = left + mCurrentDrawable.getIntrinsicWidth();
	    int bottom = top + mCurrentDrawable.getIntrinsicHeight();
        Rect to = new Rect(left, top, right, bottom);
        float scale = (float) to.width() / from.width();

        mSearchDropTargetBar.deferOnDragEnd();

        super.onDragExit(d);

        final Bitmap dragView = buildCacheBitmap(d);

        Runnable onAnimationEndRunnable = new Runnable() {
            @Override
            public void run() {
                mSearchDropTargetBar.onDragEnd();
                mLauncher.exitSpringLoadedDragMode();
                completeDrop(d, dragView);
            }
        };
        ItemInfo item = (ItemInfo) d.dragInfo;
		if (item.isDeletable())
			dragLayer.animateView(d.dragView, from, to, scale, 1f, 1f, 1f, 1f,
					0, new DecelerateInterpolator(2),
					new LinearInterpolator(), onAnimationEndRunnable,
					DragLayer.ANIMATION_END_DISAPPEAR, null);
		else {
			to = getIconRect(d.dragView.getMeasuredWidth(),
					d.dragView.getMeasuredHeight(),
					mCurrentDrawable.getIntrinsicWidth(), mCurrentDrawable.getIntrinsicHeight());
			dragLayer.animateView(d.dragView, from, to, scale, 1f, 1f, 1f,
					1f, 0,
					new DecelerateInterpolator(2), new LinearInterpolator(),
					onAnimationEndRunnable, DragLayer.ANIMATION_END_DISAPPEAR,
					null);
		}
    }

    private Bitmap buildCacheBitmap(DragObject d) {
        View v = null;
        int textColor = -1;
        BubbleTextView bubble = null;

        // When the DragObject comes from hide-seat, the dragged view cannot
        // be retrieved from workspace.
        if (d.dragSource instanceof Hideseat) {
            // retrieve icon bitmap from hide-seat
            v = mLauncher.getHideseat().getDragView();
            if (v instanceof BubbleTextView) {
                bubble = (BubbleTextView) v;
            } else {
                v = d.dragView;
            }
        } else {
            // retrieve icon bitmap from workspace
            CellLayout.CellInfo cellInfo = mLauncher.getWorkspace().getDragInfo();
            if (cellInfo != null) {
                View cell = cellInfo.cell;
                if (cell instanceof BubbleTextView) {
                    v = cell;
                    bubble = (BubbleTextView) v;
                } else if (cell instanceof FolderIcon) {
                    v = cell;
                    bubble = ((FolderIcon) v).getTitleText();
                }
            }
        }
        // remove the application, pops the top frame icon text color is not the same when it on the desktop.
        // For card-icons, the text color won't be modified; For hotseat and hideseat icons, the text color
        // will be temporarily changed, to ensure high contrast between text and background.
        // final boolean customizeColor = bubble != null && !(v instanceof
        // FolderIcon) &&
        // (bubble.isInHotseat() || /* hotseat and hideseat icon */
        // !bubble.isSupportCard()); /* not big card*/
        IconManager im = ((LauncherApplication) LauncherApplication.getContext()).getIconManager();
        final boolean customizeColor = bubble != null
                && (!im.supprtCardIcon() || (!(v instanceof FolderIcon) && bubble.isInHotseatOrHideseat()));
        if (customizeColor) {
            textColor = bubble.getCurrentTextColor();
            bubble.setTextColor(getResources().getColor(R.color.common_text_color_uninstall_title));
            bubble.setHideseatIconFadingFilterEnable(false);
        }
        if (v == null) {
            v = d.dragView;
        }

        boolean enabled = v.isDrawingCacheEnabled();
        if (!enabled) {
            v.setDrawingCacheEnabled(true);
        }
        v.destroyDrawingCache();
        Bitmap cache = v.getDrawingCache();

        if (cache != null) {
            cache = Bitmap.createBitmap(cache);
        }

        if (!enabled) {
            v.setDrawingCacheEnabled(false);
        }
        // set properties back to previous values
        if (customizeColor) {
            bubble.setTextColor(textColor);
            bubble.setHideseatIconFadingFilterEnable(true);
        }
        return cache;
    }

    private void completeDrop(DragObject d, Bitmap dragBitmap) {
        ItemInfo item = (ItemInfo) d.dragInfo;
        boolean toDelete = true;

        if (isAllAppsApplication(d.dragSource, item)) {
            // Uninstall the application if it is being dragged from AppsCustomize
            toDelete = false; // delete item in delete dialog.
            mLauncher.startApplicationUninstallActivity((ApplicationInfo) item,null,dragBitmap);
        } else if (isWorkspaceOrFolderApplication(d) || isHideseatApplication(d)) {
            //uninstall app and remove other item type 
            mLauncher.getWorkspace().checkAndRemoveEmptyCell();
            if(!item.isDeletable()) {
                toDelete = false;
                mLauncher.reVisibileDraggedItem(item);
            } else {
                if(item.itemType == Favorites.ITEM_TYPE_APPLICATION) {
                    toDelete = false; // delete item in delete dialog.
                    mLauncher.startApplicationUninstallActivity((ShortcutInfo) item, dragBitmap);
                } else {
                    LauncherModel.deleteItemFromDatabase(mLauncher, item);
                    //call the replace method after fling the downloading app
                    mLauncher.checkAndReplaceFolderIfNecessary(item);
                    //add toast for delete the download app
                    if(item.itemType == Favorites.ITEM_TYPE_SHORTCUT_DOWNLOADING){
                        ShortcutInfo info = (ShortcutInfo)item;
                        sendDownloadIconDeleteBroadcastToAppStore(info);
                        mLauncher.showToastMessage(R.string.delete_download_info);
                    }

                    else if (item.itemType == Favorites.ITEM_TYPE_VPINSTALL) {
                        //TODO: is toast needed? like download item
                    } else if (item.itemType == LauncherSettings.BaseLauncherColumns.ITEM_TYPE_SHORTCUT) {
                        UserTrackerHelper.sendUserReport(UserTrackerMessage.MSG_REMOVE_WIDGET,
                                item instanceof ShortcutInfo && ((ShortcutInfo)item).intent != null ?
                                        ((ShortcutInfo)item).intent.toString() : "");
                    }

                    if(mLauncher.isContainerHotseat(item.container)) {
                        mLauncher.getHotseat().onDrop(true, -1, null, null, true);
                    }
                }
            }
        } else if (isWorkspaceFolder(d)) {
            /*// Remove the folder from the workspace and delete the contents from launcher model
            FolderInfo folderInfo = (FolderInfo) item;
            mLauncher.removeFolder(folderInfo);
            LauncherModel.deleteFolderContentsFromDatabase(mLauncher, folderInfo);*/
            //show toast and revisible the folder
            //mLauncher.showToastMessage(R.string.toast_folder_undeletable);
            //mLauncher.reVisibileWorkspaceItem();
            //mLauncher.exitFullScreen();
            mLauncher.getWorkspace().checkAndRemoveEmptyCell();
            mLauncher.dismissFolder((FolderInfo) d.dragInfo, dragBitmap);
            toDelete = false;
        } else if (isWorkspaceOrFolderWidget(d)) {
            // Remove the widget from the workspace
            mLauncher.removeAppWidget((LauncherAppWidgetInfo) item);
            mLauncher.getWorkspace().checkAndRemoveEmptyCell();
            LauncherModel.deleteItemFromDatabase(mLauncher, item);

            final LauncherAppWidgetInfo launcherAppWidgetInfo = (LauncherAppWidgetInfo) item;
            final LauncherAppWidgetHost appWidgetHost = mLauncher.getAppWidgetHost();
            if (appWidgetHost != null) {
                // Deleting an app widget ID is a void call but writes to disk before returning
                // to the caller...
                new Thread("deleteAppWidgetId") {
                    public void run() {
                        appWidgetHost.deleteAppWidgetId(launcherAppWidgetInfo.appWidgetId);
                    }
                }.start();
            }
        } else if (isWorkspaceGadget(d)) {
            View cell = ((Workspace)d.dragSource).getDragInfo().cell;
            // cleanUp GadgetView immediately
            //if (cell instanceof GadgetView)
            //    ((GadgetView)cell).cleanUp();
            LauncherModel.deleteItemFromDatabase(mLauncher, item);
            mLauncher.getWorkspace().checkAndRemoveEmptyCell();
        }

        if (toDelete) {
            mLauncher.getWorkspace().removeDragItemFromList(item);
            if (dragBitmap != null) {
                dragBitmap.recycle();
            }
        }
    }

    public void onDrop(DragObject d) {
        if (d != null) {
            animateToTrashAndCompleteDrop(d);
        }
        String name = "";
        if (d != null && d.dragInfo != null) {
            if (d.dragInfo instanceof LauncherAppWidgetInfo
                    && ((LauncherAppWidgetInfo) d.dragInfo).providerName != null) {
                name = ((LauncherAppWidgetInfo) d.dragInfo).providerName
                        .toString();
            } else if (d.dragInfo instanceof ItemInfo
                    && ((ItemInfo) d.dragInfo).title != null) {
                name = ((ItemInfo) d.dragInfo).title.toString();
            }
        }
        UserTrackerHelper.sendUserReport(UserTrackerMessage.MSG_DRAG_TO_DELETE, name);
    }

    /**
     * Creates an animation from the current drag view to the delete trash icon.
     */
    private AnimatorUpdateListener createFlingToTrashAnimatorListener(final DragLayer dragLayer,
            DragObject d, PointF vel, ViewConfiguration config) {
        final Rect to = getIconRect(d.dragView.getMeasuredWidth(), d.dragView.getMeasuredHeight(),
                mCurrentDrawable.getIntrinsicWidth(), mCurrentDrawable.getIntrinsicHeight());
        final Rect from = new Rect();
        dragLayer.getViewRectRelativeToSelf(d.dragView, from);

        // Calculate how far along the velocity vector we should put the intermediate point on
        // the bezier curve
        float velocity = Math.abs(vel.length());
        float vp = Math.min(1f, velocity / (config.getScaledMaximumFlingVelocity() / 2f));
        int offsetY = (int) (-from.top * vp);
        int offsetX = (int) (offsetY / (vel.y / vel.x));
        final float y2 = from.top + offsetY;                        // intermediate t/l
        final float x2 = from.left + offsetX;
        final float x1 = from.left;                                 // drag view t/l
        final float y1 = from.top;
        final float x3 = to.left;                                   // delete target t/l
        final float y3 = to.top;

        final TimeInterpolator scaleAlphaInterpolator = new TimeInterpolator() {
            @Override
            public float getInterpolation(float t) {
                return t * t * t * t * t * t * t * t;
            }
        };
        return new AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                final DragView dragView = (DragView) dragLayer.getAnimatedView();
                float t = ((Float) animation.getAnimatedValue()).floatValue();
                float tp = scaleAlphaInterpolator.getInterpolation(t);
                float initialScale = dragView.getInitialScale();
                float finalAlpha = 0.5f;
                float scale = dragView.getScaleX();
                float x1o = ((1f - scale) * dragView.getMeasuredWidth()) / 2f;
                float y1o = ((1f - scale) * dragView.getMeasuredHeight()) / 2f;
                float x = (1f - t) * (1f - t) * (x1 - x1o) + 2 * (1f - t) * t * (x2 - x1o) +
                        (t * t) * x3;
                float y = (1f - t) * (1f - t) * (y1 - y1o) + 2 * (1f - t) * t * (y2 - x1o) +
                        (t * t) * y3;

                dragView.setTranslationX(x);
                dragView.setTranslationY(y);
                dragView.setScaleX(initialScale * (1f - tp));
                dragView.setScaleY(initialScale * (1f - tp));
                dragView.setAlpha(finalAlpha + (1f - finalAlpha) * (1f - tp));
            }
        };
    }
    private AnimatorUpdateListener createFlingToMoveAnimatorListener(final DragLayer dragLayer,
            DragObject d, final PointF vel, ViewConfiguration config, final long startTime, final boolean next) {
        final Rect from = new Rect();
        dragLayer.getViewRectRelativeToSelf(d.dragView, from);
        final float friction = 1f - (dragLayer.getResources().getDisplayMetrics().density * FLING_TO_DELETE_FRICTION);
        int dWidth = dragLayer.getWidth();
        if(Math.abs(vel.x) < (vel.x > 0 ? dWidth - d.x : d.x))
            vel.x = vel.x > 0 ? dWidth : -dWidth;
        return new AnimatorUpdateListener() {
            private static final float FLING_ROTATION_ANGLE = 90;
            private boolean mHasOffsetForScale;
            private long prevTime = startTime;
            private final TimeInterpolator mAlphaInterpolator = new DecelerateInterpolator(0.3f);
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                final DragView dragView = (DragView) dragLayer.getAnimatedView();
                long curTime = AnimationUtils.currentAnimationTimeMillis();
                float t = ((Float) animation.getAnimatedValue()).floatValue();

                if (!mHasOffsetForScale) {
                    mHasOffsetForScale = true;
                    float scale = dragView.getScaleX();
                    float xOffset = ((scale - 1f) * dragView.getMeasuredWidth()) / 2f;
                    float yOffset = ((scale - 1f) * dragView.getMeasuredHeight()) / 2f;

                    from.left += xOffset;
                    from.top += yOffset;
                }

                from.left += (vel.x * (curTime - prevTime) / 1000f);
                from.top += (vel.y * (curTime - prevTime) / 1000f);

                dragView.setTranslationX(from.left);
                dragView.setTranslationY(from.top);
                dragView.setRotation((next ? FLING_ROTATION_ANGLE : -FLING_ROTATION_ANGLE) * t);
                dragView.setAlpha(1f - mAlphaInterpolator.getInterpolation(t));

                vel.x *= friction;
                vel.y *= friction;
                prevTime = curTime;
            }
        };
    }

    /**
     * Creates an animation from the current drag view along its current velocity vector.
     * For this animation, the alpha runs for a fixed duration and we update the position
     * progressively.
     */
    private static class FlingAlongVectorAnimatorUpdateListener implements AnimatorUpdateListener {
        private DragLayer mDragLayer;
        private PointF mVelocity;
        private Rect mFrom;
        private long mPrevTime;
        private boolean mHasOffsetForScale;
        private float mFriction;

        private final TimeInterpolator mAlphaInterpolator = new DecelerateInterpolator(0.75f);

        public FlingAlongVectorAnimatorUpdateListener(DragLayer dragLayer, PointF vel, Rect from,
                long startTime, float friction) {
            mDragLayer = dragLayer;
            mVelocity = vel;
            mFrom = from;
            mPrevTime = startTime;
            mFriction = 1f - (dragLayer.getResources().getDisplayMetrics().density * friction);
        }

        @Override
        public void onAnimationUpdate(ValueAnimator animation) {
            final DragView dragView = (DragView) mDragLayer.getAnimatedView();
            float t = ((Float) animation.getAnimatedValue()).floatValue();
            long curTime = AnimationUtils.currentAnimationTimeMillis();

            if (!mHasOffsetForScale) {
                mHasOffsetForScale = true;
                float scale = dragView.getScaleX();
                float xOffset = ((scale - 1f) * dragView.getMeasuredWidth()) / 2f;
                float yOffset = ((scale - 1f) * dragView.getMeasuredHeight()) / 2f;

                mFrom.left += xOffset;
                mFrom.top += yOffset;
            }

            mFrom.left += (mVelocity.x * (curTime - mPrevTime) / 1000f);
            mFrom.top += (mVelocity.y * (curTime - mPrevTime) / 1000f);

            dragView.setTranslationX(mFrom.left);
            dragView.setTranslationY(mFrom.top);
            dragView.setAlpha(1f - mAlphaInterpolator.getInterpolation(t));

            mVelocity.x *= mFriction;
            mVelocity.y *= mFriction;
            mPrevTime = curTime;
        }
    };
    private AnimatorUpdateListener createFlingAlongVectorAnimatorListener(final DragLayer dragLayer,
            DragObject d, PointF vel, final long startTime, final int duration,
            ViewConfiguration config) {
        final Rect from = new Rect();
        dragLayer.getViewRectRelativeToSelf(d.dragView, from);

        return new FlingAlongVectorAnimatorUpdateListener(dragLayer, vel, from, startTime,
                FLING_TO_DELETE_FRICTION);
    }

    public void onFlingToDelete(final DragObject d, int x, int y, PointF vel) {
    	Log.e(TAG, "on Fling to delete");
        final boolean isAllApps = /*d.dragSource instanceof AppsCustomizePagedView */ false;
        
        // Don't highlight the icon as it's animating
        if (d != null) {
            d.dragView.setColor(0);
            d.dragView.updateInitialScaleToCurrentScale();
        }
        // Don't highlight the target if we are flinging from AllApps
        if (isAllApps) {
            resetHoverColor();
        }

        if (mFlingDeleteMode == MODE_FLING_DELETE_TO_TRASH) {
            // Defer animating out the drop target if we are animating to it
            mSearchDropTargetBar.deferOnDragEnd();
            mSearchDropTargetBar.finishAnimations();
        }

        final ViewConfiguration config = ViewConfiguration.get(mLauncher);
        final DragLayer dragLayer = mLauncher.getDragLayer();
        final int duration = FLING_DELETE_ANIMATION_DURATION;
        final long startTime = AnimationUtils.currentAnimationTimeMillis();

        // NOTE: Because it takes time for the first frame of animation to actually be
        // called and we expect the animation to be a continuation of the fling, we have
        // to account for the time that has elapsed since the fling finished.  And since
        // we don't have a startDelay, we will always get call to update when we call
        // start() (which we want to ignore).
        final TimeInterpolator tInterpolator = new TimeInterpolator() {
            private int mCount = -1;
            private float mOffset = 0f;

            @Override
            public float getInterpolation(float t) {
                if (mCount < 0) {
                    mCount++;
                } else if (mCount == 0) {
                    mOffset = Math.min(0.5f, (float) (AnimationUtils.currentAnimationTimeMillis() -
                            startTime) / duration);
                    mCount++;
                }
                return Math.min(1f, mOffset + t);
            }
        };
        AnimatorUpdateListener updateCb = null;
        if (mFlingDeleteMode == MODE_FLING_DELETE_TO_TRASH) {
            updateCb = createFlingToTrashAnimatorListener(dragLayer, d, vel, config);
        } else if (mFlingDeleteMode == MODE_FLING_DELETE_ALONG_VECTOR) {
            updateCb = createFlingAlongVectorAnimatorListener(dragLayer, d, vel, startTime,
                    duration, config);
        }
        DragView view = null;
        if (d != null) {
            super.onDragExit(d);
            view = d.dragView;
        }

        final Bitmap dragView = buildCacheBitmap(d);
        Runnable onAnimationEndRunnable = new Runnable() {
            @Override
            public void run() {
                mSearchDropTargetBar.onDragEnd();
                // If we are dragging from AllApps, then we allow AppsCustomizePagedView to clean up
                // itself, otherwise, complete the drop to initiate the deletion process
                if (!isAllApps) {
                    mLauncher.exitSpringLoadedDragMode();
                    completeDrop(d, dragView);
                }
                mLauncher.getDragController().onDeferredEndFling(d);
            }
        };
        if (d != null) {
            dragLayer.animateView(d.dragView, updateCb, duration, tInterpolator,
                    onAnimationEndRunnable, DragLayer.ANIMATION_END_DISAPPEAR, null);
        }
        String name = "";
        if (d != null && d.dragInfo != null) {
            if (d.dragInfo instanceof LauncherAppWidgetInfo
                    && ((LauncherAppWidgetInfo) d.dragInfo).providerName != null) {
                name = ((LauncherAppWidgetInfo) d.dragInfo).providerName
                        .toString();
            } else if (d.dragInfo instanceof ItemInfo
                    && ((ItemInfo) d.dragInfo).title != null) {
                name = ((ItemInfo) d.dragInfo).title.toString();
            }
        }
        UserTrackerHelper.sendUserReport(
                UserTrackerMessage.MSG_FLING_TO_DELETE, name);
    }
    public void onFlingToMove(final DragObject d, int x, int y, PointF vel, final boolean next) {
        final boolean isAllApps = false;

        // Don't highlight the icon as it's animating
        if (d != null) {
            d.dragView.setColor(0);
            d.dragView.updateInitialScaleToCurrentScale();
        }
        // Don't highlight the target if we are flinging from AllApps
        if (isAllApps) {
            resetHoverColor();
        }

        final ViewConfiguration config = ViewConfiguration.get(mLauncher);
        final DragLayer dragLayer = mLauncher.getDragLayer();
        final int duration = FLING_MOVE_ANIMATION_DURATION;
        final long startTime = AnimationUtils.currentAnimationTimeMillis();

        // NOTE: Because it takes time for the first frame of animation to actually be
        // called and we expect the animation to be a continuation of the fling, we have
        // to account for the time that has elapsed since the fling finished.  And since
        // we don't have a startDelay, we will always get call to update when we call
        // start() (which we want to ignore).
        final TimeInterpolator tInterpolator = new TimeInterpolator() {
            private int mCount = -1;
            private float mOffset = 0f;

            @Override
            public float getInterpolation(float t) {
                if (mCount < 0) {
                    mCount++;
                } else if (mCount == 0) {
                    mOffset = Math.min(0.5f, (float) (AnimationUtils.currentAnimationTimeMillis() -
                            startTime) / duration);
                    mCount++;
                }
                return Math.min(1f, mOffset + t);
            }
        };
        AnimatorUpdateListener updateCb = null;
        if (d != null) {
            updateCb = createFlingToMoveAnimatorListener(dragLayer, d, vel, config, startTime, next);
        }
        Runnable onAnimationEndRunnable = new Runnable() {
            @Override
            public void run() {
                View v = dragLayer.getAnimatedView();
                final float rotation = v.getRotation();
                final float y = v.getY();

                // If we are dragging from AllApps, then we allow AppsCustomizePagedView to clean up
                // itself, otherwise, complete the drop to initiate the deletion process
                if (!isAllApps) {
                    mLauncher.exitSpringLoadedDragMode();
                    completeMoveDrop(d, next, y, rotation);
                }
                mLauncher.getDragController().onDeferredEndFling(d);
            }
        };
        if (d != null) {
            dragLayer.animateView(d.dragView, updateCb, duration, tInterpolator,
                    onAnimationEndRunnable, DragLayer.ANIMATION_END_DISAPPEAR, null);
        }
        String name = "";
        if (d != null && d.dragInfo != null) {
            if (d.dragInfo instanceof LauncherAppWidgetInfo
                    && ((LauncherAppWidgetInfo) d.dragInfo).providerName != null) {
                name = ((LauncherAppWidgetInfo) d.dragInfo).providerName
                        .toString();
            } else if (d.dragInfo instanceof ItemInfo
                    && ((ItemInfo) d.dragInfo).title != null) {
                name = ((ItemInfo) d.dragInfo).title.toString();
            }
        }
        UserTrackerHelper.sendUserReport(UserTrackerMessage.MSG_FLING_TO_MOVE,
                name);
    }

    private void completeMoveDrop(DragObject d, final boolean next, final float y, final float roatation) {
        final ItemInfo item = (ItemInfo) d.dragInfo;
        boolean isFolder = isWorkspaceFolder(d);
        
        final Workspace workspace = mLauncher.getWorkspace();
        final int screen = mLauncher.getCurrentWorkspaceScreen();
        //CellLayout.CellInfo dragInfo = workspace.getDragInfo();
        final View cell = workspace.getDragItemFromList(item, false);
        if(cell!=null){
            Log.d(TAG, "sxsexe-------------------------------->completeMoveDrop cell " + cell + " tag " + ((ItemInfo)cell.getTag()));
        }else{
        	Log.d(TAG, "sxsexe-------------------------------->completeMoveDrop cell is null");
        }
        if (isWorkspaceOrFolderApplication(d) || isFolder) {
            if (cell != null) {
                final ScreenPosition sp = LauncherModel.findEmptyCellForFling(screen, next ? 1 : 0);
                Log.d(TAG, "sxsexe--------------------->completeMoveDrop item " + item.title + " find sp " + sp + " X " + cell.getX() + " Y " + cell.getY());
                if(sp == null || ((workspace.getChildCount()-1 == screen)&&next)) {
                    CellLayout cellLayout = (CellLayout) workspace.getChildAt(screen);
                    Log.d(TAG, "sxsexe------------> fling back " + item.title);
                    workspace.checkAndRemoveEmptyCell();
                    cellLayout.flingBack(cell, roatation, y, next);
                    return;
                } else {
                    final CellLayout cellLayout = (CellLayout) workspace.getChildAt(sp.s);
                    final ViewParent parent = cell.getParent();
                    if(parent != null && cellLayout != null) {
                        ViewGroup parentView = (ViewGroup)parent;
                        parentView.removeView(cell);
                        LayoutParams lp = (CellLayout.LayoutParams)cell.getLayoutParams();
                        lp.cellX = sp.x;
                        lp.cellY = sp.y;
                        lp.useTmpCoords = false;
                        int childId = LauncherModel.getCellLayoutChildId(item.container, screen, sp.x, sp.y, 1, 1);
                        //workspace.addInScreen(cell, item.container, sp.s, sp.x, sp.y,item.spanX, item.spanY, false);
                        cellLayout.addViewToCellLayout(cell, -1, childId, lp, true);
                        LauncherModel.moveItemInDatabase(mLauncher, item, item.container, sp.s, sp.x, sp.y);
                        cell.setVisibility(View.VISIBLE);
                        cellLayout.addPengindFlingDropDownTarget(cell, y, roatation, next, item.title.toString(), sp.x, sp.y);
                        workspace.checkAndRemoveEmptyCell();
                    } else {
                        Log.e(TAG, "sxsexe--><><> completeMoveDrop item  " + item.title + " parent is " + parent + " cellLayout is " + cellLayout);
                    }
                }
                workspace.removeDragItemFromList(item);
                
                if(mLauncher.getHotseat().isTouchInHotseat()) {
                    mLauncher.getHotseat().onExitHotseat(false);
                }
                
            } else {
                mLauncher.reVisibileDraggedItem(item);
            }
            /*} else {
                mLauncher.getWorkspace().checkAndRemoveEmptyCell();
                LauncherModel.deleteItemFromDatabase(mLauncher, item);
            }*/
        }
    }

/*    private void findLastEmptyCell(int[] xy, CellLayout layout) {
        if (layout == null)
            return;
        int cY = layout.getCountY() - 1;
        int cX = layout.getCountX() - 1;
        for (int y = cY; y >= 0; y--) {
            for (int x = cX; x >= 0; x--) {
                if (layout.getChildAt(x, y) != null) {
                    if (x == cX) {
                        if (y == cY) {
                            xy[0] = -1;
                            xy[1] = -1;
                            return;
                        }
                        xy[0] = 0;
                        xy[1] = y + 1;
                    } else {
                        xy[0] = x + 1;
                        xy[1] = y;
                    }
                    return;
                }
            }
        }
        xy[0] = -1;
        xy[1] = -1;
    }

    private void findFirstEmptyCell(int[] xy, CellLayout layout) {
        for (int y = 0, YN = layout.getCountY(); y < YN; y++) {
            for (int x = 0, XN = layout.getCountX(); x < XN; x++) {
                if (layout.getChildAt(x, y) == null) {
                    xy[0] = x;
                    xy[1] = y;
                    return;
                }
            }
        }
        xy[0] = -1;
        xy[1] = -1;
    }*/

    /**
     * when delete downloading icon, send broad cast to AppStore to stop downloading
     */
    private void sendDownloadIconDeleteBroadcastToAppStore(ShortcutInfo info){
        String pkgName = info.intent.getStringExtra(AppDownloadManager.TYPE_PACKAGENAME);
        Intent intent = new Intent(AppDownloadManager.ACTION_HS_DOWNLOAD_TASK);
        intent.putExtra(AppDownloadManager.TYPE_ACTION, AppDownloadManager.ACTION_HS_DOWNLOAD_CANCEL);
        intent.putExtra(AppDownloadManager.TYPE_PACKAGENAME, pkgName);
        getContext().sendBroadcast(intent);
        LauncherApplication app = (LauncherApplication)getContext().getApplicationContext();
        app.getModel().getAppDownloadManager().updatepPckageDownloadCancelTimeByHS(pkgName);
    }
}
