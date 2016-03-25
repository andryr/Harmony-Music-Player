package org.oucho.musicplayer.animation;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;


public class TransitionDrawable extends Drawable {

    private static final float TRANSITION_DURATION = 200F;

    private static final long FRAMERATE = 1000 / 60;

    private final Drawable mFirstDrawable;
    private final Drawable mSecondDrawable;

    private int mAlpha = 255;

    private boolean mAnimating = false;
    private long mAnimationStartTime;

    private boolean mShowFirstDrawable = true;

    private final Handler mHandler = new Handler(Looper.getMainLooper());

    private final Runnable mInvalidateRunnable = new Runnable() {
        @Override
        public void run() {
            invalidateSelf();
        }
    };


    public TransitionDrawable(Drawable d1, Drawable d2) {
        mFirstDrawable = copyDrawable(d1);
        mSecondDrawable = copyDrawable(d2);


    }

    private Drawable copyDrawable(Drawable d) {
        return d != null ? d.getConstantState().newDrawable().mutate() : null;
    }

    @Override
    public void draw(Canvas canvas) {

        if (mAnimating) {

            float progress = (SystemClock.elapsedRealtime() - mAnimationStartTime) / TRANSITION_DURATION;
            if (progress < 1.0F) {
                mFirstDrawable.setAlpha(Math.round((1.0F - progress) * mAlpha));
                mSecondDrawable.setAlpha(Math.round(progress * mAlpha));
                mFirstDrawable.draw(canvas);
                mSecondDrawable.draw(canvas);
            } else {

                mShowFirstDrawable = false;
                mAnimating = false;
                mSecondDrawable.setAlpha(mAlpha);
                mSecondDrawable.draw(canvas);
            }
            mHandler.postDelayed(mInvalidateRunnable,FRAMERATE);


        } else if (mShowFirstDrawable) {
            mFirstDrawable.draw(canvas);
        } else {
            mSecondDrawable.draw(canvas);
        }
    }

    @Override
    public void setAlpha(int alpha) {
        mAlpha = alpha;
    }

    @Override
    public void setColorFilter(ColorFilter cf) {
        if (mFirstDrawable != null) {
            mFirstDrawable.setColorFilter(cf);
        }

        if (mSecondDrawable != null) {
            mSecondDrawable.setColorFilter(cf);
        }
    }

    @Override
    public int getOpacity() {
        return resolveOpacity(mFirstDrawable != null ? mFirstDrawable.getOpacity() : 0, mSecondDrawable != null ? mSecondDrawable.getOpacity() : 0);
    }

    @Override
    protected void onBoundsChange(Rect bounds) {

        if (mFirstDrawable != null) {
            mFirstDrawable.setBounds(bounds);
        }

        if (mSecondDrawable != null) {
            mSecondDrawable.setBounds(bounds);
        }

        super.onBoundsChange(bounds);

    }

    @Override
    public int getIntrinsicHeight() {
        return mSecondDrawable.getIntrinsicHeight();
    }

    @Override
    public int getIntrinsicWidth() {
        return mSecondDrawable.getIntrinsicWidth();
    }

    public void startTransition() {
        mAnimationStartTime = SystemClock.elapsedRealtime();
        mAnimating = true;
        invalidateSelf();
    }


}
