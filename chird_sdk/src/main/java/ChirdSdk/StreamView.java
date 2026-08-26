package ChirdSdk;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.Log;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class StreamView extends SurfaceView implements SurfaceHolder.Callback {

    protected long lastDown = -1;
    public final static long DOUBLE_TOUCH_TIME = 250;

    private Bitmap sourceBitmap = null;

    private boolean surfaceDone = false;
    private SurfaceHolder mSurfaceHolder;
    private BitmapFactory.Options opts = new BitmapFactory.Options();

    private CallBack mCallBack = null;

    private boolean isMirrorImage = false;
    
    // 复用 Matrix 对象，避免频繁创建
    private final Matrix matrix = new Matrix();
    
    // 复用 Paint 对象
    private final Paint paint = new Paint();
    
    // 复用 Rect 对象
    private final Rect destRect = new Rect();

    private boolean isSupportTouchZoom = false;

    private float x1 = 0;
    private float x2 = 0;
    private float y1 = 0;
    private float y2 = 0;

    /**
     * 初始化状态常量
     */
    public static final int STATUS_INIT = 1;

    /**
     * 图片放大状态常量
     */
    public static final int STATUS_ZOOM_OUT = 2;

    /**
     * 图片缩小状态常量
     */
    public static final int STATUS_ZOOM_IN = 3;

    /**
     * 图片拖动状态常量
     */
    public static final int STATUS_MOVE = 4;

    /**
     * 记录当前操作的状态
     */
    private int currentStatus;

    /**
     * 控件的宽度和高度
     */
    private int swidth;
    private int sheight;

    /**
     * 记录两指同时放在屏幕上时，中心点的坐标
     */
    private float centerPointX;
    private float centerPointY;

    /**
     * 记录当前图片的宽度和高度
     */
    private float currentBitmapWidth;
    private float currentBitmapHeight;

    /**
     * 记录上次手指移动时的坐标
     */
    private float lastXMove = -1;
    private float lastYMove = -1;

    /**
     * 记录手指移动距离
     */
    private float movedDistanceX;
    private float movedDistanceY;

    /**
     * 记录图片在矩阵上的偏移值
     */
    private float totalTranslateX;
    private float totalTranslateY;

    /**
     * 记录图片缩放比例
     */
    private float totalRatio;
    private float scaledRatio;
    private float initRatio;

    /**
     * 记录上次两指之间的距离
     */
    private double lastFingerDis;

    public interface CallBack {
        void callbackSurface(Surface surface);
        void callbackDestroy();
        void onClick();
    }

    public StreamView(Context context, CallBack myListener) {
        super(context);

        setFocusable(true);
        swidth = getWidth();
        sheight = getHeight();

        mSurfaceHolder = getHolder();
        this.mCallBack = myListener;

        mSurfaceHolder.addCallback(this);

        mShowMode = StreamView.SHOW_MODE_BEST_FIT;

        currentStatus = STATUS_INIT;
    }

    public void setCallback(CallBack callback) {
        this.mCallBack = callback;
    }

    public final static int SHOW_MODE_ORIGINAL = 0;
    public final static int SHOW_MODE_STANDARD = 1;
    public final static int SHOW_MODE_BEST_FIT = 4;
    public final static int SHOW_MODE_FULLSCREEN = 8;
    private int mShowMode;

    public int getShowMode() {
        return mShowMode;
    }

    public void setShowMode(int mode) {
        mShowMode = mode;
        if (mShowMode != SHOW_MODE_FULLSCREEN) {
            isSupportTouchZoom = true;
        } else {
            isSupportTouchZoom = false;
        }
    }

    public boolean isSupportTouchZoom() {
        return isSupportTouchZoom;
    }

    public void setSupportTouchZoom(boolean supportTouchZoom) {
        isSupportTouchZoom = supportTouchZoom;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        surfaceDone = true;
        if (mCallBack != null) {
            mCallBack.callbackSurface(holder.getSurface());
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        setSurfaceSize(width, height);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        surfaceDone = false;
        if (mCallBack != null) {
            mCallBack.callbackDestroy();
        }
    }

    private int SCALING_MAX = 4;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!isSupportTouchZoom) {
            return true;
        }

        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                if (event.getPointerCount() == 1) {
                    x1 = event.getX();
                    y1 = event.getY();
                }
                break;

            case MotionEvent.ACTION_POINTER_DOWN:
                if (event.getPointerCount() == 2) {
                    lastFingerDis = distanceBetweenFingers(event);
                }
                break;

            case MotionEvent.ACTION_MOVE:
                if (event.getPointerCount() == 1) {
                    float xMove = event.getX();
                    float yMove = event.getY();
                    if (lastXMove == -1 && lastYMove == -1) {
                        lastXMove = xMove;
                        lastYMove = yMove;
                    }
                    currentStatus = STATUS_MOVE;
                    movedDistanceX = xMove - lastXMove;
                    movedDistanceY = yMove - lastYMove;

                    // 边界检查
                    if (totalTranslateX + movedDistanceX > 0) {
                        movedDistanceX = 0;
                    } else if (swidth - (totalTranslateX + movedDistanceX) > currentBitmapWidth) {
                        movedDistanceX = 0;
                    }
                    if (totalTranslateY + movedDistanceY > 0) {
                        movedDistanceY = 0;
                    } else if (sheight - (totalTranslateY + movedDistanceY) > currentBitmapHeight) {
                        movedDistanceY = 0;
                    }
                    if (mShowMode != SHOW_MODE_FULLSCREEN) {
                        refreshScreen();
                    }
                } else if (event.getPointerCount() == 2) {
                    centerPointBetweenFingers(event);
                    double fingerDis = distanceBetweenFingers(event);
                    if (fingerDis > lastFingerDis) {
                        currentStatus = STATUS_ZOOM_OUT;
                    } else {
                        currentStatus = STATUS_ZOOM_IN;
                    }

                    // 缩放倍数限制
                    if ((currentStatus == STATUS_ZOOM_OUT && totalRatio < SCALING_MAX * initRatio)
                            || (currentStatus == STATUS_ZOOM_IN && totalRatio > initRatio)) {
                        scaledRatio = (float) (fingerDis / lastFingerDis);
                        totalRatio = totalRatio * scaledRatio;
                        if (totalRatio > SCALING_MAX * initRatio) {
                            totalRatio = SCALING_MAX * initRatio;
                        } else if (totalRatio < initRatio) {
                            totalRatio = initRatio;
                        }
                        if (mShowMode != SHOW_MODE_FULLSCREEN) {
                            refreshScreen();
                        }
                    }
                    lastFingerDis = fingerDis;
                }
                break;

            case MotionEvent.ACTION_POINTER_UP:
                if (event.getPointerCount() == 2) {
                    lastXMove = -1;
                    lastYMove = -1;
                }
                break;

            case MotionEvent.ACTION_UP:
                lastXMove = -1;
                lastYMove = -1;

                if (event.getPointerCount() == 1) {
                    x2 = event.getX();
                    y2 = event.getY();
                    if (Math.abs(x1 - x2) < 25 && Math.abs(y1 - y2) < 25) {
                        if (mCallBack != null) {
                            mCallBack.onClick();
                        }
                    }
                }
                break;

            default:
                break;
        }
        return true;
    }

    private void initBitmap(Canvas canvas) {
        if (sourceBitmap != null) {
            matrix.reset();
            int bitmapWidth = sourceBitmap.getWidth();
            int bitmapHeight = sourceBitmap.getHeight();

            if (mShowMode == SHOW_MODE_BEST_FIT
                    || (mShowMode == SHOW_MODE_STANDARD && (bitmapWidth > swidth || bitmapHeight > sheight))
                    || bitmapWidth > swidth || bitmapHeight > sheight) {
                float ratio;
                if (bitmapWidth - swidth > bitmapHeight - sheight) {
                    ratio = swidth / (bitmapWidth * 1.0f);
                    matrix.postScale(ratio, ratio);
                    float translateY = (sheight - (bitmapHeight * ratio)) / 2f;
                    matrix.postTranslate(0, translateY);
                    totalTranslateY = translateY;
                    totalRatio = initRatio = ratio;
                } else {
                    ratio = sheight / (bitmapHeight * 1.0f);
                    matrix.postScale(ratio, ratio);
                    float translateX = (swidth - (bitmapWidth * ratio)) / 2f;
                    matrix.postTranslate(translateX, 0);
                    totalTranslateX = translateX;
                    totalRatio = initRatio = ratio;
                }
                currentBitmapWidth = bitmapWidth * initRatio;
                currentBitmapHeight = bitmapHeight * initRatio;
            } else {
                float translateX = (swidth - sourceBitmap.getWidth()) / 2f;
                float translateY = (sheight - sourceBitmap.getHeight()) / 2f;
                matrix.postTranslate(translateX, translateY);
                totalTranslateX = translateX;
                totalTranslateY = translateY;
                totalRatio = initRatio = 1f;
                currentBitmapWidth = bitmapWidth;
                currentBitmapHeight = bitmapHeight;
            }

            canvas.drawBitmap(sourceBitmap, matrix, paint);
        }
    }

    private void zoom(Canvas canvas) {
        matrix.reset();
        matrix.postScale(totalRatio, totalRatio);
        float scaledWidth = sourceBitmap.getWidth() * totalRatio;
        float scaledHeight = sourceBitmap.getHeight() * totalRatio;
        float translateX = 0f;
        float translateY = 0f;

        if (currentBitmapWidth < swidth) {
            translateX = (swidth - scaledWidth) / 2f;
        } else {
            translateX = totalTranslateX * scaledRatio;
            if (translateX > 0) {
                translateX = 0;
            } else if (swidth - translateX > scaledWidth) {
                translateX = swidth - scaledWidth;
            }
        }

        if (currentBitmapHeight < sheight) {
            translateY = (sheight - scaledHeight) / 2f;
        } else {
            translateY = totalTranslateY * scaledRatio;
            if (translateY > 0) {
                translateY = 0;
            } else if (sheight - translateY > scaledHeight) {
                translateY = sheight - scaledHeight;
            }
        }

        matrix.postTranslate(translateX, translateY);
        totalTranslateX = translateX;
        totalTranslateY = translateY;
        currentBitmapWidth = scaledWidth;
        currentBitmapHeight = scaledHeight;

        canvas.drawBitmap(sourceBitmap, matrix, paint);
    }

    private void move(Canvas canvas) {
        matrix.reset();
        float translateX = totalTranslateX + movedDistanceX;
        float translateY = totalTranslateY + movedDistanceY;

        matrix.postScale(totalRatio, totalRatio);
        matrix.postTranslate(translateX, translateY);
        totalTranslateX = translateX;
        totalTranslateY = translateY;

        canvas.drawBitmap(sourceBitmap, matrix, paint);
    }

    private double distanceBetweenFingers(MotionEvent event) {
        float disX = Math.abs(event.getX(0) - event.getX(1));
        float disY = Math.abs(event.getY(0) - event.getY(1));
        return Math.sqrt(disX * disX + disY * disY);
    }

    private void centerPointBetweenFingers(MotionEvent event) {
        float xPoint0 = event.getX(0);
        float yPoint0 = event.getY(0);
        float xPoint1 = event.getX(1);
        float yPoint1 = event.getY(1);
        centerPointX = (xPoint0 + xPoint1) / 2;
        centerPointY = (yPoint0 + yPoint1) / 2;
    }

    public void setSurfaceSize(int width, int height) {
        synchronized (mSurfaceHolder) {
            swidth = width;
            sheight = height;
        }
    }

    public Surface getSurface() {
        return mSurfaceHolder.getSurface();
    }

    public void clearScreen() {
        if (!surfaceDone) {
            return;
        }
        Canvas canvas = null;
        try {
            canvas = mSurfaceHolder.lockCanvas();
            if (canvas != null) {
                canvas.drawColor(Color.BLACK);
            }
        } finally {
            if (canvas != null) {
                mSurfaceHolder.unlockCanvasAndPost(canvas);
            }
        }
    }

    public void showBitmap(Bitmap bitmap) {
        if (bitmap == null || !surfaceDone) {
            return;
        }
        Canvas canvas = null;
        try {
            canvas = mSurfaceHolder.lockCanvas();
            if (canvas != null) {
                sourceBitmap = bitmap;
                
                // 镜像处理
                if (isMirrorImage) {
                    sourceBitmap = mirrorBitmap(sourceBitmap);
                }

                // 根据显示模式绘制
                if (mShowMode == SHOW_MODE_ORIGINAL) {
                    canvas.drawColor(Color.BLACK);
                    int wh = swidth > sheight ? sheight : swidth;
                    destRect.set((swidth - wh) / 2, 0, wh + (swidth - wh) / 2, wh);
                    canvas.drawBitmap(sourceBitmap, null, destRect, paint);
                } else if (mShowMode == SHOW_MODE_FULLSCREEN) {
                    destRect.set(0, 0, swidth, sheight);
                    canvas.drawBitmap(sourceBitmap, null, destRect, paint);
                } else {
                    canvas.drawColor(Color.BLACK);
                    switch (currentStatus) {
                        case STATUS_ZOOM_OUT:
                        case STATUS_ZOOM_IN:
                            zoom(canvas);
                            break;
                        case STATUS_MOVE:
                            move(canvas);
                            break;
                        case STATUS_INIT:
                        default:
                            initBitmap(canvas);
                            break;
                    }
                }
            }
        } finally {
            if (canvas != null) {
                mSurfaceHolder.unlockCanvasAndPost(canvas);
            }
        }
    }

    /**
     * 镜像处理 Bitmap
     */
    private Bitmap mirrorBitmap(Bitmap source) {
        matrix.reset();
        matrix.postScale(-1, 1);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }

    public void refreshScreen() {
        if (!surfaceDone || sourceBitmap == null) {
            return;
        }
        Canvas canvas = null;
        try {
            canvas = mSurfaceHolder.lockCanvas();
            if (canvas != null) {
                canvas.drawColor(Color.BLACK);
                switch (currentStatus) {
                    case STATUS_ZOOM_OUT:
                    case STATUS_ZOOM_IN:
                        zoom(canvas);
                        break;
                    case STATUS_MOVE:
                        move(canvas);
                        break;
                    case STATUS_INIT:
                    default:
                        initBitmap(canvas);
                        break;
                }
            }
        } finally {
            if (canvas != null) {
                mSurfaceHolder.unlockCanvasAndPost(canvas);
            }
        }
    }

    public boolean getMirrorImage() {
        return isMirrorImage;
    }

    public void setMirrorImage(boolean mirror) {
        isMirrorImage = mirror;
    }
}
