package ower.lionseun.dlplayer.data;

import android.os.Environment;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by lingyang on 8/20/15.
 */
public class ScanFile {

    private String TAG = "ScanFile";
    private static File EXT_DIR = Environment.getExternalStorageDirectory();

    // TODO

    public ScanFile() {

    }

    public static List<String> getExternlDirection() {
        String[] fileArray = EXT_DIR.list();
        List<String> listFile = new ArrayList<String>(Arrays.asList(fileArray));

        return listFile;
    }

}
