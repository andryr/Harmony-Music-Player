package com.andryr.musicplayer;

import android.content.Context;
import android.graphics.Canvas;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.nineoldandroids.view.ViewHelper;

/**
 * Created by andry on 30/08/15.
 */
abstract public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public static final int NORMAL = 1;
    public static final int HEADER = 2;

    private HeaderLayout mHeader;

    private RecyclerView mRecyclerView;

    private boolean mParallaxEnabled = false;

    private OnParallaxScrollListener mParallaxScrollListener;

    public RecyclerViewAdapter(RecyclerView recyclerView) {
        super();
        mRecyclerView = recyclerView;
        mRecyclerView.setAdapter(this);
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if(mHeader != null && mParallaxEnabled)
                {
                    float offset = mRecyclerView.computeVerticalScrollOffset()*0.5f;
                    ViewHelper.setTranslationY(mHeader,Math.round(offset));
                    mHeader.setClipOffset(Math.round(offset));

                    if(mParallaxScrollListener!=null)
                    {
                        offset = Math.min(1, 2 * offset / mHeader.getHeight());
                        mParallaxScrollListener.onParallaxScroll(offset);
                    }

                }
            }
        });
    }

    public void setParallaxEnabled(boolean enable)
    {
        mParallaxEnabled = enable;

    }

    public void setOnParallaxScrollListener(OnParallaxScrollListener listener)
    {
        mParallaxScrollListener = listener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if(viewType == HEADER && mHeader != null)
        {
            return new HeaderViewHolder(mHeader);
        }


        return onCreateViewHolderImpl(parent,viewType);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if(position !=0 && mHeader != null)
        {
            onBindViewHolderImpl(holder,position-1);
        }
        else if(position != 0)
        {
            onBindViewHolderImpl(holder,position);
        }
    }

    @Override
    public int getItemCount() {
        return mHeader==null?getItemCountImpl():getItemCountImpl()+1;
    }

    @Override
    public int getItemViewType(int position) {
        return position==0?HEADER:NORMAL;
    }

    abstract public RecyclerView.ViewHolder onCreateViewHolderImpl(ViewGroup parent, int viewType);

    abstract public void onBindViewHolderImpl(RecyclerView.ViewHolder holder, int position);

    abstract public int getItemCountImpl();

    public void setHeader(View header)
    {
        ViewGroup.LayoutParams lp = header.getLayoutParams();
        mHeader = new HeaderLayout(mRecyclerView.getContext());
        RecyclerView.LayoutManager layoutManager = mRecyclerView.getLayoutManager();
        if(lp!=null) {
            mHeader.setLayoutParams(layoutManager.generateLayoutParams(lp));
        }
        else {
            mHeader.setLayoutParams(layoutManager.generateDefaultLayoutParams());
        }
        mHeader.addView(header,new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));


    }

    public View setHeader(int resourceId)
    {
        Context context = mRecyclerView.getContext();
        mHeader = new HeaderLayout(context);

        View headerView = LayoutInflater.from(context).inflate(resourceId,mHeader,false);
        mHeader.setLayoutParams(mRecyclerView.getLayoutManager().generateLayoutParams(headerView.getLayoutParams()));
        mHeader.addView(headerView, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        return headerView;
    }

    public int getViewPosition(View view)
    {
        return mHeader==null?mRecyclerView.getChildAdapterPosition(view):mRecyclerView.getChildAdapterPosition(view)-1;
    }

    public static class HeaderViewHolder extends RecyclerView.ViewHolder
    {

        public HeaderViewHolder(View itemView) {
            super(itemView);
        }
    }

    private class HeaderLayout extends FrameLayout
    {
        private int mClipOffset;

        public HeaderLayout(Context context) {
            super(context);
        }

        public void setClipOffset(int offset)
        {
            mClipOffset = offset;
            invalidate();
        }

        @Override
        protected void dispatchDraw(Canvas canvas) {
            canvas.clipRect(getLeft(),getTop(),getRight(),getBottom()+mClipOffset);
            super.dispatchDraw(canvas);
        }
    }

    public interface OnParallaxScrollListener
    {
        void onParallaxScroll(float offset);
    }


}
