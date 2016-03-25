package org.oucho.musicplayer.model;

import android.provider.MediaStore;

public class Album {

    private final long id;
    private final String albumName;
    private final String artistName;
    private final int year;
    private final int trackCount;

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
