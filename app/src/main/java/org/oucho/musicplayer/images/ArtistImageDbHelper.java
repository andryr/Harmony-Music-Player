package org.oucho.musicplayer.images;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

class ArtistImageDbHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "ArtistImage.db";

    private static final String COMMA_SEP = ",";

    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + ArtistImageContract.Entry.TABLE_NAME + " (" +
                    ArtistImageContract.Entry._ID + " INTEGER PRIMARY KEY," +
                    ArtistImageContract.Entry.COLUMN_NAME_MBID + " CHAR(36)" + COMMA_SEP +
                    ArtistImageContract.Entry.COLUMN_NAME_ARTIST_NAME + " TEXT UNIQUE" + COMMA_SEP +
                    ArtistImageContract.Entry.COLUMN_NAME_ARTIST_IMAGE + " BLOB" +
                    " )";

    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + ArtistImageContract.Entry.TABLE_NAME;



    public ArtistImageDbHelper(Context context) {
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

    public void recreate()
    {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }


}
