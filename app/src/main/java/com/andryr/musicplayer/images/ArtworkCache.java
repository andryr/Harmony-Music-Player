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
import com.andryr.musicplayer.api.lastfm.AlbumInfo;
import com.andryr.musicplayer.api.lastfm.Image;
import com.andryr.musicplayer.api.lastfm.LastFm;
import com.andryr.musicplayer.model.Album;
import com.andryr.musicplayer.utils.Albums;
import com.andryr.musicplayer.utils.Connectivity;
import com.andryr.musicplayer.utils.ImageDownloader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Andry on 23/01/16.
 */
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
            Log.e(TAG, "get image from contentresolver", e);
        }

        try {
            return downloadImage(mContext,key,w,h);
        } catch (IOException e) {
            Log.e(TAG, "download",e);
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

    public Bitmap downloadImage(final Context context, final long albumId, int reqWidth, int reqHeight) throws IOException {
        if (!Connectivity.isConnected(context) || !Connectivity.isWifi(context)) {

            throw new IOException("not connected to wifi");
        }

        String albumName = null;
        String artistName = null;
        Album album = Albums.getAlbum(context, albumId);
        if (album != null) {
            albumName = album.getAlbumName();
            artistName = album.getArtistName();
        }
        if (mUnavailableList.contains(albumName)) {
            return null;
        }


        retrofit2.Response<AlbumInfo> response = LastFm.getAlbumInfo(albumName, artistName).execute();
        final AlbumInfo.Album info = response.body().getAlbum();
        if (info != null && info.getImageList() != null && info.getImageList().size() > 0) {
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
                    save(albumId, albumName, bitmap);
                    return bitmap;
                } else {
                    mUnavailableList.add(albumName);
                }
            }
        }
        return null;


    }

    private void save(long albumId, String albumName, Bitmap bitmap) {
        try {
            ArtworkHelper.insertOrUpdate(mContext, albumId, albumName, bitmap);
        } catch (IOException e) {
            Log.e(TAG, "save", e);
        }
    }
}
