package ower.lionseun.dlplayer.data;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by lingyang on 8/20/15.
 */
public class MusicContract {
    //(1)构建权限认证
    public static final String CONTENT_AUTHORITY = "ower.lionseun.dlplayer";
    //(2)利用Uri建立基本的Uri
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    //(3)这里登录的方式，如果是路径就采用PATH开头，列号采用NUMBER开头
    public static final String PATH_MUSIC = "music";

    //(4)构建Uri进入类，这里必须实现BaseColumns接口
    public static class MusicEntry implements BaseColumns {
        public static final String TABLENAME = "music";
        public static final String FILENAME = "filename";
        public static final String MUSIC_TITILE = "titile";
        public static final String MUSIC_SUMMARYTIME = "summarytime";
        public static final String MUSIC_ARTIST = "artist";
        public static final String MUSIC_ALBUM = "album";
    }
}
