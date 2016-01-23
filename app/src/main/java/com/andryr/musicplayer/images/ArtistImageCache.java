package com.andryr.musicplayer.images;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v4.graphics.BitmapCompat;
import android.support.v4.util.LruCache;
import android.util.Log;
import android.widget.ImageView;

import com.andryr.musicplayer.R;
import com.andryr.musicplayer.animation.TransitionDrawable;
import com.andryr.musicplayer.lastfm.ArtistInfo;
import com.andryr.musicplayer.lastfm.Image;
import com.andryr.musicplayer.lastfm.LastFmService;
import com.andryr.musicplayer.utils.Connectivity;
import com.andryr.musicplayer.utils.ImageDownloader;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.MoshiConverterFactory;
import retrofit2.Retrofit;


/**
 * Created by Andry on 17/10/15.
 */
public class ArtistImageCache {

    private static final LruCache<String, Bitmap> sLargeImageCache;


    private static final LruCache<String, Bitmap> sThumbCache;
    private static final String TAG = "ArtistImageCache";
    private static BitmapFactory.Options sBitmapOptions = new BitmapFactory.Options();
    private static ArtistImageCache sInstance = null;

    static {
        sBitmapOptions.inScaled = false;
        sBitmapOptions.inDither = false;
        sBitmapOptions.inPreferredConfig = Bitmap.Config.ARGB_8888;

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
    private final ArtistImageDbHelper mDbHelper;
    private OkHttpClient mHttpClient;
    private Retrofit mRetrofit;
    private LastFmService mLastFmService;
    private Context mContext;
    private int mLargeImageWidth;
    private int mLargeImageHeight;

    private int mThumbWidth;
    private int mThumbHeight;

    private ArtistImageCache(Context context) {

        mHttpClient = new OkHttpClient.Builder().addInterceptor(new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Request original = chain.request();

                Request request = original.newBuilder()
                        .url(original.url() + "&api_key=" + LastFmService.API_KEY)
                        .build();

                return chain.proceed(request);

            }
        }).build();
        mRetrofit = new Retrofit.Builder()
                .baseUrl("http://ws.audioscrobbler.com/2.0/")
                .client(mHttpClient)
                .addConverterFactory(MoshiConverterFactory.create())
                .build();

        mLastFmService = mRetrofit.create(LastFmService.class);

        mContext = context;

        mDbHelper = new ArtistImageDbHelper(mContext);

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

    public static void clearMemoryCache() {
        synchronized (sThumbCache) {
            sThumbCache.evictAll();
        }
        synchronized (sLargeImageCache) {
            sLargeImageCache.evictAll();
        }
    }

    private void save(String mbid, String artistName, Bitmap image) {
        mDbHelper.insertOrUpdate(mbid, artistName, image);

    }

    private void saveAndCache(String mbid, String artistName, Bitmap image) {
        if(image.getWidth()>=mLargeImageWidth) {
            save(mbid, artistName, image);
        }
        putInCache(artistName, image);

    }

    public void clearDbCache() {
        mDbHelper.recreate();
    }

    public synchronized Bitmap getBitmapFromCache(String artistName, int reqWidth, int reqHeight) {
        Bitmap b;
        if(reqWidth>mThumbWidth) {
            b = sLargeImageCache.get(artistName);
        }
        else {
            b = sThumbCache.get(artistName);
        }
        return b;

    }



    private synchronized void putInCache(String artistName, Bitmap bitmap) {
        if(bitmap.getWidth()>=mLargeImageWidth) {
            sLargeImageCache.put(artistName, bitmap);
        }
        else {
            sThumbCache.put(artistName, bitmap);
        }
    }

    public Bitmap getNonCachedBitmap(String artistName, int reqWidth, int reqHeight) {
        if (artistName == null || artistName.length() == 0) {
            return null;
        }


        final byte[] bytes = mDbHelper.getArtistImageData(artistName);
        if (bytes != null) {
            Bitmap b = BitmapHelper.decode(bytes, reqWidth, reqHeight);
            if(b != null)
            {
                putInCache(artistName, b);
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

    public void loadArtistImage(final String artistName, ImageView view, final int reqWidth, final int reqHeight) {

        Bitmap b = getBitmapFromCache(artistName, reqWidth, reqHeight);
        if (b != null) {
            setBitmap(b,view);
            return;
        }
        view.setScaleType(ImageView.ScaleType.FIT_CENTER);
        view.setImageDrawable(ArtworkHelper.getDefaultArtworkDrawable(mContext));



        final Object viewTag = view.getTag();
        Log.d(TAG, "Image\nreqWidth : "+reqWidth+"\nreqHeight : "+reqHeight);

        final WeakReference<ImageView> viewRef = new WeakReference<>(view);
        new AsyncTask<Void, Void, Bitmap>() {

            @Override
            protected Bitmap doInBackground(Void... params) {
                return getNonCachedBitmap(artistName, reqWidth, reqHeight);
            }

            @Override
            protected void onPostExecute(Bitmap result) {
                ImageView view11 = viewRef.get();
                if (view11 != null && viewTag == view11.getTag()) {
                    setBitmap(result, view11);
                }
            }
        }.execute();


    }

    private void setBitmap(Bitmap bitmap, ImageView imageView) {
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        TransitionDrawable transitionDrawable = new TransitionDrawable(ArtworkHelper.getDefaultArtworkDrawable(mContext), BitmapHelper.createBitmapDrawable(mContext, bitmap));
        imageView.setImageDrawable(transitionDrawable);
        transitionDrawable.startTransition();
    }


    public Bitmap downloadImage(final Context context, final String artistName, int reqWidth, int reqHeight) throws IOException {
        if (!Connectivity.isConnected(context) || !Connectivity.isWifi(context)) {

            throw new IOException("not connected to wifi");
        }

        if (mUnavailableList.contains(artistName)) {
            return null;
        }



        retrofit2.Response<ArtistInfo> response = mLastFmService.getArtistInfo(artistName).execute();
        final ArtistInfo info = response.body();
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
                    saveAndCache(info.getMbid(), artistName, bitmap);
                    return bitmap;
                } else {
                    mUnavailableList.add(artistName);
                }
            }
        }
        return null;


    }


}

