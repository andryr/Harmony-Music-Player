package org.oucho.musicplayer.images;

import android.Manifest;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import org.oucho.musicplayer.R;
import org.oucho.musicplayer.utils.Permissions;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class ArtworkHelper {

    private static final Uri ARTWORK_URI = Uri.parse("content://media/external/audio/albumart");
    private static final String ARTWORKS_DIR_NAME = "artworks";
    private static final String TAG = "ArtworkHelper";

    private static final String FILENAME_PREFIX = "album";

    private static Drawable sDefaultArtworkDrawable;
    private static Drawable sDefaultThumbDrawable;

    public static Uri getArtworkUri() {
        return ARTWORK_URI;
    }

    public static Drawable getDefaultArtworkDrawable(Context c) {
        if (sDefaultArtworkDrawable == null) {
            sDefaultArtworkDrawable = ContextCompat.getDrawable(c, R.drawable.default_artwork);

        }
        return sDefaultArtworkDrawable.getConstantState().newDrawable(c.getResources()).mutate();
    }

    public static Drawable getDefaultThumbDrawable(Context c) {
        if (sDefaultThumbDrawable == null) {
            sDefaultThumbDrawable = ContextCompat.getDrawable(c, R.drawable.default_album_thumb);

        }
        return sDefaultThumbDrawable.getConstantState().newDrawable(c.getResources()).mutate();
    }


    public static Uri insertOrUpdate(Context c, long albumId, String albumName, Bitmap bitmap) throws IOException {
        if (!Permissions.checkPermission(c, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            return null;
        }
        String prefix = FILENAME_PREFIX+albumName.replaceAll("\\W+", "_");
        File file = File.createTempFile(prefix, ".png", getArtworkStorageDir());
        FileOutputStream out = new FileOutputStream(file);
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
        out.flush();
        out.close();

        return insertOrUpdate(c, albumId, file.getAbsolutePath());
    }

    private static File getArtworkStorageDir() {
        File file = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_MUSIC), ARTWORKS_DIR_NAME);
        if (!file.mkdirs()) {
            Log.e(TAG, "Directory not created");
        }
        return file;
    }

    private static Uri insertOrUpdate(Context c, long albumId, String filePath) {
        deleteArtwork(c, albumId);
        ContentValues values = new ContentValues();
        values.put("album_id", albumId);
        values.put("_data", filePath);
        return c.getContentResolver().insert(ARTWORK_URI, values);
    }

    private static boolean deleteArtwork(Context c, long albumId) {
        return c.getContentResolver().delete(ContentUris.withAppendedId(ARTWORK_URI, albumId), null, null) > 0;
    }

}
