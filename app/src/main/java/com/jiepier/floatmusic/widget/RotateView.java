package com.jiepier.floatmusic.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.View;

import com.jiepier.floatmusic.R;
import com.jiepier.floatmusic.base.App;
import com.jiepier.floatmusic.util.ImageTools;

/**
 * Created by JiePier on 16/11/12.
 * 旋转cd
 */

public class RotateView extends View{

    private static final int MSG_RUN = 0x00000100;
    private static final int TIME_UPDATE = 16;

    private Bitmap mClipBitmap;//cd图片

    private Matrix mMatrix;
    private float mRotation = 0.0f;
    private volatile boolean isRunning;

    private Paint paint;
    private int ringColor;
    private int ringProgressColor;
    private float ringWidth;

    private double scale;

    public RotateView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        mMatrix = new Matrix();

        paint = new Paint();
        TypedArray mTypedArray = context.obtainStyledAttributes(attrs, R.styleable.RoundProgressBar);
        ringColor = mTypedArray.getColor(R.styleable.RoundProgressBar_ringColor,0xff50c0e9);
        ringProgressColor = mTypedArray.getColor(R.styleable.RoundProgressBar_ringProgressColor, 0xffffc641);
        ringWidth = mTypedArray.getDimension(R.styleable.RoundProgressBar_ringWidth, 20);
        mTypedArray.recycle();
    }

    public RotateView(Context context, AttributeSet attrs) {
        this(context, attrs ,0);
    }

    public RotateView(Context context) {
        this(context, null);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (mClipBitmap == null){
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            return;
        }

            int width = 0;
            int height = 0;

            int widthMode = MeasureSpec.getMode(widthMeasureSpec);
            int heightMode = MeasureSpec.getMode(heightMeasureSpec);

            int widthSize = MeasureSpec.getSize(widthMeasureSpec);
            int heightSize = MeasureSpec.getSize(heightMeasureSpec);

            if (widthMode == MeasureSpec.EXACTLY){
                width = widthSize;
            }else {
            width = mClipBitmap.getWidth();
            //子view不能大于父类
            if (widthMode == MeasureSpec.AT_MOST){
                width = Math.min(width,widthSize);
            }
        }

        if (heightMode == MeasureSpec.EXACTLY) {
            height = heightSize;
        } else {
            height = mClipBitmap.getHeight();
            if (heightMode == MeasureSpec.AT_MOST) {
                height = Math.min(height, heightSize);
            }
        }
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mClipBitmap == null)
            return;

        canvas.save();

        mMatrix.setRotate(mRotation, (float) (App.sScreenWidth/10*1.5), (float) (App.sScreenWidth/10*1.5));
        canvas.drawBitmap(mClipBitmap,mMatrix,null);
        canvas.restore();

        int center = (int)(App.sScreenWidth/2*scale);//圆心的x坐标
        int radius = (int)(center-ringWidth/2);

        /**
         * 画最外层的大圆环
         */
        paint.setColor(ringColor);//设置圆环的颜色
        paint.setStyle(Paint.Style.STROKE);//设置空心
        paint.setStrokeWidth(ringWidth); //设置圆环的宽度
        paint.setAntiAlias(true);  //消除锯齿
        canvas.drawCircle(center, center, radius, paint); //画出圆环


        paint.setStrokeWidth(ringWidth);
        paint.setColor(ringProgressColor);
        RectF oval = new RectF(center - radius, center - radius, center
                + radius, center + radius);
        paint.setStyle(Paint.Style.STROKE);
        canvas.drawArc(oval, 0, mRotation, false, paint);  //根据进度画圆弧
    }

    private Bitmap cretaeCircleBitmap(Bitmap src){
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setARGB(255,241,239,229);

        Bitmap target = Bitmap.createBitmap(getMeasuredWidth(),
                getMeasuredHeight(), Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(target);
        canvas.drawCircle(getMeasuredWidth() / 2, getMeasuredWidth() / 2,
                (float) getMeasuredWidth()/2-ringWidth, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(src, 0, 0, paint);

        return target;
    }

    public void setCdImage(Bitmap bitmap,double scale){
        this.scale = scale;
        bitmap = ImageTools.scaleBitmap(bitmap,
                (int) (App.sScreenWidth * scale));
        int widthSize = bitmap.getWidth();
        int heightSize = bitmap.getHeight();
        int widthSpec = MeasureSpec.makeMeasureSpec(widthSize,
                MeasureSpec.AT_MOST);
        int heightSpec = MeasureSpec.makeMeasureSpec(heightSize,
                MeasureSpec.AT_MOST);

        measure(widthSpec, heightSpec);
        mClipBitmap = cretaeCircleBitmap(bitmap);
        requestLayout();
        invalidate();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        isRunning = false;
    }

    public void startRoll(){
        if (isRunning)
            return;

        isRunning = true;
        mHandler.sendEmptyMessageDelayed(MSG_RUN, TIME_UPDATE);
    }

    //暂停旋转
    public void pause() {
        if (!isRunning)
            return;
        isRunning = false;
    }

    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            if (msg.what == MSG_RUN) {
                if (isRunning) {
                    if (mRotation >= 360)
                        mRotation = 0;
                    invalidate();
                    sendEmptyMessageDelayed(MSG_RUN, TIME_UPDATE);
                }
            }
        }
    };

    public void rotate(float angle){
        this.mRotation = (float) (angle*3.6);
    }

    /*@Override
    public boolean onTouchEvent(MotionEvent event) {

        int x = (int) event.getX();
        int y = (int) event.getY();

        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                break;
            case MotionEvent.ACTION_MOVE:
                break;
            case MotionEvent.ACTION_UP:
                break;
        }
        return super.onTouchEvent(event);
    }*/

}

