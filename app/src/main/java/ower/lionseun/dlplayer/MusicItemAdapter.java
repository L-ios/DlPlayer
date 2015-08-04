package ower.lionseun.dlplayer;


import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

/**
 * Created by lingyang on 8/4/15.
 */
public class MusicItemAdapter extends CursorAdapter {

    public static class ViewHolder{
        public final TextView musicTitleView;
        public final TextView musicSummayTimeView;
        public final TextView musicArtistView;
        public final TextView musicAlbumView;
        public final ImageButton playMusicButton;
        public ViewHolder(View view){
            musicTitleView = (TextView) view.findViewById(R.id.list_item_music_title);
            musicSummayTimeView =(TextView) view.findViewById(R.id.list_item_music_summary_time);
            musicArtistView = (TextView) view.findViewById(R.id.list_item_music_artist);
            musicAlbumView = (TextView) view.findViewById(R.id.list_item_music_album);
            playMusicButton = (ImageButton) view.findViewById(R.id.list_item_play_music);
        }
    }
    /**
     * Recommended constructor.
     *
     * @param context The context
     * @param c       The cursor from which to get the data.
     * @param flags   Flags used to determine the behavior of the adapter; may
     *                be any combination of {@link #FLAG_AUTO_REQUERY} and
     *                {@link #FLAG_REGISTER_CONTENT_OBSERVER}.
     */
    public MusicItemAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    /**
     * Makes a new view to hold the data pointed to by cursor.
     *
     * @param context Interface to application's global information
     * @param cursor  The cursor from which to get the data. The cursor is already
     *                moved to the correct position.
     * @param parent  The parent to which the new view is attached to
     * @return the newly created view.
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        int layoutId = R.layout.music_item;

        View view = LayoutInflater.from(context).inflate(layoutId, parent, false);
        view.setTag(new ViewHolder(view));
        return view;
    }

    /**
     * Bind an existing view to the data pointed to by cursor
     *
     * @param view    Existing view, returned earlier by newView
     * @param context Interface to application's global information
     * @param cursor  The cursor from which to get the data. The cursor is already
     */
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
    }
}
