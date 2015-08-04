package com.tpw.homeshell;

import java.util.HashMap;

import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.graphics.Bitmap;
import android.graphics.Camera;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Scroller;

import com.tpw.homeshell.setting.HomeShellSetting;

/**
 * A subclass of SmoothPagedView to handle scroll effect
 * Mainly implemented in dispatchDraw
 */
public class AnimationPagedView extends SmoothPagedView {

    public static final float CAMERA_DISTANCE = 6500;
    public static String TAG = "AnimationPagedView";

    private static final float EDIT_SCALE = 0.8f;

    private int mPageAnimType;
    private Camera mCamera;
    private Matrix mMatrix;
    private Launcher mLauncher;
    private SharedPreferences mSharedPref;
    private OnSharedPreferenceChangeListener mSharedPrefListener;

    private Scroller mOriginScroller;
    private Scroller mBounceScroller;

    // add fade out animation when screen stop moving in edit
    // mode.
    private boolean mNeedFadeoutAnimation;
    private ValueAnimator mFadeoutAnimator;

    /**
     * When scroll started, we save view's drawing cache in mIconBitmapCache.
     * Every time {@link #dispatchDraw} was called, get Bitmap from it to avoid
     * repeatedly calling {@link View#getDrawingCache}.
     */
    private HashMap<String,Bitmap> mIconBitmapCache;

    public static class Type {
        public final static int HORIZONTAL = 0;
        public final static int BOX_OUT = 1;
        public final static int BOX_IN = 2;
        public final static int ROLL_UP = 3;
        public final static int ROLL_DOWN = 4;
        public final static int ROLL_WINDOW = 5;
        public final static int ROLL_OVER = 6;
        public final static int SCALE_IN_OUT = 7;
        public final static int RANDOM_SWITCH = 8;
        public final static int RIGHT_FADE = 9;
    }

    public AnimationPagedView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0);
    }

    public AnimationPagedView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs, defStyle);
    }

    private void init(Context context, AttributeSet attrs, int defStyle) {
        mCamera = new Camera();
        mMatrix = new Matrix();
        mLauncher = LauncherApplication.mLauncher;
        mIconBitmapCache = new HashMap<String,Bitmap>();
        mPageAnimType = Type.HORIZONTAL;
        mOriginScroller = mScroller;
        mBounceScroller = new Scroller(context, new BounceBackInterpolator());
        // reset transition effect configs when effect style is changed.
        mSharedPref = context.getSharedPreferences("com.tpw.homeshell_preferences",
                Context.MODE_PRIVATE);
        mSharedPrefListener = new OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                if (HomeShellSetting.KEY_PRE_EFFECT_STYLE.equals(key)) {
                    resetTransitionEffect();
                }
            }
        };
        mSharedPref.registerOnSharedPreferenceChangeListener(mSharedPrefListener);
    }
    // reset transition effect configs.
    private void resetTransitionEffect() {
        for (int i = 0; i < getChildCount(); i++) {
            View page = getPageAt(i);
            page.setPivotX(0);
            page.setPivotY(0);
            page.setRotation(0);
            page.setRotationX(0);
            page.setRotationY(0);
            // Do not set EDIT_SCALE for AppsCustomizePagedView
            if (mLauncher.isEditMode() && (this instanceof Workspace)) {
                page.setScaleX(EDIT_SCALE);
                page.setScaleY(EDIT_SCALE);
            } else {
                page.setScaleX(1f);
                page.setScaleY(1f);
            }
            if (mLauncher.isEditMode() && (this instanceof Workspace)) {
                page.setTranslationX(page.getWidth() * (1 - EDIT_SCALE) / 2);
            } else {
                page.setTranslationX(0f);
            }
            page.setTranslationY(0f);
            page.setVisibility(VISIBLE);
            page.setAlpha(1f);

            ViewGroup container;
            if (page instanceof CellLayout) {
                CellLayout cellLayout = (CellLayout) page;
                container = cellLayout.getShortcutAndWidgetContainer();
            } else if (page instanceof PagedViewCellLayout) {
                PagedViewCellLayout cellLayout = (PagedViewCellLayout) page;
                container = cellLayout.getChildrenLayout();
            } else if (page instanceof PagedViewGridLayout) {
                container = (ViewGroup) page;
            } else {
                // never
                return;
            }
            for (int j = 0; j < container.getChildCount(); j++) {
                View view = container.getChildAt(j);
                view.setPivotX(view.getMeasuredWidth() * 0.5f);
                view.setPivotY(view.getMeasuredHeight() * 0.5f);
                view.setRotation(0);
                view.setRotationX(0);
                view.setRotationY(0);
                view.setScaleX(1f);
                view.setScaleY(1f);
                view.setTranslationX(0f);
                view.setTranslationY(0f);
                view.setVisibility(VISIBLE);
                view.setAlpha(1f);
            }
        }
    }

    protected double getPercentage(View child, int screen){
        /*  CellLayout related size
         *                       current content
         *                       ***************
         *  *********   *********   *********   *********
         *  *       *   *       *   *       *   *       *
         *  *       *   *       *   *       *   *       *
         *  *       *   *       *   *       *   *       *
         *  *       *   *       *   *       *   *       *
         *  *********   *********   *********   *********
         *           ***         ***************
         *            *                 *
         *            *                 *
         *            *       getWidth() == child.getWidth() + 2 * gapOfCellLayouts
         *    gapOfCellLayouts
         *
         *  mScroll change (child.getWidth() + gapOfCellLayouts) every time you scroll
         */

        // the gap between two CellLayouts
        double gapOfCellLayouts = ( getWidth() - child.getWidth() ) / 2;
        double molecular   = getScrollX() - ( getChildOffset(screen) - gapOfCellLayouts );
        double denominator = child.getWidth() + gapOfCellLayouts;
        double percentage  = molecular / denominator;

        if( percentage < -1 || percentage > 1 ) {
            // for the scroll between first and last screen
            if( getScrollX() < 0 ) {
                percentage = 1 + getScrollX() / denominator;
            }else{
                int last = getChildCount() - 1;
                int leftEdge = getChildOffset(last) + child.getWidth();
                // check the current screen position in editmode.
                if (mLauncher.isEditMode()) {
                    if ((getChildOffset(screen) + child.getWidth()) > getScrollX()) {
                        percentage = (getScrollX() - leftEdge) / denominator;
                    }
                } else {
                    percentage = (getScrollX() - leftEdge) / denominator;
                }
                //percentage = (getScrollX() - leftEdge) / denominator;
            }
        }

        return percentage;
    }

    // add fade out animation when screen stop moving in edit mode.
    void startFadeoutAnimator() {
        if (mFadeoutAnimator == null) {
            mFadeoutAnimator = ValueAnimator.ofFloat(1, 0);
            mFadeoutAnimator.setDuration(150);
            mFadeoutAnimator.addUpdateListener(new AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animator) {
                    invalidate();
                }
            });
        }
        mFadeoutAnimator.start();
    }

    private boolean needFadeoutAnimation() {
        if (mFadeoutAnimator != null && mFadeoutAnimator.isRunning()) {
            return true;
        } else if (mNeedFadeoutAnimation
                && mLauncher.isEditMode()
                && (mFadeoutAnimator == null || !mFadeoutAnimator.isRunning())
                && (mPageAnimType == Type.BOX_OUT || mPageAnimType == Type.BOX_IN || mPageAnimType == Type.SCALE_IN_OUT)) {
            startFadeoutAnimator();
            mNeedFadeoutAnimation = false;
            return true;
        }
        return false;
    }

    /**
     * Draw the CellLayout and add effect at the same time
     *
     * @param canvas the {@link Canvas} which whole content will be displayed
     * @param screen current screen index
     * @param drawingTime
     */
    protected void drawScreen(Canvas canvas, int screen, long drawingTime) {
        if (mLauncher.getDragController().isDragging() || (!isPageMoving()
                // add fade out animation when screen stop moving
                // in edit mode.
                && !needFadeoutAnimation())) {
            // if dragging or not moving, just use default implementation
            super.drawScreen(canvas, screen, drawingTime);
            return;
        }

        //if it's need to call ViewGroup#drawChild
        boolean drawChild = true;
        // support mainmenu feature, cancel drop feature, page management feature, and so on
        //View child = getChildAt(screen);
        View child = getPageAt(screen);

        canvas.save();
        mCamera.save();

        double percentage = getPercentage(child, screen);
        if( percentage < -1 || percentage > 1 ) return;

        /*
         * scroll to right：left 0% ~ 100%   right -100% ~ 0%
         * scroll to left ：left 100% ~ 0%   right 0% ~ -100%
         */

        switch (mPageAnimType) {
            case Type.HORIZONTAL:
                break;
            case Type.BOX_OUT:
                drawChild = boxOut(canvas, screen, percentage);
                break;
            case Type.BOX_IN:
                drawChild = boxIn(canvas, screen, percentage);
                break;
            case Type.ROLL_UP:
                drawChild = rollUp(canvas, screen, percentage);
                break;
            case Type.ROLL_DOWN:
                drawChild = rollDown(canvas, screen, percentage);
                break;
            case Type.ROLL_WINDOW:
                drawChild = rollWindow(child, screen, (float)percentage);
                break;
            case Type.ROLL_OVER:
                drawChild = rollOver(child, screen, (float)percentage);
                break;
            case Type.SCALE_IN_OUT:
                drawChild = scaleInOut(canvas, screen, percentage);
                break;
            case Type.RANDOM_SWITCH:
                drawChild = randomSwitch(child, screen, (float)percentage);
                break;
            case Type.RIGHT_FADE:
                if (mLauncher.isEditMode())
                    drawChild = rightFadeEditMode(child,screen,(float)percentage);
                else
                    drawChild = rightFade(child,screen,(float)percentage);
                break;
            default:
                break;
        }

        if (drawChild)
            drawChild(canvas, child, drawingTime);
        mCamera.restore();
        canvas.restore();
    }

    /*
     * Scroll like looking from outsize of a box
     */
    private boolean boxOut(Canvas canvas, int screen, double percentage ){
        if (mFadeoutAnimator != null && mFadeoutAnimator.isRunning()) {
            percentage = ((Float) mFadeoutAnimator.getAnimatedValue()) * percentage;
        }
        float angle = 90 * (float)percentage;
        float centerX, centerY;

        View child = getPageAt(screen);
        int childW = child.getWidth();
        int childH = child.getHeight();
        int pageOffsetX = getChildOffset(screen);
        int pageOffsetY = (int) ((getHeight() - child.getHeight()) / 2);

        if( angle >= 0 ){
            centerX = childW + pageOffsetX;
            if (mLauncher.isEditMode())
                centerX -= mPageSpacing;
            centerY = pageOffsetY + childH / 2;
        }else{
            centerX = pageOffsetX;
            if (mLauncher.isEditMode())
                centerX += mPageSpacing;
            centerY = pageOffsetY + childH / 2;
        }
        mCamera.rotateY(-angle); // rotate around Y-axis reversely
        mCamera.setLocation(0, 0, -14 * getDensity());
        mCamera.getMatrix(mMatrix);
        mMatrix.preScale(1.0f - (Math.abs((float) percentage) * 0.3f), 1.0f);
        mMatrix.preTranslate(-centerX, -centerY);
        mMatrix.postTranslate(centerX, centerY);
        canvas.concat(mMatrix);
        return true;
    }

    /*
     * Scroll like looking from inside of a box
     */
    private boolean boxIn(Canvas canvas, int screen, double percentage ){
        if (mFadeoutAnimator != null && mFadeoutAnimator.isRunning()) {
            percentage =  ((Float)mFadeoutAnimator.getAnimatedValue()) * percentage;
        }
        float angle = 90 * (float)percentage;
        float centerX, centerY, changeZ;

        View child = getPageAt(screen);
        int childW = child.getWidth();
        int childH = child.getHeight();
        int pageOffsetX = getChildOffset(screen);
        int pageOffsetY = (int) ((getHeight() - child.getHeight()) / 2);

        if( angle >= 0 ){
            centerX = childW + pageOffsetX;
            if (mLauncher.isEditMode())
                centerX -= mPageSpacing;
            centerY = pageOffsetY + childH / 2;
        }else{
            centerX = pageOffsetX;
            if (mLauncher.isEditMode())
                centerX += mPageSpacing;
            centerY = pageOffsetY + childH / 2;
        }

        // In case of image expand, change Z-order
        if (angle >= 0) {
            // far to near (0-45), near to far (45-90)
            if (angle <= 45.0f) {
                changeZ = childW*(float)Math.sin(2 * Math.PI * angle /360f);
                mCamera.translate( 0, 0, changeZ );
            } else {
                changeZ = childW*(float)Math.sin(2 * Math.PI *(90-angle)/360f);
                mCamera.translate( 0, 0, changeZ );
            }
        } else {
            // make sure that two views join well
            if (angle > -45.0f) {
                changeZ = childW * (float) Math.sin(2 * Math.PI * (-angle)/ 360f);
                mCamera.translate( 0, 0, changeZ );
            } else {
                changeZ = childW * (float) Math.sin(2 * Math.PI* (90.0f + angle) / 360f);
                mCamera.translate( 0, 0, changeZ );
            }
        }
        mCamera.rotateY(angle);
        mCamera.setLocation(0, 0, -12 * getDensity());
        mCamera.getMatrix(mMatrix);
        mMatrix.preScale(1.0f - (Math.abs((float) percentage) * 0.5f), 1.0f);
        mMatrix.preTranslate(-centerX, -centerY);
        mMatrix.postTranslate(centerX, centerY);
        canvas.concat(mMatrix);

        return true;
    }

    /*
     * Rotate around the top of the screen
     */
    private boolean rollUp(Canvas canvas, int screen, double percentage ){
        float angle = 90 * (float)percentage;
        float baseAngle = angle * 0.25f; // Maximum Angle 30
        float centerX, centerY;

        View child = getPageAt(screen);
        int childW = child.getWidth();
        int childH = child.getHeight();
        double gapOfCellLayouts = ( getWidth() - childW ) / 2;
        double switchWidth = getChildCount() * ( childW + gapOfCellLayouts );
        double wholeWidth = gapOfCellLayouts + switchWidth;

        if( getScrollX() < 0 && screen == getChildCount()-1 ) {
            centerX = getScrollX() + (float)switchWidth + getWidth() / 2;
        }else if( getScrollX() + getWidth() > wholeWidth && screen == 0){
            centerX = getScrollX() - (float)switchWidth + getWidth() / 2;
        }else{
            centerX = getScrollX() + getWidth() / 2;
        }
        centerY = getScrollY() - childH * 0.3f;

        mMatrix.reset();
        mMatrix.setRotate(baseAngle);
        mMatrix.preTranslate(-centerX, -centerY);
        mMatrix.postTranslate(centerX, centerY);
        canvas.concat(mMatrix);

        return true;
    }

    /*
     * Rotate around the bottom of the screen
     */
    private boolean rollDown(Canvas canvas, int screen, double percentage ){
        float angle = 90 * (float)percentage;
        float baseAngle = -angle * 0.333f; // Maximum Angle 30
        float centerX, centerY;

        View child = getPageAt(screen);
        int childW = child.getWidth();
        int childH = child.getHeight();
        double gapOfCellLayouts = ( getWidth() - childW ) / 2;
        double switchWidth = getChildCount() * ( childW + gapOfCellLayouts );
        double wholeWidth = gapOfCellLayouts + switchWidth;

        if( getScrollX() < 0 && screen == getChildCount()-1 ) {
            centerX = getScrollX() + (float)switchWidth + getWidth() / 2;
        }else if( getScrollX() + getWidth() > wholeWidth && screen == 0){
            centerX = getScrollX() - (float)switchWidth + getWidth() / 2;
        }else{
            centerX = getScrollX() + getWidth() / 2;
        }
        centerY = childH * 1.3f;

        mMatrix.reset();
        mMatrix.setRotate(baseAngle);
        mMatrix.preTranslate(-centerX, -centerY);
        mMatrix.postTranslate(centerX, centerY);
        canvas.concat(mMatrix);
        return true;
    }

    private boolean rollOver(View v, int i, float scrollProgress){
        if (mLauncher.isEditMode()) {
            scrollProgress = scrollProgress / 0.85f;
            scrollProgress = Math.min(1.0f, scrollProgress);
            scrollProgress = Math.max(-1.0f, scrollProgress);
        }

        v.setCameraDistance( getDensity() * CAMERA_DISTANCE);
        boolean drawChild;
        if (scrollProgress >= -0.5f && scrollProgress <= 0.5f) {
            drawChild = true;
        }else{
            drawChild = false;
        }

        int offset = 0;
        if( scrollProgress > 0.5 ){
            scrollProgress = 1 - scrollProgress;
            if (mLauncher.isEditMode()) {
                offset = - mPageSpacing;
            }
        }else if( scrollProgress < -0.5){
            scrollProgress = - 1 - scrollProgress;
            if (mLauncher.isEditMode()) {
                offset = + mPageSpacing;
            }
        }

        float rotation = -180.0f * Math.max(-1f, Math.min(1f, scrollProgress));
        v.setPivotX(v.getMeasuredWidth() * 0.5f);
        if (!mLauncher.isEditMode())
            v.setPivotY(v.getMeasuredHeight() * 0.5f);
        v.setRotationY(rotation);
        v.setTranslationX(v.getMeasuredWidth() * scrollProgress + offset);

        return drawChild;
    }

    /*
     * Flip around y-axis
     */
    private boolean rollWindow(View v, int screen, float scrollProgress){
        ViewGroup container;
        if (v instanceof CellLayout) {
            CellLayout cellLayout = (CellLayout) v;
            container = cellLayout.getShortcutAndWidgetContainer();
        } else if (v instanceof PagedViewCellLayout) {
            PagedViewCellLayout cellLayout = (PagedViewCellLayout) v;
            container = cellLayout.getChildrenLayout();
        } else if (v instanceof PagedViewGridLayout) {
            container = (ViewGroup)v;
        } else {
            //never
            return false;
        }

        if (Math.abs(scrollProgress) < 0.5f) {
            v.setAlpha(1);
            if (mLauncher.isEditMode() && scrollProgress != 0)
            {
                float trans = scrollProgress * (getWidth() + v.getWidth()) / 2;
                //v.setTranslationX(trans);
            }
            else if (!mLauncher.isEditMode())
            {
                v.setTranslationX(scrollProgress * (getWidth() + v.getWidth()) / 2);
            }
        } else {
            v.setAlpha(0);
        }

        for (int i = 0; i < container.getChildCount(); i++) {
            View view = container.getChildAt(i);

            view.setCameraDistance(10000);

            view.setPivotX(view.getWidth() * 0.5f);
            view.setRotationY(-scrollProgress * 180f);
        }
        return true;
    }

    /*
     * Squash and Stretch
     */
    private boolean scaleInOut(Canvas canvas, int screen, double percentage){
        if (mFadeoutAnimator != null && mFadeoutAnimator.isRunning()) {
            percentage =  ((Float)mFadeoutAnimator.getAnimatedValue()) * percentage;
        }
        float angle = 90f * (float)percentage;
        View child = getPageAt(screen);
        int pageOffsetX = getChildOffset(screen);
        int pageOffsetY = (int) ((getHeight() - child.getHeight()) / 2);

        if (angle >= 0) { // left page
            float centerX = pageOffsetX + child.getWidth();
            if (mLauncher.isEditMode())
                centerX -= mPageSpacing;
            float centerY = pageOffsetY;
            canvas.translate(centerX, centerY);
            canvas.scale((90.0f - angle) / 90.0f, 1.0f);
            canvas.translate(-centerX, -centerY);
        } else {
            float centerX = pageOffsetX;
            if (mLauncher.isEditMode())
                centerX += mPageSpacing;
            float centerY = pageOffsetY;
            canvas.translate(centerX, centerY);
            canvas.scale((90.0f + angle) / 90.0f, 1.0f);
            canvas.translate(-centerX, -centerY);
        }
        return true;
    }

    /*
     * Icons change it's position randomly in Y-axis.
     */
    private boolean randomSwitch(View v, int screen, float scrollProgress){
        ViewGroup container;
        float verticalDelta;

        if (v instanceof CellLayout) {
            CellLayout cellLayout = (CellLayout) v;
            container = cellLayout.getShortcutAndWidgetContainer();
            verticalDelta = 0.7f * cellLayout.getCellHeight()
                    * (float) (1 - Math.abs(2 * Math.abs(scrollProgress) - 1));
        } else if (v instanceof PagedViewCellLayout) {
            PagedViewCellLayout cellLayout = (PagedViewCellLayout) v;
            container = cellLayout.getChildrenLayout();
            verticalDelta = 0.7f * cellLayout.getCellHeight()
                    * (float) (1 - Math.abs(2 * Math.abs(scrollProgress) - 1));
        } else if (v instanceof PagedViewGridLayout) {
            PagedViewGridLayout layout = (PagedViewGridLayout)v;
            container = (ViewGroup)v;
            verticalDelta = 0.7f * (layout.getHeight()/layout.getCellCountY())
                    * (float) (1 - Math.abs(2 * Math.abs(scrollProgress) - 1));
        } else {
            //never
            return false;
        }

        for (int i = 0; i < container.getChildCount(); i++) {
            View view = container.getChildAt(i);
            int index;
            if (v instanceof CellLayout) {
                ItemInfo info = (ItemInfo) view.getTag();
                if (info == null) {
                    index = i;
                } else {
                    index = info.cellX;
                }
            } else {
                index = i;
            }

            if (index % 2 == 0) {
                // even columns
                view.setTranslationY(verticalDelta);
            } else {
                // odd columns
                view.setTranslationY(-verticalDelta);
            }
        }
        return true;
    }

    /*
     * Left side scroll to left.Right side fade away.
     */
    private boolean rightFade(View v, int i, float scrollProgress){
        if( scrollProgress >= 0 ){
            v.setScaleX(1);
            v.setScaleY(1);
            v.setAlpha(1);
            v.setTranslationX(0);
            return true;
        }

        double gapOfCellLayouts = ( getWidth() - v.getWidth() ) / 2;
        float scale = 0.5f + 0.5f * (float)(1+scrollProgress);
        float alpha = (1+scrollProgress);
        float trans = Math.abs(scrollProgress) * (float)(gapOfCellLayouts + v.getWidth());

        v.setPivotX(v.getWidth()/2);
        v.setPivotY(v.getHeight()/2);
        v.setScaleX(scale);
        v.setScaleY(scale);
        v.setAlpha(alpha);
        v.setTranslationX(-trans);

        return true;
    }

    private boolean rightFadeEditMode(View v, int i, float scrollProgress){

        double gapOfCellLayouts = ( getWidth() - v.getWidth() ) / 2;
        float trans = Math.abs(scrollProgress) * (float)(gapOfCellLayouts + v.getWidth());

        if( scrollProgress >= 0 ){
            v.setPivotX(v.getWidth() / 2);
            //v.setPivotY(0);
            v.setScaleX(EDIT_SCALE);
            v.setScaleY(EDIT_SCALE);
            v.setAlpha(1);
            v.setTranslationX(0);
            return true;
        }

        float scaleFactor = EDIT_SCALE / 2.0f;
        float scale = scaleFactor + scaleFactor * (float)(1 + scrollProgress);
        float alpha = (1 + scrollProgress);

        v.setPivotX(v.getWidth() / 2);
        //v.setPivotY(0);
        v.setScaleX(scale);
        v.setScaleY(scale);
        v.setAlpha(alpha);
        v.setTranslationX(-trans);

        return true;
    }

    @Override
    protected void onPageBeginMoving() {
        super.onPageBeginMoving();
        mNeedFadeoutAnimation = true;
        mPageAnimType = HomeShellSetting.getSlideEffectMode(this.getContext());
        if( mPageAnimType == Type.ROLL_UP || mPageAnimType == Type.ROLL_DOWN ){
            mScroller = mBounceScroller;
        }
        Log.d(TAG,"onPageBeginMoving mPageAnimType="+mPageAnimType);
    }

    @Override
    protected void onPageEndMoving() {
        super.onPageEndMoving();
        mIconBitmapCache.clear();
        if( mPageAnimType == Type.ROLL_UP || mPageAnimType == Type.ROLL_DOWN ){
            mScroller = mOriginScroller;
        }
        // recovers the transformation during sliding
        if( mPageAnimType == Type.RIGHT_FADE ){
            if(this instanceof Workspace && ((Workspace)this).getOpenFolder() != null ){
                return;
            }

            for( int i = 0; i < getChildCount(); i++ ){
                View v = getPageAt(i);
                if( v != null ){
                    float scaleX = 1.0f;
                    float scaleY = 1.0f;
                    float transX = 0f;

                    if (mLauncher.isEditMode())
                    {
                        scaleX = EDIT_SCALE;
                        scaleY = EDIT_SCALE;
                        //transX = v.getWidth() * 0.1f;
                        //transX = (getWidth() - v.getWidth()) / 2.0f;
                    }

                    v.setScaleX(scaleX);
                    v.setScaleY(scaleY);
                    v.setAlpha(1);
                    v.setTranslationX(-transX);
                }
            }
        } else if (mPageAnimType == Type.ROLL_WINDOW ||
                   mPageAnimType == Type.RANDOM_SWITCH) {
            recoverPageTransformation(true);
        } else if (mPageAnimType == Type.ROLL_OVER){
            recoverPageTransformation(true);
        }
    }

    /**
     * Recovers the transformation that applied to page and icons
     * during the sliding effect.<p>
     * Currently, {@link Type#ROLL_WINDOW} and {@link Type#RANDOM_SWITCH}
     * use this method to recover transformation to default state in
     * {@link #onPageEndMoving()}.
     * @param page
     */
    private void recoverPageTransformation(boolean childRecover) {
        for (int page = 0; page < getChildCount(); page++) {
            page = (page + getChildCount()) % getChildCount();
            View v = getPageAt(page);
            if (!mLauncher.isEditMode())
                v.setTranslationX(0);
            v.setAlpha(1);
            if (childRecover != true) {
                return;
            }

            ViewGroup container;
            if (v instanceof CellLayout) {
                CellLayout cellLayout = (CellLayout) v;
                container = cellLayout.getShortcutAndWidgetContainer();
            } else if (v instanceof PagedViewCellLayout) {
                PagedViewCellLayout cellLayout = (PagedViewCellLayout) v;
                container = cellLayout.getChildrenLayout();
            } else if (v instanceof PagedViewGridLayout) {
                container = (ViewGroup) v;
            } else {
                // never
                return;
            }

            for (int i = 0; i < container.getChildCount(); i++) {
                View view = container.getChildAt(i);
                view.setRotationY(0);
                view.setTranslationY(0);
            }
        }
    }

    private static class BounceBackInterpolator extends ScrollInterpolator {
        public BounceBackInterpolator() {
        }

        public float getInterpolation(float t) {
            t = super.getInterpolation(t);
            float UP_BOUND = 1.1f;
            float TURN_POINT = 0.9f;
            if( t < TURN_POINT ){
                return t * ( UP_BOUND / TURN_POINT );
            }else{
                return UP_BOUND - (t - TURN_POINT) * ( (UP_BOUND-1) / (1-TURN_POINT) );
            }
        }
    }

    @Override
    public void syncPages() {}
    @Override
    public void syncPageItems(int page, boolean immediate) {}
}
