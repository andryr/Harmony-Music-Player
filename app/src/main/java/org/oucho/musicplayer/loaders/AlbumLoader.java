package org.oucho.musicplayer.loaders;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;
import android.provider.MediaStore;
import android.support.v4.database.DatabaseUtilsCompat;

import org.oucho.musicplayer.R;
import org.oucho.musicplayer.model.Album;

import java.util.ArrayList;
import java.util.List;


public class AlbumLoader extends BaseLoader<List<Album>> {


    private static final String[] sProjection = {BaseColumns._ID,
            MediaStore.Audio.AlbumColumns.ALBUM,
            MediaStore.Audio.AlbumColumns.ARTIST,
            MediaStore.Audio.AlbumColumns.FIRST_YEAR,


            MediaStore.Audio.AlbumColumns.NUMBER_OF_SONGS};

    private String mArtist = null;


    public AlbumLoader(Context context) {
        super(context);
    }

    public AlbumLoader(Context context, String artist) {
        super(context);
        mArtist = artist;
    }


    @Override
    public List<Album> loadInBackground() {


        List<Album> mAlbumList = new ArrayList<>();

        Cursor cursor = getAlbumCursor();

        if (cursor != null && cursor.moveToFirst()) {
            int idCol = cursor.getColumnIndex(BaseColumns._ID);

            int albumNameCol = cursor.getColumnIndex(MediaStore.Audio.AlbumColumns.ALBUM);
            int artistCol = cursor.getColumnIndex(MediaStore.Audio.AlbumColumns.ARTIST);
            int yearCol = cursor.getColumnIndex(MediaStore.Audio.AlbumColumns.FIRST_YEAR);

            int songsNbCol = cursor
                    .getColumnIndex(MediaStore.Audio.AlbumColumns.NUMBER_OF_SONGS);

            do {

                long id = cursor.getLong(idCol);

                String name = cursor.getString(albumNameCol);
                if (name == null || name.equals(MediaStore.UNKNOWN_STRING)) {
                    name = getContext().getString(R.string.unknown_album);
                    id = -1;
                }
                String artist = cursor.getString(artistCol);
                int year = cursor.getInt(yearCol);
                int count = cursor.getInt(songsNbCol);


                mAlbumList.add(new Album(id, name, artist, year, count));

            } while (cursor.moveToNext());


        }


        if (cursor != null) {
            cursor.close();
        }
        return mAlbumList;
    }

    private Cursor getAlbumCursor() {

        Uri musicUri = MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI;

        String selection = getSelectionString();
        String[] selectionArgs = getSelectionArgs();
        if (mArtist != null) {
            selection = DatabaseUtilsCompat.concatenateWhere(selection, MediaStore.Audio.Albums.ARTIST + " = ?");
            selectionArgs = DatabaseUtilsCompat.appendSelectionArgs(selectionArgs, new String[]{mArtist});

        }

        String fieldName = MediaStore.Audio.Albums.ALBUM;
        String filter = getFilter();
        return getCursor(musicUri, sProjection, selection, selectionArgs, fieldName, filter);
    }


}