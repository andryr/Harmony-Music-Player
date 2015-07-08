package com.andryr.musicplayer;

import android.provider.MediaStore;

public class Artist {

    private long id;
    private String name;
    private int albumCount;
    private int trackCount;

    public Artist(long id, String name, int albumCount, int trackCount) {
        super();
        this.id = id;
        this.name = name == null ? MediaStore.UNKNOWN_STRING : name;
        this.albumCount = albumCount;
        this.trackCount = trackCount;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getAlbumCount() {
        return albumCount;
    }

    public int getTrackCount() {
        return trackCount;
    }

}
