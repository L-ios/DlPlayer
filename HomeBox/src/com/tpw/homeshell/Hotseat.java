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
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Point;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;

import com.tpw.homeshell.CellLayout.Mode;
import com.tpw.homeshell.DropTarget.DragObject;
import com.tpw.homeshell.LauncherSettings.Favorites;
import com.tpw.homeshell.icon.BubbleTextView;
import com.tpw.homeshell.model.LauncherModel;

import java.util.ArrayList;

public class Hotseat extends FrameLayout {
    private static final String TAG = "Hotseat";

    private Launcher mLauncher;
    private CellLayout mContent;

    private int mCellCountX;
    private int mCellCountY;
    private int mAllAppsButtonRank;
    private int mWGap;

    private boolean mTransposeLayoutWithOrientation;
    private boolean mIsLandscape;
    
    private AnimatorSet mAnimatorSet;
    
    private BubbleTextView mInvisibleView;
    private boolean mInvisibleViewAdded = false;
    private int mCurrentInvisibleIndex = -1;
    private int mAnimStartY, mAnimEndY;
    
    private boolean mTouchInHotseat;
    
    private enum HotseatDragState {NONE, DRAG_IN, DRAG_OUT};
    private HotseatDragState mDragState = HotseatDragState.NONE;
    private View mDragedItemView;
//    private boolean mAnimBackRunning = false;
    private boolean mAnimEnterRunning = false;
    private boolean mAnimLeftRunning = false;
    
    
//    private List<SwapItemInfo> listAnimators;
//    private SwapThread mSwapThread;
//    private MyHandler myHandler;
//    private boolean mSwapThreadRunning = false;
    private int mLastTouchX = -1;
    private int mMoveDireciton = 0;//0:left; 1:right
    private int mXOffset;
    
    private ArrayList<View> mViewCacheList = null;

    public Hotseat(Context context) {
        this(context, null);
    }

    public Hotseat(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public Hotseat(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        TypedArray a = context.obtainStyledAttributes(attrs,
                R.styleable.Hotseat, defStyle, 0);
        Resources r = context.getResources();
        
//        mCellCountX = a.getInt(R.styleable.Hotseat_cellCountX, -1);
//        mCellCountY = a.getInt(R.styleable.Hotseat_cellCountY, -1);
        mCellCountX = ConfigManager.getHotseatMaxCountX();
        mCellCountY = ConfigManager.getHotseatMaxCountY();
        mViewCacheList = new ArrayList<View>(mCellCountX);

        mAllAppsButtonRank = r.getInteger(R.integer.hotseat_all_apps_index);
        mTransposeLayoutWithOrientation = 
                r.getBoolean(R.bool.hotseat_transpose_layout_with_orientation);
        mIsLandscape = context.getResources().getConfiguration().orientation ==
            Configuration.ORIENTATION_LANDSCAPE;
        a.recycle();
        
    }

    public void setup(Launcher launcher) {
        mLauncher = launcher;
        setOnKeyListener(new HotseatIconKeyEventListener());
    }

    public CellLayout getLayout() {
        return mContent;
    }
  
    private boolean hasVerticalHotseat() {
        return (mIsLandscape && mTransposeLayoutWithOrientation);
    }

    /* Get the orientation invariant order of the item in the hotseat for persistence. */
    public int getOrderInHotseat(int x, int y) {
        return hasVerticalHotseat() ? (mContent.getCountY() - y - 1) : x;
    }
    /* Get the orientation specific coordinates given an invariant order in the hotseat. */
    int getCellXFromOrder(int rank) {
        return hasVerticalHotseat() ? 0 : rank;
    }
    int getCellYFromOrder(int rank) {
        return hasVerticalHotseat() ? (mContent.getCountY() - (rank + 1)) : 0;
    }
    public boolean isAllAppsButtonRank(int rank) {
        return /*rank == mAllAppsButtonRank;*/false;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        if (mCellCountX < 0) mCellCountX = LauncherModel.getCellCountX();
        if (mCellCountY < 0) mCellCountY = LauncherModel.getCellCountY();
        mContent = (CellLayout) findViewById(R.id.layout_hotseat);
        mContent.setGridSize(mCellCountX, mCellCountY);
        // the icons display in hotseat and workspace is same when aged mode
        if (!AgedModeUtil.isAgedMode()) {
            mContent.setMode(Mode.HOTSEAT);
        }
        //generateInvisibleView();
        if (AgedModeUtil.isAgedMode()) {
            getLayoutParams().height = getContext().getResources()
                    .getDimensionPixelSize(R.dimen.workspace_cell_height_3_3);
        }
        resetLayout();
    }
    
    private void generateInvisibleView() {
        Context context = getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        mInvisibleView = (BubbleTextView)
                inflater.inflate(R.layout.application, mContent, false);
        mInvisibleView.setAlpha(0);
        /*mInvisibleView.setCompoundDrawablesWithIntrinsicBounds(null,
                context.getResources().getDrawable(R.drawable.widget_preview_tile), null, null);
        mInvisibleView.setContentDescription(context.getString(R.string.all_apps_button_label));*/
        int x = 0;
        int y = getCellYFromOrder(mAllAppsButtonRank);
        CellLayout.LayoutParams lp = new CellLayout.LayoutParams(x,y,1,1);
        lp.canReorder = false;
        mInvisibleView.setLayoutParams(lp);
    }
    
    void resetLayout() {
        mContent.removeAllViewsInLayout();
    }
    
    /**
     * whle drag item into hotseat
     * @param touchX
     * @param screen
     * @param fromHotset
     * @param d
     */
    protected void onEnterHotseat(int touchX, final int screen, boolean fromHotset, final DragObject d) {
        mTouchInHotseat = true;
        final ShortcutAndWidgetContainer container = mContent.getShortcutAndWidgetContainer();
        int count = container.getChildCount();
        if(count < 0) {
            return;
        }
        if(!fromHotset && isFull()) {
            return;
        }
        if(fromHotset) {
            
            //Log.d(TAG, "sxsexe55---->11onEnterHotseat mMoveDireciton " + mMoveDireciton + " touchX " + touchX ); 
            if(Math.abs(mLastTouchX - touchX) >= 5) {
                mMoveDireciton = mLastTouchX < touchX ? 1 : 0;
                mLastTouchX = touchX;
            }
            //Log.d(TAG, "sxsexe55---->22onEnterHotseat mDragState " + mDragState + " mDragedItemView " + mDragedItemView);
            
            if((mDragState == HotseatDragState.DRAG_OUT && mDragedItemView != null)
                    || (mDragedItemView != null && mDragedItemView.getParent() == null && mDragState != HotseatDragState.DRAG_IN)) {
                //drag one item belongs to hotseat from workspace back to hotseat
//                mDragState = HotseatDragState.DRAG_SWAP;
                animateBackToHotseat(count, touchX, d.y, container);
            } else {
                //drag one item from hotseat to hotseat
                mDragState = HotseatDragState.DRAG_IN;
                animateSwap(touchX, d.y, screen, fromHotset, d);
            }
        } else {
            checkAnimateOnEnter(count, touchX, d.y, container);
        }
    }
    
    private void checkAnimateOnEnter(int childCount, int touchX, int touchY, ShortcutAndWidgetContainer container) {
        if(mAnimEnterRunning) {
            return;
        }
        //drag one item from workspace or folder
        int index = getAppropriateIndex(touchX);
//        Log.d(TAG, "sxsexe55>>>>>>>>>>>> +++++ checkAnimateOnEnter index " + index + " mCurrentInvisibleIndex " + mCurrentInvisibleIndex);
        if(mCurrentInvisibleIndex != index || mDragState == HotseatDragState.NONE) {
            mInvisibleViewAdded = false;
            Log.d(TAG, "sxsex55>>>>>>>>>>>>  checkAnimateOnEnter removeView(mInvisibleView) ");
            //container.removeView(mInvisibleView);
            //mViewCacheList.remove(mInvisibleView);
            removeViewFromCacheList(mInvisibleView);
            mInvisibleView = null;
        }
        //childCount = container.getChildCount();
        childCount = mViewCacheList.size();
        
        if(!mInvisibleViewAdded) {
            /*Log.d(TAG, "sxsexe55>>>>>>>>>>>>  checkAnimateOnEnter11 mInvisibleViewAdded " + mInvisibleViewAdded 
                    + " index " + index + " childCount " + childCount + " cacheSize " + mViewCacheList.size()
                    + " mCurrentInvisibleIndex " + mCurrentInvisibleIndex
                    + " mDragState " + mDragState);*/
            if(mInvisibleView == null) {
                generateInvisibleView();
            }
            if(index == 0 && childCount <= 1) {
                if(index < childCount) {
                    //container.addView(mInvisibleView, index);
                   addToCacheList(index, mInvisibleView);
                    //addViewWithoutInvalidate(mInvisibleView, index);
                } else {
                    //container.addView(mInvisibleView);
                    addToCacheList(-1, mInvisibleView);
                    //addViewWithoutInvalidate(mInvisibleView, -1);
                }
                mCurrentInvisibleIndex = 0;
            } else if(index == (childCount-1)) {
                //View view = container.getChildAt(index);
                View view = mViewCacheList.get(index);
                CellLayout.LayoutParams lp = (com.tpw.homeshell.CellLayout.LayoutParams) view.getLayoutParams();
                int correctedX = getCorrectedX(lp.x, mMoveDireciton == 0);
//                Log.d(TAG, "sxsexe55>>>>>>>>>>>> lp.x " + lp.x + " correctedX " + correctedX 
//                        + " touchX " + touchX + " touchY " + touchY);
                if(touchX < correctedX) {
                    //container.addView(mInvisibleView, index);
                    addToCacheList(index, mInvisibleView);
                    mCurrentInvisibleIndex = index;
                } else {
                    if(mMoveDireciton == 0) {
//                        container.addView(mInvisibleView, index);
                        addToCacheList(index, mInvisibleView);
                        mCurrentInvisibleIndex = index;
                    } else {
                        addToCacheList(-1, mInvisibleView);
                        //container.addView(mInvisibleView);
                        mCurrentInvisibleIndex = index + 1;
                    }
                }
            } else {
                //container.addView(mInvisibleView, index);
                dumpViewCacheList();
                addToCacheList(index, mInvisibleView);
                mCurrentInvisibleIndex = index;
            }
//            Log.d(TAG, "sxsexe55>>>>>>>>>>>>  checkAnimateOnEnter22 addView(mInvisibleView) mCurrentInvisibleIndex " + mCurrentInvisibleIndex);
            //mInvisibleView.setVisibility(View.INVISIBLE);
            mInvisibleViewAdded = true;
            //reLayout();
            mDragState = HotseatDragState.DRAG_IN;
            animateOnEnter(false);
        }
    }
    
    private int getCorrectedX (int leftX, boolean leftOrRight) {
        int cellW = mContent.getCellWidth();
        int centerX = leftX + cellW / 2;
        return leftOrRight ? centerX + mXOffset : centerX - mXOffset;
    }
    
    private void animateBackToHotseat(int childCount, int touchX, int touchY, ShortcutAndWidgetContainer container) {
//        Log.d(TAG, "sxsexe55---->++animateBackToHotseat");
        mDragState = HotseatDragState.DRAG_IN;
        checkAnimateOnEnter(childCount, touchX, touchY, container);
    }
    
    private void animateSwap(int touchX, int touchY, final int screen, boolean fromHotset, final DragObject d) {
        final ShortcutAndWidgetContainer container = mContent.getShortcutAndWidgetContainer();
//        Log.d(TAG, "sxsexe55------>animateSwap  enter mDragedItemView " + mDragedItemView);
        //final int index = getSwapIndex(touchX, touchY);
        if(mDragedItemView == null) {
            mDragedItemView = mLauncher.getWorkspace().getDragInfo().cell;
            //container.removeView(mDragedItemView);
            //replace by mInvisibleView
            mViewCacheList.remove(mDragedItemView);
            if(mInvisibleView == null) {
                generateInvisibleView();
            }
            if(screen < mViewCacheList.size()) {
                mCurrentInvisibleIndex = screen;
                addToCacheList(mCurrentInvisibleIndex, mInvisibleView);
            } else {
                addToCacheList(-1, mInvisibleView);
                mCurrentInvisibleIndex = mViewCacheList.size() - 1;
            }
            fillViewsFromCache();
            mInvisibleViewAdded = true;
//            Log.d(TAG, "sxsexe55------>animateSwap  create mDragedItemView " + mDragedItemView.getTag()
//                    + " mCurrentInvisibleIndex " + mCurrentInvisibleIndex);
            dumpViewCacheList();
        }
        
        checkAnimateOnEnter(container.getChildCount(), touchX, touchY, container);
   }
    
    private void animateOnEnter(boolean fromHotseat) {
        //ShortcutAndWidgetContainer container = mContent.getShortcutAndWidgetContainer();
//        int count = container.getChildCount();
        int count = mViewCacheList.size();
        if(mAnimLeftRunning && mAnimatorSet != null) {
            mAnimatorSet.end();
        }
        
        if(fromHotseat) {
            
        } else {
            int right = getRight();
            int left = getLeft();
            
            Resources res = getResources();
            int width = right - left;
            width = width - getPaddingLeft() - getPaddingRight()
                    - mContent.getPaddingLeft() - mContent.getPaddingRight();

            int cellW = mContent.getCellWidth();
            int l = 0;
            int wGap = 0;
            int space = width - count * cellW;
            int workspaceCountX = res.getInteger(
                    R.integer.cell_count_x);
            if (count >= workspaceCountX) {
                wGap = (int) (space / (float) (count - 1));
            } else {
                wGap = (int) (space / (float) (count + 1));
                l = wGap;
            }

            if(mAnimatorSet == null) {
                mAnimatorSet = new AnimatorSet();
            } else {
                mAnimatorSet.removeAllListeners();
                if(mAnimatorSet.isRunning()) {
                    mAnimatorSet.end();
                }
                clearAnimFlags();
            }
            
            ArrayList<Animator> items = new ArrayList<Animator>();
            int srcX = 0;
            int destX = 0;
//            Log.d(TAG, "sxsexe55---->animateOnEnter count"  + count);
            for (int i = 0; i < count; i++) {
                //View v = container.getChildAt(i);
                View v = mViewCacheList.get(i);
                if(v == null) {
                    continue;
                }
                CellLayout.LayoutParams lp = (CellLayout.LayoutParams) v.getLayoutParams();
                srcX = lp.x;
                destX = l + lp.leftMargin;
                l += (cellW + wGap);
//                Log.d(TAG, "sxsexe55---->animateOnEnter v == mInvisibleView "  + (v == mInvisibleView) 
//                        + " srcX " + srcX + " destX " + destX);
                if(v == mInvisibleView || srcX == destX) {
                    continue;
                }
                ItemInfo info = (ItemInfo) v.getTag();
//                Log.d(TAG, "sxsexe55---->animateOnEnter i " + i + " srcX " + srcX + " destX " + destX + " info " + (info == null ? "info" : info.title));
                items.add(createAnimator(v, srcX, destX, null, null, true));
            }
            if(!items.isEmpty()) {
                mAnimatorSet.playTogether(items);
                
                mAnimatorSet.addListener(new AnimatorListenerAdapter() {
                    
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        mAnimatorSet = null;
                        mAnimEnterRunning = false;
//                        Log.d(TAG, "sxsexe55---->animateOnEnter end ");
                        fillViewsFromCache();
                        dumpViewCacheList();
                    }
                    
                    @Override
                    public void onAnimationStart(Animator animation) {
//                        Log.d(TAG, "sxsexe55---->animateOnEnter start ");
                        mAnimEnterRunning = true;
                    }
                });
                mAnimatorSet.start();
            }
        }
    }
    
    private void clearAnimFlags() {
        mAnimEnterRunning = false;
        mAnimLeftRunning = false;
    }
    
    private void animateRestorePostion(final Runnable r) {

        ShortcutAndWidgetContainer container = mContent.getShortcutAndWidgetContainer();
        int count = container.getChildCount();
        ArrayList<View> visibleChild = new ArrayList<View>();
        for (int i = 0; i < count; i++) {
            final View child = container.getChildAt(i);
            if (child != mInvisibleView) {
                visibleChild.add(child);
            }
        }

        int right = getRight();
        int left = getLeft();
        int visibleCount = visibleChild.size();
        if (visibleCount > 0) {
            Resources res = getResources();
            int width = right - left;
            width = width - getPaddingLeft() - getPaddingRight()
                    - mContent.getPaddingLeft() - mContent.getPaddingRight();

            int cellW = mContent.getCellWidth();
            int l = 0;
            int wGap = 0;
            int space = width - visibleCount * cellW;
            int workspaceCountX = res.getInteger(
                    R.integer.cell_count_x);
            if (visibleCount >= workspaceCountX) {
                wGap = (int) (space / (float) (visibleCount - 1));
            } else {
                wGap = (int) (space / (float) (visibleCount + 1));
                l = wGap;
            }

            if(mAnimatorSet == null) {
                mAnimatorSet = new AnimatorSet();
            } else {
                mAnimatorSet.removeAllListeners();
                if(mAnimatorSet.isRunning()) {
                    mAnimatorSet.end();
                }
                clearAnimFlags();
            }
            ArrayList<Animator> items = new ArrayList<Animator>();
            int srcX = 0;
            int destX = 0;
            for (int i = 0; i < visibleCount; i++) {
                View v = visibleChild.get(i);
                CellLayout.LayoutParams lp = (CellLayout.LayoutParams) v
                        .getLayoutParams();
                srcX = (int)v.getX();
                destX = l + lp.leftMargin;
//                Log.d(TAG, "sxsexe55---->animateRestorePostion i " + i + " srcX " + srcX + " destX " + destX);
                items.add(createAnimator(v, srcX, destX, null, null, true));
                l += (cellW + wGap);
            }
            mAnimatorSet.playTogether(items);
            mAnimatorSet.addListener(new AnimatorListenerAdapter() {

                @Override
                public void onAnimationEnd(Animator animation) {
                    r.run();
                    mAnimatorSet = null;
//                    Log.d(TAG, "sxsexe55---------->animateRestorePostion end mInvisibleViewAdded " + mInvisibleViewAdded
//                            + " mDragState " + mDragState);
                }

                @Override
                public void onAnimationStart(Animator animation) {
//                    Log.d(TAG, "sxsexe55---------->animateRestorePostion start ");
                }
            });
            mAnimatorSet.start();
        }
    }
    
    private void animateLeftItems() {
//        Log.d(TAG, "sxsexe55---->animateLeftItems enter ");
        
        final ShortcutAndWidgetContainer container = mContent.getShortcutAndWidgetContainer();
        int count = container.getChildCount();
        ArrayList<View> leftChild = new ArrayList<View>();
        for (int i = 0; i < count; i++) {
            final View child = container.getChildAt(i);
//            Log.d(TAG, "sxsexe55---->animateLeftItems i " + i 
//                    + " child == mDragedItemView " + (child == mDragedItemView)
//                    + " child == mInvisibleView " + (child == mInvisibleView));
            if (child != mDragedItemView && child != mInvisibleView) {
                leftChild.add(child);
            }
        }

        int right = getRight();
        int left = getLeft();
        int leftCount = leftChild.size();
        if (leftCount > 0) {
            Resources res = getResources();
            int width = right - left;
            width = width - getPaddingLeft() - getPaddingRight()
                    - mContent.getPaddingLeft() - mContent.getPaddingRight();

            int cellW = mContent.getCellWidth();
            int l = 0;
            int wGap = 0;
            int space = width - leftCount * cellW;
            int workspaceCountX = res.getInteger(
                    R.integer.cell_count_x);
            if (leftCount >= workspaceCountX) {
                wGap = (int) (space / (float) (leftCount - 1));
            } else {
                wGap = (int) (space / (float) (leftCount + 1));
                l = wGap;
            }

            if(mAnimatorSet == null) {
                mAnimatorSet = new AnimatorSet();
            } else {
                mAnimatorSet.removeAllListeners();
                if(mAnimatorSet.isRunning()) {
                    mAnimatorSet.end();
                }
                clearAnimFlags();
            }
            
            ArrayList<Animator> items = new ArrayList<Animator>();
            int srcX = 0;
            for (int i = 0; i < leftCount; i++) {
                final View v = leftChild.get(i);
                CellLayout.LayoutParams lp = (CellLayout.LayoutParams) v
                        .getLayoutParams();
                srcX = (int)v.getX();
                final int destX = l + lp.leftMargin;
//                Log.d(TAG, "sxsexe55---->animateLeftItems i " + i + " srcX " + srcX + " destX " + destX);
                Runnable r = new Runnable() {
                    @Override
                    public void run() {
                        v.setX(destX);
                    }
                };
                l += (cellW + wGap);
                if(srcX == destX) {
                    continue;
                }
                items.add(createAnimator(v, srcX, destX, null, r, true));
            }
            mAnimatorSet.playTogether(items);
            mAnimatorSet.addListener(new AnimatorListenerAdapter() {
                
                @Override
                public void onAnimationEnd(Animator animation) {
                    if(mAnimLeftRunning) {
                        container.removeView(mDragedItemView);
                        mViewCacheList.remove(mDragedItemView);
                        if(mInvisibleViewAdded && mDragState == HotseatDragState.DRAG_OUT) {
                            container.removeView(mInvisibleView);
                            mViewCacheList.remove(mInvisibleView);
                            mInvisibleView = null;
                            mInvisibleViewAdded = false;
                            mCurrentInvisibleIndex = -1;
                        }
                    }
//                    Log.d(TAG, "sxsexe55---->animateLeftItems end");
                    mAnimatorSet = null;
                    mAnimLeftRunning = false;
                    dumpViewCacheList();
                    if(container.getChildCount() <= mViewCacheList.size()) {
                        fillViewsFromCache();
                    }
                }
                
                @Override
                public void onAnimationStart(Animator animation) {
                    mAnimLeftRunning = true;
//                    Log.d(TAG, "sxsexe55---->animateLeftItems start");
                }
                
                @Override
                public void onAnimationCancel(Animator animation) {
                    mAnimLeftRunning = false;
                }
            });
            mAnimatorSet.start();
        } 
    }
    
    /**
     * drag exit hotseat
     */
    protected void onExitHotseat(boolean fromHotseat) {
        
        //Log.d(TAG, "sxsexe22------------>onExitHotseat mDragState " + mDragState); 
        if(mDragState == HotseatDragState.NONE || mDragState == HotseatDragState.DRAG_OUT) {
            return;
        }
        
//        Log.d(TAG, "sxsexe55------------>onExitHotseat fromHotseat " + fromHotseat 
//                + " mDragedItemView " + mDragedItemView + " parent " + (mDragedItemView==null ? " null " : mDragedItemView.getParent()));
        mTouchInHotseat = false;
        mDragState = HotseatDragState.DRAG_OUT;
        
        if(fromHotseat && mDragedItemView != null) {
            //drag one item belongs to hotseat to workspace
            animateLeftItems();
            return;
        }
        
        if(!fromHotseat && mInvisibleViewAdded) {
            Runnable r = new Runnable() {
                
                @Override
                public void run() {
                    //mContent.removeView(mInvisibleView);
                    mViewCacheList.remove(mInvisibleView);
                    mInvisibleView = null;
                    mInvisibleViewAdded = false;
                    mCurrentInvisibleIndex = -1;
                    fillViewsFromCache();
                }
            };
            animateRestorePostion(r);
        }
    }
    
    private void cleanAndReset() {
//        Log.d(TAG, "sxsexe55----------->cleanAndReset ");
        mDragState = HotseatDragState.NONE;
        mInvisibleView = null;
        mInvisibleViewAdded = false;
        mCurrentInvisibleIndex = -1;
        mDragedItemView = null;
        //mAnimBackRunning = false;
        mAnimEnterRunning = false;
        mAnimLeftRunning = false;
        mAnimatorSet = null; 
        mLastTouchX = -1;
        
        updateItemCell();
        reLayout();
        updateItemInDatabase(); 
    }
    
    /**
     * drop dragItem in hotseat
     * @param success
     * @return
     */
    protected int onDrop(boolean success, int touchX, final DragView dragView, final View cell, 
            final boolean removeDragView) {
/*        Log.d(TAG, "sxsexe55---->onDrop mDragedItemView " + mDragedItemView 
                + " parent " + (mDragedItemView == null ? "null" : mDragedItemView.getParent())
                + " mDragedItemView.tag " + (mDragedItemView == null ? "null" : mDragedItemView.getTag())
                + " success " + success + " mInvisibleViewAdded " + mInvisibleViewAdded 
                + " mCurrentInvisibleIndex " + mCurrentInvisibleIndex
                + " mDragState " + mDragState);*/
        
        if(mAnimLeftRunning) {
            if (mAnimatorSet != null){
                mAnimatorSet.cancel();
            }
        } else {
            if(mAnimatorSet != null && mAnimatorSet.isRunning()) {
                mAnimatorSet.end();
            }
        }
        
        
        int index = mCurrentInvisibleIndex;
        final Runnable onDropEndRunnable = new Runnable() {
            @Override
            public void run() {
/*                Log.d(TAG, "sxsexe55---->onDropEndRunnable cell " + cell 
                        + " dragView " + dragView
                        + " cell.parent " + (cell == null ? " null " : cell.getParent())); */
                if(cell != null && cell.getVisibility() != View.VISIBLE)
                    cell.setVisibility(VISIBLE);
                mLauncher.getWorkspace().checkAndRemoveEmptyCell();
                mLauncher.getHideseat().removeEmptyScreen();
                if(mInvisibleViewAdded) {
                    //getContainer().removeView(mInvisibleView);
                    mViewCacheList.remove(mInvisibleView);
                    mInvisibleView = null;
                }
                cleanAndReset();
                if(dragView != null)
                    mLauncher.getDragController().onDeferredEndDrag(dragView);
            }
        };
        
        if(!success || dragView == null) {
            onDropEndRunnable.run();
            return index;
        }
        Animator a = null;
        int srcX = (int) dragView.getX();
        int srcY = (int) dragView.getY();
        
        if(mDragState == HotseatDragState.DRAG_IN && mDragedItemView != null && mDragedItemView.getParent() ==null) {
            //drag one item belongs to hotseat to hotseat again
            //Log.d(TAG, "sxsexe22---->onDrop2222 ");
            if(mInvisibleViewAdded && mCurrentInvisibleIndex != -1) {
                CellLayout.LayoutParams lp = (CellLayout.LayoutParams)mInvisibleView.getLayoutParams();
                int destX = lp.x;
                int destY = getLocationY();
                final Runnable r = new Runnable() {
                    
                    @Override
                    public void run() {
                        ShortcutAndWidgetContainer container = mContent.getShortcutAndWidgetContainer();
                        container.removeView(mInvisibleView);
                        mViewCacheList.remove(mInvisibleView);
//                        Log.d(TAG, "sxsexe55----->onDrop runnable cell.parent " + cell.getParent());
                        if(cell != null && cell.getParent() != null) {
                            ViewGroup parent = (ViewGroup)cell.getParent();
                            parent.removeView(cell);
                        }
                        int count = container.getChildCount();
                        if(mCurrentInvisibleIndex >= count) {
                            container.addView(cell);
                            mViewCacheList.add(cell);
                        } else {
                            container.addView(cell, mCurrentInvisibleIndex);
                            mViewCacheList.add(mCurrentInvisibleIndex, cell);
                        }
                        dumpViewCacheList();
                        onDropEndRunnable.run();
                    }
                };
//                Log.d(TAG, "sxsexe55----->onDrop end drag in from (" + srcX + " " + srcY + ") to (" 
//                        + destX + " " + destY);
                a = createDropAnimator(dragView, srcX, srcY, destX, destY, r);
                a.start();
            } else {
                onDropEndRunnable.run();
            }
            
            return index;
        } else {
            if(success) {
                if(mInvisibleViewAdded && mCurrentInvisibleIndex != -1) {
                    mContent.getShortcutAndWidgetContainer().removeView(mInvisibleView);
                    mViewCacheList.remove(mInvisibleView);
                    CellLayout.LayoutParams lp = (CellLayout.LayoutParams) mInvisibleView.getLayoutParams();
/*                    Log.d(TAG, "sxsexe55----->onDrop end drag in"
                            + " childCount " + mViewCacheList.size() + " mInvisibleView " + mInvisibleView
                            + " lp.x " + lp.x  + " lp.y " + lp.y);*/
                    a = createDropAnimator(dragView, srcX, srcY, lp.x, getLocationY(), onDropEndRunnable);
                } else {
                    onDropEndRunnable.run();
                }
                if(a != null) {
                    a.start();
                }
            }
        }
        
        return index;
    }

    private Animator createAnimator(final View v, final int srcX, final int destX, 
            final Runnable onStartRunnable,final Runnable onEndRunnable, final boolean cleanTransX) {
        ObjectAnimator a = ObjectAnimator.ofPropertyValuesHolder(v,
                PropertyValuesHolder.ofFloat(ViewHidePropertyName.X, srcX, destX));
        a.setDuration(200);
        a.setInterpolator(new LinearInterpolator());
        a.addListener(new AnimatorListenerAdapter(){

            @Override
            public void onAnimationEnd(Animator animation) {
                //Log.d(TAG, "sxsexe22------> onAnimationEnd tranX " + v.getTranslationX() + " x " + v.getX());
                if(onEndRunnable != null) {
                    onEndRunnable.run();
                }
                v.setTranslationX(0);
            }
        });
        return a;
    }
    
    private Animator createDropAnimator(View v, int srcX, int srcY, int destX, int destY, final Runnable onDropEndRunnable) {
        ObjectAnimator a = ObjectAnimator.ofPropertyValuesHolder(v,
                PropertyValuesHolder.ofFloat(ViewHidePropertyName.X, srcX, destX),
                PropertyValuesHolder.ofFloat(ViewHidePropertyName.Y, srcY, destY));
/*        Log.d(TAG, "sxsexe55----->createDropAnimator srcX " + srcX + " destX " + destX
                + " srcY " + srcY + " destY " + destY);*/
        a.setDuration(150);
        a.setInterpolator(new LinearInterpolator());
        a.addListener(new AnimatorListenerAdapter(){

            
            @Override
            public void onAnimationStart(Animator animation) {
//                Log.d(TAG, "sxsexe55------------>DropAnimator start");
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if(onDropEndRunnable != null) {
                    onDropEndRunnable.run();
                }
//                Log.d(TAG, "sxsexe55------------>DropAnimator end");
            }
        });
        return a;
    }
    
    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        LayoutParams param = (LayoutParams) this.getLayoutParams();
        if (AgedModeUtil.isAgedMode()) {
            param.height = mLauncher.getResources().getDimensionPixelSize(
                    R.dimen.workspace_cell_height_3_3);
        } else {
            param.height = mLauncher.getResources().getDimensionPixelSize(
                    R.dimen.button_bar_height_plus_padding);
        }
        reLayout(left, right, false);
        mAnimStartY = getTop();
        mAnimEndY = getBottom();
        super.onLayout(changed, left, top, right, bottom);
    }

    private void reLayout() {
        reLayout(getLeft(), getRight(), false);
    }
    private void reLayout(int left, int right, boolean unvisibleCount) {
        ShortcutAndWidgetContainer container = mContent.getShortcutAndWidgetContainer();
        int count = container.getChildCount();
        ArrayList<View> visibleChild = new ArrayList<View>();
        boolean flag = false;
        for (int i = 0; i < count; i++) {
            final View child = container.getChildAt(i);
            //Log.d(TAG, "sxsexe relayout dragging " + mLauncher.getDragController().isDragging() + "  child == mInvisibleView " + (child == mInvisibleView) );
	    if (!mLauncher.getDragController().isDragging()
		    && child == mInvisibleView) {
		flag = true;
		continue;
	    }
            if (!unvisibleCount && child.getVisibility() != GONE) {
                visibleChild.add(child);
            }
        }
        if(flag) {
            updateItemCell();
        }

        int visibleCount = visibleChild.size();
        if (visibleCount > 0) {
            Resources res = getResources();
            int width = right - left;
            width = width - getPaddingLeft() - getPaddingRight()
                    - mContent.getPaddingLeft() - mContent.getPaddingRight();


            int cellW = mContent.getCellWidth();
            mXOffset = 0;
            int l = 0;
            //int wGap = 0;
            int space = width - visibleCount * cellW;

            int workspaceCountX = ConfigManager.getCellCountX();
            if (visibleCount >= workspaceCountX) {
                mWGap = (int) (space / (float) (visibleCount - 1));
            } else {
                mWGap = (int) (space / (float) (visibleCount + 1));
                l = mWGap;
            }

            mContent.getShortcutAndWidgetContainer().setCellDimensions(cellW, mContent.getCellHeight(), mWGap, 0, mCellCountX);

            boolean rtl = mContent.getShortcutAndWidgetContainer().isLayoutRtl();
            boolean textViewNeedPadding = false;
            if (visibleCount > workspaceCountX && mWGap < 0) {
                textViewNeedPadding = true;
            }
            for (int i = 0; i < visibleCount; i++) {
                View v = visibleChild.get(!rtl ? i : visibleCount - i - 1);
                //!!important, in some case, the view's TranslationX is not 0 on animation ended 
                v.setTranslationX(0);
                v.setTop(0);
                v.setBottom(mContent.getCellHeight());
                BubbleTextView btv;
                if (textViewNeedPadding) {
                    Context context = getContext();
                    int paddingLeftAndRight = (-mWGap + (int) context.getResources().getDimension(
                            R.dimen.textview_padding_in_hotseat)) / 2;
                    if (v instanceof BubbleTextView) {
                        btv = (BubbleTextView) v;
                        btv.setTempPadding(paddingLeftAndRight);
                    } else if (v instanceof FolderIcon) {
                        FolderIcon fi = (FolderIcon) v;
                        fi.setTempPadding(paddingLeftAndRight);
                    }
                } else {
                    if (v instanceof BubbleTextView) {
                        btv = (BubbleTextView) v;
                        btv.resetTempPadding();
                    } else if (v instanceof FolderIcon) {
                        ((FolderIcon) v).resetTempPadding();
                    }
                }
                CellLayout.LayoutParams lp = (CellLayout.LayoutParams) v
                        .getLayoutParams();
                lp.x = l + lp.leftMargin;
                //ItemInfo info = (ItemInfo)v.getTag();
                //Log.d(TAG, "sxsexe33---->reLayout i : " + i + " lp.x " + lp.x + " cellW " + cellW + " info " + info);
                l += (cellW + mWGap);
            }
        }
        
        if(count > 0) {
            mContent.requestLayout();
        }
    }
    
    private int getSwapIndex(int touchX, int touchY) {
        int dockChildCount = mContent.getShortcutAndWidgetContainer().getChildCount();
        int index = -1;
        Log.d(TAG, "sxsexe33---> getSwapIndex bengin dockChildCount " + dockChildCount + " touchX " + touchX + " touchY " + touchY);
        if(dockChildCount <= 1) {
            return -1;
        }
        View child = null;
        int location[] = new int[2];
        int cellW = mContent.getCellWidth();
        int cellH = mContent.getCellHeight();
        Point p1 = new Point();
        Point p2 = new Point();
        Point p3 = new Point();
        for(int i = 0; i < dockChildCount; i++) {
            child = mContent.getShortcutAndWidgetContainer().getChildAt(i);
            child.getLocationInWindow(location);
            CellLayout.LayoutParams lp = (CellLayout.LayoutParams) child.getLayoutParams();
            location[0] = lp.x;
            p1.x = location[0] + cellW / 2;
            p1.y = location[1];
            p2.x = location[0];
            p2.y = location[1] + cellH;
            p3.x = location[0] + cellW;
            p3.y = location[1] + cellH;
            
//            Log.d(TAG, "sxsexe22---->p1 " + p1 + " p2 " + p2 + " p3 " + p3 + " lp.x " + lp.x 
//                    + " touchX " + touchX + " touchY " + touchY + " i " + i);
            Log.d(TAG, "sxsexe33---->getSwapIndex goto checkInSwapHotArea i " + i);
            if(checkInSwapHotArea(i, p1, p2, p3, touchX, touchY)) {
                Log.d(TAG, "sxsexe22------------------->getSwapIndex goto checkInSwapHotArea return " + i);
                return i;
            }
        }
        Log.d(TAG, "sxsexe22---->getSwapIndex end return " + index);
        return index;
    }
    
    private double triangleArea(Point a, Point b, Point c) {
        double result = Math.abs((a.x * b.y + b.x * c.y + c.x * a.y - b.x * a.y
                - c.x * b.y - a.x * c.y) / 2.0D);
        return result;
    }
    
    private boolean checkInSwapHotArea(int i, Point p1, Point p2, Point p3, int touchX, int touchY) {
        double areaBig = triangleArea(p1, p2, p3);
        Point touchPoint = new Point(touchX, touchY);
        double area1 = triangleArea(p1, p2, touchPoint);
        double area2 = triangleArea(p1, p3, touchPoint);
        double area3 = triangleArea(p2, p3, touchPoint);
        Log.d(TAG, "sxsexe33--->checkInSwapHotArea areaBig " + areaBig  + " other three " + (area1 + area2 + area3) + " i " + i);
        return areaBig == area1 + area2 + area3;
    }
    
    public int getAppropriateIndex(int dx) {
        //int dockChildCount = mContent.getShortcutAndWidgetContainer().getChildCount();
        int dockChildCount = mViewCacheList.size();
        int index = 0;

        if (dockChildCount == 0) {
            return index;
        }
        int cellWidth = mContent.getCellWidth();
        
        if(dockChildCount == 1) {
            //View v = mContent.getShortcutAndWidgetContainer().getChildAt(0);
            View v = mViewCacheList.get(0);
            if(v == mInvisibleView) {
                return 0;
            }
            CellLayout.LayoutParams lp = (CellLayout.LayoutParams) v.getLayoutParams();
            int centerX = lp.x + cellWidth / 2;
            return dx > centerX ? 1 : 0;
        }

        int minResult = Integer.MAX_VALUE;
        int minIndex = 0;
        View child = null;
        int correctedX = 0;
        CellLayout.LayoutParams lp = null;
//        ItemInfo info = null;
        for (int i = 0; i < dockChildCount; i++) {
            //child = mContent.getShortcutAndWidgetContainer().getChildAt(i);
            child = mViewCacheList.get(i);
//            info = (ItemInfo)child.getTag();
            lp = (CellLayout.LayoutParams) child.getLayoutParams();
            correctedX = getCorrectedX(lp.x, mMoveDireciton == 0);
//            Log.d(TAG, "sxsexe55------> getAppropriateIndex  i " + i + " info " 
//                    + (info == null ? "null" : info.title) + " lp.x " + lp.x 
//                    + " correctedX " + correctedX + " touchX " + dx);
            if (minResult > Math.abs(correctedX - dx)) {
                minResult = Math.abs(correctedX - dx);
                minIndex = i;
            }
        }
//        Log.d(TAG, "sxsexe55------> getAppropriateIndex return " + minIndex);
        return minIndex;
    }
    
    public void touchToPoint(int touchX, int[] topLeft, boolean fromHotseat, boolean toHotseat) {
        //int dockChildCount = mContent.getShortcutAndWidgetContainer().getChildCount();
        int dockChildCount = mViewCacheList.size();
        int cellWidth = mContent.getCellWidth();
        
        int minResult = Integer.MAX_VALUE;
        int minIndex = 0;
        View child = null;
        int centerX = 0;
        CellLayout.LayoutParams lp = null;
        for (int i = 0; i < dockChildCount; i++) {
            //child = mContent.getShortcutAndWidgetContainer().getChildAt(i);
            child = mViewCacheList.get(i);
            lp = (CellLayout.LayoutParams) child.getLayoutParams();
            centerX = lp.x + cellWidth / 2;
            //Log.d(TAG, "sxsexe22-----> touchToPoint i " + i + " centerX " + centerX);
            if (minResult > Math.abs(centerX - touchX)) {
                minResult = Math.abs(centerX - touchX);
                minIndex = i;
            }
        }
        //child = mContent.getShortcutAndWidgetContainer().getChildAt(minIndex);
        child = mViewCacheList.get(minIndex);
        if(child != null) {//FIXME
            lp = (CellLayout.LayoutParams) child.getLayoutParams();
            topLeft[0] = lp.x + mContent.getPaddingLeft();
            topLeft[1] = lp.y + mContent.getPaddingTop();
        }
        //Log.d(TAG, "sxsexe55-----> touchToPoint touchX " + touchX + " topLeft[0] " + topLeft[0] + " topLeft[1] " + topLeft[1]);
    }
    
    public int getAppropriateLeft(int dx, boolean fromHotseat) {
        int index = getAppropriateIndex(dx);
        View child = mContent.getShortcutAndWidgetContainer().getChildAt(index);
        int left = 0;
        if(child != null) {
            CellLayout.LayoutParams lp = (CellLayout.LayoutParams) child.getLayoutParams();
            left = lp.x;
        } else {
            left = 0;
        }
        Log.d(TAG, "sxsexe22-----> child " + child + " left " + left + " index " + index);
        return left;
    }
    
    protected boolean isFull() {
        if(mInvisibleViewAdded) {
            return false;
        }
        if (mViewCacheList.size() > ConfigManager.getHotseatMaxCountX()) {
        	fillViewsFromCache();
        }
        return mContent.getShortcutAndWidgetContainer().getChildCount() >= ConfigManager
                .getHotseatMaxCountX();
    }
    
    /**
     * update hotseat items in database
     */
    protected void updateItemInDatabase() {
        int count = mContent.getShortcutsAndWidgets().getChildCount();
        int container = Favorites.CONTAINER_HOTSEAT;

        for (int i = 0; i < count; i++) {
            View v = mContent.getShortcutsAndWidgets().getChildAt(i);
            ItemInfo info = (ItemInfo) v.getTag();
            // Null check required as the AllApps button doesn't have an item info
            if (info != null) {
                info.requiresDbUpdate = false;
                Log.d(TAG, "sxsexe------> updateItemInDatabase info " + info); 
                LauncherModel.modifyItemInDatabase(mLauncher, info, container, info.screen, info.cellX,
                        info.cellY, info.spanX, info.spanY);
            }
        }
    }
    
    /**
     * update the screen and cellX of items in hotseat
     */
    protected void updateItemCell() {
        //fillViewsFromCache();
        ShortcutAndWidgetContainer container = mContent.getShortcutsAndWidgets();
        int count = container.getChildCount();
        mViewCacheList.clear();
        
        //clear
        for (int i = 0; i < count; i++) {
            View v = container.getChildAt(i);
//            Log.d(TAG, "sxsexe55------->updateItemCell111 i " + i + " v " + v + " tag " + (v == null ? " null " : v.getTag()));
            if(v != null && v.getTag() != null) {
                mViewCacheList.add(v);
            }
        }
        
        count = mViewCacheList.size();
        for (int i = 0; i < count; i++) {
            View v = mViewCacheList.get(i);
            ItemInfo info = (ItemInfo) v.getTag();
            com.tpw.homeshell.CellLayout.LayoutParams lp = (com.tpw.homeshell.CellLayout.LayoutParams) v.getLayoutParams();
            // Null check required as the AllApps button doesn't have an item info
//            Log.d(TAG, "sxsexe55------->updateItemCell info " + (info == null ? "null" : info.title) + " screen " + i);
            if (info == null) continue;
            lp.cellX = i;
            lp.cellY = 0;
            info.cellX = i;
            info.cellY = 0;
            info.screen = i;
            info.container = Favorites.CONTAINER_HOTSEAT;
        }
        fillViewsFromCache();
        
    }
    
    protected Animator getHotseatAnimator(boolean hide) {
        int startY = hide ? mAnimStartY : mAnimEndY;
        int endY = hide ? mAnimEndY : mAnimStartY;
        ValueAnimator bounceAnim = ObjectAnimator.ofFloat(this, "y",startY, endY);
        bounceAnim.setDuration(getResources().getInteger(R.integer.config_workspaceUnshrinkTime));
        bounceAnim.setInterpolator(new LinearInterpolator());
        return bounceAnim;
    }
    
    protected void revisibleHotseat() {
        setY(Math.min(mAnimStartY, mAnimEndY));
    }
    
    public boolean isTouchInHotseat() {
        return mTouchInHotseat;
    }
    
    public void onPause() {
        onExitHotseat(false);
    }
    
    public ShortcutAndWidgetContainer getContainer() {
        return mContent.getShortcutAndWidgetContainer();
    }
    
    public CellLayout getCellLayout() {
        return mContent;
    }
    
    public boolean checkDragitem(View view) {
	ItemInfo info = (ItemInfo) view.getTag();
	return view == mDragedItemView
	        || (mDragedItemView == null &&
                    info != null &&
                    info.container == LauncherSettings.Favorites.CONTAINER_HOTSEAT);
    }
    
    private void relayouViewCacheList() {
        int visibleCount = mViewCacheList.size();
        if (visibleCount > 0) {
            Resources res = getResources();
            int width = getRight() - getLeft();
            width = width - getPaddingLeft() - getPaddingRight()
                    - mContent.getPaddingLeft() - mContent.getPaddingRight();

            int wGap = 0;
            int cellW = mContent.getCellWidth();
            int l = 0;
            //int wGap = 0;
            int space = width - visibleCount * cellW;
            int workspaceCountX = res.getInteger(
                    R.integer.cell_count_x);
            if (visibleCount >= workspaceCountX) {
                wGap = (int) (space / (float) (visibleCount - 1));
            } else {
                wGap = (int) (space / (float) (visibleCount + 1));
                l = wGap;
            }

            boolean rtl = mContent.getShortcutAndWidgetContainer().isLayoutRtl();
            for (int i = 0; i < visibleCount; i++) {
                View v = mViewCacheList.get(!rtl ? i : visibleCount - i - 1);
                if(v != mInvisibleView) {
                    l += (cellW + wGap);
                    continue;
                } else {
                    CellLayout.LayoutParams lp = (CellLayout.LayoutParams) v
                            .getLayoutParams();
                    lp.x = l + lp.leftMargin;
                    /*Log.d(TAG, "sxsexe55---->relayouViewCacheList i : " + i + " lp.x " + lp.x
                            + " lp.y " + lp.y
                            + " mInvisibleView " + (v == mInvisibleView));*/
                    return;
                }
            }
        }
    }
    
    private int getLocationY() {
        int[] location = new int[2];
        mContent.getLocationOnScreen(location);
        return location[1];
    }
    
    private void dumpViewCacheList() {/*
        for(View view : mViewCacheList) {
            ItemInfo info = (ItemInfo) view.getTag();
            Log.d(TAG, "sxsexe55---------> dumpViewCacheList " + (info == null ? " null " : info.title));
        }
        ShortcutAndWidgetContainer container = mContent.getShortcutAndWidgetContainer();
        int count = container.getChildCount();
        for(int i = 0; i < count; i++) {
            View v = container.getChildAt(i);
            ItemInfo info = (ItemInfo) v.getTag();
            Log.d(TAG, "sxsexe55---------> dumpContainer " + (info == null ? " null " : info.title));
        }
    */}
    
    public void removeViewByItemInfo(ItemInfo info) {
	ShortcutAndWidgetContainer container = mContent.getShortcutAndWidgetContainer();
	int count = container.getChildCount();
        for (int i = 0; i < count; i++) {
            View v = container.getChildAt(i);
            ItemInfo info1 = (ItemInfo) v.getTag();
           // Log.d(TAG, "sxsexe---------->removeViewByItemInfo info " + info + " info1 " + info1 + " info == info1 " + (info == info1));
            if (info == info1) {
                container.removeViewAt(i);
                break;
            }
        }
    }
    
    public void initViewCacheList() {
        mViewCacheList.clear();
        ShortcutAndWidgetContainer container = mContent.getShortcutAndWidgetContainer();
        int count = container.getChildCount();
        for(int i = 0; i < count; i++) {
            View v = container.getChildAt(i);
            //ItemInfo info = (ItemInfo) v.getTag();
            //Log.d(TAG, "sxsexe55-----> initViewCacheList add " + (info==null ? " null " : info));
            mViewCacheList.add(v);
        }
    }
    
    private void removeViewFromCacheList(View v) {
        mViewCacheList.remove(v);
    }
    
    private void addToCacheList(int index, View view) {
        if(index == -1 || index >= mViewCacheList.size()) {
           mViewCacheList.add(view);
        } else {
           mViewCacheList.add(index, view);
        }
        relayouViewCacheList();
    }
    
    private void fillViewsFromCache() {
        ShortcutAndWidgetContainer container = mContent.getShortcutAndWidgetContainer();
        container.removeAllViews();
        for(View view : mViewCacheList) {
            ItemInfo info = (ItemInfo) view.getTag();
            if(view.getParent() != null) {
            	((ViewGroup)view.getParent()).removeView(view);
            }
            //Log.d(TAG, "sxsexe55---------> fillViewFromCache " + (info == null ? " null " : info.title));
            container.addView(view);
        }
    }
    public void adjustToThreeLayout() {
        getLayoutParams().height = mLauncher.getResources()
                .getDimensionPixelSize(R.dimen.workspace_cell_height_3_3);
        mCellCountX = ConfigManager.getHotseatMaxCountX();
        mViewCacheList = new ArrayList<View>(mCellCountX);
        mContent.resetGridSize(ConfigManager.getHotseatMaxCountX(),
                ConfigManager.getHotseatMaxCountY());
        mContent.setMode(Mode.NORMAL);
        int childCount = mContent.getShortcutAndWidgetContainer().getChildCount();
        for (int i = 0; i < childCount; i++) {
            View view = mContent.getShortcutAndWidgetContainer().getChildAt(i);
            if (view instanceof BubbleTextView) {
                ((BubbleTextView) view).setMode(Mode.NORMAL);
            } else if (view instanceof FolderIcon) {
                ((FolderIcon) view).setHotseatMode(false);
            } else {
                Log.e(TAG, "hotseat error occured when adjust from three layout");
            }
        }
        getLayout().adjustToThreeLayout();
        this.setPadding(this.getPaddingLeft(),
                getResources().getDimensionPixelSize(R.dimen.hotseat_top_padding_3_3),
                this.getPaddingRight(), this.getPaddingBottom());
        mContent.getLayoutParams().height = mContent.getDesiredHeight();
    }

    public void adjustFromThreeLayout() {
        getLayoutParams().height = mLauncher.getResources()
                .getDimensionPixelSize(R.dimen.button_bar_height_plus_padding);
        mCellCountX = ConfigManager.getHotseatMaxCountX();
        mViewCacheList = new ArrayList<View>(mCellCountX);
        mContent.resetGridSize(ConfigManager.getHotseatMaxCountX(),
                ConfigManager.getHotseatMaxCountY());
        mContent.setMode(Mode.HOTSEAT);
        int childCount = mContent.getShortcutAndWidgetContainer().getChildCount();
        for (int i = 0; i < childCount; i++) {
            View view = mContent.getShortcutAndWidgetContainer().getChildAt(i);
            if(view instanceof BubbleTextView) {
                ((BubbleTextView) view).setMode(Mode.HOTSEAT);
            } else if(view instanceof FolderIcon) {
                ((FolderIcon) view).setHotseatMode(true);
            } else {
                Log.e(TAG, "hotseat error occured when adjust from three layout");
            }
        }
        getLayout().adjustFromThreeLayout();
        this.setPadding(this.getPaddingLeft(), 0, this.getPaddingRight(), this.getPaddingBottom());
        mContent.getLayoutParams().height = mContent.getDesiredHeight();
    }
/*    
    private void addViewWithoutInvalidate(View view, int index) {
        ShortcutAndWidgetContainer container = mContent.getShortcutAndWidgetContainer();
        Class<ViewGroup> clazz = ViewGroup.class;
        try {
            CellLayout.LayoutParams lp = (com.tpw.homeshell.CellLayout.LayoutParams) view.getLayoutParams();
            Method m = clazz.getDeclaredMethod("addViewInner ", View.class, Integer.class, com.tpw.homeshell.CellLayout.LayoutParams.class, Boolean.class);
            m.setAccessible(true);
            m.invoke(container, view, index, lp, false);
            
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }
    
    private void removeViewWithoutInvalidate(View view) {
        ShortcutAndWidgetContainer container = mContent.getShortcutAndWidgetContainer();
        Class<ViewGroup> clazz = ViewGroup.class;
        try {
            Method m = clazz.getDeclaredMethod("removeViewInternal ", View.class);
            m.setAccessible(true);
            m.invoke(container, view);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }
   */ 
}
