package com.andryr.musicplayer.musicbrainz;

public class MBArtist extends MBObject {
    private String name;


    public MBArtist(String id, String name) {
        super(id);
        this.name = name;
    }





    public String getName() {
        return name;
    }



}
