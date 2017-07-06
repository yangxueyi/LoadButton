package com.example.loadbutton;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

/**
 * Created by Zhang
 * Time 2017/7/3.
 */

public class LoadButton extends View {
    private static final String TAG = "LoadButton";

    //圆默认的半径
    private int mDefaultRadius;
    //文本默认的文字大小
    private int mDefaultTextSize;
    //设置字体打大小
    private int mTextSize;
    private int mStrokeColor;
    //字体颜色
    private int mTextColor;
    //背景色
    private int mBackgroundColor;
    //进度条颜色
    private int mProgressColor;
    private int mProgressSecondColor;
    //文本
    private String mText;
    //两边圆半径
    private int mRadiu;
    //进度条宽度
    private int mProgressedWidth;
    //左右的padding
    private int mLeftRightPadding;
    //上下的padding
    private int mTopBottomPadding;
    //成功时显示的图片
    private Drawable mSuccessedDrawable;
    //失败时显示的图片
    private Drawable mErrorDrawable;
    //暂停时显示的图片
    private Drawable mPauseDrawable;
    //进度条画笔
    private Paint mPaint;
    //设置默认宽度  :  总宽度   =   矩形宽度  +  圆的直径
    private int mDefaultWidth;
    //文字画笔
    private TextPaint mTextPaint;
    //中间矩形宽度
    private int rectWidth;
    //左边半圆的矩阵
    private RectF leftRectF;
    //右边半圆的矩阵
    private RectF rightRectF;
    //中间矩形的矩阵
    private RectF contentRectF;
    private boolean isUnfold;
    private OnClickListener mListener;
    //文字宽度
    private int mTextWidth;


    public LoadButton(Context context) {
        this(context,null);
    }
    public LoadButton(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
    }
    public LoadButton(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        /**设置自定义属性*/
        TypeArray(context, attrs);

        //进度条画笔
        mPaint = new Paint();
        mPaint.setAntiAlias(true);//抗锯齿;
        mPaint.setColor(mStrokeColor);
        mPaint.setStyle(Paint.Style.STROKE);//设置风格
        mPaint.setStrokeWidth(mProgressedWidth);

        //设置默认宽度  :  总宽度   =   矩形宽度  +  圆的直径
        mDefaultWidth = 200;

        //文字画笔
        mTextPaint = new TextPaint();
        mTextPaint.setColor(mTextColor);
        mTextPaint.setTextSize(mTextSize);
        mTextPaint.setTextAlign(Paint.Align.CENTER);//位置

        //中间矩形宽度
        rectWidth = mDefaultWidth - mDefaultRadius * 2;

        //RectF对象持有一个矩形的四个float坐标值，Rect对象持有一个矩形的四个integer坐标值，这是两者最大的区别
        //左边半圆的矩阵
        leftRectF = new RectF();
        //右边半圆的矩阵
        rightRectF = new RectF();
        //中间矩形的矩阵
        contentRectF = new RectF();

        //定义一个boolean值
        isUnfold = true;

        //自定义一个OnClickListener
        mListener = new OnClickListener() {
            @Override
            public void onClick(View view) {
            }
        };


        //设置显示的图片
        if(mSuccessedDrawable == null){
            mSuccessedDrawable = context.getResources().getDrawable(R.drawable.yes);
        }
        if (mErrorDrawable == null){
            mErrorDrawable = context.getResources().getDrawable(R.drawable.no);
        }
        if (mPauseDrawable == null){
            mPauseDrawable = context.getResources().getDrawable(R.drawable.pause);
        }

//        mProgressSecondColor = Color.parseColor("#c3c3c3");
//        mProgressColor = Color.WHITE;





    }

    /**设置自定义属性*/
    private void TypeArray(Context context, @Nullable AttributeSet attrs) {
        //圆默认的半径
        mDefaultRadius = 40;
        //获取TypedArray对象
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.LoadButton);
        mDefaultTextSize = 24;
        //文本
        mText = typedArray.getString(R.styleable.LoadButton_android_text);
        //设置字体打大小
        mTextSize = typedArray.getDimensionPixelSize(R.styleable.LoadButton_android_textSize, mDefaultTextSize);
        //两边圆半径
        mRadiu = typedArray.getDimensionPixelOffset(R.styleable.LoadButton_radiu, mDefaultRadius);
        mStrokeColor = typedArray.getColor(R.styleable.LoadButton_stroke_color, Color.RED);
        //字体颜色
        mTextColor = typedArray.getColor(R.styleable.LoadButton_content_color, Color.WHITE);
        //背景颜色
        mBackgroundColor = typedArray.getColor(R.styleable.LoadButton_backColor, Color.WHITE);
        //进度条颜色
        mProgressColor = typedArray.getColor(R.styleable.LoadButton_progressColor, Color.WHITE);
        mProgressSecondColor = typedArray.getColor(R.styleable.LoadButton_progressSecondColor, Color.parseColor("#c3c3c3"));

        //进度条宽
        mProgressedWidth = typedArray.getDimensionPixelOffset(R.styleable.LoadButton_progressedWidth, 2);
        //字体距离左右的padding
        mLeftRightPadding = typedArray.getDimensionPixelOffset(R.styleable.LoadButton_contentPaddingLR, 10);
        //字体距离上下的padding
        mTopBottomPadding = typedArray.getDimensionPixelOffset(R.styleable.LoadButton_contentPaddingTB, 10);

        //三个drawable分别是成功、失败、暂停时显示的图片
        mSuccessedDrawable = typedArray.getDrawable(R.styleable.LoadButton_loadSuccessDrawable);
        mErrorDrawable = typedArray.getDrawable(R.styleable.LoadButton_loadErrorDrawable);
        mPauseDrawable = typedArray.getDrawable(R.styleable.LoadButton_loadPauseDrawable);
        //回收TypedArray，释放内存，方便后面使用
        typedArray.recycle();
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        //测量出大小和模式(最大的宽高)
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);

        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);

        //定义变量设置最终控件的宽高
        int resultW = widthSize;
        int resultH = heightSize;
        //定义变量设置自己控件需要的宽高
        int contentW = 0;
        int contentH = 0;
        //MeasureSpec三个模式：AT_MOST，specSize 代表的是最大可获得的空间；
        //                     EXACTLY，specSize 代表的是精确的尺寸；
        //                     UNSPECIFIED，对于控件尺寸来说，没有任何参考意义。
        if(widthMode == MeasureSpec.AT_MOST){
            //测量文字宽度
            mTextWidth = (int) mTextPaint.measureText(mText);
            contentW += mTextWidth + mLeftRightPadding * 2 + mRadiu * 2 ;
            //得到控件最终的宽
            resultW = contentW < widthSize ? contentW : widthSize;
        }

        if (heightMode == MeasureSpec.AT_MOST){
            contentH +=  mTopBottomPadding * 2 + mTextSize;
            //得到控件最终的高
            resultH = contentH < heightSize ? contentH : heightSize;
        }

        /* 因为是在一个动态的控件，所以最终要得到变化后的控件的宽高 */
        resultW = resultW < mRadiu * 2 ? mRadiu * 2 : resultW;
        resultH = resultH < 2 * mRadiu ? 2 * mRadiu : resultH;
        setMeasuredDimension(resultW,resultH);
        Log.d(TAG,"onMeasure: w:"+resultW+" h:"+resultH);

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);







    }
}
