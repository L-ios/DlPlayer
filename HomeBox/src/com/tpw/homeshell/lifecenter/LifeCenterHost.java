package com.tpw.homeshell.lifecenter;

import android.content.Intent;
import android.os.Build;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.tpw.homeshell.Launcher;
import com.tpw.homeshell.LauncherApplication;
import com.tpw.homeshell.UserTrackerHelper;
import com.tpw.homeshell.UserTrackerMessage;

public class LifeCenterHost {
    private static final String TAG = "LifeCenterHost";

    private LifeCenterHostView mLifeCenterHostView;
    private Launcher mLauncher;

    private String mUrl = null;
    private Intent mUrlIntent = null;

    public LifeCenterHost(LifeCenterHostView hostView) {
        mLifeCenterHostView = hostView;
        mLauncher = LauncherApplication.mLauncher;
        init();
    }

    private void init() {
        mLifeCenterHostView.setOverScrollMode(View.OVER_SCROLL_NEVER);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window window = mLauncher.getWindow();
            // Translucent status bar
            final int flag = WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
            window.setFlags(flag, flag);
        }

         mLifeCenterHostView.setOnPageChangeListener(new OnPageChangeListener() {
            private float mLastPositionOffset = 0;

            @Override
            public void onPageSelected(int position) {
            }

            @Override
            public void onPageScrolled(int position, float positionOffset,
                    int positionOffsetPixels) {
                // Log.d(TAG, "onPageScrolled : " + position +
                //            " offset : " + positionOffset +
                //            " offsetPixels : " + positionOffsetPixels);

                if (!mLifeCenterHostView.isLayoutRtl()) {
                    if (position == mLifeCenterHostView.LIFECENTER_PAGE
                            && positionOffset > 0.8
                            && mLastPositionOffset != 0
                            && (positionOffset - mLastPositionOffset) > 0) {
                        Log.d(TAG, "onPageScrolled setWorkSpace true in lifecenter page.");
                        mLifeCenterHostView.setInWorkSpace(true);
                    } else {
                        // BUBUG 5209905 : press menu and do not show in home page.
                        // cause: 1 .onPageScrollStateChanged(1) -->setInWorkSpace(false) 2. onPageScrolled(1, 0, 0);
                        // solution : onPageScrolled(1, 0, 0) -->setInWorkSpace(true);
                        if (position == mLifeCenterHostView.HOME_PAGE
                                && Math.abs(positionOffset - 0.f) < 0.000001f
                                && Math.abs(positionOffsetPixels - 0.f) < 0.000001f) {
                            // Log.d(TAG, "onPageScrolled setWorkSpace true in home page.");
                            mLifeCenterHostView.setInWorkSpace(true);
                        }
                    }

                    if (position == mLifeCenterHostView.HOME_PAGE
                        && Math.abs(positionOffsetPixels - 0.f) < 0.000001f) {
                        mLifeCenterHostView.setIsMoveInHomePage(true);
                    } else {
                        mLifeCenterHostView.setIsMoveInHomePage(false);
                    }
                } else {
                    if (position == 0 && positionOffset < 0.2
                            && (positionOffset - mLastPositionOffset) < 0) {
                        mLifeCenterHostView.setInWorkSpace(true);
                    }

                    boolean inHomePage = false;
                    if (position == 1 && positionOffset != 0) {
                        inHomePage = true;
                    }

                    mLifeCenterHostView.setIsMoveInHomePage(inHomePage);
                }
                mLastPositionOffset = positionOffset;
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                // Log.d(TAG, "onPageScrollStateChanged state : " + state);

                if (ViewPager.SCROLL_STATE_DRAGGING == state) {
                    mLifeCenterHostView.setScrolling(true);
                } else {
                    mLifeCenterHostView.setScrolling(false);
                }

                int currentPage = mLifeCenterHostView.getCurrentPage();
                // Log.d(TAG,"onPageScrollStateChanged state =" + state);

                if (ViewPager.SCROLL_STATE_DRAGGING == state) {
                    mLifeCenterHostView.setLastPage(currentPage);
                }

                mLifeCenterHostView.setFullLifeCenterPageShowed(false);
                mLifeCenterHostView.setInWorkSpace(false);

                if (state == ViewPager.SCROLL_STATE_IDLE) {
                    if (currentPage == mLifeCenterHostView.HOME_PAGE) {
                        mLifeCenterHostView.setInWorkSpace(true);
                        // Log.d(TAG,"onPageScrolled ");
                        if (mLifeCenterHostView.LIFECENTER_PAGE == mLifeCenterHostView.getLastPage()) {
                            mLifeCenterHostView.exitApp();
                        }
                    } else if (currentPage == mLifeCenterHostView.LIFECENTER_PAGE) {
                        UserTrackerHelper.sendUserReport(UserTrackerMessage.MSG_ENTER_LIFE_CENTER_PAGE);
                        mLifeCenterHostView.setFullLifeCenterPageShowed(true);
                        mLifeCenterHostView.setInWorkSpace(false);
                        mLauncher.moveToDefaultScreen(false);
                        if (mLifeCenterHostView.HOME_PAGE == mLifeCenterHostView.getLastPage()) {
                            if ((mUrl != null) && (mUrl.length() != 0)) {
                                mLifeCenterHostView.showCard(mUrl, mUrlIntent);
                                mUrl = null;
                                mUrlIntent = null;
                            } else {
                                mLifeCenterHostView.enterApp();
                            }
                        }

                        mLifeCenterHostView.idleApp();
                    }
                }
            }
        });
    }

    public void enterShowDetailCard(String url ,Intent intent){
        Log.d(TAG, "enterShowDetailCard url : " + url);

        mUrl = url;
        mUrlIntent = intent;
    }

    public void exitShowDetailCard(){
        Log.d(TAG, "exitShowDetailCard url : " + mUrl);

        mUrl = null;
        mUrlIntent = null;
    }

    public void startLifeCenterInner(String url,Intent intent){
        mLifeCenterHostView.showCard(url,intent);
    }

    public void doResume() {
        mLifeCenterHostView.doResume();
    }

    public void doPause(){
        mLifeCenterHostView.doPause();
    }

    public void doCreate() {
        mLifeCenterHostView.doCreate();
    }

    public void doDestroy() {
        mLifeCenterHostView.doDestroy();
    }

    public void clear() {
    }
}
