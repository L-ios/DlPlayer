package com.tpw.homeshell.theme;

import java.util.ArrayList;
import java.util.List;

import com.tpw.homeshell.utils.HLog;
import com.tpw.homeshell.utils.Utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

public class ThemeChangedListener extends BroadcastReceiver {
    private final static String ACTION_THEME_CHANGED = "com.tpw.homeshell.aciton.THEME_CHENGED";

    private List<ThemeChanged> mListener;

    private static ThemeChangedListener mInstance;
    public static ThemeChangedListener getInstance() {
        if (mInstance == null) {
            mInstance = new ThemeChangedListener();
        }

        return mInstance;
    }

    public void release() {
        if (Utils.DEBUG) {
            int size = mListener.size();
            if (size > 0) {
                HLog.d("ThemeChangedListener", "memory leak , size : " + size);
            }
        }

        mListener.clear();
    }

    public void register(Context context) {
       IntentFilter filter = new IntentFilter();
       filter.addAction(ACTION_THEME_CHANGED);

       context.registerReceiver(this, filter);
    }

    public void unregister(Context context) {
        context.unregisterReceiver(this);
    }

    public interface ThemeChanged {
        void onThemeChanged();
    }

    public void addListener(ThemeChanged listener) {
        mListener.add(listener);
    }

    public void removeListener(ThemeChanged listener) {
        mListener.remove(listener);
    }

    private ThemeChangedListener() {
        mListener = new ArrayList<ThemeChanged>();
    }

    private void invoke() {
        for (int i = 0; i < mListener.size(); i++) {
            mListener.get(i).onThemeChanged();
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if (ACTION_THEME_CHANGED.equals(action)) {
            invoke();
        }
    }
}