package com.andryr.musicplayer.musicbrainz;

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

    private MB() {
        mWorkQueue = new LinkedBlockingQueue<>();
        mExecutor = new ThreadPoolExecutor(NUMBER_OF_CORES, NUMBER_OF_CORES, KEEP_ALIVE_TIME, KEEP_ALIVE_TIME_UNIT, mWorkQueue);
        mHandler = new Handler(Looper.getMainLooper());
    }

    public synchronized static MB getInstance() {
        if (sInstance == null) {
            sInstance = new MB();
        }
        return sInstance;
    }

    public static MBRelation getFirstOfType(String type, List<MBRelation> relations) {
        for (MBRelation rel : relations) {
            if (rel.getType().equals(type)) {
                return rel;
            }

        }
        return null;
    }

    private static List<MBArtist> parseArtistList(Document doc) {
        if (doc == null) {
            return null;
        }

        ArrayList<MBArtist> list = new ArrayList<>();
        Elements elems = doc.getElementsByTag("artist");

        for (Element e : elems) {
            String id = e.attr("id");
            Element nameElem = e.getElementsByTag("name").first();
            if (nameElem != null) {
                String name = nameElem.text();
                list.add(new MBArtist(id, name));
            }
        }
        return list;
    }

    private static List<MBRelation> parseRelationList(Document doc) {
        if (doc == null) {
            return null;
        }

        ArrayList<MBRelation> list = new ArrayList<>();

        Elements elems = doc.getElementsByTag("relation");
        for (Element e : elems) {
            String type = e.attr("type");
            Element targetElem = e.getElementsByTag("target").first();
            if (targetElem != null) {
                String id = targetElem.attr("id");
                String target = targetElem.text();
                list.add(new MBRelation(id, type, target));
            }
        }

        return list;

    }

    public List<? extends MBObject> search(EntityType entityType, String query) throws IOException {
        List<? extends MBObject> result = null;

        URL url = new URL("http://musicbrainz.org/ws/2/" + entityType + "/?query="
                + URLEncoder.encode(query, "UTF-8"));
        HttpURLConnection connection = (HttpURLConnection) url
                .openConnection();

        connection.setRequestProperty("User-Agent", "MusicPlayer/0.1");

        connection.setDoInput(true);
        connection.connect();
        Log.e("e", "code search : " + String.valueOf(connection.getResponseCode()));
        Document doc = Jsoup.parse(connection.getInputStream(), null, "",
                Parser.xmlParser());
        switch (entityType) {
            case Artist:
                result = parseArtistList(doc);
                break;
        }
        return result;
    }

    public void search(final EntityType entityType, final String query, final RequestListener listener) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                List<? extends MBObject> result = null;

                try {


                    result = search(entityType, query);


                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    Log.e("io", "io", e);

                }

                if (result != null) {
                    onRequestResult(result, listener);
                } else {
                    onRequestError(listener);

                }
            }
        });
    }

    public List<MBRelation> getRelations(EntityType entityType, String mbid) throws IOException {
        URL url = new URL("http://musicbrainz.org/ws/2/" + entityType + "/"
                + mbid + "?inc=url-rels");
        HttpURLConnection connection = (HttpURLConnection) url
                .openConnection();
        connection.setRequestProperty("User-Agent", "MusicPlayer/0.1");

        connection.setDoInput(true);
        connection.connect();
        Log.e("e", "code update : " + String.valueOf(connection.getResponseCode()));

        Document doc = Jsoup.parse(connection.getInputStream(), null, "",
                Parser.xmlParser());

        return parseRelationList(doc);
    }

    public void getRelations(final EntityType entityType, final String mbid, final RequestListener listener) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                List<MBRelation> result = null;
                try {
                    result = getRelations(entityType, mbid);
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    Log.e("io", "io", e);
                }

                if (result != null) {
                    onRequestResult(result, listener);
                } else {
                    onRequestError(listener);
                }
            }
        });
    }

    private void onRequestResult(List<? extends MBObject> result, RequestListener listener) {
        if (listener != null) {
            listener.onRequestResult(result);
        }
    }

    private void onRequestError(RequestListener listener) {
        if (listener != null) {
            listener.onRequestError();
        }

    }

    public enum EntityType {
        Artist("artist");

        private String name = "";

        EntityType(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    public interface RequestListener {
        void onRequestResult(List<? extends MBObject> result);

        void onRequestError();
    }


}
