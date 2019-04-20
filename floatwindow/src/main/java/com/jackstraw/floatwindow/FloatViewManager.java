package com.jackstraw.floatwindow;

import android.content.Context;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.os.Build;
import android.util.Log;
import android.view.Gravity;
import android.view.WindowManager;

import com.jackstraw.floatwindow.permission.FloatUtils;
import com.jackstraw.floatwindow.view.BaseFloatView;


/**
 * @author pink-jackstraw
 * @date 2019/4/19
 * @describe
 */
public class FloatViewManager {

    private final String TAG = "FloatViewManager";
    private boolean isWindowDismiss = true;
    private WindowManager windowManager = null;
    private WindowManager.LayoutParams mParams = null;
    private BaseFloatView floatView = null;
    private FloatUtils floatUtils = null;
    private static FloatViewManager instance = null;

    public static FloatViewManager getInstance() {
        if (instance == null) {
            synchronized(FloatViewManager.class) {
                if (instance == null) {
                    instance = new FloatViewManager();
                }
            }
        }
        return instance;
    }

    /**
     * 显示浮窗
     * @param context
     */
    public void showFloatView(Context context, BaseFloatView t) {
        if (t == null) {
            return;
        }

        if (floatUtils == null) {
            floatUtils = new FloatUtils();
        }

        if (floatUtils.checkPermission(context)) {
            showWindow(context, t);
        } else {
            floatUtils.applyPermission(context);
        }
    }

    /**
     * 关闭浮窗
     */
    public void dismissFloatView() {
        if (isWindowDismiss) {
            Log.e(TAG, "window can not be dismiss cause it has not been added");
            return;
        }

        isWindowDismiss = true;
        if (windowManager != null && floatView != null) {
            floatView.setIsShowing(false);
            windowManager.removeViewImmediate(floatView);
        }
    }

    private void showWindow(Context context, BaseFloatView t) {
        if (!isWindowDismiss) {
            Log.e(TAG, "view is already added here");
            return;
        }

        if(context == null){
            Log.e(TAG, "context == null");
            return;
        }

        isWindowDismiss = false;
        if (windowManager == null)
            windowManager = (WindowManager) context.getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
        Point size = new Point();
        windowManager.getDefaultDisplay().getSize(size);
        int screenWidth = size.x;
        int screenHeight = size.y;

        mParams = new WindowManager.LayoutParams();
        mParams.packageName = context.getPackageName();
        mParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
        mParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        mParams.flags = (WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR
                | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN);
        int mType;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mType = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            mType = WindowManager.LayoutParams.TYPE_SYSTEM_ERROR;
        }
        mParams.type = mType;
        mParams.format = PixelFormat.RGBA_8888;
        mParams.gravity = Gravity.LEFT | Gravity.TOP;
        mParams.x = screenWidth - dp2px(context, 100f);
        mParams.y = screenHeight - dp2px(context, 171f);

        floatView = t;
        floatView.setParams(mParams);
        floatView.setIsShowing(true);
        windowManager.addView(floatView, mParams);
    }

    private int dp2px(Context context, float dp){
        float scale = context.getResources().getDisplayMetrics().density;
        float result = dp * scale + 0.5f;
        return (int) result;
    }

}
