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
import com.tpw.homeshell.TopwiseConfig;
import com.tpw.homeshell.UserTrackerHelper;
import com.tpw.homeshell.UserTrackerMessage;
import com.tpw.homeshell.views.AliRadioButtonPreference;

public class EffectChooseActivity extends HWPreferenceActivity implements
        OnPreferenceClickListener, AliRadioButtonPreference.OnClickListener {

    private PreferenceScreen mContainer;
    private TypedArray mImgTypeArray;
    private String[] mEffectTitle;
    private String[] mEffectValue;

    private SharedPreferences mSharedPref;

    private Resources res;
    private String mCurrentChoosedEffectValue;
    boolean mIsAddPreferencesOK = false;
    private static final String TAG = "EffectChooseActivity";
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initActionBar();
        setTitle2(getResources().getString(
                R.string.settings_screen_slide_effect));
        showBackKey(true);

        mSharedPref = getPreferenceManager().getSharedPreferences();
        mContainer = getPreferenceManager().createPreferenceScreen(this);
        setPreferenceScreen(mContainer);
        prepareResource();
        addPreferences();
        // addFooter();
    }

    private void prepareResource() {
        res = getResources();
        mImgTypeArray = res.obtainTypedArray(R.array.effect_choose_img);
        mEffectTitle = res.getStringArray(R.array.entries_effect_preference);
        mEffectValue = res
                .getStringArray(R.array.entryvalues_effect_preference);
    }

    private void addPreferences() {
        mIsAddPreferencesOK = false;
        //modify by huangxunwan for config homeshell effect style
        mCurrentChoosedEffectValue = mSharedPref.getString(
                HomeShellSetting.KEY_PRE_EFFECT_STYLE, TopwiseConfig.HOMESHELL_EFFECT_STYLE);
        boolean isChecked = false;
        int length = mImgTypeArray.length();
        Log.d(TAG, "---------mCurrentChoosedEffectValue----------- " + mCurrentChoosedEffectValue);
        for (int i = 0; i < length; i++) {
            if (mCurrentChoosedEffectValue.equals(mEffectValue[i])) {
                isChecked = true;
            } else {
                isChecked = false;
            }
            addRadioButton(mEffectTitle[i], mImgTypeArray.getDrawable(i),
                    mEffectValue[i], isChecked);
        }
        mIsAddPreferencesOK = true;
        updateRadioButtons(mCurrentChoosedEffectValue);
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        return true;
    }

    @Override
    protected void onStop() {
        UserTrackerHelper
                .pageLeave(UserTrackerMessage.MSG_LAUNCHER_SETTING_LAUNCHER_SETTING_EFFECTS);
        super.onStop();
    }

    private void addFooter() {
        ViewGroup root = (ViewGroup) getListView().getParent();
        FooterBarButton mFooterBarButton = new FooterBarButton(this);
        mFooterBarButton.addItem(0,
                res.getString(R.string.settings_effect_cancel_btn));
        mFooterBarButton.setOnFooterItemClick(new OnFooterItemClick() {
            @Override
            public void onFooterItemClick(View arg0, int arg1) {
                finish();
            }
        });
        mFooterBarButton.getLayoutParams().height = res
                .getDimensionPixelSize(R.dimen.setting_effect_cancel_btn_height);
        mFooterBarButton.updateItems();
        root.addView(mFooterBarButton);
    }

    private void addRadioButton(String title, Drawable icon, String key,
            boolean isChecked) {
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
        for (String cs : mEffectValue) {
            if (activeKey.equals(cs)) {
                ((AliRadioButtonPreference) findPreference(cs))
                        .setChecked(true);
            } else {
                ((AliRadioButtonPreference) findPreference(cs))
                        .setChecked(false);
            }
        }
    }

    @Override
    public void onRadioButtonClicked(AliRadioButtonPreference preference) {
        if (!mIsAddPreferencesOK) {
            return;
        }
        mCurrentChoosedEffectValue = preference.getKey();
        updateRadioButtons(mCurrentChoosedEffectValue);
        mSharedPref
                .edit()
                .putString(HomeShellSetting.KEY_PRE_EFFECT_STYLE,
                        mCurrentChoosedEffectValue).commit();
    }

}
