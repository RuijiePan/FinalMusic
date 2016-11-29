package com.jiepier.floatmusic.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.LruCache;

/**
 * Created by JiePier on 16/11/13.
 */

public class MusicIconLoader {

    private static MusicIconLoader instance;
    private LruCache<String,Bitmap> mCache;

    public static MusicIconLoader getInstance() {
        if (instance == null) {
            synchronized (MusicIconLoader.class) {
                instance = new MusicIconLoader();
            }
        }
        return instance;
    }

    private MusicIconLoader(){
        int maxSize = (int) (Runtime.getRuntime().maxMemory()/8);
        mCache = new LruCache<String, Bitmap>(maxSize){
            protected int sizeof(String key,Bitmap value){
                return value.getByteCount();
            }
        };
    }

    public Bitmap load(String uri){
        if (uri == null)
            return null;

        Bitmap bmp = getFromCache(uri);
        if (bmp != null)
            return bmp;

        bmp = BitmapFactory.decodeFile(uri);
        addToCache(uri,bmp);
        return bmp;
    }

    private Bitmap getFromCache(String key) {
        return mCache.get(key);
    }

    // 将图片缓存到内存中
    private void addToCache(final String key, final Bitmap bmp) {
        if (getFromCache(key) == null && key != null && bmp != null)
            mCache.put(key, bmp);
    }
}
