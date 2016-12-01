package com.jiepier.floatmusic.util;

import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import com.jiepier.floatmusic.base.App;
import com.jiepier.floatmusic.bean.Music;

import java.util.ArrayList;

/**
 * Created by JiePier on 16/11/12.
 */

public class LocalMusicUtil {

    /*歌曲ID：MediaStore.Audio.Media._ID
    Int id = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID));
    歌曲的名称 ：MediaStore.Audio.Media.TITLE
    String tilte = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE));
    歌曲的专辑名：MediaStore.Audio.Media.ALBUM
    String album = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM));
    歌曲的歌手名： MediaStore.Audio.Media.ARTIST
    String artist = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST));
    歌曲文件的全路径 ：MediaStore.Audio.Media.DATA
    String url = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));
    歌曲文件的名称：MediaStroe.Audio.Media.DISPLAY_NAME
    String display_name = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME));
    歌曲文件的发行日期：MediaStore.Audio.Media.YEAR
    String year = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.YEAR));
    歌曲的总播放时长 ：MediaStore.Audio.Media.DURATION
    Int duration = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION));
    歌曲文件的大小 ：MediaStore.Audio.Media.SIZE
    Int size = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE));*/

    public static ArrayList<Music> queryMusic(String dirName){

        //select * from xx where data like dirName order by modify_time
        ArrayList<Music> musicList = new ArrayList<>();
        Cursor cursor = App.sContext.getContentResolver().query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,null,
                MediaStore.Audio.Media.DATA + " like ?",
                new String[]{dirName + "%"},
                MediaStore.Audio.Media.DEFAULT_SORT_ORDER);

        if (cursor == null)
            return musicList;


        Music music;
        for (cursor.moveToFirst();!cursor.isAfterLast();cursor.moveToNext()){
            // 如果不是音乐
            String isMusic = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.IS_MUSIC));
            if (isMusic != null && isMusic.equals(""))
                continue;

            String title = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE));
            String artist = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST));

            if(isRepeat(title, artist)) continue;

            music = new Music();
            music.setId(cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)));
            music.setTitle(title);
            music.setArtist(artist);
            music.setUri(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)));
            music.setDuration(cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)));
            music.setYear(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.YEAR)));
            music.setSize(cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE)));
            music.setAlbum(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)));
            music.setImage(getAlbumImage(cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID))));
            music.setDuration(music.getDuration()==0?1:music.getDuration());
            musicList.add(music);
        }
        cursor.close();
        return musicList;
    }

    private static String getAlbumImage(int albumId) {
        String result = "";
        Cursor cursor = null;
        try {
            cursor = App.sContext.getContentResolver().query(
                    Uri.parse("content://media/external/audio/albums/"+albumId),
                    new String[]{"album_art"},
                    null,null,null
            );
            for (cursor.moveToFirst();!cursor.isAfterLast();){
                result = cursor.getString(0);
                break;
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            if (cursor != null)
                cursor.close();
        }
        return result == null? "":result;
    }

    /**
     * 根据音乐名称和艺术家来判断是否重复包含了
     * @param title
     * @param artist
     * @return
     */
    private static boolean isRepeat(String title, String artist) {
        for(Music music : MusicUtil.sMusicList) {
            if(title.equals(music.getTitle()) && artist.equals(music.getArtist())) {
                return true;
            }
        }
        return false;
    }
}
