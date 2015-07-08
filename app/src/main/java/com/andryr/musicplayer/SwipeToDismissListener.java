package com.andryr.musicplayer;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.OnItemTouchListener;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public abstract class SwipeToDismissListener implements OnItemTouchListener {

    private View mDownView;
    private float mDownX;
    private float mDownY;
    private int mPosition;

    private int mTouchSlop;
    private int mMinimumVelocity;
    private VelocityTracker mVelocityTracker;
    private int mAnimationDuration;

    public SwipeToDismissListener(Context context) {
        ViewConfiguration viewConfig = ViewConfiguration.get(context);
        mTouchSlop = viewConfig.getScaledTouchSlop();

        mMinimumVelocity = viewConfig.getScaledMinimumFlingVelocity();
        mAnimationDuration = context.getResources().getInteger(
                android.R.integer.config_shortAnimTime);

    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    public boolean onInterceptTouchEvent(RecyclerView recyclerView,
                                         MotionEvent ev) {

        float x = ev.getX();
        float y = ev.getY();
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (mVelocityTracker != null) {
                    mVelocityTracker.recycle();
                    mVelocityTracker = null;
                }
                if (mVelocityTracker == null) {
                    mVelocityTracker = VelocityTracker.obtain();
                }
                mDownView = recyclerView.findChildViewUnder(x, y);
                mPosition = recyclerView.getChildPosition(mDownView);
                mDownX = x;
                mDownY = y;
                mVelocityTracker.addMovement(ev);
                break;
            case MotionEvent.ACTION_MOVE:
                View view = recyclerView.findChildViewUnder(x, y);
                float deltaX = x - mDownX;
                float deltaY = y - mDownY;
                mVelocityTracker.addMovement(ev);

                if (canBeDismissed(mPosition) && mDownView != null
                        && view == mDownView && Math.abs(deltaX) > mTouchSlop && Math.abs(deltaY) < mTouchSlop) {
                    mDownView.setTranslationX(Math.max(0, deltaX));
                    return true;
                }
                break;

        }
        return false;
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
    @Override
    public void onTouchEvent(RecyclerView recyclerView, MotionEvent ev) {
        float x = ev.getX();

        switch (ev.getAction()) {

            case MotionEvent.ACTION_MOVE:
                float deltaXabs = Math.max(0, x - mDownX);

                mDownView.setTranslationX(deltaXabs);
                mDownView.setAlpha(1.0F - deltaXabs
                        / (float) recyclerView.getWidth());
                mVelocityTracker.addMovement(ev);
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                mVelocityTracker.addMovement(ev);
                mVelocityTracker.computeCurrentVelocity(1000);
                float velocity = mVelocityTracker.getXVelocity();
                if (velocity >= mMinimumVelocity
                        || x >= recyclerView.getWidth() / 2) {
                    mDownView.animate().setDuration(mAnimationDuration).alpha(0.0f)
                            .translationX(recyclerView.getWidth())
                            .setListener(new AnimatorListenerAdapter() {

                                @Override
                                public void onAnimationEnd(Animator animation) {
                                    dismiss();
                                }

                            });
                } else {
                    mDownView.animate().setDuration(mAnimationDuration).alpha(1.0f)
                            .translationX(0);
                }
                mVelocityTracker.recycle();
                mVelocityTracker = null;
                break;

        }

    }

    private void dismiss() {

        onDismiss(mPosition);
        mDownView.setAlpha(1.0F);
        mDownView.setTranslationX(0);
    }

    abstract public void onDismiss(int position);

    abstract protected boolean canBeDismissed(int position);

}
