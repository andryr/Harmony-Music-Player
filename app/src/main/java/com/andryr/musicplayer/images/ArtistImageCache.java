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

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.support.v4.graphics.BitmapCompat;
import android.support.v4.util.LruCache;
import android.util.Log;

import com.andryr.musicplayer.R;
import com.andryr.musicplayer.api.lastfm.ArtistInfo;
import com.andryr.musicplayer.api.lastfm.Image;
import com.andryr.musicplayer.api.lastfm.LastFm;
import com.andryr.musicplayer.utils.Connectivity;
import com.andryr.musicplayer.utils.ImageDownloader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by Andry on 17/10/15.
 */
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
    private Context mContext;
    private int mLargeImageSize;

    private int mThumbSize;

    private ArtistImageCache(Context context) {
        super();

        mContext = context;

        mDatabase = new ArtistImageDb(mContext);

        final Resources res = mContext.getResources();
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

        try {
            return downloadImage(mContext, artistName, reqWidth, reqHeight);
        } catch (IOException e) {
            Log.e(TAG, "getNonCachedBitmap download", e);
        }
        return null;
    }

    public Bitmap downloadImage(final Context context, final String artistName, int reqWidth, int reqHeight) throws IOException {
        if (!Connectivity.isConnected(context) || !Connectivity.isWifi(context)) {

            throw new IOException("not connected to wifi");
        }

        if (mUnavailableList.contains(artistName)) {
            return null;
        }


        retrofit2.Response<ArtistInfo> response = LastFm.getArtistInfo(artistName).execute();
        ArtistInfo body = response.body();
        if (body != null) {
            final ArtistInfo.Artist info = body.getArtist();
            if (info != null && info.getImageList() != null && info.getImageList().size() > 0) {
                String imageUrl = null;
                for (Image image : info.getImageList()) {
                    if (image.getSize().equals("mega")) {
                        imageUrl = image.getUrl();
                        break;
                    }
                }
                if (imageUrl != null && !("".equals(imageUrl.trim()))) {
                    Bitmap bitmap = ImageDownloader.getInstance().download(imageUrl, mLargeImageSize, mLargeImageSize);
                    if (bitmap != null) {
                        save(info.getMbid(), artistName, bitmap);
                        return BitmapHelper.scale(bitmap, reqWidth, reqHeight);
                    } else {
                        mUnavailableList.add(artistName);
                    }
                }
            }
        }
        return null;


    }

    private void save(String mbid, String artistName, Bitmap image) {
        Log.d(TAG, "cached "+artistName);
        mDatabase.insertOrUpdate(mbid, artistName, image);
    }

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

    public void clearDbCache() {
        mDatabase.recreate();
    }

    public synchronized void clearMemoryCache() {
        sThumbCache.evictAll();
        sLargeImageCache.evictAll();
    }


}

