package com.andryr.musicplayer.musicbrainz;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;

import android.os.AsyncTask;
import android.util.Log;

public class MB {

    public interface OnSearchCompleteListener {
        void onSearchComplete(List<? extends MBEntity> list);
    }

    public static List<MBArtist> searchArtist(String artistName) {
        try {

            URL url = new URL("http://musicbrainz.org/ws/2/artist/?query="
                    + URLEncoder.encode(artistName, "UTF-8"));
            HttpURLConnection connection = (HttpURLConnection) url
                    .openConnection();

            connection.setRequestProperty("User-Agent", "MusicPlayer/0.1");

            connection.setDoInput(true);
            connection.connect();
            Log.e("e", "code search : " + String.valueOf(connection.getResponseCode()));
            Document doc = Jsoup.parse(connection.getInputStream(), null, "",
                    Parser.xmlParser());
            return parseArtistList(doc);

        } catch (IOException e) {
            // TODO Auto-generated catch block
            Log.e("io", "io", e);
        }
        return null;
    }

    public static void searchArtistAsync(String artistName,
                                         final OnSearchCompleteListener listener) {
        new AsyncTask<String, Void, List<MBArtist>>() {

            @Override
            protected List<MBArtist> doInBackground(String... params) {
                String name = params[0];
                return searchArtist(name);
            }

            @Override
            protected void onPostExecute(List<MBArtist> result) {
                if (listener != null) {
                    listener.onSearchComplete(result);

                }
            }

        }.execute(artistName);
    }

    public static void updateRelations(final MBArtist artist) {
        if (artist == null) {
            return;
        }

        try {
            URL url = new URL("http://musicbrainz.org/ws/2/artist/"
                    + artist.getId() + "?inc=url-rels");
            HttpURLConnection connection = (HttpURLConnection) url
                    .openConnection();
            connection.setRequestProperty("User-Agent", "MusicPlayer/0.1");

            connection.setDoInput(true);
            connection.connect();
            Log.e("e", "code update : " + String.valueOf(connection.getResponseCode()));

            Document doc = Jsoup.parse(connection.getInputStream(), null, "",
                    Parser.xmlParser());

            parseRelationList(doc, artist);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            Log.e("io", "io", e);
        }

    }

    public static void updateRelationsAsync(final MBArtist artist) {
        if (artist == null) {
            return;
        }

        new AsyncTask<Void, Void, Document>() {

            @Override
            protected Document doInBackground(Void... params) {
                try {

                    URL url = new URL("http://musicbrainz.org/ws/2/artist/"
                            + artist.getId() + "?inc=url-rels");
                    HttpURLConnection connection = (HttpURLConnection) url
                            .openConnection();
                    connection.setRequestProperty("User-Agent",
                            "MusicPlayer/0.1");

                    connection.setDoInput(true);
                    connection.connect();
                    Log.e("e", "code update async : " + String.valueOf(connection.getResponseCode()));

                    Document doc = Jsoup.parse(connection.getInputStream(),
                            null, "", Parser.xmlParser());
                    return doc;
                } catch (IOException e) {
                    Log.e("io", "io", e);
                }
                return null;
            }

            @Override
            protected void onPostExecute(Document doc) {
                if (doc != null) {
                    parseRelationList(doc, artist);

                }
            }

        }.execute();

    }

    private static void parseRelationList(Document doc, MBArtist artist) {
        if (doc == null || artist == null) {
            return;
        }

        Elements elems = doc.getElementsByTag("relation");
        for (Element e : elems) {
            String type = e.attr("type");
            Element targetElem = e.getElementsByTag("target").first();
            if (targetElem != null) {
                String target = targetElem.text();
                artist.addRelation(new MBRelation(type, target));
            }
        }

    }

    private static List<MBArtist> parseArtistList(Document doc) {
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
