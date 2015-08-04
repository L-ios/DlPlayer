/**
 */
package com.tpw.homeshell.views;

import android.app.AlertDialog;
import android.graphics.AvoidXfermode;
import android.graphics.AvoidXfermode.Mode;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.tpw.homeshell.AgedModeUtil;
import com.tpw.homeshell.FolderIcon;
import com.tpw.homeshell.Launcher;
import com.tpw.homeshell.LauncherApplication;
import com.tpw.homeshell.R;
import com.tpw.homeshell.icon.BubbleTextView;
import com.tpw.homeshell.icon.IconManager;


/**
 */
public class DropDownDialog extends AlertDialog {
	private static final int DIALOG_SHOW = 0;
	private static final int DIALOG_DISMISS = 1;
	private static final int DIALOG_HIDE = 2;
	private int mCancelFlag = DIALOG_SHOW;
	private Window mWindow = null;
	private View mParent = null;
	private View mContainer = null;
	private TranslateAnimation mSlideUpAnimation;
	private TranslateAnimation mSlideDownAnimation;
    private long mAnimationTime = 300;
	private SlideAnimationListener mSlideAnimationListener = null;
	private String mPositiveButtonText = null;
	private String mNegativeButtonText = null;
	private View.OnClickListener mPositiveListener = null;
	private View.OnClickListener mNegativeListener = null;
	private Button mPositiveButton;
	private Button mNegetiveButton;
	private ImageView mIcon;
	private Bitmap mIconSrc;
        private TextView mTitle;
        private String mTitleText;

	public DropDownDialog(Launcher context) {
		super(context, R.style.dialog);
	}

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mWindow = this.getWindow();
		int flags = WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION | WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
		final WindowManager.LayoutParams wlp = mWindow.getAttributes();
		wlp.flags |= flags;
		mWindow.setAttributes(wlp);
		mWindow.setContentView(R.layout.dropdown_dialog);
		mWindow.setGravity(Gravity.TOP);
		mWindow.setLayout(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		mWindow.setBackgroundDrawable(new ColorDrawable(0));
		mContainer = mWindow.findViewById(R.id.container);
		mParent = findViewById(R.id.layout_parent);
		mIcon = (ImageView)findViewById(R.id.img_icon);
		mPositiveButton = (Button) findViewById(R.id.btn_confirm);
		mNegetiveButton = (Button) findViewById(R.id.btn_cancel);
                mTitle = (TextView) findViewById(R.id.txt_title);
		
		mSlideAnimationListener = new SlideAnimationListener(this);
		
		mSlideUpAnimation = new TranslateAnimation(Animation.RELATIVE_TO_SELF,
				0.0f, Animation.RELATIVE_TO_SELF, 0.0f,
				Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF,
				-1.0f);
		mSlideUpAnimation.setDuration(mAnimationTime);
		mSlideUpAnimation.setAnimationListener(mSlideAnimationListener);

		mSlideDownAnimation = new TranslateAnimation(
				Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF,
				0.0f, Animation.RELATIVE_TO_SELF, -1.0f,
				Animation.RELATIVE_TO_SELF, 0.0f);
		mSlideDownAnimation.setDuration(mAnimationTime);
		mSlideDownAnimation.setAnimationListener(mSlideAnimationListener);
        DecelerateInterpolator interpolator = new DecelerateInterpolator(2);
        mSlideUpAnimation.setInterpolator(interpolator);
        mSlideDownAnimation.setInterpolator(interpolator);
	}

	public void show() {
	    super.show();
	    initDialog();
	    mPositiveButton.setEnabled(true);
	    mContainer.setLayerType(View.LAYER_TYPE_HARDWARE, null);
	    mContainer.startAnimation(mSlideDownAnimation);
	}

	private void initDialog() {
		mIcon.setImageBitmap(mIconSrc);
        mIcon.setAlpha(1.0f);
        mContainer.setAlpha(1.0f);
		mPositiveButton.setText(mPositiveButtonText);
		mPositiveButton.setOnClickListener(mPositiveListener);
		mNegetiveButton.setText(mNegativeButtonText);
		mNegetiveButton.setOnClickListener(mNegativeListener);
                mTitle.setText(mTitleText);
	}

	public void dismiss() {
	    mPositiveButton.setEnabled(false);
	    mCancelFlag = DIALOG_DISMISS;
	    mContainer.setLayerType(View.LAYER_TYPE_HARDWARE, null);
	    mContainer.startAnimation(mSlideUpAnimation);
	}

	public boolean getChecked() {
		boolean checked = false;
        // if (addtioncheckbox != null) {
        // checked = addtioncheckbox.isChecked();
        // }
		return checked;
	}

    /**
     * Set Dialog icon on the left.Change the title color from while to black
     * @param icon the Bitmap which will be show on the left of Dialog
     * @param originalView icon was genereted from this View
     */
    public DropDownDialog setIcon(Bitmap icon, View originalView) {
        if (AgedModeUtil.isAgedMode()) {
            icon = Bitmap.createBitmap(icon, 0, 0, icon.getWidth(), icon.getHeight(),
                    AgedModeUtil.sScaleDown, false);
        }
        mIconSrc = icon;
        int iconTitleDivider = icon.getHeight();
        IconManager im = ((LauncherApplication) LauncherApplication.getContext()).getIconManager();
        if (im == null) {
            im = new IconManager(getContext());
        }
        if( originalView instanceof BubbleTextView ){
            BubbleTextView v = (BubbleTextView)originalView;
            if( im.supprtCardIcon() && !v.isInHotseatOrHideseat() ) return this;
            iconTitleDivider = v.getCompoundPaddingTop();
        }else if( originalView instanceof FolderIcon ){
            FolderIcon v = (FolderIcon)originalView;
            if( im.supprtCardIcon() && !v.isInHotseat() ) return this;
            iconTitleDivider = v.getTitleText().getExtendedPaddingTop();
        }
        if (AgedModeUtil.isAgedMode()) {
            return this;
        }
        Canvas canvas = new Canvas(mIconSrc);
        Paint p = new Paint();
        @SuppressWarnings("deprecation")
        AvoidXfermode mode = new AvoidXfermode(Color.WHITE, 255, Mode.TARGET);
        p.setXfermode(mode);
        p.setColor(Color.BLACK);
        canvas.drawRect(0, iconTitleDivider, mIconSrc.getWidth(), mIconSrc.getHeight(), p);

        return this;
    }

	public void setTitle(String hint, String title) {
		mTitleText = String.format(hint, title);
	}

	public DropDownDialog setPositiveButton(String text,
			final View.OnClickListener listener) {
		mPositiveButtonText = text;
		mPositiveListener = listener;
		return this;
	}

	public DropDownDialog setNegativeButton(String text) {
		View.OnClickListener listener = new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				DropDownDialog.this.dismiss();
			}
		};
		setNegativeButton(text, listener);
		return this;
	}

	public DropDownDialog setNegativeButton(String text,
			final View.OnClickListener listener) {
		mNegativeButtonText = text;
		mNegativeListener = listener;
		return this;
	}

	class SlideAnimationListener implements AnimationListener {

		DropDownDialog mDialog;

		public SlideAnimationListener(DropDownDialog dialog) {
			mDialog = dialog;
		}

		@Override
		public void onAnimationStart(Animation animation) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onAnimationEnd(Animation animation) {
		    if(mDialog != null && mDialog.mContainer != null) {
		        mDialog.mContainer.setLayerType(View.LAYER_TYPE_NONE, null);
		    }
			if (animation == mSlideUpAnimation) {
				switch (mCancelFlag) {
				case DIALOG_DISMISS:
					DropDownDialog.super.dismiss();
					break;
				case DIALOG_HIDE:
					DropDownDialog.super.hide();
					break;
				}
			} else if(animation == mSlideDownAnimation) {
			    if(mDialog != null && mDialog.mParent != null) {
			        mDialog.mParent.setVisibility(View.VISIBLE);
			    }
			}
		}

		@Override
		public void onAnimationRepeat(Animation animation) {
			// TODO Auto-generated method stub

		}

	}

	@Override
	public void onDetachedFromWindow() {
		super.onDetachedFromWindow();
		if (mIconSrc != null && !mIconSrc.isRecycled()) {
			mIconSrc.recycle();
			mIconSrc = null;
		}
	}

	public Button getNegetiveButton() {
		return mNegetiveButton;
	}

	@Override
	public void onBackPressed() {
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK
				&& mNegetiveButton != null) {
	                mNegetiveButton.performClick();
			return true;
		}
		return super.onKeyUp(keyCode, event);
	}
}
