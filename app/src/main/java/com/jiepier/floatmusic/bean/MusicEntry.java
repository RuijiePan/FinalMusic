package com.jiepier.floatmusic.bean;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by panruijiesx on 2016/11/14.
 */

public class MusicEntry {

    private ImageView icon;
    private TextView title;
    private TextView artist;
    private View mark;

    public ImageView getIcon() {
        return icon;
    }

    public void setIcon(ImageView icon) {
        this.icon = icon;
    }

    public TextView getTitle() {
        return title;
    }

    public void setTitle(TextView title) {
        this.title = title;
    }

    public TextView getArtist() {
        return artist;
    }

    public void setArtist(TextView artist) {
        this.artist = artist;
    }

    public View getMark() {
        return mark;
    }

    public void setMark(View mark) {
        this.mark = mark;
    }
}
