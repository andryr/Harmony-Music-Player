package com.andryr.musicplayer.musicbrainz;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by Andry on 18/10/15.
 */
public class ImageDownloader {

    private static final int KEEP_ALIVE_TIME = 1;
    private static final TimeUnit KEEP_ALIVE_TIME_UNIT = TimeUnit.SECONDS;
    private static int NUMBER_OF_CORES =
            Runtime.getRuntime().availableProcessors();
    private static ImageDownloader sInstance = null;

    private LinkedBlockingQueue<Runnable> mWorkQueue;
    private ThreadPoolExecutor mExecutor;
    private Handler mHandler;


    private ImageDownloader() {
        mWorkQueue = new LinkedBlockingQueue<>();
        mExecutor = new ThreadPoolExecutor(NUMBER_OF_CORES, NUMBER_OF_CORES, KEEP_ALIVE_TIME, KEEP_ALIVE_TIME_UNIT, mWorkQueue);
        mHandler = new Handler(Looper.getMainLooper());
    }

    public static ImageDownloader getInstance() {
        if (sInstance == null) {
            sInstance = new ImageDownloader();
        }
        return sInstance;
    }

    private static byte[] inputStreamToByteArray(InputStream in)
            throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int r;
        while ((r = in.read()) != -1) {
            out.write(r);
        }
        return out.toByteArray();
    }

    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    private Bitmap decode(byte[] data, int reqWidth, int reqHeight)
    {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeByteArray(data, 0, data.length, options);

        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
        options.inJustDecodeBounds = false;

        return BitmapFactory.decodeByteArray(data, 0, data.length, options);
    }

    private Bitmap decode(byte[] data)
    {
        return BitmapFactory.decodeByteArray(data, 0, data.length);

    }

    private byte[] getData(String url) throws IOException
    {

        HttpURLConnection connection = (HttpURLConnection) new URL(
                url).openConnection();
        connection.setConnectTimeout(10000);
        connection.setReadTimeout(30000);
        connection.setDoInput(true);
        connection.connect();
        if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
            InputStream in = connection.getInputStream();
            byte[] data = inputStreamToByteArray(in);
            in.close();



            return data;
        }
        return null;
    }

    public Bitmap download(String url, int reqWidth, int reqHeight) throws IOException {
        byte[] data = getData(url);
        if(data != null)
        {
            return decode(data, reqWidth, reqHeight);
        }
        return null;
    }

    public Bitmap download(String url) throws IOException {
        byte[] data = getData(url);
        if(data != null)
        {
            return decode(data);
        }
        return null;
    }




    public void download(final String url, final DownloadListener listener) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                try {

                    Bitmap bitmap = download(url);

                    if (bitmap != null) {
                        onDownloadComplete(bitmap, listener);
                    } else {
                        onError(listener);
                    }


                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    Log.e("io", "io", e);
                    onError(listener);
                }

            }
        });
    }

    public void download(final String url, final int reqWidth, final int reqHeight, final DownloadListener listener) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                try {

                    Bitmap bitmap = download(url, reqWidth, reqHeight);

                    if (bitmap != null) {
                        onDownloadComplete(bitmap, listener);
                    } else {
                        onError(listener);
                    }


                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    Log.e("io", "io", e);
                    onError(listener);
                }

            }
        });
    }

    private void onDownloadComplete(final Bitmap bitmap, final DownloadListener listener) {
        if (listener != null) {

            mHandler.post(new Runnable() {
                @Override
                public void run() {

                    listener.onDownloadComplete(bitmap);

                }
            });
        }
    }

    private void onError(final DownloadListener listener) {
        if (listener != null) {

            mHandler.post(new Runnable() {
                @Override
                public void run() {

                    listener.onError();

                }
            });
        }
    }

    public interface DownloadListener {
        void onDownloadComplete(Bitmap bitmap);

        void onError();
    }
}
