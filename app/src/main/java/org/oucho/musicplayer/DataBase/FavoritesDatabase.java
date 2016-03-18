package org.oucho.musicplayer.DataBase;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import org.oucho.musicplayer.model.Song;

import java.util.ArrayList;
import java.util.List;


public class FavoritesDatabase extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "Favorites.db";
    private static final int DATABASE_VERSION = 3;


    private static final String TABLE_NAME = "favorites";

    private static final String _ID = "id";

    private static final String COLUMN_NAME_SONG_ID = "song_id";
    private static final String COLUMN_NAME_TITLE = "title";
    private static final String COLUMN_NAME_ARTIST = "artist";
    private static final String COLUMN_NAME_ALBUM = "album";
    private static final String COLUMN_NAME_TRACK_NUMBER = "number";
    private static final String COLUMN_NAME_ALBUM_ID = "album_id";
    private static final String COLUMN_NAME_GENRE = "genre";

    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + TABLE_NAME + " (" +
                    _ID + " INTEGER PRIMARY KEY," +
                    COLUMN_NAME_SONG_ID + " INTEGER UNIQUE," +
                    COLUMN_NAME_TITLE + " TEXT," +
                    COLUMN_NAME_ARTIST + " TEXT," +
                    COLUMN_NAME_ALBUM + " TEXT," +
                    COLUMN_NAME_TRACK_NUMBER + " INTEGER," +
                    COLUMN_NAME_ALBUM_ID + " INTEGER," +
                    COLUMN_NAME_GENRE + " TEXT" +
                    " )";

    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + TABLE_NAME;

    private static final String[] sProjection = new String[]
            {
                    _ID, //0
                    COLUMN_NAME_SONG_ID,
                    COLUMN_NAME_TITLE,
                    COLUMN_NAME_ARTIST,
                    COLUMN_NAME_ALBUM,
                    COLUMN_NAME_TRACK_NUMBER,
                    COLUMN_NAME_ALBUM_ID,
                    COLUMN_NAME_GENRE,
            };

    public FavoritesDatabase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }

    public void insertOrUpdate(Song song) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME_SONG_ID, song.getId());
        values.put(COLUMN_NAME_TITLE, song.getTitle());
        values.put(COLUMN_NAME_ARTIST, song.getArtist());
        values.put(COLUMN_NAME_ALBUM, song.getAlbum());
        values.put(COLUMN_NAME_TRACK_NUMBER, song.getTrackNumber());
        values.put(COLUMN_NAME_ALBUM_ID, song.getAlbumId());
        values.put(COLUMN_NAME_GENRE, song.getGenre());

        db.insertWithOnConflict(TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_REPLACE);
        db.close();
    }



    public List<Song> read(int limit) {
        SQLiteDatabase db = getReadableDatabase();

        List<Song> list = new ArrayList<>();

        Cursor cursor;
        if(limit < 0){
            cursor = db.query(TABLE_NAME, sProjection, null, null, null, null, COLUMN_NAME_TITLE);

        }
        else
        {
            cursor = db.query(TABLE_NAME, sProjection, null, null, null, null, COLUMN_NAME_TITLE, String.valueOf(limit));
        }
        if (cursor != null && cursor.moveToFirst()) {

            int idCol = cursor.getColumnIndex(COLUMN_NAME_SONG_ID);

            int titleCol = cursor
                    .getColumnIndex(COLUMN_NAME_TITLE);
            int artistCol = cursor
                    .getColumnIndex(COLUMN_NAME_ARTIST);
            int albumCol = cursor
                    .getColumnIndex(COLUMN_NAME_ALBUM);
            int albumIdCol = cursor
                    .getColumnIndex(COLUMN_NAME_ALBUM_ID);
            int trackCol = cursor
                    .getColumnIndex(COLUMN_NAME_TRACK_NUMBER);

            do {
                long id = cursor.getLong(idCol);
                String title = cursor.getString(titleCol);

                String artist = cursor.getString(artistCol);

                String album = cursor.getString(albumCol);

                long albumId = cursor.getLong(albumIdCol);

                int track = cursor.getInt(trackCol);


                list.add(new Song(id, title, artist, album, albumId, track));
            } while (cursor.moveToNext());
        }

        if (cursor != null) {
            cursor.close();
        }

        db.close();

        return list;
    }

    public boolean exists(long songId)
    {
        SQLiteDatabase db = getReadableDatabase();

        boolean result = false;

        Cursor cursor = db.query(TABLE_NAME, sProjection, COLUMN_NAME_SONG_ID+"= ?", new String[]{String.valueOf(songId)}, null, null, null, "1");
        if(cursor != null && cursor.moveToFirst())
        {
            result = true;
        }

        if(cursor != null)
        {
            cursor.close();
        }

        return result;
    }

    public void delete(long songId)
    {
        SQLiteDatabase db = getWritableDatabase();

        db.delete(TABLE_NAME, COLUMN_NAME_SONG_ID+"= ?",new String[]{String.valueOf(songId)});

        db.close();
    }


}
