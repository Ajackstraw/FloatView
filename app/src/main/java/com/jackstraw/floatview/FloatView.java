package com.jackstraw.floatview;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;
import com.jackstraw.floatwindow.view.BaseFloatView;

/**
 * @author pink-jackstraw
 * @date 2019/4/20
 * @describe
 */
public class FloatView extends BaseFloatView implements BaseFloatView.OnFloatClickListener {

    private Context context;

    public FloatView(Context context) {
        this(context, null);
    }

    public FloatView(Context context, AttributeSet attrs) {
        this(context, attrs, -1);
    }

    public FloatView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void createFloatView(Context context) {
        this.context = context;
        LayoutInflater inflater = LayoutInflater.from(context);
        View floatView = inflater.inflate(R.layout.float_window_layout, null);
        addView(floatView);
        setOnFloatClickListener(this);
    }

    @Override
    protected boolean isAnchorToSide() {
        return true;
    }

    @Override
    public void onClick() {
        Toast.makeText(context, "FloatView-click", Toast.LENGTH_LONG).show();
    }
}
