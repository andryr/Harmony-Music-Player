package com.andryr.musicplayer.lastfm;

import com.squareup.moshi.Json;

import java.util.List;

/**
 * Created by Andry on 18/01/16.
 */
public class Artist {

    String name;
    String mbid;
    @Json(name = "image")
    List<Image> imageList;

    public String getName() {
        return name;
    }

    public String getMbid() {
        return mbid;
    }

    public List<Image> getImageList() {
        return imageList;
    }
}
