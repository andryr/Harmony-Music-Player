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

package com.andryr.musicplayer.loaders;

import android.content.Context;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

/**
 * Created by Andry on 01/02/16.
 */
public class GenreSongLoader extends SongLoader {
    private final long mGenreId;

    public GenreSongLoader(Context context, long genreId) {
        super(context);
        mGenreId = genreId;
    }

    @Override
    protected Uri getContentUri() {
        return MediaStore.Audio.Genres.Members.getContentUri(
                "external", mGenreId);
    }
}
