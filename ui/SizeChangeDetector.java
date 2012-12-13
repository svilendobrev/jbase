package com.svilendobrev.ui;

import com.svilendobrev.jbase.Log;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

public class SizeChangeDetector extends LinearLayout {
    public SizeChangeDetector( Context context, AttributeSet attrs) { super(context, attrs); }

    public static interface OnSizeChangedListener {
        public void onSizeChanged(SizeChangeDetector v, int w, int h, int oldw, int oldh);
    }

    public OnSizeChangedListener listener = null;

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged( w, h, oldw, oldh);
        if (oldw == 0 && oldh == 0) {
            Log.d("skip");
            return; //just added to view hierarchy
        }
        if (listener != null)
            listener.onSizeChanged( this, w, h, oldw, oldh);
    }
}

// vim:ts=4:sw=4:expandtab
