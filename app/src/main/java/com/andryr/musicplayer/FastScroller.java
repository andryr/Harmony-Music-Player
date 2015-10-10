package com.andryr.musicplayer;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.OnScrollListener;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.AnimatorListenerAdapter;
import com.nineoldandroids.animation.ValueAnimator;
import com.nineoldandroids.view.ViewHelper;

public class FastScroller extends FrameLayout {

    private float mHandleY;
    private TextView mBubble;
    private View mHandle;

    private boolean mScrolling = false;

    private int mVerticalPadding;

    private boolean mShowScroller = true;

    private RecyclerView mRecyclerView;

    private SectionIndexer mSectionIndexer;

    private ValueAnimator mBubbleAnimator = null;

    private ValueAnimator.AnimatorUpdateListener mBubbleAnimatorListener = new ValueAnimator.AnimatorUpdateListener() {
        @Override
        public void onAnimationUpdate(ValueAnimator animation) {
            ViewHelper.setAlpha(mBubble,(Float)animation.getAnimatedValue());
        }
    };

    private ValueAnimator mHandleAnimator = null;

    private ValueAnimator.AnimatorUpdateListener mHandleAnimatorListener = new ValueAnimator.AnimatorUpdateListener() {
        @Override
        public void onAnimationUpdate(ValueAnimator animation) {
            ViewHelper.setAlpha(mHandle,(Float)animation.getAnimatedValue());
        }
    };

    private Runnable mHideScrollerRunnable = new Runnable() {

        @Override
        public void run() {
            hideHandle();

        }
    };

    private OnScrollListener mOnScrollListener = new OnScrollListener() {

        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            int visibleItems = recyclerView.getChildCount();
            int itemCount = recyclerView.getAdapter().getItemCount();

            if(((float)itemCount)/visibleItems < 2.0F)
            {
                mShowScroller = false;
                return;
            }
            if (newState == RecyclerView.SCROLL_STATE_IDLE && !mScrolling) {
                postDelayed(mHideScrollerRunnable, 1500);
            } else if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                removeCallbacks(mHideScrollerRunnable);
                if(mHandle.getVisibility()!=View.VISIBLE)
                {
                    showHandle();
                }
            }

        }

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {

            if (mScrolling || !mShowScroller) {
                return;
            }





            int extent = recyclerView.computeVerticalScrollExtent();


            int offset = recyclerView.computeVerticalScrollOffset();
            int range = recyclerView.computeVerticalScrollRange()-extent;


            float proportion = ((float)offset)/range;

            moveHandleTo(proportion);



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

    private int getAvailableHeight()
    {
        return getHeight() - mVerticalPadding;
    }

    private void moveHandleTo(float proportion) {
        int height = getAvailableHeight();
        float pos = proportion * (height - mHandle.getHeight());
        ViewHelper.setY(mHandle,pos);
        mHandleY = pos;


    }



    private void scrollTo(float pos) {
        float proportion = Math.max(0, pos / getHeight());
        int itemCount = mRecyclerView.getAdapter().getItemCount();
        int itemPos = Math.min((int) (proportion * itemCount), itemCount - 1);

        mRecyclerView.scrollToPosition(itemPos);

        float scrollerPos = pos - (mHandle.getHeight() / 2);
        int height = getHeight() - mVerticalPadding;
        scrollerPos = Math.max(0,
                Math.min(height - mHandle.getHeight(), scrollerPos));
        ViewHelper.setY(mHandle,scrollerPos);
        mHandleY = scrollerPos;

        updateBubble(itemPos);

    }

    private void updateBubble(int position) {
        if (mSectionIndexer == null) {
            return;
        }

        if (mBubble.getVisibility() != View.VISIBLE) {
            showBubble();
        }


        mBubble.setText(mSectionIndexer.getSectionForPosition(position));

    }



    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            float x = ev.getX();
            float y = ev.getY();

            if (x > ViewHelper.getX(mHandle) && x < ViewHelper.getX(mHandle) + mHandle.getWidth()
                    && y > mHandleY && y < ViewHelper.getY(mHandle) + mHandle.getHeight()) {
                mScrolling = true;
                removeCallbacks(mHideScrollerRunnable);
                if(mHandle.getVisibility()!=View.VISIBLE)
                {
                    showHandle();
                }
                mHandle.setPressed(true);
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
                hideBubble();
                mScrolling = false;
                mOnScrollListener.onScrollStateChanged(mRecyclerView,
                        RecyclerView.SCROLL_STATE_IDLE);
                mHandle.setPressed(false);

                break;
        }
        return mScrolling;
    }

    private void showBubble() {
        if(mBubbleAnimator != null)
        {
            mBubbleAnimator.cancel();
        }
        mBubble.setVisibility(View.VISIBLE);
        mBubbleAnimator = ValueAnimator.ofFloat(0.0F,1.0F);
        mBubbleAnimator.addUpdateListener(mBubbleAnimatorListener);
        mBubbleAnimator.start();
    }

    private void hideBubble() {
        if(mBubbleAnimator != null)
        {
            mBubbleAnimator.cancel();
        }

        mBubbleAnimator = ValueAnimator.ofFloat(1.0F,0.0F);
        mBubbleAnimator.addUpdateListener(mBubbleAnimatorListener);
        mBubbleAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mBubble.setVisibility(View.GONE);
            }
        });
        mBubbleAnimator.start();
    }

    private void showHandle()
    {
        ViewHelper.setAlpha(mHandle,1.0F);
        mHandle.setVisibility(View.VISIBLE);
    }

    private void hideHandle()
    {
        if(mHandleAnimator == null)
        {
            mHandleAnimator = ValueAnimator.ofFloat(1.0F,0.0F);
            mHandleAnimator.addUpdateListener(mHandleAnimatorListener);
            mHandleAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mHandle.setVisibility(View.GONE);
                }
            });
        }
        else
        {
            mHandleAnimator.cancel();
        }


        mHandleAnimator.start();


    }

    public interface SectionIndexer {
        String getSectionForPosition(int position);
    }

}
