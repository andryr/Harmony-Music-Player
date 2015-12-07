package com.andryr.musicplayer.utils;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.graphics.BitmapCompat;
import android.support.v4.util.LruCache;
import android.util.Log;
import android.widget.ImageView;

import com.andryr.musicplayer.R;
import com.andryr.musicplayer.animation.TransitionDrawable;

import java.io.FileNotFoundException;

public class ArtworkHelper {

    private static final Uri sArtworkUri = Uri
            .parse("content://media/external/audio/albumart");
    private static final LruCache<Long, Bitmap> sArtworkCache;
    private static final BitmapFactory.Options sBitmapOptions = new BitmapFactory.Options();
    private static Drawable sDefaultArtworkDrawable;
    private static Bitmap sDefaultArtworkBitmap;

    static {
        sBitmapOptions.inScaled = false;
        sBitmapOptions.inDither = false;
        sBitmapOptions.inPreferredConfig = Bitmap.Config.ARGB_8888;

        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);

        final int cacheSize = maxMemory / 8;

        sArtworkCache = new LruCache<Long, Bitmap>(cacheSize) {


            @Override
            protected int sizeOf(Long key, Bitmap bitmap) {

                return BitmapCompat.getAllocationByteCount(bitmap) / 1024;
            }
        };


    }


    public static Bitmap getArtworkBitmap(Context context, long albumId) {
        if (albumId == -1) {
            return null;
        }
        Bitmap bitmap = getBitmapFromMemCache(albumId);

        if (bitmap != null) {
            return bitmap;
        }

        Uri uri = ContentUris.withAppendedId(sArtworkUri, albumId);

        try {
            if (uri != null) {
                ContentResolver res = context.getContentResolver();
                bitmap = BitmapFactory.decodeStream(res.openInputStream(uri), null, sBitmapOptions);
                synchronized (sArtworkCache) {
                    if (sArtworkCache.get(albumId) == null) {
                        sArtworkCache.put(albumId, bitmap);
                    }

                }
                return bitmap;
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
            sDefaultArtworkDrawable = c.getResources().getDrawable(R.drawable.default_artwork);

        }
        return sDefaultArtworkDrawable.getConstantState().newDrawable(c.getResources()).mutate();
    }


    private static Bitmap getBitmapFromMemCache(final long albumId) {
        synchronized (sArtworkCache) {
            return sArtworkCache.get(albumId);
        }
    }

    public static void loadArtwork(final Context context, final long albumId, final OnArtworkLoadedListener listener ) {






        AsyncTask<Void, Void, Bitmap> task = new AsyncTask<Void, Void, Bitmap>() {

            @Override
            protected Bitmap doInBackground(Void... params) {


                return getArtworkBitmap(context, albumId);

            }

            @Override
            protected void onPostExecute(Bitmap result) {

                if (result != null) {


                    listener.onArtworkLoaded(result);


                }
            }

        };


        task.execute((Void) null);

    }
    public static void loadArtwork(final long albumId, final boolean autoScaleType, final ImageView... views) {


        final Context context = views[0].getContext();
        Bitmap b = getBitmapFromMemCache(albumId);
        if (b != null) {
            for (ImageView view : views) {
                if(autoScaleType)
                {
                    view.setScaleType(ImageView.ScaleType.CENTER_CROP);
                }
                view.setImageDrawable(createBitmapDrawable(context, b));

            }
            return;
        }


        for (ImageView view : views) {
            if (autoScaleType) {
                view.setScaleType(ImageView.ScaleType.FIT_XY);
            }
            view.setImageDrawable(getDefaultArtworkDrawable(context));

        }

        AsyncTask<Void, Void, Bitmap> task = new AsyncTask<Void, Void, Bitmap>() {

            @Override
            protected Bitmap doInBackground(Void... params) {


                return getArtworkBitmap(context, albumId);

            }

            @Override
            protected void onPostExecute(Bitmap result) {

                if (result != null) {


                    for (ImageView view : views) {
                        if(autoScaleType)
                        {
                            view.setScaleType(ImageView.ScaleType.CENTER_CROP);
                        }
                        TransitionDrawable transitionDrawable = new TransitionDrawable(getDefaultArtworkDrawable(context), createBitmapDrawable(context, result));
                        view.setImageDrawable(transitionDrawable);
                        transitionDrawable.startTransition();
                    }


                }
            }

        };


        task.execute((Void) null);

    }

    public static void loadArtwork(long albumId, ImageView... views) {
        loadArtwork(albumId, false, views);
    }

    public static void clearArtworkCache() {
        sArtworkCache.evictAll();
    }


    private static Drawable createBitmapDrawable(Context context, Bitmap bitmap) {
        BitmapDrawable d = new BitmapDrawable(context.getResources(), bitmap);
        return d.getConstantState().newDrawable(context.getResources()).mutate();
    }

    public interface OnArtworkLoadedListener {
        void onArtworkLoaded(Bitmap artwork);
    }
}
