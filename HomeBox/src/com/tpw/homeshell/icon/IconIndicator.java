package com.tpw.homeshell.icon;

import java.util.HashMap;
import java.util.Map;

import android.content.ComponentName;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.text.TextUtils;
import android.util.Pair;

import com.tpw.homeshell.AgedModeUtil;
import com.tpw.homeshell.CellLayout.Mode;
import com.tpw.homeshell.R;
import com.tpw.homeshell.ShortcutInfo;

/**
 * The small image indicator on the left side of the title in
 * <code>BubbleTextView</code>. Currently we have "new" indicator
 * and "flip card" indicator.
 */
public final class IconIndicator {

    private static boolean initialized = false;

    private static Bitmap sImgNewIndicator;
    private static Bitmap sImgCardIndicatorWhite;
    private static Bitmap sImgCardIndicatorBlack;
    private static int sImgIndicatorPadding; // the padding between indicator and text

    public static void init(Resources res) {
        if (!initialized) {
            sImgNewIndicator = BitmapFactory.decodeResource(res, R.drawable.ic_new_normal);
            sImgCardIndicatorWhite = IconIndicator.createCardIndicator(res, IconUtils.TITLE_COLOR_WHITE);
            sImgCardIndicatorBlack = IconIndicator.createCardIndicator(res, IconUtils.TITLE_COLOR_BLACK);
            sImgIndicatorPadding = res.getDimensionPixelSize(R.dimen.ic_indicator_padding);
            initialized = true;
        }
    }

    public static int getIndicatorPadding() {
        return sImgIndicatorPadding;
    }

    public static Bitmap getNewIndicatorImage() {
        return sImgNewIndicator;
    }

    public static Bitmap getCardIndicatorWhiteImage() {
        return sImgCardIndicatorWhite;
    }

    public static Bitmap getCardIndicatorBlackImage() {
        return sImgCardIndicatorBlack;
    }

    // ad-hoc indicator position
    private static final Map<String, Pair<Float, Float>> sAdhocIndicatorPositionMap;
    static {
        sAdhocIndicatorPositionMap = new HashMap<String, Pair<Float,Float>>();
        sAdhocIndicatorPositionMap.put("com.yunos.weatherservice", new Pair<Float, Float>(0.22f, 0.83f));
        sAdhocIndicatorPositionMap.put("com.android.calendar", new Pair<Float, Float>(0.5f, 0.89f));
    }

    /**
     * Retrieves the ad-hoc indicator position for fancy icon (e.g. the weather
     * and calendar app). If the specified <code>icon</code> do not need special
     * indicator layout currently, returns <code>null</code>. Otherwise, returns
     * a pair of float values that represent the percentage of x and y axis of
     * the entire icon.
     * @param icon
     * @return the (x, y) position in percentage or {@code null}
     */
    public static Pair<Float, Float> getAdhocIndicatorPosition(BubbleTextView icon){
        if (icon == null) return null;
        if (!icon.isSupportCard()) return null;
        if (!TextUtils.isEmpty(icon.getText())) return null;
        if (!(icon.getTag() instanceof ShortcutInfo)) return null;
        if (icon.getMode() != Mode.NORMAL &&
            !(AgedModeUtil.isAgedMode() && icon.getMode() == Mode.HOTSEAT)) {
            return null;
        }
        ShortcutInfo item = (ShortcutInfo) icon.getTag();
        ComponentName cmpt = item.intent != null ? item.intent.getComponent() : null;
        String packageName = cmpt != null ? cmpt.getPackageName() : null;
        if (TextUtils.isEmpty(packageName)) return null;

        Pair<Float, Float> value = sAdhocIndicatorPositionMap.get(packageName);
        if (value != null) {
            return new Pair<Float, Float>(value.first, value.second);
        } else {
            return null;
        }
    }

    private static Bitmap createBitmapWithColor(Bitmap original, int color) {
        // The original bitmap is treated as a template image, and replace the opaque
        // pixels with specified color.
        Bitmap result = Bitmap.createBitmap(original.getWidth(), original.getHeight(), Config.ARGB_8888);
        Canvas canvas = new Canvas(result);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(color);
        canvas.drawRect(0, 0, original.getWidth(), original.getHeight(), paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
        canvas.drawBitmap(original, 0, 0, paint);
        paint.setXfermode(null);
        return result;
    }

    private static Bitmap createCardIndicator(Resources res, int color) {
        if (color == IconUtils.TITLE_COLOR_WHITE) {
            return BitmapFactory.decodeResource(res, R.drawable.ic_card_indicator_light);
        } else if (color == IconUtils.TITLE_COLOR_BLACK) {
            return BitmapFactory.decodeResource(res, R.drawable.ic_card_indicator_dark);
        } else {
            return null;
        }
    }

    /* Instance Members */

    // new or card indicator, or null if currently no indicator.
    private Bitmap mImgCurrentIndicator;

    public IconIndicator() {
        mImgCurrentIndicator = null;
    }

    public boolean hasIndicator() {
        return mImgCurrentIndicator != null;
    }

    public Bitmap getCurrentIndicator() {
        return mImgCurrentIndicator;
    }

    /**
     * The current indicator can be set to {@link #getNewIndicatorImage()},
     * {@link #getCardIndicatorWhiteImage()}, {@link #getCardIndicatorBlackImage()},
     * or <code>null</code>.
     * @param bitmap
     * @return whether the indicator is changed or not
     */
    public boolean setCurrentIndicator(Bitmap bitmap) {
        if (this.mImgCurrentIndicator != bitmap) {
            this.mImgCurrentIndicator = bitmap;
            return true;
        } else {
            return false;
        }
    }
}
