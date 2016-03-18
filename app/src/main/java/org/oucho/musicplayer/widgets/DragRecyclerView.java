package org.oucho.musicplayer.widgets;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;


public class DragRecyclerView extends RecyclerView {
    private boolean mDragging = false;
    private boolean mAnimating = false;
    private int mCurrentTop;
    private int mCurrentBottom;
    private int mCurrentPosition;
    private int mAnimationDuration;
    private Drawable mHandleDrawable;
    private final Rect mHandleBounds = new Rect();
    private View mDraggedView;

    private OnItemMovedListener mOnItemMovedListener = null;

    public DragRecyclerView(Context context) {
        super(context);
        init(context);
    }

    public DragRecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public DragRecyclerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    public void setOnItemMovedListener(OnItemMovedListener listener) {
        mOnItemMovedListener = listener;
    }

    private void triggerListener(int oldPosition, int newPosition) {
        if (mOnItemMovedListener != null) {
            mOnItemMovedListener.onItemMoved(oldPosition, newPosition);
        }
    }

    private void init(Context context) {


        mAnimationDuration = context.getResources()
                .getInteger(android.R.integer.config_shortAnimTime);
        addOnItemTouchListener(new ItemTouchListener());
    }

    @Override
    public void onDraw(Canvas c) {
        super.onDraw(c);

        if (mDragging || mAnimating) {
            mHandleDrawable.draw(c);
        }
    }


    public void startDrag(View childView) {

        Context context = getContext();
        mDragging = true;

        mDraggedView = childView;

        mCurrentTop = mDraggedView.getTop();
        mCurrentBottom = mDraggedView.getBottom();
        mCurrentPosition = getChildAdapterPosition(mDraggedView);

        Bitmap bitmap = Bitmap.createBitmap(mDraggedView.getWidth(),
                mDraggedView.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        mDraggedView.draw(canvas);
        mHandleDrawable = new BitmapDrawable(context.getResources(), bitmap);

        mHandleBounds.left = mDraggedView.getLeft();
        mHandleBounds.top = mCurrentTop;
        mHandleBounds.right = mHandleBounds.left + mDraggedView.getWidth();
        mHandleBounds.bottom = mHandleBounds.top + mDraggedView.getHeight();

        mHandleDrawable.setBounds(mHandleBounds);

        mDraggedView.setVisibility(View.INVISIBLE);
    }

    public interface OnItemMovedListener {
        void onItemMoved(int oldPosition, int newPosition);

    }

    private class ItemTouchListener implements OnItemTouchListener {
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
            int position = recyclerView.getChildAdapterPosition(v);

            switch (ev.getAction()) {
                case MotionEvent.ACTION_MOVE:

                    mHandleBounds.offsetTo(mHandleBounds.left, (int) (y - mHandleBounds.height() / 2));
                    mHandleDrawable.setBounds(mHandleBounds);

                    if (mCurrentPosition != position
                            && (y < mCurrentTop || y > mCurrentBottom)) {

                        if (position > mCurrentPosition) {
                            for (int i = mCurrentPosition; i < position; i++) {
                                triggerListener(i, i + 1);
                            }
                        } else {
                            for (int i = mCurrentPosition; i > position; i--) {
                                triggerListener(i, i - 1);
                            }
                        }

                        mCurrentPosition = position;
                        mCurrentTop = v.getTop();
                        mCurrentBottom = v.getBottom();
                    }

                    invalidate();
                    break;
                case MotionEvent.ACTION_CANCEL:
                case MotionEvent.ACTION_UP:

                    ValueAnimator anim = ValueAnimator.ofInt(mHandleBounds.top, mCurrentTop)
                            .setDuration(mAnimationDuration);
                    anim.addListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationStart(Animator animation) {
                            mAnimating = true;
                            invalidate();
                        }

                        @Override
                        public void onAnimationEnd(Animator animation) {
                            mAnimating = false;
                            invalidate();
                            mDraggedView.setVisibility(View.VISIBLE);
                        }

                    });
                    anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                        @Override
                        public void onAnimationUpdate(ValueAnimator animation) {
                            mHandleBounds.offsetTo(mHandleBounds.left, (Integer) animation.getAnimatedValue());
                            mHandleDrawable.setBounds(mHandleBounds);
                            invalidate();
                        }
                    });
                    anim.start();
                    if (mCurrentPosition != position) {


                        if (position > mCurrentPosition) {
                            for (int i = mCurrentPosition; i < position; i++) {
                                triggerListener(i, i + 1);
                            }
                        } else {
                            for (int i = mCurrentPosition; i > position; i--) {
                                triggerListener(i, i - 1);
                            }
                        }

                    }

                    mCurrentPosition = position;
                    mDragging = false;
                    invalidate();

                    break;
            }

        }
    }
}
