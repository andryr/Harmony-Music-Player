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
import java.net.MalformedURLException;
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

    public void download(final String url, final OnDownloadCompleteListener listener) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    HttpURLConnection connection = (HttpURLConnection) new URL(
                            url).openConnection();
                    connection.setConnectTimeout(10000);
                    connection.setReadTimeout(30000);
                    connection.setDoInput(true);
                    connection.connect();
                    if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                        InputStream in = connection.getInputStream();
                        byte[] data = inputStreamToByteArray(in);
                        final Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0,
                                data.length);
                        in.close();
                        if (bitmap != null) {

                            if (listener != null) {
                                mHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        listener.onDownloadComplete(bitmap);
                                    }
                                });
                            }
                        }
                    }
                } catch (MalformedURLException e) {
                    // TODO Auto-generated catch block
                    Log.e("io", "io", e);
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    Log.e("io", "io", e);
                }
            }
        });
    }

    public interface OnDownloadCompleteListener {
        void onDownloadComplete(Bitmap bitmap);
    }
}
