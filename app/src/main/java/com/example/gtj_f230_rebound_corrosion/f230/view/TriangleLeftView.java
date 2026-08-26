package com.example.gtj_f230_rebound_corrosion.f230.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.view.MotionEvent;
import android.view.View;

import com.example.gtj_f230_rebound_corrosion.base.utils.ViewUtils;
import com.example.gtj_f230_rebound_corrosion.f230.model.PointBean;

public class TriangleLeftView extends View {
    private PointBean outsidePoint = new PointBean(0, 0);
    public float currentX, currentY;
    public float radius = 28f;
    private Paint paint;
    private boolean isFocus, isEfficient;
    //
    private PointTouchListener pointListener;

    public TriangleLeftView(Context context) {
        super(context);
        init();
    }

    public TriangleLeftView(Context context, boolean isFocus, float currentY) {
        super(context);
        this.isFocus = isFocus;
        this.currentY = currentY;
        init();
    }

    public TriangleLeftView(Context context, boolean isFocus, float currentX, float currentY) {
        super(context);
        this.isFocus = isFocus;
        this.currentX = currentX;
        this.currentY = currentY;
        init();
    }

    private void init() {
        paint = new Paint();
        paint.setStrokeWidth(2);  //线宽,单位是像素
        paint.setAntiAlias(true);
    }

    public void setFocus(boolean isFocus) {
        this.isFocus = isFocus;
    }

    public void setOutsidePoint(double x, double y) {
        this.outsidePoint.x = x;
        this.outsidePoint.y = y;
        invalidate();
    }

    public void setPointTouchListener(PointTouchListener pointListener) {
        this.pointListener = pointListener;
    }

    @SuppressLint("DrawAllocation")
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        PointBean pointBean = ViewUtils.getOffset(outsidePoint, new PointBean(currentX, currentY));
        //三角形的中心点
        int x = (int) (currentX + pointBean.x);
        int y = (int) (currentY + pointBean.y);
        PointBean centerPoint = new PointBean(x, y);
        float angle = (float) ViewUtils.getAngle(outsidePoint, new PointBean(currentX, currentY));
        PointBean a = ViewUtils.getCircleArcPoint(centerPoint, radius, angle);
        PointBean b = ViewUtils.getCircleArcPoint(centerPoint, radius, 120 + angle);
        PointBean c = ViewUtils.getCircleArcPoint(centerPoint, radius, 240 + angle);
        //实心
        Path path = new Path();
        path.setFillType(Path.FillType.EVEN_ODD);
        path.moveTo((float) b.x, (float) b.y);
        path.lineTo((float) b.x, (float) b.y);
        path.lineTo((float) c.x, (float) c.y);
        path.lineTo((float) a.x, (float) a.y);
        path.close();
        paint.setColor(Color.parseColor("#F6D129"));
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        canvas.drawPath(path, paint);
        //描边
        path = new Path();
        path.setFillType(Path.FillType.EVEN_ODD);
        path.moveTo((float) b.x, (float) b.y);
        path.lineTo((float) b.x, (float) b.y);
        path.lineTo((float) c.x, (float) c.y);
        path.lineTo((float) a.x, (float) a.y);
        path.close();
        paint.setColor(Color.parseColor("#009FA8"));
        paint.setStyle(Paint.Style.STROKE);
        canvas.drawPath(path, paint);
        //
        paint.setColor(Color.parseColor("#F6D129"));
        paint.setStyle(Paint.Style.FILL);
        canvas.drawCircle(currentX, currentY, 2, paint);
    }

    @SuppressLint("ClickableViewAccessibility")//此注解是点击事件和onTouchEvent冲突，返回true不会响应点击。
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (isFocus) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    if (Math.abs(event.getX() - currentX) < 56 && Math.abs(event.getY() - currentY) < 56) {
                        isEfficient = true;
                        currentX = event.getX();
                        currentY = event.getY();
                    }
                    break;
                case MotionEvent.ACTION_MOVE:
                    if (isEfficient) {
                        currentX = event.getX();
                        currentY = event.getY();
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    isEfficient = false;
                    break;
            }
        }
        if (isEfficient) {
            invalidate();  //通过draw组件重绘
            if (pointListener != null) {
                pointListener.onMove();
            }
        }
        return isEfficient;
    }
}
