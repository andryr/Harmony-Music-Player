package org.oucho.musicplayer.images;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.v4.graphics.BitmapCompat;
import android.support.v4.util.LruCache;
import android.util.Log;

import org.oucho.musicplayer.R;

import java.io.IOException;

public class ArtworkCache extends BitmapCache<Long> {
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

            @Override
            protected void entryRemoved(boolean evicted, Long key, Bitmap oldValue, Bitmap newValue) {
                super.entryRemoved(evicted, key, oldValue, newValue);
            }
        };

        sThumbCache = new LruCache<Long, Bitmap>(cacheSize) {

            @Override
            protected int sizeOf(Long key, Bitmap bitmap) {

                return BitmapCompat.getAllocationByteCount(bitmap) / 1024;
            }
        };
    }

    private final Context mContext;
    private final int mLargeArtworkSize;
    private final int mThumbSize;

    private ArtworkCache(Context context) {
        super();
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
    public synchronized Bitmap getCachedBitmap(Long key, int reqWidth, int reqHeight) {
        if (key == -1) {
            return null;
        }
        Bitmap b = null;
        if (reqWidth > mThumbSize || reqHeight > mThumbSize) {
            b = sLargeImageCache.get(key);
        }
        if (b == null) {
            b = sThumbCache.get(key);// il vaut mieux retourner une petite image que rien du tout
        }
        return b;
    }

    @Override
    protected Bitmap retrieveBitmap(Long key, int reqWidth, int reqHeight) {
        if (key == -1) {
            return null;
        }
        Uri uri = ContentUris.withAppendedId(ArtworkHelper.getArtworkUri(), key);

        try {
            if (uri != null) {
                ContentResolver res = mContext.getContentResolver();
                return BitmapHelper.decode(res.openInputStream(uri), reqWidth, reqHeight);
            }
        } catch (IOException e) {
            Log.e(TAG, "get image from contentresolver", e);
        }
        return null;
    }

    @Override
    protected synchronized void cacheBitmap(Long key, Bitmap bitmap) {
        if (bitmap.getWidth() < mLargeArtworkSize || bitmap.getHeight() < mLargeArtworkSize) {
            sThumbCache.put(key, bitmap);
        } else {
            sLargeImageCache.put(key, bitmap);
        }
    }

    @Override
    protected Bitmap getDefaultBitmap() {
        return null;
    }

    @Override
    protected Drawable getDefaultDrawable(Context context, int reqWidth, int reqHeight) {
        if(reqWidth <= mThumbSize && reqHeight <= mThumbSize) {
            return ArtworkHelper.getDefaultThumbDrawable(context);
        }
        else {
            return ArtworkHelper.getDefaultArtworkDrawable(context);
        }
    }

    public synchronized void clear() {
        sLargeImageCache.evictAll();
    }
}
