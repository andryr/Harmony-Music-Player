package com.andryr.musicplayer.musicbrainz;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.graphics.BitmapCompat;
import android.support.v4.util.LruCache;
import android.util.Log;
import android.widget.ImageView;

import com.andryr.musicplayer.R;
import com.andryr.musicplayer.utils.Connectivity;
import com.andryr.musicplayer.utils.ImageDownloader;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Andry on 17/10/15.
 */
public class ArtistImageUtils {


    final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();
    private static final LruCache<String, Bitmap> sImageCache;
    private static final int KEEP_ALIVE_TIME = 1;
    private static final TimeUnit KEEP_ALIVE_TIME_UNIT = TimeUnit.SECONDS;
    private static BitmapFactory.Options sBitmapOptions = new BitmapFactory.Options();
    private static int NUMBER_OF_CORES =
            Runtime.getRuntime().availableProcessors();
    private static ArtistImageUtils sInstance = null;

    static {
        sBitmapOptions.inScaled = false;
        sBitmapOptions.inDither = false;
        sBitmapOptions.inPreferredConfig = Bitmap.Config.ARGB_8888;

        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);

        final int cacheSize = maxMemory / 8;

        sImageCache = new LruCache<String, Bitmap>(cacheSize) {


            @Override
            protected int sizeOf(String key, Bitmap bitmap) {

                return BitmapCompat.getAllocationByteCount(bitmap) / 1024;
            }
        };
    }

    private LinkedBlockingQueue<Runnable> mWorkQueue;
    private ThreadPoolExecutor mExecutor;
    private Handler mHandler;

    private ArtistImageUtils() {
        mWorkQueue = new LinkedBlockingQueue<>();
        mExecutor = new ThreadPoolExecutor(NUMBER_OF_CORES, NUMBER_OF_CORES, KEEP_ALIVE_TIME, KEEP_ALIVE_TIME_UNIT, mWorkQueue);
        mHandler = new Handler(Looper.getMainLooper());
    }

    public static ArtistImageUtils getInstance() {
        if (sInstance == null) {
            sInstance = new ArtistImageUtils();
        }
        return sInstance;
    }

    private static boolean isFromWikimedia(String url) {
        return url != null && url.startsWith("https://commons.wikimedia.org");
    }

    private static String getWikimediaImageUrl(String url) {
        if (url == null) {
            return null;
        }

        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            synchronized (md) {
                md.reset();
                Pattern p = Pattern.compile("File:([a-z0-9A-Z_\\-.]+)");
                Matcher m = p.matcher(url);
                if (m.find()) {
                    String filename = m.group(1).replace(' ', '_');
                    Log.e("ee", "file : " + filename);
                    String md5 = new String(bytesToHex(md.digest(filename
                            .getBytes("UTF-8"))));

                    Locale l = Locale.US;
                    String hash1 = md5.substring(0, 1).toLowerCase(l);
                    String hash2 = md5.substring(0, 2).toLowerCase(l);

                    return "https://upload.wikimedia.org/wikipedia/commons/"
                            + hash1 + "/" + hash2 + "/" + filename;
                }
            }
        } catch (NoSuchAlgorithmException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    private static void save(Context context, String mbid, String artistName, Bitmap image) {
        ArtistImageDbHelper dbHelper = new ArtistImageDbHelper(context);

        dbHelper.insertOrUpdate(mbid, artistName, image);
        dbHelper.close();

    }

    public static void clearMemoryCache() {
        synchronized (sImageCache) {
            sImageCache.evictAll();
        }
    }

    public static void clearDbCache(Context context) {
        ArtistImageDbHelper dbHelper = new ArtistImageDbHelper(context);
        dbHelper.recreate();
        dbHelper.close();
    }

    public void loadArtistImage(String artistName, ImageView view) {
        if (artistName == null || artistName.length() == 0) {
            return;
        }

        Bitmap b = sImageCache.get(artistName);
        if (b != null) {

            b = getArtistFromDb(view.getContext(), artistName);
            sImageCache.put(artistName, b);
        }

        if (b != null) {
            view.setImageBitmap(b);
            return;
        }

        //loadImageFromMB(artistName, view);


    }

    private Bitmap getArtistFromDb(Context context,
                                   String artistName) {


        ArtistImageDbHelper dbHelper = new ArtistImageDbHelper(context);
        Bitmap b = dbHelper.getArtistImage(artistName);
        dbHelper.close();
        return b;
    }

    public void downloadImage(final Context context, final String artistName, final ImageDownloadListener listener) {
        if (!Connectivity.isConnected(context) /*|| !Connectivity.isWifi(context)*/) {
            if (listener != null) {
                listener.onError(ErrorType.DownloadFailed);
            }
            return;
        }

        final MB mb = MB.getInstance();
        final Resources res = context.getResources();
        final int reqWidth = res.getDimensionPixelSize(R.dimen.artist_image_req_width);
        final int reqHeight = res.getDimensionPixelSize(R.dimen.artist_image_req_height);
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    List<? extends MBObject> searchResults = mb.search(MB.EntityType.Artist, artistName);
                    if (searchResults != null && searchResults.size() > 0) {
                        final MBArtist artist = (MBArtist) searchResults.get(0);
                        List<MBRelation> relations = mb.getRelations(MB.EntityType.Artist, artist.getId());
                        if (relations != null) {
                            MBRelation rel = MB.getFirstOfType("image", relations);
                            String imgUrl = rel != null ? rel.getTarget() : null;


                            if (isFromWikimedia(imgUrl)) {
                                imgUrl = getWikimediaImageUrl(imgUrl);
                            }

                            if (imgUrl != null) {
                                final Bitmap bitmap = ImageDownloader.getInstance().download(imgUrl, reqWidth, reqHeight);
                                if (bitmap != null) {
                                    synchronized (sImageCache) {
                                        sImageCache.put(artistName, bitmap);
                                    }

                                    mHandler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            save(context, artist.getId(), artistName, bitmap);
                                            listener.onDownloadComplete(bitmap);
                                        }
                                    });
                                    return;
                                }
                            }
                        }
                    }
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            listener.onError(ErrorType.NotFound);
                        }
                    });

                } catch (IOException e) {
                    e.printStackTrace();
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            listener.onError(ErrorType.DownloadFailed);
                        }
                    });
                }

            }
        });


    }

    public enum ErrorType {
        NotFound,
        DownloadFailed,
    }


    public interface ImageDownloadListener {
        void onDownloadComplete(Bitmap bitmap);

        void onError(ErrorType errorType);
    }

}
