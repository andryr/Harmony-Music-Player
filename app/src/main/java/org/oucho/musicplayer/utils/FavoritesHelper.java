package org.oucho.musicplayer.utils;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import org.oucho.musicplayer.model.db.favorites.FavoritesDbHelper;
import org.oucho.musicplayer.model.Song;


public class FavoritesHelper {
    public static void addFavorite(Context context, long songId) {

        Uri musicUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;

        String[] projection = {MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.TITLE, MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.ALBUM, MediaStore.Audio.Media.ALBUM_ID,
                MediaStore.Audio.Media.ARTIST_ID, MediaStore.Audio.Media.TRACK};

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

            long id = cursor.getLong(idCol);
            String title = cursor.getString(titleCol);

            String artist = cursor.getString(artistCol);

            String album = cursor.getString(albumCol);

            long albumId = cursor.getLong(albumIdCol);

            int track = cursor.getInt(trackCol);


            Song song = new Song(id, title, artist, album, albumId, track);
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
