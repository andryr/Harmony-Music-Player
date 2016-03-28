/*
 * Copyright 2016 andryr
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
import com.andryr.musicplayer.utils.PrefUtils;

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

            @Override
            protected void entryRemoved(boolean evicted, Long key, Bitmap oldValue, Bitmap newValue) {
                super.entryRemoved(evicted, key, oldValue, newValue);
                //TODO recycler oldValue ?
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
        if (key == -1 || PrefUtils.getInstance().useFreeArtworks()) {
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

        if (PrefUtils.getInstance().useFreeArtworks()) {
            return getFreeArtwork(reqWidth, reqHeight);
        }

        Uri uri = ContentUris.withAppendedId(ArtworkHelper.getArtworkUri(), key);

        try {
            if (uri != null) {
                ContentResolver res = mContext.getContentResolver();
                Bitmap bitmap = BitmapHelper.decode(res.openInputStream(uri), reqWidth, reqHeight);
                return bitmap;


            }
        } catch (IOException e) {
            Log.e(TAG, "get image from contentresolver", e);
        }

        try {
            return downloadArtwork(mContext, key, reqWidth, reqHeight);
        } catch (IOException e) {
            Log.e(TAG, "download", e);
        }

        return null;
    }

    private Bitmap getFreeArtwork(int reqWidth, int reqHeight) {
        try {
            return ImageDownloader.getInstance().download("http://lorempixel.com/600/600/abstract/", reqWidth, reqHeight);
        } catch (IOException e) {
            Log.e(TAG, "failed to download free artwork at http://lorempixel.com/600/600/abstract/", e);
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
        if (reqWidth <= mThumbSize && reqHeight <= mThumbSize) {
            return ArtworkHelper.getDefaultThumbDrawable(context);
        } else {
            return ArtworkHelper.getDefaultArtworkDrawable(context);
        }
    }

    @Override
    public synchronized void clear() {
        sLargeImageCache.evictAll();


    }

    public Bitmap downloadArtwork(final Context context, final long albumId, int reqWidth, int reqHeight) throws IOException {
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
        AlbumInfo body = response.body();
        if (body != null) {
            final AlbumInfo.Album info = body.getAlbum();
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
