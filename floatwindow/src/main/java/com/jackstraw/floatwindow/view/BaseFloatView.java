package com.jackstraw.floatwindow.view;

import android.content.Context;
import android.graphics.Point;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.FrameLayout;

/**
 * @author pink-jackstraw
 * @date 2019/4/19
 * @describe
 */
public abstract class BaseFloatView extends FrameLayout {

    private final String TAG = "BaseFloatView";
    /**
     * 记录手指按下时在小悬浮窗的View上的横坐标的值
     */
    private float xInView = 0;

    /**
     * 记录手指按下时在小悬浮窗的View上的纵坐标的值
     */
    private float yInView = 0;

    /**
     * 记录当前手指位置在屏幕上的横坐标值
     */
    private float xInScreen = 0;

    /**
     * 记录当前手指位置在屏幕上的纵坐标值
     */
    private float yInScreen = 0;

    /**
     * 记录手指按下时在屏幕上的横坐标的值
     */
    private float xDownInScreen = 0;

    /**
     * 记录手指按下时在屏幕上的纵坐标的值
     */
    private float yDownInScreen = 0;


    private int width = 0;

    private int height = 0;

    private boolean isAnchoring = false;
    private boolean isShowing = false;
    private boolean isAnchorToSide = false;
    private Context context = null;
    private WindowManager windowManager = null;
    private WindowManager.LayoutParams mParams = null;
    private OnFloatClickListener onFloatClickListener = null;

    public BaseFloatView(Context context) {
        this(context, null);
    }

    public BaseFloatView(Context context, AttributeSet attrs) {
        this(context, attrs, -1);
    }

    public BaseFloatView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        initWindowManager();
        createFloatView(context);
        isAnchorToSide = isAnchorToSide();
    }

    private void initWindowManager() {
        windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
    }

    public void setOnFloatClickListener(OnFloatClickListener onFloatClickListener) {
        this.onFloatClickListener = onFloatClickListener;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        width = getWidth();
        height = getHeight();
    }

    /**
     * 返回自定义的视图。
     * @return
     */
    protected abstract void createFloatView(Context context);

    /**
     * 是否需要自动吸附
     * @return
     */
    protected abstract boolean isAnchorToSide();

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (isAnchoring) {
            return true;
        }
        switch (event.getAction()){

            case MotionEvent.ACTION_DOWN:
                xInView = event.getX();
                yInView = event.getY();
                xDownInScreen = event.getRawX();
                yDownInScreen = event.getRawY();
                xInScreen = event.getRawX();
                yInScreen = event.getRawY();
                break;

            case MotionEvent.ACTION_MOVE:
                xInScreen = event.getRawX();
                yInScreen = event.getRawY();
                // 手指移动的时候更新小悬浮窗的位置
                updateViewPosition();
                break;

            case MotionEvent.ACTION_UP:
                if (Math.abs(xDownInScreen - xInScreen) <= ViewConfiguration.get(context).getScaledEdgeSlop()
                        && Math.abs(yDownInScreen - yInScreen) <= ViewConfiguration.get(context).getScaledEdgeSlop()) {
                    // 点击效果
                    if (onFloatClickListener != null) {
                        onFloatClickListener.onClick();
                    }
                } else {
                    //吸附效果
                    if(isAnchorToSide){
                        anchorToSide();
                    }
                }
                break;
        }
        return true;
    }

    public void setParams(WindowManager.LayoutParams params) {
        mParams = params;
    }

    public void  setIsShowing(boolean isShowing) {
        this.isShowing = isShowing;
    }

    private void anchorToSide() {
        isAnchoring = true;
        Point size = new Point();
        windowManager.getDefaultDisplay().getSize(size);
        int screenWidth = size.x;
        int screenHeight = size.y;
        int middleX = mParams.x + width / 2;

        int animTime = 0;
        int xDistance = 0;
        int yDistance = 0;

        int dp_25 = dp2px(5f);

        if (middleX <= dp_25 + width / 2) {
            xDistance = dp_25 - mParams.x;
        } else if (middleX <= screenWidth / 2) {
            xDistance = dp_25 - mParams.x;
        } else if (middleX >= screenWidth - width / 2 - dp_25) {
            xDistance = screenWidth - mParams.x - width - dp_25;
        } else {
            xDistance = screenWidth - mParams.x - width - dp_25;
        }

        if (mParams.y < dp_25) {
            yDistance = dp_25 - mParams.y;
        } else if (mParams.y + height + dp_25 >= screenHeight) {
            yDistance = screenHeight - dp_25 - mParams.y - height;
        }

        if (Math.abs(xDistance) > Math.abs(yDistance)) {
            animTime = (int) (xDistance / screenWidth * 600f);
        } else{
            animTime = (int) (yDistance / screenHeight * 900f);
        }

        this.post(new AnchorAnimRunnable(Math.abs(animTime), xDistance, yDistance, System.currentTimeMillis()));
    }

    private class AnchorAnimRunnable implements Runnable {

        private Interpolator interpolator;
        private int startX;
        private int startY;
        private int animTime;
        private int xDistance;
        private int yDistance;
        private long currentStartTime;

        public AnchorAnimRunnable(int animTime, int xDistance, int yDistance, long currentStartTime){
            interpolator = new AccelerateDecelerateInterpolator();
            startX = mParams.x;
            startY = mParams.y;
            this.animTime = animTime;
            this.xDistance = xDistance;
            this.yDistance = yDistance;
            this.currentStartTime = currentStartTime;
        }

        @Override
        public void run() {
            if (System.currentTimeMillis() >= currentStartTime + animTime) {
                if (mParams.x != startX + xDistance || mParams.y != startY + yDistance) {
                    mParams.x = startX + xDistance;
                    mParams.y = startY + yDistance;
                    windowManager.updateViewLayout(BaseFloatView.this, mParams);
                }
                isAnchoring = false;
                return;
            }
            long times = (System.currentTimeMillis() - currentStartTime) / animTime;
            float delta = interpolator.getInterpolation(times);
            int xMoveDistance = (int) (xDistance * delta);
            int yMoveDistance = (int) (yDistance * delta);
            Log.e(TAG, "delta:  $delta  xMoveDistance  $xMoveDistance   yMoveDistance  $yMoveDistance");
            mParams.x = startX + xMoveDistance;
            mParams.y = startY + yMoveDistance;
            if (!isShowing) {
                return;
            }
            windowManager.updateViewLayout(BaseFloatView.this, mParams);
            BaseFloatView.this.postDelayed(this, 16);
        }
    }

    private int dp2px(float dp){
        float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }

    private void updateViewPosition() {
        //增加移动误差
        mParams.x = (int) (xInScreen - xInView);
        mParams.y = (int) (yInScreen - yInView);
        windowManager.updateViewLayout(this, mParams);
    }

    /**
     * 点击事件
     */
    public interface OnFloatClickListener {
        /**
         * 单击
         */
        void onClick();
    }


}
