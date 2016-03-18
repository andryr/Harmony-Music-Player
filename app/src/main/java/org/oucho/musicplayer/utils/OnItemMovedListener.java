package org.oucho.musicplayer.utils;


import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.OnItemTouchListener;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

public abstract class OnItemMovedListener implements OnItemTouchListener {

    private boolean mDragging = false;

    private int mCurrentTop;
    private int mCurrentBottom;

    private int mCurrentPosition;

    private RecyclerView mRecyclerView;
    private ImageView mHandle;

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
        int position = recyclerView.getChildLayoutPosition(v);

        switch (ev.getAction()) {
            case MotionEvent.ACTION_MOVE:

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

        mCurrentTop = childView.getTop();
        mCurrentBottom = childView.getBottom();
        mCurrentPosition = mRecyclerView.getChildAdapterPosition(childView);

        Bitmap bitmap = Bitmap.createBitmap(childView.getWidth(),
                childView.getHeight(), Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        childView.draw(canvas);
        mHandle.setImageBitmap(bitmap);

        //ViewHelper.setY(mHandle, mRecyclerView.getTop() + mCurrentTop);

        childView.setVisibility(View.INVISIBLE);
        mHandle.setVisibility(View.VISIBLE);
    }

}
