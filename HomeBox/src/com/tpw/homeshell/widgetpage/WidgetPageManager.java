
package com.tpw.homeshell.widgetpage;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import com.tpw.homeshell.FastBitmapDrawable;
import com.tpw.homeshell.ItemInfo;
import com.tpw.homeshell.LauncherApplication;
import com.tpw.homeshell.LauncherSettings;
import com.tpw.homeshell.R;
import com.tpw.homeshell.Launcher;
import com.tpw.homeshell.ShortcutInfo;
import com.tpw.homeshell.TopwiseConfig;
import com.tpw.homeshell.icon.IconManager;
import com.tpw.homeshell.model.LauncherModel;

public class WidgetPageManager implements IAliWidgetPage {
    public static final String TAG = "WidgetPageManager";
    public static final String PAGE_LAYOUT_NAME = "page_main";
    public static final String HOTSEAT_LAYOUT_NAME = "hotseat";
    private List<WidgetPageInfo> mWigetPageList;
    private Context mContext;
    private static IconManager mIconManager;//add by huangweiwei, topwise, 2015-1-9
    //add by huangweiwei, topwise, 2015-7-1
    private SharedPreferences mSharedPref;
    private List<WidgetPageInfo> mWigetPageTotalList;
    //add end by huangweiwei, topwise, 2015-7-1

    public class WidgetPageInfo {
        private String mPackageName;
        private View mRootView;
        private Context mRemoteContext;
        private View mHotseatView;
        private ComponentName[] mComponentList = null;//add by huangweiwei, topwise, 2015-1-20
        private int[] mHotseatViewId = null;//add by huangweiwei, topwise, 2015-1-20
        //add by huangweiwei, topwise, 2015-7-1
        private int index;
        private String mTitle;
        private Drawable mPreviewDrawable;
        private boolean mIsRemoved = false;
        //add end by huangweiwei, topwise, 2015-7-1

        public String getPackageName() {
            return mPackageName;
        }

        public View getRootView() {
            return mRootView;
        }

        public View getHotseatView() {
            return mHotseatView;
        }
        
        //add by huangweiwei, topwise, 2015-7-1
        public String getTitle() {
        	return mTitle;
        }
        
        public Drawable getPreviewDrawable() {
        	return mPreviewDrawable;
        }
        
        public boolean getIsRemoved() {
        	return mIsRemoved;
        }
        
        public void setIsRemoved(boolean removed) {
        	if (mIsRemoved != removed) {
            	mSharedPref.edit()
                    .putBoolean(mPackageName, removed)
                    .commit();
            	if (removed) {
                	mWigetPageList.remove(this);
            	} else {
                	int i=0;
                	for(; i<mWigetPageList.size(); i++) {
                		WidgetPageInfo info = mWigetPageList.get(i);
                		if (info.index > index) break;
                	}
                	mWigetPageList.add(i, this);
            	}
        	}
        	mIsRemoved = removed;
        }
        //add end by huangweiwei, topwise, 2015-7-1
    }

    public WidgetPageManager(Context context) {
        mContext = context;
        Resources r = context.getResources();
        String[] packageNames = r.getStringArray(R.array.widgetpage_array);
        //add by huangweiwei, topwise, 2015-7-1
        String[] titles = r.getStringArray(R.array.widgetpage_title);
        TypedArray imgTypeArray = r.obtainTypedArray(R.array.widgetpage_resid);
        mSharedPref = context.getSharedPreferences("com.tpw.homeshell_preferences",
                Context.MODE_PRIVATE);
        mWigetPageTotalList = new ArrayList<WidgetPageInfo>();
        //add end by huangweiwei, topwise, 2015-7-1

        mWigetPageList = new ArrayList<WidgetPageInfo>();
        String packagename;
        int hotseatH = (int) context.getResources().getDimension(
                R.dimen.button_bar_height_plus_padding);
        for (int i = 0; i < packageNames.length; i++) {
            packagename = packageNames[i];
            if (packagename == null || packagename.length() == 0) {
                continue;
            }
            WidgetPageInfo info = new WidgetPageInfo();
            info.mPackageName = packagename;
            //add by huangweiwei, topwise, 2015-7-1
            info.index = i;
            info.mTitle = titles[i];
            info.mPreviewDrawable = imgTypeArray.getDrawable(i);
            //modify by huangxunwan for config the widgetpage
            info.mIsRemoved = mSharedPref.getBoolean(packagename, !TopwiseConfig.HOMESHELL_DEF_WIDGETPAGE_SHOW);
            //add end by huangweiwei, topwise, 2015-7-1
            info.mRemoteContext = newWidgetContext(context, packagename);
            if (info.mRemoteContext == null) {
                continue;
            }
            info.mRootView = createView(info.mRemoteContext, packagename, PAGE_LAYOUT_NAME);
            if (info.mRootView == null) {
                continue;
            }
            info.mHotseatView = createView(info.mRemoteContext, packagename, HOTSEAT_LAYOUT_NAME);
            if (info.mHotseatView != null) {
                // ((Launcher)context).getDragLayer().addView(info.mHotseatView);
                FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT, hotseatH);
                lp.gravity = Gravity.BOTTOM;
                ((Launcher) context).getDragLayer().addView(info.mHotseatView, lp);
                info.mHotseatView.setVisibility(View.GONE);
            }
            //add by huangweiwei, topwise, 2015-7-1
        	mWigetPageTotalList.add(info);
            if (info.mIsRemoved) {
            } else
            //add end by huangweiwei, topwise, 2015-7-1
            mWigetPageList.add(info);
        }
        //add by huangweiwei, topwise, 2015-1-9
        if (mIconManager == null) {
            mIconManager = ((LauncherApplication)mContext.getApplicationContext()).getIconManager();
        }
	 if(isSupportWidgetPageHotseat()) { 	
        	setHotseat();
	 }	
        //add end by huangweiwei, topwise, 2015-1-9
        Log.e(TAG, "WidgetPageManager init size:" + mWigetPageList.size());

    }
    
    //add by huangweiwei, topwise, 2015-7-1
    public List<WidgetPageInfo> getWigetPageTotalList() {
    	return mWigetPageTotalList;
    }
    //add end by huangweiwei, topwise, 2015-7-1

    public int getWigetPageCount() {
        return mWigetPageList.size();
    }

    public View getWidgetPageRootView(int index) {
        WidgetPageInfo info = mWigetPageList.get(index);
        if (info != null) {
            return info.mRootView;
        }
        return null;

    }

    public View getWidgetPageRootView(String packagename) {
        WidgetPageInfo info = getWidgetPageInfo(packagename);
        if (info != null) {
            return info.mRootView;
        }
        return null;
    }

    public View getHotseatView(String packagename) {
        
        WidgetPageInfo info = getWidgetPageInfo(packagename);
        if (info != null) {
            return info.mHotseatView;
        }
        return null;
    }
    
    public WidgetPageInfo getWidgetPageInfo(String packagename) {
        for (int i = 0; i < mWigetPageList.size(); i++) {
            WidgetPageInfo info = mWigetPageList.get(i);
            if (info.mPackageName.equals(packagename)) {
                return info;
            }
        }
        return null;
    }

    public WidgetPageInfo getWidgetPageInfo(int index) {
        WidgetPageInfo info = mWigetPageList.get(index);

        return info;
    }

    public static Context newWidgetContext(Context context, String packageName) {

        int contextPermission = Context.CONTEXT_INCLUDE_CODE | Context.CONTEXT_IGNORE_SECURITY;

        Context theirContext = null;
        try {
            theirContext = context.createPackageContext(packageName, contextPermission);
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        return theirContext;
    }

    public static View createView(Context remoteContext, String packagename, String resource) {
        Context theirContext = remoteContext;

        if (theirContext == null) {
            return null;
        }
        LayoutInflater theirInflater = (LayoutInflater) theirContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        theirInflater = theirInflater.cloneInContext(theirContext);
        Resources r = theirContext.getResources();

        int id = 0;

        id = r.getIdentifier(resource, "layout", packagename);

        if (id == 0) {
            Log.e(TAG, "ERROR! can't get root layout id.");
            return null;
        }
        View v = null;

        try {
            v = theirInflater.inflate(id, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (v != null) {
            ItemInfo info = new ItemInfo();
            v.setTag(info);
        }
        return v;
    }

    @Override
    public void onPause() {

        try {

            Method m = null;
            for (int i = 0; i < mWigetPageList.size(); i++) {
                WidgetPageInfo info = mWigetPageList.get(i);
                if (info != null) {
                    m = info.mRootView.getClass().getDeclaredMethod("onPause");
                    if (m != null) {
                        m.invoke(info.mRootView);
                    }
                }

            }

        } catch (Exception e) {

            e.printStackTrace();
        }

    }

    @Override
    public void onResume() {
        try {

            Method m = null;
            for (int i = 0; i < mWigetPageList.size(); i++) {
                WidgetPageInfo info = mWigetPageList.get(i);
                if (info != null) {
                    m = info.mRootView.getClass().getDeclaredMethod("onResume");
                    if (m != null) {
                        m.invoke(info.mRootView);
                    }
                }
            }

        } catch (Exception e) {

            e.printStackTrace();
        }

    }

    @Override
    public void onPageBeginMoving() {
        try {

            Method m = null;
            for (int i = 0; i < mWigetPageList.size(); i++) {
                WidgetPageInfo info = mWigetPageList.get(i);
                if (info != null) {
                    m = info.mRootView.getClass().getDeclaredMethod("onPageBeginMoving");
                    if (m != null) {
                        m.invoke(info.mRootView);
                    }
                }

            }

        } catch (Exception e) {

            e.printStackTrace();
        }
    }

    @Override
    public void enterWidgetPage(int page) {
        try {
            Method m = null;
            WidgetPageInfo info =  ((Launcher)mContext).getWorkspace().getWidgetPageInfoAt(page);
            if (info != null) {
                m = info.mRootView.getClass().getDeclaredMethod("enterWidgetPage", Integer.TYPE);
                if (m != null) {
                    m.invoke(info.mRootView, page);
                }
            }
        } catch (Exception e) {

            e.printStackTrace();
        }
    }

    @Override
    public void leaveWidgetPage(int page) {
        try {
            Method m = null;
            WidgetPageInfo info =  ((Launcher)mContext).getWorkspace().getWidgetPageInfoAt(page);
            if (info != null) {
                m = info.mRootView.getClass().getDeclaredMethod("leaveWidgetPage", Integer.TYPE);
                if (m != null) {
                    m.invoke(info.mRootView, page);
                }
            }
        } catch (Exception e) {

            e.printStackTrace();
        }
    }
    
    public static boolean isSupportWidgetPageHotseat() {
        return false;
    }

	//add by huangweiwei, topwise, 2015-1-9
    private static final int MAX_HOTSEAT = 6;
    private static final String VIEW_LAYOUT_NAME = "bwc_layout_app";
    private void getHotseatComponentList() {
    	Method m1 = null;
    	ComponentName component = null;
    	for (int i = 0; i < mWigetPageList.size(); i++) {
    		WidgetPageInfo info = mWigetPageList.get(i);
    		if (info.mComponentList != null) {
    			continue;
    		}
			info.mComponentList = new ComponentName[MAX_HOTSEAT];
        	try {
            	m1 = info.mHotseatView.getClass().getDeclaredMethod("getHotseat", Integer.TYPE);
			} catch (Exception e) {
				Log.d("huangweiwei", "getHotseatComponentList, m1 error! e="+e);
				continue;
			}
        	for (int j=0; j<info.mComponentList.length; j++) {
        		info.mComponentList[j] = null;
        		component = null;
        		try {
                    if (m1 != null) {
                    	Object obj = m1.invoke(info.mHotseatView, j);
                    	Log.d("huangweiwei", "getHotseatComponentList obj="+obj);
                    	if (obj instanceof ComponentName) {
                    		component = (ComponentName) obj;
                    	}
                    	if (component != null) {
                    		info.mComponentList[j] = component.clone();
                    	}
                    }
				} catch (Exception e) {
				}
             }
    	}
    }
    
    private void getHotseatViewId() {
    	int id = 0;
    	for (int i = 0; i < mWigetPageList.size(); i++) {
    		WidgetPageInfo info = mWigetPageList.get(i);
    		if (info.mHotseatViewId != null) {
    			continue;
    		}
    		info.mHotseatViewId = new int[MAX_HOTSEAT];
    		for (int j=0; j<info.mHotseatViewId.length; j++) {
        		Resources r = info.mRemoteContext.getResources();
                id = r.getIdentifier(""+VIEW_LAYOUT_NAME + (j+1), "id", info.mPackageName);
                info.mHotseatViewId[j] = id;
    		}
    	}
    }
    
    public void enableSpecialHotseat(ComponentName componentName,
    		String pkg, String cls,
    		boolean viewEnable,
    		boolean viewGone) {
    	//init
    	getHotseatComponentList();
    	getHotseatViewId();
    	
    	View view = null;
    	ComponentName component = componentName;
    	if (component == null) {
    		component = new ComponentName(pkg, cls);
    	}
    	for (int i = 0; i < mWigetPageList.size(); i++) {
    		WidgetPageInfo info = mWigetPageList.get(i);
    		if (info.mComponentList != null) {
    			for (int j = 0; j < info.mComponentList.length; j++) {
    				view = null;
    				if (component.equals(info.mComponentList[j])) {
    					int id = info.mHotseatViewId[j];
    					if (id != 0) {
        					view = info.mHotseatView.findViewById(id);
    					}
    				}
    				if (view != null) {
    					view.setEnabled(viewEnable);
    					view.setVisibility(viewGone?View.GONE:View.VISIBLE);
    				}
    			}
    		}
    	}
    }
    
	public void setHotseat() {
		ArrayList<ItemInfo> infos = LauncherModel.getSbgWorkspaceItems();
		CharSequence appName = null;
		Drawable drawable = null;
		ComponentName component = null;
        try {
            Method m = null;
            Method m1 = null;
            for (int i = 0; i < mWigetPageList.size(); i++) {
                WidgetPageInfo info = mWigetPageList.get(i);
                if (info != null) {
                	try {
                    	m1 = info.mHotseatView.getClass().getDeclaredMethod("getHotseat", Integer.TYPE);
                		m = info.mHotseatView.getClass().getDeclaredMethod("setHotseat", Integer.TYPE, Drawable.class, CharSequence.class);
					} catch (Exception e) {
						Log.d("huangweiwei", "setHotseat, m1 or m error! e="+e);
						continue;
					}
                	for (int j=0; j<MAX_HOTSEAT; j++) {
                		component = null;
                        if (m1 != null) {
                        	Object obj = m1.invoke(info.mHotseatView, j);
                        	Log.d("huangweiwei", "setHotseat obj="+obj);
                        	if (obj instanceof ComponentName) {
                        		component = (ComponentName) obj;
                        	}
                        	if (component != null) {
                        		appName = null;
                        		drawable = mIconManager.getAppUnifiedIcon(component);
                        		//daiwei modify begin,
                        		//因为隐藏桌面图标功能会导致iconcache里的图标发生变化，从而导致此处的图标发生变化。
                        		//因此创建一个新的对象
                        		if (drawable instanceof FastBitmapDrawable) {
	                        		Bitmap bmp = ((FastBitmapDrawable)drawable).getBitmap();
	                        		drawable = new FastBitmapDrawable(bmp);
                        		}
                        		//daiwei modify end
                        		if (infos != null) {
                        			try {
                            			//for (ItemInfo item : infos) {
                        				ItemInfo item = null;
                        				for (int k = infos.size() - 1; k>=0; k--) {
                        					item = infos.get(k);
                            				if (item.itemType == LauncherSettings.Favorites.ITEM_TYPE_SHORTCUT
                            						|| item.itemType == LauncherSettings.Favorites.ITEM_TYPE_APPLICATION) {
                            					if ((((ShortcutInfo)item).intent != null) &&
                                                    (component.equals(((ShortcutInfo)item).intent.getComponent()))) {
                                					appName = item.title;
                            						Log.d("huangweiwei", "setHotseat, find in infos! appName="+appName);
                            						break;
                            					}
                            				}
                            			}
									} catch (Exception e) {
										Log.d("huangweiwei", "setHotseat, infos e="+e);
										appName = null;
									}
                        		}
                        		if (appName == null) {
                        			appName = getAppName(component);
            						Log.d("huangweiwei", "setHotseat, find in getAppName(xx)! appName="+appName);
                        		}
                            	Log.d("huangweiwei", "setHotseat drawable="+drawable+";appName="+appName);
                                if (m != null) {
                                    m.invoke(info.mHotseatView, j, drawable, appName);
                                }
                        	}
                        }
                	}
                }
            }
        } catch (Exception e) {
        	Log.d("huangweiwei", "setHotseat", e);
            //e.printStackTrace();
        }
    }
	
	private CharSequence getAppName(ComponentName component) {
		CharSequence appName = null;
        final android.content.Intent mainIntent = new android.content.Intent(android.content.Intent.ACTION_MAIN, null);
        mainIntent.addCategory(android.content.Intent.CATEGORY_LAUNCHER);
        mainIntent.setComponent(component);
        final android.content.pm.PackageManager packageManager = mContext.getPackageManager();
        List<android.content.pm.ResolveInfo> apps = null;
        apps = packageManager.queryIntentActivities(mainIntent, 0);
        if (apps!=null) {
        	int size = apps.size();
        	if (size == 1) {
        		appName = apps.get(0).loadLabel(packageManager).toString();
        	}
        }
        return appName;
	}
	//add end by huangweiwei, topwise, 2015-1-9
}
