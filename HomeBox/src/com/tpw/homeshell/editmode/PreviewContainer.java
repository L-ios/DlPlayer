
package com.tpw.homeshell.editmode;

import java.util.ArrayList;

import com.tpw.homeshell.Launcher;
import com.tpw.homeshell.LauncherApplication;
import com.tpw.homeshell.R;
import com.tpw.homeshell.editmode.HorizontalListView.OnScrollStateChangedListener;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

public class PreviewContainer extends LinearLayout implements View.OnClickListener,
        OnScrollStateChangedListener {
    private static final String TAG = "PreviewContainer";

    private enum ContentType {
        Widgets,
        Effects,
        WidgetPage,//add by huangweiwei, topwise, 2015-7-1
        Wallpapers,
        Themes
    };

    private TextView mTabWidgetTxt;
    private TextView mTabEffectsTxt;
    private TextView mTabWallpapersTxt;
    private TextView mTabThemesTxt;
    private HorizontalListView mPreviewList;
    private View mRightImage;
    private View mLeftImage;
    private Context mContext;
    private WidgetPreviewAdapter mWidgetAdapter;
    private EffectsPreviewAdapter mEffectsAdapter;
    private WallpapersPreviewAdapter mWallpapersAdapter;
    private ThemesPreviewAdapter mThemesAdapter;
    private View mLoadingLayout;
    private int mTabTxtColor;
    private int mTabTxtColorUnselected;
    private ContentType mContentType;

    @SuppressLint("NewApi")
    public PreviewContainer(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mContext = context;
        mTabTxtColor = context.getResources().getColor(
                R.color.preview_tab_textcolor);
        mTabTxtColorUnselected = context.getResources().getColor(
                R.color.preview_tab_textcolor_unselected);
    }

    public PreviewContainer(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PreviewContainer(Context context) {
        this(context, null, 0);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mPreviewList = (HorizontalListView) findViewById(R.id.preview_list);
        mTabWidgetTxt = (TextView) findViewById(R.id.preview_tab_widgets);
        mTabEffectsTxt = (TextView) findViewById(R.id.preview_tab_effects);
        mTabWallpapersTxt = (TextView) findViewById(R.id.preview_tab_wallpapers);
        mTabThemesTxt = (TextView) findViewById(R.id.preview_tab_themes);
        mRightImage = findViewById(R.id.appscustomize_right_image);
        mLeftImage = findViewById(R.id.appscustomize_left_image);
        mLoadingLayout = findViewById(R.id.loading_layout);
        mPreviewList.setOnScrollStateChangedListener(this);
        mTabWidgetTxt.setOnClickListener(this);
        mTabEffectsTxt.setOnClickListener(this);
        mTabWallpapersTxt.setOnClickListener(this);
        mTabThemesTxt.setOnClickListener(this);
        setContentType(ContentType.Widgets);
    }

    void setContentType(ContentType type) {
        if (mContentType != type) {
            mContentType = type;
            switch (mContentType) {
                case Widgets:
                    if (mWidgetAdapter == null) {
                        mWidgetAdapter = new WidgetPreviewAdapter((Launcher) mContext);
                    }
                    mPreviewList.setAdapter(mWidgetAdapter);
                    mTabWidgetTxt.setTextColor(mTabTxtColor);
                    mTabWallpapersTxt.setTextColor(mTabTxtColorUnselected);
                    mTabThemesTxt.setTextColor(mTabTxtColorUnselected);
                    mTabEffectsTxt.setTextColor(mTabTxtColorUnselected);
                    mPreviewList.setVisibility(View.VISIBLE);
                    mLoadingLayout.setVisibility(View.INVISIBLE);
                    break;
                case Effects:
                    if (mEffectsAdapter == null) {
                        mEffectsAdapter = new EffectsPreviewAdapter(mContext);
                    }
                    mPreviewList.setAdapter(mEffectsAdapter);
                    mTabEffectsTxt.setTextColor(mTabTxtColor);
                    mTabWidgetTxt.setTextColor(mTabTxtColorUnselected);
                    mTabWallpapersTxt.setTextColor(mTabTxtColorUnselected);
                    mTabThemesTxt.setTextColor(mTabTxtColorUnselected);
                    mPreviewList.setVisibility(View.VISIBLE);
                    mLoadingLayout.setVisibility(View.INVISIBLE);
                    break;
                //add by huangweiwei, topwise, 2015-7-1
                case WidgetPage:
                	if (mWidgetPageAdapter == null) {
                		mWidgetPageAdapter = new WidgetPagePreviewAdapter(mContext);
                	}
                	mPreviewList.setAdapter(mWidgetPageAdapter);
                    mTabEffectsTxt.setTextColor(mTabTxtColorUnselected);
                    mTabWidgetTxt.setTextColor(mTabTxtColorUnselected);
                    mTabWallpapersTxt.setTextColor(mTabTxtColorUnselected);
                    mTabThemesTxt.setTextColor(mTabTxtColorUnselected);
                    mPreviewList.setVisibility(View.VISIBLE);
                    mLoadingLayout.setVisibility(View.INVISIBLE);
                	break;
                //add by huangweiwei, topwise, 2015-7-1
                case Wallpapers:
                    if (mWallpapersAdapter == null) {
                        mWallpapersAdapter = new WallpapersPreviewAdapter(mContext);
                    } else {
                        if (mWallpapersAdapter.needReload()) {
                            mWallpapersAdapter.reload();
                        }
                    }
                    mWallpapersAdapter.setView(mPreviewList, mLoadingLayout);
                    mPreviewList.setAdapter(mWallpapersAdapter);
                    if (mWallpapersAdapter.getCount() == 0) {
                        mPreviewList.setVisibility(View.INVISIBLE);
                        mLoadingLayout.setVisibility(View.VISIBLE);
                    } else {
                        mPreviewList.setVisibility(View.VISIBLE);
                        mLoadingLayout.setVisibility(View.INVISIBLE);
                    }
                    mTabWallpapersTxt.setTextColor(mTabTxtColor);
                    mTabWidgetTxt.setTextColor(mTabTxtColorUnselected);
                    mTabEffectsTxt.setTextColor(mTabTxtColorUnselected);
                    mTabThemesTxt.setTextColor(mTabTxtColorUnselected);
                    break;
                case Themes:
                    if (mThemesAdapter == null) {
                        mThemesAdapter = new ThemesPreviewAdapter(mContext);
                    } else {
                        if (mThemesAdapter.needReload()) {
                            mThemesAdapter.reload();
                        }
                    }
                    mThemesAdapter.setView(mPreviewList, mLoadingLayout);
                    mPreviewList.setAdapter(mThemesAdapter);
                    if (mThemesAdapter.getCount() == 0) {
                        mPreviewList.setVisibility(View.INVISIBLE);
                        mLoadingLayout.setVisibility(View.VISIBLE);
                    } else {
                        mPreviewList.setVisibility(View.VISIBLE);
                        mLoadingLayout.setVisibility(View.INVISIBLE);
                    }
                    mTabThemesTxt.setTextColor(mTabTxtColor);
                    mTabWidgetTxt.setTextColor(mTabTxtColorUnselected);
                    mTabEffectsTxt.setTextColor(mTabTxtColorUnselected);
                    mTabWallpapersTxt.setTextColor(mTabTxtColorUnselected);
                    break;
                default:
                    break;
            }
            mLeftImage.setVisibility(View.INVISIBLE);
            mRightImage.setVisibility(View.INVISIBLE);
        }
    }

    private void updateRightLeftImage(int leftIndex, int rightIndex, int count) {
        if (count > 0) {
            if (leftIndex == 0) {
                mLeftImage.setVisibility(View.INVISIBLE);
            } else {
                mLeftImage.setVisibility(View.VISIBLE);
            }

            if (rightIndex == (count - 1)) {
                mRightImage.setVisibility(View.INVISIBLE);
            } else {
                mRightImage.setVisibility(View.VISIBLE);
            }
        } else {
            mLeftImage.setVisibility(View.INVISIBLE);
            mRightImage.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onClick(View v) {
        if (v.equals(mTabWidgetTxt)) {
            setContentType(ContentType.Widgets);
        } else if (v.equals(mTabEffectsTxt)) {
            setContentType(ContentType.Effects);
        } else if (v.equals(mTabWallpapersTxt)) {
            setContentType(ContentType.Wallpapers);
        } else if (v.equals(mTabThemesTxt)) {
            setContentType(ContentType.Themes);
        }
        //add by huangweiwei, topwise, 2015-7-1
        else if (v.equals(mTabWidgetPageImage)) {
        	setContentType(ContentType.WidgetPage);
        }
        //add by huangweiwei, topwise, 2015-7-1
    }

    @Override
    public void onScrollStateChanged(ScrollState scrollState) {
    }

    @Override
    public void onScroll(int leftIndex, int rightIndex, int count) {
        updateRightLeftImage(leftIndex, rightIndex, count);
    }
    
    public void onPackagesUpdated(ArrayList<Object> widgetsAndShortcuts) {
        if (mWidgetAdapter != null) {
            mWidgetAdapter.onPackagesUpdated(widgetsAndShortcuts);
            mWidgetAdapter.notifyDataSetInvalidated();
        }
    }
    
    //add by huangweiwei, topwise, 2015-7-1
    private View mTabWidgetPageImage;
    private WidgetPagePreviewAdapter mWidgetPageAdapter;
    
    @Override
    public void setVisibility(int visibility) {
    	super.setVisibility(visibility);
    	
    	if (mTabWidgetPageImage == null && LauncherApplication.getLauncher().mWidgetPageManager != null) {
    		mTabWidgetPageImage =findViewById(R.id.preview_tab_widget);
            if (LauncherApplication.getLauncher().mWidgetPageManager.getWigetPageTotalList().size() != 0) {
            	mTabWidgetPageImage.setVisibility(View.VISIBLE);
            	mTabWidgetPageImage.setOnClickListener(this);
            } else {
            	mTabWidgetPageImage.setVisibility(View.GONE);
            	mTabWidgetPageImage.setOnClickListener(null);
            }
    	}
    }
    //add end by huangweiwei, topwise, 2015-7-1
}
