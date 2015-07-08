package com.andryr.musicplayer.musicbrainz;

public class MBRelation {
    private String type;
    private String target;

    public MBRelation(String type, String target) {
        super();
        this.type = type;
        this.target = target;
    }

    public String getType() {
        return type;
    }

    public String getTarget() {
        return target;
    }


}
