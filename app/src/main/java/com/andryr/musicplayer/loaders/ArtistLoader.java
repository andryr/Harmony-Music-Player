package com.andryr.musicplayer.loaders;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;
import android.provider.MediaStore;

import com.andryr.musicplayer.model.Artist;
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
public class ArtistLoader extends BaseLoader<List<Artist>> {


    private static final String[] sProjection = {BaseColumns._ID,
            MediaStore.Audio.ArtistColumns.ARTIST, MediaStore.Audio.ArtistColumns.NUMBER_OF_ALBUMS,
            MediaStore.Audio.ArtistColumns.NUMBER_OF_TRACKS};


    private List<Artist> mArtistList;


    public ArtistLoader(Context context) {
        super(context);
    }


    @Override
    public List<Artist> loadInBackground() {


        mArtistList = new ArrayList<>();

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

            Collections.sort(mArtistList, new Comparator<Artist>() {

                @Override
                public int compare(Artist lhs, Artist rhs) {
                    Collator c = Collator.getInstance(Locale.getDefault());
                    c.setStrength(Collator.PRIMARY);
                    return c.compare(lhs.getName(), rhs.getName());
                }
            });
        }


        if (cursor != null) {
            cursor.close();
        }
        return mArtistList;
    }

    private Cursor getArtistCursor() {
        Uri musicUri = MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI;

        String filter = getFilter();
        Cursor cursor;
        if(filter == null) {
            cursor = getContext().getContentResolver().query(musicUri, sProjection,
                    null, null, null);
        }
        else
        {
            cursor = getContext().getContentResolver().query(musicUri, sProjection,
                    MediaStore.Audio.Artists.ARTIST+" LIKE ?", new String[]{"%"+filter+"%"}, null);
        }


        return cursor;
    }
}
