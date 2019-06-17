package com.kdp.tptdial;
import android.animation.ArgbEvaluator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathEffect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
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

    /**
     * 总刻度数量
     */
    private int mTickMarkCount;
    /**
     * Thumb属性
     */
    private float mThumbRadius;
    private int mThumbColor;
    private float mThumbStrokeWidth;
    private int mThumbStrokeColor;
    private int mThumbShadowColor;
    private float mThumbShadowRadius;
    /**
     * 虚线颜色
     */
    private int mDottedColor;

    /**
     * 实现颜色渐变过渡的类
     */
    private ArgbEvaluator argbEvaluator;

    /**
     * 刻度盘外接矩形
     */
    private RectF mRectangle;
    /**
     * 刻度指针外接矩形
     */
    private RectF mSelRectangle;

    /**
     * 虚线外接矩形
     */
    private RectF mDottedRectangle;

    /**
     * 当前刻度指针的位置 (介于 0 -> 总刻度数量 - 1)
     */
    private int mCurrentPosition;
    /**
     * 刻度值
     */
    private float mMinValue,mMaxValue;//默认为刻度下标
    private float mAverageValue;//每个刻度的数值
    private OnPointChangedListener onPointChangedListener;
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
        mThumbRadius = typedArray.getDimension(R.styleable.TptDialView_ThumbRadius,0);
        mThumbColor = typedArray.getColor(R.styleable.TptDialView_ThumbColor,0x000);
        mThumbStrokeWidth = typedArray.getDimension(R.styleable.TptDialView_ThumbStrokeWidth,0);
        mThumbStrokeColor = typedArray.getColor(R.styleable.TptDialView_ThumbStrokeColor,0x000);
        mThumbShadowColor = typedArray.getColor(R.styleable.TptDialView_ThumbShadowColor,0xFFF);
        mThumbShadowRadius = typedArray.getDimension(R.styleable.TptDialView_ThumbShadowRadius,0);
        mDottedColor = typedArray.getColor(R.styleable.TptDialView_DottedColor,0x000);
        mCurrentPosition = typedArray.getInt(R.styleable.TptDialView_CurPosition,0);
        mMinValue = typedArray.getFloat(R.styleable.TptDialView_MinValue,0);

        //计算总刻度数量
        mTickMarkCount = (int) ((mTickMarkSweepAngle - mTickMarkAngle) / (mTickMarkAngle + mTickMarkSpaceAngle)) + 1;
        mMaxValue = typedArray.getFloat(R.styleable.TptDialView_MaxValue,mTickMarkCount - 1);
        checkDialValue();
        //回收资源
        typedArray.recycle();

        if (mCurrentPosition < 0)
            mCurrentPosition = 0;
        if (mCurrentPosition > mTickMarkCount - 1){
            throw new IndexOutOfBoundsException("The current position is out of bounds");
        }

        if (mThumbRadius <= 0){
            mThumbRadius = 0;
            mThumbShadowRadius = 0;
            mThumbStrokeWidth = 0;
        }
        if (mThumbShadowRadius > 0){
            setLayerType(LAYER_TYPE_SOFTWARE, null);
        }

        //初始化画笔
        initPaint();

        //初始化颜色渐变类
        argbEvaluator = new ArgbEvaluator();
    }

    /**
     * 检查数值的合法性
     */
    private void checkDialValue() {
        if (mMaxValue < mMinValue)
            throw new RuntimeException("MaxValue cannot be smaller than MinValue");

        //设置每次刻度的数值
        mAverageValue = (mMaxValue - mMinValue) / mTickMarkCount;
    }

    /**
     * 设置数值
     * @param minValue 最小值
     * @param maxValue 最大值
     */
    public void setDialValue(float minValue,float maxValue){
        this.mMinValue = minValue;
        this.mMaxValue = maxValue;
        checkDialValue();
    }

    private void computeRectangle(int w,int h) {
        int left,width;
        if (mRectangle == null){
           mRectangle = new RectF();
        }
        if (mSelRectangle == null){
            mSelRectangle = new RectF();
        }

        if (mDottedRectangle == null){
            mDottedRectangle = new RectF();
        }
        left =  (int) (mSelTickMarkHeight - mTickMarkHeight + mThumbRadius * 2 + mThumbShadowRadius);
        width = w - left * 2;
        mRectangle.set(left,left,left+width,left+width);
        mSelRectangle.set(mThumbRadius*2 + mThumbShadowRadius,mThumbRadius*2 + mThumbShadowRadius,w-mThumbShadowRadius-mThumbRadius * 2,h-mThumbShadowRadius-mThumbRadius * 2);
        int dottedLeft = (int) (w/2 - mInnerRadius + mInnerRadius / 5);
        mDottedRectangle.set(dottedLeft,dottedLeft,getWidth() - dottedLeft,getHeight() - dottedLeft);
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
        int width,height;
        width = height = (int) (mSelTickMarkHeight + mInnerRadius +mThumbRadius*2 + mThumbShadowRadius) * 2;
        setMeasuredDimension(width,height);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        computeRectangle(w,h);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.save();
        //绘制所有刻度
        drawAllTickMarks(canvas);
        //绘制内圆
        drawInnerCircle(canvas);
        //绘制园内虚线
        drawDottedArc(canvas);
    }

    /**
     * 绘制圆弧虚线
     * @param canvas
     */
    private void drawDottedArc(Canvas canvas) {
        canvas.restore();
        if (mTickMarkSweepAngle < 360){
            //裁剪需要绘制虚线的部分
            Path clipPath = new Path();
            clipPath.addArc(mDottedRectangle,mTickMarkStartAngle,mTickMarkSweepAngle);
            canvas.clipPath(clipPath);
        }
        //绘制虚线圆
        PathEffect dashPathEffect = new DashPathEffect(new float[]{10,20},0);
        mPaint.setPathEffect(dashPathEffect);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(2);
        mPaint.setColor(mDottedColor);
        canvas.drawCircle(getWidth() * 0.5f,getHeight() * 0.5f,mDottedRectangle.width() / 2,mPaint);

    }

    /**
     * 设置当前刻度指针的位置
     * @param index
     */
    public void setCurPosition(int index){
        mCurrentPosition = index;
        invalidate();
    }

    public float getMaxValue(){
        return mMaxValue;
    }

    public float getMinValue(){
        return mMinValue;
    }


    /**
     * 绘制刻度
     * @param canvas
     */
    private void drawAllTickMarks(Canvas canvas) {
        canvas.rotate(mTickMarkStartAngle % 360,getWidth() * 0.5f,getHeight() * 0.5f);
        mPaint.setStyle(Paint.Style.FILL);
        canvas.drawArc(mRectangle,-mTickMarkAngle/2,mTickMarkAngle,true,mPaint);
        //先绘制开始和结束位置的刻度线
        for (int i = 0;i < mTickMarkCount;i++){
            mPaint.setColor(getTickMarkColor(i));
            if (i > 0){
                canvas.rotate(mTickMarkSpaceAngle+mTickMarkAngle,getWidth() * 0.5f,getHeight() * 0.5f);
            }
            if (i == mCurrentPosition){
                //绘制刻度指针
                canvas.drawArc(mSelRectangle,-mTickMarkAngle/2,mTickMarkAngle,true,mPaint);
                //绘制thumb
                drawThumb(canvas);
            }else {
                //绘制所有刻度
                canvas.drawArc(mRectangle,-mTickMarkAngle/2,mTickMarkAngle,true,mPaint);
            }
        }
    }

    /**
     * 绘制Thumb
     * @param canvas
     */
    private void drawThumb(Canvas canvas) {
        if (mThumbRadius > 0) {
            if (mThumbShadowRadius > 0){
                //绘制阴影
                mPaint.setShadowLayer(mThumbShadowRadius,0,0,mThumbShadowColor);
            }

            //绘制实心圆
            mPaint.setStyle(Paint.Style.FILL);
            mPaint.setColor(mThumbColor);
            canvas.drawCircle(getWidth()-mThumbRadius-mThumbShadowRadius,getHeight() * 0.5f,mThumbRadius - mThumbStrokeWidth,mPaint);

            mPaint.clearShadowLayer();
            if (mThumbStrokeWidth > 0){
                //绘制边框
                mPaint.setStyle(Paint.Style.STROKE);
                mPaint.setStrokeWidth(mThumbStrokeWidth);
                mPaint.setColor(mThumbStrokeColor);
                canvas.drawCircle(getWidth()-mThumbRadius-mThumbShadowRadius,getHeight() * 0.5f,mThumbRadius,mPaint);
            }

            mPaint.setStyle(Paint.Style.FILL);

        }


    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
       float downX = event.getX();
       float downY = event.getY();
        float distance = (float) Math.sqrt(Math.pow(Math.abs(downX - getWidth() * 0.5f),2) + Math.pow(Math.abs(downY - getHeight() * 0.5f),2));
      if (checkDialContainsPoint(downX,downY,distance)){

          //计算触摸点夹角
         int degree = calculatePointAngle(downX,downY,distance);
         //检测触摸角度是否在刻度盘的范围之内
          if (checkDialContaonsPoint(degree)){
              mCurrentPosition = (int) ((degree - mTickMarkStartAngle - mTickMarkAngle) / (mTickMarkAngle + mTickMarkSpaceAngle));
              invalidate();
              if (onPointChangedListener!=null){
                   if (mCurrentPosition == 0){
                       onPointChangedListener.onChanged(mCurrentPosition,mMinValue);
                   }else {
                       onPointChangedListener.onChanged(mCurrentPosition,mMinValue + (mCurrentPosition+1) * mAverageValue);
                   }
              }
          }
      }
        return true;
    }

    /**
     * 计算触摸点到原点的直线与三点中方向的夹角
     * @param downX 触摸点x坐标
     * @param downY 触摸点y坐标
     * @param distance 触摸点与圆心的距离
     */
    private int calculatePointAngle(float downX, float downY,float distance) {
        float trigleValue;
        int degree = 0;
        if (downX >= getWidth() * 0.5 && downY <= getHeight() * 0.5){
            //第一象限
            trigleValue = (downX - getWidth() * 0.5f) / distance;
            degree = 360 - (int) Math.round (Math.acos(trigleValue) * (180 / Math.PI));
        }else if (downX < getWidth() * 0.5 && downY <= getHeight() * 0.5){
            //第二象限
            trigleValue = (getWidth() * 0.5f - downX) / distance;
            degree = 180 + (int) Math.round (Math.acos(trigleValue) * (180 / Math.PI));
        }else if (downX < getWidth() * 0.5 && downY > getHeight() * 0.5){
            //第三象限
            trigleValue = (getWidth() * 0.5f - downX) / distance;
            degree = 180 - (int) Math.round (Math.acos(trigleValue) * (180 / Math.PI));
        }else if (downX >= getWidth() * 0.5 && downY > getHeight() * 0.5){
            //第四象限
            trigleValue = (downX - getWidth() * 0.5f ) / distance;

            degree = (int) Math.round (Math.acos(trigleValue) * (180 / Math.PI));
            if (degree < mTickMarkStartAngle){
                degree += 360;
            }
        }
        return degree;
    }

    /**
     * 判断该触摸点是否在刻度盘的有效范围之内
     *
     */
    private boolean checkDialContaonsPoint(int degree) {
       return degree >= mTickMarkStartAngle && degree <= mTickMarkSweepAngle+mTickMarkStartAngle;
    }

    /**
     * 检查触摸点是否在刻度盘上
     * @param downX
     * @param downY
     * @param distance
     * @return
     */
    private boolean checkDialContainsPoint(float downX, float downY,float distance) {
        return mSelRectangle.contains(downX,downY) && distance > mInnerRadius;
    }

    /**
     * 绘制内圆
     * @param canvas
     */
    private void drawInnerCircle(Canvas canvas) {
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(Color.WHITE);
        canvas.drawCircle(getWidth()/2F,getHeight()/2F,getWidth()/2f-mSelTickMarkHeight-mThumbRadius*2-mThumbShadowRadius,mPaint);
    }

    /**
     * 获取当前刻度颜色
     * @param index
     */
    private int getTickMarkColor(int index){
        return (int) argbEvaluator.evaluate((float) (index+1) / mTickMarkCount,mTickMarkStartColor,mTickMarkEndColor);
    }


    public void setOnPointChangedListener(OnPointChangedListener listener){
        this.onPointChangedListener = listener;
    }

    public interface OnPointChangedListener{
        void onChanged(int position,float value);
    }

}
