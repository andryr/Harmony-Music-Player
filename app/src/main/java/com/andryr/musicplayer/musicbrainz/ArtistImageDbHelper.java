package com.andryr.musicplayer.musicbrainz;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.ByteArrayOutputStream;

/**
 * Created by Andry on 18/10/15.
 */
public class ArtistImageDbHelper extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "ArtistImage.db";

    private static final String COMMA_SEP = ",";

    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + ArtistImageContract.Entry.TABLE_NAME + " (" +
                    ArtistImageContract.Entry._ID + " INTEGER PRIMARY KEY," +
                    ArtistImageContract.Entry.COLUMN_NAME_MBID + " CHAR(36) UNIQUE" + COMMA_SEP +
                    ArtistImageContract.Entry.COLUMN_NAME_ARTIST_NAME + " TEXT" + COMMA_SEP +
                    ArtistImageContract.Entry.COLUMN_NAME_ARTIST_IMAGE + " BLOB" +
                    " )";

    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + ArtistImageContract.Entry.TABLE_NAME;

    private static final String[] sProjection = new String[]
            {
                    ArtistImageContract.Entry._ID, //0
                    ArtistImageContract.Entry.COLUMN_NAME_ARTIST_NAME, //1
                    ArtistImageContract.Entry.COLUMN_NAME_ARTIST_IMAGE, //2
            };

    public ArtistImageDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    private static byte[] bitmapToByteArray(Bitmap bitmap) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
        return out.toByteArray();
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

    public void recreate()
    {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    public void insertOrUpdate(String mbid, String artistName, Bitmap image) {
        if(image == null)
        {
            return;
        }
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(ArtistImageContract.Entry.COLUMN_NAME_MBID, mbid);
        values.put(ArtistImageContract.Entry.COLUMN_NAME_ARTIST_NAME, artistName);
        values.put(ArtistImageContract.Entry.COLUMN_NAME_ARTIST_IMAGE, bitmapToByteArray(image));


        db.insertWithOnConflict(ArtistImageContract.Entry.TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_REPLACE);
    }

    public Bitmap getArtistImage(String artistName) {
        SQLiteDatabase db = getReadableDatabase();

        Bitmap b = null;

        Cursor c = db.query(ArtistImageContract.Entry.TABLE_NAME, sProjection, ArtistImageContract.Entry.COLUMN_NAME_ARTIST_NAME + "=?", new String[]{artistName}, null, null, null);
        if (c != null && c.moveToFirst()) {
            byte[] bytes = c.getBlob(2);
            b = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            c.close();
        }

        return b;
    }


    public void delete(String mbid) {
        SQLiteDatabase db = getWritableDatabase();

        db.delete(ArtistImageContract.Entry.TABLE_NAME, ArtistImageContract.Entry.COLUMN_NAME_MBID + "=?", new String[]{mbid});
    }
}
