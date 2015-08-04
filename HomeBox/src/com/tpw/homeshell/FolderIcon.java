/*
 * Copyright (C) 2008 The Android Open Source Project
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

import java.util.ArrayList;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.tpw.homeshell.DropTarget.DragObject;
import com.tpw.homeshell.FolderInfo.FolderListener;
import com.tpw.homeshell.LauncherSettings.Favorites;
import com.tpw.homeshell.appgroup.AppGroupManager;
import com.tpw.homeshell.icon.BubbleTextView;
import com.tpw.homeshell.icon.IconManager;
import com.tpw.homeshell.icon.IconUtils;
import com.tpw.homeshell.model.LauncherModel;
import com.tpw.homeshell.themeutils.ThemeUtils;

/**
 * An icon that can appear on in the workspace representing an {@link UserFolder}.
 */
public class FolderIcon extends LinearLayout implements FolderListener {
    private Launcher mLauncher;
    private Folder mFolder;
    private FolderInfo mInfo;
    private static boolean sStaticValuesDirty = true;

    private CheckLongPressHelper mLongPressHelper;

    // The number of icons to display in the
    private static final int NUM_ITEMS_IN_PREVIEW_LINE = 3;
    private static final int NUM_ITEMS_IN_PREVIEW = NUM_ITEMS_IN_PREVIEW_LINE
            * NUM_ITEMS_IN_PREVIEW_LINE;
    private static final int CONSUMPTION_ANIMATION_DURATION = 300;
    private static final int DROP_IN_ANIMATION_DURATION = 400;
    private static final int INITIAL_ITEM_ANIMATION_DURATION = 350;
    private static final int FINAL_ITEM_ANIMATION_DURATION = 200;

    // The degree to which the outer ring is scaled in its natural state
    private static  float OUTER_RING_GROWTH_FACTOR = 0.3f;

    // The amount of vertical spread between items in the stack [0...1]
    private static final float PERSPECTIVE_SHIFT_FACTOR = 0.24f;

    // The degree to which the item in the back of the stack is scaled [0...1]
    // (0 means it's not scaled at all, 1 means it's scaled to nothing)
    private static final float PERSPECTIVE_SCALE_FACTOR = 0.35f;

    public static Drawable sSharedFolderLeaveBehind = null;

    private ImageView mPreviewBackground;
    private BubbleTextView mFolderName;
    private GestureDetector mDetector;

    FolderRingAnimator mFolderRingAnimator = null;

    // These variables are all associated with the drawing of the preview; they are stored
    // as member variables for shared usage and to avoid computation on each frame
    private int mIntrinsicIconSize;
    private float mBaselineIconScale;
    private int mBaselineIconSize;
    private int mAvailableSpaceInPreview;
    private int mTotalWidth = -1;
    private int mPreviewOffsetX;
    private int mPreviewOffsetY;
    private float mMaxPerspectiveShift;
    boolean mAnimating = false;

    private PreviewItemDrawingParams mParams = new PreviewItemDrawingParams(0, 0, 0, 0);
    private PreviewItemDrawingParams mAnimParams = new PreviewItemDrawingParams(0, 0, 0, 0);
    private ArrayList<ShortcutInfo> mHiddenItems = new ArrayList<ShortcutInfo>();
    
    private static Bitmap mImgRTCorner; // right top corner image
    private static int mNumberSize;
    private int mNumIndicatorNumDrawX = 0;
    private int mNumIndicatorNumDrawY = 0;
    private static int INDICATOR_BOUNDRY_X;
    private static int INDICATOR_BOUNDRY_Y;
    private static Paint mNumberPaint;
    
    private static int mNewMarkDrawX = 0;
    private static int mNewMarkDrawY = 0;
    private static Paint mNewMarkPaint;
    public static String NEW_MARK_SHORTCUT;
    public static String NOTIFICATION_MARK;

    private static int IND_TEXT_SIZE_SMALL;
    private static int IND_TEXT_SIZE_NORMAL;
    private static int IND_NUM_SIZE_SMALL;
    private static int IND_NUM_SIZE_NORMAL;
    static Bitmap mThemeBackground;
    private static Drawable mCardBackground;
    static int mPreiewSize = 0;
    static int mPreiewPadding = 0;
    private static Drawable mCardCover;
    private static boolean mIsSupportCard;
    private static int mTextPaddingTop;
    private static int mTextSize = 0;

    public boolean mIsTempPadding = false;
    private int mTempPaddingInHotseat = 0;
//    private int mOldPaddingLeft = 0;
//    private int mOldPaddingRight = 0;

    class FolderIconGestureListener extends GestureDetector.SimpleOnGestureListener{
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
                float velocityY) {
            if( velocityY < 0 && Math.abs(velocityY) > Math.abs(velocityX) ){
                shake();
                return true;
            }
            return false;
        }
    }

    public FolderIcon(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public FolderIcon(Context context) {
        super(context);
        init();
    }

    static {
        initGlobal();
    }

    public static void initGlobal() {
        Context context = LauncherApplication.getContext();
        Resources res = context.getResources();
        mPreiewSize = ThemeUtils.getIconSize(context);
        if (mPreiewSize <= 0) {
            mPreiewSize = res.getDimensionPixelSize(
                    R.dimen.folder_preview_size);
        }
        mPreiewPadding = res.getDimensionPixelSize(R.dimen.folder_preview_padding);
        if (AgedModeUtil.isAgedMode()) {
            mPreiewSize = (int) (mPreiewSize * AgedModeUtil.SCALE_RATIO_FOR_AGED_MODE);
            mPreiewPadding = (int) (mPreiewPadding * AgedModeUtil.SCALE_RATIO_FOR_AGED_MODE);
        }
        mIsSupportCard = (((LauncherApplication)context).getIconManager().supprtCardIcon());
        if(mIsSupportCard) {
            mCardCover = res.getDrawable(R.drawable.card_folder_cover);
            mCardBackground = res.getDrawable(R.drawable.card_folder_bg);
        } else {
            mThemeBackground = buildThemeBackground(mPreiewSize, mPreiewSize);
        }

        mNumberPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mNumberPaint.setColor(Color.WHITE);
        mNewMarkPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mNewMarkPaint.setColor(Color.WHITE);

        mImgRTCorner = BitmapFactory.decodeResource(res, R.drawable.ic_corner_mark_bg);
        if (AgedModeUtil.isAgedMode()) {
            mImgRTCorner = Bitmap.createBitmap(mImgRTCorner, 0, 0, mImgRTCorner.getWidth(),
                    mImgRTCorner.getHeight(), AgedModeUtil.sScaleUp, false);
            INDICATOR_BOUNDRY_X = res.getDimensionPixelSize(R.dimen.bubble_icon_width_3_3)
                    - mImgRTCorner.getWidth();
        } else {
            INDICATOR_BOUNDRY_X = res.getDimensionPixelSize(R.dimen.folder_cell_width)
                    - mImgRTCorner.getWidth();
        }
        INDICATOR_BOUNDRY_Y = 0;

        IND_TEXT_SIZE_NORMAL = res.getDimensionPixelSize(R.dimen.bubble_text_normal_size);
        IND_TEXT_SIZE_SMALL = res.getDimensionPixelSize(R.dimen.bubble_text_small_size);
        IND_NUM_SIZE_NORMAL = res.getDimensionPixelSize(R.dimen.bubble_num_normal_size);
        IND_NUM_SIZE_SMALL = res.getDimensionPixelSize(R.dimen.bubble_num_small_size);
        mNumberSize = IND_NUM_SIZE_NORMAL;
        final String newMark = res.getString(R.string.new_mark_shortcut);
        String language = res.getConfiguration().locale.getLanguage();
        if ("zh".equals(language)) {
            	mTextSize = IND_TEXT_SIZE_NORMAL;
	} else {
	    mTextSize = IND_TEXT_SIZE_SMALL;
	}

        final Point tempPoint = calcStringPosition(newMark, mTextSize, mImgRTCorner, mNewMarkPaint);
        mNewMarkDrawX = tempPoint.x;
        mNewMarkDrawY = tempPoint.y; 
        NEW_MARK_SHORTCUT = res.getString(R.string.new_mark_shortcut);
        NOTIFICATION_MARK = res.getString(R.string.notification_mark);

        mTextPaddingTop = res.getDimensionPixelSize(R.dimen.folder_icon_padding_top);

        Log.d("FolderIcon", "initGlobal mIsSupportCard : " + mIsSupportCard);
    }

    private void init() {
        mLongPressHelper = new CheckLongPressHelper(this);
        if (Build.VERSION.SDK_INT < 19/* KITKAT */)
            setLayerType(LAYER_TYPE_HARDWARE, null);
        mDetector = new GestureDetector(getContext(),new FolderIconGestureListener());
        IND_NUM_SIZE_NORMAL = LauncherApplication.getContext().getResources()
                .getDimensionPixelSize(R.dimen.bubble_num_normal_size);
        IND_NUM_SIZE_SMALL = LauncherApplication.getContext().getResources()
                .getDimensionPixelSize(R.dimen.bubble_num_small_size);
    }

    private static Bitmap buildThemeBackground(int w, int h) {
        Bitmap bmp = Bitmap.createBitmap(w, h, Config.ARGB_8888);
        Bitmap bg = ((FastBitmapDrawable) ((LauncherApplication) LauncherApplication
                .getContext()).getIconManager().buildUnifiedIcon(bmp,
                IconManager.ICON_TYPE_FOLDER)).getBitmap();
        bmp.recycle();
        if (AgedModeUtil.isAgedMode()) {
            bg = Bitmap.createBitmap(bg, 0, 0, bg.getWidth(), bg.getHeight(),
                    AgedModeUtil.sScaleUp, false);
        }

        return bg;
    }

    private void calcNumberPosition(String num) {
        if (num == null) {
            return;
        }

        final Point tempPos = calcStringPosition(num, mNumberSize, mImgRTCorner, mNumberPaint);
        mNumIndicatorNumDrawY = tempPos.y;
        mNumIndicatorNumDrawX = tempPos.x;
    }

    /**
     * Calculate the position where string will be drawn
     * @return the (x,y) relative to text baseline
     */
    static private Point calcStringPosition(String str, int textSize, Bitmap bg,
            Paint paint) {
        final Point tempPos = new Point();
        if (str == null) {
            tempPos.x = -1;
            tempPos.y = -1;
            return tempPos;
        }

        Rect rect = new Rect();
        paint.setTextSize(textSize);
        /*
         * If you are confused with Paint.getTextBounds,read the link below:
         * {@link http://stackoverflow.com/questions/7549182/android-paint-measuretext-vs-gettextbounds }
         */
        paint.getTextBounds(str, 0, str.length(), rect);

        int leftTopPointX = (bg.getWidth()  - rect.width() ) / 2 + INDICATOR_BOUNDRY_X;
        int leftTopPointY = (bg.getHeight() - rect.height()) / 2 + INDICATOR_BOUNDRY_Y;

        tempPos.x = leftTopPointX - rect.left;
        tempPos.y = leftTopPointY - rect.top;

        return tempPos;
    }

    public boolean isDropEnabled() {
        final ViewGroup cellLayoutChildren = (ViewGroup) getParent();
        final ViewGroup cellLayout = (ViewGroup) cellLayoutChildren.getParent();
        final Workspace workspace = (Workspace) cellLayout.getParent();
        return !workspace.isSmall();
    }

    static FolderIcon fromXml(int resId, Launcher launcher, ViewGroup group,
            FolderInfo folderInfo) {
        @SuppressWarnings("all") // suppress dead code warning
        final boolean error = INITIAL_ITEM_ANIMATION_DURATION >= DROP_IN_ANIMATION_DURATION;
        if (error) {
            throw new IllegalStateException("DROP_IN_ANIMATION_DURATION must be greater than " +
                    "INITIAL_ITEM_ANIMATION_DURATION, as sequencing of adding first two items " +
                    "is dependent on this");
        }

        FolderIcon icon = (FolderIcon) LayoutInflater.from(launcher).inflate(resId, group, false);

        icon.mFolderName = (BubbleTextView) icon.findViewById(R.id.folder_icon_name);
        icon.mFolderName.setText(folderInfo.title);
        icon.mPreviewBackground = (ImageView) icon.findViewById(R.id.preview_background);

        icon.setTag(folderInfo);
        icon.setOnClickListener(launcher);
        icon.mInfo = folderInfo;
        icon.mLauncher = launcher;
        mIsSupportCard = launcher.getIconManager().supprtCardIcon();
        icon.setContentDescription(String.format(launcher.getString(R.string.folder_name_format),
                folderInfo.title));
        Folder folder = Folder.fromXml(launcher);
        folder.setDragController(launcher.getDragController());
        folder.setFolderIcon(icon);
        folder.bind(folderInfo);
        icon.mFolder = folder;

        icon.mFolderRingAnimator = new FolderRingAnimator(launcher, icon);
        folderInfo.addListener(icon);

        return icon;
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        sStaticValuesDirty = true;
        return super.onSaveInstanceState();
    }

    public static class FolderRingAnimator {
        public int mCellX;
        public int mCellY;
        private CellLayout mCellLayout;
        public float mOuterRingSize;
        public float mOuterRingHeightScale = 1;
        public FolderIcon mFolderIcon = null;
        public Drawable mOuterRingDrawable = null;
        public static Drawable sSharedOuterRingDrawable = null;
        public static int sPreviewSize = -1;
        public static int sPreviewPadding = -1;
        public static int sPreviewOffset = -1;

        private ValueAnimator mAcceptAnimator;
        private ValueAnimator mNeutralAnimator;
        private boolean mFolderAnimatorAccepted;
        
        private static Bitmap mNormalPreview = null;
        private static int mNormalPreviewSize = -1;
        private static int mNormalPreviewOffset = -1;
        private static Drawable mCardPreview = null;
        private static int mCardPreviewSize = -1;
        private static int mCardPreviewOffset = -1;
        private Context mContext = null;
        private Resources mResources = null;
        public FolderRingAnimator(Launcher launcher, FolderIcon folderIcon) {
            mFolderIcon = folderIcon;
            mContext = launcher;
            mResources = mContext.getResources();
            mOuterRingDrawable = mResources.getDrawable(R.drawable.card_folder_bg);
            // We need to reload the static values when configuration changes in case they are
            // different in another configuration
            if (sStaticValuesDirty) {
                mNormalPreviewSize = mResources.getDimensionPixelSize(R.dimen.folder_preview_size);
                mNormalPreviewOffset = mResources.getDimensionPixelSize(R.dimen.workspace_cell_width);
                mNormalPreview = Bitmap.createBitmap(mNormalPreviewSize, mNormalPreviewSize, Config.ARGB_8888);
                mCardPreview = mResources.getDrawable(R.drawable.card_folder_bg);
                mCardPreviewSize = mResources.getDimensionPixelSize(R.dimen.workspace_cell_width);
                mCardPreviewOffset = mResources.getDimensionPixelSize(R.dimen.workspace_cell_height);
                sPreviewPadding = mResources.getDimensionPixelSize(R.dimen.folder_preview_padding);
                sSharedFolderLeaveBehind = mResources.getDrawable(R.drawable.portal_ring_rest);
                sStaticValuesDirty = false;
                if (AgedModeUtil.isAgedMode()) {
                    mNormalPreviewSize *= AgedModeUtil.SCALE_RATIO_FOR_AGED_MODE;
                    mNormalPreviewOffset *= AgedModeUtil.SCALE_RATIO_FOR_AGED_MODE;
                    mCardPreviewSize *= AgedModeUtil.SCALE_RATIO_FOR_AGED_MODE;
                    mCardPreviewOffset *= AgedModeUtil.SCALE_RATIO_FOR_AGED_MODE;
                }
            }
        }

        public static void refreshStaticValues() {
            Resources res = LauncherApplication.getContext().getResources();
            if (AgedModeUtil.isAgedMode()) {
                mNormalPreviewSize = (int) (res.getDimensionPixelSize(R.dimen.folder_preview_size)
                        * AgedModeUtil.SCALE_RATIO_FOR_AGED_MODE);
                mNormalPreviewOffset = (int) (res
                        .getDimensionPixelSize(R.dimen.workspace_cell_width) * AgedModeUtil.SCALE_RATIO_FOR_AGED_MODE);
                mCardPreviewSize = (int) (res.getDimensionPixelSize(R.dimen.workspace_cell_width) * AgedModeUtil.SCALE_RATIO_FOR_AGED_MODE);
                mCardPreviewOffset = (int) (res
                        .getDimensionPixelSize(R.dimen.workspace_cell_height) * AgedModeUtil.SCALE_RATIO_FOR_AGED_MODE);
            } else {
                mNormalPreviewSize = res.getDimensionPixelSize(R.dimen.folder_preview_size);
                mNormalPreviewOffset = res.getDimensionPixelSize(R.dimen.workspace_cell_width);
                mCardPreviewSize = res.getDimensionPixelSize(R.dimen.workspace_cell_width);
                mCardPreviewOffset = res.getDimensionPixelSize(R.dimen.workspace_cell_height);
            }

        }

        public void animateToAcceptState() {
            if (mNeutralAnimator != null) {
                mNeutralAnimator.cancel();
            }
            
            if(((LauncherApplication)(mContext.getApplicationContext())).getIconManager().supprtCardIcon()){
                sPreviewSize = mCardPreviewSize;
                sPreviewOffset = mCardPreviewOffset;
                sSharedOuterRingDrawable = mCardPreview;
                mOuterRingHeightScale = 1.25f;
                if (AgedModeUtil.isAgedMode()) {
                    OUTER_RING_GROWTH_FACTOR = 0.1f;
                } else {
                    OUTER_RING_GROWTH_FACTOR = 0.15f;
                }
            }else{
                sPreviewSize = mNormalPreviewSize;
                sPreviewOffset =mNormalPreviewOffset;
                sSharedOuterRingDrawable =  ((LauncherApplication)mContext.getApplicationContext()).getIconManager().buildUnifiedIcon(mNormalPreview, IconManager.ICON_TYPE_FOLDER);
                mOuterRingHeightScale = 1f;
                OUTER_RING_GROWTH_FACTOR = 0.3f;
            }
            
            mAcceptAnimator = LauncherAnimUtils.ofFloat(mCellLayout, 0f, 1f);
            mAcceptAnimator.setDuration(CONSUMPTION_ANIMATION_DURATION);

            final int previewSize = sPreviewSize;
            mAcceptAnimator.addUpdateListener(new AnimatorUpdateListener() {
                public void onAnimationUpdate(ValueAnimator animation) {
                    final float percent = (Float) animation.getAnimatedValue();
                    mOuterRingSize = (1 + percent * OUTER_RING_GROWTH_FACTOR) * previewSize;
                    if (mCellLayout != null) {
                        mCellLayout.invalidate();
                    }
                }
            });
            mAcceptAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animation) {
                    mFolderAnimatorAccepted = true;
                    if (mFolderIcon != null) {
                        mFolderIcon.mPreviewBackground.setVisibility(INVISIBLE);
                        mFolderIcon.mDropMode = true;
                    }
                }
            });
            mAcceptAnimator.start();
        }

        public ValueAnimator getAcceptAnimator() {
            return mAcceptAnimator;
        }

        public void animateToNaturalState() {
            if (mAcceptAnimator != null) {
                mAcceptAnimator.cancel();
            }
            
            if (mNeutralAnimator != null && mNeutralAnimator.isRunning()) {
                return;
            }
            if(!mFolderAnimatorAccepted) {
                return;
            }
            
            mNeutralAnimator = LauncherAnimUtils.ofFloat(mCellLayout, 0f, 1f);
            mNeutralAnimator.setDuration(CONSUMPTION_ANIMATION_DURATION);
            
            if(((LauncherApplication)(mContext.getApplicationContext())).getIconManager().supprtCardIcon()){
                sPreviewSize = mCardPreviewSize;
                sPreviewOffset = mCardPreviewOffset;
                sSharedOuterRingDrawable = mCardPreview;
                mOuterRingHeightScale = 1.25f;
                if (AgedModeUtil.isAgedMode()) {
                    OUTER_RING_GROWTH_FACTOR = 0.1f;
                } else {
                    OUTER_RING_GROWTH_FACTOR = 0.15f;
                }
            }else{
                sPreviewSize = mNormalPreviewSize;
                sPreviewOffset =mNormalPreviewOffset;
                sSharedOuterRingDrawable =  ((LauncherApplication)mContext.getApplicationContext()).getIconManager().buildUnifiedIcon(mNormalPreview, IconManager.ICON_TYPE_FOLDER);
                mOuterRingHeightScale = 1f;
                OUTER_RING_GROWTH_FACTOR = 0.3f;
            }

            final int previewSize = sPreviewSize;
            mNeutralAnimator.addUpdateListener(new AnimatorUpdateListener() {
                public void onAnimationUpdate(ValueAnimator animation) {
                    final float percent = (Float) animation.getAnimatedValue();
                    mOuterRingSize = (1 + (1 - percent) * OUTER_RING_GROWTH_FACTOR) * previewSize;
                    if (mCellLayout != null) {
                        mCellLayout.invalidate();
                    }
                }
            });
            mNeutralAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    if (mCellLayout != null) {
                        mCellLayout.hideFolderAccept(FolderRingAnimator.this);
                    }
                    if (mFolderIcon != null) {
                        mFolderIcon.mPreviewBackground.setVisibility(VISIBLE);
                        mFolderIcon.mDropMode = false;
                    }
                }
            });
            mNeutralAnimator.start();
        }

        // Location is expressed in window coordinates
        public void getCell(int[] loc) {
            loc[0] = mCellX;
            loc[1] = mCellY;
        }

        // Location is expressed in window coordinates
        public void setCell(int x, int y) {
            mCellX = x;
            mCellY = y;
        }

        public void setCellLayout(CellLayout layout) {
            mCellLayout = layout;
        }

        public float getOuterRingSize() {
            return mOuterRingSize;
        }
        
        public float getOuterOutlineWidth() {
            return mOuterRingSize;
        }
        
        public float getOuterOutlineHeight() {
        	return mOuterRingSize * mOuterRingHeightScale;
        }

    }

    public Folder getFolder() {
        return mFolder;
    }

    public FolderInfo getFolderInfo() {
        return mInfo;
    }

    private boolean willAcceptItem(ItemInfo item) {
        final int itemType = item.itemType;
        return ((itemType == LauncherSettings.Favorites.ITEM_TYPE_APPLICATION ||
                itemType == LauncherSettings.Favorites.ITEM_TYPE_SHORTCUT ||
                itemType == LauncherSettings.Favorites.ITEM_TYPE_SHORTCUT_DOWNLOADING ||
                itemType == LauncherSettings.Favorites.ITEM_TYPE_VPINSTALL) &&
                !mFolder.isFull() && item != mInfo && !mInfo.opened);
    }

    public boolean acceptDrop(Object dragInfo) {
        final ItemInfo item = (ItemInfo) dragInfo;
        return !mFolder.isDestroyed() && willAcceptItem(item);
    }

    public void addItem(ShortcutInfo item) {
        String title = "";
        if(mInfo.count() == 1) {
            ShortcutInfo oneItem = mInfo.contents.get(0);
            if(oneItem.itemType == Favorites.ITEM_TYPE_SHORTCUT_DOWNLOADING) {
                if(item.itemType == Favorites.ITEM_TYPE_SHORTCUT_DOWNLOADING) {
                    title = getResources().getString(R.string.str_folder_name);
                } else {
                    title = item.title
                            + getResources().getString(R.string.folder_name_etc);
                }
                if(!AppGroupManager.isSwitchOn()) {
                    mInfo.title = null;
                }
            } else {
                title = oneItem.title
                        + getResources().getString(R.string.folder_name_etc);
            }
        } else {
            if(item.itemType == Favorites.ITEM_TYPE_SHORTCUT_DOWNLOADING) {
                title = getResources().getString(R.string.str_folder_name);
            } else {
                title = item.title
                        + getResources().getString(R.string.folder_name_etc);
            }
        }
        setTitle(title);
        if (mInfo.count() == 0 && mDropMode) {
            postDelayed(new Runnable() {
                public void run() {
                    mDropMode = false;
                    invalidate();
                }
            }, CONSUMPTION_ANIMATION_DURATION - 50);
        }
        mInfo.add(item);
    }

    public void setTitle(String title) {
        if (mInfo.title == null || mInfo.title.toString().isEmpty()) {
            if(title != null) {
                title = title.trim();
            }
            mInfo.setTitle(title);
            mFolder.updateFolderName(title);

            UserTrackerHelper.sendUserReport(UserTrackerMessage.MSG_ADD_FOLDER,
                    title);
        }
    }
    public void onDragEnter(Object dragInfo) {
        if (mFolder.isDestroyed() || !willAcceptItem((ItemInfo) dragInfo)) return;
        CellLayout.LayoutParams lp = (CellLayout.LayoutParams) getLayoutParams();
        CellLayout layout = (CellLayout) getParent().getParent();
        mFolderRingAnimator.setCell(lp.cellX, lp.cellY);
        mFolderRingAnimator.setCellLayout(layout);
        mFolderRingAnimator.animateToAcceptState();
        layout.showFolderAccept(mFolderRingAnimator);
    }

    public void onDragOver(Object dragInfo) {
    }

    public void performCreateAnimation(final ShortcutInfo destInfo, final View destView,
            final ShortcutInfo srcInfo, final DragView srcView, Rect dstRect,
            float scaleRelativeToDragLayer, Runnable postAnimationRunnable) {

        // These correspond two the drawable and view that the icon was dropped _onto_
        Drawable animateDrawable = ((BubbleTextView) destView).getIconDrawable();
        computePreviewDrawingParams(animateDrawable.getIntrinsicWidth(),
                destView.getMeasuredWidth());

        // This will animate the first item from it's position as an icon into its
        // position as the first item in the preview
        animateFirstItem(animateDrawable, INITIAL_ITEM_ANIMATION_DURATION, false, null);
        addItem(destInfo);

        // This will animate the dragView (srcView) into the new folder
        onDrop(srcInfo, srcView, dstRect, scaleRelativeToDragLayer, 1, postAnimationRunnable, null);
    }

    public void performDestroyAnimation(final View finalView, Runnable onCompleteRunnable) {
        Drawable animateDrawable = ((BubbleTextView) finalView).getIconDrawable();
        computePreviewDrawingParams(animateDrawable.getIntrinsicWidth(), 
                finalView.getMeasuredWidth());

        // This will animate the first item from it's position as an icon into its
        // position as the first item in the preview
        animateFirstItem(animateDrawable, FINAL_ITEM_ANIMATION_DURATION, true,
                onCompleteRunnable);
    }

    public void onDragExit(Object dragInfo) {
        onDragExit();
    }

    public void onDragExit() {
        mFolderRingAnimator.animateToNaturalState();
    }

    private void onDrop(final ShortcutInfo item, DragView animateView, Rect finalRect,
            float scaleRelativeToDragLayer, int index, Runnable postAnimationRunnable,
            DragObject d) {

        if (item.container != mInfo.id) {
            item.cellX = -1;
            item.cellY = -1;
        }
        

        // Typically, the animateView corresponds to the DragView; however, if this is being done
        // after a configuration activity (ie. for a Shortcut being dragged from AllApps) we
        // will not have a view to animate
        if (animateView != null) {
            DragLayer dragLayer = mLauncher.getDragLayer();
            Rect from = new Rect();
            dragLayer.getViewRectRelativeToSelf(animateView, from);
            Rect to = finalRect;
            if (to == null) {
                to = new Rect();
                Workspace workspace = mLauncher.getWorkspace();
                // Set cellLayout and this to it's final state to compute final animation locations
                workspace.setFinalTransitionTransform((CellLayout) getParent().getParent());
                float scaleX = getScaleX();
                float scaleY = getScaleY();
                setScaleX(1.0f);
                setScaleY(1.0f);
                scaleRelativeToDragLayer = dragLayer.getDescendantRectRelativeToSelf(this, to);
                // Finished computing final animation locations, restore current state
                setScaleX(scaleX);
                setScaleY(scaleY);
                workspace.resetTransitionTransform((CellLayout) getParent().getParent());
            }

            float[] center = new float[2];
            float scale = mFolder.getLocalCenterForIndex(index, center, animateView.getMeasuredWidth(), index >= NUM_ITEMS_IN_PREVIEW );
            center[0] *=  scaleRelativeToDragLayer;
            center[1] *= scaleRelativeToDragLayer;

            to.offset((int)center[0] - animateView.getMeasuredWidth() / 2,
                    (int)center[1] - animateView.getMeasuredHeight() / 2);

            float finalAlpha = index < NUM_ITEMS_IN_PREVIEW ? 0.5f : 0f;

            float finalScale = scale * scaleRelativeToDragLayer;
            dragLayer.animateView(animateView, from, to, finalAlpha,
                    1, 1, finalScale, finalScale, DROP_IN_ANIMATION_DURATION,
                    new DecelerateInterpolator(2), new AccelerateInterpolator(2),
                    postAnimationRunnable, DragLayer.ANIMATION_END_DISAPPEAR, null);
            addItem(item);
            mHiddenItems.add(item);
            postDelayed(new Runnable() {
                public void run() {
                    mHiddenItems.remove(item);
                    invalidate();
                }
            }, DROP_IN_ANIMATION_DURATION);
        } else {
            addItem(item);
        }
        if(CheckVoiceCommandPressHelper.PUSH_TO_TALK_SUPPORT) {
            CheckVoiceCommandPressHelper.getInstance().forceDismissVoiceCommand();
            mLauncher.setOnClickValid(true);
        }
    }

    public void onDrop(DragObject d) {
        ShortcutInfo item;
        if (d.dragInfo instanceof ApplicationInfo) {
            // Came from all apps -- make a copy
            item = ((ApplicationInfo) d.dragInfo).makeShortcut();
        } else {
            item = (ShortcutInfo) d.dragInfo;
        }
        mFolder.notifyDrop();
        onDrop(item, d.dragView, null, 1.0f, mInfo.contents.size(), d.postAnimationRunnable, d);
    }

    public DropTarget getDropTargetDelegate(DragObject d) {
        return null;
    }

    private void computePreviewDrawingParams(int drawableSize, int totalSize) {
        if (mIntrinsicIconSize != drawableSize || mTotalWidth != totalSize) {
            mIntrinsicIconSize = drawableSize;
            mTotalWidth = totalSize;

            final int previewSize = FolderRingAnimator.sPreviewSize;
            final int previewPadding = FolderRingAnimator.sPreviewPadding;

            mAvailableSpaceInPreview = (previewSize - 2 * previewPadding);
            // cos(45) = 0.707  + ~= 0.1) = 0.8f
            int adjustedAvailableSpace = (int) ((mAvailableSpaceInPreview / 2) * (1 + 0.8f));

            int unscaledHeight = (int) (mIntrinsicIconSize * (1 + PERSPECTIVE_SHIFT_FACTOR));
            mBaselineIconScale = (1.0f * adjustedAvailableSpace / unscaledHeight);

            mBaselineIconSize = (int) (mIntrinsicIconSize * mBaselineIconScale);
            mMaxPerspectiveShift = mBaselineIconSize * PERSPECTIVE_SHIFT_FACTOR;

            mPreviewOffsetX = (mTotalWidth - mAvailableSpaceInPreview) / 2;
            mPreviewOffsetY = previewPadding;
        }
    }

    private void computePreviewDrawingParams(Drawable d) {
        if(d != null)
        computePreviewDrawingParams(d.getIntrinsicWidth(), getMeasuredWidth());
    }

    class PreviewItemDrawingParams {
        PreviewItemDrawingParams(float transX, float transY, float scale, int overlayAlpha) {
            this.transX = transX;
            this.transY = transY;
            this.scale = scale;
            this.overlayAlpha = overlayAlpha;
        }
        float transX;
        float transY;
        float scale;
        int overlayAlpha;
        Drawable drawable;
    }

    private float getLocalCenterForIndex(int index, int[] center) {
        mParams = computePreviewItemDrawingParams(Math.min(NUM_ITEMS_IN_PREVIEW, index), mParams);

        mParams.transX += mPreviewOffsetX;
        mParams.transY += mPreviewOffsetY;
        float offsetX = mParams.transX + (mParams.scale * mIntrinsicIconSize) / 2;
        float offsetY = mParams.transY + (mParams.scale * mIntrinsicIconSize) / 2;

        center[0] = (int) Math.round(offsetX);
        center[1] = (int) Math.round(offsetY);
        return mParams.scale;
    }

    private PreviewItemDrawingParams computePreviewItemDrawingParams(int index,
            PreviewItemDrawingParams params) {
        index = NUM_ITEMS_IN_PREVIEW - index - 1;
        float r = (index * 1.0f) / (NUM_ITEMS_IN_PREVIEW - 1);
        float scale = (1 - PERSPECTIVE_SCALE_FACTOR * (1 - r));

        float offset = (1 - r) * mMaxPerspectiveShift;
        float scaledSize = scale * mBaselineIconSize;
        float scaleOffsetCorrection = (1 - scale) * mBaselineIconSize;

        // We want to imagine our coordinates from the bottom left, growing up and to the
        // right. This is natural for the x-axis, but for the y-axis, we have to invert things.
        float transY = mAvailableSpaceInPreview - (offset + scaledSize + scaleOffsetCorrection);
        float transX = offset + scaleOffsetCorrection;
        float totalScale = mBaselineIconScale * scale;
        final int overlayAlpha = (int) (80 * (1 - r));

        if (params == null) {
            params = new PreviewItemDrawingParams(transX, transY, totalScale, overlayAlpha);
        } else {
            params.transX = transX;
            params.transY = transY;
            params.scale = totalScale;
            params.overlayAlpha = overlayAlpha;
        }
        return params;
    }

    private void drawPreviewItem(Canvas canvas, PreviewItemDrawingParams params) {
        canvas.save();
        canvas.translate(params.transX + mPreviewOffsetX, params.transY + mPreviewOffsetY);
        canvas.scale(params.scale, params.scale);
        Drawable d = params.drawable;

        if (d != null) {
            d.setBounds(0, 0, mIntrinsicIconSize, mIntrinsicIconSize);
            d.setFilterBitmap(true);
            d.setColorFilter(Color.argb(params.overlayAlpha, 0, 0, 0), PorterDuff.Mode.SRC_ATOP);
            d.draw(canvas);            
            d.clearColorFilter();
            d.setFilterBitmap(false);
        }
        canvas.restore();
    }

    private void drawAliPreviewItem(Canvas canvas, int i, Drawable d, boolean rtl) {
        // TODO Auto-generated method stub
        if (i < 0 || i >= NUM_ITEMS_IN_PREVIEW) {
            return;
        }
        if(i == 0 ) {
            if (mThemeBackground == null) {
                mThemeBackground = buildThemeBackground(mPreiewSize, mPreiewSize);
            }
        }

        int y = i / NUM_ITEMS_IN_PREVIEW_LINE;
        int x = i % NUM_ITEMS_IN_PREVIEW_LINE;
        if (rtl) x = NUM_ITEMS_IN_PREVIEW_LINE - x - 1;

        int canvasWidth = getWidth();//canvas.getWidth();
        int previewSize = mThemeBackground.getWidth();
        if(previewSize <= 0) {
            previewSize = mPreiewSize;
        }
        int gap = previewSize / 15;
        int padding = previewSize / 6;
        int offset = (canvasWidth - previewSize) / 2;
        int offsetvertical = 0;
        if (d != null) {
            offsetvertical = (canvasWidth - d.getIntrinsicWidth()) / 2;
        }
        int cell = (previewSize - padding * 2 - (NUM_ITEMS_IN_PREVIEW_LINE - 1)
                * gap)
                / NUM_ITEMS_IN_PREVIEW_LINE;

        if (i == 0 && isDrawThemeBackground()) {
            canvas.drawBitmap(mThemeBackground, offset, offsetvertical, null);
        }

        if (d != null) {

            Rect bounds = d.getBounds();
            int l = bounds.left;
            int b = bounds.bottom;
            int t = bounds.top;
            int r = bounds.right;
            
            d.setBounds(offset + padding + x * gap + x * cell, offsetvertical + padding
                    + y * gap + y * cell, offset + padding + x * gap + (x + 1)
                    * cell, offsetvertical + padding + y * gap + (y + 1) * cell);
            
            d.draw(canvas);

            d.setBounds(l, t, r, b);
        }

    }

    private boolean isDrawThemeBackground() {
        ValueAnimator acceptAmimator = mFolderRingAnimator.getAcceptAnimator();
        if (acceptAmimator != null) {
            return !acceptAmimator.isRunning();
        }

        return true;
    }

    private boolean mHostseatMode = false;
    public void setHotseatMode(boolean hotseat){
        mHostseatMode = hotseat;
      // force textview update
        mFolderName.setText(mFolderName.getText());
        if(!hotseat) {
            resetTempPadding();
        }
    }

    /* return if this folder icon is in hotseat */
    public boolean isInHotseat(){
        return mHostseatMode;
    }

    private void drawPreviewItem(Canvas canvas, int i, View v, boolean rtl) {
        if (i < 0 || i >= NUM_ITEMS_IN_PREVIEW) {
            return;
        }
        
        if (i == 0) {
            if(mCardBackground == null){
                mCardBackground = getResources().getDrawable(R.drawable.card_folder_bg);
            }
            mCardBackground.setBounds(0,0,getWidth(),getHeight());
            mCardBackground.draw(canvas);
        }

        int y = i / NUM_ITEMS_IN_PREVIEW_LINE;
        int x = i % NUM_ITEMS_IN_PREVIEW_LINE;
        if (rtl) x = NUM_ITEMS_IN_PREVIEW_LINE - x - 1;

        float density = getResources().getDisplayMetrics().density;
        int cw = getWidth();//canvas.getWidth();
        int paddingX = (int) (density * 9);
        int paddingY = (int) (density * 7);
        int gap = (int) (density * 4);
        int ow = getResources().getDimensionPixelSize(R.dimen.workspace_cell_width);
        int oh = getResources().getDimensionPixelSize(R.dimen.workspace_cell_height);
        int w = (cw - paddingX * 2 - (NUM_ITEMS_IN_PREVIEW_LINE - 1) * gap)
                / NUM_ITEMS_IN_PREVIEW_LINE;
        float scale = (float) w / ow;
        int h = (int) (scale * oh);
        int ox = (w - ow) / 2;
        int oy = (h - oh) / 2;
        if (v != null) {
            canvas.save();
            canvas.translate(paddingX + x * (w + gap) + ox, paddingY + y * (h + gap) + oy);
            canvas.scale(scale, scale, (float) ow / 2, (float) oh / 2);
            ((BubbleTextView)v).drawBubbleTextViewInFolderIcon(ow, oh, canvas);
            canvas.restore();
        }
    }
	@Deprecated
    protected void dispatchDrawOld(Canvas canvas) {
        mFolderName.setVisibility(View.INVISIBLE);
        super.dispatchDraw(canvas);

        if (mFolder == null) return;
        // Troubleshoot the problem of folder's animation
        // whether the icons are drawn, it should draw the background
        if (mIsSupportCard && mFolder.getItemCount() == 0
                && mNeedDrawEmptyFolderOnce) {
            drawEmptyFolder(canvas);
            mNeedDrawEmptyFolderOnce = false;
        }
        if (mFolder.getItemCount() == 0 && !mAnimating) return;

        ArrayList<View> items = mFolder.getItemsInReadingOrder(false);
        Drawable d;
        TextView v;

        // Update our drawing parameters if necessary
        if (mAnimating) {
            computePreviewDrawingParams(mAnimParams.drawable);
        } else {
            v = (TextView) items.get(0);
            d = v.getCompoundDrawables()[1];
            computePreviewDrawingParams(d);
        }
        
        int msgNumTotal = 0;
        boolean newFlag = false;
        if (!mAnimating) {
            boolean rtl = getResources().getConfiguration().getLayoutDirection() == LAYOUT_DIRECTION_RTL;
            for (int i = 0; i < items.size(); i++) {
                v = (TextView) items.get(i);
                if (!mHiddenItems.contains(v.getTag())) {
                    if(mIsSupportCard) {
                        drawPreviewItem(canvas, i, v, rtl);
                    } else {
                        d = v.getCompoundDrawables()[1];
                        drawAliPreviewItem(canvas, i, d, rtl);
                    }
                }
                //show new flag error
				msgNumTotal += ((ShortcutInfo)v.getTag()).messageNum;
                if(((ShortcutInfo)v.getTag()).isNewItem()) {
                    newFlag = true;
                }
            }
        } else {
            drawPreviewItem(canvas, mAnimParams);
        }
        
        // show message number on folder 
        String mesNum = String.valueOf(msgNumTotal);
		if (msgNumTotal > 99) {
			mesNum = "99+";
		}
        final Paint paint = mNumberPaint;
        int alpha = paint.getAlpha();
        // new mark is more prior than message number
        
        int width = 0;
        if(mHostseatMode){
            width = getResources().getDimensionPixelSize(R.dimen.hotseat_cell_width);
        }else{
            width = getResources().getDimensionPixelSize(R.dimen.workspace_cell_width);
        }
        INDICATOR_BOUNDRY_X = width - mImgRTCorner.getWidth()-15;
        // for settings show icon mark
        boolean isShowIcon = LauncherModel.isShowNewMarkIcon();
        if (newFlag && isShowIcon) {
            Point tempPoint = calcStringPosition(NEW_MARK_SHORTCUT,mTextSize,mImgRTCorner,mNewMarkPaint);
            mNewMarkDrawX = tempPoint.x;
            mNewMarkDrawY = tempPoint.y;
            canvas.drawBitmap(mImgRTCorner,
                    INDICATOR_BOUNDRY_X,
                    INDICATOR_BOUNDRY_Y,
                    paint);
            paint.setTextSize(IND_TEXT_SIZE_NORMAL);
            canvas.drawText(NEW_MARK_SHORTCUT,
                    mNewMarkDrawX,
                    mNewMarkDrawY,
                    mNewMarkPaint);
        	
        }else if (mesNum != null && !mesNum.isEmpty() && msgNumTotal > 0) {
            calcNumberPosition(mesNum);
            canvas.drawBitmap(mImgRTCorner,
                              INDICATOR_BOUNDRY_X,
                              INDICATOR_BOUNDRY_Y,
                              paint);
            
            if (mesNum != null && !mesNum.isEmpty()) {
                paint.setTextSize(mNumberSize);
                canvas.drawText(mesNum,
                		mNumIndicatorNumDrawX,            
                		mNumIndicatorNumDrawY,             
                        paint);
            }
        }
        paint.setAlpha(alpha);
        if(mIsSupportCard && !mHostseatMode){
            mCardCover.draw(canvas);
        }
        
        canvas.translate(0, mTextPaddingTop);
        drawChild(canvas, mFolderName, 0);
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        final int scrollX = getScrollX();
        final int scrollY = getScrollY();
        int width = getWidth();
        int height = getHeight();
        canvas.translate(scrollX, scrollY);
        boolean supportCard = mIsSupportCard;
        if (!mAnimating) {
            mIconMarkNumCount = mFolder.getMarkNumCount();
            if (mIconMarkNumCount > 99) {
                mNumberSize = IND_NUM_SIZE_SMALL;
            } else {
                mNumberSize = IND_NUM_SIZE_NORMAL;
            }
        }

        final int realW = mPreiewSize;
        canvas.save();
        if (!supportCard) {//hide icon when playing close folder animation
            if (mThemeBackground == null) {
                mThemeBackground = buildThemeBackground(realW, realW);
            }
            int bmpW = mThemeBackground.getWidth();
            int iconW = bmpW > 0 ? bmpW : realW;
            int x = (width - iconW) / 2;
            int y = mHostseatMode ? BubbleTextView.sTopPaddingHotseat : x;
            canvas.translate(x, y);
            if (!mDropMode)
                canvas.drawBitmap(mThemeBackground, 0, 0, null);
            int padding = mPreiewPadding + (iconW - realW) / 2;
            canvas.translate(padding, padding);
            if (mFolder != null) {
                mFolder.drawFolderIcon(canvas, realW, realW, mPreiewPadding, x + padding, mHostseatMode);
            }
        } else {
            if (mFolder != null) {
                if (mCardBackground == null)
                    mCardBackground = getResources().getDrawable(R.drawable.card_folder_bg);
                if (mHostseatMode && !AgedModeUtil.isAgedMode()) {
                    float x = ((float)width - realW) / 2;
                    float y = BubbleTextView.sTopPaddingHotseat;
                    canvas.translate(x, y);
                    if (!mDropMode) {
                        mCardBackground.setBounds(0, 0, realW, realW);
                        mCardBackground.draw(canvas);
                    }
                    mFolder.drawFolderIcon(canvas, realW, realW, 0, x, mHostseatMode);
                } else {
                    if (!mDropMode) {
                        mCardBackground.setBounds(0, 0, width, height);
                        mCardBackground.draw(canvas);
                    }
                    if (AgedModeUtil.isAgedMode()) {
                        mFolder.drawFolderIcon(canvas, width, height, mPreiewPadding,
                                mPreiewPadding, mHostseatMode);
                    } else {
                        mFolder.drawFolderIcon(canvas, width, height, 0, 0, mHostseatMode);
                    }
                }
            }
        }
        canvas.restore();

        if (!mHideIcon) {
            final Paint paint = mNumberPaint;
            if (mIsTempPadding && mHostseatMode) {
                INDICATOR_BOUNDRY_X -= mTempPaddingInHotseat;
            }
            String language = getResources().getConfiguration().locale.getLanguage();
            if ("zh".equals(language)) {
            	mTextSize = IND_TEXT_SIZE_NORMAL;
    	    } else {
    	        mTextSize = IND_TEXT_SIZE_SMALL;
    	    }
            /*if (mIsNew && LauncherModel.isShowNewMarkIcon()) {
        	final String newMark = getResources().getString(R.string.new_mark_shortcut);
        	canvas.drawBitmap(mImgRTCorner, INDICATOR_BOUNDRY_X, INDICATOR_BOUNDRY_Y, paint);
                Point tempPoint = calcStringPosition(newMark, mTextSize, mImgRTCorner,
                        mNewMarkPaint);
                mNewMarkDrawX = tempPoint.x;
                mNewMarkDrawY = tempPoint.y;
                mNewMarkPaint.setTextSize(mTextSize);
                canvas.drawText(newMark,
                        mNewMarkDrawX, mNewMarkDrawY, mNewMarkPaint);

            } else*/
            if (mIconMarkNumCount > 0) {
                String string = (mIconMarkNumCount > 99 ? "99+" : String.valueOf(mIconMarkNumCount));
                calcNumberPosition(string);
                canvas.drawBitmap(mImgRTCorner, INDICATOR_BOUNDRY_X, INDICATOR_BOUNDRY_Y,
                        paint);
                paint.setTextSize(mNumberSize);
                canvas.drawText(string, mNumIndicatorNumDrawX, mNumIndicatorNumDrawY, paint);
            } /*else if (mNotificationCount > 0 && LauncherModel.showNotificationMark() ){
                mNewMarkPaint.setTextSize(mTextSize);
                canvas.drawBitmap(mImgRTCorner, INDICATOR_BOUNDRY_X, INDICATOR_BOUNDRY_Y, paint);
                String strNoticeCount = String.valueOf(mNotificationCount);
                Point p = calcStringPosition(strNoticeCount, mTextSize, mImgRTCorner, mNewMarkPaint);
                canvas.drawText(strNoticeCount, p.x, p.y, mNewMarkPaint);
            }*/

            if (mIsTempPadding && mHostseatMode) {
                INDICATOR_BOUNDRY_X += mTempPaddingInHotseat;
            }
        }

        if (!mDropMode) {
            if (supportCard && !mHostseatMode && mCardCover != null) {
                mCardCover.setBounds(0, 0, width, height);
                mCardCover.draw(canvas);
            }
            drawChild(canvas, mFolderName, 0);
        }
    }

    boolean mHideIcon;
    boolean mDropMode;
    private int mIconMarkNumCount;

    public void setHideIcon(boolean hide) {
        if(mHideIcon != hide) {
            mHideIcon = hide;
            invalidate();
        }
    }

    private void animateFirstItem(final Drawable d, int duration, final boolean reverse,
            final Runnable onCompleteRunnable) {
        final PreviewItemDrawingParams finalParams = computePreviewItemDrawingParams(0, null);

        final float scale0 = 1.0f;
        final float transX0 = (mAvailableSpaceInPreview - d.getIntrinsicWidth()) / 2;
        final float transY0 = (mAvailableSpaceInPreview - d.getIntrinsicHeight()) / 2;
        mAnimParams.drawable = d;

        ValueAnimator va = LauncherAnimUtils.ofFloat(this, 0f, 1.0f);
        va.addUpdateListener(new AnimatorUpdateListener(){
            public void onAnimationUpdate(ValueAnimator animation) {
                float progress = (Float) animation.getAnimatedValue();
                if (reverse) {
                    progress = 1 - progress;
                    mPreviewBackground.setAlpha(progress);
                }

                mAnimParams.transX = transX0 + progress * (finalParams.transX - transX0);
                mAnimParams.transY = transY0 + progress * (finalParams.transY - transY0);
                mAnimParams.scale = scale0 + progress * (finalParams.scale - scale0);
                invalidate();
            }
        });
        va.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                mAnimating = true;
            }
            @Override
            public void onAnimationEnd(Animator animation) {
                mAnimating = false;
                if (onCompleteRunnable != null) {
                    onCompleteRunnable.run();
                }
            }
        });
        va.setDuration(duration);
        va.start();
    }

    public void setTextVisible(boolean visible) {
        // if (visible) {
        // mFolderName.setVisibility(VISIBLE);
        // } else {
        // mFolderName.setVisibility(INVISIBLE);
        // }
    }

    public boolean getTextVisible() {
        return mFolderName.getVisibility() == VISIBLE;
    }

    public void onItemsChanged() {
        invalidate();
        requestLayout();
    }

    public void invalidateFolder(View v, ShortcutInfo info) {
        for (int i = 0, N = mInfo.contents.size(); i < N; i++) {
            View view = mFolder.getItemAt(i);
            if(view != null) {
                if (info == view.getTag()) {
                    invalidate();
                    break;
                }
            }
        }
    }

    public void onAdd(ShortcutInfo item) {
        invalidate();
        requestLayout();
    }

    public void onRemove(ShortcutInfo item) {
        invalidate();
        requestLayout();
    }

    public void onTitleChanged(CharSequence title) {
        mFolderName.setText(title.toString());
        setContentDescription(String.format(getContext().getString(R.string.folder_name_format),
                title));
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // Call the superclass onTouchEvent first, because sometimes it changes the state to
        // isPressed() on an ACTION_UP
        boolean result = super.onTouchEvent(event);
        mDetector.onTouchEvent(event);

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mLongPressHelper.postCheckForLongPress();
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                mLongPressHelper.cancelLongPress();
                break;
        }
        return result;
    }

    @Override
    public void cancelLongPress() {
        super.cancelLongPress();

        mLongPressHelper.cancelLongPress();
    }
    
    public void updateView() {
        if(mIsSupportCard = mLauncher.getIconManager().supprtCardIcon()) {
            if (mCardCover == null)
                mCardCover = getResources().getDrawable(R.drawable.card_folder_cover);
            if (mCardBackground == null)
                mCardBackground = getResources().getDrawable(R.drawable.card_folder_bg);
        } else {
            mThemeBackground = buildThemeBackground(mPreiewSize, mPreiewSize);
        }
        requestLayout();
        invalidate();
    }

    // Troubleshoot the problem of folder's animation
    // whether the icons are drawn, it should draw the background
    private boolean mNeedDrawEmptyFolderOnce;

    public void setNeedDrawBackgroundOnce(boolean needDrawEmptyFolder) {
        this.mNeedDrawEmptyFolderOnce = needDrawEmptyFolder;
    }

    private void drawEmptyFolder(Canvas canvas) {
        if (mCardBackground == null) {
            mCardBackground = getResources().getDrawable(
                    R.drawable.card_folder_bg);
        }
        mCardBackground.setBounds(0, 0, getWidth(), getHeight());
        mCardBackground.draw(canvas);
        mNeedDrawEmptyFolderOnce = false;
        if (mIsSupportCard && !mHostseatMode) {
            mCardCover.draw(canvas);
        }
        canvas.translate(0, mTextPaddingTop);
        drawChild(canvas, mFolderName, 0);
    }

    public BubbleTextView getTitleText() {
        return mFolderName;
    }

    public void setTempPadding(int left) {
        if (!mIsTempPadding) {
            mTempPaddingInHotseat = left;
            mIsTempPadding = true;
        }
//        setPadding(left, top, right, bottom);
    }

    public void resetTempPadding() {
        if (mIsTempPadding) {
            mTempPaddingInHotseat = 0;
//            setPadding(mOldPaddingLeft, getPaddingTop(), mOldPaddingRight, getPaddingBottom());
        }
        mIsTempPadding = false;
    }

    public static void onThemeChanged() {
        if (mThemeBackground != null) {
            mThemeBackground.recycle();
            mThemeBackground = null;
        }

        mCardCover = null;
        mCardBackground = null;

        initGlobal();
    }

    public void shake(){
        ObjectAnimator.ofFloat(this, "translationY", 0, 25, -25, 25, -25,15, -15, 0).start();
    }
//topwise zyf add for fixedfolder
    public void myinit(FixedFolderIcon ffi) {
        mLongPressHelper = new CheckLongPressHelper(ffi);
        if (Build.VERSION.SDK_INT < 19/* KITKAT */)
            setLayerType(LAYER_TYPE_HARDWARE, null);
        mDetector = new GestureDetector(getContext(),new FolderIconGestureListener());
        IND_NUM_SIZE_NORMAL = LauncherApplication.getContext().getResources()
                .getDimensionPixelSize(R.dimen.bubble_num_normal_size);
        IND_NUM_SIZE_SMALL = LauncherApplication.getContext().getResources()
                .getDimensionPixelSize(R.dimen.bubble_num_small_size);
    }
    public static int getAnimationDuration_initial_item()
    {
    	return INITIAL_ITEM_ANIMATION_DURATION;
    }
    
    public static int getAnimationDuration_drop_in()
    {
    	return DROP_IN_ANIMATION_DURATION;
    }
    public BubbleTextView getFolderName()
    {
    	return mFolderName;
    }
    public void setFolderName(BubbleTextView bubbleTextView)
    {
    	mFolderName=bubbleTextView;
    }
    public void setFolder(Folder f)
    {
    	 mFolder=f;
    }
    public void setFolderInfo(FolderInfo fi) {
         mInfo=fi;
    }

    public ImageView getPreviewBackground()
    {
    	return mPreviewBackground;
    }
    public void setPreviewBackground(ImageView img)
    {
    	 mPreviewBackground=img;
    }

    public Launcher getLauncher()
    {
    	return mLauncher;
    }
    public void setLauncher(Launcher l)
    {
    	mLauncher=l;
    }
    public static boolean getIsSupportCard()
    {
    	return mIsSupportCard;
    }
    public static void setIsSupportCard(boolean b)
    {
    	mIsSupportCard=b;
    }
//topwise zyf add end
}
