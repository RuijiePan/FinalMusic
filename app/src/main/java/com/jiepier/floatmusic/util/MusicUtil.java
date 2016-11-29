package com.jiepier.floatmusic.util;

import android.os.Environment;

import com.jiepier.floatmusic.base.App;
import com.jiepier.floatmusic.bean.Music;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by JiePier on 16/11/12.
 */

public class MusicUtil {

    public static ArrayList<Music> sMusicList = new ArrayList<>();

    public static void initMusicList(){
        sMusicList.clear();
        sMusicList.addAll(LocalMusicUtil.queryMusic(getBaseDir()));
    }

    public static String getBaseDir(){
        String dir = null;
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_UNKNOWN)){
            dir = Environment.getExternalStorageDirectory() + File.separator;
        }else {
            dir = App.sContext.getFilesDir() + File.separator;
        }

        return dir;
    }

}
