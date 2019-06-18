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
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;

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
    private OnSlideChangedListener onSlideChangedListener;
    private Paint mPaint;

    private boolean mSlidable = false;//是否可以继续滑动
    float moveX=0,moveY=0;//触摸点坐标

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
        notifyDataChanged();
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

        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                mSlidable = true;
            case MotionEvent.ACTION_MOVE:
                moveX = event.getX();
                moveY = event.getY();
                float originDistance = calculateDistance(moveX,moveY);
                int degree = calculatePointAngle(moveX,moveY,originDistance);
                if (!checkDialContainsPoint(originDistance,degree)){
                    mSlidable = false;
                }
                if (mSlidable) {
                    //触摸点在刻度盘上的位置
                    mCurrentPosition = (int) ((degree - mTickMarkStartAngle - mTickMarkAngle) / (mTickMarkAngle + mTickMarkSpaceAngle));
                    notifyDataChanged();

                }
                break;
        }
        return true;
    }

    /**
     * 通知View刷新
     */
    private void notifyDataChanged(){
        invalidate();
        if (onSlideChangedListener == null) return;
        if (mCurrentPosition == 0){
            onSlideChangedListener.onSlideChanged(mCurrentPosition,mMinValue);
        }else {
            onSlideChangedListener.onSlideChanged(mCurrentPosition,mMinValue + (mCurrentPosition+1) * mAverageValue);
        }
    }

    /**
     * 计算触摸点到原点的距离
     * @param moveX
     * @param moveY
     * @return
     */
    private float calculateDistance(float moveX, float moveY){
      return  (float) Math.sqrt(Math.pow(Math.abs(moveX - getWidth() * 0.5f),2) + Math.pow(Math.abs(moveY - getHeight() * 0.5f),2));
    }

    /**
     * 计算触摸点到原点的直线与三点钟方向的夹角
     * @param moveX 触摸点x坐标
     * @param moveY 触摸点y坐标
     * @param distance 触摸点与圆心的距离
     */
    private int calculatePointAngle(float moveX, float moveY,float distance) {
        float trigleValue;
        int degree = 0;
        if (moveX >= getWidth() * 0.5 && moveY <= getHeight() * 0.5){
            //第一象限
            trigleValue = (moveX - getWidth() * 0.5f) / distance;
            degree = 360 - (int) Math.round (Math.acos(trigleValue) * (180 / Math.PI));
        }else if (moveX < getWidth() * 0.5 && moveY <= getHeight() * 0.5){
            //第二象限
            trigleValue = (getWidth() * 0.5f - moveX) / distance;
            degree = 180 + (int) Math.round (Math.acos(trigleValue) * (180 / Math.PI));
        }else if (moveX < getWidth() * 0.5 && moveY > getHeight() * 0.5){
            //第三象限
            trigleValue = (getWidth() * 0.5f - moveX) / distance;
            degree = 180 - (int) Math.round (Math.acos(trigleValue) * (180 / Math.PI));
        }else if (moveX >= getWidth() * 0.5 && moveY > getHeight() * 0.5){
            //第四象限
            trigleValue = (moveX - getWidth() * 0.5f ) / distance;
            degree = (int) Math.round (Math.acos(trigleValue) * (180 / Math.PI));
            if (degree < mTickMarkStartAngle){
                degree += 360;
            }
        }
        return degree;
    }

    /**
     * 检查触摸点是否在刻度上
     * @param distance
     * @return
     */
    private boolean checkDialContainsPoint(float distance,int degree) {
        return  distance > mInnerRadius && distance < getWidth() / 2 && (degree >= mTickMarkStartAngle && degree <= mTickMarkSweepAngle+mTickMarkStartAngle);
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


    public void setOnSlideChangedListener(OnSlideChangedListener listener){
        this.onSlideChangedListener = listener;
    }

    /**
     * 触摸滑动回调
     */
    public interface OnSlideChangedListener{
        void onSlideChanged(int position,float value);
    }

    /**
     * 保存当前状态
     * @return
     */
    @Override
    protected Parcelable onSaveInstanceState() {
        Parcelable parcelable = super.onSaveInstanceState();
        SavedState savedState = new SavedState(parcelable);
        savedState.mCurrentPosition = mCurrentPosition;
        return savedState;
    }

    /**
     * 恢复状态
     * @param state
     */
    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        SavedState savedState = (SavedState) state;
        super.onRestoreInstanceState(savedState.getSuperState());
        setCurPosition(savedState.mCurrentPosition);
    }

    /**
     * 此类用来存储、恢复当前View的状态
     */
    static class SavedState extends BaseSavedState {
        int mCurrentPosition;
        SavedState(Parcel in) {
            super(in);
            mCurrentPosition = in.readInt();
        }

        SavedState(Parcelable superState) {
            super(superState);
        }
        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeInt(mCurrentPosition);
        }

        public static final Parcelable.Creator<SavedState> CREATOR
                = new Parcelable.ClassLoaderCreator<SavedState>() {
            @Override
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }
            @Override
            public SavedState createFromParcel(Parcel in, ClassLoader loader) {
                return new SavedState(in);
            }

            @Override
            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }
}
