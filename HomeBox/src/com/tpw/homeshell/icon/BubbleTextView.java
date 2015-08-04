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

package com.tpw.homeshell.icon;

import java.util.HashMap;
import java.util.Map;

//import tpw.aml.FancyDrawable;
//import tpw.v3.gadget.GadgetView;
import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.FontMetrics;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Region.Op;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Pair;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.TextView;

import com.tpw.homeshell.AppDownloadManager.AppDownloadStatus;
import com.tpw.homeshell.CellLayout;
import com.tpw.homeshell.CellLayout.Mode;
import com.tpw.homeshell.AgedModeUtil;
import com.tpw.homeshell.CheckLongPressHelper;
import com.tpw.homeshell.CheckVoiceCommandPressHelper;
import com.tpw.homeshell.ConfigManager;
import com.tpw.homeshell.DragLayer;
import com.tpw.homeshell.FastBitmapDrawable;
import com.tpw.homeshell.Folder;
import com.tpw.homeshell.FolderInfo;
import com.tpw.homeshell.GadgetCardHelper;
import com.tpw.homeshell.HolographicOutlineHelper;
import com.tpw.homeshell.ItemInfo;
import com.tpw.homeshell.Launcher;
import com.tpw.homeshell.LauncherApplication;
import com.tpw.homeshell.LauncherProvider;
import com.tpw.homeshell.LauncherSettings;
import com.tpw.homeshell.LauncherSettings.Favorites;
import com.tpw.homeshell.R;
import com.tpw.homeshell.ShortcutInfo;
import com.tpw.homeshell.TopwiseConfig;
import com.tpw.homeshell.UserTrackerHelper;
import com.tpw.homeshell.UserTrackerMessage;
import com.tpw.homeshell.animation.FlipAnimation;
import com.tpw.homeshell.model.LauncherModel;
import com.tpw.homeshell.themeutils.ThemeUtils;
import com.tpw.homeshell.utils.Utils;
import com.tpw.homeshell.vpinstall.VPUtils.VPInstallStatus;

/**
 * TextView that draws a bubble behind the text. We cannot use a
 * LineBackgroundSpan because we want to make the bubble taller than the text
 * and TextView's clip is too aggressive.
 */
public class BubbleTextView extends TextView {

    static final String TAG = "BubbleTextView";

	static final float CORNER_RADIUS = 4.0f;
	static final float SHADOW_LARGE_RADIUS = 1.0f;
	static final float SHADOW_SMALL_RADIUS = 1.75f;
	static final float SHADOW_Y_OFFSET = 2.0f;
	static final int SHADOW_LARGE_COLOUR = 0xff000000;//modified by qinjinchuan topwise for bug382
	static final int SHADOW_SMALL_COLOUR = 0x33000000;
	public static final float PADDING_H = 8.0f;
	public static final float PADDING_V = 3.0f;
	private static final float PROGRESS_NOT_DOWNLOAD = -1f;

	private static int DOWNLOAD_CIRCLE_RADIUS_X;
	private static int DOWNLOAD_CIRCLE_RADIUS_Y;
	private static int DOWNLOAD_CIRCLE_DRAW_X;
	private static int DOWNLOAD_CIRCLE_DRAW_Y;
	private static int DOWNLOAD_CIRCLE_IMG_R;
	private static int DOWNLOAD_ARC_DRAW_R;
	private static int DOWNLOAD_ARC_WIDTH;
	private static int ICON_X;
	private static int ICON_Y;
	public static int ICON_WIDTH;
	public static int ICON_HEIGHT;
	private static int TITLE_HEIGHT;
    private float mMotionDownY, mMotionDownX;

	private static boolean mInited = false;

	private final HolographicOutlineHelper mOutlineHelper = new HolographicOutlineHelper();
	private final Canvas mTempCanvas = new Canvas();
	private final Rect mTempRect = new Rect();
	private boolean mDidInvalidateForPressedState;
	private Bitmap mPressedOrFocusedBackground;
	private static int mFocusedOutlineColor;
	private static int mFocusedGlowColor;
	private static int mPressedOutlineColor;
	private static int mPressedGlowColor;
	private boolean mBackgroundSizeChanged;
	private Drawable mBackground;
	private Drawable mCardBackground;

	private boolean mStayPressed;
	private CheckLongPressHelper mLongPressHelper;

	private boolean mIsNew = false;
	private String mMessageNum = "";
	private int mMsgNum;
	private int mIconMarkNum;
	private static Bitmap mImgRTCorner; // right top corner image

	private static int INDICATOR_BOUNDRY_X;
	private static int INDICATOR_BOUNDRY_Y;
	private static int IND_TEXT_SIZE_SMALL;

	private static int IND_TEXT_SIZE_NORMAL;

	private static int IND_NUM_SIZE_NORMAL;
	private static int IND_NUM_SIZE_SMALL;

	private static int mNewMarkDrawX = 0;
	private static int mNewMarkDrawY = 0;

	private int mNumIndicatorNumDrawX = 0;
	private int mNumIndicatorNumDrawY = 0;

	private int mNumberSize;
	private int mTextSize;
	private static Paint mNumberPaint;
	private static Paint mTitlePaint;
	private static Paint mNewMarkPaint;

    public static String NEW_MARK_SHORTCUT;
    public static String NOTIFICATION_MARK;
	public Context mContext;

	private static int BUBBLE_WIDTH;
	private static int BUBBLE_HEIGHT;

	public static int sTopPaddingHotseat;
	public static int sTopPadding;

    private static int DOWNLOAD_CIRCLE_RADIUS_X_IN_HOTSEAT;
    private static int DOWNLOAD_CIRCLE_RADIUS_Y_IN_HOTSEAT;
    private static int DOWNLOAD_CIRCLE_DRAW_X_IN_HOTSEAT;
    private static int DOWNLOAD_CIRCLE_DRAW_Y_IN_HOTSEAT;
    private static int BUBBLE_WIDTH_IN_HOTSEAT;
    private static int BUBBLE_HEIGHT_IN_HOTSEAT;

	private static Bitmap mImgCircleWait;
	private static Bitmap sImgPause;

    private final IconIndicator mIndicator = new IconIndicator();
    private int mTitleHeight;
    private StringBuilder mDecorateSpaces;

	private float mProgress = PROGRESS_NOT_DOWNLOAD;
	private int mDownloadStatus = AppDownloadStatus.STATUS_NO_DOWNLOAD;

    private int mVPInstallStatus = VPInstallStatus.STATUS_NORMAL;

    public boolean mIsTempPadding = false;
    private int mTempPaddingInHotseat = 0;
//    private int mOldPaddingRight = 0;
//    private int mOldPaddingLeft = 0;

    private Bitmap mVPIndicatorBitmap;

    private boolean mClicked = false;
    private IconManager mIconManager = null;
    private CheckVoiceCommandPressHelper mVoiceChecker;
    private boolean mIsHideCornerFlag = false;
    private int mTitleColor = 0;
    private boolean mIsCommonThemeColor = true;

    public static final ColorMatrixColorFilter HIDESEAT_ICON_FADING_FILTER;
    private static final int HIDESEAT_ICON_ALPHA = 200;
    static {
        ColorMatrix matrix = new ColorMatrix();
        matrix.setSaturation(0.0f);
        HIDESEAT_ICON_FADING_FILTER = new ColorMatrixColorFilter(matrix);
    }
    /** @see #applyFadingEffectInHideseat() */
    private ColorMatrixColorFilter mAnimatedIconFadingFilter = null;
    private boolean mHideseatIconFadingFilterEnable = true;

    public BubbleTextView(Context context) {
        super(context);
        setLayerType(View.LAYER_TYPE_HARDWARE, null);
        mContext = context;
        init();
    }

    public BubbleTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setLayerType(View.LAYER_TYPE_HARDWARE, null);
        mContext = context;
        init();
    }

    public BubbleTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setLayerType(View.LAYER_TYPE_HARDWARE, null);
        mContext = context;
        init();
    }

	private void init() {
	    mVoiceChecker = CheckVoiceCommandPressHelper.getInstance();
		mLongPressHelper = new CheckLongPressHelper(this);
		mBackground = getBackground();

		final Resources res = getContext().getResources();
		float density = res.getDisplayMetrics().density;
		mTextPaddingBottom = (int) density * 5;
		mHotseatPadding = res
				.getDimensionPixelSize(R.dimen.hotseat_icon_padding_top_plus);
		setShadowLayer(SHADOW_LARGE_RADIUS, 0.0f, SHADOW_Y_OFFSET,
				SHADOW_LARGE_COLOUR);

		if (mInited == true) {
			return;
		}
		mNumberPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mNumberPaint.setColor(Color.WHITE);
		mNewMarkPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mNewMarkPaint.setColor(Color.WHITE);
		mTitlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mTitlePaint.setColor(Color.WHITE);
		mTitlePaint.setTextSize(getTextSize());

		mFocusedOutlineColor = mFocusedGlowColor = mPressedOutlineColor = mPressedGlowColor = res
				.getColor(R.color.holo_white_light);

        NEW_MARK_SHORTCUT = res.getString(R.string.new_mark_shortcut);
        NOTIFICATION_MARK = res.getString(R.string.notification_mark);
        mImgRTCorner = BitmapFactory.decodeResource(res, R.drawable.ic_corner_mark_bg);
        if (AgedModeUtil.isAgedMode()) {
           Bitmap tmp =  Bitmap.createBitmap(mImgRTCorner, 0, 0, mImgRTCorner.getWidth(),
                    mImgRTCorner.getHeight(), AgedModeUtil.sScaleUp, false);
           if(!mImgRTCorner.isRecycled()){
               mImgRTCorner.recycle();
           }
           mImgRTCorner = tmp;
        }
		initParams(res);
		// text is larger than number, 10 equals 14
		String language = getResources().getConfiguration().locale.getLanguage();
		if ("zh".equals(language)) {
		    mTextSize = IND_TEXT_SIZE_NORMAL;
		} else {
		    mTextSize = IND_TEXT_SIZE_SMALL;
		}
//rubbish
//		final Point tempPoint = calcStringPosition(newMark, mTextSize,
//				mImgRTCorner, mNewMarkPaint);
//		mNewMarkDrawX = tempPoint.x;
//		mNewMarkDrawY = tempPoint.y;
//		calcNumberPosition(mMessageNum);
//end

		mImgCircleWait = BitmapFactory.decodeResource(res,R.drawable.download_circle_waiting);
		sImgPause      = BitmapFactory.decodeResource(res,R.drawable.home_icon_pause);
		FontMetrics fm = mTitlePaint.getFontMetrics();
		TITLE_HEIGHT = (int) (Math.ceil(fm.bottom - fm.top)) + 2; // 26 for hdpi

        IconIndicator.init(getResources());

		ICON_WIDTH = ICON_HEIGHT = ThemeUtils.getIconSize(mContext);

		if (ICON_WIDTH <= 0) {
			ICON_WIDTH = ICON_HEIGHT = getResources().getDimensionPixelSize(
					R.dimen.folder_preview_size);
		}

		ICON_X = (BUBBLE_WIDTH - ICON_WIDTH) / 2;
		ICON_Y = (BUBBLE_HEIGHT - ICON_HEIGHT - TITLE_HEIGHT) / 2;

		DOWNLOAD_CIRCLE_DRAW_X = (BUBBLE_WIDTH - mImgCircleWait.getWidth()) / 2;
		DOWNLOAD_CIRCLE_DRAW_Y = (BUBBLE_HEIGHT - TITLE_HEIGHT - mImgCircleWait
				.getHeight()) / 2;

		DOWNLOAD_CIRCLE_IMG_R = mImgCircleWait.getWidth() / 2;
		DOWNLOAD_ARC_DRAW_R = res
				.getDimensionPixelSize(R.dimen.download_arc_draw_r);
		DOWNLOAD_ARC_WIDTH = res
				.getDimensionPixelSize(R.dimen.download_arc_width);

		DOWNLOAD_CIRCLE_RADIUS_X = DOWNLOAD_CIRCLE_DRAW_X+ DOWNLOAD_CIRCLE_IMG_R;
		DOWNLOAD_CIRCLE_RADIUS_Y = DOWNLOAD_CIRCLE_DRAW_Y+ DOWNLOAD_CIRCLE_IMG_R;

        DOWNLOAD_CIRCLE_DRAW_X_IN_HOTSEAT = (BUBBLE_WIDTH_IN_HOTSEAT - mImgCircleWait.getWidth()) / 2;
        DOWNLOAD_CIRCLE_DRAW_Y_IN_HOTSEAT = (ICON_HEIGHT - mImgCircleWait.getHeight()) / 2;
        DOWNLOAD_CIRCLE_RADIUS_X_IN_HOTSEAT = DOWNLOAD_CIRCLE_DRAW_X_IN_HOTSEAT + DOWNLOAD_CIRCLE_IMG_R;
        DOWNLOAD_CIRCLE_RADIUS_Y_IN_HOTSEAT = DOWNLOAD_CIRCLE_DRAW_Y_IN_HOTSEAT + DOWNLOAD_CIRCLE_IMG_R;

		mIconManager = ((LauncherApplication) mContext.getApplicationContext())
				.getIconManager();
		mInited = true;
	}
	

    
    private void calcTitleHeight() {
        Paint p = new Paint();
        p.setTextSize(getTextSize());
        FontMetrics fm = p.getFontMetrics();
        mTitleHeight = (int) (Math.ceil(fm.descent - fm.ascent));
    }

    private void calcEnoughSpapce() {
        if (mDecorateSpaces == null) {
            mDecorateSpaces = new StringBuilder();
        }
        mDecorateSpaces.delete(0, mDecorateSpaces.length());

        if (!mIndicator.hasIndicator()) {
            return;
        }

        int imgWidth = mIndicator.getCurrentIndicator().getWidth();
        Paint paint = new Paint();
        paint.setTextSize(getTextSize());
        paint.setTypeface(getTypeface());
        while (true) {
            mDecorateSpaces.append(" ");
            float spaceWidth = paint.measureText(mDecorateSpaces.toString());
            if (spaceWidth >= imgWidth) {
                break;
            }
        }
    }

	private void calcNumberPosition(String num) {
		if (num == null) {
			return;
		}

		final Point tempPos = calcStringPosition(num, mNumberSize,
				mImgRTCorner, mNumberPaint);
		mNumIndicatorNumDrawY = tempPos.y;
		mNumIndicatorNumDrawX = tempPos.x;
	}

    @Override
    public int getExtendedPaddingTop() {
        // getHeight maybe 0, if bug caused by getHeight() is 0 in this method
        // the private method getExtendedPaddingTop(int top, int bottom)
        return getExtendedPaddingTop(0, getHeight());
    }

    private int getExtendedPaddingTop(int top, int bottom) {
        int height = bottom - top;
        if(mIconManager == null) {
            mIconManager = ((LauncherApplication) mContext.getApplicationContext())
                .getIconManager();
        }
        if (AgedModeUtil.isAgedMode() && !mIconManager.supprtCardIcon()) {
            return (int) (height - getLayout().getLineTop(1) - mTextPaddingBottom
                * 8 / getResources().getDisplayMetrics().density);
        } else {
            return height - getLayout().getLineTop(1) - mTextPaddingBottom;
        }
    }

	@Override
	public int getPaddingBottom() {
		// TODO Auto-generated method stub
		return 0;
	}

	protected Drawable getCardBackground(int color) {
		Drawable bg = getResources().getDrawable(R.drawable.card_bg);
		bg.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
		return bg;
	}

    public boolean isSupportCard() {
        return mSupportCard;
    }

    /** Return true if this BubbleTextView is in hotseat or hideseat */
    public boolean isInHotseatOrHideseat() {
        return mMode == Mode.HOTSEAT || mMode == Mode.HIDESEAT;
    }

    public CellLayout.Mode getMode() {
        return mMode;
    }

    /**
     * This method is used to replace old <code>setHotseatMode(boolean)</code>
     * method.
     * @see CellLayout.Mode
     * @param mode
     */
    public void setMode(CellLayout.Mode mode) {
        if (mode != mMode && mShortcutInfo != null) {
            mMode = mode;
            applyFromShortcutInfo(mShortcutInfo);
        }
        /*
         * setTextColor(hotseat ? getResources().getColor(
         * android.R.color.transparent) : mTitleColor);
         */
        if (mode == Mode.NORMAL) {
            setTextColor(mTitleColor);
            resetTempPadding();
        } else if (!AgedModeUtil.isAgedMode()) {
            setTextColor(IconUtils.TITLE_COLOR_WHITE);
        } else if (mode == Mode.HIDESEAT) {
            setTextColor(IconUtils.TITLE_COLOR_WHITE);
        } else {
            setTextColor(mTitleColor);
        }
    }

    /**
     * @see #setHideseatIconFadingFilterEnable(boolean)
     */
    public boolean isHideseatIconFadingFilterEnable() {
        return mHideseatIconFadingFilterEnable;
    }

    /**
     * By default, the icon in hide-seat will be applied with a fading color
     * filter to represent its frozen state. Clients can use this method to
     * temporarily disable this behavior.
     * @param mHideseatIconFadingFilterEnable
     * @see #HIDESEAT_ICON_FADING_FILTER
     * @see #updateTopCompoundDrawableFilter()
     */
    public void setHideseatIconFadingFilterEnable(boolean enable) {
        if (mHideseatIconFadingFilterEnable != enable) {
            mHideseatIconFadingFilterEnable = enable;
            if (mShortcutInfo != null &&
                mShortcutInfo.container == Favorites.CONTAINER_HIDESEAT) {
                invalidate();
            }
        }
    }

    public void setDisableLabel(boolean disable) {
        setTextColor(disable ? getResources().getColor(
                android.R.color.transparent) : mTitleColor);
    }

    public void drawBubbleTextViewInFolderIcon(int width, int height, Canvas canvas) {
        ShortcutInfo info = (ShortcutInfo) getTag();
        IconManager iconManager = ((LauncherApplication)mContext.getApplicationContext()).getIconManager();

        Drawable cardbg = mCardBackground;

        if (!mSupportCard) {
            cardbg = iconManager.getAppUnifiedIcon(info, null);
        }

        cardbg.clearColorFilter();
        Drawable.Callback callback = cardbg.getCallback();
        cardbg.setCallback(null);
        Rect bounds = cardbg.copyBounds();
        cardbg.setBounds(0, 0, width, height);
        cardbg.draw(canvas);
        // restore the state of drawable
        cardbg.setBounds(bounds);
        cardbg.setCallback(callback);

        // draw text when view hasn't been attached
        if (mSupportCard && getCurrentTextColor() != getResources().getColor(
                android.R.color.transparent)) {
            getPaint().setColor(iconManager.getTitleColor(info));
            int index = getPaint().breakText(getText().toString(), true, width, null);
            String text = getText().subSequence(0, index).toString();

            float extendedPaddingTop = height - 18 * getResources().getDisplayMetrics().density;
            float posX = (width - getPaint().measureText(text)) / 2;
            float posY = extendedPaddingTop + mTextPaddingBottom * 2.2f;
            canvas.drawText(text, posX, posY, getPaint());
        }
    }

	private int mHotseatPadding, mTextPaddingBottom;
	// protected boolean mHotseatMode;
	protected CellLayout.Mode mMode;
	protected boolean mSupportCard;
	private ShortcutInfo mShortcutInfo;

    /**
     * Calculate the position where string will be drawn
     * @return the (x,y) relative to text baseline
     */
    private Point calcStringPosition(String str, int textSize, Bitmap bg,
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

	private void initParams(Resources res) {
        BUBBLE_WIDTH = ConfigManager.getWorkspaceIconWidth();
        BUBBLE_HEIGHT = ConfigManager.getWorkspaceIconHeight();
        BUBBLE_WIDTH_IN_HOTSEAT = ConfigManager.getHotseatIconWidth();
        BUBBLE_HEIGHT_IN_HOTSEAT = ConfigManager.getHotseatIconHeight();
		INDICATOR_BOUNDRY_X = BUBBLE_WIDTH - mImgRTCorner.getWidth();
		INDICATOR_BOUNDRY_Y = 0;

		IND_TEXT_SIZE_NORMAL = res
				.getDimensionPixelSize(R.dimen.bubble_text_normal_size);
		IND_TEXT_SIZE_SMALL = res
				.getDimensionPixelSize(R.dimen.bubble_text_small_size);

		IND_NUM_SIZE_NORMAL = res
				.getDimensionPixelSize(R.dimen.bubble_num_normal_size);
		IND_NUM_SIZE_SMALL = res
				.getDimensionPixelSize(R.dimen.bubble_num_small_size);

		mNumberSize = IND_NUM_SIZE_NORMAL;

	}

    public void applyFromShortcutInfo(ShortcutInfo info) {
        mIsNew = info.isNewItem();
        if (mIconManager == null) {
            mIconManager = ((LauncherApplication) mContext
                    .getApplicationContext()).getIconManager();
        }
        mSupportCard = mIconManager.supprtCardIcon();
        mShortcutInfo = info;
        boolean hasCardBgIcon = false;
        ComponentName component = null;
        if(info.intent != null) {
                component = info.intent.getComponent();
        }
        Drawable icon = null;
        boolean needreset = false;
        Drawable oldBacgground = mCardBackground;
        Pair<Drawable, Integer> cardBgAndTitleColor = null;
        if (info.intent != null && mSupportCard) {
            cardBgAndTitleColor = mIconManager.getAppCardBgAndTitleColor(info);
            mCardBackground = cardBgAndTitleColor.first;
            hasCardBgIcon = mIconManager.isCardBgIcon(mCardBackground);
            if (oldBacgground != null)
                oldBacgground.setCallback(null);
            if (mCardBackground != null)
                mCardBackground.setCallback(this);
            if (isInHotseatOrHideseat() && (mMode == Mode.HIDESEAT || !AgedModeUtil.isAgedMode())) {
                //if(hasCardBgIcon){
                //  mCardBackground = null;
                //}
                hasCardBgIcon = false;
            }
        }else{
            mCardBackground = null;
        }

        if(mCardBackground != null){
            if(mCardBackground != oldBacgground){
                needreset = true;
                oldBacgground = null;
            }
            if(mTitleColor == 0 || mIsCommonThemeColor || needreset){
                if (cardBgAndTitleColor != null && cardBgAndTitleColor.second != null) {
                    mTitleColor = cardBgAndTitleColor.second;
                } else {
                    mTitleColor = mIconManager.getTitleColor(info);
                }
                mIsCommonThemeColor = false;
            }
        }else if(!mSupportCard){
            mTitleColor = IconUtils.TITLE_COLOR_WHITE;
            mIsCommonThemeColor = true;
        }
        if ((mMode != Mode.HOTSEAT || AgedModeUtil.isAgedMode()) && mMode != Mode.HIDESEAT) {
            setTextColor(mTitleColor);
        }

        if (icon == null) {
            //download item's icon doesn't change when theme change
            //use info's icon, don't get icon from theme, getAppUnifiedIcon make db operation in UI thread
            //icon = mIconManager.getAppUnifiedIcon(info, null);
            icon = info.mIcon;
        }

        mIsHideCornerFlag = false;
        /*if(icon != null && icon instanceof FancyDrawable) {
            mIsHideCornerFlag = Boolean.parseBoolean(((FancyDrawable)icon).getRawAttr("hideApplicationMessage"));
        }*/

        if (hasCardBgIcon) {
            icon = null;
        }

        if (mSupportCard && isInHotseatOrHideseat()) {
            boolean needCustom = (component == null)
                    || (component != null && ThemeUtils.needCustom(mContext, component.getPackageName(),component.getClassName()));
            if(needCustom) {
                icon = IconManager.buildHotSeatIcon(icon);
            }
        }
        boolean setPadding = true;
        setCompoundDrawablesWithIntrinsicBounds(null, icon, null, null);
        if (icon != null && AgedModeUtil.isAgedMode()) {
            float scaleRatio = AgedModeUtil.SCALE_RATIO_FOR_AGED_MODE;
            int left = (int) (-icon.getIntrinsicWidth() * (scaleRatio - 1) / 2);
            int top = 0;
            int right = (int) (left + icon.getIntrinsicWidth() * scaleRatio);
            int bottom = (int) (top + icon.getIntrinsicHeight() * scaleRatio);
            icon.setBounds(left, top, right, bottom);
        }
        if ((mDownloadStatus == AppDownloadStatus.STATUS_NO_DOWNLOAD
                || mDownloadStatus == AppDownloadStatus.STATUS_INSTALLED)
                && mVPInstallStatus == VPInstallStatus.STATUS_NORMAL)
        {
            //setText(info.title);
            if(info != null && info.title != null ) {
                info.title = Utils.trimUTFSpace(info.title);
            }
            setTitle(info.title);
        }
        setGravity(Gravity.CENTER_HORIZONTAL);
        setTag(info);
        if (setPadding) {
            setPadding(icon);
        } else {
            setPadding(0, 0, 0, 0);
        }

        /*if(icon instanceof FancyDrawable &&
           mMode == Mode.HIDESEAT) {
            applyFadingEffectInHideseat();
        }*/
    }

    private void setTitle(CharSequence title) {
        if(TextUtils.isEmpty(title)) {
            return;
        }
        String strTitle = title.toString().trim();

        boolean isCalendarCard = false;
        if (strTitle.equals("Calendar")){
            isCalendarCard = true;
        }

        Drawable icon = isInHotseatOrHideseat() ? getCompoundDrawables()[1] : mCardBackground;
        /*if (icon instanceof FancyDrawable) {
            String label = ((FancyDrawable) icon).getVariableString("app_label");
            if (label != null)
                strTitle = label;
        }*/

        if (isCalendarCard) {
            if (strTitle.equals("Sunday")) {
                strTitle = "Sun";
            } else if (strTitle.equals("Monday")) {
                strTitle = "Mon";
            } else if (strTitle.equals("Tuesday")) {
                strTitle = "Tues";
            } else if (strTitle.equals("Wednesday")) {
                strTitle = "Wed";
            } else if(strTitle.equals("Thursday")) {
                strTitle = "Thur";
            } else if(strTitle.equals("Friday")) {
                strTitle = "Fri";
            } else if(strTitle.equals("Saturday")) {
                strTitle = "Sat";
            }
        }

        if(!isInHotseatOrHideseat()) {
            Resources res = getContext().getResources();
            int paddingLeft = res.getDimensionPixelSize(R.dimen.bubble_textview_padding_left);
            int paddingRight = res.getDimensionPixelSize(R.dimen.bubble_textview_padding_right);
            if(getPaddingLeft() != paddingLeft) {
                setPadding(paddingLeft, getPaddingTop(), paddingRight, getPaddingBottom());
            }
        }

        int textWidth = getTextWidth(strTitle);
        boolean isExcessed = isTextWidthExcessed(isInHotseatOrHideseat(), textWidth);
        updateIndicator(mShortcutInfo);

        if(isExcessed && mIndicator.hasIndicator()) {
            //setText(mDecorateSpaces + strTitle);
            setText(" " + strTitle);
        } else {
            setText(strTitle);
        }
    }

    public void setTempPadding(int left) {
        if (!mIsTempPadding) {
            //mOldPaddingLeft = getPaddingLeft();
            //mOldPaddingRight = getPaddingRight();
            mTempPaddingInHotseat = left;
            mIsTempPadding = true;
        }
//        setPadding(left, top, right, bottom);
    }

    public void resetTempPadding() {
        if (mIsTempPadding) {
//            setPadding(mOldPaddingLeft, getPaddingTop(), mOldPaddingRight, getPaddingBottom());
            mTempPaddingInHotseat = 0;
        }
        mIsTempPadding = false;
    }

    private void setPadding(Drawable icon) {
        if (icon == null) {
            return;
        }

        Resources res = getContext().getResources();
        int padding = 0;
        if (AgedModeUtil.isAgedMode()) {
            int iconwidth = icon.getBounds().width();
            int bubblewidth = (int) (getResources().getDimensionPixelSize(
                    R.dimen.workspace_cell_width) * AgedModeUtil.SCALE_RATIO_FOR_AGED_MODE);
            padding = Math.abs((bubblewidth - iconwidth) / 2);
            sTopPadding = padding;
        } else if (isInHotseatOrHideseat()) {
            padding = res.getDimensionPixelSize(R.dimen.bubble_textview_hotseat_top_padding);
            sTopPaddingHotseat = padding;
            if(mMode == Mode.HOTSEAT) {
                int paddingLeft = res.getDimensionPixelSize(R.dimen.bubble_textview_hotseat_left_padding);
                setPadding(paddingLeft, getPaddingTop(), paddingLeft, getPaddingBottom());
            }
            
        } else {
            int iconwidth = icon.getBounds().width();
            int bubblewidth = getResources().getDimensionPixelSize(R.dimen.workspace_cell_width);
            padding = Math.abs((bubblewidth - iconwidth) / 2);
            sTopPadding = padding;
        }
        setPadding(getPaddingLeft(), padding, getPaddingRight(), 0);
    }

	@Override
	protected boolean setFrame(int left, int top, int right, int bottom) {
		if (getLeft() != left || getRight() != right || getTop() != top
				|| getBottom() != bottom) {
			mBackgroundSizeChanged = true;
		}
		return super.setFrame(left, top, right, bottom);
	}

	@Override
	protected boolean verifyDrawable(Drawable who) {
		return who == mBackground || super.verifyDrawable(who);
	}

	@Override
	public void setTag(Object tag) {
		if (tag != null) {
			LauncherModel.checkItemInfo((ItemInfo) tag);
		}
		super.setTag(tag);
	}

	@Override
	protected void drawableStateChanged() {
		if (isPressed()) {
			// In this case, we have already created the pressed outline on
			// ACTION_DOWN,
			// so we just need to do an invalidate to trigger draw
			if (!mDidInvalidateForPressedState) {
				setCellLayoutPressedOrFocusedIcon();
			}
		} else {
			// Otherwise, either clear the pressed/focused background, or create
			// a background
			// for the focused state
			final boolean backgroundEmptyBefore = mPressedOrFocusedBackground == null;
			if (!mStayPressed) {
				mPressedOrFocusedBackground = null;
			}
			if (isFocused()) {
				if (getLayout() == null) {
					// In some cases, we get focus before we have been layed
					// out. Set the
					// background to null so that it will get created when the
					// view is drawn.
					mPressedOrFocusedBackground = null;
				} else {
					mPressedOrFocusedBackground = null;
				}
				mStayPressed = false;
				setCellLayoutPressedOrFocusedIcon();
			}
			final boolean backgroundEmptyNow = mPressedOrFocusedBackground == null;
			if (!backgroundEmptyBefore && backgroundEmptyNow) {
				setCellLayoutPressedOrFocusedIcon();
			}
		}

		Drawable d = mBackground;
		if (d != null && d.isStateful()) {
			d.setState(getDrawableState());
		}
		super.drawableStateChanged();
	}

	/**
	 * Draw this BubbleTextView into the given Canvas.
	 *
	 * @param destCanvas
	 *            the canvas to draw on
	 * @param padding
	 *            the horizontal and vertical padding to use when drawing
	 */
	private void drawWithPadding(Canvas destCanvas, int padding) {
		final Rect clipRect = mTempRect;
		getDrawingRect(clipRect);

		// adjust the clip rect so that we don't include the text label
		// clipRect.bottom = getExtendedPaddingTop() - (int)
		// BubbleTextView.PADDING_V + getLayout().getLineTop(0);

		// Draw the View into the bitmap.
		// The translate of scrollX and scrollY is necessary when drawing
		// TextViews, because
		// they set scrollX and scrollY to large values to achieve centered text
		destCanvas.save();
		destCanvas.scale(getScaleX(), getScaleY(), (getWidth() + padding) / 2,
				(getHeight() + padding) / 2);
		destCanvas.translate(-getScrollX() + padding / 2, -getScrollY()
				+ padding / 2);
		destCanvas.clipRect(clipRect, Op.REPLACE);
		draw(destCanvas);
		destCanvas.restore();
	}

	/**
	 * Returns a new bitmap to be used as the object outline, e.g. to visualize
	 * the drop location. Responsibility for the bitmap is transferred to the
	 * caller.
	 */
	private Bitmap createGlowingOutline(Canvas canvas, int outlineColor,
			int glowColor) {
		final int padding = HolographicOutlineHelper.MAX_OUTER_BLUR_RADIUS;
		final Bitmap b = Bitmap.createBitmap(getWidth() + padding, getHeight()
				+ padding, Bitmap.Config.ARGB_8888);

		canvas.setBitmap(b);
		drawWithPadding(canvas, padding);
		mOutlineHelper.applyExtraThickExpensiveOutlineWithBlur(b, canvas,
				glowColor, outlineColor);
		canvas.setBitmap(null);

		return b;
	}

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        // this switch is for fling up gesture and avoid OnClickListener.onClick()
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mMotionDownY = event.getRawY();
                mMotionDownX = event.getRawX();
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                float deltaY = 0;
                int slop = ViewConfiguration.get(mContext).getScaledTouchSlop();
                if (LauncherApplication.getLauncher() != null
                        && !Utils.isYunOS2_9System() && getVisibility() == View.VISIBLE &&
                        LauncherApplication.getLauncher().getGestureLayer().getPointerCount() <= 1 &&
                        !LauncherApplication.getLauncher().getWorkspace().isPageMoving() &&
                        (deltaY = mMotionDownY - event.getRawY()) > slop * 2 &&
                        /*modify by huangxunwan for config the support the app card by flip in the icon view 20150625*/
                        Math.abs(mMotionDownX - event.getRawX()) <  deltaY &&
                        TopwiseConfig.DEFAULT_FLIP_APP_CARD) {
                		/*end modify by huangxunwan */
                    cancelLongPress();
                    onFlingUp();
                    event.setAction(MotionEvent.ACTION_CANCEL);
                    super.onTouchEvent(event);
                    return true;
                }
                break;
            default:
                break;
        }

        // Call the superclass onTouchEvent first, because sometimes it changes
        // the state to
        // isPressed() on an ACTION_UP
        boolean result = super.onTouchEvent(event);

        switch (event.getAction()) {
        case MotionEvent.ACTION_DOWN:
            // So that the pressed outline is visible immediately when
            // isPressed() is true,
            // we pre-create it on ACTION_DOWN (it takes a small but perceptible
            // amount of time
            // to create it)
            /*
             * if (mPressedOrFocusedBackground ==
             * null) { mPressedOrFocusedBackground =
             * createGlowingOutline(mTempCanvas, mPressedGlowColor,
             * mPressedOutlineColor); }
             */
            // Invalidate so the pressed state is visible, or set a flag so we
            // know that we
            // have to call invalidate as soon as the state is "pressed"
            if (isPressed()) {
                mDidInvalidateForPressedState = true;
                // setCellLayoutPressedOrFocusedIcon();
            } else {
                mDidInvalidateForPressedState = false;
            }
            if (CheckVoiceCommandPressHelper.PUSH_TO_TALK_SUPPORT) {
                mVoiceChecker.postCheckVoiceCommand(this);
            }
            mLongPressHelper.postCheckForLongPress();
            break;
        case MotionEvent.ACTION_UP:
            if (CheckVoiceCommandPressHelper.PUSH_TO_TALK_SUPPORT) {
                mVoiceChecker.cancelCheckedVoiceCommand();
            }
        case MotionEvent.ACTION_CANCEL:
            // If we've touched down and up on an item, and it's still not
            // "pressed", then
            // destroy the pressed outline
            if (!isPressed()) {
                mPressedOrFocusedBackground = null;
            }

            mLongPressHelper.cancelLongPress();
            break;
        }
        return result;
    }

    /** where triggers card flip animation */
    private void onFlingUp(){
        Log.d(FlipAnimation.TAG,"onFlingUp");
        if( LauncherApplication.mLauncher == null ) return;
        ShortcutInfo info = (ShortcutInfo)BubbleTextView.this.getTag();
        if (info == null || info.intent == null) {
            return;
        }
        if (info.container == LauncherSettings.Favorites.CONTAINER_HIDESEAT) {
            return;
        }
        if (info.itemType != Favorites.ITEM_TYPE_APPLICATION) {
            shake();
            return;
        }
        ComponentName cn = info.intent.getComponent();
        GadgetCardHelper helper = GadgetCardHelper.getInstance(mContext);
        View gadget = helper.getCardView(cn, this, getCompoundDrawables()[0], getText(), mMsgNum);
        if( gadget == null ){
            Log.d(FlipAnimation.TAG, "onFlingUp: no card view for " + info.title);
            shake();
        }else{
            LauncherApplication.mLauncher.startFlipAnimation(BubbleTextView.this, gadget);
            Map<String, String> msg = new HashMap<String, String>();
            msg.put("PkgName", cn.getPackageName());
            msg.put("Type", /*gadget instanceof GadgetView ? "Special" : */"Normal" );
            UserTrackerHelper.sendUserReport(UserTrackerMessage.MSG_CARD_SLIDE_UP, msg);
        }
    }

	public void setStayPressed(boolean stayPressed) {
		mStayPressed = stayPressed;
		if (!stayPressed) {
			mPressedOrFocusedBackground = null;
		}
		setCellLayoutPressedOrFocusedIcon();
	}

	public void setCellLayoutPressedOrFocusedIcon() {
		/*
		 * if (getParent() instanceof ShortcutAndWidgetContainer) {
			ShortcutAndWidgetContainer parent = (ShortcutAndWidgetContainer) getParent();
			if (parent != null) {
				CellLayout layout = (CellLayout) parent.getParent();
				layout.setPressedOrFocusedIcon((mPressedOrFocusedBackground != null) ? this
						: null);
			}
		}
		*/
	}

	public void clearPressedOrFocusedBackground() {
		mPressedOrFocusedBackground = null;
		setCellLayoutPressedOrFocusedIcon();
	}

	public Bitmap getPressedOrFocusedBackground() {
		return mPressedOrFocusedBackground;
	}

	public int getPressedOrFocusedBackgroundPadding() {
		return HolographicOutlineHelper.MAX_OUTER_BLUR_RADIUS / 2;
	}

	@Override
    public void draw(Canvas canvas) {
        //if (mShortcutInfo != null) {
        //    Log.d("vqx376", "draw "
        //            + (canvas.isHardwareAccelerated() ? "H" : "S")
        //            + mShortcutInfo.title);
        //}

        // update downloading icon mask, and hide-seat fading mask
        updateTopCompoundDrawableFilter();

        // draw card background
        final Drawable cardbg = mCardBackground;
        if (cardbg != null &&
                ((AgedModeUtil.isAgedMode() && (mShortcutInfo == null || mMode != Mode.HIDESEAT))
                 || !isInHotseatOrHideseat())) {
            final int scrollX = getScrollX();
            final int scrollY = getScrollY();
            cardbg.setBounds(0, 0, getWidth(), getHeight());
            if ((scrollX | scrollY) == 0) {
                cardbg.draw(canvas);
            } else {
                canvas.translate(scrollX, scrollY);
                cardbg.draw(canvas);
                canvas.translate(-scrollX, -scrollY);
            }
        }

        boolean drawText = true;
        // If text is transparent, don't draw any shadow
        if (getCurrentTextColor() == getResources().getColor(
                android.R.color.transparent)) {
            getPaint().clearShadowLayer();
//            if (isInHotseatOrHideseat()) {
//                int padding = mHotseatPadding;
//                //canvas.translate(0, padding);
//                super.draw(canvas);
//                //canvas.translate(0, -padding);
//            } else {
            super.draw(canvas);
//            }
            drawText = false;
        }

        if (drawText) {
            // We enhance the shadow by drawing the shadow twice
            getPaint().setShadowLayer(SHADOW_LARGE_RADIUS, 0.0f,
                    SHADOW_Y_OFFSET, SHADOW_LARGE_COLOUR);
            super.draw(canvas);
//            canvas.save(Canvas.CLIP_SAVE_FLAG);
//            canvas.clipRect(getScrollX(), getScrollY()
//                    + getExtendedPaddingTop(), getScrollX() + ICON_WIDTH,
//                    getScrollY() + ICON_HEIGHT, Region.Op.INTERSECT);
//            getPaint().setShadowLayer(SHADOW_SMALL_RADIUS, 0.0f, 0.0f,
//                    SHADOW_SMALL_COLOUR);
//            super.draw(canvas);
//            canvas.restore();
        }

        // draw base image
        final Paint paint = mNumberPaint;
        if (mShortcutInfo != null) {
            if ((mShortcutInfo.container != Favorites.CONTAINER_HIDESEAT &&
                paint.getColorFilter() == HIDESEAT_ICON_FADING_FILTER) ||
                mHideseatIconFadingFilterEnable == false) {
                paint.setColorFilter(null);
            } else if (mShortcutInfo.container == Favorites.CONTAINER_HIDESEAT &&
                       paint.getColorFilter() != HIDESEAT_ICON_FADING_FILTER) {
                paint.setColorFilter(HIDESEAT_ICON_FADING_FILTER);
            }
        }
        int alpha = paint.getAlpha();

        final Drawable background = mBackground;
        if (background != null) {
            final int scrollX = getScrollX();
            final int scrollY = getScrollY();

            if (mBackgroundSizeChanged) {
                background.setBounds(0, 0, getRight() - getLeft(), getBottom() - getTop());
                mBackgroundSizeChanged = false;
            }

            if ((scrollX | scrollY) == 0) {
                background.draw(canvas);
            } else {
                canvas.translate(scrollX, scrollY);
                background.draw(canvas);

                if(mClicked) {
                    if(mIconManager.supprtCardIcon() && !isInHotseatOrHideseat()){
                        final int maskColor = mContext.getResources().getColor(R.color.holo_white_dark);
                        Drawable mask = mContext.getResources().getDrawable(R.drawable.card_bg);
                        mask.setColorFilter(maskColor, PorterDuff.Mode.SRC_ATOP);
                        mask.setBounds(0, 0, getWidth(), getHeight());
                        mask.setAlpha(128);
                        mask.draw(canvas);
                    }
                    mClicked = false;
                }

                // When downloading stopped
                if( mDownloadStatus == AppDownloadStatus.STATUS_WAITING ||
                    mDownloadStatus == AppDownloadStatus.STATUS_PAUSED){
                    drawDownloadIconMaskIfNeeded(canvas);
                    drawCircle(canvas, mProgress);
                    drawPauseImage(canvas);
                }

                // When in downloading and install process
                if( mDownloadStatus == AppDownloadStatus.STATUS_DOWNLOADING ||
                    mDownloadStatus == AppDownloadStatus.STATUS_INSTALLING) {
                    drawDownloadIconMaskIfNeeded(canvas);
                    drawCircle(canvas, mProgress);
                }

                //vp install item icon display
                else if (mVPInstallStatus == VPInstallStatus.STATUS_lOADING) {
                    drawDownloadIconMaskIfNeeded(canvas);
                    drawCircleVP(canvas, mProgress);
                }

                this.updateTitle();
                if (mIndicator.hasIndicator()) {
                    drawIndicator(canvas, paint);
                }
                if (LauncherModel.showNotificationMark()) {
                    final String mesNum = mMessageNum;
                    int notificationCount = getNotificationCount();
                    boolean isInHideseat = (mMode == Mode.HIDESEAT);
                    if ((mesNum != null && !mesNum.isEmpty() && !mIsHideCornerFlag) || (notificationCount > 0)) {
                        INDICATOR_BOUNDRY_Y = 0;

                        if (mIsTempPadding && isInHotseatOrHideseat()) {
                            INDICATOR_BOUNDRY_X -= mTempPaddingInHotseat;
                        }
                        if (mesNum != null && !mesNum.isEmpty() && !isInHideseat) {
                            canvas.drawBitmap(mImgRTCorner, INDICATOR_BOUNDRY_X, INDICATOR_BOUNDRY_Y, paint);
                            calcNumberPosition(mMessageNum);
                            paint.setTextSize(mNumberSize);
                            canvas.drawText(mesNum, mNumIndicatorNumDrawX, mNumIndicatorNumDrawY, paint);
                        } else if (notificationCount > 0 && !isInHideseat) {
                            canvas.drawBitmap(mImgRTCorner, INDICATOR_BOUNDRY_X, INDICATOR_BOUNDRY_Y, paint);
                            mTextSize = IND_TEXT_SIZE_NORMAL;
                            String strNoticeCount = String.valueOf(notificationCount);
                            Point p = calcStringPosition(strNoticeCount, mTextSize, mImgRTCorner, mNewMarkPaint);
                            canvas.drawText(strNoticeCount, p.x, p.y, mNewMarkPaint);
                        }
                        if (mIsTempPadding && isInHotseatOrHideseat()) {
                            INDICATOR_BOUNDRY_X += mTempPaddingInHotseat;
                        }
                    }
                }

                ItemInfo info = (ItemInfo) getTag();
                if(mSupportCard && info.itemType == LauncherSettings.Favorites.ITEM_TYPE_VPINSTALL) {
                    drawVPInstallIndicator(canvas, paint);
                }

                paint.setAlpha(alpha);
                canvas.translate(-scrollX, -scrollY);
            }
        }
    }
	
	private int getOneBlackStringWidth() {
        return getTextWidth(" ");
	}
	
    private int getTextWidth(String text) {
        return (int) mTitlePaint.measureText(text);
    }

    private void drawIndicator(Canvas canvas, Paint paint) {
        if (!mIndicator.hasIndicator()) {
            return;
        }
        Bitmap imgIndicator = mIndicator.getCurrentIndicator();
        int indicatorPadding = IconIndicator.getIndicatorPadding();

        float x = 0;
        float y = getExtendedPaddingTop() + (mTitleHeight  - imgIndicator.getHeight()) / 2 ;

        Pair<Float, Float> adhocPos = IconIndicator.getAdhocIndicatorPosition(this);
        if (adhocPos != null) {
            x = BUBBLE_WIDTH * adhocPos.first - imgIndicator.getWidth() / 2;
            y = BUBBLE_HEIGHT * adhocPos.second - imgIndicator.getHeight() / 2;
        } else {
            int textWidth = getTextWidth(getText().toString());
            if (isInHotseatOrHideseat()) {
                float temp = textWidth + imgIndicator.getWidth() + indicatorPadding;
                if (mIsTempPadding) {
                    temp += getPaddingLeft() + mTempPaddingInHotseat;
                }
                if (temp > BUBBLE_WIDTH_IN_HOTSEAT) {
                    x -= indicatorPadding;
                    if (mIsTempPadding) {
                        x += mTempPaddingInHotseat;
                    }
                } else {
                    x = BUBBLE_WIDTH_IN_HOTSEAT / 2 - textWidth / 2 - imgIndicator.getWidth()
                            + indicatorPadding;
                }
            } else {
                x = isTextWidthExcessed(false, textWidth) ? 0 : Math.abs(BUBBLE_WIDTH - textWidth) / 2 - imgIndicator.getWidth() - indicatorPadding;
            }
            x = x < 0 ? 0 : x;
        }

/*          Log.d("Bubble", " sxsexe82------>drawNewIndicator  tag " + getTag() 
                  +" B_W " + BUBBLE_WIDTH + " B_H " + BUBBLE_HEIGHT + " BH_W "
                  + BUBBLE_WIDTH_IN_HOTSEAT + " BH_H " + BUBBLE_HEIGHT_IN_HOTSEAT
                  + " imgW " + sImgNewIndicator.getWidth() + " imgH " + sImgNewIndicator.getHeight() 
                  + " textWidth " + textWidth + " textHeight " +sTitleHeight 
                  + " hotseat " + mHotseatMode + " mIsTempPadding " + mIsTempPadding + " paddingLeft " + getPaddingLeft() 
                  +" getPaddingTop " + getPaddingTop()
                  + " textsize " + getTextSize() +" sTopPaddingHotseat " + sTopPaddingHotseat+ " sTopPadding " + sTopPadding 
                  + " getPaddingTop " + getPaddingTop() + " x " + x + " y " + y
                  + " getExtendedTop " + getExtendedPaddingTop() + " lintop " + getLayout().getLineTop(0));*/
        canvas.drawBitmap(imgIndicator, x, y, paint);
    }

    private boolean updateIndicator(ShortcutInfo info) {
        boolean canBeFlipped = false;
        if (info != null && info.intent != null && mMode != Mode.HIDESEAT &&
            info.itemType == Favorites.ITEM_TYPE_APPLICATION) {
            ComponentName cn = info.intent.getComponent();
            GadgetCardHelper helper = GadgetCardHelper.getInstance(mContext);
            canBeFlipped = helper.hasCardView(cn, this, mMsgNum);
            
            //modify by huangxunwan for config the support the app card by flip in the icon view 20150625
            if (!TopwiseConfig.DEFAULT_FLIP_APP_CARD)
            	canBeFlipped = false;
            //end modify by huangxunwan 
            
            if (canBeFlipped) {
                Log.v(TAG, "updateIndicator: canBeFlipped is true for " + info.title);
            }
        }

        boolean needsUpdate = false;
        if (canBeFlipped) {
            int indicatorColor;
            if (mMode == Mode.NORMAL) {
                indicatorColor = mTitleColor;
            } else if (!AgedModeUtil.isAgedMode()) {
                indicatorColor = IconUtils.TITLE_COLOR_WHITE;
            } else if (mMode == Mode.HIDESEAT) {
                indicatorColor = IconUtils.TITLE_COLOR_WHITE;
            } else {
                indicatorColor = mTitleColor;
            }
            if (indicatorColor == IconUtils.TITLE_COLOR_WHITE) {
                needsUpdate = mIndicator.setCurrentIndicator(IconIndicator.getCardIndicatorWhiteImage());
            } else {
                needsUpdate = mIndicator.setCurrentIndicator(IconIndicator.getCardIndicatorBlackImage());
            }
        } else if (mIsNew && LauncherModel.isShowNewMarkIcon()) {
            needsUpdate = mIndicator.setCurrentIndicator(IconIndicator.getNewIndicatorImage());
        } else {
            needsUpdate = mIndicator.setCurrentIndicator(null);
        }
        if (needsUpdate) {
            calcEnoughSpapce();
            calcTitleHeight();
            invalidate();
            return true;
        } else {
            return false;
        }
    }

    public void updateTitleForIndicator() {
        if (!updateIndicator(mShortcutInfo)) {
            return;
        }

        CharSequence title = (mShortcutInfo != null ? mShortcutInfo.title : getText());
        Drawable icon = isInHotseatOrHideseat() ? getCompoundDrawables()[1] : mCardBackground;
        /*if (icon instanceof FancyDrawable) {
            String label = ((FancyDrawable) icon).getVariableString("app_label");
            if (label != null) title = label;
        }*/
        int textWidth = getTextWidth(title.toString());
        boolean isExcessed = isTextWidthExcessed(isInHotseatOrHideseat(), textWidth);
        if(isExcessed && mIndicator.hasIndicator()) {
            //setText(mDecorateSpaces + title.toString());
            setText(" " + title.toString());
        } else {
            setText(title);
        }
    }

    private boolean isTextWidthExcessed(boolean hotseat, int textWidth) {
        Bitmap imgIndicator = mIndicator.getCurrentIndicator();
        int indicatorPadding = IconIndicator.getIndicatorPadding();
        int indicator_width = imgIndicator == null ? 0 : imgIndicator.getWidth();
        if (hotseat) {
            return (textWidth + indicator_width + indicatorPadding) > BUBBLE_WIDTH_IN_HOTSEAT;
        } else {
            int diff = (textWidth + indicator_width + getPaddingLeft() + getPaddingRight()) - BUBBLE_WIDTH;
            return diff > getOneBlackStringWidth() / 2;
        }
    }

	private void drawVPInstallIndicator(Canvas canvas, Paint paint) {
	    canvas.save();
	    int left = 0;
	    int top = 0;
	    if(isInHotseatOrHideseat()) {
		left = getResources().getDimensionPixelSize(R.dimen.vp_install_indicator_left);
		top = getResources().getDimensionPixelSize(R.dimen.vp_install_indicator_top);
	    }
	    if(mVPIndicatorBitmap == null) {
		mVPIndicatorBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_preinstall_indicator);
	    }
	    canvas.drawBitmap(mVPIndicatorBitmap, left, top, paint);
	    canvas.restore();
	}

    /**
     * Draw downloading mask above app icon
     * @param canvas {@link Canvas} which passed by draw(Canvas canvas
     */
    private void drawDownloadIconMaskIfNeeded(Canvas canvas){
        //draw download icon mask
        if(mIconManager.supprtCardIcon() && !isInHotseatOrHideseat()){
            Drawable downloadmask = mIconManager.getDownloadCardMask();
            downloadmask.setBounds(0, 0, getWidth(), getHeight());
            downloadmask.draw(canvas);
        }
    }

    /**
     * Updates color filter of the icon according to current state:
     * <p>
     * <ul>
     * <li>if it's downloading, put black mask on top of compoundDrawable (app icon)
     * <li>if it's frozen app, put fading filter on top of compoundDrawable
     * <li>otherwise clear the filter
     * </ul>
     * <p>
     * <strong>Note that</strong> this method does not work for <code>FancyDrawable</code>.
     * For <code>FancyDrawable</code>, see {@link #applyFadingEffectInHideseat()}.
     */
    private void updateTopCompoundDrawableFilter() {
        Drawable d = getCompoundDrawables()[1];
        ShortcutInfo info = (ShortcutInfo)getTag();
        if (d == null || info == null) return;
        // For fancy icon, see applyFadingEffectInHideseat()
        //if (d instanceof FancyDrawable) return;

        if (mAnimatedIconFadingFilter != null) {
            d.setColorFilter(mAnimatedIconFadingFilter);
        } else if (mHideseatIconFadingFilterEnable &&
                   info.container == Favorites.CONTAINER_HIDESEAT) {
            d.setColorFilter(HIDESEAT_ICON_FADING_FILTER);
            d.setAlpha(HIDESEAT_ICON_ALPHA);

        } else if (info.isDownloading()) {
            // if it's downloading, put black mask on top compoundDrawable(app icon)
            d.setColorFilter(0x7f000000, PorterDuff.Mode.SRC_ATOP);
        } else {
            d.clearColorFilter();
            if (d.getAlpha() != 255) d.setAlpha(255);
        }
    }

    public void unapplyFadingEffectInHideseat() {
        final Drawable drawable = getCompoundDrawables()[1];
        // Only fancy icons need to explicitly unapply the fading effect.
        /*if (drawable instanceof FancyDrawable) {
            setLayerType(LAYER_TYPE_HARDWARE, null);
        }*/
        invalidate();
    }

    public void applyFadingEffectInHideseat() {
        final Drawable drawable = getCompoundDrawables()[1];
        if (drawable == null) return;
        /*if (drawable instanceof FancyDrawable) {
            // for fancy icon, turn into gray immediately
            Paint paint = new Paint();
            paint.setColorFilter(HIDESEAT_ICON_FADING_FILTER);
            setLayerType(LAYER_TYPE_HARDWARE, paint);
            return;
        }*/
        // for normal bitmap icon, play a transition animation
        final AnimatorSet as = new AnimatorSet();
        final ValueAnimator va = ValueAnimator.ofFloat(1f, 0f);
        va.setDuration(350);
        final ColorMatrix matrix = new ColorMatrix();
        va.addUpdateListener(new AnimatorUpdateListener(){
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = ((Float) animation.getAnimatedValue()).floatValue();
                matrix.setSaturation(value);
                mAnimatedIconFadingFilter = new ColorMatrixColorFilter(matrix);
                drawable.setAlpha(255 - (int) ((255 - HIDESEAT_ICON_ALPHA) * (1 - value)));
                invalidate();
            }
        });
        va.addListener(new AnimatorListener() {
            @Override public void onAnimationStart(Animator arg0) {}
            @Override public void onAnimationRepeat(Animator arg0) {}
            @Override
            public void onAnimationEnd(Animator arg0) {
                mAnimatedIconFadingFilter = null;
                drawable.setAlpha(HIDESEAT_ICON_ALPHA);
                invalidate();
            }
            @Override
            public void onAnimationCancel(Animator arg0) {
                onAnimationEnd(arg0);
            }
        });
        as.play(va);
        as.setStartDelay(400);
        matrix.setSaturation(1);
        mAnimatedIconFadingFilter = new ColorMatrixColorFilter(matrix);
        drawable.setAlpha(255);
        invalidate();
        as.start();
    }

    /**
     * When downloading process was pause, draw pause image in center
     * @param canvas {@link Canvas} which passed by draw(Canvas canvas)
     */
    private void drawPauseImage(Canvas canvas){
        int point[] = getAppIconCenter();
        int centerX = point[0];
        int centerY = point[1];
        float left = centerX - sImgPause.getWidth()/2;
        float top  = centerY - sImgPause.getHeight()/2;
        canvas.drawBitmap(sImgPause,left,top,null);
    }

    /**
     * Draw circle when downloading apps
     */
    private void drawCircle(Canvas canvas, float progress){
        int circleBackColor  = 0x3fffffff;//25% alpha of Color.WHITE
        int circleFrontColor = 0xffffffff;//Color.WHITE
        int point[] = getAppIconCenter();
        int centerX = point[0];
        int centerY = point[1];
        int roundHeadRadius = DOWNLOAD_ARC_WIDTH / 2;
        float sweep = Math.min(360, 360 * progress / 100);
        canvas.save();
        RectF rect = new RectF(centerX - DOWNLOAD_ARC_DRAW_R,
                               centerY - DOWNLOAD_ARC_DRAW_R,
                               centerX + DOWNLOAD_ARC_DRAW_R,
                               centerY + DOWNLOAD_ARC_DRAW_R);
        drawHoloSector(canvas, rect, 0, 360, circleBackColor);
        if ( progress != PROGRESS_NOT_DOWNLOAD ) {
            drawHoloSector(canvas, rect, 270, sweep, circleFrontColor);
            drawRoundHead(canvas, centerX, centerY, 270, roundHeadRadius, circleFrontColor);
            drawRoundHead(canvas, centerX, centerY, 270 + sweep, roundHeadRadius, circleFrontColor);
        }
        canvas.restore();
    }

    /**
     * Make the downloading circle have a round head
     * @param canvas the Canvas will be drawn on
     * @param centerX the big circle center x coordinate
     * @param centerY the big circle center y coordinate
     * @param degree the degree from x-axis positive direction
     * @param radius the radius of the round head
     * @param color the color of the round head
     */
    private void drawRoundHead(Canvas canvas, int centerX, int centerY, float degree, int radius, int color){
        double radian = degree * Math.PI / 180;
        double drawX = centerX + Math.cos(radian) * DOWNLOAD_ARC_DRAW_R;
        double drawY = centerY + Math.sin(radian) * DOWNLOAD_ARC_DRAW_R;
        Paint paint = new Paint();
        paint.setColor(color);
        paint.setAntiAlias(true);
        canvas.drawCircle((float)drawX, (float)drawY, radius, paint);
    }

    /**
     * Draw the circle of Virtual Install
     */
    private void drawCircleVP(Canvas canvas, float progress ){
        int circleBackColor  = 0x3fffffff;//25% alpha of Color.WHITE
        int circleFrontColor = 0xffffffff;//Color.WHITE
        if (progress != 0) {
            final float base= Math.min(360, 360 * progress / 100);
            final float start = 360 - ((base < 180) ? base : (360 - base));
            final float sweep = ((base < 180) ? base : (360 - base)) * 2;
            int point[] = getAppIconCenter();
            int centerX = point[0];
            int centerY = point[1];
            RectF rect = new RectF(centerX - DOWNLOAD_ARC_DRAW_R,
                                   centerY - DOWNLOAD_ARC_DRAW_R,
                                   centerX + DOWNLOAD_ARC_DRAW_R,
                                   centerY + DOWNLOAD_ARC_DRAW_R);
            canvas.save();
            drawHoloSector(canvas, rect, 0, 360, circleBackColor);
            drawHoloSector(canvas, rect, start, sweep,circleFrontColor);
            drawRoundHead(canvas, centerX, centerY, 0, DOWNLOAD_ARC_WIDTH/2, circleFrontColor);
            drawRoundHead(canvas, centerX, centerY, start+sweep, DOWNLOAD_ARC_WIDTH/2, circleFrontColor);
            canvas.restore();
        }
    }

    /**
     * Find the application icon's center position (if you want draw sth around it e.g.)
     * @return the x and y Axis of the app icon center
     */
    private int[] getAppIconCenter(){
        if (mIconManager.supprtCardIcon() && !isInHotseatOrHideseat()){
            return new int[] {getWidth()/2,getHeight()/2};
        }else{
            int iconHeight = getCompoundPaddingTop() - getCompoundDrawablePadding() - getPaddingTop();
            if (AgedModeUtil.isAgedMode()) {
                iconHeight *= AgedModeUtil.SCALE_RATIO_FOR_AGED_MODE;
            }
            int centerY = getPaddingTop() + iconHeight / 2;
            int centerX = getWidth() / 2;
            return new int[] { centerX, centerY };
        }
    }

	@Override
	public void cancelLongPress() {
		super.cancelLongPress();
		mLongPressHelper.cancelLongPress();
	}

	public void setIsNew(boolean isNew) {
	    if (mIsNew != isNew) {
	        mIsNew = isNew;
	        updateTitleForIndicator();
	    }
	}

    /**
     * get if this BubbleTextView is new
     */
    public boolean getIsNew() {
        return mIsNew;
    }

	public void setMessageNum(int num) {
	    mMsgNum = num;
	    mIconMarkNum = num;
		if (num <= 0) {
			mMessageNum = null;
			mIconMarkNum = 0;
		} else if (num > 99) {
			mMessageNum = "99+";
            if (!AgedModeUtil.isAgedMode()) {
                mNumberSize = IND_NUM_SIZE_SMALL;
            } else {
                mNumberSize = IND_NUM_SIZE_NORMAL;
            }
		} else {
			mMessageNum = String.valueOf(num);
			mNumberSize = IND_NUM_SIZE_NORMAL;
		}
		calcNumberPosition(mMessageNum);
	}

    /**
     * get the Message Number
     */
    public int getMessageNum(){
        return mMsgNum;
    }

    public void updateView(ItemInfo info) {
        setMessageNum(info.messageNum);
        setIsNew(info.isNewItem());
        updateProgress(((ShortcutInfo) info).getProgress());
        updateDownloadStatus(((ShortcutInfo) info).getAppDownloadStatus());
        updateVPInstallStatus(((ShortcutInfo) info).getVPInstallStatus());
        updateTitleForIndicator();
        invalidate();
    }

    private void updateTitle() {
        if ((mDownloadStatus == AppDownloadStatus.STATUS_NO_DOWNLOAD) &&
            (mVPInstallStatus == VPInstallStatus.STATUS_NORMAL)){
            return;
        }
        String title = null;
        if (mDownloadStatus != AppDownloadStatus.STATUS_NO_DOWNLOAD) {
            switch (mDownloadStatus) {
            case AppDownloadStatus.STATUS_WAITING:
                title = mContext.getString(R.string.waiting);
                break;
            case AppDownloadStatus.STATUS_DOWNLOADING:
                title = mContext.getString(R.string.downloading);
                break;
            case AppDownloadStatus.STATUS_PAUSED:
                title = mContext.getString(R.string.paused);
                break;
            case AppDownloadStatus.STATUS_INSTALLING:
                title = mContext.getString(R.string.installing);
                break;
            }
        } else if (mVPInstallStatus != VPInstallStatus.STATUS_NORMAL){
            title = mContext.getString(R.string.loading);
        }
        if (title != null && !title.equals(getText())) {
            setText(title);
        }
    }

    private void updateVPInstallStatus(int status) {
        mVPInstallStatus = status;
    }
	private void updateDownloadStatus(int status) {
		mDownloadStatus = status;
	}

	private void updateProgress(int progress) {
		mProgress = progress;
		// setText(String.valueOf(mProgress));
	}

    private void drawHoloSector(Canvas canvas, RectF oval, float startAngle,
            float sweepAngle, int color) {
        final Paint paint = new Paint();
        paint.setColor(color);
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(DOWNLOAD_ARC_WIDTH);
        canvas.drawArc(oval, startAngle, sweepAngle, false, paint);
    }

    public static void setNeedReInitParams() {
        mInited = false;
    }

    public void setClicked(boolean clicked) {
        mClicked = clicked;
    }

    public Drawable getIconDrawable() {
        Drawable icon = null;
        if (mIconManager.supprtCardIcon()) {
            icon = new FastBitmapDrawable(getDrawingCache());
        } else {
            icon = this.getCompoundDrawables()[1];
        }
        return icon;
    }

	// override to extract text label from fancy drawable
    @Override
    public void invalidateDrawable(Drawable drawable) {
        Drawable icon = ((isInHotseatOrHideseat() && !AgedModeUtil.isAgedMode()) || mMode == Mode.HIDESEAT) ?
                        getCompoundDrawables()[1] : mCardBackground;
        if (icon != drawable) {
            super.invalidateDrawable(drawable);
            return;
        }
        /*if (drawable instanceof FancyDrawable) {
            String label = ((FancyDrawable) drawable).getVariableString("app_label");
            if (label != null) {
                if (mShortcutInfo != null) {
                    setTitle(mShortcutInfo.title);
                } else {
                    setText(label);
                }
            }
        }*/
        if (getWindowToken() == null ) {
            FolderInfo info = Launcher.findFolderInfo(mShortcutInfo.container);
            if (info != null)
                info.invalidate(this, mShortcutInfo);
        } else if (mSupportCard && !isInHotseatOrHideseat() && mCardBackground != null) {
            invalidate();
        } /*else if (drawable instanceof FancyDrawable) {
            invalidate();
        } */else {
            super.invalidateDrawable(drawable);
        }
    }

    @Override
    public void postInvalidate() {
        if (getWindowToken() == null && mShortcutInfo != null) {
            FolderInfo info = Launcher.findFolderInfo(mShortcutInfo.container);
            if (info != null)
                info.invalidate(this, mShortcutInfo);
        } else {
            super.postInvalidate();
        }
    }

    public static int getBUBBLE_WIDTH() {
        return BUBBLE_WIDTH;
    }
    public static int getBUBBLE_HEIGHT() {
        return BUBBLE_HEIGHT;
    }

    public void shake(){
        ObjectAnimator.ofFloat(this, "translationY", 0, 25, -25, 25, -25,15, -15, 0).start();
    }

    /**
     * Get the Notification amount of this application.
     * If this BubbleTextView doesn't represent application (bookmark, etc.) return zero.
     */
    public int getNotificationCount() {
        int result = 0;
        if (getTag() != null) {
            ShortcutInfo info = (ShortcutInfo) getTag();
            if (info != null && info.intent != null) {
                ComponentName cn = info.intent.getComponent();
                if (cn != null) {
                    GadgetCardHelper helper = GadgetCardHelper.getInstance(mContext);
                    result = helper.getNotificationCount(cn, BubbleTextView.this);
                }
            }
        }
        if (!LauncherModel.showNotificationMark()) {
            mIconMarkNum = 0;
        } else if (mMsgNum > 0) {
            mIconMarkNum = mMsgNum;
        } else {
            mIconMarkNum = result;
        }
        return result;
    }
    
    /**
     * Becasue onThemeChange, we use the BubbleTextView has been created and do not create a new one,
     * so the padding will be reset to 0
     *
     */
    public void preThemeChange() {
        setPadding(getPaddingLeft(), 0, getPaddingRight(), 0);
    }

    public static void adjustToThreeLayout() {
        setNeedReInitParams();
    }

    public static void adjustFromThreeLayout() {
        setNeedReInitParams();
    }

    public int getIconMarkNum() {
        return mIconMarkNum;
    }
}
