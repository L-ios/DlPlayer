package com.tpw.homeshell.lifecenter;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.FrameLayout;

//import com.tpw.ams.tyid.TYIDConstants;
import com.tpw.homeshell.Launcher;
import com.tpw.homeshell.LauncherApplication;
import com.tpw.homeshell.R;


class CardBridge {
    private static final boolean isSupportGlobalSearch = true;

    private static final String TAG = "homeshell/CardBridge";
    private Context mNativeContext = null;
    private Class<?> mClass = null;
    private Object maObj = null;
    private Method mGetRootView = null;
    private Method mEnterApp = null;
    private Method mExitApp = null;
    private Method mIdleApp = null;
    private Method mShowCard = null;
    private Method mShowCardWithIntent = null;
    private Method mAccountChange = null;
    private Method mSetScrolling = null;
    private Method mDispatchActivityResult = null;
    private Method mLifecenterConsumed = null;
    private Method mEnableGlobalPullDown = null;
    private Method mOnCreate = null;
    private Method onDestroy = null;
    private Method mOnPause = null;
    private Method mOnResume = null;

    public CardBridge(Context context) {
        try {
            Log.d(TAG, "CardBridge start.");
            mNativeContext = LauncherApplication.mLauncher.createPackageContext("com.yunos.lifecard",
                    Context.CONTEXT_INCLUDE_CODE | Context.CONTEXT_IGNORE_SECURITY);
            mClass = Class.forName("com.yunos.lifecard.CardBridge", true, mNativeContext.getClassLoader());

            Constructor<?> con = mClass.getConstructor(Context.class, Context.class);
            maObj = con.newInstance(mNativeContext, context);

            mGetRootView = mClass.getDeclaredMethod("getRootView");

            mEnterApp = mClass.getDeclaredMethod("enterApp");

            mExitApp = mClass.getDeclaredMethod("exitApp");

            mIdleApp = mClass.getDeclaredMethod("idleApp");

            mShowCard = mClass.getDeclaredMethod("showCard", String.class);

            mShowCardWithIntent = mClass.getDeclaredMethod("showCardWithIntent", String.class,Intent.class);

            mAccountChange = mClass.getDeclaredMethod("accountChange", boolean.class);

            mSetScrolling = mClass.getDeclaredMethod("setScrolling", boolean.class);
            mDispatchActivityResult = mClass.getDeclaredMethod("dispatchActivityResult", int.class, int.class,
                    Intent.class);

            mLifecenterConsumed = mClass.getDeclaredMethod("isLifecenterConsumed");

            if (isSupportGlobalSearch) {
                mEnableGlobalPullDown = mClass.getDeclaredMethod("isEnableGlobalPullDown");
            }

            mOnCreate = mClass.getDeclaredMethod("onCreate");
            mOnResume = mClass.getDeclaredMethod("onResume");
            mOnPause = mClass.getDeclaredMethod("onPause");
            onDestroy = mClass.getDeclaredMethod("onDestroy");

            Log.d(TAG, "CardBridge finished.");
        } catch (Exception e) {
            Log.e(TAG, "Failed in CardBridge : " + e.getMessage());
        }
    }

    public View getRootView() {
        Log.d(TAG, "getRootView start.");

        View view = null;
        if (mGetRootView == null) {
            Log.e(TAG, "mGetRootView is null");
            return null;
        }

        try {
            view = (View) mGetRootView.invoke(maObj);
        } catch (Exception e) {
            Log.e(TAG, "Failed in getRootView : " + e.getMessage());
        }

        Log.d(TAG, "getRootView finish.");

        return view;
    }

    public void enterApp() {
        Log.d(TAG, "enterApp start.");

        if (mEnterApp == null) {
            Log.e(TAG, "Failed in enterApp : mEnterApp is null.");
            return;
        }

        try {
            mEnterApp.invoke(maObj);
        } catch (Exception e) {
            Log.e(TAG, "Failed in enterApp : " + e.getMessage());
        }

        Log.d(TAG, "enterApp finished.");
    }

    public void exitApp() {
        Log.d(TAG, "exitApp start.");
        if (mExitApp == null) {
            Log.e(TAG, "Failed in exitApp : mExitApp is null.");
            return;
        }

        try {
            mExitApp.invoke(maObj);
        } catch (Exception e) {
            Log.e(TAG, "Failed in exitApp : " + e.getMessage());
        }

        Log.d(TAG, "exitApp finish.");
    }

    public void idleApp() {
        Log.d(TAG, "idleApp start.");
        if (mIdleApp == null) {
            Log.e(TAG, "Failed in exitApp : mExitApp is null.");
            return;
        }

        try {
            mIdleApp.invoke(maObj);
        } catch (Exception e) {
            Log.e(TAG, "Failed in idleApp : " + e.getMessage());
        }

        Log.d(TAG, "idleApp finish.");
    }


    public void showCard(String card) {
        Log.d(TAG, "ShowCard  start.");
        if (mShowCard == null) {
            Log.e(TAG, "Failed in showCard : mShowCard is null.");
            return;
        }

        try {
            mShowCard.invoke(maObj, card);
        } catch (Exception e) {
            Log.e(TAG, "Failed in showCard : " + e.getMessage());
        }

        Log.d(TAG, "ShowCard finished.");
    }

    public void showCardWithIntent(String card,Intent intent) {
        Log.d(TAG, "showCardWithIntent  start.");
        if (mShowCardWithIntent == null) {
            Log.e(TAG, "Failed in showCardWithIntent : mShowCardWithIntent is null. downgrade use old showCard");
            showCard(card);
            return;
        }

        try {
            mShowCardWithIntent.invoke(maObj, card,intent);
        } catch (Exception e) {
            Log.e(TAG, "Failed in showCardWithIntent : " + e.getMessage());
        }
        Log.d(TAG, "showCardExtras finished.");
    }

    public void accountChange(boolean login) {
        Log.d(TAG, "accountChange start.");

        if (mAccountChange == null) {
            return;
        }

        try {
            mAccountChange.invoke(maObj, login);
        } catch (Exception e) {
            Log.e(TAG, "Failed in accountChange : " + e.getMessage());
        }

        Log.d(TAG, "mAccountChange finish.");
    }

    public void setScrolling(boolean scrolling) {
        Log.d(TAG, "setScrolling start.");

        if (mSetScrolling == null) {
            return;
        }

        try {
            mSetScrolling.invoke(maObj, scrolling);
        } catch (Exception e) {
            Log.e(TAG, "Failed in setScrolling : " + e.getMessage());
        }

        Log.d(TAG, "setScrolling finish.");
    }

    public void dispatchActivityResult(final int requestCode, final int resultCode, final Intent intent) {
        Log.d(TAG, "dispatchActivityResult start.");

        if (mDispatchActivityResult == null) {
            return;
        }

        try {
            mDispatchActivityResult.invoke(maObj, requestCode, resultCode, intent);
        } catch (IllegalAccessException e) {
            Log.e(TAG, "dispatchActivityResult error :", e);
        } catch (InvocationTargetException e) {
            Log.e(TAG, "dispatchActivityResult :", e);
        }

        Log.d(TAG, "dispatchActivityResult end.");
    }

    public boolean isLifecenterConsumed() {
        if (mLifecenterConsumed == null) {
            return false;
        }

        boolean consumed = false;

        try {
            consumed = (Boolean) mLifecenterConsumed.invoke(maObj);
        } catch (Exception e) {
            Log.e(TAG, "Failed in isLifecenterConsumed : " + e.getMessage());
        }

        return consumed;
    }

    public boolean isEnableGlobalPullDown() {
        if (mEnableGlobalPullDown == null) {
            return false;
        }

        boolean enable = false;

        try {
            enable = (Boolean) mEnableGlobalPullDown.invoke(maObj);
        } catch (Exception e) {
            Log.e(TAG, "Failed in mEnableGlobalPullDown : " + e.getMessage());
        }

        return enable;
    }

    public void onResume() {
        if (mOnResume == null) {
            return ;
        }

        try {
            mOnResume.invoke(maObj);
        } catch (Exception e) {
            Log.e(TAG, "Failed in mOnResume : " + e.getMessage());
        }

        return ;
    }

    public void onPause() {
        if (mOnPause == null) {
            return ;
        }

        try {
            mOnPause.invoke(maObj);
        } catch (Exception e) {
            Log.e(TAG, "Failed in mOnPause : " + e.getMessage());
        }

        return ;
    }

    public void onCreate() {
        if (mOnCreate == null) {
            return ;
        }

        try {
            mOnCreate.invoke(maObj);
        } catch (Exception e) {
            Log.e(TAG, "Failed in mOnCreate : " + e.getMessage());
        }

        return ;
    }

    public void onDestroy() {
        if (onDestroy == null) {
            return ;
        }

        try {
            onDestroy.invoke(maObj);
        } catch (Exception e) {
            Log.e(TAG, "Failed in onDestroy : " + e.getMessage());
        }

        return ;
    }
}

public class LifeCenterHostView extends ViewPager {
    private static final String TAG = "LifeCenterHostView";
    private static final String DEBUG_TAG = "LifeCenter";
    private static final int KEY_HOME_LIFECENTER = 1001;

    private final String DELETE_ACCOUNT_ACTION = "com.tpw.xiaoyunmi.action.DELETE_ACCOUNT";
    private final String AYUN_LOGIN_BROADCAST_ACTION = "com.tpw.xiaoyunmi.action.AYUN_LOGIN_BROADCAST";
    private final String UPDATE_ACCOUNT_ACTION = "com.tpw.xiaoyunmi.action.UPDATE_ACCOUNT";
    private final String RECEIVE_SYNC_NOTIFY_ACTION = "com.tpw.action.RECEIVE_SYNC_NOTIFY";

    public int LIFECENTER_PAGE;
    public int HOME_PAGE;

    private ViewGroup mLifeCenterView;
    private View mLauncherView;
    private Handler mHandler = new Handler();

    private List<View> mViewList = new ArrayList<View>();
    private ViewPagerAdapter mVpAdapter;
    private float mLastMotionX;
    private float mLastMotionY;
    private float mLastMotionXRemainder;
    private float mLastMotionYRemainder;
    //private boolean mIsMoveToLeftEdge = false;
    private boolean mIsMoveInHomePage = false;
    private boolean mIsMoveToLifeCenter = false;
    private boolean mInWorkSpace = true;

    private boolean mIsFullLifeCenterPageShowed = false;
    private Launcher mLauncher;
    private boolean mLastIsMoveInHomePage = false;

    private boolean mIsRtl = false;
    private int mSlot = 0;
    private float mLastTX;
    private float mTX;
    private int mTouchSlop;
    private boolean mIsInHideseat = false;
    private boolean mIsUpdated = false;

    // add this flag to decide who will resolve the event.
    // true means the web item will eat the event, otherwise native will.
    //private boolean mIsInDomRect = false;

    private CardBridge mBridge = null;
    private int mLastPage;

    private final BroadcastReceiver AppInstallReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent == null || intent.getData() == null) {
                Log.e(TAG, "onReceive intent is null.");
                return;
            }

            String action = intent.getAction();
            if (!Intent.ACTION_PACKAGE_REPLACED.equals(action)) {
                return;
            }

            String pkgName = intent.getData().getSchemeSpecificPart();
            Log.d(TAG, "onReceive pkgName : " + pkgName);
            if (!"com.yunos.lifecard".equals(pkgName)) {
                return;
            }

            boolean isInWorkspace = getInWorkSpace();
            Log.d(TAG, "onReceive need update : " + isInWorkspace);

            mIsUpdated = true;
            if (!isInWorkspace) {
                reInit();
            }
        }
    };


    private final BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();
            Log.d(TAG, "onReceive action : " + action);

            if (DELETE_ACCOUNT_ACTION.equals(action)) {
                mBridge.accountChange(false);
                clearSyncNotifications(context);

                //boolean isClearData = intent.getBooleanExtra(TYIDConstants.CLEAR_APP_DATA_KEY, false);
                //Log.d(TAG, "onReceive isClearData : " + isClearData);

            }  else if (AYUN_LOGIN_BROADCAST_ACTION.equals(action)) {
                mBridge.accountChange(true);
            }
        }

        private void clearSyncNotifications(Context context) {
            Log.d(TAG, "clearSyncNotifications");

            // NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            // manager.cancel(NoteSyncNotification.NOTIFICATION_TAG,NoteSyncNotification.NOTIFICATION_ID);
        }
    };

    public LifeCenterHostView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public LifeCenterHostView(Context context) {
        this(context, null);
    }

    public void setInWorkSpace(boolean flag) {
        mInWorkSpace = flag;
    }

    public boolean getInWorkSpace() {
        return mInWorkSpace;
    }

    public void setFullLifeCenterPageShowed(boolean flag) {
        //mIsFullLifeCenterPageShowed = flag;
    }

    public void setIsMoveInHomePage(boolean flag) {
        mIsMoveInHomePage = flag;
    }

    public View getLauncherView() {
        return mLauncherView;
    }

    public boolean isEnableGlobalPullDown() {
        if (getCurrentItem() == LIFECENTER_PAGE) {
            return mBridge.isEnableGlobalPullDown();
        }

        return true; // homeshell implement isEnableGlobalPullDown;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (mBridge.isLifecenterConsumed()) {
            Log.d(DEBUG_TAG, "onIntercept lifecenter consumed.");
            return false;
        }

        int action = ev.getAction() & MotionEvent.ACTION_MASK;
        switch (action) {
            case MotionEvent.ACTION_DOWN :
                mLastMotionY = ev.getRawY();
                mLastMotionX = ev.getRawX();
                mLastMotionXRemainder = mLastMotionYRemainder = 0;
                mIsMoveToLifeCenter = false;
                //mIsMoveToLeftEdge = false;
                mIsInHideseat = isInHideseat((int) mLastMotionX, (int) mLastMotionY);
                mIsFullLifeCenterPageShowed = getCurrentItem() == LIFECENTER_PAGE;
                // Log.d(DEBUG_TAG, "Down mIsFullLifeCenterPageShowed : " + mIsFullLifeCenterPageShowed +
                //                   " mIsMoveInHomePage : " + mIsMoveInHomePage +
                //                   " mLastIsMoveInHomePage : " + mLastIsMoveInHomePage);
                break;
            case MotionEvent.ACTION_CANCEL :
                Log.d(DEBUG_TAG, "onIntercept cancel.");
                return false;
        }

        if (mIsFullLifeCenterPageShowed) {
            return super.onInterceptTouchEvent(ev);
        }

        if (isMatchCondition(mIsRtl)) {
            int numPoints = ev.getPointerCount();

            if (numPoints > 1) {
                Log.d(DEBUG_TAG, "numPoints : " + numPoints);
                return false;
            }

            switch (action) {
                case MotionEvent.ACTION_DOWN :
                    break;
                case MotionEvent.ACTION_MOVE :
                    final float x = ev.getRawX();
                    final float y = ev.getRawY();
                    final float deltaX = mLastMotionX + mLastMotionXRemainder - x;
                    final float deltaY = mLastMotionY + mLastMotionYRemainder - y;
                    mLastMotionXRemainder = deltaX - (int) deltaX;
                    mLastMotionYRemainder = deltaY - (int) deltaY;

                    if (mIsMoveToLifeCenter) {
                        boolean flag = super.onInterceptTouchEvent(ev);
                        Log.d(DEBUG_TAG, "move to lifecenter flag : " + flag);
                        return true;
                    }

                    boolean isMovetoLifeCenter = false;
                    if (Math.abs(deltaX) > Math.abs(deltaY)) {
                        isMovetoLifeCenter = !mIsRtl ? deltaX < -mTouchSlop / 2 : deltaX > mTouchSlop / 2;
                    }

                    if (isMovetoLifeCenter
                            && !mLauncher.isAllAppShowed()
                            && !mLauncher.getDragController().isDragging()
                            && !mLauncher.isGadgetCardShowing()
                            && !mLauncher.isEditMode()) {
                        mLastMotionX = x;
                        mIsMoveToLifeCenter = true;
                        mLastIsMoveInHomePage = false;

                        ev.setAction(MotionEvent.ACTION_DOWN);
                        boolean flag = super.onInterceptTouchEvent(ev);

                        // Log.d(DEBUG_TAG, "begin MoveToLifeCenter flag : " + flag);
                        // this is return true for viewpager onTouchEvent process event at once.
                        return true;
                    }
                    break;
                case MotionEvent.ACTION_POINTER_UP :
                case MotionEvent.ACTION_UP :
                case MotionEvent.ACTION_CANCEL :
                    Log.d(DEBUG_TAG, "set mIsMoveToLifeCenter set false.");
                    mIsMoveToLifeCenter = false;
            }
        }

        if (mInWorkSpace) {
            // Log.d(DEBUG_TAG, "mInWorkSpace");
            return false;
        }

        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        int action = ev.getAction();
        if (mBridge.isLifecenterConsumed()) {
            Log.d(DEBUG_TAG, "onTouchEvent life center consumed : " + action);
            return false;
        }

        if (mLauncher.isGadgetCardShowing()) {
            Log.d(DEBUG_TAG, "onTouchEvent GagetCadShowing : " + action);
            return false;
        }

        mTX = ev.getRawX();
        if (action == MotionEvent.ACTION_DOWN) {
            mLastTX = mTX;
        }

        float dx = mTX - mLastTX;
        mLastTX = mTX;

        if (dx > 0) {
            int scrollX = mLauncher.getWorkspace().getScrollX();
            if ((scrollX - dx) < 0) {
                setIsMoveInHomePage(false);
                mLauncher.getWorkspace().setScrollX(0);
            }
        }

        if (mIsMoveInHomePage) {
            if (!mLastIsMoveInHomePage) {
                ev.setAction(MotionEvent.ACTION_DOWN);
                mLauncher.getWorkspace().onTouchEvent(ev);
            }
            ev.setAction(action);
            mLastIsMoveInHomePage = true;
            mLauncher.getWorkspace().onTouchEvent(ev);
            // Log.d(DEBUG_TAG, "onTouchEvent mIsMoveInHomePage is true : " + action);
            if (action != MotionEvent.ACTION_UP && action != MotionEvent.ACTION_CANCEL) {
                return true;
            } 
        }

        mLastIsMoveInHomePage = mIsMoveInHomePage;

        try {
            boolean flag = super.onTouchEvent(ev);
            return flag;
        } catch (IllegalArgumentException e) {
            Log.e(DEBUG_TAG, "Failed in onTounchEvent : " + e.getMessage() + " action : " + action);
            return true;
        }
    }

    // do not process KeyEvent.KEYCODE_DPAD_LEFT KEYCODE_DPAD_RIGHT keyevent.
    @Override
    public boolean arrowScroll(int direction) {
        return false;
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        boolean consumed = super.dispatchKeyEvent(event);
        if (getCurrentPage() != LIFECENTER_PAGE) {
            return consumed;
        }

        if (!consumed) {
            int action = event.getAction();
            int keyCode = event.getKeyCode();
            if (action == KeyEvent.ACTION_DOWN) {
                switch (keyCode) {
                    case KeyEvent.KEYCODE_HOME :
                    case KeyEvent.KEYCODE_BACK :
                        mLastPage = 0;
                        setCurrentItem(HOME_PAGE, true);
                        consumed = true;
                        break;
                }
            } else if (action == KeyEvent.ACTION_UP) {
                switch (keyCode) {
                    case KeyEvent.KEYCODE_HOME :
                    case KeyEvent.KEYCODE_BACK :
                        consumed = true;
                        break;
                }
            }
        }

        Log.d(DEBUG_TAG, "dispatchKeyEvent consumed : " + consumed);
        return consumed;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        Log.d(TAG, "onAttachedToWindow register broadcast.");

        IntentFilter filter = new IntentFilter();

        filter.addAction(DELETE_ACCOUNT_ACTION);
        filter.addAction(AYUN_LOGIN_BROADCAST_ACTION);
        filter.addAction(UPDATE_ACCOUNT_ACTION);
        filter.addAction(RECEIVE_SYNC_NOTIFY_ACTION);
        getContext().registerReceiver(mIntentReceiver, filter,  "com.tpw.account.permission.SEND_MANAGE_DATA", null);

        IntentFilter installFilter = new IntentFilter();
        installFilter.addAction("android.intent.action.PACKAGE_REPLACED");
        installFilter.addDataScheme("package");
        getContext().registerReceiver(AppInstallReceiver, installFilter);
    }

    @Override
    protected void onDetachedFromWindow() {
        Log.d(TAG, "onDetachedFromWindow unregister broadcast.");
        getContext().unregisterReceiver(mIntentReceiver);
        getContext().unregisterReceiver(AppInstallReceiver);

        super.onDetachedFromWindow();
    }

    private void init(Context context) {
        mLifeCenterView = (ViewGroup) View.inflate(context, R.layout.life_center_layout, null);

        mLauncher = LauncherApplication.mLauncher;
        mTouchSlop = ViewConfiguration.get(LauncherApplication.getContext()).getScaledTouchSlop();
        mSlot = LauncherApplication.getScreenWidth() / 8;

        mIsRtl = getResources().getConfiguration().getLayoutDirection() == LAYOUT_DIRECTION_RTL;

        LIFECENTER_PAGE = !mIsRtl ? 0 : 1;
        HOME_PAGE = !mIsRtl ? 1 : 0;

        mBridge = new CardBridge(getContext());
        mLifeCenterView.addView(mBridge.getRootView());

        mLauncherView = (FrameLayout) View.inflate(context, R.layout.lifecard_launcher_content, null);

        mViewList.clear();
        if (!mIsRtl) {
            mViewList.add(mLifeCenterView);
            mViewList.add(mLauncherView);
        } else {
            mViewList.add(mLauncherView);
            mViewList.add(mLifeCenterView);
        }

        mVpAdapter = new ViewPagerAdapter(mViewList);
        setAdapter(mVpAdapter);

        setCurrentPage(HOME_PAGE, false);
    }

    private AlertDialog mDialog;
    protected void updateDialog() {
        Resources res = getResources();
        res.getString(R.string.uninstall_app_confirm);
        Builder builder = new AlertDialog.Builder(mLauncher).setTitle("")
                .setMessage(res.getString(R.string.lifecenter_update_msg))
                .setPositiveButton(res.getString(R.string.confirm_btn_label), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        mIsUpdated = false;
                        android.os.Process.killProcess(android.os.Process.myPid());
                    }
                }).setNegativeButton(res.getString(R.string.cancel_btn_label), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                    }
                });

        mDialog = builder.create();
        mDialog.setOnDismissListener(new OnDismissListener() {

            @Override
            public void onDismiss(DialogInterface dialog) {
                mDialog = null;
            }
        });

        mDialog.show();
    }

    private void dismissDialog() {
        if (mDialog != null) {
            mDialog.dismiss();
            mDialog = null;
        }
    }

    private void reInit() {
        if(!getInWorkSpace()) {
            updateDialog();
        }
    }

    public boolean isInHideseat(int x, int y) {
        boolean isInHideseat = false;

        if (mLauncher.isHideseatShowing()) {
            int loc[] = new int[2];
            View hideseat = mLauncher.getHideseat();
            hideseat.getLocationInWindow(loc);

            if (x > loc[0] && x < loc[0] + hideseat.getWidth()
                && y > loc[1] && y < loc[1] + hideseat.getHeight()) {
                isInHideseat = true;
            }
        }

        Log.d(TAG, "isInHideseat : " + isInHideseat);
        return isInHideseat;
    }

    public int getCurrentPage() {
        return getCurrentItem();
    }

    public void setCurrentPage(int page, boolean smoothScroll) {
        setCurrentItem(page, smoothScroll);
    }

    public boolean isLayoutRtl() {
        return mIsRtl;
    }

    public boolean isMatchCondition(boolean isRtl) {
        if (!mInWorkSpace) {
            Log.d(DEBUG_TAG, "isMatchCondition mInWorkSpace is false.");
            return false;
        }

        if (mLauncher.getCurrentWorkspaceScreen() != 0) {
            // Log.d(DEBUG_TAG, "isMatchCondition getCurrentWorkspaceScreen is false.");
            return false;
        }

        if (mLauncher.isInEditScreenMode()) {
            Log.d(DEBUG_TAG, "isMatchCondition isInEditScreenMode is false.");
            return false;
        }

        if (mLauncher.getWorkspace().getVisibility() != View.VISIBLE) {
            Log.d(DEBUG_TAG, "isMatchCondition workspace visibility is false.");
            return false;
        }

        if (mLauncher.getWorkspace().getOpenFolder() != null) {
            Log.d(DEBUG_TAG, "isMatchCondition getOpenFolder is false.");
            return false;
        }

        if (mIsInHideseat) {
            Log.d(DEBUG_TAG, "isMatchCondition mIsInHideseat is false.");
            return false;
        }

        int scrollX = mLauncher.getWorkspace().getScrollX();
        boolean isMatched = false;
        if (!isRtl) {
            isMatched = scrollX < mSlot;
        } else {
            View page = mLauncher.getWorkspace().getChildAt(0);
            isMatched = scrollX > page.getLeft() - mSlot;
        }

        return  isMatched;
    }

    public void enterApp() {
        if(mIsUpdated) {
            updateDialog();
        }

        setFocusable(true);
        requestFocus();
        mBridge.enterApp();
    }

    public void exitApp() {
        mBridge.exitApp();
    }

    public void idleApp() {
        mBridge.idleApp();
    }

    public void showCard(String card) {
        mBridge.showCard(card);
    }

    public void showCard(String card,Intent intent) {
        mBridge.showCardWithIntent(card,intent);
    }

    public void accountChange(boolean login) {
        mBridge.accountChange(login);
    }

    public void setScrolling(boolean scrolling) {
        mBridge.setScrolling(scrolling);
    }


    public void dispatchHome(boolean alreadyOnHome) {
        long now = SystemClock.uptimeMillis();
        int key_home = alreadyOnHome ? KeyEvent.KEYCODE_HOME:KEY_HOME_LIFECENTER;
        Log.d(TAG, "dispatchHome" + key_home);
        final KeyEvent keyDown = new KeyEvent(now, now, KeyEvent.ACTION_DOWN, key_home, 0);
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                dispatchKeyEvent(keyDown);
            }
        }, 0);

        dismissDialog();
    }

    public void doCreate() {
        mBridge.onCreate();
    }

    public void doDestroy() {
        mBridge.onDestroy();
    }

    public void doPause() {
        mBridge.onPause();
    }

    public void doResume() {
        mBridge.onResume();
    }

    public void dispatchActivityResult(final int requestCode, final int resultCode, final Intent intent) {
        Log.d(TAG, "dispatchActivityResult");
        mBridge.dispatchActivityResult(requestCode, resultCode, intent);
    }

    public void setLastPage(int page){
        mLastPage = page;
    }

    public int getLastPage(){
        return mLastPage ;
    }
}
