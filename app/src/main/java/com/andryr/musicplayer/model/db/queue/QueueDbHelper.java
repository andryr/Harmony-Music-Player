/*
 * Copyright 2016 andryr
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.andryr.musicplayer.model.db.queue;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.andryr.musicplayer.model.Song;
import com.andryr.musicplayer.model.db.favorites.FavoritesContract;

import java.util.ArrayList;
import java.util.List;


public class QueueDbHelper extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 2;
    public static final String DATABASE_NAME = "Queue.db";

    private static final String COMMA_SEP = ",";

    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + QueueContract.QueueEntry.TABLE_NAME + " (" +
                    QueueContract.QueueEntry._ID + " INTEGER PRIMARY KEY," +
                    QueueContract.QueueEntry.COLUMN_NAME_SONG_ID + " INTEGER UNIQUE" + COMMA_SEP +
                    QueueContract.QueueEntry.COLUMN_NAME_TITLE + " TEXT" + COMMA_SEP +
                    QueueContract.QueueEntry.COLUMN_NAME_ARTIST + " TEXT" + COMMA_SEP +
                    QueueContract.QueueEntry.COLUMN_NAME_ALBUM + " TEXT" + COMMA_SEP +
                    QueueContract.QueueEntry.COLUMN_NAME_TRACK_NUMBER + " INTEGER" + COMMA_SEP +
                    QueueContract.QueueEntry.COLUMN_NAME_ALBUM_ID + " INTEGER" + COMMA_SEP +
                    QueueContract.QueueEntry.COLUMN_NAME_GENRE + " TEXT" + COMMA_SEP +
                    QueueContract.QueueEntry.COLUMN_NAME_DURATION + " INTEGER" +
                    " )";

    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + QueueContract.QueueEntry.TABLE_NAME;

    private static final String[] sProjection = new String[]
            {
                    QueueContract.QueueEntry._ID, //0
                    QueueContract.QueueEntry.COLUMN_NAME_SONG_ID,
                    QueueContract.QueueEntry.COLUMN_NAME_TITLE,
                    QueueContract.QueueEntry.COLUMN_NAME_ARTIST,
                    QueueContract.QueueEntry.COLUMN_NAME_ALBUM,
                    QueueContract.QueueEntry.COLUMN_NAME_TRACK_NUMBER,
                    QueueContract.QueueEntry.COLUMN_NAME_ALBUM_ID,
                    QueueContract.QueueEntry.COLUMN_NAME_GENRE,
                    QueueContract.QueueEntry.COLUMN_NAME_DURATION
            };

    public QueueDbHelper(Context context) {
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

    private void addInternal(SQLiteDatabase db, Song song) {

        ContentValues values = new ContentValues();
        values.put(QueueContract.QueueEntry.COLUMN_NAME_SONG_ID, song.getId());
        values.put(QueueContract.QueueEntry.COLUMN_NAME_TITLE, song.getTitle());
        values.put(QueueContract.QueueEntry.COLUMN_NAME_ARTIST, song.getArtist());
        values.put(QueueContract.QueueEntry.COLUMN_NAME_ALBUM, song.getAlbum());
        values.put(QueueContract.QueueEntry.COLUMN_NAME_TRACK_NUMBER, song.getTrackNumber());
        values.put(QueueContract.QueueEntry.COLUMN_NAME_ALBUM_ID, song.getAlbumId());
        values.put(QueueContract.QueueEntry.COLUMN_NAME_GENRE, song.getGenre());

        db.insert(QueueContract.QueueEntry.TABLE_NAME, null, values);

    }

    public void add(Song song) {
        SQLiteDatabase db = getWritableDatabase();

        addInternal(db, song);

        db.close();
    }

    public void removeAll() {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            db.delete(QueueContract.QueueEntry.TABLE_NAME, null, null);
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
        db.close();
    }

    public void add(List<Song> songList) {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();

        try {
            for (Song song : songList) {
                addInternal(db, song);
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }

        db.close();
    }


    public List<Song> readAll() {
        return read(-1);
    }

    public List<Song> read(int limit) {
        SQLiteDatabase db = getReadableDatabase();

        List<Song> list = new ArrayList<>();

        Cursor cursor;
        if (limit < 0) {
            cursor = db.query(QueueContract.QueueEntry.TABLE_NAME, sProjection, null, null, null, null, null);

        } else {
            cursor = db.query(QueueContract.QueueEntry.TABLE_NAME, sProjection, null, null, null, null, null, String.valueOf(limit));
        }
        if (cursor != null && cursor.moveToFirst()) {

            int idCol = cursor.getColumnIndex(QueueContract.QueueEntry.COLUMN_NAME_SONG_ID);

            int titleCol = cursor
                    .getColumnIndex(QueueContract.QueueEntry.COLUMN_NAME_TITLE);
            int artistCol = cursor
                    .getColumnIndex(QueueContract.QueueEntry.COLUMN_NAME_ARTIST);
            int albumCol = cursor
                    .getColumnIndex(QueueContract.QueueEntry.COLUMN_NAME_ALBUM);
            int albumIdCol = cursor
                    .getColumnIndex(QueueContract.QueueEntry.COLUMN_NAME_ALBUM_ID);
            int trackCol = cursor
                    .getColumnIndex(QueueContract.QueueEntry.COLUMN_NAME_TRACK_NUMBER);

            int durationCol = cursor.getColumnIndex(QueueContract.QueueEntry.COLUMN_NAME_DURATION);

            do {
                long id = cursor.getLong(idCol);
                String title = cursor.getString(titleCol);

                String artist = cursor.getString(artistCol);

                String album = cursor.getString(albumCol);

                long albumId = cursor.getLong(albumIdCol);

                int track = cursor.getInt(trackCol);

                long duration = cursor.getLong(durationCol);


                list.add(new Song(id, title, artist, album, albumId, track, duration));
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

        Cursor cursor = db.query(QueueContract.QueueEntry.TABLE_NAME, sProjection, QueueContract.QueueEntry.COLUMN_NAME_SONG_ID + "= ?", new String[]{String.valueOf(songId)}, null, null, null, "1");
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

        db.delete(QueueContract.QueueEntry.TABLE_NAME, QueueContract.QueueEntry.COLUMN_NAME_SONG_ID + "= ?", new String[]{String.valueOf(songId)});

        db.close();
    }
}
