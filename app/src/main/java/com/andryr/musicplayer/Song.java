package com.andryr.musicplayer;

import android.provider.MediaStore;

public class Song {
    private long id;
    private String title;
    private String artist;
    private String album;
    private long albumId;

    public Song(long id, String title, String artist, String album, long albumId) {
        super();
        this.id = id;
        this.title = title == null ? MediaStore.UNKNOWN_STRING : title;
        this.artist = artist == null ? MediaStore.UNKNOWN_STRING : artist;
        this.album = album == null ? MediaStore.UNKNOWN_STRING : album;
        this.albumId = albumId;
    }

    public long getId() {
        return id;
    }

    public String getAlbum() {
        return album;
    }

    public String getTitle() {
        return title;
    }

    public String getArtist() {
        return artist;
    }

    public long getAlbumId() {
        return albumId;
    }

}
