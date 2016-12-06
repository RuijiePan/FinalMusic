package com.jiepier.floatmusic.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;

import com.jiepier.floatmusic.R;
import com.jiepier.floatmusic.bean.PercentEvent;
import com.jiepier.floatmusic.widget.RotateView;

/**
 * Created by panruijiesx on 2016/11/29.
 */

public class FxService extends Service {

    LinearLayout mFloatLayout;
    WindowManager.LayoutParams mWparams;
    WindowManager mWindowManager;
    RotateView mRotateView;
    private FloatingViewClickListener mListener;
    private int pointDownX;
    private int pointDownY;
    private int pointUpX;
    private int pointUpY;
    private long pointDownTime;
    private long lastDownTime;
    public final static int DISTANCE = 15;
    public final static int LONG_CLICK_TIME = 1000;
    private ProgressRecevier mReceiver;

    private void createFloatView() {
        mWparams = new WindowManager.LayoutParams();
        mWindowManager = (WindowManager) getApplication().getSystemService(getApplication().WINDOW_SERVICE);
        mWparams.type = WindowManager.LayoutParams.TYPE_PHONE;
        mWparams.format = PixelFormat.RGBA_8888;
        mWparams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        mWparams.gravity = Gravity.LEFT|Gravity.TOP;
        mWparams.x = 0;
        mWparams.y = 0;

        mWparams.width = WindowManager.LayoutParams.WRAP_CONTENT;
        mWparams.height = WindowManager.LayoutParams.WRAP_CONTENT;

        LayoutInflater inflater = LayoutInflater.from(getApplication());
        mFloatLayout = (LinearLayout) inflater.inflate(R.layout.activity_play,null,false);
        mWindowManager.addView(mFloatLayout,mWparams);
        mRotateView = (RotateView) mFloatLayout.findViewById(R.id.cdView);
        Bitmap bmp = BitmapFactory.decodeResource(getResources(),
                R.mipmap.ic_launcher);
        mRotateView.setCdImage(bmp,0.3);
        //mRotateView.setRingWidth((float) (App.sScreenWidth*0.3-15));

        mRotateView.startRoll();
        //mRotateView.setOnTouchListener(this);
        mRotateView.setOnTouchListener(new View.OnTouchListener()
        {

            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                //getRawX是触摸位置相对于屏幕的坐标，getX是相对于按钮的坐标
                mWparams.x = (int) event.getRawX() - mRotateView.getMeasuredWidth()/2;
                mWparams.y = (int) event.getRawY() - mRotateView.getMeasuredHeight()/2 - 25;
                //刷新
                mWindowManager.updateViewLayout(mFloatLayout, mWparams);

                int x = (int) event.getRawX();
                int y = (int) event.getRawY();
                switch (event.getAction()){
                    case MotionEvent.ACTION_DOWN:
                        pointDownX = x;
                        pointDownY = y;
                        pointDownTime = System.currentTimeMillis();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        lastDownTime = System.currentTimeMillis();
                        //Log.w("haha",pointDownX+"!!"+pointDownY+"!!!x="+x+"!!!y="+y);
                        if (isLongPressed(pointDownX,pointDownY,x,y,pointDownTime,lastDownTime)) {
                            LongClick();
                            return true;
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                        pointUpX = x;
                        pointUpY = y;
                        if (Math.abs(pointDownX-pointUpX)<DISTANCE&&
                                Math.abs(pointUpY-pointDownY)<DISTANCE) {
                            onViewClick();
                            return true;
                        }
                        break;
                }
                return true;
            }
        });

        mReceiver = new ProgressRecevier();
        IntentFilter intentFilter = new IntentFilter();

        intentFilter.addAction("com.jiepier.floatmusic.RECEVER");
        registerReceiver(mReceiver, intentFilter);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new FxBinder();
    }

    public class FxBinder extends Binder {

        public FxService getService(){
            return FxService.this;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mFloatLayout != null){
            mWindowManager.removeView(mFloatLayout);
        }
        if (mReceiver!=null)
        unregisterReceiver(mReceiver);
    }


    private void LongClick() {
        if (mListener!=null)
            mListener.onLongClick();
        //stopService(new Intent(App.sContext,FxService.class));
    }

    private void onViewClick() {
        if (mListener!=null)
            mListener.OnClick();
    }

    boolean isLongPressed(float lastX, float lastY, float thisX,
                          float thisY, long lastDownTime, long thisEventTime) {
        float offsetX = Math.abs(thisX - lastX);
        float offsetY = Math.abs(thisY - lastY);
        long intervalTime = thisEventTime - lastDownTime;
        if (offsetX <= DISTANCE && offsetY <= DISTANCE && intervalTime >= LONG_CLICK_TIME) {
            return true;
        }
        return false;
    }

    public void setRotateAngle(int angle){
        mRotateView.rotate(angle);
    }

    public void setmListener(FloatingViewClickListener mListener) {
        this.mListener = mListener;
    }

    public interface FloatingViewClickListener{
        void OnClick();

        void onLongClick();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        createFloatView();
    }

    public class ProgressRecevier extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            int progress = intent.getIntExtra("progress",0);
            mRotateView.rotate(progress);
        }
    }
}