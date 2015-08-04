/**
 * Project: HomeShell
 *
 */
package com.tpw.homeshell;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.PopupWindow;

public class TpwMenu{
	private Activity mActivity = null;
	private View mContentView = null;
    private PopupWindow mMenuView = null;
    private Launcher mLauncher;
    public TpwMenu(Activity context) {
    	mActivity = context;
        mLauncher = (Launcher)context;
    	LayoutInflater inflater = LayoutInflater.from(context);
    	mContentView = inflater.inflate(R.layout.alimenu_layout, null);
    	//int height = (int)mActivity.getResources().getDimension(R.dimen.menu_bar_height);
    	mMenuView = new PopupWindow(
    			 mContentView,
                 LayoutParams.MATCH_PARENT,
                 LayoutParams.WRAP_CONTENT,
                 false //focusable
         );
    	mMenuView.setBackgroundDrawable(new ColorDrawable(0));
    	mMenuView.setOutsideTouchable(true);
    	mMenuView.setAnimationStyle(R.style.options_menu_style);

    	MenuOnClick menuOnClick = new MenuOnClick();
        mContentView.findViewById(R.id.theme).setOnClickListener(menuOnClick);
        mContentView.findViewById(R.id.wallpaper).setOnClickListener(
                menuOnClick);

        mContentView.findViewById(R.id.app_widget).setOnClickListener(menuOnClick);

        mContentView.findViewById(R.id.homeshellsetting).setOnClickListener(menuOnClick);
        mContentView.findViewById(R.id.setting).setOnClickListener(menuOnClick);
    }

    class MenuOnClick implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            try {
                Context context = mActivity.getApplicationContext();
                Intent intent = new Intent();
                int id = v.getId();
                switch (id) {
                    case R.id.theme:
                        UserTrackerHelper
                            .sendUserReport(UserTrackerMessage.MSG_ENTRY_MENU_THEME);
                        intent = new Intent(Intent.ACTION_MAIN);
                        intent.putExtra("fromEntry", "menu");
                        intent.addCategory("com.tpw.auitheme.category.THEMEMANAGER");
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(intent);
                        break;
                    case R.id.wallpaper:
                        UserTrackerHelper
                            .sendUserReport(UserTrackerMessage.MSG_ENTRY_MENU_WALLPAPER);
                        intent.putExtra("fromEntry", "menu");
                        intent.setAction("com.tpw.auitheme.action.VIEW");
                        intent.addCategory("com.tpw.auitheme.category.WALLPAPERMANAGER");
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        mActivity.startActivity(intent);
                        break;
                   
                    case R.id.app_widget:
                    // Launcher launcher = (Launcher)mActivity;
                    // if (launcher.isSearchMode()) {
                    // launcher.exitSearchMode();
                    // } else {
                    // launcher.enterSearchMode();
                    // }
                    mLauncher.onClickAppWidgetMenu();
                        break;

                    case R.id.homeshellsetting:
                    UserTrackerHelper
                            .sendUserReport(UserTrackerMessage.MSG_ENTRY_MENU_LAUNCHER_SETTING);
                        intent.setClass(mActivity,com.tpw.homeshell.setting.HomeShellSetting.class);
                        mActivity.startActivityForResult(intent, Launcher.REQUEST_HOMESHELL_SETTING);
                        break;

                    case R.id.setting:
                    UserTrackerHelper
                            .sendUserReport(UserTrackerMessage.MSG_ENTRY_MENU_SETTINGS);
                        intent.setAction(android.provider.Settings.ACTION_SETTINGS);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        mActivity.startActivity(intent);
                        break;
                    default:
                        break;
                }
            } catch (Exception e) {
                //Toast toast = Toast.makeText(mActivity,
                //        R.string.cant_find_option, Toast.LENGTH_SHORT);
                //toast.show();
                return;
            }
            dismiss();
        }
    }

	
	public void show() {
		mMenuView.showAtLocation(mActivity.findViewById(R.id.drag_layer),Gravity.BOTTOM, 0, mLauncher.getNavigationBarHeight());

	}

	private void initMenu() {
		
		
	}
	
	public View getmContentView() {
        return mContentView;
    }

    public boolean isShowing(){
		if(mMenuView!=null){
			return mMenuView.isShowing();
		}else{
			return false;
		}
	}
	
	public void dismiss() {
		if(mMenuView!=null&&mMenuView.isShowing()){
			mMenuView.dismiss();
		}
	}
    public void clear(){
    	mMenuView = null;
    	mContentView = null;
    	mActivity = null;
    }
}
