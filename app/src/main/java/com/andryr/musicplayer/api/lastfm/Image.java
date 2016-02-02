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

/**
 * Created by Andry on 18/01/16.
 */
public class Image {
    @Json(name = "#text") String url;
    String size;

    public String getUrl() {
        return url;
    }

    public String getSize() {
        return size;
    }
}
