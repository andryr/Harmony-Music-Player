package org.oucho.musicplayer.loaders;

import android.Manifest;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import org.oucho.musicplayer.model.Song;
import org.oucho.musicplayer.utils.Permissions;

import java.util.ArrayList;
import java.util.List;



public class SongLoader extends BaseLoader<List<Song>> {

    private static final String[] sProjection = {MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE, MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM, MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.ARTIST_ID, MediaStore.Audio.Media.TRACK};

    private String mOrder;

    public SongLoader(Context context) {
        super(context);

    }

    @Override
    public List<Song> loadInBackground() {
        List<Song> mSongList = new ArrayList<>();

        Cursor cursor = getSongCursor();
        if (cursor != null && cursor.moveToFirst()) {
            int idCol = cursor
                    .getColumnIndex(MediaStore.Audio.Playlists.Members.AUDIO_ID);
            if (idCol == -1) {
                idCol = cursor.getColumnIndex(MediaStore.Audio.Media._ID);
            }
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

            do {
                long id = cursor.getLong(idCol);
                String title = cursor.getString(titleCol);

                String artist = cursor.getString(artistCol);

                String album = cursor.getString(albumCol);

                long albumId = cursor.getLong(albumIdCol);

                int track = cursor.getInt(trackCol);


                mSongList.add(new Song(id, title, artist, album, albumId, track));
            } while (cursor.moveToNext());

        }

        if (cursor != null) {
            cursor.close();
        }
        Log.e("test", "  d " + mSongList.size());

        return mSongList;
    }

    Uri getContentUri() {
        return MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
    }

    private Cursor getSongCursor() {
        if (!Permissions.checkPermission(getContext(), Manifest.permission.READ_EXTERNAL_STORAGE)) {
            return null;
        }
        Uri musicUri = getContentUri();

        String selection = getSelectionString();
        String[] selectionArgs = getSelectionArgs();

        String fieldName = MediaStore.Audio.Media.TITLE;
        String filter = getFilter();
        return getCursor(musicUri, sProjection, selection, selectionArgs, fieldName, filter, mOrder);
    }

    public String getOrder() {
        return mOrder;
    }

    public void setOrder(String order) {
        this.mOrder = order;
    }
}
