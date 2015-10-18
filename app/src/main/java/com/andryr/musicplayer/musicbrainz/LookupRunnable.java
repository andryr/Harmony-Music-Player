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
public class LookupRunnable extends RequestRunnable {
    private MB.EntityType mEntityType;
    private String mMBID;

    public LookupRunnable(MB.EntityType entityType, String mbid, Handler handler, RequestListener listener) {
        super(handler, listener);
        mEntityType = entityType;
        mMBID = mbid;
    }

    @Override
    public void run() {
        List<MBRelation> result = null;
        try {
            URL url = new URL("http://musicbrainz.org/ws/2/"+mEntityType+"/"
                    + mMBID + "?inc=url-rels");
            HttpURLConnection connection = (HttpURLConnection) url
                    .openConnection();
            connection.setRequestProperty("User-Agent", "MusicPlayer/0.1");

            connection.setDoInput(true);
            connection.connect();
            Log.e("e", "code update : " + String.valueOf(connection.getResponseCode()));

            Document doc = Jsoup.parse(connection.getInputStream(), null, "",
                    Parser.xmlParser());

            result = parseRelationList(doc);
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
                list.add(new MBRelation(id,type,target));
            }
        }

        return list;

    }


}
