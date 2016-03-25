package org.oucho.musicplayer.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


public abstract class AdapterWithHeader<VH extends RecyclerView.ViewHolder> extends BaseAdapter<VH> {


    private static final int VIEW_TYPE_HEADER = 433;
    private View mHeaderView;
    private OnHeaderClickListener mOnHeaderClickListener;

    private int mHeaderLayoutId;
    private boolean mHeaderSet = false;


    public void setHeaderView(View view) {
        mHeaderView = view;
        mHeaderSet = true;
        notifyDataSetChanged();
    }

    public void setOnHeaderClickListener(OnHeaderClickListener listener) {
        mOnHeaderClickListener = listener;
    }

    @Override
    public VH onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_HEADER) {
            if(mHeaderView == null) {
                mHeaderView = LayoutInflater.from(parent.getContext()).inflate(
                        mHeaderLayoutId, parent, false);
            }
            //noinspection unchecked
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
        if (position == 0 && mHeaderSet) {
            return VIEW_TYPE_HEADER;
        }
        return getItemViewTypeImpl(position - (mHeaderView != null ? 1 : 0));
    }

    @Override
    void triggerOnItemClickListener(int position, View view) {
        super.triggerOnItemClickListener(position - (mHeaderView != null ? 1 : 0), view);
    }

    protected abstract VH onCreateViewHolderImpl(ViewGroup parent, int viewType);

    protected abstract void onBindViewHolderImpl(VH holder, int position);

    protected abstract int getItemCountImpl();

    protected abstract int getItemViewTypeImpl(int position);

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
