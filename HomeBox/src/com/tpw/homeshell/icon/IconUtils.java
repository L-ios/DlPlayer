package com.tpw.homeshell.icon;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.util.Log;

import com.tpw.homeshell.FastBitmapDrawable;
import com.tpw.homeshell.LauncherApplication;
import com.tpw.homeshell.R;
import com.tpw.icon.IconGenerator;

public class IconUtils {
	private static final String TAG = "IconUtils";
	private static final int THRESHOLD = 180;
	public static final int TITLE_COLOR_WHITE = Color.WHITE;
	public static final int TITLE_COLOR_BLACK = 0xff4c4c4c;
	private static final float WEIGHT_RED = 0.30f;
	private static final float WEIGHT_GREEN = 0.59f;
	private static final float WEIGHT_BLUE = 0.11f;
	//the card icon are ugly, 
	private static Paint sPaintForMask = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);

	static {
	sPaintForMask.setXfermode(new PorterDuffXfermode(Mode.SRC_ATOP));
	}
	
	public static int getTitleColor(Bitmap background){
		Log.d(TAG,"getTitleColor begin");
		long time = System.currentTimeMillis();
		int color = TITLE_COLOR_WHITE;
		int width = background.getWidth();
		int height = background.getHeight()-width;
		int pixels[] = new int[width * height];
		int total = 0;
		int greyvalue = 0;
		int sample;
		int sampleSize = 0;
		int red = 0;
		int green = 0;
		int blue = 0;
		int average = 0;
		background.getPixels(pixels, 0, width, 0, width, width, height);
		for(int i = 0;i<height;i+=2){
			int s = i * width;
			for(int j = 0;j<width;j+=2){
				sample = pixels[s+j];
				red = Color.red(sample);
				green = Color.green(sample);
				blue = Color.blue(sample);
				greyvalue = (int)(red*WEIGHT_RED+green*WEIGHT_GREEN+blue*WEIGHT_BLUE);
				total+=greyvalue;
				sampleSize++;
			}
		}
		
		average = total/sampleSize;
		if(average <= THRESHOLD){
			color = TITLE_COLOR_WHITE;
		}else{
			color = TITLE_COLOR_BLACK;
		}
		
		time =  System.currentTimeMillis() - time;
		Log.d(TAG,"getTitleColor time = "+time);
		Log.d(TAG,"getTitleColor end");
		return color;
	}
	
	public static int getTitleColor(int color){
		int red = 0;
		int green = 0;
		int blue = 0;
		int greyvalue = 0;
		int titlecolor = TITLE_COLOR_WHITE;
		red = Color.red(color);
		green = Color.green(color);
		blue = Color.blue(color);
		greyvalue = (int)(red*WEIGHT_RED+green*WEIGHT_GREEN+blue*WEIGHT_BLUE);
		if(greyvalue <= THRESHOLD){
			titlecolor = TITLE_COLOR_WHITE;
		}else{
			titlecolor = TITLE_COLOR_BLACK;
		}
		return titlecolor;
	}
	
	//the card icon are ugly, 
	public static Drawable createBackgroundIcon(Resources res,Bitmap icon){
            int width = res.getDimensionPixelSize(R.dimen.workspace_cell_width);
            int height = res.getDimensionPixelSize(R.dimen.workspace_cell_height);
            int toppadding = res.getDimensionPixelSize(R.dimen.card_icon_generator_top_padding);
            return createBackgroundIcon(res, icon, width, height, toppadding);
    }

    public static final Bitmap scaleBitmap(Bitmap bitmap) {
        Bitmap scaleBitmap = null;
        Context context = LauncherApplication.getContext();
        int rate = context.getResources().getDimensionPixelSize(R.dimen.bubble_icon_width);
        if (bitmap != null) {
            int width = bitmap.getWidth();
            int height = bitmap.getHeight();
            int newWidth = width;
            int newHeight = height;
            if (height > width) {
                if (height > rate) {
                    newHeight = rate;
                    newWidth = rate * width / height;
                }
            } else {
                if (width > rate) {
                    newWidth = rate;
                    newHeight = rate * height / width;
                }
            }

            float scaleWidth = ((float) newWidth) / width;
            float scaleHeight = ((float) newHeight) / height;
            if ((scaleWidth != 1) || (scaleHeight != 1)) {
                Matrix matrix = new Matrix();
                matrix.postScale(scaleWidth, scaleHeight);

                scaleBitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
            } else {
                scaleBitmap = bitmap;
            }
        }
        return scaleBitmap;
    }

    public static Drawable createBackgroundIcon(Resources res,Bitmap icon, int width, int height, int toppadding){
        Drawable mask = res.getDrawable(R.drawable.card_bg);
        Bitmap originbgicon = getOriginBackgroundIcon(icon, toppadding, width, height);
        if(originbgicon == null){
            return null;
        }
        Bitmap bm = Bitmap.createBitmap(width, height, Config.ARGB_8888);
        Canvas canvas = new Canvas(bm);
        mask.setBounds(0, 0, width, height);

        mask.draw(canvas);
        Rect rect = new Rect(0, 0, width, height);
        canvas.drawBitmap(originbgicon, null, rect, sPaintForMask);
        if(originbgicon!=null && !originbgicon.isRecycled()){
            originbgicon.recycle();
        }
        return new FastBitmapDrawable(bm);
    }

    public static Bitmap drawable2ScaledBitmap(Drawable drawable,
            int newWidth, int newHeight) {
        Rect rect = drawable.getBounds();
        int l = rect.left;
        int r = rect.right;
        int t = rect.top;
        int b = rect.bottom;
        Bitmap bitmap = Bitmap.createBitmap(newWidth, newHeight, Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, newWidth, newHeight);
        drawable.draw(canvas);
        canvas.setBitmap(null);
        drawable.setBounds(l, t, r, b);
        return bitmap;
    }

    public static final Drawable scaleImg(Drawable drawable) {
        if (drawable == null) {
            return null;
        }
        Context context = LauncherApplication.getContext();
        Bitmap bitmap = null;
        if (drawable instanceof FastBitmapDrawable) {
            bitmap = ((FastBitmapDrawable) drawable).getBitmap();
        } else {
            bitmap = ((BitmapDrawable) drawable).getBitmap();
        }
        Bitmap scaleBitmap = null;
        if (bitmap != null) {
            scaleBitmap = IconUtils.scaleBitmap(bitmap);
            if (scaleBitmap != bitmap) {
                bitmap.recycle();
            }
        }
        return new BitmapDrawable(context.getResources(), scaleBitmap);
    }

    // the card icon are ugly, save icon to sdcard icon for test
    public void saveBitmap(String bitName, Bitmap mBitmap) {
        File f = new File(Environment.getExternalStorageDirectory().getPath() + bitName + ".png");
        try {
            f.createNewFile();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            Log.e(TAG, "save bitmap "+ e.toString());
        }
        FileOutputStream fOut = null;
        try {
            fOut = new FileOutputStream(f);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        mBitmap.compress(Bitmap.CompressFormat.PNG, 100, fOut);
        try {
            fOut.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            fOut.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Bitmap drawableToBitmap(Drawable drawable) {
        int width = drawable.getIntrinsicWidth();
        int height = drawable.getIntrinsicHeight();
        Bitmap.Config config = drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888
                : Bitmap.Config.RGB_565;
        Bitmap bitmap = Bitmap.createBitmap(width, height, config);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, width, height);
        drawable.draw(canvas);
        return bitmap;
    }

    public static Bitmap drawableToBitmap(Drawable src, int bitmapWidth, int bitmapHeight) {
        int srcWidth = src.getIntrinsicWidth();
        int srcHeight = src.getIntrinsicHeight();
        Bitmap oldbmp = drawableToBitmap(src);

        int dstWidth = bitmapWidth;
        int dstHeight = bitmapHeight;
        if ((float) srcWidth / bitmapWidth >= (float) srcHeight / bitmapHeight) {
            dstHeight = srcHeight * bitmapWidth / srcWidth;
        } else {
            dstWidth = srcWidth * bitmapHeight / srcHeight;
        }
        float scaleWidth = ((float) dstWidth) / srcWidth;
        float scaleHeight = ((float) dstHeight) / srcHeight;

        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);

        Bitmap newbmp = Bitmap.createBitmap(oldbmp, 0, 0, srcWidth, srcHeight,
                matrix, true);
        if (oldbmp != null && !oldbmp.isRecycled()) {
            oldbmp.recycle();
        }
        return newbmp;
    }

    //homeshell crash for apr test
    static Bitmap getOriginBackgroundIcon(Bitmap icon, int toppadding, int width, int height){
        //long time = System.currentTimeMillis();
        Bitmap bgicon = null;
                int input[] = null;
                int output[] = null;
                int paramter[] = null;
                Bitmap tmpicon = icon;
                try{
                    IconGenerator generator = new IconGenerator();
                    int iconwidth = icon.getWidth();
                    int iconheight = icon.getHeight();
                    //homeshell crashed during prowering on
                    //root cause: The icon is not processed by theme, so the height & width of icon may be very large
                    //solution: scale icon to a small one
                    int size = (width > height) ? width : height;
                    if ((iconheight > size) || (iconwidth > size)) {
                        Log.d(TAG, "iconwidth="+iconwidth+",iconheight="+iconheight);
                        Bitmap scaleBitmap = scaleBitmap(icon);
                        if (scaleBitmap == null) {
                            return null;
                        }
                        iconwidth  = scaleBitmap.getWidth();
                        iconheight = scaleBitmap.getHeight();
                        tmpicon = scaleBitmap;
                    }
                    input = new int[iconwidth*iconheight];
                    tmpicon.getPixels(input, 0, iconwidth, 0, 0, iconwidth, iconheight);
                    output = new int[width * height];
                    paramter = new int[5];
                    paramter[0] = iconwidth;
                    paramter[1] = iconheight;
                    paramter[2]  = toppadding;
                    paramter[3]  = width;
                    paramter[4]  = height;
                    Log.d(TAG, "smallWidth="+paramter[0]+",smallHeight="+paramter[1]+",topPadding="+paramter[2]+",bigwidth="+paramter[3]+",bigheight="+paramter[4]);
                    //long preparetime = System.currentTimeMillis() - time;
                    //long time1 = System.currentTimeMillis();
                    generator.generator(input, paramter, output);
                    //long jnitime = System.currentTimeMillis() - time1;
                    //long time2 = System.currentTimeMillis();
                    bgicon = Bitmap.createBitmap(output, width, height, Config.ARGB_8888);
                }catch(Throwable e){
                    bgicon = null;
                }finally{
                    if(input != null){
                        input = null;
                    }
                    if(output != null){
                        output = null;
                    }
                    if(paramter != null){
                        paramter = null;
                    }
                }
        //long createtime = System.currentTimeMillis() - time2;
        //Log.e("vqx376","getOriginBackgroundIcon : preparetime = "+preparetime+" --- jnitime = "+jnitime + " --- createbmptime = "+createtime);
        return bgicon;
    }
}
