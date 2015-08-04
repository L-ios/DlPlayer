package com.tpw.homeshell.smartsearch;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import com.tpw.homeshell.LauncherApplication;
import com.tpw.homeshell.LauncherSettings;
import com.tpw.homeshell.R;
import com.tpw.homeshell.smartsearch.DialpadImageButton.OnPressedListener;
import com.tpw.homeshell.smartsearch.SearchTask.OnSearchDoneListener;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.pm.IPackageInstallObserver;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;

public class T9DialpadView extends FrameLayout implements
                OnClickListener, OnItemClickListener, OnSearchDoneListener,
                SearchListView.OnSearchListSlideListener, OnPressedListener {

    public static interface T9DialpadViewListener {
        void onExit();
    }

    private static final String TAG = "T9DialpadView";

    private EditText mInfoText;
    private SearchListView mList;
    private View mKeyboard;
    private ImageButton mClearAll;

    private SearchTask mSearchTask;

    private Handler mHandler = new Handler();

    private T9DialpadViewListener mT9DialpadViewListener;
    private MatchedAppsAdapter mEmptyMatchedAppsAdapter;

    private static final int DIALPAD_VIEW_ANIMATION_DURATION = 250;
    private Animator mDiapladViewInAnim;
    private Animator mDialpadViewOutAnim;
    private int mKeyboardTop;

    public static final String TYPE_PACKAGENAME = "packagename";
    public static final String TYPE_PACKAGEPATH= "packagepath";

    private static final String[] BUTTON_TAG = {
        "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "*", "#"
    };

    public T9DialpadView(Context context) {
        super(context);
    }

    public T9DialpadView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public T9DialpadView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setT9DialpadViewListener(T9DialpadViewListener l) {
        mT9DialpadViewListener = l;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mInfoText = (EditText) findViewById(R.id.info_text);
        mInfoText.setClickable(true);
        mInfoText.setOnClickListener(this);
        mKeyboard = findViewById(R.id.dialpad);
        mList = (SearchListView) findViewById(R.id.search_result);
        mEmptyMatchedAppsAdapter = new MatchedAppsAdapter(getContext(), null);
        mList.setAdapter(mEmptyMatchedAppsAdapter);
        mList.setOnItemClickListener(this);
        mList.setOnSearchListSlideListener(this);
        setupT9Pad();
        setupActionBar();
    }

    private void setupT9Pad() {
        int[] buttonIds = new int[] {
                R.id.one, R.id.two, R.id.three, R.id.four, R.id.five, R.id.six, R.id.seven,
                R.id.eight, R.id.nine, R.id.zero, R.id.delete, R.id.pound
        };
        for (int id : buttonIds) {
            ((DialpadImageButton) findViewById(id)).setOnPressedListener(this);
        }
    }


    private void setupActionBar() {
        findViewById(R.id.btn_exit_keyboard).setOnClickListener(this);
        mClearAll = (ImageButton) findViewById(R.id.btn_clearall);
        mClearAll.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_clearall: {
                clearAllText();
                break;
            }
            case R.id.btn_exit_keyboard: {
                exitSearchMode();
                break;
            }
            case R.id.info_text: {
                unfolderKeyBoard();
                break;
            }
            default: {
                break;
            }
        }
    }

    @Override
    public void onPressed(View v, boolean pressed) {
        Log.d(TAG, "onPressed:" + v.getId() + "," + pressed);
        if (pressed) {
            switch (v.getId()) {
                case R.id.one: {
                    keyPressed(KeyEvent.KEYCODE_1);
                    break;
                }
                case R.id.two: {
                    keyPressed(KeyEvent.KEYCODE_2);
                    break;
                }
                case R.id.three: {
                    keyPressed(KeyEvent.KEYCODE_3);
                    break;
                }
                case R.id.four: {
                    keyPressed(KeyEvent.KEYCODE_4);
                    break;
                }
                case R.id.five: {
                    keyPressed(KeyEvent.KEYCODE_5);
                    break;
                }
                case R.id.six: {
                    keyPressed(KeyEvent.KEYCODE_6);
                    break;
                }
                case R.id.seven: {
                    keyPressed(KeyEvent.KEYCODE_7);
                    break;
                }
                case R.id.eight: {
                    keyPressed(KeyEvent.KEYCODE_8);
                    break;
                }
                case R.id.nine: {
                    keyPressed(KeyEvent.KEYCODE_9);
                    break;
                }
                case R.id.zero: {
                    keyPressed(KeyEvent.KEYCODE_0);
                    break;
                }
                case R.id.pound: {
                    keyPressed(KeyEvent.KEYCODE_POUND);
                    break;
                }
                case R.id.delete: {
                    deleteNumber();
                    break;
                }
                default: {
                    break;
                }
            }
        }
    }

    private void clearAllText() {
        mInfoText.setText("");
        onTextChanged("");
        mClearAll.setEnabled(false);
    }

    private void exitSearchMode() {
        if (mT9DialpadViewListener != null) {
            mT9DialpadViewListener.onExit();
        }
    }

    private void keyPressed(int keyCode) {
        digtalButtonClicked(BUTTON_TAG[keyCode - KeyEvent.KEYCODE_0]);
    }

    private void deleteNumber() {
        if (mInfoText == null || mInfoText.getText().toString().isEmpty()) {
            Log.w(TAG, "digtalButtonClicked: mEditText is null:"+(mInfoText == null));
            return;
        }

        StringBuilder sb = new StringBuilder(mInfoText.getText().toString());
        int index = sb.length();
        sb.deleteCharAt(index - 1);
        String res = sb.toString();
        mInfoText.setText(res);
        mInfoText.setSelection(res.length());
        mClearAll.setEnabled(!res.isEmpty());
        onTextChanged(res);
    }

        private void digtalButtonClicked(String s) {
            if (mInfoText == null) {
                Log.w(TAG, "digtalButtonClicked: mEditText is null.");
                return;
            }

            StringBuilder sb = new StringBuilder();
            sb.append(mInfoText.getText());
            sb.append(s);
            String res = sb.toString();
            mInfoText.setText(res);
            mInfoText.setSelection(res.length());
            mClearAll.setEnabled(!res.isEmpty());
            onTextChanged(res);
        }

        private void onTextChanged(String s) {
            if (mSearchTask == null) {
                mSearchTask = new SearchTask(getContext(), this);
            }

            mSearchTask.doSearch(s);
        }

        @Override
        public void onItemClick(AdapterView<?> arg0, View v, int arg2,
                long arg3) {
            Object tag = v.getTag();
            if (tag instanceof Intent) {
                    getContext().startActivity(((Intent)tag));
            } else if (tag instanceof MatchResult) {
                MatchResult match = (MatchResult)tag;
                if (match.type == LauncherSettings.Favorites.ITEM_TYPE_VPINSTALL) {
                    Toast.makeText(getContext(), R.string.installing, Toast.LENGTH_SHORT).show();
                    installPackage(match.intent2);
                } else {
                    getContext().startActivity((match.intent2));
                }
            }
        }

        public void onExit() {
            mInfoText.setText("");
            mClearAll.setEnabled(false);
            mList.setAdapter(mEmptyMatchedAppsAdapter);

            if (mDiapladViewInAnim != null) {
                mDiapladViewInAnim.cancel();
                mDialpadViewOutAnim.cancel();
                mKeyboard.setY(mKeyboardTop);
            }
        }

        public void onEnter() {
            mKeyboard.setVisibility(VISIBLE);
        }

        @Override
        public void onSearchDone(final List<MatchResult> match) {
            mHandler.post(new Runnable (){

                @Override
                public void run() {
                    MatchedAppsAdapter adapter =
                            new MatchedAppsAdapter(getContext(), match);
                    mList.setAdapter(adapter);
                }});
        }

        @Override
        public void onSlideUp() {
            folderKeyBoard();
        }

        @Override
        public void onSlideDown() {
            unfolderKeyBoard();
        }

        private void folderKeyBoard() {
            if (mDialpadViewOutAnim == null) {
                setupDialpadViewAnimation();
            }

            if (mKeyboard.getVisibility() == VISIBLE) {
                mDialpadViewOutAnim.start();
            }
        }

        private void unfolderKeyBoard() {
            if (mDiapladViewInAnim == null) {
                setupDialpadViewAnimation();
            }

            if (mKeyboard.getVisibility() != VISIBLE) {
                mDiapladViewInAnim.start();
            }
        }

        private void setupDialpadViewAnimation() {
            int windowHeight = LauncherApplication.getScreenHeight();
            mKeyboardTop = mKeyboard.getTop();

            mDiapladViewInAnim = ObjectAnimator.ofFloat(mKeyboard, "Y",
                    windowHeight, mKeyboardTop)
                    .setDuration(DIALPAD_VIEW_ANIMATION_DURATION);

            mDialpadViewOutAnim = ObjectAnimator.ofFloat(mKeyboard, "Y",
                    mKeyboardTop, windowHeight).setDuration(
                    DIALPAD_VIEW_ANIMATION_DURATION);
            mDialpadViewOutAnim.addListener(new AnimatorListenerAdapter() {

                @Override
                public void onAnimationEnd(Animator animation) {
                    mKeyboard.setVisibility(INVISIBLE);
                }

            });
            mDiapladViewInAnim.addListener(new AnimatorListenerAdapter() {

                @Override
                public void onAnimationStart(Animator animation) {
                    mKeyboard.setVisibility(VISIBLE);
                }

            });
        }

        private void installPackage(Intent intent) {
            PackageManager pm = getContext().getPackageManager();
            Uri packageURI = Uri.fromFile(new File(intent.getStringExtra(TYPE_PACKAGEPATH)));
            try {
                installPackage(pm, packageURI, null, 0, intent.getStringExtra(TYPE_PACKAGENAME));
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }

        private static void installPackage(android.content.pm.PackageManager pm, Uri uri, IPackageInstallObserver obs, int flags, String packageName)
                throws ClassNotFoundException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
            Class<?> clz = Class.forName("android.content.pm.PackageManager");
            Class<?>[] paramsType = new Class[4];
            paramsType[0] = Uri.class;
            paramsType[1] = IPackageInstallObserver.class;
            paramsType[2] = int.class;
            paramsType[3] = String.class;

            Method method = clz.getMethod("installPackage", paramsType);
            method.invoke(pm, uri, obs, flags, packageName);
        }

}
