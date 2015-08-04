
package com.tpw.homeshell;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class PageIndicatorView extends View {

    private final Bitmap mDefault;
    private final Bitmap mFocus;
    
    private int mMax;
    private int mPos;
    private int mPrePos;
    private int mPointGap;
    private Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
    private int mLineTop;
    private int mLinePadding;
    private ValueAnimator mSwitchAnimator;
    private float mAnimationProgress = 1;
    private boolean needLine = true;

    public PageIndicatorView(Context context) {
        super(context, null);
    }

    public PageIndicatorView(Context context, AttributeSet attrs) {
        super(context, attrs, 0);
    }

    public PageIndicatorView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    {
        mDefault = BitmapFactory.decodeResource(getContext().getResources(),
                R.drawable.homeshell_page_indicator_default);
        mFocus = BitmapFactory.decodeResource(getContext().getResources(),
                R.drawable.homeshell_page_indicator_focused);
        mPaint.setColor(0xffffffff);
        mLineTop = mDefault.getHeight() / 2;
        mPaint.setStrokeWidth(getResources().getDisplayMetrics().density);
        mLinePadding = mDefault.getWidth();
        mPointGap = getResources().getDimensionPixelSize(R.dimen.page_indicator_padding_left);
    }
    
    public void setNeedLine(boolean need){
        needLine = need;
    }

    public void setMax(int max) {
        mMax = max;
    }

    public int getMax() {
        return mMax;
    }

    public void setCurrentPos(int pos) {
        if (mPos != pos) {
            mPrePos = mPos;
            mPos = pos;
            startSwitchAnimator();
        }
    }
    
    private void startSwitchAnimator() {
        cancelSwitchAnimator();
        mSwitchAnimator = ValueAnimator.ofFloat(0, 1f);
        mSwitchAnimator.setDuration(200);
        mSwitchAnimator.addUpdateListener(mAnimatorUpdateListener);
        mSwitchAnimator.addListener(mAnimatorListener);
        mSwitchAnimator.start();
    }
    
    private ValueAnimator.AnimatorUpdateListener mAnimatorUpdateListener = new ValueAnimator.AnimatorUpdateListener() {
        public void onAnimationUpdate(ValueAnimator animation) {
            mAnimationProgress = (Float) animation.getAnimatedValue();
            invalidate();
        }
    };
    
    private AnimatorListener mAnimatorListener = new AnimatorListener() {
        public void onAnimationStart(Animator animation) {
        }
        public void onAnimationRepeat(Animator animation) {
        }
        public void onAnimationEnd(Animator animation) {
        }
        public void onAnimationCancel(Animator animation) {
        }
    };

    public void cancelSwitchAnimator() {
        if (mSwitchAnimator != null && mSwitchAnimator.isRunning()) {
            mSwitchAnimator.cancel();
        }
        mSwitchAnimator = null;
    }
    
    public int getCurrentPos() {
        return mPos;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if(mDefault != null) {
            int h = Math.max(mDefault.getHeight(), mFocus.getHeight());
            setMinimumHeight(h);
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if(mDefault == null || mFocus == null || mMax <= 0) {
            return;
        }
        int padding = mLinePadding;
        
        int left = ( getWidth() - mMax*mDefault.getWidth() - mPointGap*(mMax-1) )/2;
        final int hDefault = (getHeight() -  mDefault.getHeight())/2;
        final int hFocus = (getHeight() -  mFocus.getHeight())/2;
        int lineTop = mLineTop + hDefault;
        mPaint.setAlpha(0x10);
        if( needLine ){
            canvas.drawLine(0, lineTop, left - padding, lineTop, mPaint);
        }
        float p = mAnimationProgress;
        for(int i = 0;i < mMax;i++) {
            if(i != mPos)
                if(i != mPrePos) {
                    mPaint.setAlpha(0xff);
                    canvas.drawBitmap(mDefault, left, hDefault, null);
                } else {
                    mPaint.setAlpha((int)(0xff * p));
                    canvas.drawBitmap(mDefault, left, hDefault, mPaint);
                    mPaint.setAlpha((int)(0xff * (1 - p)));
                    canvas.drawBitmap(mFocus, left, hFocus, mPaint);
                }
            else {
                mPaint.setAlpha((int)(0xff * (1 - p)));
                canvas.drawBitmap(mDefault, left, hDefault, mPaint);
                mPaint.setAlpha((int)(0xff * p));
                canvas.drawBitmap(mFocus, left, hFocus, mPaint);
            }
            left += mDefault.getWidth();
            if( i != mMax-1 ){
                left += mPointGap;
            }
        }
        mPaint.setAlpha(0x10);
        if( needLine ){
            canvas.drawLine(left + padding, lineTop, getWidth(), lineTop, mPaint);
        }
    }

}
