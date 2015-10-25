package com.andryr.musicplayer;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by Andry on 25/10/15.
 */
public class ProgressBar extends View {

    private int mMax;
    private int mProgress;
    private int mColor;
    private Paint mPaint;


    public ProgressBar(Context context) {
        super(context);
        init(context, null);
    }

    public ProgressBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    public ProgressBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        if (attrs != null) {
            TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.ProgressBar, 0, 0);
            try {
                mColor = a.getColor(R.styleable.ProgressBar_progressColor, 0);
            } finally {
                a.recycle();
            }
        }

        mPaint = new Paint();
        mPaint.setColor(mColor);
    }

    public int getMax() {
        return Math.max(1, mMax);
    }

    public void setMax(int max) {
        mMax = max;
    }

    public int getProgress() {
        return mProgress;
    }

    public void setProgress(int progress) {
        mProgress = progress;
        invalidate();
    }

    public int getColor() {
        return mColor;
    }

    public void setColor(int color) {
        mColor = color;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.drawRect(0, 0, getWidth() * (getProgress() / (float) getMax()), getHeight(), mPaint);
    }
}
