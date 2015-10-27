package com.andryr.musicplayer.utils;

import android.annotation.TargetApi;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;
import android.widget.ImageView;

import com.andryr.musicplayer.R;

import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;

public class ArtworkHelper {

    private static final Map<Long, Bitmap> sArtworkCache = new HashMap<>();
    private static final Uri sArtworkUri = Uri
            .parse("content://media/external/audio/albumart");
    private static Drawable sDefaultArtworkDrawable;
    private static Bitmap sDefaultArtworkBitmap;
    private static BitmapFactory.Options sBitmapOptions = new BitmapFactory.Options();

    static {
        sBitmapOptions.inScaled = false;
        sBitmapOptions.inDither = false;
        sBitmapOptions.inPreferredConfig = Bitmap.Config.ARGB_8888;
    }

    public static boolean isArtworkLoaded(long albumId) {
        return sArtworkCache.containsKey(albumId);
    }

    public static Bitmap getArtworkBitmap(Context context, long albumId) {
        if (albumId == -1) {
            return null;
        }
        synchronized (sArtworkCache) {
            if (isArtworkLoaded(albumId)) {
                return sArtworkCache.get(albumId);
            }
        }

        Uri uri = ContentUris.withAppendedId(sArtworkUri, albumId);

        try {
            if (uri != null) {
                ContentResolver res = context.getContentResolver();
                Bitmap b = BitmapFactory.decodeStream(res.openInputStream(uri));
                synchronized (sArtworkCache) {
                    sArtworkCache.put(albumId, b);

                }
                return b;
            }
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            Log.e("io", "io", e);
        }

        return null;

    }

    public static Bitmap getDefaultArtworkBitmap(Context c) {
        if (sDefaultArtworkBitmap == null) {
            sDefaultArtworkBitmap = ((BitmapDrawable) c.getResources().getDrawable(
                    R.drawable.note)).getBitmap();

        }
        return sDefaultArtworkBitmap;
    }

    public static Drawable getArtworkDrawable(Context context, long albumId) {
        Bitmap b = getArtworkBitmap(context, albumId);
        if (b != null) {
            return new BitmapDrawable(b);
        }

        return null;

    }

    public static Drawable getDefaultArtworkDrawable(Context c) {
        if (sDefaultArtworkDrawable == null) {
            sDefaultArtworkDrawable = c.getResources().getDrawable(
                    R.drawable.default_artwork);

        }
        return sDefaultArtworkDrawable;
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static void loadArtworkAsync(final long albumId, final ImageView... views) {


        final Context context = views[0].getContext();
        if (ArtworkHelper.isArtworkLoaded(albumId)) {
            for (ImageView view : views) {
                view.setImageDrawable(ArtworkHelper.getArtworkDrawable(context, albumId));
            }
            return;
        }
        final Drawable defaultDrawable = getDefaultArtworkDrawable(context);

        for (ImageView view : views) {
            view.setImageDrawable(defaultDrawable);
        }

        AsyncTask<Void, Void, Drawable> task = new AsyncTask<Void, Void, Drawable>() {

            @Override
            protected Drawable doInBackground(Void... params) {


                Drawable artwork = getArtworkDrawable(context, albumId);
                if (artwork == null) {
                    return null;
                }

                TransitionDrawable transitionDrawable = new TransitionDrawable(defaultDrawable, artwork);


                /*artwork = artwork.getConstantState().newDrawable();
                artwork.mutate();*/

                /*if (oldDrawable != null && artwork != null
                        && oldDrawable != artwork) {
                    TransitionDrawable transition = new TransitionDrawable(
                            new Drawable[]{oldDrawable, artwork});
                    transition.setCrossFadeEnabled(true);
                    return transition;
                }*/
                return transitionDrawable;
            }

            @Override
            protected void onPostExecute(Drawable result) {

                if (result != null) {
                    for (ImageView view : views) {
                        view.setImageDrawable(result);
                    }
                    if (result instanceof TransitionDrawable) {
                        ((TransitionDrawable) result).startTransition();
                    }
                }


            }

        };


        task.execute((Void) null);

    }

    public static void clearArtworkCache() {
        sArtworkCache.clear();
    }


}
