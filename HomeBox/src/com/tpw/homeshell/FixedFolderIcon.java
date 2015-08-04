package com.tpw.homeshell;

import java.util.ArrayList;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.tpw.homeshell.DropTarget.DragObject;
import com.tpw.homeshell.FolderInfo.FolderListener;
import com.tpw.homeshell.icon.BubbleTextView;
import com.tpw.homeshell.icon.IconManager;
import com.tpw.homeshell.icon.IconUtils;
import com.tpw.homeshell.model.LauncherModel;
import com.tpw.homeshell.themeutils.ThemeUtils;

public class FixedFolderIcon extends FolderIcon{
    public FixedFolderIcon(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public FixedFolderIcon(Context context) {
        super(context);
        init();
    }
    private void init() {
        myinit(this);
    }
    static FixedFolderIcon fromXml(int resId, Launcher launcher, ViewGroup group,
    		FixedFolderInfo folderInfo) {
        @SuppressWarnings("all") // suppress dead code warning
        final boolean error = getAnimationDuration_initial_item() >= getAnimationDuration_drop_in();
        if (error) {
            throw new IllegalStateException("DROP_IN_ANIMATION_DURATION must be greater than " +
                    "INITIAL_ITEM_ANIMATION_DURATION, as sequencing of adding first two items " +
                    "is dependent on this");
        }

        FixedFolderIcon icon = (FixedFolderIcon) LayoutInflater.from(launcher).inflate(resId, group, false);

        icon.setFolderName((BubbleTextView) icon.findViewById(R.id.folder_icon_name));
      //topwise zyf add for multi-language
  		if(folderInfo.itemExtraType==ItemInfo.ITEM_EXTRA_TYPE_APPS)
  		{
  			folderInfo.title = launcher.getResources().getString(R.string.title_folder_recommend_app);
  		}
  		else if(folderInfo.itemExtraType==ItemInfo.ITEM_EXTRA_TYPE_GAMES)
  		{
  			folderInfo.title = launcher.getResources().getString(R.string.title_folder_games);
  		}
      //topwise zyf add for multi-language end
        icon.getFolderName().setText(folderInfo.title);
        icon.setPreviewBackground((ImageView) icon.findViewById(R.id.preview_background));

        icon.setTag(folderInfo);
        icon.setOnClickListener(launcher);
        icon.setFolderInfo((FolderInfo)folderInfo);
        icon.setLauncher(launcher);
        icon.setIsSupportCard(launcher.getIconManager().supprtCardIcon());
        icon.setContentDescription(String.format(launcher.getString(R.string.folder_name_format),
                folderInfo.title));
        FixedFolder folder = FixedFolder.fromXml(launcher);
        folder.setDragController(launcher.getDragController());
        folder.setFolderIcon(icon);
        folder.bind(folderInfo);
        icon.setFolder(folder);

        icon.mFolderRingAnimator = new FolderRingAnimator(launcher, icon);
        folderInfo.addListener(icon);

        return icon;
    }
    //不允许拖入
    public boolean isDropEnabled() {
    	Log.d("zyflauncher" , "FixedFolderIcon isDropEnabled ........");
        return false;
    }
}
