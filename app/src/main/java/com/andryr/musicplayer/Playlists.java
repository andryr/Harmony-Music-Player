package com.andryr.musicplayer;

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

    public static void addToPlaylist(ContentResolver resolver, long playlistId,
                                     long audioId) {

        String[] cols = new String[]{"count(*)"};
        Uri uri = MediaStore.Audio.Playlists.Members.getContentUri("external",
                playlistId);
        Cursor cur = resolver.query(uri, cols, null, null, null);
        cur.moveToFirst();
        final int base = cur.getInt(0);
        cur.close();
        ContentValues values = new ContentValues();
        values.put(MediaStore.Audio.Playlists.Members.PLAY_ORDER,
                Integer.valueOf(base + 1));
        values.put(MediaStore.Audio.Playlists.Members.AUDIO_ID, audioId);
        resolver.insert(uri, values);
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
