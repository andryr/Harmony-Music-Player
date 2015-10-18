package com.andryr.musicplayer.musicbrainz;

public class MBRelation extends MBObject {
    private String type;
    private String target;

    public MBRelation(String id, String type, String target) {
        super(id);
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


