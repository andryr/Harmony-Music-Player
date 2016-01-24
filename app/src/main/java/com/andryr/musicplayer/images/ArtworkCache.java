package com.andryr.musicplayer.images;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.v4.graphics.BitmapCompat;
import android.support.v4.util.LruCache;
import android.util.Log;

import com.andryr.musicplayer.R;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Andry on 23/01/16.
 */
public class ArtworkCache extends AbstractBitmapCache<Long> {
    private static final LruCache<Long, Bitmap> sLargeImageCache;


    private static final LruCache<Long, Bitmap> sThumbCache;
    private static final String TAG = "ArtworkCache";
    private static ArtworkCache sInstance;

    static {


        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);

        final int cacheSize = maxMemory / 8;

        sLargeImageCache = new LruCache<Long, Bitmap>(cacheSize) {


            @Override
            protected int sizeOf(Long key, Bitmap bitmap) {

                return BitmapCompat.getAllocationByteCount(bitmap) / 1024;
            }
        };

        sThumbCache = new LruCache<Long, Bitmap>(cacheSize) {


            @Override
            protected int sizeOf(Long key, Bitmap bitmap) {

                return BitmapCompat.getAllocationByteCount(bitmap) / 1024;
            }
        };
    }

    private final List<String> mUnavailableList = new ArrayList<>();
    private final Context mContext;
    private int mLargeArtworkSize;
    private int mThumbSize;

    public ArtworkCache(Context context) {
        mContext = context;
        mLargeArtworkSize = context.getResources().getDimensionPixelSize(R.dimen.playback_activity_art_size);
        mThumbSize = context.getResources().getDimensionPixelSize(R.dimen.art_thumbnail_size);
    }

    public static void init(Context context) {
        sInstance = new ArtworkCache(context);
    }

    public static ArtworkCache getInstance() {
        return sInstance;
    }

    @Override
    public synchronized Bitmap getCachedBitmap(Long key, int w, int h) {
        if (key == -1) {
            return null;
        }
        Bitmap b;
        //if (w > mThumbSize) {
        b = sLargeImageCache.get(key);
        //} else {
        //    b = sThumbCache.get(key);
        //}
        return b;
    }

    @Override
    protected Bitmap retrieveBitmap(Long key, int w, int h) {
        if (key == -1) {
            return null;
        }
        Uri uri = ContentUris.withAppendedId(ArtworkHelper.getArtworkUri(), key);

        try {
            if (uri != null) {
                ContentResolver res = mContext.getContentResolver();
                Bitmap bitmap = BitmapHelper.decode(res.openInputStream(uri), w, h);
                return bitmap;


            }
        } catch (IOException e) {
            Log.e("io", "io", e);
        }
        return null;
    }

    @Override
    protected synchronized void cacheBitmap(Long key, Bitmap bitmap) {
        sLargeImageCache.put(key, bitmap);
    }

    @Override
    protected Bitmap getDefaultBitmap() {
        return null;
    }

    @Override
    protected Drawable getDefaultDrawable(Context context) {
        return ArtworkHelper.getDefaultArtworkDrawable(context);
    }

    @Override
    public synchronized void clear() {
        sLargeImageCache.evictAll();


    }
}
