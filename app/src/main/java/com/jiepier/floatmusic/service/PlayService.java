package com.jiepier.floatmusic.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.RemoteViews;

import com.jiepier.floatmusic.R;
import com.jiepier.floatmusic.bean.PercentEvent;
import com.jiepier.floatmusic.ui.PlayActivity;
import com.jiepier.floatmusic.util.Constants;
import com.jiepier.floatmusic.util.ImageTools;
import com.jiepier.floatmusic.util.MusicIconLoader;
import com.jiepier.floatmusic.util.MusicUtil;
import com.jiepier.floatmusic.util.SpUtils;

import org.greenrobot.eventbus.EventBus;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by JiePier on 16/11/12.
 */

public class PlayService extends Service implements MediaPlayer.OnCompletionListener{

    private int mPlayingPosition;
    private MediaPlayer mMediaPlayer;
    private OnMusicEventListener mListener;
    private PowerManager.WakeLock mWakeLock = null;//获取设备电源锁，防止锁屏后服务被停止

    private Notification notification;//通知栏
    private RemoteViews remoteViews;//通知栏布局
    private NotificationManager notificationManager;

    public static final int PRE = 1;
    public static final int PAUSE = 2;
    public static final int NEXT = 3;
    public static final int EXIT = 4;
    public static final int CANCLE = 5;

    // 单线程池
    private ExecutorService mProgressUpdatedListener = Executors
            .newSingleThreadExecutor();

    public class PlayBinder extends Binder{

        public PlayService getService(){
            return PlayService.this;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new PlayBinder();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startForeground(0, notification);//让服务前台运行
        return Service.START_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        acquireWakeLock();

        MusicUtil.initMusicList();
        mPlayingPosition = (int) SpUtils.get(this, Constants.PLAY_POS,0);

        try {
            Uri uri = Uri.parse(MusicUtil.sMusicList.get(
                    mPlayingPosition).getUri());
            mMediaPlayer = MediaPlayer.create(PlayService.this, uri);
            mMediaPlayer.setOnCompletionListener(this);

            mProgressUpdatedListener.execute(mPublishProgressRunnable);

            PendingIntent pendingIntent = PendingIntent
                    .getActivity(PlayService.this, 0,
                            new Intent(PlayService.this, PlayActivity.class), 0);

            remoteViews = new RemoteViews(getPackageName(),
                    R.layout.play_notification);
            notification = new Notification(R.drawable.ic_audiotrack_red_300_48dp,
                    R.string.palying_music + "", System.currentTimeMillis());
            notification.contentIntent = pendingIntent;
            notification.contentView = remoteViews;
            //一直存在
            notification.flags = Notification.FLAG_ONGOING_EVENT;

            Intent intent = new Intent(PlayService.class.getSimpleName());
            intent.putExtra(Constants.BUTTON_NOTI, PRE);
            PendingIntent preIntent = PendingIntent.getBroadcast(
                    PlayService.this, PRE, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT);
            remoteViews.setOnClickPendingIntent(
                    R.id.music_play_pre, preIntent
            );

            intent.putExtra(Constants.BUTTON_NOTI, PAUSE);
            PendingIntent pauseIntent = PendingIntent.getBroadcast(
                    PlayService.this, PAUSE, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT);
            remoteViews.setOnClickPendingIntent(
                    R.id.music_play_pause, pauseIntent);

            intent.putExtra(Constants.BUTTON_NOTI, NEXT);
            PendingIntent nextIntent = PendingIntent.getBroadcast(
                    PlayService.this, NEXT, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT);
            remoteViews.setOnClickPendingIntent(
                    R.id.music_play_next, nextIntent);

            intent.putExtra(Constants.BUTTON_NOTI, EXIT);
            PendingIntent exit = PendingIntent.getBroadcast(
                    PlayService.this, EXIT, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT);
            remoteViews.setOnClickPendingIntent(
                    R.id.music_play_notifi_exit, exit);

            notificationManager = (NotificationManager)
                    getSystemService(NOTIFICATION_SERVICE);
            setRemoteViews();

            /**
             * 注册广播接收者
             * 功能：
             * 监听通知栏按钮点击事件
             */
            IntentFilter filter = new IntentFilter(
                    PlayService.class.getSimpleName());
            PlayBroadCastReceiver receiver = new PlayBroadCastReceiver();
            registerReceiver(receiver, filter);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    // 申请设备电源锁
    private void acquireWakeLock(){
        if (mWakeLock == null){
            PowerManager powerManager = (PowerManager) this.getSystemService(Context.POWER_SERVICE);
            mWakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK|
                PowerManager.ON_AFTER_RELEASE,"");
            if (mWakeLock != null){
                mWakeLock.acquire();
            }
        }
    }

    // 释放设备电源锁
    private void releaseWakeLock() {

        if (null != mWakeLock) {
            mWakeLock.release();
            mWakeLock = null;
        }
    }

    public void setRemoteViews(){

        remoteViews.setTextViewText(R.id.music_name,
                MusicUtil.sMusicList.get(
                        getPlayingPosition()).getTitle());
        remoteViews.setTextViewText(R.id.music_author,
                MusicUtil.sMusicList.get(
                        getPlayingPosition()).getArtist());
        Bitmap icon = MusicIconLoader.getInstance().load(
                MusicUtil.sMusicList.get(
                        getPlayingPosition()).getImage());
        remoteViews.setImageViewBitmap(R.id.music_icon,icon == null
                ? ImageTools.scaleBitmap(R.drawable.ic_audiotrack_red_300_48dp)
                : ImageTools
                .scaleBitmap(icon));
        if (isPlaying()) {
            remoteViews.setImageViewResource(R.id.music_play_pause,
                    R.drawable.btn_notification_player_stop_normal);
        }else {
            remoteViews.setImageViewResource(R.id.music_play_pause,
                    R.drawable.btn_notification_player_play_normal);
        }
        //通知栏更新
        notificationManager.notify(CANCLE, notification);
    }

    private Runnable mPublishProgressRunnable = new Runnable() {
        @Override
        public void run() {
            while (true){
                if (mMediaPlayer != null && mMediaPlayer.isPlaying() &&
                        mListener != null){
                    int percent = mMediaPlayer.getCurrentPosition()*100/ MusicUtil.sMusicList.get(mPlayingPosition).getDuration();
                    percent = percent == 0?1:percent;
                    EventBus.getDefault().post(new PercentEvent(percent));
                    mListener.onPublish(mMediaPlayer.getCurrentPosition());

                }
                /*
			 * SystemClock.sleep(millis) is a utility function very similar
			 * to Thread.sleep(millis), but it ignores InterruptedException.
			 * Use this function for delays if you do not use
			 * Thread.interrupt(), as it will preserve the interrupted state
			 * of the thread. 这种sleep方式不会被Thread.interrupt()所打断
			 */SystemClock.sleep(200);
            }
        }
    };

    public int getPlayingPosition() {
        return mPlayingPosition;
    }

    public boolean isPlaying() {
        return null != mMediaPlayer && mMediaPlayer.isPlaying();
    }

    private class PlayBroadCastReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(
                    PlayService.class.getSimpleName())) {

                switch (intent.getIntExtra(Constants.BUTTON_NOTI,0)){
                    case PRE:
                        pre();
                        break;
                    case PAUSE:
                        if (isPlaying()) {
                            pause(); // 暂停
                        } else {
                            resume(); // 播放
                        }
                        break;
                    case NEXT:
                        next();
                        break;
                    case EXIT:
                        if (isPlaying()) {
                            pause();
                        }
                        //取消通知栏
                        notificationManager.cancel(CANCLE);
                        break;
                    default:
                        break;
                }
            }
        }
    }

    /**
     * 上一曲
     *
     * @return 当前播放的位置
     */
    public int pre() {
        if (mPlayingPosition <= 0) {
            return play(MusicUtil.sMusicList.size() - 1);
        }
        return play(mPlayingPosition - 1);
    }

    /**
     * 开始播放
     */
    private void start() {
        mMediaPlayer.start();
    }

    /**
     * 下一曲
     *
     * @return 当前播放的位置
     */
    public int next() {
        if (mPlayingPosition >= MusicUtil.sMusicList.size() - 1) {
            return play(0);
        }
        return play(mPlayingPosition + 1);
    }

    /**
     * 暂停播放
     *
     * @return 当前播放的位置
     */
    public int pause() {
        if (!isPlaying())
            return -1;
        mMediaPlayer.pause();
        setRemoteViews();
        return mPlayingPosition;
    }

    /**
     * 继续播放
     *
     * @return 当前播放的位置 默认为0
     */
    public int resume() {
        if (isPlaying())
            return -1;
        mMediaPlayer.start();
        setRemoteViews();
        return mPlayingPosition;
    }

    //循环播放
    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        play(mPlayingPosition);
    }

    /**
     * 播放
     *
     * @param position
     *            音乐列表播放的位置
     * @return 当前播放的位置
     */
    public int play(int position) {

        if (position < 0)
            position = 0;
        if (position >= MusicUtil.sMusicList.size())
            position = MusicUtil.sMusicList.size() - 1;

        try {
            mMediaPlayer.reset();
            mMediaPlayer.setDataSource(MusicUtil
                    .sMusicList.get(position).getUri());
            mMediaPlayer.prepare();

            start();
            if (mListener != null)
                mListener.onChange(position);
        } catch (Exception e) {
            e.printStackTrace();
        }

        mPlayingPosition = position;
        SpUtils.put(Constants.PLAY_POS, mPlayingPosition);
        setRemoteViews();
        return mPlayingPosition;
    }

    /**
     * 获取当前正在播放音乐的总时长
     *
     * @return
     */
    public int getDuration() {
        if (!isPlaying())
            return 0;
        return mMediaPlayer.getDuration();
    }

    /**
     * 拖放到指定位置进行播放
     *
     * @param msec
     */
    public void seek(int msec) {
        if (!isPlaying())
            return;
        mMediaPlayer.seekTo(msec);
    }

    @Override
    public void onRebind(Intent intent) {
        super.onRebind(intent);
        if (mListener != null)
            mListener.onChange(mPlayingPosition);
    }

    @Override
    public void onDestroy() {

        release();
        stopForeground(true);
        super.onDestroy();
    }

    /**
     * 服务销毁时，释放各种控件
     */
    private void release() {
        if (!mProgressUpdatedListener.isShutdown())
            mProgressUpdatedListener.shutdownNow();
        mProgressUpdatedListener = null;
        //释放设备电源锁
        releaseWakeLock();
        if (mMediaPlayer != null)
            mMediaPlayer.release();
        mMediaPlayer = null;
    }

    public void setOnMusicEventListener(OnMusicEventListener listener){
        this.mListener = listener;
    }

    /**
     * 音乐播放回调接口
     */
    public interface OnMusicEventListener {
        public void onPublish(int percent);

        public void onChange(int position);
    }
}
