package com.andryr.musicplayer.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by Andry on 09/11/15.
 */
public abstract class AdapterWithHeader<VH extends RecyclerView.ViewHolder> extends BaseAdapter<VH> {


    private static final int VIEW_TYPE_HEADER = 433;
    private View mHeaderView;
    private OnHeaderClickListener mOnHeaderClickListener;


    public void setHeaderView(View headerView) {
        mHeaderView = headerView;
        notifyDataSetChanged();
    }

    public void setOnHeaderClickListener(OnHeaderClickListener listener) {
        mOnHeaderClickListener = listener;
    }

    @Override
    public VH onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_HEADER) {
            return (VH) new HeaderViewHolder(mHeaderView);
        }
        return onCreateViewHolderImpl(parent, viewType);
    }

    @Override
    public void onBindViewHolder(VH holder, int position) {
        if (position >= 1 || mHeaderView == null) {
            onBindViewHolderImpl(holder, position - (mHeaderView != null ? 1 : 0));
        }
    }

    @Override
    public int getItemCount() {
        return getItemCountImpl() + (mHeaderView != null ? 1 : 0);
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0 && mHeaderView != null) {
            return VIEW_TYPE_HEADER;
        }
        return getItemViewTypeImpl(position - (mHeaderView != null ? 1 : 0));
    }

    @Override
    protected void triggerOnItemClickListener(int position, View view) {
        super.triggerOnItemClickListener(position - (mHeaderView != null ? 1 : 0), view);
    }

    public abstract VH onCreateViewHolderImpl(ViewGroup parent, int viewType);

    public abstract void onBindViewHolderImpl(VH holder, int position);

    public abstract int getItemCountImpl();

    public abstract int getItemViewTypeImpl(int position);

    public interface OnHeaderClickListener {
        void onHeaderClick();
    }

    private class HeaderViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public HeaderViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (mOnHeaderClickListener != null) {
                mOnHeaderClickListener.onHeaderClick();
            }
        }
    }
}