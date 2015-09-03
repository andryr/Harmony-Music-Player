package com.andryr.musicplayer.loaders;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;
import android.provider.MediaStore;

import com.andryr.musicplayer.Album;
import com.andryr.musicplayer.R;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * Created by andry on 23/08/15.
 */
public class AlbumLoader extends BaseLoader<List<Album>> {


    private static final String[] sProjection = {BaseColumns._ID,
            MediaStore.Audio.AlbumColumns.ALBUM,
            MediaStore.Audio.AlbumColumns.ARTIST,
            MediaStore.Audio.AlbumColumns.FIRST_YEAR,


            MediaStore.Audio.AlbumColumns.NUMBER_OF_SONGS};

    private String mArtist = null;

    private List<Album> mAlbumList;


    public AlbumLoader(Context context) {
        super(context);
    }
    public AlbumLoader(Context context, String artist) {
        super(context);
        mArtist = artist;
    }


    @Override
    public List<Album> loadInBackground() {


        mAlbumList = new ArrayList<>();

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

            Collections.sort(mAlbumList, new Comparator<Album>() {

                @Override
                public int compare(Album lhs, Album rhs) {
                    Collator c = Collator.getInstance(Locale.getDefault());
                    c.setStrength(Collator.PRIMARY);
                    return c.compare(lhs.getAlbumName(), rhs.getAlbumName());
                }
            });
        }


        if(cursor != null)
        {
            cursor.close();
        }
        return mAlbumList;
    }

    private Cursor getAlbumCursor()
    {
        Uri musicUri = MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI;

        Cursor cursor;
        String filter = getFilter();
        if(filter == null) {
            if (mArtist != null) {

                cursor = getContext().getContentResolver().query(musicUri, sProjection,
                        MediaStore.Audio.AlbumColumns.ARTIST + " = ?", new String[]{mArtist},
                        null);
            } else {
                cursor = getContext().getContentResolver().query(musicUri, sProjection,
                        null, null, null);
            }
        }
        else
        {
            if (mArtist != null) {

                cursor = getContext().getContentResolver().query(musicUri, sProjection,
                        MediaStore.Audio.AlbumColumns.ARTIST + " = ? AND "+MediaStore.Audio.Albums.ALBUM+" LIKE ?", new String[]{mArtist,"%"+filter+"%"},
                        null);
            } else {
                cursor = getContext().getContentResolver().query(musicUri, sProjection,MediaStore.Audio.Albums.ALBUM+" LIKE ?"
                        , new String[]{"%"+filter+"%"}, null);
            }
        }

        return cursor;
    }
}
