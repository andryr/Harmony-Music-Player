package com.andryr.musicplayer.lastfm;

import java.util.List;

/**
 * Created by Andry on 18/01/16.
 */
public class ArtistInfo {

    Artist artist;

    public String getName() {
        return artist.getName();
    }

    public String getMbid() {
        return artist.getMbid();
    }

    public List<Image> getImageList() {
        return artist.getImageList();
    }

    public Artist getArtist() {
        return artist;
    }
}
