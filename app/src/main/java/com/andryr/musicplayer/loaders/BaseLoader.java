package com.andryr.musicplayer.loaders;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;

/**
 * Created by andry on 22/08/15.
 */
abstract public class BaseLoader<D> extends AsyncTaskLoader<D> {

    private D mData;

    private String mFilter;

    private String mSelectionString;
    private String[] mSelectionArgs;

    public BaseLoader(Context context) {
        super(context);
    }

    @Override
    protected void onStartLoading() {
        if (mData != null) {
            deliverResult(mData);
        }
        if (takeContentChanged() || mData == null) {
            forceLoad();
        }
    }

    public String getFilter() {
        return mFilter;
    }

    public void setFilter(String filter) {
        mFilter = filter;
    }

    @Override
    protected void onReset() {
        super.onReset();
        mData = null;
        onStopLoading();
    }

    @Override
    protected void onStopLoading() {
        super.onStopLoading();
        cancelLoad();
    }

    @Override
    public void deliverResult(D data) {
        if (!isReset()) {
            super.deliverResult(data);
        }
    }

    public void setSelection(String selectionString, String[] selectionArgs) {
        mSelectionString = selectionString;
        mSelectionArgs = selectionArgs;
    }


    protected String getSelectionString() {
        return mSelectionString;

    }

    protected String[] getSelectionArgs() {
        return mSelectionArgs;
    }


}
