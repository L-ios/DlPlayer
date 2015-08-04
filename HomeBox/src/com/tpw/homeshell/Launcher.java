
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

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

//import tpw.aml.FancyDrawable;
//import tpw.v3.gadget.GadgetInfo;
//import tpw.v3.gadget.GadgetView;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.ActivityOptions;
import android.app.Service;
import android.app.WallpaperManager;
import android.appwidget.AppWidgetHostView;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ComponentCallbacks2;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.ServiceConnection;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.IPackageDeleteObserver;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.os.StrictMode;
import android.os.SystemClock;
import android.provider.Settings;
import android.speech.RecognizerIntent;
import android.text.Selection;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.method.TextKeyListener;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.HapticFeedbackConstants;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.DecelerateInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.Advanceable;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.tpw.homeshell.AppDownloadManager.AppDownloadStatus;
import com.tpw.homeshell.CellLayout.LayoutParams;
import com.tpw.homeshell.DropTarget.DragObject;
import com.tpw.homeshell.FolderIcon.FolderRingAnimator;
import com.tpw.homeshell.LauncherSettings.Favorites;
import com.tpw.homeshell.animation.FlipAnimation;
import com.tpw.homeshell.appfreeze.AppFreezeUtil;
import com.tpw.homeshell.appgroup.AppGroupManager;
import com.tpw.homeshell.favorite.RecommendTask;
import com.tpw.homeshell.icon.BubbleTextView;
import com.tpw.homeshell.icon.IconManager;
import com.tpw.homeshell.lifecenter.LifeCenterHost;
import com.tpw.homeshell.lifecenter.LifeCenterHostView;
import com.tpw.homeshell.model.LauncherModel;
import com.tpw.homeshell.screenmanager.ScreenManager;
import com.tpw.homeshell.setting.HomeShellSetting;
import com.tpw.homeshell.themeutils.ThemeUtils;
import com.tpw.homeshell.utils.NotificationPriorityHelper;
import com.tpw.homeshell.utils.PrivacySpaceHelper;
import com.tpw.homeshell.utils.ToastManager;
import com.tpw.homeshell.utils.Utils;
import com.tpw.homeshell.views.DropDownDialog;
import com.tpw.homeshell.globalsearch.LauncherContainer;
import android.view.animation.LinearInterpolator;

import commonlibs.utils.ACA;
import android.os.SystemProperties;//added by qinjinchuan topwise for supporting direct page turning

import com.tpw.homeshell.editmode.EffectsPreviewAdapter;
import com.tpw.homeshell.editmode.PreviewContainer;
import com.tpw.homeshell.editmode.PreviewList;
import com.tpw.homeshell.widgetpage.WidgetPageManager;

/**
 * Default launcher application.
 */
public final class Launcher extends Activity
        implements View.OnClickListener, OnLongClickListener, LauncherModel.Callbacks,
                   View.OnTouchListener {
    private Set<View> currentViews = new HashSet<View>();
    public static boolean mIsAgedMode = false;
    static final String TAG = "Launcher";
    static final String ALARM_ALERT_ACTION = "com.android.deskclock.ALARM_ALERT";
    static final boolean LOGD = false;
    public static boolean sReloadingForThemeChangeg = false;
    static final boolean PROFILE_STARTUP = false;
    static final boolean DEBUG_WIDGETS = false;
    static final boolean DEBUG_STRICT_MODE = false;
    static final boolean DEBUG_RESUME_TIME = false;

    private static final int MENU_GROUP_WALLPAPER = 1;
    private static final int MENU_WALLPAPER_SETTINGS = Menu.FIRST + 1;
    private static final int MENU_MANAGE_APPS = MENU_WALLPAPER_SETTINGS + 1;
    private static final int MENU_SYSTEM_SETTINGS = MENU_MANAGE_APPS + 1;
    private static final int MENU_HELP = MENU_SYSTEM_SETTINGS + 1;

    private static final int REQUEST_CREATE_SHORTCUT = 1;
    private static final int REQUEST_CREATE_APPWIDGET = 5;
    private static final int REQUEST_PICK_APPLICATION = 6;
    private static final int REQUEST_PICK_SHORTCUT = 7;
    private static final int REQUEST_PICK_APPWIDGET = 9;
    private static final int REQUEST_PICK_WALLPAPER = 10;

    private static final int REQUEST_BIND_APPWIDGET = 11;

    public static final int REQUEST_HOMESHELL_SETTING = 12;
    private boolean mBackFromHomeShellSetting = false;

    private static final int REQUEST_PICK_CLOUDLET = 1000;

    public static final String EXTRA_SHORTCUT_DUPLICATE = "duplicate";

    public static final int SCREEN_COUNT = 12;

    private static final String PREFERENCES = "launcher.preferences";
    public static final String PAGE_COUNT = "page_count";
    public static final String AGED_MODE_PAGE_COUNT = "aged_mode_page_count";
    public int mLastPageCount = 0;
    // To turn on these properties, type
    // adb shell setprop log.tag.PROPERTY_NAME [VERBOSE | SUPPRESS]
    static final String FORCE_ENABLE_ROTATION_PROPERTY = "launcher_force_rotate";
    static final String DUMP_STATE_PROPERTY = "launcher_dump_state";

    // The Intent extra that defines whether to ignore the launch animation
    static final String INTENT_EXTRA_IGNORE_LAUNCH_ANIMATION =
            "com.tpw.homeshell.intent.extra.shortcut.INGORE_LAUNCH_ANIMATION";

    // Type: int
    private static final String RUNTIME_STATE_CURRENT_SCREEN = "launcher.current_screen";
    // Type: int
    private static final String RUNTIME_STATE = "launcher.state";
    // Type: int
    private static final String RUNTIME_STATE_PENDING_ADD_CONTAINER = "launcher.add_container";
    // Type: int
    private static final String RUNTIME_STATE_PENDING_ADD_SCREEN = "launcher.add_screen";
    // Type: int
    private static final String RUNTIME_STATE_PENDING_ADD_CELL_X = "launcher.add_cell_x";
    // Type: int
    private static final String RUNTIME_STATE_PENDING_ADD_CELL_Y = "launcher.add_cell_y";
    // Type: boolean
    private static final String RUNTIME_STATE_PENDING_FOLDER_RENAME = "launcher.rename_folder";
    // Type: long
    private static final String RUNTIME_STATE_PENDING_FOLDER_RENAME_ID = "launcher.rename_folder_id";
    // Type: int
    private static final String RUNTIME_STATE_PENDING_ADD_SPAN_X = "launcher.add_span_x";
    // Type: int
    private static final String RUNTIME_STATE_PENDING_ADD_SPAN_Y = "launcher.add_span_y";
    // Type: parcelable
    private static final String RUNTIME_STATE_PENDING_ADD_WIDGET_INFO = "launcher.add_widget_info";

    /** The different states that Launcher can be in. */
    private enum State { NONE, WORKSPACE, APPS_CUSTOMIZE, APPS_CUSTOMIZE_SPRING_LOADED };
    private State mState = State.WORKSPACE;
    private AnimatorSet mStateAnimation;
    private AnimatorSet mDividerAnimator;

    public static final int APPWIDGET_HOST_ID = 1024;
    private static final int EXIT_SPRINGLOADED_MODE_SHORT_TIMEOUT = 300;
    private static final int EXIT_SPRINGLOADED_MODE_LONG_TIMEOUT = 600;
    
    private static final Object sLock = new Object();
    private static int sScreen = ConfigManager.getDefaultScreen();

    // How long to wait before the new-shortcut animation automatically pans the workspace
    private static int NEW_APPS_ANIMATION_INACTIVE_TIMEOUT_SECONDS = 10;

    private final BroadcastReceiver mCloseSystemDialogsReceiver
            = new CloseSystemDialogsIntentReceiver();
    private final BroadcastReceiver mWallpaperChangedReceiver
            = new WallpaperChangedIntentReceiver();
    private final ContentObserver mWidgetObserver = new AppWidgetResetObserver();

    private LayoutInflater mInflater;
    FolderUtils mFolderUtils = new FolderUtils();
    private UnlockAnimation mUnlockAnimation;
    LauncherMotionHelper mMotionHelper;
    private FlipAnimation mFlipAnim;

    private Workspace mWorkspace;
    private View mQsbDivider;
    private View mDockDivider;
    private View mLauncherView;
    private DragLayer mDragLayer;
    private DragController mDragController;
    private Folder mFolder;
    private GestureLayer mGestureLayer;

    private AppWidgetManager mAppWidgetManager;
    private LauncherAppWidgetHost mAppWidgetHost;

    private ItemInfo mPendingAddInfo = new ItemInfo();
    private AppWidgetProviderInfo mPendingAddWidgetInfo;

    private int[] mTmpAddItemCellCoordinates = new int[2];

    private FolderInfo mFolderInfo;

    private Hotseat mHotseat;
    private Hideseat mHideseat;
    private CustomHideseat mCustomHideseat;
    private View mAllAppsButton;

    private SearchDropTargetBar mSearchDropTargetBar;
    private AppsCustomizeTabHost mAppsCustomizeTabHost;
    private AppsCustomizePagedView mAppsCustomizeContent;
    private boolean mAutoAdvanceRunning = false;

    private Bundle mSavedState;
    // We set the state in both onCreate and then onNewIntent in some cases, which causes both
    // scroll issues (because the workspace may not have been measured yet) and extra work.
    // Instead, just save the state that we need to restore Launcher to, and commit it in onResume.
    private State mOnResumeState = State.NONE;

    private SpannableStringBuilder mDefaultKeySsb = null;

    private boolean mWorkspaceLoading = true;

    private boolean mPaused = true;
    private boolean mRestoring;
    private boolean mWaitingForResult;
    private boolean mOnResumeNeedsLoad;

    private ArrayList<Runnable> mOnResumeCallbacks = new ArrayList<Runnable>();

    // Keep track of whether the user has left launcher
    private static boolean sPausedFromUserAction = false;

    private Bundle mSavedInstanceState;

    private LauncherModel mModel;
    private boolean mUserPresent = true;
    private boolean mVisible = false;
    private boolean mAttached = false;

    private static LocaleConfiguration sLocaleConfiguration = null;

    private static HashMap<Long, FolderInfo> sFolders = new HashMap<Long, FolderInfo>();

    private HashMap<String, Long> mLastPressTimeOfDownloadingIcon = new HashMap<String, Long>();

    // Related to the auto-advancing of widgets
    private final int ADVANCE_MSG = 1;
    private final int mAdvanceInterval = 20000;
    private final int mAdvanceStagger = 250;
    private long mAutoAdvanceSentTime;
    private long mAutoAdvanceTimeLeft = -1;
    private HashMap<View, AppWidgetProviderInfo> mWidgetsToAdvance =
        new HashMap<View, AppWidgetProviderInfo>();

    // Determines how long to wait after a rotation before restoring the screen orientation to
    // match the sensor state.
    private final int mRestoreScreenOrientationDelay = 500;

    private Drawable mWorkspaceBackgroundDrawable;

    private final ArrayList<Integer> mSynchronouslyBoundPages = new ArrayList<Integer>();

    public static final ArrayList<String> sDumpLogs = new ArrayList<String>();

    // We only want to get the SharedPreferences once since it does an FS stat each time we get
    // it from the context.
    private SharedPreferences mSharedPrefs;

    // Holds the page that we need to animate to, and the icon views that we need to animate up
    // when we scroll to that page on resume.
    private int mNewShortcutAnimatePage = -1;
    private ArrayList<View> mNewShortcutAnimateViews = new ArrayList<View>();
    private ImageView mFolderIconImageView;
    private Bitmap mFolderIconBitmap;
    private Canvas mFolderIconCanvas;
    private Rect mRectForFolderAnimation = new Rect();
    static final boolean DEBUG_SHOW_FPS = false;

    private BubbleTextView mWaitingForResume;
   
    private TpwMenu mMenu = null;
    private DropDownDialog mDeleteDialog = null;

    private int mMenuKeyDownCount = 0;

    //private T9DialpadView mT9DialpadView;
    private final boolean mAppSearchMode = false;

    private boolean mEditMode;
    private boolean mOnClickValid = true;
    public  WidgetPageManager mWidgetPageManager;

    private int mStatusBarHeight;
    
    private static ArrayList<PendingAddArguments> sPendingAddList
            = new ArrayList<PendingAddArguments>();

    private static boolean sForceEnableRotation = isPropertyEnabled(FORCE_ENABLE_ROTATION_PROPERTY);

    private PageIndicatorView mIndicatorView = null;
    private boolean mShouldPlayAnimation;
    private boolean mIsTopWhenScreenOff;
    private boolean mIsResumed;
    private boolean mIsWakeUpByThreeFingerMode;
    private boolean mIsWakeUpFromOtherApp;

    private Toast mAppIsFrozenToast = null;

    private HashMap<View, PointF> mWorkspaceItemsEndPoints;
    private int mFolderXOnAnimation;
    private int mFolderYOnAnimation;

    private long mResumeTime;
    private long mFlipStartTime;
    private String mFlipCardPkgName = null;
    private String mFlipCardType = null;

    //private ArrayList<WeakReference<GadgetView>> gadgetViewList = new ArrayList<WeakReference<GadgetView> >();
    private ContentResolver mCR;
    private final HashSet<ComponentName> mEnabledServices = new HashSet<ComponentName>();
    private static final String ENABLED_NOTIFICATION_LISTENERS = "enabled_notification_listeners";

    private static class PendingAddArguments {
        int requestCode;
        Intent intent;
        long container;
        int screen;
        int cellX;
        int cellY;
    }

    private static boolean isPropertyEnabled(String propertyName) {
        return Log.isLoggable(propertyName, Log.VERBOSE);
    }

    public static final String LIFE_CENTER_PACKAGE_NAME = "com.yunos.lifecard";
    private LifeCenterHostView mLifeCenterHostView;
    private LifeCenterHost mLifeCenterHost;

    private boolean mSupportLifeCenter = false;
    private PreviewContainer mEditModeContainer ;
    private static final int EDIT_MODE_ENTER_TIME = 400;
    private static final int EDIT_MODE_EXIT_TIME = 400;
    private boolean mIsEditMode;

    private boolean mIsStarted;

    public boolean isStarted() {
        return mIsStarted;
    }
    public boolean isSupportLifeCenter(){
        return mSupportLifeCenter;
    }

    public LifeCenterHost getLifeCenterHost(){
        return mLifeCenterHost;
    }

    private void setUpLifeCenterViews() {
        mLifeCenterHost = new LifeCenterHost(mLifeCenterHostView);
    }

    @Override
    public View findViewById(int id) {
        if (mLauncherView != null) {
            return mLauncherView.findViewById(id);
        }
        return super.findViewById(id);
    }

    public void moveToDefaultScreen(boolean animate){
        mWorkspace.moveToDefaultScreen(animate);
        stopFlipWithoutAnimation();
    }
    public boolean isLifeCenterEnableSearch() {
        boolean enable = true;
        if (mLifeCenterHostView != null && !mLifeCenterHostView.getInWorkSpace()
                && !mLifeCenterHostView.isEnableGlobalPullDown()) {
            enable = false;
        }
        return enable;
    }

    public boolean blockTouchDown() {
        boolean block = false;
        if (mLifeCenterHostView != null && !mLifeCenterHostView.getInWorkSpace()
                && mLifeCenterHostView.isEnableGlobalPullDown()) {
            block = true;
        }
        return block;
    }

    public int getCurrentSreen() {
        int screen = 0;
        if (mLifeCenterHostView != null && !mLifeCenterHostView.getInWorkSpace()) {
            screen = -1;
        } else if(mWorkspace != null){
            screen = mWorkspace.getCurrentPage();
        }
        return screen;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (DEBUG_STRICT_MODE) {
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                    .detectDiskReads()
                    .detectDiskWrites()
                    .detectNetwork()   // or .detectAll() for all detectable problems
                    .penaltyLog()
                    .build());
            StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                    .detectLeakedSqlLiteObjects()
                    .detectLeakedClosableObjects()
                    .penaltyLog()
                    .penaltyDeath()
                    .build());
        }

        UserTrackerHelper.init(getApplicationContext());

        super.onCreate(savedInstanceState);
        LauncherApplication app = ((LauncherApplication)getApplication());
        mSharedPrefs = getSharedPreferences(LauncherApplication.getSharedPreferencesKey(),
                Context.MODE_PRIVATE);
        //open HARDWARE_ACCELERATED   
        final Window win = getWindow();
        win.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                        WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN);
        win.setFlags(~WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR,
                        WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR);
        win.setFlags(WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
                      WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);


        int flags = WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION | WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
        final WindowManager.LayoutParams wlp = getWindow().getAttributes();
        wlp.flags |= flags;
        getWindow().setAttributes(wlp);

        //set BubbleTextView's parameters
        BubbleTextView.setNeedReInitParams();
        mModel = app.setLauncher(this);
        mModel.getPackageUpdateTaskQueue().reset();
        mDragController = new DragController(this);
        mInflater = getLayoutInflater();

        mAppWidgetManager = AppWidgetManager.getInstance(this);
        mAppWidgetHost = new LauncherAppWidgetHost(this, APPWIDGET_HOST_ID);
        mAppWidgetHost.startListening();

        // If we are getting an onCreate, we can actually preempt onResume and unset mPaused here,
        // this also ensures that any synchronous binding below doesn't re-trigger another
        // LauncherModel load.
        mPaused = false;

        if (PROFILE_STARTUP) {
            android.os.Debug.startMethodTracing(
                    Environment.getExternalStorageDirectory() + "/launcher");
        }

        checkForLocaleChange();

        try {
            PackageManager pm = getPackageManager();
            if (pm != null) {
                try {
                    pm.getPackageInfo(LIFE_CENTER_PACKAGE_NAME, PackageManager.GET_ACTIVITIES);
                    mSupportLifeCenter = true;
                } catch (NameNotFoundException e) {
                    mSupportLifeCenter = false;
                    e.printStackTrace();
                }
            }

        } catch (Exception e) {
            Log.e(TAG, "life center error ", e );
        }
        mCR = getContentResolver();

        if (mSupportLifeCenter) {
            mLifeCenterHostView = (LifeCenterHostView)mInflater.inflate(R.layout.lifecard_launcher, null);
            mLauncherView = mLifeCenterHostView.getLauncherView();
            LauncherContainer container = new LauncherContainer(this);
            container.addView(mLifeCenterHostView,
                    LayoutParams.MATCH_PARENT,
                    LayoutParams.MATCH_PARENT);
            setContentView(container);
            mLifeCenterHostView.setCurrentPage(mLifeCenterHostView.HOME_PAGE, false);
            setupViews();
            setUpLifeCenterViews();
            mLifeCenterHost.doCreate();
        }else {
            setContentView(R.layout.launcher);
            setupViews();
        }
        boolean dataInvaild = ConfigManager.checkDataValid(getApplicationContext());
        mIsAgedMode = AgedModeUtil.isAgedMode();
        if(mIsAgedMode) {
            ((FrameLayout.LayoutParams) mIndicatorView.getLayoutParams()).bottomMargin = getResources()
                    .getDimensionPixelSize(R.dimen.workspace_cell_height_3_3);
            mIndicatorView.getLayoutParams().height = getResources()
                    .getDimensionPixelSize(R.dimen.page_indicator_height_3_3);
        }
        if(dataInvaild) {
            if(mIsAgedMode) {
                app.onAgedModeChanged(true, true, false);
            } else {
                app.onAgedModeChanged(false, true, false);
            }
        }

        registerContentObservers();

        lockAllApps();

        mSavedState = savedInstanceState;
        restoreState(mSavedState);

        // Update customization drawer _after_ restoring the states
        if (mAppsCustomizeContent != null) {
            new AsyncTask<Void, Void, ArrayList<Object>>() {
                protected ArrayList<Object> doInBackground(Void... params) {
                    ArrayList<Object> list = LauncherModel
                            .getSortedWidgetsAndShortcuts(Launcher.this);
                    return list;
                }
                protected void onPostExecute(ArrayList<Object> list) {
                    mAppsCustomizeContent.onPackagesUpdated(list);
                };
            }.execute();
         }

        if (PROFILE_STARTUP) {
            android.os.Debug.stopMethodTracing();
        }

        if (!mRestoring) {
            if (sPausedFromUserAction) {
                // If the user leaves launcher, then we should just load items asynchronously when
                // they return.
                mModel.resetLoadedState(true, true);
                mModel.startLoader(true, -1);
            } else {
                // We only load the page synchronously if the user rotates (or triggers a
                // configuration change) while launcher is in the foreground
                mModel.resetLoadedState(true, true);
                mModel.startLoader(true, mWorkspace.getCurrentPage());
            }
        }

        if (!mModel.isAllAppsLoaded()) {
            ViewGroup appsCustomizeContentParent = (ViewGroup) mAppsCustomizeContent.getParent();
            mInflater.inflate(R.layout.apps_customize_progressbar, appsCustomizeContentParent);
        }

        // For handling default keys
        mDefaultKeySsb = new SpannableStringBuilder();
        Selection.setSelection(mDefaultKeySsb, 0);

        IntentFilter filter = new IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        registerReceiver(mCloseSystemDialogsReceiver, filter);
        IntentFilter filter1 = new IntentFilter(Intent.ACTION_WALLPAPER_CHANGED);
        registerReceiver(mWallpaperChangedReceiver, filter1);

        //updateGlobalIcons();

        // On large interfaces, we want the screen to auto-rotate based on the current orientation
        unlockScreenOrientation(true);

        if (DEBUG_SHOW_FPS) {
            View fpsView = DisplayFrameRate.generateFpsView(this);
            FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(0, 0, Gravity.LEFT | Gravity.TOP);
            lp.topMargin =  50 * getResources().getDisplayMetrics().densityDpi / 240;
            lp.leftMargin = 50 * getResources().getDisplayMetrics().densityDpi / 240;

            Log.d("testpage", " lp.topMargin " + lp.topMargin);
            Log.d("testpage", " lp.leftMargin " + lp.leftMargin);

            mDragLayer.addView(fpsView, lp);
        }
	Intent intent = new Intent("com.tpw.systemui.action.CHANGE_STATUSBAR_BACKGROUND");
        sendBroadcast(intent);
        mUnlockAnimation = new UnlockAnimation(this);
        mMotionHelper = new LauncherMotionHelper(this);
        mFlipAnim = new FlipAnimation(this);
        // ##description: Added support for widget page
        mWidgetPageManager = new WidgetPageManager(this);
        
    }

    protected void onUserLeaveHint() {
        super.onUserLeaveHint();
        sPausedFromUserAction = true;
    }

    private void checkForLocaleChange() {
        if (sLocaleConfiguration == null) {
            new AsyncTask<Void, Void, LocaleConfiguration>() {
                @Override
                protected LocaleConfiguration doInBackground(Void... unused) {
                    LocaleConfiguration localeConfiguration = new LocaleConfiguration();
                    readConfiguration(Launcher.this, localeConfiguration);
                    return localeConfiguration;
                }

                @Override
                protected void onPostExecute(LocaleConfiguration result) {
                    sLocaleConfiguration = result;
                    checkForLocaleChange();  // recursive, but now with a locale configuration
                }
            }.execute();
            return;
        }

        final Configuration configuration = getResources().getConfiguration();

        final String previousLocale = sLocaleConfiguration.locale;
        final String locale = configuration.locale.toString();

        final int previousMcc = sLocaleConfiguration.mcc;
        final int mcc = configuration.mcc;

        final int previousMnc = sLocaleConfiguration.mnc;
        final int mnc = configuration.mnc;

        boolean localeChanged = !locale.equals(previousLocale) || mcc != previousMcc || mnc != previousMnc;

        if (localeChanged) {
            sLocaleConfiguration.locale = locale;
            sLocaleConfiguration.mcc = mcc;
            sLocaleConfiguration.mnc = mnc;

            //@@@@@@
            //need notify iconmanager reset

            final LocaleConfiguration localeConfiguration = sLocaleConfiguration;
            new Thread("WriteLocaleConfiguration") {
                @Override
                public void run() {
                    writeConfiguration(Launcher.this, localeConfiguration);
                }
            }.start();
        }
    }

    private static class LocaleConfiguration {
        public String locale;
        public int mcc = -1;
        public int mnc = -1;
    }

    private static void readConfiguration(Context context, LocaleConfiguration configuration) {
        DataInputStream in = null;
        try {
            in = new DataInputStream(context.openFileInput(PREFERENCES));
            configuration.locale = in.readUTF();
            configuration.mcc = in.readInt();
            configuration.mnc = in.readInt();
        } catch (FileNotFoundException e) {
            // Ignore
        } catch (IOException e) {
            // Ignore
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    // Ignore
                }
            }
        }
    }

    private static void writeConfiguration(Context context, LocaleConfiguration configuration) {
        DataOutputStream out = null;
        try {
            out = new DataOutputStream(context.openFileOutput(PREFERENCES, MODE_PRIVATE));
            out.writeUTF(configuration.locale);
            out.writeInt(configuration.mcc);
            out.writeInt(configuration.mnc);
            out.flush();
        } catch (FileNotFoundException e) {
            // Ignore
        } catch (IOException e) {
            //noinspection ResultOfMethodCallIgnored
            context.getFileStreamPath(PREFERENCES).delete();
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    // Ignore
                }
            }
        }
    }

    public DragLayer getDragLayer() {
        return mDragLayer;
    }

    public TpwMenu getmMenu() {
        return mMenu;
    }
    public GestureLayer getGestureLayer() {
        return mGestureLayer;
    }
    public boolean isDraggingEnabled() {
        // We prevent dragging when we are loading the workspace as it is possible to pick up a view
        // that is subsequently removed from the workspace in startBinding().
        return !mModel.isLoadingWorkspace();
    }

    static int getScreen() {
        synchronized (sLock) {
            return sScreen;
        }
    }

    static void setScreen(int screen) {
        synchronized (sLock) {
            sScreen = screen;
        }
    }

    /**
     * Returns whether we should delay spring loaded mode -- for shortcuts and widgets that have
     * a configuration step, this allows the proper animations to run after other transitions.
     */
    private boolean completeAdd(PendingAddArguments args) {

        if(args.container == LauncherSettings.Favorites.CONTAINER_DESKTOP) {
                mWorkspace.makesureAddScreenIndex(args.screen);
        }

        boolean result = false;
        switch (args.requestCode) {
            case REQUEST_PICK_APPLICATION:
                completeAddApplication(args.intent, args.container, args.screen, args.cellX,
                        args.cellY);
                break;
            case REQUEST_PICK_SHORTCUT:
                processShortcut(args.intent);
                break;
            case REQUEST_CREATE_SHORTCUT:
                completeAddShortcut(args.intent, args.container, args.screen, args.cellX,
                        args.cellY);
                result = true;
                break;
            case REQUEST_CREATE_APPWIDGET:
                int appWidgetId = args.intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1);
                completeAddAppWidget(appWidgetId, args.container, args.screen, null, null);
                result = true;
                break;
            case REQUEST_PICK_WALLPAPER:
                // We just wanted the activity result here so we can clear mWaitingForResult
                break;
        }
        // Before adding this resetAddInfo(), after a shortcut was added to a workspace screen,
        // if you turned the screen off and then back while in All Apps, Launcher would not
        // return to the workspace. Clearing mAddInfo.container here fixes this issue
        resetAddInfo();
        return result;
    }

    @Override
    protected void onActivityResult(
            final int requestCode, final int resultCode, final Intent data) {
        if (mSupportLifeCenter && (requestCode > REQUEST_PICK_CLOUDLET)) {
            if (mLifeCenterHostView !=  null)
                mLifeCenterHostView.dispatchActivityResult(requestCode, resultCode, data);
           return;
        }
        if (requestCode == REQUEST_HOMESHELL_SETTING) {
            mBackFromHomeShellSetting = true;
        } else {
            mBackFromHomeShellSetting = false;
        }
        if (requestCode == REQUEST_BIND_APPWIDGET) {
            int appWidgetId = data != null ?
                    data.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1) : -1;
            if (resultCode == RESULT_CANCELED) {
                completeTwoStageWidgetDrop(RESULT_CANCELED, appWidgetId);
            } else if (resultCode == RESULT_OK) {
                addAppWidgetImpl(appWidgetId, mPendingAddInfo, null, mPendingAddWidgetInfo);
            }
            return;
        }
        boolean delayExitSpringLoadedMode = false;
        boolean isWidgetDrop = (requestCode == REQUEST_PICK_APPWIDGET ||
                requestCode == REQUEST_CREATE_APPWIDGET);
        mWaitingForResult = false;

        // We have special handling for widgets
        if (isWidgetDrop) {
            int appWidgetId = data != null ?
                    data.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1) : -1;
            if (appWidgetId < 0) {
                Log.e(TAG, "Error: appWidgetId (EXTRA_APPWIDGET_ID) was not returned from the \\" +
                        "widget configuration activity.");
                completeTwoStageWidgetDrop(RESULT_CANCELED, appWidgetId);
            } else {
                completeTwoStageWidgetDrop(resultCode, appWidgetId);
            }
            return;
        }

        // The pattern used here is that a user PICKs a specific application,
        // which, depending on the target, might need to CREATE the actual target.

        // For example, the user would PICK_SHORTCUT for "Music playlist", and we
        // launch over to the Music app to actually CREATE_SHORTCUT.
        if (resultCode == RESULT_OK && mPendingAddInfo.container != ItemInfo.NO_ID) {
            final PendingAddArguments args = new PendingAddArguments();
            args.requestCode = requestCode;
            args.intent = data;
            args.container = mPendingAddInfo.container;
            args.screen = mPendingAddInfo.screen;
            args.cellX = mPendingAddInfo.cellX;
            args.cellY = mPendingAddInfo.cellY;
            //if (isWorkspaceLocked()) {
            if (mWorkspaceLoading || mWaitingForResult) {
                sPendingAddList.add(args);
            } else {
                delayExitSpringLoadedMode = completeAdd(args);
            }
        }
        mDragLayer.clearAnimatedView();
        // Exit spring loaded mode if necessary after cancelling the configuration of a widget
        exitSpringLoadedDragModeDelayed((resultCode != RESULT_CANCELED), delayExitSpringLoadedMode,
                null);
    }

    private void completeTwoStageWidgetDrop(final int resultCode, final int appWidgetId) {
        CellLayout cellLayout = (CellLayout) mWorkspace.getChildAt(mPendingAddInfo.screen);

        Runnable onCompleteRunnable = null;
        int animationType = 0;

        AppWidgetHostView boundWidget = null;
        if (resultCode == RESULT_OK) {
            animationType = Workspace.COMPLETE_TWO_STAGE_WIDGET_DROP_ANIMATION;
            final AppWidgetHostView layout = mAppWidgetHost.createView(this, appWidgetId,
                    mPendingAddWidgetInfo);
            boundWidget = layout;
            onCompleteRunnable = new Runnable() {
                @Override
                public void run() {
                    completeAddAppWidget(appWidgetId, mPendingAddInfo.container,
                            mPendingAddInfo.screen, layout, null);
                    exitSpringLoadedDragModeDelayed((resultCode != RESULT_CANCELED), false,
                            null);
                }
            };
        } else if (resultCode == RESULT_CANCELED) {
            animationType = Workspace.CANCEL_TWO_STAGE_WIDGET_DROP_ANIMATION;
            onCompleteRunnable = new Runnable() {
                @Override
                public void run() {
                    exitSpringLoadedDragModeDelayed((resultCode != RESULT_CANCELED), false,
                            null);
                }
            };
        }


        if (mDragLayer.getAnimatedView() != null) {
            mWorkspace.animateWidgetDrop(mPendingAddInfo, cellLayout,
                    (DragView) mDragLayer.getAnimatedView(), onCompleteRunnable,
                    animationType, boundWidget, true);
        } else {
            // The animated view may be null in the case of a rotation during widget configuration
            onCompleteRunnable.run();
        }
    }

    public boolean isGadgetCardShowing() {
        return mFlipAnim.isShowing() || mFlipAnim.isWaiting();
    }

    @Override
    protected void onStop() {
        mIsStarted = false;
        super.onStop();
        unbindNotificationService();
        mMotionHelper.unregister();
        FirstFrameAnimatorHelper.setIsVisible(false);
        if (isHideDeleteDialog()) {
            getSearchBar().hideDropTargetBar(false);
            if (mDeleteDialog != null && mDeleteDialog.getNegetiveButton() != null) {
                mDeleteDialog.getNegetiveButton().performClick();
            }
        }
        // source code to onPause
        mWorkspace.cleanDragItemList();
        if(mMenu!=null&&mMenu.isShowing()){
            mMenu.dismiss();
        }

        if( mFlipAnim.isShowing() ){
            stopFlipWithoutAnimation();
        }

        if (mEditMode) {
            exitScreenEditModeWithoutSave();
        }

        if (mWorkspace.getOpenFolder() != null && !mWorkspace.getOpenFolder().isEditingName()) {
            closeOpenFolders(false);
        }

        if(mIsResumed)
            mUnlockAnimation.finish();
    }

    @Override
    protected void onStart() {
        // if the screen is locked when Launcher starting, That suggests it's wake up from an application.
        // then we set mIsWakeUpFromOtherApp to true, so it's will not play animation after this application stoped.
        /*if (isScreenLocked()) {
            mIsWakeUpFromOtherApp = true;
        }*/
        super.onStart();
        
        mUnlockAnimation.standby();
        mMotionHelper.register();
        if (AgedModeUtil.isAgedMode() != mIsAgedMode) {
            final Workspace workspace = mWorkspace;
            if (mWorkspace != null) {
                mWorkspace.clearReference();
                int count = workspace.getChildCount();
                for (int i = 0; i < count; i++) {
                    final CellLayout layoutParent = (CellLayout) workspace.getChildAt(i);
                    layoutParent.removeAllViewsInLayout();
                }
                //clear all celllayouts on entering ageMode
                while (mWorkspace.getChildCount() > 1) {
                    mWorkspace.removeViewAt(1);
                }
            }
            if (mWidgetsToAdvance != null) {
                mWidgetsToAdvance.clear();
            }
            if (mHotseat != null) {
                mHotseat.resetLayout();
            }
            if (mHideseat != null) {
                mHideseat.resetLayout();
            }
            Log.d(AgedModeUtil.TAG, "onStart,mIsAgedMode in Launcher is :" + mIsAgedMode
                    + ",different with the state in AgedModeUtil,call onAgedModeChanged:"
                    + !mIsAgedMode);
            mIsAgedMode = AgedModeUtil.isAgedMode();
            ((LauncherApplication) getApplication()).onAgedModeChanged(mIsAgedMode, true, true);
        }
        //FirstFrameAnimatorHelper.setIsVisible(true);
        /*if (mState == State.WORKSPACE
                && !isInEditScreenMode()
                && !mIsWakeUpFromOtherApp
                && (mIsWakeUpByThreeFingerMode || mShouldPlayAnimation)) {
            Log.d(TAG, "mIsWakeUpByThreeFingerMode="+mIsWakeUpByThreeFingerMode+",mShouldPlayAnimation="
                           +mShouldPlayAnimation);
            Log.d(TAG, "onstart to play unlock animation");
            getWorkspace().setAllItemsOfCurrentPageVisibility(View.INVISIBLE);
            getAnimationPlayer().play(new ScreenUnlockedAnimation(this));
            mShouldPlayAnimation = mIsTopWhenScreenOff = false;
        }*/
        mIsStarted = true;
    }
    private void loadEnabledServices() {
        mEnabledServices.clear();
        final String flat = Settings.Secure.getString(mCR, ENABLED_NOTIFICATION_LISTENERS);
        if (flat != null && !"".equals(flat)) {
            final String[] names = flat.split(":");
            for (int i = 0; i < names.length; i++) {
                final ComponentName cn = ComponentName.unflattenFromString(names[i]);
                if (cn != null) {
                    mEnabledServices.add(cn);
                }
            }
        }
    }
    private void saveEnabledServices() {
        StringBuilder sb = null;
        for (ComponentName cn : mEnabledServices) {
            if (sb == null) {
                sb = new StringBuilder();
            } else {
                sb.append(':');
            }
            sb.append(cn.flattenToString());
        }
        Settings.Secure.putString(mCR,
                ENABLED_NOTIFICATION_LISTENERS,
                sb != null ? sb.toString() : "");
    }
    private void bindNotificationService(){
        loadEnabledServices();
        final ComponentName cn = new ComponentName("com.tpw.homeshell", "com.tpw.homeshell.CardNotificationListenerService");
        if(!mEnabledServices.contains(cn)){
            mEnabledServices.add(cn);
            saveEnabledServices();
        }
    }
    private void unbindNotificationService(){
        final ComponentName cn = new ComponentName("com.tpw.homeshell", "com.tpw.homeshell.CardNotificationListenerService");
        if(mEnabledServices.contains(cn)){
            mEnabledServices.remove(cn);
            saveEnabledServices();
        }
    }

    @Override
    protected void onResume() {
        mResumeTime = SystemClock.uptimeMillis();

        UserTrackerHelper.pageEnter(this);
        UserTrackerHelper.entryPageBegin(UserTrackerMessage.LABEL_LAUNCHER);
        // if the screen is locked when Launcher resuming, That suggests it's wake up from an application.
        // then we set mIsWakeUpFromOtherApp to true, so it's will not play animation after this application stoped.
        /*if (isScreenLocked()) {
            mIsWakeUpFromOtherApp = true;
        }*/
        // set all items invisible and play animation
        /*if (mState == State.WORKSPACE
                && !isInEditScreenMode()
                && !mIsWakeUpFromOtherApp
                && (mIsWakeUpByThreeFingerMode || mShouldPlayAnimation)) {
            Log.d(TAG, "mIsWakeUpByThreeFingerMode="+mIsWakeUpByThreeFingerMode+",mShouldPlayAnimation="
                           +mShouldPlayAnimation);
            Log.d(TAG, "onresume to play unlock animation");
            getAnimationPlayer().play(new ScreenUnlockedAnimation(this));
            mShouldPlayAnimation = mIsTopWhenScreenOff = false;
        }*/
        long startTime = 0;
        if (DEBUG_RESUME_TIME) {
            startTime = System.currentTimeMillis();
        }
        super.onResume();
        enableGesture();//added by qinjinchuan topwise for supporting direct page turning
        bindNotificationService();
        //FancyDrawable.resumeAll();
        //GadgetView.resumeAll();
        if (mSupportLifeCenter) {
            mLifeCenterHost.doResume();
        }

        // Restore the previous launcher state
        if (mOnResumeState == State.WORKSPACE) {
            showWorkspace(false);
        } else if (mOnResumeState == State.APPS_CUSTOMIZE) {
            showAllApps(false);
        }
        mOnResumeState = State.NONE;

        // Background was set to gradient in onPause(), restore to black if in all apps.
        setWorkspaceBackground(mState == State.WORKSPACE);

        // Process any items that were added while Launcher was away
        InstallShortcutReceiver.flushInstallQueue(this);

        mPaused = false;
        sPausedFromUserAction = false;
        if (mRestoring || mOnResumeNeedsLoad) {
            mWorkspaceLoading = true;
            mModel.startLoader(true, -1);
            mRestoring = false;
            mOnResumeNeedsLoad = false;
        }
        if (mOnResumeCallbacks.size() > 0) {
            // We might have postponed some bind calls until onResume (see waitUntilResume) --
            // execute them here
            long startTimeCallbacks = 0;
            if (DEBUG_RESUME_TIME) {
                startTimeCallbacks = System.currentTimeMillis();
            }

            if (mAppsCustomizeContent != null) {
                mAppsCustomizeContent.setBulkBind(true);
            }
            for (int i = 0; i < mOnResumeCallbacks.size(); i++) {
                mOnResumeCallbacks.get(i).run();
            }
            if (mAppsCustomizeContent != null) {
                mAppsCustomizeContent.setBulkBind(false);
            }
            mOnResumeCallbacks.clear();
            if (DEBUG_RESUME_TIME) {
                Log.d(TAG, "Time spent processing callbacks in onResume: " +
                    (System.currentTimeMillis() - startTimeCallbacks));
            }
        }

        // Reset the pressed state of icons that were locked in the press state while activities
        // were launching
        if (mWaitingForResume != null) {
            // Resets the previous workspace icon press state
            mWaitingForResume.setStayPressed(false);
        }
        if (mAppsCustomizeContent != null) {
            // Resets the previous all apps icon press state
            mAppsCustomizeContent.resetDrawableState();
        }
        // It is possible that widgets can receive updates while launcher is not in the foreground.
        // Consequently, the widgets will be inflated in the orientation of the foreground activity
        // (framework issue). On resuming, we ensure that any widgets are inflated for the current
        // orientation.
        getWorkspace().reinflateWidgetsIfNecessary();

        // Again, as with the above scenario, it's possible that one or more of the global icons
        // were updated in the wrong orientation.
        //updateGlobalIcons();
        if (DEBUG_RESUME_TIME) {
            Log.d(TAG, "Time spent in onResume: " + (System.currentTimeMillis() - startTime));
        }

        //reVisibileWorkspaceItem();
        //reVisibileFolderItem();

        final Context context = getApplicationContext();
        final SharedPreferences sp = context.getSharedPreferences(
                DataCollector.PREFERENCES_CONFIG, Context.MODE_PRIVATE);
        DataCollector.getInstance(context).ensureICFileIsExist();
        final long lastCollected = sp.getLong(DataCollector.LAST_DOIC_TIME, 0);
        final long currentTime = System.currentTimeMillis();
        if (0 == lastCollected) {
            DataCollector.getInstance(context).updateSendFlag();
            new Thread() {
                public void run() {
                    try {
                        // delay 10s waiting for the IC service to start when
                        // booted
                        Thread.sleep(10000);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    if(mWorkspace != null) {
                    	UserTrackerHelper.screenStatus(mWorkspace.getChildCount());
                    	LauncherModel.sendStatus();
                    }
                }
            }.start();
        } else if (currentTime - lastCollected > DataCollector.SEVEN_DAY_MILLISECONDS) {
            sp.edit().putLong(DataCollector.LAST_DOIC_TIME, currentTime).commit();
            new Thread() {
                public void run() {
                    try {
                        // delay 10s waiting for the IC service to start when
                        // booted
                        Thread.sleep(10000);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    final int days = (int) ((currentTime - lastCollected) / (24 * 60 * 60 * 1000));
                    DataCollector.getInstance(context).doIC(context, days);
                    if (mWorkspace != null) {
                        UserTrackerHelper.screenStatus(mWorkspace.getChildCount());
                    }
                    LauncherModel.sendStatus();
                }
            }.start();
        }

        mIsResumed = true;

        mOnClickValid = true;
        mUnlockAnimation.standby();
        mWidgetPageManager.onResume();
        if (mModel != null) {
            mModel.checkInstallingState();
        }
        //solve the text input box does not automatically hidden when the applications delete box is showing
        //if you want to keep the input box of operation please be filtered
        final View v = getWindow().peekDecorView();
        try {
            if (v != null && v.getWindowToken() != null) {
                InputMethodManager imm = (InputMethodManager)getSystemService(
                        INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
            }
        } catch (RuntimeException e) {
            Log.e(TAG, e.toString(), e);
        }
        AppGroupManager.getInstance().reloadAppGroupInfosFromServer();
        
        meausreStatusBarHeight();

        mBackFromHomeShellSetting = false;
        Log.d(LIVE_WEATHER_TAG, "onResume begin");
        if(isDraggingEnabled()) {
        	OnBindParticleService();
        }
        Log.d(LIVE_WEATHER_TAG, "onResume end");
    }

    protected void reVisibileDraggedItem(ItemInfo info) {
        if(mWorkspace == null) {
            return;
        }
        View cell = mWorkspace.getDragItemFromList(info, true);
        Log.d(TAG, "sxsexe------------>reVisibileWorkspaceItem info " + info + " cell " + cell);
        if(cell == null) {
            return;
        }

        if (isContainerFolder(info.container)) {
            FolderInfo folderInfo = sFolders.get(info.container);
            if (folderInfo != null) {
                folderInfo.add(mFolder.mShortcutInfoCache);
            }
        } else if (isContainerHideseat(info.container)) {
            mHideseat.addInScreen(cell, info.container, info.screen, info.cellX, info.cellY, info.spanX, info.spanY, true);
            getHideseat().onDropCompleted(cell, null, false, true);
        } else if (isContainerHotseat(info.container)) {
            mWorkspace.addInHotseat(cell, info.container, info.screen, info.cellX, info.cellY, info.spanX, info.spanY, info.screen);
            getHotseat().onDrop(false, 0, null, cell, true);
        } else if (isContainerWorkspace(info.container)) {
            cell.setVisibility(View.VISIBLE);
            if(cell.getParent() != null) {
                CellLayout layout = (CellLayout) cell.getParent().getParent();
                if(layout != null)
                    layout.markCellsAsOccupiedForView(cell);
            }
        }

        mWorkspace.mDropTargetView = null;
    }

    public static FolderInfo findFolderInfo(long container) {
        return sFolders.get(container);
    }

    @Override
    protected void onPause() {
        String action = "homeshell_stop_to_system_ui";
        Intent intent = new Intent(action);
        getApplicationContext().sendBroadcast(intent);

        LockScreenAnimator.getInstance(this).restoreIfNeeded();

        // if the screen is locked when Launcher pausing, That suggests it's wake up from an application.
        // then we set mIsWakeUpFromOtherApp to true, so it's will not play animation after this application stoped.
        /*if (isScreenLocked()) {
            mIsWakeUpFromOtherApp = true;
            getWorkspace().setAllItemsOfCurrentPageVisibility(View.VISIBLE);
        }*/
        // NOTE: We want all transitions from launcher to act as if the wallpaper were enabled
        // to be consistent.  So re-enable the flag here, and we will re-disable it as necessary
        // when Launcher resumes and we are still in AllApps.
        UserTrackerHelper.pageLeave(this);
        UserTrackerHelper.entryPageEnd(UserTrackerMessage.LABEL_LAUNCHER);
        updateWallpaperVisibility(true);

        super.onPause();
        disableGesture();//added by qinjinchuan topwise for supporting direct page turning
        //FancyDrawable.pauseAll();
        //GadgetView.pauseAll();
        if (mSupportLifeCenter) {
            mLifeCenterHost.doPause();
        }
        mPaused = true;
        mDragController.cancelDrag();
        mDragController.resetLastGestureUpTime();
        if(!isWorkspaceLocked() && !isDragToDelete()) {
            getWorkspace().checkAndRemoveEmptyCell();
        }
        mWorkspace.cancelFlingDropDownAnimation();

        mIsWakeUpByThreeFingerMode = mIsResumed = false;

        if(CheckVoiceCommandPressHelper.PUSH_TO_TALK_SUPPORT) {
            CheckVoiceCommandPressHelper.getInstance().forceDismissVoiceCommand();
        }
        getHotseat().onPause();

        // close hideseat when lock screen
        if( isHideseatShowing() ){
            hideHideseat(false);
        }
        mWidgetPageManager.onPause();

        if (mModel != null) {
            mModel.stopCheckInstallingState();
        }

        Log.d(LIVE_WEATHER_TAG, "onPause begin");
        UnBindParticleService();
        Log.d(LIVE_WEATHER_TAG, "onPause end");
    }
    //added by qinjinchuan topwise for supporting direct page turning
    private static final int MODULE_BIT = 0;//0是桌面，1是锁屏，2是来电，3是图库，4是收音机
    private void disableGesture() {
        if (SystemProperties.get("ro.stk.gesture","0").equals("1")) {  //这个表示是否支持该功能
            int mainSwitch = Settings.System.getInt(getContentResolver(), "direct_turn_on", 0) ;//这个是总开关
            if (mainSwitch == 1) {
                int sensortekValue = Settings.System.getInt(getContentResolver(), "sensortek_enable_value", 0);
                if (sensortekValue == 1) {
                    Intent intent = new Intent();
                    intent.setAction("com.sensortek.broadcast.disable");
                    intent.putExtra("module_name_bit", MODULE_BIT);
                    sendBroadcast(intent);
                }
            }
        }
    }
    private void enableGesture() {
        if (SystemProperties.get("ro.stk.gesture","0").equals("1")) {  //这个表示是否支持该功能
            int mainSwitch = Settings.System.getInt(getContentResolver(), "direct_turn_on", 0) ;//这个是总开关
            Log.d(TAG,"enableGesture:mainSwitch="+mainSwitch);
            if (mainSwitch == 1) {
                int sensortekValue = Settings.System.getInt(getContentResolver(), "sensortek_enable_value", 0);
                Log.d(TAG,"enableGesture:sensortekValue="+sensortekValue);
                if (sensortekValue == 1) {
                    Intent intent = new Intent();
                    intent.setAction("com.sensortek.broadcast.enable");
                    intent.putExtra("module_name_bit", MODULE_BIT);
                    sendBroadcast(intent);
                }
            }
        }
    }	
    //added by qinjinchuan topwise for supporting direct page turning

    @Override
    public Object onRetainNonConfigurationInstance() {
        // Flag the loader to stop early before switching
        mModel.stopLoader();
        if (mAppsCustomizeContent != null) {
            mAppsCustomizeContent.surrender();
        }
        return Boolean.TRUE;
    }

    // We can't hide the IME if it was forced open.  So don't bother
    /*
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        if (hasFocus) {
            final InputMethodManager inputManager = (InputMethodManager)
                    getSystemService(Context.INPUT_METHOD_SERVICE);
            WindowManager.LayoutParams lp = getWindow().getAttributes();
            inputManager.hideSoftInputFromWindow(lp.token, 0, new android.os.ResultReceiver(new
                        android.os.Handler()) {
                        protected void onReceiveResult(int resultCode, Bundle resultData) {
                            Log.d(TAG, "ResultReceiver got resultCode=" + resultCode);
                        }
                    });
            Log.d(TAG, "called hideSoftInputFromWindow from onWindowFocusChanged");
        }
    }
    */

    private boolean acceptFilter() {
        final InputMethodManager inputManager = (InputMethodManager)
                getSystemService(Context.INPUT_METHOD_SERVICE);
        return !inputManager.isFullscreenMode();
    }

    private void closeOpenFolders(){
        closeOpenFolders(true);
    }
    private void closeOpenFolders(boolean anim){
        if(mWorkspace.getOpenFolder() != null) {
            Folder openFolder = mWorkspace.getOpenFolder();
            if (openFolder!=null) {
                openFolder.dismissEditingName();
//                closeFolder();
                closeFolderWithoutExpandAnimation(anim);
            }
        }
    }
    
    //lixuhui menu key enter editmode
    private boolean dealMenuAsNormal(){
        if (isSearchMode()) {
            return true;
        }
        //exit screen edit mode when open menu
        if(mEditMode) {
            exitScreenEditMode(false);
        }
        
        if(isHideseatShowing()) {
            hideHideseat(false);
        }
        if(mState == State.APPS_CUSTOMIZE) {
            showWorkspace(true);
        }
        if( mFlipAnim.isShowing() ){
            stopFlipWithoutAnimation();
        }
            
        if (CheckVoiceCommandPressHelper.PUSH_TO_TALK_SUPPORT
                && CheckVoiceCommandPressHelper.getInstance().isVoiceUIShown()){
            CheckVoiceCommandPressHelper.getInstance().forceDismissVoiceCommand();
        } 

        //end add
        if (mMenu == null) {
            Log.d(TAG, "Menu, showing menu");
            closeOpenFolders();
            mMenu = new TpwMenu(this);
            mMenu.show();
            UserTrackerHelper
                    .sendUserReport(UserTrackerMessage.MSG_ENTRY_MENU);
        } else {
            Log.d(TAG, "Menu, mMenu.isShowing()="+mMenu.isShowing());
            if (mMenu.isShowing()) {
                mMenu.dismiss();
            } else {
                closeOpenFolders();
                mMenu.show();
                UserTrackerHelper
                        .sendUserReport(UserTrackerMessage.MSG_ENTRY_MENU);
            }
        }

        return true;
    }

    private boolean dealMenuAsEditmode(){
        try {
            if (mSupportLifeCenter) {
                if (mDragLayer.isLeftPageMode()) {
                    return true;
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "life center error " , e);
        }

        if(mWorkspace.isPageMoving()){
            return true;
        }

        if(mIsEditMode){ 
            exiteEditModeAnimation();
            return true;
        }
        if( LockScreenAnimator.getInstance(this).shouldPreventGesture() ) return false;
        if( mGestureLayer.getPointerCount() > 1 ) return false;
        if (mWorkspace.iscurrWidgetPage()) return false;    //added by qinjinchuan topwise for disabling long-click-hotseat in widget page

        if (!isDraggingEnabled()) return false;
        //add toast in lock mode
        /*if (mModel.isDownloadStatus()) {
            ToastManager.makeToast(ToastManager.NOT_ALLOW_EDIT_IN_DOWNING);
            //return true;
        }*/

        Log.d(TAG,
                "onLongClick mWorkspaceLoading || mWaitingForResult || mModel.isDownloadStatus() "
                        + mWorkspaceLoading + " - " + mWaitingForResult + " - "
                        + mModel.isDownloadStatus());
        if (mWorkspaceLoading || mWaitingForResult) return false;

        closeOpenFolders();
        enterEditMode();
        return true;
    }

    @Override
    public boolean onKeyUp(int keyCode,KeyEvent event){
        if(keyCode == KeyEvent.KEYCODE_MENU){

            try {
                if (mSupportLifeCenter) {
                    if (!mLifeCenterHostView.getInWorkSpace()) {
                        Log.d(TAG, "Menu, in lifecenter");
                        mMenuKeyDownCount--;
                        if(mMenuKeyDownCount>0){
                            Log.d(TAG, "in lifecenter, long press menu key");
                            mMenuKeyDownCount = 0;
                            return super.onKeyUp(keyCode, event);
                        }
                        return true;
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "life center error", e);
            }

            Log.d(TAG, "Menu,mMenuKeyDownCount = " + mMenuKeyDownCount);
            mMenuKeyDownCount--;
            if(mMenuKeyDownCount>0){
                mMenuKeyDownCount = 0;
                return super.onKeyUp(keyCode, event);
            }
            //lixuhui menu key enter editmode
            if(TopwiseConfig.HOMESHELL_MENU_KEY_EDIT_MODE){
                return dealMenuAsEditmode(); 
            }else{
                return dealMenuAsNormal();
            }
            //screen edit
        } else if (keyCode == KeyEvent.KEYCODE_BACK) {
            // mMenu cannot disappear correctly
            if (mMenu != null && mMenu.isShowing()) {
                mMenu.dismiss();
                return true;
            }
            if (mEditMode) {
                exitScreenEditMode(true);
                return true;
            }


              if (mAppSearchMode) {
                  exitSearchMode();
                  return true;
              }
        }

        return super.onKeyUp(keyCode, event);
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        final boolean handled = super.onKeyDown(keyCode, event);
        //long press menu key ,show menu
        if(event.getKeyCode() == KeyEvent.KEYCODE_MENU){
            /*
            if (isEditMode()) {
                exiteEditModeAnimation();
            }
            **/
            mMenuKeyDownCount++;
        }
        return handled;
    }

    private void clearTypedText() {
        mDefaultKeySsb.clear();
        mDefaultKeySsb.clearSpans();
        Selection.setSelection(mDefaultKeySsb, 0);
    }

    /**
     * Given the integer (ordinal) value of a State enum instance, convert it to a variable of type
     * State
     */
    private static State intToState(int stateOrdinal) {
        State state = State.WORKSPACE;
        final State[] stateValues = State.values();
        for (int i = 0; i < stateValues.length; i++) {
            if (stateValues[i].ordinal() == stateOrdinal) {
                state = stateValues[i];
                break;
            }
        }
        return state;
    }

    /**
     * Restores the previous state, if it exists.
     *
     * @param savedState The previous state.
     */
    private void restoreState(Bundle savedState) {
        if (savedState == null) {
            return;
        }

        State state = intToState(savedState.getInt(RUNTIME_STATE, State.WORKSPACE.ordinal()));
        if (state == State.APPS_CUSTOMIZE) {
            mOnResumeState = State.APPS_CUSTOMIZE;
        }

        int currentScreen = savedState.getInt(RUNTIME_STATE_CURRENT_SCREEN, -1);
        if (currentScreen > -1) {
            mWorkspace.setCurrentPageWhenRestore(currentScreen);
        }

        final long pendingAddContainer = savedState.getLong(RUNTIME_STATE_PENDING_ADD_CONTAINER, -1);
        final int pendingAddScreen = savedState.getInt(RUNTIME_STATE_PENDING_ADD_SCREEN, -1);

        if (pendingAddContainer != ItemInfo.NO_ID && pendingAddScreen > -1) {
            mPendingAddInfo.container = pendingAddContainer;
            mPendingAddInfo.screen = pendingAddScreen;
            mPendingAddInfo.cellX = savedState.getInt(RUNTIME_STATE_PENDING_ADD_CELL_X);
            mPendingAddInfo.cellY = savedState.getInt(RUNTIME_STATE_PENDING_ADD_CELL_Y);
            mPendingAddInfo.spanX = savedState.getInt(RUNTIME_STATE_PENDING_ADD_SPAN_X);
            mPendingAddInfo.spanY = savedState.getInt(RUNTIME_STATE_PENDING_ADD_SPAN_Y);
            mPendingAddWidgetInfo = savedState.getParcelable(RUNTIME_STATE_PENDING_ADD_WIDGET_INFO);
            mWaitingForResult = true;
            mRestoring = true;
        }


        boolean renameFolder = savedState.getBoolean(RUNTIME_STATE_PENDING_FOLDER_RENAME, false);
        if (renameFolder) {
            long id = savedState.getLong(RUNTIME_STATE_PENDING_FOLDER_RENAME_ID);
            mFolderInfo = mModel.getFolderById(this, sFolders, id);
            mRestoring = true;
        }

        /*
        // Restore the AppsCustomize tab
        if (mAppsCustomizeTabHost != null) {
            String curTab = savedState.getString("apps_customize_currentTab");
            if (curTab != null) {
                mAppsCustomizeTabHost.setContentTypeImmediate(
                        mAppsCustomizeTabHost.getContentTypeForTabTag(curTab));
                mAppsCustomizeContent.loadAssociatedPages(
                        mAppsCustomizeContent.getCurrentPage());
            }

            int currentIndex = savedState.getInt("apps_customize_currentIndex");
            mAppsCustomizeContent.restorePageForIndex(currentIndex);
        }*/
    }

    private void positionHideseat() {
        android.widget.FrameLayout.LayoutParams lp = (android.widget.FrameLayout.LayoutParams) mCustomHideseat.getLayoutParams();
        lp.bottomMargin = calcHideseatBottomMargin();
        Log.d(TAG, "positionHideseat: bottomMargin=" + lp.bottomMargin);
    }

    /**
     * Finds all the views we need and configure them properly.
     */
    private void setupViews() {
        final DragController dragController = mDragController;

        if (mLauncherView == null) {
            mLauncherView = findViewById(R.id.launcher);
        }

        mDragLayer = (DragLayer) findViewById(R.id.drag_layer);
        mWorkspace = (Workspace) mDragLayer.findViewById(R.id.workspace);
        mQsbDivider = findViewById(R.id.qsb_divider);
        mDockDivider = findViewById(R.id.dock_divider);
        mIndicatorView = (PageIndicatorView)findViewById(R.id.pageindicator_view);
        mGestureLayer = (GestureLayer)findViewById(R.id.gesture_layer);

        mLauncherView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        mWorkspaceBackgroundDrawable = getResources().getDrawable(R.drawable.workspace_bg);

        // Setup the drag layer
        mDragLayer.setup(this, dragController);

        // Setup the gesture layer
        mGestureLayer.setup(this,dragController);
        mGestureLayer.initFlingParams(this);

        // Setup the hotseat
        mHotseat = (Hotseat) findViewById(R.id.hotseat);
        if (mHotseat != null) {
            mHotseat.setup(this);
        }

        mCustomHideseat = (CustomHideseat) findViewById(R.id.custom_hideseat);
        mHideseat = (Hideseat) findViewById(R.id.hideseat);
        mHideseat.setup(this);

        // Setup the workspace
        mWorkspace.setHapticFeedbackEnabled(false);
        mWorkspace.setOnLongClickListener(this);
        mWorkspace.setup(dragController);

        dragController.addDragListener(mWorkspace);
        dragController.addDragListener(mHideseat);

        // Get the search/delete bar
        mSearchDropTargetBar = (SearchDropTargetBar) mDragLayer.findViewById(R.id.qsb_bar);

        // Setup AppsCustomize
        mAppsCustomizeTabHost = (AppsCustomizeTabHost) findViewById(R.id.apps_customize_pane);
        mAppsCustomizeContent = (AppsCustomizePagedView)
                mAppsCustomizeTabHost.findViewById(R.id.apps_customize_pane_content);
        mAppsCustomizeContent.setup(this, dragController);

        // Setup the drag controller (drop targets have to be added in reverse order in priority)
        dragController.setDragScoller(mWorkspace);
        dragController.setScrollView(mDragLayer);
        dragController.setMoveTarget(mWorkspace);
        dragController.addDropTarget(mWorkspace);
        if (mSearchDropTargetBar != null) {
            mSearchDropTargetBar.setup(this, dragController);
        }
        DisplayMetrics metric = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metric);
     
		CheckVoiceCommandPressHelper.getInstance().setup(this);
		
        mEditModeContainer = (PreviewContainer)findViewById(R.id.editmode_container);
        /*if (LauncherApplication.isLowMemoryDevice() && mEditModeContainer != null) {
            ((ViewGroup)mEditModeContainer.getParent()).removeView(mEditModeContainer);
            mEditModeContainer = null;
        }*/
		if(mDeleteDialog == null) {
		    mDeleteDialog = new DropDownDialog(this);
		}
    }

    /**
     * Creates a view representing a shortcut.
     *
     * @param info The data structure describing the shortcut.
     *
     * @return A View inflated from R.layout.application.
     */
    View createShortcut(ShortcutInfo info) {
        return createShortcut(R.layout.application,
                (ViewGroup) mWorkspace.getChildAt(mWorkspace.getCurrentPage()), info);
    }

    /**
     * Creates a view representing a shortcut inflated from the specified resource.
     *
     * @param layoutResId The id of the XML layout used to create the shortcut.
     * @param parent The group the shortcut belongs to.
     * @param info The data structure describing the shortcut.
     *
     * @return A View inflated from layoutResId.
     */
    View createShortcut(int layoutResId, ViewGroup parent, ShortcutInfo info) {
        BubbleTextView favorite = (BubbleTextView) mInflater.inflate(layoutResId, parent, false);
        favorite.applyFromShortcutInfo(info);
        favorite.setOnClickListener(this);
        //add messageNum or new flag on BubbleTextView
        favorite.setMessageNum(info.messageNum);
        favorite.setIsNew(info.isNewItem());
        favorite.updateView(info);

        return favorite;
    }

    /**
     * Add an application shortcut to the workspace.
     *
     * @param data The intent describing the application.
     * @param cellInfo The position on screen where to create the shortcut.
     */
    void completeAddApplication(Intent data, long container, int screen, int cellX, int cellY) {
        final int[] cellXY = mTmpAddItemCellCoordinates;
        final CellLayout layout = getCellLayout(container, screen);

        // First we check if we already know the exact location where we want to add this item.
        if (cellX >= 0 && cellY >= 0) {
            cellXY[0] = cellX;
            cellXY[1] = cellY;
        } else if (!layout.findCellForSpan(cellXY, 1, 1)) {
            showOutOfSpaceMessage(isHotseatLayout(layout));
            return;
        }

        final ShortcutInfo info = mModel.getShortcutInfo(getPackageManager(), data, this);

        if (info != null) {
            info.setActivity(data.getComponent(), Intent.FLAG_ACTIVITY_NEW_TASK |
                    Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
            info.container = ItemInfo.NO_ID;
            mWorkspace.addApplicationShortcut(info, layout, container, screen, cellXY[0], cellXY[1],
                    isWorkspaceLocked(), cellX, cellY);
        } else {
            Log.e(TAG, "Couldn't find ActivityInfo for selected application: " + data);
        }
    }

    /**
     * Add a shortcut to the workspace.
     *
     * @param data The intent describing the shortcut.
     * @param cellInfo The position on screen where to create the shortcut.
     */
    private void completeAddShortcut(Intent data, long container, int screen, int cellX,
            int cellY) {
        int[] cellXY = mTmpAddItemCellCoordinates;
        int[] touchXY = mPendingAddInfo.dropPos;
        CellLayout layout = getCellLayout(container, screen);

        boolean foundCellSpan = false;

        ShortcutInfo info = mModel.infoFromShortcutIntent(this, data, null);
        if (info == null) {
            return;
        }
        info.isNew = 1;
        Drawable orgIcon = info.mIcon;
        Drawable themeIcon = getIconManager().buildUnifiedIcon(orgIcon);
        info.setIcon(themeIcon);
        final View view = createShortcut(info);

        // First we check if we already know the exact location where we want to add this item.
        if (cellX >= 0 && cellY >= 0) {
            cellXY[0] = cellX;
            cellXY[1] = cellY;
            foundCellSpan = true;

            // If appropriate, either create a folder or add to an existing folder
            if (mWorkspace.createUserFolderIfNecessary(view, container, layout, cellXY, 0,
                    true, null,null)) {
                return;
            }
            DragObject dragObject = new DragObject();
            dragObject.dragInfo = info;
            if (mWorkspace.addToExistingFolderIfNecessary(view, layout, cellXY, 0, dragObject,
                    true)) {
                return;
            }
        } else if (touchXY != null) {
            // when dragging and dropping, just find the closest free spot
            int[] result = layout.findNearestVacantArea(touchXY[0], touchXY[1], 1, 1, cellXY);
            foundCellSpan = (result != null);
        } else {
            foundCellSpan = layout.findCellForSpan(cellXY, 1, 1);
        }

        if (!foundCellSpan) {
            showOutOfSpaceMessage(isHotseatLayout(layout));
            return;
        }

        info.setIcon(orgIcon);
        LauncherModel.addItemToDatabase(this, info, container, screen, cellXY[0], cellXY[1], false);

        info.setIcon(themeIcon);
        if (!mRestoring) {
            mWorkspace.addInScreen(view, container, screen, cellXY[0], cellXY[1], 1, 1,
                    isWorkspaceLocked());
        }
    }

    static int[] getSpanForWidget(Context context, ComponentName component, int minWidth,
            int minHeight) {
        Rect padding = AppWidgetHostView.getDefaultPaddingForWidget(context, component, null);
        // We want to account for the extra amount of padding that we are adding to the widget
        // to ensure that it gets the full amount of space that it has requested
        int requiredWidth = minWidth + padding.left + padding.right;
        int requiredHeight = minHeight + padding.top + padding.bottom;
        return CellLayout.rectToCell(context.getResources(), requiredWidth, requiredHeight, null);
    }
	/*public for edit mode*/
    public static int[] getSpanForWidget(Context context, AppWidgetProviderInfo info) {
        return getSpanForWidget(context, info.provider, info.minWidth, info.minHeight);
    }

    public static int[] getMinSpanForWidget(Context context,
            AppWidgetProviderInfo info) {
        return getSpanForWidget(context, info.provider, info.minResizeWidth, info.minResizeHeight);
    }

//    static int[] getSpanForWidget(Context context, PendingAddWidgetInfo info) {
//        return getSpanForWidget(context, info.componentName, info.minWidth, info.minHeight);
//    }
//
//    static int[] getMinSpanForWidget(Context context, PendingAddWidgetInfo info) {
//        return getSpanForWidget(context, info.componentName, info.minResizeWidth,
//                info.minResizeHeight);
//    }

    /**
     * Add a widget to the workspace.
     *
     * @param appWidgetId The app widget id
     * @param cellInfo The position on screen where to create the widget.
     */
    private void completeAddAppWidget(final int appWidgetId, long container, int screen,
            AppWidgetHostView hostView, AppWidgetProviderInfo appWidgetInfo) {
        if (appWidgetInfo == null) {
            appWidgetInfo = mAppWidgetManager.getAppWidgetInfo(appWidgetId);
            if (appWidgetInfo == null) {
                return;
              }
         }

        // Calculate the grid spans needed to fit this widget
        CellLayout layout = getCellLayout(container, screen);

        int[] minSpanXY = getMinSpanForWidget(this, appWidgetInfo);
        int[] spanXY = getSpanForWidget(this, appWidgetInfo);

        // Try finding open space on Launcher screen
        // We have saved the position to which the widget was dragged-- this really only matters
        // if we are placing widgets on a "spring-loaded" screen
        int[] cellXY = mTmpAddItemCellCoordinates;
        int[] touchXY = mPendingAddInfo.dropPos;
        int[] finalSpan = new int[2];
        boolean foundCellSpan = false;
        if (mPendingAddInfo.cellX >= 0 && mPendingAddInfo.cellY >= 0) {
            cellXY[0] = mPendingAddInfo.cellX;
            cellXY[1] = mPendingAddInfo.cellY;
            spanXY[0] = mPendingAddInfo.spanX;
            spanXY[1] = mPendingAddInfo.spanY;
            foundCellSpan = true;
        } else if (touchXY != null) {
            // when dragging and dropping, just find the closest free spot
            int[] result = layout.findNearestVacantArea(
                    touchXY[0], touchXY[1], minSpanXY[0], minSpanXY[1], spanXY[0],
                    spanXY[1], cellXY, finalSpan);
            spanXY[0] = finalSpan[0];
            spanXY[1] = finalSpan[1];
            foundCellSpan = (result != null);
        } else {
            foundCellSpan = layout.findCellForSpan(cellXY, minSpanXY[0], minSpanXY[1]);
        }

        if (!foundCellSpan) {
            if (appWidgetId != -1) {
                // Deleting an app widget ID is a void call but writes to disk before returning
                // to the caller...
                new Thread("deleteAppWidgetId") {
                    public void run() {
                        mAppWidgetHost.deleteAppWidgetId(appWidgetId);
                    }
                }.start();
            }
            showOutOfSpaceMessage(isHotseatLayout(layout));
            return;
        }

        getWorkspace().checkAndRemoveEmptyCell();

        // Build Launcher-specific widget info and save to database
        LauncherAppWidgetInfo launcherInfo = new LauncherAppWidgetInfo(appWidgetId,
                appWidgetInfo.provider);
        launcherInfo.spanX = spanXY[0];
        launcherInfo.spanY = spanXY[1];
        launcherInfo.minSpanX = mPendingAddInfo.minSpanX;
        launcherInfo.minSpanY = mPendingAddInfo.minSpanY;

        LauncherModel.addItemToDatabase(this, launcherInfo,
                container, screen, cellXY[0], cellXY[1], false);

        if (!mRestoring) {
            if (hostView == null) {
                // Perform actual inflation because we're live
                launcherInfo.hostView = mAppWidgetHost.createView(this, appWidgetId, appWidgetInfo);
                launcherInfo.hostView.setAppWidget(appWidgetId, appWidgetInfo);
            } else {
                // The AppWidgetHostView has already been inflated and instantiated
                launcherInfo.hostView = hostView;
            }

            launcherInfo.hostView.setTag(launcherInfo);
            launcherInfo.hostView.setVisibility(View.VISIBLE);
            launcherInfo.notifyWidgetSizeChanged(this);

            mWorkspace.addInScreen(launcherInfo.hostView, container, screen, cellXY[0], cellXY[1],
                    launcherInfo.spanX, launcherInfo.spanY, isWorkspaceLocked());

            addWidgetToAutoAdvanceIfNeeded(launcherInfo.hostView, appWidgetInfo);
        }
        resetAddInfo();
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if(getCurrentScreen() == -1) //lifecard does not need animation;
                return ;
            if (Intent.ACTION_SCREEN_OFF.equals(action)) {
                mUnlockAnimation.screenOff();
                //mUserPresent = false;
                mDragLayer.clearAllResizeFrames();
                updateRunning();

                if( mFlipAnim.isShowing() ){
                    stopFlipWithoutAnimation();
                }

                // Reset AllApps to its initial state only if we are not in the middle of
                // processing a multi-step drop
                // when screen off, no need to show workspace again
                /*if (mAppsCustomizeTabHost != null && mPendingAddInfo.container == ItemInfo.NO_ID) {
                    mAppsCustomizeTabHost.reset();
                    showWorkspace(false);
                }*/

                //mIsTopWhenScreenOff = isTopActivity();
                // we set mIsWakeUpFromOtherApp to false when screen off.
                //mIsWakeUpFromOtherApp = false;

                // unlock screen operation from screenedit
                if (mEditMode) {
                    exitScreenEditModeWithoutSave();
                }
            }
            else if ("tpw.intent.action.KEYGUARD_UNLOCK_INTENT_DONE".equals(action)) {
                mUnlockAnimation.finish();
            }
            else if (Intent.ACTION_SCREEN_ON.equals(action)) {
                closeOpenFolders(false);
                mUnlockAnimation.screenOn();

                if( mFlipAnim.isShowing() ){
                    stopFlipWithoutAnimation();
                }
                /*mShouldPlayAnimation = mIsTopWhenScreenOff;
                if (mState == State.WORKSPACE && ((mIsResumed && mShouldPlayAnimation) || mIsWakeUpByThreeFingerMode)) {
                    getWorkspace().cancelUnlockScreenAnimation();
                    if (isScreenLocked()){
                        getWorkspace().setAllItemsOfCurrentPageVisibility(View.INVISIBLE);
                        getAnimationPlayer().postVisibleRunnableDelayed();
                    }
                }*/
            } else if (Intent.ACTION_USER_PRESENT.equals(action)) {
                mUnlockAnimation.standby();
                //mUserPresent = true;
                //updateRunning();
                /*LockScreenAnimator lsa = LockScreenAnimator.getInstance(Launcher.this);
                if (mState == State.WORKSPACE &&
                    ( lsa.getNeedBackHomeAnim() ||(mIsResumed && mShouldPlayAnimation) || mIsWakeUpByThreeFingerMode)) {
                    Log.d(TAG, "mIsWakeUpByThreeFingerMode="+mIsWakeUpByThreeFingerMode+",mShouldPlayAnimation="
                           +mShouldPlayAnimation+",mIsResumed="+mIsResumed+",lsa.getNeedBackHomeAnim="+lsa.getNeedBackHomeAnim());
                    Log.d(TAG, "ACTION_USER_PRESENT to play unlock animation");
                    getAnimationPlayer().play(new ScreenUnlockedAnimation(Launcher.this));
                    mIsResumed = false;
                }*/
                Log.d(LIVE_WEATHER_TAG, "BroadcastReceiver ACTION_USER_PRESENT begin");
                Log.d(LIVE_WEATHER_TAG, "BroadcastReceiver ACTION_USER_PRESENT isDraggingEnabled() is " + isDraggingEnabled());
                Log.d(LIVE_WEATHER_TAG, "BroadcastReceiver mVisible is " + mVisible);
                if(isDraggingEnabled()) {
                	if(mVisible) {
                		OnBindParticleService();
                	}else {
                		UnBindParticleService();
                	}
                }
                Log.d(LIVE_WEATHER_TAG, "BroadcastReceiver ACTION_USER_PRESENT end");
            } else if (ALARM_ALERT_ACTION.equals(action)){
                if( mFlipAnim.isShowing() ){
                    stopFlipWithoutAnimation();
                }
            }
            //topwise zyf add for notify
            
	        else if (Utilities.ACTION_OPEN_FIXEDFOLDER.equals(action)) 
	        {
	        	Log.d("zyfonline","receive ACTION_OPEN_FIXEDFOLDER .....");
	        	try {
	        		int isfirst=intent.getIntExtra("TEST", 0);
		           	if(isfirst==0)
		           	{
		           		Intent intent_home = new Intent(Intent.ACTION_MAIN);
			           	intent_home.setComponent(new ComponentName("com.tpw.homeshell","com.tpw.homeshell.Launcher"));
			           	intent_home.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
			           	startActivity(intent_home);

			           	intent.putExtra("TEST",1);
			           	final Intent intent_again=intent;
			           	
			           	mHandler.postDelayed(new Runnable() {
 		                    public void run() {
 		                    	sendBroadcast(intent_again);
 		                    }
 		                }, 600);
			           	return;
		           	}
		        	int iextratype=intent.getIntExtra(Utilities.EXTRA_OPEN_FIXEDFOLDER_TYPE, -1);
		        	Log.d("zyfonline","receive ACTION_OPEN_FIXEDFOLDER .....iextratype = "+iextratype);
		        	if(mWorkspace != null&&iextratype!=-1)
		        	 {
	                     ItemInfo fi=getModel().findFixedFolderByExtraType(iextratype);
	                     if(ItemInfo.ITEM_EXTRA_TYPE_APPS==iextratype)
	                     {
	                    	 getModel().setNotificationIsShow(context,
	                    			 LauncherModel.SYSTEM_NOTIFICATION_APPS_SHOW,
	                    			 LauncherModel.NOT_SHOW_CONDITION_MUST_VIEW);
	                     }
	                     else if(ItemInfo.ITEM_EXTRA_TYPE_GAMES==iextratype)
	                     {
	                    	 getModel().setNotificationIsShow(context,
	                    			 LauncherModel.SYSTEM_NOTIFICATION_GAMES_SHOW,
	                    			 LauncherModel.NOT_SHOW_CONDITION_MUST_VIEW);
	                     }
	                     if(fi!=null)
	                     {
	                    	 FolderInfo fInfo=getModel().getFolderInfo(fi.id);
	                    	 long screenid=fInfo.screen;
	                    	 long lcontainer= fInfo.container;
	                    	 
	                    	 CellLayout layout  = null;
	                    	 boolean needtosnap=false;
	                    	 if (lcontainer == LauncherSettings.Favorites.CONTAINER_DESKTOP)
	                    	 {
		                    	 needtosnap=true;
		                    	 layout  = (CellLayout) mWorkspace.getChildAt((int)screenid);
	                    	 }
	                    	 else if(lcontainer == LauncherSettings.Favorites.CONTAINER_HOTSEAT)
	                    	 {
	                    		 layout = (CellLayout) getHotseat().getLayout();
	                    		 if(mFolderUtils.isFolderOpened())
	                    			 closeFolder();
	                    	 }
	                    	 ShortcutAndWidgetContainer container = layout.getShortcutsAndWidgets();
	                    	 int childCount = container.getChildCount();
	                    	 for (int j = 0; j < childCount; j++) 
	                    	 {
	        	                 View view = container.getChildAt(j);
	        	                 Object tag = view.getTag();
	        	                 Log.d("zyfonline","receiver : (tag instanceof FolderInfo) = "+(tag instanceof FolderInfo));
	        	                 if (tag instanceof FolderInfo) 
	        	                 {
	        	                     ItemInfo info = (ItemInfo)tag;
	        	                     Log.d("zyfonline","receiver : info = "+(info)+" , info.itemExtraType = "+info.itemExtraType);
	        	                     if(info.itemExtraType==iextratype)
	        	                     {
	                	 				final FolderIcon fIcon = (FolderIcon) view;
	                	 				if(((FolderInfo)info).opened)
	                	 				{
	                	 					Log.d("zyfonline","receiver : info = "+info+"  ,  is opened!");
	                	 				}
	                	 				else {
	                	 					if(needtosnap)
	                	 					{
	                	 						Log.d("zyfonline","receiver : snapToPage screenid = "+screenid);
	                	 						mWorkspace.snapToPage((int)screenid,new openfolderRunnable(fIcon));
	                	 					}
	                	 					else {
	                	 						if(mFolderUtils.isFolderOpened()||getWorkspace().isPageMoving())
	                	 						{
	                	 							Log.d("zyfonline","receiver delay handleFolderClick : fIcon = "+fIcon);
		                	 						mHandler.postDelayed(new Runnable() {
		                	 		                    public void run() {
		                	 		                    	handleFolderClick(fIcon);
		                	 		                    }
		                	 		                }, 800);
	                	 						}
	                	 						else
	                	 						{
	                	 							Log.d("zyfonline","receiver handleFolderClick : fIcon = "+fIcon);
	                	 							handleFolderClick(fIcon);
	                	 						}
											}
										}
	        	                        break;
	        	                     }
	        	                 }
	                    	 }
	                    	 
	                     }
		        	 }
				} catch (Exception e) {
					// TODO: handle exception
					Log.e("zyfonline","receiver handleFolderClick : error = "+e);
				}
	    		
	        }
          //topwise zyf add for notify end
        }
    };

    public boolean shouldPlayUnlockAnimation(){
        return mState == State.WORKSPACE && !isInEditScreenMode() && !isEditMode();
    }
    public int getCurrentScreen(){
        if(!mSupportLifeCenter || mLifeCenterHostView.LIFECENTER_PAGE != mLifeCenterHostView.getCurrentPage())
            return getCurrentWorkspaceScreen();
        else
            return -1;
    }
    public IconManager getIconManager(){
        return ((LauncherApplication) getApplicationContext()).getIconManager();
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();

        // Listen for broadcasts related to user-presence
        final IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        filter.addAction(Intent.ACTION_USER_PRESENT);
        filter.addAction(ALARM_ALERT_ACTION);
        filter.addAction("tpw.intent.action.KEYGUARD_UNLOCK_INTENT_DONE");
        filter.addAction("tpw.intent.action.KEYGUARD_UNLOCK_DONE");
      //topwise zyf add for notify
        filter.addAction(Utilities.ACTION_OPEN_FIXEDFOLDER);
      //topwise zyf add for notify end
        registerReceiver(mReceiver, filter);
        //start:modify by zoujianyu,Topwise,2015-5-14,GadgetView display abnormal ,bug 218,267
        //FirstFrameAnimatorHelper.initializeDrawListener(getWindow().getDecorView());
        //end:modify by zoujianyu,Topwise,2015-5-14,GadgetView display abnormal ,bug 218,267
        mAttached = true;
        mVisible = true;
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mVisible = false;

        if (mAttached) {
            unregisterReceiver(mReceiver);
            mAttached = false;
        }
        updateRunning();
    }

    public void onWindowVisibilityChanged(int visibility) {
        mVisible = visibility == View.VISIBLE;
        updateRunning();
        // The following code used to be in onResume, but it turns out onResume is called when
        // you're in All Apps and click home to go to the workspace. onWindowVisibilityChanged
        // is a more appropriate event to handle
        if (mVisible) {
            mAppsCustomizeTabHost.onWindowVisible();
            if (!mWorkspaceLoading) {
                final ViewTreeObserver observer = mWorkspace.getViewTreeObserver();
                // We want to let Launcher draw itself at least once before we force it to build
                // layers on all the workspace pages, so that transitioning to Launcher from other
                // apps is nice and speedy.
                observer.addOnDrawListener(new ViewTreeObserver.OnDrawListener() {
                    private boolean mStarted = false;
                    public void onDraw() {
                        if (mStarted) return;
                        mStarted = true;
                        // We delay the layer building a bit in order to give
                        // other message processing a time to run.  In particular
                        // this avoids a delay in hiding the IME if it was
                        // currently shown, because doing that may involve
                        // some communication back with the app.
                        final ViewTreeObserver.OnDrawListener listener = this;
                        mWorkspace.post(new Runnable() {
                                public void run() {
                                    if (mWorkspace != null &&
                                            mWorkspace.getViewTreeObserver() != null) {
                                        mWorkspace.getViewTreeObserver().
                                                removeOnDrawListener(listener);
                                    }
                                }
                            });
                        return;
                    }
                });
            }
            clearTypedText();
        }
    }

    private void sendAdvanceMessage(long delay) {
        mHandler.removeMessages(ADVANCE_MSG);
        Message msg = mHandler.obtainMessage(ADVANCE_MSG);
        mHandler.sendMessageDelayed(msg, delay);
        mAutoAdvanceSentTime = System.currentTimeMillis();
    }

    private void updateRunning() {
        boolean autoAdvanceRunning = mVisible && mUserPresent && !mWidgetsToAdvance.isEmpty();
        if (autoAdvanceRunning != mAutoAdvanceRunning) {
            mAutoAdvanceRunning = autoAdvanceRunning;
            if (autoAdvanceRunning) {
                long delay = mAutoAdvanceTimeLeft == -1 ? mAdvanceInterval : mAutoAdvanceTimeLeft;
                sendAdvanceMessage(delay);
            } else {
                if (!mWidgetsToAdvance.isEmpty()) {
                    mAutoAdvanceTimeLeft = Math.max(0, mAdvanceInterval -
                            (System.currentTimeMillis() - mAutoAdvanceSentTime));
                }
                mHandler.removeMessages(ADVANCE_MSG);
                mHandler.removeMessages(0); // Remove messages sent using postDelayed()
            }
        }
    }

    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == ADVANCE_MSG) {
                int i = 0;
                for (View key: mWidgetsToAdvance.keySet()) {
                    final View v = key.findViewById(mWidgetsToAdvance.get(key).autoAdvanceViewId);
                    final int delay = mAdvanceStagger * i;
                    if (v instanceof Advanceable) {
                       postDelayed(new Runnable() {
                           public void run() {
                               ((Advanceable) v).advance();
                           }
                       }, delay);
                    }
                    i++;
                }
                sendAdvanceMessage(mAdvanceInterval);
            }
        }
    };
    private boolean mIsAllAppShowed;
    public boolean isAllAppShowed(){
        return mIsAllAppShowed;
    }

    void addWidgetToAutoAdvanceIfNeeded(View hostView, AppWidgetProviderInfo appWidgetInfo) {
        if (appWidgetInfo == null || appWidgetInfo.autoAdvanceViewId == -1) return;
        View v = hostView.findViewById(appWidgetInfo.autoAdvanceViewId);
        if (v != null && v instanceof Advanceable) {
            mWidgetsToAdvance.put(hostView, appWidgetInfo);
            ((Advanceable) v).fyiWillBeAdvancedByHostKThx();
            updateRunning();
        }
    }

    void removeWidgetToAutoAdvance(View hostView) {
        if (mWidgetsToAdvance.containsKey(hostView)) {
            mWidgetsToAdvance.remove(hostView);
            updateRunning();
        }
    }

    public void removeAppWidget(LauncherAppWidgetInfo launcherInfo) {
        UserTrackerHelper.sendUserReport(UserTrackerMessage.MSG_REMOVE_WIDGET,
                ((launcherInfo == null) ? "" : launcherInfo.providerName
                        .toString()));
        if (launcherInfo != null) {
            removeWidgetToAutoAdvance(launcherInfo.hostView);
            launcherInfo.hostView = null;
        }
    }

    //"public" added by qinjinchuan topwise for bug318
    public void showOutOfSpaceMessage(boolean isHotseatLayout) {
        int strId = (isHotseatLayout ? R.string.hotseat_out_of_space : R.string.out_of_space);
        Toast.makeText(this, getString(strId), Toast.LENGTH_SHORT).show();
    }

    void showToastMessage(int strId) {
        Toast.makeText(this, getString(strId), Toast.LENGTH_SHORT).show();
    }

    public LauncherAppWidgetHost getAppWidgetHost() {
        return mAppWidgetHost;
    }

    public LauncherModel getModel() {
        return mModel;
    }

    void closeSystemDialogs() {
        getWindow().closeAllPanels();

        // Whatever we were doing is hereby canceled.
        mWaitingForResult = false;
    }

    @Override
    protected void onNewIntent(Intent intent) {
    	if(intent == null){
    		return;
    	}
        final Intent lifeIntent = intent;
        long startTime = 0;
        if (DEBUG_RESUME_TIME) {
            startTime = System.currentTimeMillis();
        }

        Log.d(TAG,"onNewIntent");
        super.onNewIntent(intent);
        if (intent != null && intent.getExtras() != null && intent.getExtras().containsKey("wakeUp")) {
            mIsWakeUpByThreeFingerMode = true;
            return;
        } else {
            mIsWakeUpByThreeFingerMode = false;
            mShouldPlayAnimation = false;
        }
        // Close the menu
        if (Intent.ACTION_MAIN.equals(intent.getAction())) {
            // also will cancel mWaitingForResult.
            closeSystemDialogs();
            final boolean alreadyOnHome =
                    ((intent.getFlags() & Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT)
                        != Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
            Log.d(TAG,"onNewIntent, alreadyonhome="+alreadyOnHome);
            final boolean needExitScreenEditMode = ((intent.getFlags() &
                    (Intent.FLAG_ACTIVITY_NEW_TASK |
                    Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)) ==
                    (Intent.FLAG_ACTIVITY_NEW_TASK |
                    Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED));

            final String url = intent.getStringExtra("url");
            try {
                if (mSupportLifeCenter) {
                    if ((mLifeCenterHostView.LIFECENTER_PAGE ==
                            mLifeCenterHostView.getCurrentPage() && null == url)) {
                        mLifeCenterHostView.dispatchHome(alreadyOnHome);
                        return;
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "life center error ", e);
            }

            final boolean backFromHomeShellSetting = mBackFromHomeShellSetting;
            mBackFromHomeShellSetting = false;

            Runnable processIntent = new Runnable() {
                public void run() {
                    if (mWorkspace == null) {
                        // Can be cases where mWorkspace is null, this prevents a NPE
                        return;
                    }

                    try {
                        if (mSupportLifeCenter) {
                            if (null != url) {
                                if (mLifeCenterHostView.LIFECENTER_PAGE !=
                                    mLifeCenterHostView.getCurrentPage()) {
//mLifeCenterHost.enterShowDetailCard(url);
                                    mLifeCenterHost.enterShowDetailCard(url,lifeIntent);
                                    mLifeCenterHostView.setLastPage(mLifeCenterHostView.getCurrentPage());
                                    mLifeCenterHostView.setCurrentPage(mLifeCenterHostView.LIFECENTER_PAGE, true);
                                }else {
//                                    mLifeCenterHost.startLifeCenterInner(url);
                                    mLifeCenterHost.startLifeCenterInner(url,lifeIntent);
                                }
                            }
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "life center error ", e);
                    }

                    Folder openFolder = mWorkspace.getOpenFolder();

                    // update menu
                    boolean menushowing = false;
                    if(mMenu!=null&&mMenu.isShowing()){
                        menushowing = true;
                        mMenu.dismiss();
                        return;
                    }

                    if( mFlipAnim.isShowing() && !mFlipAnim.isAnimating() ){
                        stopFlipAnimation();
                        return;
                    }else if( mFlipAnim.isShowing() ){
                        return;
                    }
                    //screen edit
                    if (mEditMode && needExitScreenEditMode) {
                        exitScreenEditMode(true);
                        return;
                    }

                    if (mAppSearchMode) {
                        exitSearchMode();
                        return;
                    }


                    if (mDeleteDialog != null && mDeleteDialog.isShowing()) {
                        mDeleteDialog.getNegetiveButton().performClick();
                        return;
                    }
                    
                    // In all these cases, only animate if we're already on home
                    mWorkspace.exitWidgetResizeMode();
                    boolean openHideseat = isHideseatShowing();
                    if (alreadyOnHome && mState == State.WORKSPACE && !mWorkspace.isTouchActive() &&
                            openFolder == null&&!menushowing && !openHideseat &&
                            !backFromHomeShellSetting) {
                        mWorkspace.moveToDefaultScreen(true);
                    }

                    if (openHideseat) {
                        hideHideseat(true);
                    }
                    closeFolder();
                    exitSpringLoadedDragMode();

                    if (isEditMode()) {
                        exiteEditModeAnimation();
                    }

                    // If we are already on home, then just animate back to the workspace,
                    // otherwise, just wait until onResume to set the state back to Workspace
                    if (alreadyOnHome) {
                        showWorkspace(true);
                    } else {
                        mOnResumeState = State.WORKSPACE;
                    }

                    final View v = getWindow().peekDecorView();

                    try {
                        if (v != null && v.getWindowToken() != null) {
                            InputMethodManager imm = (InputMethodManager)getSystemService(
                                    INPUT_METHOD_SERVICE);
                            imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                        }
                    } catch (RuntimeException e) {
                        // add try catch block due to InputMethod throws
                        // RuntimeException;
                        Log.e(TAG, e.toString(), e);
                    }

                    // Reset AllApps to its initial state
                    if (!alreadyOnHome && mAppsCustomizeTabHost != null) {
                        mAppsCustomizeTabHost.reset();
                    }
                }
            };

            if (alreadyOnHome && !mWorkspace.hasWindowFocus()) {
                // Delay processing of the intent to allow the status bar animation to finish
                // first in order to avoid janky animations.
                mWorkspace.postDelayed(processIntent, 350);
            } else {
                // Process the intent immediately.
                processIntent.run();
            }

        }
        if (DEBUG_RESUME_TIME) {
            Log.d(TAG, "Time spent in onNewIntent: " + (System.currentTimeMillis() - startTime));
        }
    }

    @Override
    public void onRestoreInstanceState(Bundle state) {
        super.onRestoreInstanceState(state);
        for (int page: mSynchronouslyBoundPages) {
            mWorkspace.restoreInstanceStateForChild(page);
        }
        if (mLifeCenterHostView != null) {
            mLifeCenterHostView.setCurrentItem(mLifeCenterHostView.HOME_PAGE);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt(RUNTIME_STATE_CURRENT_SCREEN, mWorkspace.getNextPage());
        super.onSaveInstanceState(outState);

        outState.putInt(RUNTIME_STATE, mState.ordinal());
        // We close any open folder since it will not be re-opened, and we need to make sure
        // this state is reflected.
//        closeFolder();
        if (mWorkspace.getOpenFolder() != null && !mWorkspace.getOpenFolder().isEditingName()) {
            closeFolderWithoutExpandAnimation();
        }

        if (mPendingAddInfo.container != ItemInfo.NO_ID && mPendingAddInfo.screen > -1 &&
                mWaitingForResult) {
            outState.putLong(RUNTIME_STATE_PENDING_ADD_CONTAINER, mPendingAddInfo.container);
            outState.putInt(RUNTIME_STATE_PENDING_ADD_SCREEN, mPendingAddInfo.screen);
            outState.putInt(RUNTIME_STATE_PENDING_ADD_CELL_X, mPendingAddInfo.cellX);
            outState.putInt(RUNTIME_STATE_PENDING_ADD_CELL_Y, mPendingAddInfo.cellY);
            outState.putInt(RUNTIME_STATE_PENDING_ADD_SPAN_X, mPendingAddInfo.spanX);
            outState.putInt(RUNTIME_STATE_PENDING_ADD_SPAN_Y, mPendingAddInfo.spanY);
            outState.putParcelable(RUNTIME_STATE_PENDING_ADD_WIDGET_INFO, mPendingAddWidgetInfo);
        }

        if (mFolderInfo != null && mWaitingForResult) {
            outState.putBoolean(RUNTIME_STATE_PENDING_FOLDER_RENAME, true);
            outState.putLong(RUNTIME_STATE_PENDING_FOLDER_RENAME_ID, mFolderInfo.id);
        }

        // Save the current AppsCustomize tab
        if (mAppsCustomizeTabHost != null) {
            String currentTabTag = mAppsCustomizeTabHost.getCurrentTabTag();
            if (currentTabTag != null) {
                outState.putString("apps_customize_currentTab", currentTabTag);
            }
            int currentPage = mAppsCustomizeContent.getCurrentPage();
            if (currentPage >= mAppsCustomizeContent.getPageCount()) {
                mAppsCustomizeContent.setCurrentPage(0);
            }
            int currentIndex = mAppsCustomizeContent.getSaveInstanceStateIndex();
            outState.putInt("apps_customize_currentIndex", currentIndex);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        //CheckVoiceCommandPressHelper.getInstance().deInitVoiceService();
        // Remove all pending runnables
        mHandler.removeMessages(ADVANCE_MSG);
        mHandler.removeMessages(0);
        //mWorkspace.cleanUpAllGadgets();

        // Stop callbacks from LauncherModel
        LauncherApplication app = ((LauncherApplication) getApplication());
        if (mModel != null) {
            mModel.stopLoader();
        }
        app.setLauncher(null);

        try {
            mAppWidgetHost.stopListening();
        } catch (NullPointerException ex) {
            Log.w(TAG, "problem while stopping AppWidgetHost during Launcher destruction", ex);
        }
        mAppWidgetHost = null;

        mWidgetsToAdvance.clear();

        TextKeyListener.getInstance().release();

        // Disconnect any of the callbacks and drawables associated with ItemInfos on the workspace
        // to prevent leaking Launcher activities on orientation change.
        if (mModel != null) {
            mModel.unbindItemInfosAndClearQueuedBindRunnables();
        }

        getContentResolver().unregisterContentObserver(mWidgetObserver);
        unregisterReceiver(mCloseSystemDialogsReceiver);
        // For wallpaper changed
        unregisterReceiver(mWallpaperChangedReceiver);

        unregisterReceiver(mFlipAnim);

        // unregister content observer of privacy space and notification importance
        PrivacySpaceHelper.destroy();
        NotificationPriorityHelper.destroy();

        mDragLayer.clearAllResizeFrames();
        ((ViewGroup) mWorkspace.getParent()).removeAllViews();
        mWorkspace.removeAllViews();
        mWorkspace = null;
        mDragController = null;

        LauncherAnimUtils.onDestroyActivity();

        if(mMenu!=null){
            mMenu.clear();
            mMenu = null;
        }
       
        UserTrackerHelper.deinit();

        if (mSupportLifeCenter) {
            mLifeCenterHost.doDestroy();
            mLifeCenterHost.clear();
        }
        LauncherApplication.homeshellSetting = null;
        
        if(isHideDeleteDialog()) {
            mDeleteDialog.dismiss();
        }
        mDeleteDialog = null;

        //gadgetViewList.clear();
    }

    public DragController getDragController() {
        return mDragController;
    }

    @Override
    public void startActivityForResult(Intent intent, int requestCode) {
        if (requestCode >= 0) mWaitingForResult = true;
        super.startActivityForResult(intent, requestCode);
    }

    public boolean isWorkspaceLocked() {
        return mWorkspaceLoading || mWaitingForResult || mModel.isDownloadStatus();
    }

    public boolean isWorkspaceLoading() {
        return mWorkspaceLoading;
    }

    private void resetAddInfo() {
        mPendingAddInfo.container = ItemInfo.NO_ID;
        mPendingAddInfo.screen = -1;
        mPendingAddInfo.cellX = mPendingAddInfo.cellY = -1;
        mPendingAddInfo.spanX = mPendingAddInfo.spanY = -1;
        mPendingAddInfo.minSpanX = mPendingAddInfo.minSpanY = -1;
        mPendingAddInfo.dropPos = null;
    }

    public boolean isEmptyCellCanBeRemoved() {
        return mState == State.APPS_CUSTOMIZE_SPRING_LOADED || mEditMode
                || (mScreenManager != null && mScreenManager.lockWorkspace())
                || (mWorkspace != null && mWorkspace.getChildAt(mWorkspace.getCurrentPage()) == LockScreenAnimator.getInstance(this).getWorkingOnCellLayout());
    }

    void addAppWidgetImpl(final int appWidgetId, ItemInfo info, AppWidgetHostView boundWidget,
            AppWidgetProviderInfo appWidgetInfo) {
        if (appWidgetInfo.configure != null) {
            mPendingAddWidgetInfo = appWidgetInfo;

            Intent intent = new Intent(AppWidgetManager.ACTION_APPWIDGET_CONFIGURE);
            intent.setComponent(appWidgetInfo.configure);
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
            startActivityForResultSafely(intent, REQUEST_CREATE_APPWIDGET);
            //can't add yahoo weather widget to desktop
            //if (appWidgetId != -1) {
            //   getAppWidgetHost().deleteAppWidgetId(appWidgetId);
            //}
        } else {
            // Otherwise just add it
            completeAddAppWidget(appWidgetId, info.container, info.screen, boundWidget,
                    appWidgetInfo);
            // Exit spring loaded mode if necessary after adding the widget
            exitSpringLoadedDragModeDelayed(true, false, null);
        }
    }

    /**
     * Process a shortcut drop.
     *
     * @param componentName The name of the component
     * @param screen The screen where it should be added
     * @param cell The cell it should be added to, optional
     * @param position The location on the screen where it was dropped, optional
     */
    void processShortcutFromDrop(ComponentName componentName, long container, int screen,
            int[] cell, int[] loc) {
        resetAddInfo();
        mPendingAddInfo.container = container;
        mPendingAddInfo.screen = screen;
        mPendingAddInfo.dropPos = loc;

        if (cell != null) {
            mPendingAddInfo.cellX = cell[0];
            mPendingAddInfo.cellY = cell[1];
        }

        Intent createShortcutIntent = new Intent(Intent.ACTION_CREATE_SHORTCUT);
        createShortcutIntent.setComponent(componentName);
        processShortcut(createShortcutIntent);
    }

    /**
     * Process a widget drop.
     *
     * @param info The PendingAppWidgetInfo of the widget being added.
     * @param screen The screen where it should be added
     * @param cell The cell it should be added to, optional
     * @param position The location on the screen where it was dropped, optional
     */
    void addAppWidgetFromDrop(PendingAddWidgetInfo info, long container, int screen,
            int[] cell, int[] span, int[] loc) {
        resetAddInfo();
        mPendingAddInfo.container = info.container = container;
        mPendingAddInfo.screen = info.screen = screen;
        mPendingAddInfo.dropPos = loc;
        mPendingAddInfo.minSpanX = info.minSpanX;
        mPendingAddInfo.minSpanY = info.minSpanY;

        if (cell != null) {
            mPendingAddInfo.cellX = cell[0];
            mPendingAddInfo.cellY = cell[1];
        }
        if (span != null) {
            mPendingAddInfo.spanX = span[0];
            mPendingAddInfo.spanY = span[1];
        }

        AppWidgetHostView hostView = info.boundWidget;
        int appWidgetId;
        if (hostView != null) {
            appWidgetId = hostView.getAppWidgetId();
            addAppWidgetImpl(appWidgetId, info, hostView, info.info);
        } else {
            // In this case, we either need to start an activity to get permission to bind
            // the widget, or we need to start an activity to configure the widget, or both.
            appWidgetId = getAppWidgetHost().allocateAppWidgetId();
            Bundle options = info.bindOptions;

            boolean success = false;
            // binding widget will failed, while uninstalled widgetApp before drop widget.
            try {
                if (options != null) {
                    success = mAppWidgetManager.bindAppWidgetIdIfAllowed(appWidgetId,
                            info.componentName, options);
                } else {
                    success = mAppWidgetManager.bindAppWidgetIdIfAllowed(appWidgetId,
                            info.componentName);
                }
            } catch (Exception e) {
                Log.e(TAG, "Failed in addAppWidgetFromDrop : " + e.getMessage());
            }

            if (success) {
                addAppWidgetImpl(appWidgetId, info, null, info.info);
            } else {
                mPendingAddWidgetInfo = info.info;
                Intent intent = new Intent(AppWidgetManager.ACTION_APPWIDGET_BIND);
                intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
                intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_PROVIDER, info.componentName);
                // TODO: we need to make sure that this accounts for the options bundle.
                // intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_OPTIONS, options);
                try {
                    startActivityForResult(intent, REQUEST_BIND_APPWIDGET);
                } catch (Exception e) {
                    Log.e(TAG, "sxsexe---->addAppWidgetFromDrop Error " + e.getMessage());
                }
            }
        }
    }

    void addGadgetFromDrop(GadgetItemInfo info, long container, int screen,
            int[] cell, int[] span, int[] loc) {
        /*if (info == null || info.gadgetInfo == null) {
            Log.e(TAG, "add gadget failed, info N/A");
        } else {
            View v = LauncherGadgetHelper.getGadget(this, info.gadgetInfo);
            if (v == null) {
                Log.e(TAG, "add gadget failed " + info.gadgetInfo);
            } else {
                v.setTag(info);
                mWorkspace.addInScreen(v, container, screen, cell[0], cell[1],
                        span[0], span[1], isWorkspaceLocked());
                LauncherModel.addItemToDatabase(this, info, container, screen, cell[0], cell[1], false);
            }
        }
        exitSpringLoadedDragModeDelayed(true, false, null);*/
    }

    void processShortcut(Intent intent) {
        // Handle case where user selected "Applications"
        String applicationName = getResources().getString(R.string.group_applications);
        String shortcutName = intent.getStringExtra(Intent.EXTRA_SHORTCUT_NAME);

        if (applicationName != null && applicationName.equals(shortcutName)) {
            Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
            mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);

            Intent pickIntent = new Intent(Intent.ACTION_PICK_ACTIVITY);
            pickIntent.putExtra(Intent.EXTRA_INTENT, mainIntent);
            pickIntent.putExtra(Intent.EXTRA_TITLE, getText(R.string.title_select_application));
            startActivityForResultSafely(pickIntent, REQUEST_PICK_APPLICATION);
        } else {
            startActivityForResultSafely(intent, REQUEST_CREATE_SHORTCUT);
        }

        getWorkspace().checkAndRemoveEmptyCell();
    }

    void processWallpaper(Intent intent) {
        startActivityForResult(intent, REQUEST_PICK_WALLPAPER);
    }

    FolderIcon addFolder(CellLayout layout, long container, final int screen, int cellX,
            int cellY) {
        final FolderInfo folderInfo = new FolderInfo();
        folderInfo.title = getText(R.string.folder_name);

        // Update the model
        LauncherModel.addItemToDatabase(Launcher.this, folderInfo, container, screen, cellX, cellY,
                false);
        sFolders.put(folderInfo.id, folderInfo);

        // Create the view
        FolderIcon newFolder =
            FolderIcon.fromXml(R.layout.folder_icon, this, layout, folderInfo);
        mWorkspace.addInScreen(newFolder, container, screen, cellX, cellY, 1, 1,
                isWorkspaceLocked());
        return newFolder;
    }

    void removeFolder(FolderInfo folder) {
        sFolders.remove(folder.id);
    }

    private void startWallpaper() {
        showWorkspace(true);
        final Intent pickWallpaper = new Intent(Intent.ACTION_SET_WALLPAPER);
        Intent chooser = Intent.createChooser(pickWallpaper,
                getText(R.string.chooser_wallpaper));
        // NOTE: Adds a configure option to the chooser if the wallpaper supports it
        //       Removed in Eclair MR1
//        WallpaperManager wm = (WallpaperManager)
//                getSystemService(Context.WALLPAPER_SERVICE);
//        WallpaperInfo wi = wm.getWallpaperInfo();
//        if (wi != null && wi.getSettingsActivity() != null) {
//            LabeledIntent li = new LabeledIntent(getPackageName(),
//                    R.string.configure_wallpaper, 0);
//            li.setClassName(wi.getPackageName(), wi.getSettingsActivity());
//            chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[] { li });
//        }
        startActivityForResult(chooser, REQUEST_PICK_WALLPAPER);
    }

    /**
     * Registers various content observers. The current implementation registers
     * only a favorites observer to keep track of the favorites applications.
     */
    private void registerContentObservers() {
        ContentResolver resolver = getContentResolver();
        resolver.registerContentObserver(LauncherProvider.CONTENT_APPWIDGET_RESET_URI,
                true, mWidgetObserver);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            switch (event.getKeyCode()) {
                case KeyEvent.KEYCODE_HOME:
                    return true;
                case KeyEvent.KEYCODE_VOLUME_DOWN:
                    if (isPropertyEnabled(DUMP_STATE_PROPERTY)) {
                        dumpState();
                        return true;
                    }
                    break;
                //added by qinjinchuan topwise for supporting direct page turning
                case KeyEvent.KEYCODE_PAGE_UP:
                    return true;
                case KeyEvent.KEYCODE_PAGE_DOWN:
                    return true;
                //added by qinjinchuan topwise for supporting direct page turning
            }
        } else if (event.getAction() == KeyEvent.ACTION_UP) {
            switch (event.getKeyCode()) {
                case KeyEvent.KEYCODE_HOME:
                    return true;
                case KeyEvent.KEYCODE_MENU:
                    closeFolder();
                    break;
                //added by qinjinchuan topwise for supporting direct page turning
                case KeyEvent.KEYCODE_PAGE_UP:
                	Log.d(TAG,"dispatchKeyEvent:KEYCODE_PAGE_UP");
                	mWorkspace.scrollLeft();
                	return true;
                case KeyEvent.KEYCODE_PAGE_DOWN:
                	Log.d(TAG,"dispatchKeyEvent:KEYCODE_PAGE_DOWN");
                	mWorkspace.scrollRight();
                	return true;
                //added by qinjinchuan topwise for supporting direct page turning
            }
        }

        return super.dispatchKeyEvent(event);
    }

    @Override
    public void onBackPressed() {
        boolean needGotoMainScreen = true;
        if (mIsEditMode) {
            needGotoMainScreen = false;
            exiteEditModeAnimation();
        }
        if (isAllAppsVisible()) {
            showWorkspace(true);
        } else if (mFlipAnim != null && mFlipAnim.isShowing()) {
            if (!mFlipAnim.isAnimating())
                stopFlipAnimation();
        } else if (mWorkspace.getOpenFolder() != null) {
            Folder openFolder = mWorkspace.getOpenFolder();
            if (openFolder.isEditingName()) {
                openFolder.dismissEditingName();
            } else {
                closeFolder();
            }
        } else if(mMenu!=null&&mMenu.isShowing()) {
            mMenu.dismiss();
        } else if (mWorkspaceLoading) {
            return;
        } else if( isHideseatShowing() ){
            hideHideseat(true);
        } else if (CheckVoiceCommandPressHelper.PUSH_TO_TALK_SUPPORT
                && CheckVoiceCommandPressHelper.getInstance().isVoiceUIShown()){
            CheckVoiceCommandPressHelper.getInstance().forceDismissVoiceCommand();
        } else if(needGotoMainScreen){
            mWorkspace.exitWidgetResizeMode();

            // Back button is a no-op here, but give at least some feedback for the button press
            // lixuhui when press back key and press menu key immediate, can not enter the edit mode, 
            // because the the workspace.isPageMoving() is true, so we no need execute this when in the default page
            if (mWorkspace.getCurrentPage() != ConfigManager.getDefaultScreen()) {
                mWorkspace.showOutlinesTemporarily();
            }
            gotoMainScreen();
        }
    }

    //go to default screen while pressing back key
    /**
     * go to default screen.
     */
    private void gotoMainScreen(){
        if (hasWindowFocus()) {
            if (mWorkspace.getCurrentPage() != ConfigManager.getDefaultScreen()) {
                mWorkspace.moveToDefaultScreen(true);
            }
        }
    }

    /**
     * Re-listen when widgets are reset.
     */
    private void onAppWidgetReset() {
        if (mAppWidgetHost != null) {
            mAppWidgetHost.startListening();
        }
    }

    /**
     * Launches the intent referred by the clicked shortcut.
     *
     * @param v The view representing the clicked shortcut.
     */
    public void onClick(View v) {
        // Make sure that rogue clicks don't get through while allapps is launching, or after the
        // view has detached (it's possible for this to happen if the view is removed mid touch).

        if (v.getWindowToken() == null) {
            Log.d(TAG, "sxsexe-----------------> onClick return by v.getWindowToken() is null");
            return;
        }

        if (!mWorkspace.isFinishedSwitchingState()) {
            Log.d(TAG, "sxsexe-----------------> onClick return by mWorkspace.isFinishedSwitchingState() " + mWorkspace.isFinishedSwitchingState());
            return;
        }

        if (mMenu != null && mMenu.isShowing()) {
            mMenu.dismiss();
        }

        if( LockScreenAnimator.getInstance(this).shouldPreventGesture() ){
            Log.d(TAG, "sxsexe-----------------> onClick return by shouldPreventGesture ");
            return;
        }

        if(!mOnClickValid) {
            Log.d(TAG, "sxsexe-----------------> onClick return by mOnClickValid ");
            mOnClickValid = true;
            return;
        }

        Object tag = v.getTag();
        Log.d(TAG, "sxsexe-----------------> onClick tag " + tag);
        if (tag instanceof ShortcutInfo) {
            if(v instanceof BubbleTextView) {
                BubbleTextView view = (BubbleTextView)v;
                view.setClicked(true);
                v.invalidate();
            }

            // Open shortcut
            ShortcutInfo info = ((ShortcutInfo) tag);

            if(((ShortcutInfo) tag).intent == null) {
                Log.d(TAG, "sxsexe-----------------> onClick return by intent == null ");
                return;
            }

            //create an new intent, not use the intent in shortcutinfo
            //to avoid shortcutinfo's intent change.
            final Intent intent = new Intent(((ShortcutInfo) tag).intent);

            int[] pos = new int[2];
            v.getLocationOnScreen(pos);
            intent.setSourceBounds(new Rect(pos[0], pos[1],
                    pos[0] + v.getWidth(), pos[1] + v.getHeight()));
            boolean success = false;
            //item type vpinstall need to be checked before isDownloading
            if (info.itemType == LauncherSettings.Favorites.ITEM_TYPE_VPINSTALL) {
                Log.d(TAG, "sxsexe----->vpinstall slience install");
                mModel.startVPSilentInstall(info);
            }
            else if(info.isDownloading()){
                success = onDownloadingClick(v);
            } else if (info.container == LauncherSettings.Favorites.CONTAINER_HIDESEAT) {
                // the app that is frozen in hide-seat cannot run
                if (mAppIsFrozenToast == null) {
                    mAppIsFrozenToast = Toast.makeText(this, R.string.application_unavailable_due_to_frozen, Toast.LENGTH_SHORT);
                }
                mAppIsFrozenToast.show();

                Map<String, String> param = new HashMap<String, String>();
                param.put("type", "app");
                Intent itemIntent = info.intent;
                if ((itemIntent != null) && (itemIntent.getComponent() != null)){
                    param.put("PkgName", itemIntent.getComponent().getPackageName());
                    UserTrackerHelper.sendUserReport(UserTrackerMessage.MSG_HIDESEAT_CLICK, param);
                }
            }else{
                if (isContactShortCut(info)) {
                    UserTrackerHelper.sendUserReport(UserTrackerMessage.MSG_DIRECT_DIAL_CLICK);
                }
                success = startActivitySafely(v, intent, tag);
                if (info.itemType == LauncherSettings.BaseLauncherColumns.ITEM_TYPE_APPLICATION
                        && info.container != LauncherSettings.Favorites.CONTAINER_HIDESEAT) {
                    ((LauncherApplication)getApplication()).collectUsageData(info.id);
                }
                if(info.container == LauncherSettings.Favorites.CONTAINER_HOTSEAT ){
               	    StringBuilder sb = new StringBuilder();
                    if(intent.getComponent() == null) {
             	        sb.append(intent.getAction());//for bookmark icon
                    } else {
		        sb.append(intent.getComponent().getPackageName());
		    }
                    sb.append(":").append(getCurrentWorkspaceScreen()+1).append(":").append(info.cellX + 1);
                    UserTrackerHelper.sendUserReport(UserTrackerMessage.MSG_START_APPLICATION_DOCK,sb.toString());
                }
            }

            if (success && v instanceof BubbleTextView) {
                mWaitingForResume = (BubbleTextView) v;
                info.setIsNewItem(false);
                mWaitingForResume.setIsNew(false);
                mWaitingForResume.updateView(info);
                mModel.modifyItemNewStatusInDatabase(this, info, false);
                mWaitingForResume.setStayPressed(true);

            }
            v.postInvalidateDelayed(300);
        } else if (tag instanceof FolderInfo) {
            if (v instanceof FolderIcon) {
                FolderIcon fi = (FolderIcon) v;
                handleFolderClick(fi);
            }
        } else if (v == mAllAppsButton) {
            if (isAllAppsVisible()) {
                showWorkspace(true);
            } else {
                onClickAllAppsButton(v);
            }
        } else if (v instanceof CellLayout) {
            CellLayout layout = (CellLayout) v;
            if (layout.isHideseatOpen) {
               hideHideseat(true);
            }
        }
    }

    private static boolean isContactShortCut(ItemInfo info) {
        return info instanceof ShortcutInfo && info.itemType ==
                LauncherSettings.BaseLauncherColumns.ITEM_TYPE_SHORTCUT &&
                ((ShortcutInfo)info).intent != null && Intent.ACTION_CALL
                .equals(((ShortcutInfo)info).intent.getAction());
    }

    public boolean onTouch(View v, MotionEvent event) {
        // this is an intercepted event being forwarded from mWorkspace;
        // clicking anywhere on the workspace causes the customization drawer to slide down
        showWorkspace(true);
        return false;
    }

    /**
     * Event handler for the "grid" button that appears on the home screen, which
     * enters all apps mode.
     *
     * @param v The view that was clicked.
     */
    public void onClickAllAppsButton(View v) {
        showAllApps(true);
    }

    public void onClickAppWidgetMenu() {
        mWorkspace.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS,
                HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING);
        UserTrackerHelper.entryPageBegin(UserTrackerMessage.LABEL_WIDGET_LOADER);
        UserTrackerHelper.sendUserReport(UserTrackerMessage.MSG_ENTRY_WIDGET_LOADER);
        showAllApps(true);
    }
    public void onTouchDownAllAppsButton(View v) {
        // Provide the same haptic feedback that the system offers for virtual keys.
        v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
    }

    void startApplicationDetailsActivity(ComponentName componentName) {
        String packageName = componentName.getPackageName();
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.fromParts("package", packageName, null));
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        startActivitySafely(null, intent, "startApplicationDetailsActivity");
    }

    void startApplicationUninstallActivity(final ApplicationInfo appInfo,final ShortcutInfo shortcutInfo, final Bitmap dragBitmap) {
        Log.d(TAG, "sxsexe---->startApplicationUninstallActivity app " + appInfo);
        boolean isSystemApp = (appInfo.flags & ApplicationInfo.DOWNLOADED_FLAG) == 0;
        boolean isUsbModeApp = (appInfo.flags & ApplicationInfo.SDCARD_FLAG) != 0
                               && Utils.isInUsbMode();
        if (isSystemApp || isUsbModeApp) {
            // SystemApp and sdcard-app(in usb mode) can't be uninstalled,show toast
            // We may give them the option of disabling apps this way.
            if (isUsbModeApp) {
                Toast.makeText(this, R.string.application_not_deleted_in_usb, Toast.LENGTH_SHORT).show();
            }
            reVisibileDraggedItem(appInfo);
            mWorkspace.checkAndRemoveEmptyCell();
        } else {
//            String packageName = appInfo.componentName.getPackageName();
//            String className = appInfo.componentName.getClassName();
//            Intent intent = new Intent(Intent.ACTION_DELETE, Uri.fromParts("package", packageName, className));
//            intent.setFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
//            startActivity(intent);
            final String packageName = appInfo.componentName.getPackageName();

            final IPackageDeleteObserver deleteObserver = new IPackageDeleteObserver.Stub(){
                @Override
                public void packageDeleted(String arg0, int arg1)
                        throws RemoteException {

                    //if failed to delete, revisible this app and toast
                    //1 means delete_succeed
                    if (arg1 == 1) {
                        // remove item after uninstalling successfully
                        // originally, the following section of code was called in
                        // positivelistener.onClick(). But this will cause icon is
                        // removed from UI but applicaiton is not deleted successfully
                        getWorkspace().removeDragItemFromList(appInfo);
                        checkAndReplaceFolderIfNecessary(appInfo);
                        UserTrackerHelper.sendUserReport(
                                UserTrackerMessage.MSG_REMOVE_APP,
                                (appInfo == null ? "" : appInfo.componentName
                                        .toString()));
                        // For hide-seat item, the database record should be removed when
                        // the uninstall succeed.
                        if (shortcutInfo.container == Favorites.CONTAINER_HIDESEAT) {
                            LauncherModel.deleteItemFromDatabase(Launcher.this, shortcutInfo);
                        }
                    } else {
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                reVisibileDraggedItem(appInfo);
                                Toast.makeText(getApplicationContext(), R.string.delete_failed,
                                        Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                    getWorkspace().checkAndRemoveEmptyCell();
                }
                };
            View.OnClickListener positivelistener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //if homeshell is system app, delete app, else start delete activity in system
                    if(AppUtil.isSystemApp(LauncherApplication.getContext(),LauncherApplication.getContext().getPackageName())){
                        PackageManager pm = getPackageManager();
                        ACA.PackageManager.deletePackage(pm, packageName, deleteObserver, 0);
                        if (isHideDeleteDialog()) {
                            mDeleteDialog.dismiss();
                        }
                    }else{
                        if (isHideDeleteDialog()) {
                            mDeleteDialog.dismiss();
                        }
                        Uri packageURI = Uri.parse("package:"+packageName);
                        Intent intent = new Intent(Intent.ACTION_DELETE,packageURI);
                        startActivity(intent);
                    }
                    // send download cancel broadcast to the AppStore.
                    sendDownLoadCancelBroadcastToAppStore(shortcutInfo);
                }
            };
            View.OnClickListener negativelistener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (isHideDeleteDialog()) {
                        mDeleteDialog.dismiss();
                        reVisibileDraggedItem(appInfo);
                    }
                }
            };

            Resources res = getResources();
    		if (mDeleteDialog == null)
    		    mDeleteDialog = new DropDownDialog(this);
        	PackageManager pm = getPackageManager();
    		Bitmap iconSrc = dragBitmap;
                String title = "";
    		try {
                    title = pm.getApplicationInfo(packageName, 0).loadLabel(pm).toString();
        	} catch (NameNotFoundException e) {
        	    e.printStackTrace();
        	}

            String hint = getResources().getString(R.string.uninstall_title);
            mDeleteDialog.setTitle(hint, title);
            if(iconSrc != null) {
                if (shortcutInfo.container == Favorites.CONTAINER_HIDESEAT) {
                    mDeleteDialog.setIcon(iconSrc, getHideseat().getDragView());
                } else {
                    if (getWorkspace().getDragInfo() != null) {
                        mDeleteDialog.setIcon(iconSrc, getWorkspace().getDragInfo().cell);
                    }
                }
            }
        	mDeleteDialog.setPositiveButton(res.getString(R.string.uninstall_app_confirm), positivelistener);
        	mDeleteDialog.setNegativeButton(res.getString(R.string.uninstall_app_cancel), negativelistener);
        	mDeleteDialog.setCanceledOnTouchOutside(false);
        	mDeleteDialog.setOnDismissListener(new OnDismissListener() {

                @Override
                public void onDismiss(DialogInterface dialog) {
                    getSearchBar().hideDropTargetBar(false);
                    mModel.getPackageUpdateTaskQueue().releaseLock();
                }
            });
        	mDeleteDialog.show();

            mModel.getPackageUpdateTaskQueue().retainLock("DeleteDialog");
        }
    }

    public void sendDownLoadCancelBroadcastToAppStore(final ShortcutInfo info) {
        if (info != null && info.getAppDownloadStatus() != AppDownloadStatus.STATUS_NO_DOWNLOAD) {
            String pkgName = info.intent.getComponent().getPackageName();
            Intent intent = new Intent(
                    AppDownloadManager.ACTION_HS_DOWNLOAD_TASK);
            intent.putExtra(AppDownloadManager.TYPE_ACTION,
                    AppDownloadManager.ACTION_HS_DOWNLOAD_CANCEL);
            intent.putExtra(AppDownloadManager.TYPE_PACKAGENAME, pkgName);
            sendBroadcast(intent);
            LauncherApplication app = (LauncherApplication) getApplicationContext();
            app.getModel().getAppDownloadManager()
                    .updatepPckageDownloadCancelTimeByHS(pkgName);
        }
    }

    void cancelFolderDismiss(final FolderInfo folderInfo) {
        if (isHideDeleteDialog()) {
            mDeleteDialog.dismiss();
        }
        reVisibileDraggedItem(folderInfo);
    }

    void dismissFolder(final FolderInfo folderInfo, Bitmap src) {

        // save FolderIcon before Workspace.mDragInfoDelete && Workspace.mDragInfo && CellLayout.mDragInfo
        // were changed in CellLayout.onInterceptTouchEvent
//        final FolderIcon folderIcon = (FolderIcon)getWorkspace().getDragInfo().cell;
    	//topwise zyf add 
         if(folderInfo instanceof FixedFolderInfo)
         {
        	 Log.d("sxsexe","dismissFolder  folderInfo = "+folderInfo);
        	 cancelFolderDismiss(folderInfo);
        	 return;
         }
    	//towpise zyf add end
        final FolderIcon folderIcon = (FolderIcon)getWorkspace().getDragItemFromList(folderInfo, false);

        View.OnClickListener positivelistener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //check empty cell count first
                int emptyCellCount = 0;
                if (((folderInfo.screen == 0) && (folderInfo.container == LauncherSettings.Favorites.CONTAINER_DESKTOP))
                   ||((folderInfo.container != LauncherSettings.Favorites.CONTAINER_DESKTOP) && (mWorkspace.getCurrentPage() == 0))) {
                    emptyCellCount = LauncherModel.calcEmptyCell(0);
                } else {
                    emptyCellCount = LauncherModel.calcEmptyCell(1);
                }
                Log.d(TAG, "empty cell count is " + emptyCellCount);
                if (folderInfo.container == LauncherSettings.Favorites.CONTAINER_DESKTOP) {
                    emptyCellCount = emptyCellCount + 1;
                }
                if (folderInfo.contents.size() > emptyCellCount) {
                    //not enough empty cell to take contents in folder
                    //don't dismiss the folder and recover the display
                    cancelFolderDismiss(folderInfo);
                    Toast.makeText(getApplicationContext(), R.string.folder_dismiss_cancelled,
                                                       Toast.LENGTH_SHORT).show();
                    return;
                }
                if (isHideDeleteDialog()) {
                    mDeleteDialog.dismiss();
                }
                mWorkspace.dismissFolder(folderIcon);
                mWorkspace.removeDragItemFromList(folderInfo);
                if(isContainerHotseat(folderInfo.container)){
                    getHotseat().onDrop(false, 0, null, null, true);
                }
            }
        };
        View.OnClickListener negativelistener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancelFolderDismiss(folderInfo);
            }
        };

        if (mDeleteDialog == null)
            mDeleteDialog = new DropDownDialog(this);
        String title = (String) folderInfo.title;
        String hint = getResources().getString(R.string.dismiss_folder_hint);
        mDeleteDialog.setTitle(hint, title);
        mDeleteDialog.setIcon(src,getWorkspace().getDragInfo().cell);

        mDeleteDialog.setPositiveButton(getResources().getString(R.string.dismiss_folder_confirm), positivelistener);
        mDeleteDialog.setNegativeButton(getResources().getString(R.string.dismiss_folder_cancel), negativelistener);
        mDeleteDialog.setCanceledOnTouchOutside(false);
        mDeleteDialog.setOnDismissListener(new OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                getSearchBar().hideDropTargetBar(false);
            }
        });
        mDeleteDialog.show();
    }

    void startApplicationUninstallActivity(ShortcutInfo shortcutInfo, Bitmap dragBitmap) {

        boolean storgeMount = Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
        boolean appNotMounted = !storgeMount && shortcutInfo.isSDApp != 0;
        if(appNotMounted) {
            reVisibileDraggedItem(shortcutInfo);
            Toast.makeText(this, R.string.application_not_deleted_in_usb, Toast.LENGTH_SHORT).show();
            return;
        }

        //convert shortCutInfo to ApplicationInfo
        if(shortcutInfo == null || shortcutInfo.intent == null || shortcutInfo.intent.getComponent() == null) {
            if (shortcutInfo != null) {
                Log.e(TAG,
                        "sxsexe startApplicationUninstallActivity This shortcut has no intent??? "
                                + shortcutInfo);
                LauncherModel.deleteItemFromDatabase(getApplicationContext(), shortcutInfo);
            }
            return;
        }
        String packageName = shortcutInfo.intent.getComponent().getPackageName();
        final PackageManager packageManager = getApplicationContext().getPackageManager();

        final Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        mainIntent.setPackage(packageName);
        final List<ResolveInfo> apps = packageManager.queryIntentActivities(mainIntent, 0);
        ApplicationInfo appInfo = null;
        if(apps == null || apps.size() == 0) {
            final boolean isFrozen = AppFreezeUtil.isPackageFrozen(getApplicationContext(), packageName);
            Log.d(TAG, "startApplicationUninstallActivity: failed to query appInfo." +
                       " isFrozen=" + isFrozen +
                       " shortcutInfo=" + shortcutInfo);

            if (isFrozen) {
                appInfo = new ApplicationInfo(packageManager, shortcutInfo.intent.getComponent(), shortcutInfo.title.toString(), null);
            } else if (shortcutInfo.container >= 0 ||
                       shortcutInfo.container == Favorites.CONTAINER_HIDESEAT) {
                Log.d(TAG, "startApplicationUninstallActivity: delete from db directly");
                LauncherModel.deleteItemFromDatabase(getApplicationContext(), shortcutInfo);
            } else {
                HashSet<ComponentName> sets = new HashSet<ComponentName>();
                sets.add(shortcutInfo.intent.getComponent());
                mWorkspace.removeItemsByComponentName(sets);
            }
            if (!isFrozen) {
                mModel.removeComponentFormAllAppList(packageName);
                checkAndReplaceFolderIfNecessary(shortcutInfo);
                Toast.makeText(this, R.string.sd_app_icon_del, Toast.LENGTH_SHORT).show();
                Log.e(TAG, "sxsexe startApplicationUninstallActivity why ResolveInfo is null??? pack " + packageName);
            }
            if (appInfo == null) return;
        }

        if (appInfo == null) {
            appInfo = new ApplicationInfo(packageManager, apps.get(0), null);
        }
        appInfo.container = shortcutInfo.container;
        appInfo.itemType = shortcutInfo.itemType;
        appInfo.id = shortcutInfo.id;
        appInfo.screen = shortcutInfo.screen;
        appInfo.cellX = shortcutInfo.cellX;
        appInfo.cellY = shortcutInfo.cellY;
        appInfo.spanX = shortcutInfo.spanX;
        appInfo.spanY = shortcutInfo.spanY;
        startApplicationUninstallActivity(appInfo,shortcutInfo,dragBitmap);
        DataCollector.getInstance(getApplicationContext())
                .collectDeleteAppData(shortcutInfo);
    }

    boolean startActivity(View v, Intent intent, Object tag) {
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        try {
            // Only launch using the new animation if the shortcut has not opted out (this is a
            // private contract between launcher and may be ignored in the future).
            boolean useLaunchAnimation = (v != null) &&
                    !intent.hasExtra(INTENT_EXTRA_IGNORE_LAUNCH_ANIMATION);
            if (useLaunchAnimation) {
                ActivityOptions opts = ActivityOptions.makeScaleUpAnimation(v, 0, 0,
                        v.getMeasuredWidth(), v.getMeasuredHeight());

                startActivity(intent, opts.toBundle());
            } else {
                startActivity(intent);
            }
            return true;
        } catch (SecurityException e) {
            Toast.makeText(this, R.string.activity_not_found, Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Launcher does not have the permission to launch " + intent +
                    ". Make sure to create a MAIN intent-filter for the corresponding activity " +
                    "or use the exported attribute for this activity. "
                    + "tag="+ tag + " intent=" + intent, e);
        }
        return false;
    }

    boolean startActivitySafely(View v, Intent intent, Object tag) {
        boolean success = false;
        try {
            if (mIsEditMode) {
                return false;   
            } else {
                success = startActivity(v, intent, tag);
            }
            // save activity's packageName to clear notification flag
            GadgetCardHelper.getInstance(this).onLaunchActivity(intent.getComponent());
            sendLauncherStayTimeMsg(v, intent, tag);
        } catch (ActivityNotFoundException e) {
            ShortcutInfo info = null;
            int id = ToastManager.APP_NOT_FOUND;
            if(tag instanceof ShortcutInfo){
                info = (ShortcutInfo)tag;
            }
            if (info != null) {
                if (info.isDownloading()) {
                    id = ToastManager.APP_IN_UPDATING;
                } else if (info.isSDApp > 0  && Utils.isInUsbMode()) {
                    id = ToastManager.APP_UNAVAILABLE_IN_USB;
                } else if (Hideseat.isHideseatEnabled() &&
                           AppFreezeUtil.isPackageFrozen(getApplicationContext(), info)) {
                    if (info.itemType == Favorites.ITEM_TYPE_APPLICATION) {
                        // The app has been moved out from hide-seat, but still in frozen state.
                        id = ToastManager.APP_UNAVAILABLE_BEING_UNFROZEN;
                        // In case of the unfreeze-broadcast is not sent correctly, send again here.
                        String pkgName = info.intent.getComponent().getPackageName();
                        AppFreezeUtil.asyncUnfreezePackage(getApplicationContext(), pkgName);
                    } else if (info.itemType == Favorites.ITEM_TYPE_SHORTCUT) {
                        // The original app is frozen
                        id = ToastManager.SHORTCUT_UNAVAILABLE_DUE_TO_FROZEN;
                    } else {
                        id = ToastManager.APP_NOT_FOUND;
                    }
                } else {
                    id = ToastManager.APP_NOT_FOUND;
                }
            }
            ToastManager.makeToast(id);
            Log.e(TAG, "Unable to launch. tag=" + tag + " intent=" + intent, e);
        }
        return success;
    }

    void startActivityForResultSafely(Intent intent, int requestCode) {
        try {
            startActivityForResult(intent, requestCode);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, R.string.activity_not_found, Toast.LENGTH_SHORT).show();
        } catch (SecurityException e) {
            Toast.makeText(this, R.string.activity_not_found, Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Launcher does not have the permission to launch " + intent +
                    ". Make sure to create a MAIN intent-filter for the corresponding activity " +
                    "or use the exported attribute for this activity.", e);
        }
    }

    public boolean isContainerFolder(long container) {
        return container !=  LauncherSettings.Favorites.CONTAINER_DESKTOP &&
                container !=  LauncherSettings.Favorites.CONTAINER_HOTSEAT &&
                container !=  LauncherSettings.Favorites.CONTAINER_HIDESEAT;
    }

    private boolean isContainerHideseat(long container) {
        return container ==  LauncherSettings.Favorites.CONTAINER_HIDESEAT;
    }

    public boolean isContainerHotseat(long container) {
        return container == LauncherSettings.Favorites.CONTAINER_HOTSEAT;
    }

    public boolean isContainerWorkspace(long container) {
        return container == LauncherSettings.Favorites.CONTAINER_DESKTOP;
    }

    private void handleFolderClick(FolderIcon folderIcon) {
        // fullscreen folder do not support dual-opening folder
        if(mFolderUtils.isFolderOpened())
            return;
        final FolderInfo info = folderIcon.getFolderInfo();
        Folder openFolder = mWorkspace.getFolderForTag(info);

        Log.d(TAG, "sxsexe-----> handleFolderClick openFolder " + openFolder + " mFolder " + mFolder + " info.opened " + info.opened);

        // If the folder info reports that the associated folder is open, then verify that
        // it is actually opened. There have been a few instances where this gets out of sync.
        if (info.opened && openFolder == null) {
            Log.d(TAG, "Folder info marked as open, but associated folder is not open. Screen: "
                    + info.screen + " (" + info.cellX + ", " + info.cellY + ")");
            info.opened = false;
        }
        Log.d(TAG, "opened:" + info.opened + "  destroyed:" + folderIcon.getFolder().isDestroyed());
        if (!info.opened && !folderIcon.getFolder().isDestroyed()) {
            Log.d(TAG, "1:");
            if (mIsEditMode) {
                return;
            }
//            closeFolder();
            closeFolderWithoutExpandAnimation();
            // Open the requested folder
            openFolder(folderIcon);
        } else {
            // Find the open folder...
            int folderScreen;
            if (openFolder != null) {
            	Log.d(TAG, "2:"+openFolder.toString());
                folderScreen = mWorkspace.getPageForView(openFolder);
                // .. and close it
                closeFolder(openFolder);
                if (folderScreen != mWorkspace.getCurrentPage()) {
                    // Close any folder open on the current screen
//                    closeFolder();
                    closeFolderWithoutExpandAnimation();
                    // Pull the folder onto this screen
                    openFolder(folderIcon);
                }
            }else if(mFolder != null) {
                Log.d(TAG, "3:"+mFolder.toString());
//                closeFolder();
                closeFolderWithoutExpandAnimation();
                openFolder(folderIcon);
                // openFolder(mFolder.getmFolderIcon());
            }
        }
    }

    /**
     * This method draws the FolderIcon to an ImageView and then adds and positions that ImageView
     * in the DragLayer in the exact absolute location of the original FolderIcon.
     */
    private void copyFolderIconToImage(FolderIcon fi) {
        final int width = fi.getMeasuredWidth();
        final int height = fi.getMeasuredHeight();

        // Lazy load ImageView, Bitmap and Canvas
        if (mFolderIconImageView == null) {
            mFolderIconImageView = new ImageView(this);
        }
        if (mFolderIconBitmap == null || mFolderIconBitmap.getWidth() != width ||
                mFolderIconBitmap.getHeight() != height) {
            mFolderIconBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            mFolderIconCanvas = new Canvas(mFolderIconBitmap);
        }

        DragLayer.LayoutParams lp;
        if (mFolderIconImageView.getLayoutParams() instanceof DragLayer.LayoutParams) {
            lp = (DragLayer.LayoutParams) mFolderIconImageView.getLayoutParams();
        } else {
            lp = new DragLayer.LayoutParams(width, height);
        }

        // The layout from which the folder is being opened may be scaled, adjust the starting
        // view size by this scale factor.
        float scale = mDragLayer.getDescendantRectRelativeToSelf(fi, mRectForFolderAnimation);
        lp.customPosition = true;
        lp.x = mRectForFolderAnimation.left;
        lp.y = mRectForFolderAnimation.top;
        lp.width = (int) (scale * width);
        lp.height = (int) (scale * height);

        mFolderIconCanvas.drawColor(0, PorterDuff.Mode.CLEAR);
        fi.draw(mFolderIconCanvas);
        mFolderIconImageView.setImageBitmap(mFolderIconBitmap);
        if (fi.getFolder() != null) {
            mFolderIconImageView.setPivotX(fi.getFolder().getPivotXForIconAnimation());
            mFolderIconImageView.setPivotY(fi.getFolder().getPivotYForIconAnimation());
        }
        // Just in case this image view is still in the drag layer from a previous animation,
        // we remove it and re-add it.
        if (mDragLayer.indexOfChild(mFolderIconImageView) != -1) {
            mDragLayer.removeView(mFolderIconImageView);
        }
        mDragLayer.addView(mFolderIconImageView, lp);
        if (fi.getFolder() != null) {
            fi.getFolder().bringToFront();
        }
    }

    private void growAndFadeOutFolderIcon(FolderIcon fi) {
        if (fi == null) return;
        PropertyValuesHolder alpha = PropertyValuesHolder.ofFloat("alpha", 0);
        PropertyValuesHolder scaleX = PropertyValuesHolder.ofFloat("scaleX", 1.5f);
        PropertyValuesHolder scaleY = PropertyValuesHolder.ofFloat("scaleY", 1.5f);

        FolderInfo info = (FolderInfo) fi.getTag();
        if (info.container == LauncherSettings.Favorites.CONTAINER_HOTSEAT) {
            CellLayout cl = (CellLayout) fi.getParent().getParent();
            CellLayout.LayoutParams lp = (CellLayout.LayoutParams) fi.getLayoutParams();
            cl.setFolderLeaveBehindCell(lp.cellX, lp.cellY);
        }
        //add hide icon container
        else if (info.container == LauncherSettings.Favorites.CONTAINER_HIDESEAT) {
            //maybe there has something other thing to do
            CellLayout cl = (CellLayout) fi.getParent().getParent();
            CellLayout.LayoutParams lp = (CellLayout.LayoutParams) fi.getLayoutParams();
            cl.setFolderLeaveBehindCell(lp.cellX, lp.cellY);
        }

        // Push an ImageView copy of the FolderIcon into the DragLayer and hide the original
        copyFolderIconToImage(fi);
        fi.setVisibility(View.INVISIBLE);

        ObjectAnimator oa = LauncherAnimUtils.ofPropertyValuesHolder(mFolderIconImageView, alpha,
                scaleX, scaleY);
        oa.setDuration(getResources().getInteger(R.integer.config_folderAnimDuration));
        oa.start();
    }

    private void shrinkAndFadeInFolderIcon(final FolderIcon fi) {
        if (fi == null) return;
        PropertyValuesHolder alpha = PropertyValuesHolder.ofFloat("alpha", 1.0f);
        PropertyValuesHolder scaleX = PropertyValuesHolder.ofFloat("scaleX", 1.0f);
        PropertyValuesHolder scaleY = PropertyValuesHolder.ofFloat("scaleY", 1.0f);

        final CellLayout cl = (CellLayout) fi.getParent().getParent();

        // We remove and re-draw the FolderIcon in-case it has changed
        mDragLayer.removeView(mFolderIconImageView);
        copyFolderIconToImage(fi);
        ObjectAnimator oa = LauncherAnimUtils.ofPropertyValuesHolder(mFolderIconImageView, alpha,
                scaleX, scaleY);
        oa.setDuration(getResources().getInteger(R.integer.config_folderAnimDuration));
        oa.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (cl != null) {
                    cl.clearFolderLeaveBehind();
                    // Remove the ImageView copy of the FolderIcon and make the original visible.
                    mDragLayer.removeView(mFolderIconImageView);
                    fi.setVisibility(View.VISIBLE);
                }
            }
        });
        oa.start();
    }

    /**
     * Opens the user folder described by the specified tag. The opening of the folder
     * is animated relative to the specified View. If the View is null, no animation
     * is played.
     *
     * @param folderInfo The FolderInfo describing the folder to open.
     */
    public void openFolder(final FolderIcon folderIcon) {
        // batch operations to the icons in folder
        // FolderUtils.addEditFolderShortcut(this, folderIcon);
        // when page moving,it can not open and play folder's animation
        if (getWorkspace().isPageMoving())
            return;

        Runnable openRun = new Runnable() {
            @Override
            public void run() {
                Folder folder = folderIcon.getFolder();
                FolderInfo info = folder.mInfo;

                info.opened = true;

                // Just verify that the folder hasn't already been added to the DragLayer.
                // There was a one-off crash where the folder had a parent already.
                if (folder.getParent() == null) {
                    mDragLayer.addView(folder);
                    mDragController.addDropTarget((DropTarget) folder);
                    // delete this code for new folder animation
                    // ForstedGlassUtils.setForstedGlassBackground(Launcher.this);
//            mWorkspace.setVisibility(View.GONE);
//            mHotseat.setVisibility(View.GONE);
//            mIndicatorView.setVisibility(View.GONE);
                } else {
                    Log.w(TAG, "Opening folder (" + folder + ") which already has a parent (" +
                            folder.getParent() + ").");
                }
                // added new animation for folder
//                folder.animateOpen();
                mFolderUtils.animateOpen(folder);

                // growAndFadeOutFolderIcon(folderIcon);

                // Notify the accessibility manager that this folder "window" has appeared and occluded
                // the workspace items
                folder.sendAccessibilityEvent(AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED);
                getDragLayer().sendAccessibilityEvent(AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED);
            }
        };

        if (isHideseatShowing()) {
            hideHideseat(false);
            mHandler.post(openRun);
        } else {
            openRun.run();
        }
        
        UnBindParticleService();
    }
    
    public boolean isInEditScreenMode() {
        return mEditMode;
    }

    public void exitScreenEditMode(boolean animate) {
        if (mEditMode) {
            UserTrackerHelper
                    .entryPageEnd(UserTrackerMessage.LABEL_SCREEN_MANAGER);

            if (mScreenManager != null) {
                mScreenManager.stop(animate, Boolean.valueOf(animate));
            // ##description: Added support for widget page
            mWorkspace.makeSureWidgetPages();
            }
        }
        if(isDraggingEnabled() && (mPaused == false)) {
        	Log.d(LIVE_WEATHER_TAG, "exitScreenEditMode begin");
        	UnBindParticleService();
        	OnBindParticleService();
        	Log.d(LIVE_WEATHER_TAG, "exitScreenEditMode end");
        }
    }

    public void exitScreenEditModeWithoutSave() {
        exitScreenEditMode(false);
            // ##description: Added support for widget page
            mWorkspace.makeSureWidgetPages();
    }

    public void enterScreenEditMode() {
        // it can not enter Screen edit Mode,
        // When the dialog is displayed
        if (mDeleteDialog != null && mDeleteDialog.isShowing()) {
            return;
        }

        if (mState == State.APPS_CUSTOMIZE_SPRING_LOADED) {
            // we should prevent entering
            // screen editor before exiting spring loader
            return;
        }

        if (mWorkspaceLoading || mWaitingForResult) {
            // we should prevent entering
            // screen editor when workspace is locked
            return;
        }

        if (AgedModeUtil.isAgedMode()) {
            return;
        }

        if (!mEditMode) {
            mWorkspace.removeWidgetPages();
            int currentScreenIndex = mWorkspace.getCurrentPage();
            CellLayout currentlayout = (CellLayout) mWorkspace
                    .getChildAt(currentScreenIndex);

            if (currentlayout == null) {
                Log.d(TAG, "ERROR:current page is null!!!");
                return;
            }

            mEditMode = true;
            mModel.getPackageUpdateTaskQueue().retainLock("ScreenEditMode");

            UserTrackerHelper
                    .entryPageBegin(UserTrackerMessage.LABEL_SCREEN_MANAGER);
            UserTrackerHelper
                    .sendUserReport(UserTrackerMessage.MSG_ENTRY_SCREEN_MANAGER);

            if (isHideseatShowing()) {
                hideHideseat(false);
            }

            for (int i = 0; i < mWorkspace.getChildCount(); i++) {
                CellLayout cellLayout = (CellLayout) mWorkspace.getChildAt(i);
                if (cellLayout != null) {
                    cellLayout.cancelFlingDropDownAnimation();
                }
            }

            mIndicatorView.setVisibility(View.GONE);
            mHotseat.setVisibility(View.GONE);
            if (mScreenManager == null) {
                mScreenManager = new ScreenManager(this);
                getDragLayer().addView(mScreenManager.getRootView());
                mScreenManager.setScreenManagerListener(new ScreenManager.ScreenManagerListener() {

                    @Override
                    public void onExit(Object stopTag) {
                        mEditMode = false;
                        if((Boolean) stopTag) {
                            saveScreenChange();
                        }
                        mModel.getPackageUpdateTaskQueue().releaseLock();
                        mWorkspace.setVisibility(View.VISIBLE);
                        mHotseat.setVisibility(View.VISIBLE);
                        mIndicatorView.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onItemClick(int index) {
                        if (mEditMode) {
                            UserTrackerHelper
                                    .entryPageEnd(UserTrackerMessage.LABEL_SCREEN_MANAGER);

                            if (mScreenManager != null) {
                                mScreenManager.stop(Boolean.TRUE, index);
                            }
                        }
                    }
                });
            }

            if(!mScreenManager.start()) {
                mEditMode = false;
                return;
            }
            mScreenManager.getRootView().setVisibility(View.VISIBLE);
        }
    }

    private ScreenManager mScreenManager;

    private void saveScreenChange() {
        if (mScreenManager != null) {
            List<Integer> newIndexs = mScreenManager.getNewIndexs();
            int currentPage = mScreenManager.getCurrentPage();
            screenExchange(newIndexs, currentPage);
            LauncherModel.reArrageScreen(Launcher.this, newIndexs);
        }
    }

    private void screenExchange(final List<Integer> newIndexs, int currentScreen) {
        if (newIndexs == null) {
            return;
        }

        int count = newIndexs.size();
        CellLayout[] layouts = new CellLayout[count];
        for (int i = count -1; i >= 0; i--) {
             layouts[i] = (CellLayout)mWorkspace.getChildAt(i);
             mWorkspace.removeViewAt(i);
        }

        for (int i = 0; i < count; i++) {
            int j = newIndexs.get(i);
            mWorkspace.addView(layouts[j]);
        }
        mWorkspace.setCurrentPage(currentScreen);
        mIndicatorView.setCurrentPos(currentScreen + 1);
    }

    public void closeFolderWithoutExpandAnimation() {
        closeFolderWithoutExpandAnimation(true);
    }
    public void closeFolderWithoutExpandAnimation(boolean anim) {
        final Folder folder = mWorkspace.getOpenFolder();
        if (folder != null) {
            // batch operations to the icons in folder
            folder.closeSelectApps();
            FolderUtils.removeEditFolderShortcut(folder.getInfo());

            folder.hideSoftInputMethod(null);

            if (folder.isEditingName()) {
                folder.dismissEditingName();
            }
            folder.getInfo().opened = false;
            mFolderUtils.animateClosed(folder, false, anim);
            // Notify the accessibility manager that this folder "window" has disappeard and no
            // longer occludeds the workspace items
            getDragLayer().sendAccessibilityEvent(AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED);
        }
    }

    public void closeFolder() {
        closeFolder(0);
    }
    public void closeFolder(int delay) {
        final Folder folder = mWorkspace.getOpenFolder();
        if (folder != null) {
            folder.dismissEditingName();
            // hide IME will cause window animation so we close folder some ms delay
            if (delay > 0) {
                mWorkspace.postDelayed(new Runnable() {
                    public void run() {
                        closeFolder(folder);
                    }
                }, delay);
            } else {
                closeFolder(folder);
            }
        }
    }

    void closeFolder(final Folder folder) {
        folder.getEditTextRegion().clearFocus();
        folder.closeSelectApps();
        FolderUtils.removeEditFolderShortcut(folder.getInfo());
        folder.getInfo().opened = false;

//        ViewGroup parent = (ViewGroup) folder.getParent().getParent();
//        if (parent != null) {
//            FolderIcon fi = (FolderIcon) mWorkspace.getViewForTag(folder.mInfo);
//            shrinkAndFadeInFolderIcon(fi);
//        }

        mFolderUtils.animateClosed(folder, true, getWindow() != null);
//        this.mHandler.post(new Runnable(){
//            public void run(){
//                mWorkspace.setVisibility(View.VISIBLE);
//                mHotseat.setVisibility(View.VISIBLE);
//                mIndicatorView.setVisibility(View.VISIBLE);
//                ForstedGlassUtils.clearForstedGlassBackground(Launcher.this);
//            }
//        });

        // Notify the accessibility manager that this folder "window" has disappeard and no
        // longer occludeds the workspace items
        getDragLayer().sendAccessibilityEvent(AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED);
        mWorkspace.postDelayed(new Runnable() {
            public void run() {
                folder.hideSoftInputMethod(Launcher.this);
            }
        }, 200);
        
        OnBindParticleService();
    }

    public boolean onLongClick(View v) {
        try {
            if (mSupportLifeCenter) {
                if (mDragLayer.isLeftPageMode()) {
                    return true;
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "life center error " , e);
        }

        if( LockScreenAnimator.getInstance(this).shouldPreventGesture() ) return false;
        if( mGestureLayer.getPointerCount() > 1 ) return false;
        if (mWorkspace.iscurrWidgetPage()) return false;    //added by qinjinchuan topwise for disabling long-click-hotseat in widget page

        if (!isDraggingEnabled()) return false;
        //add toast in lock mode
        /*if (mModel.isDownloadStatus()) {
            ToastManager.makeToast(ToastManager.NOT_ALLOW_EDIT_IN_DOWNING);
            //return true;
        }*/

        Log.d(TAG,
                "onLongClick mWorkspaceLoading || mWaitingForResult || mModel.isDownloadStatus() "
                        + mWorkspaceLoading + " - " + mWaitingForResult + " - "
                        + mModel.isDownloadStatus());
        if (mWorkspaceLoading || mWaitingForResult) return false;
        Log.d(TAG, "onLongClick mState " + mState);
        if (mState != State.WORKSPACE) return false;

        if (!(v instanceof CellLayout)) {
            do{
                ViewParent p = v.getParent();
                if( p == null || !(p instanceof View)) {
                    Log.d(TAG,"onLongClick getParent : " + p );
                    return true;
                }
                v = (View)p;
            }while( !(v instanceof CellLayout));
        }

        resetAddInfo();
        CellLayout.CellInfo longClickCellInfo = (CellLayout.CellInfo) v.getTag();
        // This happens when long clicking an item with the dpad/trackball
        if (longClickCellInfo == null) {
            return true;
        }

        // The hotseat touch handling does not go through Workspace, and we always allow long press
        // on hotseat items.
        final View itemUnderLongClick = longClickCellInfo.cell;
        boolean allowLongPress = isHotseatLayout(v) || (mWorkspace != null && mWorkspace.allowLongPress());
        if (allowLongPress && !mDragController.isDragging()) {
            if (itemUnderLongClick == null) {
                // User long pressed on empty space
                // mWorkspace.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS,
                // HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING);
                enterEditMode();
                return true;
                // //just go to widget list
                // UserTrackerHelper
                // .entryPageBegin(UserTrackerMessage.LABEL_WIDGET_LOADER);
                // UserTrackerHelper
                // .sendUserReport(UserTrackerMessage.MSG_ENTRY_WIDGET_LOADER);
                // showAllApps(true);
                //startWallpaper();
            } else {
                if (HomeShellSetting.getFreezeValue(this)) {
                    boolean toastShow = true;
                    if(CheckVoiceCommandPressHelper.PUSH_TO_TALK_SUPPORT) {
                        Object object = itemUnderLongClick.getTag();
                        if(object  instanceof ShortcutInfo) {
                            toastShow = !CheckVoiceCommandPressHelper.getInstance().isVoiceApp((ShortcutInfo) object);
                        }
                    }
                    if(toastShow) {
                        Toast.makeText(this, R.string.aged_freeze_homeshell_toast, Toast.LENGTH_SHORT).show();
                    }
                    return true;
                }

                if (!(itemUnderLongClick instanceof Folder)) {
                    Log.d(TAG, "onLongClick itemUnderLongclick not instanceof Folder");
                    // User long pressed on an item
                    mWorkspace.startDrag(longClickCellInfo, mWorkspace);
                }
            }
        }
        return true;
    }

    boolean isHotseatLayout(View layout) {
        return mHotseat != null
               && mHotseat.getVisibility() == View.VISIBLE
               && layout != null
               && (layout instanceof CellLayout)
               && (layout == mHotseat.getLayout());
    }

    public Hotseat getHotseat() {
        return mHotseat;
    }

    public Hideseat getHideseat() {
        return mHideseat;
    }

    public CustomHideseat getCustomHideseat() {
        return mCustomHideseat;
    }

    SearchDropTargetBar getSearchBar() {
        return mSearchDropTargetBar;
    }

    /**
     * Returns the CellLayout of the specified container at the specified screen.
     */
    CellLayout getCellLayout(long container, int screen) {
        if (container == LauncherSettings.Favorites.CONTAINER_HOTSEAT) {
            if (mHotseat != null) {
                return mHotseat.getLayout();
            } else {
                return null;
            }
        }
        //add hide icon container
        else if (container == LauncherSettings.Favorites.CONTAINER_HIDESEAT) {
            return null;
        }
        else {
            return (CellLayout) mWorkspace.getChildAt(screen);
        }
    }

    public Workspace getWorkspace() {
        return mWorkspace;
    }

    // Now a part of LauncherModel.Callbacks. Used to reorder loading steps.
    @Override
    public boolean isAllAppsVisible() {
        return (mState == State.APPS_CUSTOMIZE) || (mOnResumeState == State.APPS_CUSTOMIZE);
    }

    @Override
    public boolean isAllAppsButtonRank(int rank) {
        return /*mHotseat.isAllAppsButtonRank(rank)*/ false;
    }

    /**
     * Helper method for the cameraZoomIn/cameraZoomOut animations
     * @param view The view being animated
     * @param scaleFactor The scale factor used for the zoom
     */
    private void setPivotsForZoom(View view, float scaleFactor) {
        view.setPivotX(view.getWidth() / 2.0f);
        view.setPivotY(view.getHeight() / 2.0f);
    }

    void disableWallpaperIfInAllApps() {
        // because in widgets list we should show wallpaper as background
        /*if (isAllAppsVisible()) {
            if (mAppsCustomizeTabHost != null &&
                    !mAppsCustomizeTabHost.isTransitioning()) {
                updateWallpaperVisibility(false);
            }
        }*/
    }

    private void setWorkspaceBackground(boolean workspace) {
        mLauncherView.setBackground(workspace ?
                mWorkspaceBackgroundDrawable : null);
    }

    void updateWallpaperVisibility(boolean visible) {
        int wpflags = visible ? WindowManager.LayoutParams.FLAG_SHOW_WALLPAPER : 0;
        int curflags = getWindow().getAttributes().flags
                & WindowManager.LayoutParams.FLAG_SHOW_WALLPAPER;
        if (wpflags != curflags) {
            getWindow().setFlags(wpflags, WindowManager.LayoutParams.FLAG_SHOW_WALLPAPER);
        }
        setWorkspaceBackground(visible);
    }

    private void dispatchOnLauncherTransitionPrepare(View v, boolean animated, boolean toWorkspace) {
        if (v instanceof LauncherTransitionable) {
            ((LauncherTransitionable) v).onLauncherTransitionPrepare(this, animated, toWorkspace);
        }
    }

    private void dispatchOnLauncherTransitionStart(View v, boolean animated, boolean toWorkspace) {
        if (v instanceof LauncherTransitionable) {
            ((LauncherTransitionable) v).onLauncherTransitionStart(this, animated, toWorkspace);
        }

        // Update the workspace transition step as well
        dispatchOnLauncherTransitionStep(v, 0f);
    }

    private void dispatchOnLauncherTransitionStep(View v, float t) {
        if (v instanceof LauncherTransitionable) {
            ((LauncherTransitionable) v).onLauncherTransitionStep(this, t);
        }
    }

    private void dispatchOnLauncherTransitionEnd(View v, boolean animated, boolean toWorkspace) {
        if (v instanceof LauncherTransitionable) {
            ((LauncherTransitionable) v).onLauncherTransitionEnd(this, animated, toWorkspace);
        }

        // Update the workspace transition step as well
        dispatchOnLauncherTransitionStep(v, 1f);
    }

    /**
     * Things to test when changing the following seven functions.
     *   - Home from workspace
     *          - from center screen
     *          - from other screens
     *   - Home from all apps
     *          - from center screen
     *          - from other screens
     *   - Back from all apps
     *          - from center screen
     *          - from other screens
     *   - Launch app from workspace and quit
     *          - with back
     *          - with home
     *   - Launch app from all apps and quit
     *          - with back
     *          - with home
     *   - Go to a screen that's not the default, then all
     *     apps, and launch and app, and go back
     *          - with back
     *          -with home
     *   - On workspace, long press power and go back
     *          - with back
     *          - with home
     *   - On all apps, long press power and go back
     *          - with back
     *          - with home
     *   - On workspace, power off
     *   - On all apps, power off
     *   - Launch an app and turn off the screen while in that app
     *          - Go back with home key
     *          - Go back with back key  TODO: make this not go to workspace
     *          - From all apps
     *          - From workspace
     *   - Enter and exit car mode (becuase it causes an extra configuration changed)
     *          - From all apps
     *          - From the center workspace
     *          - From another workspace
     */

    /**
     * Zoom the camera out from the workspace to reveal 'toView'.
     * Assumes that the view to show is anchored at either the very top or very bottom
     * of the screen.
     */
    private void showAppsCustomizeHelper(final boolean animated, final boolean springLoaded) {
        if (mStateAnimation != null) {
            mStateAnimation.setDuration(0);
            mStateAnimation.cancel();
            mStateAnimation = null;
        }
        final Resources res = getResources();

        final int duration = res.getInteger(R.integer.config_appsCustomizeZoomInTime);
        final int fadeDuration = res.getInteger(R.integer.config_appsCustomizeFadeInTime);
        final float scale = (float) res.getInteger(R.integer.config_appsCustomizeZoomScaleFactor);
        final View fromView = mWorkspace;
        final AppsCustomizeTabHost toView = mAppsCustomizeTabHost;
        final int startDelay =
                res.getInteger(R.integer.config_workspaceAppsCustomizeAnimationStagger);

        setPivotsForZoom(toView, scale);

        mAppsCustomizeContent.setBulkBind(false, true);

        // Shrink workspaces away if going to AppsCustomize from workspace
        Animator workspaceAnim =
                mWorkspace.getChangeStateAnimation(Workspace.State.SMALL, animated);
        if (animated) {
            toView.setScaleX(scale);
            toView.setScaleY(scale);
            final LauncherViewPropertyAnimator scaleAnim = new LauncherViewPropertyAnimator(toView);
            scaleAnim.
                scaleX(1f).scaleY(1f).
                setDuration(duration).
                setInterpolator(new Workspace.ZoomOutInterpolator());

            toView.setVisibility(View.VISIBLE);
            toView.setAlpha(0f);
            final ObjectAnimator alphaAnim = LauncherAnimUtils
                .ofFloat(toView, "alpha", 0f, 1f)
                .setDuration(fadeDuration);
            alphaAnim.setInterpolator(new DecelerateInterpolator(1.5f));
            alphaAnim.addUpdateListener(new AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    if (animation == null) {
                        throw new NullPointerException("animation is null");
                    }
                    float t = (Float) animation.getAnimatedValue();
                    dispatchOnLauncherTransitionStep(fromView, t);
                    dispatchOnLauncherTransitionStep(toView, t);
                }
            });

            // toView should appear right at the end of the workspace shrink
            // animation
            mStateAnimation = LauncherAnimUtils.createAnimatorSet();
            mStateAnimation.play(scaleAnim).after(startDelay);
            mStateAnimation.play(alphaAnim).after(startDelay);

            mStateAnimation.addListener(new AnimatorListenerAdapter() {
                boolean animationCancelled = false;

                @Override
                public void onAnimationStart(Animator animation) {
                    updateWallpaperVisibility(true);
                    // Prepare the position
                    toView.setTranslationX(0.0f);
                    toView.setTranslationY(0.0f);
                    toView.setVisibility(View.VISIBLE);
                    toView.bringToFront();
                }
                @Override
                public void onAnimationEnd(Animator animation) {
                    dispatchOnLauncherTransitionEnd(fromView, animated, false);
                    dispatchOnLauncherTransitionEnd(toView, animated, false);

                    if (mWorkspace != null && !springLoaded && !LauncherApplication.isScreenLarge()) {
                        // Hide the workspace scrollbar
                        mWorkspace.hideScrollingIndicator(true);
                        hideDockDivider();
                    }
                    if (!animationCancelled) {
                        updateWallpaperVisibility(true);
                    }

                    if(mAppsCustomizeContent != null) {
                        mAppsCustomizeContent.invalidatePageIndicator(true);
                    }
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                    animationCancelled = true;
                }
            });

            if (workspaceAnim != null) {
                Animator hotseatAnima = getHotseatAnimator(true);  //daiwei modify
                mStateAnimation.play(workspaceAnim).with(hotseatAnima);
            }

            boolean delayAnim = false;

            dispatchOnLauncherTransitionPrepare(fromView, animated, false);
            dispatchOnLauncherTransitionPrepare(toView, animated, false);

            // If any of the objects being animated haven't been measured/laid out
            // yet, delay the animation until we get a layout pass
            if ((((LauncherTransitionable) toView).getContent().getMeasuredWidth() == 0) ||
                    (mWorkspace.getMeasuredWidth() == 0) ||
                    (toView.getMeasuredWidth() == 0)) {
                delayAnim = true;
            }

            final AnimatorSet stateAnimation = mStateAnimation;
            final Runnable startAnimRunnable = new Runnable() {
                public void run() {
                    // Check that mStateAnimation hasn't changed while
                    // we waited for a layout/draw pass
                    if (mStateAnimation != stateAnimation)
                        return;
                    setPivotsForZoom(toView, scale);
                    dispatchOnLauncherTransitionStart(fromView, animated, false);
                    dispatchOnLauncherTransitionStart(toView, animated, false);
                    LauncherAnimUtils.startAnimationAfterNextDraw(mStateAnimation, toView);
                }
            };
            if (delayAnim) {
                final ViewTreeObserver observer = toView.getViewTreeObserver();
                observer.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
                        public void onGlobalLayout() {
                            startAnimRunnable.run();
                            toView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        }
                    });
            } else {
                startAnimRunnable.run();
            }
        } else {
            toView.setTranslationX(0.0f);
            toView.setTranslationY(0.0f);
            toView.setScaleX(1.0f);
            toView.setScaleY(1.0f);
            toView.setVisibility(View.VISIBLE);
            toView.bringToFront();

            if (!springLoaded && !LauncherApplication.isScreenLarge()) {
                // Hide the workspace scrollbar
                mWorkspace.hideScrollingIndicator(true);
                hideDockDivider();
            }
            dispatchOnLauncherTransitionPrepare(fromView, animated, false);
            dispatchOnLauncherTransitionStart(fromView, animated, false);
            dispatchOnLauncherTransitionEnd(fromView, animated, false);
            dispatchOnLauncherTransitionPrepare(toView, animated, false);
            dispatchOnLauncherTransitionStart(toView, animated, false);
            dispatchOnLauncherTransitionEnd(toView, animated, false);
            updateWallpaperVisibility(true);
            mAppsCustomizeContent.invalidatePageIndicator(true);
        }
    }

    /**
     * Zoom the camera back into the workspace, hiding 'fromView'.
     * This is the opposite of showAppsCustomizeHelper.
     * @param animated If true, the transition will be animated.
     */
    private void hideAppsCustomizeHelper(State toState, final boolean animated,
            final boolean springLoaded, final Runnable onCompleteRunnable) {

        if (mStateAnimation != null) {
            mStateAnimation.setDuration(0);
            mStateAnimation.cancel();
            mStateAnimation = null;
        }
        Resources res = getResources();

        final int duration = res.getInteger(R.integer.config_appsCustomizeZoomOutTime);
        final int fadeOutDuration =
                res.getInteger(R.integer.config_appsCustomizeFadeOutTime);
        final float scaleFactor = (float)
                res.getInteger(R.integer.config_appsCustomizeZoomScaleFactor);
        final View fromView = mAppsCustomizeTabHost;
        final View toView = mWorkspace;
        Animator workspaceAnim = null;

        if (toState == State.WORKSPACE) {
            int stagger = res.getInteger(R.integer.config_appsCustomizeWorkspaceAnimationStagger);
            workspaceAnim = mWorkspace.getChangeStateAnimation(
                    Workspace.State.NORMAL, animated, stagger);
        } else if (toState == State.APPS_CUSTOMIZE_SPRING_LOADED) {
            workspaceAnim = mWorkspace.getChangeStateAnimation(
                    Workspace.State.SPRING_LOADED, animated);
        }

        setPivotsForZoom(fromView, scaleFactor);
        updateWallpaperVisibility(true);
        showHotseat(animated);
        if (animated) {
            final LauncherViewPropertyAnimator scaleAnim =
                    new LauncherViewPropertyAnimator(fromView);
            scaleAnim.
                scaleX(scaleFactor).scaleY(scaleFactor).
                setDuration(duration).
                setInterpolator(new Workspace.ZoomInInterpolator());

            final ObjectAnimator alphaAnim = LauncherAnimUtils
                .ofFloat(fromView, "alpha", 1f, 0f)
                .setDuration(fadeOutDuration);
            alphaAnim.setInterpolator(new AccelerateDecelerateInterpolator());
            alphaAnim.addUpdateListener(new AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    float t = 1f - (Float) animation.getAnimatedValue();
                    dispatchOnLauncherTransitionStep(fromView, t);
                    dispatchOnLauncherTransitionStep(toView, t);
                }
            });

            mStateAnimation = LauncherAnimUtils.createAnimatorSet();

            dispatchOnLauncherTransitionPrepare(fromView, animated, true);
            dispatchOnLauncherTransitionPrepare(toView, animated, true);
            mAppsCustomizeContent.pauseScrolling();

            mStateAnimation.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    updateWallpaperVisibility(true);
                    fromView.setVisibility(View.GONE);
                    dispatchOnLauncherTransitionEnd(fromView, animated, true);
                    dispatchOnLauncherTransitionEnd(toView, animated, true);
                    if (mWorkspace != null) {
                        mWorkspace.hideScrollingIndicator(false);
                    }
                    if (onCompleteRunnable != null) {
                        onCompleteRunnable.run();
                    }
                    mAppsCustomizeContent.updateCurrentPageScroll();
                    mAppsCustomizeContent.resumeScrolling();
                }
            });

            mStateAnimation.playTogether(scaleAnim, alphaAnim);
            if (workspaceAnim != null) {
                mStateAnimation.play(workspaceAnim);
            }
            if (toState == State.WORKSPACE) {
                Animator hotseatAnima = getHotseatAnimator(false);  //daiwei modify 
                mStateAnimation.play(hotseatAnima);
            }
            dispatchOnLauncherTransitionStart(fromView, animated, true);
            dispatchOnLauncherTransitionStart(toView, animated, true);
            LauncherAnimUtils.startAnimationAfterNextDraw(mStateAnimation, toView);
        } else {
            fromView.setVisibility(View.GONE);
            dispatchOnLauncherTransitionPrepare(fromView, animated, true);
            dispatchOnLauncherTransitionStart(fromView, animated, true);
            dispatchOnLauncherTransitionEnd(fromView, animated, true);
            dispatchOnLauncherTransitionPrepare(toView, animated, true);
            dispatchOnLauncherTransitionStart(toView, animated, true);
            dispatchOnLauncherTransitionEnd(toView, animated, true);
            mWorkspace.hideScrollingIndicator(false);
            mHotseat.revisibleHotseat();
        }
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        if (level >= ComponentCallbacks2.TRIM_MEMORY_MODERATE) {
            mAppsCustomizeTabHost.onTrimMemory();
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        if (!hasFocus) {
           if(mDeleteDialog == null || !mDeleteDialog.isShowing()) {
               if (isHideseatShowing()) {
                   hideHideseat(false);
               }
           } // When another window occludes launcher (like the notification shade, or recents),
            // ensure that we enable the wallpaper flag so that transitions are done correctly.
            updateWallpaperVisibility(true);
//            if(mMenu!=null&&mMenu.isShowing()){
//                mMenu.dismiss();
//            }
        }  /*else {
            // When launcher has focus again, disable the wallpaper if we are in AllApps
           mWorkspace.postDelayed(new Runnable() {
                @Override
                public void run() {
                    disableWallpaperIfInAllApps();
                }
            }, 500);
        }*/

        if( !hasFocus ){
            LockScreenAnimator.getInstance(this).restoreIfNeeded();
        }
    }

    void showWorkspace(boolean animated) {
        showWorkspace(animated, null);
    }

    void showWorkspace(boolean animated, Runnable onCompleteRunnable) {
        if (mState != State.WORKSPACE) {
            boolean wasInSpringLoadedMode = (mState == State.APPS_CUSTOMIZE_SPRING_LOADED);
            mWorkspace.setVisibility(View.VISIBLE);
            hideAppsCustomizeHelper(State.WORKSPACE, animated, false, onCompleteRunnable);

            // We only need to animate in the dock divider if we're going from spring loaded mode
            showDockDivider(animated && wasInSpringLoadedMode);

            // Set focus to the AppsCustomize button
            if (mAllAppsButton != null) {
                mAllAppsButton.requestFocus();
            }
        }

        mWorkspace.flashScrollingIndicator(animated);

        // Change the state *after* we've called all the transition code
        mState = State.WORKSPACE;

        // Resume the auto-advance of widgets
        mUserPresent = true;
        updateRunning();

        // Send an accessibility event to announce the context change
        getWindow().getDecorView()
                .sendAccessibilityEvent(AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED);

        mIsAllAppShowed = false;
        setOnClickValid(true);
    }

    void showAllApps(boolean animated) {
        if (isHideseatShowing()) {
            hideHideseat(false);
        }
        if (mState != State.WORKSPACE) return;

        showAppsCustomizeHelper(animated, false);
        mAppsCustomizeTabHost.requestFocus();

        // Change the state *after* we've called all the transition code
        mState = State.APPS_CUSTOMIZE;

        // Pause the auto-advance of widgets until we are out of AllApps
        mUserPresent = false;
        updateRunning();
//        closeFolder();
        closeFolderWithoutExpandAnimation();

        // Send an accessibility event to announce the context change
        getWindow().getDecorView()
                .sendAccessibilityEvent(AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED);

        mIsAllAppShowed  = true;
    }

    void enterSpringLoadedDragMode() {
        if (isAllAppsVisible()) {
            hideAppsCustomizeHelper(State.APPS_CUSTOMIZE_SPRING_LOADED, true, true, null);
            hideDockDivider();
            mState = State.APPS_CUSTOMIZE_SPRING_LOADED;
            getWorkspace().invalidatePageIndicator(true);
        }
    }

    void exitSpringLoadedDragModeDelayed(final boolean successfulDrop, boolean extendedDelay,
            final Runnable onCompleteRunnable) {
        if (mState != State.APPS_CUSTOMIZE_SPRING_LOADED) return;
        UserTrackerHelper.entryPageEnd(UserTrackerMessage.LABEL_WIDGET_LOADER);

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (successfulDrop) {
                    // Before we show workspace, hide all apps again because
                    // exitSpringLoadedDragMode made it visible. This is a bit hacky; we should
                    // clean up our state transition functions
                    mAppsCustomizeTabHost.setVisibility(View.GONE);
                    showWorkspace(true, onCompleteRunnable);
                } else {
                    exitSpringLoadedDragMode();
                }
                getWorkspace().checkAndRemoveEmptyCell();
            }
        }, (extendedDelay ?
                EXIT_SPRINGLOADED_MODE_LONG_TIMEOUT :
                EXIT_SPRINGLOADED_MODE_SHORT_TIMEOUT));
    }

    void exitSpringLoadedDragMode() {
        if (mState == State.APPS_CUSTOMIZE_SPRING_LOADED) {
            final boolean animated = true;
            final boolean springLoaded = true;
            showAppsCustomizeHelper(animated, springLoaded);
            mState = State.APPS_CUSTOMIZE;
            getWorkspace().checkAndRemoveEmptyCell();
        }
        // Otherwise, we are not in spring loaded mode, so don't do anything.
    }

    void hideDockDivider() {
        if (mQsbDivider != null && mDockDivider != null) {
            mQsbDivider.setVisibility(View.INVISIBLE);
            mDockDivider.setVisibility(View.INVISIBLE);
        }
    }

    void showDockDivider(boolean animated) {
        if (mQsbDivider != null && mDockDivider != null) {
            mQsbDivider.setVisibility(View.VISIBLE);
            mDockDivider.setVisibility(View.VISIBLE);
            if (mDividerAnimator != null) {
                mDividerAnimator.cancel();
                mQsbDivider.setAlpha(1f);
                mDockDivider.setAlpha(1f);
                mDividerAnimator = null;
            }
            if (animated) {
                mDividerAnimator = LauncherAnimUtils.createAnimatorSet();
                mDividerAnimator.playTogether(LauncherAnimUtils.ofFloat(mQsbDivider, "alpha", 1f),
                        LauncherAnimUtils.ofFloat(mDockDivider, "alpha", 1f));
                int duration = 0;
                if (mSearchDropTargetBar != null) {
                    duration = mSearchDropTargetBar.getTransitionInDuration();
                }
                mDividerAnimator.setDuration(duration);
                mDividerAnimator.start();
            }
        }
    }

    void lockAllApps() {
        // TODO
    }

    void unlockAllApps() {
        // TODO
    }

    /**
     * Shows the hotseat area.
     */
    void showHotseat(boolean animated) {
        View hotseat = (View) getHotseat();
        if (!LauncherApplication.isScreenLarge()) {
            if (animated) {
                if (hotseat.getAlpha() != 1f) {
                    int duration = 0;
                    if (mSearchDropTargetBar != null) {
                        duration = mSearchDropTargetBar.getTransitionInDuration();
                    }
                    hotseat.animate().alpha(1f).setDuration(duration);
                }
            } else {
                hotseat.setAlpha(1f);
            }
        }
    }

    /**
     * Hides the hotseat area.
     */
    void hideHotseat(boolean animated) {
        View hotseat = (View)getHotseat();
        if (!LauncherApplication.isScreenLarge()) {
            if (animated) {
                if (hotseat.getAlpha() != 0f) {
                    int duration = 0;
                    if (mSearchDropTargetBar != null) {
                        duration = mSearchDropTargetBar.getTransitionOutDuration();
                    }
                    hotseat.animate().alpha(0f).setDuration(duration);
                }
            } else {
                hotseat.setAlpha(0f);
            }
        }
    }

    /**
     * Add an item from all apps or customize onto the given workspace screen.
     * If layout is null, add to the current screen.
     */
    void addExternalItemToScreen(ItemInfo itemInfo, final CellLayout layout) {
        if (!mWorkspace.addExternalItemToScreen(itemInfo, layout)) {
            showOutOfSpaceMessage(isHotseatLayout(layout));
        }
    }

    /** Maps the current orientation to an index for referencing orientation correct global icons */
    private int getCurrentOrientationIndexForGlobalIcons() {
        // default - 0, landscape - 1
        switch (getResources().getConfiguration().orientation) {
        case Configuration.ORIENTATION_LANDSCAPE:
            return 1;
        default:
            return 0;
        }
    }

    private Drawable getExternalPackageToolbarIcon(ComponentName activityName, String resourceName) {
        try {
            PackageManager packageManager = getPackageManager();
            // Look for the toolbar icon specified in the activity meta-data
            Bundle metaData = packageManager.getActivityInfo(
                    activityName, PackageManager.GET_META_DATA).metaData;
            if (metaData != null) {
                int iconResId = metaData.getInt(resourceName);
                if (iconResId != 0) {
                    Resources res = packageManager.getResourcesForActivity(activityName);
                    return res.getDrawable(iconResId);
                }
            }
        } catch (NameNotFoundException e) {
            // This can happen if the activity defines an invalid drawable
            Log.w(TAG, "Failed to load toolbar icon; " + activityName.flattenToShortString() +
                    " not found", e);
        } catch (Resources.NotFoundException nfe) {
            // This can happen if the activity defines an invalid drawable
            Log.w(TAG, "Failed to load toolbar icon from " + activityName.flattenToShortString(),
                    nfe);
        }
        return null;
    }

    // if successful in getting icon, return it; otherwise, set button to use default drawable
    private Drawable.ConstantState updateTextButtonWithIconFromExternalActivity(
            int buttonId, ComponentName activityName, int fallbackDrawableId,
            String toolbarResourceName) {
        Drawable toolbarIcon = getExternalPackageToolbarIcon(activityName, toolbarResourceName);
        Resources r = getResources();
        int w = r.getDimensionPixelSize(R.dimen.toolbar_external_icon_width);
        int h = r.getDimensionPixelSize(R.dimen.toolbar_external_icon_height);

        TextView button = (TextView) findViewById(buttonId);
        // If we were unable to find the icon via the meta-data, use a generic one
        if (toolbarIcon == null) {
            toolbarIcon = r.getDrawable(fallbackDrawableId);
            toolbarIcon.setBounds(0, 0, w, h);
            if (button != null) {
                button.setCompoundDrawables(toolbarIcon, null, null, null);
            }
            return null;
        } else {
            toolbarIcon.setBounds(0, 0, w, h);
            if (button != null) {
                button.setCompoundDrawables(toolbarIcon, null, null, null);
            }
            return toolbarIcon.getConstantState();
        }
    }

    // if successful in getting icon, return it; otherwise, set button to use default drawable
    private Drawable.ConstantState updateButtonWithIconFromExternalActivity(
            int buttonId, ComponentName activityName, int fallbackDrawableId,
            String toolbarResourceName) {
        ImageView button = (ImageView) findViewById(buttonId);
        Drawable toolbarIcon = getExternalPackageToolbarIcon(activityName, toolbarResourceName);

        if (button != null) {
            // If we were unable to find the icon via the meta-data, use a
            // generic one
            if (toolbarIcon == null) {
                button.setImageResource(fallbackDrawableId);
            } else {
                button.setImageDrawable(toolbarIcon);
            }
        }

        return toolbarIcon != null ? toolbarIcon.getConstantState() : null;

    }

    private void updateTextButtonWithDrawable(int buttonId, Drawable d) {
        TextView button = (TextView) findViewById(buttonId);
        button.setCompoundDrawables(d, null, null, null);
    }

    private void updateButtonWithDrawable(int buttonId, Drawable.ConstantState d) {
        ImageView button = (ImageView) findViewById(buttonId);
        button.setImageDrawable(d.newDrawable(getResources()));
    }

    private void invalidatePressedFocusedStates(View container, View button) {
        if (container instanceof HolographicLinearLayout) {
            HolographicLinearLayout layout = (HolographicLinearLayout) container;
            layout.invalidatePressedFocusedStates();
        } else if (button instanceof HolographicImageView) {
            HolographicImageView view = (HolographicImageView) button;
            view.invalidatePressedFocusedStates();
        }
    }

    @Override
    public boolean dispatchPopulateAccessibilityEvent(AccessibilityEvent event) {
        final boolean result = super.dispatchPopulateAccessibilityEvent(event);
        final List<CharSequence> text = event.getText();
        text.clear();
        // Populate event with a fake title based on the current state.
        if (mState == State.APPS_CUSTOMIZE) {
            text.add(getString(R.string.all_apps_button_label));
        } else {
            text.add(getString(R.string.all_apps_home_button_label));
        }
        return result;
    }

    /**
     * Receives notifications when system dialogs are to be closed.
     */
    private class CloseSystemDialogsIntentReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            closeSystemDialogs();
        }
    }
    /**
     * Receives WALLPAPER_CHANGED intent
     */
    private class WallpaperChangedIntentReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "ACTION_WALLPAPER_CHANGED");
            DisplayMetrics dm = context.getResources().getDisplayMetrics();
            // forcely make height larger than width
            final int wallpaperWidth =  (dm.widthPixels > dm.heightPixels) ? dm.heightPixels : dm.widthPixels;
            final int wallpaperHeight = (dm.widthPixels < dm.heightPixels) ? dm.heightPixels : dm.widthPixels;
            final WallpaperManager wallpaperManager = WallpaperManager.getInstance(context);
            new Thread("setWallpaperDimension") {
                public void run() {
                    wallpaperManager.suggestDesiredDimensions(wallpaperWidth, wallpaperHeight);
                }
            }.start();
        }
    }
    /**
     * Receives notifications whenever the appwidgets are reset.
     */
    private class AppWidgetResetObserver extends ContentObserver {
        public AppWidgetResetObserver() {
            super(new Handler());
        }

        @Override
        public void onChange(boolean selfChange) {
            onAppWidgetReset();
        }
    }

    /**
     * If the activity is currently paused, signal that we need to run the passed Runnable
     * in onResume.
     *
     * This needs to be called from incoming places where resources might have been loaded
     * while we are paused.  That is becaues the Configuration might be wrong
     * when we're not running, and if it comes back to what it was when we
     * were paused, we are not restarted.
     *
     * Implementation of the method from LauncherModel.Callbacks.
     *
     * @return true if we are currently paused.  The caller might be able to
     * skip some work in that case since we will come back again.
     */
    private boolean waitUntilResume(Runnable run, boolean deletePreviousRunnables) {
        if (mPaused) {
            Log.i(TAG, "Deferring update until onResume");
            if (deletePreviousRunnables) {
                while (mOnResumeCallbacks.remove(run)) {
                }
            }
            mOnResumeCallbacks.add(run);
            return true;
        } else {
            return false;
        }
    }

    private boolean waitUntilResume(Runnable run) {
        //no waiting for onResume
        return false;
        //return waitUntilResume(run, false);
    }

    /**
     * If the activity is currently paused, signal that we need to re-run the loader
     * in onResume.
     *
     * This needs to be called from incoming places where resources might have been loaded
     * while we are paused.  That is becaues the Configuration might be wrong
     * when we're not running, and if it comes back to what it was when we
     * were paused, we are not restarted.
     *
     * Implementation of the method from LauncherModel.Callbacks.
     *
     * @return true if we are currently paused.  The caller might be able to
     * skip some work in that case since we will come back again.
     */
    public boolean setLoadOnResume() {
        if (mPaused) {
            Log.i(TAG, "setLoadOnResume");
            mOnResumeNeedsLoad = true;
            return true;
        } else {
            return false;
        }
    }

    /**
     * Implementation of the method from LauncherModel.Callbacks.
     */
    public int getCurrentWorkspaceScreen() {
        if (mWorkspace != null) {
            return mWorkspace.getCurrentPage();
        } else {
            return SCREEN_COUNT / 2;
        }
    }

    /**
     * Refreshes the shortcuts shown on the workspace.
     *
     * Implementation of the method from LauncherModel.Callbacks.
     */
    public void startBinding() {
        // If we're starting binding all over again, clear any bind calls we'd postponed in
        // the past (see waitUntilResume) -- we don't need them since we're starting binding
        // from scratch again
        mOnResumeCallbacks.clear();


        final Workspace workspace = mWorkspace;
        mNewShortcutAnimatePage = -1;
        mNewShortcutAnimateViews.clear();
        mWorkspace.clearReference();
        int count = workspace.getChildCount();
        for (int i = 0; i < count; i++) {
            // Use removeAllViewsInLayout() to avoid an extra requestLayout() and invalidate().
            final CellLayout layoutParent = (CellLayout) workspace.getChildAt(i);
            layoutParent.removeAllViewsInLayout();
        }
        // ##description: Added support for widget page 
        for (int i = count - 1; i >= 0; i--) {
            final CellLayout layoutParent = (CellLayout) workspace.getChildAt(i);
            if (layoutParent.isWidgetPage()) {
                workspace.bindWidgetPage(i);
            }
        }
        mWidgetsToAdvance.clear();
        if (mHotseat != null) {
            mHotseat.resetLayout();
        }
        if (mHideseat != null) {
            mHideseat.resetLayout();
        }

	//start:fanxiangjian,com.topwise.calendar
	int send=-1;
	try{
		int c=0;
		c=LauncherModel.getCellCountY();
		send=Settings.System.getInt(getContentResolver(),"com_topwise_calendar",-1);
		Settings.System.putInt(getContentResolver(),"com_topwise_calendar_ycount",c);
		//Log.d("fanxiangjian","launcher c"+c);
	}catch(Exception e){
	}
	if(send==1){
		Intent i=new Intent();
		i.setAction("com.topwise.calendar.refresh");
		i.putExtra("function",4);
		sendBroadcast(i);
	}
	//end:fanxiangjian,com.topwise.calendar

    }

    /**
     * Bind the items start-end from the list.
     *
     * Implementation of the method from LauncherModel.Callbacks.
     */
    public void bindItems(final ArrayList<ItemInfo> shortcuts, final int start, final int end) {
        if (waitUntilResume(new Runnable() {
                public void run() {
                    bindItems(shortcuts, start, end);
                }
            })) {
            return;
        }
        // Get the list of added shortcuts and intersect them with the set of shortcuts here
        Set<String> newApps = new HashSet<String>();
        newApps = mSharedPrefs.getStringSet(InstallShortcutReceiver.NEW_APPS_LIST_KEY, newApps);
        Workspace workspace = mWorkspace;
        if (workspace == null) {
            return;
        }
        for (int i = start; i < end; i++) {
            final ItemInfo item = shortcuts.get(i);
            // Short circuit if we are loading dock items for a configuration which has no dock
            if (item.container == LauncherSettings.Favorites.CONTAINER_HOTSEAT &&
                    mHotseat == null) {
                continue;
            }
            if (item.container == LauncherSettings.Favorites.CONTAINER_HIDESEAT) {
                //continue;
            }
            
            if(item.container == LauncherSettings.Favorites.CONTAINER_DESKTOP
                    && item.screen == 0) {
                mWorkspace.invalidatePageIndicator(true);
            }
            switch (item.itemType) {
                case LauncherSettings.Favorites.ITEM_TYPE_APPLICATION:
                case LauncherSettings.Favorites.ITEM_TYPE_SHORTCUT:
                    ShortcutInfo info = (ShortcutInfo) item;
                    String uri = info.intent.toUri(0).toString();
                    if (Hideseat.containsSamePackageOf(info)) {
                        mHideseat.bindItemInHideseat(info);
                    } else {
                        View shortcut = createShortcut(info);
                        workspace.addInScreen(shortcut, item.container, item.screen, item.cellX,
                                item.cellY, 1, 1, false);
                        boolean animateIconUp = false;
                        //cancel animate. single shortcut can't be displayed if animate is true
                        /*
                        synchronized (newApps) {
                            if (newApps.contains(uri)) {
                                animateIconUp = newApps.remove(uri);
                            }
                        }
                        */
                        if (animateIconUp) {
                            // Prepare the view to be animated up
                            shortcut.setAlpha(0f);
                            shortcut.setScaleX(0f);
                            shortcut.setScaleY(0f);
                            mNewShortcutAnimatePage = item.screen;
                            if (!mNewShortcutAnimateViews.contains(shortcut)) {
                                mNewShortcutAnimateViews.add(shortcut);
                            }
                        }
                    }
                    break;
                case LauncherSettings.Favorites.ITEM_TYPE_FOLDER:
                    FolderIcon newFolder = FolderIcon.fromXml(R.layout.folder_icon, this,
                            (ViewGroup) workspace.getChildAt(workspace.getCurrentPage()),
                            (FolderInfo) item);
                    workspace.addInScreen(newFolder, item.container, item.screen, item.cellX,
                            item.cellY, 1, 1, false);
                    break;
                /*case LauncherSettings.Favorites.ITEM_TYPE_GADGET:
                    GadgetItemInfo gadgetInfo = (GadgetItemInfo) item;
                    View gadget = LauncherGadgetHelper.getGadget(this, gadgetInfo.gadgetInfo);
                    if (gadget == null) {
                        Log.e(TAG, "failed get gadget " + gadgetInfo.title);
                        break;
                    }
                    gadget.setTag(gadgetInfo);
                    gadget.setOnLongClickListener(this);
                    workspace.addInScreen(gadget, item.container, item.screen, item.cellX,
                            item.cellY, item.spanX, item.spanY, false);
                    break;*/
                case LauncherSettings.Favorites.ITEM_TYPE_SHORTCUT_DOWNLOADING:
                    ShortcutInfo shortcutinfo = (ShortcutInfo) item;
                    String shortcuturi = shortcutinfo.intent.toUri(0).toString();
                    View shortcutview = createShortcut(shortcutinfo);
                    workspace.addInScreen(shortcutview, item.container, item.screen, item.cellX,
                            item.cellY, 1, 1, false);

                    break;
                case LauncherSettings.Favorites.ITEM_TYPE_VPINSTALL:
                    ShortcutInfo vpinfo = (ShortcutInfo) item;
                    View vpshortcut = createShortcut(vpinfo);
                    workspace.addInScreen(vpshortcut, item.container, item.screen, item.cellX,
                            item.cellY, 1, 1, false);
                    break;
                //topwise zyf add for fixedfolder
                case LauncherSettings.Favorites.ITEM_TYPE_FIXED_FOLDER:
                    FolderIcon newFixedFolder = FixedFolderIcon.fromXml(R.layout.fixedfolder_icon, this,
                            (ViewGroup) workspace.getChildAt(workspace.getCurrentPage()),
                            (FixedFolderInfo) item);
                    workspace.addInScreen(newFixedFolder, item.container, item.screen, item.cellX,
                            item.cellY, 1, 1, false);
                    break;
                  //topwise zyf add end
               }
        }

        workspace.requestLayout();
    }

    private Handler mbindHandler = new Handler(Looper.getMainLooper());

    @Override
    public void bindItemsChunkUpdated(ArrayList<ItemInfo> items, int start, int end, final boolean themeChange) {
        Log.d(TAG,"Launcher : bindItemsChunkUpdated begin");
        if (mEditMode){
            Log.d(TAG, "Animation isplaying");
            final ArrayList<ItemInfo> finalitems = new ArrayList<ItemInfo>(items);
            final int finalstart = start;
            final int finalend = end;
            Runnable r = new Runnable() {
                @Override
                public void run() {
                    bindItemsChunkUpdated(finalitems, finalstart, finalend, themeChange);
                }
            };
            mbindHandler.postDelayed(r, 500);
            return;
        }

        if (items == null || mWorkspace == null) {
            return;
        }

        final Workspace workspace = mWorkspace;
        for (int i = start; i < end; i++){
            ItemInfo iteminfo = items.get(i);

            CellLayout layout = null;
            if (iteminfo.container == LauncherSettings.Favorites.CONTAINER_HOTSEAT) {
                layout = (CellLayout) getHotseat().getLayout();
            }else if (iteminfo.container == LauncherSettings.Favorites.CONTAINER_HIDESEAT) {
                layout = (CellLayout)getHideseat().getChildAt(iteminfo.screen);
            }else if (iteminfo.container == LauncherSettings.Favorites.CONTAINER_DESKTOP){
                layout = (CellLayout) workspace.getChildAt(iteminfo.screen);
            } else {
                try {
                    FolderInfo folder = sFolders.get(iteminfo.container);
                    long container = folder.container;
                    Log.d(TAG, "folder size : " + sFolders.size() +
                               " container : " + container +
                               " screen : " + folder.screen);
                    if (container == LauncherSettings.Favorites.CONTAINER_HOTSEAT) {
                        layout = (CellLayout) getHotseat().getLayout();
                    } else if (container == LauncherSettings.Favorites.CONTAINER_DESKTOP){
                        layout = (CellLayout) workspace.getChildAt(folder.screen);
                    }
                } catch (Exception e) {
                    Log.d(TAG, "bindItemsChunkUpdated met exception : " + e);
                    layout = null;
                }
            }
            boolean isFound = false;
            if(layout == null){
                Log.d(TAG, "bindItemsChunkUpdated layout is null");
                //find the item in all layouts
                isFound = bindItemUpdateInAllLayouts(iteminfo, themeChange);
            } else {
                isFound = bindItemUpdateInOneLayout(layout, iteminfo, themeChange);
                if (isFound == false) {
                    //find the item in all layouts
                    isFound = bindItemUpdateInAllLayouts(iteminfo, themeChange);
                }
            }

            //find the item in workspace's mDragItems
            if ((isFound == false) && (iteminfo instanceof ShortcutInfo)) {
                View tmpView = workspace.searchDragItemFromList(iteminfo);
                if ((tmpView != null) && (tmpView instanceof BubbleTextView)) {
                    Log.d(TAG, "find the item in workspace's mDragItems!");
                    if(themeChange) {
                        ((BubbleTextView)tmpView).preThemeChange();
                    }
                    ((BubbleTextView)tmpView).updateView(iteminfo);
                    ((BubbleTextView)tmpView).applyFromShortcutInfo((ShortcutInfo)iteminfo);
                    continue;
                }
            }
        }

        if (end == items.size()) {
            items.clear();
        }
        if (themeChange) {
            Launcher.sReloadingForThemeChangeg = false;
        }
        Log.d(TAG,"Launcher : bindItemsChunkUpdated end");
    }

    private boolean bindItemUpdateInAllLayouts(ItemInfo iteminfo, final boolean themeChange) {
        boolean isFound = false;
        CellLayout layout = (CellLayout) getHotseat().getLayout();
        isFound = bindItemUpdateInOneLayout(layout, iteminfo, themeChange);
        if (isFound == true)
        {
            return isFound;
        }
        layout = (CellLayout)getHideseat().getChildAt(iteminfo.screen);
        isFound = bindItemUpdateInOneLayout(layout, iteminfo, themeChange);
        if (isFound == true)
        {
            return isFound;
        }
        final Workspace workspace = mWorkspace;
        for (int i = 0; i < workspace.getChildCount(); i++) {
            layout = (CellLayout) workspace.getChildAt(i);
            isFound = bindItemUpdateInOneLayout(layout, iteminfo, themeChange);
            if (isFound == true)
            {
                return isFound;
            }
        }
        return isFound;
    }

    private boolean bindItemUpdateInOneLayout(CellLayout layout, ItemInfo iteminfo, final boolean themeChange) {
        if ((layout == null) || (iteminfo == null)) {
            return false;
        }
        ShortcutAndWidgetContainer container = layout.getShortcutAndWidgetContainer();
        Log.d(TAG, "title : " + iteminfo.title + " screen is " + iteminfo.screen + " id: " + iteminfo.id);
        int childCount = container.getChildCount();
        Log.d(TAG, "childCount is " + childCount);
        boolean isFound = false;
        for (int j = 0; j < childCount; j++) {
            View view = container.getChildAt(j);
            Object tag = view.getTag();
            if (tag instanceof ShortcutInfo) {
                ItemInfo info = (ItemInfo)tag;
                if(iteminfo.equals(info)){
                    Log.d(TAG, "find the iteminfo : " + info.id);
                    isFound = true;
                    if(themeChange) {
                        ((BubbleTextView)view).preThemeChange();
                    }
                    ((BubbleTextView)view).updateView(info);
                    ((BubbleTextView)view).applyFromShortcutInfo((ShortcutInfo)info);
                    break;
                }
            }

            if (tag instanceof FolderInfo) {
                ArrayList<View> childviews = (((FolderIcon)view).getFolder()).getItemsInReadingOrder();
                if (!iteminfo.equals(tag)) {
                    //find iteminfo in folder.
                    boolean found = false;
                    for (View bubbleview : childviews) {
                        ShortcutInfo info = (ShortcutInfo)(bubbleview.getTag());
                        Log.d(TAG, "in folder title : " + info.title);
                        if (iteminfo.equals(info)) {
                            Log.d(TAG, "find the iteminfo in folder : " + info.id);
                            isFound = true;
                            if(themeChange) {
                                ((BubbleTextView)bubbleview).preThemeChange();
                            }
                            ((BubbleTextView)bubbleview).updateView(info);
                            ((BubbleTextView)bubbleview).applyFromShortcutInfo((ShortcutInfo)info);
                            found = true;
                            break;
                        }
                    }
                    if (found) {
                        ((FolderIcon)view).updateView();
                        break;
                    }
                } else {
                    // update folder include child.
                    for (View bubbleview : childviews) {
                        ShortcutInfo info = (ShortcutInfo)(bubbleview.getTag());
                        Log.d(TAG, "find the iteminfo in folder : " + info.id);
                        isFound = true;
                        if(themeChange) {
                            ((BubbleTextView)bubbleview).preThemeChange();
                        }
                        ((BubbleTextView)bubbleview).updateView(info);
                        ((BubbleTextView)bubbleview).applyFromShortcutInfo((ShortcutInfo)info);
                    }

                    Log.d(TAG, "find the iteminfo of folder : " + iteminfo.id);
                    ((FolderIcon)view).updateView();
                    (((FolderIcon) view).getFolder()).onThemeChanged();
                    break;
                }
            }

            /*if (tag instanceof GadgetItemInfo && iteminfo.equals(tag)) {
                CellLayout.LayoutParams lp = new CellLayout.LayoutParams(
                        (CellLayout.LayoutParams) view.getLayoutParams());
                GadgetItemInfo info = (GadgetItemInfo) tag;
                layout.removeView(view);
                view.setTag(null);
                ((GadgetView) view).cleanUp();

                View newGadgetView = LauncherGadgetHelper.getGadget(this,
                        info.gadgetInfo);
                if (newGadgetView == null) {
                    Log.d(TAG, "ERROR: fetch gadget error:"
                            + info.gadgetInfo.label);
                    return true;
                }
                newGadgetView.setTag(tag);
                newGadgetView.setOnLongClickListener(this);
                layout.addViewToCellLayout(newGadgetView, j, (int) info.id,
                        lp, true);
            }*/
        }

        return isFound;
    }

    @Override
    public void bindItemsUpdated(ArrayList<ItemInfo> items) {
        Log.d(TAG,"Launcher : bindItemsUpdated begin");

        if (mEditMode){
            final ArrayList<ItemInfo> finalitems = new ArrayList<ItemInfo>(items);
            Runnable r = new Runnable() {
                @Override
                public void run() {
                    bindItemsUpdated(finalitems);
                }
            };
            mbindHandler.postDelayed(r, 500);
            return;
        }
        if (items == null || mWorkspace == null) {
            return;
        }
        final int itemCount = items.size();
        Log.d(TAG, "itemCount is " + itemCount);
        final Workspace workspace = mWorkspace;
        for (int i = 0; i < itemCount; i++){
            ItemInfo iteminfo = items.get(i);

            CellLayout layout = null;
            if (iteminfo.container == LauncherSettings.Favorites.CONTAINER_HOTSEAT) {
                layout = (CellLayout) getHotseat().getLayout();
            }else if (iteminfo.container == LauncherSettings.Favorites.CONTAINER_HIDESEAT) {
                layout = (CellLayout)getHideseat().getChildAt(iteminfo.screen);
            }else if (iteminfo.container == LauncherSettings.Favorites.CONTAINER_DESKTOP){
                layout = (CellLayout) workspace.getChildAt(iteminfo.screen);
            } else {
                try {
                    FolderInfo folder = sFolders.get(iteminfo.container);
                    long container = folder.container;
                    Log.d(TAG, "folder size : " + sFolders.size() +
                               " container : " + container +
                               " screen : " + folder.screen);
                    if (container == LauncherSettings.Favorites.CONTAINER_HOTSEAT) {
                        layout = (CellLayout) getHotseat().getLayout();
                    } else if (container == LauncherSettings.Favorites.CONTAINER_DESKTOP){
                        layout = (CellLayout) workspace.getChildAt(folder.screen);
                    }
                } catch (Exception e) {
                    Log.d(TAG, "bindItemsUpdated met exception : " + e);
                    layout = null;
                }
            }
            if(layout == null){
                Log.d(TAG, "bindItemsUpdated layout is null");
                //return;
                continue;
            }

            ShortcutAndWidgetContainer container = layout.getShortcutAndWidgetContainer();
            Log.d(TAG, "title : " + iteminfo.title + " screen is " + iteminfo.screen + " id: " + iteminfo.id);
            int childCount = container.getChildCount();
            Log.d(TAG, "childCount is " + childCount);
            boolean isFound = false;
            for (int j = 0; j < childCount; j++) {
                View view = container.getChildAt(j);
                Object tag = view.getTag();
                if (tag instanceof ShortcutInfo) {
                    ItemInfo info = (ItemInfo)tag;
                    if(iteminfo.equals(info)){
                        Log.d(TAG, "find the iteminfo : " + info.id);
                        isFound = true;
                        ((BubbleTextView)view).updateView(info);
                        ((BubbleTextView)view).applyFromShortcutInfo((ShortcutInfo)info);
                        break;
                    }
                }

                if (tag instanceof FolderInfo) {
                    ArrayList<View> childviews = (((FolderIcon)view).getFolder()).getItemsInReadingOrder();
                    if (!iteminfo.equals(tag)) {
                        //find iteminfo in folder.
                        boolean found = false;
                        for (View bubbleview : childviews) {
                            ShortcutInfo info = (ShortcutInfo)(bubbleview.getTag());
                            Log.d(TAG, "in folder title : " + info.title);
                            if (iteminfo.equals(info)) {
                                Log.d(TAG, "find the iteminfo in folder : " + info.id);
                                isFound = true;
                                ((BubbleTextView)bubbleview).updateView(info);
                                ((BubbleTextView)bubbleview).applyFromShortcutInfo((ShortcutInfo)info);
                                found = true;
                                break;
                            }
                        }
                        if (found) {
                            ((FolderIcon)view).updateView();
                            break;
                        }
                    } else {
                        // update folder include child.
                        for (View bubbleview : childviews) {
                            ShortcutInfo info = (ShortcutInfo)(bubbleview.getTag());
                            Log.d(TAG, "find the iteminfo in folder : " + info.id);
                            isFound = true;
                            ((BubbleTextView)bubbleview).updateView(info);
                            ((BubbleTextView)bubbleview).applyFromShortcutInfo((ShortcutInfo)info);
                        }

                        Log.d(TAG, "find the iteminfo of folder : " + iteminfo.title);
                        ((FolderIcon)view).updateView();
                        break;
                    }
                }

                // gadget support theme changed
                /*if (tag instanceof GadgetItemInfo && iteminfo.equals(tag)) {
                    CellLayout.LayoutParams lp = new CellLayout.LayoutParams(
                            (CellLayout.LayoutParams) view.getLayoutParams());
                    GadgetItemInfo info = (GadgetItemInfo) tag;
                    layout.removeView(view);
                    view.setTag(null);
                    ((GadgetView) view).cleanUp();

                    View newGadgetView = LauncherGadgetHelper.getGadget(this,
                            info.gadgetInfo);
                    if (newGadgetView == null) {
                        Log.d(TAG, "ERROR: fetch gadget error:"
                                + info.gadgetInfo.label);
                        return;
                    }
                    newGadgetView.setTag(tag);
                    newGadgetView.setOnLongClickListener(this);
                    layout.addViewToCellLayout(newGadgetView, j, (int) info.id,
                            lp, true);
                }*/
            }

            //find the item in workspace's mDragItems
             if ((isFound == false) && (iteminfo instanceof ShortcutInfo)) {
                View tmpView = workspace.searchDragItemFromList(iteminfo);
                if ((tmpView != null) && (tmpView instanceof BubbleTextView)) {
                    Log.d(TAG, "find the item in workspace's mDragItems!");
                    ((BubbleTextView)tmpView).updateView(iteminfo);
                    ((BubbleTextView)tmpView).applyFromShortcutInfo((ShortcutInfo)iteminfo);
                    continue;
                }
            }
        }
        items.clear();
        Log.d(TAG,"Launcher : bindItemsUpdated end");
    }

    @Override
    public void bindDownloadItemsRemoved(final ArrayList<ItemInfo> items, final boolean permanent) {
        Log.d(TAG,"Launcher : bindDownloadItemsRemoved begin");
         if (waitUntilResume(new Runnable() {
             public void run() {
                 bindDownloadItemsRemoved(items, permanent);
             }
         })) {
             return;
         }
        final ArrayList<String> packageNames = new ArrayList<String>();
        for(ItemInfo info : items){
            if(info instanceof ShortcutInfo){
                if (((ShortcutInfo)info).intent != null) {
                    packageNames.add(((ShortcutInfo)info).intent.getStringExtra(AppDownloadManager.TYPE_PACKAGENAME));
                }
            }
        }
        if (mWorkspace != null) {
            mWorkspace.removeItemsByPackageName(packageNames);
        }
        items.clear();
        Runnable checkFolderRunnable = new Runnable() {
            @Override
            public void run() {
                LauncherModel.checkFolderAndUpdateByUI();
            }
        };
        mWorkspace.post(checkFolderRunnable);
        Log.d(TAG,"Launcher : bindDownloadItemsRemoved end");
    }

    //find and remove one item folder after all restore app handled by appstore
    @Override
    public void bindItemsViewRemoved(final ArrayList<ItemInfo> items) {
         Log.d(TAG,"Launcher : bindItemsViewRemoved begin");
         if (waitUntilResume(new Runnable() {
             public void run() {
                 bindItemsViewRemoved(items);
             }
         })) {
             return;
         }
        final ArrayList<ItemInfo> itemsViewRemoved = new ArrayList<ItemInfo>(items);
        if (mWorkspace == null) {
            return;
        }
        mWorkspace.removeItemsViewByItemInfo(itemsViewRemoved);
        Log.d(TAG,"Launcher : bindItemsViewRemoved end");
    }

    @Override
    public void bindItemsRemoved(final ArrayList<ItemInfo> items) {
         Log.d(TAG,"Launcher : bindItemsRemoved begin");
         if (waitUntilResume(new Runnable() {
             public void run() {
                 bindItemsRemoved(items);
             }
         })) {
             return;
         }
        final ArrayList<ItemInfo> itemsRemoved = new ArrayList<ItemInfo>(items);

        if (mWorkspace != null) {
            mWorkspace.removeItemsByItemInfo(itemsRemoved);
        }
        Log.d(TAG,"Launcher : bindItemsRemoved end");
    }

    //this function only used to move views into workspace
    //not move into folder, hotseat, or hideseat
    public void bindWorkspaceItemsViewMoved(final ArrayList<ItemInfo> items) {
        Log.d(TAG,"Launcher : bindWorkspaceItemsViewMoved begin");
        if (waitUntilResume(new Runnable() {
            public void run() {
                bindWorkspaceItemsViewMoved(items);
            }
        })) {
            return;
        }
        final ArrayList<ItemInfo> itemsViewMoved = new ArrayList<ItemInfo>(items);

        moveItemsViewByItemInfo(itemsViewMoved);
        Log.d(TAG,"Launcher : bindWorkspaceItemsViewMoved end");

    }

    @Override
    public void bindItemsAdded(ArrayList<ItemInfo> items) {
        Log.d(TAG,"Launcher : bindItemsAdded begin");
        bindItems(items, 0, items.size());
        Log.d(TAG,"Launcher : bindItemsAdded end");
    }

    @Override
    public void bindItemsViewAdded(ArrayList<ItemInfo> items) {
        final ArrayList<ItemInfo> finalitems = items;
        final ArrayList<ItemInfo> itemsToAdd = new ArrayList<ItemInfo>(items);
        getHandler().post(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG,"Launcher : bindItemsViewAdded begin");
                bindItems(itemsToAdd, 0, itemsToAdd.size());
                Log.d(TAG,"Launcher : bindItemsViewAdded end");
            }
        });
    }

    //The items in hotseat restore
    @Override
    public void bindRebuildHotseat(ArrayList<ItemInfo> items) {
        mHotseat.resetLayout();
        bindItems(items, 0, items.size());
    }

    @Override
    public void resetGridSize(int countX, int countY) {
        final int N = mWorkspace.getChildCount();
        for (int i = 0; i < N; i++) {
            CellLayout layout = (CellLayout) mWorkspace.getChildAt(i);
            layout.resetGridSize(countX, countY);
        }

        CellLayout  hotseatLayout = mHotseat.getCellLayout();
        hotseatLayout.setGridSize(hotseatLayout.getCellWidth(), hotseatLayout.getCellHeight(), hotseatLayout.getWidthGap(), hotseatLayout.getHeightGap());

        mHideseat.onWorkspaceLayoutChange(countX, countY);

        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mWorkspace.setCurrentPage(mWorkspace.getCurrentPage());
            }
        });
    }

    @Override
    public void checkAndRemoveEmptyCell() {
        if (mWorkspace != null) {
            mWorkspace.checkAndRemoveEmptyCell();
        }
    }

    @Override
    public void bindRebuildHideseat(ArrayList<ItemInfo> items) {
        //mHotseat.resetLayout();
        bindItems(items, 0, items.size());
    }

    public void startVPInstallActivity(Intent intent, Object tag) {
        if (mPaused == false) {
            startActivitySafely(null, intent, tag);
        }
    }

    /**
     * Implementation of the method from LauncherModel.Callbacks.
     */
    public void bindFolders(final HashMap<Long, FolderInfo> folders) {
        if (waitUntilResume(new Runnable() {
                public void run() {
                    bindFolders(folders);
                }
            })) {
            return;
        }
        //sFolders.clear();
        sFolders.putAll(folders);
    }

    /**
     * Add the views for a widget to the workspace.
     *
     * Implementation of the method from LauncherModel.Callbacks.
     */
    public void bindAppWidget(final LauncherAppWidgetInfo item) {
        if (waitUntilResume(new Runnable() {
                public void run() {
                    bindAppWidget(item);
                }
            })) {
            return;
        }

        final long start = DEBUG_WIDGETS ? SystemClock.uptimeMillis() : 0;
        if (DEBUG_WIDGETS) {
            Log.d(TAG, "bindAppWidget: " + item);
        }
        final Workspace workspace = mWorkspace;

        final int appWidgetId = item.appWidgetId;
        final AppWidgetProviderInfo appWidgetInfo = mAppWidgetManager.getAppWidgetInfo(appWidgetId);
        if (DEBUG_WIDGETS) {
            Log.d(TAG, "bindAppWidget: id=" + item.appWidgetId + " belongs to component " + appWidgetInfo.provider);
        }

        item.hostView = mAppWidgetHost.createView(this, appWidgetId, appWidgetInfo);

        item.hostView.setTag(item);
        item.onBindAppWidget(this);

        workspace.addInScreen(item.hostView, item.container, item.screen, item.cellX,
                item.cellY, item.spanX, item.spanY, false);
        addWidgetToAutoAdvanceIfNeeded(item.hostView, appWidgetInfo);

        workspace.requestLayout();

        if (DEBUG_WIDGETS) {
            Log.d(TAG, "bound widget id="+item.appWidgetId+" in "
                    + (SystemClock.uptimeMillis()-start) + "ms");
        }
    }

    public void onPageBoundSynchronously(int page) {
        mSynchronouslyBoundPages.add(page);
    }

    /**
     * Callback saying that there aren't any more items to bind.
     *
     * Implementation of the method from LauncherModel.Callbacks.
     */
    public void finishBindingItems() {
        if (waitUntilResume(new Runnable() {
                public void run() {
                    finishBindingItems();
                }
            })) {
            return;
        }
        if (mSavedState != null) {
            if (!mWorkspace.hasFocus()) {
                mWorkspace.getChildAt(mWorkspace.getCurrentPage()).requestFocus();
            }
            mSavedState = null;
        }

        mWorkspace.dispatchRestoreInstanceState(null);
        mWorkspace.restoreInstanceStateForRemainingPages();

        // If we received the result of any pending adds while the loader was running (e.g. the
        // widget configuration forced an orientation change), process them now.
        for (int i = 0; i < sPendingAddList.size(); i++) {
            completeAdd(sPendingAddList.get(i));
        }
        sPendingAddList.clear();

        // Animate up any icons as necessary
        if (mVisible || mWorkspaceLoading) {
            Runnable newAppsRunnable = new Runnable() {
                @Override
                public void run() {
                    runNewAppsAnimation(false);
                }
            };

            boolean willSnapPage = mNewShortcutAnimatePage > -1 &&
                    mNewShortcutAnimatePage != mWorkspace.getCurrentPage();
            if (canRunNewAppsAnimation()) {
                // If the user has not interacted recently, then either snap to the new page to show
                // the new-apps animation or just run them if they are to appear on the current page
                if (willSnapPage) {
                    mWorkspace.snapToPage(mNewShortcutAnimatePage, newAppsRunnable);
                } else {
                    runNewAppsAnimation(false);
                }
            } else {
                // If the user has interacted recently, then just add the items in place if they
                // are on another page (or just normally if they are added to the current page)
                runNewAppsAnimation(willSnapPage);
            }
        }

        mWorkspaceLoading = false;
        int PageCount = mSharedPrefs.getInt(AgedModeUtil.isAgedMode() ? AGED_MODE_PAGE_COUNT : PAGE_COUNT, 0);
        for(int i = 0; i < PageCount; i++){
            addNewScreen();
        }
        //((LauncherApplication) getApplication()).setModeChanged(false);
        positionHideseat();

        // This method ensures the correctness of frozen states of hide-seat items.
        // This is an important process after restore or fota.
        if (Hideseat.isHideseatEnabled()) {
            getHideseat().rearrangeFrozenAppsInHideseat();
        }

        getHotseat().initViewCacheList();
        getWorkspace().invalidatePageIndicator(true);
        
        Log.d(LIVE_WEATHER_TAG, "finishBindingItems begin");
        Log.d(LIVE_WEATHER_TAG, "finishBindingItems isDraggingEnabled() is " + isDraggingEnabled());
        if(isDraggingEnabled()) {
	        Log.d(LIVE_WEATHER_TAG, "finishBindingItems 1");
	        OnBindParticleService();
        }
        Log.d(LIVE_WEATHER_TAG, "finishBindingItems end");
    }

    private boolean canRunNewAppsAnimation() {
        long diff = System.currentTimeMillis() - mDragController.getLastGestureUpTime();
        return diff > (NEW_APPS_ANIMATION_INACTIVE_TIMEOUT_SECONDS * 1000);
    }

    /**
     * Runs a new animation that scales up icons that were added while Launcher was in the
     * background.
     *
     * @param immediate whether to run the animation or show the results immediately
     */
    private void runNewAppsAnimation(boolean immediate) {
        AnimatorSet anim = LauncherAnimUtils.createAnimatorSet();
        Collection<Animator> bounceAnims = new ArrayList<Animator>();

        // Order these new views spatially so that they animate in order
        Collections.sort(mNewShortcutAnimateViews, new Comparator<View>() {
            @Override
            public int compare(View a, View b) {
                CellLayout.LayoutParams alp = (CellLayout.LayoutParams) a.getLayoutParams();
                CellLayout.LayoutParams blp = (CellLayout.LayoutParams) b.getLayoutParams();
                int cellCountX = LauncherModel.getCellCountX();
                return (alp.cellY * cellCountX + alp.cellX) - (blp.cellY * cellCountX + blp.cellX);
            }
        });

        // Animate each of the views in place (or show them immediately if requested)
        if (immediate) {
            for (View v : mNewShortcutAnimateViews) {
                v.setAlpha(1f);
                v.setScaleX(1f);
                v.setScaleY(1f);
            }
        } else {
            for (int i = 0; i < mNewShortcutAnimateViews.size(); ++i) {
                View v = mNewShortcutAnimateViews.get(i);
                ValueAnimator bounceAnim = LauncherAnimUtils.ofPropertyValuesHolder(v,
                        PropertyValuesHolder.ofFloat("alpha", 1f),
                        PropertyValuesHolder.ofFloat("scaleX", 1f),
                        PropertyValuesHolder.ofFloat("scaleY", 1f));
                bounceAnim.setDuration(InstallShortcutReceiver.NEW_SHORTCUT_BOUNCE_DURATION);
                bounceAnim.setStartDelay(i * InstallShortcutReceiver.NEW_SHORTCUT_STAGGER_DELAY);
                bounceAnim.setInterpolator(new SmoothPagedView.OvershootInterpolator());
                bounceAnims.add(bounceAnim);
            }
            anim.playTogether(bounceAnims);
            anim.start();
        }

        // Clean up
        mNewShortcutAnimatePage = -1;
        mNewShortcutAnimateViews.clear();
        new Thread("clearNewAppsThread") {
            public void run() {
                mSharedPrefs.edit()
                            .putInt(InstallShortcutReceiver.NEW_APPS_PAGE_KEY, -1)
                            .putStringSet(InstallShortcutReceiver.NEW_APPS_LIST_KEY, null)
                            .commit();
            }
        }.start();
    }

    /**
     * Add the icons for all apps.
     *
     * Implementation of the method from LauncherModel.Callbacks.
     */
    public void bindAllApplications(final ArrayList<ApplicationInfo> apps) {
        Runnable setAllAppsRunnable = new Runnable() {
            public void run() {
                if (mAppsCustomizeContent != null) {
                    mAppsCustomizeContent.setApps(apps);
                }
            }
        };

        // Remove the progress bar entirely; we could also make it GONE
        // but better to remove it since we know it's not going to be used
        View progressBar = mAppsCustomizeTabHost.
            findViewById(R.id.apps_customize_progress_bar);
        if (progressBar != null) {
            ((ViewGroup)progressBar.getParent()).removeView(progressBar);

            // We just post the call to setApps so the user sees the progress bar
            // disappear-- otherwise, it just looks like the progress bar froze
            // which doesn't look great
            mAppsCustomizeTabHost.post(setAllAppsRunnable);
        } else {
            // If we did not initialize the spinner in onCreate, then we can directly set the
            // list of applications without waiting for any progress bars views to be hidden.
            setAllAppsRunnable.run();
        }
    }

    /**
     * A package was installed.
     *
     * Implementation of the method from LauncherModel.Callbacks.
     */
    public void bindAppsAdded(final ArrayList<ApplicationInfo> apps) {
        if (waitUntilResume(new Runnable() {
                public void run() {
                    bindAppsAdded(apps);
                }
            })) {
            return;
        }


        if (mAppsCustomizeContent != null) {
            mAppsCustomizeContent.addApps(apps);
        }
    }

    /**
     * A package was updated.
     *
     * Implementation of the method from LauncherModel.Callbacks.
     */
    public void bindAppsUpdated(final ArrayList<ApplicationInfo> apps) {
        if (waitUntilResume(new Runnable() {
                public void run() {
                    bindAppsUpdated(apps);
                }
            })) {
            return;
        }

        if (mWorkspace != null) {
            mWorkspace.updateShortcuts(apps);
        }

        if (mAppsCustomizeContent != null) {
            mAppsCustomizeContent.updateApps(apps);
        }
    }

    /**
     * A package was uninstalled.  We take both the super set of packageNames
     * in addition to specific applications to remove, the reason being that
     * this can be called when a package is updated as well.  In that scenario,
     * we only remove specific components from the workspace, where as
     * package-removal should clear all items by package name.
     *
     * Implementation of the method from LauncherModel.Callbacks.
     */
    public void bindComponentsRemoved(final ArrayList<String> packageNames,
                                      final ArrayList<ApplicationInfo> appInfos,
                                      final boolean matchPackageNamesOnly) {
         Log.d(TAG,"bindComponentsRemoved : begin");
        if (waitUntilResume(new Runnable() {
            public void run() {
                bindComponentsRemoved(packageNames, appInfos, matchPackageNamesOnly);
            }
        })) {
            return;
        }

        if (matchPackageNamesOnly) {
            //uninstall an app in folder, the info about this app not removed from db
            mWorkspace.removeItemsByPackageNameForAppUninstall(packageNames);
        } else {
            mWorkspace.removeItemsByApplicationInfo(appInfos);
        }
        if(mFolder != null) {
            if(mFolder.mHasDirtyData) {
                Log.d(TAG, "sxsexe-------------------->  bindComponentsRemoved mFolder.mShortcutInfoCache " + mFolder.mShortcutInfoCache);
                mFolder.getInfo().remove(mFolder.mShortcutInfoCache);
                LauncherModel.deleteItemFromDatabase(this, mFolder.mShortcutInfoCache);
            }
        }

        getWorkspace().cleanDragInfo();
        getWorkspace().checkAndRemoveEmptyCell();

        if (mAppsCustomizeContent != null) {
            mAppsCustomizeContent.removeApps(appInfos);
        }

        // Notify the drag controller
        mDragController.onAppsRemoved(appInfos, this);

        //the reason to use runnable is checkFolderAndUpdateByUI
        //must be run after view remove finished in mWorkspace removeItemsByComponentName
        Runnable checkFolderRunnable = new Runnable() {
            @Override
            public void run() {
                LauncherModel.checkFolderAndUpdateByUI();
            }
        };
        mWorkspace.post(checkFolderRunnable);

        Log.d(TAG,"bindComponentsRemoved : end");
    }

    /**
     * A number of packages were updated.
     */

    private ArrayList<Object> mWidgetsAndShortcuts;
    private Runnable mBindPackagesUpdatedRunnable = new Runnable() {
            public void run() {
                bindPackagesUpdated(mWidgetsAndShortcuts);
                mWidgetsAndShortcuts = null;
            }
        };

    public void bindPackagesUpdated(final ArrayList<Object> widgetsAndShortcuts) {
        if (waitUntilResume(mBindPackagesUpdatedRunnable, true)) {
            mWidgetsAndShortcuts = widgetsAndShortcuts;
            return;
        }

        if (mAppsCustomizeContent != null) {
            mAppsCustomizeContent.onPackagesUpdated(widgetsAndShortcuts);
        }

        if (mEditModeContainer != null) {
            mEditModeContainer.onPackagesUpdated(widgetsAndShortcuts);
        }
    }

    private int mapConfigurationOriActivityInfoOri(int configOri) {
        final Display d = getWindowManager().getDefaultDisplay();
        int naturalOri = Configuration.ORIENTATION_LANDSCAPE;
        switch (d.getRotation()) {
        case Surface.ROTATION_0:
        case Surface.ROTATION_180:
            // We are currently in the same basic orientation as the natural orientation
            naturalOri = configOri;
            break;
        case Surface.ROTATION_90:
        case Surface.ROTATION_270:
            // We are currently in the other basic orientation to the natural orientation
            naturalOri = (configOri == Configuration.ORIENTATION_LANDSCAPE) ?
                    Configuration.ORIENTATION_PORTRAIT : Configuration.ORIENTATION_LANDSCAPE;
            break;
        }

        int[] oriMap = {
                ActivityInfo.SCREEN_ORIENTATION_PORTRAIT,
                ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE,
                ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT,
                ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE
        };
        // Since the map starts at portrait, we need to offset if this device's natural orientation
        // is landscape.
        int indexOffset = 0;
        if (naturalOri == Configuration.ORIENTATION_LANDSCAPE) {
            indexOffset = 1;
        }
        return oriMap[(d.getRotation() + indexOffset) % 4];
    }

    public boolean isRotationEnabled() {
        boolean enableRotation = sForceEnableRotation ||
                getResources().getBoolean(R.bool.allow_rotation);
        return enableRotation;
    }
    public void lockScreenOrientation() {
        if (isRotationEnabled()) {
            setRequestedOrientation(mapConfigurationOriActivityInfoOri(getResources()
                    .getConfiguration().orientation));
        }
    }
    public void unlockScreenOrientation(boolean immediate) {
        if (isRotationEnabled()) {
            if (immediate) {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
            } else {
                mHandler.postDelayed(new Runnable() {
                    public void run() {
                        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
                    }
                }, mRestoreScreenOrientationDelay);
            }
        }
    }

    public boolean checkFolderIdValid(long folderId) {
    	return sFolders.containsKey(folderId);
    }

    /**
     * Prints out out state for debugging.
     */
    public void dumpState() {
        Log.d(TAG, "BEGIN launcher2 dump state for launcher " + this);
        Log.d(TAG, "mSavedState=" + mSavedState);
        Log.d(TAG, "mWorkspaceLoading=" + mWorkspaceLoading);
        Log.d(TAG, "mRestoring=" + mRestoring);
        Log.d(TAG, "mWaitingForResult=" + mWaitingForResult);
        Log.d(TAG, "mSavedInstanceState=" + mSavedInstanceState);
        Log.d(TAG, "sFolders.size=" + sFolders.size());
        mModel.dumpState();

        if (mAppsCustomizeContent != null) {
            mAppsCustomizeContent.dumpState();
        }
        Log.d(TAG, "END launcher2 dump state");
    }

    @Override
    public void dump(String prefix, FileDescriptor fd, PrintWriter writer, String[] args) {
        super.dump(prefix, fd, writer, args);
        writer.println(" ");
        writer.println("Debug logs: ");
        for (int i = 0; i < sDumpLogs.size(); i++) {
            writer.println("  " + sDumpLogs.get(i));
        }
    }

    public static void dumpDebugLogsToConsole() {
        Log.d(TAG, "");
        Log.d(TAG, "*********************");
        Log.d(TAG, "Launcher debug logs: ");
        for (int i = 0; i < sDumpLogs.size(); i++) {
            Log.d(TAG, "  " + sDumpLogs.get(i));
        }
        Log.d(TAG, "*********************");
        Log.d(TAG, "");
        sDumpLogs.clear();
    }

    @Override
    public void bindRemoveScreen(int screen) {

    }

    public void setCurrentFolder(Folder folder) {
        mFolder = folder;
    }

    protected boolean isDragToDelete() {
        if((mWorkspace != null && mWorkspace.mDropTargetView != null && mWorkspace.mDropTargetView instanceof DeleteDropTarget)
                ||(mDeleteDialog != null && mDeleteDialog.isShowing())){
            return true;
        }
        return false;
    }

    private boolean isHideDeleteDialog() {
        if (mDeleteDialog == null || !mDeleteDialog.isShowing())
            return false;
        return true;
    }

    public Handler getHandler() {
        return mHandler;
    }

    public PageIndicatorView getIndicatorView() {
        return mIndicatorView;
    }

    private boolean isTopActivity() {
        ActivityManager am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        List<RunningTaskInfo> runningTaskInfos = am.getRunningTasks(1);
        if (runningTaskInfos == null || runningTaskInfos.size() == 0)
            return false;
        ComponentName componentName = runningTaskInfos.get(0).topActivity;
        return componentName.getPackageName().equals(Launcher.class.getPackage().getName());
    }

    public void openHideseat(boolean isAnimation) {
        AnimationListener l = new AnimationListener() {
            CellLayout currentLayout = null;
            @Override
            public void onAnimationEnd(Animation animation) {
                final int N = mWorkspace.getChildCount();
                for (int i = 0; i < N; i++) {
                    CellLayout layout = (CellLayout) mWorkspace.getChildAt(i);
                    layout.didEnterHideseatMode();
                }

                mCustomHideseat.setVerticalClip(0);
                mIndicatorView.setVisibility(View.VISIBLE);
                if (currentLayout != null) {
                    currentLayout.setHideseatAnimationPlaying(false);
                    currentLayout = null;
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }

            @Override
            public void onAnimationStart(Animation animation) {
                Log.d(TAG, "enterHideseatMode");

                // can not hideseat and dock showing is not correctly.
                final int N = mWorkspace.getChildCount();
                int cur = mWorkspace.getCurrentPage();
                for (int i = 0; i < N; i++) {
                    CellLayout layout = (CellLayout) mWorkspace.getChildAt(i);
                    layout.enterHideseatMode();
                    if (i == cur) {
                        currentLayout = layout;
                        currentLayout.requestLayout();
                    }
                }

                if (mCustomHideseat.getVisibility() != View.VISIBLE) {
                    mCustomHideseat.setVisibility(View.VISIBLE);
                    mHotseat.setVisibility(View.GONE);
                    mDragController.addDropTarget(mHideseat);
                    FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams)mIndicatorView.getLayoutParams();
                    lp.bottomMargin = getNavigationBarHeight();
                    FrameLayout.LayoutParams lp1 = (FrameLayout.LayoutParams)mCustomHideseat.getLayoutParams();
                    lp1.bottomMargin = calcHideseatBottomMargin();
                    if (mGestureLayer.isLiveWallpaperFlag()) {
                        mCustomHideseat.setVerticalClip(ConfigManager.getHideseatHeight());
                        mIndicatorView.setVisibility(View.GONE);
                    }
                    if (currentLayout != null) {
                        currentLayout.setHideseatAnimationPlaying(true);
                    }
                }
            }
        };
        
        mWorkspace.removeWidgetPages(); //daiwei modify

        mGestureLayer.openHideseat(l, isAnimation);
        if (isAnimation) {
            UserTrackerHelper.sendUserReport(UserTrackerMessage.MSG_OPEN_HIDESEAT);
        }
    }

    private int calcHideseatBottomMargin() {
        CellLayout layout = (CellLayout) mWorkspace.getChildAt(mWorkspace.getCurrentPage());
        if (layout == null) {
            layout = (CellLayout) mWorkspace.getChildAt(0);
        }
        ShortcutAndWidgetContainer container = (ShortcutAndWidgetContainer) layout.getChildAt(0);
        CellLayout.LayoutParams lp = container.buildLayoutParams(0, CellLayout.HIDESEAT_CELLY, 1, 1, true);

        int top = 0;
        top += layout.getTop();
        top += container.getTop();
        top += lp.y;

        top -= layout.getHeightGap() / 2;

        int hideseatH = ConfigManager.getHideseatHeight();
        int bottomMargin = mWorkspace.getHeight() - top - hideseatH + getNavigationBarHeight();

        Log.d(TAG, "bottomMargin : " + bottomMargin + " workspace H : "
                + mWorkspace.getHeight() + " top : " + top + " hideseat H : "
                + hideseatH);

        return bottomMargin;
    }

    public void hideHideseat(boolean isAnimation) {
        AnimationListener l = new AnimationListener(){
            CellLayout currentLayout = null;
            @Override
            public void onAnimationEnd(Animation animation) {
                Log.d(TAG, "exitHideseatMode");

                final int N = mWorkspace.getChildCount();
                for (int i = 0; i < N; i++) {
                    CellLayout layout = (CellLayout) mWorkspace.getChildAt(i);
                    layout.exitHideseatMode();
                }

                if (mCustomHideseat.getVisibility() != View.GONE) {
                    FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams)mIndicatorView.getLayoutParams();
                    lp.bottomMargin = mCustomHideseat.getHeight() + getNavigationBarHeight();

                    mCustomHideseat.setVisibility(View.GONE);
                    if (!mEditMode) {
                        mHotseat.setVisibility(View.VISIBLE);
                    }

                    mDragController.removeDropTarget(mHideseat);
                    mCustomHideseat.setVerticalClip(0);
                    mIndicatorView.setVisibility(View.VISIBLE);
                }

                if (currentLayout != null) {
                    currentLayout.setHideseatAnimationPlaying(false);
                    currentLayout = null;
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }

            @Override
            public void onAnimationStart(Animation animation) {
                if (mGestureLayer.isLiveWallpaperFlag()) {
                    mIndicatorView.setVisibility(View.GONE);
                }
                currentLayout = (CellLayout) mWorkspace.getPageAt(mWorkspace.getCurrentPage());
                if (currentLayout != null) currentLayout.setHideseatAnimationPlaying(true);
            }
        };

        mWorkspace.makeSureWidgetPages();  //daiwei modify
        mGestureLayer.closeHideseat(l, isAnimation);
        if (isAnimation) {
            UserTrackerHelper.sendUserReport(UserTrackerMessage.MSG_CLOSE_HIDESEAT);
        }
    }

    public boolean isHideseatShowing() {
        return mCustomHideseat.getVisibility() == View.VISIBLE;
    }

    public boolean shouldInvalidate(CellLayout layout) {
        if (getDragLayer().getAnimatedView() != null || layout.getParent() != mWorkspace)
            return true;
        if (mWorkspace.isPageMoving())
            return false;
        return layout == mWorkspace.getChildAt(getCurrentWorkspaceScreen());
    }

    public void setOnClickValid(boolean valid) {
        mOnClickValid = valid;
    }

    public boolean isSystemAppOrFolder(Object itemInfo) {
        if(itemInfo instanceof FolderInfo) {
            return true;
        }
        if(itemInfo instanceof ShortcutInfo) {
            ShortcutInfo shortcutInfo = (ShortcutInfo)itemInfo;
            return shortcutInfo.isSystemApp;
        }
        return false;
    }

    public boolean isSystemApp(Object itemInfo) {
		//towpise zyf add for fixedfolder
		if(itemInfo!=null&&(itemInfo instanceof FixedFolderInfo))
		{
			return true;
		}
		//topwise zyf add end
        if(itemInfo instanceof ShortcutInfo) {
            ShortcutInfo shortcutInfo = (ShortcutInfo)itemInfo;
            return shortcutInfo.isSystemApp;
        }
        return false;
    }
    
    public boolean isItemUnDeletable(Object itemInfo) {
        if(itemInfo instanceof ItemInfo) {
            ItemInfo info = (ItemInfo)itemInfo;
            return !info.isDeletable();
        }
        return false;
    }
    
    protected void postRunnableToMainThread(Runnable r, long delayMillis) {
        if(mHandler != null) {
            if(delayMillis > 0) {
                mHandler.postDelayed(r, delayMillis);
            } else {
                mHandler.post(r);
            }
        }
    }

    protected void cancelRunnableInMainThread(Runnable r) {
        if(mHandler != null) {
            mHandler.removeCallbacks(r);
        }
    }

    public void enterSearchMode() {
        /*
        Log.i(TAG, "enterSearchMode:" + mAppSearchMode);
        if (mT9DialpadView == null) {
            mT9DialpadView = (T9DialpadView) findViewById(R.id.app_search);
            mT9DialpadView.setT9DialpadViewListener(new T9DialpadView.T9DialpadViewListener(){

                @Override
                public void onExit() {
                    // TODO Auto-generated method stub
                    exitSearchMode();
                }});
        }

        mT9DialpadView.onEnter();
        mT9DialpadView.setVisibility(View.VISIBLE);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        mAppSearchMode = true;
        */
    }

    public void exitSearchMode() {
        /*
        Log.i(TAG, "exitSearchMode:" + mAppSearchMode);
        mT9DialpadView.setVisibility(View.INVISIBLE);
        mT9DialpadView.onExit();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        mAppSearchMode = false;
        */
    }

    public boolean isSearchMode() {
        return mAppSearchMode;
    }

    public void checkAndReplaceFolderIfNecessary(final ItemInfo item) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (isContainerFolder(item.container) && sFolders.containsKey(item.container) && mFolder != null) {
                    if (mFolder.getInfo().count() <= 1) {
                        mFolder.replaceFolderWithFinalItem();
                    } else {
                        mFolder.updateFolderNameWithRemainedApp(item);
                    }
                }
            }
        });
    }

    public int getNavigationBarHeight() {
        boolean hasMenukey = ViewConfiguration.get(getApplicationContext()).hasPermanentMenuKey();
        boolean hasBackkey = KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_BACK);
        if(hasMenukey && hasBackkey) {
            return 0;
        } else {
            Resources resources = getResources();
            int resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android");
            if (resourceId > 0) {
                return resources.getDimensionPixelSize(resourceId);
            }
            return 0;
        }
    }
    
    public void addNewScreen() {
        View emptyScreen = View.inflate(this, R.layout.workspace_screen, null);
        if(emptyScreen != null) {
            emptyScreen.setOnLongClickListener(this);
            int screenCount = mWorkspace.getNormalScreenCount();
            mWorkspace.addView(emptyScreen, screenCount);
        }
    }
    public void recordPageCount() {
            int pageCount = mWorkspace.getNormalScreenCount();
            SharedPreferences.Editor editor = mSharedPrefs.edit();
            if(AgedModeUtil.isAgedMode()){
                editor.putInt(AGED_MODE_PAGE_COUNT, mWorkspace.getEmptyPageCount());
            }else{
                editor.putInt(PAGE_COUNT, mWorkspace.getEmptyPageCount());
            }
            editor.commit();
            if (pageCount == mLastPageCount)
                return ;
            mLastPageCount = pageCount;
            /**if(mWorkspace.getEmptyPageCount() == 0) return;
            SharedPreferences.Editor editor = mSharedPrefs.edit();
            //editor.putInt(PAGE_COUNT, mLastPageCount);
            if(AgedModeUtil.isAgedMode()){
                editor.putInt(AGED_MODE_PAGE_COUNT, mWorkspace.getEmptyPageCount());
            }else{
                editor.putInt(PAGE_COUNT, mWorkspace.getEmptyPageCount());
            }
            editor.commit();
            */
    }
    private void enterEditModeAnimation() {
        if (mState != State.WORKSPACE) {
            mState = State.WORKSPACE;
        }
        if (mEditModeContainer == null) {
            return;
        }
        PreviewList list = (PreviewList) mEditModeContainer.findViewById(R.id.preview_list);
        list.setup(this, mDragController);
        mEditModeContainer.setVisibility(View.VISIBLE);
        AnimatorSet enterAnimator = new AnimatorSet().setDuration(EDIT_MODE_ENTER_TIME);
        ObjectAnimator containerAlpha = ObjectAnimator.ofFloat(mEditModeContainer, "alpha", 0,
                1);
        ObjectAnimator containerTranslationY = ObjectAnimator.ofFloat(mEditModeContainer,
                "translationY", mEditModeContainer.getHeight(), 0);
        ObjectAnimator hotseatAlpha = ObjectAnimator.ofFloat(mHotseat, "alpha", 1, 0);
        ObjectAnimator hotseatTranslationY = ObjectAnimator.ofFloat(mHotseat, "translationY",
                0, mHotseat.getHeight());
        int indicatorTransY = getResources().getDimensionPixelSize(
                R.dimen.desktop_indicator_transY);
        ObjectAnimator indicatorTranslationY = ObjectAnimator.ofFloat(mIndicatorView,
                "translationY", 0, -indicatorTransY);
        enterAnimator.playTogether(containerAlpha, containerTranslationY, hotseatAlpha,
                hotseatTranslationY, indicatorTranslationY);
        enterAnimator.start();
    }

    public void exiteEditModeAnimation() {
        if (mEditModeContainer == null) {
            return;
        }
        AnimatorSet exitAnimator = new AnimatorSet().setDuration(EDIT_MODE_EXIT_TIME);
        exitAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationCancel(Animator animation) {
                if(!mIsEditMode){
                    mEditModeContainer.setVisibility(View.GONE);
                }
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if(!mIsEditMode){
                    mEditModeContainer.setVisibility(View.GONE);
                }
            }

        });
        ObjectAnimator containerAlpha = ObjectAnimator.ofFloat(mEditModeContainer, "alpha", 1f, 0);
        ObjectAnimator hotseatAlpha = ObjectAnimator.ofFloat(mHotseat, "alpha", 0f, 1);
        ObjectAnimator containerTranslationY = ObjectAnimator.ofFloat(mHotseat, "translationY",
                mHotseat.getHeight(), 0);
        ObjectAnimator hotseatTranslationY = ObjectAnimator.ofFloat(mEditModeContainer,
                "translationY", 0, mEditModeContainer.getHeight());
        int indicatorTransY = getResources()
                .getDimensionPixelSize(R.dimen.desktop_indicator_transY);
        ObjectAnimator indicatorTranslationY = ObjectAnimator.ofFloat(mIndicatorView,
                "translationY", -indicatorTransY, 0);
        exitAnimator.playTogether(containerAlpha, hotseatAlpha, containerTranslationY,
                hotseatTranslationY, indicatorTranslationY);
        exitAnimator.start();
        showWorkspace();
    }

    void exitEditMode() {
        if (!mIsEditMode) {
            return;
        }
        mIsEditMode = false;
        mHotseat.setVisibility(View.VISIBLE);
        mWorkspace.setEditMode(false);
    }

    public void enterEditMode() {
        if (mIsEditMode || isHideseatShowing()) {
            return;
        }
        mIsEditMode = true;
        enterEditModeAnimation();
        mWorkspace.setVisibility(View.VISIBLE);
        mIndicatorView.setVisibility(View.VISIBLE);
        mHotseat.setVisibility(View.GONE);
        mWorkspace.setEditMode(true);
        
        if(isDraggingEnabled() && (mPaused == false)) {
        	Log.d(LIVE_WEATHER_TAG, "enterEditMode begin");
        	UnBindParticleService();
        	OnBindParticleService();
        	Log.d(LIVE_WEATHER_TAG, "enterEditMode end");
        }
    }

    public boolean isEditMode() {
        return mIsEditMode;
    }

    void showWorkspace() {
        exitEditMode();
        mIndicatorView.setVisibility(View.VISIBLE);
        mWorkspace.setVisibility(View.VISIBLE);
        mState = State.WORKSPACE;
        mUserPresent = true;
        mWorkspace.requestFocus();
        updateRunning();
        getWindow().getDecorView().sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_SELECTED);
    }

    public void addAppWidget(AppWidgetProviderInfo info) {
        CellLayout layout = (CellLayout) mWorkspace.getChildAt(getCurrentWorkspaceScreen());
        if ((layout != null) && (layout.isFakeChild() == false)) {
            PendingAddWidgetInfo pendingInfo = new PendingAddWidgetInfo(info, null, null);
            int[] spanXY = Launcher.getSpanForWidget(this, info);
            pendingInfo.spanX = spanXY[0];
            pendingInfo.spanY = spanXY[1];
            int[] minSpanXY = Launcher.getMinSpanForWidget(this, info);
            pendingInfo.minSpanX = minSpanXY[0];
            pendingInfo.minSpanY = minSpanXY[1];
            pendingInfo.itemType = LauncherSettings.Favorites.ITEM_TYPE_APPWIDGET;
            addAppWidgetFromDrop(pendingInfo, LauncherSettings.Favorites.CONTAINER_DESKTOP,
                    getCurrentWorkspaceScreen(), null, null, null);
        } else {
            showOutOfSpaceMessage(isHotseatLayout(layout));
        }
    }

    /*public void addGadgetWidget(GadgetInfo info) {
        CellLayout layout = (CellLayout) mWorkspace.getChildAt(getCurrentWorkspaceScreen());
        if ((layout != null) && (layout.isFakeChild() == false)) {
            GadgetItemInfo gadgetInfo = new GadgetItemInfo(info);
            boolean foundCellSpan = false;
            int screen = 0;
            long container;
            int[] cell = new int[2];
            int[] span = new int[2];
            //modified by lixuhui 2015/06/09 fix bug#728
            span[0] = AgedModeUtil.isAgedMode() ? (Math.min(info.spanX, 3)) : info.spanX;
            span[1] = AgedModeUtil.isAgedMode() ? (Math.min(info.spanY, 3)) : info.spanY;
            //end
            foundCellSpan = layout.findCellForSpan(cell, span[0], span[1]);
            if (foundCellSpan) {
                addGadgetFromDrop(gadgetInfo, LauncherSettings.Favorites.CONTAINER_DESKTOP,
                        getCurrentWorkspaceScreen(), cell, span, null);
            } else {
                showOutOfSpaceMessage(isHotseatLayout(layout));
            }
        } else {
            showOutOfSpaceMessage(isHotseatLayout(layout));
        }
    }*/

    public void addShortcut(ResolveInfo info) {
        CellLayout layout = (CellLayout) mWorkspace.getChildAt(getCurrentWorkspaceScreen());
        if ((layout != null) && (layout.isFakeChild() == false)) {
            ComponentName component = new ComponentName(info.activityInfo.packageName,
                    info.activityInfo.name);
            processShortcutFromDrop(component, LauncherSettings.Favorites.CONTAINER_DESKTOP,
                    getCurrentWorkspaceScreen(), null, null);
        } else {
            showOutOfSpaceMessage(isHotseatLayout(layout));
        }
    }
    public int getStatusBarHeight() {
        return mStatusBarHeight;
    }
    
    private void meausreStatusBarHeight() {
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            mStatusBarHeight = getResources().getDimensionPixelSize(resourceId);
        } else {
            Rect rectangle = new Rect();
            Window window = getWindow();
            window.getDecorView().getWindowVisibleDisplayFrame(rectangle);
            int statusBarHeight = rectangle.top;
            View view = window.findViewById(Window.ID_ANDROID_CONTENT);
            if (view != null) {
                int contentViewTop = view.getTop();
                mStatusBarHeight = statusBarHeight - contentViewTop;
            }
        }
    }

	//global search begin
    public boolean isInIdleStatus(){
    	boolean idle = true;
        if ((mWorkspace != null) && (mWorkspace.getOpenFolder() != null)){
    		idle = false;
    	}else if(isInEditScreenMode()){
    		idle = false;
    	}else if(isHideseatShowing()){
    		idle = false;
    	}else if(isAllAppsVisible()){
    		idle = false;
    	}else if(isGadgetCardShowing()){
                idle = false;
        }else if(mFolderUtils.isFolderOpened()){
                idle = false;
        } else if (CheckVoiceCommandPressHelper.PUSH_TO_TALK_SUPPORT &&
                (CheckVoiceCommandPressHelper.getInstance().isVoiceUIShown() || CheckVoiceCommandPressHelper.getInstance().isVoiceUIShowMsgSent())) {
            idle = false;
        } else if(!isLifeCenterEnableSearch()){
            idle = false;
        }
    	return idle;
    }
    //global search end

    /**
     * handle click event on downloading icon
     * @param v the View was clicked
     * @return if it's handled
     */
    public boolean onDownloadingClick(View v){
        ShortcutInfo info = (ShortcutInfo)v.getTag();
        String pkgName = info.intent.getStringExtra(AppDownloadManager.TYPE_PACKAGENAME);
        if( pkgName == null ){
            pkgName = info.intent.getComponent().getPackageName();
        }

        Long lastPressTime = mLastPressTimeOfDownloadingIcon.get(pkgName);
        long now = System.currentTimeMillis();
        if (lastPressTime != null && now - lastPressTime < AppDownloadManager.DOWNLOAD_ICON_PRESS_INTERVAL){
            // if user frequently press downloading icon, ignore it
            return false;
        } else {
            mLastPressTimeOfDownloadingIcon.put(pkgName, now);
        }

        if( Utils.isSupportAppStoreQuickControl(this) ){
            // if clicked when downloading, send "Pause" broadcast
            if( info.getAppDownloadStatus() == AppDownloadStatus.STATUS_DOWNLOADING ||
                info.getAppDownloadStatus() == AppDownloadStatus.STATUS_WAITING ){
                Intent intent = new Intent(AppDownloadManager.ACTION_HS_DOWNLOAD_TASK);
                intent.putExtra(AppDownloadManager.TYPE_ACTION, AppDownloadManager.ACTION_HS_DOWNLOAD_PAUSE);
                intent.putExtra(AppDownloadManager.TYPE_PACKAGENAME, pkgName);
                sendBroadcast(intent);
                Log.d(TAG,"send download pause broacast : "+pkgName);
            }
            // if clicked when paused, send "Continue" broadcast
            if( info.getAppDownloadStatus() == AppDownloadStatus.STATUS_PAUSED ){
                Intent intent = new Intent(AppDownloadManager.ACTION_HS_DOWNLOAD_TASK);
                intent.putExtra(AppDownloadManager.TYPE_ACTION, AppDownloadManager.ACTION_HS_DOWNLOAD_RUNNING);
                intent.putExtra(AppDownloadManager.TYPE_PACKAGENAME, pkgName);
                intent.putExtra(AppDownloadManager.TYPE_PROGRESS, info.getProgress());
                sendBroadcast(intent);
                Log.d(TAG,"send download continue broacast : "+pkgName);
            }
            return true;
        }else{
            final Intent downloadintent = info.createDownloadIntent();
            return startActivitySafely(v, downloadintent, v.getTag());
        }
    }

    //items overlay after folder dismiss
    //only move views from workspace to workspace. Not from folder, hotseat or hideseat
    //if move a view in folder to workspace and the folder leave only one item
    //may cause some error
    void moveItemsViewByItemInfo(final ArrayList<ItemInfo> items) {
        if (items == null) {
            return;
        }
        Log.d(TAG, "moveItemsViewByItemInfo in");
        int screenCount = mWorkspace.getChildCount();

        for (ItemInfo moveitem: items) {
            boolean isFound = false;
            for (int screen = 0; screen < screenCount; screen++)  {
                final ViewGroup layout = ((CellLayout)mWorkspace.getChildAt(screen)).getShortcutsAndWidgets();
                if (layout == null) {
                    continue;
                }
                int childCount = layout.getChildCount();
                for (int j = 0; j < childCount; j++) {
                    final View view = layout.getChildAt(j);
                    Object tag = view.getTag();
                    final long id = ((ItemInfo)tag).id;
                    int itemtype = ((ItemInfo)tag).itemType;

                    if (id == moveitem.id) {
                        Log.d(TAG, "find same id " + id);
                        layout.removeViewInLayout(view);
                        layout.invalidate();
                        mWorkspace.addInScreen(view, moveitem.container, moveitem.screen,
                                moveitem.cellX, moveitem.cellY, moveitem.spanX, moveitem.spanY);
                        isFound = true;
                        break;
                    }
                }
                if (isFound == true) {
                    break;
                }
            }
        }
    }
    public void startFlipAnimation(final View self, final View gadget){
        // do not add gadget when is animating
        if (mFlipAnim.isAnimating() || mFlipAnim.isWaiting() ){
            return;
        }

        mFlipStartTime = SystemClock.uptimeMillis();
        ShortcutInfo info = (ShortcutInfo)((BubbleTextView)self).getTag();
        //the info is verified in BubbleTextView onFlingUp.
        mFlipCardPkgName = info.intent.getComponent().getPackageName();
        //mFlipCardType = gadget instanceof GadgetView ? "Special" : "Normal";
        mFlipCardType = gadget instanceof CardNotificationPanelView ? "Normal" : "Special";

        // gesture has been detected and it's waiting for animation on main thread
        mFlipAnim.setIsWaiting(true);

        // for bug 5233174,5237445 gadget already has a parent
        ViewGroup gadgetParent = (ViewGroup)gadget.getParent();
        if( gadgetParent != null ){
            gadgetParent.removeView(gadget);
        }

        // Add Blue Background
        final View bg = new View(this);
        mDragLayer.addView(bg);
        DragLayer.LayoutParams bgLp = new DragLayer.LayoutParams(mDragLayer.getWidth(), mDragLayer.getHeight());
        bg.setLayoutParams(bgLp);

        // Add Big Card View
        int height = getResources().getDimensionPixelSize(R.dimen.big_card_view_height);
        int width  = getResources().getDimensionPixelSize(R.dimen.big_card_view_width);
        gadget.setAlpha(0);
        mDragLayer.addView(gadget,new DragLayer.LayoutParams(width, height));

        // Add Small Card View
        int points[] = new int[2];
        Canvas canvas = new Canvas();
        mDragLayer.getLocationInDragLayer(self, points);
        Bitmap b = getWorkspace().createDragBitmap(self, canvas, 0);
        final ImageView iv = new ImageView(this);
        iv.setImageBitmap(b);
        mDragLayer.addView(iv);
        DragLayer.LayoutParams lp = new DragLayer.LayoutParams(b.getWidth(), b.getHeight());
        lp.x = points[0];
        lp.y = points[1];
        lp.customPosition = true;
        iv.setLayoutParams(lp);

        // post the animation to main thread to make sure views get their size
        mDragLayer.post(new Runnable() {
            @Override
            public void run() {
                mFlipAnim.setIsWaiting(false);
                mFlipAnim.setSmallCard(iv);
                mFlipAnim.setBigCard(gadget);
                mFlipAnim.setBackground(bg);
                mFlipAnim.setThatBubble(self);
                mFlipAnim.computeValues();
                mFlipAnim.appear();
            }
        });

        bg.setOnClickListener(new OnClickListener() {
            public void onClick(View arg0) {
                if( !mFlipAnim.isAnimating() ){
                   stopFlipAnimation();
                }
            }
        });
    }

    public void stopFlipWithoutAnimation(){
        mFlipAnim.clear();
        sendCardStayTimeMsg();
    }

    public void stopFlipAnimation(){
        mFlipAnim.disappear();
        sendCardStayTimeMsg();
    }

    private void sendCardStayTimeMsg() {
            Map<String, String> msg = new HashMap<String, String>();
            if (mFlipStartTime == 0) {
                return;
            }
            long endtime = SystemClock.uptimeMillis();
            msg.put("Time", String.valueOf(endtime - mFlipStartTime));
            msg.put("PkgName", mFlipCardPkgName);
            msg.put("Type", mFlipCardType);
            UserTrackerHelper.sendUserReport(UserTrackerMessage.MSG_CARD_STAY_TIME, msg);
            mFlipStartTime = 0;
    }

    private void sendLauncherStayTimeMsg(View v, Intent intent, Object tag) {
        if (mResumeTime == 0) {
            return;
        }
        if (!(v instanceof BubbleTextView) || !(tag instanceof ShortcutInfo) || (intent == null)) {
            return;
        }
        ShortcutInfo info = (ShortcutInfo)tag;
        if ((info.itemType != Favorites.ITEM_TYPE_APPLICATION) && (info.itemType != Favorites.ITEM_TYPE_SHORTCUT)) {
            return;
        }
        Map<String, String> msg = new HashMap<String, String>();
        long endtime = SystemClock.uptimeMillis();
        msg.put("Time", String.valueOf(endtime - mResumeTime));
        String pkgName;
        if (intent.getComponent() != null) {
            pkgName = intent.getComponent().getPackageName();
        } else if (intent.getPackage() != null) {
            pkgName = intent.getPackage();
        } else {
            Log.d(TAG, "no package name");
            return;
        }
        msg.put("PkgName", pkgName);
        msg.put("Screen", String.valueOf(info.screen));
        msg.put("type", info.itemType == Favorites.ITEM_TYPE_APPLICATION ? "app" : "shortcut");
        UserTrackerHelper.sendUserReport(UserTrackerMessage.MSG_LAUNCHER_STAY_TIME, msg);
        mResumeTime = 0;
    }

    public void adjustToThreeLayout() {
        mWorkspace.adjustToThreeLayout();
        mHotseat.adjustToThreeLayout();
        mHideseat.adjustFromThreeLayout();
        FolderIcon.onThemeChanged();
        ((FrameLayout.LayoutParams) mIndicatorView.getLayoutParams()).bottomMargin = getResources()
                .getDimensionPixelSize(R.dimen.workspace_cell_height_3_3);
        mIndicatorView.getLayoutParams().height = getResources()
                .getDimensionPixelSize(R.dimen.page_indicator_height_3_3);
        mCustomHideseat.getLayoutParams().height = ConfigManager.getHideseatHeight();
        positionHideseat();
        FolderRingAnimator.refreshStaticValues();
    }

    public void adjustFromThreeLayout() {
        mWorkspace.adjustFromThreeLayout();
        mHotseat.adjustFromThreeLayout();
        mHideseat.adjustFromThreeLayout();
        FolderIcon.onThemeChanged();
        ((FrameLayout.LayoutParams) mIndicatorView.getLayoutParams()).bottomMargin = getResources()
                .getDimensionPixelSize(R.dimen.button_bar_height_plus_padding);
        mIndicatorView.getLayoutParams().height = getResources()
                .getDimensionPixelSize(R.dimen.page_indicator_height);
        mCustomHideseat.getLayoutParams().height = ConfigManager.getHideseatHeight();
        positionHideseat();
        FolderRingAnimator.refreshStaticValues();
    }
    @Override
    public void collectCurrentViews() {
        currentViews.clear();
        int count = mWorkspace.getChildCount();
        for (int i = 0; i < count; i++) {
            CellLayout layout = (CellLayout) mWorkspace.getChildAt(i);
            int childCount = layout.getShortcutAndWidgetContainer().getChildCount();
            for (int j = 0; j < childCount; j++) {
            	if(false == layout.isWidgetPage()) {
            		currentViews.add(layout.getShortcutAndWidgetContainer().getChildAt(j));
            		layout.removeViewAt(j);
            	}
            }
            //layout.removeAllViewsInLayout();
        }
    }

    @Override
    public void reLayoutCurrentViews() {
        // TODO Auto-generated method stub
        for (View view : currentViews) {
            ((CellLayout.LayoutParams) view.getLayoutParams()).useTmpCoords = false;
            ItemInfo itemInfo = (ItemInfo) view.getTag();
            mWorkspace.addInScreen(view, itemInfo.container, itemInfo.screen, itemInfo.cellX,
                    itemInfo.cellY, itemInfo.spanX, itemInfo.spanY);
        }
        currentViews.clear();
    }
    public void onWorkspacePageBeginMoving() {
        /*for (WeakReference<GadgetView> wrf : gadgetViewList) {
            GadgetView v = wrf.get();
            if (v != null) {
                v.onPause();
            }
        }*/
        if(isDraggingEnabled() && (mPaused == false)) {
	        Log.d(LIVE_WEATHER_TAG, "onWorkspacePageBeginMoving begin");
	        UnBindParticleService();
	        Log.d(LIVE_WEATHER_TAG, "onWorkspacePageBeginMoving end");
        }
    }

    public void onWorkspacePageEndMoving() {
        /*for (Iterator<WeakReference<GadgetView>> iterator = gadgetViewList.iterator(); iterator.hasNext();) {
            WeakReference<GadgetView> wrf = iterator.next();
            GadgetView v = wrf.get();
            if (v != null) {
                v.onResume();
            } else {
                iterator.remove();
            }
        }*/
        if(isDraggingEnabled() && (mPaused == false)) {
	        Log.d(LIVE_WEATHER_TAG, "onWorkspacePageEndMoving begin");
	        OnBindParticleService();
	        Log.d(LIVE_WEATHER_TAG, "onWorkspacePageEndMoving end");
        }
    }

    /*void addGadgetView(GadgetView v) {
        gadgetViewList.add(new WeakReference<GadgetView>(v));
    }*/

    //daiwei modify begin
    private Animator getWidgetPageHotseatAnimator(boolean hide) {
    	Animator bounceAnim = null;
		if(LauncherApplication.getLauncher().mWidgetPageManager.isSupportWidgetPageHotseat()) {	
	    	View v = mWorkspace.getPageAt(mWorkspace.getCurrentPage());
	    	if (v instanceof CellLayout) {
	    		CellLayout celllayout = (CellLayout)v;
	    		if (celllayout.isWidgetPage()) {
	    			View hotseat = mWidgetPageManager.getHotseatView(celllayout.getWidgetPagePackageName());
	    			int top = hotseat.getTop();
	    			int bottom = hotseat.getBottom();
	    	        int startY = hide ? top : bottom;
	    	        int endY = hide ? bottom : top;
	    	        bounceAnim = ObjectAnimator.ofFloat(hotseat, "y",startY, endY);
	    	        bounceAnim.setDuration(getResources().getInteger(R.integer.config_workspaceUnshrinkTime));
	    	        bounceAnim.setInterpolator(new LinearInterpolator());
	    	        return bounceAnim;    			
	    		}
	    	}
	}
    	return bounceAnim;
    }
    
    private Animator getHotseatAnimator(boolean hide) {
	if(LauncherApplication.getLauncher().mWidgetPageManager.isSupportWidgetPageHotseat()) {	
	    	View v = mWorkspace.getPageAt(mWorkspace.getCurrentPage());
	    	if (v instanceof CellLayout && ((CellLayout)v).isWidgetPage()) {
	    		return getWidgetPageHotseatAnimator(hide);
	    	} else {
	    		return getHotseat().getHotseatAnimator(hide);
	    	}
	}else {
		return getHotseat().getHotseatAnimator(hide);
	}
    }
    
    //daiwei modify end

    //topwise zyf add for notify
    private class openfolderRunnable implements Runnable
    {
    	FolderIcon mFIcon;
	    public openfolderRunnable(FolderIcon fIcon){
	    		this.mFIcon=fIcon;
	    }
	    public void run() {
	    	handleFolderClick(this.mFIcon);
	    }
    }
    //topwise zyf add for notify end
    
	static final String LIVE_WEATHER_TAG = "Launcher_LiveWeather";

    public static final int ICON_RECT0 = 1000;
    public static final int ICON_RECT1 = 1100;
    private Messenger mParticleService = null;
    private static final int WEATHER_TYPE_NONE = 1;					//无
    
    public static boolean isSupportLiveWeather(){
    	boolean value = android.os.TopwiseProp.getDefaultSettingBoolean("topwise_support_liveweather");
        return value;
    }
    
	private ServiceConnection mConnection = new ServiceConnection() {
		 
	    @Override
	    public void onServiceDisconnected(ComponentName name) {
			Log.d(LIVE_WEATHER_TAG, "onServiceDisconnected begin");
			mParticleService = null;
			Log.d(LIVE_WEATHER_TAG, "onServiceDisconnected end");
	    }
		
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			Log.d(LIVE_WEATHER_TAG, "onServiceConnected begin");
			mParticleService = new Messenger(service);
			Log.d(LIVE_WEATHER_TAG, "onServiceConnected end");
		}
	};
	
	private static String getParticlePackageName(){
		String value = "com.topwise.liveweather";
		return value;
	}

	private static String getParticleClassName(){
		String value = "com.topwise.liveweather.TopWindowService";
		return value;
	}

	private void OnSendMsg(int what, int x, int y) {
		if(mParticleService != null){
			Log.d(LIVE_WEATHER_TAG, "mService is not null");
			Message msg=Message.obtain(null, what, x, y);
			try{
				mParticleService.send(msg);
			}
			catch(RemoteException e){
				e.printStackTrace();
			}
		}
	}
   
	public void OnBindParticleService() {
		int magic_weather_type = HomeShellSetting.getLiveWeatherMode(this.getApplicationContext());
		Log.d(LIVE_WEATHER_TAG, "OnBindParticleService magic_weather_type is " + magic_weather_type);
		
		if(isSupportLiveWeather() && (magic_weather_type != WEATHER_TYPE_NONE)) {
			Log.d(LIVE_WEATHER_TAG, "OnBindParticleService begin");
			CellLayout currentlayout = (CellLayout) mWorkspace.getChildAt(mWorkspace.getCurrentPage());
	
			if((mParticleService == null) && (!currentlayout.isWidgetPage())) {
				Log.d(LIVE_WEATHER_TAG, "mService is null");
				Intent intent = new Intent();
				intent.setClassName(getParticlePackageName(), getParticleClassName());
				
				intent.putExtra("LiveWeatherType", magic_weather_type);
				if((currentlayout != null) && (!mIsEditMode)) {
	                //Log.d(LIVE_WEATHER_TAG, "onServiceConnected LauncherModel.getCellCountX() is " + LauncherModel.getCellCountX());
	                //Log.d(LIVE_WEATHER_TAG, "onServiceConnected LauncherModel.getCellCountY() is " + LauncherModel.getCellCountY());
	
	                //Rect r = new Rect();
	                int[] temp = new int[2];
	                int left;
	                int top;
	                int right;
	                int bottom;
	
		            int icon_width = ThemeUtils.getIconSize(LauncherApplication.getContext());
	                Log.d(LIVE_WEATHER_TAG, "OnBindParticleService icon_width is " + icon_width);
	
	                if(icon_width <= 0) {
	                    if (AgedModeUtil.isAgedMode()) {
	                    	icon_width = (int)((float)LauncherApplication.getContext().getResources().getDimensionPixelSize(R.dimen.app_icon_size) * AgedModeUtil.SCALE_RATIO_FOR_AGED_MODE);
	                    } else {
	                    	icon_width = LauncherApplication.getContext().getResources().getDimensionPixelSize(R.dimen.app_icon_size);
	                    }
	                }
	                
	                Drawable drawable = null;
	                int drawable_height = 0;
	
	                ShortcutAndWidgetContainer container = currentlayout.getShortcutsAndWidgets();
	                if (container.getChildCount() > 0) {
			            //Log.d(LIVE_WEATHER_TAG,"onServiceConnected container.getChildCount() is:" + (container.getChildCount()));
		                //Log.d(LIVE_WEATHER_TAG, "OnBindParticleService icon_width is " + icon_width);
			            for (int i = 0; i < container.getChildCount(); i++) {
			                //ItemInfo item = (ItemInfo) container.getChildAt(i).getTag();
			                View view = container.getChildAt(i);
			                Log.d(LIVE_WEATHER_TAG, "OnBindParticleService i is " + i);
	
			                if (view instanceof BubbleTextView){
			                	drawable = ((TextView) view).getCompoundDrawables()[1];
			                	if(drawable != null) {
					                //Log.d(LIVE_WEATHER_TAG, "OnBindParticleService drawable.getBounds().left is " + drawable.getBounds().left);
					                //Log.d(LIVE_WEATHER_TAG, "OnBindParticleService drawable.getBounds().top is " + drawable.getBounds().top);
					                //Log.d(LIVE_WEATHER_TAG, "OnBindParticleService drawable.getBounds().right is " + drawable.getBounds().right);
					                //Log.d(LIVE_WEATHER_TAG, "OnBindParticleService drawable.getBounds().bottom is " + drawable.getBounds().bottom);
					                drawable_height = drawable.getBounds().bottom - drawable.getBounds().top;
			                	}
			                	break;
			                }
			            }
			        }
	                
			        if (container.getChildCount() > 0) {
			            //Log.d(LIVE_WEATHER_TAG,"onServiceConnected container.getChildCount() is:" + (container.getChildCount()));
		                //Log.d(LIVE_WEATHER_TAG, "OnBindParticleService icon_width is " + icon_width);
			            for (int i = 0; i < container.getChildCount(); i++) {
			                //ItemInfo item = (ItemInfo) container.getChildAt(i).getTag();
			                View view = container.getChildAt(i);
			                Log.d(LIVE_WEATHER_TAG, "OnBindParticleService i is " + i);
	
			                //getDragLayer().getLocationInDragLayer(view, temp);
			                getDragLayer().getDescendantCoordRelative(view, temp);
			                //Log.d(LIVE_WEATHER_TAG, "OnBindParticleService temp[0] is " + temp[0]);
			                //Log.d(LIVE_WEATHER_TAG, "OnBindParticleService temp[1] is " + temp[1]);
	
			                /*if (view instanceof GadgetView) {
			                	continue;
			                }*/
			                if (view instanceof LauncherAppWidgetHostView) {
			                	continue;
			                }
			                if ((view instanceof BubbleTextView) || (view instanceof FolderIcon) || (view instanceof FixedFolderIcon)) {
				                left = temp[0] + (view.getWidth() - icon_width) / 2;
				                top = temp[1] + (view.getHeight() - drawable_height) / 2;
				                right = left + icon_width;
				                bottom = top + drawable_height;
	
				                //Log.d(LIVE_WEATHER_TAG, "OnBindParticleService left is " + left);
				                //Log.d(LIVE_WEATHER_TAG, "OnBindParticleService top is " + top);
				                //Log.d(LIVE_WEATHER_TAG, "OnBindParticleService right is " + right);
				                //Log.d(LIVE_WEATHER_TAG, "OnBindParticleService bottom is " + bottom);
				                
				                String strLeft = "ICON_RECT_LEFT" + i;
				                String strTop = "ICON_RECT_TOP" + i;
				                String strRight = "ICON_RECT_RIGHT" + i;
				                String strBottom = "ICON_RECT_BOTTOM" + i;
				                intent.putExtra(strLeft, left);
				                intent.putExtra(strTop, top);
				                intent.putExtra(strRight, right);
				                intent.putExtra(strBottom, bottom);
			                }
			            }
			        }
	
			        ShortcutAndWidgetContainer hotseat_container = getHotseat().getLayout().getShortcutAndWidgetContainer();
	
	                if (hotseat_container.getChildCount() > 0) {
			            //Log.d(LIVE_WEATHER_TAG,"onServiceConnected container.getChildCount() is:" + (hotseat_container.getChildCount()));
		                //Log.d(LIVE_WEATHER_TAG, "OnBindParticleService icon_width is " + icon_width);
			            for (int i = 0; i < hotseat_container.getChildCount(); i++) {
			                //ItemInfo item = (ItemInfo) container.getChildAt(i).getTag();
			                View view = hotseat_container.getChildAt(i);
			                Log.d(LIVE_WEATHER_TAG, "OnBindParticleService i is " + i);
	
			                if (view instanceof BubbleTextView){
			                	drawable = ((TextView) view).getCompoundDrawables()[1];
			                	if(drawable != null) {
					                //Log.d(LIVE_WEATHER_TAG, "OnBindParticleService drawable.getBounds().left is " + drawable.getBounds().left);
					                //Log.d(LIVE_WEATHER_TAG, "OnBindParticleService drawable.getBounds().top is " + drawable.getBounds().top);
					                //Log.d(LIVE_WEATHER_TAG, "OnBindParticleService drawable.getBounds().right is " + drawable.getBounds().right);
					                //Log.d(LIVE_WEATHER_TAG, "OnBindParticleService drawable.getBounds().bottom is " + drawable.getBounds().bottom);
					                drawable_height = drawable.getBounds().bottom - drawable.getBounds().top;
			                	}
			                	break;
			                }
			            }
			        }
	                
			        if (hotseat_container.getChildCount() > 0) {
			            Log.d(LIVE_WEATHER_TAG,"onServiceConnected hotseat_container.getChildCount() is:" + (hotseat_container.getChildCount()));
			            for (int i = 0; i < hotseat_container.getChildCount(); i++) {
			                View view = hotseat_container.getChildAt(i);
			                
			                Log.d(LIVE_WEATHER_TAG, "OnBindParticleService i is " + (i + 20));
			                
			                //getDragLayer().getLocationInDragLayer(view, temp);
			                getDragLayer().getDescendantCoordRelative(view, temp);
			                //Log.d(LIVE_WEATHER_TAG, "OnBindParticleService temp[0] is " + temp[0]);
			                //Log.d(LIVE_WEATHER_TAG, "OnBindParticleService temp[1] is " + temp[1]);
			                
			                left = temp[0] + (view.getWidth() - icon_width) / 2;
			                top = temp[1] + (view.getHeight() - drawable_height) / 2;
			                right = left + icon_width;
			                bottom = top + drawable_height;
	
			                //Log.d(LIVE_WEATHER_TAG, "OnBindParticleService left is " + left);
			                //Log.d(LIVE_WEATHER_TAG, "OnBindParticleService top is " + top);
			                //Log.d(LIVE_WEATHER_TAG, "OnBindParticleService right is " + right);
			                //Log.d(LIVE_WEATHER_TAG, "OnBindParticleService bottom is " + bottom);
	
			                String strLeft = "ICON_RECT_LEFT" + (i + 20);
			                String strTop = "ICON_RECT_TOP" + (i + 20);
			                String strRight = "ICON_RECT_RIGHT" + (i + 20);
			                String strBottom = "ICON_RECT_BOTTOM" + (i + 20);
			                intent.putExtra(strLeft, left);
			                intent.putExtra(strTop, top);
			                intent.putExtra(strRight, right);
			                intent.putExtra(strBottom, bottom);
			            }
			        }
				}
				
				try {
					getApplicationContext().bindService(intent, mConnection, Service.BIND_AUTO_CREATE);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
	
			Log.d(LIVE_WEATHER_TAG, "OnBindParticleService end");
		}
	}

	public void UnBindParticleService() {
		int magic_weather_type = HomeShellSetting.getLiveWeatherMode(this.getApplicationContext());
		Log.d(LIVE_WEATHER_TAG, "UnBindParticleService magic_weather_type is " + magic_weather_type);

		if(isSupportLiveWeather() && (magic_weather_type != WEATHER_TYPE_NONE)) {
			Log.d(LIVE_WEATHER_TAG, "UnBindParticleService begin");
		
			if(mParticleService != null) {
				Log.d(LIVE_WEATHER_TAG, "mService is not null");
				try {
					getApplicationContext().unbindService(mConnection);
					mConnection.onServiceDisconnected(null);
				} catch (Exception e) {
					Log.d(LIVE_WEATHER_TAG, "e is " + e.getMessage());
					e.printStackTrace();
				}
			}
		
			Log.d(LIVE_WEATHER_TAG, "UnBindParticleService end");
		}
	}
}

interface LauncherTransitionable {
    View getContent();
    void onLauncherTransitionPrepare(Launcher l, boolean animated, boolean toWorkspace);
    void onLauncherTransitionStart(Launcher l, boolean animated, boolean toWorkspace);
    void onLauncherTransitionStep(Launcher l, float t);
    void onLauncherTransitionEnd(Launcher l, boolean animated, boolean toWorkspace);
}
