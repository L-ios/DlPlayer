
package com.tpw.homeshell;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.TimeInterpolator;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnPreDrawListener;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;

import com.tpw.homeshell.icon.BubbleTextView;
import commonlibs.utils.ACA;

import java.util.ArrayList;

import static android.view.View.*;

public class FolderUtils {
    private static final boolean DEBUG_THUMBNAIL = false;
    public static final boolean LOW_RAM = "true".equals(ACA.SystemProperties.get("ro.config.low_ram", "false"));
    protected static final int FOLDER_OPEN_DURATION = 250;
    protected static final int FOLDER_CLOSE_DURATION = 250;
    protected static final int ANIMATION_START_DELAY = 0;//modified by qinjinchuan topwise for bug322
    private TimeInterpolator mOpenInterpolator = new AccelerateDecelerateInterpolator();
    private TimeInterpolator mCloseInterpolator = new AccelerateInterpolator(0.6f);
    private Folder mFolder;
    private AnimatorSet mCurAnim;
    private Drawable mBackground;
    private OnPreDrawListener mPreDrawListener;
    private float folderTransX, folderTransY;

    public boolean isFolderOpened() {
        return mCurAnim != null || mPreDrawListener != null;
    }

    public void animateOpen(final Folder folder) {
        if (!(folder.getParent() instanceof DragLayer))
            return;
        if (mCurAnim != null || mPreDrawListener != null)
            return;
        ACA.View.setTransitionAlpha(folder, 1);
        folder.getViewTreeObserver().addOnPreDrawListener(
                mPreDrawListener = new ViewTreeObserver.OnPreDrawListener() {

                    public boolean onPreDraw() {
                        folder.getViewTreeObserver().removeOnPreDrawListener(this);
                        mPreDrawListener = null;
                        if (!folder.mLauncher.getWindow().isActive()) {
                            animateClosed(folder, false, false);
                            return false;
                        }
                        mFolder = folder;
                        // enable hardware texture
                        final Launcher launcher = folder.mLauncher;
                        final Hotseat hotseat = launcher.getHotseat();
                        final View indicator = launcher.getIndicatorView();
                        final CellLayout page = (CellLayout) launcher.getWorkspace().getPageAt(launcher.getCurrentWorkspaceScreen());
                        configLayerType(true, page,folder, hotseat, indicator);
                        final FolderIcon folderIcon = folder.getmFolderIcon();
                        final DragLayer root = launcher.getDragLayer();
                        final float scale = folder.mIconScale;
                        final ArrayList<Animator> anims = new ArrayList<Animator>();

                        final Rect iconRect = new Rect();
                        root.getViewRectRelativeToSelf(folderIcon, iconRect);

                        // calculate folder pre offset
                        folderTransX = iconRect.left + folder.mIconPaddingX;
                        folderTransY = iconRect.top + folder.mIconPaddingY;
                        // init folder state
                        folder.setPivotX(0);
                        folder.setPivotY(0);
                        folder.setScaleX(scale);
                        folder.setScaleY(scale);
                        folder.setTranslationX(folderTransX - folder.getLeft());
                        folder.setTranslationY(folderTransY - folder.getTop());

                        // add folder scale up animation
                        ObjectAnimator oa = LauncherAnimUtils.ofPropertyValuesHolder(
                                folder,
                                PropertyValuesHolder.ofFloat(SCALE_X, 1),
                                PropertyValuesHolder.ofFloat(SCALE_Y, 1),
                                PropertyValuesHolder.ofFloat(TRANSLATION_X, 0),
                                PropertyValuesHolder.ofFloat(TRANSLATION_Y, 0));
                        anims.add(oa);

                        // add background fade in animation
                        mBackground = new ColorDrawable(0xff000000);
                        mBackground.setAlpha(0);
                        root.setBackground(mBackground);
                        oa = ObjectAnimator.ofInt(mBackground, "alpha", 0, 0x80);
                        anims.add(oa);

                        // add workspace fade out animation
                        anims.add(ObjectAnimator.ofFloat(page, ALPHA, 0));
                        anims.add(ObjectAnimator.ofFloat(hotseat, ALPHA, 0));
                        anims.add(ObjectAnimator.ofFloat(indicator, ALPHA, 0));

                        final AnimatorSet animSet = new AnimatorSet();
                        animSet.playTogether(anims);
                        animSet.setStartDelay(ANIMATION_START_DELAY);
                        animSet.setDuration(FOLDER_OPEN_DURATION);
                        animSet.setInterpolator(mOpenInterpolator);
                        animSet.addListener(new Listener() {
                            public void onAnimationStart(Animator animation) {
                                folder.setState(Folder.STATE_ANIMATING);
                                folderIcon.setHideIcon(true);
                            }

                            public void onAnimationEnd(Animator animation) {
                                folder.setState(Folder.STATE_OPEN);
                                configLayerType(false, folder);
                                folder.setFocusOnFirstChild();
                                mCurAnim = null;
                            }
                        });
                        if (!DEBUG_THUMBNAIL) {
                            (mCurAnim = animSet).start();
                        }
                        return false;
                    }
                });
    }

    public void animateClosed(final Folder folder, final boolean scrollBack, boolean anim) {
        folder.switchToPage(0, scrollBack);
        if (mPreDrawListener != null) {
            folder.getViewTreeObserver().removeOnPreDrawListener(mPreDrawListener);
            mPreDrawListener = null;
        }
        if (folder.getState() == Folder.STATE_NONE) {
            folder.getmFolderIcon().setHideIcon(false);
            folder.onCloseComplete();
            return;
        }
        mFolder = folder;
        if (mCurAnim != null)
            mCurAnim.cancel();
        if (mFolder == null)
            return;
        final Launcher launcher = folder.mLauncher;
        final Hotseat hotseat = launcher.getHotseat();
        final View indicator = launcher.getIndicatorView();
        final CellLayout page = (CellLayout) launcher.getWorkspace().getPageAt(launcher.getCurrentWorkspaceScreen());

        final float scale = folder.mIconScale;
        final boolean supportCardIcon = folder.isSupportCardIcon();
        final CellLayout content = folder.getContent();
        final ShortcutAndWidgetContainer container = content.getShortcutAndWidgetContainer();
        final FolderEditText folderName = folder.mFolderName;
        final FolderIcon folderIcon = folder.getmFolderIcon();
        final int limit = folder.mDrawingLimit;
        final ArrayList<Animator> anims = new ArrayList<Animator>();

        // add folder scale down animation
        ObjectAnimator oa = LauncherAnimUtils.ofPropertyValuesHolder(
                folder,
                PropertyValuesHolder.ofFloat(SCALE_X, scale),
                PropertyValuesHolder.ofFloat(SCALE_Y, scale),
                PropertyValuesHolder.ofFloat(TRANSLATION_X, folderTransX - folder.getLeft()),
                PropertyValuesHolder.ofFloat(TRANSLATION_Y, folderTransY - folder.getTop()));
        anims.add(oa);

        // add background fade out animation
        anims.add(ObjectAnimator.ofInt(mBackground, "alpha", 0));

        // add workspace fade in animation
        anims.add(ObjectAnimator.ofFloat(page, ALPHA, 1));
        anims.add(ObjectAnimator.ofFloat(hotseat, ALPHA, 1));
        anims.add(ObjectAnimator.ofFloat(indicator, ALPHA, 1));

        final AnimatorSet animSet = new AnimatorSet();
        animSet.playTogether(anims);
        animSet.setStartDelay(ANIMATION_START_DELAY * 2);
        animSet.setDuration(FOLDER_CLOSE_DURATION);
        animSet.setInterpolator(mCloseInterpolator);
        final ArrayList<View> hideTarget = new ArrayList<View>();
        for (int i = limit, N = container.getChildCount(); i < N; i++) {
            hideTarget.add(container.getChildAt(i));
        }
        final Listener listener = new Listener() {
            public void onAnimationStart(Animator animation) {
                // hide icons which invisible inside folder icon
                for (View v : hideTarget) {
                    ACA.View.setTransitionAlpha(v, 0);
                }
                configLayerType(true, folder);
                folderName.setAlpha(0);
                if (!supportCardIcon) {
                    for (int i = 0, N = Math.min(limit, container.getChildCount()); i < N; i++)
                        ((BubbleTextView) container.getChildAt(i))
                                .setDisableLabel(true);
                }
                folder.setState(Folder.STATE_ANIMATING);
            }

            public void onAnimationEnd(Animator animation) {
                folder.onCloseComplete();
                launcher.getDragLayer().setBackground(null);
                folderIcon.setHideIcon(false);
                resetAnimatedView(folder, page, hotseat, indicator);
                folderName.setAlpha(1);
                configLayerType(false, page,folder, hotseat, indicator);
                // enable icons which invisible inside folder icon
                for (View v : hideTarget) {
                    ACA.View.setTransitionAlpha(v, 255);
                }
                if (!supportCardIcon) {
                    for (int i = 0, N = Math.min(limit, container.getChildCount()); i < N; i++)
                        ((BubbleTextView) container.getChildAt(i))
                                .setDisableLabel(false);
                }
                mCurAnim = null;
                mFolder = null;
                folder.setState(Folder.STATE_NONE);
                configLayerType(true, page);
            }
        };
        animSet.addListener(listener);

        if (anim) {
            if (DEBUG_THUMBNAIL) {
                listener.onAnimationStart(animSet);
                mFolder.postDelayed(new Runnable() {
                    public void run() {
                        listener.onAnimationEnd(animSet);
                    }
                }, 2000);
            } else {
                try {
                    (mCurAnim = animSet).start();
                } catch (NullPointerException e) {
                    listener.onAnimationEnd(animSet);
                }
            }
        } else {
            listener.onAnimationEnd(animSet);
        }
    }

    public void clearAnimation() {
        if (mFolder != null) {
            if (mPreDrawListener != null) {
                mFolder.getViewTreeObserver().removeOnPreDrawListener(mPreDrawListener);
                mPreDrawListener = null;
            }
            mPreDrawListener = null;
            if (mCurAnim != null)
                mCurAnim.cancel();
            if (mFolder != null) {
                final Folder folder = mFolder;
                final Launcher launcher = folder.mLauncher;
                final Hotseat hotseat = launcher.getHotseat();
                final View indicator = launcher.getIndicatorView();
                final CellLayout page = (CellLayout) launcher.getWorkspace().getPageAt(launcher.getCurrentWorkspaceScreen());
                launcher.getDragLayer().setBackground(null);
                resetAnimatedView(folder, page, hotseat, indicator);
                folder.getmFolderIcon().setHideIcon(false);
                folder.onCloseComplete();
                folder.setState(Folder.STATE_NONE);
            }
        }
    }

    private void resetAnimatedView(View... vs) {
        for (View v : vs) {
            // check before reset, cause some view may be null
            if (v == null)
                continue;
            v.setAlpha(1);
        }
    }

    private void configLayerType(boolean hardware, View... vs) {
        View v;
        if (hardware) {
            for (int i = 0, N = vs.length; i < N; i++) {
                v = vs[i];
                v.setLayerType(LAYER_TYPE_HARDWARE, null);
                try {
                    v.buildLayer();
                } catch (IllegalStateException e) {
                    // if view hasn't attached to Window, it'll cause
                    // IllegalStateException
                }
            }
        } else {
            for (int i = 0, N = vs.length; i < N; i++) {
                v = vs[i];
                v.setLayerType(LAYER_TYPE_NONE, null);
            }
        }
    }

    public abstract static class Listener implements AnimatorListener {
        public void onAnimationStart(Animator animation) {
        }

        public void onAnimationEnd(Animator animation) {
        }

        public void onAnimationCancel(Animator animation) {
        }

        public void onAnimationRepeat(Animator animation) {
        }
    }

    @Deprecated
    public static void addEditFolderShortcut(Launcher mLauncher, FolderIcon getmFolderIcon) {
    }

    @Deprecated
    public static void removeEditFolderShortcut(FolderInfo info) {
    }
}
