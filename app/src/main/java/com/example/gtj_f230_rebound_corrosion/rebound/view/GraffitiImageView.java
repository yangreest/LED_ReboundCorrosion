package com.example.gtj_f230_rebound_corrosion.rebound.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.appcompat.widget.AppCompatImageView;

public class GraffitiImageView extends AppCompatImageView {
    private Path mPath;//绘制路径
    private Paint mPaint;// 绘制画笔
    private Canvas mCanvas;//背景画布
    private Bitmap mMBitmap;//背景bitmap

    public GraffitiImageView(Context context) {
        this(context, null);
    }

    public GraffitiImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public GraffitiImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initPaint();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @SuppressLint("DrawAllocation")
    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        mMBitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(mMBitmap);
        mCanvas.drawColor(Color.TRANSPARENT);
        mCanvas.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG));
    }

    private void initPaint() {
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(Color.RED);
        mPaint.setStrokeWidth(8f);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setDither(true);
        mPath = new Path();
    }


    @SuppressLint("DrawAllocation")
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG));
        canvas.drawBitmap(mMBitmap, 0, 0, mPaint);
        // 通过画布绘制多点形成的图形
        canvas.drawPath(mPath, mPaint);
    }

    private float[] downPoint = new float[2];

    private float[] previousPoint = new float[2];

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        downPoint[0] = event.getX();
        downPoint[1] = event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                previousPoint[0] = downPoint[0];
                previousPoint[1] = downPoint[1];
                mPath.moveTo(downPoint[0], downPoint[1]);
                break;
            case MotionEvent.ACTION_MOVE:
                float dX = Math.abs(downPoint[0] - previousPoint[0]);
                float dY = Math.abs(downPoint[1] - previousPoint[1]);

                // 两点之间的距离大于等于3时，生成贝塞尔绘制曲线
                if (dX >= 3 || dY >= 3) {
                    // 设置贝塞尔曲线的操作点为起点和终点的一半
                    float cX = (downPoint[0] + previousPoint[0]) / 2;
                    float cY = (downPoint[1] + previousPoint[1]) / 2;
                    // 二次贝塞尔，实现平滑曲线；previousX, previousY为操作点，cX, cY为终点
                    mPath.quadTo(previousPoint[0], previousPoint[1], cX, cY);
                    // 第二次执行时，第一次结束调用的坐标值将作为第二次调用的初始坐标值
                    previousPoint[0] = downPoint[0];
                    previousPoint[1] = downPoint[1];
                }
                break;
            case MotionEvent.ACTION_UP:
                mCanvas.drawPath(mPath, mPaint);
                mPath.reset();
                break;
        }
        invalidate();
        return true;
    }

    public void clear() {
        if (mCanvas != null) {
            mPath.reset();
            mCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
            invalidate();
        }
    }
}