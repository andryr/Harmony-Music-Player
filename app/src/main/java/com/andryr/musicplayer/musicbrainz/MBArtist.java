package com.andryr.musicplayer.musicbrainz;

import java.util.ArrayList;
import java.util.List;

public class MBArtist implements MBEntity {
    private String id;
    private String name;

    private List<MBRelation> relationList = new ArrayList<>();

    public MBArtist(String id, String name) {
        super();
        this.id = id;
        this.name = name;
    }

    public void addRelation(MBRelation relation) {
        synchronized (relationList) {
            relationList.add(relation);

        }
    }

    @Override
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public List<MBRelation> getRelationList() {
        return relationList;
    }

    public List<MBRelation> getRelationList(String type) {
        List<MBRelation> relations = new ArrayList<>();
        for (MBRelation r : relationList) {
            if (r.getType().equals(type)) {
                relations.add(r);
            }
        }
        return relations;
    }

}
