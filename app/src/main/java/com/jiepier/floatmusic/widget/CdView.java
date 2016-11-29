package com.jiepier.floatmusic.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import com.jiepier.floatmusic.R;


/**
 * Created by prj on 2016/4/13.
 */
public class CdView extends View{

    private Paint paint;
    private int ringColor;
    private float ringWidth;

    private Matrix mMatrix;

    public CdView(Context context) {
        this(context,null);
    }

    public CdView(Context context, AttributeSet attrs) {
        this(context,attrs,0);
    }

    public CdView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        paint = new Paint();

        TypedArray mTypedArray = context.obtainStyledAttributes(attrs, R.styleable.RoundProgressBar);
        ringColor = mTypedArray.getColor(R.styleable.RoundProgressBar_ringColor,0xff50c0e9);
        ringWidth = mTypedArray.getDimension(R.styleable.RoundProgressBar_ringWidth, 20);
        mTypedArray.recycle();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int center = getWidth()/2;//获取圆心的x坐标
        int radius = (int) (center-ringWidth/2);//圆环的半径???????

        /**
         * 画最外层的大圆环
         */
        paint.setColor(ringColor);//设置圆环的颜色
        paint.setStyle(Paint.Style.STROKE);//设置空心
        paint.setStrokeWidth(ringWidth); //设置圆环的宽度
        paint.setAntiAlias(true);  //消除锯齿
        canvas.drawCircle(center, center, radius, paint); //画出圆环
    }

    public void setRingColor(int ringColor) {
        this.ringColor = ringColor;
    }

    public void setRingWidth(int ringWidth){
        this.ringWidth = ringWidth;
    }
}
