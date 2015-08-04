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

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.widget.FrameLayout;

import com.tpw.homeshell.R;

/*
 * Ths bar will manage the transition between the QSB search bar and the delete drop
 * targets so that each of the individual IconDropTargets don't have to.
 */
public class SearchDropTargetBar extends FrameLayout implements DragController.DragListener {

    private static final int sTransitionInDuration = 200;
    private static final int sTransitionOutDuration = 175;
    
    private static final String TAG = "SearchDropTargetBar";

    private ObjectAnimator mDropTargetBarAnim;
    private static final AccelerateInterpolator sAccelerateInterpolator =
            new AccelerateInterpolator();

    private View mDropTargetBar;
    //private ButtonDropTarget mInfoDropTarget;
    private ButtonDropTarget mDeleteDropTarget;
    private int mBarHeight;
    private boolean mDeferOnDragEnd = false;

    private Drawable mPreviousBackground;
    private boolean mEnableDropDownDropTargets;
    
    private Object mDragInfo;
    private Launcher mLauncher;
    
    private enum AnimDirection {ANIM_DEFAULT, ANIM_UP, ANIM_DOWN};
    private AnimDirection mAnimDirection = AnimDirection.ANIM_DEFAULT;

    public SearchDropTargetBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SearchDropTargetBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setup(Launcher launcher, DragController dragController) {
        mLauncher = launcher;
        dragController.addDragListener(this);
        //dragController.addDragListener(mInfoDropTarget);
        dragController.addDragListener(mDeleteDropTarget);
        //dragController.addDropTarget(mInfoDropTarget);
        dragController.addDropTarget(mDeleteDropTarget);
        dragController.setFlingToDeleteDropTarget(mDeleteDropTarget);
        //mInfoDropTarget.setLauncher(launcher);
        mDeleteDropTarget.setLauncher(launcher);
    }

    private void prepareStartAnimation(View v) {
        // Enable the hw layers before the animation starts (will be disabled in the onAnimationEnd
        // callback below)
        v.setLayerType(View.LAYER_TYPE_HARDWARE, null);
    }

    private void setupAnimation(ObjectAnimator anim, final View v) {
        anim.setInterpolator(sAccelerateInterpolator);
        anim.setDuration(sTransitionInDuration);
        anim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                v.setLayerType(View.LAYER_TYPE_NONE, null);
                if(mAnimDirection == AnimDirection.ANIM_DOWN && !mLauncher.isDragToDelete()) {
                    exitFullScreen();
                    mAnimDirection = AnimDirection.ANIM_DEFAULT;
                }
                Log.d(TAG, "sxsexe------------>onAnimationEnd mAnimDirection " + mAnimDirection);
            }

            @Override
            public void onAnimationStart(Animator animation) {
                if(mAnimDirection == AnimDirection.ANIM_UP) {
                    enterFullScreen();
                }
                Log.d(TAG, "sxsexe------------>onAnimationStart mAnimDirection " + mAnimDirection);
            }
        });
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        // Get the individual components
        mDropTargetBar = findViewById(R.id.drag_target_bar);
        //mInfoDropTarget = (ButtonDropTarget) mDropTargetBar.findViewById(R.id.info_target_text);
        mDeleteDropTarget = (ButtonDropTarget) mDropTargetBar.findViewById(R.id.delete_target_text);
        mBarHeight = getResources().getDimensionPixelSize(R.dimen.qsb_bar_height);

        //mInfoDropTarget.setSearchDropTargetBar(this);
        mDeleteDropTarget.setSearchDropTargetBar(this);

        mEnableDropDownDropTargets =
            getResources().getBoolean(R.bool.config_useDropTargetDownTransition);

        // Create the various fade animations
        if (mEnableDropDownDropTargets) {
            mDropTargetBar.setTranslationY(-mBarHeight);
            mDropTargetBarAnim = LauncherAnimUtils.ofFloat(mDropTargetBar, "translationY",
                    -mBarHeight, 0f);
        } else {
            mDropTargetBar.setAlpha(0f);
            mDropTargetBarAnim = LauncherAnimUtils.ofFloat(mDropTargetBar, "alpha", 0f, 1f);
        }
        setupAnimation(mDropTargetBarAnim, mDropTargetBar);
    }

    public void finishAnimations() {
        prepareStartAnimation(mDropTargetBar);
        mDropTargetBarAnim.reverse();
        mAnimDirection = AnimDirection.ANIM_DOWN;
    }

    /*
     * Gets various transition durations.
     */
    public int getTransitionInDuration() {
        return sTransitionInDuration;
    }
    public int getTransitionOutDuration() {
        return sTransitionOutDuration;
    }

    /*
     * DragController.DragListener implementation
     */
    @Override
    public void onDragStart(DragSource source, Object info, int dragAction) {
        mDragInfo = info;
        if(mLauncher.isItemUnDeletable(info)) {
            return;
        }
        showDropTargetBar(true);
    }

    public void deferOnDragEnd() {
        mDeferOnDragEnd = true;
    }

    @Override
    public void onDragEnd() {
        if(mAnimDirection == AnimDirection.ANIM_DEFAULT) {
            return;
        }
        hideDropTargetBar(true);
    }

    public void showDropTargetBar(boolean anim) {
        if(mLauncher.isItemUnDeletable(mDragInfo)) {
            return;
        }
        
        if(anim) {
            Log.d(TAG, "sxsexe234------------>animateShowDropTargetBar mAnimDirection " + mAnimDirection);
            if(mAnimDirection == AnimDirection.ANIM_DOWN) {
                mDropTargetBarAnim.end();
            } else if (mAnimDirection == AnimDirection.ANIM_UP) {
                return;
            }
            //mTrashBarShowIn = true;
            prepareStartAnimation(mDropTargetBar);
            mAnimDirection = AnimDirection.ANIM_UP;
            mDropTargetBarAnim.start();
        } else {
            mDropTargetBar.setTranslationY(0);
            enterFullScreen();
        }
    }
    
    public void hideDropTargetBar(boolean anim) {
        if(anim) {
            Log.d(TAG, "sxsexe234------------>animateHideDropTargetBar mAnimDirection " + mAnimDirection
                    + " mDeferOnDragEnd " + mDeferOnDragEnd);
            if(mAnimDirection == AnimDirection.ANIM_UP) {
                mDropTargetBarAnim.end();
            } else if (mAnimDirection == AnimDirection.ANIM_DOWN || mAnimDirection == AnimDirection.ANIM_DEFAULT) {
                return;
            }
            
            if (!mDeferOnDragEnd) {
                // Restore the QSB search bar, and animate out the drop target bar
                mDropTargetBarAnim.cancel();
                prepareStartAnimation(mDropTargetBar);
                mAnimDirection = AnimDirection.ANIM_DOWN;
                mDropTargetBarAnim.reverse();
            } else {
                mDeferOnDragEnd = false;
            }
        } else {
            mDropTargetBar.setTranslationY(-mBarHeight);
            exitFullScreen();
        }
    }
    
    private void exitFullScreen() {
        mLauncher.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }
    
    private  void enterFullScreen() {
        mLauncher.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }
}
