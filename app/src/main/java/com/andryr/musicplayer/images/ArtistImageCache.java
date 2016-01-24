package com.andryr.musicplayer.images;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.v4.graphics.BitmapCompat;
import android.support.v4.util.LruCache;
import android.util.Log;
import android.widget.ImageView;

import com.andryr.musicplayer.R;
import com.andryr.musicplayer.lastfm.ArtistInfo;
import com.andryr.musicplayer.lastfm.Image;
import com.andryr.musicplayer.lastfm.LastFm;
import com.andryr.musicplayer.utils.Connectivity;
import com.andryr.musicplayer.utils.ImageDownloader;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by Andry on 17/10/15.
 */
public class ArtistImageCache extends AbstractBitmapCache<String> {

    private static final LruCache<String, Bitmap> sLargeImageCache;


    private static final LruCache<String, Bitmap> sThumbCache;
    private static final String TAG = "ArtistImageCache";
    private static ArtistImageCache sInstance = null;

    static {


        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);

        final int cacheSize = maxMemory / 8;

        sLargeImageCache = new LruCache<String, Bitmap>(cacheSize) {


            @Override
            protected int sizeOf(String key, Bitmap bitmap) {

                return BitmapCompat.getAllocationByteCount(bitmap) / 1024;
            }
        };

        sThumbCache = new LruCache<String, Bitmap>(cacheSize) {


            @Override
            protected int sizeOf(String key, Bitmap bitmap) {

                return BitmapCompat.getAllocationByteCount(bitmap) / 1024;
            }
        };
    }

    private final List<String> mUnavailableList = new ArrayList<>();
    private final ArtistImageDb mDatabase;
    private Context mContext;
    private int mLargeImageWidth;
    private int mLargeImageHeight;

    private int mThumbWidth;
    private int mThumbHeight;

    private ArtistImageCache(Context context) {


        mContext = context;

        mDatabase = new ArtistImageDb(mContext);

        final Resources res = mContext.getResources();
        mLargeImageWidth = res.getDimensionPixelSize(R.dimen.artist_image_req_width);
        mLargeImageHeight = mLargeImageHeight;

        mThumbWidth = res.getDimensionPixelSize(R.dimen.art_thumbnail_size);
        mThumbHeight = mThumbWidth;

    }

    public static void init(Context context) {
        sInstance = new ArtistImageCache(context);
    }

    public static ArtistImageCache getInstance() {

        return sInstance;
    }



    @Override
    public synchronized Bitmap getCachedBitmap(String artistName, int reqWidth, int reqHeight) {
        Bitmap b;
        if (reqWidth > mThumbWidth) {
            b = sLargeImageCache.get(artistName);
        } else {
            b = sThumbCache.get(artistName);
        }
        return b;

    }

    public Bitmap downloadImage(final Context context, final String artistName, int reqWidth, int reqHeight) throws IOException {
        if (!Connectivity.isConnected(context) || !Connectivity.isWifi(context)) {

            throw new IOException("not connected to wifi");
        }

        if (mUnavailableList.contains(artistName)) {
            return null;
        }


        retrofit2.Response<ArtistInfo> response = LastFm.getArtistInfo(artistName).execute();
        final ArtistInfo.Artist info = response.body().getArtist();
        if (info.getImageList() != null && info.getImageList().size() > 0) {
            String imageUrl = null;
            for (Image image : info.getImageList()) {
                if (image.getSize().equals("mega")) {
                    imageUrl = image.getUrl();
                    break;
                }
            }
            if (imageUrl != null && !("".equals(imageUrl.trim()))) {
                Bitmap bitmap = ImageDownloader.getInstance().download(imageUrl, reqWidth, reqHeight);
                if (bitmap != null) {
                    save(info.getMbid(), artistName, bitmap);
                    return bitmap;
                } else {
                    mUnavailableList.add(artistName);
                }
            }
        }
        return null;


    }

    private void save(String mbid, String artistName, Bitmap image) {
        if (image.getWidth() >= mLargeImageWidth) {
            mDatabase.insertOrUpdate(mbid, artistName, image);
        }
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

        try {
            return downloadImage(mContext, artistName, reqWidth, reqHeight);
        } catch (IOException e) {
            Log.e(TAG, "getNonCachedBitmap download", e);
        }
        return null;
    }

    @Override
    protected synchronized void cacheBitmap(String artistName, Bitmap bitmap) {
        if (bitmap.getWidth() >= mLargeImageWidth) {
            sLargeImageCache.put(artistName, bitmap);
        } else {
            sThumbCache.put(artistName, bitmap);
        }
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
    public void clear() {
        clearDbCache();
        clearMemoryCache();
    }

    public void clearDbCache() {
        mDatabase.recreate();
    }

    public synchronized void clearMemoryCache() {
        sThumbCache.evictAll();
        sLargeImageCache.evictAll();
    }


}

