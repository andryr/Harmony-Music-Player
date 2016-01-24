package com.andryr.musicplayer.api.lastfm;

import com.squareup.moshi.Json;

/**
 * Created by Andry on 18/01/16.
 */
public class Image {
    @Json(name = "#text") String url;
    String size;

    public String getUrl() {
        return url;
    }

    public String getSize() {
        return size;
    }
}
