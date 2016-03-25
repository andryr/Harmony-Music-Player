package org.oucho.musicplayer.loaders;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;
import android.provider.MediaStore;

import org.oucho.musicplayer.R;
import org.oucho.musicplayer.model.Artist;

import java.util.ArrayList;
import java.util.List;


public class ArtistLoader extends BaseLoader<List<Artist>> {


    private static final String[] sProjection = {BaseColumns._ID,
            MediaStore.Audio.ArtistColumns.ARTIST, MediaStore.Audio.ArtistColumns.NUMBER_OF_ALBUMS,
            MediaStore.Audio.ArtistColumns.NUMBER_OF_TRACKS};


    public ArtistLoader(Context context) {
        super(context);
    }


    @Override
    public List<Artist> loadInBackground() {


        List<Artist> mArtistList = new ArrayList<>();

        Cursor cursor = getArtistCursor();

        if (cursor != null && cursor.moveToFirst()) {
            int idCol = cursor.getColumnIndex(BaseColumns._ID);

            int nameCol = cursor.getColumnIndex(MediaStore.Audio.ArtistColumns.ARTIST);

            int albumsNbCol = cursor
                    .getColumnIndex(MediaStore.Audio.ArtistColumns.NUMBER_OF_ALBUMS);

            int tracksNbCol = cursor
                    .getColumnIndex(MediaStore.Audio.ArtistColumns.NUMBER_OF_TRACKS);

            do {

                long id = cursor.getLong(idCol);

                String artistName = cursor.getString(nameCol);
                if (artistName == null || artistName.equals(MediaStore.UNKNOWN_STRING)) {
                    artistName = getContext().getString(R.string.unknown_artist);
                    id = -1;
                }


                int albumCount = cursor.getInt(albumsNbCol);

                int trackCount = cursor.getInt(tracksNbCol);

                mArtistList.add(new Artist(id, artistName, albumCount,
                        trackCount));

            } while (cursor.moveToNext());


        }


        if (cursor != null) {
            cursor.close();
        }
        return mArtistList;
    }

    private Cursor getArtistCursor() {

        Uri musicUri = MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI;

        String selection = getSelectionString();
        String[] selectionArgs = getSelectionArgs();

        String fieldName = MediaStore.Audio.Artists.ARTIST;
        String filter = getFilter();
        return getCursor(musicUri, sProjection, selection, selectionArgs, fieldName, filter);
    }
}