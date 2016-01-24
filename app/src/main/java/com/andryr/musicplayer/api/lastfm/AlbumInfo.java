package com.andryr.musicplayer.api.lastfm;

import com.squareup.moshi.Json;

import java.util.List;

/**
 * Created by Andry on 23/01/16.
 */
public class AlbumInfo {
    private Album album;


    public Album getAlbum() {
        return album;
    }

    public static class Album {
        private String name;
        private String artist;

        private String mbid;

        @Json(name = "image")
        private List<Image> imageList;

        public String getName() {
            return name;
        }

        public String getArtist() {
            return artist;
        }

        public String getMbid() {
            return mbid;
        }

        public List<Image> getImageList() {
            return imageList;
        }
    }
}
