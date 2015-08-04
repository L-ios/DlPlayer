 /* Copyright (C) 2008 The Android Open Source Project
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

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

//import tpw.v3.gadget.GadgetView;
import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.animation.Animator.AnimatorListener;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.app.WallpaperManager;
import android.appwidget.AppWidgetHostView;
import android.appwidget.AppWidgetProviderInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.Region.Op;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.IBinder;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.SparseArray;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import com.tpw.homeshell.Alarm.OnAlarmListener;
import com.tpw.homeshell.CellLayout.CellInfo;
import com.tpw.homeshell.FolderIcon.FolderRingAnimator;
import com.tpw.homeshell.Launcher;
import com.tpw.homeshell.LauncherSettings.Favorites;
import com.tpw.homeshell.appfreeze.AppFreezeUtil;
import com.tpw.homeshell.appgroup.AppGroupManager;
import com.tpw.homeshell.appgroup.AppGroupManager.Callback;
import com.tpw.homeshell.icon.BubbleTextView;
import com.tpw.homeshell.icon.IconManager;
import com.tpw.homeshell.model.LauncherModel;
import com.tpw.homeshell.setting.HomeShellSetting;
import commonlibs.utils.ACA;

// ##description: Added support for widget page
import com.tpw.homeshell.widgetpage.WidgetPageManager;

/**
 * The workspace is a wide area with a wallpaper and a finite number of pages.
 * Each page contains a number of icons, folders or widgets the user can
 * interact with. A workspace is meant to be used with a fixed width only.
 */
public class Workspace extends AnimationPagedView
        implements DropTarget, DragSource, DragScroller, View.OnTouchListener,
        DragController.DragListener, LauncherTransitionable, ViewGroup.OnHierarchyChangeListener {
    private static final String TAG = "Launcher.Workspace";

    // Y rotation to apply to the workspace screens
    private static final float WORKSPACE_OVERSCROLL_ROTATION = 24f;

    private static final int CHILDREN_OUTLINE_FADE_OUT_DELAY = 0;
    private static final int CHILDREN_OUTLINE_FADE_OUT_DURATION = 375;
    private static final int CHILDREN_OUTLINE_FADE_IN_DURATION = 100;

    private static final int BACKGROUND_FADE_OUT_DURATION = 350;
    private static final int ADJACENT_SCREEN_DROP_DURATION = 300;
    private static final int FLING_THRESHOLD_VELOCITY = 500;

    // These animators are used to fade the children's outlines
    private ObjectAnimator mChildrenOutlineFadeInAnimation;
    private ObjectAnimator mChildrenOutlineFadeOutAnimation;
    private float mChildrenOutlineAlpha = 0;

    // These properties refer to the background protection gradient used for AllApps and Customize
    // private ValueAnimator mBackgroundFadeInAnimation;
    private ValueAnimator mBackgroundFadeOutAnimation;
    private Drawable mBackground;
    boolean mDrawBackground = true;
    private float mBackgroundAlpha = 0;
    private float mOverScrollMaxBackgroundAlpha = 0.0f;

    private float mWallpaperScrollRatio = 1.0f;
    private int mOriginalPageSpacing;

    private final WallpaperManager mWallpaperManager;
    private IBinder mWindowToken;
    private static final float WALLPAPER_SCREENS_SPAN = 2f;

    private int mDefaultPage = ConfigManager.getDefaultScreen();

    /**
     * CellInfo for the cell that is currently being dragged
     */
    private CellLayout.CellInfo mDragInfo;
    private CellLayout.CellInfo mDragInfoDelete;

    /**
     * Target drop area calculated during last acceptDrop call.
     */
    private int[] mTargetCell = new int[2];
    private int mDragOverX = -1;
    private int mDragOverY = -1;

    static Rect mLandscapeCellLayoutMetrics = null;
    static Rect mPortraitCellLayoutMetrics = null;

    /**
     * The CellLayout that is currently being dragged over
     */
    private CellLayout mDragTargetLayout = null;
    /**
     * The CellLayout that we will show as glowing
     */
    private CellLayout mDragOverlappingLayout = null;

    /**
     * The CellLayout which will be dropped to
     */
    private CellLayout mDropToLayout = null;

    private Launcher mLauncher;
    private DragController mDragController;

    // These are temporary variables to prevent having to allocate a new object just to
    // return an (x, y) value from helper functions. Do NOT use them to maintain other state.
    private int[] mTempCell = new int[2];
    private int[] mTempEstimate = new int[2];
    private float[] mDragViewVisualCenter = new float[2];
    private float[] mTempDragCoordinates = new float[2];
    private float[] mTempCellLayoutCenterCoordinates = new float[2];
    private float[] mTempDragBottomRightCoordinates = new float[2];
    private Matrix mTempInverseMatrix = new Matrix();

    private SpringLoadedDragController mSpringLoadedDragController;
    private float mSpringLoadedShrinkFactor;
    
    private List<View> mDragItems = new ArrayList<View>();

//    private static final int DEFAULT_CELL_COUNT_X = 4;
//    private static final int DEFAULT_CELL_COUNT_Y = 4;

    // State variable that indicates whether the pages are small (ie when you're
    // in all apps or customize mode)
    enum State { NORMAL, SPRING_LOADED, SMALL };
    private State mState = State.NORMAL;
    private boolean mIsSwitchingState = false;

    boolean mAnimatingViewIntoPlace = false;
    boolean mIsDragOccuring = false;
    boolean mChildrenLayersEnabled = true;

    /** Is the user is dragging an item near the edge of a page? */
    private boolean mInScrollArea = false;

    private final HolographicOutlineHelper mOutlineHelper = new HolographicOutlineHelper();
    private Bitmap mDragOutline = null;
    private final Rect mTempRect = new Rect();
    private final int[] mTempXY = new int[2];
    private int[] mTempVisiblePagesRange = new int[2];
    private float mOverscrollFade = 0;
    private boolean mOverscrollTransformsSet;
    public static final int DRAG_BITMAP_PADDING = 2;
    private boolean mWorkspaceFadeInAdjacentScreens;

    enum WallpaperVerticalOffset { TOP, MIDDLE, BOTTOM };
    int mWallpaperWidth;
    int mWallpaperHeight;
    WallpaperOffsetInterpolator mWallpaperOffset;
    boolean mUpdateWallpaperOffsetImmediately = false;
    private Runnable mDelayedResizeRunnable;
    private Runnable mDelayedSnapToPageRunnable;
    private Point mDisplaySize = new Point();
    private boolean mIsStaticWallpaper;
    private int mWallpaperTravelWidth;
    private int mSpringLoadedPageSpacing;
    private int mCameraDistance;

    // Variables relating to the creation of user folders by hovering shortcuts over shortcuts
    private static final int FOLDER_CREATION_TIMEOUT = 0;
    private static final int REORDER_TIMEOUT = 300;
    private boolean mEmptyScreenAdded = false;
    private final Alarm mFolderCreationAlarm = new Alarm();
    private final Alarm mReorderAlarm = new Alarm();
    private FolderRingAnimator mDragFolderRingAnimator = null;
    private FolderIcon mDragOverFolderIcon = null;
    private boolean mCreateUserFolderOnDrop = false;
    private boolean mAddToExistingFolderOnDrop = false;
    private DropTarget.DragEnforcer mDragEnforcer;
    private float mMaxDistanceForFolderCreation;

    private boolean mEditModeFeatrueFlag = true;  
    private boolean mAnimateScrollEffectMode = false;
    private Runnable mScrollEffectAnimator = new Runnable() {
        public void run() {
            int toPage = 0;
            toPage = mCurrentPage == 0 ? 1 : mCurrentPage - 1;
            snapToPage(toPage, 400);
        }
    };

    // Variables relating to touch disambiguation (scrolling workspace vs. scrolling a widget)
    private float mXDown;
    private float mYDown;
    final static float START_DAMPING_TOUCH_SLOP_ANGLE = (float) Math.PI / 6;
    final static float MAX_SWIPE_ANGLE = (float) Math.PI / 3;
    final static float TOUCH_SLOP_DAMPING_FACTOR = 4;

    // Relating to the animation of items being dropped externally
    public static final int ANIMATE_INTO_POSITION_AND_DISAPPEAR = 0;
    public static final int ANIMATE_INTO_POSITION_AND_REMAIN = 1;
    public static final int ANIMATE_INTO_POSITION_AND_RESIZE = 2;
    public static final int COMPLETE_TWO_STAGE_WIDGET_DROP_ANIMATION = 3;
    public static final int CANCEL_TWO_STAGE_WIDGET_DROP_ANIMATION = 4;

    // Related to dragging, folder creation and reordering
    private static final int DRAG_MODE_NONE = 0;
    private static final int DRAG_MODE_CREATE_FOLDER = 1;
    private static final int DRAG_MODE_ADD_TO_FOLDER = 2;
    private static final int DRAG_MODE_REORDER = 3;
    private int mDragMode = DRAG_MODE_NONE;
    private int mLastReorderX = -1;
    private int mLastReorderY = -1;

    private SparseArray<Parcelable> mSavedStates;
    private final ArrayList<Integer> mRestoredPages = new ArrayList<Integer>();

    // These variables are used for storing the initial and final values during workspace animations
    private int mSavedScrollX;
    private float mSavedRotationY;
    private float mSavedTranslationX;
    private float mCurrentScaleX;
    private float mCurrentScaleY;
    private float mCurrentRotationY;
    private float mCurrentTranslationX;
    private float mCurrentTranslationY;
    private float[] mOldTranslationXs;
    private float[] mOldTranslationYs;
    private float[] mOldScaleXs;
    private float[] mOldScaleYs;
    private float[] mOldBackgroundAlphas;
    private float[] mOldAlphas;
    private float[] mNewTranslationXs;
    private float[] mNewTranslationYs;
    private float[] mNewScaleXs;
    private float[] mNewScaleYs;
    private float[] mNewBackgroundAlphas;
    private float[] mNewAlphas;
    private float[] mNewRotationYs;
    private float mTransitionProgress;
    
    protected View mDropTargetView;
    
    private AnimatorSet mAnimatorSet;
    private boolean mUnlockAnimationEnable = true;
    private boolean mIsPlayUnlockAniamtion;
    private final static int MOVE_ANIMATION_DURATION = 450;
    private final static int ELASTIC_ANIMATION_DURATION = 700;
    private final static int NEXT_ANIMATION_DELAY = 15;
    private final int[] DELAY_INDECIES = {
            11, 5, 7, 15,
            13, 3, 2, 9,
            14, 1, 0, 10,
            12, 4, 6, 8,
    };
    private final int[] ROTATE_ANGLE = {
            15, 15,
            -5, 5,
            -5, -5,
            15, -15,
    };
    private Context mContext = null;
    
    public static final float FOLDER_CREATION_FACTOR = 0.55f; 
    //adjustment the effect of the merger folder
    public static final float FOLDER_CREATION_FACTOR_CARDMODE = 0.8f;

    private final Runnable mBindPages = new Runnable() {
        @Override
        public void run() {
            mLauncher.getModel().bindRemainingSynchronousPages();
        }
    };

    private Callback mCallback = new Callback() {
        
        @Override
        public void onResult(final FolderIcon fi, final String folderName) {
            if (!TextUtils.isEmpty(folderName)) {
                post(new Runnable() {

                    @Override
                    public void run() {
                        fi.getFolderInfo().setTitle("");
                        fi.setTitle(folderName);
                    }
                });
            }
        }
    };

    // Folder optimization:
    // Make a delay when drag icon from folder to dock.
    private static final int sDragFromFolderToHotseatDelay = 1000;
    private boolean mDragFromFolderToHotseatEnable = true;
    private final Alarm mDragFromFolderToHotseatAlarm = new Alarm() {
        {
            setOnAlarmListener(new OnAlarmListener() {
                @Override
                public void onAlarm(Alarm alarm) {
                    mDragFromFolderToHotseatEnable = true;
                }
            });
        }
    };

    /**
     * Used to inflate the Workspace from XML.
     *
     * @param context The application's context.
     * @param attrs The attributes set containing the Workspace's customization values.
     */
    public Workspace(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
        mContext = context;
    }

    /**
     * Used to inflate the Workspace from XML.
     *
     * @param context The application's context.
     * @param attrs The attributes set containing the Workspace's customization values.
     * @param defStyle Unused.
     */
    public Workspace(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mContext = context;
        mContentIsRefreshable = false;
        mOriginalPageSpacing = mPageSpacing;
        mIsConfigPadding = true;
        mDragEnforcer = new DropTarget.DragEnforcer(context);
        // With workspace, data is available straight from the get-go
        setDataIsReady();

        mLauncher = (Launcher) context;
        final Resources res = getResources();
        mWorkspaceFadeInAdjacentScreens = res.getBoolean(R.bool.config_workspaceFadeAdjacentScreens);
        mFadeInAdjacentScreens = false;
        mWallpaperManager = WallpaperManager.getInstance(context);

//        int cellCountX = DEFAULT_CELL_COUNT_X;
//        int cellCountY = DEFAULT_CELL_COUNT_Y;
      int cellCountX = ConfigManager.DEFAULT_CELL_COUNT_X;
      int cellCountY = ConfigManager.DEFAULT_CELL_COUNT_Y;

        TypedArray a = context.obtainStyledAttributes(attrs,
                R.styleable.Workspace, defStyle, 0);

        if (LauncherApplication.isScreenLarge()) {
            // Determine number of rows/columns dynamically
            // TODO: This code currently fails on tablets with an aspect ratio < 1.3.
            // Around that ratio we should make cells the same size in portrait and
            // landscape
            TypedArray actionBarSizeTypedArray =
                context.obtainStyledAttributes(new int[] { android.R.attr.actionBarSize });
            final float actionBarHeight = actionBarSizeTypedArray.getDimension(0, 0f);
            actionBarSizeTypedArray.recycle();
            
            Point minDims = new Point();
            Point maxDims = new Point();
            mLauncher.getWindowManager().getDefaultDisplay().getCurrentSizeRange(minDims, maxDims);

            cellCountX = 1;
            while (CellLayout.widthInPortrait(res, cellCountX + 1) <= minDims.x) {
                cellCountX++;
            }

            cellCountY = 1;
            while (actionBarHeight + CellLayout.heightInLandscape(res, cellCountY + 1)
                <= minDims.y) {
                cellCountY++;
            }
        }

        mSpringLoadedShrinkFactor =
            res.getInteger(R.integer.config_workspaceSpringLoadShrinkPercentage) / 100.0f;
        mSpringLoadedPageSpacing =
                res.getDimensionPixelSize(R.dimen.workspace_spring_loaded_page_spacing);
        mCameraDistance = res.getInteger(R.integer.config_cameraDistance);

        // if the value is manually specified, use that instead
        // Get values of configures from ConfigManager
//        cellCountX = a.getInt(R.styleable.Workspace_cellCountX, cellCountX);
//        cellCountY = a.getInt(R.styleable.Workspace_cellCountY, cellCountY);
        cellCountX = ConfigManager.getCellCountX();
        cellCountY = ConfigManager.getCellCountY();
        // start modify by huangxunwan for config default page
        //mDefaultPage = a.getInt(R.styleable.Workspace_defaultScreen, 1);
        mDefaultPage = ConfigManager.getDefaultScreen();
        // end modify by huangxunwan for config default page
        a.recycle();

        setOnHierarchyChangeListener(this);

        LauncherModel.updateWorkspaceLayoutCells(cellCountX, cellCountY);
        setHapticFeedbackEnabled(false);

        initWorkspace();

        // Disable multitouch across the workspace/all apps/customize tray
        setMotionEventSplittingEnabled(true);

        // Unless otherwise specified this view is important for accessibility.
        if (getImportantForAccessibility() == View.IMPORTANT_FOR_ACCESSIBILITY_AUTO) {
            setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_YES);
        }
    }

    // estimate the size of a widget with spans hSpan, vSpan. return MAX_VALUE for each
    // dimension if unsuccessful
    public int[] estimateItemSize(int hSpan, int vSpan,
            ItemInfo itemInfo, boolean springLoaded) {
        int[] size = new int[2];
        if (getChildCount() > 0) {
            CellLayout cl = (CellLayout) mLauncher.getWorkspace().getChildAt(0);
            Rect r = estimateItemPosition(cl, itemInfo, 0, 0, hSpan, vSpan);
            size[0] = r.width();
            size[1] = r.height();
            if (springLoaded) {
                size[0] *= mSpringLoadedShrinkFactor;
                size[1] *= mSpringLoadedShrinkFactor;
            }
            return size;
        } else {
            size[0] = Integer.MAX_VALUE;
            size[1] = Integer.MAX_VALUE;
            return size;
        }
    }
    public Rect estimateItemPosition(CellLayout cl, ItemInfo pendingInfo,
            int hCell, int vCell, int hSpan, int vSpan) {
        Rect r = new Rect();
        cl.cellToRect(hCell, vCell, hSpan, vSpan, r);
        return r;
    }

    public void onDragStart(DragSource source, Object info, int dragAction) {
        mIsDragOccuring = true;
        updateChildrenLayersEnabled(false);
        mLauncher.lockScreenOrientation();
        setChildrenBackgroundAlphaMultipliers(1f);
        // Prevent any Un/InstallShortcutReceivers from updating the db while we are dragging
        InstallShortcutReceiver.enableInstallQueue();
        UninstallShortcutReceiver.enableUninstallQueue();
    }

    public void onDragEnd() {
        mIsDragOccuring = false;
        updateChildrenLayersEnabled(false);
        mLauncher.unlockScreenOrientation(false);

        // Re-enable any Un/InstallShortcutReceiver and now process any queued items
        InstallShortcutReceiver.disableAndFlushInstallQueue(getContext());
        UninstallShortcutReceiver.disableAndFlushUninstallQueue(getContext());
    }

    /**
     * Initializes various states for this workspace.
     */
    final protected void initWorkspace() {
        Context context = getContext();
        mCurrentPage = mDefaultPage;
        Launcher.setScreen(mCurrentPage);
        LauncherApplication app = (LauncherApplication)context.getApplicationContext();
        setWillNotDraw(false);
        setClipChildren(false);
        setClipToPadding(false);
        setChildrenDrawnWithCacheEnabled(true);

        final Resources res = getResources();
        try {
            mBackground = res.getDrawable(R.drawable.apps_customize_bg);
        } catch (Resources.NotFoundException e) {
            // In this case, we will skip drawing background protection
        }

        mWallpaperOffset = new WallpaperOffsetInterpolator();
        Display display = mLauncher.getWindowManager().getDefaultDisplay();
        display.getSize(mDisplaySize);
        mWallpaperTravelWidth = (int) (mDisplaySize.x *
                wallpaperTravelToScreenWidthRatio(mDisplaySize.x, mDisplaySize.y));

        float factor = mLauncher.getIconManager().supprtCardIcon() ? FOLDER_CREATION_FACTOR_CARDMODE : FOLDER_CREATION_FACTOR;
        mMaxDistanceForFolderCreation = (factor * res.getDimensionPixelSize(R.dimen.app_icon_size));
        mFlingThresholdVelocity = (int) (FLING_THRESHOLD_VELOCITY * mDensity);
    }

    @Override
    public void onChildViewAdded(View parent, View child) {
        super.onChildViewAdded(parent, child);
        if (!(child instanceof CellLayout)) {
            throw new IllegalArgumentException("A Workspace can only have CellLayout children.");
        }
        CellLayout cl = ((CellLayout) child);
        cl.setOnInterceptTouchListener(this);
        cl.setClickable(true);
        cl.setContentDescription(getContext().getString(
                R.string.workspace_description_format, getChildCount()));
    }

    @Override
    public void onChildViewRemoved(View parent, View child) {
        super.onChildViewRemoved(parent, child);
    }

    protected boolean shouldDrawChild(View child) {
        final CellLayout cl = (CellLayout) child;
        return super.shouldDrawChild(child) &&
            (cl.getShortcutsAndWidgets().getAlpha() > 0 ||
             cl.getBackgroundAlpha() > 0);
    }

    /**
     * @return The open folder on the current screen, or null if there is none
     */
    public Folder getOpenFolder() {
        DragLayer dragLayer = mLauncher.getDragLayer();
        int count = dragLayer.getChildCount();
        for (int i = 0; i < count; i++) {
            View child = dragLayer.getChildAt(i);
            if (child instanceof Folder) {
                Folder folder = (Folder) child;
                if (folder.getInfo().opened)
                    return folder;
            }
        }
        return null;
    }

    boolean isTouchActive() {
        return mTouchState != TOUCH_STATE_REST;
    }

    
    //insert icon in hotseat 
    /**
     * Adds the specified child in the specified screen. The position and dimension of
     * the child are defined by x, y, spanX and spanY.
     *
     * @param child The child to add in one of the workspace's screens.
     * @param screen The screen in which to add the child.
     * @param x The X position of the child in the screen's grid.
     * @param y The Y position of the child in the screen's grid.
     * @param spanX The number of cells spanned horizontally by the child.
     * @param spanY The number of cells spanned vertically by the child.
     */
    void addInScreen(View child, long container, int screen, int x, int y, int spanX, int spanY) {
        addInScreen(child, container, screen, x, y, spanX, spanY, false);
    }
    
    void addInHotseat(View child, long container, int screen, int x, int y, int spanX, int spanY, int index) {

        final CellLayout layout = mLauncher.getHotseat().getLayout();
        child.setOnKeyListener(null);

        // Hide folder title in the hotseat
        if (child instanceof FolderIcon) {
            ((FolderIcon) child).setTextVisible(false);
        }

        if (screen < 0) {
            screen = mLauncher.getHotseat().getOrderInHotseat(x, y);
        } else {
            // Note: We do this to ensure that the hotseat is always laid out in the orientation
            // of the hotseat in order regardless of which orientation they were added
            x = mLauncher.getHotseat().getCellXFromOrder(screen);
            y = mLauncher.getHotseat().getCellYFromOrder(screen);
        }
        LayoutParams genericLp = child.getLayoutParams();
        CellLayout.LayoutParams lp;
        if (genericLp == null || !(genericLp instanceof CellLayout.LayoutParams)) {
            lp = new CellLayout.LayoutParams(x, y, spanX, spanY);
        } else {
            lp = (CellLayout.LayoutParams) genericLp;
            lp.cellX = x;
            lp.cellY = y;
            lp.cellHSpan = spanX;
            lp.cellVSpan = spanY;
        }

        if (spanX < 0 && spanY < 0) {
            lp.isLockedToGrid = false;
        }

        // Get the canonical child id to uniquely represent this view in this screen
        int childId = LauncherModel.getCellLayoutChildId(container, screen, x, y, spanX, spanY);
        boolean markCellsAsOccupied = !(child instanceof Folder);
        if(child != null && child.getParent() != null) {
            ((ViewGroup)child.getParent()).removeView(child);
        }
        
        ShortcutAndWidgetContainer viewParent = mLauncher.getHotseat().getContainer();
        index = index >= viewParent.getChildCount() ? -1 : index;
        
        if (!layout.addViewToCellLayout(child, index, childId, lp, markCellsAsOccupied)) {
            // TODO: This branch occurs when the workspace is adding views
            // outside of the defined grid
            // maybe we should be deleting these items from the LauncherModel?
            Log.w(TAG, "Failed to add to item at (" + lp.cellX + "," + lp.cellY + ") to CellLayout");
        }

        if (!(child instanceof Folder)) {
            child.setHapticFeedbackEnabled(false);
            child.setOnLongClickListener(mLongClickListener);
        }
        if (child instanceof DropTarget) {
            mDragController.addDropTarget((DropTarget) child);
        }
    
    }

    /**
     * Adds the specified child in the specified screen. The position and dimension of
     * the child are defined by x, y, spanX and spanY.
     *
     * @param child The child to add in one of the workspace's screens.
     * @param screen The screen in which to add the child.
     * @param x The X position of the child in the screen's grid.
     * @param y The Y position of the child in the screen's grid.
     * @param spanX The number of cells spanned horizontally by the child.
     * @param spanY The number of cells spanned vertically by the child.
     * @param insert When true, the child is inserted at the beginning of the children list.
     */
    void addInScreen(View child, long container, int screen, int x, int y, int spanX, int spanY,
            boolean insert) {
        //Log.d(TAG, "sxsexe------>addInScreen screen " + screen + " childCount " + getChildCount() + " container " + container);
        if (container == LauncherSettings.Favorites.CONTAINER_DESKTOP) {
            
            if(screen >= getNormalScreenCount()) {
                makesureAddScreenIndex(screen);
            }
            
            if (screen < 0 || screen >= getChildCount()) {
                Log.e(TAG, "The screen must be >= 0 and < " + getChildCount()
                    + " (was " + screen + "); skipping child");
                return;
            }
        }
        final CellLayout layout;
        if (container == LauncherSettings.Favorites.CONTAINER_HOTSEAT) {
            layout = mLauncher.getHotseat().getLayout();
            child.setOnKeyListener(null);

            // Hide folder title in the hotseat
            if (child instanceof FolderIcon) {
                ((FolderIcon) child).setTextVisible(false);
            }

            if (screen < 0) {
                screen = mLauncher.getHotseat().getOrderInHotseat(x, y);
            } else {
                // Note: We do this to ensure that the hotseat is always laid out in the orientation
                // of the hotseat in order regardless of which orientation they were added
                x = mLauncher.getHotseat().getCellXFromOrder(screen);
                y = mLauncher.getHotseat().getCellYFromOrder(screen);
            }
        } else if ( container == LauncherSettings.Favorites.CONTAINER_HIDESEAT ){
            mLauncher.getHideseat().addInScreen(child, container, screen, x, y, spanX, spanY, insert);
            return;
        }else {
            // Show folder title if not in the hotseat
            if (child instanceof FolderIcon) {
                ((FolderIcon) child).setTextVisible(true);
            }

            layout = (CellLayout) getChildAt(screen);
            //Log.d(TAG, "sxsexe--->addInScreen screen " + screen + " childCount " + getChildCount() + " layout " + layout);
            child.setOnKeyListener(new IconKeyEventListener());
        }

        LayoutParams genericLp = child.getLayoutParams();
        CellLayout.LayoutParams lp;
        if (genericLp == null || !(genericLp instanceof CellLayout.LayoutParams)) {
            lp = new CellLayout.LayoutParams(x, y, spanX, spanY);
        } else {
            lp = (CellLayout.LayoutParams) genericLp;
            lp.cellX = x;
            lp.cellY = y;
            lp.cellHSpan = spanX;
            lp.cellVSpan = spanY;
        }

        if (spanX < 0 && spanY < 0) {
            lp.isLockedToGrid = false;
        }

        // Get the canonical child id to uniquely represent this view in this screen
        int childId = LauncherModel.getCellLayoutChildId(container, screen, x, y, spanX, spanY);
        boolean markCellsAsOccupied = !(child instanceof Folder);
        if (layout != null && !layout.addViewToCellLayout(child, insert ? 0 : -1, childId, lp, markCellsAsOccupied)) {
            // TODO: This branch occurs when the workspace is adding views
            // outside of the defined grid
            // maybe we should be deleting these items from the LauncherModel?
            Log.w(TAG, "Failed to add to item at (" + lp.cellX + "," + lp.cellY + ") to CellLayout");
        }

        if (container == Favorites.CONTAINER_HIDESEAT) {
            child.setHapticFeedbackEnabled(false);
            child.setOnLongClickListener(mLauncher.getHideseat());
        } else if (!(child instanceof Folder)) {
            child.setHapticFeedbackEnabled(false);
            child.setOnLongClickListener(mLongClickListener);
        }
        if (child instanceof DropTarget) {
            mDragController.addDropTarget((DropTarget) child);
        }
        /*if (child instanceof GadgetView) {
            mLauncher.addGadgetView((GadgetView)child);
        }*/

    }

    /**
     * Check if the point (x, y) hits a given page.
     */
    private boolean hitsPage(int index, float x, float y) {
        final View page = getChildAt(index);
        if (page != null) {
            float[] localXY = { x, y };
            mapPointFromSelfToChild(page, localXY);
            return (localXY[0] >= 0 && localXY[0] < page.getWidth()
                    && localXY[1] >= 0 && localXY[1] < page.getHeight());
        }
        return false;
    }

    @Override
    protected boolean hitsPreviousPage(float x, float y) {
        // mNextPage is set to INVALID_PAGE whenever we are stationary.
        // Calculating "next page" this way ensures that you scroll to whatever page you tap on
        final int current = (mNextPage == INVALID_PAGE) ? mCurrentPage : mNextPage;

        // Only allow tap to next page on large devices, where there's significant margin outside
        // the active workspace
        return LauncherApplication.isScreenLarge() && hitsPage(current - 1, x, y);
    }

    @Override
    protected boolean hitsNextPage(float x, float y) {
        // mNextPage is set to INVALID_PAGE whenever we are stationary.
        // Calculating "next page" this way ensures that you scroll to whatever page you tap on
        final int current = (mNextPage == INVALID_PAGE) ? mCurrentPage : mNextPage;

        // Only allow tap to next page on large devices, where there's significant margin outside
        // the active workspace
        return LauncherApplication.isScreenLarge() && hitsPage(current + 1, x, y);
    }

    /**
     * Called directly from a CellLayout (not by the framework), after we've been added as a
     * listener via setOnInterceptTouchEventListener(). This allows us to tell the CellLayout
     * that it should intercept touch events, which is not something that is normally supported.
     */
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return (isSmall() || !isFinishedSwitchingState());
    }

    public boolean isSwitchingState() {
        return mIsSwitchingState;
    }

    /** This differs from isSwitchingState in that we take into account how far the transition
     *  has completed. */
    public boolean isFinishedSwitchingState() {
        return !mIsSwitchingState || (mTransitionProgress > 0.5f);
    }

    protected void onWindowVisibilityChanged (int visibility) {
        mLauncher.onWindowVisibilityChanged(visibility);
    }

    @Override
    public boolean dispatchUnhandledMove(View focused, int direction) {
        if (isSmall() || !isFinishedSwitchingState()) {
            // when the home screens are shrunken, shouldn't allow side-scrolling
            return false;
        }
        return super.dispatchUnhandledMove(focused, direction);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        switch (ev.getAction() & MotionEvent.ACTION_MASK) {
        case MotionEvent.ACTION_DOWN:
            mXDown = ev.getX();
            mYDown = ev.getY();
            mAnimateScrollEffectMode = false;
            break;
        case MotionEvent.ACTION_POINTER_UP:
        case MotionEvent.ACTION_UP:
            if (mTouchState == TOUCH_STATE_REST) {
                final CellLayout currentPage = (CellLayout) getChildAt(mCurrentPage);
                if (currentPage != null && !currentPage.lastDownOnOccupiedCell()) {
                    onWallpaperTap(ev);
                }
            }
        }
        return super.onInterceptTouchEvent(ev);
    }

    protected void reinflateWidgetsIfNecessary() {
        final int clCount = getChildCount();
        for (int i = 0; i < clCount; i++) {
            CellLayout cl = (CellLayout) getChildAt(i);
            ShortcutAndWidgetContainer swc = cl.getShortcutsAndWidgets();
            final int itemCount = swc.getChildCount();
            for (int j = 0; j < itemCount; j++) {
                View v = swc.getChildAt(j);

                if ((v != null) && (v.getTag() instanceof LauncherAppWidgetInfo)) {
                    LauncherAppWidgetInfo info = (LauncherAppWidgetInfo) v.getTag();
                    LauncherAppWidgetHostView lahv = (LauncherAppWidgetHostView) info.hostView;
                    if (lahv != null && lahv.orientationChangedSincedInflation()) {
                        mLauncher.removeAppWidget(info);
                        // Remove the current widget which is inflated with the wrong orientation
                        cl.removeView(lahv);
                        mLauncher.bindAppWidget(info);
                    }
                }
            }
        }
    }

    @Override
    protected void determineScrollingStart(MotionEvent ev) {
        if (isSmall()) return;
        if (!isFinishedSwitchingState()) return;

        float deltaX = Math.abs(ev.getX() - mXDown);
        float deltaY = Math.abs(ev.getY() - mYDown);

        if (Float.compare(deltaX, 0f) == 0) return;

        float slope = deltaY / deltaX;
        float theta = (float) Math.atan(slope);

        if (deltaX > mTouchSlop || deltaY > mTouchSlop) {
            cancelCurrentPageLongPress();
        }

        if (theta > MAX_SWIPE_ANGLE) {
            // Above MAX_SWIPE_ANGLE, we don't want to ever start scrolling the workspace
            return;
        } else if (theta > START_DAMPING_TOUCH_SLOP_ANGLE) {
            // Above START_DAMPING_TOUCH_SLOP_ANGLE and below MAX_SWIPE_ANGLE, we want to
            // increase the touch slop to make it harder to begin scrolling the workspace. This
            // results in vertically scrolling widgets to more easily. The higher the angle, the
            // more we increase touch slop.
            theta -= START_DAMPING_TOUCH_SLOP_ANGLE;
            float extraRatio = (float)
                    Math.sqrt((theta / (MAX_SWIPE_ANGLE - START_DAMPING_TOUCH_SLOP_ANGLE)));
            super.determineScrollingStart(ev, 1 + TOUCH_SLOP_DAMPING_FACTOR * extraRatio);
        } else {
            // Below START_DAMPING_TOUCH_SLOP_ANGLE, we don't do anything special
            super.determineScrollingStart(ev);
        }
    }

    public boolean isPageMoving() {
        return super.isPageMoving();
    }

    protected void onPageBeginMoving() {
        super.onPageBeginMoving();
        //Object viewRootImpl = ACA.View.getViewRootImpl(this);
        //if(viewRootImpl != null) {
       //     ACA.ViewRootImpl.cancelInvalidate(viewRootImpl, getChildAt(mCurrentPage));
       //     ACA.ViewRootImpl.cancelInvalidate(viewRootImpl, mLauncher.getHotseat());
       // }
        // clear folder close animation when page begin moving
        mLauncher.mFolderUtils.clearAnimation();

        if (isHardwareAccelerated()) {
            updateChildrenLayersEnabled(false);
        } else {
            if (mNextPage != INVALID_PAGE) {
                // we're snapping to a particular screen
                enableChildrenCache(mCurrentPage, mNextPage);
            } else {
                // this is when user is actively dragging a particular screen, they might
                // swipe it either left or right (but we won't advance by more than one screen)
                enableChildrenCache(mCurrentPage - 1, mCurrentPage + 1);
            }
        }

        // Only show page outlines as we pan if we are on large screen
        if (LauncherApplication.isScreenLarge()) {
            showOutlines();
            mIsStaticWallpaper = mWallpaperManager.getWallpaperInfo() == null;
        }

        // If we are not fading in adjacent screens, we still need to restore the alpha in case the
        // user scrolls while we are transitioning (should not affect dispatchDraw optimizations)
        if (!mWorkspaceFadeInAdjacentScreens) {
            for (int i = 0; i < getChildCount(); ++i) {
                ((CellLayout) getPageAt(i)).setShortcutAndWidgetAlpha(1f);
            }
        }

        // Show the scroll indicator as you pan the page
        showScrollingIndicator(false);
        //((CellLayout) getChildAt(mCurrentPage)).cancelFlingDropDownAnimation();
        
        if (CheckVoiceCommandPressHelper.PUSH_TO_TALK_SUPPORT
                && CheckVoiceCommandPressHelper.getInstance().isVoiceUIShown()) {
            CheckVoiceCommandPressHelper.getInstance().forceDismissVoiceCommand();
        }
        mLauncher.onWorkspacePageBeginMoving();
        if (mLauncher.mWidgetPageManager != null) {
            mLauncher.mWidgetPageManager.onPageBeginMoving();
        }
    }

    protected void onPageEndMoving() {
        super.onPageEndMoving();

        //open all children view HardwareAccelerated
        /*
        if (isHardwareAccelerated()) {
            updateChildrenLayersEnabled(false);
        } else {
            clearChildrenCache();
        }
        */

        if (mDragController.isDragging()) {
            if (isSmall()) {
                // If we are in springloaded mode, then force an event to check if the current touch
                // is under a new page (to scroll to)
                mDragController.forceTouchMove();
            }
        } else {
            // If we are not mid-dragging, hide the page outlines if we are on a large screen
            if (LauncherApplication.isScreenLarge()) {
                hideOutlines();
            }

            // Hide the scroll indicator as you pan the page
            if (!mDragController.isDragging()) {
                hideScrollingIndicator(false);
            }
        }
        mOverScrollMaxBackgroundAlpha = 0.0f;

        if (mDelayedResizeRunnable != null) {
            mDelayedResizeRunnable.run();
            mDelayedResizeRunnable = null;
        }

        if (mDelayedSnapToPageRunnable != null) {
            mDelayedSnapToPageRunnable.run();
            mDelayedSnapToPageRunnable = null;
        }
        if(!mDragController.isDragging()) {
            checkAndRemoveEmptyCell();
        }
        CellLayout cellLayout = (CellLayout) getChildAt(mCurrentPage);
        if(cellLayout != null) {
            cellLayout.startFlingDropDownAnimation();
            cellLayout.postInvalidateDelayed(1000);
        }
        Hotseat hotseat = mLauncher.getHotseat();
        if (hotseat != null) {
            hotseat.postInvalidateDelayed(1000);
        }
        mLauncher.onWorkspacePageEndMoving();
        if (mAnimateScrollEffectMode)
            animateScrollEffect(false);
    }

    @Override
    protected void notifyPageSwitchListener() {
        super.notifyPageSwitchListener();
        Launcher.setScreen(mCurrentPage);
    };

    // As a ratio of screen height, the total distance we want the parallax effect to span
    // horizontally
    private float wallpaperTravelToScreenWidthRatio(int width, int height) {
        float aspectRatio = width / (float) height;

        // At an aspect ratio of 16/10, the wallpaper parallax effect should span 1.5 * screen width
        // At an aspect ratio of 10/16, the wallpaper parallax effect should span 1.2 * screen width
        // We will use these two data points to extrapolate how much the wallpaper parallax effect
        // to span (ie travel) at any aspect ratio:

        final float ASPECT_RATIO_LANDSCAPE = 16/10f;
        final float ASPECT_RATIO_PORTRAIT = 10/16f;
        final float WALLPAPER_WIDTH_TO_SCREEN_RATIO_LANDSCAPE = 1.5f;
        final float WALLPAPER_WIDTH_TO_SCREEN_RATIO_PORTRAIT = 1.2f;

        // To find out the desired width at different aspect ratios, we use the following two
        // formulas, where the coefficient on x is the aspect ratio (width/height):
        //   (16/10)x + y = 1.5
        //   (10/16)x + y = 1.2
        // We solve for x and y and end up with a final formula:
        final float x =
            (WALLPAPER_WIDTH_TO_SCREEN_RATIO_LANDSCAPE - WALLPAPER_WIDTH_TO_SCREEN_RATIO_PORTRAIT) /
            (ASPECT_RATIO_LANDSCAPE - ASPECT_RATIO_PORTRAIT);
        final float y = WALLPAPER_WIDTH_TO_SCREEN_RATIO_PORTRAIT - x * ASPECT_RATIO_PORTRAIT;
        return x * aspectRatio + y;
    }

    // The range of scroll values for Workspace
    private int getScrollRange() {
        return getChildOffset(getChildCount() - 1) - getChildOffset(0);
    }

    protected void setWallpaperDimension() {
        /*
        Point minDims = new Point();
        Point maxDims = new Point();
        mLauncher.getWindowManager().getDefaultDisplay().getCurrentSizeRange(minDims, maxDims);

        final int maxDim = Math.max(maxDims.x, maxDims.y);
        final int minDim = Math.min(minDims.x, minDims.y);

        // We need to ensure that there is enough extra space in the wallpaper for the intended
        // parallax effects
        if (LauncherApplication.isScreenLarge()) {
            mWallpaperWidth = (int) (maxDim * wallpaperTravelToScreenWidthRatio(maxDim, minDim));
            mWallpaperHeight = maxDim;
        } else {
            mWallpaperWidth = Math.max((int) (minDim * WALLPAPER_SCREENS_SPAN), maxDim);
            mWallpaperHeight = maxDim;
        }
        */
        DisplayMetrics dm = getResources().getDisplayMetrics();
        mWallpaperWidth =  (dm.widthPixels > dm.heightPixels) ? dm.heightPixels : dm.widthPixels;
        mWallpaperHeight = (dm.widthPixels < dm.heightPixels) ? dm.heightPixels : dm.widthPixels;
        new Thread("setWallpaperDimension") {
            public void run() {
                mWallpaperManager.suggestDesiredDimensions(mWallpaperWidth, mWallpaperHeight);
            }
        }.start();
    }

    private float wallpaperOffsetForCurrentScroll() {
        // Set wallpaper offset steps (1 / (number of screens - 1))
        mWallpaperManager.setWallpaperOffsetSteps(1.0f / (getChildCount() - 1), 1.0f);

        // For the purposes of computing the scrollRange and overScrollOffset, we assume
        // that mLayoutScale is 1. This means that when we're in spring-loaded mode,
        // there's no discrepancy between the wallpaper offset for a given page.
        float layoutScale = mLayoutScale;
        mLayoutScale = 1f;
        int scrollRange = getScrollRange();

        // Again, we adjust the wallpaper offset to be consistent between values of mLayoutScale
        float adjustedScrollX = Math.max(0, Math.min(getScrollX(), mMaxScrollX));
        adjustedScrollX *= mWallpaperScrollRatio;
        mLayoutScale = layoutScale;

        float scrollProgress =
            adjustedScrollX / (float) scrollRange;

        if (LauncherApplication.isScreenLarge() && mIsStaticWallpaper) {
            // The wallpaper travel width is how far, from left to right, the wallpaper will move
            // at this orientation. On tablets in portrait mode we don't move all the way to the
            // edges of the wallpaper, or otherwise the parallax effect would be too strong.
            int wallpaperTravelWidth = Math.min(mWallpaperTravelWidth, mWallpaperWidth);

            float offsetInDips = wallpaperTravelWidth * scrollProgress +
                (mWallpaperWidth - wallpaperTravelWidth) / 2; // center it
            float offset = offsetInDips / (float) mWallpaperWidth;
            return offset;
        } else {
            return scrollProgress;
        }
    }

    private void syncWallpaperOffsetWithScroll() {
        final boolean enableWallpaperEffects = isHardwareAccelerated();
        if (enableWallpaperEffects) {
            mWallpaperOffset.setFinalX(wallpaperOffsetForCurrentScroll());
        }
    }

    public void animateScrollEffect(boolean bStartAnim) {
        mAnimateScrollEffectMode = bStartAnim;
        long animationDelay = bStartAnim ? 100L : 50L;
        mLauncher.getHandler().removeCallbacks(mScrollEffectAnimator);
        mLauncher.getHandler().postDelayed(mScrollEffectAnimator, animationDelay);
    }

    public void updateWallpaperOffsetImmediately() {
        mUpdateWallpaperOffsetImmediately = true;
    }

    private void updateWallpaperOffsets() {
        boolean updateNow = false;
        boolean keepUpdating = true;
        if (mUpdateWallpaperOffsetImmediately) {
            updateNow = true;
            keepUpdating = false;
            mWallpaperOffset.jumpToFinal();
            mUpdateWallpaperOffsetImmediately = false;
        } else {
            updateNow = keepUpdating = mWallpaperOffset.computeScrollOffset();
        }
        if (updateNow) {
            if (mWindowToken != null) {
                mWallpaperManager.setWallpaperOffsets(mWindowToken,
                        mWallpaperOffset.getCurrX(), mWallpaperOffset.getCurrY());
            }
        }
        if (keepUpdating) {
            invalidate();
        }
    }

    @Override
    protected void updateCurrentPageScroll() {
        super.updateCurrentPageScroll();
        computeWallpaperScrollRatio(mCurrentPage);
    }

    @Override
    protected void snapToPage(int whichPage) {
        super.snapToPage(whichPage);
        computeWallpaperScrollRatio(whichPage);
    }

    @Override
    protected void snapToPage(int whichPage, int duration) {
        super.snapToPage(whichPage, duration);
        computeWallpaperScrollRatio(whichPage);
    }

    protected void snapToPage(int whichPage, Runnable r) {
        if (mDelayedSnapToPageRunnable != null) {
            mDelayedSnapToPageRunnable.run();
        }
        mDelayedSnapToPageRunnable = r;
        snapToPage(whichPage, SLOW_PAGE_SNAP_ANIMATION_DURATION);
    }

    private void computeWallpaperScrollRatio(int page) {
        // Here, we determine what the desired scroll would be with and without a layout scale,
        // and compute a ratio between the two. This allows us to adjust the wallpaper offset
        // as though there is no layout scale.
        float layoutScale = mLayoutScale;
        int scaled = getChildOffset(page) - getRelativeChildOffset(page);
        mLayoutScale = 1.0f;
        float unscaled = getChildOffset(page) - getRelativeChildOffset(page);
        mLayoutScale = layoutScale;
        if (scaled > 0) {
            mWallpaperScrollRatio = (1.0f * unscaled) / scaled;
        } else {
            mWallpaperScrollRatio = 1f;
        }
    }

    class WallpaperOffsetInterpolator {
        float mFinalHorizontalWallpaperOffset = 0.0f;
        float mFinalVerticalWallpaperOffset = 0.5f;
        float mHorizontalWallpaperOffset = 0.0f;
        float mVerticalWallpaperOffset = 0.5f;
        long mLastWallpaperOffsetUpdateTime;
        boolean mIsMovingFast;
        boolean mOverrideHorizontalCatchupConstant;
        float mHorizontalCatchupConstant = 0.35f;
        float mVerticalCatchupConstant = 0.35f;

        public WallpaperOffsetInterpolator() {
        }

        public void setOverrideHorizontalCatchupConstant(boolean override) {
            mOverrideHorizontalCatchupConstant = override;
        }

        public void setHorizontalCatchupConstant(float f) {
            mHorizontalCatchupConstant = f;
        }

        public void setVerticalCatchupConstant(float f) {
            mVerticalCatchupConstant = f;
        }

        public boolean computeScrollOffset() {
            if (Float.compare(mHorizontalWallpaperOffset, mFinalHorizontalWallpaperOffset) == 0 &&
                    Float.compare(mVerticalWallpaperOffset, mFinalVerticalWallpaperOffset) == 0) {
                mIsMovingFast = false;
                return false;
            }
            boolean isLandscape = mDisplaySize.x > mDisplaySize.y;

            long currentTime = System.currentTimeMillis();
            long timeSinceLastUpdate = currentTime - mLastWallpaperOffsetUpdateTime;
            timeSinceLastUpdate = Math.min((long) (1000/30f), timeSinceLastUpdate);
            timeSinceLastUpdate = Math.max(1L, timeSinceLastUpdate);

            float xdiff = Math.abs(mFinalHorizontalWallpaperOffset - mHorizontalWallpaperOffset);
            if (!mIsMovingFast && xdiff > 0.07) {
                mIsMovingFast = true;
            }

            float fractionToCatchUpIn1MsHorizontal;
            if (mOverrideHorizontalCatchupConstant) {
                fractionToCatchUpIn1MsHorizontal = mHorizontalCatchupConstant;
            } else if (mIsMovingFast) {
                fractionToCatchUpIn1MsHorizontal = isLandscape ? 0.5f : 0.75f;
            } else {
                // slow
                fractionToCatchUpIn1MsHorizontal = isLandscape ? 0.27f : 0.5f;
            }
            float fractionToCatchUpIn1MsVertical = mVerticalCatchupConstant;

            fractionToCatchUpIn1MsHorizontal /= 33f;
            fractionToCatchUpIn1MsVertical /= 33f;

            final float UPDATE_THRESHOLD = 0.00001f;
            float hOffsetDelta = mFinalHorizontalWallpaperOffset - mHorizontalWallpaperOffset;
            float vOffsetDelta = mFinalVerticalWallpaperOffset - mVerticalWallpaperOffset;
            boolean jumpToFinalValue = Math.abs(hOffsetDelta) < UPDATE_THRESHOLD &&
                Math.abs(vOffsetDelta) < UPDATE_THRESHOLD;

            // Don't have any lag between workspace and wallpaper on non-large devices
            if (!LauncherApplication.isScreenLarge() || jumpToFinalValue) {
                mHorizontalWallpaperOffset = mFinalHorizontalWallpaperOffset;
                mVerticalWallpaperOffset = mFinalVerticalWallpaperOffset;
            } else {
                float percentToCatchUpVertical =
                    Math.min(1.0f, timeSinceLastUpdate * fractionToCatchUpIn1MsVertical);
                float percentToCatchUpHorizontal =
                    Math.min(1.0f, timeSinceLastUpdate * fractionToCatchUpIn1MsHorizontal);
                mHorizontalWallpaperOffset += percentToCatchUpHorizontal * hOffsetDelta;
                mVerticalWallpaperOffset += percentToCatchUpVertical * vOffsetDelta;
            }

            mLastWallpaperOffsetUpdateTime = System.currentTimeMillis();
            return true;
        }

        public float getCurrX() {
            return mHorizontalWallpaperOffset;
        }

        public float getFinalX() {
            return mFinalHorizontalWallpaperOffset;
        }

        public float getCurrY() {
            return mVerticalWallpaperOffset;
        }

        public float getFinalY() {
            return mFinalVerticalWallpaperOffset;
        }

        public void setFinalX(float x) {
            mFinalHorizontalWallpaperOffset = Math.max(0f, Math.min(x, 1.0f));
        }

        public void setFinalY(float y) {
            mFinalVerticalWallpaperOffset = Math.max(0f, Math.min(y, 1.0f));
        }

        public void jumpToFinal() {
            mHorizontalWallpaperOffset = mFinalHorizontalWallpaperOffset;
            mVerticalWallpaperOffset = mFinalVerticalWallpaperOffset;
        }
    }

    @Override
    public void computeScroll() {
        super.computeScroll();
        syncWallpaperOffsetWithScroll();
    }

    void showOutlines() {
        if (!isSmall() && !mIsSwitchingState) {
            if (mChildrenOutlineFadeOutAnimation != null) mChildrenOutlineFadeOutAnimation.cancel();
            if (mChildrenOutlineFadeInAnimation != null) mChildrenOutlineFadeInAnimation.cancel();
            mChildrenOutlineFadeInAnimation = LauncherAnimUtils.ofFloat(this, "childrenOutlineAlpha", 1.0f);
            mChildrenOutlineFadeInAnimation.setDuration(CHILDREN_OUTLINE_FADE_IN_DURATION);
            mChildrenOutlineFadeInAnimation.start();
        }
    }

    void hideOutlines() {
        if (!isSmall() && !mIsSwitchingState) {
            if (mChildrenOutlineFadeInAnimation != null) mChildrenOutlineFadeInAnimation.cancel();
            if (mChildrenOutlineFadeOutAnimation != null) mChildrenOutlineFadeOutAnimation.cancel();
            mChildrenOutlineFadeOutAnimation = LauncherAnimUtils.ofFloat(this, "childrenOutlineAlpha", 0.0f);
            mChildrenOutlineFadeOutAnimation.setDuration(CHILDREN_OUTLINE_FADE_OUT_DURATION);
            mChildrenOutlineFadeOutAnimation.setStartDelay(CHILDREN_OUTLINE_FADE_OUT_DELAY);
            mChildrenOutlineFadeOutAnimation.start();
        }
    }

    public void showOutlinesTemporarily() {
        if (!mIsPageMoving && !isTouchActive()) {
            snapToPage(mCurrentPage);
        }
    }

    public void setChildrenOutlineAlpha(float alpha) {
        mChildrenOutlineAlpha = alpha;
        for (int i = 0; i < getChildCount(); i++) {
            CellLayout cl = (CellLayout) getChildAt(i);
            cl.setBackgroundAlpha(alpha);
        }
    }

    public float getChildrenOutlineAlpha() {
        return mChildrenOutlineAlpha;
    }

    void disableBackground() {
        mDrawBackground = false;
    }
    void enableBackground() {
        mDrawBackground = true;
    }

    private void animateBackgroundGradient(float finalAlpha, boolean animated) {
        if (mBackground == null) return;
        // if (mBackgroundFadeInAnimation != null) {
        // mBackgroundFadeInAnimation.cancel();
        // mBackgroundFadeInAnimation = null;
        // }
        if (mBackgroundFadeOutAnimation != null) {
            mBackgroundFadeOutAnimation.cancel();
            mBackgroundFadeOutAnimation = null;
        }
        float startAlpha = getBackgroundAlpha();
        if (finalAlpha != startAlpha) {
            if (animated) {
                mBackgroundFadeOutAnimation =
                        LauncherAnimUtils.ofFloat(this, startAlpha, finalAlpha);
                mBackgroundFadeOutAnimation.addUpdateListener(new AnimatorUpdateListener() {
                    public void onAnimationUpdate(ValueAnimator animation) {
                        setBackgroundAlpha(((Float) animation.getAnimatedValue()).floatValue());
                    }
                });
                mBackgroundFadeOutAnimation.setInterpolator(new DecelerateInterpolator(1.5f));
                mBackgroundFadeOutAnimation.setDuration(BACKGROUND_FADE_OUT_DURATION);
                mBackgroundFadeOutAnimation.start();
            } else {
                setBackgroundAlpha(finalAlpha);
            }
        }
    }

    public void setBackgroundAlpha(float alpha) {
        if (alpha != mBackgroundAlpha) {
            mBackgroundAlpha = alpha;
            invalidate();
        }
    }

    public float getBackgroundAlpha() {
        return mBackgroundAlpha;
    }

    float backgroundAlphaInterpolator(float r) {
        float pivotA = 0.1f;
        float pivotB = 0.4f;
        if (r < pivotA) {
            return 0;
        } else if (r > pivotB) {
            return 1.0f;
        } else {
            return (r - pivotA)/(pivotB - pivotA);
        }
    }

    private void updatePageAlphaValues(int screenCenter) {
        boolean isInOverscroll = mOverScrollX < 0 || mOverScrollX > mMaxScrollX;
        if (mWorkspaceFadeInAdjacentScreens &&
                mState == State.NORMAL &&
                !mIsSwitchingState &&
                !isInOverscroll) {
            for (int i = 0; i < getChildCount(); i++) {
                CellLayout child = (CellLayout) getChildAt(i);
                if (child != null) {
                    float scrollProgress = getScrollProgress(screenCenter, child, i);
                    float alpha = 1 - Math.abs(scrollProgress);
                    child.getShortcutsAndWidgets().setAlpha(alpha);
                    if (!mIsDragOccuring) {
                        child.setBackgroundAlphaMultiplier(
                                backgroundAlphaInterpolator(Math.abs(scrollProgress)));
                    } else {
                        child.setBackgroundAlphaMultiplier(1f);
                    }
                }
            }
        }
    }

    private void setChildrenBackgroundAlphaMultipliers(float a) {
        for (int i = 0; i < getChildCount(); i++) {
            CellLayout child = (CellLayout) getChildAt(i);
            child.setBackgroundAlphaMultiplier(a);
        }
    }

    @Override
    protected void screenScrolled(int screenCenter) {
        final boolean isRtl = isLayoutRtl();
        super.screenScrolled(screenCenter);

        updatePageAlphaValues(screenCenter);
        //always open hardwareLayers 
        //enableHwLayersOnVisiblePages();

        if (mOverScrollX < 0 || mOverScrollX > mMaxScrollX) {
            /*
            int index = 0;
            float pivotX = 0f;
            final float leftBiasedPivot = 0.25f;
            final float rightBiasedPivot = 0.75f;
            final int lowerIndex = 0;
            final int upperIndex = getChildCount() - 1;
            if (isRtl) {
                index = mOverScrollX < 0 ? upperIndex : lowerIndex;
                pivotX = (index == 0 ? leftBiasedPivot : rightBiasedPivot);
            } else {
                index = mOverScrollX < 0 ? lowerIndex : upperIndex;
                pivotX = (index == 0 ? rightBiasedPivot : leftBiasedPivot);
            }

            CellLayout cl = (CellLayout) getChildAt(index);
            float scrollProgress = getScrollProgress(screenCenter, cl, index);
            final boolean isLeftPage = (isRtl ? index > 0 : index == 0);
            cl.setOverScrollAmount(Math.abs(scrollProgress), isLeftPage);
            float rotation = -WORKSPACE_OVERSCROLL_ROTATION * scrollProgress;
            cl.setRotationY(rotation);
            setFadeForOverScroll(Math.abs(scrollProgress));
            if (!mOverscrollTransformsSet) {
                mOverscrollTransformsSet = true;
                cl.setCameraDistance(mDensity * mCameraDistance);
                cl.setPivotX(cl.getMeasuredWidth() * pivotX);
                cl.setPivotY(cl.getMeasuredHeight() * 0.5f);
                cl.setOverscrollTransformsDirty(true);
            }
        */
            } else {
            if (mOverscrollFade != 0) {
                setFadeForOverScroll(0);
            }
            if (mOverscrollTransformsSet) {
                mOverscrollTransformsSet = false;
                ((CellLayout) getChildAt(0)).resetOverscrollTransforms();
                ((CellLayout) getChildAt(getChildCount() - 1)).resetOverscrollTransforms();
            }
        }
    }

    @Override
    protected void overScroll(float amount) {
        //acceleratedOverScroll(amount);
        dampedOverScroll(amount);
    }

    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        mWindowToken = getWindowToken();
        computeScroll();
        mDragController.setWindowToken(mWindowToken);
    }

    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mWindowToken = null;
    }
    
    protected void cancelFlingDropDownAnimation() {
        CellLayout layout = null;
        int count = 0;
        for (int i = 0, N = getChildCount(); i < N; i++) {
            layout = (CellLayout) getChildAt(i);
            count = layout.cancelFlingDropDownAnimation();
            if (count > 0) {
                layout.postInvalidate();
            }
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        if (mFirstLayout && mCurrentPage >= 0 && mCurrentPage < getChildCount()) {
            mUpdateWallpaperOffsetImmediately = true;
        }
        super.onLayout(changed, left, top, right, bottom);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        updateWallpaperOffsets();

        // Draw the background gradient if necessary
        if (mBackground != null && mBackgroundAlpha > 0.0f && mDrawBackground) {
            int alpha = (int) (mBackgroundAlpha * 255);
            mBackground.setAlpha(alpha);
            mBackground.setBounds(getScrollX(), 0, getScrollX() + getMeasuredWidth(),
                    getMeasuredHeight());
            mBackground.draw(canvas);
        }

        super.onDraw(canvas);

        // Call back to LauncherModel to finish binding after the first draw
        post(mBindPages);
    }

    boolean isDrawingBackgroundGradient() {
        return (mBackground != null && mBackgroundAlpha > 0.0f && mDrawBackground);
    }

    @Override
    protected boolean onRequestFocusInDescendants(int direction, Rect previouslyFocusedRect) {
        if (!mLauncher.isAllAppsVisible()) {
            final Folder openFolder = getOpenFolder();
            if (openFolder != null) {
                return openFolder.requestFocus(direction, previouslyFocusedRect);
            } else {
                return super.onRequestFocusInDescendants(direction, previouslyFocusedRect);
            }
        }
        return false;
    }

    @Override
    public int getDescendantFocusability() {
        if (isSmall()) {
            return ViewGroup.FOCUS_BLOCK_DESCENDANTS;
        }
        return super.getDescendantFocusability();
    }

    @Override
    public void addFocusables(ArrayList<View> views, int direction, int focusableMode) {
        if (!mLauncher.isAllAppsVisible()) {
            final Folder openFolder = getOpenFolder();
            if (openFolder != null) {
                openFolder.addFocusables(views, direction);
            } else {
                super.addFocusables(views, direction, focusableMode);
            }
        }
    }

    public boolean isSmall() {
        return mState == State.SMALL || mState == State.SPRING_LOADED;
    }

    void enableChildrenCache(int fromPage, int toPage) {
        if (fromPage > toPage) {
            final int temp = fromPage;
            fromPage = toPage;
            toPage = temp;
        }

        final int screenCount = getChildCount();

        fromPage = Math.max(fromPage, 0);
        toPage = Math.min(toPage, screenCount - 1);

        for (int i = fromPage; i <= toPage; i++) {
            final CellLayout layout = (CellLayout) getChildAt(i);
            layout.setChildrenDrawnWithCacheEnabled(true);
            layout.setChildrenDrawingCacheEnabled(true);
        }
    }

    void clearChildrenCache() {
        final int screenCount = getChildCount();
        for (int i = 0; i < screenCount; i++) {
            final CellLayout layout = (CellLayout) getChildAt(i);
            layout.setChildrenDrawnWithCacheEnabled(false);
            // In software mode, we don't want the items to continue to be drawn into bitmaps
            if (!isHardwareAccelerated()) {
                layout.setChildrenDrawingCacheEnabled(false);
            }
        }
    }


    private void updateChildrenLayersEnabled(boolean force) {
        /*
        boolean small = mState == State.SMALL || mIsSwitchingState;
        boolean enableChildrenLayers = force || small || mAnimatingViewIntoPlace || isPageMoving();

        if (enableChildrenLayers != mChildrenLayersEnabled) {
            mChildrenLayersEnabled = enableChildrenLayers;
            if (mChildrenLayersEnabled) {
                enableHwLayersOnVisiblePages();
            } else {
                for (int i = 0; i < getPageCount(); i++) {
                    final CellLayout cl = (CellLayout) getChildAt(i);
                    cl.disableHardwareLayers();
                }
            }
        }
        */
    }

    private void enableHwLayersOnVisiblePages() {
        if (mChildrenLayersEnabled) {
            final int screenCount = getChildCount();
            getVisiblePages(mTempVisiblePagesRange);
            int leftScreen = mTempVisiblePagesRange[0];
            int rightScreen = mTempVisiblePagesRange[1];
            if (leftScreen == rightScreen) {
                // make sure we're caching at least two pages always
                if (rightScreen < screenCount - 1) {
                    rightScreen++;
                } else if (leftScreen > 0) {
                    leftScreen--;
                }
            }
            for (int i = 0; i < screenCount; i++) {
                final CellLayout layout = (CellLayout) getPageAt(i);
                if (!(leftScreen <= i && i <= rightScreen && shouldDrawChild(layout))) {
                    layout.disableHardwareLayers();
                }
            }
            for (int i = 0; i < screenCount; i++) {
                final CellLayout layout = (CellLayout) getPageAt(i);
                if (leftScreen <= i && i <= rightScreen && shouldDrawChild(layout)) {
                    layout.enableHardwareLayers();
                }
            }
        }
    }

    public void buildPageHardwareLayers() {
        // force layers to be enabled just for the call to buildLayer
        updateChildrenLayersEnabled(true);
        if (getWindowToken() != null) {
            final int childCount = getChildCount();
            for (int i = 0; i < childCount; i++) {
                CellLayout cl = (CellLayout) getChildAt(i);
                cl.buildHardwareLayer();
            }
        }
        updateChildrenLayersEnabled(false);
    }

    protected void onWallpaperTap(MotionEvent ev) {
        final int[] position = mTempCell;
        getLocationOnScreen(position);

        int pointerIndex = ev.getActionIndex();
        position[0] += (int) ev.getX(pointerIndex);
        position[1] += (int) ev.getY(pointerIndex);

        mWallpaperManager.sendWallpaperCommand(getWindowToken(),
                ev.getAction() == MotionEvent.ACTION_UP
                        ? WallpaperManager.COMMAND_TAP : WallpaperManager.COMMAND_SECONDARY_TAP,
                position[0], position[1], 0, null);
    }

    /*
     * This interpolator emulates the rate at which the perceived scale of an object changes
     * as its distance from a camera increases. When this interpolator is applied to a scale
     * animation on a view, it evokes the sense that the object is shrinking due to moving away
     * from the camera.
     */
    static class ZInterpolator implements TimeInterpolator {
        private float focalLength;

        public ZInterpolator(float foc) {
            focalLength = foc;
        }

        public float getInterpolation(float input) {
            return (1.0f - focalLength / (focalLength + input)) /
                (1.0f - focalLength / (focalLength + 1.0f));
        }
    }

    /*
     * The exact reverse of ZInterpolator.
     */
    static class InverseZInterpolator implements TimeInterpolator {
        private ZInterpolator zInterpolator;
        public InverseZInterpolator(float foc) {
            zInterpolator = new ZInterpolator(foc);
        }
        public float getInterpolation(float input) {
            return 1 - zInterpolator.getInterpolation(1 - input);
        }
    }

    /*
     * ZInterpolator compounded with an ease-out.
     */
    static class ZoomOutInterpolator implements TimeInterpolator {
        private final DecelerateInterpolator decelerate = new DecelerateInterpolator(0.75f);
        private final ZInterpolator zInterpolator = new ZInterpolator(0.13f);

        public float getInterpolation(float input) {
            return decelerate.getInterpolation(zInterpolator.getInterpolation(input));
        }
    }

    /*
     * InvereZInterpolator compounded with an ease-out.
     */
    static class ZoomInInterpolator implements TimeInterpolator {
        private final InverseZInterpolator inverseZInterpolator = new InverseZInterpolator(0.35f);
        private final DecelerateInterpolator decelerate = new DecelerateInterpolator(3.0f);

        public float getInterpolation(float input) {
            return decelerate.getInterpolation(inverseZInterpolator.getInterpolation(input));
        }
    }

    private final ZoomInInterpolator mZoomInInterpolator = new ZoomInInterpolator();

    /*
    *
    * We call these methods (onDragStartedWithItemSpans/onDragStartedWithSize) whenever we
    * start a drag in Launcher, regardless of whether the drag has ever entered the Workspace
    *
    * These methods mark the appropriate pages as accepting drops (which alters their visual
    * appearance).
    *
    */
    public void onDragStartedWithItem(View v) {
        final Canvas canvas = new Canvas();

        // The outline is used to visualize where the item will land if dropped
        mDragOutline = createDragOutline(v, canvas, DRAG_BITMAP_PADDING);
    }

    public void onDragStartedWithItem(PendingAddItemInfo info, Bitmap b, boolean clipAlpha) {
        final Canvas canvas = new Canvas();

        int[] size = estimateItemSize(info.spanX, info.spanY, info, false);

        // The outline is used to visualize where the item will land if dropped
        mDragOutline = createDragOutline(b, canvas, DRAG_BITMAP_PADDING, size[0],
                size[1], clipAlpha);
    }

    public void exitWidgetResizeMode() {
        DragLayer dragLayer = mLauncher.getDragLayer();
        dragLayer.clearAllResizeFrames();
    }

    private void initAnimationArrays() {
        final int childCount = getChildCount();
        if (mOldTranslationXs != null && mOldTranslationXs.length == childCount) return;
        mOldTranslationXs = new float[childCount];
        mOldTranslationYs = new float[childCount];
        mOldScaleXs = new float[childCount];
        mOldScaleYs = new float[childCount];
        mOldBackgroundAlphas = new float[childCount];
        mOldAlphas = new float[childCount];
        mNewTranslationXs = new float[childCount];
        mNewTranslationYs = new float[childCount];
        mNewScaleXs = new float[childCount];
        mNewScaleYs = new float[childCount];
        mNewBackgroundAlphas = new float[childCount];
        mNewAlphas = new float[childCount];
        mNewRotationYs = new float[childCount];
    }

    Animator getChangeStateAnimation(final State state, boolean animated) {
        return getChangeStateAnimation(state, animated, 0);
    }

    Animator getChangeStateAnimation(final State state, boolean animated, int delay) {
        if (mState == state) {
            return null;
        }

        // Initialize animation arrays for the first time if necessary
        initAnimationArrays();

        AnimatorSet anim = animated ? LauncherAnimUtils.createAnimatorSet() : null;

        // Stop any scrolling, move to the current page right away
        setCurrentPage(getNextPage());

        final State oldState = mState;
        final boolean oldStateIsNormal = (oldState == State.NORMAL);
        final boolean oldStateIsSpringLoaded = (oldState == State.SPRING_LOADED);
        final boolean oldStateIsSmall = (oldState == State.SMALL);
        mState = state;
        final boolean stateIsNormal = (state == State.NORMAL);
        final boolean stateIsSpringLoaded = (state == State.SPRING_LOADED);
        final boolean stateIsSmall = (state == State.SMALL);
        float finalScaleFactor = 1.0f;
        float finalBackgroundAlpha = stateIsSpringLoaded ? 1.0f : 0f;
        float translationX = 0;
        float translationY = 0;
        boolean zoomIn = true;

        if (state != State.NORMAL) {
            finalScaleFactor = mSpringLoadedShrinkFactor - (stateIsSmall ? 0.8f : 0);
            setPageSpacing(mSpringLoadedPageSpacing);
            if (oldStateIsNormal && stateIsSmall) {
                zoomIn = false;
                setLayoutScale(finalScaleFactor);
                updateChildrenLayersEnabled(false);
            } else {
                finalBackgroundAlpha = 1.0f;
                setLayoutScale(finalScaleFactor);
            }
        } else {
            setPageSpacing(mOriginalPageSpacing);
            setLayoutScale(1.0f);
        }

        final int duration = zoomIn ?
                getResources().getInteger(R.integer.config_workspaceUnshrinkTime) :
                getResources().getInteger(R.integer.config_appsCustomizeWorkspaceShrinkTime);
                
        for (int i = 0; i < getChildCount(); i++) {
            final CellLayout cl = (CellLayout) getChildAt(i);
            float finalAlpha = (!mWorkspaceFadeInAdjacentScreens || stateIsSpringLoaded ||
                    (i == mCurrentPage)) ? 1f : 0f;
            float currentAlpha = cl.getShortcutsAndWidgets().getAlpha();
            float initialAlpha = currentAlpha;

            // Determine the pages alpha during the state transition
            if ((oldStateIsSmall && stateIsNormal) ||
                (oldStateIsNormal && stateIsSmall)) {
                // To/from workspace - only show the current page unless the transition is not
                //                     animated and the animation end callback below doesn't run;
                //                     or, if we're in spring-loaded mode
                if (i == mCurrentPage || !animated || oldStateIsSpringLoaded) {
                    finalAlpha = 1f;
                } else {
                    initialAlpha = 0f;
                    finalAlpha = 0f;
                }
            }

            mOldAlphas[i] = initialAlpha;
            mNewAlphas[i] = finalAlpha;
            if (animated) {
                mOldTranslationXs[i] = cl.getTranslationX();
                mOldTranslationYs[i] = cl.getTranslationY();
                mOldScaleXs[i] = cl.getScaleX();
                mOldScaleYs[i] = cl.getScaleY();
                mOldBackgroundAlphas[i] = cl.getBackgroundAlpha();

                mNewTranslationXs[i] = translationX;
                mNewTranslationYs[i] = translationY;
                mNewScaleXs[i] = finalScaleFactor;
                mNewScaleYs[i] = finalScaleFactor;
                mNewBackgroundAlphas[i] = finalBackgroundAlpha;
            } else {
                cl.setTranslationX(translationX);
                cl.setTranslationY(translationY);
                cl.setScaleX(finalScaleFactor);
                cl.setScaleY(finalScaleFactor);
                cl.setBackgroundAlpha(finalBackgroundAlpha);
                cl.setShortcutAndWidgetAlpha(finalAlpha);
            }
        }

        if (animated) {
            for (int index = 0; index < getChildCount(); index++) {
                final int i = index;
                final CellLayout cl = (CellLayout) getChildAt(i);
                float currentAlpha = cl.getShortcutsAndWidgets().getAlpha();
                if (mOldAlphas[i] == 0 && mNewAlphas[i] == 0) {
                    cl.setTranslationX(mNewTranslationXs[i]);
                    cl.setTranslationY(mNewTranslationYs[i]);
                    cl.setScaleX(mNewScaleXs[i]);
                    cl.setScaleY(mNewScaleYs[i]);
                    cl.setBackgroundAlpha(mNewBackgroundAlphas[i]);
                    cl.setShortcutAndWidgetAlpha(mNewAlphas[i]);
                    cl.setRotationY(mNewRotationYs[i]);
                } else {
                    LauncherViewPropertyAnimator a = new LauncherViewPropertyAnimator(cl);
                    a.translationX(mNewTranslationXs[i])
                        .translationY(mNewTranslationYs[i])
                        .scaleX(mNewScaleXs[i])
                        .scaleY(mNewScaleYs[i])
                        .setDuration(duration)
                        .setInterpolator(mZoomInInterpolator);
                    anim.play(a);

                    if (mOldAlphas[i] != mNewAlphas[i] || currentAlpha != mNewAlphas[i]) {
                        LauncherViewPropertyAnimator alphaAnim =
                            new LauncherViewPropertyAnimator(cl.getShortcutsAndWidgets());
                        alphaAnim.alpha(mNewAlphas[i])
                            .setDuration(duration)
                            .setInterpolator(mZoomInInterpolator);
                        anim.play(alphaAnim);
                    }
                    if (mOldBackgroundAlphas[i] != 0 ||
                        mNewBackgroundAlphas[i] != 0) {
                        ValueAnimator bgAnim =
                                ValueAnimator.ofFloat(0f, 1f).setDuration(duration);
                        bgAnim.setInterpolator(mZoomInInterpolator);
                        bgAnim.addUpdateListener(new LauncherAnimatorUpdateListener() {
                                public void onAnimationUpdate(float a, float b) {
                                    cl.setBackgroundAlpha(
                                            a * mOldBackgroundAlphas[i] +
                                            b * mNewBackgroundAlphas[i]);
                                }
                            });
                        anim.play(bgAnim);
                    }
                }
            }
            anim.setStartDelay(delay);
        }

        if (stateIsSpringLoaded) {
            // Right now we're covered by Apps Customize
            // Show the background gradient immediately, so the gradient will
            // be showing once AppsCustomize disappears
            animateBackgroundGradient(getResources().getInteger(
                    R.integer.config_appsCustomizeSpringLoadedBgAlpha) / 100f, false);
        } else {
            // Fade the background gradient away
            animateBackgroundGradient(0f, true);
        }
        return anim;
    }

    @Override
    public void onLauncherTransitionPrepare(Launcher l, boolean animated, boolean toWorkspace) {
        mIsSwitchingState = true;
        updateChildrenLayersEnabled(false);
        cancelScrollingIndicatorAnimations();
    }

    @Override
    public void onLauncherTransitionStart(Launcher l, boolean animated, boolean toWorkspace) {
    }

    @Override
    public void onLauncherTransitionStep(Launcher l, float t) {
        mTransitionProgress = t;
    }

    @Override
    public void onLauncherTransitionEnd(Launcher l, boolean animated, boolean toWorkspace) {
        mIsSwitchingState = false;
        mWallpaperOffset.setOverrideHorizontalCatchupConstant(false);
        updateChildrenLayersEnabled(false);
        // The code in getChangeStateAnimation to determine initialAlpha and finalAlpha will ensure
        // ensure that only the current page is visible during (and subsequently, after) the
        // transition animation.  If fade adjacent pages is disabled, then re-enable the page
        // visibility after the transition animation.
        if (!mWorkspaceFadeInAdjacentScreens) {
            for (int i = 0; i < getChildCount(); i++) {
                final CellLayout cl = (CellLayout) getChildAt(i);
                cl.setShortcutAndWidgetAlpha(1f);
            }
        }
    }

    @Override
    public View getContent() {
        return this;
    }

    /**
     * Draw the View v into the given Canvas.
     *
     * @param v the view to draw
     * @param destCanvas the canvas to draw on
     * @param padding the horizontal and vertical padding to use when drawing
     */
    private void drawDragView(View v, Canvas destCanvas, int padding, boolean pruneToDrawable) {
        final Rect clipRect = mTempRect;
        v.getDrawingRect(clipRect);

        destCanvas.save();
        if (v instanceof TextView && pruneToDrawable) {
            Drawable d = ((TextView) v).getCompoundDrawables()[1];
            clipRect.set(0, 0, d.getIntrinsicWidth() + padding, d.getIntrinsicHeight() + padding);
            destCanvas.translate(padding / 2, padding / 2);
            d.draw(destCanvas);
        } else {
            destCanvas.translate(-v.getScrollX() + padding / 2, -v.getScrollY() + padding / 2);
            destCanvas.clipRect(clipRect, Op.REPLACE);
            
            //re-set the icon long press effect
            v.draw(destCanvas);
        }
        destCanvas.restore();
    }

    /**
     * Returns a new bitmap to show when the given View is being dragged around.
     * Responsibility for the bitmap is transferred to the caller.
     */
    public Bitmap createDragBitmap(View v, Canvas canvas, int padding) {
        Bitmap b;

        if (false && v instanceof TextView) {
            Drawable d = ((TextView) v).getCompoundDrawables()[1];
            b = Bitmap.createBitmap(d.getIntrinsicWidth() + padding,
                    d.getIntrinsicHeight() + padding, Bitmap.Config.ARGB_8888);
        } else {
            b = Bitmap.createBitmap(
                    v.getWidth() + padding, v.getHeight() + padding, Bitmap.Config.ARGB_8888);
        }

        canvas.setBitmap(b);
        drawDragView(v, canvas, padding, false);
        canvas.setBitmap(null);

        return b;
    }

    /**
     * Returns a new bitmap to be used as the object outline, e.g. to visualize the drop location.
     * Responsibility for the bitmap is transferred to the caller.
     */
    private Bitmap createDragOutline(View v, Canvas canvas, int padding) {
        //final int outlineColor = getResources().getColor(android.R.color.holo_blue_light);
        final Bitmap b = Bitmap.createBitmap(
                v.getWidth() + padding, v.getHeight() + padding, Bitmap.Config.ARGB_8888);

        canvas.setBitmap(b);
        drawDragView(v, canvas, padding, false);
        //replace outline with bitmap
        //mOutlineHelper.applyMediumExpensiveOutlineWithBlur(b, canvas, outlineColor, outlineColor);
        canvas.setBitmap(null);
        return b;
    }

    /**
     * Returns a new bitmap to be used as the object outline, e.g. to visualize the drop location.
     * Responsibility for the bitmap is transferred to the caller.
     */
    private Bitmap createDragOutline(Bitmap orig, Canvas canvas, int padding, int w, int h,
            boolean clipAlpha) {
        //final int outlineColor = getResources().getColor(android.R.color.holo_blue_light);
        
        // reduce bmp size 3MB about widget Outline Bitmap
        //final Bitmap b = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        final Bitmap b = Bitmap.createBitmap(w/2,h/2, Bitmap.Config.RGB_565);
        canvas.setBitmap(b);

        Rect src = new Rect(0, 0, orig.getWidth(), orig.getHeight());
        float scaleFactor = Math.min((w - padding) / (float) orig.getWidth(),
                (h - padding) / (float) orig.getHeight());
       
        int scaledWidth = (int) (scaleFactor * orig.getWidth());
        int scaledHeight = (int) (scaleFactor * orig.getHeight());
        
        Rect dst = new Rect(0, 0, scaledWidth/2, scaledHeight/2);
        
        // center the image
        dst.offset((w - scaledWidth) / 2, (h - scaledHeight) / 2);

        canvas.drawBitmap(orig, src, dst, null);
        //replace outline with bitmap
        //mOutlineHelper.applyMediumExpensiveOutlineWithBlur(b, canvas, outlineColor, outlineColor,
        //        clipAlpha);
        canvas.setBitmap(null);

        return b;
    }

    void startDrag(CellLayout.CellInfo cellInfo, DragSource source) {
        View child = cellInfo.cell;

        // Make sure the drag was started by a long press as opposed to a long click.
        if (!child.isInTouchMode()) {
            return;
        }
        
        //one app is in delete progress or the delete dialog is on, cancel the next drag
        if(mLauncher.isDragToDelete()) {
            return;
        }
        
        mDragInfo = cellInfo;
        ViewParent parent = child.getParent();
        if (parent == null) {
            return;
        }
        
        child.setVisibility(INVISIBLE);
        CellLayout layout = (CellLayout) parent.getParent();
        layout.prepareChildForDrag(child);

        child.clearFocus();
        child.setPressed(false);

        final Canvas canvas = new Canvas();

        // The outline is used to visualize where the item will land if dropped
        mDragOutline = createDragOutline(child, canvas, DRAG_BITMAP_PADDING);
        beginDragShared(child, source);
    }

    public void beginDragShared(View child, DragSource source) {
        if (child == null) {
            return;
        }
        View cahcedView = child;
        addToDragItems(cahcedView);
        
        Resources r = getResources();

        // The drag bitmap follows the touch point around on the screen
        final Bitmap b = createDragBitmap(child, new Canvas(), DRAG_BITMAP_PADDING);

        int bmpWidth = 0;
        int bmpHeight = 0;
        if (b != null) {
            bmpWidth = b.getWidth();
            bmpHeight = b.getHeight();
        } else {
            child.setVisibility(VISIBLE);
            mDragItems.remove(child);
            return;
        }

        float scale = mLauncher.getDragLayer().getLocationInDragLayer(child, mTempXY);
        int dragLayerX =
                Math.round(mTempXY[0] - (bmpWidth - scale * child.getWidth()) / 2);
        int dragLayerY =
                Math.round(mTempXY[1] - (bmpHeight - scale * bmpHeight) / 2
                        - DRAG_BITMAP_PADDING / 2);

        Point dragVisualizeOffset = null;
        Rect dragRect = null;
        if (child instanceof BubbleTextView || child instanceof PagedViewIcon) {
            int iconHeight = ConfigManager.getCelllayoutCellHeight();
            int iconWidth = ConfigManager.getCelllayoutCellWidth();
            int iconPaddingTop = r.getDimensionPixelSize(R.dimen.app_icon_padding_top);
            int top = 0;
            int bottom = 0;
            top = child.getPaddingTop();
            bottom = top + iconHeight;
            dragLayerY += top;
            int left = (bmpWidth - iconWidth) / 2;
            int right = left + iconWidth;
            // Note: The drag region is used to calculate drag layer offsets, but the
            // dragVisualizeOffset in addition to the dragRect (the size) to position the outline.
            dragVisualizeOffset = new Point(-DRAG_BITMAP_PADDING / 2,
                    iconPaddingTop - DRAG_BITMAP_PADDING / 2);
            dragRect = new Rect(left, top, right, bottom);
        } else if (child instanceof FolderIcon) {
            int previewSize = r.getDimensionPixelSize(R.dimen.folder_preview_size);
            dragRect = new Rect(0, 0, child.getWidth(), previewSize);
        }

        // Clear the pressed state if necessary
        if (child instanceof BubbleTextView) {
            BubbleTextView icon = (BubbleTextView) child;
            icon.clearPressedOrFocusedBackground();
        }
        
        mDragController.startDrag(b, dragLayerX, dragLayerY, source, child.getTag(),
                DragController.DRAG_ACTION_MOVE, dragVisualizeOffset, dragRect, scale);
        b.recycle();

        // Show the scrolling indicator when you pick up an item
        showScrollingIndicator(false);

        String name = "";
        if (child != null && child.getTag() != null) {
            if (child.getTag() instanceof LauncherAppWidgetInfo
                    && ((LauncherAppWidgetInfo) child.getTag()).providerName != null) {
                name = ((LauncherAppWidgetInfo) child.getTag()).providerName
                        .toString();
            } else if (child.getTag() instanceof ItemInfo
                    && ((ItemInfo) child.getTag()).title != null) {
                name = ((ItemInfo) child.getTag()).title.toString();
            }
        }

        UserTrackerHelper
                .sendUserReport(UserTrackerMessage.MSG_DRAG_ICON, name);
    }

    void addApplicationShortcut(ShortcutInfo info, CellLayout target, long container, int screen,
            int cellX, int cellY, boolean insertAtFirst, int intersectX, int intersectY) {
        View view = mLauncher.createShortcut(R.layout.application, target, (ShortcutInfo) info);

        final int[] cellXY = new int[2];
        target.findCellForSpanThatIntersects(cellXY, 1, 1, intersectX, intersectY);
        addInScreen(view, container, screen, cellXY[0], cellXY[1], 1, 1, insertAtFirst);
        LauncherModel.addOrMoveItemInDatabase(mLauncher, info, container, screen, cellXY[0],
                cellXY[1]);
    }

    public boolean transitionStateShouldAllowDrop() {
        return ((!isSwitchingState() || mTransitionProgress > 0.5f) && mState != State.SMALL);
    }
    
    /*
     * judge the drop target view whether can be drag/drop
     * return true if the target view can be drag/drop
     */
    private boolean acceptDragAction(CellLayout dropTargetLayout, int x, int y) {
        if (mInEditing && dropTargetLayout.getEditBtnContainer() != null
                && dropTargetLayout.isFakeChild()) {
            return false;
        }
        View dragTargetView = dropTargetLayout.getChildAt(x, y);
        return acceptDragAction(dragTargetView);
    }
   
    private boolean acceptDragAction(View dragTargetView){
        if(dragTargetView == null){
            return true;
        }
        boolean accept = true;
        ItemInfo info = (ItemInfo)dragTargetView.getTag();
        if(info!=null && info.itemType==Favorites.ITEM_TYPE_ALIAPPWIDGET){
            //accept = false;
        }
        return accept;
    }
    
    public boolean dragFromHotseat(Object dragInfo) {
        ItemInfo info = (ItemInfo)dragInfo;
        return info.container == Favorites.CONTAINER_HOTSEAT;
    }
    /**
     * {@inheritDoc}
     */
    public boolean acceptDrop(DragObject d) {
         // If it's an external drop (e.g. from All Apps), check if it should be accepted
         CellLayout dropTargetLayout = mDropToLayout;
         
         if (d.dragSource != this) {
            // Don't accept the drop if we're not over a screen at time of drop
            if (dropTargetLayout == null) {
                return false;
            }
            if (mInEditing && dropTargetLayout.isFakeChild()
                    && dropTargetLayout.getEditBtnContainer() != null) {
                    return false;
            }
            if (!transitionStateShouldAllowDrop()) return false;

            mDragViewVisualCenter = getDragViewVisualCenter(d.x, d.y, d.xOffset, d.yOffset,
                    d.dragView, mDragViewVisualCenter);

            // We want the point to be mapped to the dragTarget.
            if (mLauncher.isHotseatLayout(dropTargetLayout)) {
                mapPointFromSelfToHotseatLayout(mLauncher.getHotseat(), mDragViewVisualCenter);
            } else {
                mapPointFromSelfToChild(dropTargetLayout, mDragViewVisualCenter, null);
            }

            int spanX = 1;
            int spanY = 1;
            if (mDragInfo != null) {
                final CellLayout.CellInfo dragCellInfo = mDragInfo;
                spanX = dragCellInfo.spanX;
                spanY = dragCellInfo.spanY;
            } else {
                final ItemInfo dragInfo = (ItemInfo) d.dragInfo;
                spanX = dragInfo.spanX;
                spanY = dragInfo.spanY;
            }

            int minSpanX = spanX;
            int minSpanY = spanY;
            if (d.dragInfo instanceof PendingAddWidgetInfo) {
                minSpanX = ((PendingAddWidgetInfo) d.dragInfo).minSpanX;
                minSpanY = ((PendingAddWidgetInfo) d.dragInfo).minSpanY;
            } /*else if (d.dragInfo instanceof PendingAddGadgetInfo) {
                if (AgedModeUtil.isAgedMode()) {
                    PendingAddGadgetInfo info = ((PendingAddGadgetInfo)d.dragInfo);
                    if (minSpanX > 3) {
                       info.gadgetInfo.spanX = info.minSpanX = info.spanX = minSpanX = 3;
                       }
                    if (minSpanY > 3) {
                       info.gadgetInfo.spanY = info.minSpanY = info.spanY = minSpanY = 3;
                       }
                }
            }*/

            mTargetCell = findNearestArea((int) mDragViewVisualCenter[0],
                    (int) mDragViewVisualCenter[1], minSpanX, minSpanY, dropTargetLayout,
                    mTargetCell);
            float distance = dropTargetLayout.getDistanceFromCell(mDragViewVisualCenter[0],
                    mDragViewVisualCenter[1], mTargetCell);
            if (willCreateUserFolder((ItemInfo) d.dragInfo, dropTargetLayout,
                    mTargetCell, distance, true)) {
                return true;
            }
            if (willAddToExistingUserFolder((ItemInfo) d.dragInfo, dropTargetLayout,
                    mTargetCell, distance)) {
                return true;
            }

            int[] resultSpan = new int[2];
            mTargetCell = dropTargetLayout.createArea((int) mDragViewVisualCenter[0],
                    (int) mDragViewVisualCenter[1], minSpanX, minSpanY, spanX, spanY,
                    null, mTargetCell, resultSpan, CellLayout.MODE_ACCEPT_DROP);
            boolean foundCell = mTargetCell[0] >= 0 && mTargetCell[1] >= 0;

            // Don't accept the drop if there's no room for the item
            if (!foundCell) {
                Object info = d.dragInfo;
                if (info instanceof ItemInfo) {
                    if (((ItemInfo) info).container == LauncherSettings.Favorites.CONTAINER_HIDESEAT) {
                        // from hide-seat to workspace
                        mLauncher.showOutOfSpaceMessage(false);
                    } else if (((ItemInfo) info).container > 0) {
                        // from folder to workspace/dock
                        boolean toHotseat = mLauncher.isHotseatLayout(dropTargetLayout);
                        mLauncher.showOutOfSpaceMessage(toHotseat);
                    }
                }
                return false;
            }
        }
        return true;
    }

    boolean willCreateUserFolder(ItemInfo info, CellLayout target, int[] targetCell, float
            distance, boolean considerTimeout) {
        if (distance > mMaxDistanceForFolderCreation) {
            return false;
        }
        if (info != null && mState == State.SPRING_LOADED
                && info.itemType == LauncherSettings.Favorites.ITEM_TYPE_SHORTCUT) {
            return false;
        }
        View dropOverView = target.getChildAt(targetCell[0], targetCell[1]);

        if (dropOverView != null) {
            CellLayout.LayoutParams lp = (CellLayout.LayoutParams) dropOverView.getLayoutParams();
            if (lp.useTmpCoords && (lp.tmpCellX != lp.cellX || lp.tmpCellY != lp.cellY)) {
                return false;
            }
        }

        boolean hasntMoved = false;
        if (mDragInfo != null) {
            hasntMoved = dropOverView == mDragInfo.cell;
        } else {
            //when mDragInfo is null and dragSource is hideseat, hasntMoved should always be false
            hasntMoved = (info.container != LauncherSettings.Favorites.CONTAINER_HIDESEAT 
                   && targetCell[0] == info.cellX && targetCell[1] == info.cellY);
        }

        if (dropOverView == null || hasntMoved || (considerTimeout && !mCreateUserFolderOnDrop)) {
            return false;
        }

        boolean aboveShortcut = (dropOverView.getTag() instanceof ShortcutInfo);
        boolean willBecomeShortcut =
                (info.itemType == LauncherSettings.Favorites.ITEM_TYPE_APPLICATION ||
                info.itemType == LauncherSettings.Favorites.ITEM_TYPE_SHORTCUT||
                info.itemType == LauncherSettings.Favorites.ITEM_TYPE_SHORTCUT_DOWNLOADING||
                info.itemType == LauncherSettings.Favorites.ITEM_TYPE_VPINSTALL);

        return (aboveShortcut && willBecomeShortcut);
    }

    boolean willAddToExistingUserFolder(Object dragInfo, CellLayout target, int[] targetCell,
            float distance) {
        if (distance > mMaxDistanceForFolderCreation) return false;
        if (dragInfo != null && mState == State.SPRING_LOADED
                && ((ItemInfo) dragInfo).itemType == LauncherSettings.Favorites.ITEM_TYPE_SHORTCUT) {
            return false;
        }
        View dropOverView = target.getChildAt(targetCell[0], targetCell[1]);

        if (dropOverView != null) {
            CellLayout.LayoutParams lp = (CellLayout.LayoutParams) dropOverView.getLayoutParams();
            if (lp.useTmpCoords && (lp.tmpCellX != lp.cellX || lp.tmpCellY != lp.cellY)) {
                return false;
            }
        }

        if (dropOverView instanceof FolderIcon) {
            FolderIcon fi = (FolderIcon) dropOverView;
            if (fi.acceptDrop(dragInfo)) {
                return true;
            }
        }
        return false;
    }

    boolean createUserFolderIfNecessary(View newView, long container, CellLayout target,
            int[] targetCell, float distance, boolean external, DragView dragView,
            Runnable postAnimationRunnable) {
        if (distance > mMaxDistanceForFolderCreation) {
        	return false;
        }
        View v = null;
        if(target!=null){
            v = target.getChildAt(targetCell[0], targetCell[1]);
        }

        boolean hasntMoved = false;
        if (mDragInfo != null) {
            CellLayout cellParent = getParentCellLayoutForView(mDragInfo.cell);
            hasntMoved = (mDragInfo.cellX == targetCell[0] &&
                    mDragInfo.cellY == targetCell[1]) && (cellParent == target);
        }

        if (v == null || hasntMoved || !mCreateUserFolderOnDrop) {
        	return false;
        }
        mCreateUserFolderOnDrop = false;
        final int screen = (targetCell == null) ? mDragInfo.screen : indexOfChild(target);

        boolean aboveShortcut = (v.getTag() instanceof ShortcutInfo);
        boolean willBecomeShortcut = (newView.getTag() instanceof ShortcutInfo);

        if (aboveShortcut && willBecomeShortcut) {
            ShortcutInfo sourceInfo = (ShortcutInfo) newView.getTag();
            ShortcutInfo destInfo = (ShortcutInfo) v.getTag();
            // if the drag started here, we need to remove it from the workspace
            if (!external) {
                getParentCellLayoutForView(mDragInfo.cell).removeView(mDragInfo.cell);
            }

            Rect folderLocation = new Rect();
            float scale = mLauncher.getDragLayer().getDescendantRectRelativeToSelf(v, folderLocation);
            target.removeView(v);

            FolderIcon fi =
                mLauncher.addFolder(target, container, screen, targetCell[0], targetCell[1]);
            destInfo.cellX = -1;
            destInfo.cellY = -1;
            sourceInfo.cellX = -1;
            sourceInfo.cellY = -1;

            // If the dragView is null, we can't animate
            boolean animate = dragView != null;
            if (AppGroupManager.isSwitchOn()) {
                String title = null;
                String destPkgName = null;
                String sourcePkgName = null;
                try {
                    ComponentName desInfoName = destInfo.intent.getComponent();
                    ComponentName sourceInfoName = sourceInfo.intent.getComponent();
                    destPkgName = getPackageNameByComponentName(desInfoName, destInfo);
                    sourcePkgName = getPackageNameByComponentName(sourceInfoName, sourceInfo);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (destPkgName != null || sourcePkgName != null) {
                    AppGroupManager.getInstance().handleFolderNameByPkgNames(fi, mCallback, destPkgName, sourcePkgName);
                }
            }
            // do not display folder background when folder is empty when creating
            fi.mDropMode = true;
            if (animate) {
                fi.performCreateAnimation(destInfo, v, sourceInfo, dragView, folderLocation, scale,
                        postAnimationRunnable);
            } else {
                fi.addItem(destInfo);
                fi.addItem(sourceInfo);
            }
            return true;
        }
        return false;
    }

    private String getPackageNameByComponentName(ComponentName componentName, ShortcutInfo info) {
        String packageName = null;
        if (componentName != null) {
            packageName = componentName.getPackageName();
        } else if (info.itemType == LauncherSettings.Favorites.ITEM_TYPE_SHORTCUT_DOWNLOADING) {
            packageName = info.intent
                    .getStringExtra(AppDownloadManager.TYPE_PACKAGENAME);
        } else if (info.itemType == LauncherSettings.Favorites.ITEM_TYPE_SHORTCUT) {
            packageName = info.intent.getPackage();
        }
        return packageName;
    }

    boolean addToExistingFolderIfNecessary(View newView, CellLayout target, int[] targetCell,
            float distance, DragObject d, boolean external) {
        if (distance > mMaxDistanceForFolderCreation) return false;

        Log.d(TAG, "sxsexe-------->addToExistingFolderIfNecessary mAddToExistingFolderOnDrop " + mAddToExistingFolderOnDrop);
        View dropOverView = target.getChildAt(targetCell[0], targetCell[1]);
        //topwise zyf add for fixedfolder 不允许往文件夹中拖放 
        if (dropOverView instanceof FixedFolderIcon) 
        {
        	return false;
        }
    	//topwise zyf add end
        if (!mAddToExistingFolderOnDrop) return false;
        mAddToExistingFolderOnDrop = false;

        if (dropOverView instanceof FolderIcon) {
            FolderIcon fi = (FolderIcon) dropOverView;
            if (fi.acceptDrop(d.dragInfo)) {
                fi.onDrop(d);

                // if the drag started here, we need to remove it from the workspace
                if (!external) {
                    CellLayout cellLayout = getParentCellLayoutForView(mDragInfo.cell);
                    if(cellLayout != null) {
                        cellLayout.removeView(mDragInfo.cell);
                    }
                }
                return true;
            }
        }
        return false;
    }

    public void onDrop(final DragObject d) {
        //mLauncher.exitFullScreen();
        mDragViewVisualCenter = getDragViewVisualCenter(d.x, d.y, d.xOffset, d.yOffset, d.dragView,
                mDragViewVisualCenter);
        

        CellLayout dropTargetLayout = mDropToLayout;
        boolean toHotseat = false;
        // We want the point to be mapped to the dragTarget.
        if (dropTargetLayout != null) {
            if (mLauncher.isHotseatLayout(dropTargetLayout)) {
                toHotseat = true;
                mapPointFromSelfToHotseatLayout(mLauncher.getHotseat(), mDragViewVisualCenter);
            } else {
                toHotseat = false;
                mapPointFromSelfToChild(dropTargetLayout, mDragViewVisualCenter, null);
            }
        }
        //Log.d(TAG, "sxsexe------------> onDrop d.dragInfo " + d.dragInfo + " toHotseat " + toHotseat);

        int snapScreen = -1;
        boolean resizeOnDrop = false;
        if (d.dragSource != this) {
            final int[] touchXY = new int[] { (int) mDragViewVisualCenter[0],
                    (int) mDragViewVisualCenter[1] };
            onDropExternal(touchXY, d.dragInfo, dropTargetLayout, false, d);
        } else if (mDragInfo != null) {
            final View cell = mDragInfo.cell;

            Runnable resizeRunnable = null;
            if (dropTargetLayout != null) {
                // Move internally
                boolean hasMovedIntoHotseat = mLauncher.isHotseatLayout(dropTargetLayout);
                boolean hasMovedLayouts = (getParentCellLayoutForView(cell) != dropTargetLayout) || hasMovedIntoHotseat;
                long container = hasMovedIntoHotseat ?
                        LauncherSettings.Favorites.CONTAINER_HOTSEAT :
                        LauncherSettings.Favorites.CONTAINER_DESKTOP;
                int screen = (mTargetCell[0] < 0) ?
                        mDragInfo.screen : indexOfChild(dropTargetLayout);
                
                int spanX = mDragInfo != null ? mDragInfo.spanX : 1;
                int spanY = mDragInfo != null ? mDragInfo.spanY : 1;
                // First we find the cell nearest to point at which the item is
                // dropped, without any consideration to whether there is an item there.

                mTargetCell = findNearestArea((int) mDragViewVisualCenter[0], (int)
                        mDragViewVisualCenter[1], spanX, spanY, dropTargetLayout, mTargetCell);
                float distance = dropTargetLayout.getDistanceFromCell(mDragViewVisualCenter[0],
                        mDragViewVisualCenter[1], mTargetCell);

                // If the item being dropped is a shortcut and the nearest drop
                // cell also contains a shortcut, then create a folder with the two shortcuts.
                boolean createUserFolder = createUserFolderIfNecessary(cell, container,
                        dropTargetLayout, mTargetCell, distance, false, d.dragView, null);
                if (!mInScrollArea && createUserFolder) {
                    checkAndRemoveEmptyCell();
                    if(mLauncher.getHotseat().checkDragitem(cell)) {
                        mLauncher.getHotseat().onDrop(createUserFolder, d.x, null, cell, false);
                    }
                    return;
                }

                if (addToExistingFolderIfNecessary(cell, dropTargetLayout, mTargetCell,
                        distance, d, false)) {
                    checkAndRemoveEmptyCell();
                    if(mLauncher.getHotseat().checkDragitem(cell)) {
                        mLauncher.getHotseat().onDrop(true, d.x, null, cell, false);
                    }
                    return;
                }

                // Aside from the special case where we're dropping a shortcut onto a shortcut,
                // we need to find the nearest cell location that is vacant
                ItemInfo item = (ItemInfo) d.dragInfo;
                int minSpanX = item.spanX;
                int minSpanY = item.spanY;
                if (item.minSpanX > 0 && item.minSpanY > 0) {
                    minSpanX = item.minSpanX;
                    minSpanY = item.minSpanY;
                }

                /*if (item instanceof GadgetItemInfo) {
                    if (AgedModeUtil.isAgedMode()) {
                        GadgetItemInfo info = ((GadgetItemInfo)item);
                        if (minSpanX > 3) {
                            info.gadgetInfo.spanX = info.minSpanX = info.spanX = minSpanX = 3;
                           }
                        if (minSpanY > 3) {
                           info.gadgetInfo.spanY = info.minSpanY = info.spanY = minSpanY = 3;
                           }
                    }
                }*/

                boolean foundCell = false;
                int[] resultSpan = new int[2];
                boolean targetaccepted = this.acceptDragAction(dropTargetLayout,mTargetCell[0] ,mTargetCell[1]);
                boolean isFull = mLauncher.getHotseat().isFull();
                
                if(toHotseat) {
                    if(dragFromHotseat(d.dragInfo)) {
                        foundCell = true;
                    } else {
                        foundCell = !isFull;
                    }
                } else {
                    if(targetaccepted){
                        mTargetCell = dropTargetLayout.createArea((int) mDragViewVisualCenter[0],
                                (int) mDragViewVisualCenter[1], minSpanX, minSpanY, spanX, spanY, cell,
                                mTargetCell, resultSpan, CellLayout.MODE_ON_DROP);

                        foundCell = mTargetCell[0] >= 0 && mTargetCell[1] >= 0;
                    }
                }
                
                // if the widget resizes on drop
                if (foundCell && (cell instanceof AppWidgetHostView) &&
                        (resultSpan[0] != item.spanX || resultSpan[1] != item.spanY)) {
                    resizeOnDrop = true;
                    item.spanX = resultSpan[0];
                    item.spanY = resultSpan[1];
                    AppWidgetHostView awhv = (AppWidgetHostView) cell;
                    AppWidgetResizeFrame.updateWidgetSizeRanges(awhv, mLauncher, resultSpan[0],
                            resultSpan[1]);
                }

                if (mCurrentPage != screen && !hasMovedIntoHotseat) {
                    snapScreen = screen;
                    snapToPage(screen);
                }
                Log.d(TAG, "sxsexe55------> workspace.onDrop foundCell " + foundCell + " hasMovedLayouts " + hasMovedLayouts
                            + " toHotseat " + toHotseat + " fromHotseat " + dragFromHotseat(d.dragInfo)
                            + " item " + item 
                            + " cell.tag " + cell.getTag());
                final ItemInfo info = (ItemInfo) cell.getTag();
                if (foundCell) {
                    if (hasMovedLayouts) {
                        // Reparent the view
                        if(toHotseat) {
                            if(!dragFromHotseat(d.dragInfo)) {
                                if(mLauncher.getHotseat().isFull()) {
                                    mDragController.cancelDrag();
                                    checkAndRemoveEmptyCell();
                                    mLauncher.showOutOfSpaceMessage(true);
                                    return;
                                }
                                getParentCellLayoutForView(cell).removeView(cell);
                                int index = mLauncher.getHotseat().getAppropriateIndex(d.x);
                                mLauncher.getHotseat().onDrop(true, d.x, d.dragView, cell, true);
//                                mLauncher.getHotseat().animateDropToPosition(d.x, cell, d.dragView);
                                addInHotseat(cell, container, screen, index, 0,
                                        info.spanX, info.spanY, index);
                                Object obj = cell.getTag();
                                if (obj instanceof ShortcutInfo) {
                                    Log.d(TAG, "update hotseat container first " + container);
                                    ((ShortcutInfo)obj).container = container;
                                }
                            } else {
                                mLauncher.getHotseat().onDrop(true, d.x, d.dragView, cell, true);
                            }
                        } else {
                            if(cell.getParent() != null) {
                                getParentCellLayoutForView(cell).removeView(cell);
                            }
                            cell.setVisibility(View.VISIBLE);
                            addInScreen(cell, container, screen, mTargetCell[0], mTargetCell[1],
                                    info.spanX, info.spanY);
                            if(dragFromHotseat(d.dragInfo)) {
                                mLauncher.getHotseat().onDrop(true, d.x, null, cell, false);
                            }
                            Object obj = cell.getTag();
                            if (obj instanceof ShortcutInfo) {
                                Log.d(TAG, "update desktop container first " + container);
                                ((ShortcutInfo)obj).container = container;
                            }
                        }
                    }

                    // update the item's position after drop
                    CellLayout.LayoutParams lp = (CellLayout.LayoutParams) cell.getLayoutParams();
                    lp.cellX = lp.tmpCellX = mTargetCell[0];
                    lp.cellY = lp.tmpCellY = mTargetCell[1];
                    lp.cellHSpan = item.spanX;
                    lp.cellVSpan = item.spanY;
                    lp.isLockedToGrid = true;
                    cell.setId(LauncherModel.getCellLayoutChildId(container, mDragInfo.screen,
                            mTargetCell[0], mTargetCell[1], mDragInfo.spanX, mDragInfo.spanY));

                    if (container != LauncherSettings.Favorites.CONTAINER_HOTSEAT &&
                            cell instanceof LauncherAppWidgetHostView) {
                        final CellLayout cellLayout = dropTargetLayout;
                        // We post this call so that the widget has a chance to be placed
                        // in its final location

                        final LauncherAppWidgetHostView hostView = (LauncherAppWidgetHostView) cell;
                        AppWidgetProviderInfo pinfo = hostView.getAppWidgetInfo();
                        if (pinfo != null &&
                                pinfo.resizeMode != AppWidgetProviderInfo.RESIZE_NONE) {
                            final Runnable addResizeFrame = new Runnable() {
                                public void run() {
                                    DragLayer dragLayer = mLauncher.getDragLayer();
                                    dragLayer.addResizeFrame(info, hostView, cellLayout);
                                }
                            };
                            resizeRunnable = (new Runnable() {
                                public void run() {
                                    if (!isPageMoving()) {
                                        addResizeFrame.run();
                                    } else {
                                        mDelayedResizeRunnable = addResizeFrame;
                                    }
                                }
                            });
                        }
                    }
                    if (!toHotseat) {
                        LauncherModel.moveItemInDatabase(mLauncher, info, container, screen, lp.cellX,
                                lp.cellY);
                    }
                } else {
                    // If we can't find a drop location, we return the item to its original position
                    CellLayout.LayoutParams lp = (CellLayout.LayoutParams) cell.getLayoutParams();
                    mTargetCell[0] = lp.cellX;
                    mTargetCell[1] = lp.cellY;
                    CellLayout layout = null;
                    if(cell.getParent() != null) {
                        layout = (CellLayout) cell.getParent().getParent();
                    } else if(mLauncher.getHotseat().checkDragitem(cell)){
                        layout = mLauncher.getHotseat().getCellLayout();
                    }
                    if(layout != null)
                        layout.markCellsAsOccupiedForView(cell);
                    
                    if(toHotseat) {
                        if(!dragFromHotseat(d.dragInfo) && mLauncher.getHotseat().isFull()) {
                            mLauncher.showOutOfSpaceMessage(true);
                        }
                        mLauncher.getHotseat().onDrop(false, 0, d.dragView, cell, true);
                    } else {
                        mLauncher.showOutOfSpaceMessage(false);
                        if(dragFromHotseat(d.dragInfo)) {
                            addInHotseat(cell, info.container, info.screen,
                                    info.cellX, info.cellY, info.spanX, info.spanY, info.screen);
                        }
                        mLauncher.getHotseat().onDrop(false, 0, d.dragView, cell, true);
                    }
                }
            }

            if(cell.getParent() != null && !toHotseat) {
                final CellLayout parent = (CellLayout) cell.getParent().getParent();
                final Runnable finalResizeRunnable = resizeRunnable;
                // Prepare it to be animated into its new position
                // This must be called after the view has been re-parented
                final Runnable onCompleteRunnable = new Runnable() {
                    @Override
                    public void run() {
                        mAnimatingViewIntoPlace = false;
                        updateChildrenLayersEnabled(false);
                        if (finalResizeRunnable != null) {
                            finalResizeRunnable.run();
                        }
                    }
                };
                mAnimatingViewIntoPlace = true;
                if (d.dragView.hasDrawn()) {
                    final ItemInfo info = (ItemInfo) cell.getTag();
                    if (info.itemType == LauncherSettings.Favorites.ITEM_TYPE_APPWIDGET) {
                        int animationType = resizeOnDrop ? ANIMATE_INTO_POSITION_AND_RESIZE :
                            ANIMATE_INTO_POSITION_AND_DISAPPEAR;
                        animateWidgetDrop(info, parent, d.dragView,
                                onCompleteRunnable, animationType, cell, false);
                    } else {
                        int duration = snapScreen < 0 ? -1 : ADJACENT_SCREEN_DROP_DURATION;
                        mLauncher.getDragLayer().animateViewIntoPosition(d.dragView, cell, duration,
                                onCompleteRunnable, this);
                    }
                } else {
                    d.deferDragViewCleanupPostAnimation = false;
                    cell.setVisibility(VISIBLE);
                }
                parent.onDropChild(cell);
                
                Log.d(TAG, "sxsexe-------------------------->checkAndRemoveEmptyCell--------------------");
                checkAndRemoveEmptyCell();
            }
        } else {
            Log.e(TAG, "sxsexe-------->Error mDraginfo is null");
        }
    }

    public void setFinalScrollForPageChange(int screen) {
        CellLayout cl = (CellLayout) getChildAt(screen);
        if (cl != null) {
            mSavedScrollX = getScrollX();
            mSavedTranslationX = cl.getTranslationX();
            mSavedRotationY = cl.getRotationY();
            final int newX = getChildOffset(screen) - getRelativeChildOffset(screen);
            setScrollX(newX);
            cl.setTranslationX(0f);
            cl.setRotationY(0f);
        }
    }

    public void resetFinalScrollForPageChange(int screen) {
        CellLayout cl = (CellLayout) getChildAt(screen);
        if (cl != null) {
            setScrollX(mSavedScrollX);
            cl.setTranslationX(mSavedTranslationX);
            cl.setRotationY(mSavedRotationY);
        }
    }

    public void getViewLocationRelativeToSelf(View v, int[] location) {
        getLocationInWindow(location);
        int x = location[0];
        int y = location[1];

        v.getLocationInWindow(location);
        int vX = location[0];
        int vY = location[1];

        location[0] = vX - x;
        location[1] = vY - y;
    }

    public void onDragEnter(DragObject d) {
        mDragEnforcer.onDragEnter();
        mCreateUserFolderOnDrop = false;
        mAddToExistingFolderOnDrop = false;

        mDropToLayout = null;
        CellLayout layout = getCurrentDropLayout();
        setCurrentDropLayout(layout);
        setCurrentDragOverlappingLayout(layout);

        // Because we don't have space in the Phone UI (the CellLayouts run to the edge) we
        // don't need to show the outlines
        if (LauncherApplication.isScreenLarge()) {
            showOutlines();
        }
        
        if (!mEditModeFeatrueFlag) {
            addEmptyScreen();
        }

        // Folder optimization:
        // Make a delay when drag icon from folder to dock.
        if (d.dragSource instanceof Folder) {
            // If the source is folder, temporarily disable the flag
            mDragFromFolderToHotseatEnable = false;
            // recover the flag after a delay
            mDragFromFolderToHotseatAlarm.setAlarm(sDragFromFolderToHotseatDelay);
        } else {
            mDragFromFolderToHotseatEnable = true;
        }
        removeWidgetPages();
    }

    static Rect getCellLayoutMetrics(Launcher launcher, int orientation) {
        Resources res = launcher.getResources();
        Display display = launcher.getWindowManager().getDefaultDisplay();
        Point smallestSize = new Point();
        Point largestSize = new Point();
        display.getCurrentSizeRange(smallestSize, largestSize);
        if (orientation == CellLayout.LANDSCAPE) {
            if (mLandscapeCellLayoutMetrics == null) {
                int paddingLeft = res.getDimensionPixelSize(R.dimen.workspace_left_padding_land);
                int paddingRight = res.getDimensionPixelSize(R.dimen.workspace_right_padding_land);
                int paddingTop = res.getDimensionPixelSize(R.dimen.workspace_top_padding_land);
                int paddingBottom = res.getDimensionPixelSize(R.dimen.workspace_bottom_padding_land);
                int width = largestSize.x - paddingLeft - paddingRight;
                int height = smallestSize.y - paddingTop - paddingBottom;
                mLandscapeCellLayoutMetrics = new Rect();
                CellLayout.getMetrics(mLandscapeCellLayoutMetrics, res,
                        width, height, LauncherModel.getCellCountX(), LauncherModel.getCellCountY(),
                        orientation);
            }
            return mLandscapeCellLayoutMetrics;
        } else if (orientation == CellLayout.PORTRAIT) {
            if (mPortraitCellLayoutMetrics == null) {
                int paddingLeft = res.getDimensionPixelSize(R.dimen.workspace_left_padding_land);
                int paddingRight = res.getDimensionPixelSize(R.dimen.workspace_right_padding_land);
                int paddingTop = res.getDimensionPixelSize(R.dimen.workspace_top_padding_land);
                int paddingBottom = res.getDimensionPixelSize(R.dimen.workspace_bottom_padding_land);
                int width = smallestSize.x - paddingLeft - paddingRight;
                int height = largestSize.y - paddingTop - paddingBottom;
                mPortraitCellLayoutMetrics = new Rect();
                CellLayout.getMetrics(mPortraitCellLayoutMetrics, res,
                        width, height, LauncherModel.getCellCountX(), LauncherModel.getCellCountY(),
                        orientation);
            }
            return mPortraitCellLayoutMetrics;
        }
        return null;
    }

    public void onDragExit(DragObject d) {
        mDragEnforcer.onDragExit();

        // Here we store the final page that will be dropped to, if the workspace in fact
        // receives the drop
        if (mInScrollArea) {
            if (isPageMoving()) {
                // If the user drops while the page is scrolling, we should use that page as the
                // destination instead of the page that is being hovered over.
                mDropToLayout = (CellLayout) getPageAt(getNextPage());
            } else {
                if (!d.dragComplete) {
                    mDropToLayout = mDragOverlappingLayout;
                } else {
                    mDropToLayout = (CellLayout) getPageAt(getCurrentPage());
                }
                Log.d(TAG, "onDragExit dropLayout index : " + indexOfChild(mDropToLayout) +
                           " dragComplete : " + d.dragComplete);
            }
        } else {
            mDropToLayout = mDragTargetLayout;
        }

        if (mDragMode == DRAG_MODE_CREATE_FOLDER) {
            mCreateUserFolderOnDrop = true;
        } else if (mDragMode == DRAG_MODE_ADD_TO_FOLDER) {
            mAddToExistingFolderOnDrop = true;
        }

        // Reset the scroll area and previous drag target
        onResetScrollArea();
        setCurrentDropLayout(null);
        setCurrentDragOverlappingLayout(null);

        mSpringLoadedDragController.cancel();

        if (!mIsPageMoving) {
            hideOutlines();
        }

        mDragFromFolderToHotseatAlarm.cancelAlarm();
        mDragFromFolderToHotseatEnable = true;
        if (!mInEditing) {
            makeSureWidgetPages();
        }
    }

    void setCurrentDropLayout(CellLayout layout) {
        if (mDragTargetLayout != null) {
            mDragTargetLayout.revertTempState();
            mDragTargetLayout.onDragExit();
        }
        mDragTargetLayout = layout;
        if (mDragTargetLayout != null) {
            mDragTargetLayout.onDragEnter();
        }
        cleanupReorder(true);
        cleanupFolderCreation();
        setCurrentDropOverCell(-1, -1);
    }

    void setCurrentDragOverlappingLayout(CellLayout layout) {
        if (mDragOverlappingLayout != null) {
            mDragOverlappingLayout.setIsDragOverlapping(false);
        }
        mDragOverlappingLayout = layout;
        if (mDragOverlappingLayout != null) {
            mDragOverlappingLayout.setIsDragOverlapping(true);
        }
        invalidate();
    }

    void setCurrentDropOverCell(int x, int y) {
        if (x != mDragOverX || y != mDragOverY) {
            mDragOverX = x;
            mDragOverY = y;
            setDragMode(DRAG_MODE_NONE);
        }
    }

    void setDragMode(int dragMode) {
        if (dragMode != mDragMode) {
            if (dragMode == DRAG_MODE_NONE) {
                cleanupAddToFolder();
                // We don't want to cancel the re-order alarm every time the target cell changes
                // as this feels to slow / unresponsive.
                cleanupReorder(false);
                cleanupFolderCreation();
            } else if (dragMode == DRAG_MODE_ADD_TO_FOLDER) {
                cleanupReorder(true);
                cleanupFolderCreation();
            } else if (dragMode == DRAG_MODE_CREATE_FOLDER) {
                cleanupAddToFolder();
                cleanupReorder(true);
            } else if (dragMode == DRAG_MODE_REORDER) {
                cleanupAddToFolder();
                cleanupFolderCreation();
            }
            mDragMode = dragMode;
        }
    }

    private void cleanupFolderCreation() {
        if (mDragFolderRingAnimator != null) {
            mDragFolderRingAnimator.animateToNaturalState();
        }
        mFolderCreationAlarm.cancelAlarm();
    }

    private void cleanupAddToFolder() {
        if (mDragOverFolderIcon != null) {
            mDragOverFolderIcon.onDragExit(null);
            mDragOverFolderIcon = null;
        }
    }

    private void cleanupReorder(boolean cancelAlarm) {
        // Any pending reorders are canceled
        if (cancelAlarm) {
            mReorderAlarm.cancelAlarm();
        }
        mLastReorderX = -1;
        mLastReorderY = -1;
    }

    public DropTarget getDropTargetDelegate(DragObject d) {
        return null;
    }

    /*
    *
    * Convert the 2D coordinate xy from the parent View's coordinate space to this CellLayout's
    * coordinate space. The argument xy is modified with the return result.
    *
    */
   void mapPointFromSelfToChild(View v, float[] xy) {
       mapPointFromSelfToChild(v, xy, null);
   }

   /*
    *
    * Convert the 2D coordinate xy from the parent View's coordinate space to this CellLayout's
    * coordinate space. The argument xy is modified with the return result.
    *
    * if cachedInverseMatrix is not null, this method will just use that matrix instead of
    * computing it itself; we use this to avoid redundant matrix inversions in
    * findMatchingPageForDragOver
    *
    */
   void mapPointFromSelfToChild(View v, float[] xy, Matrix cachedInverseMatrix) {
       if (cachedInverseMatrix == null) {
           v.getMatrix().invert(mTempInverseMatrix);
           cachedInverseMatrix = mTempInverseMatrix;
       }
       int scrollX = getScrollX();
       if (mNextPage != INVALID_PAGE) {
           scrollX = mScroller.getFinalX();
       }
       xy[0] = xy[0] + scrollX - v.getLeft();
       xy[1] = xy[1] + getScrollY() - v.getTop();
       cachedInverseMatrix.mapPoints(xy);
   }


   void mapPointFromSelfToHotseatLayout(Hotseat hotseat, float[] xy) {
       hotseat.getLayout().getMatrix().invert(mTempInverseMatrix);
       xy[0] = xy[0] - hotseat.getLeft() - hotseat.getLayout().getLeft();
       xy[1] = xy[1] - hotseat.getTop() - hotseat.getLayout().getTop();
       mTempInverseMatrix.mapPoints(xy);
   }

   /*
    *
    * Convert the 2D coordinate xy from this CellLayout's coordinate space to
    * the parent View's coordinate space. The argument xy is modified with the return result.
    *
    */
   void mapPointFromChildToSelf(View v, float[] xy) {
       v.getMatrix().mapPoints(xy);
       int scrollX = getScrollX();
       if (mNextPage != INVALID_PAGE) {
           scrollX = mScroller.getFinalX();
       }
       xy[0] -= (scrollX - v.getLeft());
       xy[1] -= (getScrollY() - v.getTop());
   }

   static private float squaredDistance(float[] point1, float[] point2) {
        float distanceX = point1[0] - point2[0];
        float distanceY = point2[1] - point2[1];
        return distanceX * distanceX + distanceY * distanceY;
   }

    /*
     *
     * Returns true if the passed CellLayout cl overlaps with dragView
     *
     */
    boolean overlaps(CellLayout cl, DragView dragView,
            int dragViewX, int dragViewY, Matrix cachedInverseMatrix) {
        // Transform the coordinates of the item being dragged to the CellLayout's coordinates
        final float[] draggedItemTopLeft = mTempDragCoordinates;
        draggedItemTopLeft[0] = dragViewX;
        draggedItemTopLeft[1] = dragViewY;
        final float[] draggedItemBottomRight = mTempDragBottomRightCoordinates;
        draggedItemBottomRight[0] = draggedItemTopLeft[0] + dragView.getDragRegionWidth();
        draggedItemBottomRight[1] = draggedItemTopLeft[1] + dragView.getDragRegionHeight();

        // Transform the dragged item's top left coordinates
        // to the CellLayout's local coordinates
        mapPointFromSelfToChild(cl, draggedItemTopLeft, cachedInverseMatrix);
        float overlapRegionLeft = Math.max(0f, draggedItemTopLeft[0]);
        float overlapRegionTop = Math.max(0f, draggedItemTopLeft[1]);

        if (overlapRegionLeft <= cl.getWidth() && overlapRegionTop >= 0) {
            // Transform the dragged item's bottom right coordinates
            // to the CellLayout's local coordinates
            mapPointFromSelfToChild(cl, draggedItemBottomRight, cachedInverseMatrix);
            float overlapRegionRight = Math.min(cl.getWidth(), draggedItemBottomRight[0]);
            float overlapRegionBottom = Math.min(cl.getHeight(), draggedItemBottomRight[1]);

            if (overlapRegionRight >= 0 && overlapRegionBottom <= cl.getHeight()) {
                float overlap = (overlapRegionRight - overlapRegionLeft) *
                         (overlapRegionBottom - overlapRegionTop);
                if (overlap > 0) {
                    return true;
                }
             }
        }
        return false;
    }

    /*
     *
     * This method returns the CellLayout that is currently being dragged to. In order to drag
     * to a CellLayout, either the touch point must be directly over the CellLayout, or as a second
     * strategy, we see if the dragView is overlapping any CellLayout and choose the closest one
     *
     * Return null if no CellLayout is currently being dragged over
     *
     */
    private CellLayout findMatchingPageForDragOver(
            DragView dragView, float originX, float originY, boolean exact) {
        // We loop through all the screens (ie CellLayouts) and see which ones overlap
        // with the item being dragged and then choose the one that's closest to the touch point
        final int screenCount = getChildCount();
        CellLayout bestMatchingScreen = null;
        float smallestDistSoFar = Float.MAX_VALUE;

        for (int i = 0; i < screenCount; i++) {
            CellLayout cl = (CellLayout) getChildAt(i);

            final float[] touchXy = {originX, originY};
            // Transform the touch coordinates to the CellLayout's local coordinates
            // If the touch point is within the bounds of the cell layout, we can return immediately
            cl.getMatrix().invert(mTempInverseMatrix);
            mapPointFromSelfToChild(cl, touchXy, mTempInverseMatrix);

            if (touchXy[0] >= 0 && touchXy[0] <= cl.getWidth() &&
                    touchXy[1] >= 0 && touchXy[1] <= cl.getHeight()) {
                return cl;
            }

            if (!exact) {
                // Get the center of the cell layout in screen coordinates
                final float[] cellLayoutCenter = mTempCellLayoutCenterCoordinates;
                cellLayoutCenter[0] = cl.getWidth()/2;
                cellLayoutCenter[1] = cl.getHeight()/2;
                mapPointFromChildToSelf(cl, cellLayoutCenter);

                touchXy[0] = originX;
                touchXy[1] = originY;

                // Calculate the distance between the center of the CellLayout
                // and the touch point
                float dist = squaredDistance(touchXy, cellLayoutCenter);

                if (dist < smallestDistSoFar) {
                    smallestDistSoFar = dist;
                    bestMatchingScreen = cl;
                }
            }
        }
        return bestMatchingScreen;
    }

    // This is used to compute the visual center of the dragView. This point is then
    // used to visualize drop locations and determine where to drop an item. The idea is that
    // the visual center represents the user's interpretation of where the item is, and hence
    // is the appropriate point to use when determining drop location.
    private float[] getDragViewVisualCenter(int x, int y, int xOffset, int yOffset,
            DragView dragView, float[] recycle) {
        float res[];
        if (recycle == null) {
            res = new float[2];
        } else {
            res = recycle;
        }

        // First off, the drag view has been shifted in a way that is not represented in the
        // x and y values or the x/yOffsets. Here we account for that shift.
        x += getResources().getDimensionPixelSize(R.dimen.dragViewOffsetX);
        y += getResources().getDimensionPixelSize(R.dimen.dragViewOffsetY);

        // These represent the visual top and left of drag view if a dragRect was provided.
        // If a dragRect was not provided, then they correspond to the actual view left and
        // top, as the dragRect is in that case taken to be the entire dragView.
        // R.dimen.dragViewOffsetY.
        int left = x - xOffset;
        int top = y - yOffset;

        // In order to find the visual center, we shift by half the dragRect
        res[0] = left + dragView.getDragRegion().width() / 2;
        res[1] = top + dragView.getDragRegion().height() / 2;
        return res;
    }

    private boolean isDragWidget(DragObject d) {
        return (d.dragInfo instanceof LauncherAppWidgetInfo ||
                d.dragInfo instanceof PendingAddWidgetInfo);
    }

    private boolean isDragGadget(DragObject d) {
        return d.dragInfo instanceof GadgetItemInfo
                || d.dragInfo instanceof PendingAddGadgetInfo;
    }

    private boolean isExternalDragWidget(DragObject d) {
        return d.dragSource != this && isDragWidget(d);
    }

    public void onDragOver(DragObject d) {
        // Skip drag over events while we are dragging over side pages
        if (mInScrollArea || mIsSwitchingState || mState == State.SMALL) return;

        Rect r = new Rect();
        CellLayout layout = null;
        ItemInfo item = (ItemInfo) d.dragInfo;

        // Ensure that we have proper spans for the item that we are dropping
        if (item.spanX < 0 || item.spanY < 0) throw new RuntimeException("Improper spans found");
        mDragViewVisualCenter = getDragViewVisualCenter(d.x, d.y, d.xOffset, d.yOffset,
            d.dragView, mDragViewVisualCenter);

        final View child = (mDragInfo == null) ? null : mDragInfo.cell;
        // Identify whether we have dragged over a side page
        if (isSmall()) {
            if (mLauncher.getHotseat() != null
                && mLauncher.getHotseat().getVisibility() == View.VISIBLE
                && !isExternalDragWidget(d)
                && mDragFromFolderToHotseatEnable) {
                mLauncher.getHotseat().getHitRect(r);
                if (r.contains(d.x, d.y)) {
                    layout = mLauncher.getHotseat().getLayout();
                }
            }
            if (layout == null) {
                layout = findMatchingPageForDragOver(d.dragView, d.x, d.y, false);
            }
            if (layout != mDragTargetLayout) {

                setCurrentDropLayout(layout);
                setCurrentDragOverlappingLayout(layout);

                boolean isInSpringLoadedMode = (mState == State.SPRING_LOADED);
                if (isInSpringLoadedMode) {
                    if (mLauncher.isHotseatLayout(layout)) {
                        mSpringLoadedDragController.cancel();
                    } else {
                        mSpringLoadedDragController.setAlarm(mDragTargetLayout);
                    }
                }
            }
        } else {
            // Test to see if we are over the hotseat otherwise just use the current page
            if (mLauncher.getHotseat() != null
                    && mLauncher.getHotseat().getVisibility() == View.VISIBLE
                    && !isDragWidget(d) && !isDragGadget(d)
                    && mDragFromFolderToHotseatEnable) {
                mLauncher.getHotseat().getHitRect(r);
                if (r.contains(d.x, d.y)) {
                    layout = mLauncher.getHotseat().getLayout();
                }
            }
            if (layout == null) {
                layout = getCurrentDropLayout();
            }
            if (layout != mDragTargetLayout) {
                setCurrentDropLayout(layout);
                setCurrentDragOverlappingLayout(layout);
            }
        }
        
        if(CheckVoiceCommandPressHelper.PUSH_TO_TALK_SUPPORT) {
            CheckVoiceCommandPressHelper.getInstance().checkDragRegion(d, true, mDragViewVisualCenter);
        }
        final boolean supportCard = mLauncher.getIconManager().supprtCardIcon();

        // Handle the drag over
        if (mDragTargetLayout != null) {
            // We want the point to be mapped to the dragTarget.
            if (mLauncher.isHotseatLayout(mDragTargetLayout)) {
                mapPointFromSelfToHotseatLayout(mLauncher.getHotseat(), mDragViewVisualCenter);
                mLauncher.getHotseat().onEnterHotseat(d.x, item.screen, dragFromHotseat(d.dragInfo), d);
            } else {
                mapPointFromSelfToChild(mDragTargetLayout, mDragViewVisualCenter, null);
                boolean isDragFromHotseat = dragFromHotseat(d.dragInfo);
                if (isDragFromHotseat && (mDragInfo != null)) {
                    View view = mDragInfo.cell;
                    if (view instanceof BubbleTextView) {
                        ((BubbleTextView) view).resetTempPadding();
                    } else if (view instanceof FolderIcon) {
                        FolderIcon fi = (FolderIcon) view;
                        fi.resetTempPadding();
                    }
                }
                mLauncher.getHotseat().onExitHotseat(isDragFromHotseat);
            }

            ItemInfo info = (ItemInfo) d.dragInfo;

            mTargetCell = findNearestArea((int) mDragViewVisualCenter[0],
                    (int) mDragViewVisualCenter[1], item.spanX, item.spanY,
                    mDragTargetLayout, mTargetCell);
            
            setCurrentDropOverCell(mTargetCell[0], mTargetCell[1]);

            float targetCellDistance = mDragTargetLayout.getDistanceFromCell(
                    mDragViewVisualCenter[0], mDragViewVisualCenter[1], mTargetCell);
            //adjustment the effect of the merger folder
            // divide distance to make hover big icon easily
            //if (supportCard)
            //    targetCellDistance /= 1.3f;

            final View dragOverView = mDragTargetLayout.getChildAt(mTargetCell[0],
                    mTargetCell[1]);
            
            //traffic panel can't be drop
            if(!acceptDragAction(dragOverView)){
                if(mReorderAlarm!=null){
                    mReorderAlarm.setOnAlarmListener(null);
                }
                return;
            }
            
            manageFolderFeedback(info, mDragTargetLayout, mTargetCell,
                    targetCellDistance, dragOverView);

            int minSpanX = item.spanX;
            int minSpanY = item.spanY;
            if (item.minSpanX > 0 && item.minSpanY > 0) {
                minSpanX = item.minSpanX;
                minSpanY = item.minSpanY;
            }

            boolean nearestDropOccupied = mDragTargetLayout.isNearestDropLocationOccupied((int)
                    mDragViewVisualCenter[0], (int) mDragViewVisualCenter[1], item.spanX,
                    item.spanY, child, mTargetCell);
            boolean isHotseatLayout = mLauncher.isHotseatLayout(mDragTargetLayout);
            
            if(isHotseatLayout) {
                if(dragFromHotseat(d.dragInfo)) {
                    int [] topLeft = new int[2];
                    mLauncher.getHotseat().touchToPoint(d.x, topLeft, true, true);
                    mTargetCell[0] = topLeft[0];
                } else {
                    int [] topLeft = new int[2];
                    mLauncher.getHotseat().touchToPoint(d.x, topLeft, false, false);
                    mTargetCell[0] = topLeft[0];
                }
            }
            if (!nearestDropOccupied || isHotseatLayout) {
                mDragTargetLayout.visualizeDropLocation(child, mDragOutline,
                        (int) mDragViewVisualCenter[0], (int) mDragViewVisualCenter[1],
                        mTargetCell[0], mTargetCell[1], item.spanX, item.spanY, false,
                        d.dragView.getDragVisualizeOffset(), d.dragView.getDragRegion(), 
                        mLauncher.isHotseatLayout(mDragTargetLayout), dragFromHotseat(d.dragInfo), d.x);
            } else if ((mDragMode == DRAG_MODE_NONE || mDragMode == DRAG_MODE_REORDER)
                    && !mReorderAlarm.alarmPending() 
                    && (mLastReorderX != mTargetCell[0] || mLastReorderY != mTargetCell[1])
                    && !mLauncher.isHotseatLayout(mDragTargetLayout)) {
                // Otherwise, if we aren't adding to or creating a folder and there's no pending
                // reorder, then we schedule a reorder
                ReorderAlarmListener listener = new ReorderAlarmListener(mDragViewVisualCenter,
                        minSpanX, minSpanY, item.spanX, item.spanY, d.dragView, child, d);
                mReorderAlarm.setOnAlarmListener(listener);
                mReorderAlarm.setAlarm(REORDER_TIMEOUT);
            }

            if (mDragMode == DRAG_MODE_CREATE_FOLDER || mDragMode == DRAG_MODE_ADD_TO_FOLDER ||
                    !nearestDropOccupied) {
                if (mDragTargetLayout != null) {
                    mDragTargetLayout.revertTempState();
                }
            }
        }
    }

    private void manageFolderFeedback(ItemInfo info, CellLayout targetLayout,
            int[] targetCell, float distance, View dragOverView) {
        boolean userFolderPending = willCreateUserFolder(info, targetLayout, targetCell, distance,
                false);
        
        if(mLauncher.isHotseatLayout(targetLayout)) {
            userFolderPending = false;
        }
        if (mDragMode == DRAG_MODE_NONE && userFolderPending &&
                !mFolderCreationAlarm.alarmPending()) {
            mFolderCreationAlarm.setOnAlarmListener(new
                    FolderCreationAlarmListener(targetLayout, targetCell[0], targetCell[1]));
            mFolderCreationAlarm.setAlarm(FOLDER_CREATION_TIMEOUT);
            return;
        }

        boolean willAddToFolder =
                willAddToExistingUserFolder(info, targetLayout, targetCell, distance);
        if(mLauncher.isHotseatLayout(targetLayout)) {
            willAddToFolder = false;
        }
        if (willAddToFolder && mDragMode == DRAG_MODE_NONE) {
            mDragOverFolderIcon = ((FolderIcon) dragOverView);
            mDragOverFolderIcon.onDragEnter(info);
            if (targetLayout != null) {
                targetLayout.clearDragOutlines();
            }
            setDragMode(DRAG_MODE_ADD_TO_FOLDER);
            return;
        }

        if (mDragMode == DRAG_MODE_ADD_TO_FOLDER && !willAddToFolder) {
            setDragMode(DRAG_MODE_NONE);
        }
        if (mDragMode == DRAG_MODE_CREATE_FOLDER && !userFolderPending) {
            setDragMode(DRAG_MODE_NONE);
        }

        return;
    }

    class FolderCreationAlarmListener implements OnAlarmListener {
        CellLayout layout;
        int cellX;
        int cellY;

        public FolderCreationAlarmListener(CellLayout layout, int cellX, int cellY) {
            this.layout = layout;
            this.cellX = cellX;
            this.cellY = cellY;
        }

        public void onAlarm(Alarm alarm) {
            if (mDragFolderRingAnimator == null) {
                mDragFolderRingAnimator = new FolderRingAnimator(mLauncher, null);
            }
            mDragFolderRingAnimator.setCell(cellX, cellY);
            mDragFolderRingAnimator.setCellLayout(layout);
            mDragFolderRingAnimator.animateToAcceptState();
            layout.showFolderAccept(mDragFolderRingAnimator);
            layout.clearDragOutlines();
            setDragMode(DRAG_MODE_CREATE_FOLDER);
        }
    }

    class ReorderAlarmListener implements OnAlarmListener {
        float[] dragViewCenter;
        int minSpanX, minSpanY, spanX, spanY;
        DragView dragView;
        View child;
        DragObject dragObject;

        public ReorderAlarmListener(float[] dragViewCenter, int minSpanX, int minSpanY, int spanX,
                int spanY, DragView dragView, View child, DragObject dragObject) {
            this.dragViewCenter = dragViewCenter;
            this.minSpanX = minSpanX;
            this.minSpanY = minSpanY;
            this.spanX = spanX;
            this.spanY = spanY;
            this.child = child;
            this.dragView = dragView;
            this.dragObject = dragObject;
        }

        public void onAlarm(Alarm alarm) {
            int[] resultSpan = new int[2];
            mTargetCell = findNearestArea((int) mDragViewVisualCenter[0],
                    (int) mDragViewVisualCenter[1], spanX, spanY, mDragTargetLayout, mTargetCell);
            mLastReorderX = mTargetCell[0];
            mLastReorderY = mTargetCell[1];
            
            mTargetCell = mDragTargetLayout.createArea((int) mDragViewVisualCenter[0],
                (int) mDragViewVisualCenter[1], minSpanX, minSpanY, spanX, spanY,
                child, mTargetCell, resultSpan, CellLayout.MODE_DRAG_OVER);
            
            if (mTargetCell[0] < 0 || mTargetCell[1] < 0) {
                mDragTargetLayout.revertTempState();
            } else {
                setDragMode(DRAG_MODE_REORDER);
            }

            boolean resize = resultSpan[0] != spanX || resultSpan[1] != spanY;
            mDragTargetLayout.visualizeDropLocation(child, mDragOutline,
                (int) mDragViewVisualCenter[0], (int) mDragViewVisualCenter[1],
                mTargetCell[0], mTargetCell[1], resultSpan[0], resultSpan[1], resize,
                dragView.getDragVisualizeOffset(), dragView.getDragRegion(), 
                mLauncher.isHotseatLayout(mDragTargetLayout), dragFromHotseat(dragObject.dragInfo), dragObject.x);
        }
    }

    @Override
    public void getHitRect(Rect outRect) {
        // We want the workspace to have the whole area of the display (it will find the correct
        // cell layout to drop to in the existing drag/drop logic.
        outRect.set(0, 0, mDisplaySize.x, mDisplaySize.y);
    }

    /**
     * Add the item specified by dragInfo to the given layout.
     * @return true if successful
     */
    public boolean addExternalItemToScreen(ItemInfo dragInfo, CellLayout layout) {
        if (layout.findCellForSpan(mTempEstimate, dragInfo.spanX, dragInfo.spanY)) {
            onDropExternal(dragInfo.dropPos, (ItemInfo) dragInfo, (CellLayout) layout, false);
            return true;
        }
        mLauncher.showOutOfSpaceMessage(mLauncher.isHotseatLayout(layout));
        return false;
    }

    private void onDropExternal(int[] touchXY, Object dragInfo,
            CellLayout cellLayout, boolean insertAtFirst) {
        onDropExternal(touchXY, dragInfo, cellLayout, insertAtFirst, null);
    }

    /**
     * Drop an item that didn't originate on one of the workspace screens.
     * It may have come from Launcher (e.g. from all apps or customize), or it may have
     * come from another app altogether.
     *
     * NOTE: This can also be called when we are outside of a drag event, when we want
     * to add an item to one of the workspace screens.
     */
    private void onDropExternal(final int[] touchXY, final Object dragInfo,
            final CellLayout cellLayout, boolean insertAtFirst, DragObject d) {
        final Runnable exitSpringLoadedRunnable = new Runnable() {
            @Override
            public void run() {
                mLauncher.exitSpringLoadedDragModeDelayed(true, false, null);
            }
        };

        ItemInfo info = (ItemInfo) dragInfo;
        int spanX = info.spanX;
        int spanY = info.spanY;
        if (mDragInfo != null) {
            spanX = mDragInfo.spanX;
            spanY = mDragInfo.spanY;
        }

        final long container = mLauncher.isHotseatLayout(cellLayout) ?
                LauncherSettings.Favorites.CONTAINER_HOTSEAT :
                    LauncherSettings.Favorites.CONTAINER_DESKTOP;
        //check if the dragview from hotseat
        if(mLauncher.isHotseatLayout(cellLayout)){
            if(mLauncher.getHotseat().isFull() && !dragFromHotseat(d.dragInfo)) {
                mDragController.cancelDrag();
                checkAndRemoveEmptyCell();
                mLauncher.showOutOfSpaceMessage(true);
                return;
            }
        }
        final int screen = indexOfChild(cellLayout);
        if (!mLauncher.isHotseatLayout(cellLayout) && screen != mCurrentPage
                && mState != State.SPRING_LOADED) {
            snapToPage(screen);
        }

        if (info instanceof PendingAddItemInfo) {
            final PendingAddItemInfo pendingInfo = (PendingAddItemInfo) dragInfo;

            boolean findNearestVacantCell = true;
            if (pendingInfo.itemType == LauncherSettings.Favorites.ITEM_TYPE_SHORTCUT) {
                mTargetCell = findNearestArea((int) touchXY[0], (int) touchXY[1], spanX, spanY,
                        cellLayout, mTargetCell);
                float distance = cellLayout.getDistanceFromCell(mDragViewVisualCenter[0],
                        mDragViewVisualCenter[1], mTargetCell);
                if (willCreateUserFolder((ItemInfo) d.dragInfo, cellLayout, mTargetCell,
                        distance, true) || willAddToExistingUserFolder((ItemInfo) d.dragInfo,
                                cellLayout, mTargetCell, distance)) {
                    findNearestVacantCell = false;
                }
            }

            final ItemInfo item = (ItemInfo) d.dragInfo;
            boolean updateWidgetSize = false;
            if (findNearestVacantCell) {
                int minSpanX = item.spanX;
                int minSpanY = item.spanY;
                if (item.minSpanX > 0 && item.minSpanY > 0) {
                    minSpanX = item.minSpanX;
                    minSpanY = item.minSpanY;
                }
                int[] resultSpan = new int[2];
                mTargetCell = cellLayout.createArea((int) mDragViewVisualCenter[0],
                        (int) mDragViewVisualCenter[1], minSpanX, minSpanY, info.spanX, info.spanY,
                        null, mTargetCell, resultSpan, CellLayout.MODE_ON_DROP_EXTERNAL);

                if (resultSpan[0] != item.spanX || resultSpan[1] != item.spanY) {
                    updateWidgetSize = true;
                }
                item.spanX = resultSpan[0];
                item.spanY = resultSpan[1];
            }

            Runnable onAnimationCompleteRunnable = new Runnable() {
                @Override
                public void run() {
                    // When dragging and dropping from customization tray, we deal with creating
                    // widgets/shortcuts/folders in a slightly different way
                    switch (pendingInfo.itemType) {
                    case LauncherSettings.Favorites.ITEM_TYPE_APPWIDGET:
                        int span[] = new int[2];
                        span[0] = item.spanX;
                        span[1] = item.spanY;
                        mLauncher.addAppWidgetFromDrop((PendingAddWidgetInfo) pendingInfo,
                                container, screen, mTargetCell, span, null);
                        break;
                    /*case LauncherSettings.Favorites.ITEM_TYPE_GADGET:
                        span = new int[2];
                        span[0] = item.spanX;
                        span[1] = item.spanY;
                        GadgetItemInfo info = new GadgetItemInfo(((PendingAddGadgetInfo) pendingInfo).gadgetInfo);
                            if (AgedModeUtil.isAgedMode()) {
                            info.spanX = info.spanX > 3 ? 3 : info.spanX;
                            info.spanY = info.spanX > 3 ? 3 : info.spanY;
                            }
                        mLauncher.addGadgetFromDrop(info,
                                container, screen, mTargetCell, span, null);
                        break;*/
                    case LauncherSettings.Favorites.ITEM_TYPE_SHORTCUT:
                        mLauncher.processShortcutFromDrop(pendingInfo.componentName,
                                container, screen, mTargetCell, null);
                        break;
                    default:
                        throw new IllegalStateException("Unknown item type: " +
                                pendingInfo.itemType);
                    }
                }
            };
            View finalView = pendingInfo.itemType == LauncherSettings.Favorites.ITEM_TYPE_APPWIDGET
                    ? ((PendingAddWidgetInfo) pendingInfo).boundWidget : null;

            if (finalView instanceof AppWidgetHostView && updateWidgetSize) {
                AppWidgetHostView awhv = (AppWidgetHostView) finalView;
                AppWidgetResizeFrame.updateWidgetSizeRanges(awhv, mLauncher, item.spanX,
                        item.spanY);
            }

            int animationStyle = ANIMATE_INTO_POSITION_AND_DISAPPEAR;
            if ((pendingInfo.itemType == LauncherSettings.Favorites.ITEM_TYPE_APPWIDGET &&
                    ((PendingAddWidgetInfo) pendingInfo).info.configure != null)
                    ||
                    (pendingInfo.itemType == LauncherSettings.Favorites.ITEM_TYPE_SHORTCUT &&
                    ((PendingAddShortcutInfo) pendingInfo).shortcutActivityInfo != null)) {
                animationStyle = ANIMATE_INTO_POSITION_AND_REMAIN;
            }
            animateWidgetDrop(info, cellLayout, d.dragView, onAnimationCompleteRunnable,
                    animationStyle, finalView, true);
        } else {
            // This is for other drag/drop cases, like dragging from All Apps
            View view = null;

            switch (info.itemType) {
            case LauncherSettings.Favorites.ITEM_TYPE_APPLICATION:
            case LauncherSettings.Favorites.ITEM_TYPE_SHORTCUT:
                if (info.container == NO_ID && info instanceof ApplicationInfo) {
                    // Came from all apps -- make a copy
                    info = new ShortcutInfo((ApplicationInfo) info);
                }
                view = mLauncher.createShortcut(R.layout.application, cellLayout,
                        (ShortcutInfo) info);
                break;
            case LauncherSettings.Favorites.ITEM_TYPE_FOLDER:
                view = FolderIcon.fromXml(R.layout.folder_icon, mLauncher, cellLayout,
                        (FolderInfo) info);
                break;
            case LauncherSettings.Favorites.ITEM_TYPE_SHORTCUT_DOWNLOADING: 
                view = mLauncher.createShortcut(R.layout.application, cellLayout,
                        (ShortcutInfo) info);
                break;
            case LauncherSettings.Favorites.ITEM_TYPE_VPINSTALL:
            	view = mLauncher.createShortcut(R.layout.application, cellLayout,
                        (ShortcutInfo) info);
                break;
            default:
                throw new IllegalStateException("Unknown item type: " + info.itemType);
            }

            // First we find the cell nearest to point at which the item is
            // dropped, without any consideration to whether there is an item there.
            if (touchXY != null) {
                mTargetCell = findNearestArea((int) touchXY[0], (int) touchXY[1], spanX, spanY,
                        cellLayout, mTargetCell);
                float distance = cellLayout.getDistanceFromCell(mDragViewVisualCenter[0],
                        mDragViewVisualCenter[1], mTargetCell);
                d.postAnimationRunnable = exitSpringLoadedRunnable;
                if (createUserFolderIfNecessary(view, container, cellLayout, mTargetCell, distance,
                        true, d.dragView, d.postAnimationRunnable)) {
                    return;
                }
                if (addToExistingFolderIfNecessary(view, cellLayout, mTargetCell, distance, d,
                        true)) {
                    return;
                }
            }

            if (touchXY != null) {
                // when dragging and dropping, just find the closest free spot
                mTargetCell = cellLayout.createArea((int) mDragViewVisualCenter[0],
                        (int) mDragViewVisualCenter[1], 1, 1, 1, 1,
                        null, mTargetCell, null, CellLayout.MODE_ON_DROP_EXTERNAL);
            } else {
                cellLayout.findCellForSpan(mTargetCell, 1, 1);
            }
            
            if(mLauncher.isHotseatLayout(cellLayout)) {
                int index = mLauncher.getHotseat().getAppropriateIndex(d.x);
                mLauncher.getHotseat().onDrop(true, d.x, d.dragView, view, true);
                addInHotseat(view, container, index, index, 0,
                        info.spanX, info.spanY, index);
                mLauncher.getHotseat().updateItemCell();
                mLauncher.getHotseat().updateItemInDatabase();
            } else {
                addInScreen(view, container, screen, mTargetCell[0], mTargetCell[1], info.spanX,
                        info.spanY, insertAtFirst);
                cellLayout.onDropChild(view);
                CellLayout.LayoutParams lp = (CellLayout.LayoutParams) view.getLayoutParams();
                cellLayout.getShortcutsAndWidgets().measureChild(view);
                final long originalContainer = info.container;
                LauncherModel.addOrMoveItemInDatabase(mLauncher, info, container, screen,
                        lp.cellX, lp.cellY);

                // drag icon from hide-seat to workspace
                if (originalContainer == LauncherSettings.Favorites.CONTAINER_HIDESEAT &&
                    info instanceof ShortcutInfo) {
                    if (mLauncher != null && mLauncher.getHideseat() != null) {
                        mLauncher.getHideseat().onDragIconFromHideseatToWorkspace(view, (ShortcutInfo) info, this);
                    }
                }
            }
            if (d.dragView != null) {
                // We wrap the animation call in the temporary set and reset of the current
                // cellLayout to its final transform -- this means we animate the drag view to
                // the correct final location.
                setFinalTransitionTransform(cellLayout);
                mLauncher.getDragLayer().animateViewIntoPosition(d.dragView, view,
                        exitSpringLoadedRunnable);
                resetTransitionTransform(cellLayout);
            }
        }
    }

    public Bitmap createWidgetBitmap(ItemInfo widgetInfo, View layout) {
        int[] unScaledSize = mLauncher.getWorkspace().estimateItemSize(widgetInfo.spanX,
                widgetInfo.spanY, widgetInfo, false);
        int visibility = layout.getVisibility();
        layout.setVisibility(VISIBLE);

        int width = MeasureSpec.makeMeasureSpec(unScaledSize[0], MeasureSpec.EXACTLY);
        int height = MeasureSpec.makeMeasureSpec(unScaledSize[1], MeasureSpec.EXACTLY);
        if (unScaledSize[0] > 0 &&  unScaledSize[1] > 0) {
            Bitmap b = Bitmap.createBitmap(unScaledSize[0], unScaledSize[1],
                    Bitmap.Config.ARGB_8888);
            Canvas c = new Canvas(b);

            layout.measure(width, height);
            layout.layout(0, 0, unScaledSize[0], unScaledSize[1]);
            layout.draw(c);
            c.setBitmap(null);
            layout.setVisibility(visibility);
            return b;
        }
        return null;
    }

    private void getFinalPositionForDropAnimation(int[] loc, float[] scaleXY,
            DragView dragView, CellLayout layout, ItemInfo info, int[] targetCell,
            boolean external, boolean scale) {
        // Now we animate the dragView, (ie. the widget or shortcut preview) into its final
        // location and size on the home screen.
        int spanX = info.spanX;
        int spanY = info.spanY;

        Rect r = estimateItemPosition(layout, info, targetCell[0], targetCell[1], spanX, spanY);
        loc[0] = r.left;
        loc[1] = r.top;

        setFinalTransitionTransform(layout);
        float cellLayoutScale =
                mLauncher.getDragLayer().getDescendantCoordRelativeToSelf(layout, loc);
        resetTransitionTransform(layout);

        float dragViewScaleX;
        float dragViewScaleY;
        if (scale) {
            dragViewScaleX = (1.0f * r.width()) / dragView.getMeasuredWidth();
            dragViewScaleY = (1.0f * r.height()) / dragView.getMeasuredHeight();
        } else {
            dragViewScaleX = 1f;
            dragViewScaleY = 1f;
        }

        // The animation will scale the dragView about its center, so we need to center about
        // the final location.
        loc[0] -= (dragView.getMeasuredWidth() - cellLayoutScale * r.width()) / 2;
        loc[1] -= (dragView.getMeasuredHeight() - cellLayoutScale * r.height()) / 2;

        scaleXY[0] = dragViewScaleX * cellLayoutScale;
        scaleXY[1] = dragViewScaleY * cellLayoutScale;
    }

    public void animateWidgetDrop(ItemInfo info, CellLayout cellLayout, DragView dragView,
            final Runnable onCompleteRunnable, int animationType, final View finalView,
            boolean external) {
        Rect from = new Rect();
        mLauncher.getDragLayer().getViewRectRelativeToSelf(dragView, from);

        int[] finalPos = new int[2];
        float scaleXY[] = new float[2];
        // boolean scalePreview = !(info instanceof PendingAddShortcutInfo);
        boolean scalePreview = true;
        getFinalPositionForDropAnimation(finalPos, scaleXY, dragView, cellLayout, info, mTargetCell,
                external, scalePreview);

        Resources res = mLauncher.getResources();
        int duration = res.getInteger(R.integer.config_dropAnimMaxDuration) - 200;

        // In the case where we've prebound the widget, we remove it from the DragLayer
        if (finalView instanceof AppWidgetHostView && external) {
            Log.d(TAG, "6557954 Animate widget drop, final view is appWidgetHostView");
            mLauncher.getDragLayer().removeView(finalView);
        }
        if ((animationType == ANIMATE_INTO_POSITION_AND_RESIZE || external) && finalView != null) {
            Bitmap crossFadeBitmap = createWidgetBitmap(info, finalView);
            dragView.setCrossFadeBitmap(crossFadeBitmap);
            dragView.crossFade((int) (duration * 0.8f));
        } else if (info.itemType == LauncherSettings.Favorites.ITEM_TYPE_APPWIDGET && external) {
            scaleXY[0] = scaleXY[1] = Math.min(scaleXY[0],  scaleXY[1]);
        }

        DragLayer dragLayer = mLauncher.getDragLayer();
        if (animationType == CANCEL_TWO_STAGE_WIDGET_DROP_ANIMATION) {
            mLauncher.getDragLayer().animateViewIntoPosition(dragView, finalPos, 0f, 0.1f, 0.1f,
                    DragLayer.ANIMATION_END_DISAPPEAR, onCompleteRunnable, duration);
        } else {
            int endStyle;
            if (animationType == ANIMATE_INTO_POSITION_AND_REMAIN) {
                endStyle = DragLayer.ANIMATION_END_REMAIN_VISIBLE;
            } else {
                endStyle = DragLayer.ANIMATION_END_DISAPPEAR;;
            }

            Runnable onComplete = new Runnable() {
                @Override
                public void run() {
                    if (finalView != null) {
                        finalView.setVisibility(VISIBLE);
                    }
                    if (onCompleteRunnable != null) {
                        onCompleteRunnable.run();
                    }
                }
            };
            dragLayer.animateViewIntoPosition(dragView, from.left, from.top, finalPos[0],
                    finalPos[1], 1, 1, 1, scaleXY[0], scaleXY[1], onComplete, endStyle,
                    duration, this);
        }
    }

    public void setFinalTransitionTransform(CellLayout layout) {
        if (isSwitchingState()) {
            int index = indexOfChild(layout);
            if(index < 0){
                return;
            }
            mCurrentScaleX = layout.getScaleX();
            mCurrentScaleY = layout.getScaleY();
            mCurrentTranslationX = layout.getTranslationX();
            mCurrentTranslationY = layout.getTranslationY();
            mCurrentRotationY = layout.getRotationY();
            layout.setScaleX(mNewScaleXs[index]);
            layout.setScaleY(mNewScaleYs[index]);
            layout.setTranslationX(mNewTranslationXs[index]);
            layout.setTranslationY(mNewTranslationYs[index]);
            layout.setRotationY(mNewRotationYs[index]);
        }
    }
    public void resetTransitionTransform(CellLayout layout) {
        if (isSwitchingState()) {
            mCurrentScaleX = layout.getScaleX();
            mCurrentScaleY = layout.getScaleY();
            mCurrentTranslationX = layout.getTranslationX();
            mCurrentTranslationY = layout.getTranslationY();
            mCurrentRotationY = layout.getRotationY();
            layout.setScaleX(mCurrentScaleX);
            layout.setScaleY(mCurrentScaleY);
            layout.setTranslationX(mCurrentTranslationX);
            layout.setTranslationY(mCurrentTranslationY);
            layout.setRotationY(mCurrentRotationY);
        }
    }

    /**
     * Return the current {@link CellLayout}, correctly picking the destination
     * screen while a scroll is in progress.
     */
    public CellLayout getCurrentDropLayout() {
        return (CellLayout) getChildAt(getNextPage());
    }

    /**
     * Return the current CellInfo describing our current drag; this method exists
     * so that Launcher can sync this object with the correct info when the activity is created/
     * destroyed
     *
     */
    public CellLayout.CellInfo getDragInfo() {
        //return mDragInfo first, then return the retored draginfo mDragInfoDelete;
        if(mDragInfo!=null){
            return mDragInfo;
        }else{
            return mDragInfoDelete;
        }
    }

    /**
     * Calculate the nearest cell where the given object would be dropped.
     *
     * pixelX and pixelY should be in the coordinate system of layout
     */
    private int[] findNearestArea(int pixelX, int pixelY,
            int spanX, int spanY, CellLayout layout, int[] recycle) {
        return layout.findNearestArea(
                pixelX, pixelY, spanX, spanY, recycle);
    }

    void setup(DragController dragController) {
        mSpringLoadedDragController = new SpringLoadedDragController(mLauncher);
        mDragController = dragController;

        // hardware layers on children are enabled on startup, but should be disabled until
        // needed
        updateChildrenLayersEnabled(false);
        setWallpaperDimension();
    }

    /**
     * Called at the end of a drag which originated on the workspace.
     */
    public void onDropCompleted(View target, DragObject d, boolean isFlingToDelete,
            boolean success) {
        
        mDropTargetView = target;
        if (success) {
            if (target != this) {
                if (mDragInfo != null) {
                    // not remove the view in case user click the cancel uninstall
                    //getParentCellLayoutForView(mDragInfo.cell).removeView(mDragInfo.cell);
                    if(d.dragInfo instanceof ShortcutInfo) {
                        ShortcutInfo shortcutInfo = (ShortcutInfo)d.dragInfo;
                        if(d.isFlingToMove) {
                            mDragInfo.cell.setVisibility(View.GONE);
                        } else {
                            if(shortcutInfo.itemType == Favorites.ITEM_TYPE_APPLICATION) {
                                if (target instanceof Hideseat) {
                                    getParentCellLayoutForView(mDragInfo.cell).removeView(mDragInfo.cell);
                                } else {
                                    if(!shortcutInfo.isDeletable()) {
                                        mLauncher.reVisibileDraggedItem(shortcutInfo);
                                    } else {
                                        mDragInfo.cell.setVisibility(View.GONE);
                                    }
                                }
                            } else if(shortcutInfo.itemType == Favorites.ITEM_TYPE_SHORTCUT_DOWNLOADING) {
                                if(!shortcutInfo.isDeletable()) {
                                    mLauncher.reVisibileDraggedItem(shortcutInfo);
                                } else {
                                    CellLayout layout = getParentCellLayoutForView(mDragInfo.cell);
                                    if (layout != null) {
                                        layout.removeView(mDragInfo.cell);
                                    }
                                }
                            } else {
                                DataCollector.getInstance(
                                        getContext().getApplicationContext())
                                        .collectDeleteShortcutData(shortcutInfo);
                                CellLayout layout = getParentCellLayoutForView(mDragInfo.cell);
                                if (layout != null) {
                                    layout.removeView(mDragInfo.cell);
                                }
                            }
                        }
                    } else if(d.dragInfo instanceof LauncherAppWidgetInfo) {
                        getParentCellLayoutForView(mDragInfo.cell).removeView(mDragInfo.cell);                        
                        //release widget view referenece 
                         mDragInfo.cell = null;
                        mDragOutline = null;
                    } else if (d.dragInfo instanceof GadgetItemInfo) {
                        CellLayout layout = getParentCellLayoutForView(mDragInfo.cell);
                        if (layout != null) {
                            layout.removeView(mDragInfo.cell);
                        }
                    } else {
                        if (d.dragInfo instanceof FolderInfo && target instanceof Hideseat) {
                            getParentCellLayoutForView(mDragInfo.cell).removeView(mDragInfo.cell);
                        } else {
                            mDragInfo.cell.setVisibility(View.GONE);
                        }
                    }
                    if (mDragInfo.cell instanceof DropTarget) {
                        mDragController.removeDropTarget((DropTarget) mDragInfo.cell);
                    }
                }
            }
        } else if (mDragInfo != null) {
            CellLayout cellLayout;
            if (mLauncher.isHotseatLayout(target)) {
                cellLayout = mLauncher.getHotseat().getLayout();
            } else {
                cellLayout = (CellLayout) getChildAt(mDragInfo.screen);
            }
            if (cellLayout != null) {
                cellLayout.onDropChild(mDragInfo.cell);
                cellLayout.markCellsAsOccupiedForView(mDragInfo.cell);
            }
            if (target != this) {
                if(mLauncher.getHotseat().checkDragitem(mDragInfo.cell)) {
                    //back to hotseat
                    ItemInfo info = (ItemInfo)mDragInfo.cell.getTag();
                    Log.d(TAG, "sxsexe22-----------> drag not success back to hotseat index " + info.screen);
                    addInHotseat(mDragInfo.cell, info.container, info.screen,
                            info.cellX, info.cellY, info.spanX, info.spanY, info.screen);
                    mLauncher.getHotseat().onDrop(false, 0, d.dragView, mDragInfo.cell, true);
                } else {
                    mLauncher.getDragLayer().animateViewIntoPosition(d.dragView, mDragInfo.cell);
                }
            }
        }

        if (d.cancelled && mDragInfo != null && mDragInfo.cell != null) {
            mDragInfo.cell.setVisibility(VISIBLE);
        }
        
        if (d.cancelled && mDragInfoDelete != null
                && mDragInfoDelete.cell != null) {
            mDragInfoDelete.cell.setVisibility(VISIBLE);
        }
        if (mInEditing) {
            // delete item, check and add delete button.
            if (d.dragSource != target && (target instanceof DeleteDropTarget)) {
                CellLayout cl = (CellLayout) getChildAt(mCurrentPage);
                if (!cl.hasChild()) {
                    cl.addEditBtnContainer();
                    cl.setEditBtnContainerMode(false);
                }
            } else {
                CellLayout targetCl = mDropToLayout;
                // target is a CellLayout, check and remove button container.
                if (targetCl != null && targetCl.hasChild()) {
                    targetCl.removeEditBtnContainer();
                }
                // source is Workspace CellLayout, check and remove button
                // container.
                if (d.dragSource == target && (d.dragSource instanceof Workspace)
                        && (mDragInfo != null)) {
                    CellLayout sourceCl = (CellLayout) getChildAt(mDragInfo.screen);
                    if (sourceCl!=null&&!sourceCl.hasChild()) {//modified by qinjinchuan topwise for bug452(nullpointer protection)
                        sourceCl.addEditBtnContainer();
                        sourceCl.setEditBtnContainerMode(false);
                    }
                }
            }
        }
        //set mDragInfo to null to keep android logic and restore mDragInfo for delete
        mDragInfoDelete = mDragInfo;
        mDragOutline = null;
        mDragInfo = null;

        // Hide the scrolling indicator after you pick up an item
        hideScrollingIndicator(false);
        
        if(!d.isFlingToMove && !mLauncher.isDragToDelete()) {
            removeDragItemFromList((ItemInfo)d.dragInfo);
        }

        // onFling to delete target, checkAndRemoveEmptyCell is called in DeleteDropTarget's ondrop.
        if (d.dragSource != target && !(target instanceof DeleteDropTarget)) {
            checkAndRemoveEmptyCell();
        }
    }
    
    void cleanDragInfo() {
        if(mDragInfo != null && mDragInfo.cell != null) {
            ItemInfo info = (ItemInfo) mDragInfo.cell.getTag();
            View view = getDragItemFromList(info, false);
            if(view != null) {
                return;
            }
        }
        mDragInfoDelete = null;
        mDragOutline = null;
        mDragInfo = null;
    }
    void cleanDragInfoFromFolder() {
        mDragInfoDelete = mDragInfo;
        mDragOutline = null;
        mDragInfo = null;
    }

    void updateItemLocationsInDatabase(CellLayout cl) {
        int count = cl.getShortcutsAndWidgets().getChildCount();

        int screen = indexOfChild(cl);
        int container = Favorites.CONTAINER_DESKTOP;

        if (mLauncher.isHotseatLayout(cl)) {
            screen = -1;
            container = Favorites.CONTAINER_HOTSEAT;
        }

        for (int i = 0; i < count; i++) {
            View v = cl.getShortcutsAndWidgets().getChildAt(i);
            ItemInfo info = (ItemInfo) v.getTag();
            // Null check required as the AllApps button doesn't have an item info
            if (info != null && info.requiresDbUpdate) {
                info.requiresDbUpdate = false;
                LauncherModel.modifyItemInDatabase(mLauncher, info, container, screen, info.cellX,
                        info.cellY, info.spanX, info.spanY);
            }
        }
    }

    @Override
    public boolean supportsFlingToDelete() {
        return true;
    }

    @Override
    public void onFlingToDelete(DragObject d, int x, int y, PointF vec) {
        // Do nothing
    }

    @Override
    public void onFlingToDeleteCompleted() {
        if (mInEditing) {
            CellLayout cl = (CellLayout)getChildAt(mCurrentPage);
            if (!cl.hasChild()) {
                cl.addEditBtnContainer();
                cl.setEditBtnContainerMode(false);
            }
        }
    }

    public boolean isDropEnabled() {
        return true;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        super.onRestoreInstanceState(state);
        Launcher.setScreen(mCurrentPage);
    }

    @Override
    protected void dispatchRestoreInstanceState(SparseArray<Parcelable> container) {
        // We don't dispatch restoreInstanceState to our children using this code path.
        // Some pages will be restored immediately as their items are bound immediately, and 
        // others we will need to wait until after their items are bound.
        mSavedStates = container;
    }

    public void restoreInstanceStateForChild(int child) {
        if (mSavedStates != null) {
            mRestoredPages.add(child);
            CellLayout cl = (CellLayout) getChildAt(child);
            cl.restoreInstanceState(mSavedStates);
        }
    }

    public void restoreInstanceStateForRemainingPages() {
        int count = getChildCount();
        for (int i = 0; i < count; i++) {
            if (!mRestoredPages.contains(i)) {
                restoreInstanceStateForChild(i);
            }
        }
        mRestoredPages.clear();
    }

    @Override
    public void scrollLeft() {
        if (!isSmall() && !mIsSwitchingState) {
            if (isScrollHideseat) {
                mLauncher.getHideseat().scrollLeft();
            } else {
                super.scrollLeft();
            }
        }
        Folder openFolder = getOpenFolder();
        if (openFolder != null) {
            openFolder.completeDragExit();
        }
    }

    @Override
    public void scrollRight() {
        if (!isSmall() && !mIsSwitchingState) {
            if (isScrollHideseat) {
                     mLauncher.getHideseat().scrollRight();
            } else {
                super.scrollRight();
            }
        }
        Folder openFolder = getOpenFolder();
        if (openFolder != null) {
            openFolder.completeDragExit();
        }
    }

    private boolean isScrollHideseat = false;
    @Override
    public boolean onEnterScrollArea(int x, int y, int direction) {
        // Ignore the scroll area if we are dragging over the hot seat
        boolean isPortrait = !LauncherApplication.isScreenLandscape(getContext());
        isScrollHideseat = false;
        if (mLauncher.isHideseatShowing() && isPortrait) {
            Rect r = new Rect();
            mLauncher.getCustomHideseat().getHitRect(r);
            if (r.contains(x, y)) {
                isScrollHideseat = true;
                return true;
            }
        }
        
        if (mLauncher.getHotseat() != null && isPortrait) {
            Rect r = new Rect();
            mLauncher.getHotseat().getHitRect(r);
            if (r.contains(x, y)) {
                return false;
            }
        }

        boolean result = false;
        if (!isSmall() && !mIsSwitchingState) {
            mInScrollArea = true;
            //modified by lixuhui 2015/05/28 fix bug#513: support cycle display CellLayout between head and tail when drag the item 
            int page = getNextPage() +
                       (direction == DragController.SCROLL_LEFT ? -1 : 1);
            if(sContinuousHomeShellFeature){
                if (page >= getChildCount()) {
                    page = getChildCount() - 1;
                }else if(page < 0){
                    page = 0;
                }
            }
            //end

            // We always want to exit the current layout to ensure parity of enter / exit
            setCurrentDropLayout(null);

            if (0 <= page && page < getChildCount()) {
                CellLayout layout = (CellLayout) getChildAt(page);
                setCurrentDragOverlappingLayout(layout);

                // Workspace is responsible for drawing the edge glow on adjacent pages,
                // so we need to redraw the workspace when this may have changed.
                invalidate();
                result = true;
            }
        }
        return result;
    }

    @Override
    public boolean onExitScrollArea() {
        boolean result = false;
        if (mInScrollArea) {
            invalidate();
            CellLayout layout = getCurrentDropLayout();
            setCurrentDropLayout(layout);
            setCurrentDragOverlappingLayout(layout);

            result = true;
            mInScrollArea = false;
        }
        return result;
    }

    private void onResetScrollArea() {
        setCurrentDragOverlappingLayout(null);
        mInScrollArea = false;
    }

    /**
     * Returns a specific CellLayout
     */
    CellLayout getParentCellLayoutForView(View v) {
        ArrayList<CellLayout> layouts = getWorkspaceAndHotseatCellLayouts();
        for (CellLayout layout : layouts) {
            if (layout.getShortcutsAndWidgets().indexOfChild(v) > -1) {
                return layout;
            }
        }
        
        if(mLauncher.getHotseat().checkDragitem(v)) {
            return mLauncher.getHotseat().getCellLayout();
        }
        return null;
    }

    /**
     * Returns a list of all the CellLayouts in the workspace.
     */
    ArrayList<CellLayout> getWorkspaceAndHotseatCellLayouts() {
        ArrayList<CellLayout> layouts = new ArrayList<CellLayout>();
        int screenCount = getChildCount();
        for (int screen = 0; screen < screenCount; screen++) {
            layouts.add(((CellLayout) getChildAt(screen)));
        }
        if (mLauncher.getHotseat() != null) {
            layouts.add(mLauncher.getHotseat().getLayout());
        }

        screenCount = mLauncher.getHideseat().getChildCount();
        for (int screen = 0; screen < screenCount; screen++) {
            layouts.add((CellLayout) mLauncher.getHideseat().getChildAt(screen));
        }

        return layouts;
    }

    /**
     * We should only use this to search for specific children.  Do not use this method to modify
     * ShortcutsAndWidgetsContainer directly. Includes ShortcutAndWidgetContainers from
     * the hotseat and workspace pages
     */
    ArrayList<ShortcutAndWidgetContainer> getAllShortcutAndWidgetContainers() {
        ArrayList<ShortcutAndWidgetContainer> childrenLayouts =
                new ArrayList<ShortcutAndWidgetContainer>();
        int screenCount = getChildCount();
        for (int screen = 0; screen < screenCount; screen++) {
            childrenLayouts.add(((CellLayout) getChildAt(screen)).getShortcutsAndWidgets());
        }
        if (mLauncher.getHotseat() != null) {
            childrenLayouts.add(mLauncher.getHotseat().getLayout().getShortcutsAndWidgets());
        }
        return childrenLayouts;
    }

    public Folder getFolderForTag(Object tag) {
        ArrayList<ShortcutAndWidgetContainer> childrenLayouts =
                getAllShortcutAndWidgetContainers();
        for (ShortcutAndWidgetContainer layout: childrenLayouts) {
            int count = layout.getChildCount();
            for (int i = 0; i < count; i++) {
                View child = layout.getChildAt(i);
                if (child instanceof Folder) {
                    Folder f = (Folder) child;
                    if (f.getInfo() == tag && f.getInfo().opened) {
                        return f;
                    }
                }
            }
        }
        return null;
    }

    public View getViewForTag(Object tag) {
        ArrayList<ShortcutAndWidgetContainer> childrenLayouts =
                getAllShortcutAndWidgetContainers();
        for (ShortcutAndWidgetContainer layout: childrenLayouts) {
            int count = layout.getChildCount();
            for (int i = 0; i < count; i++) {
                View child = layout.getChildAt(i);
                if (child.getTag() == tag) {
                    return child;
                }
            }
        }
        return null;
    }

    void clearReference() {
        ArrayList<ShortcutAndWidgetContainer> childrenLayouts =
                getAllShortcutAndWidgetContainers();
        for (ShortcutAndWidgetContainer layout: childrenLayouts) {
            int childCount = layout.getChildCount();
            for (int j = 0; j < childCount; j++) {
                View v = layout.getChildAt(j);
                if (v instanceof DropTarget) {
                    mDragController.removeDropTarget((DropTarget) v);
                }

                if (v instanceof FolderIcon) {
                    mLauncher.removeFolder((FolderInfo)v.getTag());
                }
            }
        }
    }

    // Removes ALL items that match a given package name, this is usually called when a package
    // has been removed and we want to remove all components (widgets, shortcuts, apps) that
    // belong to that package.
    void removeItemsByPackageName(final ArrayList<String> packages) {
        HashSet<String> packageNames = new HashSet<String>();
        packageNames.addAll(packages);

        // Just create a hash table of all the specific components that this will affect
        HashSet<ComponentName> cns = new HashSet<ComponentName>();
        ArrayList<CellLayout> cellLayouts = getWorkspaceAndHotseatCellLayouts();
        for (CellLayout layoutParent : cellLayouts) {
            ViewGroup layout = layoutParent.getShortcutsAndWidgets();
            int childCount = layout.getChildCount();
            for (int i = 0; i < childCount; ++i) {
                View view = layout.getChildAt(i);
                Object tag = view.getTag();
                //NullPointer Exception fix
                if (tag instanceof ShortcutInfo) {
                    ShortcutInfo info = (ShortcutInfo) tag;
                    if ((info == null) || (info.intent == null) || (info.intent.getComponent() == null)) {
                        continue;
                    }
                    ComponentName cn = info.intent.getComponent();
                    if ((cn != null) && packageNames.contains(cn.getPackageName())) {
                        cns.add(cn);
                    }
                } else if (tag instanceof FolderInfo) {
                    FolderInfo info = (FolderInfo) tag;
                     if (info == null) {
                        continue;
                    }
                    for (ShortcutInfo s : info.contents) {
                        if ((s == null) || (s.intent == null)) {
                            continue;
                        }
                        ComponentName cn = s.intent.getComponent();
                        if ((cn != null) && packageNames.contains(cn.getPackageName())) {
                            cns.add(cn);
                        }
                    }
                } else if (tag instanceof LauncherAppWidgetInfo) {
                    LauncherAppWidgetInfo info = (LauncherAppWidgetInfo) tag;
                    if ((info == null) || ( info.providerName == null)) {
                        continue;
                    }
                    ComponentName cn = info.providerName;
                    if ((cn != null) && packageNames.contains(cn.getPackageName())) {
                        cns.add(cn);
                    }
                }
            }
        }

        // Remove all the things
        removeItemsByComponentName(cns);
    }

    /**
     * Clean up all gadgets in workspace. This method should be called when
     * launcher is being destroyed to avoid memory leak.
     */
    /*void cleanUpAllGadgets() {
        for (CellLayout layout : getWorkspaceAndHotseatCellLayouts()) {
            ViewGroup container = layout.getShortcutAndWidgetContainer();
            int childCount = container.getChildCount();
            for (int i = 0; i < childCount; i++) {
                View view = container.getChildAt(i);
                if (view instanceof GadgetView) {
                    ((GadgetView) view).cleanUp();
                }
            }
        }
    }*/

    void removeItemsByPackageNameForAppUninstall(final ArrayList<String> packages) {
        HashSet<String> packageNames = new HashSet<String>();
        packageNames.addAll(packages);

        // Just create a hash table of all the specific components that this will affect
        HashSet<ComponentName> cns = new HashSet<ComponentName>();
        ArrayList<CellLayout> cellLayouts = getWorkspaceAndHotseatCellLayouts();
        for (CellLayout layoutParent : cellLayouts) {
            ViewGroup layout = layoutParent.getShortcutsAndWidgets();
            int childCount = layout.getChildCount();
            for (int i = 0; i < childCount; ++i) {
                View view = layout.getChildAt(i);
                Object tag = view.getTag();
                //NullPointer Exception fix
                if (tag instanceof ShortcutInfo) {
                    ShortcutInfo info = (ShortcutInfo) tag;
                    if ((info == null) || (info.intent == null) || (info.intent.getComponent() == null)) {
                        continue;
                    }
                    ComponentName cn = info.intent.getComponent();
                    if ((cn != null) && packageNames.contains(cn.getPackageName())) {
                        cns.add(cn);
                    }
                } else if (tag instanceof FolderInfo) {
                    FolderInfo info = (FolderInfo) tag;
                     if (info == null) {
                        continue;
                    }
                    for (ShortcutInfo s : info.contents) {
                        if ((s == null) || (s.intent == null)) {
                            continue;
                        }
                        ComponentName cn = s.intent.getComponent();
                        if ((cn != null) && packageNames.contains(cn.getPackageName())) {
                            cns.add(cn);
                        }
                    }
                } else if (tag instanceof LauncherAppWidgetInfo) {
                    LauncherAppWidgetInfo info = (LauncherAppWidgetInfo) tag;
                    if ((info == null) || ( info.providerName == null)) {
                        continue;
                    }
                    ComponentName cn = info.providerName;
                    if ((cn != null) && packageNames.contains(cn.getPackageName())) {
                        cns.add(cn);
                    }
                }
            }
        }

        // Remove all the things
        if (cns.isEmpty() == true) {
            mLauncher.getModel().deleteItemsInDatabaseByPackageName(packages);
        } else {
            removeItemsByComponentName(cns);
        }
    }

    // Removes items that match the application info specified, when applications are removed
    // as a part of an update, this is called to ensure that other widgets and application
    // shortcuts are not removed.
    void removeItemsByApplicationInfo(final ArrayList<ApplicationInfo> appInfos) {
        // Just create a hash table of all the specific components that this will affect
        HashSet<ComponentName> cns = new HashSet<ComponentName>();
        for (ApplicationInfo info : appInfos) {
            cns.add(info.componentName);
        }

        // Remove all the things
        removeItemsByComponentName(cns);
    }

    //find and remove one item folder after all restore app handled by appstore
    void removeItemsViewByItemInfo(final ArrayList<ItemInfo> items) {
        ArrayList<CellLayout> cellLayouts = getWorkspaceAndHotseatCellLayouts();
        for (final CellLayout layoutParent: cellLayouts) {
            final ViewGroup layout = layoutParent.getShortcutsAndWidgets();

            // Avoid ANRs by treating each screen separately
            post(new Runnable() {
                public void run() {
                    final ArrayList<View> childrenToRemove = new ArrayList<View>();
                    childrenToRemove.clear();

                    int childCount = layout.getChildCount();
                    ArrayList<ItemInfo> itemsToRemove = new ArrayList<ItemInfo>();
                    for (ItemInfo item: items) {
                        for (int j = 0; j < childCount; j++) {
                            final View view = layout.getChildAt(j);
                            Object tag = view.getTag();
                            final long id = ((ItemInfo)tag).id;
                            int itemtype = ((ItemInfo)tag).itemType;

                            if (id == item.id) {
                                Log.d(TAG, "find same id " + id);
                                childrenToRemove.add(view);
                                itemsToRemove.add(item);
                                continue;
                            } else if (itemtype == Favorites.ITEM_TYPE_FOLDER) {
                                ArrayList<View> childviews = (((FolderIcon)view).getFolder()).getItemsInReadingOrder();
                                for (View bubbleview : childviews){
                                    if (item.id == ((ItemInfo)(bubbleview.getTag())).id) {
                                        Log.d(TAG, "find in folder same id " + item.id);
                                        childrenToRemove.add(bubbleview);
                                        itemsToRemove.add((ItemInfo)(bubbleview.getTag()));
                                        continue;
                                    }
                                }
                            }
                        }
                    }

                    childCount = childrenToRemove.size();
                    CellLayout cellLayout = null;
                    boolean dockHit = false;
                    for (int j = 0; j < childCount; j++) {
                        View child = childrenToRemove.get(j);
                        // Note: We can not remove the view directly from CellLayoutChildren as this
                        // does not re-mark the spaces as unoccupied.
                        ItemInfo info = (ItemInfo)child.getTag();
                        cellLayout = (CellLayout) getChildAt(info.screen);
                        if (cellLayout != null) {
                            cellLayout.removePendingFlingDropDownItem(child);
                        }

                        if(info.container == LauncherSettings.Favorites.CONTAINER_HOTSEAT) {
                            dockHit = true;
                        }

                        layoutParent.removeViewInLayout(child);
                        if (child instanceof DropTarget) {
                            mDragController.removeDropTarget((DropTarget)child);
                        }

                        if (info.container == Favorites.CONTAINER_HIDESEAT) {
                            mLauncher.getHideseat().readOrderOnRemoveItem(info);
                        }

                        if (info.itemType == Favorites.ITEM_TYPE_FOLDER) {
                            mLauncher.removeFolder((FolderInfo) info);
                        }
                    }

                    if (childCount > 0) {
                        layout.requestLayout();
                        layout.invalidate();
                        if(dockHit) {
                            mLauncher.getHotseat().requestLayout();
                        }
                    }
                    for(ItemInfo item: itemsToRemove) {
                        items.remove(item);
                    }
                }
            });
        }
    }

    void removeItemsByItemInfo(final ArrayList<ItemInfo> items) {
        ArrayList<CellLayout> cellLayouts = getWorkspaceAndHotseatCellLayouts();
        for (final CellLayout layoutParent: cellLayouts) {
            final ViewGroup layout = layoutParent.getShortcutsAndWidgets();

            // Avoid ANRs by treating each screen separately
            post(new Runnable() {
                public void run() {
                    final ArrayList<View> childrenToRemove = new ArrayList<View>();

                    int childCount = layout.getChildCount();
                    ArrayList<ItemInfo> itemsToRemove = new ArrayList<ItemInfo>();
                    for (ItemInfo item: items) {
                        for (int j = 0; j < childCount; j++) {
                            final View view = layout.getChildAt(j);
                            Object tag = view.getTag();
                            final long id = ((ItemInfo)tag).id;
                            int itemtype = ((ItemInfo)tag).itemType;

                            if (id == item.id) {
                                Log.d(TAG, "find same id " + id);
                                childrenToRemove.add(view);
                                itemsToRemove.add(item);
                                final ItemInfo finalitem = item;
                                LauncherModel.deleteItemFromDatabase(mLauncher, finalitem);
                                continue;
                            } else if (itemtype == Favorites.ITEM_TYPE_FOLDER) {
                                ArrayList<View> childviews = (((FolderIcon)view).getFolder()).getItemsInReadingOrder();
                                for (View bubbleview : childviews){
                                    if (item.id == ((ItemInfo)(bubbleview.getTag())).id) {
                                        Log.d(TAG, "find in folder same id " + item.id);
                                        childrenToRemove.add(bubbleview);
                                        itemsToRemove.add((ItemInfo)(bubbleview.getTag()));
                                        final ItemInfo finalitem = item;
                                        LauncherModel.deleteItemFromDatabase(mLauncher, finalitem);
                                        continue;
                                    }
                                }
                            }
                        }
                    }

                    childCount = childrenToRemove.size();
                    CellLayout cellLayout = null;
                    boolean dockHit = false;
                    for (int j = 0; j < childCount; j++) {
                        View child = childrenToRemove.get(j);
                        // Note: We can not remove the view directly from CellLayoutChildren as this
                        // does not re-mark the spaces as unoccupied.
                        ItemInfo info = (ItemInfo)child.getTag();
                        cellLayout = (CellLayout) getChildAt(info.screen);
                        if (cellLayout == null) {
                            continue;
                        }
                        cellLayout.removePendingFlingDropDownItem(child);
                        if(info.container == LauncherSettings.Favorites.CONTAINER_HOTSEAT) {
                            dockHit = true;
                        }

                        layoutParent.removeViewInLayout(child);
                        if (child instanceof DropTarget) {
                            mDragController.removeDropTarget((DropTarget)child);
                        }

                        if (info.container == Favorites.CONTAINER_HIDESEAT) {
                            mLauncher.getHideseat().readOrderOnRemoveItem(info);
                        }
                    }

                    if (childCount > 0) {
                        layout.requestLayout();
                        layout.invalidate();
                        if(dockHit) {
                            mLauncher.getHotseat().requestLayout();
                        }
                    }
                    for(ItemInfo item: itemsToRemove) {
                        items.remove(item);
                    }
                }
            });
        }
    }


    void removeItemsByComponentName(final HashSet<ComponentName> componentNames) {
        ArrayList<CellLayout> cellLayouts = getWorkspaceAndHotseatCellLayouts();
        for (final CellLayout layoutParent: cellLayouts) {
            final ViewGroup layout = layoutParent.getShortcutsAndWidgets();

            // Avoid ANRs by treating each screen separately
            post(new Runnable() {
                public void run() {
                    final ArrayList<View> childrenToRemove = new ArrayList<View>();
                    childrenToRemove.clear();

                    int childCount = layout.getChildCount();
                    for (int j = 0; j < childCount; j++) {
                        final View view = layout.getChildAt(j);
                        Object tag = view.getTag();

                        if (tag instanceof ShortcutInfo) {
                            final ShortcutInfo info = (ShortcutInfo) tag;
                            final Intent intent = info.intent;
                            ComponentName name = null;
                            if (intent != null) {
                                name = intent.getComponent();
                            }
                            if (name != null) {
                                if (componentNames.contains(name)) {
                                    LauncherModel.deleteItemFromDatabase(mLauncher, info);
                                    LauncherModel.deleteItemFromAnotherTable(mLauncher, info);
                                    childrenToRemove.add(view);
                                }
                            }
                        } else if (tag instanceof FolderInfo) {
                            final FolderInfo info = (FolderInfo) tag;
                            final ArrayList<ShortcutInfo> contents = info.contents;
                            final int contentsCount = contents.size();
                            final ArrayList<ShortcutInfo> appsToRemoveFromFolder =
                                    new ArrayList<ShortcutInfo>();

                            for (int k = 0; k < contentsCount; k++) {
                                final ShortcutInfo appInfo = contents.get(k);
                                if(appInfo.isEditFolderShortcut()) {
                                	continue;
                                }
                                final Intent intent = appInfo.intent;
                                final ComponentName name = intent.getComponent();

                                if (name != null) {
                                    if (componentNames.contains(name)) {
                                        appsToRemoveFromFolder.add(appInfo);
                                    }
                                }
                            }
                            for (ShortcutInfo item: appsToRemoveFromFolder) {
                                info.remove(item);
                                LauncherModel.deleteItemFromDatabase(mLauncher, item);
                                LauncherModel.deleteItemFromAnotherTable(mLauncher, item);
                            }
                        } else if (tag instanceof LauncherAppWidgetInfo) {
                            final LauncherAppWidgetInfo info = (LauncherAppWidgetInfo) tag;
                            final ComponentName provider = info.providerName;
                            if (provider != null) {
                                if (componentNames.contains(provider)) {
                                    LauncherModel.deleteItemFromDatabase(mLauncher, info);
                                    childrenToRemove.add(view);
                                }
                            }
                        }
                    }

                    childCount = childrenToRemove.size();
                    CellLayout cellLayout = null;
                    boolean dockHit = false;
                    for (int j = 0; j < childCount; j++) {
                        View child = childrenToRemove.get(j);
                        // Note: We can not remove the view directly from CellLayoutChildren as this
                        // does not re-mark the spaces as unoccupied.
                        ItemInfo info = (ItemInfo)child.getTag();

                        //icon in hotseat doesn't removed after uninstall from app store
                        if (info.container == LauncherSettings.Favorites.CONTAINER_HOTSEAT) {
                            cellLayout = mLauncher.getHotseat().getLayout();
                        } else if (info.container == LauncherSettings.Favorites.CONTAINER_HIDESEAT) {
                            cellLayout = (CellLayout) mLauncher.getHideseat().getChildAt(info.screen);
                        } else {
                            cellLayout = (CellLayout) getChildAt(info.screen);
                        }
                        // there is a nullpointerExcetion
                        if (cellLayout == null) {
                            continue;
                        }
                        cellLayout.removePendingFlingDropDownItem(child);
                        if(info.container == LauncherSettings.Favorites.CONTAINER_HOTSEAT) {
                            dockHit = true;
                        }
                        
                        layoutParent.removeViewInLayout(child);
                        if (child instanceof DropTarget) {
                            mDragController.removeDropTarget((DropTarget)child);
                        }

                        if (info.container == Favorites.CONTAINER_HIDESEAT) {
                            mLauncher.getHideseat().readOrderOnRemoveItem(info);
                        }
                    }

                    if (childCount > 0) {
                        layout.requestLayout();
                        layout.invalidate();
                        if(dockHit) {
                            mLauncher.getHotseat().requestLayout();
                            mLauncher.getHotseat().initViewCacheList();
                        }
                    }
                }
            });
        }

        // Clean up new-apps animation list
        final Context context = getContext();
        post(new Runnable() {
            @Override
            public void run() {
                String spKey = LauncherApplication.getSharedPreferencesKey();
                SharedPreferences sp = context.getSharedPreferences(spKey,
                        Context.MODE_PRIVATE);
                Set<String> newApps = sp.getStringSet(InstallShortcutReceiver.NEW_APPS_LIST_KEY,
                        null);

                // Remove all queued items that match the same package
                if (newApps != null) {
                    synchronized (newApps) {
                        Iterator<String> iter = newApps.iterator();
                        while (iter.hasNext()) {
                            try {
                                Intent intent = Intent.parseUri(iter.next(), 0);
                                if (componentNames.contains(intent.getComponent())) {
                                    iter.remove();
                                }

                                // It is possible that we've queued an item to be loaded, yet it has
                                // not been added to the workspace, so remove those items as well.
                                ArrayList<ItemInfo> shortcuts;
                                shortcuts = LauncherModel.getWorkspaceShortcutItemInfosWithIntent(
                                        intent);
                                for (ItemInfo info : shortcuts) {
                                    LauncherModel.deleteItemFromDatabase(context, info);
                                }
                            } catch (URISyntaxException e) {}
                        }
                    }
                }
            }
        });
    }

    void updateShortcuts(ArrayList<ApplicationInfo> apps) {
        ArrayList<ShortcutAndWidgetContainer> childrenLayouts = getAllShortcutAndWidgetContainers();
        IconManager iconManager = ((LauncherApplication)mContext.getApplicationContext()).getIconManager();
        for (ShortcutAndWidgetContainer layout: childrenLayouts) {
            int childCount = layout.getChildCount();
            for (int j = 0; j < childCount; j++) {
                final View view = layout.getChildAt(j);
                Object tag = view.getTag();
                if (tag instanceof ShortcutInfo) {
                    ShortcutInfo info = (ShortcutInfo) tag;
                    // We need to check for ACTION_MAIN otherwise getComponent() might
                    // return null for some shortcuts (for instance, for shortcuts to
                    // web pages.)
                    final Intent intent = info.intent;
                    if (intent == null) {
                        continue;
                    }
                    final ComponentName name = intent.getComponent();
                    if (info.itemType == LauncherSettings.Favorites.ITEM_TYPE_APPLICATION &&
                            Intent.ACTION_MAIN.equals(intent.getAction()) && name != null) {
                        final int appCount = apps.size();
                        for (int k = 0; k < appCount; k++) {
                            ApplicationInfo app = apps.get(k);
                            if (app.componentName.equals(name)) {
                                BubbleTextView shortcut = (BubbleTextView) view;
                                Context context = getContext().getApplicationContext();
                                Drawable icon = iconManager.getAppUnifiedIcon(info,null);
                                info.setIcon(icon);
                                info.title = app.title.toString();
                                shortcut.applyFromShortcutInfo(info);
                            }
                        }
                    }
                }
            }
        }
    }

    public void removeWidgetsByShortcuts(Collection<ShortcutInfo> shortcuts) {
        if (shortcuts == null || shortcuts.isEmpty()) return;
        Set<String> pkgNames = new HashSet<String>();
        for (ShortcutInfo item : shortcuts) {
            if (item.intent == null) continue;
            ComponentName cmpt = item.intent.getComponent();
            if (cmpt == null || TextUtils.isEmpty(cmpt.getPackageName())) continue;
            pkgNames.add(cmpt.getPackageName());
        }
        removeWidgetsByPackageNames(pkgNames);
    }

    private void removeWidgetsByPackageNames(Collection<String> packageNames) {
        if (packageNames == null || packageNames.isEmpty()) return;
        Map<ItemInfo, View> itemsToRemove = new HashMap<ItemInfo, View>();
        for (ShortcutAndWidgetContainer layout: getAllShortcutAndWidgetContainers()) {
            for (int j = 0; j < layout.getChildCount(); j++) {
                final View view = layout.getChildAt(j);
                Object tag = view.getTag();
                if (tag instanceof LauncherAppWidgetInfo) {
                    LauncherAppWidgetInfo info = (LauncherAppWidgetInfo) tag;
                    String pkgName = info.providerName != null ? info.providerName.getPackageName() : null;
                    if (!TextUtils.isEmpty(pkgName) && packageNames.contains(pkgName)) {
                        itemsToRemove.put(info, view);
                    }
                }
            }
        }
        if (itemsToRemove.isEmpty()) return;
        for (ItemInfo item : itemsToRemove.keySet()) {
            Log.v(TAG, "prepare to remove frozen widget: " + item.title);
        }
        removeWidgets(itemsToRemove);
        itemsToRemove.clear();
        itemsToRemove = null;
    }

    private void removeWidgets(Map<ItemInfo, View> itemsToRemove) {
        if (itemsToRemove == null || itemsToRemove.isEmpty()) return;
        Log.d(TAG, "removeWidgets begin");
        // remove from database
        for (Entry<ItemInfo, View> entry : itemsToRemove.entrySet()) {
            ItemInfo item = entry.getKey();
            if (item instanceof LauncherAppWidgetInfo) {
                LauncherModel.deleteItemFromDatabase(mLauncher, item);
            }
        }
        // remove from workspace
        for (Entry<ItemInfo, View> entry : itemsToRemove.entrySet()) {
            ItemInfo item = entry.getKey();
            View view = entry.getValue();
            if (item instanceof LauncherAppWidgetInfo) {
                CellLayout layout = (CellLayout) getChildAt(item.screen);
                if (item.container != Favorites.CONTAINER_DESKTOP) {
                    Log.w(TAG, "removeWidgets incorrect state: item.container=" + item.container);
                    continue;
                }
                if (layout == null) {
                    Log.w(TAG, "removeWidgets incorrect state: item.screen=" + item.container);
                    continue;
                }
                layout.removePendingFlingDropDownItem(view);
                layout.removeViewInLayout(view);
                if (view instanceof DropTarget) {
                    mDragController.removeDropTarget((DropTarget) view);
                }
                layout.requestLayout();
                layout.invalidate();
            }
        }
        Log.d(TAG, "removeWidgets end");
    }

    void moveToDefaultScreen(boolean animate) {
        if (!isSmall()) {
            if (animate) {
                //modified by lixuhui 2015/05/29 fix bug#649, double click home key cause FC after switch language completed
                int page = Math.max(0, Math.min(mDefaultPage, getPageCount() - 1));
                snapToPage(page);
                setCurrentPage(page);
                //end
            } else {
                setCurrentPage(mDefaultPage);
            }
        }
        if (getChildAt(mDefaultPage) != null) {
            getChildAt(mDefaultPage).requestFocus();
        }
    }

    @Override
    protected String getCurrentPageDescription() {
        int page = (mNextPage != INVALID_PAGE) ? mNextPage : mCurrentPage;
        return String.format(getContext().getString(R.string.workspace_scroll_format),
                page + 1, getChildCount());
    }

    public void getLocationInDragLayer(int[] loc) {
        mLauncher.getDragLayer().getLocationInDragLayer(this, loc);
    }

    void setFadeForOverScroll(float fade) {
        if (!isScrollingIndicatorEnabled()) return;

        mOverscrollFade = fade;
        float reducedFade = 0.5f + 0.5f * (1 - fade);
        final ViewGroup parent = (ViewGroup) getParent();
        final ImageView qsbDivider = (ImageView) (parent.findViewById(R.id.qsb_divider));
        final ImageView dockDivider = (ImageView) (parent.findViewById(R.id.dock_divider));
        //final View scrollIndicator = getScrollingIndicator();

        cancelScrollingIndicatorAnimations();
        if (qsbDivider != null) qsbDivider.setAlpha(reducedFade);
        if (dockDivider != null) dockDivider.setAlpha(reducedFade);
        //scrollIndicator.setAlpha(1 - fade);
    }
    
    private final Runnable mAddEmptyScreen = new Runnable () {
        @Override
        public void run() {
            Log.d(TAG, "sxsexe---->mAddEmptyScreen run add new screen ");
            
            if(getChildCount() >= ConfigManager.getScreenMaxCount() || mEmptyScreenAdded) {
                //Toast.makeText(mLauncher, R.string.toast_max_screen_count, Toast.LENGTH_SHORT).show();
                return;
            }
            View view = View.inflate(getContext(), R.layout.workspace_screen, Workspace.this);
            if(view != null && mLongClickListener != null) {
                view.setOnLongClickListener(mLongClickListener);
            }
            invalidatePageIndicator(true);
            mEmptyScreenAdded = true;

            // newly added celllayout must enter hideseatmode if hideseat is open
            CellLayout currentLayout = (CellLayout)getChildAt(getCurrentPage());
            CellLayout lastAddLayout = (CellLayout)getChildAt(getChildCount()-1);
            if( currentLayout != null && lastAddLayout != null && currentLayout.isHideseatOpen ){
                lastAddLayout.enterHideseatMode();
            }
        }
    };

    public void addEmptyScreen() {
        removeCallbacks(mAddEmptyScreen);
        post(mAddEmptyScreen);
    }
    // batch operations to the icons in folder
    public boolean addEmptyScreenSync() {
        if(getChildCount() >= ConfigManager.getScreenMaxCount() || mEmptyScreenAdded) {
            return false;
        }
        View view = View.inflate(getContext(), R.layout.workspace_screen, Workspace.this);
        if(view != null && mLongClickListener != null) {
            view.setOnLongClickListener(mLongClickListener);
        }
        //invalidatePageIndicator(true);
        mEmptyScreenAdded = true;

        // newly added celllayout must enter hideseatmode if hideseat is open
        CellLayout currentLayout = (CellLayout)getChildAt(getCurrentPage());
        CellLayout lastAddLayout = (CellLayout)getChildAt(getChildCount()-1);
        if( currentLayout != null && lastAddLayout != null && currentLayout.isHideseatOpen ){
            lastAddLayout.enterHideseatMode();
        }
        return true;
    }

    private final Runnable mRemoveEmptyCell = new Runnable (){

        @Override
        public void run() {
            if (isPlayingAnimation()) {
                removeCallbacks(mRemoveEmptyCell);
                postDelayed(mRemoveEmptyCell, 100);
                return;
            }
            if (mLauncher.getModel().isLoadingWorkspace()) {
                return;
            }

            if (mLauncher.isEmptyCellCanBeRemoved()) {
                Log.d(TAG, "can not remove empty cell!!!");
                return;
            }

            if((mLauncher.getDragController() != null) && ( mLauncher.getDragController().isDragging() == true)) {
                return;
            }
            final int count = getChildCount();
            
            if(count <= 1 && mWindowToken != null) {
                makesureAddScreenIndex(0);
                return;
            }

            if((mLauncher.getModel() != null) &&
                (mLauncher.getModel().isLoadingWorkspace() == true)) {
                return;
            }

            int start = 0;
            if((mLauncher.getModel() != null) &&
                (mLauncher.getModel().isEmptyCellCanBeRemoved() == false)) {
                Log.d(TAG, "in workspace loading, can not empty remove");
                //only check the last cell
                if (mEmptyScreenAdded == true) {
                    start = count - 1;
                } else {
                    start = count;
                }
            }
            for(int i = start; i < count; i++) {
                View child = getChildAt(i);
                if (getChildCount() <= 1) {
                    break;
                }
                if(child != null && child instanceof CellLayout) {
                    //boolean isAllChildGone = isAllChildUnvisiable((CellLayout)child);
                    if(((CellLayout)child).hasChild() == false /*|| isAllChildGone*/
                            && !isWidgetPageView(child)) {
                        Log.d(TAG, "sxsexe---->mRemoveEmptyCell removeViewAt " + i + " count " + count);
                        
                        ((CellLayout)getChildAt(i)).cancelFlingDropDownAnimation();
                        ((CellLayout)getChildAt(i)).onRemove();
                        removeViewAt(i);
                        invalidatePageIndicator(true);
                        if(mCurrentPage > i) {
                            mCurrentPage--;
                        }
                        if (i < (count - 1)) {
                            LauncherModel.checkEmptyScreen(mContext.getApplicationContext(), i);
                        }
                        if(!isPageMoving()){
                            setCurrentPage(mCurrentPage);
                            onPageEndMoving();
                        }
                        i--;
                    }
                }
            }
            mEmptyScreenAdded = false;
            mDropTargetView = null;
        }
    };
    
    public void checkAndRemoveEmptyCell() {
        if (isPlayingAnimation()) {
            return;
        }
       
        // empty screen is useful, and delay check empty screen.
        if (mLauncher.isEmptyCellCanBeRemoved()) {
            return;
        }

        if(isPageMoving()) {
            return;
        }

        if (mLauncher.getModel().isLoadingWorkspace()) {
            return;
        }
        if (mEditModeFeatrueFlag) {
            mEmptyScreenAdded = false;
            mDropTargetView = null;
            return;
        }

        removeCallbacks(mRemoveEmptyCell);
        post(mRemoveEmptyCell);
    }
    
/*    public void bindRemoveScreen(final int screen) {
        post(new Runnable() {
            
            @Override
            public void run() {
                Log.d(TAG, "sxsexe---->bindRemoveScreen removeViewAt " + screen + " getChildCount() " + getChildCount());
                if(screen >= getChildCount()) {
                    return;
                }
                
                for(int i = screen; i < getChildCount(); i ++) {
                    View child = getChildAt(i);
                    
                    if(child != null && child instanceof CellLayout) {
                        if(((CellLayout)child).hasChild() == false) {
                            removeViewAt(i);
                            if(mCurrentPage > i)
                                mCurrentPage--;
                            setCurrentPage(mCurrentPage);
                            i--;
                        }
                    }
                }
            }
        });
    }*/

    public int getEmptyPageCount(){
        int normalPageCount = getNormalScreenCount();
        int emptyCount = 0;
        for(int i = 0; i < normalPageCount; i++){
            CellLayout cellLayout = (CellLayout)getChildAt(i);
            if(cellLayout != null && cellLayout.hasChild()){
                continue;
            }
            emptyCount ++;
        }
        return emptyCount;
    }
    
    public void makesureAddScreenIndex(int screen) {
        CellLayout currentPage = (CellLayout) getChildAt(getCurrentPage());
        boolean hideseatMode = currentPage != null ? currentPage.isHideseatOpen : false;
        makeSureWidgetPages();
        while(screen >= getNormalScreenCount()) {
            Log.d(TAG, "sxsexe------>makesureAddScreenIndex screen " + screen + " childCount " + getChildCount());
            View view = View.inflate(getContext(), R.layout.workspace_screen, null);
            if(view != null && mLongClickListener != null) {
            	addView(view, getNormalScreenCount());
                view.setOnLongClickListener(mLongClickListener);
            }

            invalidatePageIndicator(true);

            if (hideseatMode) {
                CellLayout lastPage = (CellLayout) getChildAt(getChildCount() - 1);
                lastPage.enterHideseatMode();
            }
        }
    }
    
    public void addToDragItems(View dragItem) {
        if(mDragItems == null)
            mDragItems = new ArrayList<View>();
        
        ItemInfo info = (ItemInfo)dragItem.getTag();
        
        //clear the same view of which parent has changed
        for(View cell : mDragItems) {
            ItemInfo tmpInfo = (ItemInfo)cell.getTag();
            if(info.id == tmpInfo.id) {
                mDragItems.remove(cell);
                break;
            }
        }
        
        Log.d(TAG, "sxsexe----------------->addToDragItems cell " + info + " parent " + dragItem.getParent());
        mDragItems.add(dragItem);
    }

    public View searchDragItemFromList(ItemInfo itemInfo) {
        if(mDragItems != null && mDragItems.size() > 0) {
            for(View cell : mDragItems) {
                ItemInfo info = (ItemInfo)cell.getTag();
                if(info.id == itemInfo.id) {
                    Log.d(TAG, "----------------->find DragItemFromList info " + info.title);
                    return cell;
                }
            }
        }
        Log.d(TAG, "----------------->no find in DragItemFromList");
        return null;
    }

    public View getDragItemFromList(ItemInfo itemInfo, boolean remove) {
        if(mDragItems != null && mDragItems.size() > 0) {
            for(View cell : mDragItems) {
                ItemInfo info = (ItemInfo)cell.getTag();
                if(info.id == itemInfo.id) {
                    if(remove) {
                        mDragItems.remove(cell);
                    }
                    Log.d(TAG, "sxsexe111----------------->get DragItemFromList info " + info.title);
                    return cell;
                }
            }
        }
        if(mDragInfo != null) {
            return mDragInfo.cell;
        } else {
            Log.e(TAG, "sxsexe------------------->mDragInfo is null and gragList is empty itemInfo " + itemInfo);
            return null;
        }
    }
    
    public void removeDragItemFromList(ItemInfo itemInfo) {
        if(mDragItems != null && mDragItems.size() > 0) {
            for(View tmpCell : mDragItems) {
                ItemInfo info = (ItemInfo)tmpCell.getTag();
                if(info.id == itemInfo.id) {
                    Log.d(TAG, "sxsexe111----------------->remove DragItemFromList info " + info.title + " parent " + tmpCell.getParent());
                    mDragItems.remove(tmpCell);
                    return;
                }
            }
        }
    }
    
    public void cleanDragItemList() {
        if(mDragItems != null && mDragItems.size() > 0) {
            mDragItems.clear();
            mDragItems = null;
        }
    }
    
    /**
     * you can use this method to set unlockAniamtion disable or enable
     * @param flag new value
     */
    public void setUnlockAnimationEnable(boolean flag) {
        mUnlockAnimationEnable = flag;
    }
    
    /**
     * @return Is it playing unlock Animation 
     */
    public boolean isPlayingAnimation() {
        return LockScreenAnimator.getInstance(mLauncher).isRuning();
//        return mIsPlayUnlockAniamtion;
    }
    
    public void setIsPlayingUnlockAnimation(boolean isPlaying){
        mIsPlayUnlockAniamtion = isPlaying;
    }

    
    /**
     * to play unlock animation
     */
    public void playUnlockAnimation() {
        if (!mUnlockAnimationEnable)
            return;
        mIsPlayUnlockAniamtion = true;
        final View indicator = mLauncher.getIndicatorView();
        indicator.setVisibility(INVISIBLE);
        
        // get darglayer and set it disable
        final DragLayer root = mLauncher.getDragLayer();
        root.setTouchEnable(false);
        
        // create a new AnimatorSet to play animations together
        mAnimatorSet = new AnimatorSet();
        
        // create lists to store some statuses
        final ArrayList<Animator> items = new ArrayList<Animator>();
        final ArrayList<View> recordViews = new ArrayList<View>();
        final ArrayList<CellLayout.LayoutParams> recordParams = new ArrayList<CellLayout.LayoutParams>();
        
        // 
        int currentPageIndex = getCurrentPage();
        final CellLayout currentPage = (CellLayout) getChildAt(currentPageIndex);
        final ShortcutAndWidgetContainer container = currentPage.getShortcutAndWidgetContainer();
        
        final int childCount = container.getChildCount();
        
        Rect relative = new Rect();
        root.getViewRectRelativeToSelf(container, relative);
        final float deltaX = relative.left;
        final float deltaY = relative.top;

        for(int i=childCount-1;i>=0;i--) {
            final View child = container.getChildAt(i);
            CellLayout.LayoutParams layoutParams = (CellLayout.LayoutParams) child.getLayoutParams();
            child.setVisibility(INVISIBLE);

            recordViews.add(child);
            recordParams.add(layoutParams);
            
            container.removeViewAt(i);
            DragLayer.LayoutParams rootLayoutParams = getRootLayoutParams(layoutParams);
            root.addView(child, rootLayoutParams);
            child.setX(child.getX() + deltaX);
            child.setY(child.getY() + deltaY);
            items.addAll(createAnimators(child, layoutParams.cellX, layoutParams.cellY));
        }
        
        CellLayout hotseat = mLauncher.getHotseat().getLayout();
        ShortcutAndWidgetContainer hotseatContainer = hotseat.getShortcutAndWidgetContainer();
        int hotseatChildCount = hotseatContainer.getChildCount();
        for(int i=0;i<hotseatChildCount;i++) {
            final View child = hotseatContainer.getChildAt(i);
            child.setVisibility(INVISIBLE);
            items.add(createMoveUpAnimator(child));
        }
        
        items.add(createFadeInAnimator(indicator));
        
        final AnimatorListener setListener = new AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
            }
            @Override
            public void onAnimationRepeat(Animator animation) {
            }
            @Override
            public void onAnimationEnd(Animator animation) {
                for (int i=0;i<childCount;i++) {
                    View child = recordViews.get(i);
                    root.removeView(child);
                    CellLayout.LayoutParams params = new CellLayout.LayoutParams(recordParams.get(i));
                    // reset child's position for re-add
                    child.setX(0);
                    child.setY(0);
                    currentPage.addViewToCellLayout(child, i, child.getId(), params, true);
                }
                mIsPlayUnlockAniamtion = false;
                mAnimatorSet = null;
                root.setTouchEnable(true);
                LockScreenAnimator.getInstance(mLauncher).setNeedBackHomeAnim(false);
            }
            @Override
            public void onAnimationCancel(Animator animation) {
                onAnimationEnd(animation);
            }
        };
        mLauncher.getHandler().post(new Runnable() {
            @Override
            public void run() {
                                if (mAnimatorSet == null) {
                                    Log.e(TAG, "oops!!! mAnimatorSet is null!!!");
                                    return;
                                }
                mAnimatorSet.addListener(setListener);
                mAnimatorSet.playTogether(items);
                mAnimatorSet.start();
            }
        });
    }
    
    private DragLayer.LayoutParams getRootLayoutParams(CellLayout.LayoutParams layoutParams) {
        DragLayer.LayoutParams rootLayoutParams = new DragLayer.LayoutParams(layoutParams.width, layoutParams.height);
        return rootLayoutParams;
    }
    
    /**
     * @param target - target view
     * @param cellX - Horizontal location of the item in the grid
     * @param cellY - Vertical location of the item in the grid
     * @return a collection of animators for this view
     */
    private ArrayList<Animator> createAnimators(final View target, int cellX, int cellY) {
        AnimatorListener listener = new AnimatorListener() {
            @Override
            public void onAnimationStart(Animator arg0) {
                target.setVisibility(View.VISIBLE);
            }
            @Override
            public void onAnimationRepeat(Animator arg0) {}
            @Override
            public void onAnimationEnd(Animator arg0) {}
            @Override
            public void onAnimationCancel(Animator arg0) {}
        };
        int row = cellY;
        int col = cellX;
        int index = cellY * 4 + cellX;
        int startScaleX = 3;
        int startScaleY = 3;
        int startWidth = target.getWidth() * startScaleX;
        int startHeight = target.getHeight() * startScaleY;
        float startX = 0;
        float startY = 0;
        if (row == 1 || row == 0) {
            startY = target.getY() + target.getHeight() / 2 - startHeight;
        } else if (row == 2 || row == 3) {
            startY = target.getY() + startHeight;
        }
        if (col == 1 || col == 0) {
            startX = target.getX() /*+ target.getWidth() / 2*/ - startWidth;
        } else {
            startX = target.getX() + startWidth;
        } 
        int startAngleX = ROTATE_ANGLE[2 * col];
        int startAngleY = ROTATE_ANGLE[2 * col + 1];
        if (index < DELAY_INDECIES.length) {
            index = DELAY_INDECIES[index];
        } else {
            index = 0;
        }
        
        ArrayList<Animator> animators = new ArrayList<Animator>();
        
        DecelerateInterpolator decelerateInterpolator = new DecelerateInterpolator(0.5f);
        AccelerateInterpolator accelerateInterpolator = new AccelerateInterpolator();
        ElasticInterpolator elasticInterpolator = new ElasticInterpolator();
        
        Animator animatorX = ObjectAnimator.ofFloat(target, "x", startX, target.getX());
        animatorX.setStartDelay(index * NEXT_ANIMATION_DELAY);
        animatorX.addListener(listener);
        animatorX.setDuration(MOVE_ANIMATION_DURATION);
        animatorX.setInterpolator(decelerateInterpolator);
        animators.add(animatorX);
        
        Animator animatorY = ObjectAnimator.ofFloat(target, "y", startY, target.getY());
        animatorY.setStartDelay(index * NEXT_ANIMATION_DELAY);
        animatorY.setDuration(MOVE_ANIMATION_DURATION);
        animatorY.setInterpolator(decelerateInterpolator);
        animators.add(animatorY);
        
        Animator animatorAlpha = ObjectAnimator.ofFloat(target, "alpha", 0, 1);
        animatorAlpha.setStartDelay(index * NEXT_ANIMATION_DELAY);
        animatorAlpha.setDuration(MOVE_ANIMATION_DURATION);
        animatorAlpha.setInterpolator(accelerateInterpolator);
        animators.add(animatorAlpha);
        
        Animator animatorScaleX = ObjectAnimator.ofFloat(target, "scaleX", startScaleX, 1);
        animatorScaleX.setStartDelay(index * NEXT_ANIMATION_DELAY);
        animatorScaleX.setDuration(ELASTIC_ANIMATION_DURATION);
        animatorScaleX.setInterpolator(elasticInterpolator);
        animators.add(animatorScaleX);
        
        Animator animatorScaleY = ObjectAnimator.ofFloat(target, "scaleY", startScaleY, 1);
        animatorScaleY.setStartDelay(index * NEXT_ANIMATION_DELAY);
        animatorScaleY.setDuration(ELASTIC_ANIMATION_DURATION);
        animatorScaleY.setInterpolator(elasticInterpolator);
        animators.add(animatorScaleY);
        
        Animator animatorRotationX = ObjectAnimator.ofFloat(target, "rotationX", startAngleX, 0);
        animatorRotationX.setStartDelay(index * NEXT_ANIMATION_DELAY);
        animatorRotationX.setDuration(ELASTIC_ANIMATION_DURATION);
        animatorRotationX.setInterpolator(accelerateInterpolator);
        animators.add(animatorRotationX);

        Animator animatorRotationY = ObjectAnimator.ofFloat(target, "rotationY", startAngleY, 0);
        animatorRotationY.setStartDelay(index * NEXT_ANIMATION_DELAY);
        animatorRotationY.setDuration(ELASTIC_ANIMATION_DURATION);
        animatorRotationY.setInterpolator(accelerateInterpolator);
        animators.add(animatorRotationY);
        
        return animators;
    }
    
    private Animator createMoveUpAnimator(final View target) {
        AnimatorListener listener = new AnimatorListener() {
            @Override
            public void onAnimationStart(Animator arg0) {
                target.setVisibility(View.VISIBLE);
            }
            @Override
            public void onAnimationRepeat(Animator arg0) {}
            @Override
            public void onAnimationEnd(Animator arg0) {}
            @Override
            public void onAnimationCancel(Animator arg0) {}
        };
        float startY = target.getHeight() + 240;
        Animator moveUp = ObjectAnimator.ofFloat(target, "y", startY , 0);
        moveUp.addListener(listener);
        moveUp.setDuration(MOVE_ANIMATION_DURATION);
        moveUp.setInterpolator(new DecelerateInterpolator(2));
        return moveUp;
    }

    private Animator createFadeInAnimator(final View target) {
        AnimatorListener listener = new AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                target.setVisibility(VISIBLE);
            }
            @Override
            public void onAnimationRepeat(Animator animation) {
            }
            @Override
            public void onAnimationEnd(Animator animation) {
            }
            @Override
            public void onAnimationCancel(Animator animation) {
            }
        };
        Animator fadeIn = ObjectAnimator.ofFloat(target, "alpha", 0, 1);
        fadeIn.addListener(listener);
        fadeIn.setDuration(ELASTIC_ANIMATION_DURATION);
        fadeIn.setInterpolator(new DecelerateInterpolator(2));
        return fadeIn;
    }
    
    public static class ElasticInterpolator implements Interpolator {
//        private final float FACTOR = 1.05158f;
        /* (non-Javadoc)
         * @see android.animation.TimeInterpolator#getInterpolation(float)
         */
        @Override
        public float getInterpolation(float input) {
//            return (input = input - 1) * input
//                    * ((FACTOR + 1) * input + FACTOR) + 1;
//            return (pos) * pos * ((s + 1) * pos - s);
            float FACTOR = 1.05158f;
            if((input /= 0.5f) < 1)
                return 0.5f * (input * input * (((FACTOR *= (1.175f)) + 1) * input - FACTOR));
            return 0.5f * ((input -= 2) * input * (((FACTOR *= (1.175f)) + 1) * input + FACTOR) + 2);
        }
    }

    public void setAllItemsOfCurrentPageVisibility(int visibility) {
        int currentPageIndex = getCurrentPage();
        final CellLayout currentPage = (CellLayout) getChildAt(currentPageIndex);
        ShortcutAndWidgetContainer container = null;
        if (currentPage != null) {
            container = currentPage.getShortcutAndWidgetContainer();
        }
        int childCount = 0;
        if (container != null) {
            childCount = container.getChildCount();
        }
        for(int i=0;i<childCount;i++) {
            final View child = container.getChildAt(i);
            child.setVisibility(visibility);
            child.setAlpha(1);
        }
        CellLayout hotseat = mLauncher.getHotseat().getLayout();
        ShortcutAndWidgetContainer hotseatContainer = null;
        if (hotseat != null) {
            hotseatContainer = hotseat.getShortcutAndWidgetContainer();
        }
        int hotseatChildCount = 0;
        if (hotseatContainer != null) {
            hotseatChildCount = hotseatContainer.getChildCount();
        }
        for(int i=0;i<hotseatChildCount;i++) {
            final View child = hotseatContainer.getChildAt(i);
            child.setVisibility(visibility);
            child.setAlpha(1);
        }
    }

    public void dismissFolder(FolderIcon folder) {
        if (folder == null)
            return;// return if folder is N/A
        FolderInfo folderInfo = (FolderInfo) folder.getTag();

        CellLayout layout = mLauncher.getCellLayout(folderInfo.container, folderInfo.screen);
        if (layout != null) {
            layout.removeView(folder);
        }
        LauncherModel.deleteItemFromDatabase(mLauncher, folderInfo);

        if (folder instanceof DropTarget) {
            mDragController.removeDropTarget((DropTarget) folder);
        }
        mLauncher.removeFolder(folderInfo);

        Folder f = folder.getFolder();
        ShortcutAndWidgetContainer container = f.getContent().getShortcutAndWidgetContainer();

        container.removeAllViews();
        container = layout.getShortcutAndWidgetContainer();
        //int cellXY[] = new int[2];
        int curScreen = getCurrentPage();

        CellLayout.LayoutParams fromLp = container.buildLayoutParams(2, -1, 1, 1);
        ArrayList<ScreenPosition> posList = new ArrayList<ScreenPosition>();

        Log.d(TAG, "folder's screen is " + folderInfo.screen);
        if (folderInfo.container != Favorites.CONTAINER_DESKTOP) {
            int currentPage = getCurrentPage();
            if ((currentPage < 0) ||
                 (currentPage >= ConfigManager.getScreenMaxCount())){
                currentPage = 1;
            }
            LauncherModel.getEmptyPosList(posList, currentPage, ConfigManager.getScreenMaxCount() - 1);
            if (currentPage > 1) {
                LauncherModel.getEmptyPosList(posList, currentPage -1, 1);
            }
        } else if (folderInfo.screen == 0) {
            LauncherModel.getEmptyPosList(posList, 0, ConfigManager.getScreenMaxCount() - 1);
        } else if (folderInfo.screen == 1) {
            LauncherModel.getEmptyPosList(posList, 1, ConfigManager.getScreenMaxCount() - 1);
        } else if (folderInfo.screen == ConfigManager.getScreenMaxCount() - 1) {
            LauncherModel.getEmptyPosList(posList, folderInfo.screen, 1);
        } else {
            LauncherModel.getEmptyPosList(posList, folderInfo.screen, ConfigManager.getScreenMaxCount() - 1);
            LauncherModel.getEmptyPosList(posList, folderInfo.screen -1, 1);
        }
        Log.d(TAG, "posList size is " + posList.size());

        int posNumber = 0;
        for (ShortcutInfo info :  folderInfo.contents) {
            //int screen = findCellForSpan(cellXY, curScreen, info.spanX, info.spanY);
            ScreenPosition screenPos = null;
            if (posNumber < posList.size()) {
                screenPos = posList.get(posNumber);
                posNumber++;
            }
            if (screenPos == null) {
                if ((info.itemType == Favorites.ITEM_TYPE_APPLICATION) ||
                    (info.itemType == Favorites.ITEM_TYPE_NOSPACE_APPLICATION)) {
                    info.itemType = Favorites.ITEM_TYPE_NOSPACE_APPLICATION;
                    info.container = Favorites.CONTAINER_DESKTOP;
                    info.screen = -1;
                    info.cellX = -1;
                    info.cellY = -1;
                    LauncherModel.updateItemInDatabase(mLauncher, info);
                } else {
                    LauncherModel.deleteItemFromDatabase(mLauncher, info);
                }
                continue;
            }

            info.cellX = screenPos.x;
            info.cellY = screenPos.y;
            info.screen = screenPos.s;
 
            LauncherModel.addOrMoveItemInDatabase(mLauncher, info, Favorites.CONTAINER_DESKTOP, info.screen, info.cellX, info.cellY);
            View v = mLauncher.createShortcut(R.layout.application, null, info);
            addInScreen(v, Favorites.CONTAINER_DESKTOP, info.screen, info.cellX, info.cellY, info.spanX, info.spanY);

            if (info.screen == curScreen) {
                CellLayout.LayoutParams toLp = container.buildLayoutParams(info.cellX, info.cellY, info.spanX, info.spanY);
                ObjectAnimator a = ObjectAnimator.ofPropertyValuesHolder(v,
                        PropertyValuesHolder.ofFloat("translationX", (fromLp.x - toLp.x), 0),
                        PropertyValuesHolder.ofFloat("translationY", (fromLp.y - toLp.y), 0));
                a.setDuration(500);
                a.start();
            } else {
                ((CellLayout)getPageAt(info.screen)).addPengindFlingDropDownTarget(v, 0, 0, true, info.title.toString(), 0, 0);
            }
        }
        Log.d(TAG, "dismissFolder finish");

        checkAndRemoveEmptyCell();
    }

    private int findCellForSpan(int[] cellXY, int start, int spanX, int spanY) {
        final int N = getPageCount();
        for (int i = start; i < N; i++) {
            CellLayout layout = (CellLayout) getPageAt(i);
            if (layout.findCellForSpan(cellXY, spanX, spanY)) {
                return i;
            }
        }

        int defautlStartScreen = ConfigManager.DEFAULT_FIND_EMPTY_SCREEN_START;
        if (start > defautlStartScreen) {
            for (int i = defautlStartScreen; i < start; i++) {
                CellLayout layout = (CellLayout) getPageAt(i);
                if (layout.findCellForSpan(cellXY, spanX, spanY)) {
                    return i;
                }
            }
        }

        return -1;
    }

    public void arrangeAllPages(){
        arrangeCurrentPageAsync();
        for( int i = 0; i < getChildCount(); i++ ){
            if( i == getCurrentPage() ) continue;
            arrangePageAsync(i);
        }
    }
    public void arrangeAllPagesPostDelay(final HomeShellSetting.Callback callback){
        postDelayed(new Runnable() {
            @Override
            public void run() {
                int count = getChildCount();
                for( int i = 0; i < count; i++ ){
                    arrangePage(i);
                }
                callback.onFinish();
            }
        },200);
    }
    public void arrangePageAsync( final int index ){
        post(new Runnable() {
            @Override
            public void run() {
               arrangePage(index);
            }
        });
    }

    /**
     * Arrange 'index' page's icons one by one and update UI.
     */
    public void arrangePage( int index ){
        CellLayout cl = (CellLayout)getChildAt(index);
        int maxX = cl.getCountX();
        int maxY = cl.getCountY();

        //first screen differ from the others
        int startY, endY, deltaY;
        if( index == 0 ){
            startY = maxY - 1;
            endY   = -1;
            deltaY = -1;
        }else{
            startY = 0;
            endY   = maxY;
            deltaY = 1;
        }

        for( int y = startY; y != endY; y += deltaY ){
            for( int x = 0; x < maxX; x++ ){
                View view = cl.getChildAt(x, y);
                if( view == null ) continue;
                CellLayout.LayoutParams lp = (CellLayout.LayoutParams)view.getLayoutParams();
                ItemInfo info = (ItemInfo)view.getTag();
                if (info == null || info.itemType == LauncherSettings.Favorites.ITEM_TYPE_APPWIDGET
                        || info.itemType == LauncherSettings.Favorites.ITEM_TYPE_GADGET) {
                    continue;
                }
                int[] position = findArrangePosition(index, lp.cellX, lp.cellY, maxX, maxY);

                cl.onMove(view, position[0], position[1], 1, 1);

                lp.useTmpCoords = false;
                info.cellX = lp.cellX = position[0];
                info.cellY = lp.cellY = position[1];

                view.setLayoutParams(lp);
                LauncherModel.updateItemInDatabase(mLauncher, info);
            }
        }
    }


    public void arrangeCurrentPageAsync(){
        post(new Runnable() {
            @Override
            public void run() {
                arrangeCurrentPage();
            }
        });
    }

    public void arrangeCurrentPage(){
        arrangePage(getCurrentPage());
    }

    /**
     * find the first empty cell from top to bottom, from left to right
     */
    public int[] findArrangePosition(int screen, int x, int y, int maxX, int maxY){
        CellLayout cl = (CellLayout)getChildAt(screen);

        int startY, endY, deltaY;
        if( screen != 0 ){
            startY = 0;
            endY   = maxY;
            deltaY = 1;
        }else{
            startY = maxY - 1;
            endY   = -1;
            deltaY = -1;
        }

        for( int indexY = startY; indexY != endY; indexY += deltaY ){
            for( int indexX = 0; indexX < maxX; indexX++ ){
                if( indexX == x && indexY == y ) return new int[]{x,y};
                if( cl.getChildAt(indexX, indexY) == null ) {
                    return new int[]{indexX,indexY};
                }
            }
        }
        return null;
    }
    public void updateWorkspaceAfterDelItems(List<ItemInfo> removedItems) {
        for ( int i = 0; i < removedItems.size(); i++) {
            ItemInfo item = removedItems.get(i);
            CellLayout layout = null;
            if(item.container == LauncherSettings.Favorites.CONTAINER_DESKTOP) {
                layout = (CellLayout) getChildAt(item.screen);
            } else if(item.container == LauncherSettings.Favorites.CONTAINER_HOTSEAT) {
                layout = (CellLayout) mLauncher.getHotseat().getLayout();
            } else if (item.container == LauncherSettings.Favorites.CONTAINER_HIDESEAT) {
                layout = (CellLayout)mLauncher.getHideseat().getChildAt(item.screen);
            }
            if(layout == null) {
                Log.e(TAG,"fail when find the cellLayout in workspace");
                return;
            }
            ShortcutAndWidgetContainer container = layout.getShortcutAndWidgetContainer();
            int childCount = container.getChildCount();
            for(int j = 0; j < childCount; j++) {
                View view = container.getChildAt(j);
                Object tag = view.getTag();
                if (tag instanceof ShortcutInfo) {
                    ItemInfo info = (ItemInfo) tag;
                    if(item.id == info.id) {
                        layout.removeView(view);
                        layout.markCellsAsUnoccupiedForView(view);
                        break;
                    }
                }
            }
        }
    }
    public void makeSureWidgetPages() {
        int screenCount = getChildCount();
        int normalCount = getNormalScreenCount();
        int widgePageIndex = 0;
        WidgetPageManager wpManager = mLauncher.mWidgetPageManager;
        boolean removeAllWidgetPage = false;

        for (int i = normalCount; i < screenCount; i++, widgePageIndex++) {
            View child = getChildAt(i);
            CellLayout cl = (CellLayout) child;
            String curName = cl.getWidgetPagePackageName();
            WidgetPageManager.WidgetPageInfo info = wpManager.getWidgetPageInfo(widgePageIndex);
            if (curName != null && !curName.equals(info.getPackageName())) {
                removeAllWidgetPage = true;
                break;
            }

        }

        if (removeAllWidgetPage) {
            for (int i = screenCount - 1; i >= normalCount; i--) {
                View child = getChildAt(i);
                removeView(child);
            }
        }
        screenCount = getChildCount();
        int wpCount = wpManager.getWigetPageCount();
        if (screenCount != normalCount + wpCount) {
            for (int i = 0; i < wpCount; i++) {
                WidgetPageManager.WidgetPageInfo info = wpManager.getWidgetPageInfo(i);
                addWidgetPage(normalCount + i, info);
            }
        }
        
        invalidatePageIndicator(true);//add by huangweiwei, topwise, 2015-7-2

    }

    private void addWidgetPage(int position, WidgetPageManager.WidgetPageInfo info) {
        if (info == null) {
            return;
        }
        CellLayout cl = (CellLayout) (View.inflate(getContext(), R.layout.workspace_widget_screen, null));
        cl.setWidgetPagePackageName(info.getPackageName());
        addView(cl, position);
        addWidgetPageToLayout(cl, info.getRootView(), position);
    }

    private void addWidgetPageToLayout(CellLayout cl, View widgetPageView, int position) {
        int childId = LauncherModel.getCellLayoutChildId(
                LauncherSettings.Favorites.CONTAINER_DESKTOP, position, 0, 0, cl.getCountX(),
                cl.getCountY());
        CellLayout.LayoutParams lp = new CellLayout.LayoutParams(0, 0, cl.getCountX(),
                cl.getCountY());
        ShortcutAndWidgetContainer container = cl.getShortcutAndWidgetContainer();
        if (container.indexOfChild(widgetPageView) != -1) {
            container.removeView(widgetPageView);
        }
        cl.addViewToCellLayout(widgetPageView, 0, childId, lp, true);
    }

    public void bindWidgetPage(int position) {
        CellLayout cl = (CellLayout)getChildAt(position);
        String widgetPageName = cl.getWidgetPagePackageName();
        if (widgetPageName!= null) {
            View v = mLauncher.mWidgetPageManager.getWidgetPageRootView(widgetPageName);
            addWidgetPageToLayout(cl, v, position);
        }
    }

    public void removeWidgetPages() {
        final int screenCount = getChildCount();
        for (int i = screenCount - 1; i >= 0; i--) {
            CellLayout child = (CellLayout) getChildAt(i);
            if (isWidgetPageView(child)) {
                if(LauncherApplication.getLauncher().mWidgetPageManager.isSupportWidgetPageHotseat()) {
	                String widgetPackagename = child.getWidgetPagePackageName();
	                View hotseatView = mLauncher.mWidgetPageManager.getHotseatView(widgetPackagename);
	                if (hotseatView != null) {
	                    hotseatView.setVisibility(View.INVISIBLE);
	                }
                }
                removeView(child);
                child.removeAllViews();
            }
        }
        invalidatePageIndicator(true);//added by qinjinchuan topwise for update page indicator before opening hideseat

    }

    public boolean iscurrMusicWidgetPage() {
        int curPage = getCurrentPage();
        View v = getChildAt(curPage);
        if(v instanceof CellLayout) {
            CellLayout cell = (CellLayout) v;
            return cell.isWidgetPage() && cell.getWidgetPagePackageName().equals("com.android.music_widgetpage");
        }
        return false;
    }

    public boolean isNextWidgetPage() {
        int nextPage = getNextPage();
        View v = getChildAt(nextPage);
        if (isWidgetPageView(v)) {
            return true;
        }
        return false;
    }

    public boolean iscurrWidgetPage() {
        int curPage = getCurrentPage();
        View v = getChildAt(curPage);
        if (isWidgetPageView(v)) {
            return true;
        }
        return false;
    }

    public static boolean isWidgetPageView(View view) {
        if (view instanceof CellLayout) {
            return ((CellLayout) view).isWidgetPage();
        }
        return false;
    }
    public int getNormalScreenCount() {
        int screenCount = getChildCount();
        if (screenCount == 0) {
            return 0;
        }
        int normalCount = screenCount;
        for (int i = 0; i < screenCount; i++) {
            View child = getChildAt(i);
            if (isWidgetPageView(child)) {
                normalCount--;
            }
        }
        return normalCount;
    }
    private boolean mInEditing;
    private static final float EDIT_SCALE = 0.8f;
    private static final int EDIT_MODE_DURATION = 300;
    private static boolean sContinuousHomeShellSaved;

    public void setEditMode(boolean bSet) {
        mInEditing = bSet;
        //start modify by huangxunwan for remove widget page at Edit Mode 20150515
        if (mInEditing) {
            removeWidgetPages();
        }
        //end modify by huangxunwan for remove widget page at Edit Mode 20150515
        setPageSpacing(bSet ? mSpringLoadedPageSpacing : 0);
        setLayoutScale(bSet ? EDIT_SCALE : 1.0f);
        //ColorDrawable colorDrawable = new ColorDrawable(bSet ? R.color.allapp_background : android.R.color.transparent);
        int bgColor = bSet ? getResources().getColor(R.color.editmode_background) : getResources()
                .getColor(android.R.color.transparent);
        ColorDrawable colorDrawable = new ColorDrawable(bgColor);
        ((View)getParent()).setBackgroundDrawable(colorDrawable);
        ObjectAnimator.ofInt(colorDrawable, "alpha", 0, 255).setDuration(EDIT_MODE_DURATION).start();

        final int screenCount = getChildCount();
        initScreen(mCurrentPage);
        for (int i = 0; i < screenCount; i++) {
            if (i != mCurrentPage) {
                initScreen(i);
            }
        }
        setCurrentPage((mNextPage != INVALID_PAGE) ? mNextPage : mCurrentPage);
        setChildrenEditMode(bSet);
        if (mInEditing) {
            sContinuousHomeShellSaved = sContinuousHomeShellFeature;
            //modified by qinjinchuan topwise for enabling continuous homeshell in editmode
            //sContinuousHomeShellFeature = false;
            //modified by qinjinchuan topwise for enabling continuous homeshell in editmode
        } else {
            sContinuousHomeShellFeature = sContinuousHomeShellSaved;
            updateItemScreenInDatebase();
            mLauncher.recordPageCount();
            //start modify by huangxunwan for remove widget page at Edit Mode 20150515
            makeSureWidgetPages();
            //end modify by huangxunwan for remove widget page at Edit Mode 20150515
        }
    }

    private void resetScreenParams(CellLayout cl) {
        if (mInEditing) {
            cl.setPivotX(0);
            cl.setPivotY(0);
        } else {
            cl.setPivotX(cl.getWidth() / 2);
            cl.setPivotY(cl.getHeight() / 2);
        }
        cl.setRotation(0);
        cl.setRotationY(0);
        cl.setAlpha(1.0f);
        if (cl.getVisibility() != VISIBLE) {
            cl.setVisibility(VISIBLE);
        }
        cl.invalidate();
    }

    private void initScreen(int i) {
        final CellLayout cl = (CellLayout) getChildAt(i);
        if (cl != null) {
            resetScreenParams(cl);
            float cellTransX = getChildAt(0).getWidth() * 0.1f;
            Drawable drawable = mInEditing ? getResources().getDrawable(R.drawable.em_cell_bg)
                    : null;
            if (i != mCurrentPage) {
                cl.setTranslationX(mInEditing ? cellTransX : 0);
                cl.setScaleX(mInEditing ? EDIT_SCALE : 1.0f);
                cl.setScaleY(mInEditing ? EDIT_SCALE : 1.0f);
            } else {
                AnimatorSet enterAnimator = new AnimatorSet();
                ObjectAnimator translationX = ObjectAnimator.ofFloat(cl, "translationX",
                        mInEditing ? 0 : cellTransX, mInEditing ? cellTransX : 0);
                ObjectAnimator scaleY = ObjectAnimator.ofFloat(cl, "scaleY", mInEditing ? 1
                        : EDIT_SCALE, mInEditing ? EDIT_SCALE : 1);
                ObjectAnimator scaleX = ObjectAnimator.ofFloat(cl, "scaleX", mInEditing ? 1
                        : EDIT_SCALE, mInEditing ? EDIT_SCALE : 1);
                enterAnimator.playTogether(scaleY, scaleX, translationX);
                enterAnimator.setDuration(EDIT_MODE_DURATION);
                enterAnimator.start();
            }
            if (drawable != null) {
                ObjectAnimator.ofInt(drawable, "alpha", 0, 255).setDuration(EDIT_MODE_DURATION)
                        .start();
            }
            cl.setBackground(drawable);
        }
    }

    public void setChildrenEditMode(boolean bSet) {

        if (bSet) {
            int screenCount = getChildCount();
            if (screenCount > ConfigManager.getScreenMaxCount()) {
                return;
            } else if (screenCount == ConfigManager.getScreenMaxCount()) {
                CellLayout lastChild = (CellLayout)getChildAt(screenCount - 1);
                if (lastChild.isFakeChild() && lastChild.getEditBtnContainer() != null) {
                    lastChild.setEditBtnContainerMode(false);
                }
            } else {
                boolean saved = mEmptyScreenAdded;
                addEmptyScreenSync();
                mEmptyScreenAdded = saved;
                screenCount = getChildCount();
                CellLayout lastChild = (CellLayout)getChildAt(screenCount - 1);
                lastChild.addEditBtnContainer();
                lastChild.setEditBtnContainerMode(true);
                initScreen(screenCount - 1);
                invalidatePageIndicator(true);
            }
            screenCount = getChildCount();
            for (int i = 0; i < screenCount; i++) {
                CellLayout cl = (CellLayout) getChildAt(i);
                if (i == screenCount - 1 && !cl.isFakeChild()) {
                    cl.addEditBtnContainer();
                    cl.setEditBtnContainerMode(false);
                } else if (i != screenCount - 1 && !cl.hasChild()) {
                    cl.addEditBtnContainer();
                    cl.setEditBtnContainerMode(false);
                }

            }
        } else {
            final int screenCount = getChildCount();
            for (int i = 0; i < screenCount; i++) {
                CellLayout cl = (CellLayout) getChildAt(i);
                cl.removeEditBtnContainer();
            }
            CellLayout lastCl = (CellLayout) getChildAt(screenCount -1);
            if (screenCount <= ConfigManager.getScreenMaxCount()
                    && lastCl.isFakeChild()) {
                if (mCurrentPage == screenCount - 1 ) {
                    mCurrentPage--;
                }
                if (!isPageMoving()) {
                    setCurrentPage(mCurrentPage);
                    onPageEndMoving();
                }
                removeViewAt(screenCount - 1);
                invalidatePageIndicator(true);
            }
        }
    }

    public void deleteEmptyScreen(CellLayout cl) {
        int screenCount = getChildCount();

        if (screenCount == ConfigManager.getScreenMaxCount()) {
            CellLayout lastChild = (CellLayout)getChildAt(screenCount - 1);
            if (!lastChild.isFakeChild() && !lastChild.hasChild()) {
                lastChild.setEditBtnContainerMode(true);
                return;
            } else if (lastChild.hasChild()){
                removeView(cl);
                setChildrenEditMode(true);
                return;
            }
        }
        removeView(cl);

        invalidatePageIndicator(true);//added by qinjinchuan topwise for bug418
    }
    // update the ItemInfo.screen in database and save the page count.
    private void updateItemScreenInDatebase() {
        int pageCount = getChildCount();
        for (int i = 0; i < pageCount; i++) {
            CellLayout cell = (CellLayout) getChildAt(i);
            ShortcutAndWidgetContainer container = (ShortcutAndWidgetContainer) cell.getChildAt(0);
            for (int j = 0; j < container.getChildCount(); j++) {

                ItemInfo info = (ItemInfo) container.getChildAt(j).getTag();
                if (info.screen != i) {
                    info.screen = i;
                    LauncherModel.updateItemInDatabase(getContext(), info);
                }
            }
        }
    }
	    //modify by huangweiwei, topwise, 2015-1-9, old is private
    public void hotseatScrolled(int screenCenter) {
        int childCount = getChildCount();
        if (mLauncher.mWidgetPageManager.getWigetPageCount() == 0) {
            return;
        }
        View hotseatView;

        float pubHotseatProgress = 0;
        int pubHotseatCount = 0;
        for (int i = 0; i < childCount; i++) {
            hotseatView = null;
            CellLayout widgetPage = (CellLayout) getChildAt(i);
            //add by huangweiwei, topwise, 2015-1-9
            if (screenCenter < 0 || screenCenter > getChildOffset(getPageCount() - 1) + getMeasuredWidth()) {
            	Log.d("huangweiwei", "ERROR screenCenter="+screenCenter);
            	if (screenCenter < 0) {
            		screenCenter = getChildOffset(getPageCount() - 1) + getMeasuredWidth() + screenCenter;
            	} else {
            		screenCenter = screenCenter - (getChildOffset(getPageCount() - 1) + getMeasuredWidth());
            	}
            	Log.d("huangweiwei", "NEW screenCenter="+screenCenter);
            }
            //add end
            float progress = getScrollProgress(screenCenter, widgetPage, i);
            float absProgress = Math.abs(progress);
            String widgetPackagename = widgetPage.getWidgetPagePackageName();
            if (widgetPackagename != null) {
                hotseatView = mLauncher.mWidgetPageManager.getHotseatView(widgetPackagename);
                if (hotseatView != null && hotseatView.getVisibility() != VISIBLE/* && isNextWidgetPage()*/) {//modify by huangweiwei, topwise, 2015-1-9
                    hotseatView.setVisibility(View.VISIBLE);
                }
            }
            if (hotseatView == null) {
                pubHotseatProgress = absProgress + pubHotseatProgress;
                pubHotseatCount ++;
            } else {
                absProgress = Math.min(absProgress * 2, 1.0f);
                //add by huangweiwei, topwise, 2015-1-9
                int height = hotseatView.getHeight();
                if (height == 0) {
                	height = (int) mLauncher.getResources().getDimension(
                            R.dimen.button_bar_height_plus_padding);
                }
                hotseatView.setTranslationY(absProgress * height);
                //add end
            }
        }
        pubHotseatProgress =  pubHotseatProgress - (pubHotseatCount - 1);
        pubHotseatProgress =  Math.min(pubHotseatProgress * 2, 1.0f);
        hotseatView = mLauncher.getHotseat();
        hotseatView.setTranslationY(pubHotseatProgress * hotseatView.getHeight());
    }

    public WidgetPageManager.WidgetPageInfo getWidgetPageInfoAt(int page) {
        CellLayout widgetPage = (CellLayout) getChildAt(page);
        String packagename = widgetPage.getWidgetPagePackageName();
        return mLauncher.mWidgetPageManager.getWidgetPageInfo(packagename);
    }

    public void adjustToThreeLayout() {
        int paddingBottom = getResources().getDimensionPixelSize(
                R.dimen.workspace_bottom_padding_3_3);
        setPadding(getPaddingLeft(), getPaddingTop(), getPaddingRight(), paddingBottom);
        refreshMaxDistanceForFolderCreation();
        int count = getChildCount();
        for (int i = 0; i < count; i++) {
            ((CellLayout) getChildAt(i)).adjustToThreeLayout();
        }
    }

    public void adjustFromThreeLayout() {
        int paddingBottom = getResources().getDimensionPixelSize(
                R.dimen.workspace_bottom_padding);
        setPadding(getPaddingLeft(), getPaddingTop(), getPaddingRight(), paddingBottom);
        refreshMaxDistanceForFolderCreation();
        int count = getChildCount();
        for (int i = 0; i < count; i++) {
            ((CellLayout) getChildAt(i)).adjustFromThreeLayout();
        }
    }

    private void refreshMaxDistanceForFolderCreation() {
        float factor = mLauncher.getIconManager().supprtCardIcon() ? FOLDER_CREATION_FACTOR_CARDMODE
                : FOLDER_CREATION_FACTOR;
        if (AgedModeUtil.isAgedMode()) {
            mMaxDistanceForFolderCreation = (factor * getResources().getDimensionPixelSize(
                    R.dimen.app_icon_size) * AgedModeUtil.SCALE_RATIO_FOR_AGED_MODE);
        } else {
            mMaxDistanceForFolderCreation = (factor * getResources().getDimensionPixelSize(
                    R.dimen.app_icon_size));
        }

    }
}
