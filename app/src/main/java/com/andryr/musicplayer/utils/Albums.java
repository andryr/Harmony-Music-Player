package com.andryr.musicplayer.utils;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;
import android.provider.MediaStore;

import com.andryr.musicplayer.model.Album;

/**
 * Created by Andry on 24/01/16.
 */
public class Albums {


    private static final String[] sProjection = {BaseColumns._ID,
            MediaStore.Audio.AlbumColumns.ALBUM,
            MediaStore.Audio.AlbumColumns.ARTIST,
            MediaStore.Audio.AlbumColumns.FIRST_YEAR,


            MediaStore.Audio.AlbumColumns.NUMBER_OF_SONGS};

    public static Album getAlbum(Context context, long albumId) {
        if (!Permissions.checkPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE)) {
            return null;
        }
        Uri musicUri = MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI;


        ContentResolver resolver = context.getContentResolver();

        Cursor cursor = resolver.query(musicUri, sProjection,
                MediaStore.Audio.Albums._ID + " = ?", new String[]{String.valueOf(albumId)},
                null);

        if (cursor != null) {
            Album album = null;
            if (cursor.moveToFirst()) {
                int idCol = cursor.getColumnIndex(BaseColumns._ID);

                int albumNameCol = cursor.getColumnIndex(MediaStore.Audio.AlbumColumns.ALBUM);
                int artistCol = cursor.getColumnIndex(MediaStore.Audio.AlbumColumns.ARTIST);
                int yearCol = cursor.getColumnIndex(MediaStore.Audio.AlbumColumns.FIRST_YEAR);

                int songsNbCol = cursor
                        .getColumnIndex(MediaStore.Audio.AlbumColumns.NUMBER_OF_SONGS);


                long id = cursor.getLong(idCol);

                String name = cursor.getString(albumNameCol);

                String artist = cursor.getString(artistCol);
                int year = cursor.getInt(yearCol);
                int count = cursor.getInt(songsNbCol);

                album = new Album(id, name, artist, year, count);
            }
            cursor.close();
            return album;
        }
        return null;
    }
}
