package com.jiepier.floatmusic.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.jiepier.floatmusic.R;
import com.jiepier.floatmusic.base.BaseViewHolder;
import com.jiepier.floatmusic.bean.Music;
import com.jiepier.floatmusic.util.MusicUtil;
import com.jiepier.floatmusic.util.TimeUtil;

/**
 * Created by panruijiesx on 2016/11/14.
 */

public class MusicAdapter extends RecyclerView.Adapter<BaseViewHolder> {

    private int mPlayingPosition;
    private OnItemClickLisetener mLisetener;

    @Override
    public BaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View item = LayoutInflater.from(parent.getContext()).inflate(R.layout.music_list_item,parent,false);
        return new BaseViewHolder(item);
    }

    @Override
    public void onBindViewHolder(BaseViewHolder holder, final int position) {

        Music music = MusicUtil.sMusicList.get(position);

        //Log.w("haha","!!!!!++");
        if (mPlayingPosition == position){
            holder.setVisibility(R.id.music_list_selected,true);
        }else {
            holder.setVisibility(R.id.music_list_selected,false);
        }

        /*Bitmap icon = MusicIconLoader.getInstance()
                .load(music.getImage());
        icon = icon == null? ImageTools.scaleBitmap(R.drawable.ic_audiotrack_red_400_24dp):ImageTools.scaleBitmap(icon);*/

        holder//.setImageBitmap(R.id.music_list_icon,icon)
                .setText(R.id.tv_music_list_title,music.getTitle())
                .setText(R.id.tv_music_list_time, TimeUtil.getTime(music.getDuration()))
                .setText(R.id.tv_music_list_artist,music.getArtist());

        if (mLisetener != null)
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mLisetener.onItemClick(position);
                    notifyDataSetChanged();
                    /*notifyItemChanged(position);
                    notifyItemChanged(mPlayingPosition);*/
                    mPlayingPosition = position;
                }
            });
    }

    @Override
    public int getItemCount() {
        return MusicUtil.sMusicList == null ? 0 : MusicUtil.sMusicList.size();
    }

    public void setPlayingPosition(int position) {
        mPlayingPosition = position;
    }

    public int getPlayingPosition() {
        return mPlayingPosition;
    }

    public interface OnItemClickLisetener{
        void onItemClick(int position);
    }

    public OnItemClickLisetener getmLisetener() {
        return mLisetener;
    }

    public void setOnItemClickLisetener(OnItemClickLisetener mLisetener) {
        this.mLisetener = mLisetener;
    }
}
