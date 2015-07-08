package com.andryr.musicplayer;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.OnScrollListener;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.SectionIndexer;
import android.widget.TextView;

public class FastScroller extends FrameLayout {

    private float mHandleY;
    private TextView mBubble;
    private View mHandle;

    private boolean mScrolling = false;

    private int mVerticalPadding;

    private RecyclerView mRecyclerView;

    private SectionIndexer mSectionIndexer;

    private Runnable mHideScrollerRunnable = new Runnable() {

        @Override
        public void run() {
            mHandle.setVisibility(View.GONE);

        }
    };

    private OnScrollListener mOnScrollListener = new OnScrollListener() {

        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            if (newState == RecyclerView.SCROLL_STATE_IDLE && !mScrolling) {
                postDelayed(mHideScrollerRunnable, 1500);
            } else if (newState == RecyclerView.SCROLL_STATE_DRAGGING
                    && mHandle.getVisibility() != View.VISIBLE) {
                removeCallbacks(mHideScrollerRunnable);
                mHandle.setVisibility(View.VISIBLE);

            }
        }

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            if (mScrolling) {
                return;
            }
            View firstVisibleView = recyclerView.getChildAt(0);
            int position = recyclerView.getChildPosition(firstVisibleView);

            int itemCount = recyclerView.getAdapter().getItemCount();
            int visibleItems = recyclerView.getChildCount();

            float proportion = (float) position
                    / (float) (itemCount - visibleItems);

            setScrollerPosition(proportion);
        }

    };

    public FastScroller(Context context) {
        super(context);
        init(context);
    }

    public FastScroller(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public FastScroller(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        LayoutInflater inflater = LayoutInflater.from(context);
        inflater.inflate(R.layout.fastscroller, this);
        View root = findViewById(R.id.fastscroller);
        mVerticalPadding = root.getPaddingTop() + root.getPaddingBottom();
        mHandle = findViewById(R.id.handle);

        mBubble = (TextView) findViewById(R.id.bubble);
    }

    public void setRecyclerView(RecyclerView view) {
        mRecyclerView = view;
        mRecyclerView.setOnScrollListener(mOnScrollListener);

    }

    public void setSectionIndexer(SectionIndexer si) {
        mSectionIndexer = si;
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void setScrollerPosition(float proportion) {
        int height = getHeight() - mVerticalPadding;
        float pos = proportion * (height - mHandle.getHeight());
        mHandle.setY(pos);
        mHandleY = pos;
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void scrollTo(float pos) {
        float proportion = pos / getHeight();
        int itemPos = (int) (proportion * mRecyclerView.getAdapter()
                .getItemCount());
        mRecyclerView.scrollToPosition(itemPos);

        float scrollerPos = pos - (mHandle.getHeight() / 2);
        int height = getHeight() - mVerticalPadding;
        scrollerPos = Math.max(0,
                Math.min(height - mHandle.getHeight(), scrollerPos));
        mHandle.setY(scrollerPos);
        mHandleY = scrollerPos;

        updateBubble(itemPos);

    }

    private void updateBubble(int position) {
        if (mSectionIndexer == null) {
            return;
        }

        if (mBubble.getVisibility() != View.VISIBLE) {
            mBubble.setVisibility(View.VISIBLE);
        }

        int index = mSectionIndexer.getSectionForPosition(position);
        Object section = mSectionIndexer.getSections()[index];
        if (section != null) {
            mBubble.setText(section.toString());
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            float x = ev.getX();
            float y = ev.getY();
            Log.e("pos", String.format("x %f %f %f y %f %f %f", mHandle.getX(),
                    x, mHandle.getX() + mHandle.getWidth(), mHandle.getY(), y,
                    mHandle.getY() + mHandle.getHeight()));
            if (x > mHandle.getX() && x < mHandle.getX() + mHandle.getWidth()
                    && y > mHandleY && y < mHandleY + mHandle.getHeight()) {
                mScrolling = true;
                removeCallbacks(mHideScrollerRunnable);

                return true;
            }
        }
        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        float y = ev.getY();

        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                break;
            case MotionEvent.ACTION_MOVE:
                scrollTo(y);
                break;

            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                scrollTo(y);
                mBubble.setVisibility(View.GONE);
                mScrolling = false;
                mOnScrollListener.onScrollStateChanged(mRecyclerView,
                        RecyclerView.SCROLL_STATE_IDLE);

                break;
        }
        return mScrolling;
    }

}
