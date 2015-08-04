package com.tpw.homeshell.screenmanager;

import com.tpw.homeshell.R;

import android.content.Context;

class ScreenmanagerParamFactory {

    private static ScreenmanagerParamFactory mInstance = new ScreenmanagerParamFactory();

    private ScreenmanagerParamFactory() {}

    static ScreenmanagerParamFactory getFactory() {
        return mInstance;
    }

    ScreenManagerParam createScreenManagerPamaram(Context context,
            int width, int height, int size, int current) {
        if(size <= Const.CATGORY_1_MAX_SCREENS) {
            return new ScreenManagerParamSize4(context, width, height, size, current);
        } else if (size <= Const.CATGORY_2_MAX_SCREENS) {
            return new ScreenManagerParamSize8(context, width, height, size, current);
        }
        return new ScreenManagerParamSize12(context, width, height, size, current);
    }

    private static class ScreenManagerParamSize4 extends ScreenManagerParam {

        private final int X_GAP;
        private final int Y_GAP;
        private int mStartX;
        private int mStartY;

        ScreenManagerParamSize4(Context context,
                int width, int height, int size, int current) {
            super(context,width, height, size, current);
            X_GAP = context.getResources()
                    .getDimensionPixelSize(R.dimen.screen_edit_cell_gap_x1);
            Y_GAP = context.getResources()
                    .getDimensionPixelSize(R.dimen.screen_edit_cell_gap_y1);
            mStartX = mCenterX - (SCREEN_CARD_WIDTH * (mSize > 1 ? 2 : 1)
                    + X_GAP * (mSize > 1 ? 1 : 0)) / 2;
            mStartY = mCenterY - (SCREEN_CARD_HEIGHT * ((mSize + 1) / 2)
                    + Y_GAP * ((mSize + 1) / 2 - 1)) / 2;
        }

        @Override
        int getX(int index) {
            return mStartX + SCREEN_CARD_WIDTH * (index % 2)
                    + X_GAP * (index % 2);
        }

        @Override
        int getY(int index) {
            return mStartY + SCREEN_CARD_HEIGHT * (index / 2)
                    + Y_GAP * (index / 2);
        }
    }

    private static class ScreenManagerParamSize8 extends ScreenManagerParam {

        private final int X_GAP;
        private final int Y_GAP;
        private int mStartX;
        private int mStartY;

        ScreenManagerParamSize8(Context context,
                int width, int height, int size, int current) {
            super(context,width, height, size, current);
            X_GAP = context.getResources()
                    .getDimensionPixelSize(R.dimen.screen_edit_cell_gap_x1);
            Y_GAP = context.getResources()
                    .getDimensionPixelSize(R.dimen.screen_edit_cell_gap_y2);
            mStartX = mCenterX - (SCREEN_CARD_WIDTH * 2 + X_GAP) / 2;
            mStartY = mCenterY - (SCREEN_CARD_HEIGHT * ((mSize + 1) / 2)
                    + Y_GAP * ((mSize + 1) / 2 - 1)) / 2;
        }

        @Override
        int getX(int index) {
            return mStartX + SCREEN_CARD_WIDTH * (index % 2)
                    + X_GAP * (index % 2);
        }

        @Override
        int getY(int index) {
            return mStartY + SCREEN_CARD_HEIGHT * (index / 2)
                    + Y_GAP * (index / 2);
        }
    }

    private static class ScreenManagerParamSize12 extends ScreenManagerParam {

        private final int X_GAP;
        private final int Y_GAP;
        private int mStartX;
        private int mStartY;

        ScreenManagerParamSize12(Context context,
                int width, int height, int size, int current) {
            super(context,width, height, size, current);
            X_GAP = context.getResources()
                    .getDimensionPixelSize(R.dimen.screen_edit_cell_gap_x2);
            Y_GAP = context.getResources()
                    .getDimensionPixelSize(R.dimen.screen_edit_cell_gap_y2);
            mStartX = mCenterX - (SCREEN_CARD_WIDTH * 3 + X_GAP * 2) / 2;
            mStartY = mCenterY - (SCREEN_CARD_HEIGHT * ((mSize + 2) / 3)
                    + Y_GAP * ((mSize + 2) / 3 - 1)) / 2;
        }

        @Override
        int getX(int index) {
            return mStartX + SCREEN_CARD_WIDTH * (index % 3)
                    + X_GAP * (index % 3);
        }

        @Override
        int getY(int index) {
             return mStartY + SCREEN_CARD_HEIGHT * (index / 3)
                        + Y_GAP * (index / 3);
        }
    }

}
