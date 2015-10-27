package com.andryr.musicplayer.utils;


import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.OnItemTouchListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.ImageView;

import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.AnimatorListenerAdapter;
import com.nineoldandroids.view.ViewHelper;
import com.nineoldandroids.view.ViewPropertyAnimator;

public abstract class OnItemMovedListener implements OnItemTouchListener {

    private boolean mDragging = false;

    private int mCurrentTop;
    private int mCurrentBottom;

    private int mCurrentPosition;

    private int mAnimationDuration;

    private RecyclerView mRecyclerView;
    private ImageView mHandle;
    private View mDraggedView;

    public OnItemMovedListener(RecyclerView recyclerView, ImageView handle) {

        mRecyclerView = recyclerView;
        mHandle = handle;

        mAnimationDuration = mRecyclerView.getContext().getResources()
                .getInteger(android.R.integer.config_shortAnimTime);


    }

    @Override
    public void onRequestDisallowInterceptTouchEvent(boolean b) {

    }


    @Override
    public boolean onInterceptTouchEvent(RecyclerView view, MotionEvent ev) {

        return mDragging;
    }

    @Override
    public void onTouchEvent(RecyclerView recyclerView, MotionEvent ev) {
        if (!mDragging) {
            return;
        }
        float y = ev.getY();

        View v = recyclerView
                .findChildViewUnder(recyclerView.getWidth() / 2, y);
        if (v == null) {
            return;
        }
        int position = recyclerView.getChildPosition(v);
        int recyclerTop = mRecyclerView.getTop();

        switch (ev.getAction()) {
            case MotionEvent.ACTION_MOVE:
                ViewHelper.setY(mHandle, recyclerTop + y - mHandle.getHeight() / 2);

                if (mCurrentPosition != position
                        && (y < mCurrentTop || y > mCurrentBottom)) {

                    onItemMoved(mCurrentPosition, position);

                    mCurrentPosition = position;
                    mCurrentTop = v.getTop();
                    mCurrentBottom = v.getBottom();
                }


                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:

                ViewPropertyAnimator.animate(mHandle).y(recyclerTop + mCurrentTop)
                        .setDuration(mAnimationDuration)
                        .setListener(new AnimatorListenerAdapter() {

                            @Override
                            public void onAnimationEnd(Animator animation) {
                                mHandle.setVisibility(View.GONE);
                                mDraggedView.setVisibility(View.VISIBLE);
                            }

                        }).start();
                if (mCurrentPosition != position) {

                    onItemMoved(mCurrentPosition, position);
                }

                mCurrentPosition = position;
                mDragging = false;

                break;
        }

    }

    abstract public void onItemMoved(int oldPosition, int newPosition);

    public void startDrag(View childView) {
        mDragging = true;

        mDraggedView = childView;

        mCurrentTop = mDraggedView.getTop();
        mCurrentBottom = mDraggedView.getBottom();
        mCurrentPosition = mRecyclerView.getChildAdapterPosition(mDraggedView);

        Bitmap bitmap = Bitmap.createBitmap(mDraggedView.getWidth(),
                mDraggedView.getHeight(), Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        mDraggedView.draw(canvas);
        mHandle.setImageBitmap(bitmap);

        ViewHelper.setY(mHandle, mRecyclerView.getTop() + mCurrentTop);

        mDraggedView.setVisibility(View.INVISIBLE);
        mHandle.setVisibility(View.VISIBLE);
    }

}
