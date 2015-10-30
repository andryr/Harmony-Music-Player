package com.andryr.musicplayer.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Created by Andry on 28/10/15.
 */
public abstract class BaseAdapter<VH extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<VH> {

    private OnItemClickListener mOnItemClickListener;

    public void setOnItemClickListener(OnItemClickListener listener)
    {
        mOnItemClickListener = listener;
    }

    protected void triggerOnItemClickListener(int position, View view)
    {
        if(mOnItemClickListener != null)
        {
            mOnItemClickListener.onItemClick(position, view);
        }
    }

    public interface OnItemClickListener {
        void onItemClick(int position, View view);
    }
}
