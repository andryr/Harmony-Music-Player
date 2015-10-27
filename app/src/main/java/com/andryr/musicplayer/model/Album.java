package com.andryr.musicplayer.model;

import android.provider.MediaStore;

public class Album {

    private long id;
    private String albumName;
    private String artistName;
    private int year;
    private int trackCount;

    public Album(long id, String albumName, String artistName, int year, int trackCount) {
        super();
        this.id = id;
        this.albumName = albumName == null ? MediaStore.UNKNOWN_STRING : albumName;
        this.artistName = artistName == null ? MediaStore.UNKNOWN_STRING : artistName;
        this.year = year;
        this.trackCount = trackCount;
    }

    public long getId() {
        return id;
    }

    public String getAlbumName() {
        return albumName;
    }

    public String getArtistName() {
        return artistName;
    }

    public int getYear() {
        return year;
    }

    public int getTrackCount() {
        return trackCount;
    }


}
