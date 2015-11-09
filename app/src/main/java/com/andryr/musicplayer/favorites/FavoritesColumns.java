package com.andryr.musicplayer.favorites;

import android.provider.BaseColumns;

/**
 * Created by Andry on 08/11/15.
 */
public interface FavoritesColumns extends BaseColumns {
    String COLUMN_NAME_SONG_ID = "song_id";
    String COLUMN_NAME_TITLE = "title";
    String COLUMN_NAME_ARTIST = "artist";
    String COLUMN_NAME_ALBUM = "album";
    String COLUMN_NAME_TRACK_NUMBER = "number";
    String COLUMN_NAME_ALBUM_ID = "album_id";
    String COLUMN_NAME_GENRE = "genre";
}
