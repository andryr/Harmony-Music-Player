package org.oucho.musicplayer.loaders;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.database.DatabaseUtilsCompat;

abstract public class BaseLoader<D> extends AsyncTaskLoader<D> {

    private D mData;

    private String mFilter;

    private String mSelectionString;
    private String[] mSelectionArgs;

    BaseLoader(Context context) {
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

    String getFilter() {
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


    String getSelectionString() {
        return mSelectionString;

    }

    String[] getSelectionArgs() {
        return mSelectionArgs;
    }

    @Nullable
    Cursor getCursor(Uri musicUri, String[] projection, String selection, String[] selectionArgs, String filteredFieldName, String filter, String orderBy) {
        Cursor cursor;
        if (filter != null) {
            if (filter.equals("")) {
                return null; // empty filter means that we don't want any result
            }
            selection = DatabaseUtilsCompat.concatenateWhere(selection, filteredFieldName + " LIKE ?");
            selectionArgs = DatabaseUtilsCompat.appendSelectionArgs(selectionArgs, new String[]{"%" + filter + "%"});

        }

        cursor = getContext().getContentResolver().query(musicUri, projection,
                selection, selectionArgs,
                orderBy);

        return cursor;
    }

    @Nullable
    protected Cursor getCursor(Uri musicUri, String[] projection, String selection, String[] selectionArgs, String filteredFieldName, String filter) {
        return getCursor(musicUri, projection, selection, selectionArgs, filteredFieldName, filter, null);
    }


}


