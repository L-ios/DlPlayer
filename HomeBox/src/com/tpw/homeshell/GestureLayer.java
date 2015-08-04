package com.tpw.homeshell;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import android.app.WallpaperManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.Bitmap.Config;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.PowerManager;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.Animation.AnimationListener;
import android.widget.FrameLayout;
import android.widget.Scroller;
import android.widget.Toast;
import android.graphics.Color;
import android.content.BroadcastReceiver;
import android.content.IntentFilter;

import com.tpw.homeshell.icon.BubbleTextView;
import com.tpw.homeshell.setting.HomeShellSetting;
import com.tpw.homeshell.utils.ToastManager;
import com.tpw.homeshell.utils.Utils;

public class GestureLayer extends FrameLayout {
    private static final String TAG = "GestureLayer";
    
    private enum MoveType{UP,DOWN,LEFT,RIGHT,NONE};

    private Set<Integer> mPointOnScreen;
    private SparseArray<PointF> mInitPosition;
    private DragController mDragController;
    private Launcher mLauncher;
    
    private boolean hasDetected;
    private boolean folderIsOpen = false;

    // most points during the process of gesture
    private int mostPointsReached;

    private boolean mTouchEnabled = true;

    private boolean mAllowHandleTwoFingers = true;
    private final int TWO_FINGER_MAX_DELTA = (int) (10 * LauncherApplication.getScreenDensity());

    public GestureLayer(Context context, AttributeSet attrs) {
        super(context, attrs);
        mPointOnScreen = new HashSet<Integer>();
        mInitPosition = new SparseArray<PointF>();
        hasDetected = false;
        
        Interpolator interpolator = new AccelerateInterpolator(1.0f);
        mScroller = new Scroller(context, interpolator);
    }
    
    public void setup( Launcher launcher, DragController dragController ){
        mLauncher = launcher;
        mDragController = dragController;
    }
    
    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        // added new feature: fling application icon to call speech service
        /*if (handleFlingEvent(event))
            return true;*/
        if (mLauncher.isGadgetCardShowing())
            return super.onInterceptTouchEvent(event);
       
        handleGestureEvent(event);
        return filterUnnecessaryEvent(event);
    }
    
    private boolean filterUnnecessaryEvent(MotionEvent event){
        int action = event.getAction() & MotionEvent.ACTION_MASK;

        //avoid move when two finger on screen 
        if (action == MotionEvent.ACTION_MOVE && event.getPointerCount() >= 2) {
            return true;
        }

        //avoid on click when doing animation
        if (action == MotionEvent.ACTION_DOWN && 
            LockScreenAnimator.getInstance(mLauncher).isRuning() ){ 
            return true;
        }

        // is touch is not enabled, ignore this event
        if ( !mTouchEnabled ){
            // this happens before delete dialog shown
            return true;
        }

        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);
        if( event.getActionMasked() != MotionEvent.ACTION_DOWN ){
            //don't handle ACTION_DONW to avoid duplicate down event
            handleGestureEvent(event);
        }
        return true;
        //这里返回true是为了防止一个ACTION_DOWN从头到尾都没有人处理
    }

    /**
     * 在onInterceptTouchEvent和onTouchEvent之中处理事件
     */
    private void handleGestureEvent(MotionEvent event){
        // it can not enter Screen edit Mode,
        // When the dialog is displayed
        if (!mTouchEnabled)
            return;
        int action = event.getAction() & MotionEvent.ACTION_MASK;
        int N = event.getPointerCount();

        // whatHappend(event);

        if (hideseatProcess(event)) {
            return;
        }

        //如果已经执行了锁屏或者其他操作，但是多余的Touch事件还在忘里面传
        //这时候就要把多余的清理掉，用hasBeenLocked判断。直到下一个ACTION_DOWN表明新的按下的过程开始了
        if( hasDetected){
            if( action == MotionEvent.ACTION_DOWN ){
                hasDetected = false;
            } else{
                return;
            }
        }
        assert N == mPointOnScreen.size();

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                clear();
                onPointDown(event);
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                onPointDown(event);
                break;
            case MotionEvent.ACTION_MOVE:
                onPointUpdate(event);
                break;
            case MotionEvent.ACTION_POINTER_UP:
                onPointUp(event);
                break;
            case MotionEvent.ACTION_UP:
                onPointUp(event);
                clear();
                break;
            default:
                clear();
                break;
        }
    }

    /**
     * 在ACTION_MOVE的时候进行一些更新的动作，比如检测点移动的距离
     */
    private void onPointUpdate(MotionEvent event) {
        if( mDragController.isDragging() ){
            return;
        }
        
        if (!checkProcessedDown(event)) {
            return;
        }

        int numPoints = event.getPointerCount();
        if (numPoints == 1 && mAllowHandleTwoFingers) {
            PointF p0 = mInitPosition.get(event.getPointerId(0));
            PointF pt = new PointF(event.getX(0), event.getY(0));
            if (distance(p0, pt) > TWO_FINGER_MAX_DELTA) {
                // if the first point moves too far, the second finger won't
                // be recognized as two finger gesture.
                mAllowHandleTwoFingers = false;
            }
        }
        if( numPoints < 2 ) {
            //handleSingleFingerMove(event);
            return;
        }

        PointF[] pNow = new PointF[numPoints];
        PointF[] pInit = new PointF[numPoints];
        for (int i = 0; i < numPoints; i++) {
            int pointId = event.getPointerId(i);
            pNow[i] = new PointF(event.getX(i), event.getY(i));
            pInit[i] = mInitPosition.get(pointId);
        }

        if( Utils.isYunOS2_9System()) return;
        if (HomeShellSetting.getFreezeValue(getContext())) return;
        if( forbidGesture() ) return;
        checkTwoFinger( pInit, pNow);
        checkThreeFinger( pInit, pNow);
    }
    
    private boolean checkProcessedDown(MotionEvent event) {
        final int N = event.getPointerCount();
        for (int i = 0; i < N; i++) {
            int id = event.getPointerId(i);
            if (mInitPosition.get(id) == null) {
                return false;
            }
        }

        return true;
    }

    public int getPointerCount(){
        if( mPointOnScreen != null ){
            return mPointOnScreen.size();
        }
        return 0;
    }
    
    private boolean forbidGesture(){
        if( mLauncher.isAllAppsVisible() )
            return true;
        if( folderIsOpen )
            return true;
        if( mLauncher.isGadgetCardShowing())
            return true;
        if( LockScreenAnimator.getInstance(mLauncher).shouldPreventGesture() )
            return true;
        if( mLauncher.isWorkspaceLoading()) {
            return true;
        }
        if ( mLauncher.getWorkspace().isPageMoving()) {
            return true;
        }
        //added by qinjinchuan topwise for disabling hideseat in editmode#bug243
        if ( mLauncher.isEditMode()) {
            return true;
        }
        //added by qinjinchuan topwise for disabling hideseat in editmode#bug243
        if ( mLauncher.getWorkspace().iscurrWidgetPage())return true; //added by qinjinchuan topwise for disabling hideseat in widget page
        return false;
    }
    /**
     * @param pInit 两个指头按下时候的坐标
     * @param pNow  两个指头滑动时候的坐标
     */
    private void checkTwoFinger(PointF[] pInit, PointF[] pNow){
        if( pInit.length != 2 ) return;
        if( pInit.length != pNow.length ) return;
        if( mostPointsReached > 2 ) return;
        if( !mAllowHandleTwoFingers ) {
            return;
        }

        // enter or exit screen edit mode when hideseat is closed
        if (mHideseatStatus == STATUS.CLOSED) {
            if (mLauncher.isInEditScreenMode()) {
                if (perimeter(pNow) > perimeter(pInit) * 2 &&
                        isTwoFingerOpen(pInit, pNow)) {
                    exitScreenEditMode();
                }
                // if it is in screen edit mode , just return
                return;
            } else {
                if (perimeter(pNow) < perimeter(pInit) / 2 &&
                        isTwoFingerClose(pInit, pNow)
                        && !mLauncher.isEditMode()) {
                    enterScreenEditMode();
                    return;
                }
            }
        }

        // 两指捏合
        if (perimeter(pNow) < perimeter(pInit) &&
            isTwoFingerClose(pInit, pNow) ) {
            float y0 = pInit[0].y;
            float y1 = pInit[1].y;

            Log.d(TAG, "y0 : " + y0 + " y1 : " + y1);
            if (y0 != y1 && mLauncher.isHideseatShowing()) {
                mHideseatStatus = STATUS.CLOSING;
                mResult = 0;
                mIndex = y0 > y1 ? 0 : 1;
                mStartY = (int) pNow[mIndex].y;
                mLastY = mStartY;
                mLauncher.hideHideseat(true);
                hasDetected = true;
            }
        }
        
        // 两指放大
        if (perimeter(pNow) > perimeter(pInit) &&
            isTwoFingerOpen(pInit, pNow) ) {
            float y0 = pInit[0].y;
            float y1 = pInit[1].y;
            Log.d(TAG, "y0 : " + y0 + " y1 : " + y1);
            if (y0 != y1 && !mLauncher.isHideseatShowing()
                    && !mLauncher.isEditMode()) {
                mResult = 0;
                mIndex = y0 > y1 ? 0 : 1;
                mStartY = (int) pNow[mIndex].y;
                mLastY = mStartY;
                mHideseatStatus = STATUS.OPENING;
                updateLiveWallpaperFlag();
                mLauncher.openHideseat(true);
                hasDetected = true;
            }
        }
    }

    /**
     * 检测一个点的移动方向
     */
    private MoveType getMoveType(PointF pInit, PointF pNow){
        float yDelta = pNow.y - pInit.y;
        float xDelta = pNow.x - pInit.x;
        float yAbs = Math.abs(yDelta);
        float xAbs = Math.abs(xDelta);

        if( yDelta < 0 && xAbs < yAbs) return MoveType.UP;
        if( yDelta > 0 && xAbs < yAbs) return MoveType.DOWN;
        if( xDelta < 0 && yAbs < xAbs) return MoveType.LEFT;
        if( xDelta > 0 && yAbs < xAbs) return MoveType.RIGHT;

        return MoveType.NONE;
    }

    /**
     * 判断两指是否推开
     */
    private boolean isTwoFingerOpen(PointF[] pInit, PointF[] pNow ){
        if( pInit.length != 2 ) return false;
        if( pInit.length != 2 ) return false;

        MoveType[] types = new MoveType[2];
        types[0] = getMoveType(pInit[0], pNow[0]);
        types[1] = getMoveType(pInit[1], pNow[1]);

        if (pInit[0].y < pInit[1].y) {
            if (types[0] == MoveType.UP && types[1] == MoveType.DOWN)
                return true;
        } else {
            if (types[1] == MoveType.UP && types[0] == MoveType.DOWN)
                return true;
        }

        return false;
    }

    /**
     * 判断两指是否捏合
     */
    private boolean isTwoFingerClose(PointF[] pInit, PointF[] pNow ){
        if( pInit.length != 2 ) return false;
        if( pInit.length != 2 ) return false;

        MoveType[] types = new MoveType[2];
        types[0] = getMoveType(pInit[0], pNow[0]);
        types[1] = getMoveType(pInit[1], pNow[1]);

        if (pInit[0].y > pInit[1].y) {
            if (types[0] == MoveType.UP && types[1] == MoveType.DOWN)
                return true;
        } else {
            if (types[1] == MoveType.UP && types[0] == MoveType.DOWN)
                return true;
        }

        return false;
    }

    /**
     * @param pInit 三个（及以上）指头按下时候的坐标
     * @param pNow  三个（及以上）指头滑动时候的坐标
     */
    private void checkThreeFinger(PointF[] pInit, PointF[] pNow) {
        if (pInit.length < 3) return;
        if (pInit.length != pNow.length) return;
        if (mLauncher.isHideseatShowing() ) return;

        // 三指或者以上向外锁屏
        if (perimeter(pNow) > perimeter(pInit) * 2) {
            if (!mLauncher.isInEditScreenMode() && mLauncher.hasWindowFocus()) {
                LockScreenAnimator lsa = LockScreenAnimator
                        .getInstance(mLauncher);
                lsa.play();
                hasDetected = true;
                UserTrackerHelper.sendUserReport(UserTrackerMessage.MSG_THREE_FINGER_LOCK);
            }
        }
    }

    //check drop down gesture
    private boolean checkDropDownMove(PointF pInit, PointF pNow) {
        float distanceY = pNow.y - pInit.y;
        float distanceX = pNow.x -pInit.x;
        //Log.d(TAG, "checkDropDownMove distanceY " + distanceY + " distanceX " + distanceX);
        /*return distanceY >= Launcher.SEARCH_UI_Y_MIN_DISTANCE 
                && Math.abs(distanceX) < Launcher.SEARCH_UI_X_MAX_DISTANCE;*/
        return false;//comment Search Entry
    }
    
    private void enterScreenEditMode(){
        if (mLauncher != null) {
            if (mLauncher.getModel().isEmptyCellCanBeRemoved() == false) {
                ToastManager.makeToast(ToastManager.NOT_ALLOW_EDIT_IN_RESTORE);
            }else{
                mLauncher.enterScreenEditMode();
            }
            hasDetected = true;
        }
    }
    
    private void exitScreenEditMode() {
        if (mLauncher.isInEditScreenMode()) {
            mLauncher.exitScreenEditMode(true);
            hasDetected = true;
        }
    }
    
    private void lockScreen() {
        lockScreen(getContext());
        hasDetected = true;
    }

    /**
     * 当一个点下落到屏幕的时候调用
     * @param event 传递过来的MotionEvent对象
     */
    private void onPointDown(MotionEvent event) {
        int pointIndex = event.getActionIndex();
        int pointId = event.getPointerId(pointIndex);
        PointF p = new PointF(event.getX(pointIndex), event.getY(pointIndex));
        mPointOnScreen.add(pointId);
        mInitPosition.put(pointId, p);
        mostPointsReached++;
        
        if( mLauncher.getWorkspace().getOpenFolder() != null ){
            folderIsOpen = true;
        }
        if(CheckVoiceCommandPressHelper.PUSH_TO_TALK_SUPPORT) {
            if(CheckVoiceCommandPressHelper.getInstance().isVoiceUIShown()) {
                CheckVoiceCommandPressHelper.getInstance().forceDismissVoiceCommand();
            }
        }
    }
     
    /**
     * 当一个点离开屏幕的时候调用
     * @param event 传递过来的MotionEvent对象
     */
    private void onPointUp(MotionEvent event) {
        if (!checkProcessedDown(event)) {
            return;
        }

        int pointId = event.getPointerId(event.getActionIndex());
        mPointOnScreen.remove(pointId);
        mInitPosition.remove(pointId);
     }

    /**
     * 清空变量
     */
    private void clear() {
        mPointOnScreen.clear();
        mInitPosition.clear();
        folderIsOpen = false;
        mostPointsReached = 0;
        mAllowHandleTwoFingers = true;
    }

    /**
     * 打印触摸事件的信息 
     * @param event 传递过来的MotionEvent对象
     */
    private void whatHappend(MotionEvent event) {
        int id = event.getPointerId(event.getActionIndex());
        String loginfo = "";
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
        case MotionEvent.ACTION_DOWN:
            loginfo = "ACTION_DOWN";
            break;
        case MotionEvent.ACTION_POINTER_DOWN:
            loginfo = "ACTION_POINTER_DOWN";
            break;
        case MotionEvent.ACTION_MOVE:
            loginfo = "ACTION_MOVE";
            break;
        case MotionEvent.ACTION_UP:
            loginfo = "ACTION_UP";
            break;
        case MotionEvent.ACTION_POINTER_UP:
            loginfo = "ACTION_POINTER_UP";
            break;
        case MotionEvent.ACTION_CANCEL:
            loginfo = "ACTION_CANCEL";
        default:
            break;
        }
        Log.d(TAG, id + " " + loginfo+" point count = "+event.getPointerCount());
    }

    /**
     * 求两点之间的距离
     */
    private double distance(PointF p1, PointF p2) {
        return Math.sqrt(Math.pow(p1.x - p2.x, 2) + Math.pow(p1.y - p2.y, 2));
    }

    /**
     * 求周长
     */
    private double perimeter(PointF[] points){
        double sum = 0;
        
        if( points.length < 2 ) 
            return sum;
        
        sum += distance(points[points.length-1], points[0]);
        for( int i = 1; i < points.length; i++ ){
            sum += distance(points[i-1], points[i]);
        }
        return sum;
    }

    private void lockScreen(Context context){
        try {
            //This is what happens when the power key is pressed to turn off the screen
            PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            pm.goToSleep(SystemClock.uptimeMillis());
        } catch (Exception e){
            Log.d(TAG,"lockscrenn fail",e);
        }
    }
	private View mTouchedView;
	private VelocityTracker mVelocityTracker = VelocityTracker.obtain();
	private int mFlingToCallSpeechThresholdVelocity;
	private final int MIN_FLING_DEGREES = 145;
	
    private static final String ALIVOICE_PACKAGE_NAME = "com.yunos.vui.phone";
    private static final String ALIVOICE_ACTIVITY_NAME = "com.yunos.vui.PushTalkActivity";
    private static final String SCENE_TYPE_CONTACT = "yunos.vui.contact";
    private static final String SCENE_TYPE_CALL = "yunos.vui.call";
    private static final String SCENE_TYPE_SMS = "yunos.vui.sms";
    private static final String SCENE_TYPE_SETTING = "yunos.vui.setting";
    private static final String SCENE_TYPE_WEBSITE = "yunos.vui.website";
    
    private HashMap<String, String> speechPackageMap = new HashMap<String, String>();
    
    private FlingToCallSpeechFilter mFilter = new FlingToCallSpeechFilter() {
    	
		@Override
		public String isItFlingToCallSpeech(Object target) {
			if (target instanceof ShortcutInfo) {
				StringBuilder result = new StringBuilder();
				ShortcutInfo info = (ShortcutInfo) target;
				try{
					result.append(info.intent.getComponent().getPackageName())
				    .append("/")
				    .append(info.intent.getComponent().getClassName());
					return speechPackageMap.get(result.toString());
				}catch(Exception e){
					Log.w(TAG,"isItFlingToCallSpeech : get package info error : "+e.toString());
					return null;
				}
			}
			return null;
		}
	};
	
	private void initSpeechList() {
		speechPackageMap.put("com.yunos.alicontacts/com.yunos.alicontacts.activities.DialtactsActivity", SCENE_TYPE_CALL);
		speechPackageMap.put("com.yunos.alicontacts/com.yunos.alicontacts.activities.PeopleActivity2", SCENE_TYPE_CONTACT);
		speechPackageMap.put("com.android.settings/com.android.settings.tpw.AliSettingsMain", SCENE_TYPE_SETTING);
		speechPackageMap.put("com.yunos.alicontacts/com.yunos.alimms.ui.ConversationList", SCENE_TYPE_SMS);
		speechPackageMap.put("com.UCMobile.yunos/com.UCMobile.main.UCMobile", SCENE_TYPE_WEBSITE);
        speechPackageMap.put("com.android.mms/com.android.mms.ui.ConversationList", SCENE_TYPE_SMS);
		speechPackageMap.put("com.yunos.alicontacts/com.yunos.alicontacts.activities.DialtactsContactsActivity", SCENE_TYPE_CALL);
	}
	
	private Runnable mFlingRunnable = new Runnable() {
		@Override
		public void run() {
			if (mTouchedView != null) {
				Object flingObject = mTouchedView.getTag();
				if (flingObject != null) {
					String session = mFilter.isItFlingToCallSpeech(flingObject);
//					ShortcutInfo info = (ShortcutInfo) flingObject;
//					String cmp = info.intent.getComponent().getPackageName() + "/" + info.intent.getComponent().getClassName();
//					android.widget.Toast.makeText(mLauncher, cmp + "    fling:" + flingObject, android.widget.Toast.LENGTH_LONG).show();
					if (session != null) {
						startActivityByFlingObject(flingObject, session, (int)mTouchedView.getX(), (int)mTouchedView.getY());
					}
				}
			}
		}
	};
	
	public void initFlingParams(Launcher launcher) {
		this.mLauncher = launcher;
		Resources r = launcher.getResources();
		float density = r.getDisplayMetrics().density;
        mFlingToCallSpeechThresholdVelocity =
                (int) (r.getInteger(R.integer.config_flingToDeleteMinVelocity) * density);
        initSpeechList();
	}
	
	private boolean handleFlingEvent(MotionEvent me) {
		if (isThreeFingerMode(me)) {
			mTouchedView = null;
			return false;
		}
		if (mDragController.isDragging()) {
			mTouchedView = null;
			return false;
		}
		if (mLauncher.getWorkspace().getOpenFolder() != null
				&& mLauncher.getWorkspace().getOpenFolder().getState() == Folder.STATE_ANIMATING) {
			mTouchedView = null;
			return false;
		}
		int x = (int) me.getRawX();
		int y = (int) me.getRawY();
		int action = me.getAction();
		switch(action) {
		case MotionEvent.ACTION_DOWN:
			Rect hitRect = new Rect();
			
			// when there is a folder opened.
			Folder folder = mLauncher.getWorkspace().getOpenFolder();
			if (folder != null) {
				final ShortcutAndWidgetContainer container = folder.getContent().getShortcutAndWidgetContainer();
				mTouchedView = getTouchedView(container, hitRect, x, y, (int)folder.getX(), (int)folder.getY());
				break;
			}
			// normal state
	        int currentPageIndex = mLauncher.getWorkspace().getCurrentPage();
	    	final CellLayout currentPage = (CellLayout) mLauncher.getWorkspace().getChildAt(currentPageIndex);
            if (currentPage != null) {
                final ShortcutAndWidgetContainer container = currentPage.getShortcutAndWidgetContainer();
                mTouchedView = getTouchedView(container, hitRect, x, y, (int)currentPage.getPaddingLeft(), (int)currentPage.getY() + currentPage.getPaddingTop());
                if (mTouchedView == null) {
                    final CellLayout hotseat = mLauncher.getHotseat().getLayout();
                    final ShortcutAndWidgetContainer hotseatContainer = hotseat.getShortcutAndWidgetContainer();
                    mTouchedView = getTouchedView(hotseatContainer, hitRect, x, y, (int)mLauncher.getHotseat().getPaddingLeft(), (int)mLauncher.getHotseat().getY() + mLauncher.getHotseat().getPaddingTop());
                }
            }
		    break;
		case MotionEvent.ACTION_UP:
			PointF vec = isFlingingToCallSpeech();
			if (vec != null) {
				mFlingRunnable.run();
			}
			mTouchedView = null;
			break;
		case MotionEvent.ACTION_MOVE:
			if (mTouchedView != null) {
				mVelocityTracker.addMovement(me);
			}
			break;
		}
		return false;
	}
    private View getTouchedView(ViewGroup container, Rect hitRect, int x, int y, int relativeX, int relativeY) {
    	final int childCount = container.getChildCount();
    	for(int i=0;i<childCount;i++) {
    		final View child = container.getChildAt(i);
    		if (!isChildCanbeFling(child)) {
    			continue;
    		}
    		hitRect = new Rect((int)child.getX() + relativeX,
    				(int)child.getY() + relativeY,
    				(int)child.getX() + relativeX + child.getWidth(),
    				(int)child.getY() + relativeY + child.getHeight());
    		if (hitRect.contains(x, y)) {
    			return child;
    		}
    	}
    	return null;
    }
    
    private boolean isChildCanbeFling(View child) {
    	return child instanceof BubbleTextView;
    }
    
    private PointF isFlingingToCallSpeech() {
        ViewConfiguration config = ViewConfiguration.get(mLauncher);
        mVelocityTracker.computeCurrentVelocity(1000, config.getScaledMaximumFlingVelocity());

        if (Math.abs(mVelocityTracker.getYVelocity()) > Math.abs(mFlingToCallSpeechThresholdVelocity)) {
            // Do a quick dot product test to ensure that we are flinging upwards
            PointF vel = new PointF(mVelocityTracker.getXVelocity(),
            		Math.abs(mVelocityTracker.getYVelocity()));
            PointF upVec = new PointF(0f, -1f);
            float theta = (float) Math.acos(((vel.x * upVec.x) + (vel.y * upVec.y)) /
                    (vel.length() * upVec.length()));
            if (theta >= Math.toRadians(MIN_FLING_DEGREES)) {
                return vel;
            }
        }
        return null;
    }

    private void startActivityByFlingObject(Object flingObject, String session, int iconX, int iconY) {
		Intent intent = new Intent();
		intent.setClassName(ALIVOICE_PACKAGE_NAME, ALIVOICE_ACTIVITY_NAME);
		intent.putExtra("scene", session);
		intent.putExtra("icon_x", iconX);
		intent.putExtra("icon_y", iconY);
		if (mLauncher != null) {
			try {
				mLauncher.startActivity(intent);
			} catch (ActivityNotFoundException ane) {
				Toast.makeText(mLauncher, R.string.notify_wait_speech, Toast.LENGTH_SHORT).show();
			}
		}
    }

    public static interface FlingToCallSpeechFilter {
    	public String isItFlingToCallSpeech(Object target);
    }
    
    private boolean isThreeFingerMode(MotionEvent me) {
    	return me.getPointerCount() >= 3;
    }

    private Point getDisplaySizeForWallpaper() {
        if (mWallpaper == null || mWallpaper.isRecycled()) {
            return null;
        }
        Point displaySize = new Point(getWidth(), getHeight() - 10);
        if (mWallpaper.getWidth() * displaySize.y >
            mWallpaper.getHeight() * displaySize.x) {
            return displaySize;
        } else {
            return null;
        }
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        // draw wallpaper when hide-seat is open
        if (mWallpaper != null && !mWallpaper.isRecycled()) {
            CustomHideseat hs = mLauncher.getCustomHideseat();
            int t = hs.getTop();
            int h = hs.getHeight();
            Point displaySize = getDisplaySizeForWallpaper();
            float scale = 1.0f;
            Rect srcRect = null;
            if (displaySize != null) {
                int offset_x = (mWallpaper.getWidth() * displaySize.y / mWallpaper.getHeight() - displaySize.x) / 2;
                scale = mWallpaper.getHeight() / (float) getHeight();
                srcRect = new Rect(offset_x, (int) (t * scale), mWallpaper.getWidth() - offset_x,
                        mWallpaper.getHeight() - (int) (h * scale));
            } else {
                srcRect = new Rect(0, (int) (t * scale), mWallpaper.getWidth(),
                                   mWallpaper.getHeight() - (int) (h * scale));
            }
            Rect dst = new Rect(0, t + h, getWidth(), getHeight());
            canvas.drawBitmap(mWallpaper, srcRect, dst, null);
        }

        super.dispatchDraw(canvas);

        // draw the cached bitmap during hide-seat open/close animation
        switch (mHideseatStatus) {
        case OPENING:
        case CLOSING:
            {
                final boolean opening = (mHideseatStatus == STATUS.OPENING);
                CustomHideseat hs = mLauncher.getCustomHideseat();
                int t = hs.getTop();
                int h = hs.getHeight();
                int curY = mDiffY;
                if (mScroller.computeScrollOffset()) {
                    curY += opening ? mScroller.getCurrY(): -mScroller.getCurrY();
                }
                Rect dst = opening ? new Rect(0, t, getWidth(), getHeight()) :
                                     new Rect(0, t + h, getWidth(), getHeight() + h);
                Log.d(TAG, "dispatchDraw opening step : " + curY);
                canvas.save();
                canvas.translate(0, curY);
                // mAniBitmap is built in method: buildDrawingCacheBitmap()
                canvas.drawBitmap(mAniBitmap, null, dst, null);
                canvas.restore();

                mAnimListener.onAnimationRepeat(null);
                if ((opening && curY >= h) || (!opening && curY <= -h)) {
                    mDiffY = 0;
                    mHideseatStatus = opening ? STATUS.OPENED : STATUS.CLOSED;
                    mAnimListener.onAnimationEnd(null);
                    mAniBitmap.recycle();
                    mAniBitmap = null;
                }

                if (isLiveWallpaperFlag()) {
                    mLauncher.getCustomHideseat().setVerticalClip(
                              mHideseatStatus == STATUS.OPENING ?
                              h - curY : -curY);
                }
                invalidate();
            }
            break;
        default:
            break;
        }
    }

    private int mDiffY = -1;
    private int mLastY = 0;
    private int mResult = 0;
    private int mStartY = 0;
    private int mIndex = 0;
    private Scroller mScroller;
    private Bitmap mAniBitmap = null;
    private Bitmap mWallpaper = null;
    private AnimationListener mAnimListener;
    private STATUS mHideseatStatus = STATUS.CLOSED;
    private final int HIDESEAT_DURATION = 400;

    private enum STATUS {OPENING, OPENED, CLOSING, CLOSED};

    void openHideseat(AnimationListener l, boolean isAnimation) {
        Log.d(TAG, "openHideseat");
        mLauncher.getHideseat().setNeedsCheckLayoutConsistency();
        if (isAnimation) {
            mAniBitmap = buildDrawingCacheBitmap();
            if (mAniBitmap != null) {
                mAnimListener = l;
            } else {
                // if the bitmap cannot be generated, then temporarily disable the animation.
                isAnimation = false;
            }
        }

        l.onAnimationStart(null);

        if (isAnimation) {
            if (mHideseatStatus != STATUS.OPENING) {
                int h = mLauncher.getCustomHideseat().getHeight();
                
                int dy = h - mDiffY;
                int duration = (int) ((float)dy / h * HIDESEAT_DURATION);
                mScroller.startScroll(0, 0, 0, dy, duration);
                mHideseatStatus = STATUS.OPENING;
            }
            invalidate();
        } else {
            l.onAnimationEnd(null);
            mHideseatStatus = STATUS.OPENED;
        }
    }

    void closeHideseat(AnimationListener l, boolean isAnimation) {
        Log.d(TAG, "closeHideseat");
        if (isAnimation) {
            mAniBitmap = buildDrawingCacheBitmap();
            if (mAniBitmap != null) {
                mAnimListener = l;
            } else {
                // if the bitmap cannot be generated, then temporarily disable the animation.
                isAnimation = false;
            }
        }

        l.onAnimationStart(null);

        if (isAnimation) {
            if (mHideseatStatus != STATUS.CLOSING) {
                int h = mLauncher.getCustomHideseat().getHeight();
                int dy = h + mDiffY;
                int duration = (int) ((float)dy / h * HIDESEAT_DURATION);
                mScroller.startScroll(0, 0, 0, dy, duration);
                mHideseatStatus = STATUS.CLOSING;
            }

            invalidate();
        } else {
            l.onAnimationEnd(null);
            mHideseatStatus = STATUS.CLOSED;
        }
        
            mWallpaper = null;
    }

    private boolean hideseatProcess(MotionEvent event) {
        if (mHideseatStatus != STATUS.OPENING
            && mHideseatStatus != STATUS.CLOSING) {
            return false;
        }

        final int N = event.getPointerCount();
        if (N < 2) {
            return false;
        }

        int initY = mStartY;
        int curY = (int) event.getY(mIndex);
        int diff = (int) (curY - initY);
        int action = event.getAction() & MotionEvent.ACTION_MASK;
        int h = ConfigManager.getHideseatHeight();

        if (mHideseatStatus == STATUS.OPENING) {
            mDiffY = Math.min(h - 3, Math.max(0, diff / 3));
            if (action == MotionEvent.ACTION_POINTER_UP) {
                if (mResult >= 0) {
                    int dy = h - mDiffY;
                    int duration = (int) ((float)dy / h * HIDESEAT_DURATION);
                    mScroller.startScroll(0, 0, 0, dy, duration);
                    invalidate();
                } else {
                    mDiffY = -(h - mDiffY);
                    mLauncher.hideHideseat(true);
                }
                return true;
            }
        } else if (mHideseatStatus == STATUS.CLOSING) {
            mDiffY = Math.max(-h + 3, Math.min(0, diff / 3));

            if (action == MotionEvent.ACTION_POINTER_UP) {
                if (mResult <= 0) {
                    int dy = h + mDiffY;
                    int duration = (int) ((float)dy / h * HIDESEAT_DURATION);
                    mScroller.startScroll(0, 0, 0, dy, duration);
                    invalidate();
                } else {
                    mDiffY = h + mDiffY;
                    mLauncher.openHideseat(true);
                }
                return true;
            }
        }

        if (Math.abs(mLastY - curY) > 5) {
            mResult = curY - mLastY;
            mLastY = curY;
            if (isLiveWallpaperFlag()) {
                mLauncher.getCustomHideseat().setVerticalClip(
                          mHideseatStatus == STATUS.OPENING ?
                      h - mDiffY : -mDiffY);
            }
            invalidate();
        }

        return true;
    }

    /**
     * Builds a cached bitmap for opening/closing hide-seat animation.
     * Returns <code>null</code> when it's currently unable to generate
     * the bitmap.
     * @return the cached bitmap or {@code null}
     */
    private Bitmap buildDrawingCacheBitmap() {
        Log.d(TAG, "buildDrawingCacheBitmap: begin");
        if (mAniBitmap != null) {
            mAniBitmap.recycle();
            mAniBitmap = null;
        }

        Workspace ws = mLauncher.getWorkspace();
        final CellLayout cl = ws.getCurrentDropLayout();
        // if failed to retrieve the current layout in some weird situation,
        // just return null to disable the transition animation.
        if (cl == null) {
            Log.w(TAG, "failed to generate hideseat animation bitmap! Workspace.getNextPage() = " + ws.getNextPage());
            return null;
        }

        final PageIndicatorView pv = mLauncher.getIndicatorView();
        final boolean pvEnabled = pv.isDrawingCacheEnabled();
        if (!pvEnabled) {
            pv.setDrawingCacheEnabled(true);
        }

        final Hotseat hs = mLauncher.getHotseat();
        final boolean hsEnabled = hs.isDrawingCacheEnabled();
        if (!hsEnabled) {
            hs.setDrawingCacheEnabled(true);
        }
        
        final int oldLayerType = hs.getLayout().getLayerType();
        if( oldLayerType != View.LAYER_TYPE_NONE ){
            hs.getLayout().setLayerType(View.LAYER_TYPE_NONE, null);
        }
        hs.destroyDrawingCache();

        final Runnable recoverStateRunnable = new Runnable() {
            @Override
            public void run() {
                if (!pvEnabled) pv.setDrawingCacheEnabled(false);
                if (!hsEnabled) hs.setDrawingCacheEnabled(false);
                if (oldLayerType != View.LAYER_TYPE_NONE) {
                    hs.getLayout().setLayerType(oldLayerType, null);
                }
            }
        };

        // if failed to generate drawing cache, just return null to disable the
        // transition animation.
        Bitmap pvBmp = null;
        Bitmap hsBmp = null;
        try {
            pvBmp = pv.getDrawingCache();
            hsBmp = hs.getDrawingCache();
        } catch (Exception ex) {
            // Note that the framework API won't throw an OutOfMemoryException
            // when it is out of memory. We have to catch all kinds of exceptions
            // here.
            Log.w(TAG, "failed to get drawing cache (probably out of memory)", ex);
            recoverStateRunnable.run();
            return null;
        }

        if (pvBmp == null || hsBmp == null) {
            Log.w(TAG, "failed to generate hideseat animation drawing cache! (pv,hs) = "
                        + pvBmp + "," + hsBmp);
            recoverStateRunnable.run();
            return null;
        }

        if (mWallpaper == null) {
            WallpaperManager manager = WallpaperManager.getInstance(getContext());
            if (!isLiveWallpaperFlag()) {
                Drawable d = manager.getDrawable();
                if (d != null) {
                    mWallpaper = ((BitmapDrawable)d).getBitmap();
                    Log.d(TAG, "wallpaper size: " + mWallpaper.getWidth() + ", " + mWallpaper.getHeight());
                } else {
                    Log.w(TAG, "failed to retrieve wallpaper!");
                    recoverStateRunnable.run();
                    return null;
                }
            }
        }

        final boolean rtl = getResources().getConfiguration().getLayoutDirection() == LAYOUT_DIRECTION_RTL;
        final int left = ws.getLeft() + ws.getChildAt(rtl ? ws.getChildCount() - 1 : 0).getLeft();
        final int top = ws.getTop() + cl.getTop();
        final int hideTop = calcCustomHideseatTop();
        final int hideHeight = cl.isHideseatOpen ? ConfigManager.getHideseatHeight() : 0;
        final int W = getWidth();
        final int H = getHeight() - hideTop; // trim the area above hide-seat
        Log.v(TAG, "buildDrawingCacheBitmap: hideTop=" + hideTop + " hideHeight=" + hideHeight);

        Bitmap bitmap = Bitmap.createBitmap(W, H, Config.ARGB_8888);
        if (bitmap == null) {
            // bitmap will be null when out of memory
            Log.w(TAG, "failed to create bitmap for hide-seat animation");
            recoverStateRunnable.run();
            return null;
        }
        Log.d(TAG, "buildDrawingCacheBitmap: bitmap created");

        Canvas c = new Canvas(bitmap);
        if (mWallpaper != null && !mWallpaper.isRecycled()) {
            // c.drawBitmap(mWallpaper, 0, -hideTop, null);
            Point displaySize = getDisplaySizeForWallpaper();
            Rect srcRect = null;
            if (displaySize != null) {
                int offset_x = (mWallpaper.getWidth() * displaySize.y / mWallpaper.getHeight() - displaySize.x) / 2;
                srcRect = new Rect(offset_x, 0, mWallpaper.getWidth() - offset_x, mWallpaper.getHeight());
            } else {
                srcRect = new Rect(0, 0, mWallpaper.getWidth(), mWallpaper.getHeight());
            }
            Rect dstRect = new Rect(0, -hideTop, W, H);
            c.drawBitmap(mWallpaper, srcRect, dstRect, null);
        }

        // draw cell layout
        c.save();
        c.translate(left, top - hideTop - hideHeight);
        if (!cl.isHideseatOpen) {
            // Method draw() uses less memory because it's not creating any bitmap cache.
            // However, it does not work with advanced blending mode that used by some
            // gadgets.
            cl.drawShortcutsAndWidgetsOnCanvas(c);
        } else {
            // Calling draw() is fine when closing hide-seat. Because the gadgets are not
            // interactive during the hide-seat is in open state.
            boolean animationPlaying = cl.isHideseatAnimationPlaying();
            cl.setHideseatAnimationPlaying(false);
            cl.draw(c);
            cl.setHideseatAnimationPlaying(animationPlaying);
        }
        c.restore();

        // draw page indicator and hot-seat
        int nav_bar_height = mLauncher.getNavigationBarHeight();
        c.drawBitmap(pvBmp, 0, H - pvBmp.getHeight() - hsBmp.getHeight() - nav_bar_height, null);
        c.drawBitmap(hsBmp, 0, H - hsBmp.getHeight() - nav_bar_height, null);

        recoverStateRunnable.run();
        Log.d(TAG, "buildDrawingCacheBitmap: end");
        return bitmap;
    }

    /**
     * Retrieves the top of hide-seat (in workspace).
     */
    private int calcCustomHideseatTop() {
        Workspace workspace = mLauncher.getWorkspace();
        int top = 0;
        CellLayout layout = (CellLayout) workspace.getPageAt(workspace.getCurrentPage());
        if (layout == null) {
            layout = (CellLayout) workspace.getPageAt(0);
        }
        ShortcutAndWidgetContainer container = layout.getShortcutAndWidgetContainer();
        CellLayout.LayoutParams lp = container.buildLayoutParams(0, CellLayout.HIDESEAT_CELLY, 1, 1, true);
        top += workspace.getTop();
        top += layout.getTop();
        top += container.getTop();
        top += lp.y;
        top -= layout.getHeightGap() / 2;
        Log.v(TAG, "calcCustomHideseatTop: result = " + top);
        return top;
    }

    private boolean mLiveWallpaperFlag = false;

    private void updateLiveWallpaperFlag() {
        WallpaperManager manager = WallpaperManager.getInstance(getContext());
        mLiveWallpaperFlag = manager.getWallpaperInfo() != null;
        Log.v(TAG, "updateLiveWallpaperFlag: " + mLiveWallpaperFlag);
    }

    public boolean isLiveWallpaperFlag() {
        return mLiveWallpaperFlag;
    }

    public void setTouchEnabled(boolean mTouchEnabled) {
        this.mTouchEnabled = mTouchEnabled;
    }
    
    // for global search M 4.0 begin
    public boolean hasDragging() {
       return mDragController.isDragging();
    }
    // for global search M 4.0 end
}

