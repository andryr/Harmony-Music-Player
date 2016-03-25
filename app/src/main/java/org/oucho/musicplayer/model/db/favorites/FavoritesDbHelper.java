package org.oucho.musicplayer.model.db.favorites;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import org.oucho.musicplayer.model.Song;

import java.util.ArrayList;
import java.util.List;


public class FavoritesDbHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "Favorites.db";

    private static final String COMMA_SEP = ",";

    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + FavoritesContract.FavoritesEntry.TABLE_NAME + " (" +
                    FavoritesContract.FavoritesEntry._ID + " INTEGER PRIMARY KEY," +
                    FavoritesContract.FavoritesEntry.COLUMN_NAME_SONG_ID + " INTEGER UNIQUE" + COMMA_SEP +
                    FavoritesContract.FavoritesEntry.COLUMN_NAME_TITLE + " TEXT" + COMMA_SEP +
                    FavoritesContract.FavoritesEntry.COLUMN_NAME_ARTIST + " TEXT" + COMMA_SEP +
                    FavoritesContract.FavoritesEntry.COLUMN_NAME_ALBUM + " TEXT" + COMMA_SEP +
                    FavoritesContract.FavoritesEntry.COLUMN_NAME_TRACK_NUMBER + " INTEGER" + COMMA_SEP +
                    FavoritesContract.FavoritesEntry.COLUMN_NAME_ALBUM_ID + " INTEGER" + COMMA_SEP +
                    FavoritesContract.FavoritesEntry.COLUMN_NAME_GENRE + " TEXT" +
                    " )";

    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + FavoritesContract.FavoritesEntry.TABLE_NAME;

    private static final String[] sProjection = new String[]
            {
                    FavoritesContract.FavoritesEntry._ID, //0
                    FavoritesContract.FavoritesEntry.COLUMN_NAME_SONG_ID,
                    FavoritesContract.FavoritesEntry.COLUMN_NAME_TITLE,
                    FavoritesContract.FavoritesEntry.COLUMN_NAME_ARTIST,
                    FavoritesContract.FavoritesEntry.COLUMN_NAME_ALBUM,
                    FavoritesContract.FavoritesEntry.COLUMN_NAME_TRACK_NUMBER,
                    FavoritesContract.FavoritesEntry.COLUMN_NAME_ALBUM_ID,
                    FavoritesContract.FavoritesEntry.COLUMN_NAME_GENRE,
            };

    public FavoritesDbHelper(Context context) {
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
        values.put(FavoritesContract.FavoritesEntry.COLUMN_NAME_SONG_ID, song.getId());
        values.put(FavoritesContract.FavoritesEntry.COLUMN_NAME_TITLE, song.getTitle());
        values.put(FavoritesContract.FavoritesEntry.COLUMN_NAME_ARTIST, song.getArtist());
        values.put(FavoritesContract.FavoritesEntry.COLUMN_NAME_ALBUM, song.getAlbum());
        values.put(FavoritesContract.FavoritesEntry.COLUMN_NAME_TRACK_NUMBER, song.getTrackNumber());
        values.put(FavoritesContract.FavoritesEntry.COLUMN_NAME_ALBUM_ID, song.getAlbumId());
        values.put(FavoritesContract.FavoritesEntry.COLUMN_NAME_GENRE, song.getGenre());

        db.insertWithOnConflict(FavoritesContract.FavoritesEntry.TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_REPLACE);
        db.close();
    }


    public List<Song> read() {
        return read(-1);
    }

    public List<Song> read(int limit) {
        SQLiteDatabase db = getReadableDatabase();

        List<Song> list = new ArrayList<>();

        Cursor cursor;
        if (limit < 0) {
            cursor = db.query(FavoritesContract.FavoritesEntry.TABLE_NAME, sProjection, null, null, null, null, FavoritesContract.FavoritesEntry.COLUMN_NAME_TITLE);

        } else {
            cursor = db.query(FavoritesContract.FavoritesEntry.TABLE_NAME, sProjection, null, null, null, null, FavoritesContract.FavoritesEntry.COLUMN_NAME_TITLE, String.valueOf(limit));
        }
        if (cursor != null && cursor.moveToFirst()) {

            int idCol = cursor.getColumnIndex(FavoritesContract.FavoritesEntry.COLUMN_NAME_SONG_ID);

            int titleCol = cursor
                    .getColumnIndex(FavoritesContract.FavoritesEntry.COLUMN_NAME_TITLE);
            int artistCol = cursor
                    .getColumnIndex(FavoritesContract.FavoritesEntry.COLUMN_NAME_ARTIST);
            int albumCol = cursor
                    .getColumnIndex(FavoritesContract.FavoritesEntry.COLUMN_NAME_ALBUM);
            int albumIdCol = cursor
                    .getColumnIndex(FavoritesContract.FavoritesEntry.COLUMN_NAME_ALBUM_ID);
            int trackCol = cursor
                    .getColumnIndex(FavoritesContract.FavoritesEntry.COLUMN_NAME_TRACK_NUMBER);

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

    public boolean exists(long songId) {
        SQLiteDatabase db = getReadableDatabase();

        boolean result = false;

        Cursor cursor = db.query(FavoritesContract.FavoritesEntry.TABLE_NAME, sProjection, FavoritesContract.FavoritesEntry.COLUMN_NAME_SONG_ID + "= ?", new String[]{String.valueOf(songId)}, null, null, null, "1");
        if (cursor != null && cursor.moveToFirst()) {
            result = true;
        }

        if (cursor != null) {
            cursor.close();
        }

        return result;
    }

    public void delete(long songId) {
        SQLiteDatabase db = getWritableDatabase();

        db.delete(FavoritesContract.FavoritesEntry.TABLE_NAME, FavoritesContract.FavoritesEntry.COLUMN_NAME_SONG_ID + "= ?", new String[]{String.valueOf(songId)});

        db.close();
    }
}