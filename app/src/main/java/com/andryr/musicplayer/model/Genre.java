package com.andryr.musicplayer.model;

public class Genre {
    private long id;
    private String name;

    public Genre(long id, String name) {
        super();
        this.id = id;
        this.name = name;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }


}
