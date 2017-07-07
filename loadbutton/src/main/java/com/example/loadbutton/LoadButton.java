package com.example.loadbutton;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

/**
 * Created by Zhang
 * Time 2017/7/3.
 */

public class LoadButton extends View implements Animator.AnimatorListener{
    private static final String TAG = "LoadButton";

    //圆默认的半径
    private int mDefaultRadius;
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
    //进度条矩阵
    private RectF progressRect;


    private float circleSweep;
    private boolean isUnfold;
    private OnClickListener mListener;
    private Path mPath;
    private ObjectAnimator shrinkAnim;
    private ObjectAnimator loadAnimator;

    private boolean progressReverse;


    private State mCurrentState;

    LoadListener mLoadListener;

    public LoadListener getLoadListener(){
        return mLoadListener;
    }

    public void setLoadListener(LoadListener listener){
        this.mLoadListener = listener;
    }

    public void setCircleSweep(float circleSweep) {
        this.circleSweep = circleSweep;
        invaidateSelft();
    }

    enum State {
        INITIAL,
        FODDING,
        LOADDING,
        COMPLETED_ERROR,
        COMPLETED_SUCCESSED,
        LOADDING_PAUSE
    }

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
        int mDefaultWidth = 200;

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

                if (mCurrentState == State.FODDING){
                    return;
                }
                if(mCurrentState == State.INITIAL){
                    if (isUnfold){
                        shringk();
                    }
                }else if ( mCurrentState == State.COMPLETED_ERROR ) {
                    if (mLoadListener != null ) {
                        mLoadListener.onClick(false);
                    }
                } else if ( mCurrentState == State.COMPLETED_SUCCESSED ) {
                    if (mLoadListener != null ) {
                        mLoadListener.onClick(true);
                    }
                } else if ( mCurrentState == State.LOADDING_PAUSE ) {
                    if (mLoadListener != null ) {
                        mLoadListener.needLoading();
                        load();
                    }
                } else if ( mCurrentState == State.LOADDING) {
                    mCurrentState = State.LOADDING_PAUSE;
                    cancelAnimation();
                    invaidateSelft();
                }

            }
        };

        //设置点击效果
        setOnClickListener(mListener);

        mCurrentState = State.INITIAL;

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

        mProgressSecondColor = Color.parseColor("#c3c3c3");
        mProgressColor = Color.WHITE;

    }

    public void reset(){
        mCurrentState = State.INITIAL;
        rectWidth = getWidth() - mRadiu * 2;
        isUnfold = true;
        cancelAnimation();
        invaidateSelft();
    }

    /**设置自定义属性*/
    private void TypeArray(Context context, @Nullable AttributeSet attrs) {
        //圆默认的半径
        mDefaultRadius = 40;
        //获取TypedArray对象
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.LoadButton);
        int mDefaultTextSize = 24;
        //文本
        mText = typedArray.getString(R.styleable.LoadButton_android_text);
        //设置字体打大小
        mTextSize = typedArray.getDimensionPixelSize(R.styleable.LoadButton_android_textSize, mDefaultTextSize);
        //两边圆半径
        mRadiu = typedArray.getDimensionPixelOffset(R.styleable.LoadButton_radiu, mDefaultRadius);
        mStrokeColor = typedArray.getColor(R.styleable.LoadButton_stroke_color, Color.RED);
        //字体颜色
        mTextColor = typedArray.getColor(R.styleable.LoadButton_content_color, Color.RED);
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
            int mTextWidth = (int) mTextPaint.measureText(mText);
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

        //圆的半径
        mRadiu = resultH / 2;
        //中间矩形的宽
        rectWidth = resultW - 2 * mRadiu;
        setMeasuredDimension(resultW,resultH);
        Log.e(TAG,"onMeasure: w:"+resultW+" h:"+resultH);

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int cx = getWidth() / 2;// 获取整个view宽的一半，也就是中间的宽
        int cy = getHeight() / 2; //获取整个view高的一半，也就是中间的高
        //画路径
        drawPath(canvas , cx , cy);

        //获取mTextPaint上边和下边
        int textDescent = (int) mTextPaint.getFontMetrics().descent;
        int textAscent = (int) mTextPaint.getFontMetrics().ascent;
        //上边减去下边就是mTextPaint的高
        int delta = Math.abs(textAscent) - textDescent;


        int circleR = mRadiu / 2;

        if ( mCurrentState == State.INITIAL) {
            //文字显示的位置就是view高一半加上字体高的一半
            canvas.drawText(mText,cx,cy + delta / 2,mTextPaint);

        } else if ( mCurrentState == State.LOADDING ) {

            if ( progressRect == null ) {
                progressRect = new RectF();
            }
            progressRect.set(cx - circleR,cy - circleR,cx + circleR,cy + circleR);

            mPaint.setColor(mProgressSecondColor);
            canvas.drawCircle(cx,cy,circleR,mPaint);
            mPaint.setColor(mProgressColor);
            Log.d(TAG,"onDraw() pro:"+progressReverse+" swpeep:"+circleSweep);
            if ( circleSweep != 360 ) {
                int mProgressStartAngel = progressReverse ? 270 : (int) (270 + circleSweep);
                canvas.drawArc(progressRect, mProgressStartAngel,progressReverse ? circleSweep : (int) (360 - circleSweep),
                        false,mPaint);
            }
            mPaint.setColor(mBackgroundColor);
        } else if ( mCurrentState == State.COMPLETED_ERROR ) {
            mErrorDrawable.setBounds(cx - circleR,cy - circleR,cx + circleR,cy + circleR);
            mErrorDrawable.draw(canvas);
        } else if (mCurrentState == State.COMPLETED_SUCCESSED) {
            mSuccessedDrawable.setBounds(cx - circleR,cy - circleR,cx + circleR,cy + circleR);
            mSuccessedDrawable.draw(canvas);
        } else if (mCurrentState == State.LOADDING_PAUSE) {
            mPauseDrawable.setBounds(cx - circleR,cy - circleR,cx + circleR,cy + circleR);
            mPauseDrawable.draw(canvas);
        }
    }

    private void drawPath(Canvas canvas, int cx, int cy) {
        //创建一个path对象
        if(mPath == null){
            mPath = new Path();
        }
        //每次都重置一下
        mPath.reset();

        //获取上下左右各个点的位置
        int left = cx - rectWidth / 2 - mRadiu;
        int top = 0;
        int right = cx + rectWidth / 2 + mRadiu;
        int bottom = getHeight();
        //画出矩阵的路线
        leftRectF.set(left, top, left + mRadiu * 2, bottom);
        rightRectF.set(right - mRadiu * 2 , top, right, bottom);
        contentRectF.set(cx - rectWidth / 2, top,cx + rectWidth / 2, bottom);
        //到达的第一点的位置
        mPath.moveTo(cx - rectWidth /2, bottom);
        /**
         * aroTo:画圆
         * 第一个参数：startAngle 开始的度数
         * 第二个参数：sweepAngle 旋转的度数
         */
        mPath.arcTo(leftRectF,90.0f,180f);
        mPath.lineTo(cx + rectWidth /2, top);
        mPath.arcTo(rightRectF,270.0f,180f);
        //不要忘记关掉
        mPath.close();

        mPaint.setStyle(Paint.Style.FILL);//设置画笔风格为填充
        mPaint.setColor(mBackgroundColor);
        canvas.drawPath(mPath,mPaint);
        //画好后，重新设置画笔
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setColor(mStrokeColor);

    }
    //设置矩阵的宽度  必须设置这个方法，不然没有动画效果
    public void setRectWidth (int width) {
        rectWidth = width;
        invaidateSelft();
    }

    /**缩放动画*/
    @SuppressLint("ObjectAnimatorBinding")
    public void shringk() {
        if (shrinkAnim == null) {
            shrinkAnim = ObjectAnimator.ofInt(this, "rectWidth", rectWidth, 0);
        }
        shrinkAnim.addListener(this);

        shrinkAnim.setDuration(500);
        shrinkAnim.start();
        mCurrentState = State.FODDING;
    }

    @SuppressLint("ObjectAnimatorBinding")
    public void load() {
        if (loadAnimator == null) {
            loadAnimator = ObjectAnimator.ofFloat(this,"circleSweep",0,360);
        }

        loadAnimator.setDuration(1000);
        loadAnimator.setRepeatMode(ValueAnimator.RESTART);
        loadAnimator.setRepeatCount(ValueAnimator.INFINITE);

        loadAnimator.removeAllListeners();

        loadAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {

            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {
//                Log.d(TAG,"onAnimationRepeat:"+progressReverse);
                progressReverse = !progressReverse;
            }
        });
        loadAnimator.start();
        mCurrentState = State.LOADDING;
    }


    private void invaidateSelft() {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            invalidate();
        } else {
            postInvalidate();
        }
    }


    public void loadSuccessed() {
        mCurrentState = State.COMPLETED_SUCCESSED;
        cancelAnimation();
        invaidateSelft();
    }

    public void loadFailed() {
        mCurrentState = State.COMPLETED_ERROR;
        cancelAnimation();
        invaidateSelft();
    }


    @Override
    public void onAnimationStart(Animator animator) {

    }

    @Override
    public void onAnimationEnd(Animator animator) {
        isUnfold = false;
        load();
    }

    @Override
    public void onAnimationCancel(Animator animator) {

    }

    @Override
    public void onAnimationRepeat(Animator animator) {

    }
    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        cancelAnimation();

    }

    public void cancelAnimation() {
        if ( shrinkAnim != null && shrinkAnim.isRunning() ) {
            shrinkAnim.removeAllListeners();
            shrinkAnim.cancel();
            shrinkAnim = null;
        }
        if ( loadAnimator != null && loadAnimator.isRunning() ) {
            loadAnimator.removeAllListeners();
            loadAnimator.cancel();
            loadAnimator = null;
        }
    }


    //创建一个按钮点击的接口
    public interface LoadListener {
        void onClick(boolean isSuccessed);
        void needLoading();

    }


}
