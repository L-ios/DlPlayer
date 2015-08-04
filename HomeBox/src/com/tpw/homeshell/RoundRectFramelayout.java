
package com.tpw.homeshell;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.widget.FrameLayout;

public class RoundRectFramelayout extends FrameLayout {
    private float mDensity = Resources.getSystem().getDisplayMetrics().density;
    private float mRadius = mDensity * 20;
    private Path mPath = new Path();
    private RectF mRect = new RectF();
    private boolean mClip = true;
    int mOffsetY;

    public RoundRectFramelayout(Context context) {
        super(context);
    }

    public RoundRectFramelayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public RoundRectFramelayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setCornerRadius(float radius) {
        mRadius = radius;
    }

    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (changed && mRadius > 0) {
            mRect.set(left, top + mOffsetY, right, bottom + mOffsetY);
            mPath.rewind();
            mPath.addRoundRect(mRect, mRadius, mRadius, Path.Direction.CW);
        }
    }

    protected void dispatchDraw(Canvas canvas) {
        if (mClip)
            canvas.clipPath(mPath);
        super.dispatchDraw(canvas);
    }

    public void setClip(boolean clip) {
        mClip = clip;
    }
}
