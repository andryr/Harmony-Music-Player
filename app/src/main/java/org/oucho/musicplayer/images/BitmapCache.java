package org.oucho.musicplayer.images;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.widget.ImageView;

import org.oucho.musicplayer.animation.TransitionDrawable;

import java.lang.ref.WeakReference;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


abstract public class BitmapCache<K> {


    private static final int KEEP_ALIVE_TIME = 1;
    private static final TimeUnit KEEP_ALIVE_TIME_UNIT = TimeUnit.SECONDS;
    private static int NUMBER_OF_CORES =
            Runtime.getRuntime().availableProcessors();

    private LinkedBlockingQueue<Runnable> mWorkQueue;
    private ThreadPoolExecutor mExecutor;
    private Handler mHandler;

    public BitmapCache() {
        mWorkQueue = new LinkedBlockingQueue<>();
        mExecutor = new ThreadPoolExecutor(NUMBER_OF_CORES, NUMBER_OF_CORES, KEEP_ALIVE_TIME, KEEP_ALIVE_TIME_UNIT, mWorkQueue);
        mHandler = new Handler(Looper.getMainLooper());
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



    public void loadBitmap(final K key, ImageView view, final int reqWidth, final int reqHeight, final Drawable placeholder, final boolean smoothTransition) {
        Context context = view.getContext();

        Bitmap b = getCachedBitmap(key, reqWidth, reqHeight);
        if (b != null) {
            view.setScaleType(ImageView.ScaleType.CENTER_CROP);
            view.setImageBitmap(b);
            if (hasRequiredSize(b, reqWidth, reqHeight)) {
                return; // si l'image a une taille satisfaisante, pas besoin d'en charger une autre
            }
        }

        if (placeholder != null) {
            view.setImageDrawable(placeholder);
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
                        if (view11 != null && viewTag == view11.getTag()) {
                            setBitmap(bitmap, view11, placeholder, reqWidth, reqHeight, smoothTransition);
                        }
                        if (bitmap != null) {
                            cacheBitmap(key, bitmap);
                        }
                    }
                });

            }
        });
    }

    public void loadBitmap(final K key, ImageView view, final int reqWidth, final int reqHeight, final Drawable placeholder) {
        loadBitmap(key, view, reqWidth, reqHeight, placeholder, true);
    }

    public void loadBitmap(final K key, ImageView view, final int reqWidth, final int reqHeight) {
        loadBitmap(key, view, reqWidth, reqHeight, null, false);
    }

    private static boolean hasRequiredSize(Bitmap bitmap, int reqWidth, int reqHeight) {
        return bitmap != null && bitmap.getWidth() >= reqWidth && bitmap.getHeight() >= reqHeight;
    }

    abstract protected Drawable getDefaultDrawable(Context context, int reqWidth, int reqHeight);

    protected void setBitmap(Bitmap bitmap, ImageView imageView, Drawable placeholder, int reqWidth, int reqHeight, boolean smoothTransition) {
        Context context = imageView.getContext();

        if(bitmap == null) {
            imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
            imageView.setImageDrawable(placeholder == null ? getDefaultDrawable(context, reqWidth, reqHeight) : placeholder);
            return;
        }
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);


        if (smoothTransition) {
            Drawable firstDrawable = placeholder != null ? placeholder : getDefaultDrawable(context, reqWidth, reqHeight);
            TransitionDrawable transitionDrawable = new TransitionDrawable(firstDrawable, BitmapHelper.createBitmapDrawable(context, bitmap));
            imageView.setImageDrawable(transitionDrawable);
            transitionDrawable.startTransition();
        } else {
            imageView.setImageBitmap(bitmap);
        }
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

    abstract public void clear();

    public interface Callback {
        void onBitmapLoaded(Bitmap bitmap);
    }
}