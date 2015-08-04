package com.tpw.homeshell;


import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.Bitmap;
import android.graphics.ColorFilter;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader.TileMode;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.SimpleOnPageChangeListener;
import android.text.Editable;
import android.text.InputType;
import android.text.Selection;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
import android.view.animation.LinearInterpolator;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.tpw.homeshell.Alarm.OnAlarmListener;
import com.tpw.homeshell.DropTarget.DragObject;
import com.tpw.homeshell.FolderInfo.FolderListener;
import com.tpw.homeshell.LauncherSettings.Favorites;
import com.tpw.homeshell.icon.BubbleTextView;
import com.tpw.homeshell.icon.IconManager;
import com.tpw.homeshell.model.LauncherModel;


import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class FixedFolder extends Folder{
	private static final String TAG = "Launcher.FixedFolder";

    /**
     * Used to inflate the Workspace from XML.
     *
     * @param context The application's context.
     * @param attrs The attribtues set containing the Workspace's customization values.
     */
    public FixedFolder(Context context, AttributeSet attrs) {
    	super(context, attrs);
        setAlwaysDrawnWithCacheEnabled(false);
        setLayoutInflater(LayoutInflater.from(context));

/*
        Resources res = getResources();
        mMaxCountX = res.getInteger(R.integer.folder_max_count_x);
        mMaxCountY = res.getInteger(R.integer.folder_max_count_y);
        mMaxNumItems = res.getInteger(R.integer.folder_max_num_items);
        if (mMaxCountX < 0 || mMaxCountY < 0 || mMaxNumItems < 0) {
            mMaxCountX = LauncherModel.getCellCountX();
            mMaxCountY = LauncherModel.getCellCountY();
            mMaxNumItems = mMaxCountX * mMaxCountY;
        }
*/
        mLauncher = (Launcher) context;
        mIconManager = mLauncher.getIconManager();
        setMaxCountX(ConfigManager.getFolderMaxCountX());
        setMaxCountY(ConfigManager.getFolderMaxCountY());
		  setMaxNumItems(getMaxCountX()*getMaxCountY());

        Resources res = getResources();
        setInputMethodManager((InputMethodManager)
                getContext().getSystemService(Context.INPUT_METHOD_SERVICE));

        setExpandDuration(res.getInteger(R.integer.config_folderAnimDuration));

        if (getDefaultFolderName() == null) {
            setDefaultFolderName(res.getString(R.string.folder_name));
        }
        /*
         * if (sHintText == null) { sHintText =
         * res.getString(R.string.folder_hint_text); }
         */
        // We need this view to be focusable in touch mode so that when text editing of the folder
        // name is complete, we have something to focus on, thus hiding the cursor and giving
        // reliable behvior when clicking the text field (since it will always gain focus on click).
        setFocusableInTouchMode(true);
        
        mShortcutInfoCache = new ShortcutInfo();
        
        Display display = mLauncher.getWindowManager().getDefaultDisplay();
        display.getSize(getDisplaySize());
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mFolderName.setEnabled(false);
    }
    static FixedFolder fromXml(Context context) {
        return (FixedFolder) LayoutInflater.from(context).inflate(R.layout.fixeduser_folder, null);
    }

    public boolean onLongClick(View v) {
    	//zyf add for fixedfolder  不支持长按删除或拖动
    	Log.d("zyflauncher" , " onLongClick ........");
    		return true;
    	//zyf add end
    }
//不支持长按删除或拖动
    public boolean isDropEnabled() {
    	Log.d("zyflauncher" , " isDropEnabled ........");
        return false;
    }
    public void onDrop(DragObject dragObject){}

    public void onDragEnter(DragObject dragObject){}

    public void onDragOver(DragObject dragObject){}

    public void onDragExit(DragObject dragObject){}
}
