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
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import com.andryr.musicplayer.model.Song;
import com.andryr.musicplayer.model.db.favorites.FavoritesDbHelper;

/**
 * Created by Andry on 09/11/15.
 */
public class FavoritesHelper {
    public static void addFavorite(Context context, long songId) {

        Uri musicUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;

        String[] projection = {MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.TITLE, MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.ALBUM, MediaStore.Audio.Media.ALBUM_ID,
                MediaStore.Audio.Media.ARTIST_ID, MediaStore.Audio.Media.TRACK,
                MediaStore.Audio.Media.DURATION
        };

        Cursor cursor = context.getContentResolver().query(musicUri, projection,
                MediaStore.Audio.Media._ID + "= ?", new String[]{String.valueOf(songId)}, null);
        if (cursor != null && cursor.moveToFirst()) {
            int idCol = cursor.getColumnIndex(MediaStore.Audio.Media._ID);

            int titleCol = cursor
                    .getColumnIndex(MediaStore.Audio.Media.TITLE);
            int artistCol = cursor
                    .getColumnIndex(MediaStore.Audio.Media.ARTIST);
            int albumCol = cursor
                    .getColumnIndex(MediaStore.Audio.Media.ALBUM);
            int albumIdCol = cursor
                    .getColumnIndex(MediaStore.Audio.Media.ALBUM_ID);
            int trackCol = cursor
                    .getColumnIndex(MediaStore.Audio.Media.TRACK);
            int durationCol = cursor
                    .getColumnIndex(MediaStore.Audio.Media.DURATION);

            long id = cursor.getLong(idCol);
            String title = cursor.getString(titleCol);

            String artist = cursor.getString(artistCol);

            String album = cursor.getString(albumCol);

            long albumId = cursor.getLong(albumIdCol);

            int track = cursor.getInt(trackCol);

            long duration = cursor.getLong(durationCol);

            Song song = new Song(id, title, artist, album, albumId, track, duration);
            FavoritesDbHelper dbHelper = new FavoritesDbHelper(context);
            dbHelper.insertOrUpdate(song);
            dbHelper.close();
        }
        if (cursor != null) {
            cursor.close();
        }
    }

    public static boolean isFavorite(Context context, long songId) {
        FavoritesDbHelper dbHelper = new FavoritesDbHelper(context);
        boolean result = dbHelper.exists(songId);
        dbHelper.close();
        return result;
    }

    public static void removeFromFavorites(Context context, long songId) {
        FavoritesDbHelper dbHelper = new FavoritesDbHelper(context);
        dbHelper.delete(songId);
        dbHelper.close();
    }
}
