package com.andryr.musicplayer.loaders;

import android.Manifest;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.database.DatabaseUtilsCompat;
import android.util.Log;

import com.andryr.musicplayer.model.Song;
import com.andryr.musicplayer.utils.Permissions;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by andry on 21/08/15.
 */
public class SongLoader extends BaseLoader<List<Song>> {

    private static final String[] sProjection = {MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE, MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM, MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.ARTIST_ID, MediaStore.Audio.Media.TRACK};

    private List<Song> mSongList = null;
    private String mOrder;

    public SongLoader(Context context) {
        super(context);

    }

    @Override
    public List<Song> loadInBackground() {
        mSongList = new ArrayList<>();

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

         /*   Collections.sort(mSongList, new Comparator<Song>() {

                @Override
                public int compare(Song lhs, Song rhs) {
                    Collator c = Collator.getInstance(Locale.getDefault());
                    c.setStrength(Collator.PRIMARY);
                    return c.compare(lhs.getTitle(), rhs.getTitle());
                }
            });*/

        }

        if (cursor != null) {
            cursor.close();
        }
        Log.e("test", "  d " + mSongList.size());

        return mSongList;
    }


    private Cursor getSongCursor() {
        if (!Permissions.checkPermission(getContext(), Manifest.permission.READ_EXTERNAL_STORAGE)) {
            return null;
        }
        Uri musicUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;

        String selection = getSelectionString();
        String[] selectionArgs = getSelectionArgs();
        String filter = getFilter();

        if (filter != null) {
            selection = DatabaseUtilsCompat.concatenateWhere(selection, MediaStore.Audio.Media.TITLE + " LIKE ?");
            selectionArgs = DatabaseUtilsCompat.appendSelectionArgs(selectionArgs, new String[]{"%" + filter + "%"});
        }

        Cursor cursor = getContext().getContentResolver().query(musicUri, sProjection,
                selection, selectionArgs, mOrder);

        return cursor;
    }


    public void setOrder(String order) {
        this.mOrder = order;
    }
}
