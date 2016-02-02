/*
 * Copyright 2016 andryr
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.andryr.musicplayer.api.lastfm;

import com.squareup.moshi.Json;

import java.util.List;

/**
 * Created by Andry on 18/01/16.
 */
public class ArtistInfo {

    Artist artist;

    public Artist getArtist() {
        return artist;
    }

    public static class Artist {

        String name;
        String mbid;
        @Json(name = "image")
        List<Image> imageList;

        public String getName() {
            return name;
        }

        public String getMbid() {
            return mbid;
        }

        public List<Image> getImageList() {
            return imageList;
        }
    }
}
