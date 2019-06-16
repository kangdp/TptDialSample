package com.kdp.tptdial;

import android.animation.ArgbEvaluator;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

/***
 * @author kdp
 * @date 2019/6/15 9:27
 * @description
 */
public class TptDialView extends View {
    private static final String TAG = TptDialView.class.getSimpleName();
    /**
     * 内圆半径
     */
    private float mInnerRadius;
    /**
     * 刻度线高度
     */
    private float mTickMarkHeight;
    /**
     * 刻度线起始颜色
     */
    private int mTickMarkStartColor;
    /**
     * 刻度线结束颜色
     */
    private int mTickMarkEndColor;
    /**
     * 选中的刻度线高度
     */
    private float mSelTickMarkHeight;
    /**
     * 绘制刻度的开始角度
     */
    private int mTickMarkStartAngle;
    /**
     * 所有刻度扫过的角度
     */
    private int mTickMarkSweepAngle;
    /**
     * 每一个刻度的角度
     */
    private float mTickMarkAngle;
    /**
     * 刻度之间的间隔角度
     */
    private int mTickMarkSpaceAngle;

    private Drawable mThumbDrawable;

    /**
     * 总刻度数量
     */
    private int mTickMarkCount;

    /**
     * 实现颜色渐变过渡的类
     */
    private ArgbEvaluator argbEvaluator;

    /**
     * 刻度盘外接矩形
     */
    private RectF mRectangle;

    /**
     * 当前刻度指针的位置 (介于 0 -> 总刻度数量 - 1)
     */
    private int mCurrentPosition;
    /**
     * 当前刻度指针偏移的角度
     */
    private int mCurrentTickMarkOffsetAngle;

    private Paint mPaint;

    public TptDialView(Context context) {
        this(context,null);
    }

    public TptDialView(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public TptDialView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray typedArray = context.obtainStyledAttributes(attrs,R.styleable.TptDialView,defStyleAttr,0);
        mInnerRadius = typedArray.getDimension(R.styleable.TptDialView_InnerRadius,0);
        mTickMarkHeight = typedArray.getDimension(R.styleable.TptDialView_TickMarkHeight,0);
        mTickMarkStartColor = typedArray.getColor(R.styleable.TptDialView_TickMarkStartColor,0x000);
        mTickMarkEndColor = typedArray.getColor(R.styleable.TptDialView_TickMarkEndColor,0xfff);
        mSelTickMarkHeight = typedArray.getDimension(R.styleable.TptDialView_SelTickMarkHeight,0);
        mTickMarkAngle = typedArray.getFloat(R.styleable.TptDialView_TickMarkAngle,0);
        mTickMarkSpaceAngle = typedArray.getInt(R.styleable.TptDialView_TickMarkSpaceAngle,0);
        mTickMarkStartAngle = typedArray.getInt(R.styleable.TptDialView_TickMarkStartAngle,0);
        mTickMarkSweepAngle = typedArray.getInt(R.styleable.TptDialView_TickMarkSweepAngle, 0);
        mThumbDrawable = typedArray.getDrawable(R.styleable.TptDialView_Thumb);
        mCurrentPosition = typedArray.getInt(R.styleable.TptDialView_CurPosition,0);

        //回收资源
        typedArray.recycle();

        Log.e(TAG, "TptDialView: thume width = " + mThumbDrawable.getIntrinsicWidth());
        Log.e(TAG, "TptDialView: thume height = " + mThumbDrawable.getIntrinsicHeight());
        //初始化画笔
        initPaint();
        //计算总刻度数量
        mTickMarkCount = Math.round((mTickMarkSweepAngle - mTickMarkAngle) * 1.0F / (mTickMarkAngle + mTickMarkSpaceAngle)) + 1;
        //初始化颜色渐变类
        argbEvaluator = new ArgbEvaluator();


        if (mCurrentPosition < 0)
            mCurrentPosition = 0;
        if (mCurrentPosition > mTickMarkCount - 1){
            throw new IndexOutOfBoundsException("The current position is out of bounds");
        }
    }

    private void computeRectangle() {
        int left,width;
        if (mRectangle == null){
           mRectangle = new RectF();
        }
        left =  (int) (mSelTickMarkHeight - mTickMarkHeight + mThumbDrawable.getIntrinsicHeight());
        width = (int) ((mInnerRadius+mTickMarkHeight) * 2);
        mRectangle.set(left,left,left+width,left+width);
    }


    private void initPaint() {
        mPaint = new Paint();
        //抗锯齿
        mPaint.setAntiAlias(true);
        //防抖动
        mPaint.setDither(true);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
//
//        if (widthMode == MeasureSpec.AT_MOST && heightMode == MeasureSpec.AT_MOST){
//            //重新计算圆盘的尺寸
//         width = height = Math.round((mSelTickMarkHeight + mInnerRadius + mThumbRadius*2) * 2);
//        }else {
//            //取最小长度为圆盘的尺寸
//            width = height = Math.min(width,height);
//        }
        Log.e(TAG, "onMeasure: bitmap width = " + mThumbDrawable.getIntrinsicWidth());
        width = height = (int) (mSelTickMarkHeight + mInnerRadius + mThumbDrawable.getIntrinsicWidth()) * 2;
        Log.e(TAG, "onMeasure: width = " + width);
        setMeasuredDimension(width,height);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        computeRectangle();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.save();
        canvas.drawColor(Color.GRAY);
        //绘制所有刻度
        drawAllTickMarks(canvas);
        //绘制刻度指针
//        drawCurTickMark(canvas);
        //绘制内圆
        drawInnerCircle(canvas);
        //绘制指针上的Thumb
//        drawThumb(canvas);
    }


    public void setCurPosition(int index){
        mCurrentPosition = index;
        invalidate();
    }

    public int getMaxValue(){
        return mTickMarkCount;
    }


    /**
     * 绘制刻度
     * @param canvas
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void drawAllTickMarks(Canvas canvas) {
        canvas.restore();
        canvas.rotate(mTickMarkStartAngle,getWidth()/2,getHeight()/2);
        mPaint.setStyle(Paint.Style.FILL);
        //先绘制开始和结束位置的刻度线
        for (int i = 0;i < mTickMarkCount;i++){
            mPaint.setColor(getTickMarkColor(i));
            if (i > 0){
                canvas.rotate(mTickMarkSpaceAngle+mTickMarkAngle,getWidth()/2,getHeight()/2);
            }
            int mTickMarkOffsetAngle = computeTickMarkOffsetAngle(i);
            if (i == mCurrentPosition){
                mCurrentTickMarkOffsetAngle = mTickMarkOffsetAngle;
                //绘制刻度指针
                canvas.drawArc(0,0,getWidth()-mThumbDrawable.getIntrinsicWidth(),getHeight()-mThumbDrawable.getIntrinsicHeight(),0,mTickMarkAngle,true,mPaint);
                BitmapDrawable bitmapDrawable = (BitmapDrawable) mThumbDrawable;
                Bitmap bitmap = bitmapDrawable.getBitmap();
                int left = getWidth() - mThumbDrawable.getIntrinsicWidth();
                int top = getHeight()/2 - mThumbDrawable.getIntrinsicWidth()/2;
                int thumbWidth = mThumbDrawable.getIntrinsicWidth();
                int thumbHeight = mThumbDrawable.getIntrinsicHeight();
                canvas.drawBitmap(bitmap,null,new Rect(left,top,left+thumbWidth,top+thumbHeight),mPaint);
            }else {
                //绘制所有刻度
                canvas.drawArc(mRectangle,0,mTickMarkAngle,true,mPaint);
            }
        }
    }



    /**
     * 绘制内圆
     * @param canvas
     */
    private void drawInnerCircle(Canvas canvas) {
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(Color.WHITE);
        canvas.drawCircle(getWidth()/2F,getHeight()/2F,getHeight()/2f-mSelTickMarkHeight-mThumbDrawable.getIntrinsicHeight(),mPaint);
    }

    /**
     * 绘制Thumb
     * @param canvas
     */
    private void drawThumb(Canvas canvas) {
        //计算thumb的圆心坐标
//        computeThumbCenter();

    }

    /**
     * 计算thumb的原点坐标
     */
//    private void computeThumbCenter() {
//        int curAngle = (mTickMarkStartAngle + mCurrentTickMarkOffsetAngle) % 360;
//        Log.e(TAG, "computeThumbCenter: curAngle = " + curAngle );
//        float radius = (float) getWidth()/2 - mThumbDiameter;
//        double radian = Math.PI * 2 / 360 * curAngle;
//        double distanceX = radius * Math.cos(radian);
//        double distanceY = radius * Math.sin(radian);
//        Log.e(TAG, "computeThumbCenter: x = " + distanceX);
//        Log.e(TAG, "computeThumbCenter: y = " + distanceY);
//        thumbCenter[0] = (float) (getWidth() / 2 + distanceX);
//        thumbCenter[1] = (float) (getHeight() / 2 + distanceY);
//    }


    /**
     * 计算刻度偏移量
     * @param index
     * @return
     */
    private int computeTickMarkOffsetAngle(int index){
        return (int) (index * (mTickMarkSpaceAngle + mTickMarkAngle));
    }

    /**
     * 获取当前刻度颜色
     * @param index
     */
    private int getTickMarkColor(int index){
        return (int) argbEvaluator.evaluate((float) (index+1) / mTickMarkCount,mTickMarkStartColor,mTickMarkEndColor);
    }

}
