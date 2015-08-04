package com.tpw.homeshell.setting;

import storeaui.app.HWPreferenceActivity;
import storeaui.widget.FooterBar.FooterBarButton;
import storeaui.widget.FooterBar.FooterBarType.OnFooterItemClick;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceScreen;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.tpw.homeshell.R;
import com.tpw.homeshell.UserTrackerHelper;
import com.tpw.homeshell.UserTrackerMessage;
import com.tpw.homeshell.views.AliRadioButtonPreference;

public class LiveWeatherChooseActivity extends HWPreferenceActivity implements
        OnPreferenceClickListener, AliRadioButtonPreference.OnClickListener {

    private PreferenceScreen mContainer;
    private String[] mLiveWeatherTitle;
    private String[] mLiveWeatherValue;

    private SharedPreferences mSharedPref;

    private Resources res;
    private String mCurrentChoosedLiveWeatherValue;
    boolean mIsAddPreferencesOK = false;
    private static final String TAG = "LiveWeatherChooseActivity";
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initActionBar();
        setTitle2(getResources().getString(
                R.string.settings_screen_live_weather));
        showBackKey(true);

        mSharedPref = getPreferenceManager().getSharedPreferences();
        mContainer = getPreferenceManager().createPreferenceScreen(this);
        setPreferenceScreen(mContainer);
        prepareResource();
        addPreferences();
    }

    private void prepareResource() {
        res = getResources();
        mLiveWeatherTitle = res.getStringArray(R.array.entries_liveweather_preference);
        mLiveWeatherValue = res.getStringArray(R.array.entryvalues_liveweather_preference);
    }

    private void addPreferences() {
        mIsAddPreferencesOK = false;
        mCurrentChoosedLiveWeatherValue = mSharedPref.getString(
                HomeShellSetting.KEY_PRE_LIVEWEATHER_STYLE, "0");
        boolean isChecked = false;
        int length = mLiveWeatherValue.length;
        Log.d(TAG, "---------mCurrentChoosedLiveWeatherValue----------- " + mCurrentChoosedLiveWeatherValue);
        for (int i = 0; i < length; i++) {
            if (mCurrentChoosedLiveWeatherValue.equals(mLiveWeatherValue[i])) {
                isChecked = true;
            } else {
                isChecked = false;
            }
            addRadioButton(mLiveWeatherTitle[i], null, mLiveWeatherValue[i], isChecked);
        }
        mIsAddPreferencesOK = true;
        updateRadioButtons(mCurrentChoosedLiveWeatherValue);
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        return true;
    }

    @Override
    protected void onStop() {
        UserTrackerHelper.pageLeave(UserTrackerMessage.MSG_LAUNCHER_SETTING_LAUNCHER_SETTING_LIVEWEATHER);
        super.onStop();
    }

    private void addRadioButton(String title, Drawable icon, String key, boolean isChecked) {
        Log.d(TAG, "---------addRadioButton----------- title isChecked " + title+" - "+isChecked);
        AliRadioButtonPreference preference = new AliRadioButtonPreference(this);
        preference.setTitle(title);
        preference.setKey(key);
        preference.setIcon(icon);
        preference.setChecked(isChecked);
        preference.setOnClickListener(this);
        mContainer.addPreference(preference);
    }

    private void updateRadioButtons(String activeKey) {
        Log.d(TAG, "---------updateRadioButtons----------- activeKey " + activeKey);
        for (String cs : mLiveWeatherValue) {
            if (activeKey.equals(cs)) {
                ((AliRadioButtonPreference) findPreference(cs)).setChecked(true);
            } else {
                ((AliRadioButtonPreference) findPreference(cs)).setChecked(false);
            }
        }
    }

    @Override
    public void onRadioButtonClicked(AliRadioButtonPreference preference) {
        if (!mIsAddPreferencesOK) {
            return;
        }
        mCurrentChoosedLiveWeatherValue = preference.getKey();
        updateRadioButtons(mCurrentChoosedLiveWeatherValue);
        mSharedPref.edit().putString(HomeShellSetting.KEY_PRE_LIVEWEATHER_STYLE, mCurrentChoosedLiveWeatherValue).commit();
    }

}
