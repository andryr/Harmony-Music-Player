package org.oucho.musicplayer.images;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.support.v4.graphics.BitmapCompat;
import android.support.v4.util.LruCache;

import org.oucho.musicplayer.R;

import java.util.ArrayList;
import java.util.List;


public class ArtistImageCache extends BitmapCache<String> {

    private static final LruCache<String, Bitmap> sLargeImageCache;


    private static final LruCache<String, Bitmap> sThumbCache;
    private static final String TAG = "ArtistImageCache";
    private static ArtistImageCache sInstance = null;

    static {

        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);

        sLargeImageCache = new LruCache<String, Bitmap>(maxMemory / 16) {


            @Override
            protected int sizeOf(String key, Bitmap bitmap) {

                return BitmapCompat.getAllocationByteCount(bitmap) / 1024;
            }
        };

        sThumbCache = new LruCache<String, Bitmap>(maxMemory / 8) {

            @Override
            protected int sizeOf(String key, Bitmap bitmap) {

                return BitmapCompat.getAllocationByteCount(bitmap) / 1024;
            }
        };
    }

    private final List<String> mUnavailableList = new ArrayList<>();
    private final ArtistImageDb mDatabase;
    private int mLargeImageSize;

    private int mThumbSize;

    private ArtistImageCache(Context context) {
        super();

        mDatabase = new ArtistImageDb(context);

        final Resources res = context.getResources();
        mLargeImageSize = res.getDimensionPixelSize(R.dimen.artist_image_req_width);

        mThumbSize = res.getDimensionPixelSize(R.dimen.art_thumbnail_size);

    }

    public static void init(Context context) {
        sInstance = new ArtistImageCache(context);
    }

    public static ArtistImageCache getInstance() {

        return sInstance;
    }


    @Override
    public synchronized Bitmap getCachedBitmap(String artistName, int reqWidth, int reqHeight) {
        Bitmap b = null;
        if (reqWidth > mThumbSize || reqHeight > mThumbSize) {
            b = sLargeImageCache.get(artistName);
        }
        if (b == null) {
            b = sThumbCache.get(artistName);// il vaut mieux retourner une petite image que rien du tout
        }
        return b;

    }

    @Override
    public Bitmap retrieveBitmap(String artistName, int reqWidth, int reqHeight) {
        if (artistName == null || artistName.length() == 0) {
            return null;
        }


        final byte[] bytes = mDatabase.getArtistImageData(artistName);
        if (bytes != null) {
            Bitmap b = BitmapHelper.decode(bytes, reqWidth, reqHeight);
            if (b != null) {
                return b;
            }
        }

        return null;
    }


/*    private void save(String mbid, String artistName, Bitmap image) {
        if (image.getWidth() >= mLargeImageSize) {
            mDatabase.insertOrUpdate(mbid, artistName, image);
        }
    }*/

    @Override
    protected synchronized void cacheBitmap(String artistName, Bitmap bitmap) {
        if (bitmap.getWidth() < mLargeImageSize || bitmap.getHeight() < mLargeImageSize) {
            sThumbCache.put(artistName, bitmap);
        } else {
            sLargeImageCache.put(artistName, bitmap);
        }
    }

    @Override
    protected Bitmap getDefaultBitmap() {
        return null;
    }

    @Override
    protected Drawable getDefaultDrawable(Context context, int reqWidth, int reqHeight) {
        if (reqWidth <= mThumbSize && reqHeight <= mThumbSize) {
            return ArtistImageHelper.getDefaultArtistThumb(context);
        } else {
            return ArtistImageHelper.getDefaultArtistImage(context);
        }
    }


    @Override
    public void clear() {
        clearDbCache();
        clearMemoryCache();
    }

    private void clearDbCache() {
        mDatabase.recreate();
    }

    private synchronized void clearMemoryCache() {
        sThumbCache.evictAll();
        sLargeImageCache.evictAll();
    }


}

