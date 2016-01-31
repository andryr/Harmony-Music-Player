package com.andryr.musicplayer.images;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.widget.ImageView;

import com.andryr.musicplayer.animation.TransitionDrawable;

import java.lang.ref.WeakReference;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by Andry on 23/01/16.
 */
abstract public class BitmapCache<K> {


    private static final int KEEP_ALIVE_TIME = 1;
    private static final TimeUnit KEEP_ALIVE_TIME_UNIT = TimeUnit.SECONDS;
    private static int NUMBER_OF_CORES =
            Runtime.getRuntime().availableProcessors();

    private LinkedBlockingQueue<Runnable> mWorkQueue;
    private ThreadPoolExecutor mExecutor;
    private Handler mHandler;
    private Drawable mDefaultDrawable;

    public BitmapCache() {
        mWorkQueue = new LinkedBlockingQueue<>();
        mExecutor = new ThreadPoolExecutor(NUMBER_OF_CORES, NUMBER_OF_CORES, KEEP_ALIVE_TIME, KEEP_ALIVE_TIME_UNIT, mWorkQueue);
        mHandler = new Handler(Looper.getMainLooper());
    }

    private static boolean hasRequiredSize(Bitmap bitmap, int reqWidth, int reqHeight) {
        return bitmap != null && bitmap.getWidth() >= reqWidth && bitmap.getHeight() >= reqHeight;
    }

    public Bitmap getBitmap(K key, int w, int h) {
        Bitmap b = getCachedBitmap(key, w, h);
        if (b != null) {
            return b;
        }

        b = retrieveBitmap(key, w, h);
        if (b != null) {
            cacheBitmap(key, b);
        }

        return b;
    }

    abstract public Bitmap getCachedBitmap(K key, int reqWidth, int reqHeight);

    abstract protected Bitmap retrieveBitmap(K key, int reqWidth, int reqHeight);

    abstract protected void cacheBitmap(K key, Bitmap bitmap);

    abstract protected Bitmap getDefaultBitmap();

    protected Drawable getDefaultDrawable(Context context) {
        if (mDefaultDrawable == null) {
            mDefaultDrawable = BitmapHelper.createBitmapDrawable(context, getDefaultBitmap());
        }
        return mDefaultDrawable;
    }

    public void loadBitmap(final K key, ImageView view, final int reqWidth, final int reqHeight, final Drawable placeholder) {
        Context context = view.getContext();

        Bitmap b = getCachedBitmap(key, reqWidth, reqHeight);
        if (b != null) {
            view.setScaleType(ImageView.ScaleType.CENTER_CROP);
            view.setImageBitmap(b);
            if (hasRequiredSize(b, reqWidth, reqHeight)) {
                return; // si l'image a une taille satisfaisante, pas besoin d'en charger une autre
            }
        }

        if(placeholder != null) {
            view.setImageDrawable(placeholder);
        }
        else {
            view.setScaleType(ImageView.ScaleType.FIT_CENTER);
            view.setImageDrawable(getDefaultDrawable(context));
        }

        final Object viewTag = view.getTag();

        final WeakReference<ImageView> viewRef = new WeakReference<>(view);
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                final Bitmap bitmap = retrieveBitmap(key, reqWidth, reqHeight);
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        ImageView view11 = viewRef.get();

                        if (bitmap != null) {
                            cacheBitmap(key, bitmap);
                            if (view11 != null && viewTag == view11.getTag()) {
                                setBitmap(bitmap, view11, placeholder);
                            }
                        }
                    }
                });

            }
        });
    }

    public void loadBitmap(final K key, ImageView view, final int reqWidth, final int reqHeight) {
        loadBitmap(key, view, reqWidth, reqHeight, null);
    }

    public void loadBitmap(final K key, final int w, final int h, final Callback callback) {


        new AsyncTask<Void, Void, Bitmap>() {

            @Override
            protected Bitmap doInBackground(Void... params) {
                return retrieveBitmap(key, w, h);
            }

            @Override
            protected void onPostExecute(Bitmap result) {
                if (result != null) {
                    cacheBitmap(key, result);
                }
                callback.onBitmapLoaded(result);
            }
        }.execute();

    }

    protected void setBitmap(Bitmap bitmap, ImageView imageView, Drawable placeholder) {
        Context context = imageView.getContext();

        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

        Drawable firstDrawable = placeholder != null ? placeholder : getDefaultDrawable(context);

        TransitionDrawable transitionDrawable = new TransitionDrawable(firstDrawable, BitmapHelper.createBitmapDrawable(context, bitmap));
        imageView.setImageDrawable(transitionDrawable);
        transitionDrawable.startTransition();
    }

    abstract public void clear();

    public interface Callback {
        void onBitmapLoaded(Bitmap bitmap);
    }
}
