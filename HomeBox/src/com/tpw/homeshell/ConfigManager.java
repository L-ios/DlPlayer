package com.tpw.homeshell;

import com.tpw.homeshell.icon.IconManager;
import com.tpw.homeshell.model.LauncherModel;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.util.Log;

public class ConfigManager {
    private static final String TAG = "ConfigManager";
    private static Context mContext;

    private static final String CONFIG_KEY = "com.tpw.homeshell.config";

    // SharedPreferences keys
    private static final String EXIST_KEY = "exist";
    private static final String VPINSTALL_ENABLE_KEY = "vpinstall_enable";
    private static final String SERVICECENTER_ENABLE_KEY = "servicecenter_enable";
    private static final String ICONCARDBG_ENABLE_KEY = "iconcardbg_enable";
    private static final String SCREEN_MAX_COUNT_KEY = "screen_max_count";
    private static final String CELL_COUNT_X_KEY = "cell_count_x";
    private static final String CELL_COUNT_Y_KEY = "cell_count_y";
    private static final String FOLDER_MAX_COUNT_X_KEY = "folder_max_count_x";
    private static final String FOLDER_MAX_COUNT_Y_KEY = "folder_max_count_y";
    private static final String HOTSEAT_MAX_COUNT_X_KEY = "hotseat_max_count_x";
    private static final String HOTSEAT_MAX_COUNT_Y_KEY = "hotseat_max_count_y";
    private static final String HOTSEAT_MAX_COUNT_KEY = "hotseat_max_count";
    private static final String HIDESEAT_MAX_SCREEN_COUNT_KEY = "hideseat_max_screen_count";
    private static final String HIDESEAT_MAX_COUNT_X_KEY = "hideseat_max_count_x";
    private static final String HIDESEAT_MAX_COUNT_Y_KEY = "hideseat_max_count_y";
    private static final String CELL_MAX_COUNT_X_KEY = "cell_max_count_x";
    private static final String CELL_MAX_COUNT_Y_KEY = "cell_max_count_y";

    // Screen
    //public static final int DEFAULT_HOME_SCREEN_INDEX = 2;
    // modify by huangxunwan for config the homeshell123456 default page
    public static final int DEFAULT_FIND_EMPTY_SCREEN_START = getDefaultScreen();//modified by qinjinchuan topwise for topwise widget configuration
    public static final int DEFAULT_CELL_COUNT_X = 4;
    public static final int DEFAULT_CELL_COUNT_Y = 5;
    private static final int DEFAULT_SCREEN_MAX_COUNT = 18;

    public static final int DEFAULT_CELL_MAX_COUNT_X = 4;
    public static final int DEFAULT_CELL_MAX_COUNT_Y = 5;

    // Folder
    private static final int DEFAULT_FOLDER_MAX_COUNT_X = 3;
    private static final int DEFAULT_FOLDER_MAX_COUNT_Y = 4;
    private static final int CARD_FOLDER_MAX_COUNT_Y = 3;
        // for 3*3 layout
    private static final int FOLDER_MAX_COUNT_Y_AGED_MODE = 3;
    private static final int DEFAULT_FOLDER_ITEMS_MAX_COUNT = DEFAULT_FOLDER_MAX_COUNT_X * DEFAULT_FOLDER_MAX_COUNT_Y;
    private static final int CARD_FOLER_ITEMS_MAX_COUNT = DEFAULT_FOLDER_MAX_COUNT_X
            * CARD_FOLDER_MAX_COUNT_Y;
    // Hotseat
    private static final int DEFAULT_HOTSEAT_MAX_COUNT_X = 5;
    private static final int DEFAULT_HOTSEAT_MAX_COUNT_Y = 1;
    private static final int DEFAULT_HOTSEAT_MAX_COUNT = DEFAULT_HOTSEAT_MAX_COUNT_X * DEFAULT_HOTSEAT_MAX_COUNT_Y;

    // Hideseat
    // Hide-seat has increased maximum screen number from 3 to 6.
    // Note that some hard-coded numbers should be also modified at same time when
    // this value changed. (search for "HIDESEAT_SCREEN_NUM_MARKER" in the entire project)
    public static final int DEFAULT_HIDESEAT_SCREEN_MAX_COUNT = 6;
    public static final int DEFAULT_HIDESEAT_MAX_COUNT_X = 4;
    public static final int DEFAULT_HIDESEAT_MAX_COUNT_Y = 1;
    private static final int DEFAULT_HIDESEAT_ITEMS_MAX_COUNT = DEFAULT_HIDESEAT_SCREEN_MAX_COUNT * DEFAULT_HIDESEAT_MAX_COUNT_X * DEFAULT_HIDESEAT_MAX_COUNT_Y;

    private static boolean sIsVPInstallEnable = false;
    private static boolean sIsServiceCenterEnable = false;
    private static boolean sIsIconCardBGEnable = false;
    // Screen
    private static int sScreenMaxCount = DEFAULT_SCREEN_MAX_COUNT;
    private static int sCellCountX = DEFAULT_CELL_COUNT_X;
    private static int sCellCountY = DEFAULT_CELL_COUNT_Y;
    private static int sCellMaxCountX = DEFAULT_CELL_MAX_COUNT_X;
    private static int sCellMaxCountY = DEFAULT_CELL_MAX_COUNT_Y;
    // Folder
    private static int sFolderMaxCountX = DEFAULT_FOLDER_MAX_COUNT_X;
    private static int sFolderMaxCountY = DEFAULT_FOLDER_MAX_COUNT_Y;
    private static int sFolderItemsMaxCount = DEFAULT_FOLDER_ITEMS_MAX_COUNT;
    // Hotseat
    private static int sHotseatMaxCountX = DEFAULT_HOTSEAT_MAX_COUNT_X;
    private static int sHotseatMaxCountY = DEFAULT_HOTSEAT_MAX_COUNT_Y;
    private static int sHotseatMaxCount = DEFAULT_HOTSEAT_MAX_COUNT;
    // Hideseat
    private static int sHideseatScreenMaxCount = DEFAULT_HIDESEAT_SCREEN_MAX_COUNT;
    private static int sHideseatMaxCountX = DEFAULT_HIDESEAT_MAX_COUNT_X;
    private static int sHideseatMaxCountY = DEFAULT_HIDESEAT_MAX_COUNT_Y;
    private static int sHideseatItemsMaxCount = DEFAULT_HIDESEAT_ITEMS_MAX_COUNT;

    private static int sHideseatHeight;

    private ConfigManager(){
    }

    public static void init() {
        mContext = LauncherApplication.getContext();
        // load the params neednot from sharedprefer
        if (AgedModeUtil.isAgedMode()) {
            sHideseatHeight = mContext.getResources().getDimensionPixelSize(R.dimen.workspace_cell_height_3_3);
        } else {
            sHideseatHeight = mContext.getResources().getDimensionPixelSize(
                    R.dimen.button_bar_height_plus_padding);
        }
        SharedPreferences sp = mContext.getSharedPreferences(CONFIG_KEY, Context.MODE_PRIVATE);
        if (sp.contains(EXIST_KEY) == false) {
            getAndSaveDefaultConfig(sp);
            Log.d(TAG, "init(): Save config complete.");
        } else {
            getAllConfigs(sp);
            Log.d(TAG, "init(): Get config complete.");
        }
        Log.d(TAG, "init(): " + toLogString());
    }

    public static void reset() {
        Log.d(TAG, "reset()");
        mContext = LauncherApplication.getContext();
        SharedPreferences sp = mContext.getSharedPreferences(CONFIG_KEY, Context.MODE_PRIVATE);
        getAndSaveDefaultConfig(sp);
        Log.d(TAG, "reset(): Reset config complete. " + toLogString());
    }

    private static void getAndSaveDefaultConfig(SharedPreferences sharedPreferences) {
        // Config shared preference xml isn't created, get default shared preferences from default config.xml,
        // and create config shared preference xml.
        // Move the configurations from res/xml/default_config.xml to res/values/config.xml
        sIsVPInstallEnable = mContext.getResources().getBoolean(R.bool.vpinstall_enable);
        sIsServiceCenterEnable = mContext.getResources().getBoolean(R.bool.service_center_enable);
        sIsIconCardBGEnable = mContext.getResources().getBoolean(R.bool.icon_cardbg_enable);
        // Screen
        sScreenMaxCount = mContext.getResources().getInteger(R.integer.screen_max_count);
        sCellCountX = mContext.getResources().getInteger(R.integer.cell_count_x);
        sCellCountY = mContext.getResources().getInteger(R.integer.cell_count_y);
        sCellMaxCountX = mContext.getResources().getInteger(R.integer.cell_max_count_x);
        sCellMaxCountY = mContext.getResources().getInteger(R.integer.cell_max_count_y);
        // Folder
        sFolderMaxCountX = mContext.getResources().getInteger(R.integer.folder_max_count_x);
        sFolderMaxCountY = mContext.getResources().getInteger(R.integer.folder_max_count_y);
        sFolderItemsMaxCount = sFolderMaxCountX * sFolderMaxCountY;

        // Hideseat Hotseat
        if (AgedModeUtil.isAgedMode()) {
            sHotseatMaxCountX = mContext.getResources().getInteger(
                    R.integer.hotseat_max_count_x_aged_mode);
            sHideseatScreenMaxCount = mContext.getResources().getInteger(R.integer.hideseat_screen_max_count_3_3);
            sHideseatMaxCountX = mContext.getResources().getInteger(
                    R.integer.hideseat_max_count_x_3_3);
        } else {
            sHotseatMaxCountX = mContext.getResources().getInteger(R.integer.hotseat_max_count_x);
            sHideseatScreenMaxCount = mContext.getResources().getInteger(R.integer.hideseat_screen_max_count);
            sHideseatMaxCountX = mContext.getResources().getInteger(R.integer.hideseat_max_count_x);
        }
        sHotseatMaxCountY = mContext.getResources().getInteger(R.integer.hotseat_max_count_y);
        sHotseatMaxCount = sHotseatMaxCountX * sHotseatMaxCountY;

        sHideseatMaxCountY = mContext.getResources().getInteger(R.integer.hideseat_max_count_y);
        sHideseatItemsMaxCount = sHideseatScreenMaxCount * sHideseatMaxCountX * sHideseatMaxCountY;

        Editor editor = sharedPreferences.edit();
        editor.putBoolean(EXIST_KEY, true);
        editor.putBoolean(VPINSTALL_ENABLE_KEY, sIsVPInstallEnable);
        editor.putBoolean(SERVICECENTER_ENABLE_KEY, sIsServiceCenterEnable);
        editor.putBoolean(ICONCARDBG_ENABLE_KEY, sIsIconCardBGEnable);
        // Screen
        editor.putInt(SCREEN_MAX_COUNT_KEY, sScreenMaxCount);
        editor.putInt(CELL_COUNT_X_KEY, sCellCountX);
        editor.putInt(CELL_COUNT_Y_KEY, sCellCountY);
        editor.putInt(CELL_MAX_COUNT_X_KEY, sCellMaxCountX);
        editor.putInt(CELL_MAX_COUNT_Y_KEY, sCellMaxCountY);
        // Folder
        editor.putInt(FOLDER_MAX_COUNT_X_KEY, sFolderMaxCountX);
        editor.putInt(FOLDER_MAX_COUNT_Y_KEY, sFolderMaxCountY);
        // Hotseat
        editor.putInt(HOTSEAT_MAX_COUNT_X_KEY, sHotseatMaxCountX);
        editor.putInt(HOTSEAT_MAX_COUNT_Y_KEY, sHotseatMaxCountY);
        // Hideseat
        editor.putInt(HIDESEAT_MAX_SCREEN_COUNT_KEY, sHideseatScreenMaxCount);
        editor.putInt(HIDESEAT_MAX_COUNT_X_KEY, sHideseatMaxCountX);
        editor.putInt(HIDESEAT_MAX_COUNT_Y_KEY, sHideseatMaxCountY);

        editor.commit();
    }

    private static void getAllConfigs(SharedPreferences sharedPreferences) {
        sIsVPInstallEnable = sharedPreferences.getBoolean(VPINSTALL_ENABLE_KEY, false);
        sIsServiceCenterEnable = sharedPreferences.getBoolean(SERVICECENTER_ENABLE_KEY, true);
        sIsIconCardBGEnable = sharedPreferences.getBoolean(ICONCARDBG_ENABLE_KEY, true);
        // Screen
        sScreenMaxCount = sharedPreferences.getInt(SCREEN_MAX_COUNT_KEY, DEFAULT_SCREEN_MAX_COUNT);
        sCellCountX = sharedPreferences.getInt(CELL_COUNT_X_KEY, DEFAULT_CELL_COUNT_X);
        sCellCountY = sharedPreferences.getInt(CELL_COUNT_Y_KEY, DEFAULT_CELL_COUNT_Y);
        sCellMaxCountX = sharedPreferences.getInt(CELL_MAX_COUNT_X_KEY, DEFAULT_CELL_MAX_COUNT_X);
        sCellMaxCountY = sharedPreferences.getInt(CELL_MAX_COUNT_Y_KEY, DEFAULT_CELL_MAX_COUNT_Y);
        // Folder
        //sFolderMaxCountX = sharedPreferences.getInt(FOLDER_MAX_COUNT_X_KEY, DEFAULT_FOLDER_MAX_COUNT_X);
        //sFolderMaxCountY = sharedPreferences.getInt(FOLDER_MAX_COUNT_Y_KEY, DEFAULT_FOLDER_MAX_COUNT_Y);
        sFolderMaxCountX = mContext.getResources().getInteger(R.integer.folder_max_count_x);
        sFolderMaxCountY = mContext.getResources().getInteger(R.integer.folder_max_count_y);
        sFolderItemsMaxCount = sFolderMaxCountX * sFolderMaxCountY;
        // Hotseat
        sHotseatMaxCountX = sharedPreferences.getInt(HOTSEAT_MAX_COUNT_X_KEY, DEFAULT_HOTSEAT_MAX_COUNT_X);
        sHotseatMaxCountY = sharedPreferences.getInt(HOTSEAT_MAX_COUNT_Y_KEY, DEFAULT_HOTSEAT_MAX_COUNT_Y);
        sHotseatMaxCount = sHotseatMaxCountX * sHotseatMaxCountY;
        // Hideseat
        // HideseatScreenMaxCount was changed from 3 to 6
        //sHideseatScreenMaxCount = sharedPreferences.getInt(HIDESEAT_MAX_SCREEN_COUNT_KEY, DEFAULT_HIDESEAT_SCREEN_MAX_COUNT);
        if (AgedModeUtil.isAgedMode()) {
            sHideseatScreenMaxCount = mContext.getResources().getInteger(
                    R.integer.hideseat_screen_max_count_3_3);
            sHideseatMaxCountX = mContext.getResources().getInteger(
                    R.integer.hideseat_max_count_x_3_3);
        } else {
            sHideseatScreenMaxCount = mContext.getResources().getInteger(
                    R.integer.hideseat_screen_max_count);
            sHideseatMaxCountX = mContext.getResources().getInteger(
                    R.integer.hideseat_max_count_x);
        }
        // sHideseatMaxCountX =
        // sharedPreferences.getInt(HIDESEAT_MAX_COUNT_X_KEY,
        // DEFAULT_HIDESEAT_MAX_COUNT_X);
        sHideseatMaxCountY = sharedPreferences.getInt(HIDESEAT_MAX_COUNT_Y_KEY, DEFAULT_HIDESEAT_MAX_COUNT_Y);
        sHideseatItemsMaxCount = sHideseatScreenMaxCount * sHideseatMaxCountX * sHideseatMaxCountY;
    }

    // below functions are all config items get and set interface
    // vp install enable
    public static boolean isVPInstallEnable() {
        return sIsVPInstallEnable;
    }

    public static void setVPInstallEnable(boolean isVPInstallEnable) {
        sIsVPInstallEnable = isVPInstallEnable;
        SharedPreferences sp = mContext.getSharedPreferences(CONFIG_KEY, Context.MODE_PRIVATE);
        sp.edit().putBoolean(VPINSTALL_ENABLE_KEY, sIsVPInstallEnable).commit();
    }

    // service center enable
    public static boolean isServiceCenterEnable() {
        return sIsServiceCenterEnable;
    }

    public static void setServiceCenterEnable(boolean isServiceCenterEnable) {
        sIsServiceCenterEnable = isServiceCenterEnable;
        SharedPreferences sp = mContext.getSharedPreferences(CONFIG_KEY, Context.MODE_PRIVATE);
        sp.edit().putBoolean(SERVICECENTER_ENABLE_KEY, sIsServiceCenterEnable).commit();
    }

    // icon card background enable
    public static boolean isIconCardBGEnable() {
        return sIsIconCardBGEnable;
    }

    public static void setIconCardBGEnable(boolean isIconCardBGEnable) {
        sIsIconCardBGEnable = isIconCardBGEnable;
        SharedPreferences sp = mContext.getSharedPreferences(CONFIG_KEY, Context.MODE_PRIVATE);
        sp.edit().putBoolean(ICONCARDBG_ENABLE_KEY, sIsIconCardBGEnable).commit();
    }

    // Screen
    public static int getScreenMaxCount() {
        return sScreenMaxCount;
    }

    public static void setScreenMaxCount(int screenMaxCount) {
        sScreenMaxCount = screenMaxCount;
        SharedPreferences sp = mContext.getSharedPreferences(CONFIG_KEY, Context.MODE_PRIVATE);
        sp.edit().putInt(SCREEN_MAX_COUNT_KEY, sScreenMaxCount).commit();
    }

    public static int getCellCountX() {
        return sCellCountX;
    }

    public static void setCellCountX(int cellCountX) {
        sCellCountX = cellCountX;
        SharedPreferences sp = mContext.getSharedPreferences(CONFIG_KEY, Context.MODE_PRIVATE);
        sp.edit().putInt(CELL_COUNT_X_KEY, sCellCountX).commit();
    }

    public static int getCellCountY() {
        return sCellCountY;
    }

    public static void setCellCountY(int cellCountY) {
        sCellCountY = cellCountY;
        SharedPreferences sp = mContext.getSharedPreferences(CONFIG_KEY, Context.MODE_PRIVATE);
        sp.edit().putInt(CELL_COUNT_Y_KEY, sCellCountY).commit();
    }

    public static int getCellMaxCountX() {
        return sCellMaxCountX;
    }

    public static void setCellMaxCountX(int cellMaxCountX) {
        sCellMaxCountX = cellMaxCountX;
        SharedPreferences sp = mContext.getSharedPreferences(CONFIG_KEY, Context.MODE_PRIVATE);
        sp.edit().putInt(CELL_MAX_COUNT_X_KEY, sCellMaxCountX).commit();
    }

    public static int getCellMaxCountY() {
        return sCellMaxCountY;
    }

    public static void setCellMaxCountY(int cellMaxCountY) {
        sCellMaxCountY = cellMaxCountY;
        SharedPreferences sp = mContext.getSharedPreferences(CONFIG_KEY, Context.MODE_PRIVATE);
        sp.edit().putInt(CELL_MAX_COUNT_Y_KEY, sCellMaxCountY).commit();
    }

    // Folder
    public static int getFolderMaxCountX() {
        return sFolderMaxCountX;
    }

    public static void setFolderMaxCountX(int folderMaxCountX) {
        sFolderMaxCountX = folderMaxCountX;
        sFolderItemsMaxCount = sFolderMaxCountX * sFolderMaxCountY;
        SharedPreferences sp = mContext.getSharedPreferences(CONFIG_KEY, Context.MODE_PRIVATE);
        sp.edit().putInt(FOLDER_MAX_COUNT_X_KEY, sFolderMaxCountX).commit();
    }

    public static int getFolderMaxCountY() {
        // for 3*3 layout
        IconManager im = ((LauncherApplication) LauncherApplication.getContext()).getIconManager();
        if (im == null) {
            im = new IconManager(mContext);
        }
        if (AgedModeUtil.isAgedMode()) {
            return FOLDER_MAX_COUNT_Y_AGED_MODE;
        } else if (im.supprtCardIcon()) {
            return CARD_FOLDER_MAX_COUNT_Y;
        } else {
            return sFolderMaxCountY;
        }
    }

    public static void setFolderMaxCountY(int FolderMaxCountY) {
        sFolderMaxCountY = FolderMaxCountY;
        sFolderItemsMaxCount = sFolderMaxCountX * sFolderMaxCountY;
        SharedPreferences sp = mContext.getSharedPreferences(CONFIG_KEY, Context.MODE_PRIVATE);
        sp.edit().putInt(FOLDER_MAX_COUNT_Y_KEY, sFolderMaxCountY).commit();
    }

    public static int getFolderMaxItemsCount() {
        return sFolderItemsMaxCount;
    }

    // Hotseat
    public static int getHotseatMaxCountX() {
        return sHotseatMaxCountX;
    }

    public static void setHotseatMaxCountX(int hotseatMaxCountX) {
        sHotseatMaxCountX = hotseatMaxCountX;
        SharedPreferences sp = mContext.getSharedPreferences(CONFIG_KEY, Context.MODE_PRIVATE);
        sp.edit().putInt(HOTSEAT_MAX_COUNT_X_KEY, sHotseatMaxCountX).commit();
        setHotseatMaxCount(sHotseatMaxCountX * sHotseatMaxCountY);
    }

    public static int getHotseatMaxCountY() {
        return sHotseatMaxCountY;
    }

    public static void setHotseatMaxCountY(int hotseatMaxCountY) {
        sHotseatMaxCountY = hotseatMaxCountY;
        SharedPreferences sp = mContext.getSharedPreferences(CONFIG_KEY, Context.MODE_PRIVATE);
        sp.edit().putInt(HOTSEAT_MAX_COUNT_Y_KEY, sHotseatMaxCountY).commit();
        setHotseatMaxCount(sHotseatMaxCountX * sHotseatMaxCountY);
    }

    public static int getHotseatMaxCount() {
        return sHotseatMaxCount;
    }

    public static void setHotseatMaxCount(int hotseatMaxCount) {
        sHotseatMaxCount = hotseatMaxCount;
        SharedPreferences sp = mContext.getSharedPreferences(CONFIG_KEY, Context.MODE_PRIVATE);
        sp.edit().putInt(HOTSEAT_MAX_COUNT_KEY, sHotseatMaxCount).commit();
    }

    // Hideseat
    public static int getHideseatScreenMaxCount() {
        return sHideseatScreenMaxCount;
    }

    public static void setHideseatScreenMaxCount(int hideseatScreenMaxCount) {
        sHideseatScreenMaxCount = hideseatScreenMaxCount;
        sHideseatItemsMaxCount = sHideseatScreenMaxCount * sHideseatMaxCountX * sHideseatMaxCountY;
        SharedPreferences sp = mContext.getSharedPreferences(CONFIG_KEY, Context.MODE_PRIVATE);
        sp.edit().putInt(HIDESEAT_MAX_SCREEN_COUNT_KEY, sHideseatScreenMaxCount).commit();
    }

    public static int getHideseatMaxCountX() {
        return sHideseatMaxCountX;
    }

    public static void setHideseatMaxCountX(int hideseatMaxCountX) {
        sHideseatMaxCountX = hideseatMaxCountX;
        sHideseatItemsMaxCount = sHideseatScreenMaxCount * sHideseatMaxCountX * sHideseatMaxCountY;
        SharedPreferences sp = mContext.getSharedPreferences(CONFIG_KEY, Context.MODE_PRIVATE);
        sp.edit().putInt(HIDESEAT_MAX_COUNT_X_KEY, sHideseatMaxCountX).commit();
    }

    public static int getHideseatMaxCountY() {
        return sHideseatMaxCountY;
    }

    public static void setHideseatMaxCountY(int hideseatMaxCountY) {
        sHideseatMaxCountY = hideseatMaxCountY;
        sHideseatItemsMaxCount = sHideseatScreenMaxCount * sHideseatMaxCountX * sHideseatMaxCountY;
        SharedPreferences sp = mContext.getSharedPreferences(CONFIG_KEY, Context.MODE_PRIVATE);
        sp.edit().putInt(HIDESEAT_MAX_COUNT_Y_KEY, sHideseatMaxCountY).commit();
    }

    public static int getHideseatItemsMaxCount() {
        return sHideseatItemsMaxCount;
    }

    public static int getHideseatHeight() {
        return sHideseatHeight;
    }

    public static void setHideseatHeight(int hideseatHeight) {
        sHideseatHeight = hideseatHeight;
    }

    public static int getCellLayoutPaddingTop() {
        int resId = -1;
        int countY = LauncherModel.getCellCountY();
        switch (countY) {
        case 4:
            resId = R.dimen.screen_celllayout_paddingtop_4_4;
            break;
        case 5:
            resId = R.dimen.screen_celllayout_paddingtop_4_5;
            break;
        default:
            resId = R.dimen.cell_layout_top_padding;
            break;
        }

        // TODP : use getIdentifier 
        Resources res = LauncherApplication.getContext().getResources();
        return res.getDimensionPixelSize(resId);
    }

    public static int getCelllayoutCellHeight() {
        int countX = LauncherModel.getCellCountX();
        int countY = LauncherModel.getCellCountY();
        int resId = -1;
        if (countX == 3 && countY == 3) {
            resId = R.dimen.bubble_icon_height_3_3;
        } else if (countY == 4) {
            resId = R.dimen.cell_height_4_4;
        } else {
            resId = R.dimen.cell_height_4_5;
        }

        Resources res = LauncherApplication.getContext().getResources();
        return res.getDimensionPixelSize(resId);
    }

    public static int getCellLayoutPaddingTopInHideseat() {
        Resources res = LauncherApplication.getContext().getResources();
        return res.getDimensionPixelSize(R.dimen.hideseat_cell_layout_top_padding);
    }

    public static int getCellLayoutCellHeightInHideseat() {
        Resources res = LauncherApplication.getContext().getResources();
        return res.getDimensionPixelSize(R.dimen.hideseat_cell_layout_cell_height);
    }

    public static int getCelllayoutCellWidth() {
        Resources res = LauncherApplication.getContext().getResources();
        int countX = LauncherModel.getCellCountX();
        int countY = LauncherModel.getCellCountY();
        if (countX == 3 && countY == 3) {
            return res.getDimensionPixelSize(R.dimen.bubble_icon_width_3_3);
        } else {
            return res.getDimensionPixelSize(R.dimen.workspace_cell_width);
        }
    }

    public static int getCelllayoutCellWidth(boolean hotseat) {
        if (hotseat) {
            return getHotseatIconWidth();
        } else {
            return getCelllayoutCellWidth();
        }
    }
    public static int getCelllayoutCellHeight(boolean hotseat) {
        if (hotseat) {
            return getHotseatIconHeight();
        } else {
            return getCelllayoutCellHeight();
        }
    }

    public static int getWorkspaceIconWidth() {
        if (AgedModeUtil.isAgedMode()) {
            return mContext.getResources().getDimensionPixelSize(R.dimen.bubble_icon_width_3_3);
        } else {
            return mContext.getResources().getDimensionPixelSize(R.dimen.workspace_cell_width);
        }
    }

    public static int getWorkspaceIconHeight() {
        if (AgedModeUtil.isAgedMode()) {
            return mContext.getResources().getDimensionPixelSize(R.dimen.bubble_icon_height_3_3);
        } else {
            return mContext.getResources().getDimensionPixelSize(R.dimen.workspace_cell_height);
        }
    }

    public static int getHotseatIconWidth() {
        if (AgedModeUtil.isAgedMode()) {
            return mContext.getResources().getDimensionPixelSize(R.dimen.bubble_icon_width_3_3);
        } else {
            return mContext.getResources().getDimensionPixelSize(R.dimen.hotseat_cell_width);
        }
    }

    public static int getHotseatIconHeight() {
        if (AgedModeUtil.isAgedMode()) {
            return mContext.getResources().getDimensionPixelSize(R.dimen.bubble_icon_height_3_3);
        } else {
            return mContext.getResources().getDimensionPixelSize(R.dimen.hotseat_cell_height);
        }
    }

    public static void adjustToThreeLayout() {
        setHideseatScreenMaxCount(mContext.getResources()
                .getInteger(R.integer.hideseat_screen_max_count_3_3));
        setHideseatMaxCountX(mContext.getResources()
                .getInteger(R.integer.hideseat_max_count_x_3_3));
        setHideseatHeight(mContext.getResources()
                .getDimensionPixelSize(R.dimen.workspace_cell_height_3_3));
        setHotseatMaxCountX(mContext.getResources()
                .getInteger(R.integer.hotseat_max_count_x_aged_mode));
    }

    public static void adjustFromThreeLayout() {
        setHideseatScreenMaxCount(mContext.getResources()
                .getInteger(R.integer.hideseat_screen_max_count));
        setHideseatMaxCountX(mContext.getResources()
                .getInteger(R.integer.hideseat_max_count_x));
        setHideseatHeight(mContext.getResources()
                .getDimensionPixelSize(R.dimen.button_bar_height_plus_padding));
        setHotseatMaxCountX(mContext.getResources()
                .getInteger(R.integer.hotseat_max_count_x));
    }

    public static boolean checkDataValid(Context context) {
        boolean dataInvalid = false;
        Resources res = context.getResources();
        int hotseatCountAgedMode = res.getInteger(R.integer.hotseat_max_count_x_aged_mode)
                * res.getInteger(R.integer.hotseat_max_count_y);
        int hidesetCountXAgedMode = res.getInteger(R.integer.hideseat_max_count_x_3_3);

        if (AgedModeUtil.isAgedMode()) {
            if (getCellCountX() != 3 || getCellCountY() != 3 || getHotseatMaxCount() != hotseatCountAgedMode
                    || getHideseatMaxCountX() != hidesetCountXAgedMode) {
                Log.d(AgedModeUtil.TAG, "AgedModeUtil is agedMode while UI not agedMode, call onAgedModeChanged:true");
                dataInvalid = true;
            }
        } else {
            if (getCellCountX() == 3 || getCellCountY() == 3 || getHotseatMaxCount() == hotseatCountAgedMode
                    || getHideseatMaxCountX() == hidesetCountXAgedMode) {
                Log.d(AgedModeUtil.TAG,
                        "AgedModeUtil is not agedMode while UI is agedMode,so call onAgedModeChanged:false");
                dataInvalid = true;
            }
        }
        return dataInvalid;
    }

    public static int getDefaultScreen(){
        /*if(AgedModeUtil.isAgedMode()){
            return 0;
        }else{ **/
        // start modify by huangxunwan for config the homeshell default page
        //    return mContext.getResources().getInteger(R.integer.default_screen);
        //}
        return TopwiseConfig.HOMESHELL_DEF_PAGE;
        //end modify by huangxunwan 
    }

    public static String toLogString() {
        return "ConfigManager [sIsVPInstallEnable=" + sIsVPInstallEnable + ", sIsServiceCenterEnable=" + sIsServiceCenterEnable
                        + ", sIsIconCardBGEnable=" + sIsIconCardBGEnable + ", sScreenMaxCount=" + sScreenMaxCount
                        + ", sHotseatMaxCount=" + sHotseatMaxCount + ", sCellCountX=" + sCellCountX
                        + ", sCellCountY=" + sCellCountY + ", sHideseatScreenMaxCount=" + sHideseatScreenMaxCount
                        + ", sHideseatMaxCountX=" + sHideseatMaxCountX + ", sHideseatMaxCountY=" + sHideseatMaxCountY
                        + ", sHideseatItemsMaxCount=" + sHideseatItemsMaxCount + ", sFolderMaxCountX=" + sFolderMaxCountX
                        + ", sFolderMaxCountY=" + sFolderMaxCountY + ", sFolderItemsMaxCount=" + sFolderItemsMaxCount
                        + ", sCellMaxCountX=" + sCellMaxCountX + ", sCellMaxCountY=" + sCellMaxCountX + "]";
    }
}
