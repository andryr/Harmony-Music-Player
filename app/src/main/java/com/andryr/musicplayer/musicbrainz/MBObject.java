package com.andryr.musicplayer.musicbrainz;

public abstract class MBObject {
    private String id;

    public MBObject(String id)
    {
        this.id = id;
    }

    public String getId()
    {
        return id;
    }
}
