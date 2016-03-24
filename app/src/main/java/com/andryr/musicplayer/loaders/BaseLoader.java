/*
 * Copyright 2016 andryr
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.andryr.musicplayer.loaders;

import android.Manifest;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.database.DatabaseUtilsCompat;

import com.andryr.musicplayer.utils.Utils;

/**
 * Created by andry on 22/08/15.
 */
abstract public class BaseLoader<D> extends AsyncTaskLoader<D> {

    private D mData;

    private String mFilter;

    private String mSelectionString;
    private String[] mSelectionArgs;
    private String mSortOrder = null;

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

    @Nullable
    protected Cursor getCursor(Uri musicUri, String[] projection, String selection, String[] selectionArgs, String filteredFieldName, String filter, String orderBy) {
        if (!Utils.checkPermission(getContext(), Manifest.permission.READ_EXTERNAL_STORAGE)) {
            return null;
        }

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
        return getCursor(musicUri, projection, selection, selectionArgs, filteredFieldName, filter, mSortOrder);
    }


    public void setSortOrder(String orderBy) {
        mSortOrder = orderBy;
    }


}
