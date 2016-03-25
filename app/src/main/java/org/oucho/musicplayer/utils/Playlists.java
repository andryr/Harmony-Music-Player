package org.oucho.musicplayer.utils;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

public class Playlists {

    public static Uri createPlaylist(ContentResolver resolver, String playlistName) {
        Uri uri = MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI;
        ContentValues values = new ContentValues();
        values.put(MediaStore.Audio.Playlists.NAME, playlistName);
        return resolver.insert(uri, values);
    }



    private static int getSongCount(ContentResolver resolver, Uri uri) {
        String[] cols = new String[]{"count(*)"};

        Cursor cur = resolver.query(uri, cols, null, null, null);
        assert cur != null;
        cur.moveToFirst();
        final int count = cur.getInt(0);
        cur.close();
        return count;
    }

    private static void insert(ContentResolver resolver, Uri uri, long songId, int index) {

        ContentValues values = new ContentValues();
        values.put(MediaStore.Audio.Playlists.Members.PLAY_ORDER, index);
        values.put(MediaStore.Audio.Playlists.Members.AUDIO_ID, songId);
        resolver.insert(uri, values);

    }

    public static void addSongToPlaylist(ContentResolver resolver, long playlistId, long songId) {
        Uri uri = MediaStore.Audio.Playlists.Members.getContentUri("external", playlistId);
        final int base = getSongCount(resolver, uri);
        insert(resolver, uri, songId, base + 1);
    }

    public static void addAlbumToPlaylist(ContentResolver resolver, long playlistId, long albumId) {
        String cols[] = {MediaStore.Audio.Media._ID};

        Uri uri = MediaStore.Audio.Playlists.Members.getContentUri("external", playlistId);
        int index = getSongCount(resolver, uri) + 1;

        Uri musicUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;

        Cursor cursor = resolver.query(musicUri, cols,
                MediaStore.Audio.Media.ALBUM_ID + " = " + albumId, null, null);

        if (cursor != null && cursor.moveToFirst()) {


            do {
                int songId = cursor.getInt(0);
                insert(resolver, uri, songId, index);
                index++;
            } while (cursor.moveToNext());


        }

        if (cursor != null) {
            cursor.close();
        }
    }

    public static void removeFromPlaylist(ContentResolver resolver,
                                          long playlistId, long audioId) {
        Uri uri = MediaStore.Audio.Playlists.Members.getContentUri("external",
                playlistId);

        resolver.delete(uri, MediaStore.Audio.Playlists.Members.AUDIO_ID
                + " = " + audioId, null);
    }

    public static boolean moveItem(ContentResolver res, long playlistId,
                                   int from, int to) {
        Uri uri = MediaStore.Audio.Playlists.Members
                .getContentUri("external", playlistId).buildUpon()
                .appendEncodedPath(String.valueOf(from))
                .appendQueryParameter("move", "true").build();
        ContentValues values = new ContentValues();
        values.put(MediaStore.Audio.Playlists.Members.PLAY_ORDER, to);
        return res.update(uri, values, null, null) != 0;
    }


}
