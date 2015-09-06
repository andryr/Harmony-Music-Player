package com.andryr.musicplayer.loaders;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import com.andryr.musicplayer.Song;

import java.lang.reflect.Array;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * Created by andry on 21/08/15.
 */
public class SongLoader extends BaseLoader<List<Song>>
{
    public static final int ALL_SONGS = 1;
    public static final int ALBUM_SONGS = 2;
    public static final int ARTIST_SONGS = 3;
    public static final int ARTIST_ALBUM_SONGS = 4;
    public static final int GENRE_SONGS = 5;

    private int mSongListType = ALL_SONGS;
    private long mArtistId;
    private long mAlbumId;
    private long mGenreId;

    private List<Song> mSongList = null;

    private static final String[] sProjection = {MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE, MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM, MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.ARTIST_ID, MediaStore.Audio.Media.TRACK};


    private String mOrder;

    public SongLoader(Context context) {
        super(context);
        this.mSongListType = mSongListType;

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
            int trackCol  = cursor
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

        if(cursor != null)
        {
            cursor.close();
        }
        Log.e("test", "  d " + mSongList.size());

        return mSongList;
    }



    private Cursor getSongCursor()
    {
        Uri musicUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;

        String selection = null;

        switch (mSongListType) {

            case ARTIST_SONGS:

                 selection = MediaStore.Audio.Media.ARTIST_ID + " = " + mArtistId;

                break;
            case ALBUM_SONGS:
                selection = MediaStore.Audio.Media.ALBUM_ID + " = " + mAlbumId;
                break;
            case ARTIST_ALBUM_SONGS:
                // TODO
                break;
            case GENRE_SONGS:
                musicUri = MediaStore.Audio.Genres.Members.getContentUri(
                        "external", mGenreId);

                break;





        }
        Cursor cursor;
        String filter = getFilter();
        if(filter != null && !filter.equals("")) {
            if (selection == null) {

                selection = "";
            }
            else
            {
                selection += " AND ";
            }

            selection += MediaStore.Audio.Media.TITLE +" LIKE ?";

            cursor = getContext().getContentResolver().query( musicUri, sProjection,
                    selection, new String[]{"%"+filter+"%"}, mOrder);

        }
        else {
            cursor = getContext().getContentResolver().query(musicUri, sProjection,
                    selection, null, mOrder);
        }
        return cursor;
    }

    public long getArtistId() {
        return mArtistId;
    }

    public void setArtistId(long mArtistId) {
        this.mArtistId = mArtistId;
    }

    public long getAlbumId() {
        return mAlbumId;
    }

    public void setAlbumId(long mAlbumId) {
        this.mAlbumId = mAlbumId;
    }

    public long getGenreId() {
        return mGenreId;
    }

    public void setGenreId(long mGenreId) {
        this.mGenreId = mGenreId;
    }

    public int getSongListType() {
        return mSongListType;
    }

    public void setSongListType(int mSongListType) {
        this.mSongListType = mSongListType;
    }

    public String getOrder() {
        return mOrder;
    }

    public void setOrder(String order) {
        this.mOrder = order;
    }
}
