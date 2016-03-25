package org.oucho.musicplayer.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import org.oucho.musicplayer.loaders.SortOrder;


public class PrefUtils {


    private static final String SONG_SORT_ORDER = "song_sort_order";
    private static final String ARTIST_SORT_ORDER = "artist_sort_order";
    private static final String ALBUM_SORT_ORDER = "album_sort_order";


    private static PrefUtils sInstance = null;

    private final SharedPreferences mPreferences;

    private PrefUtils(Context context) {
        mPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public static void init(Context context) {
        sInstance = new PrefUtils(context);
    }

    public static PrefUtils getInstance() {
        return sInstance;
    }

    private void putString(String key, String value) {
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putString(key, value);
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
}
