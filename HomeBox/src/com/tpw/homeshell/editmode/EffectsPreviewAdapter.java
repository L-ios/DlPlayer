
package com.tpw.homeshell.editmode;

import com.tpw.homeshell.setting.HomeShellSetting;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.TextView;

import com.tpw.homeshell.R;
import com.tpw.homeshell.TopwiseConfig;

public class EffectsPreviewAdapter extends BaseAdapter {
    private TypedArray mImgTypeArray;
    private String[] mEffectTitle;
    private String[] mEffectValue;
    private String mCurrentChoosedEffectValue;
    private SharedPreferences mSharedPref;
    private OnSharedPreferenceChangeListener mSharedPrefListener;
    private LayoutInflater mInflater;
    private Context mContext;

    public EffectsPreviewAdapter(Context context) {
        Resources res = context.getResources();
        mImgTypeArray = res.obtainTypedArray(R.array.editmode_effect_choose_img);
        mEffectTitle = res.getStringArray(R.array.entries_effect_preference);
        mEffectValue = res.getStringArray(R.array.entryvalues_effect_preference);
        mSharedPref = context.getSharedPreferences("com.tpw.homeshell_preferences",
                Context.MODE_PRIVATE);
        //modify by huangxunwan for config homeshell effect style
        mCurrentChoosedEffectValue = mSharedPref.getString(HomeShellSetting.KEY_PRE_EFFECT_STYLE,
                TopwiseConfig.HOMESHELL_EFFECT_STYLE);
        mSharedPrefListener = new OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                if (HomeShellSetting.KEY_PRE_EFFECT_STYLE.equals(key)) {
                	//modify by huangxunwan for config homeshell effect style
                    mCurrentChoosedEffectValue = sharedPreferences.getString(
                            HomeShellSetting.KEY_PRE_EFFECT_STYLE,
                            TopwiseConfig.HOMESHELL_EFFECT_STYLE);
                    notifyDataSetChanged();
                }
            }
        };
        mSharedPref.registerOnSharedPreferenceChangeListener(mSharedPrefListener);
        mInflater = LayoutInflater.from(context);
        mContext = context;
    }

    @Override
    public int getCount() {
        return mEffectValue.length;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            ViewHolder vh = new ViewHolder();
            convertView = mInflater.inflate(R.layout.preview_item, parent, false);

            vh.previewImgView = (ImageView) convertView.findViewById(R.id.preview_image);
            Drawable previewImg = mImgTypeArray.getDrawable(position);
            vh.previewImgView.setImageDrawable(previewImg);
            vh.previewImgView.setScaleType(ScaleType.FIT_CENTER);
            vh.titleTextView = (TextView) convertView.findViewById(R.id.preview_title);
            vh.titleTextView.setText(mEffectTitle[position]);
            vh.previewChecked = (ImageView) convertView.findViewById(R.id.preview_checked);
            if (isChecked(position)) {
                vh.previewChecked.setVisibility(View.VISIBLE);
            } else {
                vh.previewChecked.setVisibility(View.GONE);
            }
            vh.position = position;
            convertView.setTag(vh);

        } else {
            ViewHolder vh = (ViewHolder) convertView.getTag();
            Drawable previewImg = mImgTypeArray.getDrawable(position);
            vh.previewImgView.setImageDrawable(previewImg);
            vh.titleTextView.setText(mEffectTitle[position]);
            vh.position = position;
            if (isChecked(position)) {
                vh.previewChecked.setVisibility(View.VISIBLE);
            } else {
                vh.previewChecked.setVisibility(View.GONE);
            }

        }
        return convertView;
    }

    private boolean isChecked(int position) {
        if (mEffectValue[position].equals(mCurrentChoosedEffectValue)) {
            return true;
        }
        return false;
    }

    public void setEffectValue(int position) {
        mCurrentChoosedEffectValue = mEffectValue[position];
        mSharedPref.edit()
                .putString(HomeShellSetting.KEY_PRE_EFFECT_STYLE, mCurrentChoosedEffectValue)
                .commit();
    }

    class ViewHolder {
        public int position;
        public ImageView previewImgView;
        public TextView titleTextView;
        public ImageView previewChecked;
    }

}
