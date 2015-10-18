package com.andryr.musicplayer.musicbrainz;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class MB {


    private static final int KEEP_ALIVE_TIME = 1;
    private static final TimeUnit KEEP_ALIVE_TIME_UNIT = TimeUnit.SECONDS;
    private static int NUMBER_OF_CORES =
            Runtime.getRuntime().availableProcessors();
    private static MB sInstance = null;

    private LinkedBlockingQueue<Runnable> mWorkQueue;
    private ThreadPoolExecutor mExecutor;
    private Handler mHandler;

    public enum EntityType {
        Artist ("artist");

        private String name = "";

        EntityType(String name)
        {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }


    private MB() {
        mWorkQueue = new LinkedBlockingQueue<>();
        mExecutor = new ThreadPoolExecutor(NUMBER_OF_CORES, NUMBER_OF_CORES, KEEP_ALIVE_TIME, KEEP_ALIVE_TIME_UNIT, mWorkQueue);
        mHandler = new Handler(Looper.getMainLooper());
    }


    public static MB getInstance() {
        if (sInstance == null) {
            sInstance = new MB();
        }
        return sInstance;
    }

    public void search(EntityType type, String query, RequestRunnable.RequestListener listener)
    {
        mExecutor.execute(new SearchRunnable(type, query, mHandler, listener));
    }

    public void getRelations(EntityType type, String entityName, RequestRunnable.RequestListener listener)
    {
        mExecutor.execute(new LookupRunnable(type, entityName, mHandler, listener));
    }





















}
