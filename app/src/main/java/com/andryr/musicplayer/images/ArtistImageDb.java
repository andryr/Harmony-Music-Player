package com.andryr.musicplayer.images;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;

/**
 * Created by Andry on 23/01/16.
 */
public class ArtistImageDb {
    private static final String[] sProjection = new String[]
            {
                    ArtistImageContract.Entry._ID, //0
                    ArtistImageContract.Entry.COLUMN_NAME_ARTIST_NAME, //1
                    ArtistImageContract.Entry.COLUMN_NAME_ARTIST_IMAGE, //2
            };

    private final ArtistImageDbHelper mDbHelper;

    public ArtistImageDb(Context context) {
        mDbHelper = new ArtistImageDbHelper(context);
    }

    public void insertOrUpdate(String mbid, String artistName, Bitmap image) {
        if (image == null) {
            return;
        }
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(ArtistImageContract.Entry.COLUMN_NAME_MBID, mbid);
        values.put(ArtistImageContract.Entry.COLUMN_NAME_ARTIST_NAME, artistName);
        values.put(ArtistImageContract.Entry.COLUMN_NAME_ARTIST_IMAGE, BitmapHelper.bitmapToByteArray(image));


        db.insertWithOnConflict(ArtistImageContract.Entry.TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_REPLACE);
    }

    public byte[] getArtistImageData(String artistName) {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        Bitmap b = null;

        Cursor c = db.query(ArtistImageContract.Entry.TABLE_NAME, sProjection, ArtistImageContract.Entry.COLUMN_NAME_ARTIST_NAME + "=?", new String[]{artistName}, null, null, null);
        if (c != null && c.moveToFirst()) {
            byte[] bytes = c.getBlob(2);
            c.close();
            return bytes;
        }

        if (c != null) {
            c.close();
        }

        return null;
    }


    public void delete(String mbid) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        db.delete(ArtistImageContract.Entry.TABLE_NAME, ArtistImageContract.Entry.COLUMN_NAME_MBID + "=?", new String[]{mbid});
    }

    public void recreate() {
        mDbHelper.recreate();
    }
}
