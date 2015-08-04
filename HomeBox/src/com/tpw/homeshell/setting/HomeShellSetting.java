package com.tpw.homeshell.setting;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import android.provider.Settings;
import storeaui.app.HWPreferenceActivity;
import storeaui.preference.HWSwitchPreference;
import storeaui.widget.ActionSheet;
import storeaui.widget.ActionSheet.SingleChoiceListener;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceCategory;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.util.Log;
import android.widget.Toast;

import com.tpw.homeshell.AgedModeUtil;
import com.tpw.homeshell.CardNotificationListenerService;
import com.tpw.homeshell.Launcher;
import com.tpw.homeshell.LauncherApplication;
import com.tpw.homeshell.R;
import com.tpw.homeshell.TopwiseConfig;
import com.tpw.homeshell.UserTrackerHelper;
import com.tpw.homeshell.UserTrackerMessage;
import com.tpw.homeshell.icon.IconManager;
import com.tpw.homeshell.model.LauncherModel;
import com.tpw.homeshell.utils.Utils;

import commonlibs.utils.ACA;

public class HomeShellSetting extends HWPreferenceActivity implements
        Preference.OnPreferenceChangeListener,
        Preference.OnPreferenceClickListener{
        //ThemeChangedListener.IThemeChanged{
    private static final String TAG = "HomeShellSetting";
    private PreferenceScreen mContainer;
    private PreferenceCategory mLayoutCategory;
    private PreferenceCategory mEffectCategory;
    private PreferenceCategory mLiveWeatherCategory;
    private PreferenceCategory mNewMarkIconCategory;

    private Preference mEffectPref = null;
    private Preference mLiveWeatherPref = null;
    private Preference mArrangePagePref = null;
    private SharedPreferences mSharedRef;
    private Preference mLayoutPref = null;
    private HWSwitchPreference mContinuousHomePref;
    private int mOldLayoutMode = 0;
    private HWSwitchPreference mShowNewMarkIconPref = null;
    private Preference m3rdAppNotificationPrefNew = null;
    private TypedArray mImgTypeArray;
    private String[] mEffectTitle;
    private String[] mEffectValue;
    private String[] mLiveWeatherTitle;
    private String[] mLiveWeatherValue;
    private String[] mLayoutTitle;
    private String[] mLayoutValue;
    private String[] mNotificationTitle;

    public static final String KEY_PRE_EFFECT_STYLE = "effect_preference";
    public static final String KEY_PRE_LIVEWEATHER_STYLE = "liveweather_preference";
    public static final String KEY_PRE_LAYOUT_STYLE = "layout_preference";
    public static final String KEY_NOTIFICATION_MARK_PREF_OLD = "3rd_app_notification_preference";
    public static final String KEY_NOTIFICATION_MARK_PREF_NEW = "3rd_app_notification_preference_new";
    private static final String KEY_PRE_ARRANGE_PAGE = "arrange_page_preference";
    private static final String KEY_PRE_SHOW_NEW_MARK = "show_new_mark_preference";
    private static final int EVENT_ARRANGE_LAYOUT = 1;
    private static final int EVENT_LAYOUT_STYLE = 2;

    /* three ways to show notifications */
    public static int ALL_NOTIFICATION  = 0;
    public static int PART_NOTIFICATION = 1;
    public static int NO_NOTIFICATION   = 2;

    public static final String DB_SHOW_NEW_MARK_ICON = "db_show_new_mark_icon";
    
    private boolean mIsShowNewMarkIconOldMode;

    private IconManager mIconManager;

    private boolean mIsShowIconMarkChange;

    private static final String CONTINUOUS_HOMESHELL_SHOW_ACTION = "tpw.settings.CONTINUOUS_HOMESHELL_SHOW_CHECKED";
    private static final String CONTINUOUS_HOMESHELL_SHOW_KEY = "ContinuousHomeShellChecked";
    private static final String KEY_CONTINUOUS_HOMESHELL_STYLE = "continuous_homeshell";

    public static final String ACTION_ON_MARK_TYPE_CHANGE = "com.tpw.homeshell.markTypeChange";
    public static final String ACTION_ON_SHOW_NEW_MARK_CHANGE = "com.tpw.homeshell.showNewMarkChange";

    private HWSwitchPreference mFreezePref;
    private HWSwitchPreference mAgedPref;
    public static final String FREEZE_HOMESHELL_FLAG = "FREEZE_HOMESHELL_FLAG";
    public static final String AGED_MODE_FLAG = "AGED_MODE_FLAG";
    public static final String KEY_PRENO_AGED_LAYOUT_STYLE = "no_aged_layout_preference";

    private HWSwitchPreference mShortCutPref;
    public static final String KEY_PRE_SHORTCUT = "shortcut_preference";
    public static final String SHORTCUT_MODE = "shortcut_mode";

    private Context mContext;
    private ActionSheet mLayoutActionSheet;
    private ActionSheet mNotificationActionSheet;
    private Set<String> mNotificationPackageNames;
    public static final String EXTRA_NOTIFICATION_PACKAGE_NAMES = "com.tpw.homeshell.NotificationPackageNames";
    public static String THEME_CHANGED_ACTION = "com.tpw.homeshellsetting.ACTION_THEME_CHANGED";

    private static final String[] MARK_NOTIFICATION_ITEMS = new String[] {"all", "major", "off"};
    private static final String[] PATTERNS = new String[] {"old", "standard"};
    private static final String[] LOCK_STATUS = new String[] {"on", "off"};

    private void registerThemeChangedReceiver(Context context) {
        final IntentFilter filter = new IntentFilter();
        filter.addAction(THEME_CHANGED_ACTION);
        context.registerReceiver(mSettingReceiver, filter);
    }
    private final BroadcastReceiver mSettingReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent == null) {
                Log.e(TAG, "Intent is null, out of memory?");
                return;
            }
            final String action = intent.getAction();
            Log.d(TAG, "action="+action);
            if (THEME_CHANGED_ACTION.equals(action)) {
                updateLayoutPreferenceContent();
            }
        }
    };
    // end of 5258002 
    private Handler mHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
            case EVENT_ARRANGE_LAYOUT:
                if (LauncherApplication.mLauncher != null) {
                    LauncherApplication.mLauncher.getWorkspace()
                            .arrangeAllPagesPostDelay(
                                    new HomeShellSetting.Callback() {
                                        @Override
                                        public void onFinish() {
                                            Utils.dismissLoadingDialog();
                                        }
                                    });
                } else {
                    Utils.dismissLoadingDialog();
                }
                break;
            case EVENT_LAYOUT_STYLE:
                LauncherApplication app = (LauncherApplication) mContext
                        .getApplicationContext();
                    app.getModel().changeLayout(msg.arg1, msg.arg2);
                break;

            default:
                break;
            }
        };
    };
    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        mIconManager = ((LauncherApplication) this.getApplicationContext()).getIconManager();
        mContext = this;
        initActionBar();
        setTitle2(this.getResources().getString(R.string.menu_item_homeshellsetting));
        showBackKey(true);
        
        // modify by huangxunwan for coolPad UI 20150430
        getActionBar().getCustomView().setBackgroundResource(R.color.tpw_color_actionbar);
        
        // getHeaderBuilder().setBackgroundResource(R.drawable.header_bg);
        loadResources();

        mSharedRef = getPreferenceManager().getSharedPreferences();
        mContainer = getPreferenceManager().createPreferenceScreen(this);
        setPreferenceScreen(mContainer);

        mEffectCategory = new PreferenceCategory(this);
        mEffectCategory.setTitle(R.string.settings_individuation_title);
        mEffectCategory.setKey("0");
        mContainer.addPreference(mEffectCategory);

        mEffectPref = new Preference(this);
        mEffectPref.setTitle(R.string.settings_screen_slide_effect);
        mEffectPref.setOnPreferenceClickListener(this);
        mEffectPref.setKey(KEY_PRE_EFFECT_STYLE);
        updateEffectSummary(false);
        mEffectCategory.addPreference(mEffectPref);

        if(Launcher.isSupportLiveWeather()) {
	        mLiveWeatherCategory = new PreferenceCategory(this);
	        mLiveWeatherCategory.setTitle(R.string.settings_individuation_title);
	        mLiveWeatherCategory.setKey("0");
	        mContainer.addPreference(mLiveWeatherCategory);
	
	        mLiveWeatherPref = new Preference(this);
	        mLiveWeatherPref.setTitle(R.string.settings_screen_live_weather);
	        mLiveWeatherPref.setOnPreferenceClickListener(this);
	        mLiveWeatherPref.setKey(KEY_PRE_LIVEWEATHER_STYLE);
	        updateLiveWeatherSummary();
	        mLiveWeatherCategory.addPreference(mLiveWeatherPref);
        }

        mLayoutCategory = new PreferenceCategory(this);
        mLayoutCategory.setTitle(R.string.settings_layout_title);
        mLayoutCategory.setKey("1");
        mContainer.addPreference(mLayoutCategory);
        rebuildLayoutPreference();

        addArrangePagePreference();
        if( !isLifeCenterExists() ){
            addContinuousHomePreference();
        }
        addNewMarkPreference();

        addNotificationMarkNew();


        addFreezePreference();

        if(hasShortCutFeature())
            addShortCutPreference();

        // add ThemeChangeListener when theme changed to update layout Preference
        //ThemeChangedListener.getInstance(this).addListener(this);
        registerThemeChangedReceiver(this);
        if (LauncherApplication.homeshellSetting != this) {
            LauncherApplication.homeshellSetting = this;
        }
        LauncherModel.homeshellSetting = this;
    }

    boolean hasShortCutFeature(){
        String shortcut = ACA.SystemProperties.get("ro.yunos.support.shortcut","unknown");
        return "yes".equals(shortcut);
    }

    private void addShortCutPreference() {
        mShortCutPref = new HWSwitchPreference(this);
        mShortCutPref.setTitle(R.string.permlab_install_shortcut);
        mShortCutPref.setSummary(R.string.permdesc_install_shortcut);
        mShortCutPref.setKey(KEY_PRE_SHORTCUT);
        mShortCutPref.setChecked(getShortCutValue(this));
        mContainer.addPreference(mShortCutPref);
        mShortCutPref.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                boolean isOn = (Boolean) newValue;
                mShortCutPref.setChecked(isOn);
                setShortCutValue(HomeShellSetting.this,isOn);
                Map<String, String> pParams = new HashMap<String, String>();
                pParams.put("status", isOn ? "on" : "off");
                UserTrackerHelper.sendUserReport(HomeShellSetting.this, UserTrackerMessage.MSG_INSTALL_SHORTCUT, pParams);
                return true;
            }
        });
    }

    public static boolean getShortCutValue(Context context) {
        return (Settings.Secure.getInt(context.getContentResolver(),SHORTCUT_MODE,0) == 1);
    }

    public static void setShortCutValue(Context context, boolean value) {
        Settings.Secure.putInt(context.getContentResolver(),SHORTCUT_MODE,value ? 1 : 0);
    }

    private void addFreezePreference() {
        // TODO Auto-generated method stub
        mFreezePref = new HWSwitchPreference(this);
        mFreezePref.setTitle(R.string.aged_freeze_homeshell);
        mFreezePref.setSummary(R.string.aged_freeze_homeshell_sub);
        mFreezePref.setKey(FREEZE_HOMESHELL_FLAG);
        mFreezePref.setChecked(getFreezeValue(this));
        mContainer.addPreference(mFreezePref);
        mFreezePref.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                boolean isOn = (Boolean) newValue;
                mFreezePref.setChecked(isOn);
                Map<String, String> param = new HashMap<String, String>();
                param.put(UserTrackerMessage.Key.PATTERN, AgedModeUtil.isAgedMode() ? PATTERNS[0]
                        : PATTERNS[1]);
                param.put(UserTrackerMessage.Key.RESULT, isOn ? LOCK_STATUS[0] : LOCK_STATUS[1]);
                UserTrackerHelper.sendUserReport(UserTrackerMessage.MSG_EDIT_LOCK, param);
                return true;
            }
        });
    }

    public static boolean getFreezeValue(Context context) {
        SharedPreferences preference = PreferenceManager.getDefaultSharedPreferences(context);
        if (preference == null) {
            return false;
        }
        return preference.getBoolean(FREEZE_HOMESHELL_FLAG, false);
    }

    public static void setFreezeValue(Context context, boolean value) {
        SharedPreferences preference = PreferenceManager.getDefaultSharedPreferences(context);
        if (preference == null) {
            return;
        }
        SharedPreferences.Editor editor = preference.edit();
        editor.putBoolean(FREEZE_HOMESHELL_FLAG, value);
        editor.commit();
    }

    public static void setLayoutValue(Context context, boolean aged) {
        SharedPreferences preference = PreferenceManager.getDefaultSharedPreferences(context);
        if (preference == null) {
            return;
        }
        SharedPreferences.Editor editor = preference.edit();
        String current = preference.getString(KEY_PRE_LAYOUT_STYLE, "");
        String backup = preference.getString(KEY_PRENO_AGED_LAYOUT_STYLE, "");
        if (current != null && !current.isEmpty()) {
            editor.putString(KEY_PRENO_AGED_LAYOUT_STYLE, current);
            editor.commit();
        }

        if (backup == null || backup.isEmpty()) {
            backup = aged ? "2" : "0";
        }
        editor.putString(KEY_PRE_LAYOUT_STYLE, backup);
        editor.commit();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        updateEffectSummary(false);
        if(Launcher.isSupportLiveWeather()) {
        	updateLiveWeatherSummary();
        }

        rebuildLayoutPreference();
    }

    private void rebuildLayoutPreference(){
        // create layout preference if necessary
        if (mLayoutPref == null) {
            mLayoutPref = new Preference(this);
            mLayoutPref.setTitle(R.string.settings_icon_layout);
            mLayoutPref.setKey(KEY_PRE_LAYOUT_STYLE);
            mLayoutPref.setOnPreferenceChangeListener(this);
            mLayoutPref.setOnPreferenceClickListener(this);
        }
        if (!AgedModeUtil.isAgedMode() && !mIconManager.supprtCardIcon()) {
            mLayoutPref.setEnabled(true);
            // add preference to category
            mLayoutCategory.addPreference(mLayoutPref);
            // update summary
            int countX = LauncherModel.getCellCountX();
            int countY = LauncherModel.getCellCountY();
            String layoutvalue = getLayoutValue(countX, countY);
            mSharedRef.edit().putString(KEY_PRE_LAYOUT_STYLE, layoutvalue).commit();
            mOldLayoutMode = getCurrentLayoutIndex();
            updateLayoutSummary();
        } else {
            // hide preference when in aged mode or big card theme
            if (mLayoutCategory.findPreference(KEY_PRE_LAYOUT_STYLE) != null) {
                mLayoutCategory.removePreference(mLayoutPref);
            }
            /*
                this preference item was only disabled (but not removed)
                when currently in aged mode or big card theme, and a summary was shown:
                    mLayoutPref.setEnabled(false);
                    mLayoutPref.setSummary(R.string.forbid_layout_change_when_3_3) or
                    mLayoutPref.setSummary(R.string.forbid_layout_change_when_big)
             */
        }
    }

    private void addNewMarkPreference(){

        mNewMarkIconCategory = new PreferenceCategory(this);
        mNewMarkIconCategory.setTitle(R.string.settings_icon_mark_title);
        mNewMarkIconCategory.setKey("show_new_mark_pre");
        mContainer.addPreference(mNewMarkIconCategory);

        mShowNewMarkIconPref = new HWSwitchPreference(this);
//        mShowNewMarkIconPref.setSummaryOff(R.string.setttings_mark_not_enabled);
//        mShowNewMarkIconPref.setSummaryOn(R.string.setttings_mark_enabled);
        mShowNewMarkIconPref.setTitle(R.string.settings_icon_mark);
        mShowNewMarkIconPref.setKey(KEY_PRE_SHOW_NEW_MARK);
        mNewMarkIconCategory.addPreference(mShowNewMarkIconPref);
        mIsShowNewMarkIconOldMode = LauncherModel.isShowNewMarkIcon();
        mShowNewMarkIconPref.setChecked(mIsShowNewMarkIconOldMode);
        mShowNewMarkIconPref.setOnPreferenceChangeListener(this);
        UserTrackerHelper.bindPageName(this, UserTrackerMessage.MSG_LAUNCHER_SETTING);
    }

    private void addNotificationMarkNew(){
        if( Utils.isYunOS2_9System() ) return;
        int notificationType = LauncherModel.getNotificationMarkType();
        m3rdAppNotificationPrefNew = new Preference(this);
        m3rdAppNotificationPrefNew.setTitle(R.string.settings_notification_mark_title);
        m3rdAppNotificationPrefNew.setKey(KEY_NOTIFICATION_MARK_PREF_NEW);
        m3rdAppNotificationPrefNew.setSummary(mNotificationTitle[notificationType]);
        mNewMarkIconCategory.addPreference(m3rdAppNotificationPrefNew);
        m3rdAppNotificationPrefNew.setOnPreferenceClickListener(this);
    }

    private void loadResources() {
        mEffectTitle = getResources().getStringArray(R.array.entries_effect_preference);
        mEffectValue = getResources().getStringArray(R.array.entryvalues_effect_preference);
        mLiveWeatherTitle = getResources().getStringArray(R.array.entries_liveweather_preference);
        mLiveWeatherValue = getResources().getStringArray(R.array.entryvalues_liveweather_preference);
        mLayoutTitle = getResources().getStringArray(R.array.entries_layout_preference);
        mLayoutValue = getResources().getStringArray(R.array.entryvalues_layout_preference);
        mNotificationTitle = getResources().getStringArray(R.array.notification_type_title);
    }

    //added by qinjinchuan topwise for default continuous configuration
    private void setDefaultContinuousHome(Context context) {
        boolean defaultContinuousHomeshell=false;
        SharedPreferences preference = PreferenceManager.getDefaultSharedPreferences(context);
        if(preference !=null){ 
            defaultContinuousHomeshell=preference.getBoolean(KEY_CONTINUOUS_HOMESHELL_STYLE,TopwiseConfig.DEFAULT_CONTINUOUS_HOMESHELL);
        }
        if(mContinuousHomePref !=null && defaultContinuousHomeshell){
            Map<String, String> pParams = new HashMap<String, String>();
            pParams.put("status", defaultContinuousHomeshell ? "on" : "off");
            UserTrackerHelper.sendUserReport(HomeShellSetting.this, UserTrackerMessage.MSG_LAUNCHER_SETTING_LOOPING, pParams);
            mContinuousHomePref.setChecked(defaultContinuousHomeshell);
            Intent mHomeLoopIntent = new Intent(CONTINUOUS_HOMESHELL_SHOW_ACTION);
            mHomeLoopIntent.putExtra(CONTINUOUS_HOMESHELL_SHOW_KEY, defaultContinuousHomeshell);
            sendBroadcast(mHomeLoopIntent);
        }
    }
    //added by qinjinchuan topwise for default continuous configuration
    private void addContinuousHomePreference() {
        mContinuousHomePref = new HWSwitchPreference(this);
        mContinuousHomePref.setTitle(R.string.settings_continuous_homeshell);
        mContinuousHomePref.setKey(KEY_CONTINUOUS_HOMESHELL_STYLE);
        setDefaultContinuousHome(this);//added by qinjinchuan topwise for default continuous configuration
        mContainer.addPreference(mContinuousHomePref);
        mContinuousHomePref.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                boolean isOn = (Boolean) newValue;
                Map<String, String> pParams = new HashMap<String, String>();
                pParams.put("status", isOn ? "on" : "off");
                UserTrackerHelper.sendUserReport(HomeShellSetting.this, UserTrackerMessage.MSG_LAUNCHER_SETTING_LOOPING, pParams);
                mContinuousHomePref.setChecked(isOn);
                Intent mHomeLoopIntent = new Intent(CONTINUOUS_HOMESHELL_SHOW_ACTION);
                mHomeLoopIntent.putExtra(CONTINUOUS_HOMESHELL_SHOW_KEY, isOn);
                sendBroadcast(mHomeLoopIntent);
                return true;
            }
        });
    }

    private void addArrangePagePreference() {
        mArrangePagePref = new Preference(this);
        mArrangePagePref.setTitle(R.string.settings_arrange_page);
        mArrangePagePref.setSummary(R.string.setttings_arrange_page_subtitle);
        mArrangePagePref.setKey(KEY_PRE_ARRANGE_PAGE);
        mContainer.addPreference(mArrangePagePref);
        mArrangePagePref.setOnPreferenceClickListener(this);
    }

    @Override
    protected void onResume() {
        UserTrackerHelper.pageEnter(UserTrackerMessage.MSG_LAUNCHER_SETTING);
        mIsShowNewMarkIconOldMode = LauncherModel.isShowNewMarkIcon();
        mShowNewMarkIconPref.setChecked(mIsShowNewMarkIconOldMode);
        updateEffectSummary(false);
        if(Launcher.isSupportLiveWeather()) {
        	updateLiveWeatherSummary();
        }
        if (mFreezePref != null) {
            mFreezePref.setChecked(getFreezeValue(this));
        }
        super.onResume();
    }

    @Override
    protected void onPause() {
        UserTrackerHelper.pageLeave(UserTrackerMessage.MSG_LAUNCHER_SETTING);
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        notifyChange();

        if( mLayoutActionSheet != null ) mLayoutActionSheet.dismiss(false);
        if( mNotificationActionSheet != null ) mNotificationActionSheet.dismiss(false);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHandler.removeMessages(EVENT_ARRANGE_LAYOUT);
        mHandler.removeMessages(EVENT_LAYOUT_STYLE);
        //ThemeChangedListener.getInstance(this).removeListener(this);
        unregisterReceiver(mSettingReceiver);
        LauncherApplication.homeshellSetting = null;
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object objValue) {
        final String key = preference.getKey();
        if (KEY_PRE_SHOW_NEW_MARK.equals(key)) {
            boolean value = (Boolean) objValue;
            updateShowMarkIconSummary(value);
            Map<String, String> pParams = new HashMap<String, String>();
            pParams.put("status", value ? "on" : "off");
            UserTrackerHelper.sendUserReport(HomeShellSetting.this,
                    UserTrackerMessage.MSG_LAUNCHER_SETTING_MARK_NEW, pParams);
        } else if (KEY_PRE_LAYOUT_STYLE.equals(key)) {
            String currentString = (String) objValue;
            for (int i = 0; i < mLayoutValue.length; i++) {
                if (mLayoutValue[i].equals(currentString)) {
                    mLayoutPref.setSummary(mLayoutTitle[i]);
                }
            }
        }
        return true;
    }

    private void notifyChange() {
        boolean isShowIconNewMode = mShowNewMarkIconPref.isChecked();
        mIsShowIconMarkChange = mIsShowNewMarkIconOldMode ^ isShowIconNewMode;
        boolean isNotificationPrefChange = mNotificationPackageNames != null;
        if (mIsShowIconMarkChange || isNotificationPrefChange) {
            Intent intent = new Intent();
            intent.setAction(LauncherModel.ACTION_UPDATE_LAYOUT);
            if (isNotificationPrefChange) {
                intent.putExtra(EXTRA_NOTIFICATION_PACKAGE_NAMES,
                        mNotificationPackageNames.toArray(new String[0]));
                mNotificationPackageNames = null;
            }
            sendBroadcast(intent);
        }
    }

    private void updateEffectSummary(boolean isResult) {
    	//modify by huangxunwan for config homeshell effect style
        String nowValue = mSharedRef.getString(KEY_PRE_EFFECT_STYLE, TopwiseConfig.HOMESHELL_EFFECT_STYLE);
        for (int i = 0; i < mEffectValue.length; i++) {
            if (nowValue.equals(mEffectValue[i])) {
                if (isResult) {
                    UserTrackerHelper.sendLauncherEffectsResult(i + 1);
                }
                mEffectPref.setSummary(mEffectTitle[i]);
                return;
            }
        }
    }

    private void updateLiveWeatherSummary() {
        String nowValue = mSharedRef.getString(KEY_PRE_LIVEWEATHER_STYLE, "0");
        for (int i = 0; i < mLiveWeatherValue.length; i++) {
            if (nowValue.equals(mLiveWeatherValue[i])) {
                mLiveWeatherPref.setSummary(mLiveWeatherTitle[i]);
                return;
            }
        }
    }
    
    private void updateLayoutSummary() {
        String currentValue = mSharedRef.getString(KEY_PRE_LAYOUT_STYLE, "0");
        for (int i = 0; i < mLayoutValue.length; i++) {
            if (currentValue.equals(mLayoutValue[i])) {
                mLayoutPref.setSummary(mLayoutTitle[i]);
            }
        }
    }

    private void updateShowMarkIconSummary(boolean isShow) {
        String spKey = LauncherApplication.getSharedPreferencesKey();
        SharedPreferences sp = LauncherApplication.getContext().getSharedPreferences(spKey,
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean(DB_SHOW_NEW_MARK_ICON, isShow);
        editor.commit();
        sendBroadcast(new Intent(ACTION_ON_SHOW_NEW_MARK_CHANGE));
    }

    public static int getSlideEffectMode(Context ctx) {
        SharedPreferences preference = PreferenceManager.getDefaultSharedPreferences(ctx);
        if (preference == null) {
            return 0;
        }
        //modify by huangxunwan for config homeshell effect style
        String value = preference.getString(KEY_PRE_EFFECT_STYLE, TopwiseConfig.HOMESHELL_EFFECT_STYLE);
        int mode = Integer.parseInt(value);
        return mode;
    }       

    public static int getLiveWeatherMode(Context ctx) {
        SharedPreferences preference = PreferenceManager.getDefaultSharedPreferences(ctx);
        if (preference == null) {
            return 0;
        }
        String value = preference.getString(KEY_PRE_LIVEWEATHER_STYLE, "0");
        int mode = Integer.parseInt(value);
        return mode;
    }
    
    public static boolean getLoopDesktopMode(Context ctx){
        SharedPreferences preference = PreferenceManager.getDefaultSharedPreferences(ctx) ;
        if(preference == null){
            return false;
        }
        return preference.getBoolean("loop_desktop_preference",false);
    }       

    private void notifyLayoutChanged() {
        int mode = getCurrentLayoutIndex();
        if (mode == mOldLayoutMode) {
            return;
        }
        mOldLayoutMode = mode;
        int countX = 0;
        int countY = 0;
        switch (mode) {
        case 0:
            countX = 4;
            countY = 4;
            break;
        case 1:
            countX = 4;
            countY = 5;
            break;
        case 2:
            countX = 3;
            countY = 3;
            break;
        default:
            countX = 4;
            countY = 4;
        }

        if (LauncherModel.checkGridSize(countX, countY)) {
            Utils.showLoadingDialog(HomeShellSetting.this);
            Message msg = new Message();
            msg.what = EVENT_LAYOUT_STYLE;
            msg.arg1 = countX;
            msg.arg2 = countY;
            mHandler.removeMessages(EVENT_LAYOUT_STYLE);
            mHandler.sendMessageDelayed(msg, 200);
        }
    }

    private String getLayoutValue( int countX, int countY){
        String layoutTitle = ""+countX+"x"+countY;
        for( int i = 0; i < mLayoutTitle.length; i++ ){
            if( layoutTitle.equals(mLayoutTitle[i])){
                return mLayoutValue[i];
            }
        }
        return "0";
    }

    private int getCurrentLayoutIndex() {
        String currentString = mSharedRef.getString(KEY_PRE_LAYOUT_STYLE, "0");
        for (int i = 0; i < mLayoutValue.length; i++) {
            if (mLayoutValue[i].equals(currentString)) {
                return i;
            }
        }
        return 0;
    }

    private void startLayoutDialog() {
        if (mLayoutActionSheet != null && mLayoutActionSheet.isShowing()) {
            return;
        }
        mLayoutActionSheet = new ActionSheet(this);
        mLayoutActionSheet.setTitle(getResources().getString(R.string.settings_layout_title));
        mLayoutActionSheet.setSingleChoiceItems(mLayoutTitle, getCurrentLayoutIndex(), new SingleChoiceListener() {
            
            @Override
            public void onDismiss(ActionSheet actionSheet) {
            }
            
            @Override
            public void onClick(int position) {
                if (mIconManager.supprtCardIcon() && position == 1) {
                    Toast.makeText(getApplicationContext(),
                    R.string.layout_change_toast,
                    Toast.LENGTH_SHORT).show();
                } else {
                    mSharedRef.edit().putString(KEY_PRE_LAYOUT_STYLE, mLayoutValue[position]).commit();
                    updateLayoutSummary();
                    UserTrackerHelper.sendLauncherLayoutResult(Integer.parseInt(mLayoutValue[position]) + 1);
                    UserTrackerHelper
                        .pageLeave(UserTrackerMessage.MSG_LAUNCHER_SETTING_LAUNCHER_SETTING_LAYOUT);
                    notifyLayoutChanged();
                }
            }
        });
        mLayoutActionSheet.setDispalyStyle(ActionSheet.DisplayStyle.PHONE);
        mLayoutActionSheet.show(getWindow().getDecorView());
    }

    /**
     * start Notification show type action sheet
     */
    private void startNotificationDialog(){
        mNotificationActionSheet = new ActionSheet(this);
        mNotificationActionSheet.setTitle(getResources().getString(R.string.settings_notification_mark_title));
        int checkedItem = LauncherModel.getNotificationMarkType();
        mNotificationActionSheet.setSingleChoiceItems(mNotificationTitle, checkedItem, new SingleChoiceListener() {
            public void onDismiss(ActionSheet arg0) {}
            @Override
            public void onClick(int index) {
                Map<String, String> param = new HashMap<String, String>();
                param.put(UserTrackerMessage.Key.RESULT, MARK_NOTIFICATION_ITEMS[index]);
                UserTrackerHelper.sendUserReport(UserTrackerMessage.MSG_MARK_NOTIFICATION_SELECT, param);
                String spKey = LauncherApplication.getSharedPreferencesKey();
                SharedPreferences sp = LauncherApplication.getContext().getSharedPreferences(spKey,
                        Context.MODE_PRIVATE);
                sp.edit().putInt(KEY_NOTIFICATION_MARK_PREF_NEW, index).commit();
                m3rdAppNotificationPrefNew.setSummary(mNotificationTitle[index]);
                CardNotificationListenerService cnls = CardNotificationListenerService.getInstance();
                if( cnls != null ) {
                    mNotificationPackageNames = cnls.flushAndReinit();
                }
                sendBroadcast(new Intent(ACTION_ON_MARK_TYPE_CHANGE));
                Log.d(TAG,"startNotificationDialog onclick");
            }
        });
        mNotificationActionSheet.setDispalyStyle(ActionSheet.DisplayStyle.PHONE);
        mNotificationActionSheet.show(getWindow().getDecorView());
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        final String key = preference.getKey();
        if (KEY_PRE_EFFECT_STYLE.equals(key)) {
            UserTrackerHelper.sendUserReport(HomeShellSetting.this,
                    UserTrackerMessage.MSG_LAUNCHER_SETTING_EFFECTS);
            UserTrackerHelper
                    .pageEnter(UserTrackerMessage.MSG_LAUNCHER_SETTING_LAUNCHER_SETTING_EFFECTS);
            try{
                Intent intent = new Intent(HomeShellSetting.this, EffectChooseActivity.class);
                startActivity(intent);
            }catch(Exception e){
                Log.d(TAG,"startActivityForResult fail may be caused by system",e);
            }
        }else if (KEY_PRE_LIVEWEATHER_STYLE.equals(key)) {
            UserTrackerHelper.sendUserReport(HomeShellSetting.this,
                    UserTrackerMessage.MSG_LAUNCHER_SETTING_LIVEWEATHER);
            UserTrackerHelper
                    .pageEnter(UserTrackerMessage.MSG_LAUNCHER_SETTING_LAUNCHER_SETTING_LIVEWEATHER);
            try{
                Intent intent = new Intent(HomeShellSetting.this, LiveWeatherChooseActivity.class);
                startActivity(intent);
            }catch(Exception e){
                Log.d(TAG,"startActivityForResult fail may be caused by system",e);
            }
        } else if (KEY_PRE_LAYOUT_STYLE.equals(key)) {
            UserTrackerHelper.sendUserReport(HomeShellSetting.this,
                    UserTrackerMessage.MSG_LAUNCHER_SETTING_LAYOUT);
            UserTrackerHelper
                    .pageEnter(UserTrackerMessage.MSG_LAUNCHER_SETTING_LAUNCHER_SETTING_LAYOUT);
            startLayoutDialog();
        } else if (KEY_PRE_ARRANGE_PAGE.equals(key)) {
            UserTrackerHelper.sendUserReport(HomeShellSetting.this,
                    UserTrackerMessage.MSG_LAUNCHER_SETTING_ARRANGE);
            Utils.showLoadingDialog(HomeShellSetting.this);
            mHandler.removeMessages(EVENT_ARRANGE_LAYOUT);
            mHandler.sendEmptyMessageDelayed(EVENT_ARRANGE_LAYOUT, 500);
        } else if (KEY_NOTIFICATION_MARK_PREF_NEW.equals(key) ){
            UserTrackerHelper.sendUserReport(UserTrackerMessage.MSG_MARK_NOTIFICATION);
            startNotificationDialog();
        }

        return false;
    }

    private boolean isLifeCenterExists() {
        String pkgName = Launcher.LIFE_CENTER_PACKAGE_NAME;
        PackageManager pm = getPackageManager();
        try {
            pm.getPackageInfo(pkgName, PackageManager.GET_ACTIVITIES);
            return true;
        } catch (Exception e) {
            Log.d(TAG,"package:"+pkgName+" doesn't exist");
        }
        return false;
    }
    public interface Callback
    {
        public void onFinish();
    }

    /**
     * update Layout Preference when theme changed
     */
    private void updateLayoutPreferenceContent(){
        boolean isSupportCard = mIconManager.supprtCardIcon();
        boolean isEnabled = mLayoutPref.isEnabled();
        Log.e(TAG, "isSupportCard=" + isSupportCard + ",isEnabled=" + isEnabled);
        if (AgedModeUtil.isAgedMode()) {
            mLayoutPref.setEnabled(false);
            mLayoutPref.setSummary(R.string.forbid_layout_change_when_3_3);
        } else {
            if (isSupportCard) {
                mLayoutPref.setEnabled(false);
                int countY = LauncherModel.getCellCountY();
                String layoutvalue = getLayoutValue(4, countY);
                mSharedRef.edit().putString(KEY_PRE_LAYOUT_STYLE, layoutvalue).commit();
                mOldLayoutMode = getCurrentLayoutIndex();
                mLayoutPref.setSummary(R.string.forbid_layout_change_when_big);
            } else {
                mLayoutPref.setEnabled(true);
                mLayoutPref.setOnPreferenceChangeListener(this);
                mLayoutPref.setOnPreferenceClickListener(this);
                int countY = LauncherModel.getCellCountY();
                String layoutvalue = getLayoutValue(4, countY);
                mSharedRef.edit().putString(KEY_PRE_LAYOUT_STYLE, layoutvalue).commit();
                mOldLayoutMode = getCurrentLayoutIndex();
                updateLayoutSummary();
            }
        }
    }

    public void updateLayoutPreference() {
        updateLayoutPreferenceContent();
    }
}
