package com.jiepier.floatmusic.ui;

import android.content.Intent;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;

import com.jiepier.floatmusic.R;
import com.jiepier.floatmusic.base.App;
import com.jiepier.floatmusic.base.BaseActivity;
import com.jiepier.floatmusic.service.PlayService;
import com.jiepier.floatmusic.util.BitmapUtil;
import com.jiepier.floatmusic.widget.RotateView;

import butterknife.BindView;

/**
 * Created by JiePier on 16/11/13.
 */

public class PlayActivity extends BaseActivity implements View.OnTouchListener{

    @BindView(R.id.cdView)
    RotateView rotateView;
    @BindView(R.id.root)
    LinearLayout root;
    private int mLastX;
    private int mLastY;
    private double scale = 0.3;
    private int pointDownX;
    private int pointDownY;
    private long pointDownTime;
    private long lastDownTime;
    private int pointUpX;
    private int pointUpY;
    private boolean isFirst = true;
    public final static int DISTANCE = 5;
    public final static int LONG_CLICK_TIME = 3000;

    @Override
    public int initContentView() {
        return R.layout.activity_play;
    }
    @Override
    public void initUiAndListener() {

        Bitmap bmp = BitmapUtil.getBitmapFromDrawable(
                getResources().getDrawable(R.drawable.ic_audiotrack_red_300_48dp));
        rotateView.setCdImage(bmp,scale);

        rotateView.startRoll();

        /*rotateView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mPlayService.isPlaying()) {
                    mPlayService.pause();
                } else {
                    mPlayService.resume();
                }
            }
        });

        rotateView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                finish();
                stopService(new Intent(PlayActivity.this,PlayService.class));
                return true;
            }
        });*/

        rotateView.setOnTouchListener(this);
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
        rotateView.rotate(percent);
    }

    @Override
    public void onChange(int position) {

    }

    @Override
    protected void onResume() {
        super.onResume();
        allowBindService();
    }

    @Override
    protected void onPause() {
        allowUnBindService();
        super.onPause();
    }

    @Override
    public boolean onTouch(View view, MotionEvent event) {
        int x = (int) event.getX();
        int y = (int) event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                pointDownX = x;
                pointDownY = y;
                pointDownTime = System.currentTimeMillis();
                isFirst = true;
                break;
            case MotionEvent.ACTION_MOVE:
                if (isTouchView(x,y)) {
                    lastDownTime = System.currentTimeMillis();
                    if (isLongPressed(pointDownX,pointDownY,x,y,pointDownTime,lastDownTime)) {
                        LongClick();
                        //Toast.makeText(PlayActivity.this,"haha",Toast.LENGTH_LONG).show();
                    }else {
                        int dx = mLastX - x;
                        int dy = mLastY - y;

                        if (rotateView.getScrollX() +dx>= 0) {
                            if (dx > 0) {
                                dx = -rotateView.getScrollX();
                            }
                            //Log.w("haha", "111");
                        } else if (rotateView.getRight() * scale - rotateView.getScrollX() +dx>= root.getRight()) {
                            if (dx < 0) {
                                dx = -(int) (root.getRight() + rotateView.getScrollX() - rotateView.getRight()*scale);
                                Log.w("haha",dx+"");
                            }
                            //Log.w("haha", "222");
                        }

                        if (rotateView.getScrollY() +dy>= 0) {
                            if (dy > 0) {
                                dy = -rotateView.getScrollY();
                            }
                            //Log.w("haha", "333");
                        } else if (rotateView.getBottom() * scale - rotateView.getScrollY() +dy>= App.sScreenHeight) {
                            //Log.w("haha", "444");
                        /*Log.w("haha", rotateView.getBottom() * scale + "");
                        Log.w("haha", rotateView.getScrollY() + "");
                        Log.w("haha", App.sScreenHeight+ "");*/
                            if (dy < 0) {
                                dy = -(int) (App.sScreenHeight+rotateView.getScrollY()-rotateView.getBottom()*scale);
                            }
                        }
                        rotateView.scrollBy(dx, dy);
                    }
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
        mLastX = x;
        mLastY = y;
        return true;
    }

    private void LongClick() {
        stopService(new Intent(PlayActivity.this,PlayService.class));
        finish();
    }

    private void onViewClick() {
        if (mPlayService.isPlaying()) {
            mPlayService.pause();
        } else {
            mPlayService.resume();
        }
    }

    public boolean isTouchView(int x,int y){
        int left = (int) -rotateView.getScrollX();
        int right = (int) (left + rotateView.getWidth()*scale);
        int top = (int) -rotateView.getScrollY();
        int bottom = (int) (top + rotateView.getWidth()*scale);

        /*Log.w("haha","left="+left+",right="+right+",top="+top+",bottom="+bottom);
        Log.w("haha","x="+x+",y="+y);*/
        if (x>=left&&x<=right&&y>=top&&y<=bottom)
            return true;
        return false;
    }

    boolean isLongPressed(float lastX, float lastY, float thisX,
                                 float thisY, long lastDownTime, long thisEventTime) {
        float offsetX = Math.abs(thisX - lastX);
        float offsetY = Math.abs(thisY - lastY);
        long intervalTime = thisEventTime - lastDownTime;
        if (offsetX <= DISTANCE && offsetY <= DISTANCE && intervalTime >= LONG_CLICK_TIME&&isFirst) {
            isFirst = false;
            return true;
        }
        return false;
    }
}
