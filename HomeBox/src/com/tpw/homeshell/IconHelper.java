
package com.tpw.homeshell;

import android.content.ComponentName;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class IconHelper {
    private static final String TAG = "IconHelper";

    private static final int ALPHA_MIN = 230;

    private static final int ALPHA_ATOP = 200;

    private static final float DARKEN_LEVEL = 1.15f;

    private static HashMap<String, String> sIconMapping = new HashMap<String, String>();

    private static final HashMap<String, Integer> sColorMapping = new HashMap<String, Integer>();

    static {
        sColorMapping.put("com.android.calendar", Color.WHITE);
        sColorMapping.put("com.android.settings", 0xff758a99);
        sColorMapping.put("com.yunos.theme.thememanager", 0xff4d279a);
        sColorMapping.put("com.tpw.SecurityCenter", 0xff00c732);
        sColorMapping.put("com.taobao.reader", 0xffffe494);
        sColorMapping.put("com.duomi.yunos", 0xffff8200);
        sColorMapping.put("fm.xiami.yunos", 0xffff8200);
        sColorMapping.put("com.xiami.walkman", 0xffff8200);
        sColorMapping.put("com.tpw.mobile.email", 0xffffbf00);
        sColorMapping.put("com.tpw.video", 0xff5a0eb8);
        sColorMapping.put("com.yunos.gamestore", 0xff9112c7);
        sColorMapping.put("com.mediatek.FMRadio", 0xffff8600);
        sColorMapping.put("xcxin.filexperttpw", 0xffffbb45);
        sColorMapping.put("com.yunos.camera", 0xff353535);
        sColorMapping.put("com.android.calculator2", 0xff7e9498);
        sColorMapping.put("com.tpw.wireless.vos.appstore", 0xffff284c);
        sColorMapping.put("com.android.alarmclock", 0xff353535);
        sColorMapping.put("com.tpw.image.app.Gallery", 0xff00be00);
        sColorMapping.put("com.yunos.alicontacts.sim.operator.LoadSimContactsService", 0xff00be00);
        sColorMapping.put("com.yunos.alicontacts.activities.DialtactsActivity", 0xff00be00);
    }

    public static final int getCardColor(ComponentName cn) {
        if (cn != null)
            for (String name : getIconNames(cn.getPackageName(), cn.getClassName())) {
                Integer color = sColorMapping.get(name);
                if (color != null)
                    return color;
            }
        return 0;
    }

    private static List<String> getIconNames(String packageName, String className) {
        ArrayList<String> paths = new ArrayList<String>();
        if (packageName == null)
            return paths;
        String fileName = getFileName(packageName, className);
        String mappingName = sIconMapping.get(fileName);
        if (mappingName != null)
            paths.add(mappingName);
        paths.add(fileName);
        if (className != null) {
            if (!className.startsWith(packageName))
                paths.add(className);
        }
        paths.add(packageName);
        mappingName = sIconMapping.get(packageName);
        if (mappingName != null)
            paths.add(mappingName);
        return paths;
    }

    private static String getFileName(String packageName, String className) {
        if (className == null)
            return packageName;
        if (className.startsWith(packageName))
            return className;
        else
            return packageName + '#' + className;
    }

    public static final int getCardColor(Bitmap bmp) {
        try {
            int baseWidth = bmp.getWidth();
            int baseHeight = bmp.getHeight();
            int basePixels[] = new int[baseWidth * baseHeight];
            bmp.getPixels(basePixels, 0, baseWidth, 0, 0, baseWidth, baseHeight);
            return getAverageColor(baseWidth, baseHeight, basePixels);
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        } catch (OutOfMemoryError e) {
            Log.e(TAG, e.toString());
        }
        return 0;
    }

    private static int getAverageColor(int height, int width, int[] pixels) {

        // Alpha range is 0...255
        final int minAlpha = ALPHA_MIN;

        final int stride = 10;

        // Saturation range is 0...1
        float minSaturation = 0.2f;

        // Number of pixels to sample
        int hSamples = width / stride;
        int vSamples = height / stride;

        // Holds temporary sum of HSV values
        float[] sampleTotals = {
                0, 0, 0
        };

        // Loop through pixels horizontally
        float[] hsv = new float[3];
        int sample;
        int sampleSize = 0;
        for (int j = vSamples, sV = height / vSamples; j < height; j += sV) {
            // Loop through pixels horizontal
            int s = j * width;
            for (int i = hSamples, sH = width / hSamples; i < width; i += sH) {
                // Get pixel & convert to HSV format
                sample = pixels[s + i];
                // Check pixel matches criteria to be included in sample
                if ((Color.alpha(sample) > minAlpha)) {
                    Color.colorToHSV(sample, hsv);
                    if (hsv[1] >= minSaturation) {
                        // Add sample values to total
                        sampleTotals[0] += hsv[0]; // H
                        sampleTotals[1] += hsv[1]; // S
                        sampleTotals[2] += hsv[2]; // V
                        sampleSize++;
                    }
                }
            }
        }
        if (sampleSize == 0)
            return Color.TRANSPARENT;

        sampleTotals[0] /= sampleSize;
        sampleTotals[1] /= sampleSize;
        sampleTotals[2] /= sampleSize;
        sampleTotals[1] /= DARKEN_LEVEL;
        sampleTotals[2] /= DARKEN_LEVEL;

        // Return average tuplet as RGB color
        return Color.HSVToColor(ALPHA_ATOP, sampleTotals);
    }
}
