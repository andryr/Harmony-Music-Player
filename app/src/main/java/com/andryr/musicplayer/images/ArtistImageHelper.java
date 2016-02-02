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

package com.andryr.musicplayer.images;

import android.content.Context;
import android.graphics.drawable.Drawable;

import com.andryr.musicplayer.R;

/**
 * Created by Andry on 01/02/16.
 */
public class ArtistImageHelper {

    private static Drawable sDefaultArtistImage;
    private static Drawable sDefaultArtistThumb;

    public static Drawable getDefaultArtistImage(Context c) {
        if (sDefaultArtistImage == null) {
            sDefaultArtistImage = c.getResources().getDrawable(R.drawable.default_artist_image);

        }
        return sDefaultArtistImage.getConstantState().newDrawable(c.getResources()).mutate();
    }

    public static Drawable getDefaultArtistThumb(Context c) {
        if (sDefaultArtistThumb == null) {
            sDefaultArtistThumb = c.getResources().getDrawable(R.drawable.default_artist_thumb);

        }
        return sDefaultArtistThumb.getConstantState().newDrawable(c.getResources()).mutate();
    }
}
