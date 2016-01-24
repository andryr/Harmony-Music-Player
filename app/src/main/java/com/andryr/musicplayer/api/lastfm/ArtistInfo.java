package com.andryr.musicplayer.api.lastfm;

import com.squareup.moshi.Json;

import java.util.List;

/**
 * Created by Andry on 18/01/16.
 */
public class ArtistInfo {

    Artist artist;

    public Artist getArtist() {
        return artist;
    }

    public static class Artist {

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
}
