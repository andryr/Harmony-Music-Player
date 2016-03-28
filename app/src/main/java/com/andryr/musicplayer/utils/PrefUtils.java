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

package com.andryr.musicplayer.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.andryr.musicplayer.R;
import com.andryr.musicplayer.loaders.SortOrder;

import java.util.Set;

/**
 * Created by andry on 20/03/16.
 */
public class PrefUtils {


    private static final String SONG_SORT_ORDER = "song_sort_order";
    public static final String ARTIST_SORT_ORDER = "artist_sort_order";
    public static final String ALBUM_SORT_ORDER = "album_sort_order";


    private static PrefUtils sInstance = null;

    private Context mContext;

    private SharedPreferences mPreferences;

    private PrefUtils(Context context) {
        mContext = context;
        mPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public static void init(Context context) {
        sInstance = new PrefUtils(context);
    }

    public static PrefUtils getInstance() {
        return sInstance;
    }

    public void putString(String key, String value) {
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putString(key, value);
        editor.apply();
    }

    public void putInt(String key, int value) {
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putInt(key, value);
        editor.apply();
    }

    public void putLong(String key, long value) {
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putLong(key, value);
        editor.apply();
    }

    public void putBoolean(String key, boolean value) {
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putBoolean(key, value);
        editor.apply();
    }

    public void putFloat(String key, float value) {
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putFloat(key, value);
        editor.apply();
    }


    public void setSongSortOrder(String value) {
        putString(SONG_SORT_ORDER, value);
    }

    public void setArtistSortOrder(String value) {
        putString(ARTIST_SORT_ORDER, value);
    }

    public void setAlbumSortOrder(String value) {
        putString(ALBUM_SORT_ORDER, value);
    }

    public String getSongSortOrder() {
        return mPreferences.getString(SONG_SORT_ORDER, SortOrder.SongSortOrder.SONG_A_Z);
    }

    public String getArtistSortOrder() {
        return mPreferences.getString(ARTIST_SORT_ORDER, SortOrder.ArtistSortOrder.ARTIST_A_Z);
    }

    public String getAlbumSortOrder() {
        return mPreferences.getString(ALBUM_SORT_ORDER, SortOrder.AlbumSortOrder.ALBUM_A_Z);
    }

    public boolean useFreeArtworks() {
        String prefKey = mContext.getString(R.string.pref_use_free_artworks_key);
        return mPreferences.getBoolean(prefKey, false);
    }
}
