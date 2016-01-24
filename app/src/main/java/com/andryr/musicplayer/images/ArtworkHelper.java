package com.andryr.musicplayer.images;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;

import com.andryr.musicplayer.R;

public class ArtworkHelper {

    private static final Uri sArtworkUri = Uri
            .parse("content://media/external/audio/albumart");
    private static Drawable sDefaultArtworkDrawable;
    private static Bitmap sDefaultArtworkBitmap;


    public static Uri getArtworkUri() {
        return sArtworkUri;
    }

    public static Bitmap getDefaultArtworkBitmap(Context c) {
        if (sDefaultArtworkBitmap == null) {
            sDefaultArtworkBitmap = ((BitmapDrawable) c.getResources().getDrawable(
                    R.drawable.note)).getBitmap();

        }
        return sDefaultArtworkBitmap;
    }


    public static Drawable getDefaultArtworkDrawable(Context c) {
        if (sDefaultArtworkDrawable == null) {
            sDefaultArtworkDrawable = c.getResources().getDrawable(R.drawable.default_artwork);

        }
        return sDefaultArtworkDrawable.getConstantState().newDrawable(c.getResources()).mutate();
    }


    public static boolean deleteArtwork(Context c, int albumId) {
        return c.getContentResolver().delete(ContentUris.withAppendedId(sArtworkUri, albumId), null, null) > 0;
    }

    public static Uri insertOrUpdate(Context c, int albumId, String filePath) {
        deleteArtwork(c,albumId);
        ContentValues values = new ContentValues();
        values.put("album_id", albumId);
        values.put("_data", filePath);
        return c.getContentResolver().insert(sArtworkUri, values);
    }

}
