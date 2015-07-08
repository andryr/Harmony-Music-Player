package com.andryr.musicplayer;

import android.provider.MediaStore;

public class Album {

    private long id;
    private String name;
    private int trackCount;

    public Album(long id, String name, int trackCount) {
        super();
        this.id = id;
        this.name = name == null ? MediaStore.UNKNOWN_STRING : name;
        this.trackCount = trackCount;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getTrackCount() {
        return trackCount;
    }


}
