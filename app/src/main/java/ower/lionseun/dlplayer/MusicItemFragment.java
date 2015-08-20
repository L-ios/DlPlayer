package ower.lionseun.dlplayer;

import android.app.Fragment;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;

/**
 * Created by lingyang on 8/20/15.
 */
public class MusicItemFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{

    static final int COL_MUSICITEM_ID = 0;
    static final int COL_MUSICITEM_TITLE = 1;
    static final int COL_MUSICITEM_SUMMARYTIME = 2;
    static final int COL_MUSICITEM_ARTIST = 3;
    static final int COL_MUSICITEM_ALBUM = 4;

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {

        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}
