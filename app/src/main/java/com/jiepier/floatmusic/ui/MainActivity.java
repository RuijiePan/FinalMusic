package com.jiepier.floatmusic.ui;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.jiepier.floatmusic.R;
import com.jiepier.floatmusic.adapter.MusicAdapter;
import com.jiepier.floatmusic.base.App;
import com.jiepier.floatmusic.base.BaseActivity;
import com.jiepier.floatmusic.bean.ClickEvent;
import com.jiepier.floatmusic.bean.ListenerEvent;
import com.jiepier.floatmusic.bean.Music;
import com.jiepier.floatmusic.service.FxService;
import com.jiepier.floatmusic.service.PlayService;
import com.jiepier.floatmusic.util.MusicUtil;
import com.jiepier.floatmusic.util.RecyclerViewDivider;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by JiePier on 16/11/12.
 */

public class MainActivity extends BaseActivity {

    @BindView(R.id.iv_play)
    ImageView ivPlay;
    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;
    @BindView(R.id.play_progress)
    SeekBar playProgress;
    @BindView(R.id.tv_play_title)
    TextView tvPlayTitle;
    @BindView(R.id.tv_play_artist)
    TextView tvPlayArtist;
    @BindView(R.id.activity_main)
    RelativeLayout activityMain;
    @BindView(R.id.rl_controller)
    RelativeLayout rlController;
    @BindView(R.id.iv_play_icon)
    ImageView ivPlayIcon;

    private int mPosition;
    private boolean isPause;
    private MusicAdapter mMusicAdapter;
    public static final int WINDOW_REQUEST_CODE = 200;
    private static final int REQUEST_CODE = 1;

    @Override
    public int initContentView() {
        return R.layout.activity_main;
    }

    @Override
    public void initUiAndListener() {

        mMusicAdapter = new MusicAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(mMusicAdapter);
        recyclerView.addItemDecoration(new RecyclerViewDivider(
                this, RecyclerViewDivider.VERTICAL_LIST));

        //rlController.setVisibility(View.GONE);

        mMusicAdapter.setOnItemClickLisetener(
                new MusicAdapter.OnItemClickLisetener() {
                    @Override
                    public void onItemClick(int position) {

                        mPlayService.play(position);
                        startService(new Intent(App.sContext, FxService.class));
                        BindFxService();
                        //mPosition = position;
                        playProgress.setMax(MusicUtil.sMusicList.get(position).getDuration());

                        /*if (mFxService != null) {
                            mFxService.setmListener(new FxService.FloatingViewClickListener() {
                                @Override
                                public void OnClick() {
                                    if (mPlayService.isPlaying()) {
                                        mPlayService.pause();
                                        EventBus.getDefault().post(new ClickEvent(true));
                                    } else {
                                        mPlayService.resume();
                                        EventBus.getDefault().post(new ClickEvent(false));
                                    }
                                }

                                @Override
                                public void onLongClick() {
                                    mPlayService.pause();
                                    unBindFxService();
                                }
                            });
                            Intent intent = new Intent(App.sContext, MusicActivity.class);
                            intent.putExtra("position", mPosition);
                            startActivity(intent);
                        }*/
                    }
                });

        playProgress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int progress = seekBar.getProgress();
                mPlayService.seek(progress);
            }
        });

    }


    @Override
    protected boolean isApplyStatusBarTranslucency() {
        return true;
    }

    @Override
    protected boolean isApplyStatusBarColor() {
        return true;
    }

    @Override
    public void onPublish(int percent) {
        //Log.w("haha",percent+"");
        if (isPause)
            return;
        playProgress.setProgress(percent*MusicUtil.sMusicList.get(mPlayService.getPlayingPosition()).getDuration()/100);

        if (mFxService != null) {
            mFxService.setRotateAngle(percent);

            mFxService.setmListener(new FxService.FloatingViewClickListener() {
                @Override
                public void OnClick() {
                    if (mPlayService.isPlaying()) {
                        mPlayService.pause();
                        //EventBus.getDefault().post(new ClickEvent(true));
                    } else {
                        mPlayService.resume();
                        //EventBus.getDefault().post(new ClickEvent(false));
                    }
                }

                @Override
                public void onLongClick() {
                    isPause = true;
                    ivPlay.setImageResource(android.R.drawable.ic_media_pause);
                    //Toast.makeText(App.sContext,"关闭浮窗",Toast.LENGTH_LONG).show();
                    mPlayService.pause();
                    unBindFxService();
                    //stopService(new Intent(App.sContext,FxService.class));
                    //Toast.makeText(App.sContext,"long",Toast.LENGTH_SHORT).show();
                }
            });

            /*Intent intent = new Intent(App.sContext, MusicActivity.class);
            intent.putExtra("position", mPosition);
            startActivity(intent);*/
        }

    }

    @Override
    public void onChange(int position) {
        Music music = MusicUtil.sMusicList.get(position);
        tvPlayTitle.setText(music.getTitle());
        tvPlayArtist.setText(music.getArtist());
    }

    @Override
    protected void onStart() {
        super.onStart();
        allowBindService();
    }

    //解除绑定歌曲播放服务
    @Override
    protected void onStop() {
        super.onStop();
        //allowUnBindService();
    }

    @Override
    protected void onResume() {
        super.onResume();
        isPause = false;
    }

    @Override
    protected void onPause() {
        super.onPause();
        isPause = true;
    }

    @OnClick({R.id.iv_pre, R.id.iv_play, R.id.iv_next, R.id.iv_play_icon})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_play_icon:
                //Intent intent = new Intent(App.sContext,MusicActivity)
                startActivity(new Intent(App.sContext, MusicActivity.class));
                break;
            case R.id.iv_pre:
                if (MusicUtil.sMusicList.size() != 0)
                    mPlayService.pre();
                break;
            case R.id.iv_play:
                if (MusicUtil.sMusicList.size() != 0) {
                    if (mPlayService.isPlaying()) {
                        mPlayService.pause();
                        isPause = true;
                        ivPlay.setImageResource(android.R.drawable.ic_media_pause);
                    } else {
                        mPlayService.resume();
                        isPause = false;
                        ivPlay.setImageResource(android.R.drawable.ic_media_play);
                    }
                }
                break;
            case R.id.iv_next:
                if (MusicUtil.sMusicList.size() != 0)
                    getPlayService().next();
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(final int requestCode, @NonNull final String[] permissions, @NonNull final int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == WINDOW_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted.

            } else {
                // User refused to grant permission.
                Toast.makeText(this,"给予创建浮窗权限，否则app没法用啊",Toast.LENGTH_LONG).show();
            }
        }

    }

}
