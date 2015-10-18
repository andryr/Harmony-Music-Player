package com.andryr.musicplayer.musicbrainz;

import android.os.Handler;

import java.util.List;

/**
 * Created by Andry on 18/10/15.
 */
public abstract class RequestRunnable implements Runnable {

    private Handler mHandler = null;
    private RequestListener mListener = null;
    public RequestRunnable(Handler handler, RequestListener listener) {
        mHandler = handler;
        mListener = listener;
    }

    protected void onRequestResult(final List<? extends MBObject> result) {
        if (mHandler != null && mListener != null) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mListener.onRequestResult(result);
                }
            });
        }
    }

    protected void onRequestError() {
        if (mHandler != null && mListener != null) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mListener.onRequestError();
                }
            });
        }
    }

    public interface RequestListener {
        void onRequestResult(List<? extends MBObject> result);

        void onRequestError();
    }
}
