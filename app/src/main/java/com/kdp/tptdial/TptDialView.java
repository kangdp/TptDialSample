package com.kdp.tptdial;

import android.animation.ArgbEvaluator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
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
     * 绘制刻度的结束角度
     */
    private int mTickMarkEndAngle;
    /**
     * 每一个刻度的角度
     */
    private int mTickMarkAngle;
    /**
     * 刻度之间的间隔角度
     */
    private int mTickMarkSpaceAngle;
    /**
     * thumb半径
     */
    private float mThumbRadius;
    /**
     * thumb直径
     */
    private float mThumbDiameter;
    /**
     * thumb颜色
     */
    private float mThumbColor;
    /**
     * thumb边框宽度
     */
    private float mThumbStrokeWidth;
    /**
     * thumb边框颜色
     */
    private float mThumbStrokeColor;

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
    private Rect mRectangle;

    /**
     * 当前刻度指针的位置 (介于 0 -> 总刻度数量 - 1)
     */
    private int mCurrentPosition;

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
        mTickMarkAngle = typedArray.getInt(R.styleable.TptDialView_TickMarkAngle,0);
        mTickMarkSpaceAngle = typedArray.getInt(R.styleable.TptDialView_TickMarkSpaceAngle,0);
        mTickMarkStartAngle = typedArray.getInt(R.styleable.TptDialView_TickMarkStartAngle,0);
        mTickMarkEndAngle = typedArray.getInt(R.styleable.TptDialView_TickMarkEndAngle, 0);
        mCurrentPosition = typedArray.getInt(R.styleable.TptDialView_CurPosition,0);
        mThumbRadius = typedArray.getDimension(R.styleable.TptDialView_ThumbRadiu,0);
        mThumbColor = typedArray.getDimension(R.styleable.TptDialView_ThumbColor,0xFFF);
        mThumbStrokeWidth = typedArray.getDimension(R.styleable.TptDialView_ThumbStrokeWidth,0);
        mThumbStrokeColor = typedArray.getDimension(R.styleable.TptDialView_ThumbStrokeColor,0x000);
        //回收资源
        typedArray.recycle();

        //初始化画笔
        initPaint();
        //计算总刻度数量
        mTickMarkCount = Math.round((mTickMarkEndAngle - mTickMarkStartAngle - mTickMarkAngle) * 1.0F / (mTickMarkAngle + mTickMarkSpaceAngle)) + 1;
        //初始化颜色渐变类
        argbEvaluator = new ArgbEvaluator();


        if (mCurrentPosition < 0)
            mCurrentPosition = 0;
        if (mCurrentPosition > mTickMarkCount - 1){
            throw new IndexOutOfBoundsException("The current position is out of bounds");
        }
        //直径
        mThumbDiameter = mThumbRadius * 2;
    }

    private void computeRectangle() {
        int left,width;
        if (mRectangle == null){
           mRectangle = new Rect();
        }
        left =  (int) (mSelTickMarkHeight - mTickMarkHeight + mThumbDiameter);
        width = (int) ((mInnerRadius+mTickMarkHeight) * 2);
        mRectangle.set(left,left,left+width,left+width);
    }


    private void initPaint() {
        mPaint = new Paint();
        //抗锯齿
        mPaint.setAntiAlias(true);
        //防抖动
        mPaint.setDither(true);
        //设置画笔颜色
        mPaint.setColor(Color.RED);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);

        if (widthMode == MeasureSpec.AT_MOST && heightMode == MeasureSpec.AT_MOST){
            //重新计算圆盘的尺寸
         width = height = Math.round((mSelTickMarkHeight + mInnerRadius + mThumbRadius*2) * 2);
        }else {
            //取最小长度为圆盘的尺寸
            width = height = Math.min(width,height);
        }
        setMeasuredDimension(width,height);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        computeRectangle();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawColor(Color.WHITE);

        //绘制所有刻度
        drawAllTickMarks(canvas);
        //绘制刻度指针
        drawCurTickMark(canvas);
        //绘制内圆
        drawInnerCircle(canvas);
        //绘制指针上的Thumb
        drawThumb(canvas);
    }



    /**
     * 绘制所有刻度
     * @param canvas
     */
    private void drawAllTickMarks(Canvas canvas) {

        //先绘制开始和结束位置的刻度线
        for (int i = 0;i < mTickMarkCount;i++){
            mPaint.setColor(getTickMarkColor(i));
            int mTickMarkOffsetAngle = computeTickMarkOffsetAngle(i);
            canvas.drawArc(mRectangle.left,mRectangle.top,mRectangle.right,mRectangle.bottom,mTickMarkStartAngle + mTickMarkOffsetAngle,mTickMarkAngle,true,mPaint);
        }
    }

    /**
     * 绘制刻度指针
     * @param canvas
     */
    private void drawCurTickMark(Canvas canvas) {
        int mTickMarkOffsetAngle = computeTickMarkOffsetAngle(mCurrentPosition);
        mPaint.setColor(getTickMarkColor(mCurrentPosition));
        canvas.drawArc(mThumbDiameter,mThumbDiameter,getWidth()-mThumbDiameter,getHeight()-mThumbDiameter,mTickMarkStartAngle + mTickMarkOffsetAngle,mTickMarkAngle,true,mPaint);
    }


    /**
     * 绘制内圆
     * @param canvas
     */
    private void drawInnerCircle(Canvas canvas) {
        mPaint.setColor(Color.WHITE);
        canvas.drawCircle(getWidth()/2,getHeight()/2,getHeight()/2-mSelTickMarkHeight-mThumbDiameter,mPaint);
    }

    /**
     * 绘制Thumb
     * @param canvas
     */
    private void drawThumb(Canvas canvas) {

    }


    /**
     * 计算刻度偏移量
     * @param index
     * @return
     */
    private int computeTickMarkOffsetAngle(int index){
        return index * (mTickMarkSpaceAngle + mTickMarkAngle);
    }

    /**
     * 获取当前刻度颜色
     * @param index
     */
    private int getTickMarkColor(int index){
        return (int) argbEvaluator.evaluate((float) (index+1) / mTickMarkCount,mTickMarkStartColor,mTickMarkEndColor);
    }

}
