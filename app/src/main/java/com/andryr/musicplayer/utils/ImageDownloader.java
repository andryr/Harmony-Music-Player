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

package com.andryr.musicplayer.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.andryr.musicplayer.images.BitmapHelper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

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

    private OkHttpClient mHttpClient = new OkHttpClient();


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

    private Bitmap decode(byte[] data) {
        return BitmapFactory.decodeByteArray(data, 0, data.length);

    }

    private byte[] getData(String url) throws IOException {


        Request request = new Request.Builder().url(url).get().build();


        Response response = mHttpClient.newCall(request).execute();
        if (response.code() == 200) {
            return response.body().bytes();
        }
        return null;
    }

    public Bitmap download(String url, int reqWidth, int reqHeight) throws IOException {
        byte[] data = getData(url);
        if (data != null) {
            return BitmapHelper.decode(data, reqWidth, reqHeight);
        }
        return null;
    }

    public Bitmap download(String url) throws IOException {
        byte[] data = getData(url);
        if (data != null) {
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
                        onError(listener, new NullPointerException("bitmap is null"));
                    }


                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    Log.e("io", "io", e);
                    onError(listener, e);
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
                        onError(listener, new NullPointerException("bitmap is null"));
                    }


                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    Log.e("io", "io", e);
                    onError(listener, e);
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

    private void onError(final DownloadListener listener, final Throwable t) {
        if (listener != null) {

            mHandler.post(new Runnable() {
                @Override
                public void run() {

                    listener.onError(t);

                }
            });
        }
    }

    public interface DownloadListener {
        void onDownloadComplete(Bitmap bitmap);

        void onError(Throwable t);
    }
}
