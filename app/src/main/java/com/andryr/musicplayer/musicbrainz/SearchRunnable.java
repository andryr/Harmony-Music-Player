package com.andryr.musicplayer.musicbrainz;

import android.os.Handler;
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

/**
 * Created by Andry on 18/10/15.
 */
public class SearchRunnable extends RequestRunnable {
    private MB.EntityType mEntityType;
    private String mQuery;

    public SearchRunnable(MB.EntityType entityType, String query, Handler handler, RequestListener listener) {
        super(handler, listener);
        mEntityType = entityType;
        mQuery = query;
    }

    @Override
    public void run() {
        List<? extends MBObject> result = null;

        try {

            URL url = new URL("http://musicbrainz.org/ws/2/"+mEntityType+"/?query="
                    + URLEncoder.encode(mQuery, "UTF-8"));
            HttpURLConnection connection = (HttpURLConnection) url
                    .openConnection();

            connection.setRequestProperty("User-Agent", "MusicPlayer/0.1");

            connection.setDoInput(true);
            connection.connect();
            Log.e("e", "code search : " + String.valueOf(connection.getResponseCode()));
            Document doc = Jsoup.parse(connection.getInputStream(), null, "",
                    Parser.xmlParser());
            switch (mEntityType)
            {
                case Artist:
                    result = parseArtistList(doc);
                    break;
            }





        } catch (IOException e) {
            // TODO Auto-generated catch block
            Log.e("io", "io", e);

        }

        if(result != null)
        {
            onRequestResult(result);
        }
        else
        {
            onRequestError();

        }

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
}
