package com.tpw.homeshell;


import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import android.widget.TextView;

public class CustomHideseat extends FrameLayout {
    public CustomHideseat(Context context) {
        this(context, null);
    }

    public CustomHideseat(Context context, AttributeSet attr) {
        this(context, attr, -1);
    }
    
    public CustomHideseat(Context context, AttributeSet attr, int defStyle) {
        super(context, attr, defStyle);
    }
    
    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        Hideseat hideseat = (Hideseat) findViewById(R.id.hideseat);

        PageIndicatorView indicator = (PageIndicatorView) findViewById(R.id.hideseat_pageindicator);
        indicator.setNeedLine(false);
        hideseat.setPageIndicator(indicator);
        
        TextView hintView = (TextView) findViewById(R.id.cling_hint);
        hideseat.setHintView(hintView);

        if (AgedModeUtil.isAgedMode()) {
            getLayoutParams().height = getResources()
                    .getDimensionPixelSize(R.dimen.workspace_cell_height_3_3);
        }
    }

    // clips the contents below hide-seat during animation
    private int mVerticalClip; // 0: no clip;
                               // 1 ~ hideseat.height: clipped

    @Override
    public void draw(Canvas canvas) {
        if (mVerticalClip == 0) {
            super.draw(canvas);
        } else {
            canvas.save();
            canvas.clipRect(0, 0, getWidth(), getHeight() - mVerticalClip);
            super.draw(canvas);
            canvas.restore();
        }
    }

    public void setVerticalClip(int value) {
        if (mVerticalClip != value) {
            mVerticalClip = value;
            invalidate();
        }
    }

}
